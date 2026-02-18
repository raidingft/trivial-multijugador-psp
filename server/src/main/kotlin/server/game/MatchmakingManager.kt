package server.game

import kotlinx.coroutines.*
import server.network.ClientHandler
import server.model.CreateTriviaMsg
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object MatchmakingManager {
    
    private val waitingPlayers = ConcurrentLinkedQueue<Pair<ClientHandler, CreateTriviaMsg>>()
    private val activeMatches = ConcurrentHashMap<String, PvPGameSession>()
    
    suspend fun findMatch(client: ClientHandler, config: CreateTriviaMsg): Boolean {
        // Buscar oponente compatible
        val opponent = waitingPlayers.poll()
        
        return if (opponent != null) {
            // Encontrado, crear partida
            val matchId = "${client.id}-${opponent.first.id}"
            println("🎮 Emparejando: ${client.playerName} vs ${opponent.first.playerName}")
            
            // Crear sesión PVP
            val session = PvPGameSession(
                client1 = client,
                client2 = opponent.first,
                config = config
            )
            
            activeMatches[matchId] = session
            session.start()
            true
        } else {
            // Nadie esperando, entrar en cola
            println("⏳ ${client.playerName} esperando oponente...")
            waitingPlayers.offer(Pair(client, config))
            false
        }
    }
    
    fun cancelWaiting(client: ClientHandler) {
        waitingPlayers.removeIf { it.first.id == client.id }
    }
    
    fun removeMatch(matchId: String) {
        activeMatches.remove(matchId)
    }
}
