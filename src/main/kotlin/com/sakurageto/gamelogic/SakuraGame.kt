package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.card.Card
import com.sakurageto.card.CardName
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
    private var player1_status: PlayerStatus
    private var player2_status: PlayerStatus

    init {
        game_mode = 0
        player1_status = PlayerStatus()
        player2_status = PlayerStatus()
    }

    suspend fun waitUntil(player_id: Int, wait_command: CommandEnum): SakuraSendData {
        if (player_id == 1){
            for (frame in player1.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val data = Json.decodeFromString<SakuraSendData>(text)
                    if (data.command == wait_command){
                        return data
                    }
                }
            }
        }

        else {
            for (frame in player2.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val data = Json.decodeFromString<SakuraSendData>(text)
                    if (data.command == wait_command){
                        return data
                    }
                }
            }
        }

        return SakuraSendData(CommandEnum.SELECT_MODE, null)
    }

    suspend fun waitCardSetUntil(player_id: Int, wait_command: CommandEnum): SakuraCardSetSend? {
        if (player_id == 1){
            for (frame in player1.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val data = Json.decodeFromString<SakuraCardSetSend>(text)
                    if (data.command == wait_command){
                        return data
                    }
                }
            }
        }

        else {
            for (frame in player2.session.incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()
                    val data = Json.decodeFromString<SakuraCardSetSend>(text)
                    if (data.command == wait_command){
                        return data
                    }
                }
            }
        }

        return null
    }

    suspend fun selectMode(){
        val data = SakuraSendData(CommandEnum.SELECT_MODE, null)
        player1.session.send(Json.encodeToString(data))
        game_mode = waitUntil(1, CommandEnum.SELECT_MODE).data?.get(0) ?: 0
    }

    suspend fun selectEnd(){
        val data = SakuraSendData(CommandEnum.END_OF_SELECTMODE, mutableListOf(game_mode))
        val send_data = Json.encodeToString(data)
        player1.session.send(send_data)
        player2.session.send(send_data)
    }

    suspend fun selectMegami(){
        val data = SakuraSendData(CommandEnum.SELECT_MEGAMI, null)
        val send_data = Json.encodeToString(data)
        player1.session.send(send_data)
        player2.session.send(send_data)
        val player1_data = waitUntil(1, CommandEnum.SELECT_MEGAMI)
        val player2_data = waitUntil(2, CommandEnum.SELECT_MEGAMI)
        if(game_mode == 0){
            player1_status.setMegamiSSangjang(player1_data)
            player2_status.setMegamiSSangjang(player2_data)
            val end_data_player1 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, player1_status.returnListMegami2())
            val end_data_player2 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, player2_status.returnListMegami2())
            player1.session.send(Json.encodeToString(end_data_player1))
            player2.session.send(Json.encodeToString(end_data_player2))
        }
        else{
            player1_status.setMegamiSamSep(player1_data)
            player2_status.setMegamiSamSep(player2_data)
            val end_data_player1 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, player1_status.returnListMegami3())
            val end_data_player2 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, player2_status.returnListMegami3())
            player1.session.send(Json.encodeToString(end_data_player1))
            player2.session.send(Json.encodeToString(end_data_player2))
        }
    }

    suspend fun checkMegami(){
        val check_data_player1 = SakuraSendData(CommandEnum.CHECK_MEGAMI, player1_status.returnListMegami3())
        val check_data_player2 = SakuraSendData(CommandEnum.CHECK_MEGAMI, player2_status.returnListMegami3())
        player1.session.send(Json.encodeToString(check_data_player2))
        player2.session.send(Json.encodeToString(check_data_player1))
    }

    suspend fun selectBan(){
        val select_ban = SakuraSendData(CommandEnum.SELECT_BAN, null)

        player1.session.send(Json.encodeToString(select_ban))
        player2.session.send(Json.encodeToString(select_ban))

        val player1_data = waitUntil(1, CommandEnum.SELECT_BAN)
        val player2_data = waitUntil(2, CommandEnum.SELECT_BAN)

        player1_status.banMegami(player2_data)
        player2_status.banMegami(player1_data)

        val end_data = SakuraSendData(CommandEnum.END_SELECT_BAN, null)

        player1.session.send(Json.encodeToString(end_data))
        player1.session.send(Json.encodeToString(end_data))
    }

    suspend fun checkFinalMegami(){
        val player1_player1_data = player1_status.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player2_player2_data = player2_status.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player1_player2_data = player2_status.makeMegamiData(CommandEnum.CHECK_ANOTHER)
        val player2_player1_data = player1_status.makeMegamiData(CommandEnum.CHECK_ANOTHER)

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
        player1_status.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(player1_status.megami_1))
        player1_status.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(player1_status.megami_2))
        player2_status.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(player2_status.megami_1))
        player2_status.unselected_card.addAll(CardName.Companion.returnNormalCardNameByMegami(player2_status.megami_2))
        player1_status.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(player1_status.megami_1))
        player1_status.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(player1_status.megami_2))
        player2_status.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(player2_status.megami_1))
        player2_status.unselected_specialcard.addAll(CardName.Companion.returnSpecialCardNameByMegami(player2_status.megami_2))

        val send_request_player1 = SakuraCardSetSend(CommandEnum.SELECT_CARD, player1_status.unselected_card, player1_status.unselected_specialcard)
        val send_request_player2 = SakuraCardSetSend(CommandEnum.SELECT_CARD, player2_status.unselected_card, player2_status.unselected_specialcard)

        player1.session.send(Json.encodeToString(send_request_player1))
        player2.session.send(Json.encodeToString(send_request_player2))

        val player1_data = waitCardSetUntil(1, CommandEnum.SELECT_CARD)
        val player2_data = waitCardSetUntil(2, CommandEnum.SELECT_CARD)

        var card_data_player1: MutableList<CardName>
        var specialcard_data_player1: MutableList<CardName>
        var card_data_player2 : MutableList<CardName>
        var specialcard_data_player2 : MutableList<CardName>

        if(checkCardSet(player1_status.unselected_card, player1_data!!.normal_card, 7))
            card_data_player1 = player1_data!!.normal_card
        else
            card_data_player1 = player1_status.unselected_card.subList(0, 7)

        if(checkCardSet(player2_status.unselected_card, player2_data!!.normal_card, 7))
            card_data_player2 = player2_data!!.normal_card
        else
            card_data_player2 = player2_status.unselected_card.subList(0, 7)

        if(checkCardSet(player1_status.unselected_specialcard, player1_data!!.special_card, 3))
            specialcard_data_player1 = player1_data!!.special_card
        else
            specialcard_data_player1 = player1_status.unselected_specialcard.subList(0, 3)

        if(checkCardSet(player2_status.unselected_specialcard, player2_data!!.special_card, 3))
            specialcard_data_player2 = player2_data!!.special_card
        else
            specialcard_data_player2 = player2_status.unselected_specialcard.subList(0, 3)

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