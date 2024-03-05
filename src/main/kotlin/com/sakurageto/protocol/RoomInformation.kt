package com.sakurageto.protocol

import java.util.concurrent.ConcurrentHashMap

object RoomInformation{
    var roomHashMap = ConcurrentHashMap<Int, Room>()
}