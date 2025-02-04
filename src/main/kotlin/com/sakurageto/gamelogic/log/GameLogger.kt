package com.sakurageto.gamelogic.log

import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.protocol.LocationEnum

class GameLogger {
    private val gameLogQueue = ArrayDeque<GameLog>()

    fun insert(gameLog: GameLog){
        gameLogQueue.addLast(gameLog)
    }

    fun reset(){
        gameLogQueue.clear()
    }

    fun playerUseCardNumber(player: PlayerEnum): Int{
        var result = 0
        for(log in gameLogQueue){
            if(log.player == player && log.isTextUseCard()) result +=1
        }
        return result
    }

    fun checkThisCardUseInCover(player: PlayerEnum, card_number: Int): Boolean{
        for (log in gameLogQueue.asReversed()){
            if(log.player == player){
                if(card_number == log.number1){
                    if(log.text == LogEnum.USE_CARD_IN_COVER || log.text == LogEnum.USE_CARD_IN_COVER_AND_REACT){
                        return true
                    }
                    else if(log.isTextUseCard()){
                        return false
                    }
                }
            }
        }
        return false
    }

    fun checkThisCardUseInSoldier(player: PlayerEnum, card_number: Int): Boolean{
        for (log in gameLogQueue.asReversed()){
            if(log.player == player && log.number1 == card_number && log.isTextUseCard()){
                return log.text == LogEnum.USE_CARD_IN_SOLDIER || log.text == LogEnum.USE_CARD_IN_SOLDIER_PERJURE
            }
        }
        return false
    }

