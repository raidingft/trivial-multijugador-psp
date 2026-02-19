package server.data

import kotlinx.serialization.json.Json
import server.model.Category
import server.model.Difficulty
import server.model.QuestionsFile
import server.model.TriviaQuestion

object QuestionBank {

    private val json = Json { ignoreUnknownKeys = true }
    private val questions: List<TriviaQuestion> = load()

    private fun load(): List<TriviaQuestion> {
        return try {
            val text = object {}.javaClass.getResourceAsStream("/questions.json")
                ?.bufferedReader()
                ?.readText()
                ?: error("No se encontró questions.json")

            val file = json.decodeFromString<QuestionsFile>(text)
            println("Preguntas cargadas: ${file.questions.size}")
            file.questions
        } catch (e: Exception) {
            println("Error al cargar preguntas: ${e.message}")
            emptyList()
        }
    }

    fun get(count: Int, categories: List<Category>, difficulty: Difficulty): List<TriviaQuestion> {
        var pool = questions.filter { it.category in categories }
        if (difficulty != Difficulty.MIXTA) {
            pool = pool.filter { it.difficulty == difficulty }
        }
        return pool.shuffled().take(count)
    }
}
