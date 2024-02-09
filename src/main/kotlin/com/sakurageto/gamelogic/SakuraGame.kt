package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.RoomInformation
import com.sakurageto.card.*
import com.sakurageto.gamelogic.GameStatus.Companion.END_PHASE
import com.sakurageto.gamelogic.GameStatus.Companion.MAIN_PHASE
import com.sakurageto.gamelogic.GameStatus.Companion.START_PHASE
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import com.sakurageto.protocol.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random


class SakuraGame(private val roomNumber: Int, val player1: Connection, val player2: Connection) {
    private var gameMode: GameMode //0 = no ban 1 = pick ban
    private var gameStatus: GameStatus

    private var turnNumber = 0
    private var firstTurn = PlayerEnum.PLAYER1

    private fun getSocket(player: PlayerEnum): Connection{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    init {
        gameMode = GameMode.SSANG_JANG_YO_LAN
        gameStatus = GameStatus(PlayerStatus(PlayerEnum.PLAYER1), PlayerStatus(PlayerEnum.PLAYER2), player1, player2)
        RoomInformation.roomHashMap[roomNumber]?.game = gameStatus
    }

    /***
     * firstCode and secondCode are used to reconnect user, it is not associated with selectVersion's mechanism
     */
    private suspend fun selectVersion(){
        val random = Random(System.currentTimeMillis())
        val firstCode = random.nextInt()
        val secondCode = random.nextInt()
        RoomInformation.roomHashMap[roomNumber]?.firstUserCode = firstCode
        RoomInformation.roomHashMap[roomNumber]?.secondUserCode = secondCode
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true;}
        val dataPlayer1 = SakuraCardCommand(CommandEnum.SELECT_VERSION_OTHER, firstCode)
        val dataPlayer2 = SakuraCardCommand(CommandEnum.SELECT_VERSION_YOUR, secondCode)
        player1.session.send(json.encodeToString(dataPlayer1))
        player2.session.send(json.encodeToString(dataPlayer2))
        val dataGet = waitUntil(player2, CommandEnum.SELECT_VERSION_YOUR).data?: mutableListOf(0)
        val version = if(dataGet.size != 0) GameVersion.fromInt(dataGet[0]) else GameVersion.VERSION_7_2
        val endData = SakuraCardCommand(CommandEnum.SET_VERSION, version.real_number)
        player1.session.send(json.encodeToString(endData))
        player2.session.send(json.encodeToString(endData))
        gameStatus.version = version
    }

