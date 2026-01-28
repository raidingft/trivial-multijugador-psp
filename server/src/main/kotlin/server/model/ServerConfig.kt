package server.model

data class ServerConfig(
    val host: String,
    val port: Int,
    val maxClients: Int
)
