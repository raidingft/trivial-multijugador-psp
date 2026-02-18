package org.example.trivial

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "TrivialMultijugador",
        state = WindowState(width = 1200.dp, height = 800.dp)
    ) {
        App()
    }
}
