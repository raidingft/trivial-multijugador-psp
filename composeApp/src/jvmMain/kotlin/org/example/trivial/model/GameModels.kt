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
    val timeLimit: Int = 15
)
