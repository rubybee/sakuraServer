package com.sakurageto.gamelogic

import com.sakurageto.protocol.Connection
import com.sakurageto.protocol.RoomInformation
import com.sakurageto.card.*
import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.gamelogic.log.EventLog
import com.sakurageto.gamelogic.log.LogText
import com.sakurageto.gamelogic.log.GameLogger
import com.sakurageto.gamelogic.megamispecial.Stratagem
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import com.sakurageto.plugins.makeBugReportFile
import com.sakurageto.protocol.*
import com.sakurageto.protocol.CommandEnum.Companion.BASIC_OPERATION_CAUSE_BY_CARD
import com.sakurageto.protocol.TokenEnum.Companion.toLacerationLocation
import io.ktor.websocket.*

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus, private val player1_socket: Connection, private val player2_socket: Connection) {
    var perjuryCheck =
        arrayOf(false, false, false, false, false, false, false, false, false, false,
                false, false, false, false, false, false, false, false, false, false
    )
    companion object{
        const val START_PHASE = 1
        const val MAIN_PHASE = 2
        const val END_PHASE = 3

        val RENRI_FALSE_STAB = Card.cardMakerByName(true, CardName.RENRI_FALSE_STAB, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_7_2)
        val RENRI_TEMPORARY_EXPEDIENT = Card.cardMakerByName(true, CardName.RENRI_TEMPORARY_EXPEDIENT, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_7_2)
        val RENRI_BLACK_AND_WHITE = Card.cardMakerByName(true, CardName.RENRI_BLACK_AND_WHITE, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_7_2)
        val RENRI_FLOATING_CLOUDS = Card.cardMakerByName(true, CardName.RENRI_FLOATING_CLOUDS, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_7_2)
        val RENRI_FISHING = Card.cardMakerByName(true, CardName.RENRI_FISHING, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_7_2)
        val RENRI_FISHING_V8_2 = Card.cardMakerByName(true, CardName.RENRI_FISHING, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_8_2)
        val RENRI_FALSE_WEAPON = Card.cardMakerByName(true, CardName.RENRI_FALSE_WEAPON, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_8_2)
        val RENRI_ESSENCE_OF_BLADE = Card.cardMakerByName(true, CardName.RENRI_ESSENCE_OF_BLADE, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_8_2)
        val RENRI_FIRST_SAKURA_ORDER = Card.cardMakerByName(true, CardName.RENRI_FIRST_SAKURA_ORDER, PlayerEnum.PLAYER1,
            LocationEnum.OUT_OF_GAME, GameVersion.VERSION_8_2)

        fun getValidPerjuryCard(megamiEnum: MegamiEnum, card_number: Int, version: GameVersion): Card?{
            return when(card_number){
                NUMBER_RENRI_FALSE_STAB, NUMBER_RENRI_FALSE_STAB + SECOND_PLAYER_START_NUMBER-> {
                    RENRI_FALSE_STAB
                }
                NUMBER_RENRI_TEMPORARY_EXPEDIENT, NUMBER_RENRI_TEMPORARY_EXPEDIENT + SECOND_PLAYER_START_NUMBER -> {
                    if(megamiEnum == MegamiEnum.RENRI_A1){
                        null
                    }
                    else{
                        RENRI_TEMPORARY_EXPEDIENT
                    }
                }
                NUMBER_RENRI_BLACK_AND_WHITE, NUMBER_RENRI_BLACK_AND_WHITE + SECOND_PLAYER_START_NUMBER -> {
                    RENRI_BLACK_AND_WHITE
                }
                NUMBER_RENRI_FLOATING_CLOUDS, NUMBER_RENRI_FLOATING_CLOUDS + SECOND_PLAYER_START_NUMBER -> {
                    if(megamiEnum == MegamiEnum.RENRI_A1){
                        null
                    }
                    else{
                        RENRI_FLOATING_CLOUDS
                    }
                }
                NUMBER_RENRI_FISHING, NUMBER_RENRI_FISHING + SECOND_PLAYER_START_NUMBER -> {
                    if(version.isHigherThen(GameVersion.VERSION_8_1)){
                        RENRI_FISHING_V8_2
                    }
                    else{
                        RENRI_FISHING
                    }
                }
                NUMBER_RENRI_FALSE_WEAPON, NUMBER_RENRI_FALSE_WEAPON + SECOND_PLAYER_START_NUMBER -> {
                    if(megamiEnum == MegamiEnum.RENRI){
                        null
                    }
                    else{
                        RENRI_FALSE_WEAPON
                    }

                }
                NUMBER_RENRI_ESSENCE_OF_BLADE, NUMBER_RENRI_ESSENCE_OF_BLADE + SECOND_PLAYER_START_NUMBER -> {
                    if(megamiEnum == MegamiEnum.RENRI){
                        null
                    }
                    else{
                        RENRI_ESSENCE_OF_BLADE
                    }
                }
                NUMBER_RENRI_FIRST_SAKURA_ORDER, NUMBER_RENRI_FIRST_SAKURA_ORDER + SECOND_PLAYER_START_NUMBER -> {
                    if(megamiEnum == MegamiEnum.RENRI){
                        null
                    }
                    else{
                        RENRI_FIRST_SAKURA_ORDER
                    }
                }
                else -> null
            }
        }
    }

    var isThisTurnDoAction = false
    var version = GameVersion.VERSION_8_1

    var turnPlayer = PlayerEnum.PLAYER1

    val gameLogger = GameLogger()

    var endCurrentPhase: Boolean = false
    var nowPhase: Int = START_PHASE

    var startTurnDistance = 10
    var isThisTurnDistanceChange = false

    var thisTurnSwellDistance = 2

    var turnNumber = 0

    suspend fun start(){
        while(true){
            startPhase()
            mainPhase()
            endPhase()
            if(gameEnd) {
                break
            }
        }
    }

    suspend fun startPhase(){
        endCurrentPhase = false
        nowPhase = START_PHASE
        beforeStartPhaseEffectProcess()

        sendStartPhaseStart(getSocket(turnPlayer), getSocket(turnPlayer.opposite()))
        startPhaseEffectProcess()
        if(turnNumber == 0 || turnNumber == 1 || endCurrentPhase){
            return
        }
        startPhaseDefaultSecond()
    }
    suspend fun mainPhase(){
        endCurrentPhase = false
        nowPhase = MAIN_PHASE
        if(getPlayer(turnPlayer).nextMainPhaseSkip){
            getPlayer(turnPlayer).nextMainPhaseSkip = false
            return
        }

        sendMainPhaseStart(getSocket(turnPlayer), getSocket(turnPlayer.opposite()))
        mainPhaseEffectProcess()
        doMainPhaseAction()
        if(endCurrentPhase){
            return
        }
        mainPhaseEndProcess()
    }

    private suspend fun doMainPhaseAction(){
        if(receiveFullPowerRequest(getSocket(turnPlayer))){
            fullPowerAction()
        }
        else{
            normalAction()
        }
    }

    private suspend fun fullPowerAction(){
        setPlayerFullAction(turnPlayer, true)
        while (true){
            val data = receiveFullPowerActionRequest(getSocket(turnPlayer))
            if(data.first == CommandEnum.FULL_POWER_NO){
                sendSimpleCommand(getSocket(turnPlayer), CommandEnum.FULL_POWER_NO)
                normalAction()
                return
            }
            else if(data.first == CommandEnum.ACTION_END_TURN){
                return
            }
            else if(data.first.isUseCard()){
                if(cardUseNormal(turnPlayer, data.first, data.second)){
                    return
                }
            }
        }
    }

    private suspend fun normalAction(){
        setPlayerFullAction(turnPlayer, false)
        while (true){
            if(endCurrentPhase || getEndTurn(turnPlayer)){
                return
            }
            val data = receiveActionRequest(getSocket(turnPlayer))
            when(data.first){
                CommandEnum.ACTION_END_TURN -> {
                    return
                }
                CommandEnum.FULL_POWER_YES -> {
                    if(!isThisTurnDoAction){
                        sendSimpleCommand(getSocket(turnPlayer), CommandEnum.FULL_POWER_YES)
                        fullPowerAction()
                        return
                    }
                }
                else -> {
                    if(data.first.isUseCard()){
                        if(cardUseNormal(turnPlayer, data.first, data.second)){
                            isThisTurnDoAction = true
                        }
                    }
                    else if(data.first.isBasicOperation()){
                        if(canDoBasicOperation(turnPlayer, data.first) && payBasicOperationCost(turnPlayer, data.second)){
                            doBasicOperation(turnPlayer, data.first, if(data.second == -1) -1 else 0)
                            isThisTurnDoAction = true
                        }
                    }
                }
            }
        }
    }

    suspend fun endPhase(){
        endCurrentPhase = false
        nowPhase = END_PHASE

        sendEndPhaseStart(getSocket(turnPlayer), getSocket(turnPlayer.opposite()))
        endPhaseEffectProcess()
        resetTurnValue()
        turnNumber += 1

        if(endCurrentPhase){
            changeTurnPlayer()
            return
        }
        endTurnHandCheck()
        changeTurnPlayer()
    }

    suspend fun getAdjustSwellDistance(): Int{
        var nowSwellDistance = thisTurnSwellDistance

        for(card in player1.enchantmentCard.values) nowSwellDistance += card.effectAllValidEffect(PlayerEnum.PLAYER1, this, TextEffectTag.CHANGE_SWELL_DISTANCE)
        for(card in player2.enchantmentCard.values) nowSwellDistance += card.effectAllValidEffect(PlayerEnum.PLAYER2, this, TextEffectTag.CHANGE_SWELL_DISTANCE)
        for(card in player1.usedSpecialCard.values) nowSwellDistance += card.effectAllValidEffect(PlayerEnum.PLAYER1, this, TextEffectTag.CHANGE_SWELL_DISTANCE)
        for(card in player2.usedSpecialCard.values) nowSwellDistance += card.effectAllValidEffect(PlayerEnum.PLAYER2, this, TextEffectTag.CHANGE_SWELL_DISTANCE)

        return nowSwellDistance
    }

    fun getTokenDistance(): Int {
        var distance = distanceToken

        distance -= player1ArtificialTokenOn
        distance -= player2ArtificialTokenOn
        distance += player1ArtificialTokenOut
        distance += player2ArtificialTokenOut

        return distance
    }

    suspend fun getAdjustDistance(): Int{
        var distance = getTokenDistance()

        for(card in player1.enchantmentCard.values){
            distance += card.effectAllValidEffect(PlayerEnum.PLAYER1, this, TextEffectTag.CHANGE_DISTANCE)
        }
        for(card in player2.enchantmentCard.values){
            distance += card.effectAllValidEffect(PlayerEnum.PLAYER2, this, TextEffectTag.CHANGE_DISTANCE)
        }

        distance += thisTurnDistanceChangeValue

        if(distance < 0) distance = 0
        return distance
    }

    suspend fun addThisTurnDistance(value: Int){
        if(value == 0) return
        thisTurnDistanceChangeValue += value
        if(value > 0){
            sendSimpleCommand(player1_socket, player2_socket, CommandEnum.ADD_THIS_TURN_DISTANCE, value)
        }
        else{
            sendSimpleCommand(player1_socket, player2_socket, CommandEnum.REDUCE_THIS_TURN_DISTANCE, value * -1)
        }
        whenDistanceChange()
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
    var thisTurnDistanceChangeValue = 0
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

    var beforeDistance = -1

    suspend fun whenDistanceChange(){
        beforeDistance = getAdjustDistance()
        if(isThisTurnDistanceChange || startTurnDistance - beforeDistance != 0){
            isThisTurnDistanceChange = true
        }
        distanceListenerProcess(PlayerEnum.PLAYER1)
        distanceListenerProcess(PlayerEnum.PLAYER2)
    }

    //before call this function must check player have enough artificial token
    suspend fun addArtificialTokenAtDistance(player: PlayerEnum, on: Boolean, number: Int){
        when(player){
            PlayerEnum.PLAYER1 -> {
                if(player1.artificialToken == null) return
                if(on){
                    isThisTurnDistanceChange = true
                    player1.artificialToken = player1.artificialToken!! - number
                    player1ArtificialTokenOn += number
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
                else{
                    isThisTurnDistanceChange = true
                    player1.artificialToken = player1.artificialToken!! - number
                    player1ArtificialTokenOut += number
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(player2.artificialToken == null) return
                if(on){
                    isThisTurnDistanceChange = true
                    player2.artificialToken = player2.artificialToken!! - number
                    player2ArtificialTokenOn += number
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
                else{
                    isThisTurnDistanceChange = true
                    player2.artificialToken = player2.artificialToken!! - number
                    player2ArtificialTokenOut += number
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN,
                        LocationEnum.MACHINE_YOUR, LocationEnum.DISTANCE, number, -1)
                }
            }
        }
        whenDistanceChange()
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

    lateinit var firstTurnPlayer: PlayerEnum

    fun getPlayer(player: PlayerEnum): PlayerStatus{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    fun getSocket(player: PlayerEnum): Connection {
        return if(player ==  PlayerEnum.PLAYER1) player1_socket else player2_socket
    }

    private fun getPlayerTempAttackBuff(player: PlayerEnum): AttackBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1TempAttackBuff else player2TempAttackBuff
    }

    fun getPlayerAttackBuff(player: PlayerEnum): AttackBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1.attackBuff else player2.attackBuff
    }

    private fun getPlayerTempRangeBuff(player: PlayerEnum): RangeBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1TempRangeBuff else player2TempRangeBuff
    }

    fun getPlayerRangeBuff(player: PlayerEnum): RangeBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1.rangeBuff else player2.rangeBuff
    }

    private fun getPlayerTempOtherBuff(player: PlayerEnum): OtherBuffQueue{
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
            PlayerEnum.PLAYER1 -> player1.endTurn
            PlayerEnum.PLAYER2 -> player2.endTurn
        }
    }

    fun getPlayerHandSize(player: PlayerEnum): Int{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.hand.size
            PlayerEnum.PLAYER2 -> player2.hand.size
        }
    }

    fun getConcentrationValue(player: PlayerEnum): Int{
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
        turnPlayer = player
        firstTurnPlayer = player
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

    fun changeTurnPlayer(){
        turnPlayer = turnPlayer.opposite()
    }

    fun setEndTurn(player: PlayerEnum, turn: Boolean){
        when(player){
            PlayerEnum.PLAYER1 -> player1.endTurn = turn
            PlayerEnum.PLAYER2 -> player2.endTurn = turn
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

    fun removeImmediateReconstructListener(player: PlayerEnum, card_number: Int){
        when(player){
            PlayerEnum.PLAYER1 -> {
                player1ReconstructListener.removeIf {
                    it.cardNumber == card_number
                }
            }
            PlayerEnum.PLAYER2 -> {
                player2ReconstructListener.removeIf {
                    it.cardNumber == card_number
                }
            }
        }
    }

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

    private fun getTerminationListener(player: PlayerEnum): ArrayDeque<Listener>{
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

    private val player1DamageListener: ArrayDeque<Listener> = ArrayDeque()
    private val player2DamageListener: ArrayDeque<Listener> = ArrayDeque()

    private fun getDamageListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1DamageListener
            PlayerEnum.PLAYER2 -> player2DamageListener
        }
    }

    fun addDamageListener(player: PlayerEnum, listener: Listener) {
        when (player) {
            PlayerEnum.PLAYER1 -> player1DamageListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2DamageListener.addLast(listener)
        }
    }

    private suspend fun damageListenerProcess(player: PlayerEnum){
        val damageListener = getDamageListener(player)
        if(!damageListener.isEmpty()){
            for(i in 1..damageListener.size){
                if(damageListener.isEmpty()) break
                val now = damageListener.first()
                damageListener.removeFirst()
                if(!(now.doAction(this, -1, -1, booleanPara1 = false, booleanPara2 = false))){
                    damageListener.addLast(now)
                }
            }
        }
    }

    private suspend fun distanceListenerProcess(player: PlayerEnum){
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

    private suspend fun moveTokenCardToSome(player: PlayerEnum, place: Int, number: Int, card: Card, card_number: Int){
        if(place == 9) cardToDistance(player, number, card, card_number)
        else if(place == 0) cardToAura(player, number, card, card_number)
        else if(place == 2) cardToFlare(player, number, card, card_number)
        else if(place > 100){
            cardToCard(player, number, card, getPlayer(player).enchantmentCard[place]!!, card_number)
        }
    }

    //true means cannot move
    private suspend fun moveTokenCheckArrow(from: LocationEnum, to: LocationEnum): Boolean{
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
    private suspend fun auraListenerProcess(player: PlayerEnum, beforeFull: Boolean, afterFull: Boolean){
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

    private suspend fun lifeListenerProcess(player: PlayerEnum, reason: Int, before: Int, reconstruct: Boolean, damage: Boolean){
        val nowPlayer = getPlayer(player)
        when(player){
            PlayerEnum.PLAYER1 -> {
                if(!player1Listener.isEmpty()){
                    for(i in 1..player1Listener.size){
                        if(player1Listener.isEmpty()) break
                        val now = player1Listener.first()
                        player1Listener.removeFirst()
                        if(!(now.doAction(this, reason, before * 100 + nowPlayer.life, reconstruct, damage))){
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
                        if(!(now.doAction(this, reason, before * 100 + nowPlayer.life, reconstruct, damage))){
                            player2Listener.addLast(now)
                        }
                    }
                }
            }
        }
    }

    private suspend fun getBothDirection(player: PlayerEnum, locAndNumber: Int): Boolean{
        while(true){
            return when(receiveCardEffectSelect(player, locAndNumber, CommandEnum.SELECT_ARROW_DIRECTION)){
                CommandEnum.SELECT_ONE -> false
                CommandEnum.SELECT_TWO -> true
                else -> continue
            }
        }
    }

    private suspend fun bothDirectionCheck(reason: Int, player: PlayerEnum): Boolean{
        for(card in getPlayer(player).enchantmentCard.values){
            if(card.effectAllValidEffect(reason, player, this, TextEffectTag.CHANGE_ARROW_BOTH) != 0){
                return true
            }
        }
        return false
    }

    private suspend fun chasmProcess(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        nowPlayer.enchantmentCard.values.filter {card ->
            card.chasmCheck()
        }.forEach{ card ->
            enchantmentDestructionNotNormally(player, card)
        }
    }

    private suspend fun cardToDustCheck(player: PlayerEnum, number: Int, card: Card, startPhaseProcess: Boolean, card_number: Int): Boolean{
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
                    if(receiveData.size != 1){
                        continue
                    }
                    moveTokenCardToSome(player, receiveData[0], number, card, card_number)
                    break
                }
                false
            }
        }
    }

    private suspend fun checkWhenGetAura(player: PlayerEnum): Boolean{
        var result: Int
        for(card in getPlayer(player.opposite()).enchantmentCard.values){
            result = card.effectAllValidEffect(player.opposite(), this, TextEffectTag.FORBID_GET_AURA_OTHER)
            if(result != 0){
                return true
            }
        }
        return false
    }

    private suspend fun afterCheckWhenGetAura(player: PlayerEnum){
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
                    gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
                    return true
                }
            }
            2 -> {
                if(auraToFlow(player, 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
                    return true
                }
            }
            3 -> {
                if(flareToFlow(player, 1) > 0){
                    gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
                    return true
                }
            }
            4 -> {
                if(lifeToFlow(player, 1) > 0){
                    gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
                    return true
                }
            }
            else -> {
                return true
            }
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
                else if(auraToDust(player.opposite(), 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
            }
            3 -> {
                if(flareToAura(player.opposite(), player, 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
                else if(flareToDust(player.opposite(), 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
            }
            4 -> {
                if(lifeToAura(player.opposite(), player, 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
                else if(lifeToDust(player.opposite(), 1, Arrow.NULL, player, player, NUMBER_AKINA_AKINA) > 0){
                    return true
                }
            }
            else -> {}
        }
        return false
    }

    suspend fun dustToFlow(player: PlayerEnum, number: Int): Int{
        val nowPlayer = getPlayer(player)
        if(nowPlayer.flow == null) return 0

        var value = number

        if(dust < value){
            value = dust
        }

        val emptyPlace = 5 - nowPlayer.flow!!
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            dust -= value
            nowPlayer.flow = nowPlayer.flow!! + value

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.DUST, LocationEnum.FLOW_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.FLOW_YOUR, value, -1)
        }

        return value
    }

    suspend fun auraToFlow(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int): Int{
        val nowPlayer = getPlayer(player)
        if(nowPlayer.flow == null) return 0
        var value = number

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLOW.encode(number))){
            flowToAura(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
            return 0
        }

        val beforeFull = nowPlayer.checkAuraFull()
        if(nowPlayer.aura < value){
            value = nowPlayer.aura
        }

        val emptyPlace = 5 - nowPlayer.flow!!
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            nowPlayer.aura -= value
            nowPlayer.flow = nowPlayer.flow!! + value

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.AURA_YOUR, LocationEnum.FLOW_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.AURA_YOUR, LocationEnum.FLOW_YOUR, value, -1)

            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }

        return value
    }

    suspend fun flareToFlow(player: PlayerEnum, number: Int): Int{
        val nowPlayer = getPlayer(player)
        if(nowPlayer.flow == null) return 0
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }

        val emptyPlace = 5 - nowPlayer.flow!!
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            nowPlayer.flare -= value
            nowPlayer.flow = nowPlayer.flow!! + value

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.FLARE_YOUR, LocationEnum.FLOW_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.FLARE_YOUR, LocationEnum.FLOW_YOUR, value, -1)
        }

        return value
    }

    suspend fun lifeToFlow(player: PlayerEnum, number: Int): Int{
        val nowPlayer = getPlayer(player)
        if(nowPlayer.flow == null) return 0

        var value = number
        val before = nowPlayer.life

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        val emptyPlace = 5 - nowPlayer.flow!!
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            dust -= value
            nowPlayer.flow = nowPlayer.flow!! + value

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.LIFE_YOUR, LocationEnum.FLOW_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.LIFE_YOUR, LocationEnum.FLOW_YOUR, value, -1)
            lifeListenerProcess(player, -1, before, reconstruct = false, damage = false)
            if(nowPlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= nowPlayer.life){
                makeOneZoneLacerationToDamage(player, null, INDEX_LACERATION_LIFE)
            }
            if(nowPlayer.life <= 0){
                gameEnd(player.opposite(), player)
            }

        }

        return value
    }

    suspend fun flowToDust(player: PlayerEnum, number: Int){
        val nowPlayer = getPlayer(player)
        if(nowPlayer.flow == null) return
        var value = number

        if(nowPlayer.flow!! < value){
            value = nowPlayer.flow!!
        }

        if(value != 0){
            dust += value
            nowPlayer.flow = nowPlayer.flow!! - value

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, NUMBER_AKINA_AKINA, value,
                LocationEnum.FLOW_YOUR, LocationEnum.DUST, false))
            gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.FLOW_YOUR, LocationEnum.DUST, value, -1)
        }
    }

    suspend fun flowToFlare(player_flow: PlayerEnum, player_flare: PlayerEnum, number: Int, reason: Int){
        val flowPlayer = getPlayer(player_flow)
        if(flowPlayer.flow == null) return
        val flarePlayer = getPlayer(player_flare)
        var value = number

        if(flowPlayer.flow!! < value){
            value = flowPlayer.flow!!
        }

        if(value != 0){
            flowPlayer.flow = flowPlayer.flow!! - value
            flarePlayer.flare += value

            if(player_flow == player_flare){
                gameLogger.insert(EventLog(player_flow, LogText.MOVE_TOKEN, reason, value,
                    LocationEnum.FLOW_YOUR, LocationEnum.FLARE_YOUR, false))
                sendMoveToken(getSocket(player_flow), getSocket(player_flow.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.FLOW_YOUR, LocationEnum.FLARE_YOUR, value, -1)
            }
            else{
                gameLogger.insert(EventLog(player_flow, LogText.MOVE_TOKEN, reason, value,
                    LocationEnum.FLOW_YOUR, LocationEnum.FLARE_OTHER, false))
                sendMoveToken(getSocket(player_flow), getSocket(player_flow.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.FLOW_YOUR, LocationEnum.FLARE_OTHER, value, -1)
            }
        }
    }

    suspend fun flowToAura(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int){
        val nowPlayer = getPlayer(player)
        if(nowPlayer.flow == null) return
        var value = number

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            !getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLOW.encode(number))){
            auraToFlow(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
            return
        }

        if(nowPlayer.flow!! < value){
            value = nowPlayer.flow!!
        }

        nowPlayer.setMaxAura(arrow, user)
        val beforeFull = nowPlayer.checkAuraFull()
        val emptyPlace =  nowPlayer.maxAura - nowPlayer.aura
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            if(checkWhenGetAura(player)){
                flowToDust(player, value)
                afterCheckWhenGetAura(player)
                nowPlayer.maxAura = 5
                return
            }

            nowPlayer.flow = nowPlayer.flow!! - value
            nowPlayer.aura += value

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.FLOW_YOUR, LocationEnum.AURA_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.FLOW_YOUR, LocationEnum.AURA_YOUR, value, -1)

            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
        else{
            nowPlayer.maxAura = 5
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

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, anvilCard.card_number, value,
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

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.DUST, LocationEnum.MEMORY_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.MEMORY_YOUR, value, -1)
        }
    }

    suspend fun journeyToDust(player: PlayerEnum, number: Int, reason: Int){
        dust += number

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, number,
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
            LocationEnum.OUT_OF_GAME, LocationEnum.AURA_YOUR, value, -1)
        val afterFull = nowPlayer.checkAuraFull()
        auraListenerProcess(player, beforeFull, afterFull)
    }

    //main token move function

    suspend fun auraToOut(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                          reason: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
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

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.AURA_YOUR, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.aura -= value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.AURA_YOUR, LocationEnum.OUT_OF_GAME, value, -1)
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
    }

    suspend fun dustToOut(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                          reason: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            getBothDirection(user, LocToLoc.DUST_TO_OUT.encode(number))){
            return outToDust(number, arrow, user, card_owner, reason)
        }

        val value = if(number > dust) dust else number

        gameLogger.insert(EventLog(user, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DUST, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0){
            dust -= value
            sendMoveToken(getSocket(user), getSocket(user.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.OUT_OF_GAME, value, -1)
        }
    }

    suspend fun outToDust(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                            reason: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            !getBothDirection(user, LocToLoc.DUST_TO_OUT.encode(number))){
            return dustToOut(number, arrow, user, card_owner, reason)
        }

        gameLogger.insert(EventLog(user, LogText.MOVE_TOKEN, reason, number,
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

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
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
                outToDust(value, Arrow.NULL, user, card_owner, reason)
                afterCheckWhenGetAura(player)
                nowPlayer.maxAura = 5
                return
            }
            nowPlayer.aura += value
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.OUT_OF_GAME, LocationEnum.AURA_YOUR, arrow != Arrow.NULL))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.OUT_OF_GAME, LocationEnum.AURA_YOUR, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
        else{
            nowPlayer.maxAura = 5
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.OUT_OF_GAME, LocationEnum.AURA_YOUR, arrow != Arrow.NULL))
        }
    }

    suspend fun flareToOut(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int){
        if (number <= 0) return

        var value = number

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.FLARE_YOUR_TO_OUT.encode(number))
            else getBothDirection(user, LocToLoc.FLARE_OTHER_TO_OUT.encode(number))){
            return outToFlare(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        val nowPlayer = getPlayer(player)

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, number,
            LocationEnum.FLARE_YOUR, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.flare -= value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.OUT_OF_GAME, LocationEnum.FLARE_YOUR, number, -1)
        }
    }

    suspend fun outToFlare(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int){
        if (number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.FLARE_YOUR_TO_OUT.encode(number))
            else !getBothDirection(user, LocToLoc.FLARE_OTHER_TO_OUT.encode(number))){
            return flareToOut(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        val nowPlayer = getPlayer(player)

        nowPlayer.flare += number
        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, number,
            LocationEnum.OUT_OF_GAME, LocationEnum.FLARE_YOUR, arrow != Arrow.NULL))
        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.OUT_OF_GAME, LocationEnum.FLARE_YOUR, number, -1)
    }

    suspend fun auraToAura(playerGive: PlayerEnum, playerGet: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                           card_owner: PlayerEnum, reason: Int): Int{
        if(number <= 0) return 0

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(playerGet == user){
                if(getBothDirection(user, LocToLoc.AURA_OTHER_TO_AURA_YOUR.encode(number))){
                    auraToAura(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                    return 0
                }
            }
            else{
                if(!getBothDirection(user, LocToLoc.AURA_OTHER_TO_AURA_YOUR.encode(number))){
                    auraToAura(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
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
                auraToDust(playerGive, value, Arrow.NULL, user, card_owner, reason)
                afterCheckWhenGetAura(playerGet)
                nowPlayer.maxAura = 5
                return 0
            }

            getPlayer(playerGive).aura -= value
            getPlayer(playerGet).aura += value

            gameLogger.insert(EventLog(playerGive, LogText.MOVE_TOKEN, reason, number,
                LocationEnum.AURA_YOUR, LocationEnum.AURA_OTHER, arrow != Arrow.NULL))
            sendMoveToken(getSocket(playerGive), getSocket(playerGet), TokenEnum.SAKURA_TOKEN,
                LocationEnum.AURA_YOUR, LocationEnum.AURA_OTHER, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(playerGet, beforeFull, afterFull)
        }
        else{
            nowPlayer.maxAura = 5
            gameLogger.insert(EventLog(playerGive, LogText.MOVE_TOKEN, reason, number,
                LocationEnum.AURA_YOUR, LocationEnum.AURA_OTHER, arrow != Arrow.NULL))
        }

        return value
    }

    suspend fun auraToDistance(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                               reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.AURA_YOUR_TO_DISTANCE.encode(number))
            else getBothDirection(user, LocToLoc.AURA_OTHER_TO_DISTANCE.encode(number))){
            return distanceToAura(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        var value = number

        if(getPlayerAura(player) < value) {
            value = getPlayerAura(player)
        }
        if(distanceToken + value > 10) {
            value = 10 - distanceToken
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, number,
            LocationEnum.AURA_YOUR, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            getPlayer(player).aura -= value
            distanceToken += value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.AURA_YOUR, LocationEnum.DISTANCE, value, -1)
            whenDistanceChange()
        }
    }

    suspend fun auraToFlare(player_aura: PlayerEnum, player_flare: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                            card_owner: PlayerEnum, reason: Int){
        if(number <= 0) return


        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(player_aura == user){
                if(player_flare == user){
                    if(getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_YOUR.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return
                    }
                }
                else{
                    if(getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_OTHER.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return
                    }
                }
            }
            else{
                if(player_flare == user){
                    if(getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_YOUR.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return
                    }
                }
                else{
                    if(getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_OTHER.encode(number))){
                        flareToAura(player_flare, player_aura, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
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
                gameLogger.insert(EventLog(player_aura, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.AURA_YOUR, LocationEnum.FLARE_YOUR, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_aura), getSocket(player_aura.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.AURA_YOUR, LocationEnum.FLARE_YOUR, value, -1)
            }
            else{
                gameLogger.insert(EventLog(player_aura, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.AURA_YOUR, LocationEnum.FLARE_OTHER, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_aura), getSocket(player_aura.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.AURA_YOUR, LocationEnum.FLARE_OTHER, value, -1)
            }
        }
        else{
            if(player_aura == player_flare){
                gameLogger.insert(EventLog(player_aura, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.AURA_YOUR, LocationEnum.FLARE_YOUR, arrow != Arrow.NULL))
            }
            else{
                gameLogger.insert(EventLog(player_aura, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.AURA_YOUR, LocationEnum.FLARE_OTHER, arrow != Arrow.NULL))
            }
        }
    }

    suspend fun cardToAura(player: PlayerEnum, number: Int, card: Card, reason: Int){
        if(!(card.checkCanMoveToken(reason, player, this)) || number <= 0 || card.isItDestruction()) return
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
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, seed,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, seed, card.card_number)
        }

        if(sakura != 0){
            if(checkWhenGetAura(player)){
                dust += sakura
                gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, sakura,
                    LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DUST, false))
                sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DUST, sakura, card.card_number)
                afterCheckWhenGetAura(player)
                nowPlayer.maxAura = 5
                return
            }

            nowPlayer.aura += sakura
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, sakura,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.AURA_YOUR, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.AURA_YOUR, sakura, card.card_number)
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
        else{
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, sakura,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.AURA_YOUR, false))
        }

        if(card.getNap() == 0){
            card.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
        }
    }

    suspend fun cardToFlare(player: PlayerEnum, number: Int?, card: Card,
                            reason: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(!(card.checkCanMoveToken(reason, player, this)) || number == null || number <= 0  || card.isItDestruction()) return
        val nowPlayer = getPlayer(player)

        val (sakura, seed) = card.reduceNap(player, this, number)

        if(seed != 0){
            nowPlayer.notReadySeed = nowPlayer.notReadySeed!! + seed
            if(location == LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
                gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, seed,
                    LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, false))
            }
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                location, LocationEnum.NOT_READY_DIRT_ZONE_YOUR, seed, card.card_number)
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, sakura,
            location, LocationEnum.FLARE_YOUR, false))
        if(sakura != 0){
            nowPlayer.flare += sakura
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                location, LocationEnum.FLARE_YOUR, sakura, card.card_number)
        }

        if(card.getNap() == 0){
            card.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
        }
    }

    suspend fun cardToDistance(player: PlayerEnum, number: Int, card: Card, reason: Int){
        if(!(card.checkCanMoveToken(reason, player, this)) || number <= 0 || card.isItDestruction()) return
        var value = number

        val nowPlayer = getPlayer(player)
        if(distanceToken + value > 10) value = 10 - distanceToken

        val (sakura, seed) = card.reduceNap(player, this, value)

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, sakura,
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DISTANCE, false))
        if(sakura != 0){
            distanceToken += sakura

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DISTANCE, sakura, card.card_number)

            whenDistanceChange()
        }

        if(seed != 0){
            nowPlayer.notReadySeed = nowPlayer.notReadySeed!! + seed
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, seed,
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
    suspend fun cardToCard(player: PlayerEnum, number: Int, fromCard: Card, toCard: Card, reason: Int){
        if(!(fromCard.checkCanMoveToken(reason, player, this)) || number <= 0 || fromCard.isItDestruction()) return
        var value = number

        if(value > (fromCard.getNap()?: 0)) value = fromCard.getNap()?: 0

        val (sakura, seed) = fromCard.reduceNap(player, this, value)

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, sakura,
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, false))
        if(sakura != 0){
            toCard.addNap(sakura)
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, sakura, fromCard.card_number, toCard.card_number)
        }

        if(seed != 0){
            toCard.addNap(seed, true)
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, seed,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, false))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SEED_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, seed, fromCard.card_number, toCard.card_number)
        }

        if(fromCard.getNap() == 0){
            fromCard.effectText(player, this, null, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE)
        }
    }

    suspend fun distanceToFlare(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                                reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_OTHER.encode(number))){
            return flareToDistance(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        if(arrow != Arrow.NULL && moveTokenCheckArrow(LocationEnum.DISTANCE, LocationEnum.FLARE_YOUR)) return

        var value = number

        if(distanceToken < value){
            value = distanceToken
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DISTANCE, LocationEnum.FLARE_YOUR, Arrow.NULL != arrow))
        if(value != 0){
            distanceToken -= value
            getPlayer(player).flare += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.FLARE_YOUR, value, -1)

            whenDistanceChange()

            for(card in getPlayer(user.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(user.opposite(), this, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN)
            }

            getPlayer(user).isMoveDistanceToken = true
        }
    }

    suspend fun distanceToDust(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)
            && getBothDirection(user, LocToLoc.DISTANCE_TO_DUST.encode(number))){
            return dustToDistance(number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        if(arrow != Arrow.NULL && moveTokenCheckArrow(LocationEnum.DISTANCE, LocationEnum.DUST)) return

        var value = number

        if(distanceToken < value){
            value = distanceToken
        }

        gameLogger.insert(EventLog(user, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DISTANCE, LocationEnum.DUST, Arrow.NULL != arrow))
        if(value != 0){
            distanceToken -= value
            dust += value

            sendMoveToken(player1_socket, player2_socket, TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.DUST, value, -1)

            whenDistanceChange()

            for(card in getPlayer(user.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(user.opposite(), this, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN)
            }

            getPlayer(user).isMoveDistanceToken = true
        }

    }

    suspend fun distanceToAura(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                               reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.AURA_YOUR_TO_DISTANCE.encode(number))
            else !getBothDirection(user, LocToLoc.AURA_OTHER_TO_DISTANCE.encode(number))){
            return auraToDistance(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        if(arrow != Arrow.NULL && moveTokenCheckArrow(LocationEnum.DISTANCE, LocationEnum.AURA_YOUR)) return

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
                distanceToDust(value, Arrow.NULL, user, card_owner, reason)
                afterCheckWhenGetAura(player)
                nowPlayer.maxAura = 5
                return
            }

            distanceToken -= value

            nowPlayer.aura += value
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.DISTANCE, LocationEnum.AURA_YOUR, Arrow.NULL != arrow))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.AURA_YOUR, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
            whenDistanceChange()

            for(card in getPlayer(user.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(user.opposite(), this, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN)
            }
            getPlayer(user).isMoveDistanceToken = true
        }
        else{
            nowPlayer.maxAura = 5
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.DISTANCE, LocationEnum.AURA_YOUR, Arrow.NULL != arrow))
        }
    }

    //must check card is destruction
    suspend fun cardToDust(player: PlayerEnum, number: Int?, card: Card, startPhaseProcess: Boolean,
                           reason: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(!(card.checkCanMoveToken(reason, player, this)) || number == null || number <= 0 || card.isItDestruction()) return
        val nowPlayer = getPlayer(player)

        var value = number

        if(value > (card.getNap()?: 0)){
            value = card.getNap()?: 0
        }

        if(value != 0){
            if(cardToDustCheck(player, value, card, startPhaseProcess, reason)){
                val (sakura, seed) = card.reduceNap(player, this, number)

                if(sakura != 0){
                    dust += sakura
                    gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, sakura,
                        location, LocationEnum.DUST, false))
                    sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                        location, LocationEnum.DUST, number, card.card_number)
                }

                if(seed != 0){
                    nowPlayer.notReadySeed = nowPlayer.notReadySeed!! + seed

                    if(location == LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
                        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, seed,
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

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.AURA_YOUR, location, false))
        if(value != 0){
            nowPlayer.aura -= value
            card.addNap(value)

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.AURA_YOUR, location, value, card.card_number)
        }
    }

    suspend fun lifeToCard(player: PlayerEnum, number: Int, card: Card, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD,
                           reconstruct: Boolean, damage: Boolean, reason: Int){
        var value = number
        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.LIFE_YOUR, location, false))
        if(value != 0){
            nowPlayer.life -= value
            card.addNap(value)

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.LIFE_YOUR, location, value, card.card_number)
            lifeListenerProcess(player, reason, before, reconstruct, damage)
            if(nowPlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= nowPlayer.life){
                makeOneZoneLacerationToDamage(player, null, INDEX_LACERATION_LIFE)
            }
            if(nowPlayer.life <= 0){
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun outToCard(player: PlayerEnum, number: Int, card: Card,
                          card_number: Int, location: LocationEnum = LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD){
        if(number <= 0) return

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, card_number, number,
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

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, card_number, value,
            LocationEnum.DUST, location, false))

        if(value != 0){
            dust -= value
            card.addNap(value)

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, location, value, card.card_number)
        }

    }

    suspend fun dustToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DUST_TO_LIFE_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DUST_TO_LIFE_OTHER.encode(number))){
            lifeToDust(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
            return
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.life + value > 10){
            value = 10 - nowPlayer.life
        }

        if(value > dust){
            value = dust
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DUST, LocationEnum.LIFE_YOUR, arrow != Arrow.NULL))
        if(value != 0){
            dust -= value
            nowPlayer.life += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.LIFE_YOUR, value, -1)
        }
    }

    suspend fun dustToDistance(number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            !getBothDirection(user, LocToLoc.DISTANCE_TO_DUST.encode(number))){
            return distanceToDust(number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        var value = number

        if(distanceToken + value > 10){
            value = 10 - distanceToken
        }

        if(value > dust){
            value = dust
        }

        gameLogger.insert(EventLog(user, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DUST, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            distanceToken += value

            dust -= value

            sendMoveToken(player1_socket, player2_socket, TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.DISTANCE, value, -1)

            whenDistanceChange()
        }


    }

    suspend fun dustToAura(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int): Int{
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DUST_TO_AURA_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DUST_TO_AURA_OTHER.encode(number))){
            auraToDust(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
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
                nowPlayer.maxAura = 5
                return 0
            }

            nowPlayer.aura += value
            dust -= value

            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.DUST, LocationEnum.AURA_YOUR, arrow != Arrow.NULL))
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.AURA_YOUR, value, -1)
            nowPlayer.maxAura = 5
            val afterFull = nowPlayer.checkAuraFull()
            auraListenerProcess(player, beforeFull, afterFull)
        }
        else{
            nowPlayer.maxAura = 5
            gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
                LocationEnum.DUST, LocationEnum.AURA_YOUR, arrow != Arrow.NULL))
        }

        return value
    }

    suspend fun dustToFlare(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                            reason: Int){
        if (number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.DUST_TO_FLARE_YOUR.encode(number))
            else getBothDirection(user, LocToLoc.DUST_TO_FLARE_OTHER.encode(number))){
            flareToDust(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
            return
        }

        val nowPlayer = getPlayer(player)
        var value = number
        if(value > dust){
            value = dust
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DUST, LocationEnum.FLARE_YOUR, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.flare += value
            dust -= value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.FLARE_YOUR, value, -1)
        }
    }

    suspend fun auraToDust(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int): Int{
        if(number <= 0) return 0

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DUST_TO_AURA_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DUST_TO_AURA_OTHER.encode(number))){
            dustToAura(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
            return 0
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.aura < value){
            value = nowPlayer.aura
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.AURA_YOUR, LocationEnum.DUST, arrow != Arrow.NULL))

        if(value != 0){
            nowPlayer.aura -= value
            dust += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.AURA_YOUR, LocationEnum.DUST, value, -1)
        }

        return value
    }

    suspend fun flareToDistance(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                                reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DISTANCE_TO_FLARE_OTHER.encode(number))){
            return distanceToFlare(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }
        if(10 - distanceToken < value){
            value = 10 - distanceToken
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.FLARE_YOUR, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.flare -= value

            distanceToken += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.FLARE_YOUR, LocationEnum.DISTANCE, value, -1)

            whenDistanceChange()
        }

    }

    suspend fun flareToDust(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                            reason: Int): Int{
        if(number <= 0) return 0

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DUST_TO_FLARE_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DUST_TO_FLARE_OTHER.encode(number))){
            dustToFlare(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
            return 0
        }

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.FLARE_YOUR, LocationEnum.DUST, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.flare -= value

            dust += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.FLARE_YOUR, LocationEnum.DUST, value, -1)
        }

        return value
    }

    suspend fun flareToAura(player_flare: PlayerEnum, player_aura: PlayerEnum, number: Int,
                            arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, reason: Int): Int{
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(player_aura == user){
                if(player_flare == user){
                    if(!getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_YOUR.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
                else{
                    if(!getBothDirection(user, LocToLoc.AURA_YOUR_TO_FLARE_OTHER.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
            }
            else{
                if(player_flare == user){
                    if(!getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_YOUR.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
                else{
                    if(!getBothDirection(user, LocToLoc.AURA_OTHER_TO_FLARE_OTHER.encode(number))){
                        auraToFlare(player_aura, player_flare, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
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
                flareToDust(player_flare, value, Arrow.NULL, user, card_owner, reason)
                afterCheckWhenGetAura(player_aura)
                auraPlayer.maxAura = 5
                return 0
            }
            flarePlayer.flare -= value
            auraPlayer.aura += value

            if(player_flare == player_aura){
                gameLogger.insert(EventLog(player_flare, LogText.MOVE_TOKEN, reason, value,
                    LocationEnum.FLARE_YOUR, LocationEnum.AURA_YOUR, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_flare), getSocket(player_flare.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.FLARE_YOUR, LocationEnum.AURA_YOUR, value, -1)
            }
            else{
                gameLogger.insert(EventLog(player_flare, LogText.MOVE_TOKEN, reason, value,
                    LocationEnum.FLARE_YOUR, LocationEnum.AURA_OTHER, arrow != Arrow.NULL))
                sendMoveToken(getSocket(player_flare), getSocket(player_flare.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.FLARE_YOUR, LocationEnum.AURA_OTHER, value, -1)
            }
            auraPlayer.maxAura = 5
            val afterFull = auraPlayer.checkAuraFull()
            auraListenerProcess(player_aura, beforeFull, afterFull)
        }
        else{
            if(player_flare == player_aura){
                gameLogger.insert(EventLog(player_flare, LogText.MOVE_TOKEN, reason, value,
                    LocationEnum.FLARE_YOUR, LocationEnum.AURA_YOUR, arrow != Arrow.NULL))
            }
            else{
                gameLogger.insert(EventLog(player_flare, LogText.MOVE_TOKEN, reason, value,
                    LocationEnum.FLARE_YOUR, LocationEnum.AURA_OTHER, arrow != Arrow.NULL))
            }
        }

        return value
    }

    suspend fun lifeToDust(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                           reason: Int, endIgnore: Boolean = false): Int{
        val nowPlayer = getPlayer(player)

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.DUST_TO_LIFE_YOUR.encode(number))
            else !getBothDirection(user, LocToLoc.DUST_TO_LIFE_OTHER.encode(number))){
            dustToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
            return 0
        }

        val before = nowPlayer.life

        var value = number

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.FLARE_YOUR, LocationEnum.DUST, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life -= value
            dust += value
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.LIFE_YOUR, LocationEnum.DUST, value, -1)
            lifeListenerProcess(player, reason, before, reconstruct = false, damage = false)
            if(nowPlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= nowPlayer.life){
                makeOneZoneLacerationToDamage(player, null, INDEX_LACERATION_LIFE)
            }
            if(nowPlayer.life == 0 && !endIgnore){
                gameEnd(player.opposite(), player)
            }
        }
        return value
    }

    suspend fun selfFlareToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                                reason: Int){
        if(number == 0) {
            return
        }

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) && !getBothDirection(user, LocToLoc.YOUR_LIFE_TO_YOUR_FLARE.encode(number))){
            return lifeToSelfFlare(player, number, reconstruct = false, damage = false, arrow = Arrow.BOTH_DIRECTION, user = user,
                card_owner = card_owner, reason = reason
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

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.FLARE_YOUR, LocationEnum.LIFE_YOUR, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life += value
            nowPlayer.flare -= value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.FLARE_YOUR, LocationEnum.LIFE_YOUR, value, -1)
        }
    }

    suspend fun lifeToSelfFlare(player: PlayerEnum, number: Int, reconstruct: Boolean, damage: Boolean,
                                arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum, reason: Int){
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            getBothDirection(user, LocToLoc.YOUR_LIFE_TO_YOUR_FLARE.encode(number))){
            return selfFlareToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        var value = number
        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life < value){
            value = nowPlayer.life
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.LIFE_YOUR, LocationEnum.FLARE_YOUR, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life -= value
            nowPlayer.flare += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.LIFE_YOUR, LocationEnum.FLARE_YOUR, value, -1)
            lifeListenerProcess(player, reason, before, reconstruct, damage)
            if(nowPlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= nowPlayer.life){
                makeOneZoneLacerationToDamage(player, null, INDEX_LACERATION_LIFE)
            }
            if(nowPlayer.life == 0){
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun auraToLife(player_aura: PlayerEnum, player_life: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                           card_owner: PlayerEnum, reason: Int): Int{
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(player_life == user){
                if(player_aura == user){
                    if(!getBothDirection(user, LocToLoc.LIFE_YOUR_TO_AURA_YOUR.encode(number))){
                        lifeToAura(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
                else{
                    if(!getBothDirection(user, LocToLoc.LIFE_YOUR_TO_AURA_OTHER.encode(number))){
                        lifeToAura(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
            }
            else{
                if(player_aura == user){
                    if(!getBothDirection(user, LocToLoc.LIFE_OTHER_TO_AURA_YOUR.encode(number))){
                        lifeToAura(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
                else{
                    if(!getBothDirection(user, LocToLoc.LIFE_OTHER_TO_AURA_OTHER.encode(number))){
                        lifeToAura(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
            }
        }

        val lifePlayer = getPlayer(player_life)
        val auraPlayer = getPlayer(player_aura)
        var value = number
        val beforeFull = auraPlayer.checkAuraFull()
        val beforeLife = lifePlayer.life

        if(value > auraPlayer.aura){
            value = auraPlayer.aura
        }

        val emptyPlace = 10 - lifePlayer.life
        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value != 0){
            auraPlayer.aura -= value
            lifePlayer.life += value

            if(player_life == player_aura){
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.AURA_YOUR, LocationEnum.LIFE_YOUR, false))
                sendMoveToken(getSocket(player_life), getSocket(player_life.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.AURA_YOUR, LocationEnum.LIFE_YOUR, value, -1)
            }
            else{
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.AURA_YOUR, LocationEnum.LIFE_OTHER, false))
                sendMoveToken(getSocket(player_life), getSocket(player_life.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.AURA_YOUR, LocationEnum.LIFE_OTHER, value, -1)
            }

            val afterFull = auraPlayer.checkAuraFull()
            auraListenerProcess(player_aura, beforeFull, afterFull)
            lifeListenerProcess(player_life, reason, beforeLife, reconstruct = false, damage = false)
        }
        else{
            if(player_life == player_aura){
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_YOUR, false))
            }
            else{
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_OTHER, false))
            }
        }

        return value
    }

    suspend fun lifeToAura(player_life: PlayerEnum, player_aura: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                           card_owner: PlayerEnum, reason: Int): Int{
        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(player_life == user){
                if(player_aura == user){
                    if(getBothDirection(user, LocToLoc.LIFE_YOUR_TO_AURA_YOUR.encode(number))){
                        auraToLife(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
                else{
                    if(getBothDirection(user, LocToLoc.LIFE_YOUR_TO_AURA_OTHER.encode(number))){
                        auraToLife(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
            }
            else{
                if(player_aura == user){
                    if(getBothDirection(user, LocToLoc.LIFE_OTHER_TO_AURA_YOUR.encode(number))){
                        auraToLife(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
                else{
                    if(getBothDirection(user, LocToLoc.LIFE_OTHER_TO_AURA_OTHER.encode(number))){
                        auraToLife(player_aura, player_life, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                        return 0
                    }
                }
            }
        }

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
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_YOUR, false))
                sendMoveToken(getSocket(player_life), getSocket(player_life.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_YOUR, value, -1)
            }
            else{
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_OTHER, false))
                sendMoveToken(getSocket(player_life), getSocket(player_life.opposite()), TokenEnum.SAKURA_TOKEN,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_OTHER, value, -1)
            }

            val afterFull = auraPlayer.checkAuraFull()
            auraListenerProcess(player_aura, beforeFull, afterFull)

            lifeListenerProcess(player_life, reason, beforeLife, reconstruct = false, damage = false)
            if(lifePlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= lifePlayer.life){
                makeOneZoneLacerationToDamage(player_life, null, INDEX_LACERATION_LIFE)
            }
            if (lifePlayer.life == 0) {
                gameEnd(player_life.opposite(), player_life)
            }
        }
        else{
            if(player_life == player_aura){
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_YOUR, false))
            }
            else{
                gameLogger.insert(EventLog(player_life, LogText.MOVE_TOKEN, reason, number,
                    LocationEnum.LIFE_YOUR, LocationEnum.AURA_OTHER, false))
            }
        }

        return value
    }

    suspend fun distanceToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum, card_owner: PlayerEnum,
                               reason: Int){
        if(number == 0){
            return
        }

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) !getBothDirection(user, LocToLoc.LIFE_YOUR_TO_DISTANCE.encode(number))
            else !getBothDirection(user, LocToLoc.LIFE_OTHER_TO_DISTANCE.encode(number))){
            return lifeToDistance(player, number, false, Arrow.BOTH_DIRECTION, user, card_owner, reason)
        }

        if(arrow != Arrow.NULL && moveTokenCheckArrow(LocationEnum.DISTANCE, LocationEnum.LIFE_YOUR)) return

        var value = number
        val nowPlayer = getPlayer(player)

        if(distanceToken > value){
            value = distanceToken
        }

        if(nowPlayer.life + value > 10){
            value = 10 - nowPlayer.life
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.DISTANCE, LocationEnum.LIFE_YOUR, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life += value

            distanceToken -= value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.LIFE_YOUR, value, -1)

            whenDistanceChange()

            for(card in getPlayer(user.opposite()).enchantmentCard.values){
                card.effectAllValidEffect(user.opposite(), this, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN)
            }
            getPlayer(user).isMoveDistanceToken = true
        }
    }

    suspend fun outToLife(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                          card_owner: PlayerEnum, reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(player == user){
                if(!getBothDirection(user, LocToLoc.LIFE_YOUR_TO_OUT.encode(number))){
                    return lifeToOut(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                }
            }
            else{
                if(!getBothDirection(user, LocToLoc.LIFE_OTHER_TO_OUT.encode(number))){
                    return lifeToOut(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                }
            }
        }

        var value = number

        val nowPlayer = getPlayer(player)

        if(10 - nowPlayer.life < value){
            value = 10 - nowPlayer.life
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.OUT_OF_GAME, LocationEnum.LIFE_YOUR, arrow != Arrow.NULL))
        if(value != 0) {
            nowPlayer.life += value
            sendMoveToken(
                getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.OUT_OF_GAME, LocationEnum.LIFE_YOUR, value, -1
            )
        }
    }

    suspend fun lifeToOut(player: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                          card_owner: PlayerEnum, reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(player == user){
                if(getBothDirection(user, LocToLoc.LIFE_YOUR_TO_OUT.encode(number))){
                    return outToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                }
            }
            else{
                if(getBothDirection(user, LocToLoc.LIFE_OTHER_TO_OUT.encode(number))){
                    return outToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                }
            }
        }

        var value = number

        val nowPlayer = getPlayer(player)
        val before = nowPlayer.life

        if(before < value){
            value = before
        }

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.LIFE_YOUR, LocationEnum.OUT_OF_GAME, arrow != Arrow.NULL))
        if(value != 0) {
            nowPlayer.life -= value

            sendMoveToken(
                getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.LIFE_YOUR, LocationEnum.OUT_OF_GAME, value, -1
            )
            lifeListenerProcess(player, reason, before, reconstruct = false, damage = false)
            if(nowPlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= nowPlayer.life){
                makeOneZoneLacerationToDamage(player, null, INDEX_LACERATION_LIFE)
            }
            if (nowPlayer.life == 0) {
                gameEnd(player.opposite(), player)
            }
        }
    }

    suspend fun lifeToLife(playerGive: PlayerEnum, playerGet: PlayerEnum, number: Int, arrow: Arrow, user: PlayerEnum,
                           card_owner: PlayerEnum, reason: Int){
        if(number <= 0) return

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner)){
            if(playerGive == user){
                if(getBothDirection(user, LocToLoc.LIFE_YOUR_TO_LIFE_OTHER.encode(number))){
                    return lifeToLife(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
                }
            }
            else{
                if(!getBothDirection(user, LocToLoc.LIFE_YOUR_TO_LIFE_OTHER.encode(number))){
                    return lifeToLife(playerGet, playerGive, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
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

        gameLogger.insert(EventLog(playerGive, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.LIFE_YOUR, LocationEnum.LIFE_OTHER, arrow != Arrow.NULL))
        if(value != 0) {
            givePlayer.life -= value
            getPlayer.life += value

            sendMoveToken(
                getSocket(playerGet), getSocket(playerGive), TokenEnum.SAKURA_TOKEN,
                LocationEnum.LIFE_YOUR, LocationEnum.LIFE_OTHER, value, -1
            )
            if(givePlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= givePlayer.life){
                makeOneZoneLacerationToDamage(playerGive, null, INDEX_LACERATION_LIFE)
            }
            lifeListenerProcess(playerGet, reason, before, reconstruct = false, damage = false)
            if (givePlayer.life == 0) {
                gameEnd(playerGive.opposite(), playerGive)
            }
        }
    }

    suspend fun lifeToDistance(player: PlayerEnum, number: Int, damage: Boolean, arrow: Arrow, user: PlayerEnum,
                               card_owner: PlayerEnum, reason: Int) {
        if(number == 0) {
            return
        }

        if(arrow == Arrow.ONE_DIRECTION && bothDirectionCheck(reason, card_owner) &&
            if(user == player) getBothDirection(user, LocToLoc.LIFE_YOUR_TO_DISTANCE.encode(number))
            else getBothDirection(user, LocToLoc.LIFE_OTHER_TO_DISTANCE.encode(number))){
            return distanceToLife(player, number, Arrow.BOTH_DIRECTION, user, card_owner, reason)
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

        gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, reason, value,
            LocationEnum.LIFE_YOUR, LocationEnum.DISTANCE, arrow != Arrow.NULL))
        if(value != 0){
            nowPlayer.life -= value

            distanceToken += value

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.LIFE_YOUR, LocationEnum.DISTANCE, value, -1)
            lifeListenerProcess(player, reason, before, false, damage)
            whenDistanceChange()
            if(nowPlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= nowPlayer.life){
                makeOneZoneLacerationToDamage(player, null, INDEX_LACERATION_LIFE)
            }
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
        val nowPlayer = getPlayer(player)
        var nowCost = cost

        if(nowCost == NUMBER_MARKET_PRICE){
            return getPlayer(card.player).getMarketPrice()
        }

        for(queue in nowPlayer.costBuff){
            val tempQ: ArrayDeque<CostBuff> = ArrayDeque()
            for(buff in queue){
                if(buff.condition(player, this, card)){
                    buff.counter *= -1
                    tempQ.add(buff)
                }
            }
            for(buff in tempQ){
                nowCost = buff.effect(nowCost, player, this)
            }
        }

        return nowCost
    }

    private suspend fun applyAllAttackBuff(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        val nowAttack = nowPlayer.preAttackCard!!

        nowAttack.effectText(player, this, nowAttack, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK)

        nowAttack.addTempAttackBuff(getPlayerTempAttackBuff(player))
        nowAttack.addTempOtherBuff(getPlayerTempOtherBuff(player))
    }

    private suspend fun attackRangeCheck(attack: MadeAttack, player: PlayerEnum): Boolean{
        return if(attack.isTrace){
            attack.rangeCheck(getPlayer(player).aiming?: -999, this, player)
        } else{
            attack.rangeCheck(getAdjustDistance(), this, player)
        }
    }

    private fun cleanCostBuffWhenUsed(){
        cleanCostBuff(player1.costBuff)
        cleanCostBuff(player2.costBuff)
    }

    fun cleanCostBuffWhenUnused(){
        cleanCostTempBuff(player1.costBuff)
        cleanCostTempBuff(player2.costBuff)
    }

    private fun cleanAllBuffWhenUnused(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.preAttackCard!!.returnWhenBuffDoNotUse(nowPlayer.rangeBuff)
        player1TempRangeBuff.clearBuff()
        player1TempAttackBuff.clearUnUsedBuff()
        player1TempOtherBuff.clearBuff()
        player2TempRangeBuff.clearBuff()
        player2TempAttackBuff.clearUnUsedBuff()
        player2TempOtherBuff.clearBuff()
    }

    suspend fun addPreAttackZone(player: PlayerEnum, attack: MadeAttack): Boolean{
        val nowPlayer = getPlayer(player)
        if(nowPlayer.canNotAttack) return false

        val nowTempBuffQueue = getPlayerTempRangeBuff(player)

        nowPlayer.addPreAttackZone(attack)
        addAllCardTextBuff(player)
        attack.addTempRangeBuff(nowTempBuffQueue)

        return if(attackRangeCheck(attack, player)){
            getPlayerTempRangeBuff(player.opposite()).clearBuff()
            getPlayerTempAttackBuff(player.opposite()).clearBuff()
            getPlayerTempOtherBuff(player.opposite()).clearBuff()
            applyAllAttackBuff(player)
            true
        } else{
            cleanAllBuffWhenUnused(player)
            nowPlayer.preAttackCard = null
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
            BufTag.MULTIPLE_IMMEDIATE, BufTag.DIVIDE_IMMEDIATE, BufTag.PLUS_MINUS_IMMEDIATE,
            BufTag.PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED -> {
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
            BufTag.MULTIPLE_IMMEDIATE, BufTag.DIVIDE_IMMEDIATE, BufTag.PLUS_MINUS_IMMEDIATE,
            BufTag.PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED -> {
                getPlayerTempAttackBuff(player).removeAttackBuff(effectTag, card_number)
            }
        }
    }

    var buffNumberCounter = 0

    fun useBuffNumberCounter(): Int{
        buffNumberCounter += 1
        return buffNumberCounter - 1
    }

    fun addThisTurnRangeBuff(player: PlayerEnum, effect: RangeBuff){
        val nowPlayer = getPlayer(player)
        val nowTempRangeBuff = getPlayerTempRangeBuff(player)

        when(effect.tag){
            RangeBufTag.CARD_CHANGE, RangeBufTag.CHANGE, RangeBufTag.CHANGE_AFTER,
            RangeBufTag.ADD, RangeBufTag.DELETE, RangeBufTag.PLUS,
            RangeBufTag.MINUS-> {
                nowPlayer.rangeBuff.addRangeBuff(useBuffNumberCounter(), effect)
            }
            RangeBufTag.CARD_CHANGE_IMMEDIATE, RangeBufTag.CHANGE_IMMEDIATE, RangeBufTag.CHANGE_AFTER_IMMEDIATE,
            RangeBufTag.ADD_IMMEDIATE, RangeBufTag.DELETE_IMMEDIATE, RangeBufTag.PLUS_IMMEDIATE,
            RangeBufTag.MINUS_IMMEDIATE -> {
                nowTempRangeBuff.addRangeBuff(useBuffNumberCounter(), effect)
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
        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        when(nowPlayer.addConcentration()){
            0 -> {
                sendAddConcentration(nowSocket, otherSocket)
            }
            1 -> {
                sendRemoveShrink(nowSocket, otherSocket)
            }
        }
    }

    suspend fun decreaseConcentration(player: PlayerEnum): Boolean{
        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        if(nowPlayer.decreaseConcentration()) {
            sendDecreaseConcentration(nowSocket, otherSocket)
            return true
        }
        else{
            return false
        }
    }

    private suspend fun reactCheck(react_player: PlayerEnum, card: Card, attack: MadeAttack): Boolean{
        if(card.canUseAtReact(react_player, this) &&
            card.canReactAt(attack, this, react_player, getPlayerOtherBuff(react_player.opposite()))){
            return true
        }
        return false
    }

    suspend fun setGauge(player: PlayerEnum, thunder: Boolean, number: Int){
        val nowPlayer = getPlayer(player)
        if(thunder){
            nowPlayer.thunderGauge?.let {
                if(number > 20){
                    nowPlayer.thunderGauge = 20
                    sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_THUNDER_GAUGE_YOUR, 20)
                }
                else{
                    nowPlayer.thunderGauge = number
                    sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_THUNDER_GAUGE_YOUR, number)
                }
            }
        }
        else{
            nowPlayer.windGauge?.let {
                if(number > 20){
                    nowPlayer.windGauge = 20
                    sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_WIND_GAUGE_YOUR, 20)
                }
                else{
                    nowPlayer.windGauge = number
                    sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_WIND_GAUGE_YOUR, number)
                }
            }
        }
    }

    suspend fun thunderGaugeIncrease(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.thunderGauge?.let {
            if(it != 20){
                nowPlayer.thunderGauge = it + 1
                sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.INCREASE_THUNDER_GAUGE_YOUR, -1)
            }
        }
    }

    suspend fun windGaugeIncrease(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.windGauge?.let {
            if(it != 20){
                nowPlayer.windGauge = it + 1
                sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.INCREASE_WIND_GAUGE_YOUR, -1)
            }
        }
    }

    suspend fun tabooGaugeIncrease(player: PlayerEnum, number: Int): Boolean{
        val nowPlayer = getPlayer(player)

        nowPlayer.tabooGauge = nowPlayer.tabooGauge?.let {
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_TABOO_GAUGE_YOUR, it + number)
            if(it + number >= 16){
                gameEnd(null, player)
            }
            it + number
        }?: return false

        for(card in nowPlayer.usedSpecialCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_TABOO_CHANGE)
        }

        return true
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
                when(receiveCardEffectSelect(player, NUMBER_RAIRA_BEAST_NAIL)){
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
     true means this card is terminationText card
     false means this card is not terminationText card
     */
    suspend fun terminationListenerProcess(player: PlayerEnum, card: Card): Boolean{
        if(card.card_data.sub_type == SubType.REACTION && gameLogger.checkThisCardUseInSoldier(player, card.card_number)){
            for(usedCard in getPlayer(player).usedSpecialCard.values){
                if(usedCard.effectAllValidEffect(player, this, TextEffectTag.REMOVE_TERMINATION_REACTION_USE_IN_SOLDIER) == 1){
                    return false
                }
            }
        }
        card.card_data.effect?.let {
            for(text in it){
                if(text === CardSet.terminationText){
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

        val nowAttack = nowPlayer.preAttackCard
        nowAttack?.activeOtherBuff(this, player, nowPlayer.otherBuff)
        nowAttack?.getDamage(this, player, nowPlayer.attackBuff)
        nowPlayer.preAttackCard = null
    }

    private suspend fun divingProcess(player: PlayerEnum, card: Card?): Boolean{
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
                otherPlayer.forwardDiving = null
            }
            false -> {
                sendSimpleCommand(getSocket(player.opposite()), CommandEnum.DIVING_SHOW)
                sendSimpleCommand(getSocket(player), CommandEnum.DIVING_BACKWARD)
                addThisTurnDistance(1)
                addThisTurnSwellDistance(1)
                otherPlayer.forwardDiving = null
            }
        }

        val nowPlayer = getPlayer(player)

        card?.let {
            if(it.card_data.card_type == CardType.ATTACK){
                val nowAttack = nowPlayer.preAttackCard
                if(nowAttack?.rangeCheck(getAdjustDistance(), this, player) == false){
                    nowPlayer.divingSuccess = true
                    return true
                }
            }
        }

        return false
    }

    suspend fun useCardFromNotFullAction(player: PlayerEnum, card: Card, location: LocationEnum, react: Boolean, react_attack: MadeAttack?,
                                         isCost: Boolean, isConsume: Boolean, napChange: Int = -1, cardMoveCancel: Boolean = false): Boolean{
        val preFullAction = getFullAction(player)
        setPlayerFullAction(player, false)
        val result = useCardFrom(player, card, location , react, react_attack, isCost, isConsume, napChange, cardMoveCancel)
        setPlayerFullAction(player, preFullAction)
        return result
    }

    private fun addUseCardLog(player: PlayerEnum, card: Card, useLocation: LocationEnum, react: Boolean){
        if(useLocation == LocationEnum.READY_SOLDIER_ZONE) {
            gameLogger.insert(
                EventLog(
                    player, LogText.USE_CARD_IN_SOLDIER, card.card_number,
                    card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                )
            )
        }
        else if(useLocation == LocationEnum.COVER_CARD && react) {
            gameLogger.insert(
                EventLog(
                    player, LogText.USE_CARD_IN_COVER_AND_REACT, card.card_number,
                    card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                )
            )
        }
        else if(useLocation == LocationEnum.COVER_CARD) {
            gameLogger.insert(
                EventLog(
                    player, LogText.USE_CARD_IN_COVER, card.card_number,
                    card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                )
            )
        }
        else if(react) {
            gameLogger.insert(
                EventLog(
                    player, LogText.USE_CARD_REACT, card.card_number,
                    card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                )
            )
        }
        else {
            gameLogger.insert(
                EventLog(
                    player, LogText.USE_CARD, card.card_number,
                    card.card_data.megami.real_number, boolean = card.card_data.sub_type == SubType.FULL_POWER
                )
            )
        }
    }

    private suspend fun processKamuwiLogic(player: PlayerEnum, card: Card){
        val nowPlayer = getPlayer(player)
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
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KAMUWI_LOGIC, cardMustPay
            ) { it, _ -> nowPlayer.nextCostAddMegami == it.card_data.megami }?.let {selected ->
                for(card_number in selected){
                    popCardFrom(player, card_number, LocationEnum.HAND, true)?.let {
                        insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                }
            }
        }

        nowPlayer.nextCostAddMegami = null
    }

    private suspend fun payCost(player: PlayerEnum, card: Card, cost: Int){
        if(cost < 0){
            addLacerationToken(player, player, INDEX_LACERATION_FLARE, cost / 4 * -1)
        }
        else{
            flareToDust(player, cost, Arrow.NULL, player, card.player, EventLog.SPECIAL_COST)
            gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.SPECIAL_COST, -1))
        }
        cleanCostBuffWhenUsed()
    }

    /**
     isCost means bi yong
     isConsume means so mo gap
     */
    suspend fun useCardFrom(player: PlayerEnum, card: Card, location: LocationEnum, react: Boolean, react_attack: MadeAttack?,
                            isCost: Boolean, isConsume: Boolean, napChange: Int = -1, cardMoveCancel: Boolean = false): Boolean{
        if(react_attack != null && !react){
            react_attack.isItReact = false
        }

        if(getEndTurn(player) || endCurrentPhase){
            return false
        }

        val cost = card.canUse(player, this, react_attack, isCost, isConsume)

        if(cost == -3){
            return true
        }
        else if(cost != -2){
            addUseCardLog(player, card, location, react)

            if(!(getPlayer(player).canNotCharge)){
                gaugeIncreaseRequest(player, card)
            }

            val nowPlayer = getPlayer(player)

            val isTermination = terminationListenerProcess(player, card)
            if(isCost) card.effectText(player, this, react_attack, TextEffectTag.COST)

            processKamuwiLogic(player, card)

            //kamuwi dawn
            if(react_attack?.effectText(card.card_number, player.opposite(), this, react_attack,
                    TextEffectTag.WHEN_THIS_CARD_REACTED) == 1){
                popCardFrom(player, card.card_number, location, true)?.let {
                    if(cost == -1){
                        insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                    else{
                        payCost(player, card, cost)
                        insertCardTo(player, it, LocationEnum.YOUR_USED_CARD, true)
                    }
                }
                if(card.card_data.card_type == CardType.ATTACK){
                    val nowAttack = nowPlayer.preAttackCard
                    nowAttack?.activeOtherBuff(this, player, nowPlayer.otherBuff)
                    nowAttack?.getDamage(this, player, nowPlayer.attackBuff)
                    nowPlayer.preAttackCard = null
                }
                return true
            }

            //tokoyo duetChitanYangMyeong
            if(react){
                getPlayer(player).usedSpecialCard.values.forEach { specialCard ->
                    specialCard.effectAllValidEffect(card.card_number, player, this, TextEffectTag.WHEN_REACT_YOUR)
                }
            }

            //lighthouse effect
            if(cost == -1){
                var lightHouseCheck = 0
                if(turnPlayer == player && card.card_data.card_type != CardType.ATTACK && location == LocationEnum.HAND){
                    getPlayer(player.opposite()).usedSpecialCard.values.forEach {otherUsedCard ->
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
                    if(location != LocationEnum.PLAYING_ZONE_YOUR){
                        popCardFrom(player, card.card_number, location, true)?.let {
                            insertCardTo(player, it, LocationEnum.PLAYING_ZONE_YOUR, true)
                        }
                    }
                    sendUseCardMeesage(getSocket(player), getSocket(player.opposite()), react, card.card_number)
                    card.use(player, this, react_attack, isTermination, napChange, cardMoveCancel)
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
            else{
                nowPlayer.isUseCard = true
                card.special_card_state = SpecialCardEnum.PLAYING

                payCost(player, card, cost)

                if(divingProcess(player, card)){
                    afterDivingSuccess(player, card, location)
                    return true
                }

                if(location != LocationEnum.PLAYING_ZONE_YOUR){
                    popCardFrom(player, card.card_number, location, true)?.let {
                        insertCardTo(player, it, LocationEnum.PLAYING_ZONE_YOUR, true)
                    }
                }
                sendUseCardMeesage(getSocket(player), getSocket(player.opposite()), react, card.card_number)
                card.use(player, this, react_attack, isTermination, napChange, cardMoveCancel)
                return true
            }
        }
        return false
    }

    private suspend fun useCardPerjury(player: PlayerEnum, falseCard: Card, perjury_card_number: Int,
                                       location: LocationEnum): Boolean{
        val nowPlayer = getPlayer(player)
        val megami = if (nowPlayer.megamiOne.real_number / 10 == 22){
            nowPlayer.megamiOne
        } else{
            nowPlayer.megamiTwo
        }

        val perjuryCard = getValidPerjuryCard(megami, perjury_card_number, this.version)?: return false
        if(perjuryCheck[perjury_card_number - NUMBER_RENRI_FALSE_STAB]){
            return false
        }
        val cost = perjuryCard.canUse(player, this, null, isCost = true, isConsume = true)

        if(cost != -2){
            if(!getPlayer(player).canNotCharge){
                gaugeIncreaseRequest(player, perjuryCard)
            }

            processKamuwiLogic(player, perjuryCard)

            perjuryCheck[perjury_card_number - NUMBER_RENRI_FALSE_STAB] = true
            var public = false
            var isDisprove = true

            when(receiveSelectDisprove(getSocket(player.opposite()), perjury_card_number)){
                CommandEnum.SELECT_ONE -> {
                    public = true
                    sendSimpleCommand(player1_socket, player2_socket, CommandEnum.SHOW_DISPROVE_RESULT, falseCard.card_number)
                    if(perjuryCard.card_data.card_name == falseCard.card_data.card_name){
                        //disprove fail
                        gameLogger.insert(EventLog(player, LogText.FAIL_DISPROVE, -1, -1))

                        chojoDamageProcess(player.opposite())

                        cardForEffect = perjuryCard
                        perjuryCard.effectText(falseCard.card_number, player, this,
                            null, TextEffectTag.WHEN_THIS_CARD_DISPROVE_FAIL)
                        cardForEffect = null
                    }
                    else{
                        //disprove success
                        popCardFrom(player, falseCard.card_number, location, true)?.let {
                            insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                        }
                        return true
                    }
                }
                else -> {
                    isDisprove = false
                    if(perjuryCard.effectText(falseCard.card_data.card_name.toCardNumber(true),
                        player, this, null, TextEffectTag.WHEN_THIS_CARD_WHEN_PERJURE_NOT_DISPROVE) == 1){
                        public = true
                    }
                    var flag = 0
                    for(card in nowPlayer.enchantmentCard.values){
                        flag += card.effectAllValidEffect(player, this, TextEffectTag.RIRARURIRARO_EFFECT)
                        if(flag > 0){
                            if(riRaRuRiRaRoEffect(falseCard, perjuryCard, player)){
                                public = true
                            }
                            break
                        }
                    }
                }
            }

            if(location == LocationEnum.READY_SOLDIER_ZONE) {
                gameLogger.insert(
                    EventLog(
                        player, LogText.USE_CARD_IN_SOLDIER_PERJURE, perjuryCard.card_number,
                        perjuryCard.card_data.megami.real_number, boolean = perjuryCard.card_data.sub_type == SubType.FULL_POWER
                    )
                )
            }
            else {
                gameLogger.insert(
                    EventLog(
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
                perjuryCard.use(player, this, null, false, 0, false, isDisprove)

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

    /**
     * @return means card is public (true) or not (false)
     */
    private suspend fun riRaRuRiRaRoEffect(falseCard: Card, perjuryCard: Card, player: PlayerEnum): Boolean{
        while(true){
            when(receiveCardEffectSelect(player, NUMBER_RI_RA_RU_RI_RA_RO_REVEAL_CARD)){
                CommandEnum.SELECT_ONE -> {
                    showPlayersSelectResult(player.opposite(), NUMBER_RENRI_RI_RA_RU_RI_RA_RO, falseCard.card_number)
                    if(falseCard.card_data.card_name != perjuryCard.card_data.card_name){
                        getConcentrationValue(player)
                    }
                    return true
                }
                CommandEnum.SELECT_NOT -> {
                    return false
                }
                else -> {}
            }
        }
    }

    /**
     * @param card_number only used to inform user what attack is generated
     */
    suspend fun afterMakeAttack(card_number: Int, attack_player: PlayerEnum, react_attack: MadeAttack?){
        val attackPlayer = getPlayer(attack_player)
        if(attackPlayer.preAttackCard == null){
            return
        }

        fun addAttackLog(attack: MadeAttack){
            gameLogger.insert(
                EventLog(attack_player, LogText.ATTACK, attack.card_number, when(attack.card_class){
                    CardClass.POISON -> EventLog.ATTACK_NUMBER_POISON
                    CardClass.IDEA -> EventLog.ATTACK_NUMBER_IDEA
                    CardClass.SOLDIER -> EventLog.ATTACK_NUMBER_SOLDIER
                    CardClass.SPECIAL -> EventLog.ATTACK_NUMBER_SPECIAL
                    CardClass.NORMAL -> EventLog.ATTACK_NUMBER_NORMAL
                    CardClass.CUSTOM_PARTS, CardClass.MAIN_PARTS -> EventLog.ATTACK_NUMBER_PARTS
                    CardClass.NULL -> EventLog.ATTACK_NUMBER_NULL }))
        }

        val attackerSocket = getSocket(attack_player)
        val otherSocket = getSocket(attack_player.opposite())
        val otherPlayer = getPlayer(attack_player.opposite())

        val nowAttack = attackPlayer.preAttackCard!!
        attackPlayer.preAttackCard = null

        addAttackLog(nowAttack)

        makeAttackComplete(attackerSocket, otherSocket, card_number)
        sendAttackInformation(attackerSocket, otherSocket, nowAttack.toInformation())

        if(react_attack == null){
            var reactCheckCancel = false
            for(card in otherPlayer.usedSpecialCard.values){
                if (card.effectAllValidEffect(attack_player.opposite(), this, TextEffectTag.WHEN_GET_ATTACK) == 1){
                    reactCheckCancel = true
                }
            }
            if(!otherPlayer.endTurn && !reactCheckCancel){
                while(true){
                    sendRequestReact(otherSocket)
                    if(reactProcess(attack_player.opposite(), receiveReact(otherSocket), nowAttack)){
                        break
                    }
                }
            }
        }

        nowAttack.activeOtherBuff(this, attack_player, attackPlayer.otherBuff)
        val damage = nowAttack.getDamage(this, attack_player, attackPlayer.attackBuff)
        var selectedDamage: DamageSelect = DamageSelect.NULL

        gameLogger.insert(EventLog(attack_player, LogText.ATTACK_DAMAGE, damage.first, damage.second))

        if(endCurrentPhase){
            return
        }

        gameLogger.insert(EventLog(attack_player, LogText.DAMAGE_PROCESS_START, nowAttack.card_number, nowAttack.card_number))
        if(nowAttack.editedInevitable || attackRangeCheck(nowAttack, attack_player)){
            otherPlayer.isNextTurnTailWind = false
            if(nowAttack.isItValid){
                if(nowAttack.isItDamage){
                    if(nowAttack.beforeProcessDamageCheck(attack_player, this, nowAttack)){
                        if (nowAttack.effectText(attack_player, this, react_attack, TextEffectTag.CAN_NOT_CHOOSE_AURA_DAMAGE) == 1){
                            nowAttack.canNotSelectAura = true
                        }
                        gameLogger.insert(EventLog(attack_player, LogText.START_PROCESS_ATTACK_DAMAGE, nowAttack.card_number, -1))
                        val chosen = if(nowAttack.canNotSelectAura){
                            CommandEnum.CHOOSE_LIFE
                        } else if(nowAttack.effectText(attack_player, this, react_attack, TextEffectTag.SELECT_DAMAGE_BY_ATTACKER) == 1){
                            damageSelect(attack_player, CommandEnum.CHOOSE_CARD_DAMAGE_OTHER, damage, laceration = nowAttack.editedLaceration)
                        } else {
                            damageSelect(attack_player.opposite(), CommandEnum.CHOOSE_CARD_DAMAGE, damage, laceration = nowAttack.editedLaceration)
                        }
                        val auraReplace = nowAttack.effectText(attack_player, this, react_attack, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE)
                        val lifeReplace = nowAttack.effectText(attack_player, this, react_attack, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE)

                        if(nowAttack.editedLaceration){
                            selectedDamage = if(chosen == CommandEnum.CHOOSE_LIFE) DamageSelect.LIFE else DamageSelect.AURA
                            if(nowAttack.bothSideDamage){
                                addLacerationToken(attack_player.opposite(), attack_player, INDEX_LACERATION_AURA, damage.first)
                                addLacerationToken(attack_player.opposite(), attack_player, INDEX_LACERATION_LIFE, damage.second)
                            }
                            else if(chosen == CommandEnum.CHOOSE_LIFE){
                                addLacerationToken(attack_player.opposite(), attack_player, INDEX_LACERATION_LIFE, damage.second)
                            }
                            else{
                                addLacerationToken(attack_player.opposite(), attack_player, INDEX_LACERATION_AURA, damage.first)
                            }
                        }
                        else{
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
                        }
                        gameLogger.insert(EventLog(attack_player, LogText.END_EFFECT, nowAttack.card_number, -1))
                    }
                }
                nowAttack.afterAttackProcess(attack_player, this, react_attack, selectedDamage)
                afterResolveAttack(attack_player, selectedDamage, damage, nowAttack.card_number)
            }
        }

        if(endCurrentPhase){
            return
        }

        for(text in nowAttack.afterAttackCompleteEffect){
            if(text.tag == TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE){
                if(selectedDamage == DamageSelect.BOTH || selectedDamage == DamageSelect.AURA){
                    text.effect!!(nowAttack.card_number, attack_player, this, null)
                }
            }
            else{
                text.effect?.invoke(nowAttack.card_number, attack_player, this, react_attack)
            }
        }

        for(card in getPlayer(attack_player.opposite()).usedSpecialCard.values){
            card.effectAllValidEffect(attack_player.opposite(), this, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE)
        }

        for(card in getPlayer(attack_player.opposite()).enchantmentCard.values){
            card.effectAllValidEffect(attack_player.opposite(), this, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE)
        }
    }

    /**
     * @return 1. true means player's command is valid, so proceed remain process
     * 2. false means player command is invalid, so request again
     */
    private suspend fun reactProcess(react_player: PlayerEnum, react: Pair<CommandEnum, Int>, nowAttack: MadeAttack): Boolean{
        val reactPlayer = getPlayer(react_player)
        val location = when (react.first) {
            CommandEnum.REACT_USE_CARD_HAND -> LocationEnum.HAND
            CommandEnum.REACT_USE_CARD_SPECIAL -> LocationEnum.SPECIAL_CARD
            CommandEnum.REACT_USE_CARD_SOLDIER -> LocationEnum.READY_SOLDIER_ZONE
            CommandEnum.REACT_NO -> return true
            else -> return false
        }
        val card = getCardFrom(react_player, react.second, location)?: return false
        if(reactCheck(react_player, card, nowAttack) && useCardFrom(react_player, card, location, true, nowAttack, isCost = true, isConsume = true)){
            reactPlayer.thisTurnReact = true
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

    suspend fun addLacerationToken(getDamagePlayer: PlayerEnum, giveDamagePlayer: PlayerEnum, index: Int, number: Int){
        val damagePlayer = getPlayer(getDamagePlayer)
        val tokenList = damagePlayer.getLacerationToken(giveDamagePlayer)
        tokenList[index] += number
        sendMoveToken(getSocket(getDamagePlayer), getSocket(getDamagePlayer.opposite()),
            TokenEnum.getLaceration(getDamagePlayer, giveDamagePlayer), LocationEnum.OUT_OF_GAME, index.toLacerationLocation(),
            number, -1)
        if(damagePlayer.getTotalLacerationToken(INDEX_LACERATION_LIFE) >= damagePlayer.life){
            makeOneZoneLacerationToDamage(getDamagePlayer, null, INDEX_LACERATION_LIFE)
        }
    }

    suspend fun removeLacerationToken(player: PlayerEnum, tokenPlayer: PlayerEnum, index: Int, number: Int){
        val value = if(getPlayer(player).getLacerationToken(tokenPlayer)[index] < number){
            getPlayer(player).getLacerationToken(tokenPlayer)[index]
        } else number
        getPlayer(player).getLacerationToken(tokenPlayer)[index] -= value
        sendMoveToken(getSocket(player), getSocket(player.opposite()),
            TokenEnum.getLaceration(player, tokenPlayer), index.toLacerationLocation(), LocationEnum.OUT_OF_GAME,
            value, -1)
    }

    suspend fun processAllLacerationDamageCancelAble(player: PlayerEnum){
        val tokensYour = getPlayer(player).getLacerationToken(player)
        val tokensOther = getPlayer(player.opposite()).getLacerationToken(player)
        while(true){
            if(player1.getOnePlayersAllLacerationToken(turnPlayer) == 0 && player2.getOnePlayersAllLacerationToken(turnPlayer) == 0){
                break
            }
            when(receiveCardEffectSelect(player, NUMBER_LACERATION_DAMAGE_SELECT_CANCELABLE)){
                CommandEnum.SELECT_ONE -> {
                    if(tokensYour[INDEX_LACERATION_AURA] != 0){
                        makeOneZoneLacerationToDamage(player, player, INDEX_LACERATION_AURA)
                    }
                }
                CommandEnum.SELECT_TWO -> {
                    if(tokensYour[INDEX_LACERATION_FLARE] != 0){
                        makeOneZoneLacerationToDamage(player, player, INDEX_LACERATION_FLARE)
                    }
                }
                CommandEnum.SELECT_THREE -> {
                    if(tokensYour[INDEX_LACERATION_LIFE] != 0){
                        makeOneZoneLacerationToDamage(player, player, INDEX_LACERATION_LIFE)
                    }
                }
                CommandEnum.SELECT_FOUR -> {
                    if(tokensOther[INDEX_LACERATION_AURA] != 0){
                        makeOneZoneLacerationToDamage(player.opposite(), player, INDEX_LACERATION_AURA)
                    }
                }
                CommandEnum.SELECT_FIVE -> {
                    if(tokensOther[INDEX_LACERATION_FLARE] != 0){
                        makeOneZoneLacerationToDamage(player.opposite(), player, INDEX_LACERATION_FLARE)
                    }
                }
                CommandEnum.SELECT_SIX -> {
                    if(tokensOther[INDEX_LACERATION_LIFE] != 0){
                        makeOneZoneLacerationToDamage(player.opposite(), player, INDEX_LACERATION_LIFE)
                    }
                }
                CommandEnum.SELECT_NOT -> {
                    break
                }
                else -> {}
            }
        }
    }

    suspend fun processAllLacerationDamage(player: PlayerEnum){
        val tokensYour = getPlayer(player).getLacerationToken(player)
        val tokensOther = getPlayer(player.opposite()).getLacerationToken(player)
        while(true){
            if(player1.getOnePlayersAllLacerationToken(turnPlayer) == 0 && player2.getOnePlayersAllLacerationToken(turnPlayer) == 0){
                break
            }
            when(receiveCardEffectSelect(player, NUMBER_LACERATION_DAMAGE_SELECT)){
                CommandEnum.SELECT_ONE -> {
                    if(tokensYour[INDEX_LACERATION_AURA] != 0){
                        makeOneZoneLacerationToDamage(player, player, INDEX_LACERATION_AURA)
                    }
                }
                CommandEnum.SELECT_TWO -> {
                    if(tokensYour[INDEX_LACERATION_FLARE] != 0){
                        makeOneZoneLacerationToDamage(player, player, INDEX_LACERATION_FLARE)
                    }
                }
                CommandEnum.SELECT_THREE -> {
                    if(tokensYour[INDEX_LACERATION_LIFE] != 0){
                        makeOneZoneLacerationToDamage(player, player, INDEX_LACERATION_LIFE)
                    }
                }
                CommandEnum.SELECT_FOUR -> {
                    if(tokensOther[INDEX_LACERATION_AURA] != 0){
                        makeOneZoneLacerationToDamage(player.opposite(), player, INDEX_LACERATION_AURA)
                    }
                }
                CommandEnum.SELECT_FIVE -> {
                    if(tokensOther[INDEX_LACERATION_FLARE] != 0){
                        makeOneZoneLacerationToDamage(player.opposite(), player, INDEX_LACERATION_FLARE)
                    }
                }
                CommandEnum.SELECT_SIX -> {
                    if(tokensOther[INDEX_LACERATION_LIFE] != 0){
                        makeOneZoneLacerationToDamage(player.opposite(), player, INDEX_LACERATION_LIFE)
                    }
                }
                CommandEnum.SELECT_NOT -> {

                }
                else -> {}
            }
        }
    }

    private suspend fun makeOneZoneLacerationToDamage(getDamagePlayer: PlayerEnum, giveDamagePlayer: PlayerEnum?, index: Int){
        val damagePlayer = getPlayer(getDamagePlayer)
        when (index) {
            INDEX_LACERATION_FLARE -> {
                if(giveDamagePlayer == null){
                    val totalDamage = damagePlayer.getTotalLacerationToken(index)
                    removeLacerationToken(getDamagePlayer, PlayerEnum.PLAYER1, index, 999)
                    removeLacerationToken(getDamagePlayer, PlayerEnum.PLAYER2, index, 999)
                    gameLogger.insert(EventLog(getDamagePlayer, LogText.GET_FLARE_DAMAGE, totalDamage, NUMBER_SHISUI_SHISUI))
                    flareToDust(getDamagePlayer, totalDamage, Arrow.NULL, getDamagePlayer, getDamagePlayer, NUMBER_SHISUI_SHISUI)

                }
                else{
                    val totalDamage = damagePlayer.getLacerationToken(giveDamagePlayer)[index]
                    removeLacerationToken(getDamagePlayer, giveDamagePlayer, index, 999)
                    gameLogger.insert(EventLog(getDamagePlayer, LogText.GET_FLARE_DAMAGE, totalDamage, NUMBER_SHISUI_SHISUI))
                    flareToDust(getDamagePlayer, totalDamage, Arrow.NULL, getDamagePlayer, getDamagePlayer, NUMBER_SHISUI_SHISUI)
                }
            }
            INDEX_LACERATION_AURA -> {
                if(giveDamagePlayer == null){
                    val totalDamage = damagePlayer.getTotalLacerationToken(index)
                    removeLacerationToken(getDamagePlayer, PlayerEnum.PLAYER1, index, 999)
                    removeLacerationToken(getDamagePlayer, PlayerEnum.PLAYER2, index, 999)
                    processDamage(getDamagePlayer, CommandEnum.CHOOSE_AURA, Pair(totalDamage, 999), false,
                        null, null, NUMBER_SHISUI_SHISUI)
                }
                else{
                    val totalDamage = damagePlayer.getLacerationToken(giveDamagePlayer)[index]
                    removeLacerationToken(getDamagePlayer, giveDamagePlayer, index, 999)
                    processDamage(getDamagePlayer, CommandEnum.CHOOSE_AURA, Pair(totalDamage, 999), false,
                        null, null, NUMBER_SHISUI_SHISUI)
                }
            }
            INDEX_LACERATION_LIFE -> {
                if(giveDamagePlayer == null){
                    val totalDamage = damagePlayer.getTotalLacerationToken(index)
                    removeLacerationToken(getDamagePlayer, PlayerEnum.PLAYER1, index, 999)
                    removeLacerationToken(getDamagePlayer, PlayerEnum.PLAYER2, index, 999)
                    processDamage(getDamagePlayer, CommandEnum.CHOOSE_LIFE, Pair(999, totalDamage), false,
                        null, null, NUMBER_SHISUI_SHISUI)
                }
                else{
                    val totalDamage = damagePlayer.getLacerationToken(giveDamagePlayer)[index]
                    removeLacerationToken(getDamagePlayer, giveDamagePlayer, index, 999)
                    processDamage(getDamagePlayer, CommandEnum.CHOOSE_LIFE, Pair(999, totalDamage), false,
                        null, null, NUMBER_SHISUI_SHISUI)
                }
            }
            else -> {}
        }
        gameLogger.insert(EventLog(getDamagePlayer.opposite(), LogText.END_EFFECT, NUMBER_SHISUI_SHISUI, -1))
    }

    suspend fun movePlayingCard(player: PlayerEnum, place: LocationEnum?, card_number: Int): Boolean{
        val card = popCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR, true)?: return false

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
                CardClass.SOLDIER -> {
                    insertCardTo(card.player, card, LocationEnum.NOT_READY_SOLDIER_ZONE, true)
                }
                else -> {
                    insertCardTo(card.player, card, LocationEnum.DISCARD_YOUR, true)
                }
            }
        }

        return true
    }

    private suspend fun useAfterTriggerProcess(player: PlayerEnum, text: Text?, card_number: Int){
        if(text == null) return
        else{
            when(text.tag){
                TextEffectTag.WHEN_THIS_CARD_REACTED_AFTER -> {
                    text.effect!!(card_number, player.opposite(), this, null)
                }
                else -> {
                    text.effect!!(card_number, player, this, null)
                }
            }
        }
    }

    var cardForEffect: Card? = null

    suspend fun afterCardUsed(card_number: Int, player: PlayerEnum, thisCard: Card, cardMoveCancel: Boolean){
        cardForEffect = thisCard
        for(card in getPlayer(player).enchantmentCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_AFTER_CARD_USE)
        }
        if(!cardMoveCancel){
            if(thisCard.checkConditionText(player, this, null, TextEffectTag.WHEN_AFTER_CARD_USE_AND_MOVE_DISCARD_CONDITION) != null){
                thisCard.addEffect(TextEffectTag.WHEN_AFTER_CARD_USE_AND_MOVE_DISCARD, thisCard.cardUseEndEffect)
            }
        }
        cardForEffect = null

        gameLogger.insert(EventLog(PlayerEnum.PLAYER1, LogText.END_EFFECT, card_number, -1))

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

        if(!cardMoveCancel){
            movePlayingCard(player, null, card_number)
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
            CardClass.SOLDIER -> {
                insertCardTo(card.player, card, LocationEnum.NOT_READY_SOLDIER_ZONE, true)
            }
            else -> {
                insertCardTo(card.player, card, location, true)
            }
        }

        cardToDust(card.player, card.getNap(), card, false, EventLog.AFTER_DESTRUCTION_PROCESS)
        gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.AFTER_DESTRUCTION_PROCESS, -1))
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
            gameLogger.insert(EventLog(PlayerEnum.PLAYER1, LogText.END_EFFECT, card.card_number, -1))
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

        for(nowCard in player1.enchantmentCard.values){
            cardToDust(PlayerEnum.PLAYER1, 1, nowCard, true, EventLog.NORMAL_NAP_PROCESS)
            if(nowCard.isItDestruction()){
                player1Card[nowCard.card_number] = true
            }
        }

        for(nowCard in player2.enchantmentCard.values){
            cardToDust(PlayerEnum.PLAYER2, 1, nowCard, true, EventLog.NORMAL_NAP_PROCESS)
            if(nowCard.isItDestruction()){
                player2Card[nowCard.card_number] = true
            }
        }

        gameLogger.insert(EventLog(PlayerEnum.PLAYER1, LogText.END_EFFECT, EventLog.NORMAL_NAP_PROCESS, -1))

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
            val loseProtectCards = mutableListOf<Card>()

            for(card in loserPlayer.enchantmentCard.values){
                if(card.isThisCardHaveTag(TextEffectTag.CAN_NOT_LOSE)){
                    return
                }
            }

            for(card in loserPlayer.specialCardDeck.values){
                if(card.isThisCardHaveTag(TextEffectTag.WHEN_LOSE_GAME)){
                    loseProtectCards.add(card)
                }
            }
            for(card in loserPlayer.usedSpecialCard.values){
                if(card.isThisCardHaveTag(TextEffectTag.WHEN_LOSE_GAME)){
                    loseProtectCards.add(card)
                }
            }
            for(card in loserPlayer.enchantmentCard.values){
                if(card.isThisCardHaveTag(TextEffectTag.WHEN_LOSE_GAME_ENCHANTMENT)){
                    loseProtectCards.add(card)
                }
            }

            while(true){
                val selected = selectCardFrom(loser, loseProtectCards.map { card -> card.card_number }.toMutableList(),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SELECT_CARD_WHEN_LOSE_GAME, 1)
                if(selected.size > 1){
                    continue
                }
                else if(selected.size == 0){
                    break
                }
                else{
                    val card = loseProtectCards.filter {card -> card.card_number == selected[0]}[0]
                    if(card.effectText(loser, this, null, TextEffectTag.WHEN_LOSE_GAME) == 1){
                        if(!(loserPlayer.isLose())){
                            return
                        }
                    }
                    else if(card.effectText(loser, this, null, TextEffectTag.WHEN_LOSE_GAME_ENCHANTMENT) == 1){
                        if(!(loserPlayer.isLose())){
                            return
                        }
                    }
                    else{
                        continue
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

            for(card in loserPlayer.enchantmentCard.values){
                if(card.isThisCardHaveTag(TextEffectTag.CAN_NOT_LOSE)){
                    return
                }
            }

            for(card in winnerPlayer.enchantmentCard.values){
                if(card.effectAllValidEffect(winner, this, TextEffectTag.CAN_NOT_WIN) != 0){
                    return
                }
            }

            for(card in loserPlayer.specialCardDeck.values){
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
                if(data[index] == LocationEnum.AURA_YOUR.real_number){
                    if(replace == null){
                        auraToDust(player, data[index + 1], Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, card_number)
                    }
                    else{
                        moveTokenByInt(player, LocationEnum.AURA_YOUR.real_number,
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

    suspend fun damageSelect(player: PlayerEnum, command: CommandEnum, damage: Pair<Int, Int>, laceration: Boolean): CommandEnum{
        suspend fun damageSelect(get_damage_player: PlayerEnum, command: CommandEnum, damage: Pair<Int, Int>, select_player: PlayerEnum, laceration: Boolean): CommandEnum{
            if(damage.first == 999) return CommandEnum.CHOOSE_LIFE

            if(damage.second == 999) return CommandEnum.CHOOSE_AURA

            if(getPlayer(get_damage_player).checkAuraDamage(damage.first, laceration) == null) return CommandEnum.CHOOSE_LIFE

            sendChooseDamage(getSocket(select_player), command, damage.first, damage.second)

            return receiveChooseDamage(getSocket(select_player))
        }

        return if(command == CommandEnum.CHOOSE_CARD_DAMAGE_OTHER){
            damageSelect(player.opposite(), command, damage, player, laceration)
        }
        else{
            damageSelect(player, command, damage, player, laceration)
        }
    }

    suspend fun moveTokenByInt(player: PlayerEnum, from: Int, to: Int, number: Int, damage: Boolean, cardNumber: Int,
                               effectCard: Int){
        when(LocationEnum.fromInt(from)){
            LocationEnum.AURA_YOUR -> {
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
                            auraToDistance(player, number, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER1, effectCard)
                        }
                        LocationEnum.DUST -> {
                            auraToDust(player, number, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER1, effectCard)
                        }
                        else -> {
                            makeBugReportFile("moveTokenByInt() do not support aura to ${LocationEnum.fromInt(to)}")
                        }
                    }
                }
            }
            LocationEnum.LIFE_YOUR -> {
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
                        LocationEnum.DUST -> {
                            lifeToDust(player, number, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER1, effectCard)
                        }
                        else -> {
                            makeBugReportFile("moveTokenByInt() do not support life to ${LocationEnum.fromInt(to)}")
                        }
                    }
                }

            }
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD -> {
                if(to > 99){
                    cardToCard(player, number, getPlayer(player).enchantmentCard[cardNumber]!!,
                        getPlayer(player).enchantmentCard[to]!!, effectCard)
                }
                else{
                    when(LocationEnum.fromInt(to)){
                        LocationEnum.DISTANCE -> {
                            cardToDistance(player, number, getPlayer(player).enchantmentCard[cardNumber]!!, effectCard)
                        }
                        LocationEnum.DUST -> {
                            cardToDust(player, number, getPlayer(player).enchantmentCard[cardNumber]!!, false, effectCard)
                        }
                        else -> {
                            makeBugReportFile("moveTokenByInt() do not support card to ${LocationEnum.fromInt(to)}")
                        }
                    }
                }
            }
            else -> {
                makeBugReportFile("moveTokenByInt() do not support card to ${LocationEnum.fromInt(to)}")
            }
        }

    }

    //damage first = AURA, damage second = LIFE
    suspend fun processDamage(player: PlayerEnum, command: CommandEnum, damage: Pair<Int, Int>, reconstruct: Boolean,
        auraReplace: Int?, lifeReplace: Int?, card_number: Int): Int{
        val nowPlayer = getPlayer(player)

        var checkDamage = 0
        for(card in nowPlayer.usedSpecialCard.values){
            checkDamage += card.effectAllValidEffect(player, this, TextEffectTag.CAN_NOT_GET_DAMAGE)
        }
        if(checkDamage > 0){
            return -1
        }

        if(command == CommandEnum.CHOOSE_AURA){
            val selectable = nowPlayer.checkAuraDamage(damage.first, false)
            gameLogger.insert(EventLog(player, LogText.GET_AURA_DAMAGE, damage.first, card_number))
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.GET_DAMAGE_AURA_YOUR)
            if(selectable == null){
                auraDamageProcess(player, nowPlayer.getAllAuraDamageablePlace(), auraReplace, card_number)
            }
            else{
                if(selectable.size == 1){
                    if(selectable[0] == LocationEnum.AURA_YOUR.real_number){
                        if(auraReplace == null){
                            auraToDust(player, damage.first, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, card_number)
                        }
                        else{
                            moveTokenByInt(player, LocationEnum.AURA_YOUR.real_number,
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
                else if(selectable.size > 1){
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
            gameLogger.insert(EventLog(player, LogText.GET_LIFE_DAMAGE, damage.second, card_number))
            sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.GET_DAMAGE_LIFE_YOUR)
            if(!reconstruct){
                addMarketPrice(player.opposite())
                reduceMarketPrice(player)
            }
            if(lifeReplace == null){
                lifeToSelfFlare(player, damage.second, reconstruct, true, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, card_number)
            }
            else{
                moveTokenByInt(player, LocationEnum.LIFE_YOUR.real_number, lifeReplace, damage.second, true, -1, card_number)
            }

            if(!reconstruct) chasmProcess(player)
        }

        damageListenerProcess(player)
        return 1
    }

    suspend fun drawCard(player: PlayerEnum, number: Int){
        if(number < 1) return

        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        var numberToDraw = number

        if(nowPhase == START_PHASE){
            when(player){
                PlayerEnum.PLAYER1 -> {
                    if(player1NextStartPhaseDraw == 1){
                        numberToDraw = 1
                        player1NextStartPhaseDraw = 0
                    }
                }
                PlayerEnum.PLAYER2 -> {
                    if(player2NextStartPhaseDraw == 1){
                        numberToDraw = 1
                        player2NextStartPhaseDraw = 0
                    }
                }
            }
        }

        for(i in 1..numberToDraw){
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
                chojoDamageProcess(player)
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
        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        return nowPlayer.hand[card_number]?.let {
            if(Below) nowPlayer.normalCardDeck.addLast(it)
            else nowPlayer.normalCardDeck.addFirst(it)
            nowPlayer.hand.remove(card_number)
            sendHandToDeck(nowSocket, otherSocket, card_number, public, Below)
            true
        }?: false
    }

    private suspend fun removeArtificialToken(){
        when(turnPlayer){
            PlayerEnum.PLAYER1 -> {
                if(player1ArtificialTokenOn != 0) {
                    isThisTurnDistanceChange = true
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player1ArtificialTokenOn, -1)
                    player1.artificialTokenBurn += player1ArtificialTokenOn
                    player1ArtificialTokenOn = 0
                }
                if(player1ArtificialTokenOut != 0) {
                    isThisTurnDistanceChange = true
                    sendMoveToken(player1_socket, player2_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player1ArtificialTokenOut, -1)
                    player1.artificialTokenBurn += player1ArtificialTokenOut
                    player1ArtificialTokenOut = 0
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(player2ArtificialTokenOn != 0) {
                    isThisTurnDistanceChange = true
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player2ArtificialTokenOn, -1)
                    player2.artificialTokenBurn += player2ArtificialTokenOn
                    player2ArtificialTokenOn = 0
                }
                if(player2ArtificialTokenOut != 0) {
                    isThisTurnDistanceChange = true
                    sendMoveToken(player2_socket, player1_socket, TokenEnum.YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN, LocationEnum.DISTANCE,
                        LocationEnum.MACHINE_BURN_YOUR, player2ArtificialTokenOut, -1)
                    player2.artificialTokenBurn += player2ArtificialTokenOut
                    player2ArtificialTokenOut = 0
                }
            }
        }
    }

    var player1NextStartPhaseDraw = 2
    var player2NextStartPhaseDraw = 2

    suspend fun beforeStartPhaseEffectProcess(){
        startTurnDistance = getAdjustDistance()
        beforeDistance = startTurnDistance

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

    private suspend fun handleStartPhaseEffect(card_number: Int, effect: Pair<CardEffectLocation, Text?>){
        when(effect.first){
            CardEffectLocation.ENCHANTMENT_YOUR, CardEffectLocation.USED_YOUR -> {
                effect.second!!.effect!!(card_number, turnPlayer, this, null)
            }
            CardEffectLocation.ENCHANTMENT_OTHER, CardEffectLocation.USED_OTHER -> {
                effect.second!!.effect!!(card_number, turnPlayer.opposite(), this, null)
            }
            CardEffectLocation.ARTIFICIAL_TOKEN -> {
                removeArtificialToken()
            }
            CardEffectLocation.DIVING -> {
                divingProcess(turnPlayer, null)
            }
            CardEffectLocation.EFFECT_LACERATION -> {
                processAllLacerationDamage(turnPlayer)
            }
            else -> {
                makeBugReportFile("startPhaseEffectProcess() do not support effectLocation: ${effect.first}")
            }
        }
    }

    suspend fun startPhaseEffectProcess(){
        if(player1.getOnePlayersAllLacerationToken(turnPlayer) != 0 || player2.getOnePlayersAllLacerationToken(turnPlayer) != 0){
            startPhaseEffect[NUMBER_SHISUI_SHISUI] = Pair(CardEffectLocation.EFFECT_LACERATION, null)
        }

        val nowPlayer = getPlayer(turnPlayer)
        val otherPlayer = getPlayer(turnPlayer.opposite())

        when(turnPlayer){
            PlayerEnum.PLAYER1 -> {
                if(player1ArtificialTokenOn != 0 || player1ArtificialTokenOut != 0) {
                    startPhaseEffect[NUMBER_CARD_UNAME] = Pair(CardEffectLocation.ARTIFICIAL_TOKEN, null)
                }
                if(player1.forwardDiving != null){
                    startPhaseEffect[NUMBER_POISON_ANYTHING] = Pair(CardEffectLocation.DIVING, null)
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(player2ArtificialTokenOn != 0 || player2ArtificialTokenOut != 0) {
                    startPhaseEffect[NUMBER_CARD_UNAME] = Pair(CardEffectLocation.ARTIFICIAL_TOKEN, null)
                }
                if(player2.forwardDiving != null){
                    startPhaseEffect[NUMBER_POISON_ANYTHING] = Pair(CardEffectLocation.DIVING, null)
                }
            }
        }

        for(card in nowPlayer.enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_START_PHASE_YOUR)
        }
        for(card in nowPlayer.usedSpecialCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_START_PHASE_YOUR)
        }

        for(card in otherPlayer.enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer.opposite(), this, TextEffectTag.WHEN_START_PHASE_OTHER)
        }
        for(card in otherPlayer.usedSpecialCard.values){
            card.effectAllValidEffect(turnPlayer.opposite(), this, TextEffectTag.WHEN_START_PHASE_OTHER)
        }

        val keys = startPhaseEffect.keys.toMutableList()
        if(keys.isNotEmpty()){
            while(keys.size >= 2){
                val selected = receiveCardEffectOrder(getSocket(turnPlayer), CommandEnum.SELECT_START_PHASE_EFFECT_ORDER, keys)
                startPhaseEffect[selected]?.let { effect ->
                    handleStartPhaseEffect(selected, effect)
                    startPhaseEffect.remove(selected)
                    keys.remove(selected)
                    refreshEffectKey(startPhaseEffect, keys)
                }

                if(endCurrentPhase){
                    startPhaseEffect.clear()
                    return
                }
            }
            if(keys.size == 1){
                startPhaseEffect[keys[0]]?.let { effect ->
                    handleStartPhaseEffect(keys[0], effect)
                    startPhaseEffect.remove(keys[0])
                    keys.remove(keys[0])
                }
            }
        }
    }

    suspend fun startPhaseDefaultSecond(){
        addConcentration(turnPlayer)
        enchantmentReduceAll(turnPlayer)
        if(!endCurrentPhase){
            if(receiveReconstructRequest(getSocket(turnPlayer))){
                deckReconstruct(turnPlayer, true)
            }
            when(turnPlayer){
                PlayerEnum.PLAYER1 -> {
                    drawCard(turnPlayer, player1NextStartPhaseDraw)
                }
                PlayerEnum.PLAYER2 -> {
                    drawCard(turnPlayer, player2NextStartPhaseDraw)
                }
            }
        }
        player1NextStartPhaseDraw = 2
        player2NextStartPhaseDraw = 2
    }

    suspend fun mainPhaseEffectProcess(){
        //TODO("change this mechanism like endphaseeffectprocess(can choose order of effect)")
        val nowPlayer = getPlayer(turnPlayer)

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

        nowPlayer.megamiCard?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_MAIN_PHASE_YOUR)
        nowPlayer.megamiCard2?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_MAIN_PHASE_YOUR)

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

    suspend fun mainPhaseEndProcess(){
        val nowPlayer = getPlayer(turnPlayer)

        nowPlayer.megamiCard?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_MAIN_PHASE_END_YOUR)
        nowPlayer.megamiCard2?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_MAIN_PHASE_END_YOUR)
    }

    val endPhaseEffect = HashMap<Int, Pair<CardEffectLocation, Text?>>()
    val nextEndPhaseEffect = HashMap<Int, Pair<CardEffectLocation, Text?>>()
    val startPhaseEffect = HashMap<Int, Pair<CardEffectLocation, Text?>>()

    private fun cleanEndPhaseEffect(){
        endPhaseEffect.clear()
        for(key in nextEndPhaseEffect.keys){
            nextEndPhaseEffect[key]?.let {
                endPhaseEffect[key] = it
            }
        }
        nextEndPhaseEffect.clear()
    }

    private fun refreshEffectKey(effectHashMap: HashMap<Int, Pair<CardEffectLocation, Text?>>, keys: MutableList<Int>){
        val iterator = keys.iterator()
        val nowPlayer = getPlayer(turnPlayer)
        val otherPlayer = getPlayer(turnPlayer.opposite())
        while(iterator.hasNext()){
            val nowKey = iterator.next()
            effectHashMap[nowKey]?.let ret@{ effect ->
                when(effect.first){
                    CardEffectLocation.EFFECT_LACERATION, CardEffectLocation.ARTIFICIAL_TOKEN,
                    CardEffectLocation.IDEA_PLAYER1, CardEffectLocation.IDEA_PLAYER2,
                    CardEffectLocation.TEMP_PLAYER1, CardEffectLocation.TEMP_PLAYER2,
                    CardEffectLocation.DIVING, CardEffectLocation.MEGAMI_YOUR -> {}
                    CardEffectLocation.ENCHANTMENT_YOUR -> {
                        if(nowKey !in nowPlayer.enchantmentCard){
                            return@ret null
                        }
                    }
                    CardEffectLocation.DISCARD_YOUR -> {
                        if(!(nowPlayer.isDiscardHave(nowKey))){
                            return@ret null
                        }
                    }
                    CardEffectLocation.RETURN_YOUR -> {
                        if(nowKey !in nowPlayer.specialCardDeck){
                            return@ret null
                        }
                    }
                    CardEffectLocation.USED_YOUR -> {
                        if(nowKey !in nowPlayer.usedSpecialCard){
                            return@ret null
                        }
                    }
                    CardEffectLocation.ENCHANTMENT_OTHER -> {
                        if(nowKey !in otherPlayer.enchantmentCard){
                            return@ret null
                        }
                    }
                    CardEffectLocation.USED_OTHER -> {
                        if(nowKey !in otherPlayer.usedSpecialCard){
                            return@ret null
                        }
                    }
                }
                1
            }?: run {
                iterator.remove()
                effectHashMap.remove(nowKey)
            }
        }
    }

    private suspend fun handleEndPhaseEffect(card_number: Int, effect: Pair<CardEffectLocation, Text?>){
        when(effect.first){
            CardEffectLocation.RETURN_YOUR -> {
                returnSpecialCard(turnPlayer, card_number)
            }
            CardEffectLocation.ENCHANTMENT_YOUR, CardEffectLocation.MEGAMI_YOUR, CardEffectLocation.USED_YOUR,
            CardEffectLocation.DISCARD_YOUR ->{
                effect.second!!.effect!!(card_number, turnPlayer, this, null)
            }
            CardEffectLocation.ENCHANTMENT_OTHER, CardEffectLocation.USED_OTHER -> {
                effect.second!!.effect!!(card_number, turnPlayer.opposite(), this, null)
            }
            CardEffectLocation.IDEA_PLAYER1, CardEffectLocation.TEMP_PLAYER1 -> {
                effect.second!!.effect!!(card_number, PlayerEnum.PLAYER1, this, null)
            }
            CardEffectLocation.IDEA_PLAYER2, CardEffectLocation.TEMP_PLAYER2 -> {
                effect.second!!.effect!!(card_number, PlayerEnum.PLAYER2, this, null)
            }
            else -> {
                makeBugReportFile("endPhaseEffect() do not support effectLocation: ${effect.first}")
            }
        }
    }

    private suspend fun additionalCheck(){
        for(card in player1.usedSpecialCard.values){
            if(card.effectAllValidEffect(PlayerEnum.PLAYER1, this, TextEffectTag.WHEN_END_PHASE_ADDITIONAL_CHECK) == 1){
                break
            }
        }
        for(card in player2.usedSpecialCard.values){
            if(card.effectAllValidEffect(PlayerEnum.PLAYER2, this, TextEffectTag.WHEN_END_PHASE_ADDITIONAL_CHECK) == 1){
                break
            }
        }
    }

    private suspend fun endPhaseAdditionalCheck(){
        val keys = endPhaseEffect.keys.toMutableList()
        if(keys.isNotEmpty()){
            while(keys.size >= 2){
                val selected = receiveCardEffectOrder(getSocket(turnPlayer), CommandEnum.SELECT_END_PHASE_EFFECT_ORDER, keys)
                endPhaseEffect[selected]?.let { effect ->
                    handleEndPhaseEffect(selected, effect)
                    additionalCheck()
                    keys.remove(selected)
                    endPhaseEffect.remove(selected)
                    refreshEffectKey(endPhaseEffect, keys)
                }

                if(endCurrentPhase){
                    cleanEndPhaseEffect()
                    return
                }
            }

            if(keys.size == 1){
                endPhaseEffect[keys[0]]?.let { effect ->
                    handleEndPhaseEffect(keys[0], effect)
                    endPhaseEffect.remove(keys[0])
                    keys.remove(keys[0])
                }
            }
        }

        additionalCheck()
        cleanEndPhaseEffect()
    }

    suspend fun endPhaseEffect(player: PlayerEnum){
        val keys = endPhaseEffect.keys.toMutableList()
        if(keys.isNotEmpty()){
            while(keys.size >= 2){
                val selected = receiveCardEffectOrder(getSocket(player), CommandEnum.SELECT_END_PHASE_EFFECT_ORDER, keys)
                endPhaseEffect[selected]?.let { effect ->
                    handleEndPhaseEffect(selected, effect)
                    endPhaseEffect.remove(selected)
                    keys.remove(selected)
                    refreshEffectKey(endPhaseEffect, keys)
                }

                if(endCurrentPhase){
                    cleanEndPhaseEffect()
                    return
                }
            }

            if(keys.size == 1){
                endPhaseEffect[keys[0]]?.let { effect ->
                    handleEndPhaseEffect(keys[0], effect)
                    endPhaseEffect.remove(keys[0])
                    keys.remove(keys[0])
                }
            }
        }

        cleanEndPhaseEffect()
    }

    suspend fun endPhaseEffectProcess(){
        val nowPlayer = getPlayer(turnPlayer)
        val otherPlayer = getPlayer(turnPlayer.opposite())

        nowPlayer.usedCardReturn(this)
        var additionalCheck = false

        for(card in nowPlayer.enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_END_PHASE_YOUR)
        }
        for(card in nowPlayer.usedSpecialCard.values){
            card.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_END_PHASE_YOUR)
            if(additionalCheck || card.effectAllValidEffect(turnPlayer, this, TextEffectTag.END_PHASE_ADDITIONAL_CHECK) != 0){
                additionalCheck = true
            }
        }
        for(card in nowPlayer.discard){
            card.effectText(turnPlayer, this, null, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD)
        }

        for(card in otherPlayer.enchantmentCard.values){
            card.effectAllValidEffect(turnPlayer.opposite(), this, TextEffectTag.WHEN_END_PHASE_OTHER)
        }
        for(card in otherPlayer.usedSpecialCard.values){
            if(additionalCheck || card.effectAllValidEffect(turnPlayer.opposite(), this, TextEffectTag.END_PHASE_ADDITIONAL_CHECK) != 0){
                additionalCheck = true
                break
            }
        }

        nowPlayer.megamiCard?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_END_PHASE_YOUR)
        nowPlayer.megamiCard2?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_END_PHASE_YOUR)
        if(player1.canIdeaProcess){
            player1.ideaCard?.ideaProcess(PlayerEnum.PLAYER1, this, player1.isIdeaCardFlipped, null)
        }
        if(player2.canIdeaProcess){
            player2.ideaCard?.ideaProcess(PlayerEnum.PLAYER2, this, player2.isIdeaCardFlipped, null)
        }

        if(additionalCheck){
            endPhaseAdditionalCheck()
        }
        else{
            endPhaseEffect(turnPlayer)
        }
    }

    private suspend fun hatsumiTailWindClear(){
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
    }

    private suspend fun resetTurnValue(){
        gameLogger.reset()
        hatsumiTailWindClear()

        thisTurnSwellDistance = 2; thisTurnDistanceChangeValue = 0; isThisTurnDistanceChange = false
        isThisTurnDoAction = false
        player1.endTurn = false; player2.endTurn = false
        player1.didBasicOperation = false; player2.didBasicOperation = false
        player1.canNotGoForward = false; player2.canNotGoForward = false
        player1.rangeBuff.clearBuff(); player2.rangeBuff.clearBuff(); player1.attackBuff.clearBuff(); player2.attackBuff.clearBuff()
        player1.otherBuff.clearBuff(); player2.otherBuff.clearBuff()
        player1.lastTurnReact = player1.thisTurnReact; player2.lastTurnReact = player2.thisTurnReact
        player1.thisTurnReact = false; player2.thisTurnReact = false
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
        player1.isRecoupThisTurn = false; player2.isRecoupThisTurn = false
        player1.canNotUseConcentration = false; player2.canNotUseConcentration = false
        player1.canNotAttack = false; player2.canNotAttack = false
        for(i in perjuryCheck.indices){
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
            val receive = receiveSelectCard(nowSocket, list, CommandEnum.SELECT_CARD_REASON_INSTALLATION, -1)
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

    private suspend fun installationProcess(player: PlayerEnum){
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

    private suspend fun perjuryInstallationProcess(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        if(nowPlayer.coverCard.size == 0) return

        nowPlayer.perjuryInstallation?.let {
            val selectList = mutableListOf<Int>()
            for (cardName in it){
                if(perjuryCheck[cardName.toCardNumber(true) - NUMBER_RENRI_FALSE_STAB]){
                    selectList.add(cardName.toCardNumber(true))
                }
            }
            if(selectList.size > 0){
                while(true){
                    when(receiveCardEffectSelect(player, NUMBER_PERJURY_INSTALLATION_CHOOSE_USE_CARD)){
                        CommandEnum.SELECT_ONE -> {
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            return
                        }
                        else -> {}
                    }
                }
                val useCardNumber = selectCardFrom(player, selectList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_PERJURY_INSTALLATION_CHOOSE_USE_CARD)[0]
                val removeCardNumber = selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_PERJURY_INSTALLATION_CHOOSE_REAL_CARD, 1){_, _ -> true}!![0]
                getCardFrom(player, removeCardNumber, LocationEnum.COVER_CARD)?.let {removeCard ->
                    useCardPerjury(player, removeCard, useCardNumber, LocationEnum.COVER_CARD)
                }
            }
        }
    }

    private suspend fun disassembleAll(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        nowPlayer.assemblyZone?.let { parts ->
            for (card_number in parts.keys.toList()){
                popCardFrom(player, card_number, LocationEnum.ASSEMBLY_YOUR, true)?.let {
                    insertCardTo(player, it, LocationEnum.UNASSEMBLY_YOUR, true)
                }
            }
        }
    }

    private suspend fun digitalAttack(mainPart: Card, player: PlayerEnum) {
        val nowPlayer = getPlayer(player)

        val attack = mainPart.card_data.let {
            MadeAttack(it.card_name, mainPart.card_number, CardClass.NULL,
                it.getDistance(), it.auraDamage?: 0,  it.lifeDamage?: 0,  it.megami,
                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
            ).addTextAndReturn(nowPlayer.umbrella, it)
        }

        selectCardFrom(player, player, player,
            listOf(LocationEnum.ASSEMBLY_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_DIGITAL_INSTALLATION_CUSTOM
        ){card, _ -> card.card_data.card_class == CardClass.CUSTOM_PARTS}?.let { customPartsNumber ->
            for(card_number in customPartsNumber){
                getCardFrom(player, card_number, LocationEnum.ASSEMBLY_YOUR)?.let { card ->
                    sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.USE_CUSTOM_PARTS_YOUR, card.card_number)
                    card.useCustomPart(player, this, customPartsNumber.size, attack)
                }
            }
        }

        disassembleAll(player)

        if(addPreAttackZone(player, attack)){
            afterMakeAttack(attack.card_number, player, null)
        }
    }

    private suspend fun digitalInstallation(player: PlayerEnum): Boolean{
        val nowPlayer = getPlayer(player)
        nowPlayer.assemblyZone?.let ret@{assemblyZone ->
            if(assemblyZone.size == 0) return@ret null

            while(true){
                when(receiveCardEffectSelect(player, NUMBER_DIGITAL_INSTALLATION)){
                    CommandEnum.SELECT_ONE -> {
                        val mainPartCardNumber = selectCardFrom(player, player, player,
                            listOf(LocationEnum.ASSEMBLY_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_DIGITAL_INSTALLATION_MAIN, 1
                        ){card, _ -> card.card_data.card_class == CardClass.MAIN_PARTS}?.get(0) ?: return@ret null
                        showSome(player, CommandEnum.SHOW_ASSEMBLY_YOUR)
                        val mainPart = getCardFrom(player, mainPartCardNumber, LocationEnum.ASSEMBLY_YOUR)?: return@ret null
                        digitalAttack(mainPart, player)
                        return@ret 1
                    }
                    CommandEnum.SELECT_NOT -> {
                        return@ret null
                    }
                    else -> {}
                }
            }
        }?: return false
        return true
    }

    suspend fun deckReconstruct(player: PlayerEnum, damage: Boolean){
        if(digitalInstallation(player)){
            return
        }

        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        getPlayer(player).usedSpecialCard.values.forEach {card ->
            card.effectText(player, this, null, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR)
        }

        getPlayer(player).discard.forEach {card ->
            card.effectText(player, this, null, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR)
        }

        nowPlayer.megamiCard?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR)
        nowPlayer.megamiCard2?.effectAllValidEffect(turnPlayer, this, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR)

        perjuryInstallationProcess(player)
        installationProcess(player)

        if(endCurrentPhase){
            return
        }

        for(card in getPlayer(player).usedSpecialCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR_AFTER_INSTALLATION)
        }

        for(card in getPlayer(player).enchantmentCard.values){
            card.effectAllValidEffect(player, this, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR_AFTER_INSTALLATION)
        }

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
                    EventLog.DECK_RECONSTRUCT_DAMAGE)
                gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.DECK_RECONSTRUCT_DAMAGE, -1))
            }
        }
        Card.cardReconstructInsert(nowPlayer.discard, nowPlayer.coverCard, nowPlayer.normalCardDeck)

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
                if(getPlayer(player).megamiOne.real_number / 10 == MegamiEnum.NUMBER_RENRI_ORIGIN_NUMBER
                    || getPlayer(player).megamiTwo.real_number / 10 == MegamiEnum.NUMBER_RENRI_ORIGIN_NUMBER){
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

    suspend fun canUseConcentration(player: PlayerEnum): Boolean{
        if(getPlayer(player).canNotUseConcentration || getPlayer(player.opposite()).enchantmentCard.values.any {card ->
                card.effectAllValidEffect(player.opposite(), this, TextEffectTag.CAN_NOT_USE_CONCENTRATION_OTHER) == 1
            }) return false
        return true
    }

    private suspend fun payBasicOperationCost(player: PlayerEnum, card_number: Int): Boolean{
        val nowPlayer = getPlayer(player)

        if(card_number == -1){
            if(!canUseConcentration(player)){
                return false
            }
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

    private suspend fun basicOperationEnchantmentCheck(player: PlayerEnum, command: CommandEnum): Boolean{
        when(command){
            CommandEnum.ACTION_GO_BACKWARD ->  TextEffectTag.FORBID_GO_BACKWARD_OTHER
            CommandEnum.ACTION_BREAK_AWAY -> TextEffectTag.FORBID_BREAK_AWAY_OTHER
            CommandEnum.ACTION_INCUBATE -> TextEffectTag.FORBID_INCUBATE_OTHER
            else -> null
        }?.let {tag ->
            for(card in getPlayer(player.opposite()).enchantmentCard.values){
                if(card.effectAllValidEffect(player.opposite(), this, tag) != 0) return false
            }
        }

        when(command){
            CommandEnum.ACTION_GO_FORWARD -> TextEffectTag.FORBID_GO_FORWARD_YOUR
            CommandEnum.ACTION_BREAK_AWAY -> TextEffectTag.FORBID_BREAK_AWAY_YOUR
            else -> null
        }?.let { tag ->
            for(card in getPlayer(player).enchantmentCard.values){
                if(card.effectAllValidEffect(player, this, tag) != 0) return false
            }
        }

        return true
    }

    suspend fun requestAndDoBasicOperation(player: PlayerEnum, card_number: Int): CommandEnum {
        if(getEndTurn(player) || endCurrentPhase){
            return CommandEnum.NULL
        }

        while(true){
            val command = receiveBasicOperation(getSocket(player), card_number)
            if(command.isBasicOperationAddSelectNot()){
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

    suspend fun requestAndDoBasicOperation(player: PlayerEnum,
                                           card_number: Int, canNotSelect: HashSet<CommandEnum>): CommandEnum {
        while(true){
            val command = receiveBasicOperation(getSocket(player), card_number)
            if(command !in canNotSelect && command.isBasicOperationAddSelectNot()){
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
        if(nowPlayer.endTurn || endCurrentPhase){
            return false
        }
        for(transformCard in nowPlayer.transformZone.values){
            if(transformCard.effectAllValidEffect(player, this, TextEffectTag.FORBID_BASIC_OPERATION_YOUR) != 0) return false
        }

        if(getQuickChangeCard(player)?.let ret@{
            for(transformCard in it){
                if(transformCard.effectAllValidEffect(player, this, TextEffectTag.FORBID_BASIC_OPERATION_YOUR) != 0) return true
            }
                null
        }?: false){
            return false
        }

        return when(command){
            CommandEnum.ACTION_GO_FORWARD ->
                !(nowPlayer.aura + nowPlayer.freezeToken == nowPlayer.maxAura || distanceToken == 0 || getAdjustDistance() <= getAdjustSwellDistance())
                        && !(getPlayer(player).canNotGoForward)
            CommandEnum.ACTION_GO_BACKWARD -> {
                !(nowPlayer.aura == 0 || distanceToken == 10) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_GO_BACKWARD)
            }
            CommandEnum.ACTION_WIND_AROUND -> !(dust == 0 || nowPlayer.aura + nowPlayer.freezeToken == nowPlayer.maxAura ||
                    checkAdditionalBasicOperation(player, TextEffectTag.CONDITION_ADD_DO_WIND_AROUND))
            CommandEnum.ACTION_INCUBATE -> (nowPlayer.aura != 0 || nowPlayer.freezeToken != 0) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_INCUBATE)
            CommandEnum.ACTION_BREAK_AWAY -> {
                !(dust == 0 || getAdjustDistance() > getAdjustSwellDistance() || distanceToken == 10) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_BREAK_AWAY)
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

        if(nowPlayer.isNextBasicOperationInvalid){
            nowPlayer.isNextBasicOperationInvalid = false
            return
        }

        if(command != CommandEnum.ACTION_WIND_AROUND){
            nowPlayer.didBasicOperation = true
        }

        when(command){
            CommandEnum.ACTION_GO_FORWARD -> doGoForward(player, card)
            CommandEnum.ACTION_GO_BACKWARD -> doGoBackward(player, card)
            CommandEnum.ACTION_WIND_AROUND -> if(doWindAround(player, card)){
                nowPlayer.didBasicOperation = true
            }
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
            if(addPreAttackZone(
                    player, MadeAttack(CardName.FORM_ASURA, NUMBER_FORM_ASURA, CardClass.NULL,
                            sortedSetOf(3, 5), 3,  2, MegamiEnum.THALLYA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        ).addTextAndReturn(CardSet.attackAsuraText)
                ) ){
                afterMakeAttack(NUMBER_FORM_ASURA, player, null)
            }
            getPlayer(player).asuraUsed = true
        }
    }

    private suspend fun doYaksha(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_YAKSHA)){
            sendDoBasicAction(getSocket(player), getSocket(player.opposite()), CommandEnum.ACTION_YAKSHA_YOUR, card)
            if(addPreAttackZone(
                    player, MadeAttack(CardName.FORM_YAKSHA, NUMBER_FORM_YAKSHA, CardClass.NULL,
                            sortedSetOf(2, 4, 6, 8), 2,  1, MegamiEnum.THALLYA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        ).addTextAndReturn(CardSet.afterAttackManeuverBaseActionText)
                ) ){
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
                dustToDistance(1, Arrow.ONE_DIRECTION, player, player, EventLog.BASIC_OPERATION)
            }
        }

    private suspend fun doGoForward(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_GO_FORWARD)){
            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_GO_FORWARD_YOUR, card)
            distanceToAura(player, 1, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, EventLog.BASIC_OPERATION)
            for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                enchantmentCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
            }
        }
    }

    //this 5 function must call after check when select
    private suspend fun doGoBackward(player: PlayerEnum, card: Int){
        if(canDoBasicOperation(player, CommandEnum.ACTION_GO_BACKWARD)){
            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_GO_BACKWARD_YOUR, card)
            auraToDistance(player, 1 , Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, EventLog.BASIC_OPERATION)
            for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                enchantmentCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
            }
        }
    }

    //this 5 function must call after check when select
    private suspend fun doWindAround(player: PlayerEnum, card: Int): Boolean{
        if(canDoBasicOperation(player, CommandEnum.ACTION_WIND_AROUND)){
            var additionalCheck = 0
            for(usedCard in getPlayer(player).usedSpecialCard.values){
                additionalCheck += usedCard.effectAllValidEffect(player, this, TextEffectTag.WHEN_DO_WIND_AROUND)
                if(additionalCheck != 0) {
                    break
                }
            }
            if(additionalCheck != 0) return false

            val nowSocket = getSocket(player)
            val otherSocket = getSocket(player.opposite())

            sendDoBasicAction(nowSocket, otherSocket, CommandEnum.ACTION_WIND_AROUND_YOUR, card)
            dustToAura(player, 1, Arrow.NULL, PlayerEnum.PLAYER1 , PlayerEnum.PLAYER2, EventLog.BASIC_OPERATION)
            for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                enchantmentCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
            }
        }
        return true
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
                    LocationEnum.AURA_YOUR, LocationEnum.OUT_OF_GAME, 1, -1)
            }
            else{
                auraToFlare(player, player, 1, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, EventLog.BASIC_OPERATION)
                for(enchantmentCard in getPlayer(player.opposite()).enchantmentCard.values){
                    enchantmentCard.effectAllValidEffect(player.opposite(), this, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA)
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
            dustToDistance(1, Arrow.NULL, PlayerEnum.PLAYER1, PlayerEnum.PLAYER2, EventLog.BASIC_OPERATION)
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

    suspend fun endTurnHandCheck(){
        val nowPlayer = getPlayer(turnPlayer)

        while (true){
            if(nowPlayer.hand.size <= nowPlayer.maxHand) {
                nowPlayer.maxHand = 2
                return
            }
            coverCard(turnPlayer, turnPlayer, 0)
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
                        searchPlayer.insertCardNumberTwoCondition(location, cardList, condition) {
                                card -> !(card.isSoftAttack)
                        }
                    }
                    else{
                        searchPlayer.insertCardNumberOneCondition(location, cardList, condition, this.version)
                    }
                }
                LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                    otherPlayer.insertCardNumberOneCondition(location, cardList, condition, this.version)
                }
                else -> {
                    searchPlayer.insertCardNumberOneCondition(location, cardList, condition, this.version)
                }
            }
        }

        if(cardList.isEmpty()) return null

        while (true){
            return receiveSelectCard(getSocket(select_player), cardList, reason, card_number)
        }
    }

    /**
    * 1. use this function player cannot select card select number
     * 2. check softattack when wherever soft attack not in hand or cover
     */

    suspend fun selectCardFrom(player: PlayerEnum, select_player: PlayerEnum, user: PlayerEnum, location_list: List<LocationEnum>,
                               reason: CommandEnum, card_number: Int, listSize: Int, condition: suspend (Card, LocationEnum) -> Boolean): MutableList<Int>?{
        val cardList = mutableListOf<Int>()
        val searchPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())

        for (location in location_list){
            when(location){
                LocationEnum.HAND -> {
                    if(user != player){
                        searchPlayer.insertCardNumberTwoCondition(location, cardList, condition) {
                                card -> !(card.isSoftAttack)
                        }
                    }
                    else{
                        searchPlayer.insertCardNumberOneCondition(location, cardList, condition, this.version)
                    }
                }
                LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                    otherPlayer.insertCardNumberOneCondition(location, cardList, condition, this.version)
                }
                else -> {
                    searchPlayer.insertCardNumberOneCondition(location, cardList, condition, this.version)
                }
            }
        }

        if(cardList.isEmpty()) return null
        else if(cardList.size <= listSize) return cardList

        while (true){
            val set = mutableSetOf<Int>()
            val list = receiveSelectCard(getSocket(select_player), cardList, reason, card_number)
            set.addAll(list)
            if(set.size == listSize) return list
        }
    }

    suspend fun selectCardFrom(player: PlayerEnum, cardList: MutableList<Int>, reason: CommandEnum, card_number: Int,
        listSize: Int): MutableList<Int>{
        if(cardList.size <= listSize) return cardList
        while (true){
            val set = mutableSetOf<Int>()
            val list = receiveSelectCard(getSocket(player), cardList, reason, card_number)
            set.addAll(list)
            if(set.size == listSize) return list
        }
    }

    suspend fun selectCardFrom(player: PlayerEnum, cardList: MutableList<Int>, reason: CommandEnum, card_number: Int): MutableList<Int>{
        while (true) {
            return receiveSelectCard(getSocket(player), cardList, reason, card_number)
        }
    }

    /**
     * @param card_number is not related result when location is YOUR_DECK_TOP
     */
    suspend fun popCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum, public: Boolean,
                            discardCheck: Boolean = true): Card?{
        val nowPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())
        when(location){
            LocationEnum.COVER_CARD -> for(card in nowPlayer.coverCard) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_COVER_YOUR)
                nowPlayer.coverCard.remove(card)
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
                val result = nowPlayer.specialCardDeck[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_SPECIAL_YOUR)
                nowPlayer.specialCardDeck.remove(card_number)
                return result
            }
            LocationEnum.YOUR_USED_CARD -> {
                val result = nowPlayer.usedSpecialCard[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_USED_YOUR)
                nowPlayer.usedSpecialCard.remove(card_number)
                return result
            }
            LocationEnum.OTHER_USED_CARD -> {
                val result = otherPlayer.usedSpecialCard[card_number]?: return null
                sendPopCardZone(otherSocket, nowSocket, card_number, public, CommandEnum.POP_USED_YOUR)
                otherPlayer.usedSpecialCard.remove(card_number)
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
            LocationEnum.RELIC_YOUR -> {
                val result = nowPlayer.relic?.get(card_number)?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_RELIC_YOUR)
                nowPlayer.relic?.remove(card_number)
                return result
            }
            LocationEnum.RELIC_OTHER -> {
                val result = otherPlayer.relic?.get(card_number)?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_RELIC_OTHER)
                otherPlayer.relic?.remove(card_number)
                return result
            }
            LocationEnum.UNASSEMBLY_YOUR -> {
                val result = nowPlayer.unassemblyZone?.get(card_number)?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_UNASSEMBLY_YOUR)
                nowPlayer.unassemblyZone?.remove(card_number)
                return result
            }
            LocationEnum.UNASSEMBLY_OTHER -> {
                val result = otherPlayer.unassemblyZone?.get(card_number)?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_UNASSEMBLY_OTHER)
                otherPlayer.unassemblyZone?.remove(card_number)
                return result
            }
            LocationEnum.ASSEMBLY_YOUR -> {
                val result = nowPlayer.assemblyZone?.get(card_number)?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_ASSEMBLY_YOUR)
                nowPlayer.assemblyZone?.remove(card_number)
                return result
            }
            LocationEnum.ASSEMBLY_OTHER -> {
                val result = otherPlayer.assemblyZone?.get(card_number)?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_ASSEMBLY_OTHER)
                otherPlayer.assemblyZone?.remove(card_number)
                return result
            }
            else -> {
                makeBugReportFile("popCardFrom(cardNumber) do not support location: $location")
                return null
            }
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
            LocationEnum.RELIC_YOUR -> {
                val nowRelicZone = nowPlayer.relic?: return null
                val result = nowRelicZone.remove(card_name.toCardNumber(true))?:
                nowRelicZone.remove(card_name.toCardNumber(true))?: return null
                sendPopCardZone(nowSocket, otherSocket, result.card_number, public, CommandEnum.POP_RELIC_YOUR)
                return result
            }
            else -> {
                makeBugReportFile("popCardFrom(cardName) do not support location: $location")
                return null
            }
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
                card.location = LocationEnum.DECK
                cardOwner.normalCardDeck.addLast(card)
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.DECK_BELOW_YOUR, publicForYour)
            }
            LocationEnum.YOUR_DECK_TOP, LocationEnum.DECK -> {
                card.location = LocationEnum.DECK
                cardOwner.normalCardDeck.addFirst(card)
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.DECK_TOP_YOUR, publicForYour)
            }
            LocationEnum.DISCARD_YOUR, LocationEnum.DISCARD_OTHER -> {
                card.location = LocationEnum.DISCARD_YOUR
                cardOwner.discard.addFirst(card)
                if(discardCheck){
                    for(transformCard in getPlayer(card.player.opposite()).transformZone.values){
                        transformCard.effectAllValidEffect(card.player.opposite(), this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                    }
                    getQuickChangeCard(card.player.opposite())?.let {
                        for (transformCard in it){
                            transformCard.effectAllValidEffect(card.player.opposite(), this, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER)
                        }
                    }
                }
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.DISCARD_CARD_YOUR, publicForYour)
            }
            LocationEnum.PLAYING_ZONE_YOUR -> {
                card.location = LocationEnum.PLAYING_ZONE_YOUR
                nowPlayer.usingCard.addFirst(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.PLAYING_CARD_YOUR, publicForYour)
            }
            LocationEnum.YOUR_USED_CARD, LocationEnum.OTHER_USED_CARD -> {
                card.location = LocationEnum.YOUR_USED_CARD
                cardOwner.usedSpecialCard[card.card_number] = card
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.USED_CARD_YOUR, publicForYour)
            }
            LocationEnum.COVER_CARD -> {
                card.location = LocationEnum.COVER_CARD
                cardOwner.coverCard.addFirst(card)
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.COVER_CARD_YOUR, publicForYour)
            }
            LocationEnum.ENCHANTMENT_ZONE, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD -> {
                card.location = LocationEnum.ENCHANTMENT_ZONE
                nowPlayer.enchantmentCard[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.ENCHANTMENT_CARD_YOUR, publicForYour)
            }
            LocationEnum.SPECIAL_CARD -> {
                card.location = LocationEnum.SPECIAL_CARD
                cardOwner.specialCardDeck[card.card_number] = card
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.SPECIAL_YOUR, publicForYour)
            }
            LocationEnum.HAND -> {
                card.location = LocationEnum.HAND
                cardOwner.hand[card.card_number] = card
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.HAND_YOUR, publicForYour)
            }
            LocationEnum.SEAL_ZONE -> {
                card.location = LocationEnum.SEAL_ZONE
                nowPlayer.sealZone[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.SEAL_YOUR, publicForYour)
            }
            LocationEnum.POISON_BAG -> {
                card.location = LocationEnum.POISON_BAG
                if(card.card_number.isPoison()){
                    val poisonOwner = getPlayer(card.player.opposite())
                    val poisonOwnerSocket = getSocket(card.player.opposite())
                    val poisonOwnerOppositeSocket = getSocket(card.player)
                    poisonOwner.poisonBag[card.card_data.card_name] = card
                    sendAddCardZone(poisonOwnerSocket, poisonOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.POISON_BAG_YOUR, publicForYour)
                }
                else{
                    insertCardTo(player, card, LocationEnum.DISCARD_YOUR, true)
                }
            }
            LocationEnum.OUT_OF_GAME -> {
                card.location = LocationEnum.OUT_OF_GAME
                cardOwner.outOfGame[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.OUT_OF_GAME_YOUR, publicForYour)
            }
            LocationEnum.TRANSFORM -> {
                card.location = LocationEnum.YOUR_USED_CARD
                nowPlayer.transformZone[card.card_data.card_name] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.TRANSFORM_YOUR, publicForYour)
            }
            LocationEnum.ADDITIONAL_CARD -> {
                card.location = LocationEnum.ADDITIONAL_CARD
                cardOwner.additionalHand[card.card_data.card_name] = card
                sendAddCardZone(cardOwnerSocket, cardOwnerOppositeSocket, card.card_number, publicForOther, CommandEnum.ADDITIONAL_YOUR, publicForYour)
            }
            LocationEnum.NOT_READY_SOLDIER_ZONE -> {
                card.location = LocationEnum.NOT_READY_SOLDIER_ZONE
                nowPlayer.notReadySoldierZone[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.NOT_READY_SOLDIER_ZONE_YOUR, publicForYour)
            }
            LocationEnum.READY_SOLDIER_ZONE -> {
                card.location = LocationEnum.READY_SOLDIER_ZONE
                nowPlayer.readySoldierZone[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.READY_SOLDIER_ZONE_YOUR, publicForYour)
            }
            LocationEnum.IDEA_YOUR -> {
                card.location = LocationEnum.YOUR_USED_CARD
                nowPlayer.ideaCard = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.IDEA_YOUR, publicForYour)
            }
            LocationEnum.END_IDEA_YOUR -> {
                card.location = LocationEnum.END_IDEA_YOUR
                nowPlayer.endIdeaCards[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.END_IDEA_YOUR, publicForYour)
            }
            LocationEnum.ANVIL_YOUR -> {
                card.location = LocationEnum.ANVIL_YOUR
                nowPlayer.anvil = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.ANVIL_YOUR, publicForYour)
            }
            LocationEnum.MEMORY_YOUR -> {
                card.location = LocationEnum.MEMORY_YOUR
                nowPlayer.memory!![card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.MEMORY_YOUR, publicForYour)
            }
            LocationEnum.RELIC_YOUR -> {
                card.location = LocationEnum.RELIC_YOUR
                nowPlayer.relic?.let {
                    it[card.card_number] = card
                    sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.RELIC_YOUR, publicForYour)
                }
            }
            LocationEnum.RELIC_OTHER -> {
                card.location = LocationEnum.RELIC_YOUR
                getPlayer(player.opposite()).relic?.let {
                    it[card.card_number] = card
                    sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.RELIC_OTHER, publicForYour)
                }
            }
            LocationEnum.UNASSEMBLY_YOUR -> {
                card.location = LocationEnum.UNASSEMBLY_YOUR
                getPlayer(player).unassemblyZone?.let {
                    it[card.card_number] = card
                    sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.UNASSEMBLY_YOUR, publicForYour)
                }
            }
            LocationEnum.UNASSEMBLY_OTHER -> {
                card.location = LocationEnum.UNASSEMBLY_YOUR
                getPlayer(player.opposite()).unassemblyZone?.let {
                    it[card.card_number] = card
                    sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.UNASSEMBLY_OTHER, publicForYour)
                }
            }
            LocationEnum.ASSEMBLY_YOUR -> {
                card.location = LocationEnum.ASSEMBLY_YOUR
                getPlayer(player).assemblyZone?.let {
                    it[card.card_number] = card
                    sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.ASSEMBLY_YOUR, publicForYour)
                }
            }
            LocationEnum.ASSEMBLY_OTHER -> {
                card.location = LocationEnum.ASSEMBLY_YOUR
                getPlayer(player.opposite()).assemblyZone?.let {
                    it[card.card_number] = card
                    sendAddCardZone(nowSocket, otherSocket, card.card_number, publicForOther, CommandEnum.ASSEMBLY_OTHER, publicForYour)
                }
            }
            else -> {
                makeBugReportFile("insertCardTo() do not support location: $location")
            }
        }
    }

    suspend fun showSome(show_player: PlayerEnum, command: CommandEnum){
        val nowPlayer = getPlayer(show_player)
        val list = mutableListOf<Int>()
        when(command){
            CommandEnum.SHOW_COVER_YOUR -> {
                for(card in nowPlayer.coverCard) list.add(card.card_number)
            }
            CommandEnum.SHOW_HAND_YOUR -> list.addAll(nowPlayer.hand.keys)
            CommandEnum.SHOW_SPECIAL_YOUR -> list.addAll(nowPlayer.specialCardDeck.keys)
            CommandEnum.SHOW_ASSEMBLY_YOUR -> {
                nowPlayer.assemblyZone?.let { mainParts ->
                    list.addAll(mainParts.keys)
                }
            }
            else -> {
                makeBugReportFile("showSome() do not support command: $command")
            }
        }
        sendShowInformation(command, getSocket(show_player), getSocket(show_player.opposite()), list)
    }

    suspend fun showSome(show_player: PlayerEnum, command: CommandEnum, card_number: Int){
        val list = mutableListOf<Int>()
        when(command){
            CommandEnum.SHOW_HAND_SOME_YOUR -> list.add(card_number)
            CommandEnum.SHOW_DECK_TOP_YOUR -> list.add(card_number)
            else -> {
                makeBugReportFile("showSome() do not support command: $command")
            }
        }
        sendShowInformation(command, getSocket(show_player), getSocket(show_player.opposite()), list)
    }

    /**
     when YOUR_DECK_TOP -> card_number means top nth card (0 means top)
     */
    fun getCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum): Card?{
        val nowPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())
        return when(location){
            LocationEnum.HAND -> nowPlayer.getCardFromHand(card_number)
            LocationEnum.COVER_CARD -> nowPlayer.getCardFromCover(card_number)
            LocationEnum.DISCARD_YOUR -> nowPlayer.getCardFromDiscard(card_number)
            LocationEnum.DISCARD_OTHER -> otherPlayer.getCardFromDiscard(card_number)
            LocationEnum.SPECIAL_CARD -> nowPlayer.getCardFromSpecial(card_number)
            LocationEnum.YOUR_DECK_TOP -> nowPlayer.getCardFromDeckTop(card_number)
            LocationEnum.PLAYING_ZONE_YOUR -> nowPlayer.getCardFromPlaying(card_number)
            LocationEnum.PLAYING_ZONE_OTHER -> otherPlayer.getCardFromPlaying(card_number)
            LocationEnum.ADDITIONAL_CARD -> nowPlayer.getCardFromAdditional(card_number)
            LocationEnum.YOUR_USED_CARD -> nowPlayer.getCardFromUsed(card_number)
            LocationEnum.ENCHANTMENT_ZONE, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD -> {
                nowPlayer.getCardFromEnchantment(card_number)
            }
            LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                otherPlayer.getCardFromEnchantment(card_number)
            }
            LocationEnum.IDEA_YOUR -> {
                nowPlayer.ideaCard
            }
            LocationEnum.IDEA_OTHER -> {
                otherPlayer.ideaCard
            }
            LocationEnum.END_IDEA_YOUR -> {
                nowPlayer.endIdeaCards[card_number]
            }
            LocationEnum.END_IDEA_OTHER -> {
                otherPlayer.endIdeaCards[card_number]
            }
            LocationEnum.MEMORY_YOUR -> {
                nowPlayer.memory?.get(card_number)
            }
            LocationEnum.MEMORY_OTHER -> {
                otherPlayer.memory?.get(card_number)
            }
            LocationEnum.READY_SOLDIER_ZONE -> {
                nowPlayer.readySoldierZone[card_number]
            }
            LocationEnum.NOT_READY_SOLDIER_ZONE -> {
                nowPlayer.notReadySoldierZone[card_number]
            }
            LocationEnum.DECK -> {
                nowPlayer.normalCardDeck.filter {card ->
                    card.card_number == card_number
                }.getOrNull(0)
            }
            LocationEnum.HAND_OTHER -> {
                otherPlayer.getCardFromHand(card_number)
            }
            LocationEnum.OTHER_DECK_TOP -> {
                otherPlayer.getCardFromDeckTop(card_number)
            }
            LocationEnum.YOUR_DECK_BELOW -> {
                nowPlayer.getCardFromDeckBelow(card_number)
            }
            LocationEnum.OTHER_DECK_BELOW -> {
                otherPlayer.getCardFromDeckBelow(card_number)
            }
            LocationEnum.OTHER_USED_CARD -> {
                otherPlayer.getCardFromUsed(card_number)
            }
            LocationEnum.SEAL_ZONE -> {
                nowPlayer.sealZone[card_number]?: otherPlayer.sealZone[card_number]
            }
            LocationEnum.POISON_BAG -> {
                nowPlayer.poisonBag[card_number.toCardName()]
            }
            LocationEnum.TRANSFORM -> {
                nowPlayer.transformZone[card_number.toCardName()]
            }
            LocationEnum.ANVIL_YOUR -> {
                nowPlayer.anvil
            }
            LocationEnum.ANVIL_OTHER -> {
                otherPlayer.anvil
            }
            LocationEnum.ASSEMBLY_YOUR -> {
                nowPlayer.assemblyZone?.get(card_number)
            }
            LocationEnum.UNASSEMBLY_YOUR -> {
                nowPlayer.unassemblyZone?.get(card_number)
            }
            else -> null
        }
    }

    suspend fun getCardFrom(player: PlayerEnum, card_name: CardName, location: LocationEnum): Card?{
        return when(location){
            LocationEnum.ADDITIONAL_CARD -> getPlayer(player).getCardFromAdditional(card_name)
            else -> {
                makeBugReportFile("getCardFrom() do not support location: $location")
                null
            }
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

    suspend fun deckToCoverCard(player: PlayerEnum, numberToMove: Int){
        for(i in 1..numberToMove){
            popCardFrom(player, 0, LocationEnum.YOUR_DECK_TOP, false)?.let {
                insertCardTo(player, it, LocationEnum.COVER_CARD, false)
            }?: break
        }
    }

    fun getPlayingCardMegami(player: PlayerEnum, card_number: Int) =
        getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.card_data?.megami

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
        return if(card_number < SECOND_PLAYER_START_NUMBER) firstTurnPlayer
        else firstTurnPlayer.opposite()
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

    suspend fun moveOutCard(to_player: PlayerEnum, nameList: MutableSet<CardName>, to_location: LocationEnum){
        for(card_name in nameList){
            Card.cardMakerByName(getPlayer(to_player).firstTurn, card_name, to_player, to_location, this.version).let {
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
            else -> {}
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

    suspend fun sendCommand(player: PlayerEnum, commandEnum: CommandEnum, data: Int){
        sendSimpleCommand(getSocket(player), commandEnum, data)
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

    private suspend fun addMarketPrice(player: PlayerEnum){
        setMarketPrice(player, getPlayer(player).getMarketPrice() + 1)
    }

    private suspend fun reduceMarketPrice(player: PlayerEnum){
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

    suspend fun showPlayersSelectResult(player: PlayerEnum, original_card: Int, additional_information: Int){
        sendSimpleCommand(getSocket(player), CommandEnum.SHOW_SELECT_RESULT, original_card * 100000 + additional_information)
    }

    suspend fun chojoDamageProcess(player: PlayerEnum){
        var now = 0
        for(card in getPlayer(player.opposite()).enchantmentCard.values){
            now += card.effectAllValidEffect(player.opposite(), this, TextEffectTag.CHOJO_DAMAGE_CHANGE_OTHER)
            if(now > 0){
                break
            }
        }

        val aura = if(now == 0){
            1
        } else{
            now / 10
        }
        val life = if(now == 0){
            1
        } else{
            now % 10
        }

        val chosen = damageSelect(player, CommandEnum.CHOOSE_CHOJO, Pair(aura, life), laceration = false)
        processDamage(player, chosen, Pair(aura, life), false, null, null, EventLog.CHOJO)
        gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.CHOJO, -1))
    }

    suspend fun setAiming(player: PlayerEnum, number: Int){
        sendSimpleCommand(getSocket(player), getSocket(player.opposite()), CommandEnum.SET_AIMING_YOUR, number)
        if(number == -1){
            getPlayer(player).aiming = null
        }
        else{
            getPlayer(player).aiming = number
        }
    }
    //megami special function
}