package com.sakurageto.gamelogic

import com.sakurageto.card.CardName
import com.sakurageto.card.CardSet
import com.sakurageto.card.PlayerEnum

//number1, number2 used to express location and card number
/**
 when (text == ATTACK): number1 means attack number number2 means attack's type(0 == null, 1 == normal, 2 == special)
 */
class Log(val player: PlayerEnum, val text: LogText, val number1: Int, val number2: Int) {
    fun cardCheck(card_name: CardName): Boolean{
        return card_name.toCardNumber(true) == number1 || card_name.toCardNumber(false) == number1
    }

}

enum class LogText{
    ATTACK,
    USE_CARD_IN_COVER_AND_REACT,
    USE_CARD_IN_COVER,
    USE_CARD_IN_SOLDIER,
    USE_CARD,
    USE_CARD_REACT,
    USE_CENTRIFUGAL,
    GET_LIFE_DAMAGE,
    GET_AURA_DAMAGE,
}