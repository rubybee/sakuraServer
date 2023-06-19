package com.sakurageto.gamelogic.log

import com.sakurageto.card.PlayerEnum
import com.sakurageto.protocol.LocationEnum
import kotlin.math.max

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
            if(log.player == player && log.isTextUseCard() && log.number1 == card_number) return true
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

    fun isThisAttackFirst(player: PlayerEnum, card_number: Int): Boolean{
        var check = true
        for(log in logQueue){
            if(log.player == player && log.text == LogText.ATTACK){
                if(log.number1 != card_number) return false
                else{
                    if(check) check = false
                    else return false
                }
            }
        }
        return true
    }

    fun checkSaljin(flipped: Boolean): Boolean{
        var count = 0
        for(log in logQueue){
            if(log.text == LogText.ATTACK) count += 1
            else if(log.text == LogText.USE_CARD_REACT) count += 1
        }
        return if(flipped){
            count >= 5
        } else {
            count >= 2
        }
    }

    fun checkSakuraWave(): Boolean{
        val store = HashMap<Int, Int>()
        for(log in logQueue){
            if(log.text == LogText.MOVE_TOKEN){
                when(log.number1){
                    Log.IGNORE -> {}
                    Log.BASIC_OPERATION -> {
                        if(log.number2 >= 3){
                            return true
                        }
                    }
                    else -> {
                        if(log.destination != LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
                            store[log.number1] = store[log.number1]?.let {
                                it + log.number2
                            }?: run {
                                log.number2
                            }
                        }
                    }
                }
            }
            else if(log.text == LogText.END_EFFECT){
                if((store[log.number1]?: 0) >= 3){
                    return true
                }
                store.remove(log.number1)
            }
        }
        return false
    }

    fun checkSakuraWaveFlipped(): Boolean{
        val store = HashMap<Int, Int>()
        for(log in logQueue){
            if(log.text == LogText.MOVE_TOKEN){
                when(log.number1){
                    Log.IGNORE -> {}
                    Log.BASIC_OPERATION -> {
                        if(log.number2 >= 5){
                            return true
                        }
                    }
                    else -> {
                        store[log.number1] = store[log.number1]?.let {
                            it + log.number2
                        }?: run {
                            log.number2
                        }
                    }
                }
            }
            else if(log.text == LogText.END_EFFECT){
                if((store[log.number1]?: 0) >= 5){
                    return true
                }
                store.remove(log.number1)
            }
        }
        return false
    }

    fun checkWhistle(flipped: Boolean): Boolean{
        val value = if(flipped) 2 else 1
        val storeFrom = HashMap<Int, Int>()
        val storeTo = HashMap<Int, Int>()
        for(log in logQueue){
            if(log.text == LogText.MOVE_TOKEN){
                when(log.number1){
                    Log.IGNORE, Log.BASIC_OPERATION -> {}
                    else -> {
                        if(log.resource == LocationEnum.YOUR_LIFE){
                            storeFrom[log.number1] = storeFrom[log.number1]?.let{
                                it + log.number2
                            }?: run {
                                log.number2
                            }
                        }
                        else if(log.destination == LocationEnum.YOUR_LIFE){
                            storeTo[log.number1] = storeTo[log.number1]?.let{
                                it + log.number2
                            }?: run {
                                log.number2
                            }
                        }
                    }
                }
            }
            else if(log.text == LogText.END_EFFECT){
                if((storeFrom[log.number1]?: 0) >= value){
                    return true
                }
                storeFrom.remove(log.number1)

                if((storeTo[log.number1]?: 0) >= value){
                    return true
                }
                storeTo.remove(log.number1)
            }
        }
        return false
    }

    fun checkMyeongJeon(flipped: Boolean): Boolean{
        val value = if(flipped) 2 else 1
        var count = 0
        for(log in logQueue){
            if(log.text == LogText.MOVE_TOKEN){
                if(log.boolean){
                    count += 1
                    if(count >= value){
                        return true
                    }
                }
            }
        }
        return false
    }


    fun checkThisTurnUseFullPower(): Boolean{
        for(log in logQueue){
            if(log.isTextUseCard() && log.boolean) return true
        }
        return false
    }

    fun checkThisTurnIdea(player: PlayerEnum): Boolean{
        for(log in logQueue){
            if(log.player == player && log.text == LogText.IDEA) return true
        }
        return false
    }


}