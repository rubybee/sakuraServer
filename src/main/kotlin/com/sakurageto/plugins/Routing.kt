package com.sakurageto.plugins

import com.sakurageto.protocol.Room
import com.sakurageto.protocol.RoomInformation
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

suspend fun makeBugReportFile(content: String){
    val currentTime = Date()

    val currentDirectory = System.getProperty("user.dir")
    val directory = File("$currentDirectory/bugreport/")
    if (!directory.exists()) {
        directory.mkdirs()
    }

    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(currentTime)

    var filePath = "$currentDirectory/bugreport/file_$timestamp.txt"
    var fileNumber = 1

    while (File(filePath).exists()) {
        filePath = "$currentDirectory/bugreport/file_${timestamp}_$fileNumber.txt"
        fileNumber++
    }

    try {
        val file = File(filePath)
        withContext(Dispatchers.IO) {
            val fileWriter = FileWriter(file)
            fileWriter.write(content)
            fileWriter.close()
        }
    } catch (_: Exception) {
    }
}

fun Application.configureRouting() {
    val roomNumberRange = (2..10000)

    routing {
        get("/") {
            call.respondText("Hello World!")
        }

        get("/makeroom") {
            while (true){
                val nowRoomNumber = roomNumberRange.random()
                if(RoomInformation.roomHashMap.containsKey(nowRoomNumber)){
                    val nowRoom = RoomInformation.roomHashMap[nowRoomNumber]
                    if(nowRoom == null){
                        break
                    }
                    else if(nowRoom.waitStatus){
                        if(nowRoom.isItExpirationWhenWait(System.currentTimeMillis())){
                            nowRoom.firstUserConnection?.session?.close()
                            nowRoom.secondUserConnection?.session?.close()
                        }
                    }
                    else{
                        if(nowRoom.isItExpirationWhenGameDoing(System.currentTimeMillis())){
                            //TODO(expiration when game is running)
                        }
                    }
                }
                else{
                    if(RoomInformation.roomHashMap.putIfAbsent(nowRoomNumber, Room(System.currentTimeMillis())) == null){
                        call.respondText(nowRoomNumber.toString(), status = HttpStatusCode.OK)
                        break
                    }
                }
            }
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
            makeBugReportFile(message)
        }
    }
}
