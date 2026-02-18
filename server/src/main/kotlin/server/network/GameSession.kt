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
    
    // Estadísticas detalladas
    private val categoryStats = mutableMapOf<Category, Pair<Int, Int>>() // (correct, total)
    private val difficultyStats = mutableMapOf<Difficulty, Pair<Int, Int>>()
    private var totalResponseTime = 0L

    suspend fun start() {
        println("🎮 Partida iniciada [$gameId] para ${client.playerName} — ${config.mode}, ${questions.size} preguntas")
        sendNextQuestion()
    }

    private fun sendNextQuestion() {
        if (currentIndex >= questions.size) {
            endGame()
            return
        }
        val q = questions[currentIndex]
        questionStartTime = System.currentTimeMillis()

        client.send("QUESTION", json.encodeToString(
            QuestionMsg(
                id             = q.id,
                category       = q.category,
                difficulty     = q.difficulty,
                question       = q.question,
                options        = q.options,
                timeLimit      = config.timeLimit,
                questionNumber = currentIndex + 1,
                totalQuestions = questions.size
            )
        ))
    }

    suspend fun processAnswer(msg: AnswerMsg) {
        val q = questions.find { it.id == msg.questionId } ?: return
        val elapsed = System.currentTimeMillis() - questionStartTime
        val isCorrect = msg.selectedOption == q.correctAnswer
        
        // Trackear tiempo
        totalResponseTime += elapsed

        // Trackear categoría
        val catStats = categoryStats.getOrDefault(q.category, Pair(0, 0))
        categoryStats[q.category] = if (isCorrect) {
            Pair(catStats.first + 1, catStats.second + 1)
        } else {
            Pair(catStats.first, catStats.second + 1)
        }
        
        // Trackear dificultad
        val diffStats = difficultyStats.getOrDefault(q.difficulty, Pair(0, 0))
        difficultyStats[q.difficulty] = if (isCorrect) {
            Pair(diffStats.first + 1, diffStats.second + 1)
        } else {
            Pair(diffStats.first, diffStats.second + 1)
        }

        var points = 0
        if (isCorrect) {
            correct++
            streak++

            points = when (q.difficulty) {
                Difficulty.FACIL   -> 10
                Difficulty.MEDIA   -> 15
                Difficulty.DIFICIL -> 20
                Difficulty.MIXTA   -> 10
            }

            if (elapsed < 5_000) points += 5
            if (streak >= 5) points *= 2

            score += points
        } else {
            streak = 0
        }

        client.send("ANSWER_RESULT", json.encodeToString(
            AnswerResultMsg(
                questionId    = q.id,
                correct       = isCorrect,
                correctAnswer = q.correctAnswer,
                points        = points,
                explanation   = q.explanation
            )
        ))

        client.send("SCORE_UPDATE", json.encodeToString(
            ScoreUpdateMsg(listOf(
                PlayerScoreData(client.playerName, score, streak, correct)
            ))
        ))

        currentIndex++
        delay(500)
        sendNextQuestion()
    }

    private fun endGame() {
        val totalAnswered = questions.size
        val won = correct > totalAnswered / 2

        records.updateAfterGame(
            playerName        = client.playerName,
            score             = score,
            won               = won,
            streak            = streak,
            correct           = correct,
            answered          = totalAnswered,
            categoryStats     = categoryStats,
            difficultyStats   = difficultyStats,
            totalResponseTime = totalResponseTime
        )

        client.send("GAME_END", json.encodeToString(
            GameEndMsg(
                winner         = if (won) client.playerName else null,
                finalScores    = listOf(PlayerScoreData(client.playerName, score, streak, correct)),
                correctAnswers = mapOf(client.playerName to correct)
            )
        ))

        println("🏁 Partida finalizada [$gameId] — ${client.playerName}: $score pts, $correct/$totalAnswered correctas")
    }
}
