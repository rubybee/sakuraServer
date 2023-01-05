package com.sakurageto.gamelogic

import com.sakurageto.card.CardName

class ImmediateBackListner(
    var card_number: Int,
    var condition: (Int, Int, Boolean) -> Boolean
){

    fun IsItBack(before_life: Int, after_life: Int, reconstruct: Boolean): Boolean{
        return condition(before_life, after_life, reconstruct)
    }
}