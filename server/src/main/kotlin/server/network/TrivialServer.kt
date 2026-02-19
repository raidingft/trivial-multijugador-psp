package server.network

import kotlinx.coroutines.*
import server.config.ConfigManager
import server.data.RecordsManager
import java.net.ServerSocket
import java.util.concurrent.ConcurrentHashMap

class TrivialServer(
    configManager: ConfigManager,
    private val records: RecordsManager
) {
    private val config = configManager.loadConfig()
    private val clients = ConcurrentHashMap<String, ClientHandler>()
    private lateinit var serverSocket: ServerSocket

    // Método Iniciar Servidor
    suspend fun start() = coroutineScope {
        serverSocket = ServerSocket(
            config.port, 50,
            java.net.InetAddress.getByName(config.host)
        )

        println("Servidor escuchando en ${config.host}:${config.port}")
        println("Máximo clientes: ${config.maxClients}")
        println("Esperando conexiones...\n")

        while (true) {
            val socket = withContext(Dispatchers.IO) { serverSocket.accept() }

            if (clients.size >= config.maxClients) {
                println("Servidor lleno, rechazando conexión")
                socket.close()
                continue
            }

            val handler = ClientHandler(socket, this@TrivialServer, records)
            clients[handler.id] = handler

            launch { handler.handle() }

            println("Clientes conectados: ${clients.size}/${config.maxClients}")
        }
    }

    // Método Eliminar Cliente
    fun removeClient(handler: ClientHandler) {
        clients.remove(handler.id)
        println("Clientes conectados: ${clients.size}/${config.maxClients}")
    }

    // Método Parar Servidor
    fun stop() {
        println("\n Cerrando servidor...")
        clients.values.forEach { it.disconnect() }
        if (::serverSocket.isInitialized) serverSocket.close()
        println("Servidor cerrado")
    }
}
