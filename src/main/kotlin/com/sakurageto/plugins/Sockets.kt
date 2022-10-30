package com.sakurageto.plugins

import com.sakurageto.Connection
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import io.ktor.server.application.*
import com.sakurageto.RoomInformation
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
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
            if((roomnumber == 1) or !(RoomInformation.room_number_hashmap[roomnumber] ?: false)){
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }
            else{
                while (true){
                    if(!RoomInformation.room_wait_hashmap[roomnumber]!!){
                        close(CloseReason(CloseReason.Codes.NORMAL, "player match successly"))
                        break
                    }
                    Thread.sleep(5000)
                }
            }
        }

        webSocket("/play/{roomnumber}") {
            var roomnumber: Int = call.parameters["roomnumber"]?.toInt() ?: 1
            if ((roomnumber == 1) or !(RoomInformation.room_number_hashmap[roomnumber] ?: false)){
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "invalid room number"))
            }
            else if(!(RoomInformation.room_wait_hashmap[roomnumber] ?: true)){
                if (RoomInformation.room_connection_hashmap[roomnumber]?.isEmpty() ?: true){
                    val thisconnection = Connection(this)
                    RoomInformation.room_connection_hashmap[roomnumber] = mutableListOf(thisconnection)
                    while (true){
                        Thread.sleep(5000)
                    }
                }
                else{
                    if(RoomInformation.room_connection_hashmap[roomnumber]!!.size == 1){
                        val thisconnection = Connection(this)
                        RoomInformation.room_connection_hashmap[roomnumber]!!.add(thisconnection)
                        val now1 = RoomInformation.room_connection_hashmap[roomnumber]!!.get(0)
                        val now2 = RoomInformation.room_connection_hashmap[roomnumber]!!.get(1)
                        //make play section

                        //make play section
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
