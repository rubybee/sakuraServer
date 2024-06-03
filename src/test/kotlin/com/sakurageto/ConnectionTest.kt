package com.sakurageto

import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.protocol.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json

class ConnectionTest(private val player: PlayerEnum, session: DefaultWebSocketServerSession): Connection(session) {
    private val receiveData = ArrayDeque<String>()
    private var autoSelectNapDataIterator = autoSelectNapDataList.iterator()
    private var autoSelectDataIterator = autoSelectDataList.iterator()

    private var beforeData: String = SakuraBaseData(CommandEnum.NULL, -1).toString()

    fun putReceiveData(data: SakuraData){
        receiveData.addLast(data.toString())
    }

    private fun handleSakuraBaseData(data: SakuraBaseData): String {
        return when(data.command){
            CommandEnum.REACT_REQUEST -> SakuraBaseData(CommandEnum.REACT_NO).toString()
            CommandEnum.SELECT_CARD_EFFECT -> {
                if(!autoSelectDataIterator.hasNext()){
                    autoSelectDataIterator = autoSelectDataList.iterator()
                }
                autoSelectDataIterator.next()
            }
            CommandEnum.DECK_RECONSTRUCT_REQUEST -> {
                SakuraBaseData(CommandEnum.DECK_RECONSTRUCT_NO).toString()
            }
            else -> {
                receiveData.removeFirst()
            }
        }
    }

    private fun handleSakuraArrayData(data: SakuraArrayData): String {
        return when(data.command){
            CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, CommandEnum.SELECT_CARD_REASON_INSTALLATION -> {
                SakuraArrayData(data.command, mutableListOf(data.data?.get(0)?: 0)).toString()
            }
            CommandEnum.SELECT_AFTER_CARD_USED_EFFECT_ORDER, CommandEnum.SELECT_END_PHASE_EFFECT_ORDER,
            CommandEnum.SELECT_START_PHASE_EFFECT_ORDER -> {
                SakuraBaseData(data.command, data.data?.get(0)?: 0).toString()
            }
            CommandEnum.CHOOSE_CARD_DAMAGE, CommandEnum.CHOOSE_CHOJO -> {
                SakuraBaseData(CommandEnum.CHOOSE_AURA).toString()
            }
            CommandEnum.SELECT_NAP -> {
                if(!autoSelectNapDataIterator.hasNext()){
                    autoSelectNapDataIterator = autoSelectNapDataList.iterator()
                }
                autoSelectNapDataIterator.next()
            }
            else -> receiveData.removeFirst()
        }
    }

    private fun returnAutoData(): String {
        runCatching {
            Json.decodeFromString<SakuraBaseData>(beforeData)
        }.onSuccess {
            return handleSakuraBaseData(it)
        }.onFailure {
            runCatching {
                Json.decodeFromString<SakuraArrayData>(beforeData)
            }.onSuccess {
                return handleSakuraArrayData(it)
            }.onFailure {
                return receiveData.removeFirst()
            }
        }
        return receiveData.removeFirst()
    }

    override suspend fun receive(): String {
        if(receiveData.size == 0){
            val data = returnAutoData()
            logger.info("(GameRoom${roomNumber}) receive message from ${player}: $data")
            return data
        }
        else{
            logger.info("(GameRoom${roomNumber}) receive message from ${player}: ${receiveData.first()}")
            return receiveData.removeFirst()
        }
    }

    override suspend fun send(data: String){
        if(this.gameEnd) return
        beforeData = data
        logger.info("(GameRoom${roomNumber}) send message to ${player}: $data")
    }

    companion object{
        private var autoSelectDataList = arrayListOf(
            SakuraBaseData(CommandEnum.SELECT_ONE, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_TWO, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_THREE, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_FOUR, -1).toString(),
            SakuraBaseData(CommandEnum.SELECT_NOT, -1).toString(),
        )

        private var autoSelectNapDataList = arrayListOf(
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