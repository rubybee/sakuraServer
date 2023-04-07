package com.sakurageto.plugins

import com.sakurageto.Room
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import com.sakurageto.RoomInformation
import java.lang.NumberFormatException

fun Application.configureRouting() {
    val roomNumberRange = (2..10000)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/makeroom") {
            var nowRoomNumber = roomNumberRange.random()
            while (RoomInformation.roomHashMap.containsKey(nowRoomNumber)){
                nowRoomNumber  = roomNumberRange.random()
            }
            RoomInformation.roomHashMap[nowRoomNumber] = Room(System.currentTimeMillis())
            call.respondText(nowRoomNumber.toString())
        }

        get("/enterroom/{roomnumber}"){
            try {
                call.parameters["roomnumber"]?.toInt()?.let {
                    val room = RoomInformation.roomHashMap[it]
                    if(room?.waitStatus == true){
                        call.respondText("okay enter room")
                        room.waitStatus = false
                    }
                    else {
                        call.respondText("invalid room number")
                    }
                }
            }catch (_: NumberFormatException){

            }
        }
    }
}
