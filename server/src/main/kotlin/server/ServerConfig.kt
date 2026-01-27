package server

import java.io.FileInputStream
import java.util.Properties

class ServerConfig(configFile: String = "server.properties") {
    val host: String
    val port: Int
    val maxClients: Int

    init {
        val properties = Properties()
        
        // Intenta cargar desde el classpath primero
        val inputStream = this::class.java.classLoader.getResourceAsStream(configFile)
            ?: FileInputStream(configFile) // Si no está en el classpath, intenta desde el sistema de archivos
        
        inputStream.use {
            properties.load(it)
        }
        
        host = properties.getProperty("server.host", "localhost")
        port = properties.getProperty("server.port", "5678").toInt()
        maxClients = properties.getProperty("max.clients", "10").toInt()
        
        println("Configuración cargada:")
        println("Host: $host")
        println("Puerto: $port")
        println("Máximo de clientes: $maxClients")
    }
}
