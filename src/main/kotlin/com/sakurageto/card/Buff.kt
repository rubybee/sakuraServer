package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus

class AttackBuff(
 val cardName: CardName, var counter: Int, val tag: AttackBufTag, val condition: (PlayerEnum, GameStatus) -> Boolean, val effect: (MadeAttack) -> Unit
) {

}

class RangeBuff(
    val cardName: CardName, var counter: Int, val tag: RangeBufTag, val condition: (PlayerEnum, GameStatus) -> Boolean, val effect: (MadeAttack) -> Unit
) {

}

fun cleanAttackBuff(array: Array<ArrayDeque<AttackBuff>>){

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

fun cleanAttackTempBuff(array: Array<ArrayDeque<AttackBuff>>){
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