package com.sakurageto.gamelogic

import com.sakurageto.card.*
import com.sakurageto.card.CardSet.toCardData
import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.gamelogic.megamispecial.Stratagem
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.gamelogic.megamispecial.YatsuhaJourney
import com.sakurageto.gamelogic.megamispecial.storyboard.Act
import com.sakurageto.plugins.makeBugReportFile
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.SakuraArrayData
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap
import kotlin.collections.HashSet

class PlayerStatus(private val player_enum: PlayerEnum) {
    var firstTurn = false
    var fullAction = false
    var endTurn = false

    lateinit var megamiOne: MegamiEnum
    lateinit var megamiTwo: MegamiEnum
    lateinit var megamiBanned: MegamiEnum

    var life = 10
    var flare = 0
    var aura = 3
    var freezeToken = 0
    var maxAura = 5
    var maxHand = 2

    var concentration = 0
    var maxConcentration = 2
    var shrink = false

    var megamiCard: Card? = null
    var megamiCard2: Card? = null

    var normalCardDeck = ArrayDeque<Card>()
    var hand = HashMap<Int, Card>()
    var coverCard = ArrayDeque<Card>()
    var discard = ArrayDeque<Card>()
    var enchantmentCard: HashMap<Int, Card> = HashMap()
    var specialCardDeck = HashMap<Int, Card>()
    var usedSpecialCard = HashMap<Int, Card>()
    var additionalHand: EnumMap<CardName, Card> = EnumMap(CardName::class.java)
    var sealZone = HashMap<Int, Card>()
    var sealInformation = HashMap<Int, MutableList<Int>>()
    var outOfGame = HashMap<Int, Card>()

    var preAttackCard: MadeAttack? = null
    var usingCard = ArrayDeque<Card>()

    var unselectedCard: MutableSet<CardName> = mutableSetOf()
    var unselectedSpecialCard: MutableSet<CardName> = mutableSetOf()

    //for megami(must be present)
    var umbrella: Umbrella? = null

    var stratagem: Stratagem? = null

    var poisonBag: EnumMap<CardName, Card> = EnumMap(CardName::class.java)

    var artificialToken: Int? = null
    var artificialTokenBurn: Int = 0
    var transformZone: EnumMap<CardName, Card> = EnumMap(CardName::class.java)

    var windGauge: Int? = null
    var thunderGauge: Int? = null

    var isThisTurnTailWind: Boolean = true
    var isNextTurnTailWind: Boolean = true
    var divingSuccess = false

    var readySoldierZone= hashMapOf<Int, Card>()
    var notReadySoldierZone = hashMapOf<Int, Card>()
    var notReadySeed: Int? = null

    var nowAct: Act? = null
    var tempIdeaProcess: Boolean = false
    var ideaProcess: Boolean = false
    var beforeTurnIdeaProcess: Boolean = false
    var ideaCard: Card? = null
    var isIdeaCardFlipped: Boolean = false
    var ideaCardStage = 0
    var endIdeaCards = HashMap<Int, Card>()
    var canIdeaProcess: Boolean = true
    var perjuryInstallation: HashSet<CardName>? = null
    var relic: HashMap<Int, Card>? = null

    var canNotUseCardName1: Pair<Int, CardName>? = null
    var canNotUseCardName2: Pair<Int, CardName>? = null

    var tabooGauge: Int? = null

    var anvil: Card? = null

    var forwardDiving: Boolean? = null

    var journey: YatsuhaJourney? = null
    var memory: HashMap<Int, Card>? = null

    var marketPrice: Int? = null

    var aiming: Int? = null

    var unassemblyZone: HashMap<Int, Card>? = null
    var assemblyZone: HashMap<Int, Card>? = null

    fun getAssemblyZoneSize() = assemblyZone?.size ?: 0


    fun getMarketPrice() = marketPrice?: 1
    var flow: Int? = null
    fun getCapital() = aura + flare + (flow?: 0)

