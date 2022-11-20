package com.sakurageto.card

import com.sakurageto.card.CardSet.returnCardDataByName
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
               when(i.timing_tag){
                   TextEffectTimingTag.CONSTANT_EFFECT -> {
                   }
                   TextEffectTimingTag.IN_DEPLOYMENT -> {
                   }
                   else -> {
                       continue
                   }
               }
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




}