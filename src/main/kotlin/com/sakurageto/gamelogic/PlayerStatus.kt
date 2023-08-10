package com.sakurageto.gamelogic

import com.sakurageto.card.*
import com.sakurageto.card.CardSet.toCardData
import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.gamelogic.megamispecial.YatsuhaJourney
import com.sakurageto.gamelogic.megamispecial.storyboard.Act
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.SakuraSendData
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap

class PlayerStatus(private val player_enum: PlayerEnum) {
    var firstTurn = false

    var fullAction = false

    var maxHand = 2
    var aura = 3
    var freezeToken = 0

    var maxAura = 5

    //for megami(must be present)
    var umbrella: Umbrella? = null

    var stratagem: Stratagem? = null

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

    var canNotUseCardName1: Pair<Int, CardName>? = null
    var canNotUseCardName2: Pair<Int, CardName>? = null

    var tabooGauge: Int? = null

    var anvil: Card? = null

    var forwardDiving: Boolean? = null

    var journey: YatsuhaJourney? = null
    var memory: HashMap<Int, Card>? = null

    var marketPrice: Int? = null
    fun getMarketPrice() = marketPrice?: 0
    var flow: Int = 0
    //for megami(must be present)


    //for some card(some day refactor may be needed)
    var thisTurnReact = false
    var lastTurnReact = false
    var transformNumber = 0
    var asuraUsed = false
    var notCharge = false
    var readySeed: Int = 0
    var nextEnchantmentGrowing = 0
    var justRunNoCondition: Boolean = false
    var isNextBasicOperationInvalid = false
    var isMoveDistanceToken = false
    var loseCounter = false
    var canNotGoForward: Boolean = false
    var didBasicOperation: Boolean = false
    var napBuff = 0
    var nextMainPhaseSkip = false
    var nextCostAddMegami: MegamiEnum? = null
    var afterCardUseTermination: Boolean = false
    var isUseCard: Boolean = false
    var isRecoupThisTurn: Boolean = false
    //for some card(some day refactor may be needed)

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
                    totalAura += if(data[index] == LocationEnum.YOUR_AURA.real_number){
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

    var usingCard = ArrayDeque<Card>()



    var hand = HashMap<Int, Card>()

    var enchantmentCard: HashMap<Int, Card> = HashMap()

    var special_card_deck = HashMap<Int, Card>()

    var sealZone = HashMap<Int, Card>()
    var sealInformation = HashMap<Int, MutableList<Int>>()
    var outOfGame = HashMap<Int, Card>()

    var end_turn = false

    var life = 10
    var flare = 0

    var concentration = 0
    var max_concentration = 2
    var shrink = false

    fun getCardFromSoldier(card_number: Int) = readySoldierZone[card_number]

    fun getCardFromPlaying(card_number: Int): Card?{
        for(card in usingCard){
            if(card.card_number == card_number) return card
        }
        return null
    }

    fun getCardFromHand(card_number: Int): Card?{
        return hand[card_number]
    }

    fun getCardFromEnchantment(card_number: Int): Card?{
        return enchantmentCard[card_number]
    }

    fun getFullAuraDamage(): MutableList<Int>{
        val selectable = mutableListOf<Int>()
        if(this.aura > 0){
            selectable.add(LocationEnum.YOUR_AURA.real_number)
            selectable.add(this.aura)
        }
        for(card in enchantmentCard.values){
            if(card.checkAuraReplaceable()){
                selectable.add(card.card_number)
                selectable.add(card.getNap()!!)
            }
        }
        return selectable
    }

    fun checkAuraDamage(damage: Int): MutableList<Int>?{
        val selectable = mutableListOf<Int>()
        var totalAura = this.aura
        if(this.aura > 0){
            selectable.add(LocationEnum.YOUR_AURA.real_number)
        }
        for(card in enchantmentCard.values){
            if(card.checkAuraReplaceable()){
                totalAura += card.getNap()!!
                selectable.add(card.card_number)
            }
        }
        if(totalAura >= damage) return selectable
        return null
    }

    fun getCardFromSpecial(card_number: Int): Card?{
        return special_card_deck[card_number]
    }

    var normalCardDeck = ArrayDeque<Card>()
    var usedSpecialCard = HashMap<Int, Card>()

    fun getCardFromUsed(index: Int): Card?{
        return usedSpecialCard[index]
    }

    fun getCardFromDeckTop(index: Int): Card?{
        if(normalCardDeck.size > index) return normalCardDeck[index]
        return null
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

    var discard = ArrayDeque<Card>()

    fun getCardFromDiscard(card_number: Int): Card?{
        for(card in discard){
            if(card.card_number == card_number) return card
        }
        return null
    }

    var cover_card = ArrayDeque<Card>()

    fun getCardFromCover(card_number: Int): Card?{
        for(card in cover_card){
            if(card.card_number == card_number) return card
        }
        return null
    }

    fun getInstallationCard(): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        for(card in cover_card){
            if(card.isItInstallation()){
                cardList.add(card.card_number)
            }
        }
        return cardList
    }

    //0 success add conentration, 1 fail because shrink, 2 can not plus because full
    fun addConcentration(): Int{
        if(shrink){
            shrink = false
            return 1
        }
        else if(concentration < max_concentration){
            concentration += 1
            return 0
        }
        return 2
    }

    fun decreaseConcentration(): Boolean{
        if(concentration == 0) return false
        concentration -= 1
        return true
    }


    lateinit var megamiOne: MegamiEnum
    lateinit var megamiTwo: MegamiEnum
    lateinit var megamiBanned: MegamiEnum


    fun megamiOneNormalForm() = megamiOne.changeNormalMegami()

    fun haveSpecificMegami(megami: MegamiEnum) = megamiOne == megami || megamiTwo == megami

    var megamiCard: Card? = null
    var megamiCard2: Card? = null

    var unselectedCard: MutableList<CardName> = mutableListOf()
    var unselectedSpecialCard: MutableList<CardName> = mutableListOf()

    var additionalHand: EnumMap<CardName, Card> = EnumMap(CardName::class.java)
    fun getCardFromAdditional(card_name: CardName): Card?{
        return additionalHand[card_name]
    }
    fun getCardFromAdditional(card_number: Int): Card?{
        return additionalHand[card_number.toCardName()]
    }
    var poisonBag: EnumMap<CardName, Card> = EnumMap(CardName::class.java)

    var pre_attack_card: MadeAttack? = null

    fun addPreAttackZone(madeAttack: MadeAttack){
        pre_attack_card = madeAttack
    }

    var otherBuff: OtherBuffQueue = OtherBuffQueue()
    var attackBuff: AttackBuffQueue = AttackBuffQueue()
    var rangeBuff: RangeBuffQueue = RangeBuffQueue()
    var costBuff: Array<ArrayDeque<CostBuff>> = arrayOf(
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque(),
        ArrayDeque()
    )

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

    fun setMegamiSSangjang(data: SakuraSendData){
        megamiOne = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(10)
        }

        megamiTwo = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(20)
        }