    //aura, flare, life
    private val lacerationTokenForPlayer1 = mutableListOf(0, 0, 0)
    private val lacerationTokenForPlayer2 = mutableListOf(0, 0, 0)
    fun getLacerationToken(player: PlayerEnum) = when(player){
        PlayerEnum.PLAYER1 -> lacerationTokenForPlayer1
        PlayerEnum.PLAYER2 -> lacerationTokenForPlayer2
    }
    fun getTotalLacerationToken(index: Int) = lacerationTokenForPlayer1[index] + lacerationTokenForPlayer2[index]
    fun getOnePlayersAllLacerationToken(player: PlayerEnum): Int{
        val token = getLacerationToken(player)
        return token[INDEX_LACERATION_AURA] + token[INDEX_LACERATION_FLARE] + token[INDEX_LACERATION_LIFE]
    }

    //for some card(some day refactor may be needed)
    var thisTurnReact = false
    var lastTurnReact = false
    var transformNumber = 0
    var asuraUsed = false
    var canNotCharge = false
    var readySeed: Int = 0
    var nextEnchantmentGrowing = 0
    var justRunStratagem: Boolean = false
    var isNextBasicOperationInvalid = false
    var isMoveDistanceToken = false
    var loseCounter = 0
    var canNotGoForward: Boolean = false
    var didBasicOperation: Boolean = false
    var napBuff = 0
    var nextMainPhaseSkip = false
    var nextCostAddMegami: MegamiEnum? = null
    var afterCardUseTermination: Boolean = false
    var isUseCard: Boolean = false
    var isRecoupThisTurn: Boolean = false
    var canNotUseConcentration: Boolean = false
    var canNotAttack: Boolean = false

    //buff queue
    var otherBuff: OtherBuffQueue = OtherBuffQueue()
    var attackBuff: AttackBuffQueue = AttackBuffQueue()
    var rangeBuff: RangeBuffQueue = RangeBuffQueue()
    var costBuff: Array<ArrayDeque<CostBuff>> = arrayOf(
        ArrayDeque(), ArrayDeque(), ArrayDeque(), ArrayDeque(), ArrayDeque(), ArrayDeque(),
        ArrayDeque(), ArrayDeque(), ArrayDeque(), ArrayDeque(), ArrayDeque())

    //game init function
    fun megamiOneNormalForm() = megamiOne.changeNormalMegami()
    fun setMegamiSsangjang(data: SakuraArrayData){
        megamiOne = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            println("Error: Invalid MegamiNumber: ${data.data?.get(0)?: 10}")
            MegamiEnum.fromInt(10)
        }

