package com.sakurageto

class RoomInformation{
    companion object{
        var room_number_hashmap = HashMap<Int, Boolean>()
        var room_wait_hashmap = HashMap<Int, Boolean>()
        var room_connection_hashmap = HashMap<Int, MutableList<Connection>>()
        init{
            room_number_hashmap.put(1, false)
        }
    }
}