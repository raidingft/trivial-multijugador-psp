package org.example.trivial.game

import org.example.trivial.data.QuestionBank
import org.example.trivial.model.*

class GameManager(private val config: GameConfig) {

    private val questions: List<Question>
    private var currentQuestionIndex = 0
    private var startTime: Long = 0

    val playerScore = PlayerScore("Jugador 1")

    init {
        questions = QuestionBank.getQuestions(
            count = config.numberOfQuestions,
            categories = config.categories,
            difficulty = config.difficulty
        )
    }

    fun getCurrentQuestion(): Question? {
        return if (currentQuestionIndex < questions.size) {
            questions[currentQuestionIndex]
        } else null
    }

    fun startQuestion() {
        startTime = System.currentTimeMillis()
    }

    fun submitAnswer(selectedOption: Int): AnswerResult {
        val question = getCurrentQuestion() ?: return AnswerResult(
            correct = false,
            points = 0,
            correctAnswer = 0,
            explanation = "No hay pregunta disponible",
            timeElapsed = 0
        )

        val timeElapsed = System.currentTimeMillis() - startTime
        val timeInSeconds = timeElapsed / 1000

        val isCorrect = selectedOption == question.correctAnswer

        playerScore.totalAnswers++

        var points = 0

        if (isCorrect) {
            playerScore.correctAnswers++
            playerScore.streak++

            // Puntos base según dificultad
            points = when (question.difficulty) {
                Difficulty.FACIL -> 10
                Difficulty.MEDIA -> (10 * 1.5).toInt()  // 15 puntos
                Difficulty.DIFICIL -> 10 * 2  // 20 puntos
                Difficulty.MIXTA -> 10
            }

            // Bonus por velocidad (menos de 5 segundos)
            if (timeInSeconds < 5) {
                points += 5
            }

            // Multiplicador de racha (a partir de 5 correctas seguidas)
            if (playerScore.streak >= 5) {
                points *= 2
            }

            playerScore.score += points
        } else {
            // Resetear racha si falla
            playerScore.streak = 0
        }

        return AnswerResult(
            correct = isCorrect,
            points = points,
            correctAnswer = question.correctAnswer,
            explanation = question.explanation,
            timeElapsed = timeElapsed
        )
    }

    fun nextQuestion(): Boolean {
        currentQuestionIndex++
        return hasMoreQuestions()
    }

    fun hasMoreQuestions(): Boolean {
        return currentQuestionIndex < questions.size
    }

    fun getProgress(): Pair<Int, Int> {
        return Pair(currentQuestionIndex + 1, questions.size)
    }

    fun getTotalQuestions(): Int = questions.size

    fun getCurrentQuestionNumber(): Int = currentQuestionIndex + 1

    fun getAccuracyPercentage(): Float {
        return if (playerScore.totalAnswers > 0) {
            (playerScore.correctAnswers.toFloat() / playerScore.totalAnswers.toFloat()) * 100
        } else 0f
    }

    fun reset() {
        currentQuestionIndex = 0
        playerScore.score = 0
        playerScore.streak = 0
        playerScore.correctAnswers = 0
        playerScore.totalAnswers = 0
    }
}