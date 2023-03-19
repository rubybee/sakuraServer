package com.sakurageto.card

import com.sakurageto.card.CardSet.cardNameHashmapSecond
import com.sakurageto.card.CardSet.cardNameHashmapFirst
import com.sakurageto.card.CardSet.returnCardDataByName
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.Umbrella
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.receiveNapInformation
import kotlin.collections.ArrayDeque

class Card(val card_number: Int, var card_data: CardData, val player: PlayerEnum, var special_card_state: SpecialCardEnum?) {
    var vertical: Boolean
    var flipped: Boolean
    var nap: Int? = null

    init {
        vertical = true
        flipped = true
    }
    companion object{
        fun cardMakerByName(start_turn: Boolean, card_name: CardName, player: PlayerEnum): Card{
            val data = returnCardDataByName(card_name)
            if (data.isItSpecial()){
                if(start_turn){
                    return Card(cardNameHashmapFirst[card_name]?: -1, data, player, SpecialCardEnum.UNUSED)
                }
                return Card(cardNameHashmapSecond[card_name]?: -1, data, player, SpecialCardEnum.UNUSED)
            }
            else{
                if(start_turn){
                    return Card(cardNameHashmapFirst[card_name]?: -1, data, player, null)
                }
                return Card(cardNameHashmapSecond[card_name]?: -1, data, player, null)
            }

        }

        fun cardInitInsert(start_turn: Boolean, dest: ArrayDeque<Card>, src: MutableList<CardName>, player: PlayerEnum){
            src.shuffle()
            for(card_name in src){
                dest.add(cardMakerByName(start_turn, card_name, player))
            }
        }

        fun cardInitInsert(start_turn: Boolean, dest: HashMap<Int, Card>, src: MutableList<CardName>, player: PlayerEnum){
            for(card_name in src){
                if(start_turn){
                    dest[cardNameHashmapFirst[card_name]?: -1] = cardMakerByName(true, card_name, player)
                }
                else{
                    dest[cardNameHashmapSecond[card_name]?: -1] = cardMakerByName(false, card_name, player)
                }
            }
        }

        fun cardReconstructInsert(src1: ArrayDeque<Card>, src2: ArrayDeque<Card>, dest: ArrayDeque<Card>){
            dest.addAll(src1)
            dest.addAll(src2)
            dest.shuffle()
            src1.clear()
            src2.clear()
        }
    }

    fun enchantmentUsable(text: Text): Boolean =
        text.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT && (nap?: 0) > 0
    fun usedEffectUsable(text: Text): Boolean =
        text.timing_tag == TextEffectTimingTag.USED && this.special_card_state == SpecialCardEnum.PLAYED

