package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.card.Card
import com.sakurageto.card.CardName
import com.sakurageto.card.PlayerEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.SakuraCardSetSend
import com.sakurageto.protocol.SakuraSendData
import com.typesafe.config.ConfigException.Null
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.logging.LogManager
import javax.swing.text.StyledEditorKit.BoldAction

class SakuraGame(private val player1: Connection, private val player2: Connection) {
    private var game_mode: Int //0 = no ban 1 = pick ban
    private var game_status: GameStatus

    init {
        game_mode = 0
        game_status = GameStatus(PlayerStatus(), PlayerStatus(), player1, player2)
    }

    suspend fun waitUntil(player_id: PlayerEnum, wait_command: CommandEnum): SakuraSendData {
        if (player_id == PlayerEnum.PLAYER1){
            for (frame in player1.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try{
                        val data = Json.decodeFromString<SakuraSendData>(text)
                        if (data.command == wait_command){
                            return data
                        }
                    }finally {
                        continue
                    }

                }
            }
        }

        else {
            for (frame in player2.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try{
                        val data = Json.decodeFromString<SakuraSendData>(text)
                        if (data.command == wait_command){
                            return data
                        }
                    }finally {
                        continue
                    }
                }
            }
        }

        return SakuraSendData(CommandEnum.SELECT_MODE, null)
    }

    suspend fun waitCardSetUntil(player_id: PlayerEnum, wait_command: CommandEnum): SakuraCardSetSend? {
        if (player_id == PlayerEnum.PLAYER1){
            for (frame in player1.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try{
                        val data = Json.decodeFromString<SakuraCardSetSend>(text)
                        if (data.command == wait_command){
                            return data
                        }
                    }finally {
                        continue
                    }
                }
            }
        }

        else {
            for (frame in player2.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    try{
                        val data = Json.decodeFromString<SakuraCardSetSend>(text)
                        if (data.command == wait_command){
                            return data
                        }
                    }finally {
                        continue
                    }
                }
            }
        }

        return null
    }

    suspend fun selectMode(){
        val data = SakuraSendData(CommandEnum.SELECT_MODE, null)
        player1.session.send(Json.encodeToString(data))
        game_mode = waitUntil(PlayerEnum.PLAYER1, CommandEnum.SELECT_MODE).data?.get(0) ?: 0
    }

    suspend fun selectEnd(){
        val data = SakuraSendData(CommandEnum.END_OF_SELECTMODE, mutableListOf(game_mode))
        val send_data = Json.encodeToString(data)
        player1.session.send(send_data)
        player2.session.send(send_data)
    }

    suspend fun selectMegami(){
        TODO("CHANGE GAME_STATUS_PLAYER1, 2")
        val data = SakuraSendData(CommandEnum.SELECT_MEGAMI, null)
        val send_data = Json.encodeToString(data)
        player1.session.send(send_data)
        player2.session.send(send_data)
        val player1_data = waitUntil(PlayerEnum.PLAYER1, CommandEnum.SELECT_MEGAMI)
        val player2_data = waitUntil(PlayerEnum.PLAYER2, CommandEnum.SELECT_MEGAMI)
        if(game_mode == 0){
            game_status.player1.setMegamiSSangjang(player1_data)
            game_status.player2.setMegamiSSangjang(player2_data)
            val end_data_player1 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player1.returnListMegami2())
            val end_data_player2 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player2.returnListMegami2())
            player1.session.send(Json.encodeToString(end_data_player1))
            player2.session.send(Json.encodeToString(end_data_player2))
        }
        else{
            game_status.player1.setMegamiSamSep(player1_data)
            game_status.player2.setMegamiSamSep(player2_data)
            val end_data_player1 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player1.returnListMegami3())
            val end_data_player2 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player2.returnListMegami3())
            player1.session.send(Json.encodeToString(end_data_player1))
            player2.session.send(Json.encodeToString(end_data_player2))
        }
    }

    suspend fun checkMegami(){
        TODO("CHANGE GAME_STATUS_PLAYER1, 2")
        val check_data_player1 = SakuraSendData(CommandEnum.CHECK_MEGAMI, game_status.player1.returnListMegami3())
        val check_data_player2 = SakuraSendData(CommandEnum.CHECK_MEGAMI, game_status.player2.returnListMegami3())
        player1.session.send(Json.encodeToString(check_data_player2))
        player2.session.send(Json.encodeToString(check_data_player1))
    }

    suspend fun selectBan(){
        val select_ban = SakuraSendData(CommandEnum.SELECT_BAN, null)

        player1.session.send(Json.encodeToString(select_ban))
        player2.session.send(Json.encodeToString(select_ban))

        val player1_data = waitUntil(PlayerEnum.PLAYER1, CommandEnum.SELECT_BAN)
        val player2_data = waitUntil(PlayerEnum.PLAYER2, CommandEnum.SELECT_BAN)

        TODO("CHANGE GAME_STATUS_PLAYER1, 2")
        game_status.player1.banMegami(player2_data)
        game_status.player2.banMegami(player1_data)

        val end_data = SakuraSendData(CommandEnum.END_SELECT_BAN, null)

        player1.session.send(Json.encodeToString(end_data))
        player1.session.send(Json.encodeToString(end_data))
    }

    suspend fun checkFinalMegami(){
        TODO("CHANGE GAME_STATUS_PLAYER1, 2")
        val player1_player1_data = game_status.player1.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player2_player2_data = game_status.player2.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player1_player2_data = game_status.player2.makeMegamiData(CommandEnum.CHECK_ANOTHER)
        val player2_player1_data = game_status.player1.makeMegamiData(CommandEnum.CHECK_ANOTHER)

        player1.session.send(Json.encodeToString(player1_player1_data))
        player2.session.send(Json.encodeToString(player2_player2_data))

        player1.session.send(Json.encodeToString(player1_player2_data))
        player2.session.send(Json.encodeToString(player2_player1_data))
    }

    fun checkCardSet(bigger: MutableList<CardName>, smaller: MutableList<CardName>, size: Int): Boolean{
        if (smaller.size == size && smaller.distinct().size == size){
            for (item in smaller){
                if (bigger.contains(item)){
                    continue
                }
                return false
            }
            for (item in smaller){
                bigger.remove(item)
            }
            return true
        }
        else{
            return false
        }
    }

    suspend fun selectCard(){
        TODO("CHANGE GAME_STATUS_PLAYER1, 2")
        game_status.player1.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(game_status.player1.megami_1))
        game_status.player1.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(game_status.player1.megami_2))
        game_status.player2.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(game_status.player2.megami_1))
        game_status.player2.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(game_status.player2.megami_2))
        game_status.player1.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(game_status.player1.megami_1))
        game_status.player1.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(game_status.player1.megami_2))
        game_status.player2.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(game_status.player2.megami_1))
        game_status.player2.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(game_status.player2.megami_2))

        val send_request_player1 = SakuraCardSetSend(CommandEnum.SELECT_CARD, game_status.player1.unselected_card, game_status.player1.unselected_specialcard)
        val send_request_player2 = SakuraCardSetSend(CommandEnum.SELECT_CARD, game_status.player2.unselected_card, game_status.player2.unselected_specialcard)

        player1.session.send(Json.encodeToString(send_request_player1))
        player2.session.send(Json.encodeToString(send_request_player2))

        val player1_data = waitCardSetUntil(PlayerEnum.PLAYER1, CommandEnum.SELECT_CARD)
        val player2_data = waitCardSetUntil(PlayerEnum.PLAYER2, CommandEnum.SELECT_CARD)

        var card_data_player1: MutableList<CardName>
        var specialcard_data_player1: MutableList<CardName>
        var card_data_player2 : MutableList<CardName>
        var specialcard_data_player2 : MutableList<CardName>

        if(checkCardSet(game_status.player1.unselected_card, player1_data!!.normal_card, 7))
            card_data_player1 = player1_data!!.normal_card
        else
            card_data_player1 = game_status.player1.unselected_card.subList(0, 7)

        if(checkCardSet(game_status.player2.unselected_card, player2_data!!.normal_card, 7))
            card_data_player2 = player2_data!!.normal_card
        else
            card_data_player2 = game_status.player2.unselected_card.subList(0, 7)

        if(checkCardSet(game_status.player1.unselected_specialcard, player1_data!!.special_card, 3))
            specialcard_data_player1 = player1_data!!.special_card
        else
            specialcard_data_player1 = game_status.player1.unselected_specialcard.subList(0, 3)

        if(checkCardSet(game_status.player2.unselected_specialcard, player2_data!!.special_card, 3))
            specialcard_data_player2 = player2_data!!.special_card
        else
            specialcard_data_player2 = game_status.player2.unselected_specialcard.subList(0, 3)

        val end_player1_select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, card_data_player1, specialcard_data_player1)
        val end_player2_select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, card_data_player2, specialcard_data_player2)

        player1.session.send(Json.encodeToString(end_player1_select))
        player2.session.send(Json.encodeToString(end_player2_select))

        //card make section

        //card make section
    }

    suspend fun startGame(){
        selectMode()
        selectEnd()
        selectMegami()
        if(game_mode == 1){
            checkMegami()
            selectBan()
        }
        checkFinalMegami()
        selectCard()
    }
}