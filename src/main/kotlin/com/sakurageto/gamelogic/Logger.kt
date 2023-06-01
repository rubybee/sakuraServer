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
            if(log.player == player && (log.text == LogText.USE_CARD || log.text == LogText.USE_CARD_REACT ||
                        log.text == LogText.USE_CARD_IN_COVER_AND_REACT || log.text == LogText.USE_CARD_IN_COVER)) result +=1
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

    fun checkThisCardUseInSoldier(player: PlayerEnum, card_number: Int): Boolean{
        for (log in logQueue.asReversed()){
            if(log.player == player && log.number1 == card_number){
                return log.text == LogText.USE_CARD_IN_SOLDIER
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

    fun checkThisTurnDoAttack(player: PlayerEnum): Boolean{
        for(log in logQueue){
            if(log.player == player && log.text == LogText.ATTACK) return true
        }
        return false
    }

    fun checkThisTurnDoAttackNotSpecial(player: PlayerEnum): Boolean{
        for(log in logQueue){
            if(log.player == player && log.text == LogText.ATTACK && log.number2 != 2) return true
        }
        return false
    }

    fun checkThisTurnUseCardCondition(player: PlayerEnum, filter: (Int, Int) -> Int): Boolean{
        for(log in logQueue){
            if(log.player == player && log.isTextUseCard()){
                when(filter(log.number1, log.number2)){
                    0 -> return false
                    1 -> return true
                    2 -> continue
                }

            }
        }
        return true
    }

    fun checkThisTurnUseCard(player: PlayerEnum, filter: (Int) -> Boolean): Boolean{
        for(log in logQueue){
            if(log.player == player && log.isTextUseCard() && filter(log.number1)){
                return true
            }
        }
        return false
    }

    fun countCardUseCount(player: PlayerEnum, card_number: Int): Int{
        var count = 0
        for(log in logQueue){
            if(log.player == player && log.isTextUseCard() && log.number1 == card_number){
                count += 1
            }
        }
        return count
    }

    fun checkThisCardUsed(player: PlayerEnum, card_number: Int): Boolean{
        for(log in logQueue.asReversed()){
            if(log.player == player && log.number1 == card_number) return true
        }
        return false
    }

    fun checkUseCentrifugal(player: PlayerEnum): Boolean{
        for(log in logQueue.asReversed()){
            if(log.player == player && log.text == LogText.USE_CENTRIFUGAL) return true
        }
        return false
    }

    fun checkThisTurnAttackNumber(player: PlayerEnum): Int{
        var number = 0
        for(log in logQueue){
            if(log.player == player && log.text == LogText.ATTACK) number += 1
        }
        return number
    }

    fun checkThisTurnTransform(player: PlayerEnum): Boolean{
        for(log in logQueue){
            if(log.player == player && log.text == LogText.TRANSFORM) return true
        }
        return false
    }
}