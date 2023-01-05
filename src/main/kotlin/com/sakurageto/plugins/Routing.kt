package com.sakurageto.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import com.sakurageto.RoomInformation

fun Application.configureRouting() {
    val room_number_range = (2..10000)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/makeroom") {
            var now_room_number = room_number_range.random()
            while (RoomInformation.room_number_hashmap.containsKey(now_room_number)){
                now_room_number = room_number_range.random()
            }
            RoomInformation.room_number_hashmap.put(now_room_number, true)
            RoomInformation.room_wait_hashmap.put(now_room_number, true)
            call.respondText(now_room_number.toString())
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
