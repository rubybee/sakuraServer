package com.sakurageto.card

import com.sakurageto.card.BufTag.*
import com.sakurageto.gamelogic.GameStatus

class CostBuff(val cardNumber: Int, var counter: Int, val tag: BufTag, val condition: suspend (PlayerEnum, GameStatus, Card) -> Boolean, val effect: suspend (Int) -> Int
)


class Buff(
 val cardNumber: Int, var counter: Int, val tag: BufTag, val condition: suspend (PlayerEnum, GameStatus, MadeAttack) -> Boolean, val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
)

class OtherBuff(
    val cardNumber: Int, var counter: Int, val tag: OtherBuffTag, val condition: suspend (PlayerEnum, GameStatus, MadeAttack) -> Boolean, val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
)

class AttackBuffQueue() {
    companion object {
        const val buffQueueNumber = 6
    }

    private var attackBuff: Array<ArrayDeque<Buff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
    )

    fun addAttackBuff(buf: Buff) {
        when (buf.tag) {
            CARD_CHANGE -> attackBuff[0].add(buf)
            INSERT -> attackBuff[1].add(buf)
            CHANGE_EACH -> attackBuff[2].add(buf)
            MULTIPLE -> attackBuff[3].add(buf)
            DIVIDE -> attackBuff[4].add(buf)
            PLUS_MINUS -> attackBuff[5].add(buf)
            CARD_CHANGE_IMMEDIATE -> attackBuff[0].add(buf)
            INSERT_IMMEDIATE -> attackBuff[1].add(buf)
            CHANGE_EACH_IMMEDIATE -> attackBuff[2].add(buf)
            MULTIPLE_IMMEDIATE -> attackBuff[3].add(buf)
            DIVIDE_IMMEDIATE -> attackBuff[4].add(buf)
            PLUS_MINUS_IMMEDIATE -> attackBuff[5].add(buf)
        }
    }

    fun clearBuff(){
        for (queue in attackBuff){
            queue.clear()
        }
    }

    fun addAllBuff(buffQueue: AttackBuffQueue){
        for(index in 0 until buffQueueNumber){
            attackBuff[index].addAll(buffQueue.attackBuff[index])
        }
    }

    suspend fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<Buff> ){
        for(buff in attackBuff[index]){
            if(buff.condition(player, game_status, madeAttack)){
                tempQueue.add(buff)
            }
        }
    }

    suspend fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<Buff>, receiveQueue: AttackBuffQueue){
        for(i in 1..attackBuff[index].size){
            val now = attackBuff[index].first()
            attackBuff[index].removeFirst()
            if(now.condition(player, game_status, madeAttack)){
                tempQueue.add(now)
            }
            else{
                receiveQueue.attackBuff[index].add(now)
            }
        }
    }
}

class RangeBuff(
    val cardNumber: Int, var counter: Int, val tag: RangeBufTag, val condition: (PlayerEnum, GameStatus, MadeAttack) -> Boolean, val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
)

class RangeBuffQueue() {
    companion object {
        const val buffQueueNumber = 6
    }

    private var rangeBuff: Array<ArrayDeque<RangeBuff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque()
    )

    fun addRangeBuff(buff: RangeBuff) {
        when (buff.tag) {
            RangeBufTag.CARD_CHANGE -> rangeBuff[0].add(buff)
            RangeBufTag.CHANGE -> rangeBuff[1].add(buff)
            RangeBufTag.ADD -> rangeBuff[2].add(buff)
            RangeBufTag.DELETE -> rangeBuff[3].add(buff)
            RangeBufTag.PLUS -> rangeBuff[4].add(buff)
            RangeBufTag.MINUS -> rangeBuff[5].add(buff)
            RangeBufTag.CARD_CHANGE_IMMEDIATE -> rangeBuff[0].add(buff)
            RangeBufTag.CHANGE_IMMEDIATE -> rangeBuff[1].add(buff)
            RangeBufTag.ADD_IMMEDIATE -> rangeBuff[2].add(buff)
            RangeBufTag.DELETE_IMMEDIATE -> rangeBuff[3].add(buff)
            RangeBufTag.PLUS_IMMEDIATE -> rangeBuff[4].add(buff)
            RangeBufTag.MINUS_IMMEDIATE -> rangeBuff[5].add(buff)
        }
    }

    fun clearBuff(){
        for (queue in rangeBuff){
            queue.clear()
        }
    }

    fun addAllBuff(buffQueue: RangeBuffQueue){
        for(index in 0 until buffQueueNumber){
            rangeBuff[index].addAll(buffQueue.rangeBuff[index])
        }
    }

    fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<RangeBuff>){
        for(buff in rangeBuff[index]){
            if(buff.condition(player, game_status, madeAttack)){
                tempQueue.add(buff)
            }
        }
    }

    fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<RangeBuff>, receiveQueue: RangeBuffQueue){
        for(i in 1..rangeBuff[index].size){
            val now = rangeBuff[index].first()
            rangeBuff[index].removeFirst()
            if(now.condition(player, game_status, madeAttack)){
                tempQueue.add(now)
            }
            else{
                receiveQueue.rangeBuff[index].add(now)
            }
        }
    }

    fun cleanNotUsedBuff(buffQueue: RangeBuffQueue){
        buffQueue.addAllBuff(this)
    }
}

