package org.example.trivial.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.trivial.model.*

@Composable
fun ConfigScreen(
    currentConfig: GameConfig,
    onConfigChanged: (GameConfig) -> Unit,
    onBack: () -> Unit
) {
    var numberOfQuestions by remember { mutableStateOf(currentConfig.numberOfQuestions) }
    var difficulty by remember { mutableStateOf(currentConfig.difficulty) }
    var gameMode by remember { mutableStateOf(currentConfig.gameMode) }
    var timeLimit by remember { mutableStateOf(currentConfig.timeLimit) }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⚙️ Configuración",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            
            // Número de preguntas
            ConfigSection(title = "Número de Preguntas") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    listOf(3, 5, 10, 20).forEach { num ->
                        FilterChip(
                            selected = numberOfQuestions == num,
                            onClick = { numberOfQuestions = num },
                            label = { Text("$num") },
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Dificultad
            ConfigSection(title = "Dificultad") {
                Column {
                    Difficulty.values().forEach { diff ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = difficulty == diff,
                                onClick = { difficulty = diff }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = diff.name.replace("_", " "),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Modo de juego
            ConfigSection(title = "Modo de Juego") {
                Column {
                    GameMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = gameMode == mode,
                                onClick = { gameMode = mode }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = mode.name.replace("_", " "),
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Tiempo límite
            if (gameMode == GameMode.CONTRARRELOJ) {
                ConfigSection(title = "Tiempo por Pregunta") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf(10, 15, 30).forEach { time ->
                            FilterChip(
                                selected = timeLimit == time,
                                onClick = { timeLimit = time },
                                label = { Text("${time}s") },
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            // Botones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                        .height(50.dp)
                ) {
                    Text("← Volver", fontSize = 18.sp)
                }
                
                Button(
                    onClick = {
                        onConfigChanged(
                            GameConfig(
                                numberOfQuestions = numberOfQuestions,
                                difficulty = difficulty,
                                gameMode = gameMode,
                                timeLimit = timeLimit
                            )
                        )
                        onBack()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                        .height(50.dp)
                ) {
                    Text("💾 Guardar", fontSize = 18.sp)
                }
            }
        }
    }
}

@Composable
private fun ConfigSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}
