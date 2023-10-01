package com.sakurageto.plugins

import com.sakurageto.Room
import com.sakurageto.RoomInformation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

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
            call.respondText(nowRoomNumber.toString(), status = HttpStatusCode.OK)
        }

        get("/enterroom/{roomnumber}"){
            try {
                call.parameters["roomnumber"]?.toInt()?.let {
                    val room = RoomInformation.roomHashMap[it]
                    if(room?.waitStatus == true){
                        call.respondText("okay enter room", status = HttpStatusCode.OK)
                        room.waitStatus = false
                    }
                    else {
                        call.respondText("invalid room number", status = HttpStatusCode.OK)
                    }
                }
            }catch (_: NumberFormatException){
            }
        }

        post("/bugreport"){
            val message = call.receive<String>()
            call.respondText("bug report stored correctly", status = HttpStatusCode.Created)

            val currentTime = Date()

            val currentDirectory = System.getProperty("user.dir")
            val directory = File("$currentDirectory/bugreport/")
            if (!directory.exists()) {
                directory.mkdirs() // 디렉터리 생성
            }

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTime)

            var filePath = "$currentDirectory/bugreport/file_$timestamp.txt"
            var fileNumber = 1

            while (File(filePath).exists()) {
                filePath = "file_${timestamp}_$fileNumber.txt"
                fileNumber++
            }

            try {
                val file = File(filePath)
                withContext(Dispatchers.IO) {
                    val fileWriter = FileWriter(file)
                    fileWriter.write(message)
                    fileWriter.close()
                }
            } catch (_: Exception) {
            }
        }
    }
}
