package com.sakurageto.card

import com.sakurageto.card.CardSet.returnCardDataByName
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.PlayerStatus
import kotlin.collections.ArrayDeque

class Card(val card_data: CardData, val player: PlayerEnum, var special_card_state: SpecialCardEnum?) {
    var vertical: Boolean
    var flipped: Boolean
    var nap: Int? = null

    init {
        vertical = true
        flipped = true
    }
    companion object{
        fun cardMakerByName(card_name: CardName, player: PlayerEnum): Card{
            val data = returnCardDataByName(card_name)
            if (data.isItSpecial()){
                return Card(data, player, SpecialCardEnum.UNUSED)
            }
            else{
                return Card(data, player, null)
            }

        }

        fun cardInitInsert(dest: ArrayDeque<Card>, src: MutableList<CardName>, player: PlayerEnum) {
            src.shuffle()
            for(card_name in src){
                dest.add(cardMakerByName(card_name, player))
            }
        }
    }

    fun reduceNapNormaly(): Boolean{
       card_data.effect?.let {
           for(i in it){
               if(i.timing_tag == TextEffectTimingTag.CONSTANT_EFFECT || i.timing_tag == TextEffectTimingTag.IN_DEPLOYMENT){
                   when(i.tag){
                       TextEffectTag.DO_NOT_NAP -> {
                           return false
                       }
                       else -> {
                           continue
                       }
                   }
               }
           }
       }
        nap = nap!! - 1
        return true
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
    suspend fun canUse(player: PlayerEnum, gameStatus: GameStatus): Boolean{
        if(card_data.sub_type == SubType.FULLPOWER){
            if(!gameStatus.getPlayerFullAction(player)){
                return false
            }
        }

        if(!textUseCheck(player, gameStatus)){
            return false
        }

        if(card_data.card_class == CardClass.SPECIAL){
            gameStatus.addAllCardCostBuff()
        }

        return true
    }
}