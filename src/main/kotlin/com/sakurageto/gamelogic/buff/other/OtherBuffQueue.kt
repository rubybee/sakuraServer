package com.sakurageto.gamelogic.buff.other

import com.sakurageto.card.MadeAttack
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameStatus

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