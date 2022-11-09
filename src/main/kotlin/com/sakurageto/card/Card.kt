package com.sakurageto.card

import com.sakurageto.card.CardSet.returnCardDataByName

class Card(val card_data: CardData, val player: PlayerEnum) {
    var special_card_state: SpecialCardEnum? = null
    var vertical: Boolean
    var flipped: Boolean

    init {
        vertical = true
        flipped = true
    }
    companion object{
        fun cardMakerByName(card_name: CardName, player: PlayerEnum): Card{
            return Card(returnCardDataByName(card_name), player)
        }
    }
}