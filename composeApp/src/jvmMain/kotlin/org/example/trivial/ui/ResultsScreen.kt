package org.example.trivial.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.trivial.model.PlayerScore

@Composable
fun ResultsScreen(
    playerScore: PlayerScore,
    totalQuestions: Int,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val accuracy = if (playerScore.totalAnswers > 0) {
        (playerScore.correctAnswers.toFloat() / playerScore.totalAnswers.toFloat() * 100).toInt()
    } else 0
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "🎉 ¡Juego Terminado!",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Tarjeta de resultados
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Puntuación final
                    Text(
                        text = "Puntuación Final",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = "${playerScore.score}",
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Divider(modifier = Modifier.padding(vertical = 24.dp))
                    
                    // Estadísticas
                    StatRow(
                        label = "Respuestas Correctas",
                        value = "${playerScore.correctAnswers}/$totalQuestions"
                    )
                    StatRow(
                        label = "Precisión",
                        value = "$accuracy%"
                    )
                    StatRow(
                        label = "Racha Máxima",
                        value = "${playerScore.streak}"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onBackToMenu,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = "🏠 Menú",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(60.dp)
                ) {
                    Text(
                        text = "🔄 Jugar de Nuevo",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
