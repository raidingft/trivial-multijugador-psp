package server.config

import server.model.ServerConfig
import java.io.File
import java.util.Properties

class ConfigManager(private val configFile: String = "server.properties") {

    fun loadConfig(): ServerConfig {
        val file = File(configFile)

        if (!file.exists()) {
            createDefaultConfig(file)
        }

        val properties = Properties()
        file.inputStream().use { properties.load(it) }

        return ServerConfig(
            host = properties.getProperty("server.host", "localhost"),
            port = properties.getProperty("server.port", "5678").toInt(),
            maxClients = properties.getProperty("max.clients", "10").toInt()
        )
    }

    private fun createDefaultConfig(file: File) {
        file.writeText("""
            # Configuración del Servidor Trivial Multijugador
            server.host=localhost
            server.port=5678
            max.clients=10
        """.trimIndent())
        println("✅ Archivo de configuración creado: ${file.absolutePath}")
    }
}