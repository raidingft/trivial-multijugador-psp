package org.example.trivial.network

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import org.example.trivial.network.model.ServerEvent

expect class NetworkClient() {
    val connected: StateFlow<Boolean>
    val messages: SharedFlow<ServerEvent>
    
    suspend fun connect(host: String, port: Int, playerName: String): Boolean
    fun startGame(questions: Int, categories: List<String>, difficulty: String, timeLimit: Int)
    fun sendAnswer(questionId: Int, selectedOption: Int, timeElapsed: Long)
    fun requestRecords()
    fun disconnect()
}
