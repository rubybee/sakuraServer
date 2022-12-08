package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus

class CostBuff(val cardName: CardName, var counter: Int, val tag: BufTag, val condition: (PlayerEnum, GameStatus, Card) -> Boolean, val effect: (Int) -> Int
) {

}


class Buff(
 val cardName: CardName, var counter: Int, val tag: BufTag, val condition: (PlayerEnum, GameStatus) -> Boolean, val effect: (MadeAttack) -> Unit
) {

}

class RangeBuff(
    val cardName: CardName, var counter: Int, val tag: RangeBufTag, val condition: (PlayerEnum, GameStatus) -> Boolean, val effect: (MadeAttack) -> Unit
) {

}

fun cleanAttackBuff(array: Array<ArrayDeque<Buff>>){

}

fun cleanRangeBuff(array: Array<ArrayDeque<RangeBuff>>){
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

fun cleanAttackTempBuff(array: Array<ArrayDeque<Buff>>){
    for(index in array.indices){
        if(index % 2 == 0){
            array[index].clear()
        }
    }
}


fun cleanRangeTempBuff(array: Array<ArrayDeque<RangeBuff>>){
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

enum class BufTag(){
    INSERT,
    CHANGE_EACH,
    MULTIPLE,
    DIVIDE,
    PLUS_MINUS,
    INSERT_IMMEDIATE,
    CHANGE_EACH_IMMEDIATE,
    MULTIPLE_IMMEDIATE,
    DIVIDE_IMMEDIATE,
    PLUS_MINUS_IMMEDIATE,
}

enum class RangeBufTag(){
    CHANGE,
    ADD,
    DELETE,
    PLUS,
    MINUS,
    CHANGE_IMMEDIATE,
    ADD_IMMEDIATE,
    DELETE_IMMEDIATE,
    PLUS_IMMEDIATE,
    MINUS_IMMEDIATE
}