package com.sakurageto.protocol

import com.sakurageto.card.basicenum.PlayerEnum
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.onClosed
import kotlinx.coroutines.channels.onSuccess
import kotlinx.coroutines.delay
import org.slf4j.Logger
import org.slf4j.LoggerFactory


open class Connection(open var session: DefaultWebSocketServerSession) {
    var gameEnd: Boolean = false
    var socketPlayer: PlayerEnum = PlayerEnum.PLAYER1
    var roomNumber: Int = -1
    var disconnectTime: Long = -1

    private suspend fun sendData(data: String){
        logger.info("(GameRoom${roomNumber}) send message to ${socketPlayer}: $data")
        session.send(data)
    }

    open suspend fun waitReconnect(){
        disconnectTime = System.currentTimeMillis()
        while (true){
            delay(1000)
            if(disconnectTime == -1L){
                break
            }
        }
    }

    open suspend fun receive(): String {
        while (true){
            session.incoming.receiveCatching().onSuccess { frame ->
                if(frame is Frame.Text){
                    logger.info("(GameRoom${roomNumber}) receive message from ${socketPlayer}: ${frame.readText()}")
                    return frame.readText()
                }
            }.onClosed {
                waitReconnect()
                return receive()
            }
        }
    }

    open suspend fun send(data: String){
        sendData(data)
        receive()
    }
    
    companion object{
        val logger: Logger = LoggerFactory.getLogger("WebSocketLogger")
    }
}