        if(megamiOne == megamiTwo){
            megamiOne = MegamiEnum.YURINA
            megamiTwo = MegamiEnum.HIMIKA
        }
    }

    fun setMegamiSamSep(data: SakuraSendData){
        megamiOne = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(10)
        }

        megamiTwo = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(20)
        }

        megamiBanned = try{
            MegamiEnum.fromInt(data.data?.get(2)?: 30)
        }catch (e: NoSuchElementException){
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

    fun banMegami(data: SakuraSendData){
        val ben_megami = data.data?.get(0)?: megamiOne
        if (ben_megami != megamiBanned.real_number){
            if(ben_megami == megamiOne.real_number){
                megamiOne = megamiBanned
            }
            else{
                megamiTwo = megamiBanned
            }
        }
    }

    fun makeMegamiData(command: CommandEnum): SakuraSendData {
        return SakuraSendData(command, mutableListOf(megamiOne.real_number, megamiTwo.real_number))
    }

    fun deleteNormalUsedCard(card: MutableList<CardName>){
        for(name in card){
            val now = unselectedCard.indexOf(name)
            if(now != -1){
                unselectedCard.removeAt(now)
            }
        }
    }

    fun deleteSpeicalUsedCard(card: MutableList<CardName>){
        for(name in card){
            val now = unselectedCard.indexOf(name)
            if(now != -1){
                unselectedSpecialCard.removeAt(now)
            }
        }
    }

    suspend fun insertCardNumberPlusCondition(location: LocationEnum, list: MutableList<Int>, condition: suspend (Card, LocationEnum) -> Boolean,
        condition2: suspend (Card) -> Boolean) {
        when (location) {
            LocationEnum.DISCARD_YOUR -> for (card in discard) {
                if (condition(card, location) && condition2(card)) {
                    list.add(card.card_number)
                }
            }

            LocationEnum.DECK -> for (card in normalCardDeck) {
                if (condition(card, location) && condition2(card)) {
                    list.add(card.card_number)
                }
            }

            LocationEnum.HAND -> for (card in hand.values) {
                if (condition(card, location) && condition2(card)) {
                    list.add(card.card_number)
                }
            }

            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.ENCHANTMENT_ZONE, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD -> {
                for (card in enchantmentCard.values) {
                    if (condition(card, location) && condition2(card)) {
                        list.add(card.card_number)
                    }
                }
            }

            LocationEnum.COVER_CARD -> for (card in cover_card) {
                if (condition(card, location) && condition2(card)) {
                    list.add(card.card_number)
                }
            }

            LocationEnum.YOUR_USED_CARD -> for (card in usedSpecialCard.values) {
                if (condition(card, location) && condition2(card)) {
                    list.add(card.card_number)
                }
            }

            LocationEnum.NOT_READY_SOLDIER_ZONE ->
                for (card in notReadySoldierZone.values) {
                    if (condition(card, location) && condition2(card)) {
                        list.add(card.card_number)
                    }
                }
            LocationEnum.MEMORY_YOUR -> {
                memory?.values?.forEach{
                    if(condition(it, location) && condition2(it)){
                        list.add(it.card_number)
                    }
                }
            }
            else -> throw Exception("location: $location not supported")
        }
    }

    suspend fun insertCardNumber(location: LocationEnum, list: MutableList<Int>, condition: suspend (Card, LocationEnum) -> Boolean){
        when(location){
            LocationEnum.DISCARD_YOUR -> for (card in discard) {
                if(condition(card, location)) {
                    list.add(card.card_number)
                }
            }
            LocationEnum.DECK -> for (card in normalCardDeck) {
                if(condition(card, location)) {
                    list.add(card.card_number)
                }
            }
            LocationEnum.HAND -> for (card in hand.values) {
                if(condition(card, location)) {
                    list.add(card.card_number)
                }
            }
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.ENCHANTMENT_ZONE, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD  -> {
                for (card in enchantmentCard.values){
                    if(condition(card, location)){
                        list.add(card.card_number)
                    }
                }
            }
            LocationEnum.COVER_CARD -> for (card in cover_card) {
                if(condition(card, location)){
                    list.add(card.card_number)
                }
            }
            LocationEnum.YOUR_USED_CARD -> for (card in usedSpecialCard.values){
                if(condition(card, location)){
                    list.add(card.card_number)
                }
            }
            LocationEnum.NOT_READY_SOLDIER_ZONE ->
                for(card in notReadySoldierZone.values){
                    if(condition(card, location)){
                        list.add(card.card_number)
                    }
                }
            LocationEnum.ALL_NORMAL -> {
                list.addAll(megamiOne.getAllNormalCardName().map { it.toCardNumber(true) })
                list.addAll(megamiTwo.getAllNormalCardName().map { it.toCardNumber(true) })
                list.addAll(megamiOne.getAllAdditionalCardName().filter
                {it.toCardData().card_class == CardClass.NORMAL}.map
                {it.toCardNumber(true)})
                list.addAll(megamiTwo.getAllAdditionalCardName().filter
                {it.toCardData().card_class == CardClass.NORMAL}.map
                {it.toCardNumber(true)})
            }
            LocationEnum.ALL -> {
                if(megamiTwo == MegamiEnum.RENRI){
                    list.add(707); list.add(407); list.add(1313)
                }
                else if(megamiOne == MegamiEnum.RENRI){
                    list.add(707); list.add(407); list.add(1313)
                }
                list.addAll(megamiOne.getAllNormalCardName().map { it.toCardNumber(true) })
                list.addAll(megamiTwo.getAllNormalCardName().map { it.toCardNumber(true) })
                list.addAll(megamiOne.getAllSpecialCardName().map { it.toCardNumber(true) })
                list.addAll(megamiTwo.getAllSpecialCardName().map { it.toCardNumber(true) })
                list.addAll(megamiOne.getAllAdditionalCardName().map {it.toCardNumber(true)})
                list.addAll(megamiTwo.getAllAdditionalCardName().map {it.toCardNumber(true)})
            }
            LocationEnum.NOT_SELECTED_NORMAL_CARD -> {
                unselectedCard.forEach{
                    if(condition(Card.cardMakerByName(this.firstTurn, it, player_enum), location)){
                        list.add(it.toCardNumber(true))
                    }
                }
            }
            LocationEnum.NOT_SELECTED_NORMAL -> {
                unselectedCard.forEach{
                    list.add(it.toCardNumber(true))
                }
            }
            LocationEnum.NOT_SELECTED_SPECIAL -> {
                unselectedSpecialCard.forEach{
                    list.add(it.toCardNumber(true))
                }
            }
            LocationEnum.ADDITIONAL_CARD -> {
                additionalHand.values.forEach{
                    if(condition(it, location)){
                        list.add(it.card_number)
                    }
                }
            }
            LocationEnum.MEMORY_YOUR -> {
                memory?.values?.forEach{
                    if(condition(it, location)){
                        list.add(it.card_number)
                    }
                }
            }
            else -> throw Exception("location: $location not supported")
        }
    }

    suspend fun deckToCoverCard(game_status: GameStatus, numberToMove: Int){
        var index = 0
        for(i in 1..numberToMove){
            normalCardDeck.getOrNull(index)?.let let@{
                if(it.card_data.canCover) {
                    val card = game_status.popCardFrom(player_enum, it.card_number, LocationEnum.DECK, false)?: return@let
                    game_status.insertCardTo(player_enum, card, LocationEnum.COVER_CARD, false)
                }
                else {
                    index += 1
                    return@let
                }
            }?: break
        }
    }
}