    fun checkThisTurnGetAuraDamage(player: PlayerEnum): Boolean{
        for (log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.GET_AURA_DAMAGE) return true
        }
        return false
    }

    fun checkThisTurnDoAttack(player: PlayerEnum): Boolean{
        for(log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.ATTACK) return true
        }
        return false
    }

    fun checkThisPhaseDoAttack(player: PlayerEnum): Boolean{
        for(log in gameLogQueue.asReversed()){
            if(log.text.isPhaseLog()) break
            if(log.player == player && log.text == LogEnum.ATTACK) return true
        }
        return false
    }

    fun checkThisTurnDoAttackNotSpecial(player: PlayerEnum): Boolean{
        for(log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.ATTACK && log.number2 != GameLog.ATTACK_NUMBER_SPECIAL) return true
        }
        return false
    }

    fun checkThisTurnUseCardCondition(player: PlayerEnum, filter: (Int, Int) -> Int): Boolean{
        for(log in gameLogQueue){
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
        for(log in gameLogQueue){
            if(log.player == player && log.isTextUseCard() && filter(log.number1)){
                return true
            }
        }
        return false
    }

    fun countCardUseCount(player: PlayerEnum, card_number: Int): Int{
        var count = 0
        for(log in gameLogQueue){
            if(log.player == player && log.isTextUseCard() && log.number1 == card_number){
                count += 1
            }
        }
        return count
    }

    fun checkThisCardUsed(player: PlayerEnum, card_number: Int): Boolean{
        for(log in gameLogQueue.asReversed()){
            if(log.player == player && log.isTextUseCard() && log.number1 == card_number) return true
        }
        return false
    }

    fun checkUseCentrifugal(player: PlayerEnum): Boolean{
        for(log in gameLogQueue.asReversed()){
            if(log.player == player && log.text == LogEnum.USE_CENTRIFUGAL) return true
        }
        return false
    }

    fun checkThisTurnAttackNumber(player: PlayerEnum): Int{
        var number = 0
        for(log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.ATTACK) number += 1
        }
        return number
    }

    fun checkThisTurnTransform(player: PlayerEnum): Boolean{
        for(log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.TRANSFORM) return true
        }
        return false
    }

    fun isThisAttackFirst(player: PlayerEnum, card_number: Int): Boolean{
        var check = true
        for(log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.ATTACK){
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
        for(log in gameLogQueue){
            if(log.text == LogEnum.ATTACK) count += 1
            else if(log.isTextUseCard() && log.boolean2) count += 1
        }

        return if(flipped){
            count >= 5
        } else {
            count >= 2
        }
    }

    fun checkSakuraWave(): Boolean{
        val store = HashMap<Int, Int>()
        for(log in gameLogQueue){
            if(log.text == LogEnum.MOVE_TOKEN){
                when(log.number1){
                    GameLog.IGNORE -> {}
                    GameLog.BASIC_OPERATION -> {
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
            else if(log.text == LogEnum.END_EFFECT){
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
        for(log in gameLogQueue){
            if(log.text == LogEnum.MOVE_TOKEN){
                when(log.number1){
                    GameLog.IGNORE -> {}
                    GameLog.BASIC_OPERATION -> {
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
            else if(log.text == LogEnum.END_EFFECT){
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
        for(log in gameLogQueue){
            if(log.text == LogEnum.MOVE_TOKEN){
                when(log.number1){
                    GameLog.IGNORE, GameLog.BASIC_OPERATION -> {}
                    else -> {
                        if(log.resource == LocationEnum.LIFE_YOUR){
                            storeFrom[log.number1] = storeFrom[log.number1]?.let{
                                it + log.number2
                            }?: run {
                                log.number2
                            }
                        }
                        else if(log.destination == LocationEnum.LIFE_YOUR){
                            storeTo[log.number1] = storeTo[log.number1]?.let{
                                it + log.number2
                            }?: run {
                                log.number2
                            }
                        }
                    }
                }
            }
            else if(log.text == LogEnum.END_EFFECT){
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
        for(log in gameLogQueue){
            if(log.text == LogEnum.MOVE_TOKEN){
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
        for(log in gameLogQueue){
            if(log.isTextUseCard() && log.boolean) return true
        }
        return false
    }

    fun checkThisTurnIdea(player: PlayerEnum): Boolean{
        for(log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.IDEA) return true
        }
        return false
    }

    fun findGetDamageByThisAttack(attack_number: Int): Pair<Int, Int>{
        for(log in gameLogQueue.asReversed()){
            if(log.number2 == attack_number){
                when (log.text) {
                    LogEnum.GET_AURA_DAMAGE -> {
                        return Pair(log.number1, 0)
                    }
                    LogEnum.GET_LIFE_DAMAGE -> {
                        return Pair(0, log.number1)
                    }
                    LogEnum.DAMAGE_PROCESS_START -> {
                        return Pair(0, 0)
                    }
                    else -> {}
                }
            }
        }
        return Pair(0, 0)
    }

    fun checkThisTurnMoveDustToken(): Boolean{
        for(log in gameLogQueue){
            if(log.text == LogEnum.MOVE_TOKEN && log.resource == LocationEnum.DUST && log.number2 >= 1){
                return true
            }
        }
        return false
    }

    fun checkThisTurnFailDisprove(player: PlayerEnum): Boolean{
        for(log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.FAIL_DISPROVE){
                return true
            }
        }
        return false
    }

    fun checkAhumAttack(playerUseAhum: PlayerEnum, attack_number: Int): Boolean{
        var index = 0
        while (index < gameLogQueue.size){
            val log = gameLogQueue[index]
            if(log.isAhumBasicOperation(playerUseAhum)){
                return false
            }
            else if(log.text == LogEnum.START_PROCESS_ATTACK_DAMAGE && playerUseAhum == log.player){
                val (endIndex, isMove) = isAttackMoveAura(playerUseAhum, index + 1)
                index = endIndex
                while(gameLogQueue[index].text != LogEnum.END_EFFECT){
                    index += 1
                }
                if(isMove){
                    return if(log.number1 == attack_number){
                        !isAhumAttackTwice(playerUseAhum, attack_number, index + 1)
                    } else{
                        false
                    }
                }
            }
            index += 1
        }
        return false
    }

    private fun isAttackMoveAura(playerUseAhum: PlayerEnum, startIndex: Int): Pair<Int, Boolean>{
        var index = startIndex
        while (index < gameLogQueue.size){
            val log = gameLogQueue[index]
            if(log.text == LogEnum.END_EFFECT){
                return Pair(index, false)
            }
            else if(log.isMoveAuraForAttack(playerUseAhum.opposite())){
                return Pair(index, true)
            }
            index += 1
        }
        return Pair(index, false)
    }

    private fun isAhumAttackTwice(playerUseAhum: PlayerEnum, attack_number: Int, startIndex: Int): Boolean{
        var index = startIndex
        while (index < gameLogQueue.size){
            val log = gameLogQueue[index]
            if(log.player == playerUseAhum && log.text == LogEnum.ATTACK && log.number1 == attack_number){
                return true
            }
            index += 1
        }
        return false
    }

    fun checkAhumBasicOperation(ahumPlayer: PlayerEnum): Boolean{
        var index = 0
        while (index < gameLogQueue.size){
            val log = gameLogQueue[index]
            if(log.isAhumBasicOperation(ahumPlayer)){
                return !isAhumBasicOperationTwice(ahumPlayer, index + 1)
            }
            else if(log.text == LogEnum.START_PROCESS_ATTACK_DAMAGE && ahumPlayer == log.player){
                val (endIndex, isMove) = isAttackMoveAura(ahumPlayer, index + 1)
                index = endIndex
                while(gameLogQueue[index].text != LogEnum.END_EFFECT){
                    index += 1
                }
                if(isMove){
                    return false
                }
            }
            index += 1
        }
        return false
    }

    private fun isAhumBasicOperationTwice(playerUseAhum: PlayerEnum, startIndex: Int): Boolean{
        var index = startIndex
        while (index < gameLogQueue.size){
            val log = gameLogQueue[index]
            if(log.isAhumBasicOperation(playerUseAhum)){
                return true
            }
            index += 1
        }
        return false
    }

    fun countGetDamage(player: PlayerEnum): Int{
        var count = 0
        for(log in gameLogQueue){
            if(log.player == player && log.isGetDamageLog()){
                count += 1
            }
        }
        return count
    }

    fun cardUseCounter(player: PlayerEnum, card_number: Int): Int{
        var result = 0
        for (log in gameLogQueue){
            if(log.player == player && log.isTextUseCard()){
                if(log.number1 == card_number){
                    result += 1
                }
            }
        }
        return result
    }

    fun checkThisCardUseWhen(player: PlayerEnum, card_number: Int): Int{
        var useNumber = cardUseCounter(player, card_number)
        var cardUseCounter = 0
        for (log in gameLogQueue){
            if(log.player == player && log.isTextUseCard()){
                cardUseCounter += 1
                if(log.number1 == card_number){
                    useNumber -= 1
                    if(useNumber == 0){
                        return cardUseCounter
                    }
                }
            }
        }
        return -1
    }

    fun isPlayerMakeOverAuraDamageOver3(player: PlayerEnum): Boolean{
        for (log in gameLogQueue){
            if(log.player == player && log.text == LogEnum.ATTACK_DAMAGE){
                if(log.number1 > 3){
                    return true
                }
            }
        }
        return false
    }
}