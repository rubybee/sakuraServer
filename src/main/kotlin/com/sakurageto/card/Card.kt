package com.sakurageto.card

import com.sakurageto.card.CardSet.cardname_hashmap_for_second_turn
import com.sakurageto.card.CardSet.cardname_hashmap_for_start_turn
import com.sakurageto.card.CardSet.returnCardDataByName
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.PlayerStatus
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
                    return Card(cardname_hashmap_for_start_turn[card_name]?: -1, data, player, SpecialCardEnum.UNUSED)
                }
                return Card(cardname_hashmap_for_second_turn[card_name]?: -1, data, player, SpecialCardEnum.UNUSED)
            }
            else{
                if(start_turn){
                    return Card(cardname_hashmap_for_start_turn[card_name]?: -1, data, player, null)
                }
                return Card(cardname_hashmap_for_second_turn[card_name]?: -1, data, player, null)
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
                    dest[cardname_hashmap_for_start_turn[card_name]?: -1] = cardMakerByName(true, card_name, player)
                }
                else{
                    dest[cardname_hashmap_for_second_turn[card_name]?: -1] = cardMakerByName(false, card_name, player)
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

    fun reduceNapNormaly(): Int{
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
        nap = nap!! - 1
        return 1
    }

    fun isItDestruction(): Boolean{
        //some text can be added
        if(nap == 0){
            return true
        }
        return false
    }

    fun destructionEnchantmentNormaly(): ArrayDeque<Text>{
        val return_data: ArrayDeque<Text> = ArrayDeque()
        card_data.effect?.let {
            for(i in it){
                when(i.timing_tag){
                    TextEffectTimingTag.AFTER_DESTRUCTION -> {
                        return_data.add(i)
                    }
                    else -> {}
                }
            }
        }
        return return_data
    }
    suspend fun addAttackBuff(player: PlayerEnum, gameStatus: GameStatus){
        card_data.effect?.let {
            for(i in it){
                when(i.timing_tag){
                    TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTimingTag.USED -> {
                        when(i.tag){
                            TextEffectTag.NEXT_ATTACK_ENCHANTMENT -> {
                               i.effect!!(player, gameStatus, null)
                            }
                            else -> continue
                        }
                    }
                    TextEffectTimingTag.IN_DEPLOYMENT -> {
                        if((nap ?:0) >= 1 && card_data.card_type == CardType.ENCHANTMENT){
                            when(i.tag){
                                TextEffectTag.NEXT_ATTACK_ENCHANTMENT -> {
                                    i.effect!!(player, gameStatus, null)
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

    suspend fun addCostBuff(player: PlayerEnum, gameStatus: GameStatus){
        card_data.effect?.let {
            for(i in it){
                when(i.timing_tag){
                    TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTimingTag.USED -> {
                        when(i.tag){
                            TextEffectTag.COST_BUFF -> {
                                i.effect!!(player, gameStatus, null)
                            }
                            else -> continue
                        }
                    }
                    TextEffectTimingTag.IN_DEPLOYMENT -> {
                        if((nap ?:0) >= 1 && card_data.card_type == CardType.ENCHANTMENT){
                            when(i.tag){
                                TextEffectTag.COST_BUFF -> {
                                    i.effect!!(player, gameStatus, null)
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
        card_data.effect?.let {
            for(text in it){
                when(text.timing_tag){
                    TextEffectTimingTag.CONSTANT_EFFECT -> {
                        when(text.tag){
                            TextEffectTag.CAN_REACTABLE -> {
                                if(text.effect!!(player, gameStatus, null)!! == 1){
                                    return true
                                }
                                return false
                            }
                            else -> continue
                        }
                    }
                    else -> continue

                }
            }
        }
        return false
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
                            return text.effect!!(player, gamestatus, react_attack)!!
                        }
                    }
                }
            }

            return this.card_data.charge!!
        }
    }

    suspend fun getBaseCost(player: PlayerEnum, gameStatus: GameStatus): Int{
        if(card_data.cost != null){
            return card_data.cost!!
        }
        else{
            card_data.effect?.let {
                for(text in it){
                    if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT){
                        if(text.tag == TextEffectTag.COST_X){
                            return text.effect!!(player, gameStatus, null)!!
                        }
                    }
                }
            }
        }
        return 1000
    }

    suspend fun textUseCheck(player: PlayerEnum, gameStatus: GameStatus): Boolean{
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT && text.tag == TextEffectTag.USING_CONDITION){
                    if(text.effect!!(player, gameStatus, null)!! == 1){
                        return true
                    }
                    return false
                }
            }
        }
        return true
    }

    suspend fun makeAttack(player: PlayerEnum, gameStatus: GameStatus): MadeAttack{
        this.addAttackBuff(player, gameStatus)
        return MadeAttack(
            distance_type = this.card_data.distance_type!!,
            life_damage = this.card_data.life_damage!!,
            aura_damage = this.card_data.aura_damage!!,
            distance_cont = this.card_data.distance_cont,
            distance_uncont = this.card_data.distance_uncont,
            megami = this.card_data.megami
        )
    }
    //-2: can't use -1: can use >= 0 cost
    suspend fun canUse(player: PlayerEnum, gameStatus: GameStatus): Int{
        if(card_data.sub_type == SubType.FULLPOWER){
            if(!gameStatus.getPlayerFullAction(player)){
                return -2
            }
        }

        if(!textUseCheck(player, gameStatus)){
            return -2
        }

        var cost: Int

        if(card_data.card_class == CardClass.SPECIAL){
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
                if(gameStatus.addPreAttackZone(player, this.makeAttack(player, gameStatus).addAttackAndReturn(this.card_data))){
                    return cost
                }
                return -2
            }
            CardType.UNDEFINED -> return -2
            else -> {}
        }

        return cost
    }

    suspend fun attackUseNormal(player: PlayerEnum, gamestatus: GameStatus, react_attack: MadeAttack?){
        gamestatus.afterMakeAttack(this.card_data.card_name, player, react_attack)
    }

    suspend fun behaviorUseNormal(player: PlayerEnum, gamestatus: GameStatus, react_attack: MadeAttack?){
        card_data.effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.USING){
                    text.effect!!(player, gamestatus, react_attack)
                }
            }
        }
    }

    suspend fun enchantmentUseNormal(player: PlayerEnum, gamestatus: GameStatus, react_attack: MadeAttack?) {
        val now_need_nap = returnNap(player, gamestatus, react_attack)
        if (now_need_nap > gamestatus.getPlayerAura(player) + gamestatus.dust) {
            gamestatus.dustToCard(player, gamestatus.dust, this)
            gamestatus.auraToCard(player, gamestatus.getPlayerAura(player), this)
        } else {
            while (true) {
                val receive_data =
                    receiveNapInformation(gamestatus.getSocket(player), now_need_nap, this.card_data.card_name)
                val aura = receive_data.first
                val dust = receive_data.second
                if (aura + dust != now_need_nap && gamestatus.getPlayerAura(player) < aura && gamestatus.dust < dust) {
                    continue
                }
                gamestatus.auraToCard(player, aura, this)
                gamestatus.dustToCard(player, dust, this)
                break
            }
        }

        card_data.effect?.let {
            for (text in it) {
                if(text.timing_tag == TextEffectTimingTag.START_DEPLOYMENT){
                    text.effect!!(player, gamestatus, react_attack)
                }
            }
        }
    }

    suspend fun use(player: PlayerEnum, gamestatus: GameStatus, react_attack: MadeAttack?){
        when(this.card_data.card_type){
            CardType.ATTACK -> {
                attackUseNormal(player, gamestatus, react_attack)
            }
            CardType.BEHAVIOR -> {
                behaviorUseNormal(player, gamestatus, react_attack)
            }
            CardType.ENCHANTMENT -> {
                enchantmentUseNormal(player, gamestatus, react_attack)
            }
            CardType.UNDEFINED -> {}
        }

        gamestatus.afterCardUsed(player, this)
    }

    fun chasmCheck(): Boolean{
        this.card_data.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.CHASM) return true
            }
        }
        return false
    }

}