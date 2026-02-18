package org.example.trivial

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.example.trivial.network.NetworkClient
import org.example.trivial.network.model.*
import org.example.trivial.model.GameConfig
import org.example.trivial.model.GameMode
import org.example.trivial.ui.*

enum class Screen {
    LOGIN,
    MENU,
    CONFIG,
    RECORDS,
    GAME_SERVER,
    RESULTS
}

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val networkClient = remember { NetworkClient() }

    var currentScreen by remember { mutableStateOf(Screen.LOGIN) }
    var gameConfig    by remember { mutableStateOf(GameConfig()) }
    var playerName    by remember { mutableStateOf("") }
    var isConnecting  by remember { mutableStateOf(false) }
    var connectError  by remember { mutableStateOf<String?>(null) }

    var currentQuestion  by remember { mutableStateOf<QuestionData?>(null) }
    var answerResult     by remember { mutableStateOf<AnswerResultData?>(null) }
    var scores           by remember { mutableStateOf<List<PlayerScoreData>>(emptyList()) }
    var gameEndData      by remember { mutableStateOf<GameEndData?>(null) }
    var records          by remember { mutableStateOf<RecordsData?>(null) }
    var questionStart    by remember { mutableStateOf(0L) }

    LaunchedEffect(networkClient) {
        networkClient.messages.collect { event ->
            when (event) {
                is ServerEvent.Welcome -> {
                    currentScreen = Screen.MENU
                    networkClient.requestRecords()
                }
                is ServerEvent.Records -> {
                    records = event.data
                }
                is ServerEvent.Question -> {
                    currentQuestion = event.data
                    answerResult    = null
                    questionStart   = System.currentTimeMillis()
                }
                is ServerEvent.AnswerResult -> {
                    answerResult = event.data
                }
                is ServerEvent.ScoreUpdate -> {
                    scores = event.data.players
                }
                is ServerEvent.GameEnd -> {
                    gameEndData   = event.data
                    currentScreen = Screen.RESULTS
                    networkClient.requestRecords()  // Actualizar records
                }
                is ServerEvent.Error -> {
                    println("❌ Error del servidor: ${event.data.message}")
                }
            }
        }
    }

    MaterialTheme {
        when (currentScreen) {

            Screen.LOGIN -> {
                LoginScreen(
                    isConnecting = isConnecting,
                    errorMessage = connectError,
                    onConnect = { name ->
                        scope.launch {
                            isConnecting = true
                            connectError = null
                            val ok = networkClient.connect("localhost", 5678, name)
                            if (ok) {
                                playerName = name
                            } else {
                                connectError = "No se pudo conectar al servidor"
                            }
                            isConnecting = false
                        }
                    }
                )
            }

            Screen.MENU -> {
                MenuScreen(
                    onStartSinglePlayer = {
                        currentQuestion = null
                        answerResult    = null
                        scores          = emptyList()
                        gameEndData     = null

                        networkClient.startGame(
                            questions  = gameConfig.numberOfQuestions,
                            categories = gameConfig.categories.map { it.name },
                            difficulty = gameConfig.difficulty.name,
                            timeLimit  = gameConfig.timeLimit
                        )
                        currentScreen = Screen.GAME_SERVER
                    },
                    onShowConfig  = { currentScreen = Screen.CONFIG },
                    onShowRecords = { currentScreen = Screen.RECORDS },
                    onExit        = { networkClient.disconnect(); System.exit(0) }
                )
            }

            Screen.CONFIG -> {
                ConfigScreen(
                    currentConfig    = gameConfig,
                    onConfigChanged  = { gameConfig = it },
                    onBack           = { currentScreen = Screen.MENU }
                )
            }

            Screen.RECORDS -> {
                RecordsScreen(
                    records    = records,
                    playerName = playerName,
                    onBack     = { currentScreen = Screen.MENU }
                )
            }

            Screen.GAME_SERVER -> {
                ServerGameScreen(
                    question      = currentQuestion,
                    answerResult  = answerResult,
                    scores        = scores,
                    playerName    = playerName,
                    gameMode      = gameConfig.gameMode,
                    onAnswer      = { questionId, option ->
                        val elapsed = System.currentTimeMillis() - questionStart
                        networkClient.sendAnswer(questionId, option, elapsed)
                    },
                    onBackToMenu  = { currentScreen = Screen.MENU }
                )
            }

            Screen.RESULTS -> {
                ServerResultsScreen(
                    gameEndData   = gameEndData,
                    playerName    = playerName,
                    onPlayAgain   = {
                        currentQuestion = null
                        answerResult    = null
                        scores          = emptyList()
                        gameEndData     = null
                        networkClient.startGame(
                            questions  = gameConfig.numberOfQuestions,
                            categories = gameConfig.categories.map { it.name },
                            difficulty = gameConfig.difficulty.name,
                            timeLimit  = gameConfig.timeLimit
                        )
                        currentScreen = Screen.GAME_SERVER
                    },
                    onBackToMenu  = { currentScreen = Screen.MENU }
                )
            }
        }
    }
}
