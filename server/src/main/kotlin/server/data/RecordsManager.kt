package server.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.model.PlayerRecord
import server.model.RecordsFile
import java.io.File

class RecordsManager(private val filePath: String = "records.json") {

    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private var data: RecordsFile = load()

    private fun load(): RecordsFile {
        val file = File(filePath)
        if (!file.exists()) {
            val empty = RecordsFile()
            file.writeText(json.encodeToString(empty))
            println("📄 records.json creado en ${file.absolutePath}")
            return empty
        }
        return try {
            json.decodeFromString<RecordsFile>(file.readText())
        } catch (e: Exception) {
            println("⚠️ Error al leer records.json: ${e.message}")
            RecordsFile()
        }
    }

    private fun save() {
        File(filePath).writeText(json.encodeToString(data))
    }

    fun getAll(): RecordsFile = data

    fun updateAfterGame(
        playerName: String,
        score: Int,
        won: Boolean,
        streak: Int,
        correct: Int,
        answered: Int
    ) {
        val old = data.players[playerName] ?: PlayerRecord(playerName)
        data.players[playerName] = old.copy(
            bestScore    = maxOf(old.bestScore, score),
            gamesWon     = if (won) old.gamesWon + 1 else old.gamesWon,
            gamesLost    = if (!won) old.gamesLost + 1 else old.gamesLost,
            maxStreak    = maxOf(old.maxStreak, streak),
            totalCorrect = old.totalCorrect + correct,
            totalAnswered = old.totalAnswered + answered
        )
        save()
        println("💾 Record actualizado: $playerName → score=${data.players[playerName]?.bestScore}")
    }
}
