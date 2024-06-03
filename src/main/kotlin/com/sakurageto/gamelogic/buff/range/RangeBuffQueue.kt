package com.sakurageto.gamelogic.buff.range

import com.sakurageto.card.MadeAttack
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameStatus

class RangeBuffQueue {
    companion object {
        const val buffQueueNumber = 7
        const val INDEX_CARD_CHANGE = 0
        const val INDEX_CHANGE = 1
        const val INDEX_CHANGE_AFTER = 2
        const val INDEX_ADD = 3
        const val INDEX_DELETE = 4
        const val INDEX_PLUS = 5
        const val INDEX_MINUS = 6
    }

    private val buffNumberSet = mutableSetOf<Int>()
    private var rangeBuff: Array<ArrayDeque<Pair<Int, RangeBuff>>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque()
    )

    fun addRangeBuff(buffNumber: Int, buff: RangeBuff) {
        when (buff.tag) {
            RangeBuffTag.CARD_CHANGE -> rangeBuff[INDEX_CARD_CHANGE].add(Pair(buffNumber, buff))
            RangeBuffTag.CHANGE -> rangeBuff[INDEX_CHANGE].add(Pair(buffNumber, buff))
            RangeBuffTag.CHANGE_AFTER -> rangeBuff[INDEX_CHANGE_AFTER].add(Pair(buffNumber, buff))
            RangeBuffTag.ADD -> rangeBuff[INDEX_ADD].add(Pair(buffNumber, buff))
            RangeBuffTag.DELETE -> rangeBuff[INDEX_DELETE].add(Pair(buffNumber, buff))
            RangeBuffTag.PLUS -> rangeBuff[INDEX_PLUS].add(Pair(buffNumber, buff))
            RangeBuffTag.MINUS -> rangeBuff[INDEX_MINUS].add(Pair(buffNumber, buff))
            RangeBuffTag.CARD_CHANGE_IMMEDIATE -> rangeBuff[INDEX_CARD_CHANGE].add(Pair(buffNumber, buff))
            RangeBuffTag.CHANGE_IMMEDIATE -> rangeBuff[INDEX_CHANGE].add(Pair(buffNumber, buff))
            RangeBuffTag.CHANGE_AFTER_IMMEDIATE -> rangeBuff[INDEX_CHANGE_AFTER].add(Pair(buffNumber, buff))
            RangeBuffTag.ADD_IMMEDIATE -> rangeBuff[INDEX_ADD].add(Pair(buffNumber, buff))
            RangeBuffTag.DELETE_IMMEDIATE -> rangeBuff[INDEX_DELETE].add(Pair(buffNumber, buff))
            RangeBuffTag.PLUS_IMMEDIATE -> rangeBuff[INDEX_PLUS].add(Pair(buffNumber, buff))
            RangeBuffTag.MINUS_IMMEDIATE -> rangeBuff[INDEX_MINUS].add(Pair(buffNumber, buff))
        }
    }

    fun clearBuff(){
        for (queue in rangeBuff){
            queue.clear()
        }
    }

    fun addAllBuff(buffQueue: RangeBuffQueue){
        for(index in 0 until buffQueueNumber){
            buffQueue.rangeBuff[index].forEach {
                if(it.first !in buffNumberSet){
                    buffNumberSet.add(it.first)
                    rangeBuff[index].add(it)
                }
            }
        }
    }

    fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<RangeBuff>){
        for((_, buff) in rangeBuff[index]){
            if(buff.condition(player, game_status, madeAttack)){
                tempQueue.add(buff)
            }
        }
    }

    fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<RangeBuff>, receiveQueue: RangeBuffQueue){
        for(i in 1..rangeBuff[index].size){
            val (number, buff) = rangeBuff[index].first()
            rangeBuff[index].removeFirst()
            if(buff.condition(player, game_status, madeAttack)){
                tempQueue.add(buff)
                rangeBuff[index].addLast(Pair(number, buff))
                if(buff.counter == 999){
                    receiveQueue.rangeBuff[index].add(Pair(number, buff))
                }
            }
            else{
                receiveQueue.rangeBuff[index].add(Pair(number, buff))
            }
        }
    }

    fun cleanNotUsedBuff(buffQueue: RangeBuffQueue){
        buffQueue.addAllBuff(this)
    }
}