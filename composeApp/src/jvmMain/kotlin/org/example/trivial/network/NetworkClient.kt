package org.example.trivial.network

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.trivial.network.model.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException

actual class NetworkClient {

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var listenJob: Job? = null
    private var reconnectJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true }

    private var lastHost = ""
    private var lastPort = 0
    private var lastPlayerName = ""
    private var shouldReconnect = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val reconnectDelayMs = 3000L

    private val _connected = MutableStateFlow(false)
    actual val connected: StateFlow<Boolean> = _connected

    private val _messages = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 32)
    actual val messages: SharedFlow<ServerEvent> = _messages

    actual suspend fun connect(host: String, port: Int, playerName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                lastHost = host
                lastPort = port
                lastPlayerName = playerName
                shouldReconnect = true
                reconnectAttempts = 0

                socket = Socket(host, port).apply {
                    soTimeout = 30000
                    tcpNoDelay = true
                }
                
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = PrintWriter(socket!!.getOutputStream(), true)
                _connected.value = true

                listenJob = CoroutineScope(Dispatchers.IO).launch { listen() }
                send("CONNECT", json.encodeToString(ConnectMsg(playerName)))

                println("✅ Conectado")
                true
            } catch (e: Exception) {
                println("❌ Error: ${e.message}")
                _connected.value = false
                if (shouldReconnect && reconnectAttempts < maxReconnectAttempts) {
                    scheduleReconnect()
                }
                false
            }
        }

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            reconnectAttempts++
            _messages.emit(ServerEvent.Error(ErrorData("Reconectando...")))
            delay(reconnectDelayMs)
            if (shouldReconnect) {
                connect(lastHost, lastPort, lastPlayerName)
            }
        }
    }

    private suspend fun listen() {
        try {
            while (socket?.isConnected == true && !socket!!.isClosed) {
                val line = try {
                    reader?.readLine()
                } catch (e: SocketTimeoutException) {
                    continue
                } catch (e: SocketException) {
                    null
                }
                if (line == null) break
                parseServerMessage(line)
            }
        } catch (e: Exception) {
            println("❌ Error: ${e.message}")
        } finally {
            handleDisconnection()
        }
    }

    private fun handleDisconnection() {
        _connected.value = false
        if (shouldReconnect && reconnectAttempts < maxReconnectAttempts) {
            scheduleReconnect()
        }
    }

    private suspend fun parseServerMessage(line: String) {
        val idx = line.indexOf(':')
        if (idx < 0) return

        val type = line.substring(0, idx).trim()
        val payload = line.substring(idx + 1).trim()

        try {
            val event: ServerEvent = when (type) {
                "WELCOME"               -> ServerEvent.Welcome(json.decodeFromString(payload))
                "RECORDS"               -> ServerEvent.Records(json.decodeFromString(payload))
                "QUESTION"              -> ServerEvent.Question(json.decodeFromString(payload))
                "ANSWER_RESULT"         -> ServerEvent.AnswerResult(json.decodeFromString(payload))
                "SCORE_UPDATE"          -> ServerEvent.ScoreUpdate(json.decodeFromString(payload))
                "GAME_END"              -> ServerEvent.GameEnd(json.decodeFromString(payload))
                "ERROR"                 -> ServerEvent.Error(json.decodeFromString(payload))
                "SEARCHING_MATCH"       -> ServerEvent.SearchingMatch
                "WAITING"               -> ServerEvent.Waiting
                "PVP_MATCHED"           -> {
                    val data = json.decodeFromString<Map<String, String?>>(payload)
                    ServerEvent.PvPMatched(
                        opponentName = data["opponent"] ?: "Desconocido",
                        gameMode = data["mode"]
                    )
                }
                "MATCHMAKING_CANCELLED" -> ServerEvent.MatchmakingCancelled
                "OPPONENT_ANSWERED"     -> return
                else                    -> return
            }
            _messages.emit(event)
        } catch (e: Exception) {
            println("⚠️ Error: ${e.message}")
        }
    }

    actual fun startGame(questions: Int, categories: List<String>, difficulty: String, timeLimit: Int) {
        send("CREATE_TRIVIA", json.encodeToString(
            CreateTriviaMsg("PVE", questions, categories, difficulty, timeLimit)
        ))
    }

    fun startPvPGame(questions: Int, categories: List<String>, difficulty: String, timeLimit: Int, mode: String) {
        send("START_PVP_MATCHMAKING", json.encodeToString(
            CreateTriviaMsg(mode, questions, categories, difficulty, timeLimit)
        ))
    }

    fun cancelMatchmaking() {
        send("CANCEL_MATCHMAKING", "{}")
    }

    actual fun sendAnswer(questionId: Int, selectedOption: Int, timeElapsed: Long) {
        send("ANSWER", json.encodeToString(
            AnswerMsg(questionId, selectedOption, timeElapsed)
        ))
    }

    actual fun requestRecords() {
        send("GET_RECORDS", "{}")
    }

    actual fun disconnect() {
        shouldReconnect = false
        reconnectJob?.cancel()
        listenJob?.cancel()
        send("DISCONNECT", "{}")
        try { socket?.close() } catch (_: Exception) {}
        _connected.value = false
    }

    private fun send(type: String, payload: String) {
        try {
            writer?.println("$type:$payload")
        } catch (e: Exception) {
            println("⚠️ Error: ${e.message}")
            handleDisconnection()
        }
    }
}
