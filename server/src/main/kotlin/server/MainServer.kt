package server

import kotlinx.coroutines.runBlocking
import server.config.ConfigManager
import server.data.RecordsManager
import server.network.TrivialServer

fun main() = runBlocking {
    println("   TRIVIAL MULTIJUGADOR - SERVIDOR   ")

    try {
        val configManager = ConfigManager()
        val records       = RecordsManager()
        val server        = TrivialServer(configManager, records)

        Runtime.getRuntime().addShutdownHook(Thread { server.stop() })

        server.start()

    } catch (e: Exception) {
        println("Error fatal: ${e.message}")
        e.printStackTrace()
    }
}
