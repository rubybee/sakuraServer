package com.sakurageto.gamelogic.buff

import com.sakurageto.card.Card
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.buff.attack.AttackBuffTag
import com.sakurageto.gamelogic.GameStatus

class CostBuff(
    val cardNumber: Int, var counter: Int, val tag: AttackBuffTag,
    val condition: suspend (PlayerEnum, GameStatus, Card) -> Boolean,
    val effect: suspend (Int, PlayerEnum, GameStatus) -> Int
) {
    companion object {
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
    }
}