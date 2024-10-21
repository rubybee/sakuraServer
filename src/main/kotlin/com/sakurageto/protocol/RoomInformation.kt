package com.sakurageto.protocol

import java.util.concurrent.ConcurrentHashMap

object RoomInformation{
    var roomHashMap = ConcurrentHashMap<Int, Room>()

    suspend fun endRoom(roomNumber: Int){
        val nowRoom = roomHashMap[roomNumber] ?: return
        roomHashMap.remove(roomNumber)
        nowRoom.end()
    }
}