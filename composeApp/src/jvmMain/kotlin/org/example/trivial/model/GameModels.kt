package org.example.trivial.model

enum class Category {
    HISTORIA,
    CIENCIA_NATURALEZA,
    DEPORTES,
    GEOGRAFIA,
    ARTE_LITERATURA,
    ENTRETENIMIENTO,
    TECNOLOGIA,
    CONOCIMIENTO_GENERAL
}

enum class Difficulty {
    FACIL,
    MEDIA,
    DIFICIL,
    MIXTA
}

enum class GameMode {
    POR_TURNOS,
    SIMULTANEO,
    CONTRARRELOJ
}

data class GameConfig(
    val numberOfQuestions: Int = 10,
    val categories: List<Category> = Category.values().toList(),
    val difficulty: Difficulty = Difficulty.MEDIA,
    val gameMode: GameMode = GameMode.CONTRARRELOJ,
    val timeLimit: Int = 15 // segundos
)

data class Question(
    val id: Int,
    val category: Category,
    val difficulty: Difficulty,
    val question: String,
    val options: List<String>,
    val correctAnswer: Int, // índice de la respuesta correcta
    val explanation: String = ""
)

data class PlayerScore(
    val playerName: String,
    var score: Int = 0,
    var streak: Int = 0,
    var correctAnswers: Int = 0,
    var totalAnswers: Int = 0
)

data class AnswerResult(
    val correct: Boolean,
    val points: Int,
    val correctAnswer: Int,
    val explanation: String,
    val timeElapsed: Long
)
