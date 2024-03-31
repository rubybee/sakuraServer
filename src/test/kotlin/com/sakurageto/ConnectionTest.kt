package com.sakurageto

import com.sakurageto.card.PlayerEnum
import com.sakurageto.protocol.*
import io.ktor.server.websocket.*

class ConnectionTest(private val player: PlayerEnum, session: DefaultWebSocketServerSession): Connection(session) {
    private val receiveData = ArrayDeque<String>()
    private var autoReceiveDataIterator = autoDataList.iterator()

    fun putReceiveData(data: SakuraData){
        receiveData.addLast(data.toString())
    }

    override suspend fun receive(): String {
        if(receiveData.size == 0){
            if(!autoReceiveDataIterator.hasNext()){
                autoReceiveDataIterator = autoDataList.iterator()

            }
            val now =autoReceiveDataIterator.next()
            logger.info("(GameRoom${roomNumber}) receive message from ${player}: $now")
            return now
        }
        else{
            logger.info("(GameRoom${roomNumber}) receive message from ${player}: ${receiveData.first()}")
            return receiveData.removeFirst()
        }
    }

    override suspend fun send(data: String){
        logger.info("(GameRoom${roomNumber}) send message to ${player}: $data")
    }

    companion object{
        private val autoDataList = arrayListOf(
            SakuraBaseData(CommandEnum.SELECT_ONE, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_TWO, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_THREE, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_FOUR, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_NOT, -1).toString(),
            SakuraBaseData(CommandEnum.CHOOSE_AURA, -1).toString(),
            SakuraBaseData(CommandEnum.CHOOSE_LIFE, -1).toString(),
            SakuraBaseData(CommandEnum.REACT_NO, -1).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 1)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 2)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 3)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 4)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 5)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 6)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(1, 0)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(2, 0)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(3, 0)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(4, 0)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(5, 0)).toString(),
            SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(6, 0)).toString(),
        )
    }
}