class OtherBuffQueue() {
    companion object {
        const val buffQueueNumber = 2
    }

    private var otherBuff: Array<ArrayDeque<OtherBuff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
    )

    fun addOtherBuff(buf: OtherBuff){
        when (buf.tag){
            OtherBuffTag.GET -> otherBuff[0].add(buf)
            OtherBuffTag.LOSE -> otherBuff[1].add(buf)
            OtherBuffTag.GET_IMMEDIATE -> otherBuff[0].add(buf)
            OtherBuffTag.LOSE_IMMEDIATE -> otherBuff[1].add(buf)
        }
    }

    fun clearBuff(){
        for (queue in otherBuff){
            queue.clear()
        }
    }

    fun addAllBuff(buffQueue: OtherBuffQueue){
        for(index in 0 until buffQueueNumber){
            otherBuff[index].addAll(buffQueue.otherBuff[index])
        }
    }

    suspend fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<OtherBuff> ){
        for(buff in otherBuff[index]){
            if(buff.condition(player, game_status, madeAttack)){
                tempQueue.add(buff)
            }
        }
    }

    suspend fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<OtherBuff>, receiveQueue: OtherBuffQueue){
        for(i in 1..otherBuff[index].size){
            val now = otherBuff[index].first()
            otherBuff[index].removeFirst()
            if(now.condition(player, game_status, madeAttack)){
                tempQueue.add(now)
            }
            else{
                receiveQueue.otherBuff[index].add(now)
            }
        }
    }
}

fun cleanCostBuff(array: Array<ArrayDeque<CostBuff>>){
    for(index in array.indices){
        if(index % 2 == 0){
            array[index].clear()
        }
        else{
            for(i in array[index].indices){
                var now = array[index].first()
                array[index].removeFirst()
                if(now.counter < 0){
                    now.counter *= -1
                    now.counter -= 1
                    if(now.counter == 0){
                        continue
                    }
                }
                array[index].addLast(now)
            }
        }
    }
}

fun cleanCostTempBuff(array: Array<ArrayDeque<CostBuff>>){
    for(index in array.indices){
        if(index % 2 == 0){
            array[index].clear()
        }
        else{
            for(buff in array[index]){
                if(buff.counter < 0){
                    buff.counter *= -1
                }
            }
        }
    }
}

enum class BufTag {
    CARD_CHANGE,
    INSERT,
    CHANGE_EACH,
    MULTIPLE,
    DIVIDE,
    PLUS_MINUS,
    CARD_CHANGE_IMMEDIATE,
    INSERT_IMMEDIATE,
    CHANGE_EACH_IMMEDIATE,
    MULTIPLE_IMMEDIATE,
    DIVIDE_IMMEDIATE,
    PLUS_MINUS_IMMEDIATE,
}

enum class RangeBufTag {
    CARD_CHANGE,
    CHANGE,
    ADD,
    DELETE,
    PLUS,
    MINUS,
    CARD_CHANGE_IMMEDIATE,
    CHANGE_IMMEDIATE,
    ADD_IMMEDIATE,
    DELETE_IMMEDIATE,
    PLUS_IMMEDIATE,
    MINUS_IMMEDIATE
}

enum class OtherBuffTag{
    GET,
    LOSE,
    GET_IMMEDIATE,
    LOSE_IMMEDIATE
}