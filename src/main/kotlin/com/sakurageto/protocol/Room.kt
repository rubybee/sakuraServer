package com.sakurageto.protocol

import com.sakurageto.gamelogic.GameStatus

class Room(private val madeTime: Long){
    var waitStatus: Boolean = true

    var firstUserConnection: Connection? = null
    var secondUserConnection: Connection? = null

    var firstUserCode: Int = -1
    var secondUserCode: Int = -1

    var game: GameStatus? = null

    fun isItExpirationWhenWait(nowTime: Long): Boolean{
        if(nowTime - madeTime > 600000){
            return true
        }
        return false
    }

    fun isItExpirationWhenGameDoing(nowTime: Long): Boolean{
        if(nowTime - madeTime > 30000000){
            return true
        }
        return false
    }
}