package com.sakurageto.plugins

import com.sakurageto.protocol.Connection
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import io.ktor.server.application.*
import com.sakurageto.protocol.RoomInformation
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameFactory
import kotlinx.coroutines.delay
import java.lang.NumberFormatException
import org.slf4j.LoggerFactory
import kotlin.time.Duration.Companion.seconds

fun Application.configureSockets() {
    install(WebSockets) {
        timeout = 40.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/waitroom/{roomnumber}") {
            val roomNumber = call.parameters["roomnumber"]?.toIntOrNull()
            var matchSuccess = false

            if (roomNumber == null) {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
                return@webSocket
            }

            RoomInformation.roomHashMap[roomNumber]?.let { room ->
                try {
                    while (true) {
                        if (!room.waitStatus) {
                            this.send("player match success")
                            matchSuccess = true
                            close(CloseReason(CloseReason.Codes.NORMAL, "player match success"))
                            break
                        }
                        delay(1000)
                    }
                } finally {
                    if (!matchSuccess) {
                        RoomInformation.roomHashMap.remove(roomNumber)
                        close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "time out"))
                    }
                }
            } ?: close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
        }

        webSocket("/play/{roomNumber}") {
            try {
                val logger = LoggerFactory.getLogger("WebSocketLogger")
                call.parameters["roomNumber"]?.toInt()?.let {
                    RoomInformation.roomHashMap[it]?.let { room ->
                        if(room.waitStatus){
                            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
                        }
                        else{
                            if (room.firstUserConnection == null){
                                room.firstUserConnection = Connection(this).apply {
                                    socketPlayer = PlayerEnum.PLAYER1; roomNumber = it
                                }
                                while(true){
                                    if(room.firstUserConnection?.gameEnd == true){
                                        break
                                    }
                                    delay(1000)
                                }
                            }
                            else{
                                room.secondUserConnection = Connection(this).apply {
                                    socketPlayer = PlayerEnum.PLAYER2; roomNumber = it
                                }
                                val now1 = room.firstUserConnection!!
                                val now2 = room.secondUserConnection!!
                                val game = GameFactory(it, now1, now2).makeGame()
                                logger.info("GameRoom Num$it: start game")
                                game.start()
                                RoomInformation.endRoom(it)
                            }
                        }
                    }?: close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
                }?: close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }catch (_ : NumberFormatException){
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }
        }

        webSocket("/reconnect/{roomNumber}/{userCode}") {
            try {
                call.parameters["roomNumber"]?.toInt()?.let { roomNumber ->
                    RoomInformation.roomHashMap[roomNumber]?.let { room ->
                        call.parameters["userCode"]?.toInt()?.let { userCode ->
                            if(room.firstUserCode == userCode){
                                room.firstUserConnection?.session?.incoming?.cancel()
                                room.firstUserConnection?.session?.close()
                                while(room.firstUserConnection?.disconnectTime == -1L){
                                    if(room.firstUserConnection?.gameEnd == true){
                                        break
                                    }
                                    delay(500)
                                }
                                val logger = LoggerFactory.getLogger("WebSocketLogger")
                                logger.info("GameRoom Num$roomNumber: reconnect Player1")
                                room.firstUserConnection?.session = this
                                room.firstUserConnection?.disconnectTime = -1L

                                val nowSession = room.firstUserConnection?.session
                                while(true){
                                    if(nowSession != room.firstUserConnection?.session
                                        || room.firstUserConnection?.gameEnd == true) {
                                        break
                                    }
                                    delay(5000)
                                }
                            }
                            else if(room.secondUserCode == userCode){
                                room.secondUserConnection?.session?.incoming?.cancel()
                                room.secondUserConnection?.session?.close()
                                while(room.secondUserConnection?.disconnectTime == -1L){
                                    if(room.secondUserConnection?.gameEnd == true){
                                        break
                                    }
                                    delay(500)
                                }
                                val logger = LoggerFactory.getLogger("WebSocketLogger")
                                logger.info("GameRoom Num$roomNumber: reconnect Player2")
                                room.secondUserConnection?.session = this
                                room.secondUserConnection?.disconnectTime = -1L

                                val nowSession = room.secondUserConnection?.session
                                while(true){
                                    if(nowSession != room.secondUserConnection?.session
                                        || room.secondUserConnection?.gameEnd == true) break
                                    delay(5000)
                                }
                            }
                            else{
                                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid user code"))
                            }
                        }?: run{
                            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "can not find user code"))
                        }
                    }?: run{
                        close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "can not find room information"))
                    }
                }?: close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }catch (_ : NumberFormatException) {
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "it is not number format"))
            }
        }
    }
}