    private suspend fun selectMode(){
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true; encodeDefaults = true;}
        val dataPlayer1 = SakuraCardCommand(CommandEnum.SELECT_MODE_OTHER)
        val dataPlayer2 = SakuraCardCommand(CommandEnum.SELECT_MODE_YOUR)
        player1.session.send(json.encodeToString(dataPlayer1))
        player2.session.send(json.encodeToString(dataPlayer2))
        val dataGet = waitUntil(player2, CommandEnum.SELECT_MODE_YOUR).data?: mutableListOf(0)
        val mode = if (dataGet.isEmpty()) 0 else dataGet[0]
        if(mode == 0){
            this.gameMode = GameMode.SSANG_JANG_YO_LAN
        }
        else{
            this.gameMode = GameMode.SAM_SEUB_IL_SA
        }
    }

    private suspend fun selectEnd(){
        val data = SakuraSendData(CommandEnum.END_OF_SELECTMODE, mutableListOf(gameMode.real_number))
        val sendData = Json.encodeToString(data)
        player1.session.send(sendData)
        player2.session.send(sendData)
    }

    private suspend fun selectMegami(){
        val data = SakuraCardCommand(CommandEnum.SELECT_MEGAMI, -1)
        val sendData = Json.encodeToString(data)
        player1.session.send(sendData)
        player2.session.send(sendData)
        val player1Data = waitUntil(player1, CommandEnum.SELECT_MEGAMI)
        val player2Data = waitUntil(player2, CommandEnum.SELECT_MEGAMI)
        if(gameMode == GameMode.SSANG_JANG_YO_LAN){
            gameStatus.player1.setMegamiSsangjang(player1Data)
            gameStatus.player2.setMegamiSsangjang(player2Data)
        }
        else if(gameMode == GameMode.SAM_SEUB_IL_SA){
            gameStatus.player1.setMegamiSamSep(player1Data)
            gameStatus.player2.setMegamiSamSep(player2Data)
        }
    }

    private suspend fun checkMegami(){
        val checkDataPlayer1 = SakuraSendData(CommandEnum.CHECK_MEGAMI, gameStatus.player1.returnListMegami3())
        val checkDataPlayer2 = SakuraSendData(CommandEnum.CHECK_MEGAMI, gameStatus.player2.returnListMegami3())
        player1.session.send(Json.encodeToString(checkDataPlayer2))
        player2.session.send(Json.encodeToString(checkDataPlayer1))
    }

    private suspend fun selectBan(){
        val player1Data = waitUntil(player1, CommandEnum.SELECT_BAN)
        val player2Data = waitUntil(player2, CommandEnum.SELECT_BAN)

        gameStatus.player1.banMegami(player2Data)
        gameStatus.player2.banMegami(player1Data)
    }

    private suspend fun checkFinalMegami(){
        val player1Player1Data = gameStatus.player1.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player2Player2Data = gameStatus.player2.makeMegamiData(CommandEnum.CHECK_YOUR)
        val player1Player2Data = gameStatus.player2.makeMegamiData(CommandEnum.CHECK_ANOTHER)
        val player2Player1Data = gameStatus.player1.makeMegamiData(CommandEnum.CHECK_ANOTHER)

        player1.session.send(Json.encodeToString(player1Player1Data))
        player2.session.send(Json.encodeToString(player2Player2Data))

        player1.session.send(Json.encodeToString(player1Player2Data))
        player2.session.send(Json.encodeToString(player2Player1Data))

        settingForAnotherMegami(PlayerEnum.PLAYER1, gameStatus.player1.megamiOne)
        settingForAnotherMegami(PlayerEnum.PLAYER1, gameStatus.player1.megamiTwo)
        settingForAnotherMegami(PlayerEnum.PLAYER2, gameStatus.player2.megamiOne)
        settingForAnotherMegami(PlayerEnum.PLAYER2, gameStatus.player2.megamiTwo)

        settingForMegami(PlayerEnum.PLAYER1, gameStatus.player1.megamiOne.changeNormalMegami())
        settingForMegami(PlayerEnum.PLAYER1, gameStatus.player1.megamiTwo.changeNormalMegami())
        settingForMegami(PlayerEnum.PLAYER2, gameStatus.player2.megamiOne.changeNormalMegami())
        settingForMegami(PlayerEnum.PLAYER2, gameStatus.player2.megamiTwo.changeNormalMegami())
    }

    private fun settingForMegami(player: PlayerEnum, megami: MegamiEnum){
        when(megami){
            MegamiEnum.YUKIHI -> settingForYukihi(player)
            MegamiEnum.SHINRA -> settingForShinra(player)
            MegamiEnum.CHIKAGE -> settingForChikage(player)
            MegamiEnum.THALLYA -> settingForThallya(player)
            MegamiEnum.RAIRA -> settingForRaira(player)
            MegamiEnum.MIZUKI -> settingForMizuki(player)
            MegamiEnum.MEGUMI -> settingForMegumi(player)
            MegamiEnum.KANAWE -> settingForKanawe(player)
            MegamiEnum.KAMUWI -> settingForKamuwi(player)
            MegamiEnum.AKINA -> settingForAkina(player)
            MegamiEnum.MISORA -> settingForMisora(player)
            else -> {}
        }
    }

    private fun settingForYukihi(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.umbrella = Umbrella.FOLD
        if(nowPlayer.megamiOneNormalForm() == MegamiEnum.YUKIHI){
            nowPlayer.megamiCard = Card.cardMakerByName(firstTurn == player, CardName.YUKIHI_YUKIHI, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(firstTurn == player, CardName.YUKIHI_YUKIHI, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForShinra(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.stratagem = Stratagem.SHIN_SAN
        if(nowPlayer.megamiOneNormalForm() == MegamiEnum.SHINRA){
            nowPlayer.megamiCard = Card.cardMakerByName(firstTurn == player, CardName.SHINRA_SHINRA, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(firstTurn == player, CardName.SHINRA_SHINRA, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForChikage(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)
        val turnCheck = firstTurn == player.opposite()

        for(card_name in CardName.poisonList){
            val card = Card.cardMakerByName(turnCheck, card_name, player.opposite(),
                LocationEnum.POISON_BAG, gameStatus.version)
            nowPlayer.poisonBag[card.card_data.card_name] = card
        }
    }

    private fun settingForThallya(player: PlayerEnum){
        when(player){
            PlayerEnum.PLAYER1 -> {
                gameStatus.player1.artificialToken = 5
                gameStatus.player1ManeuverListener = ArrayDeque()
            }
            PlayerEnum.PLAYER2 -> {
                gameStatus.player2.artificialToken = 5
                gameStatus.player2ManeuverListener = ArrayDeque()
            }
        }
    }

    private fun settingForRaira(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.windGauge = 0
        nowPlayer.thunderGauge = 0
    }

    private fun settingForMizuki(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)
        val turnCheck = firstTurn == player

        for(card_name in CardName.soldierList){
            val card = Card.cardMakerByName(turnCheck, card_name, player,
                LocationEnum.NOT_READY_SOLDIER_ZONE, gameStatus.version)
            nowPlayer.notReadySoldierZone[card.card_number] = card
        }
    }

    private fun settingForMegumi(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)
        nowPlayer.notReadySeed = 5
    }

    private fun settingForKanawe(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.nowAct = StoryBoard.getActByNumber(0)
        if(nowPlayer.megamiOneNormalForm() == MegamiEnum.KANAWE){
            nowPlayer.megamiCard = Card.cardMakerByName(firstTurn == player,
                CardName.KANAWE_KANAWE, player, LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(firstTurn == player,
                CardName.KANAWE_KANAWE, player, LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForKamuwi(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.tabooGauge = 0
    }

    private fun settingForAkina(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.flow = 0
        if(nowPlayer.megamiOneNormalForm() == MegamiEnum.AKINA){
            nowPlayer.megamiCard = Card.cardMakerByName(firstTurn == player, CardName.AKINA_AKINA, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(firstTurn == player, CardName.AKINA_AKINA, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }

        nowPlayer.marketPrice = 2
    }

    private fun settingForMisora(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        if(nowPlayer.megamiOneNormalForm() == MegamiEnum.SHINRA){
            nowPlayer.megamiCard = Card.cardMakerByName(firstTurn == player, CardName.MISORA_MISORA, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard?.special_card_state = SpecialCardEnum.PLAYED
        }
        else{
            nowPlayer.megamiCard2 = Card.cardMakerByName(firstTurn == player, CardName.MISORA_MISORA, player,
                LocationEnum.YOUR_USED_CARD, gameStatus.version)
            nowPlayer.megamiCard2?.special_card_state = SpecialCardEnum.PLAYED
        }
    }

    private fun settingForAnotherMegami(player: PlayerEnum, megami: MegamiEnum){
        when(megami){
            MegamiEnum.YATSUHA_AA1 -> settingForYatsuhaAA1(player)
            MegamiEnum.RENRI_A1 -> settingForRenriA1(player)
            MegamiEnum.OBORO_A2 -> settingForOboroA2(player)
            else -> {}
        }
    }

    private fun settingForRenriA1(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)
        nowPlayer.relic = HashMap()
        nowPlayer.perjuryInstallation = hashSetOf(CardName.RENRI_FALSE_WEAPON)
        for(card_name in CardName.relicList){
            val card = Card.cardMakerByName(nowPlayer.firstTurn, card_name, player,
                LocationEnum.RELIC_YOUR, gameStatus.version)
            nowPlayer.relic!![card.card_number] = card
        }
    }

    private fun settingForYatsuhaAA1(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.memory = hashMapOf()
    }

    private fun settingForOboroA2(player: PlayerEnum){
        val nowPlayer = gameStatus.getPlayer(player)

        nowPlayer.assemblyMainZone = hashMapOf()
        nowPlayer.assemblyCustomZone = hashMapOf()
        nowPlayer.unassemblyZone = hashMapOf()

        for(card_name in CardName.partsList){
            val card = Card.cardMakerByName(nowPlayer.firstTurn, card_name, player,
                LocationEnum.UNASSEMBLY_YOUR, gameStatus.version)
            nowPlayer.unassemblyZone!![card.card_number] = card
        }
    }

    private fun checkCardSet(bigger: MutableSet<CardName>, smaller: MutableList<CardName>?, size: Int): Boolean{
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

    private suspend fun selectCard(){
        gameStatus.player1.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.version, gameStatus.player1.megamiOne))
        gameStatus.player1.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.version, gameStatus.player1.megamiTwo))
        gameStatus.player2.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.version, gameStatus.player2.megamiOne))
        gameStatus.player2.unselectedCard.addAll(CardName.returnNormalCardNameByMegami(gameStatus.version, gameStatus.player2.megamiTwo))
        gameStatus.player1.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.version, gameStatus.player1.megamiOne))
        gameStatus.player1.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.version, gameStatus.player1.megamiTwo))
        gameStatus.player2.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.version, gameStatus.player2.megamiOne))
        gameStatus.player2.unselectedSpecialCard.addAll(CardName.returnSpecialCardNameByMegami(gameStatus.version, gameStatus.player2.megamiTwo))

        val sendRequestPlayer1 = SakuraCardSetSend(CommandEnum.SELECT_CARD,
            gameStatus.player1.unselectedCard.toMutableList(), gameStatus.player1.unselectedSpecialCard.toMutableList())
        val sendRequestPlayer2 = SakuraCardSetSend(CommandEnum.SELECT_CARD,
            gameStatus.player2.unselectedCard.toMutableList(), gameStatus.player2.unselectedSpecialCard.toMutableList())

        player1.session.send(Json.encodeToString(sendRequestPlayer1))
        player2.session.send(Json.encodeToString(sendRequestPlayer2))

        val player1Data = waitCardSetUntil(player1, CommandEnum.SELECT_CARD)
        val player2Data = waitCardSetUntil(player2, CommandEnum.SELECT_CARD)

        val cardDataPlayer1: MutableList<CardName> = mutableListOf()
        val specialCardDataPlayer1: MutableList<CardName> = mutableListOf()
        val cardDataPlayer2 : MutableList<CardName> = mutableListOf()
        val specialCardDataPlayer2 : MutableList<CardName> = mutableListOf()

        if(checkCardSet(gameStatus.player1.unselectedCard, player1Data.normal_card, 7))
            cardDataPlayer1.addAll(player1Data.normal_card!!)
        else
            cardDataPlayer1.addAll(gameStatus.player1.unselectedCard.toMutableList().subList(0, 7))

        if(checkCardSet(gameStatus.player2.unselectedCard, player2Data.normal_card, 7))
            cardDataPlayer2.addAll(player2Data.normal_card!!)
        else
            cardDataPlayer2.addAll(gameStatus.player2.unselectedCard.toMutableList().subList(0, 7))

        if(checkCardSet(gameStatus.player1.unselectedSpecialCard, player1Data.special_card, 3))
            specialCardDataPlayer1.addAll(player1Data.special_card!!)
        else
            specialCardDataPlayer1.addAll(gameStatus.player1.unselectedSpecialCard.toMutableList().subList(0, 3))

        if(checkCardSet(gameStatus.player2.unselectedSpecialCard, player2Data.special_card, 3))
            specialCardDataPlayer2.addAll(player2Data.special_card!!)
        else
            specialCardDataPlayer2.addAll(gameStatus.player2.unselectedSpecialCard.toMutableList().subList(0, 3))

        val endPlayer1Select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, cardDataPlayer1, specialCardDataPlayer1)
        val endPlayer2Select = SakuraCardSetSend(CommandEnum.END_SELECT_CARD, cardDataPlayer2, specialCardDataPlayer2)

        player1.session.send(Json.encodeToString(endPlayer1Select))
        player2.session.send(Json.encodeToString(endPlayer2Select))

        when(firstTurn){
            PlayerEnum.PLAYER1 -> {
                Card.cardInitInsert(true, gameStatus.player1.normalCardDeck, cardDataPlayer1,
                    PlayerEnum.PLAYER1, gameStatus.version)
                Card.cardInitInsert(true, gameStatus.player1.specialCardDeck, specialCardDataPlayer1,
                    PlayerEnum.PLAYER1, gameStatus.version)
                Card.cardInitInsert(false, gameStatus.player2.normalCardDeck, cardDataPlayer2,
                    PlayerEnum.PLAYER2, gameStatus.version)
                Card.cardInitInsert(false, gameStatus.player2.specialCardDeck, specialCardDataPlayer2,
                    PlayerEnum.PLAYER2, gameStatus.version)
            }
            PlayerEnum.PLAYER2 -> {
                Card.cardInitInsert(false, gameStatus.player1.normalCardDeck, cardDataPlayer1,
                    PlayerEnum.PLAYER1, gameStatus.version)
                Card.cardInitInsert(false, gameStatus.player1.specialCardDeck, specialCardDataPlayer1,
                    PlayerEnum.PLAYER1, gameStatus.version)
                Card.cardInitInsert(true, gameStatus.player2.normalCardDeck, cardDataPlayer2,
                    PlayerEnum.PLAYER2, gameStatus.version)
                Card.cardInitInsert(true, gameStatus.player2.specialCardDeck, specialCardDataPlayer2,
                    PlayerEnum.PLAYER2, gameStatus.version)
            }
        }

        val additionalCardPlayer1 = mutableListOf<CardName>()
        val additionalCardPlayer2 = mutableListOf<CardName>()
        additionalCardPlayer1.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player1.megamiOne))
        additionalCardPlayer2.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player2.megamiOne))
        additionalCardPlayer1.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player1.megamiTwo))
        additionalCardPlayer2.addAll(CardName.returnAdditionalCardNameByMegami(gameStatus.player2.megamiTwo))

        if(additionalCardPlayer1.isNotEmpty()){
            val turnCheck = firstTurn == PlayerEnum.PLAYER1
            for(card_name in additionalCardPlayer1){
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER1,
                    LocationEnum.ADDITIONAL_CARD, gameStatus.version)
                gameStatus.player1.additionalHand[card.card_data.card_name] = card
            }
        }

        if(additionalCardPlayer2.isNotEmpty()){
            val turnCheck = firstTurn == PlayerEnum.PLAYER2
            for(card_name in additionalCardPlayer2){
                val card = Card.cardMakerByName(turnCheck, card_name, PlayerEnum.PLAYER2,
                    LocationEnum.ADDITIONAL_CARD, gameStatus.version)
                gameStatus.player2.additionalHand[card.card_data.card_name] = card
            }
        }

        gameStatus.player1.deleteSelectedNormalCard(cardDataPlayer1)
        gameStatus.player1.deleteSelectedSpecialCard(specialCardDataPlayer1)

        gameStatus.player2.deleteSelectedNormalCard(cardDataPlayer2)
        gameStatus.player2.deleteSelectedSpecialCard(specialCardDataPlayer2)
    }

     private fun decideFirstTurn(){
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
    private suspend fun muligun(){
        val data = SakuraCardCommand(CommandEnum.MULIGUN, -1)
        player1.session.send(Json.encodeToString(data))
        player2.session.send(Json.encodeToString(data))
        val player1Data = waitCardSetUntil(player1, CommandEnum.MULIGUN)
        val player2Data = waitCardSetUntil(player2, CommandEnum.MULIGUN)
        var count = 0
        player1Data.normal_card?.let {
            for(card_name in it){
                if(gameStatus.insertHandToDeck(public = false, Below = true, player = PlayerEnum.PLAYER1,
                        card_number = gameStatus.getCardNumber(PlayerEnum.PLAYER1, card_name)
                    )){
                    count += 1
                }
            }
        }
        gameStatus.drawCard(PlayerEnum.PLAYER1, count)

        count = 0
        player2Data.normal_card?.let {
            for(card_name in it){
                if(gameStatus.insertHandToDeck(public = false, Below = true, player = PlayerEnum.PLAYER2,
                        card_number = gameStatus.getCardNumber(PlayerEnum.PLAYER2, card_name)
                    )){
                    count += 1
                }
            }
        }
        gameStatus.drawCard(PlayerEnum.PLAYER2, count)
        sendMuligunEnd(player1, player2)
    }

    private fun isActionUseCard(action: CommandEnum) =
        action in setOf(CommandEnum.ACTION_USE_CARD_HAND, CommandEnum.ACTION_USE_CARD_SPECIAL,
            CommandEnum.ACTION_USE_CARD_COVER, CommandEnum.ACTION_USE_CARD_PERJURY, CommandEnum.ACTION_USE_CARD_SOLDIER)
    
    private fun isActionBasicOperation(action: CommandEnum) = 
        action in setOf(
            CommandEnum.ACTION_GO_FORWARD, CommandEnum.ACTION_GO_BACKWARD, CommandEnum.ACTION_WIND_AROUND,
            CommandEnum.ACTION_INCUBATE, CommandEnum.ACTION_BREAK_AWAY,
            CommandEnum.ACTION_GARUDA, CommandEnum.ACTION_YAKSHA, CommandEnum.ACTION_NAGA,
            CommandEnum.ACTION_ASURA,
        )

    private suspend fun fullPowerAction(){
        gameStatus.setPlayerFullAction(gameStatus.turnPlayer, true)
        while (true){
            val data = receiveFullPowerActionRequest(getSocket(gameStatus.turnPlayer))
            if(data.first == CommandEnum.FULL_POWER_NO){
                sendSimpleCommand(getSocket(gameStatus.turnPlayer), CommandEnum.FULL_POWER_NO)
                normalAction()
                return
            }
            else if(data.first == CommandEnum.ACTION_END_TURN){
                return
            }
            else if(isActionUseCard(data.first)){
                if(gameStatus.cardUseNormal(gameStatus.turnPlayer, data.first, data.second)){
                    return
                }
            }
        }
    }

    private suspend fun normalAction(){
        gameStatus.setPlayerFullAction(gameStatus.turnPlayer, false)
        while (true){
            if(gameStatus.endCurrentPhase || gameStatus.getEndTurn(gameStatus.turnPlayer)){
                return
            }
            val data = receiveActionRequest(getSocket(gameStatus.turnPlayer))
            if(data.first == CommandEnum.ACTION_END_TURN) {
                return
            }
            if(data.first == CommandEnum.FULL_POWER_YES || !gameStatus.isThisTurnDoAction){
                sendSimpleCommand(getSocket(gameStatus.turnPlayer), CommandEnum.FULL_POWER_YES)
                fullPowerAction()
                return
            }
            else if(isActionUseCard(data.first)){
                if(gameStatus.cardUseNormal(gameStatus.turnPlayer, data.first, data.second)){
                    gameStatus.isThisTurnDoAction = true
                }
            }
            else if(isActionBasicOperation(data.first)){
                if(gameStatus.canDoBasicOperation(gameStatus.turnPlayer, data.first) && gameStatus.payBasicOperationCost(gameStatus.turnPlayer, data.second)){
                    gameStatus.doBasicOperation(gameStatus.turnPlayer, data.first, if(data.second == -1) -1 else 0)
                    gameStatus.isThisTurnDoAction = true
                }
            }
        }
    }

    private suspend fun doMainPhaseAction(){
        if(receiveFullPowerRequest(getSocket(gameStatus.turnPlayer))){
            fullPowerAction()
        }
        else{
            normalAction()
        }
    }

    private suspend fun startPhase(){
        gameStatus.endCurrentPhase = false
        gameStatus.nowPhase = START_PHASE
        gameStatus.beforeStartPhaseEffectProcess()

        sendStartPhaseStart(getSocket(gameStatus.turnPlayer), getSocket(gameStatus.turnPlayer.opposite()))
        gameStatus.startPhaseEffectProcess()
        if(turnNumber == 0 || turnNumber == 1 || gameStatus.endCurrentPhase){
            return
        }
        gameStatus.startPhaseDefaultSecond()
    }

    private suspend fun mainPhase(){
        gameStatus.endCurrentPhase = false
        gameStatus.nowPhase = MAIN_PHASE
        if(gameStatus.getPlayer(gameStatus.turnPlayer).nextMainPhaseSkip){
            gameStatus.getPlayer(gameStatus.turnPlayer).nextMainPhaseSkip = false
            return
        }

        sendMainPhaseStart(getSocket(gameStatus.turnPlayer), getSocket(gameStatus.turnPlayer.opposite()))
        gameStatus.mainPhaseEffectProcess()
        doMainPhaseAction()
        if(gameStatus.endCurrentPhase){
            return
        }
        gameStatus.mainPhaseEndProcess()
    }

    private suspend fun endPhase(){
        gameStatus.endCurrentPhase = false
        gameStatus.nowPhase = END_PHASE

        sendEndPhaseStart(getSocket(gameStatus.turnPlayer), getSocket(gameStatus.turnPlayer.opposite()))
        gameStatus.endPhaseEffectProcess()
        gameStatus.resetTurnValue()
        gameStatus.logger.reset()
        this.turnNumber += 1
        if(gameStatus.endCurrentPhase){
            gameStatus.changeTurnPlayer()
            return
        }
        gameStatus.endTurnHandCheck()
        gameStatus.changeTurnPlayer()
    }

    private suspend fun gameStart(){
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
        selectVersion()
        selectMode()
        selectEnd()
        selectMegami()
        if(gameMode == GameMode.SAM_SEUB_IL_SA){
            checkMegami()
            selectBan()
        }
        decideFirstTurn()
        checkFinalMegami()
        selectCard()
        gameStatus.drawCard(PlayerEnum.PLAYER1, 3)
        gameStatus.drawCard(PlayerEnum.PLAYER2, 3)
        muligun()
        gameStart()
    }
}