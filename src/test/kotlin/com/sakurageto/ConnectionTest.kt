package com.sakurageto

import com.sakurageto.protocol.SakuraData
import io.ktor.server.websocket.*

class ConnectionTest(session: DefaultWebSocketServerSession): Connection(session) {
    private val receiveData = ArrayDeque<String>()

    fun putReceiveData(data: SakuraData){
        receiveData.addLast(data.toString())
    }

    override suspend fun receive(): String {
        logger.info("(GameRoom${roomNumber}) send message to ${socketPlayer}: ${receiveData.first()}")
        return receiveData.removeFirst()
    }

    override suspend fun send(data: String){
        logger.info("(GameRoom${roomNumber}) send message to ${socketPlayer}: $data")
    }
}