package server.network

import kotlinx.coroutines.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.data.QuestionBank
import server.data.RecordsManager
import server.game.MatchmakingManager
import server.game.PvPGameSession
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
    private var pvpSession: PvPGameSession? = null
    private var isPvPMode = false

    fun send(type: String, payload: String) {
        writer.println("$type:$payload")
    }

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

                send("WELCOME", json.encodeToString(WelcomeMsg("Bienvenido, $playerName!", id)))
                send("RECORDS", json.encodeToString(records.getAll()))
            }

            "START_PVP_MATCHMAKING" -> {
                val msg = json.decodeFromString<CreateTriviaMsg>(payload)
                isPvPMode = true
                
                send("SEARCHING_MATCH", json.encodeToString(mapOf("status" to "searching")))
                val matched = MatchmakingManager.findMatch(this, msg)
                if (!matched) {
                    send("WAITING", json.encodeToString(mapOf("status" to "waiting")))
                }
            }

            "CREATE_TRIVIA" -> {
                val msg = json.decodeFromString<CreateTriviaMsg>(payload)
                val questions = QuestionBank.get(msg.questions, msg.categories, msg.difficulty)

                if (questions.isEmpty()) {
                    send("ERROR", json.encodeToString(ErrorMsg("No hay preguntas")))
                    return
                }

                // Modo PVE
                gameSession = GameSession(
                    client    = this,
                    config    = msg,
                    questions = questions,
                    records   = records
                )
                gameSession!!.start()
            }

            "ANSWER" -> {
                val msg = json.decodeFromString<AnswerMsg>(payload)
                if (isPvPMode && pvpSession != null) {
                    pvpSession!!.processAnswer(this, msg)
                } else {
                    gameSession?.processAnswer(msg)
                }
            }

            "CANCEL_MATCHMAKING" -> {
                MatchmakingManager.cancelWaiting(this)
                isPvPMode = false
                send("MATCHMAKING_CANCELLED", json.encodeToString(mapOf("status" to "cancelled")))
            }

            "GET_RECORDS" -> {
                send("RECORDS", json.encodeToString(records.getAll()))
            }

            "DISCONNECT" -> disconnect()

            else -> println("⚠️ Tipo desconocido: $type")
        }
    }

    fun setPvPSession(session: PvPGameSession) {
        pvpSession = session
    }

    fun disconnect() {
        try {
            MatchmakingManager.cancelWaiting(this)
            pvpSession?.notifyOpponentDisconnected(this)
            println("👋 Desconectado: $playerName")
            socket.close()
            server.removeClient(this)
        } catch (_: Exception) {}
    }
}
