package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.RoomInformation
import com.sakurageto.card.*
import com.sakurageto.gamelogic.GameStatus.Companion.END_PHASE
import com.sakurageto.gamelogic.GameStatus.Companion.MAIN_PHASE
import com.sakurageto.gamelogic.GameStatus.Companion.START_PHASE
import com.sakurageto.gamelogic.storyboard.StoryBoard
import com.sakurageto.protocol.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

enum class GameMode(var realnumber: Int){
    SSANG_JANG_YO_LAN(0),
    SAM_SEUB_IL_SA(1),
}

class SakuraGame(val roomNumber: Int, val player1: Connection, val player2: Connection) {
    private var gameMode: GameMode //0 = no ban 1 = pick ban
    private var gameStatus: GameStatus

    private var turnNumber = 0
    private var firstTurn = PlayerEnum.PLAYER1
    private var turnPlayer = PlayerEnum.PLAYER1

    private fun getSocket(player: PlayerEnum): Connection{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    init {
        gameMode = GameMode.SSANG_JANG_YO_LAN
        gameStatus = GameStatus(PlayerStatus(PlayerEnum.PLAYER1), PlayerStatus(PlayerEnum.PLAYER2), player1, player2)
        RoomInformation.roomHashMap[roomNumber]?.game = gameStatus
    }

    private suspend fun selectMode(){
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true;}
        val random = Random(System.currentTimeMillis())
        val firstCode = random.nextInt()
        val secondCode = random.nextInt()
        RoomInformation.roomHashMap[roomNumber]?.firstUserCode = firstCode
        RoomInformation.roomHashMap[roomNumber]?.secondUserCode = secondCode
        println("firstplayer: connectCode: ${firstCode}")
        println("secondplayer: connectCode: ${secondCode}")
        val data = SakuraCardCommand(CommandEnum.SELECT_MODE_OTHER, firstCode)
        val dataOther = SakuraCardCommand(CommandEnum.SELECT_MODE_YOUR, secondCode)
        player1.session.send(json.encodeToString(data))
        player2.session.send(json.encodeToString(dataOther))
        val dataGet = waitUntil(player2, CommandEnum.SELECT_MODE_YOUR).data?: mutableListOf(0)
        val mode = if (dataGet.isEmpty()) 0 else dataGet[0]
        if(mode == 0){
            this.gameMode = GameMode.SSANG_JANG_YO_LAN
        }
        else{
            this.gameMode = GameMode.SAM_SEUB_IL_SA
        }
    }

    suspend fun selectEnd(){
        val data = SakuraSendData(CommandEnum.END_OF_SELECTMODE, mutableListOf(gameMode.realnumber))
        val send_data = Json.encodeToString(data)
        player1.session.send(send_data)
        player2.session.send(send_data)
    }

    suspend fun selectMegami(){
        val data = SakuraCardCommand(CommandEnum.SELECT_MEGAMI, -1)
        val sendData = Json.encodeToString(data)
        player1.session.send(sendData)
        player2.session.send(sendData)
        val player1_data = waitUntil(player1, CommandEnum.SELECT_MEGAMI)
        val player2_data = waitUntil(player2, CommandEnum.SELECT_MEGAMI)
        if(gameMode == GameMode.SSANG_JANG_YO_LAN){
            gameStatus.player1.setMegamiSSangjang(player1_data)
            gameStatus.player2.setMegamiSSangjang(player2_data)
        }
        else if(gameMode == GameMode.SAM_SEUB_IL_SA){
            gameStatus.player1.setMegamiSamSep(player1_data)
            gameStatus.player2.setMegamiSamSep(player2_data)
        }
    }

    suspend fun checkMegami(){
        val check_data_player1 = SakuraSendData(CommandEnum.CHECK_MEGAMI, gameStatus.player1.returnListMegami3())
        val check_data_player2 = SakuraSendData(CommandEnum.CHECK_MEGAMI, gameStatus.player2.returnListMegami3())
        player1.session.send(Json.encodeToString(check_data_player2))
        player2.session.send(Json.encodeToString(check_data_player1))
    }

