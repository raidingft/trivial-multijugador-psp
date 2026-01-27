package server

import kotlinx.coroutines.runBlocking
import server.config.ConfigManager
import server.data.QuestionBank
import server.data.RecordsManager
import server.network.TrivialServer

fun main() = runBlocking {
    println("╔════════════════════════════════════════╗")
    println("║  🎮 TRIVIAL MULTIJUGADOR - SERVIDOR  ║")
    println("╚════════════════════════════════════════╝\n")
    
    try {
        val configManager = ConfigManager()
        val recordsManager = RecordsManager()
        val questionBank = QuestionBank()
        
        val server = TrivialServer(configManager, recordsManager, questionBank)
        
        // Agregar shutdown hook para cerrar el servidor correctamente
        Runtime.getRuntime().addShutdownHook(Thread {
            server.stop()
        })
        
        server.start()
        
    } catch (e: Exception) {
        println("❌ Error fatal al iniciar el servidor: ${e.message}")
        e.printStackTrace()
    }
}
