package com.sakurageto.card

import com.sakurageto.card.CardSet.cardNameHashmapSecond
import com.sakurageto.card.CardSet.cardNameHashmapFirst
import com.sakurageto.card.CardSet.returnCardDataByName
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.protocol.receiveNapInformation
import kotlin.collections.ArrayDeque

class Card(val card_number: Int, val card_data: CardData, val player: PlayerEnum, var special_card_state: SpecialCardEnum?) {
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
                    when(text.tag){
                        TextEffectTag.MAKE_ATTACK -> {
                            text.effect!!(this.card_number, player, game_status, null)
                            game_status.afterMakeAttack(this.card_number, player, null)
                        }
                        else -> text.effect!!(this.card_number, player, game_status, null)
                    }
                }
            }
        }
    }
    fun isItDestruction(): Boolean{
        //some text can be added
        return nap == 0
    }

    suspend fun addAttackBuff(player: PlayerEnum, gameStatus: GameStatus){
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.USED && this.special_card_state == SpecialCardEnum.PLAYED){
                    if(text.tag == TextEffectTag.NEXT_ATTACK_ENCHANTMENT) text.effect!!(this.card_number, player, gameStatus, null)
                }
                else if(text.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT && (nap?: 0) > 0){
                    if(text.tag == TextEffectTag.NEXT_ATTACK_ENCHANTMENT) text.effect!!(this.card_number, player, gameStatus, null)
                }
            }
        }
    }

    suspend fun addCostBuff(player: PlayerEnum, gameStatus: GameStatus){
        card_data.effect?.let {
            for(i in it){
                when(i.timing_tag){
                    TextEffectTimingTag.CONSTANT_EFFECT -> {
                        when(i.tag){
                            TextEffectTag.COST_BUFF -> {
                                if(this.special_card_state != SpecialCardEnum.PLAYED) i.effect!!(this.card_number, player, gameStatus, null)
                            }
                            else -> continue
                        }
                    }
                    TextEffectTimingTag.USED -> {
                        when(i.tag){
                            TextEffectTag.COST_BUFF -> {
                                if(this.special_card_state == SpecialCardEnum.PLAYED) i.effect!!(this.card_number, player, gameStatus, null)
                            }
                            else -> continue
                        }
                    }
                    TextEffectTimingTag.IN_DEPLOYMENT -> {
                        if((nap ?:0) >= 1 && card_data.card_type == CardType.ENCHANTMENT){
                            when(i.tag){
                                TextEffectTag.COST_BUFF -> {
                                    i.effect!!(this.card_number, player, gameStatus, null)
                                }
                                else -> continue
                            }
                        }
                    }
                    else -> continue
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

    suspend fun returnNap(player: PlayerEnum, gamestatus: GameStatus, react_attack: MadeAttack?): Int{
        if(this.card_data.charge == null){
            return -1
        }
        else{
            this.card_data.effect?.let {
                for(text in it){
                    if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                        if(text.tag == TextEffectTag.ADJUST_NAP){
                            return text.effect!!(this.card_number, player, gamestatus, react_attack)!!
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
        return MadeAttack(
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

    //-2: can't use                    -1: can use                 >= 0: cost
    suspend fun canUse(player: PlayerEnum, gameStatus: GameStatus, react_attack: MadeAttack?): Int{
        if(card_data.sub_type == SubType.FULLPOWER && !gameStatus.getPlayerFullAction(player)) return -2

        if(!textUseCheck(player, gameStatus, react_attack)){
            return -2
        }

        val cost: Int

        if(card_data.card_class == CardClass.SPECIAL){
            this.addCostBuff(player, gameStatus)
            gameStatus.addAllCardCostBuff()
            cost = gameStatus.applyAllCostBuff(player, this.getBaseCost(player, gameStatus), this)
            if(cost > gameStatus.getPlayerFlare(player)){
                gameStatus.cleanCostBuff()
                return -2
            }
        }
        else{
            cost = -1
        }

        when(card_data.card_type){
            CardType.ATTACK -> {
                if(gameStatus.addPreAttackZone(player, this.makeAttack(player, gameStatus, react_attack).addAttackAndReturn(this.card_data))){
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
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.USING){
                    when(text.tag){
                        TextEffectTag.MAKE_ATTACK -> {
                            text.effect!!(this.card_number, player, game_status, null)
                            game_status.afterMakeAttack(this.card_number, player, null)
                        }
                        else -> text.effect!!(this.card_number, player, game_status, react_attack)
                    }

                }
            }
        }
    }

    suspend fun enchantmentUseNormal(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?) {
        val now_need_nap = returnNap(player, game_status, react_attack)
        if (now_need_nap > game_status.getPlayerAura(player) + game_status.dust) {
            game_status.dustToCard(player, game_status.dust, this)
            game_status.auraToCard(player, game_status.getPlayerAura(player), this)
        } else {
            while (true) {
                val receive_data =
                    receiveNapInformation(game_status.getSocket(player), now_need_nap, this.card_number)
                val aura = receive_data.first
                val dust = receive_data.second
                if (aura + dust != now_need_nap || game_status.getPlayerAura(player) < aura || game_status.dust < dust) {
                    continue
                }
                game_status.auraToCard(player, aura, this)
                game_status.dustToCard(player, dust, this)
                break
            }
        }

        card_data.effect?.let {
            for (text in it) {
                if(text.timing_tag == TextEffectTimingTag.START_DEPLOYMENT){
                    when(text.tag){
                        TextEffectTag.MAKE_ATTACK -> {
                            text.effect!!(this.card_number, player, game_status, null)
                            game_status.afterMakeAttack(this.card_number, player, null)
                        }
                        else -> text.effect!!(this.card_number, player, game_status, react_attack)
                    }
                }
            }
        }
    }

    suspend fun use(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
        this.card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                    when(text.tag){
                        TextEffectTag.TERMINATION -> {
                            game_status.setEndTurn(player, true)
                        }
                        else -> {

                        }
                    }
                }
            }
        }

        when(this.card_data.card_type){
            CardType.ATTACK -> {
                attackUseNormal(player, game_status, react_attack)
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
}