package org.example.trivial.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import org.example.trivial.sound.SoundPlayer

fun categoryIcon(category: String) = when (category) {
    "HISTORIA"             -> "🏛️"
    "CIENCIA_NATURALEZA"   -> "🔬"
    "DEPORTES"             -> "⚽"
    "GEOGRAFIA"            -> "🌍"
    "ARTE_LITERATURA"      -> "🎨"
    "ENTRETENIMIENTO"      -> "🎬"
    "TECNOLOGIA"           -> "💻"
    "CONOCIMIENTO_GENERAL" -> "🧠"
    else                   -> "❓"
}

fun categoryName(category: String) = when (category) {
    "HISTORIA"             -> "Historia"
    "CIENCIA_NATURALEZA"   -> "Ciencia"
    "DEPORTES"             -> "Deportes"
    "GEOGRAFIA"            -> "Geografía"
    "ARTE_LITERATURA"      -> "Arte y Literatura"
    "ENTRETENIMIENTO"      -> "Entretenimiento"
    "TECNOLOGIA"           -> "Tecnología"
    "CONOCIMIENTO_GENERAL" -> "Conocimiento General"
    else                   -> category
}

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
    val myScore = scores.find { it.name == playerName }

    // Reproducir sonido cuando llega el resultado
    LaunchedEffect(answerResult) {
        answerResult?.let {
            if (it.correct) {
                SoundPlayer.playCorrect()
            } else {
                SoundPlayer.playIncorrect()
            }
        }
    }

    // Animación de racha
    val streakAnimating = remember { mutableStateOf(false) }
    val streakScale by animateFloatAsState(
        targetValue = if (streakAnimating.value) 1.4f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "streak"
    )

    LaunchedEffect(myScore?.streak) {
        if ((myScore?.streak ?: 0) >= 5) {
            streakAnimating.value = true
            delay(300)
            streakAnimating.value = false
        }
    }

    // Timer
    LaunchedEffect(question) {
        if (question != null && gameMode == GameMode.CONTRARRELOJ) {
            timeLeft = question.timeLimit
            while (timeLeft > 0 && !showResult) {
                delay(1000)
                timeLeft--
            }
            if (!showResult && selectedOption == null) {
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Esperando pregunta...", fontSize = 20.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Modo de juego ────────────────────────────────────
                Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
                ) {
                        Text(
                            text = when (gameMode) {
                                GameMode.POR_TURNOS -> "🔄 Modo: Por Turnos"
                                GameMode.SIMULTANEO -> "⚡ Modo: Simultáneo"
                                GameMode.CONTRARRELOJ -> "⏱️ Modo: Contrarreloj"
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // ── Indicador de turno (solo en modo POR_TURNOS) ────────────
                    if (gameMode == GameMode.POR_TURNOS && question.currentTurnPlayer != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (question.currentTurnPlayer == playerName) {
                                    Color(0xFF4CAF50)  // Verde si es tu turno
                                } else {
                                    Color(0xFFFF9800)  // Naranja si es turno del otro
                                }
                            )
                        ) {
                            Text(
                                text = if (question.currentTurnPlayer == playerName) {
                                    "👉 ¡ES TU TURNO!"
                                } else {
                                    "⏸️ Turno de: ${question.currentTurnPlayer}"
                                },
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                textAlign = TextAlign.Center,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // ── Panel de puntuación ────────────────────────────────────
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Puntos", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(
                                text = "${myScore?.score ?: 0}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.scale(streakScale)
                        ) {
                            val streak = myScore?.streak ?: 0
                            Text("Racha", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(
                                text = if (streak >= 5) "🔥 $streak" else "$streak",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (streak >= 5) Color(0xFFFF6B35) else Color.Unspecified
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Correctas", fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text(
                                text = "${myScore?.correctAnswers ?: 0}/${question.totalQuestions}",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (gameMode == GameMode.CONTRARRELOJ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Tiempo", fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                                Text(
                                    text = "${timeLeft}s",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (timeLeft <= 5) Color(0xFFE53935) else Color.Unspecified
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Barra de progreso de preguntas ─────────────────────────
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pregunta ${question.questionNumber} de ${question.totalQuestions}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${((question.questionNumber.toFloat() / question.totalQuestions) * 100).toInt()}%",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { question.questionNumber.toFloat() / question.totalQuestions },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Barra de progreso del tiempo ───────────────────────────
                if (gameMode == GameMode.CONTRARRELOJ) {
                    LinearProgressIndicator(
                        progress = { timeLeft.toFloat() / question.timeLimit },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = when {
                            timeLeft <= 5  -> Color(0xFFE53935)
                            timeLeft <= 10 -> Color(0xFFFF9800)
                            else           -> Color(0xFF4CAF50)
                        },
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // ── Categoría con icono ────────────────────────────────────
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "${categoryIcon(question.category)} ${categoryName(question.category)}  •  ${question.difficulty}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Pregunta ───────────────────────────────────────────────
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Text(
                        text = question.question,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ── Opciones ───────────────────────────────────────────────
                question.options.forEachIndexed { index, option ->
                    val bgColor = when {
                        showResult && index == answerResult!!.correctAnswer -> Color(0xFF4CAF50)
                        showResult && index == selectedOption && index != answerResult!!.correctAnswer -> Color(0xFFE53935)
                        else -> MaterialTheme.colorScheme.surface
                    }
                    val textColor = when {
                        showResult && (index == answerResult!!.correctAnswer ||
                            (index == selectedOption && index != answerResult!!.correctAnswer)) -> Color.White
                        else -> MaterialTheme.colorScheme.onSurface
                    }

                    Button(
                        onClick = {
                            if (!showResult && selectedOption == null && (gameMode != GameMode.POR_TURNOS || question.currentTurnPlayer == playerName)) {
                                selectedOption = index
                                onAnswer(question.id, index)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(64.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = bgColor,
                            contentColor   = textColor,
                            disabledContainerColor = bgColor,
                            disabledContentColor   = textColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !showResult && (gameMode != GameMode.POR_TURNOS || question.currentTurnPlayer == playerName)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${'A' + index}",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Text(text = option, fontSize = 16.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Spacer(modifier = Modifier.weight(1f))

                // ── Feedback resultado ─────────────────────────────────────
                AnimatedVisibility(
                    visible = showResult,
                    enter = fadeIn() + expandVertically()
                ) {
                    answerResult?.let { result ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (result.correct) Color(0xFF4CAF50) else Color(0xFFE53935)
                            )
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (result.correct) "✅ ¡CORRECTO! +${result.points} pts"
                                           else "❌ INCORRECTO",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (result.explanation.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = result.explanation,
                                        fontSize = 14.sp,
                                        color = Color.White.copy(alpha = 0.9f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                                val streak = myScore?.streak ?: 0
                                if (streak >= 5) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "🔥 ¡RACHA DE $streak! x2 puntos",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFFD700)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Siguiente pregunta en breve...",
                                    fontSize = 13.sp,
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
