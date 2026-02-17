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
import org.example.trivial.model.GameMode
import org.example.trivial.network.model.AnswerResultData
import org.example.trivial.network.model.PlayerScoreData
import org.example.trivial.network.model.QuestionData

@Composable
fun ServerGameScreen(
    question:     QuestionData?,
    answerResult: AnswerResultData?,
    scores:       List<PlayerScoreData>,
    playerName:   String,
    gameMode:     GameMode,
    onAnswer:     (questionId: Int, option: Int) -> Unit,
    onBackToMenu: () -> Unit
) {
    var selectedOption by remember(question) { mutableStateOf<Int?>(null) }
    var timeLeft       by remember(question) { mutableStateOf(30) }
    val showResult = answerResult != null

    // Timer
    LaunchedEffect(question) {
        if (question != null && gameMode == GameMode.CONTRARRELOJ) {
            timeLeft = question.timeLimit
            while (timeLeft > 0 && answerResult == null) {
                delay(1000)
                timeLeft--
            }
            // Tiempo agotado
            if (answerResult == null && selectedOption == null) {
                selectedOption = -1
                onAnswer(question.id, -1)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (question == null) {
            // Esperando pregunta del servidor
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Esperando pregunta del servidor...", fontSize = 20.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header puntuación
                val myScore = scores.find { it.name == playerName }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🎯 ${myScore?.score ?: 0} pts",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if ((myScore?.streak ?: 0) >= 3) {
                            Text(
                                text = "🔥 Racha: ${myScore?.streak}",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (gameMode == GameMode.CONTRARRELOJ) {
                            Text(
                                text = "⏱️ ${timeLeft}s",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (timeLeft <= 5) MaterialTheme.colorScheme.error
                                        else Color.Unspecified
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Categoría
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "📚 ${question.category}  •  ${question.difficulty}",
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pregunta
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Text(
                        text = question.question,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Opciones
                question.options.forEachIndexed { index, option ->
                    val bgColor = when {
                        showResult && index == answerResult!!.correctAnswer -> Color(0xFF4CAF50)
                        showResult && index == selectedOption && index != answerResult!!.correctAnswer -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val textColor = when {
                        showResult && (index == answerResult!!.correctAnswer ||
                            (index == selectedOption && index != answerResult!!.correctAnswer)) -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Button(
                        onClick = {
                            if (!showResult && selectedOption == null) {
                                selectedOption = index
                                onAnswer(question.id, index)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(68.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bgColor,
                            contentColor   = textColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !showResult
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(
                                text = "${'A' + index}. $option",
                                fontSize = 18.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Resultado
                AnimatedVisibility(
                    visible = showResult,
                    enter = fadeIn() + expandVertically()
                ) {
                    answerResult?.let { result ->
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (result.correct) Color(0xFF4CAF50)
                                                 else Color(0xFFF44336)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (result.correct) "✅ ¡CORRECTO! +${result.points} pts"
                                           else "❌ INCORRECTO",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (result.explanation.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = result.explanation,
                                        fontSize = 15.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Esperando siguiente pregunta...",
                                    fontSize = 14.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
