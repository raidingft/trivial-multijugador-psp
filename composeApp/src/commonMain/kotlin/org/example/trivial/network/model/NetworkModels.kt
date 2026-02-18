package org.example.trivial.network.model

import kotlinx.serialization.Serializable

@Serializable
data class ConnectMsg(val playerName: String)

@Serializable
data class CreateTriviaMsg(
    val mode: String,
    val questions: Int,
    val categories: List<String>,
    val difficulty: String,
    val timeLimit: Int
)

@Serializable
data class AnswerMsg(
    val questionId: Int,
    val selectedOption: Int,
    val timeElapsed: Long
)

@Serializable
data class WelcomeData(val message: String, val playerId: String)

@Serializable
data class CategoryStats(val correct: Int = 0, val total: Int = 0)

@Serializable
data class DifficultyStats(val correct: Int = 0, val total: Int = 0)

@Serializable
data class PlayerRecordData(
    val playerName: String,
    val bestScore: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val maxStreak: Int = 0,
    val totalCorrect: Int = 0,
    val totalAnswered: Int = 0,
    val categoryStats: Map<String, CategoryStats> = emptyMap(),
    val difficultyStats: Map<String, DifficultyStats> = emptyMap(),
    val totalResponseTime: Long = 0,
    val lastPlayed: Long = 0
)

@Serializable
data class RecordsData(val players: Map<String, PlayerRecordData> = emptyMap())

@Serializable
data class QuestionData(
    val id: Int,
    val category: String,
    val difficulty: String,
    val question: String,
    val options: List<String>,
    val timeLimit: Int,
    val questionNumber: Int = 1,
    val totalQuestions: Int = 1
)

@Serializable
data class AnswerResultData(
    val questionId: Int,
    val correct: Boolean,
    val correctAnswer: Int,
    val points: Int,
    val explanation: String
)

@Serializable
data class PlayerScoreData(
    val name: String,
    val score: Int,
    val streak: Int,
    val correctAnswers: Int = 0
)

@Serializable
data class ScoreUpdateData(val players: List<PlayerScoreData>)

@Serializable
data class GameEndData(
    val winner: String?,
    val finalScores: List<PlayerScoreData>,
    val correctAnswers: Map<String, Int>
)

@Serializable
data class ErrorData(val message: String)

sealed class ServerEvent {
    data class Welcome(val data: WelcomeData)           : ServerEvent()
    data class Records(val data: RecordsData)           : ServerEvent()
    data class Question(val data: QuestionData)         : ServerEvent()
    data class AnswerResult(val data: AnswerResultData) : ServerEvent()
    data class ScoreUpdate(val data: ScoreUpdateData)   : ServerEvent()
    data class GameEnd(val data: GameEndData)           : ServerEvent()
    data class Error(val data: ErrorData)               : ServerEvent()
    
    // Eventos PVP
    data object SearchingMatch : ServerEvent()
    data object Waiting : ServerEvent()
    data class PvPMatched(val opponentName: String) : ServerEvent()
    data object MatchmakingCancelled : ServerEvent()
}