    suspend fun selectBan(){
        val player1_data = waitUntil(player1, CommandEnum.SELECT_BAN)
        val player2_data = waitUntil(player2, CommandEnum.SELECT_BAN)

        gameStatus.player1.banMegami(player2_data)
        gameStatus.player2.banMegami(player1_data)
    }

    suspend fun checkFinalMegami(){
        val player1_player1_data = gameStatus.player1.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player2_player2_data = gameStatus.player2.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player1_player2_data = gameStatus.player2.makeMegamiData(CommandEnum.CHECK_ANOTHER)
        val player2_player1_data = gameStatus.player1.makeMegamiData(CommandEnum.CHECK_ANOTHER)

        player1.session.send(Json.encodeToString(player1_player1_data))
        player2.session.send(Json.encodeToString(player2_player2_data))

        player1.session.send(Json.encodeToString(player1_player2_data))
        player2.session.send(Json.encodeToString(player2_player1_data))

        //additional board setting here
        if(gameStatus.player1.megami_1 == MegamiEnum.YUKIHI || gameStatus.player1.megami_2 == MegamiEnum.YUKIHI ||
            gameStatus.player1.megami_1 == MegamiEnum.YUKIHI_A1 || gameStatus.player1.megami_2 == MegamiEnum.YUKIHI_A1
        ){
            gameStatus.player1.umbrella = Umbrella.FOLD
            if(gameStatus.player1.megami_1 == MegamiEnum.YUKIHI){
                gameStatus.player1.megamiCard = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER1, CardName.YUKIHI_YUKIHI, PlayerEnum.PLAYER1)
                gameStatus.player1.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
            }
            else{
                gameStatus.player1.megamiCard2 = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER1, CardName.YUKIHI_YUKIHI, PlayerEnum.PLAYER1)
                gameStatus.player1.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
            }
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.YUKIHI || gameStatus.player2.megami_2 == MegamiEnum.YUKIHI ||
            gameStatus.player2.megami_1 == MegamiEnum.YUKIHI_A1 || gameStatus.player2.megami_2 == MegamiEnum.YUKIHI_A1){
            gameStatus.player2.umbrella = Umbrella.FOLD
            if(gameStatus.player2.megami_1 == MegamiEnum.YUKIHI){
                gameStatus.player2.megamiCard = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER2, CardName.YUKIHI_YUKIHI, PlayerEnum.PLAYER2)
                gameStatus.player2.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
            }
            else{
                gameStatus.player2.megamiCard2 = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER2, CardName.YUKIHI_YUKIHI, PlayerEnum.PLAYER2)
                gameStatus.player2.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
            }
        }

        if(gameStatus.player1.megami_1 == MegamiEnum.SHINRA || gameStatus.player1.megami_1 == MegamiEnum.SHINRA_A1 ||
            gameStatus.player1.megami_2 == MegamiEnum.SHINRA || gameStatus.player1.megami_2 == MegamiEnum.SHINRA_A1){
            gameStatus.player1.stratagem = Stratagem.SHIN_SAN
            if(gameStatus.player1.megami_1 == MegamiEnum.SHINRA || gameStatus.player1.megami_1 == MegamiEnum.SHINRA_A1){
                gameStatus.player1.megamiCard = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER1, CardName.SHINRA_SHINRA, PlayerEnum.PLAYER1)
                gameStatus.player1.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
            }
            else{
                gameStatus.player1.megamiCard2 = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER1, CardName.SHINRA_SHINRA, PlayerEnum.PLAYER1)
                gameStatus.player1.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
            }
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.SHINRA || gameStatus.player2.megami_1 == MegamiEnum.SHINRA_A1 ||
            gameStatus.player2.megami_2 == MegamiEnum.SHINRA || gameStatus.player2.megami_2 == MegamiEnum.SHINRA_A1){
            gameStatus.player2.stratagem = Stratagem.SHIN_SAN
            if(gameStatus.player2.megami_1 == MegamiEnum.SHINRA || gameStatus.player2.megami_1 == MegamiEnum.SHINRA_A1){
                gameStatus.player2.megamiCard = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER2, CardName.SHINRA_SHINRA, PlayerEnum.PLAYER2)
                gameStatus.player2.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
            }
            else{
                gameStatus.player2.megamiCard2 = Card.cardMakerByName(firstTurn == PlayerEnum.PLAYER2, CardName.SHINRA_SHINRA, PlayerEnum.PLAYER2)
                gameStatus.player2.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
            }
        }

        if(gameStatus.player1.megami_1 == MegamiEnum.CHIKAGE || gameStatus.player1.megami_2 == MegamiEnum.CHIKAGE
            || gameStatus.player1.megami_1 == MegamiEnum.CHIKAGE_A1 || gameStatus.player1.megami_2 == MegamiEnum.CHIKAGE_A1){
            for(card_name in CardName.returnPoisonCardName()){
                val turnCheck = firstTurn == PlayerEnum.PLAYER2
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER2)
                gameStatus.player1.poisonBag[card.card_data.card_name] = card
            }
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.CHIKAGE || gameStatus.player2.megami_2 == MegamiEnum.CHIKAGE
            || gameStatus.player2.megami_1 == MegamiEnum.CHIKAGE_A1 || gameStatus.player2.megami_2 == MegamiEnum.CHIKAGE_A1){
            for(card_name in CardName.returnPoisonCardName()){
                val turnCheck = firstTurn == PlayerEnum.PLAYER1
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER1)
                gameStatus.player2.poisonBag[card.card_data.card_name] = card
            }
        }

        if(gameStatus.player1.megami_1 == MegamiEnum.THALLYA || gameStatus.player1.megami_2 == MegamiEnum.THALLYA ||
            gameStatus.player1.megami_1 == MegamiEnum.THALLYA_A1 || gameStatus.player1.megami_2 == MegamiEnum.THALLYA_A1){
            gameStatus.player1.artificialToken = 5
            gameStatus.player1ManeuverListener = ArrayDeque()
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.THALLYA || gameStatus.player2.megami_2 == MegamiEnum.THALLYA ||
            gameStatus.player2.megami_1 == MegamiEnum.THALLYA_A1 || gameStatus.player2.megami_2 == MegamiEnum.THALLYA_A1){
            gameStatus.player2.artificialToken = 5
            gameStatus.player2ManeuverListener = ArrayDeque()
        }

        if(gameStatus.player1.megami_1 == MegamiEnum.RAIRA || gameStatus.player1.megami_2 == MegamiEnum.RAIRA ||
            gameStatus.player1.megami_1 == MegamiEnum.RAIRA_A1 || gameStatus.player1.megami_2 == MegamiEnum.RAIRA_A1){
            gameStatus.getPlayer(PlayerEnum.PLAYER1).windGauge = 0
            gameStatus.getPlayer(PlayerEnum.PLAYER1).thunderGauge = 0
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.RAIRA || gameStatus.player2.megami_2 == MegamiEnum.RAIRA ||
            gameStatus.player2.megami_1 == MegamiEnum.RAIRA_A1 || gameStatus.player2.megami_2 == MegamiEnum.RAIRA_A1){
            gameStatus.getPlayer(PlayerEnum.PLAYER2).windGauge = 0
            gameStatus.getPlayer(PlayerEnum.PLAYER2).thunderGauge = 0
        }

        if(gameStatus.player1.megami_1 == MegamiEnum.MIZUKI || gameStatus.player1.megami_2 == MegamiEnum.MIZUKI){
            for(card_name in CardName.returnSoldierCardName()){
                val turnCheck = firstTurn == PlayerEnum.PLAYER1
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER1)
                gameStatus.player1.notReadySoldierZone[card.card_number] = card
            }
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.MIZUKI || gameStatus.player2.megami_2 == MegamiEnum.MIZUKI){
            for(card_name in CardName.returnSoldierCardName()){
                val turnCheck = firstTurn == PlayerEnum.PLAYER2
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER2)
                gameStatus.player2.notReadySoldierZone[card.card_number] = card
            }
        }

        if(gameStatus.player1.megami_1 == MegamiEnum.MEGUMI || gameStatus.player1.megami_2 == MegamiEnum.MEGUMI){
            gameStatus.player1.notReadySeed = 5
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.MEGUMI || gameStatus.player2.megami_2 == MegamiEnum.MEGUMI){
            gameStatus.player2.notReadySeed = 5
        }

        if(gameStatus.player1.megami_1 == MegamiEnum.KANAWE || gameStatus.player1.megami_2 == MegamiEnum.KANAWE){
            gameStatus.player1.nowAct = StoryBoard.getActByNumber(0)
        }

        if(gameStatus.player2.megami_1 == MegamiEnum.KANAWE || gameStatus.player2.megami_2 == MegamiEnum.KANAWE){
            gameStatus.player2.nowAct = StoryBoard.getActByNumber(0)
        }
        //additional board setting here
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
        gameStatus.player1.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.player1.megami_1))
        gameStatus.player1.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.player1.megami_2))
        gameStatus.player2.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.player2.megami_1))
        gameStatus.player2.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.player2.megami_2))
        gameStatus.player1.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.player1.megami_1))
        gameStatus.player1.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.player1.megami_2))
        gameStatus.player2.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.player2.megami_1))
        gameStatus.player2.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.player2.megami_2))

        val send_request_player1 = SakuraCardSetSend(CommandEnum.SELECT_CARD, gameStatus.player1.unselectedCard, gameStatus.player1.unselectedSpecialCard)
        val send_request_player2 = SakuraCardSetSend(CommandEnum.SELECT_CARD, gameStatus.player2.unselectedCard, gameStatus.player2.unselectedSpecialCard)

        player1.session.send(Json.encodeToString(send_request_player1))
        player2.session.send(Json.encodeToString(send_request_player2))

        val player1_data = waitCardSetUntil(player1, CommandEnum.SELECT_CARD)
        val player2_data = waitCardSetUntil(player2, CommandEnum.SELECT_CARD)

        val card_data_player1: MutableList<CardName> = mutableListOf()
        val specialcard_data_player1: MutableList<CardName> = mutableListOf()
        val card_data_player2 : MutableList<CardName> = mutableListOf()
        val specialcard_data_player2 : MutableList<CardName> = mutableListOf()

        if(checkCardSet(gameStatus.player1.unselectedCard, player1_data.normal_card, 7))
            card_data_player1.addAll(player1_data.normal_card!!)
        else
            card_data_player1.addAll(gameStatus.player1.unselectedCard.subList(0, 7))

        if(checkCardSet(gameStatus.player2.unselectedCard, player2_data.normal_card, 7))
            card_data_player2.addAll(player2_data.normal_card!!)
        else
            card_data_player2.addAll(gameStatus.player2.unselectedCard.subList(0, 7))

        if(checkCardSet(gameStatus.player1.unselectedSpecialCard, player1_data.special_card, 3))
            specialcard_data_player1.addAll(player1_data.special_card!!)
        else
            specialcard_data_player1.addAll(gameStatus.player1.unselectedSpecialCard.subList(0, 3))

        if(checkCardSet(gameStatus.player2.unselectedSpecialCard, player2_data.special_card, 3))
            specialcard_data_player2.addAll(player2_data.special_card!!)
        else
            specialcard_data_player2.addAll(gameStatus.player2.unselectedSpecialCard.subList(0, 3))

        val end_player1_select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, card_data_player1, specialcard_data_player1)
        val end_player2_select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, card_data_player2, specialcard_data_player2)

        player1.session.send(Json.encodeToString(end_player1_select))
        player2.session.send(Json.encodeToString(end_player2_select))

        when(firstTurn){
            PlayerEnum.PLAYER1 -> {
                Card.cardInitInsert(true, gameStatus.player1.normalCardDeck, card_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(true, gameStatus.player1.special_card_deck, specialcard_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(false, gameStatus.player2.normalCardDeck, card_data_player2, PlayerEnum.PLAYER2)
                Card.cardInitInsert(false, gameStatus.player2.special_card_deck, specialcard_data_player2, PlayerEnum.PLAYER2)
            }
            PlayerEnum.PLAYER2 -> {
                Card.cardInitInsert(false, gameStatus.player1.normalCardDeck, card_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(false, gameStatus.player1.special_card_deck, specialcard_data_player1, PlayerEnum.PLAYER1)
                Card.cardInitInsert(true, gameStatus.player2.normalCardDeck, card_data_player2, PlayerEnum.PLAYER2)
                Card.cardInitInsert(true, gameStatus.player2.special_card_deck, specialcard_data_player2, PlayerEnum.PLAYER2)
            }
        }

        val additional_card_player1 = mutableListOf<CardName>()
        val additional_card_player2 = mutableListOf<CardName>()
        additional_card_player1.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player1.megami_1))
        additional_card_player2.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player2.megami_1))
        additional_card_player1.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player1.megami_2))
        additional_card_player2.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player2.megami_2))

        if(!additional_card_player1.isEmpty()){
            val turnCheck = firstTurn == PlayerEnum.PLAYER1
            for(card_name in additional_card_player1){
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER1)
                gameStatus.player1.additionalHand[card.card_data.card_name] = card
            }
        }

        if(!additional_card_player2.isEmpty()){
            val turnCheck = firstTurn == PlayerEnum.PLAYER2
            for(card_name in additional_card_player2){
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER2)
                gameStatus.player2.additionalHand[card.card_data.card_name] = card
            }
        }

        gameStatus.player1.deleteNormalUsedCard(card_data_player1)
        gameStatus.player1.deleteSpeicalUsedCard(specialcard_data_player1)

        gameStatus.player2.deleteNormalUsedCard(card_data_player2)
        gameStatus.player2.deleteSpeicalUsedCard(specialcard_data_player2)
    }

     fun selectFirst(){
        val random = Random(System.currentTimeMillis()).nextInt(2)
        if(random == 0){
            firstTurn = PlayerEnum.PLAYER1
            gameStatus.setFirstTurn(PlayerEnum.PLAYER1)
        }
        else{
            firstTurn = PlayerEnum.PLAYER2
            gameStatus.setFirstTurn(PlayerEnum.PLAYER2)
        }
    }

    //first card is most upper
    suspend fun muligun(){
        val data = SakuraCardCommand(CommandEnum.MULIGUN, -1)
        player1.session.send(Json.encodeToString(data))
        player2.session.send(Json.encodeToString(data))
        val player1_data = waitCardSetUntil(player1, CommandEnum.MULIGUN)
        val player2_data = waitCardSetUntil(player2, CommandEnum.MULIGUN)
        var count = 0
        player1_data.normal_card?.let {
            for(card_name in it){
                if(gameStatus.insertHandToDeck(false, true, PlayerEnum.PLAYER1, gameStatus.getCardNumber(PlayerEnum.PLAYER1, card_name))){
                    count += 1
                }
            }
        }
        gameStatus.drawCard(PlayerEnum.PLAYER1, count)

        count = 0
        player2_data.normal_card?.let {
            for(card_name in it){
                if(gameStatus.insertHandToDeck(false, true, PlayerEnum.PLAYER2, gameStatus.getCardNumber(PlayerEnum.PLAYER2, card_name))){
                    count += 1
                }
            }
        }
        gameStatus.drawCard(PlayerEnum.PLAYER2, count)
        sendMuligunEnd(player1, player2)
    }

    suspend fun startPhase(){
        gameStatus.endCurrentPhase = false
        gameStatus.nowPhase = START_PHASE
        sendStartPhaseStart(getSocket(this.turnPlayer), getSocket(this.turnPlayer.opposite()))
        gameStatus.startPhaseDefaultFirst(this.turnPlayer)
        gameStatus.startPhaseEffectProcess(this.turnPlayer)
        if(turnNumber == 0 || turnNumber == 1){
            return
        }
        gameStatus.startPhaseDefaultSecond(this.turnPlayer)
    }

    suspend fun mainPhase(){
        gameStatus.endCurrentPhase = false
        gameStatus.nowPhase = MAIN_PHASE
        sendMainPhaseStart(getSocket(this.turnPlayer), getSocket(this.turnPlayer.opposite()))
        gameStatus.mainPhaseEffectProcess(this.turnPlayer)
        if(receiveFullPowerRequest(getSocket(this.turnPlayer))){
            gameStatus.setPlayerFullAction(this.turnPlayer, true)
            while (true){
                val data = receiveFullPowerActionRequest(getSocket(this.turnPlayer))
                if(data.first == CommandEnum.ACTION_END_TURN){
                    return
                }
                else if(gameStatus.cardUseNormal(this.turnPlayer, data.first, data.second)){
                    return
                }
                else{
                    continue
                }
            }
        }
        else{
            gameStatus.setPlayerFullAction(this.turnPlayer, false)
            while (true){
                if(gameStatus.endCurrentPhase || gameStatus.getEndTurn(this.turnPlayer)){
                    return
                }
                val data = receiveActionRequest(getSocket(this.turnPlayer))
                if(data.first == CommandEnum.ACTION_END_TURN) return
                else if(data.first == CommandEnum.ACTION_USE_CARD_HAND || data.first == CommandEnum.ACTION_USE_CARD_SPECIAL || data.first == CommandEnum.ACTION_USE_CARD_COVER){
                    gameStatus.cardUseNormal(this.turnPlayer, data.first, data.second)
                }
                else{
                    if(gameStatus.canDoBasicOperation(this.turnPlayer, data.first)){
                        if(gameStatus.basicOperationCost(this.turnPlayer, data.second)){
                            gameStatus.doBasicOperation(this.turnPlayer, data.first, if(data.second == -1) -1 else 0)
                        }
                    }
                }
            }
        }
    }

    suspend fun endPhase(){
        gameStatus.endCurrentPhase = false
        gameStatus.nowPhase = END_PHASE
        sendEndPhaseStart(getSocket(this.turnPlayer), getSocket(this.turnPlayer.opposite()))
        gameStatus.endPhaseEffectProcess(this.turnPlayer)
        gameStatus.setEndTurn(PlayerEnum.PLAYER1, false)
        gameStatus.setEndTurn(PlayerEnum.PLAYER2, false)
        gameStatus.endTurnHandCheck(this.turnPlayer)
        gameStatus.logger.reset()
        this.turnPlayer = this.turnPlayer.opposite()
        this.turnNumber += 1
    }

    suspend fun gameStart(){
        this.turnPlayer = this.firstTurn

        while(true){
            startPhase()
            mainPhase()
            endPhase()
            if(gameStatus.gameEnd) {
                break
            }
        }
    }

    suspend fun startGame(){
        selectMode()
        selectEnd()
        selectMegami()
        if(gameMode == GameMode.SAM_SEUB_IL_SA){
            checkMegami()
            selectBan()
        }
        selectFirst()
        checkFinalMegami()
        selectCard()
        gameStatus.drawCard(PlayerEnum.PLAYER1, 3)
        gameStatus.drawCard(PlayerEnum.PLAYER2, 3)
        muligun()
        gameStart()
    }
}