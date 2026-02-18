package server.game

import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.data.QuestionBank
import server.model.*
import server.network.ClientHandler
import java.util.UUID

class PvPGameSession(
    private val client1: ClientHandler,
    private val client2: ClientHandler,
    private val config: CreateTriviaMsg
) {
    private val json = Json { ignoreUnknownKeys = true }
    private val gameId = UUID.randomUUID().toString()
    private val questions = QuestionBank.get(config.questions, config.categories, config.difficulty)
    
    private var currentIndex = 0
    private var questionStartTime = 0L
    
    private var score1 = 0
    private var streak1 = 0
    private var correctCount1 = 0
    private var answer1: Int? = null
    private var time1: Long = 0
    
    private var score2 = 0
    private var streak2 = 0
    private var correctCount2 = 0
    private var answer2: Int? = null
    private var time2: Long = 0

    suspend fun start() {
        client1.setPvPSession(this)
        client2.setPvPSession(this)
        
        println("🆚 Partida PVP [$gameId]: ${client1.playerName} vs ${client2.playerName}")
        
        client1.send("PVP_MATCHED", json.encodeToString(mapOf("opponent" to client2.playerName)))
        client2.send("PVP_MATCHED", json.encodeToString(mapOf("opponent" to client1.playerName)))
        
        delay(2000)
        sendNextQuestion()
    }

    private fun sendNextQuestion() {
        if (currentIndex >= questions.size) {
            endGame()
            return
        }
        
        val q = questions[currentIndex]
        questionStartTime = System.currentTimeMillis()
        answer1 = null
        answer2 = null
        time1 = 0
        time2 = 0
        
        val msg = QuestionMsg(
            id             = q.id,
            category       = q.category,
            difficulty     = q.difficulty,
            question       = q.question,
            options        = q.options,
            timeLimit      = config.timeLimit,
            questionNumber = currentIndex + 1,
            totalQuestions = questions.size
        )
        
        client1.send("QUESTION", json.encodeToString(msg))
        client2.send("QUESTION", json.encodeToString(msg))
    }

    suspend fun processAnswer(client: ClientHandler, msg: AnswerMsg) {
        val q = questions.find { it.id == msg.questionId } ?: return
        val elapsed = System.currentTimeMillis() - questionStartTime
        
        if (client.id == client1.id) {
            answer1 = msg.selectedOption
            time1 = elapsed
        } else {
            answer2 = msg.selectedOption
            time2 = elapsed
        }
        
        if (answer1 == null || answer2 == null) return
        
        val isCorrect1 = answer1 == q.correctAnswer
        val isCorrect2 = answer2 == q.correctAnswer
        
        var points1 = 0
        if (isCorrect1) {
            correctCount1++
            streak1++
            
            points1 = when (q.difficulty) {
                Difficulty.FACIL   -> 10
                Difficulty.MEDIA   -> 15
                Difficulty.DIFICIL -> 20
                Difficulty.MIXTA   -> 10
            }
            
            if (time1 < 5_000) points1 += 5
            if (streak1 >= 5) points1 *= 2
            if (isCorrect2 && time1 < time2) points1 += 5
            
            score1 += points1
        } else {
            streak1 = 0
        }
        
        var points2 = 0
        if (isCorrect2) {
            correctCount2++
            streak2++
            
            points2 = when (q.difficulty) {
                Difficulty.FACIL   -> 10
                Difficulty.MEDIA   -> 15
                Difficulty.DIFICIL -> 20
                Difficulty.MIXTA   -> 10
            }
            
            if (time2 < 5_000) points2 += 5
            if (streak2 >= 5) points2 *= 2
            if (isCorrect1 && time2 < time1) points2 += 5
            
            score2 += points2
        } else {
            streak2 = 0
        }
        
        client1.send("ANSWER_RESULT", json.encodeToString(
            AnswerResultMsg(q.id, isCorrect1, q.correctAnswer, points1, q.explanation)
        ))
        
        client2.send("ANSWER_RESULT", json.encodeToString(
            AnswerResultMsg(q.id, isCorrect2, q.correctAnswer, points2, q.explanation)
        ))
        
        val scoreUpdate = ScoreUpdateMsg(listOf(
            PlayerScoreData(client1.playerName, score1, streak1, correctCount1),
            PlayerScoreData(client2.playerName, score2, streak2, correctCount2)
        ))
        
        client1.send("SCORE_UPDATE", json.encodeToString(scoreUpdate))
        client2.send("SCORE_UPDATE", json.encodeToString(scoreUpdate))
        
        currentIndex++
        delay(500)
        sendNextQuestion()
    }

    private fun endGame() {
        val winner = when {
            score1 > score2 -> client1.playerName
            score2 > score1 -> client2.playerName
            else -> null
        }
        
        val gameEnd = GameEndMsg(
            winner = winner,
            finalScores = listOf(
                PlayerScoreData(client1.playerName, score1, streak1, correctCount1),
                PlayerScoreData(client2.playerName, score2, streak2, correctCount2)
            ),
            correctAnswers = mapOf(
                client1.playerName to correctCount1,
                client2.playerName to correctCount2
            )
        )
        
        client1.send("GAME_END", json.encodeToString(gameEnd))
        client2.send("GAME_END", json.encodeToString(gameEnd))
        
        println("🏁 PVP [$gameId] - Ganador: ${winner ?: "Empate"}")
        MatchmakingManager.removeMatch(gameId)
    }
}