    fun reduceNapNormal(): Int{
       card_data.effect?.let {
           for(i in it){
               if(i.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT || i.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT){
                   when(i.tag){
                       TextEffectTag.DO_NOT_NAP -> {
                           return 0
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

    fun canReactable(attack: MadeAttack): Boolean{
        if(attack.cannot_react_special){
            if(card_data.card_class == CardClass.SPECIAL){
                return false
            }
        }
        else if(attack.cannot_react){
            return false
        }
        else if(attack.cannot_react_normal){
            if(card_data.card_class == CardClass.NORMAL){
                return false
            }
        }
        return true
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
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT && text.tag == TextEffectTag.USING_CONDITION){
                    if(text.effect!!(this.card_number, player, game_status, react_attack)!! == 1){
                        return true
                    }
                    return false
                }
            }
        }
        return true
    }

    suspend fun makeAttack(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): MadeAttack{
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
                        megami = this.card_data.megami
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
                        megami = this.card_data.megami
                    )
                }
                null -> {
                    return MadeAttack(
                        card_name =  this.card_data.card_name,
                        card_number = this.card_number,
                        card_class = this.card_data.card_class,
                        distance_type = DistanceType.DISCONTINUOUS,
                        life_damage = 0,
                        aura_damage = 0,
                        distance_cont = null,
                        distance_uncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false),
                        megami = this.card_data.megami
                    )
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
                megami = this.card_data.megami
            )
        }
    }

    //-2: can't use                    -1: can use                 >= 0: cost
    suspend fun canUse(player: PlayerEnum, gameStatus: GameStatus, react_attack: MadeAttack?, isCost: Boolean, isConsume: Boolean): Int{
        if(card_data.sub_type == SubType.FULL_POWER && !gameStatus.getPlayerFullAction(player)) return -2

        if(!textUseCheck(player, gameStatus, react_attack)){
            return -2
        }

        val cost: Int

        if(card_data.card_class == CardClass.SPECIAL){
            if(isCost && isConsume){
                this.thisCardCostBuff(player, gameStatus)
                gameStatus.addAllCardCostBuff()
                cost = gameStatus.applyAllCostBuff(player, this.getBaseCost(player, gameStatus), this)
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
                for(card in gameStatus.getPlayer(player).enchantment_card.values){
                    for(text in card.card_data.effect!!){
                        if(enchantmentUsable(text)){
                            if(text.tag == TextEffectTag.CAN_NOT_USE_ATTACK) return -2
                        }
                    }
                }
                if(gameStatus.addPreAttackZone(player, this.makeAttack(player, gameStatus, react_attack).addTextAndReturn(gameStatus.getUmbrella(this.player), this.card_data))){
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

    suspend fun attackUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?,
                                reactAttackBuffQueue: AttackBuffQueue?, reactRangeBuffQueue: RangeBuffQueue?){
        game_status.afterMakeAttack(this.card_number, player, react_attack, reactAttackBuffQueue, reactRangeBuffQueue)
    }

    suspend fun behaviorUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
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

        for(card in game_status.getPlayer(player).enchantment_card.values){
            effectAllMaintainCard(player, game_status, TextEffectTag.WHEN_USE_BEHAVIOR_END)
        }

    }

    suspend fun enchantmentUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?) {
        var now_need_nap = returnNap(player, game_status, react_attack) + game_status.getPlayer(player).napBuff
        if(now_need_nap < 0) now_need_nap = 0
        game_status.getPlayer(player).napBuff = 0
        when {
            now_need_nap == 0 -> {}
            now_need_nap > game_status.getPlayerAura(player) + game_status.dust -> {
                game_status.dustToCard(player, game_status.dust, this)
                game_status.auraToCard(player, game_status.getPlayerAura(player), this)
            }
            else -> {
                while (true) {
                    val receive_data =
                        receiveNapInformation(game_status.getSocket(player), now_need_nap, this.card_number)
                    val aura = receive_data.first
                    val dust = receive_data.second
                    if (aura < 0 || dust < 0 || aura + dust != now_need_nap || game_status.getPlayerAura(player) < aura || game_status.dust < dust) {
                        continue
                    }
                    game_status.auraToCard(player, aura, this)
                    game_status.dustToCard(player, dust, this)
                    break
                }
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

    suspend fun use(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?,
                    reactAttackBuffQueue: AttackBuffQueue?, reactRangeBuffQueue: RangeBuffQueue?){
        this.card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    when(text.tag){
                        TextEffectTag.TERMINATION -> {
                            game_status.setEndTurn(player, true)
                        }
                        TextEffectTag.ADD_LOG -> {
                            text.effect!!(this.card_number, player, game_status, react_attack)
                        }
                        else -> {

                        }
                    }
                }
            }
        }

        when(this.card_data.card_type){
            CardType.ATTACK -> {
                attackUseNormal(player, game_status, react_attack, reactAttackBuffQueue, reactRangeBuffQueue)
            }
            CardType.BEHAVIOR -> {
                behaviorUseNormal(player, game_status, react_attack)
            }
            CardType.ENCHANTMENT -> {
                enchantmentUseNormal(player, game_status, react_attack)
            }
            CardType.UNDEFINED -> {}
        }

        game_status.afterCardUsed(this.card_number, player)
    }

    fun chasmCheck(): Boolean{
        this.card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.CHASM) return true
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
        return this.card_data.effect?.let {
            var check = false
            for (text in it){
                if(text.timing_tag == TextEffectTimingTag.USED && text.tag == TextEffectTag.RETURN){
                    if(text.effect!!(this.card_number, player, game_status, null) == 1) {
                        check = true
                        break
                    }
                }
            }
            check
        }?: false
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

    suspend fun forbidTokenMove(player: PlayerEnum, game_status: GameStatus): Int{
        this.card_data.effect?.let{
            for(text in it){
                if (text.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT && text.tag == TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE && (nap
                        ?: -1) > 0
                ) {
                    return text.effect!!(this.card_number, player, game_status, null)!!
                }
            }
        }
        return -1
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

    suspend fun endPhaseEffect(player: PlayerEnum, game_status: GameStatus) {
        this.card_data.effect?.let {
            for(text in it){
                if (usedEffectUsable(text)) {
                    if(text.tag == TextEffectTag.WHEN_END_PHASE_YOUR){
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

    suspend fun effectAllMaintainCard(player: PlayerEnum, game_status: GameStatus, effectTag: TextEffectTag): Int{
        var now = 0
        card_data.effect?.let {
            for(text in it){
                if(usedEffectUsable(text)){
                    if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, null)?.let { result ->
                        now += result
                    }
                }
                else if(enchantmentUsable(text)){
                    if(text.tag == effectTag) text.effect!!(this.card_number, player, game_status, null)?.let {result ->
                        now += result
                    }
                }
            }
        }
        return now
    }

    fun operationForbidCheck(forbidYour: Boolean, command: CommandEnum, game_status: GameStatus): Boolean{
        val findTag = when(command){
            CommandEnum.ACTION_GO_BACKWARD -> if(forbidYour) TODO() else TextEffectTag.FORBID_GO_BACKWARD_OTHER
            CommandEnum.ACTION_BREAK_AWAY -> if(forbidYour) TODO() else TextEffectTag.FORBID_BREAK_AWAY
            else -> TODO()
        }
        card_data.effect?.let {
            for(text in it){
                if(enchantmentUsable(text)){
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

}