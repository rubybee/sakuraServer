package com.sakurageto.card

import com.sakurageto.card.CardSet.toCardData
import com.sakurageto.card.basicenum.*
import com.sakurageto.gamelogic.*
import com.sakurageto.gamelogic.buff.other.OtherBuffQueue
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.gamelogic.log.GameLog
import com.sakurageto.gamelogic.log.LogEnum
import com.sakurageto.gamelogic.megamispecial.storyboard.Act
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.receiveNapInformation
import java.util.SortedSet
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap

class Card(val card_number: Int, var card_data: CardData, val player: PlayerEnum,
           var special_card_state: SpecialCardEnum?, var location: LocationEnum){
    private var nap: Int? = null
    private var seedToken: Int = 0
    var beforeCardData: CardData? = null
    var cardUseEndEffect = HashMap<Int, Text>()

    /***
     1. can not select by other user's card effect if your soft attack in your hand
     2. your soft attack not in hand or cover can not move to hand or deck except reconstruct
     3. soft attack can not seal
     */
    var isSoftAttack = false
    var numberForX = 0
    var flipped = false

    fun getNap() = nap
    fun getSeedToken() = seedToken

    suspend fun reduceNap(player: PlayerEnum, game_status: GameStatus, number: Int): Pair<Int, Int>{
        var value = number
        if((nap?: 0) < number){
            value = nap?: 0
        }
        val sakuraToken = (nap?: 0) - seedToken

        nap = (nap?: 0) - value

        effectText(player, game_status, null, TextEffectTag.WHEN_MOVE_TOKEN)

        return if(sakuraToken >= value){
            Pair(value, 0)
        } else{
            seedToken = seedToken - value + sakuraToken
            Pair(sakuraToken, value - sakuraToken)
        }
    }

    fun addNap(number: Int, seed: Boolean = false) {
        if(number == 0) return

        nap = nap?.let {
            it + number
        }?: number

        if(seed){
            seedToken += number
        }
    }

    companion object{
        fun cardMakerByName(start_turn: Boolean, card_name: CardName, player: PlayerEnum, location: LocationEnum,
                            version: GameVersion): Card{
            val data = card_name.toCardData(version)
            return if (data.isItSpecial()){
                if(start_turn){
                    Card(card_name.toCardNumber(true), data, player, SpecialCardEnum.UNUSED, location)
                } else{
                    Card(card_name.toCardNumber(false), data, player, SpecialCardEnum.UNUSED, location)
                }

            }
            else{
                if(start_turn){
                    Card(card_name.toCardNumber(true), data, player, null, location)
                } else{
                    Card(card_name.toCardNumber(false), data, player, null, location)
                }

            }
        }

        fun cardInitInsert(start_turn: Boolean, dest: ArrayDeque<Card>, src: MutableList<CardName>, player: PlayerEnum,
                           version: GameVersion){
            src.shuffle()
            for(card_name in src){
                dest.add(cardMakerByName(start_turn, card_name, player, LocationEnum.DECK, version))
            }
        }

        fun cardInitInsert(start_turn: Boolean, dest: HashMap<Int, Card>, src: MutableList<CardName>, player: PlayerEnum,
                           version: GameVersion){
            for(card_name in src){
                if(start_turn){
                    dest[card_name.toCardNumber(true)] = cardMakerByName(true, card_name, player,
                        LocationEnum.SPECIAL_CARD, version)
                }
                else{
                    dest[card_name.toCardNumber(false)] = cardMakerByName(false, card_name, player,
                        LocationEnum.SPECIAL_CARD, version)
                }
            }
        }

        fun cardReconstructInsert(src1: ArrayDeque<Card>, src2: ArrayDeque<Card>, dest: ArrayDeque<Card>){
            dest.addAll(src1)
            dest.addAll(src2)
            dest.shuffle()
            for(card in dest){
                if(card.card_number.isPoison()){
                    dest.remove(card)
                    dest.addFirst(card)
                }
            }
            src1.clear()
            src2.clear()
        }
    }

    private fun enchantmentUsable(text: Text): Boolean =
        location == LocationEnum.ENCHANTMENT_ZONE && text.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT && (nap?: 0) > 0
    private fun usedEffectUsable(text: Text): Boolean =
        text.timing_tag == TextEffectTimingTag.USED && this.special_card_state == SpecialCardEnum.PLAYED

    suspend fun destructionEnchantmentNormal(player: PlayerEnum, game_status: GameStatus){
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.AFTER_DESTRUCTION){
                    text.effect!!(this.card_number, player, game_status, null)
                }
            }
        }
    }
    fun isItDestruction(): Boolean{
        //some text can be added
        return nap == 0 || nap == null
    }

    suspend fun addCostBuff(player: PlayerEnum, game_status: GameStatus){
        card_data.effect?.let {
            for(text in it){
                if(usedEffectUsable(text)){
                    if(text.tag == TextEffectTag.COST_BUFF) text.effect!!(this.card_number, player, game_status, null)
                }
                else if(enchantmentUsable(text)){
                    if(text.tag == TextEffectTag.COST_BUFF) text.effect!!(this.card_number, player, game_status, null)
                }
            }
        }
    }

    suspend fun thisCardCostBuff(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    if(text.tag == TextEffectTag.COST_BUFF) text.effect!!(this.card_number, player, game_status, react_attack)
                }
            }
        }
    }

    suspend fun canUseAtReact(player: PlayerEnum, gameStatus: GameStatus): Boolean{
        if(card_data.sub_type == SubType.REACTION){
            return true
        }
        return card_data.effect?.let {
            var result = false
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT && text.tag == TextEffectTag.CAN_USE_REACT){
                    result =  text.effect!!(this.card_number, player, gameStatus, null)!! == 1
                    break
                }

            }
            result
        }?: false
    }

    suspend fun canReactAt(attack: MadeAttack, game_status: GameStatus, react_player: PlayerEnum, continuousBuffQueue: OtherBuffQueue): Boolean{
        return attack.canReacted(this, game_status, react_player.opposite(), continuousBuffQueue)
    }

    private suspend fun returnNapWhenSomeCondition(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Int{
        if(this.card_data.charge == null){
            return -1
        }
        else{
            this.card_data.effect?.let {
                for(text in it){
                    if(text.tag == TextEffectTag.ADJUST_NAP_CONTAIN_OTHER_PLACE){
                        return text.effect!!(this.card_number, player, game_status, react_attack)?: this.card_data.charge?: 0
                    }
                }
            }
            return this.card_data.charge?: 0
        }
    }

    private suspend fun returnNap(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Int{
        if(this.card_data.charge == null){
            return -1
        }
        else{
            this.card_data.effect?.let {
                for(text in it){
                    if(text.tag == TextEffectTag.ADJUST_NAP){
                        return text.effect!!(this.card_number, player, game_status, react_attack)?: this.card_data.charge?: 0
                    }
                }
            }
            return this.card_data.charge?: 0
        }
    }

    private suspend fun getBaseCost(player: PlayerEnum, gameStatus: GameStatus): Pair<Boolean, Int>{
        var x = 10000
        x = this.card_data.cost?: card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    if(text.tag == TextEffectTag.COST_X){
                        x = text.effect!!(this.card_number, player, gameStatus, null)!!
                        this.numberForX = x
                        break
                    }
                }
            }
            x
        }?: 10000
        return if (x < 0){
            Pair(true, -1 * x)
        } else{
            Pair(false, x)
        }
    }

    //true mean can use
    private suspend fun textUseCheck(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.USING_CONDITION){
                    if(text.effect!!(this.card_number, player, game_status, react_attack)!! == 1){
                        return true
                    }
                    return false
                }
            }
        }
        return true
    }

    private suspend fun textCostCheck(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Boolean {
        card_data.effect?.let {
            for (text in it) {
                if (text.tag == TextEffectTag.COST_CHECK) {
                    if (text.effect!!(this.card_number, player, game_status, react_attack)!! == 1) {
                        return true
                    }
                    return false
                }
            }
        }
        return true
    }

    private fun getDistance(umbrella: Umbrella?): SortedSet<Int>{
        val result = sortedSetOf<Int>()
        if(card_data.umbrellaMark){
            when(umbrella){
                Umbrella.FOLD -> {
                    when(card_data.distanceTypeFold){
                        DistanceType.DISCONTINUOUS -> {
                            for(i in card_data.distanceUncontFold!!.indices){
                                if(card_data.distanceUncontFold!![i]){
                                    result.add(i)
                                }
                            }
                        }
                        DistanceType.CONTINUOUS -> {
                            for(i in card_data.distanceContFold!!.first..card_data.distanceContFold!!.second){
                                result.add(i)
                            }
                        }
                        else -> {}
                    }
                }
                Umbrella.UNFOLD -> {
                    when(card_data.distanceTypeUnfold){
                        DistanceType.DISCONTINUOUS -> {
                            for(i in card_data.distanceUncontUnfold!!.indices){
                                if(card_data.distanceUncontUnfold!![i]){
                                    result.add(i)
                                }
                            }
                        }
                        DistanceType.CONTINUOUS -> {
                            for(i in card_data.distanceContUnfold!!.first..card_data.distanceContUnfold!!.second){
                                result.add(i)
                            }
                        }
                        else -> {}
                    }
                }
                null -> {}
            }
        }
        else{
            when(card_data.distanceType){
                DistanceType.DISCONTINUOUS -> {
                    for(i in card_data.distanceUncont!!.indices){
                        if(card_data.distanceUncont!![i]){
                            result.add(i)
                        }
                    }
                }
                DistanceType.CONTINUOUS -> {
                    for(i in card_data.distanceCont!!.first..card_data.distanceCont!!.second){
                        result.add(i)
                    }
                }
                else -> {}
            }
        }
        return result
    }

    suspend fun makeAttack(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, subType: SubType?): MadeAttack?{
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT && text.tag == TextEffectTag.NEXT_ATTACK_ENCHANTMENT){
                    text.effect!!(this.card_number, player, game_status, react_attack)
                }
            }
        }

        if(this.card_data.umbrellaMark){
            when(game_status.getUmbrella(this.player)){
                Umbrella.FOLD -> {
                    card_data.effectFold?.let {
                        for(text in it){
                            if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT && text.tag == TextEffectTag.NEXT_ATTACK_ENCHANTMENT){
                                text.effect!!(this.card_number, player, game_status, react_attack)
                            }
                        }
                    }
                    return MadeAttack(
                        card_player = this.player,
                        card_name =  this.card_data.card_name,
                        card_number = this.card_number,
                        card_class = this.card_data.card_class,
                        distance = this.getDistance(Umbrella.FOLD),
                        life_damage = this.card_data.lifeDamageFold!!,
                        aura_damage = this.card_data.auraDamageFold!!,
                        megami = this.card_data.megami,
                        cannotReactNormal = this.card_data.cannotReactNormal,
                        cannotReactSpecial = this.card_data.cannotReactSpecial,
                        cannotReact = this.card_data.cannotReact,
                        chogek = this.card_data.chogek,
                        inevitable = this.card_data.inevitable,
                        subType = subType ?: this.card_data.sub_type,
                        isLaceration = this.card_data.isLaceration,
                        isTrace = this.card_data.isTrace
                    )
                }
                Umbrella.UNFOLD -> {
                    card_data.effectUnfold?.let {
                        for(text in it){
                            if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT && text.tag == TextEffectTag.NEXT_ATTACK_ENCHANTMENT){
                                text.effect!!(this.card_number, player, game_status, react_attack)
                            }
                        }
                    }
                    return MadeAttack(
                        card_player = this.player,
                        card_name =  this.card_data.card_name,
                        card_number = this.card_number,
                        card_class = this.card_data.card_class,
                        distance = getDistance(Umbrella.UNFOLD),
                        life_damage = this.card_data.lifeDamageUnfold!!,
                        aura_damage = this.card_data.auraDamageUnfold!!,
                        megami = this.card_data.megami,
                        cannotReactNormal = this.card_data.cannotReactNormal,
                        cannotReactSpecial = this.card_data.cannotReactSpecial,
                        cannotReact = this.card_data.cannotReact,
                        chogek = this.card_data.chogek,
                        inevitable = this.card_data.inevitable,
                        subType = subType ?: this.card_data.sub_type,
                        isLaceration = this.card_data.isLaceration,
                        isTrace = this.card_data.isTrace
                    )
                }
                null -> {
                    return null
                }
            }
        }
        else{
            return MadeAttack(
                card_player = this.player,
                card_name =  this.card_data.card_name,
                card_number = this.card_number,
                card_class = this.card_data.card_class,
                distance = getDistance(null),
                life_damage = this.card_data.lifeDamage!!,
                aura_damage = this.card_data.auraDamage!!,
                megami = this.card_data.megami,
                cannotReactNormal = this.card_data.cannotReactNormal,
                cannotReactSpecial = this.card_data.cannotReactSpecial,
                cannotReact = this.card_data.cannotReact,
                chogek = this.card_data.chogek,
                inevitable = this.card_data.inevitable,
                subType = subType ?: this.card_data.sub_type,
                isLaceration = this.card_data.isLaceration,
                isTrace = this.card_data.isTrace
            )
        }
    }

    private fun kanaweSealCheck(player: PlayerEnum, game_status: GameStatus) =
            game_status.turnPlayer == player &&
            (game_status.getPlayer(player).canNotUseCardName1?.second == card_data.card_name ||
            game_status.getPlayer(player).canNotUseCardName2?.second == card_data.card_name)
                    && card_data.card_name != CardName.RENRI_DECEPTION_FOG

    /**
     * @return (-2) means can't use
     *
     * (-1) means can use(when not special card)
     *
     * (>=0) flare cost
     *
     * (<=-4) laceration flare cost
     */
    suspend fun canUse(player: PlayerEnum, gameStatus: GameStatus, react_attack: MadeAttack?, isCost: Boolean, isConsume: Boolean): Int{
        val nowPlayer = gameStatus.getPlayer(player)
        var cost: Int

        if(!textUseCheck(player, gameStatus, react_attack)){
            return -2
        }

        if(isCost){
            if(!textCostCheck(player, gameStatus, react_attack)){
                return -2
            }

            val otherPlayer = gameStatus.getPlayer(player.opposite())
            var cardMustPay = 0

            if(nowPlayer.nextCostAddMegami == card_data.megami){
                cardMustPay += 1
            }

            if(card_data.card_class == CardClass.SPECIAL && card_data.card_type == CardType.BEHAVIOR){
                for(card in otherPlayer.usedSpecialCard.values){
                    cardMustPay += card.effectAllValidEffect(player.opposite(), gameStatus, TextEffectTag.KAMUWI_LOGIC)
                }
            }

            if(cardMustPay != 0){
                val cardCanUseCost = nowPlayer.hand.values.filter {
                    it.card_data.megami == this.card_data.megami
                            && it.card_number != this.card_number
                }.size

                if(cardCanUseCost < cardMustPay){
                    return -2
                }
            }
        }

        if(card_data.card_class == CardClass.SPECIAL){
            if(kanaweSealCheck(player, gameStatus)){
                return -2
            }

            if(isCost && isConsume){
                this.thisCardCostBuff(player, gameStatus, react_attack)
                gameStatus.addAllCardCostBuff()
                val (laceration, baseCost) = this.getBaseCost(player, gameStatus)
                cost = gameStatus.applyAllCostBuff(player, baseCost, this)

                if(cost < 0) cost = 0

                if(cost > gameStatus.getPlayerFlare(player)){
                    gameStatus.cleanCostBuffWhenUnused()
                    return -2
                }

                if(laceration){
                    cost *= -4
                }
            }
            else{
                cost = 0
            }
        }
        else if(card_data.card_class == CardClass.NORMAL){
            if(gameStatus.getPlayer(player.opposite()).nowAct?.actColor == Act.COLOR_RED
                && kanaweSealCheck(player, gameStatus)){
                return -2
            }
            cost = -1
        }
        else{
            cost = -1
        }

        when(card_data.card_type){
            CardType.ATTACK -> {
                if(nowPlayer.canNotAttack || nowPlayer.enchantmentCard.values.any { card ->
                    card.effectAllValidEffect(player, gameStatus, TextEffectTag.CAN_NOT_USE_ATTACK) > 0
                }) return -2
                if(gameStatus.addPreAttackZone(
                        player,
                        this.makeAttack(player, gameStatus, react_attack, this.card_data.sub_type)?.
                        addTextAndReturn(gameStatus.getUmbrella(this.player), this.card_data)?:
                            MadeAttack(
                                card_player = this.player,
                                card_name =  this.card_data.card_name,
                                card_number = this.card_number,
                                card_class = this.card_data.card_class,
                                distance = sortedSetOf(),
                                life_damage = 0,
                                aura_damage = 0,
                                megami = this.card_data.megami,
                                cannotReactNormal = false,
                                cannotReactSpecial = false,
                                cannotReact = false,
                                chogek = false ,
                                inevitable = this.card_data.inevitable,
                                subType = this.card_data.sub_type,
                                isLaceration = this.card_data.isLaceration,
                                isTrace = this.card_data.isTrace
                            )
                    )){
                    return cost
                }
                if(card_data.card_class == CardClass.SPECIAL){
                    gameStatus.cleanCostBuffWhenUnused()
                }
                return -2
            }
            CardType.UNDEFINED -> {
                if(this.effectText(player, gameStatus, react_attack, TextEffectTag.TREAT_AS_DIFFERENT_CARD) == 1){
                    return -3
                }
                return -2
            }
            else -> {}
        }

        return cost
    }

    suspend fun attackUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
        game_status.afterMakeAttack(this.card_number, player, react_attack)
    }

    suspend fun behaviorUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
        for(card in game_status.getPlayer(player).enchantmentCard.values){
            card.addOneValidEffect(TextEffectTag.WHEN_USE_BEHAVIOR_END, cardUseEndEffect)
        }

        if(this.card_data.umbrellaMark) {
            when (game_status.getUmbrella(this.player)) {
                Umbrella.FOLD -> {
                    card_data.effectFold?.let {
                        for(text in it){
                            if(text.timing_tag == TextEffectTimingTag.USING){
                                text.effect!!(this.card_number, player, game_status, react_attack)
                            }
                        }
                    }
                }
                Umbrella.UNFOLD -> {
                    card_data.effectUnfold?.let {
                        for(text in it){
                            if(text.timing_tag == TextEffectTimingTag.USING){
                                text.effect!!(this.card_number, player, game_status, react_attack)
                            }
                        }
                    }
                }
                null -> {
                }
            }
        }
        else{
            card_data.effect?.let {
                for(text in it){
                    if(text.timing_tag == TextEffectTimingTag.USING){
                        text.effect!!(this.card_number, player, game_status, react_attack)
                    }
                }
            }
        }
    }

    private suspend fun enchantmentUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, nap_change: Int = -1) {
        val nowPlayer = game_status.getPlayer(player)

        for(card in game_status.getPlayer(player.opposite()).enchantmentCard.values){
            card.effectAllValidEffect(player.opposite(), game_status, TextEffectTag.WHEN_DEPLOYMENT_OTHER)
        }

        var nowNeedNap = when (nap_change) {
            -2 -> {
                returnNapWhenSomeCondition(player, game_status, react_attack) + game_status.getPlayer(player).napBuff
            }
            -1 -> {
                returnNap(player, game_status, react_attack) + game_status.getPlayer(player).napBuff
            }
            else -> {
                nap_change + game_status.getPlayer(player).napBuff
            }
        }

        if(nowNeedNap < 0) nowNeedNap = 0
        nowPlayer.napBuff = 0
        nowPlayer.notReadySeed?.let {
            if (it > 0){
                game_status.notReadyToReadySeed(player, 1)
            }
        }
        if(nowNeedNap != 0 && nowPlayer.readySeed > 0){
            while(true){
                val sproutData =
                    receiveNapInformation(game_status.getSocket(player), nowNeedNap, this.card_number, CommandEnum.SELECT_SPROUT)
                val sprout = sproutData.first
                if(sprout > nowNeedNap || sprout > nowPlayer.readySeed || sprout < 0) {
                    continue
                }
                game_status.readySeedToCard(player, sprout, this)
                nowNeedNap -= sprout
                break
            }
        }

        when {
            nowNeedNap == 0 -> {}
            (nowNeedNap > game_status.getPlayerAura(player) + game_status.dust) -> {
                game_status.dustToCard(player, game_status.dust, this, GameLog.NORMAL_NAP_COST)
                game_status.auraToCard(player, game_status.getPlayerAura(player), this, GameLog.NORMAL_NAP_COST)
                game_status.gameLogger.insert(GameLog(player, LogEnum.END_EFFECT, GameLog.NORMAL_NAP_COST, -1))
            }
            game_status.getPlayerAura(player) == 0 -> {
                game_status.dustToCard(player, nowNeedNap, this, GameLog.NORMAL_NAP_COST)
                game_status.gameLogger.insert(GameLog(player, LogEnum.END_EFFECT, GameLog.NORMAL_NAP_COST, -1))
            }
            game_status.dust == 0 -> {
                game_status.auraToCard(player, nowNeedNap, this, GameLog.NORMAL_NAP_COST)
                game_status.gameLogger.insert(GameLog(player, LogEnum.END_EFFECT, GameLog.NORMAL_NAP_COST, -1))
            }
            else -> {
                while (true) {
                    val (aura, dust)  =
                        receiveNapInformation(game_status.getSocket(player), nowNeedNap, this.card_number, CommandEnum.SELECT_NAP)
                    if (aura < 0 || dust < 0 || aura + dust != nowNeedNap || game_status.getPlayerAura(player) < aura || game_status.dust < dust) {
                        continue
                    }
                    game_status.auraToCard(player, aura, this, GameLog.NORMAL_NAP_COST)
                    game_status.dustToCard(player, dust, this, GameLog.NORMAL_NAP_COST)
                    game_status.gameLogger.insert(GameLog(player, LogEnum.END_EFFECT, GameLog.NORMAL_NAP_COST, -1))
                    break
                }
            }

        }

        var nowGrowing = card_data.growing
        if(card_data.megami != MegamiEnum.MEGUMI){
            nowGrowing += nowPlayer.nextEnchantmentGrowing
            nowPlayer.nextEnchantmentGrowing = 0
        }
        if(nowGrowing > 0){
            while(true){
                val growingData =
                    receiveNapInformation(game_status.getSocket(player), nowGrowing, this.card_number, CommandEnum.SELECT_GROWING)
                val growing = growingData.first
                if(growing > nowGrowing || growing > nowPlayer.readySeed || growing < 0) {
                    continue
                }
                game_status.readySeedToCard(player, growing, this)
                break
            }
        }

        card_data.effect?.let {
            for (text in it) {
                if(text.timing_tag == TextEffectTimingTag.START_DEPLOYMENT){
                    text.effect!!(this.card_number, player, game_status, react_attack)
                }
            }
        }
    }

    suspend fun useCustomPart(player: PlayerEnum, game_status: GameStatus, customPartUseNumber: Int, main_attack: MadeAttack?){
        val usingTag = when(customPartUseNumber){
            1 -> TextEffectTag.CUSTOM_PART_LV_1
            2 -> TextEffectTag.CUSTOM_PART_LV_2
            3 -> TextEffectTag.CUSTOM_PART_LV_3
            4 -> TextEffectTag.CUSTOM_PART_LV_4
            else -> TextEffectTag.NULL
        }
        effectText(player, game_status, main_attack, usingTag)
    }

    suspend fun use(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, isTermination: Boolean,
                    nap_change: Int = -1, cardMoveCancel: Boolean, isDisprove: Boolean = false, ){
        this.card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    when(text.tag){
                        TextEffectTag.ADD_TEXT_TO_ATTACK, TextEffectTag.THIS_CARD_NAP_CHANGE,
                        TextEffectTag.ADD_LOG -> {
                            text.effect!!(this.card_number, player, game_status, react_attack)
                        }
                        else -> {}
                    }
                }
            }
        }

        if(card_data.sub_type == SubType.FULL_POWER){
            for(card in game_status.getPlayer(player).usedSpecialCard.values){
                card.addOneValidEffect(TextEffectTag.WHEN_USE_FULL_POWER_YOUR_END, cardUseEndEffect)
                card.effectAllValidEffect(player, game_status, TextEffectTag.WHEN_USE_FULL_POWER_YOUR)
            }
        }
        else if(card_data.sub_type == SubType.REACTION){
            for(card in game_status.getPlayer(player).enchantmentCard.values){
                card.addOneValidEffect(TextEffectTag.WHEN_USE_REACT_CARD_YOUR_END, cardUseEndEffect)
            }
        }

        react_attack?.addValidEffect(TextEffectTag.WHEN_THIS_CARD_REACTED_AFTER, cardUseEndEffect)

        if(!isDisprove){
            this.effectText(player, game_status, react_attack, TextEffectTag.WHEN_THIS_CARD_NOT_DISPROVE)
        }

        when(this.card_data.card_type){
            CardType.ATTACK -> {
                attackUseNormal(player, game_status, react_attack)
            }
            CardType.BEHAVIOR -> {
                behaviorUseNormal(player, game_status, react_attack)
            }
            CardType.ENCHANTMENT -> {
                enchantmentUseNormal(player, game_status, react_attack, nap_change)
            }
            CardType.UNDEFINED -> {}
        }

        if(isTermination || game_status.getPlayer(player).afterCardUseTermination){
            game_status.setEndTurn(player, true)
            game_status.getPlayer(player).afterCardUseTermination = false
        }

        game_status.afterCardUsed(this.card_number, player, this, cardMoveCancel)
        if(this.card_data.card_type == CardType.ENCHANTMENT && !cardMoveCancel){
            this.effectText(player, game_status, react_attack, TextEffectTag.AFTER_DEPLOYMENT)
        }
    }

    fun chasmCheck(): Boolean{
        this.card_data.effect?.let {
            for(text in it){
                if(text === CardSet.chasmText) return true
            }
        }
        return false
    }

    fun checkAuraReplaceable(): Boolean{
        return this.card_data.effect?.let {
            var check = false
            for(text in it) {
                if (text.tag == TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE && (nap ?: -1) > 0
                ) {
                    check = true
                    break
                }
            }
            check
        }?: false
    }

    suspend fun returnCheck(player: PlayerEnum, game_status: GameStatus): Boolean{
        this.card_data.effect?.let {
            for (text in it){
                if(text.timing_tag == TextEffectTimingTag.USED && text.tag == TextEffectTag.RETURN){
                    if(text.effect!!(this.card_number, player, game_status, null) == 1) {
                        return true
                    }
                }
            }
            return false
        }?: return false
    }

    suspend fun addReturnListener(player: PlayerEnum, game_status: GameStatus){
        this.card_data.effect?.let{
            for(text in it){
                if(text.tag == TextEffectTag.IMMEDIATE_RETURN){
                    text.effect!!(this.card_number, player, game_status, null)
                }
            }
        }
    }

    suspend fun forbidTokenMoveUsingArrow(player: PlayerEnum, game_status: GameStatus): Int{
        val now = effectAllValidEffect(player, game_status, TextEffectTag.FORBID_MOVE_TOKEN_USING_ARROW)
        return if (now == 0) -1
        else now
    }

    fun isItAttack(): Boolean = card_data.card_type == CardType.ATTACK

    fun isItInstallation(): Boolean{
        this.card_data.effect?.let{
            for(text in it){
                if (text.tag == TextEffectTag.INSTALLATION) {
                    return true
                }
            }
        }
        return false
    }

    fun isItInstallationInfinite(): Boolean{
        this.card_data.effect?.let{
            for(text in it){
                if (text.tag == TextEffectTag.INSTALLATION_INFINITE) {
                    return true
                }
            }
        }
        return false
    }

    suspend fun checkConditionText(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, conditionTag: TextEffectTag): Int?{
        return effectText(player, game_status, react_attack, conditionTag)
    }

    suspend fun effectText(card_number: Int, player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, tag: TextEffectTag): Int?{
        this.card_data.effect?.let {
            for(text in it){
                if(text.tag == tag){
                    return text.effect!!(card_number, player, game_status, react_attack)
                }
            }
        }
        return null
    }

    suspend fun effectText(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, tag: TextEffectTag): Int?{
        this.card_data.effect?.let {
            for(text in it){
                if(text.tag == tag){
                    return text.effect!!(this.card_number, player, game_status, react_attack)
                }
            }
        }
        return null
    }

    suspend fun checkWhenUmbrellaChange(player: PlayerEnum, game_status: GameStatus){
        this.card_data.effect?.let {
            for(text in it){
                if (text.tag == TextEffectTag.WHEN_UMBRELLA_CHANGE){
                    text.effect!!(card_number, player, game_status, null)
                }
            }
        }
    }

    private fun addOneValidEffect(effectTag: TextEffectTag, queue: HashMap<Int, Text>){
        card_data.effect?.let {
            for(text in it){
                if(usedEffectUsable(text) || enchantmentUsable(text)){
                    if(text.tag == effectTag) {
                        queue[this.card_number] = text
                        return
                    }
                }
            }
        }
    }

    fun addOneEffect(effectTag: TextEffectTag, queue: HashMap<Int, Text>){
        card_data.effect?.let {
            for(text in it){
                if(text.tag == effectTag) {
                    queue[this.card_number] = text
                    return
                }
            }
        }
    }

    suspend fun effectAllValidEffect(card_number: Int, player: PlayerEnum, game_status: GameStatus, effectTag: TextEffectTag,
                                     react_attack: MadeAttack? = null): Int{
        var now = 0
        if(this.card_data.umbrellaMark) {
            when (game_status.getUmbrella(this.player)) {
                Umbrella.FOLD -> {
                    card_data.effectFold?.let {
                        for(text in it){
                            if(usedEffectUsable(text) || enchantmentUsable(text)){
                                if(text.tag == effectTag) text.effect!!(card_number, player, game_status, react_attack)?.let { result ->
                                    now += result
                                }
                            }
                        }
                    }
                }
                Umbrella.UNFOLD -> {
                    card_data.effectUnfold?.let {
                        for(text in it){
                            if(usedEffectUsable(text) || enchantmentUsable(text)){
                                if(text.tag == effectTag) text.effect!!(card_number, player, game_status, react_attack)?.let { result ->
                                    now += result
                                }
                            }
                        }
                    }
                }
                null -> {
                }
            }
        }
        else{
            card_data.effect?.let {
                for(text in it){
                    if(usedEffectUsable(text) || enchantmentUsable(text)){
                        if(text.tag == effectTag) text.effect!!(card_number, player, game_status, react_attack)?.let { result ->
                            now += result
                        }
                    }
                }
            }
        }

        return now
    }

    suspend fun effectAllValidEffect(player: PlayerEnum, game_status: GameStatus, effectTag: TextEffectTag, react_attack: MadeAttack? = null): Int{
        var now = 0
        if(this.card_data.umbrellaMark) {
            when (game_status.getUmbrella(this.player)) {
                Umbrella.FOLD -> {
                    card_data.effectFold?.let {
                        for(text in it){
                            if(usedEffectUsable(text) || enchantmentUsable(text)){
                                if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, react_attack)?.let { result ->
                                    now += result
                                }
                            }
                        }
                    }
                }
                Umbrella.UNFOLD -> {
                    card_data.effectUnfold?.let {
                        for(text in it){
                            if(usedEffectUsable(text) || enchantmentUsable(text)){
                                if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, react_attack)?.let { result ->
                                    now += result
                                }
                            }
                        }
                    }
                }
                null -> {
                }
            }
        }
        else{
            card_data.effect?.let {
                for(text in it){
                    if(usedEffectUsable(text) || enchantmentUsable(text)){
                        if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, react_attack)?.let { result ->
                            now += result
                        }
                    }
                }
            }
        }

        return now
    }

    fun canUseAtCover(): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.CAN_USE_AT_COVER) return true
            }
        }
        return false
    }

    fun canUseEffectCheck(tag: TextEffectTag): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(enchantmentUsable(text) || usedEffectUsable(text)){
                    if(text.tag == tag) return true
                }
            }
        }
        return false
    }

    suspend fun checkCanMoveToken(reason: Int, player: PlayerEnum, game_status: GameStatus): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(enchantmentUsable(text) || usedEffectUsable(text)){
                    if(text.tag == TextEffectTag.CAN_NOT_MOVE_TOKEN){
                        return text.effect!!(reason, player, game_status, null) != 1
                    }
                }
            }
        }
        return true
    }

    fun thisCardHaveStratagem(): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.RUN_STRATAGEM) return true
            }
        }
        return false
    }

    suspend fun runStratagem(player: PlayerEnum, game_status: GameStatus){
        card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.RUN_STRATAGEM) {
                    text.effect!!(card_number, player, game_status, null)
                }
            }
        }
    }

    suspend fun ideaProcess(player: PlayerEnum, game_status: GameStatus, flipped: Boolean, keys: MutableList<Int>?){
        suspend fun checkIdeaCondition(player: PlayerEnum, game_status: GameStatus, flipped: Boolean): Boolean{
            val tag = if(flipped) TextEffectTimingTag.IDEA_CONDITION_FLIP else TextEffectTimingTag.IDEA_CONDITION
            card_data.effect?.let {
                for(text in it){
                    if(text.timing_tag == tag) {
                        return text.effect!!(card_number, player, game_status, null) == 1
                    }
                }
            }
            return false
        }

        fun ideaRun(player: PlayerEnum, game_status: GameStatus, flipped: Boolean, keys: MutableList<Int>?){
            val tag = if(flipped) TextEffectTimingTag.IDEA_PROCESS_FLIP else TextEffectTimingTag.IDEA_PROCESS
            card_data.effect?.let {
                for(text in it){
                    if(text.timing_tag == tag) {
                        when(player){
                            PlayerEnum.PLAYER1 -> game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.IDEA_PLAYER1, text)
                            PlayerEnum.PLAYER2 -> game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.IDEA_PLAYER2, text)
                        }
                        keys?.add(card_number)
                        return
                    }
                }
            }
        }

        val nowPlayer = game_status.getPlayer(player)
        if(checkIdeaCondition(player, game_status, flipped)){
            nowPlayer.tempIdeaProcess = true
            ideaRun(player, game_status, flipped, keys)
        }
    }

    /***
     this function don't care if the umbrella is fold or unfold
     */
    fun isThisCardHaveTag(tag: TextEffectTag): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(text.tag == tag) return true
            }
        }
        return false
    }

    fun canCover(): Boolean{
        return card_data.card_class != CardClass.POISON
    }
}