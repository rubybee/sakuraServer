package com.sakurageto.gamelogic

import com.sakurageto.card.*
import com.sakurageto.card.CardSet.toCardData
import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.gamelogic.storyboard.Act
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

    var readySoldierZone= hashMapOf<Int, Card>()
    fun getCardFromSoldier(card_number: Int) = readySoldierZone[card_number]
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
    //for some card(some day refactor may be needed)


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

    fun getCardFromPlaying(card_number: Int): Card?{
        for(card in usingCard){
            if(card.card_number == card_number) return card
        }
        return null
    }

    var hand = HashMap<Int, Card>()

    fun getCardFromHand(card_number: Int): Card?{
        return hand[card_number]
    }

    var enchantmentCard: HashMap<Int, Card> = HashMap()

    fun getCardFromEnchantment(card_number: Int): Card?{
        return enchantmentCard[card_number]
    }

    var special_card_deck = HashMap<Int, Card>()

    var sealZone = HashMap<Int, Card>()
    var sealInformation = HashMap<Int, MutableList<Int>>()
    var outOfGame = HashMap<Int, Card>()

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

    var end_turn = false

    var life = 10
    var flare = 0

    var concentration = 0
    var max_concentration = 2
    var shrink = false

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


    lateinit var megami_1: MegamiEnum
    lateinit var megami_2: MegamiEnum
    lateinit var megami_ban: MegamiEnum

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

    var cost_buf: Array<ArrayDeque<CostBuff>> = arrayOf(
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
            BufTag.INSERT -> cost_buf[1].add(buf)
            BufTag.CHANGE_EACH -> cost_buf[3].add(buf)
            BufTag.MULTIPLE -> cost_buf[5].add(buf)
            BufTag.DIVIDE -> cost_buf[7].add(buf)
            BufTag.PLUS_MINUS -> cost_buf[9].add(buf)
            BufTag.INSERT_IMMEDIATE -> cost_buf[0].add(buf)
            BufTag.CHANGE_EACH_IMMEDIATE -> cost_buf[2].add(buf)
            BufTag.MULTIPLE_IMMEDIATE -> cost_buf[4].add(buf)
            BufTag.DIVIDE_IMMEDIATE -> cost_buf[6].add(buf)
            BufTag.PLUS_MINUS_IMMEDIATE -> cost_buf[8].add(buf)
            else -> cost_buf[11].add(buf)
        }
    }

    fun setMegamiSSangjang(data: SakuraSendData){
        megami_1 = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(10)
        }

        megami_2 = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(20)
        }

        if(megami_1 == megami_2){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
        }
    }

    fun setMegamiSamSep(data: SakuraSendData){
        megami_1 = try{
            MegamiEnum.fromInt(data.data?.get(0)?: 10)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(10)
        }

        megami_2 = try{
            MegamiEnum.fromInt(data.data?.get(1)?: 20)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(20)
        }

        megami_ban = try{
            MegamiEnum.fromInt(data.data?.get(2)?: 30)
        }catch (e: NoSuchElementException){
            MegamiEnum.fromInt(30)
        }

        if(megami_2 == megami_1){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
            megami_ban = MegamiEnum.SAINE
        }
        else if(megami_1 == megami_ban){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
            megami_ban = MegamiEnum.SAINE
        }
        else if(megami_2 == megami_ban){
            megami_1 = MegamiEnum.YURINA
            megami_2 = MegamiEnum.HIMIKA
            megami_ban = MegamiEnum.SAINE
        }
    }

    fun returnListMegami3(): MutableList<Int>{
        return mutableListOf(megami_1.real_number, megami_2.real_number, megami_ban.real_number)
    }

    fun banMegami(data: SakuraSendData){
        val ben_megami = data.data?.get(0)?: megami_1
        if (ben_megami != megami_ban.real_number){
            if(ben_megami == megami_1.real_number){
                megami_1 = megami_ban
            }
            else{
                megami_2 = megami_ban
            }
        }
    }

    fun makeMegamiData(command: CommandEnum): SakuraSendData {
        return SakuraSendData(command, mutableListOf(megami_1.real_number, megami_2.real_number))
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

    suspend fun insertCardNumber(location: LocationEnum, list: MutableList<Int>, condition: suspend (Card) -> Boolean){
        when(location){
            LocationEnum.DISCARD_YOUR -> for (card in discard) if(condition(card)) list.add(card.card_number)
            LocationEnum.DECK -> for (card in normalCardDeck) if(condition(card)) list.add(card.card_number)
            LocationEnum.HAND -> for (card in hand.values) if(condition(card)) list.add(card.card_number)
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.ENCHANTMENT_ZONE, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD  -> {
                for (card in enchantmentCard.values) if(condition(card)) list.add(card.card_number)
            }
            LocationEnum.COVER_CARD -> for (card in cover_card) if(condition(card)) list.add(card.card_number)
            LocationEnum.YOUR_USED_CARD -> for (card in usedSpecialCard.values) if(condition(card)) list.add(card.card_number)
            LocationEnum.ALL -> {
                list.addAll(megami_1.getAllNormalCardName().map { it.toCardNumber(true) })
                list.addAll(megami_2.getAllNormalCardName().map { it.toCardNumber(true) })
                list.addAll(megami_1.getAllAdditionalCardName().filter
                {it.toCardData().card_class == CardClass.NORMAL}.map
                {it.toCardNumber(true)})
                list.addAll(megami_2.getAllAdditionalCardName().filter
                {it.toCardData().card_class == CardClass.NORMAL}.map
                {it.toCardNumber(true)})
            }
            LocationEnum.NOT_READY_SOLDIER_ZONE -> list.addAll(notReadySoldierZone.keys)
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
            else -> TODO()
        }
    }
}