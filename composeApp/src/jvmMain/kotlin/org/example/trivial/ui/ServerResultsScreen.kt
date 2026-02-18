package org.example.trivial.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.example.trivial.network.model.GameEndData

@Composable
fun ServerResultsScreen(
    gameEndData:        GameEndData?,
    playerName:         String,
    isPvP:              Boolean = false,
    playAgainRequest:   String? = null,   // nombre del rival que quiere revancha
    playAgainRejected:  String? = null,   // mensaje de rechazo
    opponentWentToMenu: String? = null,   // mensaje de que el rival fue al menú
    onPlayAgain:        () -> Unit,
    onAcceptPlayAgain:  () -> Unit = {},
    onRejectPlayAgain:  () -> Unit = {},
    onBackToMenu:       () -> Unit
) {
    val myScore   = gameEndData?.finalScores?.find { it.name == playerName }
    val myCorrect = gameEndData?.correctAnswers?.get(playerName) ?: 0
    val won       = gameEndData?.winner == playerName

    // ── Diálogo: el rival quiere jugar de nuevo ────────────────────────────
    if (playAgainRequest != null) {
        Dialog(onDismissRequest = {}) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🔄", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "$playAgainRequest quiere jugar de nuevo",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(
                            onClick = onRejectPlayAgain,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("❌ Rechazar")
                        }
                        Button(
                            onClick = onAcceptPlayAgain,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("✅ Aceptar")
                        }
                    }
                }
            }
        }
    }

    // ── Diálogo: el rival rechazó la revancha ─────────────────────────────
    if (playAgainRejected != null) {
        Dialog(onDismissRequest = onBackToMenu) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("😔", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = playAgainRejected,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBackToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Volver al menú", color = Color(0xFF37474F), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ── Diálogo: el rival fue al menú ─────────────────────────────────────
    if (opponentWentToMenu != null) {
        Dialog(onDismissRequest = onBackToMenu) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF37474F))
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏠", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = opponentWentToMenu,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onBackToMenu,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Volver al menú", color = Color(0xFF37474F), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // ── Pantalla principal de resultados ───────────────────────────────────
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (won) "🏆 ¡Has Ganado!" else "😔 ¡Buen intento!",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Puntuación Final",
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${myScore?.score ?: 0}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Divider(modifier = Modifier.padding(vertical = 20.dp))

                    ResultStatRow("Respuestas Correctas", "$myCorrect")
                    ResultStatRow("Racha Máxima", "${myScore?.streak ?: 0}")

                    if ((gameEndData?.finalScores?.size ?: 0) > 1) {
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        Text(
                            text = "Clasificación",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        gameEndData?.finalScores
                            ?.sortedByDescending { it.score }
                            ?.forEachIndexed { idx, player ->
                                ResultStatRow(
                                    label = "${idx + 1}. ${player.name}",
                                    value = "${player.score} pts"
                                )
                            }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = onBackToMenu,
                    modifier = Modifier.weight(1f).padding(end = 8.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("🏠 Menú", fontSize = 20.sp)
                }
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.weight(1f).padding(start = 8.dp).height(56.dp),
                    // En PvP, deshabilitar si ya hay una petición pendiente o el rival se fue
                    enabled = !(isPvP && (playAgainRejected != null || opponentWentToMenu != null))
                ) {
                    Text(
                        text = if (isPvP) "🔄 Revancha" else "🔄 Jugar de nuevo",
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ResultStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
