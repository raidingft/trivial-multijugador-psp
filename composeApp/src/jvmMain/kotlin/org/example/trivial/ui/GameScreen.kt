package org.example.trivial.ui

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.trivial.game.GameManager
import org.example.trivial.model.AnswerResult
import org.example.trivial.model.GameMode
import org.example.trivial.model.Question

@Composable
fun GameScreen(
    gameManager: GameManager,
    gameMode: GameMode,
    onGameFinished: () -> Unit
) {
    var currentQuestion by remember { mutableStateOf(gameManager.getCurrentQuestion()) }
    var selectedAnswer by remember { mutableStateOf<Int?>(null) }
    var answerResult by remember { mutableStateOf<AnswerResult?>(null) }
    var showResult by remember { mutableStateOf(false) }
    var timeLeft by remember { mutableStateOf(30) }
    val coroutineScope = rememberCoroutineScope()
    
    // Timer para modo contrarreloj
    LaunchedEffect(currentQuestion) {
        if (gameMode == GameMode.CONTRARRELOJ && currentQuestion != null) {
            gameManager.startQuestion()
            timeLeft = 30
            
            while (timeLeft > 0 && !showResult) {
                delay(1000)
                timeLeft--
            }
            
            // Tiempo agotado, forzar respuesta incorrecta
            if (!showResult) {
                selectedAnswer = -1
                val result = gameManager.submitAnswer(-1)
                answerResult = result
                showResult = true
            }
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        currentQuestion?.let { question ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header con progreso y puntuación
                GameHeader(
                    questionNumber = gameManager.getCurrentQuestionNumber(),
                    totalQuestions = gameManager.getTotalQuestions(),
                    score = gameManager.playerScore.score,
                    streak = gameManager.playerScore.streak,
                    timeLeft = if (gameMode == GameMode.CONTRARRELOJ) timeLeft else null
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Categoría
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "📚 ${question.category.name.replace("_", " ")}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }
                
                // Pregunta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Text(
                        text = question.question,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(32.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Opciones de respuesta
                question.options.forEachIndexed { index, option ->
                    AnswerButton(
                        text = option,
                        index = index,
                        selected = selectedAnswer == index,
                        showResult = showResult,
                        isCorrect = index == question.correctAnswer,
                        onClick = {
                            if (!showResult) {
                                selectedAnswer = index
                                val result = gameManager.submitAnswer(index)
                                answerResult = result
                                showResult = true
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Mostrar resultado
                AnimatedVisibility(
                    visible = showResult,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    ResultCard(
                        result = answerResult,
                        onNext = {
                            if (gameManager.nextQuestion()) {
                                // Siguiente pregunta
                                currentQuestion = gameManager.getCurrentQuestion()
                                selectedAnswer = null
                                answerResult = null
                                showResult = false
                                timeLeft = 30
                            } else {
                                // Fin del juego
                                onGameFinished()
                            }
                        }
                    )
                }
            }
        } ?: run {
            // No hay pregunta, fin del juego
            LaunchedEffect(Unit) {
                onGameFinished()
            }
        }
    }
}

@Composable
private fun GameHeader(
    questionNumber: Int,
    totalQuestions: Int,
    score: Int,
    streak: Int,
    timeLeft: Int?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progreso
            Text(
                text = "Pregunta $questionNumber/$totalQuestions",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Puntuación y racha
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "🎯 $score puntos",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                if (streak >= 3) {
                    Text(
                        text = "🔥 Racha: $streak",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Timer
            timeLeft?.let {
                Text(
                    text = "⏱️ ${it}s",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (it <= 5) MaterialTheme.colorScheme.error else Color.Unspecified
                )
            }
        }
    }
}

@Composable
private fun AnswerButton(
    text: String,
    index: Int,
    selected: Boolean,
    showResult: Boolean,
    isCorrect: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        showResult && isCorrect -> Color(0xFF4CAF50) // Verde
        showResult && selected && !isCorrect -> Color(0xFFF44336) // Rojo
        selected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        else -> MaterialTheme.colorScheme.surface
    }
    
    val contentColor = when {
        showResult && (isCorrect || (selected && !isCorrect)) -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }
    
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = if (selected) 8.dp else 4.dp
        ),
        enabled = !showResult
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${'A' + index}. ",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = text,
                fontSize = 18.sp,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
private fun ResultCard(
    result: AnswerResult?,
    onNext: () -> Unit
) {
    result?.let {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (it.correct) 
                    Color(0xFF4CAF50).copy(alpha = 0.9f) 
                else 
                    Color(0xFFF44336).copy(alpha = 0.9f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (it.correct) "✅ ¡CORRECTO!" else "❌ INCORRECTO",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                if (it.points > 0) {
                    Text(
                        text = "+${it.points} puntos",
                        fontSize = 24.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (it.explanation.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = it.explanation,
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = if (it.correct) Color(0xFF4CAF50) else Color(0xFFF44336)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Siguiente →",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
