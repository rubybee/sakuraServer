package com.sakurageto.plugins

import com.sakurageto.Connection
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import com.sakurageto.RoomInformation
import com.sakurageto.gamelogic.SakuraGame
import kotlinx.coroutines.delay

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(180)
        timeout = Duration.ofSeconds(180)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    routing {
        webSocket("/chat") { // websocketSession
            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    outgoing.send(Frame.Text("YOU SAID: $text"))
                    if (text.equals("bye", ignoreCase = true)) {
                        close(CloseReason(CloseReason.Codes.NORMAL, "Client said BYE"))
                    }
                }
            }
        }

        webSocket("/waitroom/{roomnumber}") { // websocketSession
            var roomnumber: Int = call.parameters["roomnumber"]?.toInt() ?: 1
            if((roomnumber == 1) or (RoomInformation.room_number_hashmap[roomnumber] != true)){
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }
            else{
                while (true){
                    if(!RoomInformation.room_wait_hashmap[roomnumber]!!){
                        close(CloseReason(CloseReason.Codes.NORMAL, "player match successly"))
                        break
                    }
                    delay(1000)
                }
            }
        }

        webSocket("/play/{roomnumber}") {
            var roomnumber: Int = call.parameters["roomnumber"]?.toInt() ?: 1
            if ((roomnumber == 1) or (RoomInformation.room_number_hashmap[roomnumber] != true)){
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }
            else if(RoomInformation.room_wait_hashmap[roomnumber] == false){
                if (RoomInformation.room_connection_hashmap[roomnumber]?.isEmpty() != false){
                    val thisconnection = Connection(this)
                    RoomInformation.room_connection_hashmap[roomnumber] = mutableListOf(thisconnection)
                    while(true){
                        delay(200)
                    }

                }
                else{
                    if(RoomInformation.room_connection_hashmap[roomnumber]!!.size == 1){
                        val thisconnection = Connection(this)
                        RoomInformation.room_connection_hashmap[roomnumber]!!.add(thisconnection)
                        val now1 = RoomInformation.room_connection_hashmap[roomnumber]!!.get(0)
                        val now2 = RoomInformation.room_connection_hashmap[roomnumber]!!.get(1)
                        val game = SakuraGame(now1, now2)
                        game.startGame()
                    }
                    else{
                        close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
                    }
                }
            }
            else{
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }

        }
    }
}