        megamiTwo = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            println("Error: Invalid MegamiNumber: ${data.data?.get(1)?: 20}")
            MegamiEnum.fromInt(20)
        }

        if(megamiOne == megamiTwo){
            megamiOne = MegamiEnum.YURINA
            megamiTwo = MegamiEnum.HIMIKA
        }
    }

    fun setMegamiSamSep(data: SakuraArrayData){
        megamiOne = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            println("Error: Invalid MegamiNumber: ${data.data?.get(0)?: 10}")
            MegamiEnum.fromInt(10)
        }

        megamiTwo = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            println("Error: Invalid MegamiNumber: ${data.data?.get(1)?: 20}")
            MegamiEnum.fromInt(20)
        }

        megamiBanned = try{
            MegamiEnum.fromInt(data.data?.get(2)?: 30)
        }catch (e: NoSuchElementException){
            println("Error: Invalid MegamiNumber: ${data.data?.get(2)?: 30}")
            MegamiEnum.fromInt(30)
        }

        if(megamiTwo == megamiOne){
            megamiOne = MegamiEnum.YURINA
            megamiTwo = MegamiEnum.HIMIKA
            megamiBanned = MegamiEnum.SAINE
        }
        else if(megamiOne == megamiBanned){
            megamiOne = MegamiEnum.YURINA
            megamiTwo = MegamiEnum.HIMIKA
            megamiBanned = MegamiEnum.SAINE
        }
        else if(megamiTwo == megamiBanned){
            megamiOne = MegamiEnum.YURINA
            megamiTwo = MegamiEnum.HIMIKA
            megamiBanned = MegamiEnum.SAINE
        }
    }

    fun returnListMegami3(): MutableList<Int>{
        return mutableListOf(megamiOne.real_number, megamiTwo.real_number, megamiBanned.real_number)
    }

    fun banMegami(data: SakuraArrayData){
        val benMegami = data.data?.get(0)?: megamiOne
        if (benMegami != megamiBanned.real_number){
            if(benMegami == megamiOne.real_number){
                megamiOne = megamiBanned
            }
            else{
                megamiTwo = megamiBanned
            }
        }
    }

    fun makeMegamiData(command: CommandEnum): SakuraArrayData {
        return SakuraArrayData(command, mutableListOf(megamiOne.real_number, megamiTwo.real_number))
    }

    fun deleteSelectedNormalCard(cards: MutableList<CardName>){
        cards.forEach {card ->
            unselectedCard.remove(card)
        }
    }

    fun deleteSelectedSpecialCard(cards: MutableList<CardName>){
        cards.forEach {card ->
            unselectedSpecialCard.remove(card)
        }
    }

    //game logic function(get card information)
    fun getCardFromHand(card_number: Int) = hand[card_number]
    fun getCardFromEnchantment(card_number: Int) = enchantmentCard[card_number]
    fun getCardFromSpecial(card_number: Int) = specialCardDeck[card_number]
    fun getCardFromUsed(card_number: Int) = usedSpecialCard[card_number]
    fun getCardFromAdditional(card_name: CardName) = additionalHand[card_name]

    fun getCardFromAdditional(card_number: Int): Card?{
        return additionalHand[card_number.toCardName()]
    }

    fun getCardFromDeckTop(index: Int): Card?{
        if(normalCardDeck.size > index) return normalCardDeck[index]
        return null
    }

    fun getCardFromDeckBelow(index: Int): Card?{
        if(normalCardDeck.size > index) return normalCardDeck[normalCardDeck.size - index - 1]
        return null
    }

    fun getCardFromPlaying(card_number: Int): Card?{
        for(card in usingCard){
            if(card.card_number == card_number) return card
        }
        return null
    }

    fun getCardFromDiscard(card_number: Int): Card?{
        for(card in discard){
            if(card.card_number == card_number) return card
        }
        return null
    }

    fun getCardFromCover(card_number: Int): Card?{
        for(card in coverCard){
            if(card.card_number == card_number) return card
        }
        return null
    }

    //game logic function(concentration)
    /**
    0 success add conentration, 1 fail because shrink, 2 can not plus because full
     */
    internal fun addConcentration(): Int{
        if(shrink){
            shrink = false
            return 1
        }
        else if(concentration < maxConcentration){
            concentration += 1
            return 0
        }
        return 2
    }

    internal fun decreaseConcentration(): Boolean{
        if(concentration == 0) return false
        concentration -= 1
        return true
    }

    //game logic function(used when make user choose card list for card effect)
    suspend fun insertCardNumberTwoCondition(location: LocationEnum, destList: MutableList<Int>, condition: suspend (Card, LocationEnum) -> Boolean,
                                             condition2: suspend (Card) -> Boolean) {
        when (location) {
            LocationEnum.DISCARD_YOUR -> for (card in discard) {
                if (condition(card, location) && condition2(card)) {
                    destList.add(card.card_number)
                }
            }

            LocationEnum.DECK -> for (card in normalCardDeck) {
                if (condition(card, location) && condition2(card)) {
                    destList.add(card.card_number)
                }
            }

            LocationEnum.HAND -> for (card in hand.values) {
                if (condition(card, location) && condition2(card)) {
                    destList.add(card.card_number)
                }
            }

            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.ENCHANTMENT_ZONE, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                for (card in enchantmentCard.values) {
                    if (condition(card, location) && condition2(card)) {
                        destList.add(card.card_number)
                    }
                }
            }

            LocationEnum.COVER_CARD -> for (card in coverCard) {
                if (condition(card, location) && condition2(card)) {
                    destList.add(card.card_number)
                }
            }

            LocationEnum.YOUR_USED_CARD -> for (card in usedSpecialCard.values) {
                if (condition(card, location) && condition2(card)) {
                    destList.add(card.card_number)
                }
            }

            LocationEnum.NOT_READY_SOLDIER_ZONE ->
                for (card in notReadySoldierZone.values) {
                    if (condition(card, location) && condition2(card)) {
                        destList.add(card.card_number)
                    }
                }
            LocationEnum.MEMORY_YOUR -> {
                memory?.values?.forEach{
                    if(condition(it, location) && condition2(it)){
                        destList.add(it.card_number)
                    }
                }
            }
            LocationEnum.ASSEMBLY_YOUR -> {
                assemblyZone?.values?.forEach {
                    if(condition(it, location) && condition2(it)){
                        destList.add(it.card_number)
                    }
                }
            }
            LocationEnum.UNASSEMBLY_YOUR -> {
                unassemblyZone?.values?.forEach {
                    if(condition(it, location) && condition2(it)){
                        destList.add(it.card_number)
                    }
                }
            }
            else -> {
                makeBugReportFile("insertCardNumberTwoCondition() do not support location: $location")
            }
        }
    }

    suspend fun insertCardNumberOneCondition(location: LocationEnum, destList: MutableList<Int>,
                                             condition: suspend (Card, LocationEnum) -> Boolean, version: GameVersion){
        when(location){
            LocationEnum.DISCARD_YOUR -> for (card in discard) {
                if(condition(card, location)) {
                    destList.add(card.card_number)
                }
            }
            LocationEnum.DECK -> for (card in normalCardDeck) {
                if(condition(card, location)) {
                    destList.add(card.card_number)
                }
            }
            LocationEnum.HAND -> for (card in hand.values) {
                if(condition(card, location)) {
                    destList.add(card.card_number)
                }
            }
            LocationEnum.SPECIAL_CARD -> for (card in specialCardDeck.values) {
                if(condition(card, location)){
                    destList.add(card.card_number)
                }
            }
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.ENCHANTMENT_ZONE, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD  -> {
                for (card in enchantmentCard.values){
                    if(condition(card, location)){
                        destList.add(card.card_number)
                    }
                }
            }
            LocationEnum.COVER_CARD -> for (card in coverCard) {
                if(condition(card, location)){
                    destList.add(card.card_number)
                }
            }
            LocationEnum.YOUR_USED_CARD -> for (card in usedSpecialCard.values){
                if(condition(card, location)){
                    destList.add(card.card_number)
                }
            }
            LocationEnum.NOT_READY_SOLDIER_ZONE ->
                for(card in notReadySoldierZone.values){
                    if(condition(card, location)){
                        destList.add(card.card_number)
                    }
                }
            LocationEnum.ALL_NORMAL -> {
                destList.addAll(megamiOne.getAllNormalCardName(version).map { it.toCardNumber(true) })
                destList.addAll(megamiTwo.getAllNormalCardName(version).map { it.toCardNumber(true) })
                destList.addAll(megamiOne.getAllAdditionalCardName().filter
                {it.toCardData(version).card_class == CardClass.NORMAL}.map
                {it.toCardNumber(true)})
                destList.addAll(megamiTwo.getAllAdditionalCardName().filter
                {it.toCardData(version).card_class == CardClass.NORMAL}.map
                {it.toCardNumber(true)})
            }
            LocationEnum.ALL -> {
                if(megamiTwo == MegamiEnum.RENRI){
                    destList.add(NUMBER_TOKOYO_KUON)
                    destList.add(NUMBER_SHINRA_WANJEON_NONPA)
                    destList.add(NUMBER_UTSURO_MANG_A)
                }
                else if(megamiOne == MegamiEnum.RENRI){
                    destList.add(NUMBER_TOKOYO_KUON)
                    destList.add(NUMBER_SHINRA_WANJEON_NONPA)
                    destList.add(NUMBER_UTSURO_MANG_A)
                }
                destList.addAll(megamiOne.getAllNormalCardName(version).map { it.toCardNumber(true) })
                destList.addAll(megamiTwo.getAllNormalCardName(version).map { it.toCardNumber(true) })
                destList.addAll(megamiOne.getAllSpecialCardName(version).map { it.toCardNumber(true) })
                destList.addAll(megamiTwo.getAllSpecialCardName(version).map { it.toCardNumber(true) })
                destList.addAll(megamiOne.getAllAdditionalCardName().map {it.toCardNumber(true)})
                destList.addAll(megamiTwo.getAllAdditionalCardName().map {it.toCardNumber(true)})
            }
            LocationEnum.NOT_SELECTED_NORMAL_CARD -> {
                unselectedCard.forEach{
                    if(condition(Card.cardMakerByName(this.firstTurn, it, player_enum, LocationEnum.OUT_OF_GAME, version), location)){
                        destList.add(it.toCardNumber(true))
                    }
                }
            }
            LocationEnum.NOT_SELECTED_NORMAL -> {
                unselectedCard.forEach{
                    destList.add(it.toCardNumber(true))
                }
            }
            LocationEnum.NOT_SELECTED_SPECIAL -> {
                unselectedSpecialCard.forEach{
                    destList.add(it.toCardNumber(true))
                }
            }
            LocationEnum.ADDITIONAL_CARD -> {
                additionalHand.values.forEach{
                    if(condition(it, location)){
                        destList.add(it.card_number)
                    }
                }
            }
            LocationEnum.MEMORY_YOUR -> {
                memory?.values?.forEach{
                    if(condition(it, location)){
                        destList.add(it.card_number)
                    }
                }
            }
            LocationEnum.ASSEMBLY_YOUR -> {
                assemblyZone?.values?.forEach {
                    if(condition(it, location)){
                        destList.add(it.card_number)
                    }
                }
            }
            LocationEnum.UNASSEMBLY_YOUR -> {
                unassemblyZone?.values?.forEach {
                    if(condition(it, location)){
                        destList.add(it.card_number)
                    }
                }
            }
            else -> {
                makeBugReportFile("insertCardNumberOneCondition() do not support location: $location")
            }
        }
    }

    //game logic function
    fun isLose() = (tabooGauge?: 0) >= 16 || life == 0

    fun setMaxAura(arrow: Arrow, user: PlayerEnum) {
        if(user == player_enum && arrow != Arrow.NULL){
            for(card in usedSpecialCard.values){
                card.card_data.effect?.let {
                    for(text in it){
                        if(text.tag == TextEffectTag.TOKOYO_EIGHT_SAKURA){
                            maxAura = 8
                            return
                        }
                    }
                }
            }
        }
    }

    fun checkAuraFull(): Boolean = aura + freezeToken >= maxAura

    fun auraDamagePossible(data: MutableList<Int>?, damage: Int, possibleList: MutableList<Int>): Boolean{
        var totalAura = 0
        if(data == null || data.size % 2 == 1) return false
        else{
            val duplicateTest = mutableSetOf<Int>()
            for (index in data.indices){
                if(index % 2 == 0){
                    if(duplicateTest.contains(data[index])) return false
                    duplicateTest.add(data[index])
                }
            }
            for (index in data.indices){
                if(index % 2 == 0){
                    if(!possibleList.contains(data[index])) return false
                    totalAura += if(data[index] == LocationEnum.AURA_YOUR.real_number){
                        if(data[index + 1] <= this.aura) data[index + 1]
                        else return false
                    } else{
                        if(data[index + 1] <= enchantmentCard[data[index]]!!.getNap()!!) data[index + 1]
                        else return false
                    }
                }
            }
        }
        if(totalAura == damage) return true
        return false
    }

    fun checkAuraDamage(damage: Int, laceration: Boolean): MutableList<Int>?{
        val selectable = mutableListOf<Int>()
        var totalAura = this.aura
        if(!laceration){
            if(this.aura > 0){
                selectable.add(LocationEnum.AURA_YOUR.real_number)
            }

            enchantmentCard.values.forEach{ card ->
                if(card.checkAuraReplaceable()){
                    card.getNap()?.let { nap ->
                        totalAura += nap
                        selectable.add(card.card_number)
                    }
                }
            }
        }
        if(totalAura >= damage) return selectable
        return null
    }

    fun getAllAuraDamageablePlace(): MutableList<Int>{
        val selectable = mutableListOf<Int>()
        if(this.aura > 0){
            selectable.add(LocationEnum.AURA_YOUR.real_number)
            selectable.add(this.aura)
        }

        enchantmentCard.values.forEach { card ->
            if(card.checkAuraReplaceable()){
                card.getNap()?.let { nap ->
                    selectable.add(card.card_number)
                    selectable.add(nap)
                }
            }
        }

        return selectable
    }

    suspend fun usedCardReturn(game_status: GameStatus){
        for (cardNumber in usedSpecialCard.keys){
            if (usedSpecialCard[cardNumber]!!.returnCheck(player_enum, game_status)){
                game_status.endPhaseEffect[cardNumber] = Pair(CardEffectLocation.RETURN_YOUR, null)
            }
        }
    }

    fun infiniteInstallationCheck(): Boolean{
        for (card in usedSpecialCard.values){
            if (card.isItInstallationInfinite()) return true
        }
        return false
    }

    fun getInstallationCard(): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        for(card in coverCard){
            if(card.isItInstallation()){
                cardList.add(card.card_number)
            }
        }
        return cardList
    }

    fun haveSpecificMegami(megami: MegamiEnum) = megamiOne == megami || megamiTwo == megami

    fun addPreAttackZone(madeAttack: MadeAttack){
        preAttackCard = madeAttack
    }

    fun addCostBuff(buf: CostBuff){
        when(buf.tag){
            BufTag.INSERT -> costBuff[1].add(buf)
            BufTag.CHANGE_EACH -> costBuff[3].add(buf)
            BufTag.MULTIPLE -> costBuff[5].add(buf)
            BufTag.DIVIDE -> costBuff[7].add(buf)
            BufTag.PLUS_MINUS -> costBuff[9].add(buf)
            BufTag.INSERT_IMMEDIATE -> costBuff[0].add(buf)
            BufTag.CHANGE_EACH_IMMEDIATE -> costBuff[2].add(buf)
            BufTag.MULTIPLE_IMMEDIATE -> costBuff[4].add(buf)
            BufTag.DIVIDE_IMMEDIATE -> costBuff[6].add(buf)
            BufTag.PLUS_MINUS_IMMEDIATE -> costBuff[8].add(buf)
            else -> costBuff[11].add(buf)
        }
    }

    fun isDiscardHave(find_name: CardName) = discard.any {
        it.card_data.card_name == find_name
    }

    fun isDiscardHave(card_number: Int) = discard.any {
        it.card_number == card_number
    }

    fun isDeckHave(find_name: CardName) = normalCardDeck.any {
        it.card_data.card_name == find_name
    }

    fun isCoverHave(find_name: CardName) = coverCard.any {
        it.card_data.card_name == find_name
    }
}