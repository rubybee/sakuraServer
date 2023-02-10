package com.sakurageto.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import com.sakurageto.RoomInformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun Application.configureRouting() {
    val roomNumberRange = (2..10000)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/makeroom") {
            var nowRoomNumber = roomNumberRange.random()
            while (RoomInformation.room_number_hashmap.containsKey(nowRoomNumber)){
                nowRoomNumber  = roomNumberRange.random()
            }
            RoomInformation.room_number_hashmap[nowRoomNumber] = true
            RoomInformation.room_wait_hashmap[nowRoomNumber] = true
            call.respondText(nowRoomNumber.toString())
        }

        get("/enterroom/{roomnumber}"){
            var roomnumber: Int = call.parameters["roomnumber"]?.toInt() ?: 1
            if ((RoomInformation.room_number_hashmap[roomnumber] == true)
                and (RoomInformation.room_wait_hashmap[roomnumber] == true)) {
                call.respondText("okay enter room")
                RoomInformation.room_wait_hashmap[roomnumber] = false
            } else {
                call.respondText("invalid room number")
            }
        }
    }
}
