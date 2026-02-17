package server.network

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.data.QuestionBank
import server.data.RecordsManager
import server.model.*
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.UUID

class ClientHandler(
    private val socket: Socket,
    private val server: TrivialServer,
    private val records: RecordsManager
) {
    val id: String = UUID.randomUUID().toString()
    var playerName: String = "Jugador_$id"

    private val json = Json { ignoreUnknownKeys = true }
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
    private val writer = PrintWriter(socket.getOutputStream(), true)

    private var gameSession: GameSession? = null

    // ── Enviar ─────────────────────────────────────────────────────────────

    fun send(type: String, payload: String) {
        writer.println("$type:$payload")
    }

    // ── Bucle principal ────────────────────────────────────────────────────

    suspend fun handle() = coroutineScope {
        try {
            println("🔌 Conexión desde ${socket.inetAddress.hostAddress}")

            while (socket.isConnected && !socket.isClosed) {
                val line = withContext(Dispatchers.IO) { reader.readLine() } ?: break
                processLine(line)
            }
        } catch (e: Exception) {
            println("⚠️ Error en cliente $playerName: ${e.message}")
        } finally {
            disconnect()
        }
    }

    private suspend fun processLine(line: String) {
        val colonIdx = line.indexOf(':')
        if (colonIdx < 0) return

        val type    = line.substring(0, colonIdx).trim()
        val payload = line.substring(colonIdx + 1).trim()

        when (type) {
            "CONNECT" -> {
                val msg = json.decodeFromString<ConnectMsg>(payload)
                playerName = msg.playerName
                println("👤 Jugador conectado: $playerName")

                // Bienvenida + records actuales
                send("WELCOME", json.encodeToString(WelcomeMsg("Bienvenido, $playerName!", id)))
                send("RECORDS", json.encodeToString(records.getAll()))
            }

            "CREATE_TRIVIA" -> {
                val msg = json.decodeFromString<CreateTriviaMsg>(payload)
                val questions = QuestionBank.get(msg.questions, msg.categories, msg.difficulty)

                if (questions.isEmpty()) {
                    send("ERROR", json.encodeToString(ErrorMsg("No hay preguntas para esa configuración")))
                    return
                }

                gameSession = GameSession(
                    client       = this,
                    config       = msg,
                    questions    = questions,
                    records      = records
                )
                gameSession!!.start()
            }

            "ANSWER" -> {
                val msg = json.decodeFromString<AnswerMsg>(payload)
                gameSession?.processAnswer(msg)
            }

            "GET_RECORDS" -> {
                send("RECORDS", json.encodeToString(records.getAll()))
            }

            "DISCONNECT" -> disconnect()

            else -> println("⚠️ Tipo desconocido: $type")
        }
    }

    fun disconnect() {
        try {
            println("👋 Desconectado: $playerName")
            socket.close()
            server.removeClient(this)
        } catch (_: Exception) {}
    }
}
