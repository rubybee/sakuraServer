package com.sakurageto.gamelogic

import com.sakurageto.card.PlayerEnum

class Logger {
    private val logQueue = ArrayDeque<Log>()

    fun insert(log: Log){
        logQueue.addLast(log)
    }

    fun reset(){
        logQueue.clear()
    }

    fun playerUseCardNumber(player: PlayerEnum): Int{
        var result = 0
        for(log in logQueue){
            if(log.player == player && (log.text == LogText.USE_CARD || log.text == LogText.USE_CARD_REACT)) result +=1
        }
        return result
    }
}