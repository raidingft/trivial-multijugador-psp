package server.game

import kotlinx.coroutines.*
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
    private var currentTurnIsPlayer1 = true
    private var questionProcessed = false
    private var timeoutJob: Job? = null
    
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
        
        println("🆚 PVP [$gameId]: ${client1.playerName} vs ${client2.playerName}")
        println("⚙️ ${config.mode}, ${config.difficulty}, ${questions.size} preguntas")
        
        client1.send("PVP_MATCHED", json.encodeToString(mapOf(
            "opponent" to client2.playerName,
            "mode" to config.mode
        )))
        client2.send("PVP_MATCHED", json.encodeToString(mapOf(
            "opponent" to client1.playerName,
            "mode" to config.mode
        )))
        
        delay(3000)
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
        
        val currentTurnPlayer = when (config.mode) {
            "POR_TURNOS" -> if (currentTurnIsPlayer1) client1.playerName else client2.playerName
            else -> null
        }
        
        val msg = QuestionMsg(
            id               = q.id,
            category         = q.category,
            difficulty       = q.difficulty,
            question         = q.question,
            options          = q.options,
            timeLimit        = config.timeLimit,
            questionNumber   = currentIndex + 1,
            totalQuestions   = questions.size,
            currentTurnPlayer = currentTurnPlayer
        )
        
        questionProcessed = false
        client1.send("QUESTION", json.encodeToString(msg))
        client2.send("QUESTION", json.encodeToString(msg))
        
        // Timeout en el servidor para no quedarse colgado (por si alguien no responde)
        if (config.mode == "SIMULTANEO" || config.mode == "CONTRARRELOJ") {
            timeoutJob?.cancel()
            timeoutJob = kotlinx.coroutines.GlobalScope.launch {
                delay((config.timeLimit + 5) * 1000L)
                if (!questionProcessed) {
                    // Forzar respuesta -1 a quien no haya contestado
                    val q = questions[currentIndex]
                    if (answer1 == null) { answer1 = -1; time1 = (config.timeLimit * 1000).toLong() }
                    if (answer2 == null) { answer2 = -1; time2 = (config.timeLimit * 1000).toLong() }
                    processSimultaneousAnswers(q)
                }
            }
        }
    }

    suspend fun processAnswer(client: ClientHandler, msg: AnswerMsg) {
        val q = questions.find { it.id == msg.questionId } ?: return
        val elapsed = System.currentTimeMillis() - questionStartTime
        
        if (config.mode == "POR_TURNOS") {
            val isPlayer1Turn = (currentTurnIsPlayer1 && client.id == client1.id)
            val isPlayer2Turn = (!currentTurnIsPlayer1 && client.id == client2.id)
            if (!isPlayer1Turn && !isPlayer2Turn) return
        }
        
        if (questionProcessed) return
        
        if (client.id == client1.id) {
            if (answer1 != null) return  // Ya respondió
            answer1 = msg.selectedOption
            time1 = elapsed
        } else {
            if (answer2 != null) return  // Ya respondió
            answer2 = msg.selectedOption
            time2 = elapsed
        }
        
        if (config.mode == "POR_TURNOS") {
            processTurnBasedAnswer(q)
            return
        }
        
        if (config.mode == "CONTRARRELOJ" || config.mode == "SIMULTANEO") {
            // El primero en responder gana la pregunta
            questionProcessed = true
            timeoutJob?.cancel()
            processContrarrelojAnswer(q)
            return
        }
    }

    private suspend fun processContrarrelojAnswer(q: TriviaQuestion) {
        // El jugador que respondió primero gana; el otro recibe sin puntos
        val player1Answered = answer1 != null
        val player2Answered = answer2 != null
        
        val isCorrect1 = if (player1Answered) answer1 == q.correctAnswer else false
        val isCorrect2 = if (player2Answered) answer2 == q.correctAnswer else false
        
        var points1 = 0
        if (isCorrect1) {
            correctCount1++; streak1++
            points1 = when (q.difficulty) {
                Difficulty.FACIL -> 10; Difficulty.MEDIA -> 15
                Difficulty.DIFICIL -> 20; Difficulty.MIXTA -> 10
            }
            if (time1 < 5_000) points1 += 5
            if (streak1 >= 5) points1 *= 2
            score1 += points1
        } else if (player1Answered) streak1 = 0
        
        var points2 = 0
        if (isCorrect2) {
            correctCount2++; streak2++
            points2 = when (q.difficulty) {
                Difficulty.FACIL -> 10; Difficulty.MEDIA -> 15
                Difficulty.DIFICIL -> 20; Difficulty.MIXTA -> 10
            }
            if (time2 < 5_000) points2 += 5
            if (streak2 >= 5) points2 *= 2
            score2 += points2
        } else if (player2Answered) streak2 = 0
        
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
        delay(3000)
        sendNextQuestion()
    }

    private suspend fun processTurnBasedAnswer(q: TriviaQuestion) {
        val isPlayer1 = currentTurnIsPlayer1
        val answer = if (isPlayer1) answer1 else answer2
        val time = if (isPlayer1) time1 else time2
        val isCorrect = answer == q.correctAnswer
        
        var points = 0
        if (isCorrect) {
            if (isPlayer1) { correctCount1++; streak1++ } else { correctCount2++; streak2++ }
            
            points = when (q.difficulty) {
                Difficulty.FACIL -> 10
                Difficulty.MEDIA -> 15
                Difficulty.DIFICIL -> 20
                Difficulty.MIXTA -> 10
            }
            
            if (time < 5_000) points += 5
            if ((if (isPlayer1) streak1 else streak2) >= 5) points *= 2
            
            if (isPlayer1) score1 += points else score2 += points
        } else {
            if (isPlayer1) streak1 = 0 else streak2 = 0
        }
        
        val currentClient = if (isPlayer1) client1 else client2
        currentClient.send("ANSWER_RESULT", json.encodeToString(
            AnswerResultMsg(q.id, isCorrect, q.correctAnswer, points, q.explanation)
        ))
        
        val scoreUpdate = ScoreUpdateMsg(listOf(
            PlayerScoreData(client1.playerName, score1, streak1, correctCount1),
            PlayerScoreData(client2.playerName, score2, streak2, correctCount2)
        ))
        
        client1.send("SCORE_UPDATE", json.encodeToString(scoreUpdate))
        client2.send("SCORE_UPDATE", json.encodeToString(scoreUpdate))
        
        currentTurnIsPlayer1 = !currentTurnIsPlayer1
        currentIndex++
        delay(3000)
        sendNextQuestion()
    }

    private suspend fun processSimultaneousAnswers(q: TriviaQuestion) {
        if (questionProcessed) return
        questionProcessed = true

        val isCorrect1 = answer1 == q.correctAnswer
        val isCorrect2 = answer2 == q.correctAnswer
        
        var points1 = 0
        if (isCorrect1) {
            correctCount1++
            streak1++
            points1 = when (q.difficulty) {
                Difficulty.FACIL -> 10
                Difficulty.MEDIA -> 15
                Difficulty.DIFICIL -> 20
                Difficulty.MIXTA -> 10
            }
            if (time1 < 5_000) points1 += 5
            if (streak1 >= 5) points1 *= 2
            if (isCorrect2 && time1 < time2) points1 += 5
            score1 += points1
        } else streak1 = 0
        
        var points2 = 0
        if (isCorrect2) {
            correctCount2++
            streak2++
            points2 = when (q.difficulty) {
                Difficulty.FACIL -> 10
                Difficulty.MEDIA -> 15
                Difficulty.DIFICIL -> 20
                Difficulty.MIXTA -> 10
            }
            if (time2 < 5_000) points2 += 5
            if (streak2 >= 5) points2 *= 2
            if (isCorrect1 && time2 < time1) points2 += 5
            score2 += points2
        } else streak2 = 0
        
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
        delay(3000)
        sendNextQuestion()
    }

    fun notifyOpponentDisconnected(disconnectedClient: ClientHandler) {
        val rival = if (disconnectedClient.id == client1.id) client2 else client1
        rival.send("OPPONENT_DISCONNECTED", json.encodeToString(
            mapOf("playerName" to disconnectedClient.playerName)
        ))
        timeoutJob?.cancel()
        MatchmakingManager.removeMatch(gameId)
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
        
        println("🏁 PVP [$gameId] - ${winner ?: "Empate"}")
        MatchmakingManager.removeMatch(gameId)
    }
}
