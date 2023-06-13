package com.sakurageto.gamelogic.log

import com.sakurageto.card.CardName
import com.sakurageto.card.PlayerEnum

//number1, number2 used to express location and card number
/**
 when (text == ATTACK): number1 means attack number number2 means attack's type(0 == null, 1 == normal, 2 == special)
 */
class Log(val player: PlayerEnum, val text: LogText, val number1: Int, val number2: Int, val number3: Int = 0, val boolean: Boolean = false) {
    fun cardCheck(card_name: CardName): Boolean{
        return card_name.toCardNumber(true) == number1 || card_name.toCardNumber(false) == number1
    }

    fun isTextUseCard() = this.text == LogText.USE_CARD || this.text == LogText.USE_CARD_IN_SOLDIER ||
            this.text == LogText.USE_CARD_REACT || this.text == LogText.USE_CARD_IN_COVER || this.text == LogText.USE_CARD_IN_COVER_AND_REACT
}

enum class LogText{
    IDEA,
    ATTACK,
    TRANSFORM,
    USE_CARD_IN_COVER_AND_REACT,
    USE_CARD_IN_COVER,
    USE_CARD_IN_SOLDIER,
    USE_CARD,
    USE_CARD_REACT,
    USE_CENTRIFUGAL,
    GET_LIFE_DAMAGE,
    GET_AURA_DAMAGE,
}