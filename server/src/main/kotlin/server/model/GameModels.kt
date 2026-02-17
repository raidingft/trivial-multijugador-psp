package server.model

import kotlinx.serialization.Serializable

@Serializable
enum class Category {
    HISTORIA, CIENCIA_NATURALEZA, DEPORTES, GEOGRAFIA,
    ARTE_LITERATURA, ENTRETENIMIENTO, TECNOLOGIA, CONOCIMIENTO_GENERAL
}

@Serializable
enum class Difficulty { FACIL, MEDIA, DIFICIL, MIXTA }

@Serializable
enum class GameMode { PVP, PVE }

@Serializable
data class TriviaQuestion(
    val id: Int,
    val category: Category,
    val difficulty: Difficulty,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int,
    val explanation: String = ""
)

@Serializable
data class QuestionsFile(val questions: List<TriviaQuestion>)

@Serializable
data class CreateTriviaMsg(
    val mode: String,
    val questions: Int,
    val categories: List<Category>,
    val difficulty: Difficulty,
    val timeLimit: Int
)

@Serializable
data class AnswerMsg(
    val questionId: Int,
    val selectedOption: Int,
    val timeElapsed: Long
)

@Serializable
data class ConnectMsg(val playerName: String)

@Serializable
data class QuestionMsg(
    val id: Int,
    val category: Category,
    val difficulty: Difficulty,
    val question: String,
    val options: List<String>,
    val timeLimit: Int
)

@Serializable
data class AnswerResultMsg(
    val questionId: Int,
    val correct: Boolean,
    val correctAnswer: Int,
    val points: Int,
    val explanation: String
)

@Serializable
data class PlayerScoreData(val name: String, val score: Int, val streak: Int)

@Serializable
data class ScoreUpdateMsg(val players: List<PlayerScoreData>)

@Serializable
data class GameEndMsg(
    val winner: String?,
    val finalScores: List<PlayerScoreData>,
    val correctAnswers: Map<String, Int>
)

@Serializable
data class WelcomeMsg(val message: String, val playerId: String)

@Serializable
data class ErrorMsg(val message: String)

@Serializable
data class PlayerRecord(
    val playerName: String,
    val bestScore: Int = 0,
    val gamesWon: Int = 0,
    val gamesLost: Int = 0,
    val maxStreak: Int = 0,
    val totalCorrect: Int = 0,
    val totalAnswered: Int = 0
)

@Serializable
data class RecordsFile(val players: MutableMap<String, PlayerRecord> = mutableMapOf())
