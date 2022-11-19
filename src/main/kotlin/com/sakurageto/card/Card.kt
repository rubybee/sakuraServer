package com.sakurageto.card

import com.sakurageto.card.CardSet.returnCardDataByName
import kotlin.collections.ArrayDeque

class Card(val card_data: CardData, val player: PlayerEnum, var special_card_state: SpecialCardEnum?) {
    var vertical: Boolean
    var flipped: Boolean

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
}