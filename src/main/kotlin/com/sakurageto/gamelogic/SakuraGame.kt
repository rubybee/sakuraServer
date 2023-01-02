package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.card.Card
import com.sakurageto.card.CardName
import com.sakurageto.card.CardSet
import com.sakurageto.card.PlayerEnum
import com.sakurageto.protocol.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

enum class GameMode(var realnumber: Int){
    SSANG_JANG_YO_LAN(0),
    SAM_SEUB_IL_SA(1),
}

class SakuraGame(val player1: Connection, val player2: Connection) {
    private var game_mode: GameMode //0 = no ban 1 = pick ban
    private var game_status: GameStatus

    private var turn_number = 0
    private var first_turn = PlayerEnum.PLAYER1
    private var turn_player = PlayerEnum.PLAYER1

    inline fun getSocket(player: PlayerEnum): Connection{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    init {
        game_mode = GameMode.SSANG_JANG_YO_LAN
        game_status = GameStatus(PlayerStatus(), PlayerStatus(), player1, player2)
    }

    suspend fun selectMode(){
        val data = SakuraSendData(CommandEnum.SELECT_MODE, null)
        player1.session.send(Json.encodeToString(data))
        val mode = waitUntil(player1, CommandEnum.SELECT_MODE).data?.get(0) ?: 0
        if(mode == 0){
            this.game_mode = GameMode.SSANG_JANG_YO_LAN
        }
        else{
            this.game_mode = GameMode.SAM_SEUB_IL_SA
        }
    }

    suspend fun selectEnd(){
        val data = SakuraSendData(CommandEnum.END_OF_SELECTMODE, mutableListOf(game_mode.realnumber))
        val send_data = Json.encodeToString(data)
        player1.session.send(send_data)
        player2.session.send(send_data)
    }

    suspend fun selectMegami(){
        val data = SakuraSendData(CommandEnum.SELECT_MEGAMI, null)
        val send_data = Json.encodeToString(data)
        player1.session.send(send_data)
        player2.session.send(send_data)
        val player1_data = waitUntil(player1, CommandEnum.SELECT_MEGAMI)
        val player2_data = waitUntil(player2, CommandEnum.SELECT_MEGAMI)
        if(game_mode == GameMode.SSANG_JANG_YO_LAN){
            game_status.player1.setMegamiSSangjang(player1_data)
            game_status.player2.setMegamiSSangjang(player2_data)
            val end_data_player1 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player1.returnListMegami2())
            val end_data_player2 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player2.returnListMegami2())
            player1.session.send(Json.encodeToString(end_data_player1))
            player2.session.send(Json.encodeToString(end_data_player2))
        }
        else if(game_mode == GameMode.SAM_SEUB_IL_SA){
            game_status.player1.setMegamiSamSep(player1_data)
            game_status.player2.setMegamiSamSep(player2_data)
            val end_data_player1 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player1.returnListMegami3())
            val end_data_player2 = SakuraSendData(CommandEnum.END_OF_SELECT_MEGAMI, game_status.player2.returnListMegami3())
            player1.session.send(Json.encodeToString(end_data_player1))
            player2.session.send(Json.encodeToString(end_data_player2))
        }
    }

    suspend fun checkMegami(){
        val check_data_player1 = SakuraSendData(CommandEnum.CHECK_MEGAMI, game_status.player1.returnListMegami3())
        val check_data_player2 = SakuraSendData(CommandEnum.CHECK_MEGAMI, game_status.player2.returnListMegami3())
        player1.session.send(Json.encodeToString(check_data_player2))
        player2.session.send(Json.encodeToString(check_data_player1))
    }

    suspend fun selectBan(){
        val select_ban = SakuraSendData(CommandEnum.SELECT_BAN, null)

        player1.session.send(Json.encodeToString(select_ban))
        player2.session.send(Json.encodeToString(select_ban))

        val player1_data = waitUntil(player1, CommandEnum.SELECT_BAN)
        val player2_data = waitUntil(player2, CommandEnum.SELECT_BAN)

        game_status.player1.banMegami(player2_data)
        game_status.player2.banMegami(player1_data)

        val end_data = SakuraSendData(CommandEnum.END_SELECT_BAN, null)

        player1.session.send(Json.encodeToString(end_data))
        player2.session.send(Json.encodeToString(end_data))
    }

    suspend fun checkFinalMegami(){
        val player1_player1_data = game_status.player1.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player2_player2_data = game_status.player2.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player1_player2_data = game_status.player2.makeMegamiData(CommandEnum.CHECK_ANOTHER)
        val player2_player1_data = game_status.player1.makeMegamiData(CommandEnum.CHECK_ANOTHER)

        player1.session.send(Json.encodeToString(player1_player1_data))
        player2.session.send(Json.encodeToString(player2_player2_data))

        player1.session.send(Json.encodeToString(player1_player2_data))
        player2.session.send(Json.encodeToString(player2_player1_data))
    }

    fun checkCardSet(bigger: MutableList<CardName>, smaller: MutableList<CardName>?, size: Int): Boolean{
        if(smaller == null){
            return false
        }
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
        game_status.player1.unselected_card.addAll(CardName.returnNormalCardNameByMegami(game_status.player1.megami_1))
        game_status.player1.unselected_card.addAll(CardName.returnNormalCardNameByMegami(game_status.player1.megami_2))
        game_status.player2.unselected_card.addAll(CardName.returnNormalCardNameByMegami(game_status.player2.megami_1))
        game_status.player2.unselected_card.addAll(CardName.returnNormalCardNameByMegami(game_status.player2.megami_2))
        game_status.player1.unselected_specialcard.addAll(CardName.returnSpecialCardNameByMegami(game_status.player1.megami_1))
        game_status.player1.unselected_specialcard.addAll(CardName.returnSpecialCardNameByMegami(game_status.player1.megami_2))
        game_status.player2.unselected_specialcard.addAll(CardName.returnSpecialCardNameByMegami(game_status.player2.megami_1))
        game_status.player2.unselected_specialcard.addAll(CardName.returnSpecialCardNameByMegami(game_status.player2.megami_2))

        val send_request_player1 = SakuraCardSetSend(CommandEnum.SELECT_CARD, game_status.player1.unselected_card, game_status.player1.unselected_specialcard)
        val send_request_player2 = SakuraCardSetSend(CommandEnum.SELECT_CARD, game_status.player2.unselected_card, game_status.player2.unselected_specialcard)
        val send_othersinformation_player1 = SakuraCardSetSend(CommandEnum.SELECT_CARD_OTHER_PLAYERS, game_status.player2.unselected_card, game_status.player2.unselected_card)
        val send_othersinformation_player2 = SakuraCardSetSend(CommandEnum.SELECT_CARD_OTHER_PLAYERS, game_status.player1.unselected_card, game_status.player1.unselected_card)

        player1.session.send(Json.encodeToString(send_request_player1))
        player2.session.send(Json.encodeToString(send_request_player2))
        player1.session.send(Json.encodeToString(send_othersinformation_player1))
        player2.session.send(Json.encodeToString(send_othersinformation_player2))

        val player1_data = waitCardSetUntil(player1, CommandEnum.SELECT_CARD)
        val player2_data = waitCardSetUntil(player2, CommandEnum.SELECT_CARD)

        var card_data_player1: MutableList<CardName>
        var specialcard_data_player1: MutableList<CardName>
        var card_data_player2 : MutableList<CardName>
        var specialcard_data_player2 : MutableList<CardName>

        if(checkCardSet(game_status.player1.unselected_card, player1_data.normal_card, 7))
            card_data_player1 = player1_data.normal_card!!
        else
            card_data_player1 = game_status.player1.unselected_card.subList(0, 7)

        if(checkCardSet(game_status.player2.unselected_card, player2_data.normal_card, 7))
            card_data_player2 = player2_data.normal_card!!
        else
            card_data_player2 = game_status.player2.unselected_card.subList(0, 7)

        if(checkCardSet(game_status.player1.unselected_specialcard, player1_data.special_card, 3))
            specialcard_data_player1 = player1_data.special_card!!
        else
            specialcard_data_player1 = game_status.player1.unselected_specialcard.subList(0, 3)

        if(checkCardSet(game_status.player2.unselected_specialcard, player2_data.special_card, 3))
            specialcard_data_player2 = player2_data.special_card!!
        else
            specialcard_data_player2 = game_status.player2.unselected_specialcard.subList(0, 3)

        val end_player1_select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, card_data_player1, specialcard_data_player1)
        val end_player2_select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, card_data_player2, specialcard_data_player2)

        player1.session.send(Json.encodeToString(end_player1_select))
        player2.session.send(Json.encodeToString(end_player2_select))

        when(first_turn){
            PlayerEnum.PLAYER1 -> {
                Card.cardInitInsert(true, game_status.player1.normal_card_deck, card_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(true, game_status.player1.special_card_deck, specialcard_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(false, game_status.player2.normal_card_deck, card_data_player2, PlayerEnum.PLAYER2)
                Card.cardInitInsert(false, game_status.player2.special_card_deck, specialcard_data_player2, PlayerEnum.PLAYER2)
            }
            PlayerEnum.PLAYER2 -> {
                Card.cardInitInsert(false, game_status.player1.normal_card_deck, card_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(false, game_status.player1.special_card_deck, specialcard_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(true, game_status.player2.normal_card_deck, card_data_player2, PlayerEnum.PLAYER2)
                Card.cardInitInsert(true, game_status.player2.special_card_deck, specialcard_data_player2, PlayerEnum.PLAYER2)
            }
        }

        val additional_card_player1 = mutableListOf<CardName>()
        val additional_card_player2 = mutableListOf<CardName>()
        additional_card_player1.addAll(CardName.returnAdditionalCardNameByMegami(game_status.player1.megami_1))
        additional_card_player2.addAll(CardName.returnAdditionalCardNameByMegami(game_status.player2.megami_1))
        additional_card_player1.addAll(CardName.returnAdditionalCardNameByMegami(game_status.player1.megami_2))
        additional_card_player2.addAll(CardName.returnAdditionalCardNameByMegami(game_status.player2.megami_2))

        if(!additional_card_player1.isEmpty()){
            val turn_check = first_turn == PlayerEnum.PLAYER1
            for(card_name in additional_card_player1){
                var card = Card.cardMakerByName(turn_check, card_name, PlayerEnum.PLAYER1)
                game_status.player1.additional_hand[card.card_number] = card
            }
        }

        if(!additional_card_player2.isEmpty()){
            val turn_check = first_turn == PlayerEnum.PLAYER2
            for(card_name in additional_card_player2){
                var card = Card.cardMakerByName(turn_check, card_name, PlayerEnum.PLAYER2)
                game_status.player2.additional_hand[card.card_number] = card
            }
        }

        game_status.player1.deleteNormalUsedCard(card_data_player1)
        game_status.player1.deleteSpeicalUsedCard(specialcard_data_player1)

        game_status.player2.deleteNormalUsedCard(card_data_player2)
        game_status.player2.deleteSpeicalUsedCard(specialcard_data_player2)
    }

    suspend fun selectFirst(){
        val random = Random.nextInt(2)
        var player1_data: SakuraSendData
        var player2_data: SakuraSendData
        if(random == 0){
            player1_data = SakuraSendData(CommandEnum.FIRST_TURN, null)
            player2_data = SakuraSendData(CommandEnum.SECOND_TURN, null)
            first_turn = PlayerEnum.PLAYER1
            game_status.setFirstTurn(PlayerEnum.PLAYER1)
        }
        else{
            player1_data = SakuraSendData(CommandEnum.SECOND_TURN, null)
            player2_data = SakuraSendData(CommandEnum.FIRST_TURN, null)
            first_turn = PlayerEnum.PLAYER2
            game_status.setFirstTurn(PlayerEnum.PLAYER2)
        }

        player1.session.send(Json.encodeToString(player1_data))
        player2.session.send(Json.encodeToString(player2_data))
    }

    suspend fun muligun(){
        val data = SakuraSendData(CommandEnum.MULIGUN, null)
        player1.session.send(Json.encodeToString(data))
        player2.session.send(Json.encodeToString(data))
        val player1_data = waitCardSetUntil(player1, CommandEnum.MULIGUN)
        val player2_data = waitCardSetUntil(player2, CommandEnum.MULIGUN)
        var count = 0
        player1_data.normal_card?.also {
            for(card_name in it){
                if(game_status.insertHandToDeck(PlayerEnum.PLAYER1, card_name)){
                    count += 1
                }
            }
        }
        game_status.drawCard(PlayerEnum.PLAYER1, count)

        count = 0
        player2_data.normal_card?.also {
            for(card_name in it){
                if(game_status.insertHandToDeck(PlayerEnum.PLAYER2, card_name)){
                    count += 1
                }
            }
        }
        game_status.drawCard(PlayerEnum.PLAYER2, count)
        sendMuligunEnd(player1, player2)
    }

    suspend fun startPhaseDefault(){
        game_status.addConcentration(this.turn_player)
        game_status.enchantmentReduceAll(this.turn_player)
        if(receiveReconstructRequest(getSocket(this.turn_player))){
            game_status.deckReconstruct(this.turn_player, true)
        }
        game_status.drawCard(this.turn_player, 2)
    }

    suspend fun startPhase(){
        sendStartPhaseStart(getSocket(this.turn_player), getSocket(this.turn_player.Opposite()))
        game_status.start_distance = game_status.distance
        game_status.startPhaseEffectProcess()
        if(turn_number == 0 || turn_number == 1){
            return
        }
        startPhaseDefault()
    }

    suspend fun mainPhase(){
        sendMainPhaseStart(getSocket(this.turn_player), getSocket(this.turn_player.Opposite()))
        game_status.mainPhaseEffectProcess()
        if(receiveFullPowerRequest(getSocket(this.turn_player))){
            game_status.setPlayerFullAction(this.turn_player, true)
            while (true){
                var data = receiveFullPowerActionRequest(getSocket(this.turn_player))
                if(data.first == CommandEnum.ACTION_END_TURN){
                    return
                }
                else if(game_status.cardUseNormaly(this.turn_player, data.first, data.second)){
                    return
                }
                else{
                    continue
                }
            }
        }
        else{
            game_status.setPlayerFullAction(this.turn_player, false)
            while (true){
                var data = receiveActionRequest(getSocket(this.turn_player))
                if(data.first == CommandEnum.ACTION_END_TURN || game_status.getEndTurn(this.turn_player)){
                    return
                }
                else if(data.first == CommandEnum.ACTION_USE_CARD_HAND || data.first == CommandEnum.ACTION_USE_CARD_SPECIAL){
                    print(this.turn_player)
                    print(": use card " + data.second + "\ndistance:")
                    print(game_status.distance)
                    game_status.cardUseNormaly(this.turn_player, data.first, data.second)
                }
                else{
                    if(game_status.canDoBasicOperation(this.turn_player, data.first)){
                        if(game_status.basicOperationCost(this.turn_player, data.second)){
                            game_status.doBasicOperation(this.turn_player, data.first)
                        }
                    }
                }
            }
        }
    }

    suspend fun endPhase(){
        sendEndPhaseStart(getSocket(this.turn_player), getSocket(this.turn_player.Opposite()))
        game_status.endPhaseEffectProcess()
        game_status.setEndTurn(PlayerEnum.PLAYER1, false)
        game_status.setEndTurn(PlayerEnum.PLAYER2, false)
        game_status.endTurnHandCheck(this.turn_player)
        this.turn_player = this.turn_player.Opposite()
        this.turn_number += 1
    }

    suspend fun gameStart(){
        this.turn_player = this.first_turn

        while(true){
            startPhase()
            mainPhase()
            endPhase()
        }
    }

    suspend fun startGame(){
        selectMode()
        selectEnd()
        selectMegami()
        if(game_mode == GameMode.SAM_SEUB_IL_SA){
            checkMegami()
            selectBan()
        }
        checkFinalMegami()
        selectFirst()
        selectCard()
        game_status.drawCard(PlayerEnum.PLAYER1, 3)
        game_status.drawCard(PlayerEnum.PLAYER2, 3)
        muligun()
        gameStart()
    }
}