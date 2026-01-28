package org.example.trivial

import androidx.compose.runtime.*
import androidx.compose.material3.MaterialTheme
import org.example.trivial.game.GameManager
import org.example.trivial.model.GameConfig
import org.example.trivial.ui.*

enum class Screen {
    MENU,
    CONFIG,
    GAME,
    RESULTS
}

@Composable
fun App() {
    var currentScreen by remember { mutableStateOf(Screen.MENU) }
    var gameConfig by remember { mutableStateOf(GameConfig()) }
    var gameManager by remember { mutableStateOf<GameManager?>(null) }
    
    MaterialTheme {
        when (currentScreen) {
            Screen.MENU -> {
                MenuScreen(
                    onStartSinglePlayer = {
                        gameManager = GameManager(gameConfig)
                        currentScreen = Screen.GAME
                    },
                    onShowConfig = {
                        currentScreen = Screen.CONFIG
                    },
                    onExit = {
                        // Cerrar la aplicación
                        System.exit(0)
                    }
                )
            }
            
            Screen.CONFIG -> {
                ConfigScreen(
                    currentConfig = gameConfig,
                    onConfigChanged = { newConfig ->
                        gameConfig = newConfig
                    },
                    onBack = {
                        currentScreen = Screen.MENU
                    }
                )
            }
            
            Screen.GAME -> {
                gameManager?.let { manager ->
                    GameScreen(
                        gameManager = manager,
                        gameMode = gameConfig.gameMode,
                        onGameFinished = {
                            currentScreen = Screen.RESULTS
                        }
                    )
                }
            }
            
            Screen.RESULTS -> {
                gameManager?.let { manager ->
                    ResultsScreen(
                        playerScore = manager.playerScore,
                        totalQuestions = manager.getTotalQuestions(),
                        onPlayAgain = {
                            gameManager = GameManager(gameConfig)
                            currentScreen = Screen.GAME
                        },
                        onBackToMenu = {
                            gameManager = null
                            currentScreen = Screen.MENU
                        }
                    )
                }
            }
        }
    }
}
