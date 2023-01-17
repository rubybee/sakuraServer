package com.sakurageto.gamelogic

import com.sakurageto.card.CardName
import com.sakurageto.card.CardSet
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

    fun checkThisCardUseInCover(player: PlayerEnum, card_number: Int): Boolean{
        for (log in logQueue.asReversed()){
            if(log.player == player){
                if(log.text == LogText.USE_CARD_REACT && log.number1 == card_number) return false
                else if(log.text == LogText.USE_CARD && log.number1 == card_number) return false
                else if(log.text == LogText.USE_CARD_IN_COVER && log.number1 == card_number) return true
            }
        }
        return false
    }

    fun checkThisTurnGetAuraDamage(player: PlayerEnum): Boolean{
        for (log in logQueue){
            if(log.player == player && log.text == LogText.GET_AURA_DAMAGE) return true
        }
        return false
    }


}