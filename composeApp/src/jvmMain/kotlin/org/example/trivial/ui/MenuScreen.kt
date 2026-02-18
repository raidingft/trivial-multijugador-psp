package org.example.trivial.ui

import androidx.compose.foundation.layout.*
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

@Composable
fun MenuScreen(
    disconnectedMessage: String? = null,
    onDisconnectedMessageShown: () -> Unit = {},
    onStartSinglePlayer: () -> Unit,
    onStartPvP: () -> Unit,
    onShowConfig: () -> Unit,
    onShowRecords: () -> Unit,
    onExit: () -> Unit
) {
    // Diálogo de desconexión del rival
    if (disconnectedMessage != null) {
        Dialog(onDismissRequest = onDisconnectedMessageShown) {
            Card(
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFB71C1C))
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📡", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = disconnectedMessage,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onDisconnectedMessageShown,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Aceptar", color = Color(0xFFB71C1C), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
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
                text = "🎯 TRIVIAL",
                fontSize = 56.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Multijugador",
                fontSize = 32.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 64.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            MenuButton(
                text = "🎮 Jugar Solo (PVE)",
                onClick = onStartSinglePlayer
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "🆚 Jugar PVP",
                onClick = onStartPvP,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "📊 Records",
                onClick = onShowRecords,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "⚙️ Configuración",
                onClick = onShowConfig,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            MenuButton(
                text = "❌ Salir",
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            )
        }
    }
}

@Composable
private fun MenuButton(
    text: String,
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(300.dp).height(60.dp),
        colors = colors
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
