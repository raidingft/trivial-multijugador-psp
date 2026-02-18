package org.example.trivial.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.trivial.network.model.PlayerRecordData
import org.example.trivial.network.model.RecordsData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun RecordsScreen(
    records: RecordsData?,
    playerName: String,
    onBack: () -> Unit
) {
    val ranking = records?.players?.values?.sortedByDescending { it.bestScore } ?: emptyList()
    val myRecord = records?.players?.get(playerName)
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "📊 Records y Estadísticas",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            myRecord?.let { record ->
                Text(
                    text = "Tus Estadísticas",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        StatRow("🏆 Mejor Puntuación", "${record.bestScore}")
                        StatRow("✅ Partidas Ganadas", "${record.gamesWon}")
                        StatRow("❌ Partidas Perdidas", "${record.gamesLost}")
                        StatRow("🔥 Racha Máxima", "${record.maxStreak}")
                        StatRow("📊 Total Correctas", "${record.totalCorrect}/${record.totalAnswered}")
                        
                        val accuracy = if (record.totalAnswered > 0) {
                            (record.totalCorrect * 100f / record.totalAnswered).toInt()
                        } else 0
                        StatRow("🎯 Precisión Global", "$accuracy%")
                        
                        val avgTime = if (record.totalAnswered > 0) {
                            (record.totalResponseTime / record.totalAnswered / 1000f)
                        } else 0f
                        StatRow("⏱️ Tiempo Promedio", String.format("%.1fs", avgTime))
                        
                        if (record.lastPlayed > 0) {
                            val date = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                                .format(Date(record.lastPlayed))
                            StatRow("🕒 Última Partida", date)
                        }
                    }
                }
                
                if (record.categoryStats.isNotEmpty()) {
                    Text(
                        text = "Estadísticas por Categoría",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            record.categoryStats.entries
                                .sortedByDescending { 
                                    if (it.value.total > 0) it.value.correct * 100 / it.value.total else 0
                                }
                                .forEach { (cat, stats) ->
                                    val percent = if (stats.total > 0) {
                                        (stats.correct * 100 / stats.total)
                                    } else 0
                                    val icon = categoryIcon(cat)
                                    val name = categoryName(cat)
                                    StatRow("$icon $name", "$percent% (${stats.correct}/${stats.total})")
                                }
                            
                            val favorite = record.categoryStats.entries
                                .filter { it.value.total >= 3 }
                                .maxByOrNull { 
                                    if (it.value.total > 0) it.value.correct * 100f / it.value.total else 0f
                                }
                            favorite?.let {
                                Divider(modifier = Modifier.padding(vertical = 8.dp))
                                val percent = (it.value.correct * 100 / it.value.total)
                                Text(
                                    text = "⭐ Categoría Favorita: ${categoryName(it.key)} ($percent%)",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                if (record.difficultyStats.isNotEmpty()) {
                    Text(
                        text = "Estadísticas por Dificultad",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)
                    )
                    
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            listOf("FACIL", "MEDIA", "DIFICIL").forEach { diff ->
                                record.difficultyStats[diff]?.let { stats ->
                                    val percent = if (stats.total > 0) {
                                        (stats.correct * 100 / stats.total)
                                    } else 0
                                    StatRow(diff, "$percent% (${stats.correct}/${stats.total})")
                                }
                            }
                        }
                    }
                }
            }
            
            if (ranking.isNotEmpty()) {
                Text(
                    text = "🏅 Ranking Global",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ranking.take(10).forEachIndexed { index, player ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val medal = when (index) {
                                        0 -> "🥇"
                                        1 -> "🥈"
                                        2 -> "🥉"
                                        else -> "${index + 1}."
                                    }
                                    Text(
                                        text = medal,
                                        fontSize = 20.sp,
                                        modifier = Modifier.width(40.dp)
                                    )
                                    Text(
                                        text = player.playerName,
                                        fontSize = 18.sp,
                                        fontWeight = if (player.playerName == playerName) 
                                            FontWeight.Bold else FontWeight.Normal,
                                        color = if (player.playerName == playerName)
                                            MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Text(
                                    text = "${player.bestScore} pts",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (index < ranking.size - 1 && index < 9) {
                                Divider()
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("← Volver al Menú", fontSize = 20.sp)
            }
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
