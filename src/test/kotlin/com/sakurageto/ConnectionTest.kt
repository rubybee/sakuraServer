package com.sakurageto

import com.sakurageto.card.PlayerEnum
import com.sakurageto.protocol.Connection
import com.sakurageto.protocol.SakuraData
import io.ktor.server.websocket.*

class ConnectionTest(private val player: PlayerEnum, session: DefaultWebSocketServerSession): Connection(session) {
    private val receiveData = ArrayDeque<String>()

    fun putReceiveData(data: SakuraData){
        receiveData.addLast(data.toString())
    }

    override suspend fun receive(): String {
        logger.info("(GameRoom${roomNumber}) receive message from ${player}: ${receiveData.first()}")
        return receiveData.removeFirst()
    }

    override suspend fun send(data: String){
        logger.info("(GameRoom${roomNumber}) send message to ${player}: $data")
    }
}