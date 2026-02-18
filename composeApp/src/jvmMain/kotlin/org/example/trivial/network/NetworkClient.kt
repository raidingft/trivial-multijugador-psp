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

    // Configuración de reconexión
    private var lastHost = ""
    private var lastPort = 0
    private var lastPlayerName = ""
    private var shouldReconnect = false
    private var reconnectAttempts = 0
    private val maxReconnectAttempts = 5
    private val reconnectDelayMs = 3000L

    // Estado
    private val _connected = MutableStateFlow(false)
    actual val connected: StateFlow<Boolean> = _connected

    private val _messages = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 32)
    actual val messages: SharedFlow<ServerEvent> = _messages

    // ── Conectar con reconexión automática ─────────────────────────────────

    actual suspend fun connect(host: String, port: Int, playerName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // Guardar config para reconexión
                lastHost = host
                lastPort = port
                lastPlayerName = playerName
                shouldReconnect = true
                reconnectAttempts = 0

                socket = Socket(host, port).apply {
                    soTimeout = 30000  // 30s timeout
                    tcpNoDelay = true
                }
                
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = PrintWriter(socket!!.getOutputStream(), true)
                _connected.value = true

                // Lanzar escucha
                listenJob = CoroutineScope(Dispatchers.IO).launch { listen() }

                // Identificarse
                send("CONNECT", json.encodeToString(ConnectMsg(playerName)))

                println("✅ Conectado a $host:$port")
                true
            } catch (e: Exception) {
                println("❌ Error al conectar: ${e.message}")
                _connected.value = false
                
                // Intentar reconectar
                if (shouldReconnect && reconnectAttempts < maxReconnectAttempts) {
                    scheduleReconnect()
                }
                false
            }
        }

    // ── Reconexión automática ──────────────────────────────────────────────

    private fun scheduleReconnect() {
        reconnectJob?.cancel()
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            reconnectAttempts++
            println("🔄 Reconectando... (intento $reconnectAttempts/$maxReconnectAttempts)")
            
            _messages.emit(ServerEvent.Error(ErrorData("Conexión perdida. Reconectando...")))
            
            delay(reconnectDelayMs)
            
            if (shouldReconnect) {
                val success = connect(lastHost, lastPort, lastPlayerName)
                if (success) {
                    reconnectAttempts = 0
                    _messages.emit(ServerEvent.Error(ErrorData("Reconexión exitosa")))
                }
            }
        }
    }

    // ── Escuchar mensajes ──────────────────────────────────────────────────

    private suspend fun listen() {
        try {
            while (socket?.isConnected == true && !socket!!.isClosed) {
                val line = try {
                    reader?.readLine()
                } catch (e: SocketTimeoutException) {
                    continue  // Timeout normal, seguir escuchando
                } catch (e: SocketException) {
                    null  // Conexión cerrada
                }
                
                if (line == null) break
                parseServerMessage(line)
            }
        } catch (e: Exception) {
            println("❌ Error en escucha: ${e.message}")
        } finally {
            handleDisconnection()
        }
    }

    private fun handleDisconnection() {
        _connected.value = false
        println("📡 Desconectado del servidor")
        
        // Intentar reconectar automáticamente
        if (shouldReconnect && reconnectAttempts < maxReconnectAttempts) {
            scheduleReconnect()
        } else if (reconnectAttempts >= maxReconnectAttempts) {
            CoroutineScope(Dispatchers.IO).launch {
                _messages.emit(ServerEvent.Error(
                    ErrorData("No se pudo reconectar después de $maxReconnectAttempts intentos")
                ))
            }
        }
    }

    private suspend fun parseServerMessage(line: String) {
        val colonIdx = line.indexOf(':')
        if (colonIdx < 0) return

        val type    = line.substring(0, colonIdx).trim()
        val payload = line.substring(colonIdx + 1).trim()

        try {
            val event: ServerEvent = when (type) {
                "WELCOME"       -> ServerEvent.Welcome(json.decodeFromString(payload))
                "RECORDS"       -> ServerEvent.Records(json.decodeFromString(payload))
                "QUESTION"      -> ServerEvent.Question(json.decodeFromString(payload))
                "ANSWER_RESULT" -> ServerEvent.AnswerResult(json.decodeFromString(payload))
                "SCORE_UPDATE"  -> ServerEvent.ScoreUpdate(json.decodeFromString(payload))
                "GAME_END"      -> ServerEvent.GameEnd(json.decodeFromString(payload))
                "ERROR"         -> ServerEvent.Error(json.decodeFromString(payload))
                else            -> return
            }
            _messages.emit(event)
        } catch (e: Exception) {
            println("⚠️ Error al parsear '$type': ${e.message}")
        }
    }

    // ── Enviar mensajes ────────────────────────────────────────────────────

    actual fun startGame(questions: Int, categories: List<String>, difficulty: String, timeLimit: Int) {
        send("CREATE_TRIVIA", json.encodeToString(
            CreateTriviaMsg(
                mode       = "PVE",
                questions  = questions,
                categories = categories,
                difficulty = difficulty,
                timeLimit  = timeLimit
            )
        ))
    }

    actual fun sendAnswer(questionId: Int, selectedOption: Int, timeElapsed: Long) {
        send("ANSWER", json.encodeToString(
            AnswerMsg(
                questionId     = questionId,
                selectedOption = selectedOption,
                timeElapsed    = timeElapsed
            )
        ))
    }

    actual fun requestRecords() {
        send("GET_RECORDS", "{}")
    }

    actual fun disconnect() {
        shouldReconnect = false  // No reconectar si es desconexión manual
        reconnectJob?.cancel()
        listenJob?.cancel()
        
        send("DISCONNECT", "{}")
        
        try {
            socket?.close()
        } catch (_: Exception) {}
        
        _connected.value = false
        println("👋 Desconectado manualmente")
    }

    private fun send(type: String, payload: String) {
        try {
            writer?.println("$type:$payload")
        } catch (e: Exception) {
            println("⚠️ Error al enviar '$type': ${e.message}")
            handleDisconnection()
        }
    }
}
