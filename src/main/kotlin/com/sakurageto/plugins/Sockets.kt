package com.sakurageto.plugins

import com.sakurageto.Connection
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import com.sakurageto.RoomInformation
import com.sakurageto.card.PlayerEnum
import com.sakurageto.gamelogic.SakuraGame
import kotlinx.coroutines.delay
import java.lang.NumberFormatException

fun Application.configureSockets() {
    install(WebSockets) {
        timeout = Duration.ofSeconds(40)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/waitroom/{roomnumber}") { // websocketSession
            try {
                call.parameters["roomnumber"]?.toInt()?.let {
                    RoomInformation.roomHashMap[it]?.let {room ->
                        while (true){
                            if(!room.waitStatus){
                                this.send("player match success")
                                close(CloseReason(CloseReason.Codes.NORMAL, "player match success"))
                                break
                            }
                            delay(1000)
                        }
                    }?: close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
                }
            }catch (_: NumberFormatException){
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }
        }

        webSocket("/play/{roomNumber}") {
            try {
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
                                    delay(1000)
                                }
                            }
                            else{
                                room.secondUserConnection = Connection(this).apply {
                                    socketPlayer = PlayerEnum.PLAYER2; roomNumber = it
                                }
                                val now1 = room.firstUserConnection!!
                                val now2 = room.secondUserConnection!!
                                val game = SakuraGame(it, now1, now2)
                                game.startGame()
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
                                println("player1 reconnect")
                                room.firstUserConnection?.session?.incoming?.cancel()
                                room.firstUserConnection?.session?.close()
                                while(room.firstUserConnection?.disconnectTime == -1L){
                                    delay(500)
                                }
                                room.firstUserConnection?.session = this
                                room.firstUserConnection?.disconnectTime = -1L
                                while(true){
                                    delay(1000)
                                }
                            }
                            else if(room.secondUserCode == userCode){
                                println("player2 reconnect")
                                room.secondUserConnection?.session?.incoming?.cancel()
                                room.secondUserConnection?.session?.close()
                                while(room.secondUserConnection?.disconnectTime == -1L){
                                    delay(500)
                                }
                                room.secondUserConnection?.session = this
                                room.secondUserConnection?.disconnectTime = -1L
                                while(true){
                                    delay(1000)
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
