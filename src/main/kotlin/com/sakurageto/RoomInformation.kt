package com.sakurageto

import com.sakurageto.gamelogic.GameStatus

class Room(madeTime: Long){
    var waitStatus: Boolean = true

    var firstUserConnection: Connection? = null
    var secondUserConnection: Connection? = null

    var firstUserCode: Int = -1
    var secondUserCode: Int = -1

    var game: GameStatus? = null
}

object RoomInformation{
    var roomHashMap = HashMap<Int, Room>()
}