package com.sakurageto.gamelogic.megamispecial

import com.sakurageto.card.Card
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.SubType

data class Kikou(var attack: Int = 0, var behavior: Int = 0, var enchantment: Int = 0, var reaction: Int = 0, var fullPower: Int = 0){
    fun add(card: Card){
        when(card.card_data.card_type){
            CardType.ATTACK -> this.attack += 1
            CardType.BEHAVIOR -> this.behavior += 1
            CardType.ENCHANTMENT -> this.enchantment += 1
            CardType.UNDEFINED -> {}
        }
        when(card.card_data.sub_type){
            SubType.FULL_POWER -> this.fullPower += 1
            SubType.REACTION -> this.reaction += 1
            SubType.NONE -> {}
            SubType.UNDEFINED -> {}
        }
    }
}