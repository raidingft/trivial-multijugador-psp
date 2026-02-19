package server.game

import kotlinx.coroutines.*
import server.network.ClientHandler
import server.model.CreateTriviaMsg
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

object MatchmakingManager {
    
    private val waitingPlayers = ConcurrentLinkedQueue<Pair<ClientHandler, CreateTriviaMsg>>()
    private val activeMatches = ConcurrentHashMap<String, PvPGameSession>()
    
    // Método Buscar Partida
    suspend fun findMatch(client: ClientHandler, config: CreateTriviaMsg): Boolean {
        val opponent = waitingPlayers.poll()
        
        return if (opponent != null) {
            val matchId = "${client.id}-${opponent.first.id}"
            println("Emparejando: ${opponent.first.playerName} (host) vs ${client.playerName}")
            println("Config host: ${opponent.second.mode}, ${opponent.second.difficulty}")
            
            val session = PvPGameSession(
                client1 = opponent.first,
                client2 = client,
                config = opponent.second
            )
            
            activeMatches[matchId] = session
            
            CoroutineScope(Dispatchers.IO).launch {
                session.start()
            }
            
            true
        } else {
            println(" ${client.playerName} esperando oponente...")
            waitingPlayers.offer(Pair(client, config))
            false
        }
    }
    
    // Método Cancelar Búsqueda
    fun cancelWaiting(client: ClientHandler) {
        waitingPlayers.removeIf { it.first.id == client.id }
    }
    
    // Método Eliminar Partida
    fun removeMatch(matchId: String) {
        activeMatches.remove(matchId)
    }
}
