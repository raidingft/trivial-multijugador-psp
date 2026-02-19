package org.example.trivial

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import org.example.trivial.network.NetworkClient
import org.example.trivial.network.model.*
import org.example.trivial.model.GameConfig
import org.example.trivial.ui.*

enum class Screen {
    LOGIN, MENU, CONFIG, RECORDS, WAITING_MATCH, GAME_SERVER, RESULTS
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
    var opponentName     by remember { mutableStateOf<String?>(null) }
    var disconnectedMessage   by remember { mutableStateOf<String?>(null) }
    var playAgainRequest      by remember { mutableStateOf<String?>(null) }
    var playAgainRejected     by remember { mutableStateOf<String?>(null) }
    var opponentWentToMenu    by remember { mutableStateOf<String?>(null) }
    var isPvPGame             by remember { mutableStateOf(false) }

    // Método Escuchar Eventos del Servidor
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
                is ServerEvent.SearchingMatch, is ServerEvent.Waiting -> {
                    currentScreen = Screen.WAITING_MATCH
                    opponentName = null
                }
                is ServerEvent.PvPMatched -> {
                    isPvPGame = true
                    opponentName = event.opponentName

                    event.gameMode?.let { mode ->
                        gameConfig = gameConfig.copy(
                            gameMode = when (mode) {
                                "POR_TURNOS"  -> org.example.trivial.model.GameMode.POR_TURNOS
                                "SIMULTANEO"  -> org.example.trivial.model.GameMode.SIMULTANEO
                                "CONTRARRELOJ" -> org.example.trivial.model.GameMode.CONTRARRELOJ
                                else          -> gameConfig.gameMode
                            }
                        )
                        println("⚙️ Modo actualizado del host: $mode")
                    }
                }
                is ServerEvent.MatchmakingCancelled -> {
                    currentScreen = Screen.MENU
                    opponentName = null
                }
                is ServerEvent.Question -> {
                    if (currentScreen == Screen.WAITING_MATCH) {
                        currentScreen = Screen.GAME_SERVER
                    }
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
                    gameEndData        = event.data
                    playAgainRequest   = null
                    playAgainRejected  = null
                    opponentWentToMenu = null
                    currentScreen = Screen.RESULTS
                    networkClient.requestRecords()
                }
                is ServerEvent.Error -> {
                    println("❌ Error: ${event.data.message}")
                }
                is ServerEvent.OpponentDisconnected -> {
                    disconnectedMessage = "${event.playerName} se ha desconectado"
                    isPvPGame = false
                    currentScreen = Screen.MENU
                }
                is ServerEvent.PlayAgainRequest -> {
                    playAgainRequest = event.playerName
                }
                is ServerEvent.PlayAgainAccepted -> {

                    currentQuestion = null
                    answerResult    = null
                    scores          = emptyList()
                    gameEndData     = null
                    playAgainRequest = null
                    playAgainRejected = null
                    opponentWentToMenu = null
                    currentScreen = Screen.WAITING_MATCH
                }
                is ServerEvent.PlayAgainRejected -> {
                    playAgainRejected = "${event.playerName} ha rechazado jugar de nuevo"
                }
                is ServerEvent.OpponentWentToMenu -> {
                    opponentWentToMenu = "${event.playerName} ha vuelto al menú"
                }
            }
        }
    }

    MaterialTheme {
        when (currentScreen) {
            // Pantalla Login
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
                                connectError = "No se pudo conectar"
                            }
                            isConnecting = false
                        }
                    }
                )
            }

            // Pantalla Menú
            Screen.MENU -> {
                MenuScreen(
                    disconnectedMessage = disconnectedMessage,
                    onDisconnectedMessageShown = { disconnectedMessage = null },
                    isTurnMode = gameConfig.gameMode == org.example.trivial.model.GameMode.POR_TURNOS,
                    onStartSinglePlayer = {
                        currentQuestion = null
                        answerResult    = null
                        scores          = emptyList()
                        gameEndData     = null
                        isPvPGame       = false

                        networkClient.startGame(
                            questions  = gameConfig.numberOfQuestions,
                            categories = gameConfig.categories.map { it.name },
                            difficulty = gameConfig.difficulty.name,
                            timeLimit  = gameConfig.timeLimit
                        )
                        currentScreen = Screen.GAME_SERVER
                    },
                    onStartPvP = {
                        currentQuestion    = null
                        answerResult       = null
                        scores             = emptyList()
                        gameEndData        = null
                        opponentName       = null
                        playAgainRequest   = null
                        playAgainRejected  = null
                        opponentWentToMenu = null
                        disconnectedMessage = null

                        networkClient.startPvPGame(
                            questions  = gameConfig.numberOfQuestions,
                            categories = gameConfig.categories.map { it.name },
                            difficulty = gameConfig.difficulty.name,
                            timeLimit  = gameConfig.timeLimit,
                            mode       = gameConfig.gameMode.name  // Enviar el modo
                        )
                        currentScreen = Screen.WAITING_MATCH
                    },
                    onShowConfig  = { currentScreen = Screen.CONFIG },
                    onShowRecords = { currentScreen = Screen.RECORDS },
                    onExit        = { networkClient.disconnect(); System.exit(0) }
                )
            }

            // Pantalla Configuración
            Screen.CONFIG -> {
                ConfigScreen(
                    currentConfig   = gameConfig,
                    onConfigChanged = { gameConfig = it },
                    onBack          = { currentScreen = Screen.MENU }
                )
            }

            // Pantalla Records
            Screen.RECORDS -> {
                RecordsScreen(
                    records    = records,
                    playerName = playerName,
                    onBack     = { currentScreen = Screen.MENU }
                )
            }

            // Pantalla Espera de Partida
            Screen.WAITING_MATCH -> {
                WaitingMatchScreen(
                    opponentName = opponentName,
                    onCancel = {
                        networkClient.cancelMatchmaking()
                        currentScreen = Screen.MENU
                    }
                )
            }

            // Pantalla Juego
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

            // Pantalla Resultados
            Screen.RESULTS -> {
                ServerResultsScreen(
                    gameEndData        = gameEndData,
                    playerName         = playerName,
                    isPvP              = isPvPGame,
                    playAgainRequest   = playAgainRequest,
                    playAgainRejected  = playAgainRejected,
                    opponentWentToMenu = opponentWentToMenu,
                    onPlayAgain = {
                        if (isPvPGame) {
                            networkClient.requestPlayAgain()
                        } else {
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
                        }
                    },
                    onAcceptPlayAgain = {
                        networkClient.respondPlayAgain(true)
                    },
                    onRejectPlayAgain = {
                        networkClient.respondPlayAgain(false)
                        playAgainRequest = null
                    },
                    onBackToMenu = {
                        if (isPvPGame) networkClient.notifyWentToMenu()
                        isPvPGame = false
                        playAgainRequest = null
                        playAgainRejected = null
                        opponentWentToMenu = null
                        currentScreen = Screen.MENU
                    }
                )
            }
        }
    }
}
