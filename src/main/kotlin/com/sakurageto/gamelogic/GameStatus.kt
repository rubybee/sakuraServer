package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.RoomInformation
import com.sakurageto.card.*
import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.gamelogic.log.Log
import com.sakurageto.gamelogic.log.LogText
import com.sakurageto.gamelogic.log.Logger
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import com.sakurageto.protocol.*
import com.sakurageto.protocol.CommandEnum.Companion.BASIC_OPERATION_CAUSE_BY_CARD
import io.ktor.websocket.*
import kotlin.Exception

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus, private val player1_socket: Connection, private val player2_socket: Connection) {

    companion object{
        const val START_PHASE = 1
        const val START_PHASE_REDUCE_NAP = 4
        const val MAIN_PHASE = 2
        const val END_PHASE = 3

        val RENRI_FALSE_STAB = Card.cardMakerByName(true, CardName.RENRI_FALSE_STAB, PlayerEnum.PLAYER1)
        val RENRI_TEMPORARY_EXPEDIENT = Card.cardMakerByName(true, CardName.RENRI_TEMPORARY_EXPEDIENT, PlayerEnum.PLAYER1)
        val RENRI_BLACK_AND_WHITE = Card.cardMakerByName(true, CardName.RENRI_BLACK_AND_WHITE, PlayerEnum.PLAYER1)
        val RENRI_FLOATING_CLOUDS = Card.cardMakerByName(true, CardName.RENRI_FLOATING_CLOUDS, PlayerEnum.PLAYER1)
        val RENRI_FISHING = Card.cardMakerByName(true, CardName.RENRI_FISHING, PlayerEnum.PLAYER1)

        fun getPerjuryCard(card_number: Int): Card?{
            return when(card_number){
                NUMBER_RENRI_FALSE_STAB -> RENRI_FALSE_STAB
                NUMBER_RENRI_TEMPORARY_EXPEDIENT -> RENRI_TEMPORARY_EXPEDIENT
                NUMBER_RENRI_BLACK_AND_WHITE -> RENRI_BLACK_AND_WHITE
                NUMBER_RENRI_FLOATING_CLOUDS -> RENRI_FLOATING_CLOUDS
                NUMBER_RENRI_FISHING -> RENRI_FISHING
                else -> null
            }
        }
    }
    var perjuryCheck = arrayOf(false, false, false, false, false, false)

    var turnPlayer = PlayerEnum.PLAYER1

    val logger = Logger()

    var endCurrentPhase: Boolean = false
    var nowPhase: Int = START_PHASE

    var startTurnDistance = 10
    var thisTurnDistanceChange = false

    var thisTurnSwellDistance = 2


    suspend fun getAdjustSwellDistance(player: PlayerEnum): Int{
        var nowSwellDistance = thisTurnSwellDistance

        for(card in player1.enchantmentCard.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player2.enchantmentCard.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player1.usedSpecialCard.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player2.usedSpecialCard.values) nowSwellDistance += card.swellAdjust(player, this)

        return nowSwellDistance
    }

    suspend fun getAdjustDistance(): Int{
        var distance = thisTurnDistance

        distance -= player1ArtificialTokenOn
        distance -= player2ArtificialTokenOn
        distance += player1ArtificialTokenOut
        distance += player2ArtificialTokenOut

        for(card in player1.enchantmentCard.values){
            distance += card.effectAllValidEffect(PlayerEnum.PLAYER1, this, TextEffectTag.CHANGE_DISTANCE)
        }
        for(card in player2.enchantmentCard.values){
            distance += card.effectAllValidEffect(PlayerEnum.PLAYER2, this, TextEffectTag.CHANGE_DISTANCE)
        }

        if(distance < 0) distance = 0
        return distance
    }

    suspend fun addThisTurnDistance(number: Int){
        if(number == 0) return
        var value = number
        if(value > 0){
            thisTurnDistance += value
            sendSimpleCommand(player1_socket, player2_socket, CommandEnum.ADD_THIS_TURN_DISTANCE, value)
            thisTurnDistanceChange = true
        }
        else{
            if(thisTurnDistance != 0){
                thisTurnDistanceChange = true
                if(value * -1 > thisTurnDistance){
                    value = -thisTurnDistance
                }
                thisTurnDistance += value
                sendSimpleCommand(player1_socket, player2_socket, CommandEnum.REDUCE_THIS_TURN_DISTANCE, value * -1)
            }
        }
        distanceListenerProcess(PlayerEnum.PLAYER1)
        distanceListenerProcess(PlayerEnum.PLAYER2)
    }

    suspend fun addThisTurnSwellDistance(number: Int){
        var value = number
        if(value > 0){
            thisTurnSwellDistance += value
            sendSimpleCommand(player1_socket, player2_socket, CommandEnum.ADD_THIS_TURN_SWELL_DISTANCE, value)
        }
        else{
            if(value * -1 > thisTurnSwellDistance){
                value = -thisTurnSwellDistance
            }
            thisTurnSwellDistance += value
            sendSimpleCommand(player1_socket, player2_socket, CommandEnum.REDUCE_THIS_TURN_SWELL_DISTANCE, value * -1)
        }
    }

    var distanceToken = 10
    var thisTurnDistance = 10
    var dust = 0

    var player1ArtificialTokenOn = 0
    var player1ArtificialTokenOut = 0
    var player2ArtificialTokenOn = 0
    var player2ArtificialTokenOut = 0

    suspend fun restoreArtificialToken(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)
        nowPlayer.artificialToken?.let {
            var value = number
            if(nowPlayer.artificialTokenBurn < value) value = nowPlayer.artificialTokenBurn
            nowPlayer.artificialToken = it + value
            nowPlayer.artificialTokenBurn -= value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN,
                LocationEnum.MACHINE_BURN_YOUR, LocationEnum.MACHINE_YOUR, value, -1)
        }

    }

    //before call this function must check player have enough artificial token
    suspend fun combust(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)
        nowPlayer.artificialToken?.let {
            nowPlayer.artificialToken = it - number
            nowPlayer.artificialTokenBurn += number
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN,
                LocationEnum.MACHINE_YOUR, LocationEnum.MACHINE_BURN_YOUR, number, -1)
        }

    }

    //before call this function must check player have enough artificial token
    suspend fun addArtificialTokenAtDistance(player: PlayerEnum, on: Boolean, number: Int){
        when(player){
            PlayerEnum.PLAYER1 -> {
                if(player1.artificialToken == null) return
                if(on){
                    thisTurnDistanceChange = true
                    player1.artificialToken = player1.artificialToken!! - number
                    player1ArtificialTokenOn += number
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
                else{
                    thisTurnDistanceChange = true
                    player1.artificialToken = player1.artificialToken!! - number
                    player1ArtificialTokenOut += number
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(player2.artificialToken == null) return
                if(on){
                    thisTurnDistanceChange = true
                    player2.artificialToken = player2.artificialToken!! - number
                    player2ArtificialTokenOn += number
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
                else{
                    thisTurnDistanceChange = true
                    player2.artificialToken = player2.artificialToken!! - number
                    player2ArtificialTokenOut += number
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
            }
        }
    }
    var player1Listener: ArrayDeque<Listener> = ArrayDeque()
    var player2Listener: ArrayDeque<Listener> = ArrayDeque()

    val player1UmbrellaListener: ArrayDeque<Listener> = ArrayDeque()
    val player2UmbrellaListener: ArrayDeque<Listener> = ArrayDeque()

    private var player1TempAttackBuff = AttackBuffQueue()
    private var player2TempAttackBuff = AttackBuffQueue()

    private var player1TempRangeBuff = RangeBuffQueue()
    private var player2TempRangeBuff = RangeBuffQueue()

    private var player1TempOtherBuff = OtherBuffQueue()
    private var player2TempOtherBuff = OtherBuffQueue()

    lateinit var first_turn: PlayerEnum

    fun getPlayer(player: PlayerEnum): PlayerStatus{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    fun getSocket(player: PlayerEnum): Connection{
        return if(player ==  PlayerEnum.PLAYER1) player1_socket else player2_socket
    }

    fun getPlayerTempAttackBuff(player: PlayerEnum): AttackBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1TempAttackBuff else player2TempAttackBuff
    }

    fun getPlayerAttackBuff(player: PlayerEnum): AttackBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1.attackBuff else player2.attackBuff
    }

    fun getPlayerTempRangeBuff(player: PlayerEnum): RangeBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1TempRangeBuff else player2TempRangeBuff
    }

    fun getPlayerRangeBuff(player: PlayerEnum): RangeBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1.rangeBuff else player2.rangeBuff
    }

    fun getPlayerTempOtherBuff(player: PlayerEnum): OtherBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1TempOtherBuff else player2TempOtherBuff
    }

    fun getPlayerOtherBuff(player: PlayerEnum): OtherBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1.otherBuff else player2.otherBuff
    }

    fun getCardNumber(player: PlayerEnum, card_name: CardName): Int{
        return if (getPlayer(player).firstTurn) card_name.toCardNumber(true)
        else card_name.toCardNumber(false)
    }

    fun getPlayerLife(player: PlayerEnum): Int{
        return when (player){
            PlayerEnum.PLAYER1 -> player1.life
            PlayerEnum.PLAYER2 -> player2.life
        }
    }

    fun getPlayerAura(player: PlayerEnum): Int{
        return when (player){
            PlayerEnum.PLAYER1 -> player1.aura
            PlayerEnum.PLAYER2 -> player2.aura
        }
    }

    fun getPlayerFlare(player: PlayerEnum): Int{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.flare
            PlayerEnum.PLAYER2 -> player2.flare
        }
    }

    fun getEndTurn(player: PlayerEnum): Boolean{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.end_turn
            PlayerEnum.PLAYER2 -> player2.end_turn
        }
    }

    fun getPlayerHandSize(player: PlayerEnum): Int{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.hand.size
            PlayerEnum.PLAYER2 -> player2.hand.size
        }
    }

    fun getConcentration(player: PlayerEnum): Int{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.concentration
            PlayerEnum.PLAYER2 -> player2.concentration
        }
    }

    fun getFullAction(player: PlayerEnum): Boolean{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.fullAction
            PlayerEnum.PLAYER2 -> player2.fullAction
        }
    }
    
    suspend fun setConcentration(player: PlayerEnum, number: Int){
        when(player){
            PlayerEnum.PLAYER1 -> player1.concentration = number
            PlayerEnum.PLAYER2 -> player2.concentration = number
        }
        sendSetConcentration(getSocket(player), getSocket(player.opposite()), number)
    }

    fun setPlayerFullAction(player: PlayerEnum, full: Boolean){
        when (player){
            PlayerEnum.PLAYER1 -> player1.fullAction = full
            PlayerEnum.PLAYER2 -> player2.fullAction = full
        }
    }

    suspend fun setShrink(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.shrink = true
        sendSetShrink(getSocket(player), getSocket(player.opposite()))
    }

    fun setFirstTurn(player: PlayerEnum){
        first_turn = player
        when(player){
            PlayerEnum.PLAYER1 -> {
                player1.firstTurn = true
                player2.concentration = 1
            }
            PlayerEnum.PLAYER2 -> {
                player2.firstTurn = true
                player1.concentration = 1
            }
        }
    }

    fun setEndTurn(player: PlayerEnum, turn: Boolean){
        when(player){
            PlayerEnum.PLAYER1 -> player1.end_turn = turn
            PlayerEnum.PLAYER2 -> player2.end_turn = turn
        }
    }

    suspend fun receiveCardEffectSelect(player: PlayerEnum, card_number: Int, command: CommandEnum = CommandEnum.SELECT_CARD_EFFECT): CommandEnum{
        return receiveCardEffectSelect(getSocket(player), card_number, command)
    }

    fun addImmediateLifeListener(player: PlayerEnum, listener: Listener){
        when(player){
            PlayerEnum.PLAYER1 -> player1Listener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2Listener.addLast(listener)
        }
    }

    fun addImmediateUmbrellaListener(player: PlayerEnum, listener: Listener){
        when(player){
            PlayerEnum.PLAYER1 -> player1UmbrellaListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2UmbrellaListener.addLast(listener)
        }
    }

    private val player1ReconstructListener: ArrayDeque<Listener> = ArrayDeque()
    private val player2ReconstructListener: ArrayDeque<Listener> = ArrayDeque()

    fun addImmediateReconstructListener(player: PlayerEnum, listener: Listener){
        when(player){
            PlayerEnum.PLAYER1 -> player1ReconstructListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2ReconstructListener.addLast(listener)
        }
    }

    fun getImmediateReconstructListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1ReconstructListener
            PlayerEnum.PLAYER2 -> player2ReconstructListener
        }
    }

    var player1ManeuverListener: ArrayDeque<Listener>? = null
    var player2ManeuverListener: ArrayDeque<Listener>? = null

    fun getManeuverListener(player: PlayerEnum): ArrayDeque<Listener>?{
        return when(player){
            PlayerEnum.PLAYER1 -> player1ManeuverListener
            PlayerEnum.PLAYER2 -> player2ManeuverListener
        }
    }

    fun addImmediateManeuverListener(player: PlayerEnum, listener: Listener) {
        when (player) {
            PlayerEnum.PLAYER1 -> player1ManeuverListener?.addLast(listener)
            PlayerEnum.PLAYER2 -> player2ManeuverListener?.addLast(listener)
        }
    }

    var player1AdditionalListener: ArrayDeque<Listener> = ArrayDeque()
    var player2AdditionalListener: ArrayDeque<Listener> = ArrayDeque()

    fun getAdditionalListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1AdditionalListener
            PlayerEnum.PLAYER2 -> player2AdditionalListener
        }
    }

    fun addAdditionalListener(player: PlayerEnum, listener: Listener) {
        when (player) {
            PlayerEnum.PLAYER1 -> player1AdditionalListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2AdditionalListener.addLast(listener)
        }
    }

    var player1MainPhaseListener: ArrayDeque<Listener> = ArrayDeque()
    var player2MainPhaseListener: ArrayDeque<Listener> = ArrayDeque()

    fun getMainPhaseListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1MainPhaseListener
            PlayerEnum.PLAYER2 -> player2MainPhaseListener
        }
    }

    fun addMainPhaseListener(player: PlayerEnum, listener: Listener) {
        when (player) {
            PlayerEnum.PLAYER1 -> player1MainPhaseListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2MainPhaseListener.addLast(listener)
        }
    }

    fun removeMainPhaseListener(player: PlayerEnum, card_number: Int){
        when(player){
            PlayerEnum.PLAYER1 -> {
                player1MainPhaseListener.removeIf {
                    it.cardNumber == card_number
                }
            }
            PlayerEnum.PLAYER2 -> {
                player2MainPhaseListener.removeIf {
                    it.cardNumber == card_number
                }
            }
        }
    }

    var player1AuraListener: ArrayDeque<Listener> = ArrayDeque()
    var player2AuraListener: ArrayDeque<Listener> = ArrayDeque()

    fun getAuraListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1AuraListener
            PlayerEnum.PLAYER2 -> player2AuraListener
        }
    }

    fun addAuraListener(player: PlayerEnum, listener: Listener) {
        when (player) {
            PlayerEnum.PLAYER1 -> player1AuraListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2AuraListener.addLast(listener)
        }
    }

    private val player1DistanceListener: ArrayDeque<Listener> = ArrayDeque()
    private val player2DistanceListener: ArrayDeque<Listener> = ArrayDeque()

    fun getDistanceListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1DistanceListener
            PlayerEnum.PLAYER2 -> player2DistanceListener
        }
    }

    fun addDistanceListener(player: PlayerEnum, listener: Listener) {
        when (player) {
            PlayerEnum.PLAYER1 -> player1DistanceListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2DistanceListener.addLast(listener)
        }
    }

    private val player1TerminationListener: ArrayDeque<Listener> = ArrayDeque()
    private val player2TerminationListener: ArrayDeque<Listener> = ArrayDeque()

    fun getTerminationListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1TerminationListener
            PlayerEnum.PLAYER2 -> player2TerminationListener
        }
    }

    fun addTerminationListener(player: PlayerEnum, listener: Listener) {
        when (player) {
            PlayerEnum.PLAYER1 -> player1TerminationListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2TerminationListener.addLast(listener)
        }
    }

    suspend fun distanceListenerProcess(player: PlayerEnum){
        val distanceListener = getDistanceListener(player)
        if(!distanceListener.isEmpty()){
            for(i in 1..distanceListener.size){
                if(distanceListener.isEmpty()) break
                val now = distanceListener.first()
                distanceListener.removeFirst()
                if(!(now.doAction(this, -1, -1, booleanPara1 = false, booleanPara2 = false))){
                    distanceListener.addLast(now)
                }
            }
        }
    }

    suspend fun moveTokenCardToSome(player: PlayerEnum, place: Int, number: Int, card: Card, card_number: Int){
        if(place == 9) cardToDistance(player, number, card, card_number)
        else if(place == 0) cardToAura(player, number, card, card_number)
        else if(place == 2) cardToFlare(player, number, card, card_number)
        else if(place > 100){
            cardToCard(player, number, card, getPlayer(player).enchantmentCard[place]!!, card_number)
        }
    }

    //true means cannot move
    suspend fun moveTokenCheckArrow(from: LocationEnum, to: LocationEnum): Boolean{
        var now: Int
        for(card in player1.enchantmentCard.values){
            now = card.forbidTokenMoveUsingArrow(PlayerEnum.PLAYER1, this)
            if(now == -1) continue
            if(now / 100 == from.real_number || now % 100 == to.real_number) return true
        }
        for(card in player2.enchantmentCard.values){
            now = card.forbidTokenMoveUsingArrow(PlayerEnum.PLAYER2, this)
            if(now == -1) continue
            if(now / 100 == from.real_number || now % 100 == to.real_number) return true
        }
        return false
    }

    /***
    SOME AURA CHANGE FUNCTION ADDED, THIS FUNCTION MUST BE EDDITED
     */
    suspend fun auraListenerProcess(player: PlayerEnum, beforeFull: Boolean, afterFull: Boolean){
        val auraListener = getAuraListener(player)
        if(!(auraListener.isEmpty())){
            for(i in 1..auraListener.size){
                if(auraListener.isEmpty()) break
                val now = auraListener.first()
                auraListener.removeFirst()
                if(!(now.doAction(this, -1, -1, booleanPara1 = beforeFull, booleanPara2 = afterFull))){
                    auraListener.addLast(now)
                }
            }
        }
    }

    suspend fun lifeListenerProcess(player: PlayerEnum, before: Int, reconstruct: Boolean, damage: Boolean){
        val nowPlayer = getPlayer(player)
        when(player){
            PlayerEnum.PLAYER1 -> {
                if(!player1Listener.isEmpty()){
                    for(i in 1..player1Listener.size){
                        if(player1Listener.isEmpty()) break
                        val now = player1Listener.first()
                        player1Listener.removeFirst()
                        if(!(now.doAction(this, before, nowPlayer.life, reconstruct, damage))){
                            player1Listener.addLast(now)
                        }
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(!player2Listener.isEmpty()){
                    for(i in 1..player2Listener.size){
                        if(player2Listener.isEmpty()) break
                        val now = player2Listener.first()
                        player2Listener.removeFirst()
                        if(!(now.doAction(this, before, nowPlayer.life, reconstruct, damage))){
                            player2Listener.addLast(now)
                        }
                    }
                }
            }
        }
    }

    suspend fun getBothDirection(player: PlayerEnum, locAndNumber: Int): Boolean{
        while(true){
            return when(receiveCardEffectSelect(player, locAndNumber, CommandEnum.SELECT_ARROW_DIRECTION)){
                CommandEnum.SELECT_ONE -> false
                CommandEnum.SELECT_TWO -> true
                else -> continue
            }
        }
    }

    suspend fun bothDirectionCheck(player: PlayerEnum): Boolean{
        for(card in getPlayer(player).enchantmentCard.values){
            if(card.effectAllValidEffect(player, this, TextEffectTag.CHANGE_ARROW_BOTH) != 0){
                return true
            }
        }
        return false
    }

    suspend fun chasmProcess(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        for(enchantment_card in nowPlayer.enchantmentCard){
            if(enchantment_card.value.chasmCheck()){
                enchantmentDestructionNotNormally(player, enchantment_card.value)
            }
        }

    }

    suspend fun cardToDustCheck(player: PlayerEnum, number: Int, card: Card, startPhaseProcess: Boolean, card_number: Int): Boolean{
        val locationList = ArrayDeque<Int>()

        if(startPhaseProcess){
            val nowPlayer = getPlayer(player)
            for(enchantment in nowPlayer.enchantmentCard.values){
                if(card === enchantment){
                    continue
                }
                enchantment.card_data.effect?.let {
                    for (text in it){
                        if(text.tag == TextEffectTag.OTHER_CARD_NAP_LOCATION_HERE){
                            locationList.addLast(text.effect!!(card.card_number, player, this, null)!!)
                        }
                        break
                    }
                }
            }
        }

        card.card_data.effect?.let {
            for (text in it){
                if(text.tag == TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE){
                    locationList.addLast(text.effect!!(card.card_number, player, this, null)!!)
                }
                break
            }
        }

        return when (locationList.size) {
            0 -> true
            1 -> {
                moveTokenCardToSome(player, locationList[0], number, card, card_number)
                false
            }
            else -> {
                while(true){
                    val receiveData = receiveSelectCard(getSocket(turnPlayer), locationList, CommandEnum.SELECT_NAP_LOCATION, -1)
                    if(receiveData == null || receiveData.size != 1){
                        continue
                    }
                    moveTokenCardToSome(player, receiveData[0], number, card, card_number)
                    break
                }
                false
            }
        }
    }

    suspend fun checkWhenGetAura(player: PlayerEnum): Boolean{
        var result: Int
        for(card in getPlayer(player.opposite()).enchantmentCard.values){
            result = card.effectAllValidEffect(player.opposite(), this, TextEffectTag.FORBID_GET_AURA_OTHER)
            if(result != 0){
                return true
            }
        }
        return false
    }

    suspend fun afterCheckWhenGetAura(player: PlayerEnum){
        for(card in getPlayer(player.opposite()).enchantmentCard.values){
            if(card.effectAllValidEffect(player.opposite(), this, TextEffectTag.FORBID_GET_AURA_OTHER_AFTER) != 0){
                return
            }
        }
    }

    //token move function

    suspend fun investmentTokenMove(player: PlayerEnum): Boolean{
        val nowPlayer = getPlayer(player)
        when(nowPlayer.marketPrice){
            1 -> {
                if(dustToFlow(player, 1) > 0){
                    return true
                }
            }
            2 -> {
                if(auraToFlow(player, 1) > 0){
                    return true
                }
            }
            3 -> {
                if(flareToFlow(player, 1) > 0){
                    return true
                }
            }
            4 -> {
                if(lifeToFlow(player, 1) > 0){
                    return true
                }
            }
            else -> {throw Exception("invalid marketPrice: ${nowPlayer.marketPrice}")}
        }
        return false
    }

    suspend fun recoupTokenMove(player: PlayerEnum): Boolean{
        val nowPlayer = getPlayer(player)
        when(nowPlayer.marketPrice){
            1 -> {
                if(dustToAura(player, 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
            }
            2 -> {
                if(auraToAura(player.opposite(), player, 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
            }
            3 -> {
                if(flareToAura(player.opposite(), player, 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
            }
            4 -> {
                if(lifeToAura(player.opposite(), player, 1, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
            }
            else -> {throw Exception("invalid marketPrice: ${nowPlayer.marketPrice}")}
        }
        return false
    }

    suspend fun dustToFlow(player: PlayerEnum, number: Int): Int{
        val nowPlayer = getPlayer(player)
        var value = number

        if(dust < value){
            value = dust
        }

        val emptyPlace = 5 - nowPlayer.flow
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            dust -= value
            nowPlayer.flow += value

            logger.insert(Log(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.DUST, LocationEnum.FLOW_YOUR, false))
            logger.insert(Log(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.FLOW_YOUR, value, -1)
        }

        return value
    }

    suspend fun auraToFlow(player: PlayerEnum, number: Int): Int{
        val nowPlayer = getPlayer(player)
        var value = number

        val beforeFull = nowPlayer.checkAuraFull()
        if(nowPlayer.aura < value){
            value = nowPlayer.aura
        }

        val emptyPlace = 5 - nowPlayer.flow
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            nowPlayer.aura -= value
            nowPlayer.flow += value

            logger.insert(Log(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.YOUR_AURA, LocationEnum.FLOW_YOUR, false))
            logger.insert(Log(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.FLOW_YOUR, value, -1)

            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }

        return value
    }

    suspend fun flareToFlow(player: PlayerEnum, number: Int): Int{
        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }

        val emptyPlace = 5 - nowPlayer.flow
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            nowPlayer.flare -= value
            nowPlayer.flow += value

            logger.insert(Log(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.YOUR_FLARE, LocationEnum.FLOW_YOUR, false))
            logger.insert(Log(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_FLARE, LocationEnum.FLOW_YOUR, value, -1)
        }

        return value
    }

    suspend fun lifeToFlow(player: PlayerEnum, number: Int): Int{
        val nowPlayer = getPlayer(player)
        var value = number
        val before = nowPlayer.life

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        val emptyPlace = 5 - nowPlayer.flow
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            dust -= value
            nowPlayer.flow += value

            logger.insert(Log(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.YOUR_LIFE, LocationEnum.FLOW_YOUR, false))
            logger.insert(Log(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_LIFE, LocationEnum.FLOW_YOUR, value, -1)
            lifeListenerProcess(player, before, reconstruct = false, damage = false)
            if(nowPlayer.life <= 0){
                gameEnd(player.opposite(), player)
            }
        }

        return value
    }

    suspend fun flowToDust(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flow < value){
            value = nowPlayer.flow
        }

        if(value != 0){
            dust += value
            nowPlayer.flow -= value

            logger.insert(Log(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.FLOW_YOUR, LocationEnum.DUST, false))
            logger.insert(Log(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.FLOW_YOUR, LocationEnum.DUST, value, -1)
        }
    }

    suspend fun dustToAnvil(player: PlayerEnum, number: Int){
        var value = number

        if(dust < value){
            value = dust
        }

        if(value != 0){
            val anvilCard = getPlayer(player).anvil!!
            dust -= value
            anvilCard.addNap(value)

            logger.insert(Log(player, LogText.MOVE_TOKEN, anvilCard.card_number, value,
                LocationEnum.DUST, LocationEnum.ANVIL_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.ANVIL_YOUR, value, anvilCard.card_number)
        }
    }

    suspend fun dustToJourney(player: PlayerEnum, number: Int, reason: Int){
        var value = number

        if(dust < value){
            value = dust
        }

        if(value != 0){
            dust -= value

            logger.insert(Log(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.DUST, LocationEnum.MEMORY_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.MEMORY_YOUR, value, -1)
        }
    }

    suspend fun journeyToDust(player: PlayerEnum, number: Int, reason: Int){
        dust += number

        logger.insert(Log(player, LogText.MOVE_TOKEN, reason, number,
            LocationEnum.MEMORY_YOUR, LocationEnum.DUST, false))
        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.MEMORY_YOUR, LocationEnum.DUST, number, -1)
    }

    suspend fun notReadyToReadySeed(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)
        if(number == 0 || nowPlayer.notReadySeed == 0) return
        var value = number
        nowPlayer.notReadySeed?.let {
            if(it < value){
                value = it
            }
            nowPlayer.notReadySeed = it - value
            nowPlayer.readySeed += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                LocationEnum.NOT_READY_DIRT_ZONE_YOUR, LocationEnum.READY_DIRT_ZONE_YOUR, value, -1)
        }
    }

    suspend fun outToAuraFreeze(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)
        val beforeFull = nowPlayer.checkAuraFull()
        var value = number
        val emptyPlace = nowPlayer.maxAura - nowPlayer.freezeToken - nowPlayer.aura

        if(emptyPlace < value){
            value = emptyPlace
        }

        nowPlayer.freezeToken += value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.FREEZE_TOKEN,
            LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_AURA, value, -1)
        val afterFull = nowPlayer.checkAuraFull()
        auraListenerProcess(player, beforeFull, afterFull)
    }

    //main token move function

    suspend fun auraToOut(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                          reason: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.AURA_YOUR_TO_OUT.encode(number))
            else getBothDirection(user, LocToLoc.AURA_OTHER_TO_OUT.encode(number))){
            return outToAura(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        val nowPlayer = getPlayer(player)
        val beforeFull = nowPlayer.checkAuraFull()
        var value = number

        if(nowPlayer.aura < value){
            value = nowPlayer.aura
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.YOUR_AURA, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.aura -= value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.OUT_OF_GAME, value, -1)
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
    }

    suspend fun dustToOut(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                          reason: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            getBothDirection(user, LocToLoc.DUST_TO_OUT.encode(number))){
            return outToDust(number, arrow, user, card_owner, reason)
        }

        val value = if(number > dust) dust else number

        logger.insert(Log(user, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DUST, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0){
            dust -= value
            sendMoveToken(getSocket(user), getSocket(user.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.OUT_OF_GAME, value, -1)
        }
    }

    suspend fun outToDust(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                            reason: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            !getBothDirection(user, LocToLoc.DUST_TO_OUT.encode(number))){
            return dustToOut(number, arrow, user, card_owner, reason)
        }

        logger.insert(Log(user, LogText.MOVE_TOKEN, reason, number,
            LocationEnum.OUT_OF_GAME, LocationEnum.DUST, arrow != Arrow.NULL))
        if(number != 0){
            dust += number
            sendMoveToken(getSocket(user), getSocket(user.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.OUT_OF_GAME, LocationEnum.DUST, number, -1)
        }
    }

    suspend fun outToAura(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                          reason: Int){
        if (number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.AURA_YOUR_TO_OUT.encode(number))
            else !getBothDirection(user, LocToLoc.AURA_OTHER_TO_OUT.encode(number))){
            return auraToOut(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        val nowPlayer = getPlayer(player)
        nowPlayer.setMaxAura(arrow, user)
        val beforeFull = nowPlayer.checkAuraFull()
        var value = number

        val emptyPlace = nowPlayer.maxAura - nowPlayer.aura - nowPlayer.freezeToken
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            if(checkWhenGetAura(player)){
                outToDust(value, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                afterCheckWhenGetAura(player)
                return
            }
            nowPlayer.aura += value
            logger.insert(Log(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_AURA, arrow != Arrow.NULL))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_AURA, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
        else{
            logger.insert(Log(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_AURA, arrow != Arrow.NULL))
        }
    }

    suspend fun flareToOut(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           card_number: Int){
        if (number <= 0) return

        var value = number

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.FLARE_YOUR_TO_OUT.encode(number))
            else getBothDirection(user, LocToLoc.FLARE_OTHER_TO_OUT.encode(number))){
            return outToFlare(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        val nowPlayer = getPlayer(player)

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, number,
            LocationEnum.YOUR_FLARE, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.flare -= value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_FLARE, number, -1)
        }
    }

    suspend fun outToFlare(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           card_number: Int){
        if (number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.FLARE_YOUR_TO_OUT.encode(number))
            else !getBothDirection(user, LocToLoc.FLARE_OTHER_TO_OUT.encode(number))){
            return flareToOut(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        val nowPlayer = getPlayer(player)

        nowPlayer.flare += number
        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, number,
            LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_FLARE, arrow != Arrow.NULL))
        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_FLARE, number, -1)
    }

    suspend fun auraToAura(playerGive: PlayerEnum, playerGet: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                           card_owner: PlayerEnum, card_number: Int): Int{
        if(number <= 0) return 0

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner)){
            if(playerGet == user){
                if(getBothDirection(user, LocToLoc.AURA_OTHER_TO_AURA_YOUR.encode(number))){
                    auraToAura(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                    return 0
                }
            }
            else{
                if(!getBothDirection(user, LocToLoc.AURA_OTHER_TO_AURA_YOUR.encode(number))){
                    auraToAura(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                    return 0
                }
            }
        }

        var value = number

        val nowPlayer = getPlayer(playerGet)
        nowPlayer.setMaxAura(arrow, user)
        val beforeFull = nowPlayer.checkAuraFull()
        val emptyPlace = nowPlayer.maxAura - nowPlayer.aura - nowPlayer.freezeToken

        if(getPlayerAura(playerGive) < value) {
            value = getPlayerAura(playerGive)
        }
        if(emptyPlace < value) {
            value = emptyPlace
        }

        if(value != 0){
            if(checkWhenGetAura(playerGet)){
                auraToDust(playerGive, value, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                afterCheckWhenGetAura(playerGet)
                return 0
            }

            getPlayer(playerGive).aura -= value
            getPlayer(playerGet).aura += value

            logger.insert(Log(playerGive, LogText.MOVE_TOKEN, card_number, number,
                LocationEnum.YOUR_AURA, LocationEnum.OTHER_AURA, arrow != Arrow.NULL))
            sendMoveToken(getSocket(playerGive), getSocket(playerGet), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.OTHER_AURA, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(playerGet, beforeFull, afterFull)
        }
        else{
            logger.insert(Log(playerGive, LogText.MOVE_TOKEN, card_number, number,
                LocationEnum.YOUR_AURA, LocationEnum.OTHER_AURA, arrow != Arrow.NULL))
        }

        return value
    }

    suspend fun auraToDistance(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                               card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.AURA_YOUR_TO_DISTANCE.encode(number))
            else getBothDirection(user, LocToLoc.AURA_OTHER_TO_DISTANCE.encode(number))){
            return distanceToAura(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        var value = number

        if(getPlayerAura(player) < value) {
            value = getPlayerAura(player)
        }
        if(distanceToken + value > 10) {
            value = 10 - distanceToken
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, number,
            LocationEnum.YOUR_AURA, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            thisTurnDistanceChange = true
            getPlayer(player).aura -= value
            distanceToken += value
            thisTurnDistance += value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.DISTANCE, value, -1)
            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)
        }
    }

    suspend fun auraToFlare(player_aura: PlayerEnum, player_flare: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                            card_owner: PlayerEnum, card_number: Int){
        if(number <= 0) return


        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner)){
            if(player_aura == user){
                if(player_flare == user){
                    if(getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_YOUR.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return
                    }
                }
                else{
                    if(getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_OTHER.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return
                    }
                }
            }
            else{
                if(player_flare == user){
                    if(getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_YOUR.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return
                    }
                }
                else{
                    if(getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_OTHER.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return
                    }
                }
            }
        }

        val auraPlayer = getPlayer(player_aura)
        val flarePlayer = getPlayer(player_flare)
        var value = number

        if(value > auraPlayer.aura){
            value = auraPlayer.aura
        }

        if(value != 0){
            auraPlayer.aura -= value
            flarePlayer.flare += value

            if(player_aura == player_flare){
                logger.insert(Log(player_aura, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_aura), getSocket(player_aura.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, value, -1)
            }
            else{
                logger.insert(Log(player_aura, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_AURA, LocationEnum.OTHER_FLARE, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_aura), getSocket(player_aura.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_AURA, LocationEnum.OTHER_FLARE, value, -1)
            }
        }
        else{
            if(player_aura == player_flare){
                logger.insert(Log(player_aura, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, arrow != Arrow.NULL))
            }
            else{
                logger.insert(Log(player_aura, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_AURA, LocationEnum.OTHER_FLARE, arrow != Arrow.NULL))
            }
        }
    }

    suspend fun cardToAura(player: PlayerEnum, number: Int, card: Card, card_number: Int){
        if(!(card.checkCanMoveToken(player, this)) || number <= 0 || card.isItDestruction()) return
        var value = number

        val nowPlayer = getPlayer(player)
        val beforeFull = nowPlayer.checkAuraFull()
        val emptyPlace = nowPlayer.maxAura - nowPlayer.aura - nowPlayer.freezeToken

        if(emptyPlace < value){
            value = emptyPlace
        }

        val (sakura, seed) = card.reduceNap(player, this,  value)

        if(seed != 0){
            nowPlayer.notReadySeed = nowPlayer.notReadySeed!! + seed
            logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, seed,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, seed, card.card_number)
        }

        if(sakura != 0){
            if(checkWhenGetAura(player)){
                dust += sakura
                logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, sakura,
                    LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DUST, false))
                sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DUST, sakura, card.card_number)
                afterCheckWhenGetAura(player)
                return
            }

            nowPlayer.aura += sakura
            logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, sakura,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_AURA, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_AURA, sakura, card.card_number)
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
        else{
            logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, sakura,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_AURA, false))
        }

        if(card.getNap() == 0){
            card.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
        }
    }

    suspend fun cardToFlare(player: PlayerEnum, number: Int?, card: Card,
                            card_number: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(!(card.checkCanMoveToken(player, this)) || number == null || number <= 0  || card.isItDestruction()) return
        val nowPlayer = getPlayer(player)

        val (sakura, seed) = card.reduceNap(player, this, number)

        if(seed != 0){
            nowPlayer.notReadySeed = nowPlayer.notReadySeed!! + seed
            if(location == LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
                logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, seed,
                    LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, false))
            }
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                location, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, seed, card.card_number)
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, sakura,
            location, LocationEnum.YOUR_FLARE, false))
        if(sakura != 0){
            nowPlayer.flare += sakura
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                location, LocationEnum.YOUR_FLARE, sakura, card.card_number)
        }

        if(card.getNap() == 0){
            card.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
        }
    }

    suspend fun cardToDistance(player: PlayerEnum, number: Int, card: Card, card_number: Int){
        if(!(card.checkCanMoveToken(player, this)) || number <= 0 || card.isItDestruction()) return
        var value = number

        val nowPlayer = getPlayer(player)
        if(distanceToken + value > 10) value = 10 - distanceToken

        val (sakura, seed) = card.reduceNap(player, this, value)

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, sakura,
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DISTANCE, false))
        if(sakura != 0){
            thisTurnDistanceChange = true
            distanceToken += sakura
            thisTurnDistance += sakura

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DISTANCE, sakura, card.card_number)

            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)
        }

        if(seed != 0){
            nowPlayer.notReadySeed = nowPlayer.notReadySeed!! + seed
            logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, seed,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, seed, card.card_number)
        }

        if(card.getNap() == 0){
            card.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
        }
    }

    /**
    It is assumed that both the destination and destination are the same player's enhancement card.
    it will be Needed to receive who are both players if some effect that need both player added
     */
    suspend fun cardToCard(player: PlayerEnum, number: Int, fromCard: Card, toCard: Card, card_number: Int){
        if(!(fromCard.checkCanMoveToken(player, this)) || number <= 0 || fromCard.isItDestruction()) return
        var value = number

        if(value > (fromCard.getNap()?: 0)) value = fromCard.getNap()?: 0

        val (sakura, seed) = fromCard.reduceNap(player, this, value)

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, sakura,
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, false))
        if(sakura != 0){
            toCard.addNap(sakura)
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, sakura, fromCard.card_number, toCard.card_number)
        }

        if(seed != 0){
            toCard.addNap(seed, true)
            logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, seed,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, seed, fromCard.card_number, toCard.card_number)
        }

        if(fromCard.getNap() == 0){
            fromCard.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
        }
    }

    suspend fun distanceToFlare(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                                card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_OTHER.encode(number))){
            return flareToDistance(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        var value = number

        if(distanceToken < value){
            value = distanceToken
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DISTANCE, LocationEnum.YOUR_FLARE, Arrow.NULL != arrow))
        if(value != 0){
            thisTurnDistanceChange = true
            distanceToken -= value
            thisTurnDistance -= value
            getPlayer(player).flare += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.YOUR_FLARE, value, -1)

            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)
        }
    }

    suspend fun distanceToDust(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) && getBothDirection(user, LocToLoc.DISTANCE_TO_DUST.encode(number))){
            return dustToDistance(number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        if(arrow != Arrow.NULL && moveTokenCheckArrow(LocationEnum.DISTANCE, LocationEnum.DUST)) return

        var value = number

        if(distanceToken < value){
            value = distanceToken
        }

        logger.insert(Log(user, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DISTANCE, LocationEnum.DUST, Arrow.NULL != arrow))
        if(value != 0){
            thisTurnDistanceChange = true
            distanceToken -= value
            thisTurnDistance -= value

            dust += value

            sendMoveToken(player1_socket, player2_socket, TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.DUST, value, -1)

            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)

            for(card in getPlayer(user.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(user.opposite(), this, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN)
            }

            getPlayer(user).isMoveDistanceToken = true
        }

    }

    suspend fun distanceToAura(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                               card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.AURA_YOUR_TO_DISTANCE.encode(number))
            else !getBothDirection(user, LocToLoc.AURA_OTHER_TO_DISTANCE.encode(number))){
            return distanceToAura(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        if(arrow != Arrow.NULL && moveTokenCheckArrow(LocationEnum.DISTANCE, LocationEnum.YOUR_AURA)) return

        var value = number
        val nowPlayer = getPlayer(player)
        nowPlayer.setMaxAura(arrow, user)
        val beforeFull = nowPlayer.checkAuraFull()
        val emptyPlace = nowPlayer.maxAura - nowPlayer.aura - nowPlayer.freezeToken

        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value > distanceToken){
            value = distanceToken
        }

        if(value != 0){
            if(checkWhenGetAura(player)){
                distanceToDust(value, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                afterCheckWhenGetAura(player)
                return
            }

            thisTurnDistanceChange = true
            distanceToken -= value
            thisTurnDistance -= value

            nowPlayer.aura += value
            logger.insert(Log(user, LogText.MOVE_TOKEN, card_number, value,
                LocationEnum.DISTANCE, LocationEnum.YOUR_AURA, Arrow.NULL != arrow))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.YOUR_AURA, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)

            for(card in getPlayer(user.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(user.opposite(), this, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN)
            }
            getPlayer(user).isMoveDistanceToken = true
        }
        else{
            logger.insert(Log(user, LogText.MOVE_TOKEN, card_number, value,
                LocationEnum.DISTANCE, LocationEnum.YOUR_AURA, Arrow.NULL != arrow))
        }
    }

    //must check card is destruction
    suspend fun cardToDust(player: PlayerEnum, number: Int?, card: Card, startPhaseProcess: Boolean,
                           card_number: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(!(card.checkCanMoveToken(player, this)) || number == null || number <= 0 || card.isItDestruction()) return
        val nowPlayer = getPlayer(player)

        var value = number

        if(value > (card.getNap()?: 0)){
            value = card.getNap()?: 0
        }

        if(value != 0){
            if(cardToDustCheck(player, value, card, startPhaseProcess, card_number)){
                val (sakura, seed) = card.reduceNap(player, this, number)

                if(sakura != 0){
                    dust += sakura
                    logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, sakura,
                        location, LocationEnum.DUST, false))
                    sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                        location, LocationEnum.DUST, number, card.card_number)
                }

                if(seed != 0){
                    nowPlayer.notReadySeed = nowPlayer.notReadySeed!! + seed

                    if(location == LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
                        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, seed,
                            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, false))
                    }
                    sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                        location, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, seed, card.card_number)
                }

                if(card.getNap() == 0){
                    card.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
                }
            }
        }
    }

    suspend fun readySeedToCard(player: PlayerEnum, number: Int, card: Card, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(number <= 0) return
        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.readySeed < value){
            value = nowPlayer.readySeed
        }

        nowPlayer.readySeed -= value
        card.addNap(value, true)

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
            LocationEnum.READY_DIRT_ZONE_YOUR, location, value, card.card_number)
    }

    //this two function is must check number before use
    suspend fun auraToCard(player: PlayerEnum, number: Int, card: Card,
                           card_number: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(number <= 0) return
        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.aura < value){
            value = nowPlayer.aura
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_AURA, location, false))
        if(value != 0){
            nowPlayer.aura -= value
            card.addNap(value)

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, location, value, card.card_number)
        }
    }

    suspend fun lifeToCard(player: PlayerEnum, number: Int, card: Card, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD,
                           reconstruct: Boolean, damage: Boolean, card_number: Int){
        var value = number
        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_LIFE, location, false))
        if(value != 0){
            nowPlayer.life -= value
            card.addNap(value)

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_LIFE, location, value, card.card_number)
            lifeListenerProcess(player, before, reconstruct, damage)
            if(nowPlayer.life <= 0){
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun outToCard(player: PlayerEnum, number: Int, card: Card,
                          card_number: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(number <= 0) return

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, number,
            LocationEnum.OUT_OF_GAME, location, false))

        card.addNap(number)
        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.OUT_OF_GAME, location, number, card.card_number)

    }

    suspend fun dustToCard(player: PlayerEnum, number: Int, card: Card,
                           card_number: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(number <= 0) return

        var value = number

        if(dust < value){
            value = dust
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DUST, location, false))

        if(value != 0){
            dust -= value
            card.addNap(value)

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, location, value, card.card_number)
        }

    }

    suspend fun dustToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DUST_TO_LIFE_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DUST_TO_LIFE_OTHER.encode(number))){
            return lifeToDust(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.life + value > 10){
            value = 10 - nowPlayer.life
        }

        if(value > dust){
            value = dust
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DUST, LocationEnum.YOUR_LIFE, arrow != Arrow.NULL))
        if(value != 0){
            dust -= value
            nowPlayer.life += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.YOUR_LIFE, value, -1)
        }
    }

    suspend fun dustToDistance(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) && !getBothDirection(user, LocToLoc.DISTANCE_TO_DUST.encode(number))){
            return distanceToDust(number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        var value = number

        if(distanceToken + value > 10){
            value = 10 - distanceToken
        }

        if(value > dust){
            value = dust
        }

        logger.insert(Log(user, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DUST, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            thisTurnDistanceChange = true
            distanceToken += value
            thisTurnDistance += value

            dust -= value

            sendMoveToken(player1_socket, player2_socket, TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.DISTANCE, value, -1)

            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)
        }


    }

    suspend fun dustToAura(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           card_number: Int): Int{
        if (number <= 0) return 0

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DUST_TO_AURA_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DUST_TO_AURA_OTHER.encode(number))){
            auraToDust(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
            return 0
        }

        val nowPlayer = getPlayer(player)
        nowPlayer.setMaxAura(arrow, user)
        var value = number
        val beforeFull = nowPlayer.checkAuraFull()
        val emptyPlace = nowPlayer.maxAura - nowPlayer.freezeToken - nowPlayer.aura

        if(number > dust){
            value = dust
        }

        if(emptyPlace < value){
            value = emptyPlace
        }


        if(value != 0){
            if(checkWhenGetAura(player)){
                afterCheckWhenGetAura(player)
                return 0
            }

            nowPlayer.aura += value
            dust -= value

            logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
                LocationEnum.DUST, LocationEnum.YOUR_AURA, arrow != Arrow.NULL))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.YOUR_AURA, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
        else{
            logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
                LocationEnum.DUST, LocationEnum.YOUR_AURA, arrow != Arrow.NULL))
        }

        return value
    }

    suspend fun dustToFlare(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                            card_number: Int){
        if (number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DUST_TO_FLARE_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DUST_TO_FLARE_OTHER.encode(number))){
            return flareToDust(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        val nowPlayer = getPlayer(player)
        var value = number
        if(value > dust){
            value = dust
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DUST, LocationEnum.YOUR_FLARE, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.flare += value
            dust -= value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.YOUR_FLARE, value, -1)
        }
    }

    suspend fun auraToDust(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DUST_TO_AURA_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DUST_TO_AURA_OTHER.encode(number))){
            dustToAura(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
            return
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.aura < value){
            value = nowPlayer.aura
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_AURA, LocationEnum.DUST, arrow != Arrow.NULL))

        if(value != 0){
            nowPlayer.aura -= value
            dust += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.DUST, value, -1)
        }
    }

    suspend fun flareToDistance(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                                card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_OTHER.encode(number))){
            return distanceToFlare(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }
        if(10 - distanceToken < value){
            value = 10 - distanceToken
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_FLARE, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            thisTurnDistanceChange = true
            nowPlayer.flare -= value

            distanceToken += value
            thisTurnDistance += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_FLARE, LocationEnum.DISTANCE, value, -1)

            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)
        }

    }

    suspend fun flareToDust(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                            card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DUST_TO_FLARE_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DUST_TO_FLARE_OTHER.encode(number))){
            return dustToFlare(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_FLARE, LocationEnum.DUST, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.flare -= value

            dust += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_FLARE, LocationEnum.DUST, value, -1)
        }
    }

    suspend fun flareToAura(player_flare: PlayerEnum, player_aura: PlayerEnum, number: Int,
                            arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, card_number: Int): Int{
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner)){
            if(player_aura == user){
                if(player_flare == user){
                    if(!getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_YOUR.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return 0
                    }
                }
                else{
                    if(!getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_OTHER.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return 0
                    }
                }
            }
            else{
                if(player_flare == user){
                    if(!getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_YOUR.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return 0
                    }
                }
                else{
                    if(!getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_OTHER.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                        return 0
                    }
                }
            }
        }

        val flarePlayer = getPlayer(player_flare)
        val auraPlayer = getPlayer(player_aura)
        auraPlayer.setMaxAura(arrow, user)
        val beforeFull = auraPlayer.checkAuraFull()
        val emptyPlace = auraPlayer.maxAura - auraPlayer.freezeToken - auraPlayer.aura

        var value = number

        if(flarePlayer.flare < value){
            value = flarePlayer.flare
        }

        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            if(checkWhenGetAura(player_aura)){
                flareToDust(player_flare, value, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                afterCheckWhenGetAura(player_aura)
                return 0
            }
            flarePlayer.flare -= value
            auraPlayer.aura += value

            if(player_flare == player_aura){
                logger.insert(Log(player_flare, LogText.MOVE_TOKEN, card_number, value,
                    LocationEnum.YOUR_FLARE, LocationEnum.YOUR_AURA, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_flare), getSocket(player_flare.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_FLARE, LocationEnum.YOUR_AURA, value, -1)
            }
            else{
                logger.insert(Log(player_flare, LogText.MOVE_TOKEN, card_number, value,
                    LocationEnum.YOUR_FLARE, LocationEnum.OTHER_AURA, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_flare), getSocket(player_flare.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_FLARE, LocationEnum.OTHER_AURA, value, -1)
            }
            auraPlayer.maxAura = 5
            val afterFull = auraPlayer.checkAuraFull()
            auraListenerProcess(player_aura, beforeFull, afterFull)
        }
        else{
            if(player_flare == player_aura){
                logger.insert(Log(player_flare, LogText.MOVE_TOKEN, card_number, value,
                    LocationEnum.YOUR_FLARE, LocationEnum.YOUR_AURA, arrow != Arrow.NULL))
            }
            else{
                logger.insert(Log(player_flare, LogText.MOVE_TOKEN, card_number, value,
                    LocationEnum.YOUR_FLARE, LocationEnum.OTHER_AURA, arrow != Arrow.NULL))
            }
        }

        return value
    }

    suspend fun lifeToDust(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           card_number: Int, endIgnore: Boolean = false){
        val nowPlayer = getPlayer(player)

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DUST_TO_LIFE_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DUST_TO_LIFE_OTHER.encode(number))){
            return dustToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        val before = nowPlayer.life

        var value = number

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_FLARE, LocationEnum.DUST, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life -= value
            dust += value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_LIFE, LocationEnum.DUST, value, -1)
            lifeListenerProcess(player, before, reconstruct = false, damage = false)
            if(nowPlayer.life == 0 && !endIgnore){
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun selfFlareToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                                card_number: Int){
        if(number == 0) {
            return
        }

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) && !getBothDirection(user, LocToLoc.YOUR_LIFE_TO_YOUR_FLARE.encode(number))){
            return lifeToSelfFlare(player, number, reconstruct = false, damage = false, arrow = Arrow.BOTH_DIRECTION, user = user,
                card_owner = card_owner, card_number = card_number
            )
        }

        var value = number
        val nowPlayer = getPlayer(player)

        if(nowPlayer.flare > value){
            value = nowPlayer.flare
        }

        if(10 - nowPlayer.life < value){
            value = 10 - nowPlayer.life
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_FLARE, LocationEnum.YOUR_LIFE, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life += value
            nowPlayer.flare -= value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_FLARE, LocationEnum.YOUR_LIFE, value, -1)
        }
    }

    suspend fun lifeToSelfFlare(player: PlayerEnum, number: Int, reconstruct: Boolean, damage: Boolean,
                                arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, card_number: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) && getBothDirection(user, LocToLoc.YOUR_LIFE_TO_YOUR_FLARE.encode(number))){
            return selfFlareToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        var value = number
        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_LIFE, LocationEnum.YOUR_FLARE, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life -= value
            nowPlayer.flare += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_LIFE, LocationEnum.YOUR_FLARE, value, -1)
            lifeListenerProcess(player, before, reconstruct, damage)
            if(nowPlayer.life == 0){
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun lifeToAura(player_life: PlayerEnum, player_aura: PlayerEnum, number: Int, card_number: Int): Int{

        val lifePlayer = getPlayer(player_life)
        val auraPlayer = getPlayer(player_aura)
        var value = number
        val beforeFull = auraPlayer.checkAuraFull()
        val beforeLife = lifePlayer.life

        if(value > lifePlayer.life){
            value = lifePlayer.life
        }

        val emptyPlace = auraPlayer.maxAura - auraPlayer.aura
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            lifePlayer.life -= value
            auraPlayer.aura += value

            if(player_life == player_aura){
                logger.insert(Log(player_life, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_LIFE, LocationEnum.YOUR_AURA, false))
                sendMoveToken(getSocket(player_life), getSocket(player_life.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_LIFE, LocationEnum.YOUR_AURA, value, -1)
            }
            else{
                logger.insert(Log(player_life, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_LIFE, LocationEnum.OTHER_AURA, false))
                sendMoveToken(getSocket(player_life), getSocket(player_life.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_LIFE, LocationEnum.OTHER_AURA, value, -1)
            }

            val afterFull = auraPlayer.checkAuraFull()
            auraListenerProcess(player_aura, beforeFull, afterFull)

            lifeListenerProcess(player_life, beforeLife, reconstruct = false, damage = false)
            if (lifePlayer.life == 0) {
                gameEnd(player_life.opposite(), player_life)
            }
        }
        else{
            if(player_life == player_aura){
                logger.insert(Log(player_life, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_LIFE, LocationEnum.YOUR_AURA, false))
            }
            else{
                logger.insert(Log(player_life, LogText.MOVE_TOKEN, card_number, number,
                    LocationEnum.YOUR_LIFE, LocationEnum.OTHER_AURA, false))
            }
        }

        return value
    }

    suspend fun distanceToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                               card_number: Int){
        if(number == 0){
            return
        }

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.LIFE_YOUR_TO_DISTANCE.encode(number))
            else !getBothDirection(user, LocToLoc.LIFE_OTHER_TO_DISTANCE.encode(number))){
            return lifeToDistance(player, number, false, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        var value = number
        val nowPlayer = getPlayer(player)

        if(distanceToken > value){
            value = distanceToken
        }

        if(nowPlayer.life + value > 10){
            value = 10 - nowPlayer.life
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DISTANCE, LocationEnum.YOUR_LIFE, arrow != Arrow.NULL))
        if(value != 0){
            thisTurnDistanceChange = true
            nowPlayer.life += value

            distanceToken -= value
            thisTurnDistance -= value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.YOUR_LIFE, value, -1)

            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)

            for(card in getPlayer(user.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(user.opposite(), this, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN)
            }
            getPlayer(user).isMoveDistanceToken = true
        }
    }

    suspend fun outToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                            card_owner: PlayerEnum, card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner)){
            if(player == user){
                if(!getBothDirection(user, LocToLoc.LIFE_YOUR_TO_OUT.encode(number))){
                    return lifeToOut(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                }
            }
            else{
                if(!getBothDirection(user, LocToLoc.LIFE_OTHER_TO_OUT.encode(number))){
                    return lifeToOut(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                }
            }
        }

        var value = number

        val nowPlayer = getPlayer(player)

        if(10 - nowPlayer.life < value){
            value = 10 - nowPlayer.life
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_LIFE, arrow != Arrow.NULL))
        if(value != 0) {
            nowPlayer.life += value
            sendMoveToken(
                getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.OUT_OF_GAME, LocationEnum.YOUR_LIFE, value, -1
            )
        }
    }

    suspend fun lifeToOut(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                          card_owner: PlayerEnum, card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner)){
            if(player == user){
                if(getBothDirection(user, LocToLoc.LIFE_YOUR_TO_OUT.encode(number))){
                    return outToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                }
            }
            else{
                if(getBothDirection(user, LocToLoc.LIFE_OTHER_TO_OUT.encode(number))){
                    return outToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                }
            }
        }

        var value = number

        val nowPlayer = getPlayer(player)
        val before = nowPlayer.life

        if(before < value){
            value = before
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_LIFE, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0) {
            nowPlayer.life -= value

            sendMoveToken(
                getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_LIFE, LocationEnum.OUT_OF_GAME, value, -1
            )
            lifeListenerProcess(player, before, reconstruct = false, damage = false)
            if (nowPlayer.life == 0) {
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun lifeToLife(playerGive: PlayerEnum, playerGet: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                           card_owner: PlayerEnum, card_number: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner)){
            if(playerGive == user){
                if(getBothDirection(user, LocToLoc.LIFE_YOUR_TO_LIFE_OTHER.encode(number))){
                    return lifeToLife(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                }
            }
            else{
                if(!getBothDirection(user, LocToLoc.LIFE_YOUR_TO_LIFE_OTHER.encode(number))){
                    return lifeToLife(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
                }
            }
        }

        var value = number

        val getPlayer = getPlayer(playerGet)
        val givePlayer = getPlayer(playerGive)

        val before = givePlayer.life

        if(givePlayer.life < value){
            value = givePlayer.life
        }

        else if(10 - getPlayer.life < value){
            value = 10 - getPlayer.life
        }

        logger.insert(Log(playerGive, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_LIFE, LocationEnum.OTHER_LIFE, arrow != Arrow.NULL))
        if(value != 0) {
            givePlayer.life -= value
            getPlayer.life += value

            sendMoveToken(
                getSocket(playerGet), getSocket(playerGive), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_LIFE, LocationEnum.OTHER_LIFE, value, -1
            )
            lifeListenerProcess(playerGet, before, reconstruct = false, damage = false)
            if (givePlayer.life == 0) {
                gameEnd(playerGive.opposite(), playerGive)
            }
        }
    }

    suspend fun lifeToDistance(player: PlayerEnum, number: Int, damage: Boolean, arrow: Arrow, user: PlayerEnum,
                               card_owner: PlayerEnum, card_number: Int) {
        if(number == 0) {
            return
        }

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.LIFE_YOUR_TO_DISTANCE.encode(number))
            else getBothDirection(user, LocToLoc.LIFE_OTHER_TO_DISTANCE.encode(number))){
            return distanceToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, card_number)
        }

        var value = number
        if(distanceToken + value > 10) value = 10 - distanceToken

        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(value > nowPlayer.life){
            value = nowPlayer.life
        }

        if(10 - distanceToken < value){
            value = 10 - distanceToken
        }

        logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.YOUR_LIFE, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            thisTurnDistanceChange = true
            nowPlayer.life -= value

            distanceToken += value
            thisTurnDistance += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_LIFE, LocationEnum.DISTANCE, value, -1)
            lifeListenerProcess(player, before, false, damage)
            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)
            if(nowPlayer.life == 0){
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun addAllCardTextBuff(player: PlayerEnum){
        val mine = getPlayer(player)
        val other = getPlayer(player.opposite())
        mine.megamiCard?.effectAllValidEffect(player, this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT)
        mine.megamiCard2?.effectAllValidEffect(player, this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT)
        for(card in mine.enchantmentCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT)
        }
        for(card in mine.usedSpecialCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT)
        }
        for(card in other.enchantmentCard.values){
            card.effectAllValidEffect(player.opposite(), this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER)
        }
        for(card in other.usedSpecialCard.values){
            card.effectAllValidEffect(player.opposite(), this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER)
        }
    }

    suspend fun addAllCardCostBuff(){
        for(card in player1.enchantmentCard){
            card.value.addCostBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player1.usedSpecialCard){
            card.value.addCostBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player2.enchantmentCard){
            card.value.addCostBuff(PlayerEnum.PLAYER2, this)
        }
        for(card in player2.usedSpecialCard){
            card.value.addCostBuff(PlayerEnum.PLAYER2, this)
        }
    }

    suspend fun applyAllCostBuff(player: PlayerEnum, cost: Int, card: Card): Int{
        val now_player = getPlayer(player)
        var now_cost = cost

        for(queue in now_player.costBuff){
            val tempq: ArrayDeque<CostBuff> = ArrayDeque()
            for(buff in queue){
                if(buff.condition(player, this, card)){
                    buff.counter *= -1
                    tempq.add(buff)
                }
            }
            for(buff in tempq){
                now_cost = buff.effect(now_cost)
            }
        }

        return now_cost
    }

    private suspend fun applyAllAttackBuff(player: PlayerEnum, react_attack: MadeAttack?){
        val nowPlayer = getPlayer(player)
        val nowAttack = nowPlayer.pre_attack_card!!

        nowAttack.effectText(player, this, react_attack, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK)

        nowAttack.addTempAttackBuff(getPlayerTempAttackBuff(player))
        nowAttack.addTempOtherBuff(getPlayerTempOtherBuff(player))
    }

    private suspend fun attackRangeCheck(player: PlayerEnum): Boolean{
        addAllCardTextBuff(player)

        val nowPlayer = getPlayer(player)
        val nowTempBuffQueue = getPlayerTempRangeBuff(player)
        val nowBuffQueue = getPlayerRangeBuff(player)
        val nowAttack = nowPlayer.pre_attack_card!!

        nowAttack.addTempRangeBuff(nowTempBuffQueue)
        return nowAttack.rangeCheck(getAdjustDistance(), this, player, nowBuffQueue)
    }

    fun cleanAfterUseCost(){
        cleanCostBuff(player1.costBuff)
        cleanCostBuff(player2.costBuff)
    }

    fun cleanCostBuff(){
        cleanCostTempBuff(player1.costBuff)
        cleanCostTempBuff(player2.costBuff)
    }

    private fun cleanAllBuffWhenUnused(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.pre_attack_card!!.returnWhenBuffDoNotUse(nowPlayer.rangeBuff)
        player1TempRangeBuff.clearBuff()
        player1TempAttackBuff.clearBuff()
        player1TempOtherBuff.clearBuff()
        player2TempRangeBuff.clearBuff()
        player2TempAttackBuff.clearBuff()
        player2TempOtherBuff.clearBuff()
    }

    suspend fun addPreAttackZone(player: PlayerEnum, attack: MadeAttack, react_attack: MadeAttack?): Boolean{
        val nowPlayer = getPlayer(player)

        nowPlayer.addPreAttackZone(attack)

        return if(attackRangeCheck(player)){
            getPlayerTempRangeBuff(player.opposite()).clearBuff()
            getPlayerTempAttackBuff(player.opposite()).clearBuff()
            getPlayerTempOtherBuff(player.opposite()).clearBuff()
            applyAllAttackBuff(player, react_attack)
            true
        } else{
            cleanAllBuffWhenUnused(player)
            nowPlayer.pre_attack_card = null
            false
        }
    }

    fun addThisTurnCostBuff(player: PlayerEnum, effect: CostBuff){
        when (player){
            PlayerEnum.PLAYER1 -> player1.addCostBuff(effect)
            PlayerEnum.PLAYER2 -> player2.addCostBuff(effect)
        }
    }

    fun addThisTurnAttackBuff(player: PlayerEnum, effect: Buff){
        val nowPlayer = getPlayer(player)

        when(effect.tag){
            BufTag.CARD_CHANGE, BufTag.INSERT, BufTag.CHANGE_EACH,
            BufTag.MULTIPLE, BufTag.DIVIDE, BufTag.PLUS_MINUS -> {
                nowPlayer.attackBuff.addAttackBuff(effect)
            }
            BufTag.CARD_CHANGE_IMMEDIATE, BufTag.INSERT_IMMEDIATE, BufTag.CHANGE_EACH_IMMEDIATE,
            BufTag.MULTIPLE_IMMEDIATE, BufTag.DIVIDE_IMMEDIATE, BufTag.PLUS_MINUS_IMMEDIATE -> {
                getPlayerTempAttackBuff(player).addAttackBuff(effect)
            }
        }
    }

    fun removeThisTurnAttackBuff(player: PlayerEnum, effectTag: BufTag, card_number: Int){
        val nowPlayer = getPlayer(player)

        when(effectTag){
            BufTag.CARD_CHANGE, BufTag.INSERT, BufTag.CHANGE_EACH,
            BufTag.MULTIPLE, BufTag.DIVIDE, BufTag.PLUS_MINUS -> {
                nowPlayer.attackBuff.removeAttackBuff(effectTag, card_number)
            }
            BufTag.CARD_CHANGE_IMMEDIATE, BufTag.INSERT_IMMEDIATE, BufTag.CHANGE_EACH_IMMEDIATE,
            BufTag.MULTIPLE_IMMEDIATE, BufTag.DIVIDE_IMMEDIATE, BufTag.PLUS_MINUS_IMMEDIATE -> {
                getPlayerTempAttackBuff(player).removeAttackBuff(effectTag, card_number)
            }
        }
    }

    fun addThisTurnRangeBuff(player: PlayerEnum, effect: RangeBuff){
        val nowPlayer = getPlayer(player)
        val nowTempRangeBuff = getPlayerTempRangeBuff(player)

        when(effect.tag){
            RangeBufTag.CARD_CHANGE, RangeBufTag.CHANGE, RangeBufTag.ADD,
            RangeBufTag.DELETE, RangeBufTag.PLUS, RangeBufTag.MINUS-> {
                nowPlayer.rangeBuff.addRangeBuff(effect)
            }
            RangeBufTag.CARD_CHANGE_IMMEDIATE, RangeBufTag.CHANGE_IMMEDIATE, RangeBufTag.ADD_IMMEDIATE,
            RangeBufTag.DELETE_IMMEDIATE, RangeBufTag.PLUS_IMMEDIATE, RangeBufTag.MINUS_IMMEDIATE -> {
                nowTempRangeBuff.addRangeBuff(effect)
            }
        }
    }

    fun addThisTurnOtherBuff(player: PlayerEnum, effect: OtherBuff){
        when(effect.tag){
            OtherBuffTag.GET, OtherBuffTag.LOSE -> {
                getPlayerOtherBuff(player).addOtherBuff(effect)
            }

            OtherBuffTag.GET_IMMEDIATE, OtherBuffTag.LOSE_IMMEDIATE -> {
                getPlayerTempOtherBuff(player).addOtherBuff(effect)
            }
        }
    }

    suspend fun addConcentration(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        when(now_player.addConcentration()){
            0 -> {
                sendAddConcentration(now_socket, other_socket)
            }
            1 -> {
                sendRemoveShrink(now_socket, other_socket)
            }
        }
    }

    suspend fun decreaseConcentration(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        if(now_player.decreaseConcentration()) sendDecreaseConcentration(now_socket, other_socket)
    }

    suspend fun reactCheck(player: PlayerEnum, card: Card, react_attack: MadeAttack): Boolean{
        if(card.canUseAtReact(player, this)){
            if(card.canReactable(react_attack, this, player, getPlayerOtherBuff(player))){
                return true
            }
        }
        return false
    }

    suspend fun setGauge(player: PlayerEnum, thunder: Boolean, number: Int){
        val nowPlayer = getPlayer(player)
        if(thunder){
            nowPlayer.thunderGauge?.let {
                nowPlayer.thunderGauge = number
                sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_THUNDER_GAUGE_YOUR, number)
            }
        }
        else{
            nowPlayer.windGauge?.let {
                nowPlayer.windGauge = number
                sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_WIND_GAUGE_YOUR, number)
            }
        }
    }

    suspend fun thunderGaugeIncrease(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.thunderGauge?.let {
            nowPlayer.thunderGauge = it + 1
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.INCREASE_THUNDER_GAUGE_YOUR, -1)
        }
    }

    suspend fun windGaugeIncrease(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.windGauge?.let {
            nowPlayer.windGauge = it + 1
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.INCREASE_WIND_GAUGE_YOUR, -1)
        }
    }

    suspend fun tabooGaugeIncrease(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)

        nowPlayer.tabooGauge = nowPlayer.tabooGauge?.let {
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_TABOO_GAUGE_YOUR, it + number)
            if(it + number >= 16){
                gameEnd(null, player)
            }
            it + number
        }

        for(card in nowPlayer.usedSpecialCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_TABOO_CHANGE)
        }
    }

    //call this function when use some card that have effect to change wind, thunder gauge, cannot select not increase
    suspend fun gaugeIncreaseRequest(player: PlayerEnum, card: Int){
        val nowPlayer = getPlayer(player)
        if(nowPlayer.thunderGauge != null){
            while(true){
                when(receiveCardEffectSelect(player, card)){
                    CommandEnum.SELECT_ONE -> {
                        thunderGaugeIncrease(player)
                    }
                    CommandEnum.SELECT_TWO -> {
                        windGaugeIncrease(player)
                    }
                    else -> {
                        continue
                    }
                }
                break
            }
        }
    }

    //call this function when after use any card
    suspend fun gaugeIncreaseRequest(player: PlayerEnum, card: Card){
        val nowPlayer = getPlayer(player)
        if(nowPlayer.thunderGauge != null && card.card_data.megami != MegamiEnum.RAIRA && card.card_data.megami != MegamiEnum.NONE){
            while(true){
                when(receiveCardEffectSelect(player, 1200)){
                    CommandEnum.SELECT_ONE -> {
                        thunderGaugeIncrease(player)
                    }
                    CommandEnum.SELECT_TWO -> {
                        windGaugeIncrease(player)
                    }
                    CommandEnum.SELECT_NOT -> {
                    }
                    else -> {
                        continue
                    }
                }
                break
            }
        }
    }

    /**
     true means this card is termination card
     false means this card is not termination card
     */
    suspend fun terminationListenerProcess(player: PlayerEnum, card: Card): Boolean{
        if(card.card_data.sub_type == SubType.REACTION && logger.checkThisCardUseInSoldier(player, card.card_number)){
            for(usedCard in getPlayer(player).usedSpecialCard.values){
                if(usedCard.effectAllValidEffect(player, this, TextEffectTag.REMOVE_REACTIONS_TERMINATION) == 1){
                    return false
                }
            }
        }
        card.card_data.effect?.let {
            for(text in it){
                if(text === CardSet.termination){
                    val terminationListener = getTerminationListener(player)
                    if(!terminationListener.isEmpty()){
                        for(i in 1..terminationListener.size){
                            if(terminationListener.isEmpty()) break
                            val now = terminationListener.first()
                            terminationListener.removeFirst()
                            if(!(now.doAction(this, -1, -1,
                                    booleanPara1 = false, booleanPara2 = false))){
                                terminationListener.addLast(now)
                            }
                        }
                    }
                    return true
                }
            }
        }
        return false
    }

    suspend fun afterDivingSuccess(player: PlayerEnum, card: Card, location: LocationEnum){
        val nowPlayer = getPlayer(player)

        if(card.card_data.card_class == CardClass.SPECIAL){
            card.special_card_state = SpecialCardEnum.PLAYED
            popCardFrom(player, card.card_number, location, true)?.let {
                insertCardTo(player, it, LocationEnum.YOUR_USED_CARD, true)
            }
        }
        else{
            popCardFrom(player, card.card_number, location, true)?.let {
                insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
            }
        }

        val nowAttack = nowPlayer.pre_attack_card
        nowAttack?.activeOtherBuff(this, player, nowPlayer.otherBuff)
        nowAttack?.getDamage(this, player, nowPlayer.attackBuff)
        nowPlayer.pre_attack_card = null
    }

    suspend fun divingProcess(player: PlayerEnum, card: Card?): Boolean{
        val otherPlayer = getPlayer(player.opposite())

        when(otherPlayer.forwardDiving){
            null -> {
                return false
            }
            true -> {
                sendSimpleCommand(getSocket(player.opposite()), CommandEnum.DIVING_SHOW)
                sendSimpleCommand(getSocket(player), CommandEnum.DIVING_FORWARD)
                addThisTurnDistance(-1)
                addThisTurnSwellDistance(-1)

            }
            false -> {
                sendSimpleCommand(getSocket(player.opposite()), CommandEnum.DIVING_SHOW)
                sendSimpleCommand(getSocket(player), CommandEnum.DIVING_BACKWARD)
                addThisTurnDistance(1)
                addThisTurnSwellDistance(1)
            }
        }

        val nowPlayer = getPlayer(player)

        card?.let {
            if(it.card_data.card_type == CardType.ATTACK){
                val nowAttack = nowPlayer.pre_attack_card
                if(nowAttack?.rangeCheck(getAdjustDistance(), this, player, getPlayerRangeBuff(player)) == false){
                    nowPlayer.divingSuccess = true
                    return true
                }
            }
        }

        return false
    }

    /**
     isCost means bi yong
     isConsume means so mo gap
     */
    suspend fun useCardFrom(player: PlayerEnum, card: Card, location: LocationEnum, react: Boolean, react_attack: MadeAttack?,
                            isCost: Boolean, isConsume: Boolean, napChange: Int = -1): Boolean{
        if(react_attack != null && !react){
            react_attack.isItReact = false
        }

        if(getEndTurn(player) || endCurrentPhase){
            return false
        }

        val cost = card.canUse(player, this, react_attack, isCost, isConsume)
        if(cost != -2){
            if(!getPlayer(player).notCharge){
                gaugeIncreaseRequest(player, card)
            }

            if(location == LocationEnum.READY_SOLDIER_ZONE) {
                logger.insert(
                    Log(
                        player, LogText.USE_CARD_IN_SOLDIER, card.card_number,
                        card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }
            else if(location == LocationEnum.COVER_CARD && react) {
                logger.insert(
                    Log(
                        player, LogText.USE_CARD_IN_COVER_AND_REACT, card.card_number,
                        card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }
            else if(location == LocationEnum.COVER_CARD) {
                logger.insert(
                    Log(
                        player, LogText.USE_CARD_IN_COVER, card.card_number,
                        card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }
            else if(react) {
                logger.insert(
                    Log(
                        player, LogText.USE_CARD_REACT, card.card_number,
                        card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }
            else {
                logger.insert(
                    Log(
                        player, LogText.USE_CARD, card.card_number,
                        card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }

            val nowPlayer = getPlayer(player)

            val isTermination = terminationListenerProcess(player, card)
            if(isCost) card.effectText(player, this, react_attack, TextEffectTag.COST)

            //kamuwi card cost
            val otherPlayer = getPlayer(player.opposite())
            var cardMustPay = 0

            if(card.card_data.card_class == CardClass.SPECIAL && card.card_data.card_type == CardType.BEHAVIOR){
                for(usedCard in otherPlayer.usedSpecialCard.values){
                    cardMustPay += card.effectAllValidEffect(player.opposite(), this, TextEffectTag.KAMUWI_LOGIC)
                }
            }

            if(nowPlayer.nextCostAddMegami == card.card_data.megami){
                cardMustPay += 1
            }

            if(cardMustPay > 0){
                selectCardFrom(player, player, player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 2111, cardMustPay
                ) { it, _ -> nowPlayer.nextCostAddMegami == it.card_data.megami }?.let {selected ->
                    for(card_number in selected){
                        popCardFrom(player.opposite(), card_number, LocationEnum.HAND, true)?.let {
                            insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                        }
                    }
                }
            }

            nowPlayer.nextCostAddMegami = null
            //kamuwi card cost

            //kamuwi dawn
            if(react_attack?.effectText(card.card_number, player.opposite(), this, react_attack,
                    TextEffectTag.WHEN_THIS_CARD_REACTED) == 1){
                popCardFrom(player, card.card_number, location, true)?.let {
                    if(cost == -1){
                        insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                    else{
                        insertCardTo(player, it, LocationEnum.YOUR_USED_CARD, true)
                    }
                }
                if(card.card_data.card_type == CardType.ATTACK){
                    val nowAttack = nowPlayer.pre_attack_card
                    nowAttack?.activeOtherBuff(this, player, nowPlayer.otherBuff)
                    nowAttack?.getDamage(this, player, nowPlayer.attackBuff)
                    nowPlayer.pre_attack_card = null
                }
                return true
            }
            //kamuwi dawn

            if(cost == -1){
                var lightHouseCheck = 0
                if(turnPlayer == player && card.card_data.card_type != CardType.ATTACK && location == LocationEnum.HAND){
                    for(otherUsedCard in getPlayer(player.opposite()).usedSpecialCard.values){
                        lightHouseCheck += otherUsedCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.HATSUMI_LIGHTHOUSE)
                    }
                }

                //normal card use
                if(lightHouseCheck == 0){
                    if(divingProcess(player, card)){
                        afterDivingSuccess(player, card, location)
                        nowPlayer.isUseCard = true
                        return true
                    }
                    popCardFrom(player, card.card_number, location, true)?.let {
                        insertCardTo(player, it, LocationEnum.PLAYING_ZONE_YOUR, true)
                    }
                    sendUseCardMeesage(getSocket(player), getSocket(player.opposite()), react, card.card_number)
                    card.use(player, this, react_attack, isTermination, napChange)
                }
                //hatsumi's lighthouse work so, behavior card can not work
                else{
                    popCardFrom(player, card.card_number, location, true)?.let {
                        insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                    for(otherUsedCard in getPlayer(player.opposite()).usedSpecialCard.values){
                        if(otherUsedCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.AFTER_HATSUMI_LIGHTHOUSE) > 0){
                            break
                        }
                    }
                }
                return true
            }
            if(cost >= 0){
                card.special_card_state = SpecialCardEnum.PLAYING
                flareToDust(player, cost, Arrow.NULL, player, card.player, Log.SPECIAL_COST)
                logger.insert(Log(player, LogText.END_EFFECT, Log.SPECIAL_COST, -1))
                cleanAfterUseCost()

                if(divingProcess(player, card)){
                    afterDivingSuccess(player, card, location)
                    return true
                }

                popCardFrom(player, card.card_number, location, true)?.let {
                    insertCardTo(player, it, LocationEnum.PLAYING_ZONE_YOUR, true)
                }
                sendUseCardMeesage(getSocket(player), getSocket(player.opposite()), react, card.card_number)
                card.use(player, this, react_attack, isTermination, napChange)
                return true
            }
            nowPlayer.isUseCard = true
        }
        return false
    }

    private suspend fun useCardPerjury(player: PlayerEnum, falseCard: Card, perjury_card_number: Int,
                                       location: LocationEnum): Boolean{
        val perjuryCard = getPerjuryCard(perjury_card_number)?: return false
        if(perjuryCheck[perjury_card_number - 2200]){
            return false
        }
        val cost = perjuryCard.canUse(player, this, null, isCost = true, isConsume = true)

        if(cost != -2){
            if(!getPlayer(player).notCharge){
                gaugeIncreaseRequest(player, perjuryCard)
            }

            //kamuwi card cost
            val nowPlayer = getPlayer(player)
            var cardMustPay = 0

            if(nowPlayer.nextCostAddMegami == perjuryCard.card_data.megami){
                cardMustPay += 1
            }

            if(cardMustPay > 0){
                selectCardFrom(player, player, player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 2111, cardMustPay
                ) { it, _ -> nowPlayer.nextCostAddMegami == it.card_data.megami }?.let {selected ->
                    for(card_number in selected){
                        popCardFrom(player.opposite(), card_number, LocationEnum.HAND, true)?.let {
                            insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                        }
                    }
                }
            }

            nowPlayer.nextCostAddMegami = null
            //kamuwi card cost

            perjuryCheck[perjury_card_number - 2200] = true
            var public = false
            when(receiveSelectDisprove(getSocket(player.opposite()), perjury_card_number)){
                CommandEnum.SELECT_ONE -> {
                    public = true
                    sendSimpleCommand(player1_socket, player2_socket, CommandEnum.SHOW_DISPROVE_RESULT, falseCard.card_number)
                    if(perjuryCard.card_data.card_name == falseCard.card_data.card_name){
                        //반증에 실패
                        logger.insert(Log(player, LogText.FAIL_DISPROVE, -1, -1))

                        sendChooseDamage(getSocket(player.opposite()), CommandEnum.CHOOSE_CHOJO, 1, 1)
                        val chosen = receiveChooseDamage(getSocket(player.opposite()))
                        processDamage(player, chosen, Pair(1, 1), false, null, null, Log.CHOJO)
                        logger.insert(Log(player, LogText.END_EFFECT, Log.CHOJO, -1))

                        if(perjuryCard.card_data.card_name == CardName.RENRI_FLOATING_CLOUDS){
                            sendChooseDamage(getSocket(player.opposite()), CommandEnum.CHOOSE_CHOJO, 1, 1)
                            val secondChosen = receiveChooseDamage(getSocket(player.opposite()))
                            processDamage(player, secondChosen, Pair(1, 1), false, null, null, Log.CHOJO)
                            logger.insert(Log(player, LogText.END_EFFECT, Log.CHOJO, -1))
                        }
                    }
                    else{
                        //반증에 성공
                        popCardFrom(player, falseCard.card_number, location, true)?.let {
                            insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                        }
                        return false
                    }
                }
                else -> {
                    if(perjuryCard.effectText(falseCard.card_number,
                        player, this, null, TextEffectTag.WHEN_THIS_CARD_NOT_DISPROVE) == 1){
                        public = true
                    }
                }
            }

            if(location == LocationEnum.READY_SOLDIER_ZONE) {
                logger.insert(
                    Log(
                        player, LogText.USE_CARD_IN_SOLDIER_PERJURE, perjuryCard.card_number,
                        perjuryCard.card_data.megami.real_number, boolean = perjuryCard.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }
            else {
                logger.insert(
                    Log(
                        player, LogText.USE_CARD_PERJURE, perjuryCard.card_number,
                        perjuryCard.card_data.megami.real_number, boolean = perjuryCard.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }

            var lightHouseCheck = 0
            if(turnPlayer == player && perjuryCard.card_data.card_type != CardType.ATTACK && location == LocationEnum.HAND){
                for(otherUsedCard in getPlayer(player.opposite()).usedSpecialCard.values){
                    lightHouseCheck += otherUsedCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.HATSUMI_LIGHTHOUSE)
                }
            }

            //normal card use
            if(lightHouseCheck == 0){
                if(divingProcess(player, perjuryCard)){
                    afterDivingSuccess(player, falseCard, location)
                    nowPlayer.isUseCard = true
                    return true
                }
                popCardFrom(player, falseCard.card_number, location, public)?.let {
                    insertCardTo(player, it, LocationEnum.PLAYING_ZONE_YOUR, public)
                }
                sendUseCardMeesage(getSocket(player), getSocket(player.opposite()), false, perjuryCard.card_number)
                perjuryCard.use(player, this, null, false, 0)

                if(public){
                    popCardFrom(player, falseCard.card_number, LocationEnum.PLAYING_ZONE_YOUR, true)?.let {
                        insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                }
                else{
                    popCardFrom(player, falseCard.card_number, LocationEnum.PLAYING_ZONE_YOUR, false)?.let {
                        insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                    }
                }
            }
            //hatsumi's lighthouse work so, behavior card can not work
            else{
                popCardFrom(player, falseCard.card_number, location, true)
                insertCardTo(player, falseCard, LocationEnum.DISCARD_YOUR, true)
                for(otherUsedCard in getPlayer(player.opposite()).usedSpecialCard.values){
                    if(otherUsedCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.AFTER_HATSUMI_LIGHTHOUSE) > 0){
                        break
                    }
                }
            }

            nowPlayer.isUseCard = true
            return true
        }
        return false
    }

    private suspend fun afterResolveAttack(attackPlayer: PlayerEnum, selectedDamage: DamageSelect, damage: Pair<Int, Int>,
                                           attackNumber: Int){
        if((selectedDamage == DamageSelect.BOTH && (damage.first >= 1 || damage.second >= 1)) ||
            (selectedDamage == DamageSelect.AURA && damage.first >= 1) ||
            (selectedDamage == DamageSelect.LIFE && damage.second >= 1)){
            for(card in getPlayer(attackPlayer.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(attackPlayer.opposite(), this, TextEffectTag.WHEN_GET_DAMAGE_BY_ATTACK)
            }
        }

        for(card in getPlayer(attackPlayer).enchantmentCard.values){
            card.effectAllValidEffect(attackNumber, attackPlayer, this, TextEffectTag.WHEN_AFTER_ATTACK_RESOLVE_OTHER_USE_ATTACK_NUMBER)
        }
    }

    suspend fun afterMakeAttack(card_number: Int, attack_player: PlayerEnum, react_attack: MadeAttack?){
        val attackerSocket = getSocket(attack_player)
        val otherSocket = getSocket(attack_player.opposite())
        val attackPlayer = getPlayer(attack_player)
        val otherPlayer = getPlayer(attack_player.opposite())

        if(attackPlayer.pre_attack_card == null){
            return
        }

        val nowAttack = attackPlayer.pre_attack_card!!
        attackPlayer.pre_attack_card = null

        logger.insert(
            Log(attack_player, LogText.ATTACK, nowAttack.card_number, when(nowAttack.card_class){
                CardClass.POISON -> 5
                CardClass.IDEA -> 4
                CardClass.SOLDIER -> 3
                CardClass.SPECIAL -> 2
                CardClass.NORMAL -> 1
                CardClass.NULL -> 0
        })
        )

        makeAttackComplete(attackerSocket, otherSocket, card_number)
        sendAttackInformation(attackerSocket, otherSocket, nowAttack.toInformation())
        if(react_attack == null){
            var reactCheckCancel = false
            for(card in otherPlayer.usedSpecialCard.values){
                if (card.effectAllValidEffect(attack_player.opposite(), this, TextEffectTag.WHEN_GET_ATTACK) == 1){
                    reactCheckCancel = true
                }
            }
            if(!otherPlayer.end_turn && !reactCheckCancel){
                while(true){
                    sendRequestReact(otherSocket)
                    val react = receiveReact(otherSocket)
                    if(react.first == CommandEnum.REACT_USE_CARD_HAND){
                        val card = otherPlayer.getCardFromHand(react.second)?: continue
                        if(reactCheck(attack_player.opposite(), card, nowAttack)){
                            if(useCardFrom(attack_player.opposite(), card, LocationEnum.HAND, true, nowAttack,
                                    isCost = true, isConsume = true)) {
                                otherPlayer.thisTurnReact = true
                                break
                            }
                        }

                    }
                    else if(react.first == CommandEnum.REACT_USE_CARD_SPECIAL){
                        val card = otherPlayer.getCardFromSpecial(react.second)?: continue
                        if(reactCheck(attack_player.opposite(), card, nowAttack)){
                            if(useCardFrom(attack_player.opposite(), card, LocationEnum.SPECIAL_CARD, true, nowAttack,
                                    isCost = true, isConsume = true)) {
                                otherPlayer.thisTurnReact = true
                                break
                            }
                        }
                    }
                    else if(react.first == CommandEnum.REACT_USE_CARD_SOLDIER){
                        val card = otherPlayer.getCardFromSoldier(react.second)?: continue
                        if(reactCheck(attack_player.opposite(), card, nowAttack)){
                            if(useCardFrom(attack_player.opposite(), card, LocationEnum.READY_SOLDIER_ZONE, true, nowAttack,
                                    isCost = true, isConsume = true)) {
                                otherPlayer.thisTurnReact = true
                                break
                            }
                        }
                    }
                    else{
                        break
                    }
                }
            }
        }

        nowAttack.activeOtherBuff(this, attack_player, attackPlayer.otherBuff)
        val damage = nowAttack.getDamage(this, attack_player, attackPlayer.attackBuff)
        var selectedDamage: DamageSelect = DamageSelect.NULL

        if(endCurrentPhase){
            return
        }

        logger.insert(Log(attack_player, LogText.DAMAGE_PROCESS_START, nowAttack.card_number, nowAttack.card_number))
        if(nowAttack.editedInevitable || nowAttack.rangeCheck(getAdjustDistance(), this, attack_player, attackPlayer.rangeBuff)){
            otherPlayer.isNextTurnTailWind = false
            if(nowAttack.isItValid){
                if(nowAttack.isItDamage){
                    if(nowAttack.beforeProcessDamageCheck(attack_player, this, react_attack)){
                        logger.insert(Log(attack_player, LogText.START_PROCESS_ATTACK_DAMAGE, nowAttack.card_number, -1))
                        val chosen = if(nowAttack.canNotSelectAura){
                            CommandEnum.CHOOSE_LIFE
                        }
                        else if(nowAttack.effectText(attack_player, this, react_attack, TextEffectTag.SELECT_DAMAGE_BY_ATTACKER) == 1){
                            damageSelect(attack_player, damage, false)
                        } else {
                            damageSelect(attack_player.opposite(), damage)
                        }
                        val auraReplace = nowAttack.effectText(attack_player, this, react_attack, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE)
                        val lifeReplace = nowAttack.effectText(attack_player, this, react_attack, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE)

                        if(nowAttack.bothSideDamage){
                            selectedDamage = DamageSelect.BOTH
                            val auraDamage = processDamage(attack_player.opposite(), CommandEnum.CHOOSE_AURA,
                                Pair(damage.first, 999), false, auraReplace, lifeReplace, nowAttack.card_number)
                            val lifeDamage = processDamage(attack_player.opposite(), CommandEnum.CHOOSE_LIFE,
                                Pair(999, damage.second), false, auraReplace, lifeReplace, nowAttack.card_number)
                            if(auraDamage == -1 && lifeDamage == -1){
                                selectedDamage = DamageSelect.NULL
                            }
                        }
                        else{
                            selectedDamage = if(chosen == CommandEnum.CHOOSE_LIFE) DamageSelect.LIFE else DamageSelect.AURA
                            val auraDamage = processDamage(attack_player.opposite(), chosen,
                                Pair(damage.first, damage.second), false, auraReplace, lifeReplace, nowAttack.card_number)
                            if(auraDamage == -1){
                                selectedDamage = DamageSelect.NULL
                            }
                        }
                        logger.insert(Log(attack_player, LogText.END_EFFECT, nowAttack.card_number, -1))
                    }
                }
                nowAttack.afterAttackProcess(attack_player, this, react_attack, selectedDamage)
                afterResolveAttack(attack_player, selectedDamage, damage, nowAttack.card_number)
            }
        }

        if(endCurrentPhase){
            return
        }

        for(card in getPlayer(attack_player.opposite()).usedSpecialCard.values){
            card.effectAllValidEffect(attack_player.opposite(), this, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE)
        }

        for(card in getPlayer(attack_player.opposite()).enchantmentCard.values){
            card.effectAllValidEffect(attack_player.opposite(), this, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE)
        }
    }

    suspend fun movePlayingCard(player: PlayerEnum, place: LocationEnum?, card_number: Int){
        val card = popCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR, true)?: return

        if(place != null){
            if(card.card_data.card_class == CardClass.SPECIAL){
                if(place == LocationEnum.SPECIAL_CARD){
                    card.special_card_state = SpecialCardEnum.UNUSED
                }
                else{
                    card.special_card_state = SpecialCardEnum.PLAYED
                }
            }

            if(card.isSoftAttack){
                if(place == LocationEnum.HAND || place == LocationEnum.DECK || place == LocationEnum.YOUR_DECK_BELOW
                    || place == LocationEnum.YOUR_DECK_TOP){
                    insertCardTo(card.player, card, LocationEnum.DISCARD_YOUR, true)
                }
                else{
                    insertCardTo(card.player, card, place, true)
                }
            }
            else{
                insertCardTo(card.player, card, place, true)
            }
        }
        else if(card.card_data.card_type == CardType.ENCHANTMENT){
            insertCardTo(player, card, LocationEnum.ENCHANTMENT_ZONE, true)
            if(card.isItDestruction()){
                enchantmentDestruction(player, card)
            }
        }
        else{
            when(card.card_data.card_class){
                CardClass.SPECIAL -> {
                    card.addReturnListener(card.player, this)
                    card.special_card_state = SpecialCardEnum.PLAYED
                    insertCardTo(card.player, card, LocationEnum.YOUR_USED_CARD, true)
                }
                CardClass.NORMAL, CardClass.POISON  -> {
                    insertCardTo(card.player, card, LocationEnum.DISCARD_YOUR, true)
                }
                CardClass.NULL -> {
                    insertCardTo(card.player, card, LocationEnum.NOT_READY_SOLDIER_ZONE, true)
                }
                CardClass.SOLDIER -> {
                    insertCardTo(card.player, card, LocationEnum.NOT_READY_SOLDIER_ZONE, true)
                }
                CardClass.IDEA -> {
                    TODO()
                }
            }
        }
    }

    private suspend fun useAfterTriggerProcess(player: PlayerEnum, text: Text?, card_number: Int){
        if(text == null) return
        else{
            when(text.tag){
                TextEffectTag.WHEN_USE_REACT_CARD_YOUR_END, TextEffectTag.WHEN_USE_BEHAVIOR_END,
                TextEffectTag.WHEN_FULL_POWER_USED_YOUR, TextEffectTag.WHEN_AFTER_CARD_USE -> {
                    text.effect!!(card_number, player, this, null)
                }
                TextEffectTag.WHEN_THIS_CARD_REACTED_AFTER -> {
                    text.effect!!(card_number, player.opposite(), this, null)
                }
                else -> {
                    TODO("${text.tag} error")
                }
            }
        }
    }

    var cardForEffect: Card? = null

    suspend fun afterCardUsed(card_number: Int, player: PlayerEnum, thisCard: Card){
        movePlayingCard(player, null, card_number)

        cardForEffect = thisCard
        for(card in getPlayer(player).enchantmentCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_AFTER_CARD_USE)
        }
        cardForEffect = null

        logger.insert(Log(PlayerEnum.PLAYER1, LogText.END_EFFECT, card_number, -1))

        val keys = thisCard.cardUseEndEffect.keys.toMutableList()
        if(keys.isNotEmpty()){
            while(keys.size >= 2){
                val selected = receiveCardEffectOrder(getSocket(turnPlayer), CommandEnum.SELECT_AFTER_CARD_USED_EFFECT_ORDER, keys)
                if(selected in keys){
                    val effect = thisCard.cardUseEndEffect[selected]
                    useAfterTriggerProcess(player, effect, selected)
                    thisCard.cardUseEndEffect.remove(selected)
                    keys.remove(selected)
                }
            }
            val lastEffect = thisCard.cardUseEndEffect[keys[0]]
            useAfterTriggerProcess(player, lastEffect, keys[0])
            thisCard.cardUseEndEffect.remove(keys[0])
        }
    }

    suspend fun afterDestruction(player: PlayerEnum, card_number: Int, location: LocationEnum){
        val card = popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?: return
        card.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
        when(card.card_data.card_class){
            CardClass.SPECIAL -> {
                card.addReturnListener(card.player, this)
                card.special_card_state = SpecialCardEnum.PLAYED
                insertCardTo(card.player, card, LocationEnum.YOUR_USED_CARD, true)
            }
            CardClass.NORMAL, CardClass.POISON -> {
                insertCardTo(card.player, card, location, true)
            }
            CardClass.SOLDIER -> {
                insertCardTo(card.player, card, LocationEnum.NOT_READY_SOLDIER_ZONE, true)
            }
            CardClass.NULL, CardClass.IDEA -> {
                TODO()
            }
        }

        cardToDust(card.player, card.getNap(), card, false, Log.AFTER_DESTRUCTION_PROCESS)
        logger.insert(Log(player, LogText.END_EFFECT, Log.AFTER_DESTRUCTION_PROCESS, -1))
    }

    suspend fun enchantmentDestruction(player: PlayerEnum, card: Card){
        sendDestructionEnchant(getSocket(player), getSocket(player.opposite()), card.card_number)

        var checkValid = true
        for(checkCard in getPlayer(player.opposite()).enchantmentCard.values){
            if(checkCard.canUseEffectCheck(TextEffectTag.AFTER_DESTRUCTION_EFFECT_INVALID_OTHER)){
                checkValid = false
                break
            }
        }
        if(checkValid){
            card.destructionEnchantmentNormaly(player, this)
            logger.insert(Log(PlayerEnum.PLAYER1, LogText.END_EFFECT, card.card_number, -1))
        }


        if(!endCurrentPhase){
            for(enchantmentCard in getPlayer(player).enchantmentCard.values){
                enchantmentCard.effectAllValidEffect(player, this, TextEffectTag.WHEN_ENCHANTMENT_DESTRUCTION_YOUR)
            }
        }
        afterDestruction(player, card.card_number, LocationEnum.DISCARD_YOUR)
    }

    suspend fun enchantmentDestructionNotNormally(player: PlayerEnum, card: Card){
        sendDestructionEnchant(getSocket(player), getSocket(player.opposite()), card.card_number)
        afterDestruction(player, card.card_number, LocationEnum.COVER_CARD)
    }

    private suspend fun enchantmentReduceAll(player: PlayerEnum){
        if(player1.enchantmentCard.isEmpty() && player2.enchantmentCard.isEmpty()){
            return
        }

        sendReduceNapStart(player1_socket)
        sendReduceNapStart(player2_socket)

        val player1Card: HashMap<Int, Boolean> = HashMap()
        val player2Card: HashMap<Int, Boolean> = HashMap()

        nowPhase = START_PHASE_REDUCE_NAP

        for(nowCard in player1.enchantmentCard.values){
            val nap = nowCard.reduceNapNormal(PlayerEnum.PLAYER1, this)
            cardToDust(PlayerEnum.PLAYER1, nap, nowCard, true, Log.NORMAL_NAP_PROCESS)
            if(nowCard.isItDestruction()){
                player1Card[nowCard.card_number] = true
            }
        }

        for(nowCard in player2.enchantmentCard.values){
            val nap = nowCard.reduceNapNormal(PlayerEnum.PLAYER2, this)
            cardToDust(PlayerEnum.PLAYER2, nap, nowCard, true, Log.NORMAL_NAP_PROCESS)
            if(nowCard.isItDestruction()){
                player2Card[nowCard.card_number] = true
            }
        }

        nowPhase = START_PHASE

        logger.insert(Log(PlayerEnum.PLAYER1, LogText.END_EFFECT, Log.NORMAL_NAP_PROCESS, -1))

        sendReduceNapEnd(player1_socket)
        sendReduceNapEnd(player2_socket)

        if(player1Card.isEmpty() && player2Card.isEmpty()){
            return
        }

        when(player){
            PlayerEnum.PLAYER1 -> {
                sendStartSelectEnchantment(player1_socket)
                while(true){
                    sendRequestEnchantmentCard(player1_socket, player1Card.keys.toMutableList(), player2Card.keys.toMutableList())
                    val receive = receiveEnchantment(player1_socket)
                    when(receive.first){
                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
                            if(player1Card[receive.second] == true){
                                val card = player1.enchantmentCard[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
                                player1Card.remove(receive.second)
                                if(endCurrentPhase){
                                    break
                                }
                            }
                            if(player1Card.isEmpty() && player2Card.isEmpty()){
                                break
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
                            if(player2Card[receive.second] == true){
                                val card = player2.enchantmentCard[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
                                player2Card.remove(receive.second)
                                if(endCurrentPhase){
                                    break
                                }
                            }
                            if(player1Card.isEmpty() && player2Card.isEmpty()){
                                break
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_END -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                sendStartSelectEnchantment(player2_socket)
                while(true){
                    sendRequestEnchantmentCard(player2_socket, player2Card.keys.toMutableList(), player1Card.keys.toMutableList())
                    val receive = receiveEnchantment(player2_socket)
                    when(receive.first){
                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
                            if(player2Card[receive.second] == true){
                                val card = player2.enchantmentCard[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
                                player2Card.remove(receive.second)
                                if(endCurrentPhase){
                                    break
                                }
                            }
                            if(player1Card.isEmpty() && player2Card.isEmpty()){
                                break
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
                            if(player1Card[receive.second] == true){
                                val card = player1.enchantmentCard[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
                                player1Card.remove(receive.second)
                                if(endCurrentPhase){
                                    break
                                }
                            }
                            if(player1Card.isEmpty() && player2Card.isEmpty()){
                                break
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_END -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
        }

        sendSimpleCommand(getSocket(player), CommandEnum.SELECT_ENCHANTMENT_END)

        if(endCurrentPhase){
            if(player1Card.isNotEmpty()){
                for(card_number in player1Card.keys){
                    afterDestruction(PlayerEnum.PLAYER1, card_number, LocationEnum.DISCARD_YOUR)
                }
            }

            if(player2Card.isNotEmpty()){
                for(card_number in player2Card.keys){
                    afterDestruction(PlayerEnum.PLAYER2, card_number, LocationEnum.DISCARD_YOUR)
                }
            }
        }
        else{
            if(player1Card.isNotEmpty()){
                for(card_number in player1Card.keys){
                    val card = player1.enchantmentCard[card_number]
                    enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
                }
            }

            if(player2Card.isNotEmpty()){
                for(card_number in player2Card.keys){
                    val card = player2.enchantmentCard[card_number]
                    enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
                }
            }
        }
    }

    var gameEnd = false

    suspend fun gameEnd(winner: PlayerEnum?, loser: PlayerEnum?){
        val winnerSocket: Connection
        val loserSocket: Connection

        // you lose effect
        if(winner == null){
            val loserPlayer = getPlayer(loser!!)

            for(card in loserPlayer.special_card_deck.values){
                if(card.effectText(loser, this, null, TextEffectTag.WHEN_LOSE_GAME) == 1){
                    if(!(loserPlayer.isLose())){
                        return
                    }
                }
            }
            for(card in loserPlayer.usedSpecialCard.values){
                if(card.effectText(loser, this, null, TextEffectTag.WHEN_LOSE_GAME) == 1){
                    if(!(loserPlayer.isLose())){
                        return
                    }
                }
            }

            winnerSocket = getSocket(loser.opposite())
            loserSocket = getSocket(loser)
        }

        //you win effect
        else if(loser == null){
            val winnerPlayer = getPlayer(winner)

            for(card in winnerPlayer.enchantmentCard.values){
                if(card.effectAllValidEffect(winner, this, TextEffectTag.CAN_NOT_WIN) != 0){
                    return
                }
            }

            winnerSocket = getSocket(winner)
            loserSocket = getSocket(winner.opposite())
        }

        //else(life)
        else {
            val loserPlayer = getPlayer(loser)
            val winnerPlayer = getPlayer(winner)

            for(card in winnerPlayer.enchantmentCard.values){
                if(card.effectAllValidEffect(winner, this, TextEffectTag.CAN_NOT_WIN) != 0){
                    return
                }
            }

            for(card in loserPlayer.special_card_deck.values){
                if(card.effectText(loser, this, null, TextEffectTag.WHEN_LOSE_GAME) == 1){
                    if(!(loserPlayer.isLose())){
                        return
                    }
                }
            }
            for(card in loserPlayer.usedSpecialCard.values){
                if(card.effectText(loser, this, null, TextEffectTag.WHEN_LOSE_GAME) == 1){
                    if(!(loserPlayer.isLose())){
                        return
                    }
                }
            }

            winnerSocket = getSocket(winner)
            loserSocket = getSocket(loser)
        }

        sendGameEnd(winnerSocket, loserSocket)

        player1_socket.session.close()
        player2_socket.session.close()
        RoomInformation.roomHashMap.remove(winnerSocket.roomNumber)
        winnerSocket.gameEnd = true
        loserSocket.gameEnd = true
        gameEnd = true
    }

    suspend fun auraDamageProcess(player: PlayerEnum, data: MutableList<Int>, replace: Int?, card_number: Int){
        val nowPlayer = getPlayer(player)
        for (index in data.indices){
            if(index % 2 == 0){
                if(data[index] == LocationEnum.YOUR_AURA.real_number){
                    if(replace == null){
                        auraToDust(player, data[index + 1], Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, card_number)
                    }
                    else{
                        moveTokenByInt(player, LocationEnum.YOUR_AURA.real_number,
                            replace, data[index + 1], true, -1, card_number)
                    }
                }
                else{
                    if(replace == null){
                        cardToDust(player, data[index + 1], nowPlayer.enchantmentCard[data[index]]!!, false, card_number)
                    }
                    else{
                        moveTokenByInt(player, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD.real_number,
                            replace, data[index + 1], true, data[index], card_number)
                    }
                    if(nowPlayer.enchantmentCard[data[index]]!!.isItDestruction()) enchantmentDestruction(player, nowPlayer.enchantmentCard[data[index]]!!)
                }
            }
        }
    }

    suspend fun damageSelect(player: PlayerEnum, damage: Pair<Int, Int>, your: Boolean = true): CommandEnum{
        if(damage.first == 999) return CommandEnum.CHOOSE_LIFE
        if(damage.second == 999) return CommandEnum.CHOOSE_AURA
        if(if(your) getPlayer(player).checkAuraDamage(damage.first) == null
            else getPlayer(player.opposite()).checkAuraDamage(damage.first) == null ) return CommandEnum.CHOOSE_LIFE
        sendChooseDamage(getSocket(player), if(your) CommandEnum.CHOOSE_CARD_DAMAGE
            else CommandEnum.CHOOSE_CARD_DAMAGE_OTHER, damage.first, damage.second)
        return receiveChooseDamage(getSocket(player))
    }

    suspend fun moveTokenByInt(player: PlayerEnum, from: Int, to: Int, number: Int, damage: Boolean, cardNumber: Int,
                               effectCard: Int){
        when(LocationEnum.fromInt(from)){
            LocationEnum.YOUR_AURA -> {
                if(to > 99){
                    getCardFrom(player.opposite(), to, LocationEnum.ENCHANTMENT_ZONE)?.let {
                        auraToCard(player, number, it, effectCard, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD)
                    }?:getCardFrom(player, to, LocationEnum.ENCHANTMENT_ZONE)?.let {
                        auraToCard(player, number, it, effectCard, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD)
                    }?: run {
                        auraToDust(player, number, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, effectCard)
                    }
                }
                else {
                    when(LocationEnum.fromInt(to)){
                        LocationEnum.DISTANCE -> {
                            auraToDistance(player, number, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, effectCard)
                        }
                        else -> TODO()
                    }
                }
            }
            LocationEnum.YOUR_LIFE -> {
                if(to > 99){
                    getCardFrom(player.opposite(), to, LocationEnum.ENCHANTMENT_ZONE)?.let {
                        lifeToCard(player, number, it, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD, false, damage, effectCard)
                    }?:getCardFrom(player, to, LocationEnum.ENCHANTMENT_ZONE)?.let {
                        lifeToCard(player, number, it, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, false, damage, effectCard)
                    }?: run {
                        lifeToSelfFlare(player, number, false, damage, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, effectCard)
                    }
                }
                else{
                    when(LocationEnum.fromInt(to)){
                        LocationEnum.DISTANCE -> {
                            lifeToDistance(player, number, damage, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, effectCard)
                        }
                        else -> TODO()
                    }
                }

            }
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD -> {
                when(LocationEnum.fromInt(to)){
                    LocationEnum.DISTANCE -> {
                        cardToDistance(player, number, getPlayer(player).enchantmentCard[cardNumber]!!, effectCard)
                    }
                    else -> TODO()
                }
            }
            else -> TODO()
        }

    }

    //damage first = AURA, damage second = LIFE
    suspend fun processDamage(player: PlayerEnum, command: CommandEnum, damage: Pair<Int, Int>, reconstruct: Boolean,
        auraReplace: Int?, lifeReplace: Int?, card_number: Int): Int{
        val nowPlayer = getPlayer(player)

        var checkDamage = 0
        for(card in nowPlayer.usedSpecialCard.values){
            checkDamage += card.effectAllValidEffect(player, this, TextEffectTag.DO_NOT_GET_DAMAGE)
        }
        if(checkDamage > 0){
            return -1
        }


        if(command == CommandEnum.CHOOSE_AURA){
            val selectable = nowPlayer.checkAuraDamage(damage.first)
            logger.insert(Log(player, LogText.GET_AURA_DAMAGE, damage.first, card_number))
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.GET_DAMAGE_AURA_YOUR)
            if(selectable == null){
                auraDamageProcess(player, nowPlayer.getFullAuraDamage(), auraReplace, card_number)
            }
            else{
                if(selectable.size == 1){
                    if(selectable[0] == LocationEnum.YOUR_AURA.real_number){
                        if(auraReplace == null){
                            auraToDust(player, damage.first, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, card_number)
                        }
                        else{
                            moveTokenByInt(player, LocationEnum.YOUR_AURA.real_number,
                                auraReplace, damage.first, true, -1, card_number)
                        }
                    }
                    else{
                        if(auraReplace == null){
                            cardToDust(player, damage.first, nowPlayer.enchantmentCard[selectable[0]]!!,
                                false, card_number)
                        }
                        else{
                            moveTokenByInt(player, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD.real_number,
                                auraReplace, damage.first, true, selectable[0], card_number)
                        }
                        if(nowPlayer.enchantmentCard[selectable[0]]!!.isItDestruction()) enchantmentDestruction(player, nowPlayer.enchantmentCard[selectable[0]]!!)
                    }
                }
                else{
                    while(true){
                        val receive = receiveAuraDamageSelect(getSocket(player), selectable, damage.first)
                        if (nowPlayer.auraDamagePossible(receive, damage.first, selectable)){
                            auraDamageProcess(player, receive!!, auraReplace, card_number)
                            break
                        }
                    }
                }
            }
        }
        else{
            logger.insert(Log(player, LogText.GET_LIFE_DAMAGE, damage.second, card_number))
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.GET_DAMAGE_LIFE_YOUR)
            if(!reconstruct){
                addMarketPrice(player.opposite())
                reduceMarketPrice(player)
            }
            if(lifeReplace == null){
                lifeToSelfFlare(player, damage.second, reconstruct, true, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, card_number)
            }
            else{
                moveTokenByInt(player, LocationEnum.YOUR_LIFE.real_number, lifeReplace, damage.second, true, -1, card_number)
            }

            if(!reconstruct) chasmProcess(player)
        }
        return 1
    }

    suspend fun drawCard(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        for(i in 1..number){
            var drawCancel = false

            for(card in nowPlayer.usedSpecialCard.values){
                if(card.effectAllValidEffect(player, this, TextEffectTag.WHEN_DRAW_CARD) == 1){
                    drawCancel = true
                }
            }

            if(drawCancel){
                continue
            }

            if(nowPlayer.normalCardDeck.size == 0){
                sendChooseDamage(nowSocket, CommandEnum.CHOOSE_CHOJO, 1, 1)
                val chosen = receiveChooseDamage(nowSocket)
                processDamage(player, chosen, Pair(1, 1), false, null, null, Log.CHOJO)
                logger.insert(Log(player, LogText.END_EFFECT, Log.CHOJO, -1))
                continue
            }
            val drawCard = nowPlayer.normalCardDeck.first()
            sendDrawCard(nowSocket, otherSocket, drawCard.card_number)
            nowPlayer.hand[drawCard.card_number] = drawCard
            nowPlayer.normalCardDeck.removeFirst()
        }
    }

    //first means upperside of deck, last means belowside of deck
    suspend fun insertHandToDeck(public: Boolean, Below: Boolean, player: PlayerEnum, card_number: Int): Boolean{
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        return now_player.hand[card_number]?.let {
            if(Below) now_player.normalCardDeck.addLast(it)
            else now_player.normalCardDeck.addFirst(it)
            now_player.hand.remove(card_number)
            sendHandToDeck(now_socket, other_socket, card_number, public, Below)
            true
        }?: false
    }

    private suspend fun removeArtificialToken(){
        when(turnPlayer){
            PlayerEnum.PLAYER1 -> {
                if(player1ArtificialTokenOn != 0) {
                    thisTurnDistanceChange = true
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player1ArtificialTokenOn, -1)
                    player1.artificialTokenBurn += player1ArtificialTokenOn
                    player1ArtificialTokenOn = 0
                }
                if(player1ArtificialTokenOut != 0) {
                    thisTurnDistanceChange = true
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player1ArtificialTokenOut, -1)
                    player1.artificialTokenBurn += player1ArtificialTokenOut
                    player1ArtificialTokenOut = 0
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(player2ArtificialTokenOn != 0) {
                    thisTurnDistanceChange = true
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player2ArtificialTokenOn, -1)
                    player2.artificialTokenBurn += player2ArtificialTokenOn
                    player2ArtificialTokenOn = 0
                }
                if(player2ArtificialTokenOut != 0) {
                    thisTurnDistanceChange = true
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player2ArtificialTokenOut, -1)
                    player2.artificialTokenBurn += player2ArtificialTokenOut
                    player2ArtificialTokenOut = 0
                }
            }
        }
    }

    var player1NextTurnDraw = 2
    var player2NextTurnDraw = 2

    suspend fun startPhaseBeforeEffect(turnPlayer: PlayerEnum){
        this.turnPlayer = turnPlayer
        startTurnDistance = getAdjustDistance()

        val nowPlayer = getPlayer(turnPlayer)
        if(nowPlayer.tabooGauge != null){
            if(nowPlayer.life <= 5){
                tabooGaugeIncrease(turnPlayer, 2)
            }
            else if(nowPlayer.life <= 9){
                tabooGaugeIncrease(turnPlayer, 1)
            }
        }
    }

    suspend fun startPhaseEffectProcess(turnPlayer: PlayerEnum){
        when(turnPlayer){
            PlayerEnum.PLAYER1 -> {
                if(player1ArtificialTokenOn != 0 || player1ArtificialTokenOut != 0) {
                    startPhaseEffect[0] = Pair(CardEffectLocation.ARTIFICIAL_TOKEN, null)
                }
                if(player1.forwardDiving != null){
                    startPhaseEffect[1] = Pair(CardEffectLocation.DIVING, null)
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(player2ArtificialTokenOn != 0 || player2ArtificialTokenOut != 0) {
                    startPhaseEffect[0] = Pair(CardEffectLocation.ARTIFICIAL_TOKEN, null)
                }
                if(player2.forwardDiving != null){
                    startPhaseEffect[1] = Pair(CardEffectLocation.DIVING, null)
                }
            }
        }

        for(card in getPlayer(turnPlayer.opposite()).enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer.opposite(), this, TextEffectTag.WHEN_START_PHASE_OTHER)
        }
        for(card in getPlayer(turnPlayer.opposite()).usedSpecialCard.values){
            card.effectAllValidEffect(turnPlayer.opposite(), this, TextEffectTag.WHEN_START_PHASE_OTHER)
        }
        for(card in getPlayer(turnPlayer).enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_START_PHASE_YOUR)
        }
        for(card in getPlayer(turnPlayer).usedSpecialCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_START_PHASE_YOUR)
        }

        val keys = startPhaseEffect.keys.toMutableList()
        if(keys.isNotEmpty()){
            while(keys.size >= 2){
                val selected = receiveCardEffectOrder(getSocket(turnPlayer), CommandEnum.SELECT_START_PHASE_EFFECT_ORDER, keys)
                if(selected in keys){
                    val result = startPhaseEffect[selected]
                    when(result!!.first){
                        CardEffectLocation.ENCHANTMENT_YOUR, CardEffectLocation.USED_YOUR -> {
                            result.second!!.effect!!(selected, turnPlayer, this, null)
                        }
                        CardEffectLocation.ENCHANTMENT_OTHER, CardEffectLocation.USED_OTHER -> {
                            result.second!!.effect!!(selected, turnPlayer.opposite(), this, null)
                        }
                        CardEffectLocation.ARTIFICIAL_TOKEN -> {
                            removeArtificialToken()
                        }
                        CardEffectLocation.DIVING -> {
                            divingProcess(turnPlayer.opposite(), null)
                        }
                        else -> TODO()
                    }
                    startPhaseEffect.remove(selected)
                    keys.remove(selected)
                }
//                TODO("If there is an effect of canceling the effect in the start phase(immediate return due to life changes)," +
//                        " a check may be required, not now")
//                for(key in keys){
//                    val (place, effect) = startPhaseEffect[key]!!
//                }
            }
            if(keys.size == 1){
                val lastEffect = startPhaseEffect[keys[0]]
                when(lastEffect!!.first){
                    CardEffectLocation.ENCHANTMENT_YOUR, CardEffectLocation.USED_YOUR -> {
                        lastEffect.second!!.effect!!(keys[0], turnPlayer, this, null)
                    }
                    CardEffectLocation.ENCHANTMENT_OTHER, CardEffectLocation.USED_OTHER -> {
                        lastEffect.second!!.effect!!(keys[0], turnPlayer.opposite(), this, null)
                    }
                    CardEffectLocation.ARTIFICIAL_TOKEN -> {
                        removeArtificialToken()
                    }
                    CardEffectLocation.DIVING -> {
                        divingProcess(turnPlayer.opposite(), null)
                    }
                    else -> TODO()
                }
                startPhaseEffect.remove(keys[0])
                keys.remove(keys[0])
            }
        }
    }

    suspend fun startPhaseDefaultSecond(turnPlayer: PlayerEnum){
        addConcentration(turnPlayer)
        enchantmentReduceAll(turnPlayer)
        if(!endCurrentPhase){
            if(receiveReconstructRequest(getSocket(turnPlayer))){
                deckReconstruct(turnPlayer, true)
            }
            when(turnPlayer){
                PlayerEnum.PLAYER1 -> drawCard(turnPlayer, player1NextTurnDraw)
                PlayerEnum.PLAYER2 -> drawCard(turnPlayer, player2NextTurnDraw)
            }
        }
        player1NextTurnDraw = 2
        player2NextTurnDraw = 2
    }

    suspend fun mainPhaseEffectProcess(turnPlayer: PlayerEnum){
        //TODO("change this mechanism like endphaseeffectprocess(can choose order of effect)")
        val mainPhaseListener = getMainPhaseListener(turnPlayer)
        if(!(mainPhaseListener.isEmpty())){
            for(i in 1..mainPhaseListener.size){
                if(mainPhaseListener.isEmpty()) break
                val now = mainPhaseListener.first()
                mainPhaseListener.removeFirst()
                if(!(now.doAction(this, -1, -1, booleanPara1 = false, booleanPara2 = false))){
                    mainPhaseListener.addLast(now)
                }
            }
        }

        for(card in getPlayer(turnPlayer).usedSpecialCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_MAIN_PHASE_YOUR)
        }

        for(card in getPlayer(turnPlayer).enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_MAIN_PHASE_YOUR)
        }

        for(card in getPlayer(turnPlayer.opposite()).enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer.opposite(), this, TextEffectTag.WHEN_MAIN_PHASE_OTHER)
        }
    }

    val endPhaseEffect = HashMap<Int, Pair<CardEffectLocation, Text?>>()
    val nextEndPhaseEffect = HashMap<Int, Pair<CardEffectLocation, Text?>>()
    val startPhaseEffect = HashMap<Int, Pair<CardEffectLocation, Text?>>()

    fun cleanEndPhaseEffect(){
        endPhaseEffect.clear()
        for(key in nextEndPhaseEffect.keys){
            endPhaseEffect[key] = nextEndPhaseEffect[key]!!
        }
        nextEndPhaseEffect.clear()
    }

    suspend fun endPhaseEffectProcess(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())

        nowPlayer.usedCardReturn(this)
        for(card in nowPlayer.enchantmentCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_END_PHASE_YOUR)
        }
        for(card in nowPlayer.usedSpecialCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_END_PHASE_YOUR)
        }
        for(card in nowPlayer.discard){
            card.effectText(player, this, null, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD)
        }

        nowPlayer.megamiCard?.effectAllValidEffect(player, this, TextEffectTag.WHEN_END_PHASE_YOUR)
        nowPlayer.megamiCard2?.effectAllValidEffect(player, this, TextEffectTag.WHEN_END_PHASE_YOUR)
        if(player1.canIdeaProcess){
            player1.ideaCard?.ideaProcess(PlayerEnum.PLAYER1, this, player1.isIdeaCardFlipped, null)
        }
        if(player2.canIdeaProcess){
            player2.ideaCard?.ideaProcess(PlayerEnum.PLAYER2, this, player2.isIdeaCardFlipped, null)
        }

        for(card in otherPlayer.enchantmentCard.values){
            card.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_END_PHASE_OTHER)
        }

        val keys = endPhaseEffect.keys.toMutableList()
        if(keys.isNotEmpty()){
            while(keys.size >= 2){
                val selected = receiveCardEffectOrder(getSocket(player), CommandEnum.SELECT_END_PHASE_EFFECT_ORDER, keys)
                if(selected in keys){
                    val result = endPhaseEffect[selected]
                    when(result!!.first){
                        CardEffectLocation.RETURN_YOUR -> {
                            returnSpecialCard(player, selected)
                        }
                        CardEffectLocation.ENCHANTMENT_OTHER -> {
                            result.second!!.effect!!(selected, player.opposite(), this, null)
                        }
                        CardEffectLocation.IDEA_PLAYER1, CardEffectLocation.TEMP_PLAYER1 -> {
                            result.second!!.effect!!(selected, PlayerEnum.PLAYER1, this, null)
                        }
                        CardEffectLocation.IDEA_PLAYER2, CardEffectLocation.TEMP_PLAYER2 -> {
                            result.second!!.effect!!(selected, PlayerEnum.PLAYER2, this, null)
                        }
                        else -> {
                            result.second!!.effect!!(selected, player, this, null)
                        }
                    }
                    endPhaseEffect.remove(selected)
                    keys.remove(selected)
                }

                if(endCurrentPhase){
                    cleanEndPhaseEffect()
                    return
                }
            }

            if(keys.size == 1){
                val lastEffect = endPhaseEffect[keys[0]]
                when(lastEffect!!.first){
                    CardEffectLocation.RETURN_YOUR -> {
                        returnSpecialCard(player, keys[0])
                    }
                    CardEffectLocation.ENCHANTMENT_OTHER -> {
                        lastEffect.second!!.effect!!(keys[0], player.opposite(), this, null)
                    }
                    CardEffectLocation.IDEA_PLAYER1, CardEffectLocation.TEMP_PLAYER1 -> {
                        lastEffect.second!!.effect!!(keys[0], PlayerEnum.PLAYER1, this, null)
                    }
                    CardEffectLocation.IDEA_PLAYER2, CardEffectLocation.TEMP_PLAYER2 -> {
                        lastEffect.second!!.effect!!(keys[0], PlayerEnum.PLAYER2, this, null)
                    }
                    else -> {
                        lastEffect.second!!.effect!!(keys[0], player, this, null)
                    }
                }
                endPhaseEffect.remove(keys[0])
                keys.remove(keys[0])
            }
        }

        for(key in nextEndPhaseEffect.keys){
            endPhaseEffect[key] = nextEndPhaseEffect[key]!!
        }

        nextEndPhaseEffect.clear()
    }

    suspend fun endPhaseEffectProcess2(){
        if(player1.isNextTurnTailWind || player1.divingSuccess){
            player1.isThisTurnTailWind = true
            sendSimpleCommand(player1_socket, player2_socket, CommandEnum.SET_TAIL_WIND_YOUR)
            player1.divingSuccess = false
        }
        else{
            player1.isThisTurnTailWind = false
            sendSimpleCommand(player1_socket, player2_socket, CommandEnum.SET_HEAD_WIND_YOUR)
        }

        if(player2.isNextTurnTailWind || player2.divingSuccess){
            player2.isThisTurnTailWind = true
            sendSimpleCommand(player2_socket, player1_socket, CommandEnum.SET_TAIL_WIND_YOUR)
            player2.divingSuccess = false
        }
        else{
            player2.isThisTurnTailWind = false
            sendSimpleCommand(player2_socket, player1_socket, CommandEnum.SET_HEAD_WIND_YOUR)
        }
        player1.isNextTurnTailWind = true; player2.isNextTurnTailWind = true

        thisTurnSwellDistance = 2; thisTurnDistance = distanceToken
        player1.didBasicOperation = false; player2.didBasicOperation = false
        player1.canNotGoForward = false; player2.canNotGoForward = false
        player1.rangeBuff.clearBuff(); player2.rangeBuff.clearBuff(); player1.attackBuff.clearBuff(); player2.attackBuff.clearBuff()
        player1.otherBuff.clearBuff(); player2.otherBuff.clearBuff()
        player1.lastTurnReact = player1.thisTurnReact; player2.lastTurnReact = player2.thisTurnReact
        player1.thisTurnReact = false; player2.thisTurnReact = false
        thisTurnDistanceChange = false
        player1.asuraUsed = false; player2.asuraUsed = false
        player1.nextEnchantmentGrowing = 0; player2.nextEnchantmentGrowing = 0
        player1.isNextBasicOperationInvalid = false; player2.isNextBasicOperationInvalid = false
        player1.isMoveDistanceToken = false; player2.isMoveDistanceToken = false
        player1.beforeTurnIdeaProcess = player1.ideaProcess; player2.beforeTurnIdeaProcess = player2.ideaProcess
        player1.tempIdeaProcess = false; player2.tempIdeaProcess = false
        player1.canIdeaProcess = true; player2.canIdeaProcess = true
        player1.ideaProcess = false; player2.ideaProcess = false
        player1.nextCostAddMegami = null; player2.nextCostAddMegami = null
        player1.fullAction = false; player2.fullAction = false
        for(i in 0..5){
            perjuryCheck[i] = false
        }
    }

    //0 = player don't want using card more || 1 = player card use success || 2 = cannot use because there are no installation card
    suspend fun useInstallationOnce(player: PlayerEnum): Int{
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val list = nowPlayer.getInstallationCard()
        if(list.isEmpty()) return 2

        while(true){
            val receive = receiveSelectCard(nowSocket, list, CommandEnum.SELECT_CARD_REASON_INSTALLATION, -1)?: continue
            if (receive.size == 1){
                val card = nowPlayer.getCardFromCover(receive[0])?: continue
                if (useCardFrom(player, card, LocationEnum.COVER_CARD, false, null,
                        isCost = true, isConsume = true)) {
                    break
                }
            }
            else if(receive.isEmpty()) return 0
        }
        return 1
    }

    suspend fun installationProcess(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        if(nowPlayer.getInstallationCard().isEmpty()) return
        if(nowPlayer.infiniteInstallationCheck()){
            while(true){
                when(useInstallationOnce(player)){
                    0 -> break
                    2 -> break
                    else -> {}
                }
            }
        }
        else{
            useInstallationOnce(player)
        }
    }

    private fun getQuickChangeCard(player: PlayerEnum): MutableList<Card>?{
        fun getQuickChangeCard(quickChange: Int): MutableList<Card>{
            val result = mutableListOf<Card>()
            player1.sealInformation[quickChange]?.let {
                for(cardNumber in it){
                    player1.sealZone[cardNumber]?.let { transform ->
                        result.add(transform)
                    }?: player2.sealZone[cardNumber]?.let { transform ->
                        result.add(transform)
                    }
                }
            }
            player2.sealInformation[quickChange]?.let {
                for(cardNumber in it){
                    player1.sealZone[cardNumber]?.let { transform ->
                        result.add(transform)
                    }?: player2.sealZone[cardNumber]?.let { transform ->
                        result.add(transform)
                    }
                }
            }
            println("quickchange: ${result[0]}")
            return result
        }

        for(card in getPlayer(player).enchantmentCard.values){
            card.card_data.effect?.let {
                for (text in it){
                    if(text.tag == TextEffectTag.ACTIVE_TRANSFORM_BELOW_THIS_CARD){
                        return getQuickChangeCard(card.card_number)
                    }
                }
            }
        }
        return null
    }

    suspend fun deckReconstruct(player: PlayerEnum, damage: Boolean){
        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        for(card in getPlayer(player).usedSpecialCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR)
        }

        installationProcess(player)

        if(endCurrentPhase){
            return
        }

        sendDeckReconstruct(nowSocket, otherSocket)

        if(damage){
            var damageCancel = false
            for(card in getPlayer(player).usedSpecialCard.values){
                if (card.effectAllValidEffect(player, this, TextEffectTag.WHEN_GET_DAMAGE_BY_DECK_RECONSTRUCT) == 1){
                    damageCancel = true
                    break
                }
            }
            if(!damageCancel){
                processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), true, null, null,
                    Log.DECK_RECONSTRUCT_DAMAGE)
                logger.insert(Log(player, LogText.END_EFFECT, Log.DECK_RECONSTRUCT_DAMAGE, -1))
            }
        }
        Card.cardReconstructInsert(nowPlayer.discard, nowPlayer.cover_card, nowPlayer.normalCardDeck)

        //TODO("change this mechanism like endphaseeffectprocess(can choose order of effect)")
        val reconstructListener = getImmediateReconstructListener(player)
        if(!reconstructListener.isEmpty()){
            for(i in 1..reconstructListener.size){
                if(reconstructListener.isEmpty()) break
                val now = reconstructListener.first()
                reconstructListener.removeFirst()
                if(!(now.doAction(this, -1, -1, false, false))){
                    reconstructListener.addLast(now)
                }
            }
        }

        for(card in getPlayer(player.opposite()).transformZone.values){
            card.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_DECK_RECONSTRUCT_OTHER)
        }

        getQuickChangeCard(player.opposite())?.let {transformList ->
            for(transform in transformList){
                transform.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_DECK_RECONSTRUCT_OTHER)
            }
        }
    }

    private fun checkFullPowerCanUse(player: PlayerEnum, card: Card): Boolean{
        return if(card.card_data.sub_type == SubType.FULL_POWER){
            getFullAction(player)
        } else{
            true
        }
    }

    suspend fun cardUseNormal(player: PlayerEnum, commandEnum: CommandEnum, card_number: Int): Boolean{
        if(card_number == -1){
            return false
        }

        when(commandEnum){
            CommandEnum.ACTION_USE_CARD_HAND -> {
                val card = getCardFrom(player, card_number, LocationEnum.HAND)?: return false
                if(!checkFullPowerCanUse(player, card)) return false
                if(useCardFrom(player, card, LocationEnum.HAND, false, null,
                        isCost = true, isConsume = true)) return true
            }
            CommandEnum.ACTION_USE_CARD_SPECIAL -> {
                val card = getCardFrom(player, card_number, LocationEnum.SPECIAL_CARD)?: return false
                if(!checkFullPowerCanUse(player, card)) return false
                if(useCardFrom(player, card, LocationEnum.SPECIAL_CARD, false, null,
                        isCost = true, isConsume = true)) return true
            }
            CommandEnum.ACTION_USE_CARD_COVER -> {
                val card = getCardFrom(player, card_number, LocationEnum.COVER_CARD)?: return false
                if(!checkFullPowerCanUse(player, card)) return false
                if(card.canUseAtCover()){
                    if(useCardFrom(player, card, LocationEnum.COVER_CARD, false, null,
                            isCost = true, isConsume = true)) {
                        return true
                    }
                }
            }
            CommandEnum.ACTION_USE_CARD_SOLDIER -> {
                val card = getCardFrom(player, card_number, LocationEnum.READY_SOLDIER_ZONE)?: return false
                if(!checkFullPowerCanUse(player, card)) return false
                if(useCardFrom(player, card, LocationEnum.READY_SOLDIER_ZONE, false, null,
                        isCost = true, isConsume = true)) {
                    return true
                }
            }
            //
            CommandEnum.ACTION_USE_CARD_PERJURY -> {
                if(getPlayer(player).megamiOne == MegamiEnum.RENRI || getPlayer(player).megamiTwo == MegamiEnum.RENRI){
                    val realCardNumber = card_number / 100000
                    val perjuryCardNumber = card_number % 100000
                    getCardFrom(player, realCardNumber, LocationEnum.HAND)?.let {
                        if(!(realCardNumber.isPoison() && realCardNumber.isSoldier())){
                            return useCardPerjury(player, it, perjuryCardNumber, LocationEnum.HAND)
                        }
                    }?: getCardFrom(player, realCardNumber, LocationEnum.READY_SOLDIER_ZONE)?.let {
                        if(!(realCardNumber.isPoison() && realCardNumber.isSoldier())){
                            return useCardPerjury(player, it, perjuryCardNumber, LocationEnum.READY_SOLDIER_ZONE)
                        }
                    }
                }
            }
            else -> return false
        }
        return false
    }

    suspend fun basicOperationCost(player: PlayerEnum, card_number: Int): Boolean{
        val nowPlayer = getPlayer(player)

        if(card_number == -1){
            return if(nowPlayer.concentration == 0) false
            else {
                decreaseConcentration(player)
                true
            }
        } else{
            val card = nowPlayer.getCardFromHand(card_number)?: return false
            if(!card.card_data.canCover) return false
            popCardFrom(player, card_number, LocationEnum.HAND, false)
            insertCardTo(player, card, LocationEnum.COVER_CARD, false)
            return true
        }
    }

    private fun basicOperationEnchantmentCheck(player: PlayerEnum, command: CommandEnum): Boolean{
        for(card in getPlayer(player.opposite()).enchantmentCard.values){
            if(card.operationForbidCheck(false, command)) return false
        }
        return true
    }

    suspend fun requestAndDoBasicOperation(player: PlayerEnum, card_number: Int): CommandEnum {
        while(true){
            val command = receiveBasicOperation(getSocket(player), card_number)
            if(command.isBasicOperation()){
                if(command == CommandEnum.SELECT_NOT){
                    return command
                }
                if(canDoBasicOperation(player, command)){
                    doBasicOperation(player, command, BASIC_OPERATION_CAUSE_BY_CARD + card_number)
                    return command
                }
            }
        }
    }

    suspend fun requestAndDoBasicOperation(player: PlayerEnum, card_number: Int, canNotSelect: HashSet<CommandEnum>):
        CommandEnum {
        while(true){
            val command = receiveBasicOperation(getSocket(player), card_number)
            if(command !in canNotSelect && command.isBasicOperation()){
                if(command == CommandEnum.SELECT_NOT){
                    return command
                }
                if(canDoBasicOperation(player, command)){
                    doBasicOperation(player, command, BASIC_OPERATION_CAUSE_BY_CARD + card_number)
                    return command
                }
            }
        }
    }

    suspend fun checkAdditionalBasicOperation(player: PlayerEnum, textTag: TextEffectTag): Boolean{
        for(card in getPlayer(player).usedSpecialCard.values){
            if(card.effectAllValidEffect(player, this, textTag) != 0) return true
        }
        return false
    }

    fun transformBasicOperationCheck(player: PlayerEnum, card_name: CardName) = getQuickChangeCard(player)?.let ret@{
        for(transform in it){
            if(transform.card_data.card_name == card_name){
                return@ret true
            }
        }
        false
    }?: false

    suspend fun canDoBasicOperation(player: PlayerEnum, command: CommandEnum): Boolean{
        val nowPlayer = getPlayer(player)
        if(nowPlayer.end_turn || endCurrentPhase){
            return false
        }
        for(transformCard in nowPlayer.transformZone.values){
            if(transformCard.effectAllValidEffect(player, this, TextEffectTag.FORBID_BASIC_OPERATION) != 0) return false
        }

        if(getQuickChangeCard(player)?.let ret@{
            for(transformCard in it){
                if(transformCard.effectAllValidEffect(player, this, TextEffectTag.FORBID_BASIC_OPERATION) != 0) return true
            }
                null
        }?: false){
            return false
        }

        return when(command){
            CommandEnum.ACTION_GO_FORWARD ->
                !(nowPlayer.aura + nowPlayer.freezeToken == nowPlayer.maxAura || distanceToken == 0 || thisTurnDistance <= getAdjustSwellDistance(player))
                        && !(getPlayer(player).canNotGoForward)
            CommandEnum.ACTION_GO_BACKWARD -> {
                !(nowPlayer.aura == 0 || distanceToken == 10) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_GO_BACKWARD)
            }
            CommandEnum.ACTION_WIND_AROUND -> !(dust == 0 || nowPlayer.aura + nowPlayer.freezeToken == nowPlayer.maxAura ||
                    checkAdditionalBasicOperation(player, TextEffectTag.CONDITION_ADD_DO_WIND_AROUND))
            CommandEnum.ACTION_INCUBATE -> (nowPlayer.aura != 0 || nowPlayer.freezeToken != 0) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_INCUBATE)
            CommandEnum.ACTION_BREAK_AWAY -> {
                !(dust == 0 || getAdjustDistance() > getAdjustSwellDistance(player) || distanceToken == 10) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_BREAK_AWAY)
            }
            CommandEnum.ACTION_YAKSHA -> {
                getPlayer(player).transformZone[CardName.FORM_YAKSHA] != null || transformBasicOperationCheck(player, CardName.FORM_YAKSHA)
            }
            CommandEnum.ACTION_NAGA -> {
                getPlayer(player).transformZone[CardName.FORM_NAGA] != null || transformBasicOperationCheck(player, CardName.FORM_NAGA)
            }
            CommandEnum.ACTION_GARUDA -> {
                getPlayer(player).transformZone[CardName.FORM_GARUDA] != null || transformBasicOperationCheck(player, CardName.FORM_GARUDA)
            }
            CommandEnum.ACTION_ASURA -> {
                (getPlayer(player).transformZone[CardName.FORM_ASURA] != null || transformBasicOperationCheck(player, CardName.FORM_ASURA))
                        && !getPlayer(player).asuraUsed
            }
            else -> false
        }
    }

    suspend fun doBasicOperation(player: PlayerEnum, command: CommandEnum, card: Int){
        val nowPlayer = getPlayer(player)
        nowPlayer.didBasicOperation = true
        if(nowPlayer.isNextBasicOperationInvalid){
            nowPlayer.isNextBasicOperationInvalid = false
            return
        }
        when(command){
            CommandEnum.ACTION_GO_FORWARD -> doGoForward(player, card)
            CommandEnum.ACTION_GO_BACKWARD -> doGoBackward(player, card)
            CommandEnum.ACTION_WIND_AROUND -> doWindAround(player, card)
            CommandEnum.ACTION_INCUBATE -> doIncubate(player, card)
            CommandEnum.ACTION_BREAK_AWAY -> doBreakAway(player, card)
            CommandEnum.ACTION_YAKSHA -> doYaksha(player, card)
            CommandEnum.ACTION_NAGA -> doNaga(player, card)
            CommandEnum.ACTION_GARUDA -> doGaruda(player, card)
            CommandEnum.ACTION_ASURA -> doAsura(player, card)
            else -> {}
        }
    }

    private suspend fun doAsura(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_ASURA)){
            sendDoBasicAction(getSocket(player), getSocket(player.opposite()), CommandEnum.ACTION_ASURA_YOUR, card)
            if(addPreAttackZone(player, MadeAttack(CardName.FORM_ASURA, NUMBER_FORM_ASURA, CardClass.NULL,
                    sortedSetOf(3, 5), 3,  2, MegamiEnum.THALLYA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ).addTextAndReturn(CardSet.attackAsuraText), null) ){
                afterMakeAttack(NUMBER_FORM_ASURA, player, null)
            }
            getPlayer(player).asuraUsed = true
        }
    }

    private suspend fun doYaksha(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_YAKSHA)){
            sendDoBasicAction(getSocket(player), getSocket(player.opposite()), CommandEnum.ACTION_YAKSHA_YOUR, card)
            if(addPreAttackZone(player, MadeAttack(CardName.FORM_YAKSHA, NUMBER_FORM_YAKSHA, CardClass.NULL,
                    sortedSetOf(2, 4, 6, 8), 1,  1, MegamiEnum.THALLYA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ).addTextAndReturn(CardSet.attackYakshaText), null) ){
                afterMakeAttack(NUMBER_FORM_YAKSHA, player, null)
            }
        }
    }

    private suspend fun doNaga(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_NAGA)){
            sendDoBasicAction(getSocket(player), getSocket(player.opposite()), CommandEnum.ACTION_NAGA_YOUR, card)
            popCardFrom(player.opposite(), -1, LocationEnum.YOUR_DECK_TOP, true)?.let {
                insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
            }
        }
    }

    private suspend fun doGaruda(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_GARUDA)){
                sendDoBasicAction(getSocket(player), getSocket(player.opposite()), CommandEnum.ACTION_GARUDA_YOUR, card)
                dustToDistance(1, Arrow.ONE_DIRECTION, player, player, Log.BASIC_OPERATION)
                distanceListenerProcess(PlayerEnum.PLAYER1)
                distanceListenerProcess(PlayerEnum.PLAYER2)
            }
        }

    private suspend fun doGoForward(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_GO_FORWARD)){
            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_GO_FORWARD_YOUR, card)
            distanceToAura(player, 1, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, Log.BASIC_OPERATION)
            for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                enchantmentCard.effectAllValidEffect(enchantmentCard.card_number,
                    player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
            }
        }


    }

    //this 5 function must call after check when select
    private suspend fun doGoBackward(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_GO_BACKWARD)){
            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_GO_BACKWARD_YOUR, card)
            auraToDistance(player, 1 , Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, Log.BASIC_OPERATION)
            for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                enchantmentCard.effectAllValidEffect(enchantmentCard.card_number,
                    player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
            }
        }
    }

    //this 5 function must call after check when select
    private suspend fun doWindAround(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_WIND_AROUND)){
            var additionalCheck = 0
            for(usedCard in getPlayer(player).usedSpecialCard.values){
                additionalCheck += usedCard.effectAllValidEffect(player, this, TextEffectTag.WHEN_DO_WIND_AROUND)
                if(additionalCheck != 0) {
                    break
                }
            }
            if(additionalCheck != 0) return

            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_WIND_AROUND_YOUR, card)
            dustToAura(player, 1, Arrow.NULL, PlayerEnum.PLAYER1 , PlayerEnum.PLAYER2, Log.BASIC_OPERATION)
            for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                enchantmentCard.effectAllValidEffect(enchantmentCard.card_number,
                    player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
            }
        }
    }

    //this 5 function must call after check when select
    private suspend fun doIncubate(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_INCUBATE)){
            val nowPlayer = getPlayer(player)

            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_INCUBATE_YOUR, card)
            if(nowPlayer.freezeToken >= 1){
                nowPlayer.freezeToken -= 1
                sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.FREEZE_TOKEN,
                    LocationEnum.YOUR_AURA, LocationEnum.OUT_OF_GAME, 1, -1)
            }
            else{
                auraToFlare(player, player, 1, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, Log.BASIC_OPERATION)
                for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                    enchantmentCard.effectAllValidEffect(enchantmentCard.card_number,
                        player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
                }
            }
        }
    }

    //this 5 function must call after check when select
    private suspend fun doBreakAway(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_BREAK_AWAY)){
            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_BREAK_AWAY_YOUR, card)
            thisTurnDistanceChange = true
            dust -= 1
            distanceToken += 1
            thisTurnDistance += 1
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.DISTANCE, 1, -1)
            distanceListenerProcess(PlayerEnum.PLAYER1)
            distanceListenerProcess(PlayerEnum.PLAYER2)
        }
    }

    suspend fun coverCard(player: PlayerEnum, select_player: PlayerEnum, reason: Int){
        val nowSocket = getSocket(select_player)

        while(true){
            val list = mutableListOf<Int>()
            for(card in getPlayer(player).hand.values){
                if(card.card_data.canCover) list.add(card.card_number)
            }
            if(list.size == 0) break
            val cardNumber = receiveCoverCardSelect(nowSocket, list, reason)
            if(getCardFrom(player, cardNumber, LocationEnum.HAND)?.card_data?.canCover == true){
                val card = popCardFrom(player, cardNumber, LocationEnum.HAND, false)
                if(card == null) continue
                else insertCardTo(player, card, LocationEnum.COVER_CARD, false)
            }
            break
        }
    }

    suspend fun endTurnHandCheck(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        while (true){
            if(nowPlayer.hand.size <= nowPlayer.maxHand) {
                nowPlayer.maxHand = 2
                return
            }
            coverCard(player, player, 0)
        }

    }

    //select_player -> player who select card ||| player -> victim ||| function that select card in list(list check is not needed)
    suspend fun selectCardFrom(player: PlayerEnum, select_player: PlayerEnum, user: PlayerEnum, location_list: List<LocationEnum>,
                               reason: CommandEnum, card_number: Int, condition: suspend (Card, LocationEnum) -> Boolean): MutableList<Int>?{
        val cardList = mutableListOf<Int>()
        val searchPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())

        for (location in location_list){
            when(location){
                LocationEnum.HAND -> {
                    if(user != player){
                        searchPlayer.insertCardNumberPlusCondition(location, cardList, condition) {
                                card -> !(card.isSoftAttack)
                        }
                    }
                    else{
                        searchPlayer.insertCardNumber(location, cardList, condition)
                    }
                }
                LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                    otherPlayer.insertCardNumber(location, cardList, condition)
                }
                else -> {
                    searchPlayer.insertCardNumber(location, cardList, condition)
                }
            }
        }

        if(cardList.isEmpty()) return null

        while (true){
            return receiveSelectCard(getSocket(select_player), cardList, reason, card_number) ?: continue
        }
    }

    //use this function player cannot select card select number
    suspend fun selectCardFrom(player: PlayerEnum, select_player: PlayerEnum, user: PlayerEnum, location_list: List<LocationEnum>,
                               reason: CommandEnum, card_number: Int, listSize: Int, condition: suspend (Card, LocationEnum) -> Boolean): MutableList<Int>?{
        val cardList = mutableListOf<Int>()
        val searchPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())

        for (location in location_list){
            when(location){
                LocationEnum.HAND -> {
                    if(user != player){
                        searchPlayer.insertCardNumberPlusCondition(location, cardList, condition) {
                                card -> !(card.isSoftAttack)
                        }
                    }
                    else{
                        searchPlayer.insertCardNumber(location, cardList, condition)
                    }
                }
                LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                    otherPlayer.insertCardNumber(location, cardList, condition)
                }
                else -> {
                    searchPlayer.insertCardNumber(location, cardList, condition)
                }
            }
        }

        if(cardList.isEmpty()) return null
        else if(cardList.size < listSize) return cardList

        while (true){
            val set = mutableSetOf<Int>()
            val list = receiveSelectCard(getSocket(select_player), cardList, reason, card_number) ?: continue
            set.addAll(list)
            if(set.size == listSize) return list
        }
    }

    suspend fun selectCardFrom(player: PlayerEnum, cardList: MutableList<Int>, reason: CommandEnum, card_number: Int,
        listSize: Int): MutableList<Int>{
        if(cardList.size <= listSize) return cardList
        while (true){
            val set = mutableSetOf<Int>()
            val list = receiveSelectCard(getSocket(player), cardList, reason, card_number) ?: continue
            set.addAll(list)
            if(set.size == listSize) return list
        }
    }

    suspend fun selectCardFrom(player: PlayerEnum, cardList: MutableList<Int>, reason: CommandEnum, card_number: Int): MutableList<Int>{
        while (true) {
            return receiveSelectCard(getSocket(player), cardList, reason, card_number) ?: continue
        }
    }

    suspend fun popCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum, public: Boolean,
                            discardCheck: Boolean = true): Card?{
        val nowPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())
        when(location){
            LocationEnum.COVER_CARD -> for(card in nowPlayer.cover_card) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_COVER_YOUR)
                nowPlayer.cover_card.remove(card)
                return card
            }
            LocationEnum.DISCARD_YOUR -> for(card in nowPlayer.discard) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_DISCARD_YOUR)
                nowPlayer.discard.remove(card)
                if(discardCheck){
                    for(transformCard in otherPlayer.transformZone.values){
                        transformCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                    }
                    getQuickChangeCard(player.opposite())?.let {
                        for (transformCard in it){
                            transformCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                        }
                    }
                }
                return card
            }
            LocationEnum.DISCARD_OTHER -> for(card in otherPlayer.discard) if (card.card_number == card_number){
                sendPopCardZone(otherSocket, nowSocket, card_number, public, CommandEnum.POP_DISCARD_YOUR)
                otherPlayer.discard.remove(card)
                if(discardCheck){
                    for(transformCard in nowPlayer.transformZone.values){
                        transformCard.effectAllValidEffect(player, this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                    }
                    getQuickChangeCard(player)?.let {
                        for (transformCard in it){
                            transformCard.effectAllValidEffect(player, this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                        }
                    }
                }
                return card
            }
            LocationEnum.DECK -> for(card in nowPlayer.normalCardDeck) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_DECK_YOUR)
                nowPlayer.normalCardDeck.remove(card)
                return card
            }
            LocationEnum.HAND -> {
                if(card_number in nowPlayer.hand){
                    sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_HAND_YOUR)
                    val result = nowPlayer.hand[card_number]
                    nowPlayer.hand.remove(card_number)
                    return result
                }
            }
            LocationEnum.HAND_OTHER -> {
                if(card_number in getPlayer(player.opposite()).hand){
                    sendPopCardZone(otherSocket, nowSocket, card_number, public, CommandEnum.POP_HAND_YOUR)
                    val result = getPlayer(player.opposite()).hand[card_number]
                    getPlayer(player.opposite()).hand.remove(card_number)
                    return result
                }
            }
            LocationEnum.SPECIAL_CARD -> {
                val result = nowPlayer.special_card_deck[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_SPECIAL_YOUR)
                nowPlayer.special_card_deck.remove(card_number)
                return result
            }
            LocationEnum.YOUR_USED_CARD -> {
                val result = nowPlayer.usedSpecialCard[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_USED_YOUR)
                nowPlayer.usedSpecialCard.remove(card_number)
                return result
            }
            LocationEnum.PLAYING_ZONE_YOUR -> {
                for(card in nowPlayer.usingCard) if (card.card_number == card_number) {
                    sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_PLAYING_YOUR)
                    nowPlayer.usingCard.remove(card)
                    return card
                }
            }
            LocationEnum.ENCHANTMENT_ZONE -> {
                val result = nowPlayer.enchantmentCard[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, result.card_number, public, CommandEnum.POP_ENCHANTMENT_YOUR)
                nowPlayer.enchantmentCard.remove(card_number)
                return result
            }
            LocationEnum.YOUR_DECK_TOP -> {
                if(nowPlayer.normalCardDeck.isEmpty()) return null
                val result = nowPlayer.normalCardDeck.first()
                sendPopCardZone(nowSocket, otherSocket, result.card_number, public, CommandEnum.POP_DECK_YOUR)
                nowPlayer.normalCardDeck.removeFirst()
                return result
            }
            LocationEnum.SEAL_ZONE -> {
                val result = nowPlayer.sealZone[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_SEAL_YOUR)
                nowPlayer.sealZone.remove(card_number)
                return result
            }
            LocationEnum.POISON_BAG -> {
                val result = nowPlayer.poisonBag[card_number.toCardName()]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_POISON_BAG_YOUR)
                nowPlayer.poisonBag.remove(card_number.toCardName())
                return result
            }
            LocationEnum.NOT_READY_SOLDIER_ZONE -> {
                val result = nowPlayer.notReadySoldierZone[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_NOT_READY_SOLDIER_ZONE_YOUR)
                nowPlayer.notReadySoldierZone.remove(card_number)
                return result
            }
            LocationEnum.READY_SOLDIER_ZONE -> {
                val result = nowPlayer.readySoldierZone[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_READY_SOLDIER_ZONE_YOUR)
                nowPlayer.readySoldierZone.remove(card_number)
                return result
            }
            LocationEnum.IDEA_YOUR -> {
                val result = nowPlayer.ideaCard?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_IDEA_YOUR)
                nowPlayer.ideaCard = null
                return result
            }
            LocationEnum.END_IDEA_YOUR -> {
                val result = nowPlayer.endIdeaCards[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_END_IDEA_YOUR)
                nowPlayer.endIdeaCards.remove(card_number)
                return result
            }
            LocationEnum.MEMORY_YOUR -> {
                val result = nowPlayer.memory?.get(card_number)?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_MEMORY_YOUR)
                nowPlayer.memory?.remove(card_number)
                return result
            }
            else -> throw Exception("location: $location not supported")
        }
        return null
    }

    suspend fun popCardFrom(player: PlayerEnum, card_name: CardName, location: LocationEnum, public: Boolean): Card?{
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())
        when(location){
            LocationEnum.ADDITIONAL_CARD -> {
                val result = nowPlayer.additionalHand[card_name]?: return null
                sendPopCardZone(nowSocket, otherSocket, result.card_number, public, CommandEnum.POP_ADDITIONAL_YOUR)
                nowPlayer.additionalHand.remove(card_name)
                return result
            }
            else -> TODO()
        }
    }

    suspend fun insertCardTo(player: PlayerEnum, card: Card, location: LocationEnum, publicForOther: Boolean,
                             publicForYour: Boolean = true, discardCheck: Boolean = true){
        if(card.card_data.card_class == CardClass.SOLDIER){
            when(location){
                LocationEnum.NOT_READY_SOLDIER_ZONE, LocationEnum.READY_SOLDIER_ZONE, LocationEnum.PLAYING_ZONE_YOUR,
                LocationEnum.ENCHANTMENT_ZONE, LocationEnum.OUT_OF_GAME, LocationEnum.SEAL_ZONE -> {

                }
                else -> {
                    insertCardTo(player, card, LocationEnum.NOT_READY_SOLDIER_ZONE, true)
                    return
                }
            }
        }

        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())
        val cardOwner = getPlayer(card.player)
        val cardOwnerSocket = getSocket(card.player)
        val cardOwnerOppositeSocket = getSocket(card.player.opposite())

        when(location){
            LocationEnum.YOUR_DECK_BELOW -> {
                cardOwner.normalCardDeck.addLast(card)
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.DECK_BELOW_YOUR, publicForYour)
            }
            LocationEnum.YOUR_DECK_TOP -> {
                cardOwner.normalCardDeck.addFirst(card)
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.DECK_TOP_YOUR, publicForYour)
            }
            LocationEnum.DISCARD_YOUR -> {
                cardOwner.discard.addFirst(card)
                if(discardCheck){
                    for(transformCard in getPlayer(player.opposite()).transformZone.values){
                        transformCard.effectAllValidEffect(player, this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                    }
                    getQuickChangeCard(player.opposite())?.let {
                        for (transformCard in it){
                            transformCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                        }
                    }
                }
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.DISCARD_CARD_YOUR, publicForYour)
            }
            LocationEnum.PLAYING_ZONE_YOUR -> {
                nowPlayer.usingCard.addFirst(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.PLAYING_CARD_YOUR, publicForYour)
            }
            LocationEnum.YOUR_USED_CARD -> {
                cardOwner.usedSpecialCard[card.card_number] = card
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.USED_CARD_YOUR, publicForYour)
            }
            LocationEnum.COVER_CARD -> {
                cardOwner.cover_card.addFirst(card)
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.COVER_CARD_YOUR, publicForYour)
            }
            LocationEnum.ENCHANTMENT_ZONE -> {
                nowPlayer.enchantmentCard[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.ENCHANTMENT_CARD_YOUR, publicForYour)
            }
            LocationEnum.SPECIAL_CARD -> {
                cardOwner.special_card_deck[card.card_number] = card
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.SPECIAL_YOUR, publicForYour)
            }
            LocationEnum.HAND -> {
                cardOwner.hand[card.card_number] = card
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.HAND_YOUR, publicForYour)
            }
            LocationEnum.SEAL_ZONE -> {
                nowPlayer.sealZone[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.SEAL_YOUR, publicForYour)
            }
            LocationEnum.POISON_BAG -> {
                val poisonOwner = getPlayer(card.player.opposite())
                val poisonOwnerSocket = getSocket(card.player.opposite())
                val poisonOwnerOppositeSocket = getSocket(card.player)
                poisonOwner.poisonBag[card.card_data.card_name] = card
                sendAddCardZone(poisonOwnerSocket, poisonOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.POISON_BAG_YOUR, publicForYour)
            }
            LocationEnum.OUT_OF_GAME -> {
                cardOwner.outOfGame[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.OUT_OF_GAME_YOUR, publicForYour)
            }
            LocationEnum.TRANSFORM -> {
                nowPlayer.transformZone[card.card_data.card_name] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.TRANSFORM_YOUR, publicForYour)
            }
            LocationEnum.ADDITIONAL_CARD -> {
                nowPlayer.additionalHand[card.card_data.card_name] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.ADDITIONAL_YOUR, publicForYour)
            }
            LocationEnum.NOT_READY_SOLDIER_ZONE -> {
                nowPlayer.notReadySoldierZone[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.NOT_READY_SOLDIER_ZONE_YOUR, publicForYour)
            }
            LocationEnum.READY_SOLDIER_ZONE -> {
                nowPlayer.readySoldierZone[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.READY_SOLDIER_ZONE_YOUR, publicForYour)
            }
            LocationEnum.IDEA_YOUR -> {
                nowPlayer.ideaCard = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.IDEA_YOUR, publicForYour)
            }
            LocationEnum.END_IDEA_YOUR -> {
                nowPlayer.endIdeaCards[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.END_IDEA_YOUR, publicForYour)
            }
            LocationEnum.ANVIL_YOUR -> {
                nowPlayer.anvil = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.ANVIL_YOUR, publicForYour)
            }
            LocationEnum.MEMORY_YOUR -> {
                nowPlayer.memory!![card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.MEMORY_YOUR, publicForYour)
            }
            else -> TODO()
        }
    }

    suspend fun showSome(show_player: PlayerEnum, command: CommandEnum, card_number: Int){
        val nowPlayer = getPlayer(show_player)
        val list = mutableListOf<Int>()
        when(command){
            CommandEnum.SHOW_COVER_YOUR -> {
                for(card in nowPlayer.cover_card) list.add(card.card_number)
            }
            CommandEnum.SHOW_HAND_ALL_YOUR -> list.addAll(nowPlayer.hand.keys)
            CommandEnum.SHOW_HAND_YOUR -> list.add(card_number)
            CommandEnum.SHOW_SPECIAL_YOUR -> list.addAll(nowPlayer.special_card_deck.keys)
            CommandEnum.SHOW_DECK_TOP_YOUR -> list.add(card_number)
            else -> TODO()
        }
        sendShowInformation(command, getSocket(show_player), getSocket(show_player.opposite()), list)
    }

    /**
     when YOUR_DECK_TOP -> card_number means top nth card (0 means top)
     */
    fun getCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum): Card?{
        return when(location){
            LocationEnum.HAND -> getPlayer(player).getCardFromHand(card_number)
            LocationEnum.COVER_CARD -> getPlayer(player).getCardFromCover(card_number)
            LocationEnum.DISCARD_YOUR -> getPlayer(player).getCardFromDiscard(card_number)
            LocationEnum.SPECIAL_CARD -> getPlayer(player).getCardFromSpecial(card_number)
            LocationEnum.YOUR_DECK_TOP -> getPlayer(player).getCardFromDeckTop(card_number)
            LocationEnum.PLAYING_ZONE_YOUR -> getPlayer(player).getCardFromPlaying(card_number)
            LocationEnum.ADDITIONAL_CARD -> getPlayer(player).getCardFromAdditional(card_number)
            LocationEnum.YOUR_USED_CARD -> getPlayer(player).getCardFromUsed(card_number)
            LocationEnum.ENCHANTMENT_ZONE, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD -> {
                getPlayer(player).getCardFromEnchantment(card_number)
            }
            LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                getPlayer(player.opposite()).getCardFromEnchantment(card_number)
            }
            LocationEnum.END_IDEA_YOUR -> {
                getPlayer(player).endIdeaCards[card_number]
            }
            LocationEnum.MEMORY_YOUR -> {
                getPlayer(player).memory?.get(card_number)
            }
            else -> throw Exception("location: $location not supported")
        }
    }

    fun getCardFrom(player: PlayerEnum, card_name: CardName, location: LocationEnum): Card?{
        return when(location){
            LocationEnum.ADDITIONAL_CARD -> getPlayer(player).getCardFromAdditional(card_name)
            else -> TODO()
        }
    }

    suspend fun returnSpecialCard(player: PlayerEnum, card_number: Int): Boolean{
        val card = popCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD, true)?: return false
        insertCardTo(player, card, LocationEnum.SPECIAL_CARD, true)
        card.effectAllValidEffect(player, this, TextEffectTag.WHEN_THIS_CARD_RETURN)
        card.special_card_state = SpecialCardEnum.UNUSED
        for(nowCard in getPlayer(player).usedSpecialCard.values){
            nowCard.effectAllValidEffect(player, this, TextEffectTag.WHEN_SPECIAL_RETURN_YOUR)
        }
        return true
    }

    //megami special function

    fun getUmbrella(player: PlayerEnum): Umbrella?{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.umbrella
            PlayerEnum.PLAYER2 -> player2.umbrella
        }
    }

    fun getUmbrellaListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1UmbrellaListener
            PlayerEnum.PLAYER2 -> player2UmbrellaListener
        }
    }

    suspend fun changeUmbrella(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.umbrella?.let {
            nowPlayer.umbrella = it.opposite()
            sendChangeUmbrella(getSocket(player), getSocket(player.opposite()))
            val umbrellaListener = getUmbrellaListener(player)
            if(!umbrellaListener.isEmpty()){
                for(i in 1..umbrellaListener.size){
                    if(umbrellaListener.isEmpty()) break
                    val now = umbrellaListener.first()
                    umbrellaListener.removeFirst()
                    if(!(now.doAction(this, -1, -1, false, false))){
                        umbrellaListener.addLast(now)
                    }
                }
            }
            for(card in nowPlayer.hand.values){
                card.checkWhenUmbrellaChange(player, this)
            }
        }
    }

    suspend fun getStratagem(player: PlayerEnum): Stratagem? {
        return when(player){
            PlayerEnum.PLAYER1 -> {
                if(player1.stratagem != null) sendGetStratagem(player1_socket, player2_socket, player1.stratagem!!)
                player1.stratagem
            }
            PlayerEnum.PLAYER2 -> {
                if(player2.stratagem != null) sendGetStratagem(player2_socket, player1_socket, player2.stratagem!!)
                player2.stratagem
            }
        }
    }

    suspend fun setStratagem(player: PlayerEnum, stratagem: Stratagem) {
        when(player){
            PlayerEnum.PLAYER1 -> {
                player1.stratagem = stratagem
                sendSetStratagem(player1_socket, player2_socket, stratagem)
            }
            PlayerEnum.PLAYER2 -> {
                player2.stratagem = stratagem
                sendSetStratagem(player2_socket, player1_socket, stratagem)
            }
        }
    }

    fun getCardOwner(card_number: Int): PlayerEnum{
        return if(card_number < 10000) first_turn
        else first_turn.opposite()
    }

    suspend fun moveAdditionalCard(player: PlayerEnum, card_name: CardName, location: LocationEnum): Card?{
        val card = popCardFrom(player, card_name, LocationEnum.ADDITIONAL_CARD, true)?.let {
            if(location == LocationEnum.YOUR_USED_CARD){
                it.special_card_state = SpecialCardEnum.PLAYED
            }
            insertCardTo(player, it, location, true)
            it
        } ?: return null

        val additionalListener = getAdditionalListener(player)
        if(!(additionalListener.isEmpty())){
            for(i in 1..additionalListener.size){
                if(additionalListener.isEmpty()) break
                val now = additionalListener.first()
                additionalListener.removeFirst()
                if(!(now.doAction(this, -1, -1, booleanPara1 = false, booleanPara2 = false))){
                    additionalListener.addLast(now)
                }
            }
        }

        return card
    }

    suspend fun moveOutCard(to_player: PlayerEnum, nameList: MutableList<CardName>, to_location: LocationEnum){
        for(card_name in nameList){
            Card.cardMakerByName(getPlayer(to_player).firstTurn, card_name, to_player).let {
                insertCardTo(to_player, it, to_location, false)
            }
        }

    }

    fun countToken(player: PlayerEnum, location: LocationEnum): Int{
        var count = 0
        when(location){
            LocationEnum.ENCHANTMENT_ZONE -> {
                for(card in getPlayer(player).enchantmentCard.values){
                    count += card.getNap()?: 0
                }
            }
            LocationEnum.YOUR_USED_CARD -> {
                for(card in getPlayer(player).usedSpecialCard.values){
                    count += card.getNap()?: 0
                }
            }
            else -> {
                TODO()
            }
        }
        return count
    }

    fun getTotalSeedNumber(player: PlayerEnum): Int{
        var result = 0
        for(card in getPlayer(player).enchantmentCard.values){
            result += card.getSeedToken()
        }
        return result
    }

    suspend fun processIdeaStage(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.ideaCardStage += 1
        sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_IDEA_STAGE_YOUR, nowPlayer.ideaCardStage)
    }

    suspend fun selectAct(player: PlayerEnum): Int{
        val nowPlayer = getPlayer(player)

        val list = if(nowPlayer.isIdeaCardFlipped){
            nowPlayer.nowAct?.getNextActTrial()
        } else nowPlayer.nowAct?.getNextAct()

        if(list == null || list.size == 0) return -1

        return receiveSelectAct(getSocket(player), list)
    }

    suspend fun setAct(player: PlayerEnum, act_number: Int){
        getPlayer(player).nowAct = StoryBoard.getActByNumber(act_number)
        sendCommand(player, player.opposite(), CommandEnum.SET_ACT_YOUR, act_number)
    }

    suspend fun sendCommand(player1: PlayerEnum, player2: PlayerEnum, command: CommandEnum){
        sendSimpleCommand(getSocket(player1), getSocket(player2), command)
    }

    suspend fun sendCommand(player1: PlayerEnum, player2: PlayerEnum, command: CommandEnum, data: Int){
        sendSimpleCommand(getSocket(player1), getSocket(player2), command, data)
    }


    fun getMirror(): Int {
        var count = 0
        if(player1.aura == player2.aura){
            count += 1
        }
        if(player1.flare == player2.flare){
            count += 1
        }
        if(player1.life == player2.life){
            count += 1
        }
        return count
    }

    suspend fun diving(player: PlayerEnum){
        if(getPlayer(player).forwardDiving != null){
            return
        }

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        sendSimpleCommand(nowSocket, otherSocket, CommandEnum.DIVING_YOUR)
        while(true){
            when(receiveSimpleCommand(nowSocket, CommandEnum.DIVING_REQUEST)){
                CommandEnum.SELECT_ONE -> {
                    getPlayer(player).forwardDiving = true
                    break
                }
                CommandEnum.SELECT_TWO -> {
                    getPlayer(player).forwardDiving = false
                    break
                }
                else -> {
                    continue
                }
            }
        }
    }

    suspend fun addMarketPrice(player: PlayerEnum){
        setMarketPrice(player, getPlayer(player).getMarketPrice() + 1)
    }

    suspend fun reduceMarketPrice(player: PlayerEnum){
        setMarketPrice(player, getPlayer(player).getMarketPrice() - 1)
    }

    suspend fun setMarketPrice(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)
        val value = if(number < 1) 1 else if(number > 4) 4 else number
        if(nowPlayer.marketPrice != null){
            if(nowPlayer.marketPrice != value){
                sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_MARKET_PRICE_YOUR, value)
            }
            nowPlayer.marketPrice = value
        }
    }

    //megami special function
}