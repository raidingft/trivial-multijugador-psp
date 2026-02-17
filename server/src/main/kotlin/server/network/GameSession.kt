package server.network

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.data.RecordsManager
import server.model.*
import java.util.UUID

class GameSession(
    private val client: ClientHandler,
    private val config: CreateTriviaMsg,
    private val questions: List<TriviaQuestion>,
    private val records: RecordsManager
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val gameId = UUID.randomUUID().toString()

    private var currentIndex = 0
    private var score = 0
    private var streak = 0
    private var correct = 0
    private var questionStartTime = 0L

    // ── Inicio ─────────────────────────────────────────────────────────────

    suspend fun start() {
        println("🎮 Partida iniciada [$gameId] para ${client.playerName} — ${config.mode}, ${questions.size} preguntas")
        sendNextQuestion()
    }

    // ── Enviar pregunta ────────────────────────────────────────────────────

    private fun sendNextQuestion() {
        if (currentIndex >= questions.size) {
            endGame()
            return
        }
        val q = questions[currentIndex]
        questionStartTime = System.currentTimeMillis()

        client.send("QUESTION", json.encodeToString(
            QuestionMsg(
                id         = q.id,
                category   = q.category,
                difficulty = q.difficulty,
                question   = q.question,
                options    = q.options,
                timeLimit  = config.timeLimit
            )
        ))
    }

    // ── Procesar respuesta ─────────────────────────────────────────────────

    suspend fun processAnswer(msg: AnswerMsg) {
        val q = questions.find { it.id == msg.questionId } ?: return
        val elapsed = System.currentTimeMillis() - questionStartTime

        val isCorrect = msg.selectedOption == q.correctAnswer

        // Calcular puntos
        var points = 0
        if (isCorrect) {
            correct++
            streak++

            points = when (q.difficulty) {
                Difficulty.EASY   -> 10
                Difficulty.MEDIUM -> 15   // x1.5
                Difficulty.HARD   -> 20   // x2
                Difficulty.MIXED  -> 10
            }

            // Bonus velocidad < 5 s
            if (elapsed < 5_000) points += 5

            // Racha x2 a partir de 5
            if (streak >= 5) points *= 2

            score += points
        } else {
            streak = 0
        }

        // Resultado
        client.send("ANSWER_RESULT", json.encodeToString(
            AnswerResultMsg(
                questionId    = q.id,
                correct       = isCorrect,
                correctAnswer = q.correctAnswer,
                points        = points,
                explanation   = q.explanation
            )
        ))

        // Score update (partida en solitario: solo el jugador)
        client.send("SCORE_UPDATE", json.encodeToString(
            ScoreUpdateMsg(listOf(
                PlayerScoreData(client.playerName, score, streak)
            ))
        ))

        currentIndex++

        // Pequeña pausa antes de la siguiente pregunta
        delay(500)
        sendNextQuestion()
    }

    // ── Fin de partida ─────────────────────────────────────────────────────

    private fun endGame() {
        val totalAnswered = questions.size
        val won = correct > totalAnswered / 2   // gana si acierta más de la mitad

        records.updateAfterGame(
            playerName = client.playerName,
            score      = score,
            won        = won,
            streak     = streak,
            correct    = correct,
            answered   = totalAnswered
        )

        client.send("GAME_END", json.encodeToString(
            GameEndMsg(
                winner      = if (won) client.playerName else null,
                finalScores = listOf(PlayerScoreData(client.playerName, score, streak)),
                correctAnswers = mapOf(client.playerName to correct)
            )
        ))

        println("🏁 Partida finalizada [$gameId] — ${client.playerName}: $score pts, $correct/$totalAnswered correctas")
    }
}
