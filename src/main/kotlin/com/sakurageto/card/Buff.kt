package com.sakurageto.card

import com.sakurageto.card.BufTag.*
import com.sakurageto.gamelogic.GameStatus

class CostBuff(
    val cardNumber: Int, var counter: Int, val tag: BufTag,
    val condition: suspend (PlayerEnum, GameStatus, Card) -> Boolean,
    val effect: suspend (Int, PlayerEnum, GameStatus) -> Int
)


class Buff(
    val cardNumber: Int, var counter: Int, val tag: BufTag,
    val condition: suspend (PlayerEnum, GameStatus, MadeAttack) -> Boolean,
    val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
)

class OtherBuff(
    val cardNumber: Int, var counter: Int, val tag: OtherBuffTag,
    val condition: suspend (PlayerEnum, GameStatus, MadeAttack) -> Boolean,
    val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
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
            PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED -> attackBuff[5].add(buf)
        }
    }

    private fun ArrayDeque<Buff>.removeByNumber(card_number: Int){
        this.removeIf {
            it.cardNumber == card_number
        }
    }

    fun removeAttackBuff(tag: BufTag, card_number: Int) {
        when (tag) {
            CARD_CHANGE -> attackBuff[0].removeByNumber(card_number)
            INSERT -> attackBuff[1].removeByNumber(card_number)
            CHANGE_EACH -> attackBuff[2].removeByNumber(card_number)
            MULTIPLE -> attackBuff[3].removeByNumber(card_number)
            DIVIDE -> attackBuff[4].removeByNumber(card_number)
            PLUS_MINUS -> attackBuff[5].removeByNumber(card_number)
            CARD_CHANGE_IMMEDIATE -> attackBuff[0].removeByNumber(card_number)
            INSERT_IMMEDIATE -> attackBuff[1].removeByNumber(card_number)
            CHANGE_EACH_IMMEDIATE -> attackBuff[2].removeByNumber(card_number)
            MULTIPLE_IMMEDIATE -> attackBuff[3].removeByNumber(card_number)
            DIVIDE_IMMEDIATE -> attackBuff[4].removeByNumber(card_number)
            PLUS_MINUS_IMMEDIATE -> attackBuff[5].removeByNumber(card_number)
            PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED -> attackBuff[5].removeByNumber(card_number)
        }
    }

    fun clearUnUsedBuff(){
        for (index in 0 until buffQueueNumber){
            if(index == 5){
                for (i in 0 until attackBuff[index].size){
                    val now = attackBuff[5].removeFirst()
                    if(now.tag == PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED){
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
                attackBuff[index].addLast(now)
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
            RangeBufTag.CARD_CHANGE -> rangeBuff[INDEX_CARD_CHANGE].add(Pair(buffNumber, buff))
            RangeBufTag.CHANGE -> rangeBuff[INDEX_CHANGE].add(Pair(buffNumber, buff))
            RangeBufTag.CHANGE_AFTER -> rangeBuff[INDEX_CHANGE_AFTER].add(Pair(buffNumber, buff))
            RangeBufTag.ADD -> rangeBuff[INDEX_ADD].add(Pair(buffNumber, buff))
            RangeBufTag.DELETE -> rangeBuff[INDEX_DELETE].add(Pair(buffNumber, buff))
            RangeBufTag.PLUS -> rangeBuff[INDEX_PLUS].add(Pair(buffNumber, buff))
            RangeBufTag.MINUS -> rangeBuff[INDEX_MINUS].add(Pair(buffNumber, buff))
            RangeBufTag.CARD_CHANGE_IMMEDIATE -> rangeBuff[INDEX_CARD_CHANGE].add(Pair(buffNumber, buff))
            RangeBufTag.CHANGE_IMMEDIATE -> rangeBuff[INDEX_CHANGE].add(Pair(buffNumber, buff))
            RangeBufTag.CHANGE_AFTER_IMMEDIATE -> rangeBuff[INDEX_CHANGE_AFTER].add(Pair(buffNumber, buff))
            RangeBufTag.ADD_IMMEDIATE -> rangeBuff[INDEX_ADD].add(Pair(buffNumber, buff))
            RangeBufTag.DELETE_IMMEDIATE -> rangeBuff[INDEX_DELETE].add(Pair(buffNumber, buff))
            RangeBufTag.PLUS_IMMEDIATE -> rangeBuff[INDEX_PLUS].add(Pair(buffNumber, buff))
            RangeBufTag.MINUS_IMMEDIATE -> rangeBuff[INDEX_MINUS].add(Pair(buffNumber, buff))
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
                otherBuff[index].addLast(now)
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
                val now = array[index].first()
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
    /***
     * this tag is needed to not affect buff at now attack(different with not immediate buff)
     * but not remove when unused(different with immediate buff)
     */
    PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED
}

enum class RangeBufTag {
    CARD_CHANGE,
    CHANGE,
    CHANGE_AFTER,
    ADD,
    DELETE,
    PLUS,
    MINUS,
    CARD_CHANGE_IMMEDIATE,
    CHANGE_IMMEDIATE,
    CHANGE_AFTER_IMMEDIATE,
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