package com.sakurageto.card

import com.sakurageto.card.CardSet.toCardData
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.gamelogic.Umbrella
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.receiveNapInformation
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.HashMap

class Card(val card_number: Int, var card_data: CardData, val player: PlayerEnum,
           var special_card_state: SpecialCardEnum?){
    private var nap: Int? = null
    private var seedToken: Int = 0

    fun getNap() = nap
    fun getSeedToken() = seedToken

    fun reduceNap(number: Int): Pair<Int, Int>{
        var value = number
        if((nap?: 0) < number){
            value = nap?: 0
        }
        val sakuraToken = (nap?: 0) - seedToken

        nap = (nap?: 0) - value
        if(sakuraToken >= value){
            return Pair(value, 0)
        }
        else{
            seedToken = seedToken - value + sakuraToken
            return Pair(sakuraToken, value - sakuraToken)
        }
    }

    fun addNap(number: Int, seed: Boolean = false) {
        nap = nap?.let {
            it + number
        }?: number

        if(seed){
            seedToken += number
        }
    }

    var beforeCardData: CardData? = null

    var cardUseEndEffect = HashMap<Int, Text>()

    companion object{
        fun cardMakerByName(start_turn: Boolean, card_name: CardName, player: PlayerEnum): Card{
            val data = card_name.toCardData()
            if (data.isItSpecial()){
                if(start_turn){
                    return Card(card_name.toCardNumber(true), data, player, SpecialCardEnum.UNUSED)
                }
                return Card(card_name.toCardNumber(false), data, player, SpecialCardEnum.UNUSED)
            }
            else{
                if(start_turn){
                    return Card(card_name.toCardNumber(true), data, player, null)
                }
                return Card(card_name.toCardNumber(false), data, player, null)
            }

        }

        fun cardInitInsert(start_turn: Boolean, dest: ArrayDeque<Card>, src: MutableList<CardName>, player: PlayerEnum){
            src.shuffle(Random(System.currentTimeMillis()))
            for(card_name in src){
                dest.add(cardMakerByName(start_turn, card_name, player))
            }
        }

        fun cardInitInsert(start_turn: Boolean, dest: HashMap<Int, Card>, src: MutableList<CardName>, player: PlayerEnum){
            for(card_name in src){
                if(start_turn){
                    dest[card_name.toCardNumber(true)] = cardMakerByName(true, card_name, player)
                }
                else{
                    dest[card_name.toCardNumber(false)] = cardMakerByName(false, card_name, player)
                }
            }
        }

        fun cardReconstructInsert(src1: ArrayDeque<Card>, src2: ArrayDeque<Card>, dest: ArrayDeque<Card>){
            dest.addAll(src1)
            dest.addAll(src2)
            dest.shuffle(Random(System.currentTimeMillis()))
            for(card in dest){
                if(CardSet.isPoison(card.card_number)){
                    dest.remove(card)
                    dest.addFirst(card)
                }
            }
            src1.clear()
            src2.clear()
        }
    }

    fun enchantmentUsable(text: Text): Boolean =
        text.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT && (nap?: 0) > 0
    fun usedEffectUsable(text: Text): Boolean =
        text.timing_tag == TextEffectTimingTag.USED && this.special_card_state == SpecialCardEnum.PLAYED

    suspend fun reduceNapNormal(player: PlayerEnum, game_status: GameStatus): Int{
       card_data.effect?.let {
           for(text in it){
               if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT || text.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT){
                   when(text.tag){
                       TextEffectTag.DO_NOT_NAP -> {
                           return text.effect!!(this.card_number, player, game_status, null)!!
                       }
                       else -> {
                           continue
                       }
                   }
               }
           }
       }
        return 1
    }

    suspend fun destructionEnchantmentNormaly(player: PlayerEnum, game_status: GameStatus){
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

    suspend fun thisCardCostBuff(player: PlayerEnum, game_status: GameStatus){
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    if(text.tag == TextEffectTag.COST_BUFF) text.effect!!(this.card_number, player, game_status, null)
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
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT && text.tag == TextEffectTag.CAN_REACTABLE){
                    result =  text.effect!!(this.card_number, player, gameStatus, null)!! == 1
                    break
                }

            }
            result
        }?: false
    }

    suspend fun canReactable(attack: MadeAttack, game_status: GameStatus, player: PlayerEnum, continuousBuffQueue: OtherBuffQueue): Boolean{
        return attack.canReactByThisCard(this, game_status, player, continuousBuffQueue)
    }

    suspend fun returnNap(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Int{
        if(this.card_data.charge == null){
            return -1
        }
        else{
            this.card_data.effect?.let {
                for(text in it){
                    if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                        if(text.tag == TextEffectTag.ADJUST_NAP){
                            return text.effect!!(this.card_number, player, game_status, react_attack)!!
                        }
                    }
                }
            }

            return this.card_data.charge!!
        }
    }

    suspend fun getBaseCost(player: PlayerEnum, gameStatus: GameStatus): Int{
        return this.card_data.cost ?: card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    if(text.tag == TextEffectTag.COST_X){
                        text.effect!!(this.card_number, player, gameStatus, null)!!
                    }
                }
            }
            10000
        }?: 10000
    }

    //true mean can use
    suspend fun textUseCheck(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Boolean{
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

    suspend fun textCostCheck(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Boolean {
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
                    return MadeAttack(
                        card_name =  this.card_data.card_name,
                        card_number = this.card_number,
                        card_class = this.card_data.card_class,
                        distance_type = this.card_data.distanceTypeFold!!,
                        life_damage = this.card_data.lifeDamageFold!!,
                        aura_damage = this.card_data.auraDamageFold!!,
                        distance_cont = this.card_data.distanceContFold,
                        distance_uncont = this.card_data.distanceUncontFold,
                        megami = this.card_data.megami,
                        cannotReactNormal = this.card_data.cannotReactNormal,
                        cannotReactSpecial = this.card_data.cannotReactSpecial,
                        cannotReact = this.card_data.cannotReact,
                        chogek = this.card_data.chogek,
                        inevitable = this.card_data.inevitable,
                        subType = subType ?: this.card_data.sub_type
                    )
                }
                Umbrella.UNFOLD -> {
                    return MadeAttack(
                        card_name =  this.card_data.card_name,
                        card_number = this.card_number,
                        card_class = this.card_data.card_class,
                        distance_type = this.card_data.distanceTypeUnfold!!,
                        life_damage = this.card_data.lifeDamageUnfold!!,
                        aura_damage = this.card_data.auraDamageUnfold!!,
                        distance_cont = this.card_data.distanceContUnfold,
                        distance_uncont = this.card_data.distanceUncontUnfold,
                        megami = this.card_data.megami,
                        cannotReactNormal = this.card_data.cannotReactNormal,
                        cannotReactSpecial = this.card_data.cannotReactSpecial,
                        cannotReact = this.card_data.cannotReact,
                        chogek = this.card_data.chogek,
                        inevitable = this.card_data.inevitable,
                        subType = subType ?: this.card_data.sub_type
                    )
                }
                null -> {
                    return null
                }
            }
        }
        else{
            return MadeAttack(
                card_name =  this.card_data.card_name,
                card_number = this.card_number,
                card_class = this.card_data.card_class,
                distance_type = this.card_data.distance_type!!,
                life_damage = this.card_data.life_damage!!,
                aura_damage = this.card_data.aura_damage!!,
                distance_cont = this.card_data.distance_cont,
                distance_uncont = this.card_data.distance_uncont,
                megami = this.card_data.megami,
                cannotReactNormal = this.card_data.cannotReactNormal,
                cannotReactSpecial = this.card_data.cannotReactSpecial,
                cannotReact = this.card_data.cannotReact,
                chogek = this.card_data.chogek,
                inevitable = this.card_data.inevitable,
                subType = subType ?: this.card_data.sub_type
            )
        }
    }

    //-2: can't use                    -1: can use                 >= 0: cost
    suspend fun canUse(player: PlayerEnum, gameStatus: GameStatus, react_attack: MadeAttack?, isCost: Boolean, isConsume: Boolean): Int{
        if(!textUseCheck(player, gameStatus, react_attack)){
            return -2
        }

        if(isCost && !textCostCheck(player, gameStatus, react_attack)){
            return -2
        }

        var cost: Int

        if(card_data.card_class == CardClass.SPECIAL){
            if(isCost && isConsume){
                this.thisCardCostBuff(player, gameStatus)
                gameStatus.addAllCardCostBuff()
                cost = gameStatus.applyAllCostBuff(player, this.getBaseCost(player, gameStatus), this)
                if(cost < 0) cost = 0
                if(cost > gameStatus.getPlayerFlare(player)){
                    gameStatus.cleanCostBuff()
                    return -2
                }
            }
            else{
                cost = 0
            }
        }
        else{
            cost = -1
        }

        when(card_data.card_type){
            CardType.ATTACK -> {
                for(card in gameStatus.getPlayer(player).enchantmentCard.values){
                    card.card_data.effect?.let {
                        for(text in it){
                            if(card.enchantmentUsable(text)){
                                if(text.tag == TextEffectTag.CAN_NOT_USE_ATTACK) return -2
                            }
                        }
                    }
                }
                if(gameStatus.addPreAttackZone(player, this.makeAttack(player, gameStatus, react_attack, this.card_data.sub_type)?.addTextAndReturn(gameStatus.getUmbrella(this.player), this.card_data)?:
                    MadeAttack(
                        card_name =  this.card_data.card_name,
                        card_number = this.card_number,
                        card_class = this.card_data.card_class,
                        distance_type = DistanceType.DISCONTINUOUS,
                        life_damage = 0,
                        aura_damage = 0,
                        distance_cont = null,
                        distance_uncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false),
                        megami = this.card_data.megami,
                        cannotReactNormal = false,
                        cannotReactSpecial = false,
                        cannotReact = false,
                        chogek = false ,
                        inevitable = this.card_data.inevitable,
                        subType = this.card_data.sub_type
                    ))){
                    return cost
                }
                if(card_data.card_class == CardClass.SPECIAL){
                    gameStatus.cleanCostBuff()
                }
                return -2
            }
            CardType.UNDEFINED -> return -2
            else -> {}
        }

        return cost
    }

    suspend fun attackUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
        game_status.afterMakeAttack(this.card_number, player, react_attack)
    }

    suspend fun behaviorUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
        for(card in game_status.getPlayer(player).enchantmentCard.values){
            card.addValidEffect(TextEffectTag.WHEN_USE_BEHAVIOR_END, cardUseEndEffect)
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

    suspend fun enchantmentUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, nap_change: Int = -1) {
        val nowPlayer = game_status.getPlayer(player)

        for(card in game_status.getPlayer(player.opposite()).enchantmentCard.values){
            card.effectAllValidEffect(player.opposite(), game_status, TextEffectTag.WHEN_DEPLOYMENT_OTHER)
        }
        var nowNeedNap = if(nap_change == -1){
            returnNap(player, game_status, react_attack) + game_status.getPlayer(player).napBuff
        } else{
            nap_change + game_status.getPlayer(player).napBuff
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
            nowNeedNap > game_status.getPlayerAura(player) + game_status.dust -> {
                game_status.dustToCard(player, game_status.dust, this)
                game_status.auraToCard(player, game_status.getPlayerAura(player), this)
            }
            else -> {
                while (true) {
                    val (aura, dust)  =
                        receiveNapInformation(game_status.getSocket(player), nowNeedNap, this.card_number, CommandEnum.SELECT_NAP)
                    if (aura < 0 || dust < 0 || aura + dust != nowNeedNap || game_status.getPlayerAura(player) < aura || game_status.dust < dust) {
                        continue
                    }
                    game_status.auraToCard(player, aura, this)
                    game_status.dustToCard(player, dust, this)
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
                    receiveNapInformation(game_status.getSocket(player), nowNeedNap, this.card_number, CommandEnum.SELECT_GROWING)
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

    suspend fun use(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, isTermination: Boolean, nap_change: Int = -1){
        if(isTermination){
            game_status.setEndTurn(player, true)
        }

        this.card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    when(text.tag){
                        TextEffectTag.ADD_TEXT_TO_ATTACK, TextEffectTag.THIS_CARD_NAP_CHANGE,
                        TextEffectTag.ADD_LOG -> {
                            text.effect!!(this.card_number, player, game_status, react_attack)
                        }
                        else -> {
                        }
                    }
                }
            }
        }

        if(card_data.sub_type == SubType.FULL_POWER){
            for(card in game_status.getPlayer(player).usedSpecialCard.values){
                card.addValidEffect(TextEffectTag.WHEN_FULL_POWER_USED_YOUR, cardUseEndEffect)
            }
        }
        else if(card_data.sub_type == SubType.REACTION){
            for(card in game_status.getPlayer(player).enchantmentCard.values){
                card.addValidEffect(TextEffectTag.WHEN_USE_REACT_CARD_YOUR_END, cardUseEndEffect)
            }
        }
        react_attack?.addValidEffect(TextEffectTag.WHEN_THIS_CARD_REACTED, cardUseEndEffect)

        when(this.card_data.card_type){
            CardType.ATTACK -> {
                attackUseNormal(player, game_status, react_attack)
            }
            CardType.BEHAVIOR -> {1
                behaviorUseNormal(player, game_status, react_attack)
            }
            CardType.ENCHANTMENT -> {
                enchantmentUseNormal(player, game_status, react_attack, nap_change)
            }
            CardType.UNDEFINED -> {}
        }

        game_status.afterCardUsed(this.card_number, player, this)
    }

    fun chasmCheck(): Boolean{
        this.card_data.effect?.let {
            for(text in it){
                if(text === CardSet.chasm) return true
            }
        }
        return false
    }

    suspend fun swellAdjust(player: PlayerEnum, game_status: GameStatus): Int{
        this.card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.CHANGE_SWELL_DISTANCE){
                    when(text.timing_tag){
                        TextEffectTimingTag.IN_DEPLOYMENT -> {
                            if((this.nap ?: -1) > 0 && this.card_data.card_type == CardType.ENCHANTMENT) return text.effect!!(this.card_number, player, game_status, null)!!
                        }
                        TextEffectTimingTag.USED -> {
                            if(this.special_card_state == SpecialCardEnum.PLAYED) return text.effect!!(this.card_number, player, game_status, null)!!
                        }
                        else -> continue
                    }
                }

            }
        }
        return 0
    }

    fun checkAuraReplaceable(): Boolean{
        return this.card_data.effect?.let {
            var check = false
            for(text in it) {
                if (text.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT && text.tag == TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE && (nap
                        ?: -1) > 0
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

    suspend fun endPhaseEffect(player: PlayerEnum, game_status: GameStatus) {
        this.card_data.effect?.let {
            for(text in it){
                if (usedEffectUsable(text) || enchantmentUsable(text)) {
                    if(text.tag == TextEffectTag.WHEN_END_PHASE_YOUR){
                        text.effect!!(this.card_number, player, game_status, null)
                    }
                }
            }
        }
    }

    suspend fun endPhaseDiscardEffect(player: PlayerEnum, game_status: GameStatus) {
        this.card_data.effect?.let {
            for(text in it){
                if (text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT) {
                    if(text.tag == TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD){
                        text.effect!!(this.card_number, player, game_status, null)
                    }
                }
            }
        }
    }

    suspend fun checkWhenUmbrellaChange(player: PlayerEnum, game_status: GameStatus){
        this.card_data.effect?.let {
            for(text in it){
                if (text.tag == TextEffectTag.SHOW_HAND_WHEN_CHANGE_UMBRELLA){
                    text.effect!!(card_number, player, game_status, null)
                }
            }
        }
    }

    // It is assumed that no more than two are added
    fun addValidEffect(effectTag: TextEffectTag, queue: HashMap<Int, Text>){
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

    fun addValidEffect(location: CardEffectLocation, effectTag: TextEffectTag, queue: HashMap<Int, Pair<CardEffectLocation, Text?>>){
        card_data.effect?.let {
            for(text in it){
                if(usedEffectUsable(text) || enchantmentUsable(text)){
                    if(text.tag == effectTag) {
                        queue[this.card_number] = Pair(location, text)
                        return
                    }
                }
            }
        }
    }

    suspend fun effectAllValidEffect(player: PlayerEnum, game_status: GameStatus, effectTag: TextEffectTag): Int{
        var now = 0
        if(this.card_data.umbrellaMark) {
            when (game_status.getUmbrella(this.player)) {
                Umbrella.FOLD -> {
                    card_data.effectFold?.let {
                        for(text in it){
                            if(usedEffectUsable(text) || enchantmentUsable(text)){
                                if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, null)?.let { result ->
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
                                if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, null)?.let { result ->
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
                        if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, null)?.let { result ->
                            now += result
                        }
                    }
                }
            }
        }

        return now
    }

    fun operationForbidCheck(forbidYour: Boolean, command: CommandEnum): Boolean{
        val findTag = when(command){
            CommandEnum.ACTION_GO_BACKWARD -> if(forbidYour) TODO() else TextEffectTag.FORBID_GO_BACKWARD_OTHER
            CommandEnum.ACTION_BREAK_AWAY -> if(forbidYour) TODO() else TextEffectTag.FORBID_BREAK_AWAY_OTHER
            CommandEnum.ACTION_INCUBATE -> if(forbidYour) TODO() else TextEffectTag.FORBID_INCUBATE_OTHER
            else -> TODO()
        }
        card_data.effect?.let {
            for(text in it){
                if(enchantmentUsable(text) || usedEffectUsable(text)){
                    if(text.tag == findTag) return true
                }
            }
        }
        return false
    }

    fun canUseAtCover(): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.CAN_USE_COVER) return true
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

    suspend fun checkCanMoveToken(player: PlayerEnum, game_status: GameStatus): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(enchantmentUsable(text) || usedEffectUsable(text)){
                    if(text.tag == TextEffectTag.DO_NOT_MOVE_TOKEN){
                        return text.effect!!(this.card_number, player, game_status, null) == 1
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
}