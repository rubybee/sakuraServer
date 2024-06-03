package com.sakurageto.gamelogic.buff.attack

import com.sakurageto.card.MadeAttack
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameStatus

class AttackBuffQueue() {
    companion object {
        const val buffQueueNumber = 6
    }

    private var attackBuff: Array<ArrayDeque<AttackBuff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
    )

    fun addAttackBuff(buf: AttackBuff) {
        when (buf.tag) {
            AttackBuffTag.CARD_CHANGE -> attackBuff[0].add(buf)
            AttackBuffTag.INSERT -> attackBuff[1].add(buf)
            AttackBuffTag.CHANGE_EACH -> attackBuff[2].add(buf)
            AttackBuffTag.MULTIPLE -> attackBuff[3].add(buf)
            AttackBuffTag.DIVIDE -> attackBuff[4].add(buf)
            AttackBuffTag.PLUS_MINUS -> attackBuff[5].add(buf)
            AttackBuffTag.CARD_CHANGE_IMMEDIATE -> attackBuff[0].add(buf)
            AttackBuffTag.INSERT_IMMEDIATE -> attackBuff[1].add(buf)
            AttackBuffTag.CHANGE_EACH_IMMEDIATE -> attackBuff[2].add(buf)
            AttackBuffTag.MULTIPLE_IMMEDIATE -> attackBuff[3].add(buf)
            AttackBuffTag.DIVIDE_IMMEDIATE -> attackBuff[4].add(buf)
            AttackBuffTag.PLUS_MINUS_IMMEDIATE -> attackBuff[5].add(buf)
            AttackBuffTag.PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED -> attackBuff[5].add(buf)
        }
    }

    private fun ArrayDeque<AttackBuff>.removeByNumber(card_number: Int){
        this.removeIf {
            it.cardNumber == card_number
        }
    }

    fun removeAttackBuff(tag: AttackBuffTag, card_number: Int) {
        when (tag) {
            AttackBuffTag.CARD_CHANGE -> attackBuff[0].removeByNumber(card_number)
            AttackBuffTag.INSERT -> attackBuff[1].removeByNumber(card_number)
            AttackBuffTag.CHANGE_EACH -> attackBuff[2].removeByNumber(card_number)
            AttackBuffTag.MULTIPLE -> attackBuff[3].removeByNumber(card_number)
            AttackBuffTag.DIVIDE -> attackBuff[4].removeByNumber(card_number)
            AttackBuffTag.PLUS_MINUS -> attackBuff[5].removeByNumber(card_number)
            AttackBuffTag.CARD_CHANGE_IMMEDIATE -> attackBuff[0].removeByNumber(card_number)
            AttackBuffTag.INSERT_IMMEDIATE -> attackBuff[1].removeByNumber(card_number)
            AttackBuffTag.CHANGE_EACH_IMMEDIATE -> attackBuff[2].removeByNumber(card_number)
            AttackBuffTag.MULTIPLE_IMMEDIATE -> attackBuff[3].removeByNumber(card_number)
            AttackBuffTag.DIVIDE_IMMEDIATE -> attackBuff[4].removeByNumber(card_number)
            AttackBuffTag.PLUS_MINUS_IMMEDIATE -> attackBuff[5].removeByNumber(card_number)
            AttackBuffTag.PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED -> attackBuff[5].removeByNumber(card_number)
        }
    }

    fun clearUnUsedBuff(){
        for (index in 0 until buffQueueNumber){
            if(index == 5){
                for (i in 0 until attackBuff[index].size){
                    val now = attackBuff[5].removeFirst()
                    if(now.tag == AttackBuffTag.PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED){
                        attackBuff[5].addLast(now)
                    }
                }
            }
            else{
                attackBuff[index].clear()
            }
        }
    }

    fun clearBuff(){
        for (buffQueue in attackBuff){
            buffQueue.clear()
        }
    }

    fun addAllBuff(buffQueue: AttackBuffQueue){
        for(index in 0 until buffQueueNumber){
            attackBuff[index].addAll(buffQueue.attackBuff[index])
        }
    }

    suspend fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<AttackBuff> ){
        for(buff in attackBuff[index]){
            if(buff.condition(player, game_status, madeAttack)){
                tempQueue.add(buff)
            }
        }
    }

    suspend fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<AttackBuff>, receiveQueue: AttackBuffQueue){
        for(i in 1..attackBuff[index].size){
            val now = attackBuff[index].first()
            attackBuff[index].removeFirst()
            if(now.condition(player, game_status, madeAttack)){
                tempQueue.add(now)
                attackBuff[index].addLast(now)
            }
            else{
                receiveQueue.attackBuff[index].add(now)
            }
        }
    }
}