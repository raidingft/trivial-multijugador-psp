package server.data

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import server.model.*
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
        answered: Int,
        categoryStats: Map<Category, Pair<Int, Int>>,
        difficultyStats: Map<Difficulty, Pair<Int, Int>>,
        totalResponseTime: Long
    ) {
        val old = data.players[playerName] ?: PlayerRecord(playerName)
        
        // Merge category stats
        val newCategoryStats = old.categoryStats.toMutableMap()
        categoryStats.forEach { (cat, pair) ->
            val existing = newCategoryStats[cat.name] ?: CategoryStats()
            newCategoryStats[cat.name] = CategoryStats(
                correct = existing.correct + pair.first,
                total   = existing.total + pair.second
            )
        }
        
        // Merge difficulty stats
        val newDifficultyStats = old.difficultyStats.toMutableMap()
        difficultyStats.forEach { (diff, pair) ->
            val existing = newDifficultyStats[diff.name] ?: DifficultyStats()
            newDifficultyStats[diff.name] = DifficultyStats(
                correct = existing.correct + pair.first,
                total   = existing.total + pair.second
            )
        }
        
        data.players[playerName] = old.copy(
            bestScore         = maxOf(old.bestScore, score),
            gamesWon          = if (won) old.gamesWon + 1 else old.gamesWon,
            gamesLost         = if (!won) old.gamesLost + 1 else old.gamesLost,
            maxStreak         = maxOf(old.maxStreak, streak),
            totalCorrect      = old.totalCorrect + correct,
            totalAnswered     = old.totalAnswered + answered,
            categoryStats     = newCategoryStats,
            difficultyStats   = newDifficultyStats,
            totalResponseTime = old.totalResponseTime + totalResponseTime,
            lastPlayed        = System.currentTimeMillis()
        )
        save()
        println("💾 Record actualizado: $playerName → score=${data.players[playerName]?.bestScore}")
    }
}
