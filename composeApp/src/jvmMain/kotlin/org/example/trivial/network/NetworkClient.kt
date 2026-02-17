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

class NetworkClient {

    private var socket: Socket? = null
    private var reader: BufferedReader? = null
    private var writer: PrintWriter? = null
    private var listenJob: Job? = null

    private val json = Json { ignoreUnknownKeys = true }

    // Estado de conexión
    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    // Mensajes recibidos del servidor
    private val _messages = MutableSharedFlow<ServerEvent>(extraBufferCapacity = 32)
    val messages: SharedFlow<ServerEvent> = _messages

    // ── Conectar ───────────────────────────────────────────────────────────

    suspend fun connect(host: String, port: Int, playerName: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                socket = Socket(host, port)
                reader = BufferedReader(InputStreamReader(socket!!.getInputStream()))
                writer = PrintWriter(socket!!.getOutputStream(), true)
                _connected.value = true

                // Lanzar escucha
                listenJob = CoroutineScope(Dispatchers.IO).launch { listen() }

                // Identificarse
                send("CONNECT", json.encodeToString(ConnectMsg(playerName)))

                true
            } catch (e: Exception) {
                println("❌ Error al conectar: ${e.message}")
                _connected.value = false
                false
            }
        }

    // ── Escuchar mensajes del servidor ─────────────────────────────────────

    private suspend fun listen() {
        try {
            while (socket?.isConnected == true && !socket!!.isClosed) {
                val line = reader?.readLine() ?: break
                parseServerMessage(line)
            }
        } catch (e: Exception) {
            println("❌ Conexión perdida: ${e.message}")
        } finally {
            _connected.value = false
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
            println("⚠️ Error al parsear mensaje '$type': ${e.message}")
        }
    }

    // ── Enviar mensajes al servidor ────────────────────────────────────────

    fun startGame(questions: Int, categories: List<String>, difficulty: String, timeLimit: Int) {
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

    fun sendAnswer(questionId: Int, selectedOption: Int, timeElapsed: Long) {
        send("ANSWER", json.encodeToString(
            AnswerMsg(
                questionId     = questionId,
                selectedOption = selectedOption,
                timeElapsed    = timeElapsed
            )
        ))
    }

    fun requestRecords() {
        send("GET_RECORDS", "{}")
    }

    // ── Desconectar ────────────────────────────────────────────────────────

    fun disconnect() {
        send("DISCONNECT", "{}")
        listenJob?.cancel()
        socket?.close()
        _connected.value = false
    }

    // ── Envío raw ──────────────────────────────────────────────────────────

    private fun send(type: String, payload: String) {
        try {
            writer?.println("$type:$payload")
        } catch (e: Exception) {
            println("⚠️ Error al enviar '$type': ${e.message}")
        }
    }
}
