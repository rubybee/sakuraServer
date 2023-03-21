package com.sakurageto.card

import com.sakurageto.card.BufTag.*
import com.sakurageto.gamelogic.GameStatus

//condition은 어떠한 공격에 어떠한것을 적용한다에서만 사용한다(다음 오라 3데미지 이하인 공격에 적용 등)
//양면 대미지와 대응불가의 경우 CHANGE_EACH_IMMEDIATE에 기입하였다
class CostBuff(val cardNumber: Int, var counter: Int, val tag: BufTag, val condition: suspend (PlayerEnum, GameStatus, Card) -> Boolean, val effect: suspend (Int) -> Int
)


class Buff(
 val cardNumber: Int, var counter: Int, val tag: BufTag, val condition: suspend (PlayerEnum, GameStatus, MadeAttack) -> Boolean, val effect: suspend (MadeAttack) -> Unit
)

class AttackBuffQueue() {
    companion object {
        val buffQueueNumber = 6
    }
    private var attackBuff: Array<ArrayDeque<Buff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque()
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

    suspend fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<Buff> ){
        for(i in 0 until attackBuff[index].size){
            val now = attackBuff[index].first()
            attackBuff[index].removeFirst()
            if(now.condition(player, game_status, madeAttack)){
                now.counter -= 1
                tempQueue.add(now)
            }
            if(now.counter != 0){
                attackBuff[index].addLast(now)
            }
        }
    }
}

class RangeBuff(
    val cardNumber: Int, var counter: Int, val tag: RangeBufTag, val condition: (PlayerEnum, GameStatus, MadeAttack) -> Boolean, val effect: (MadeAttack) -> Unit
)

class RangeBuffQueue() {
    companion object {
        val buffQueueNumber = 6
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

    fun resetCounter(){
        for (queue in rangeBuff){
            for(buff in queue){
                if(buff.counter < 0){
                    buff.counter *= -1
                }
            }
        }
    }

    fun cleanUsedBuff(){
        for(index in 1..buffQueueNumber){
            for(i in 1..rangeBuff[index].size){
                val now = rangeBuff[index].first()
                rangeBuff[index].removeFirst()
                if(now.counter < 0){
                    now.counter *= -1
                    now.counter -= 1
                    if(now.counter == 0){
                        continue
                    }
                }
                rangeBuff[index].addLast(now)
            }
        }
    }

    fun applyBuff(index: Int, player: PlayerEnum, game_status: GameStatus, madeAttack: MadeAttack, tempQueue: ArrayDeque<RangeBuff> ){
        for(i in 1..  rangeBuff[index].size){
            val now = rangeBuff[index].first()
            rangeBuff[index].removeFirst()
            if(now.condition(player, game_status, madeAttack)){
                now.counter *= -1
                tempQueue.add(now)
            }
            if(now.counter != 0){
                rangeBuff[index].addLast(now)
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