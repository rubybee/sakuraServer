package com.sakurageto.gamelogic

import com.sakurageto.card.CardName
import com.sakurageto.card.CardSet
import com.sakurageto.card.PlayerEnum

//number1, number2 used to express location and card number
class Log(val player: PlayerEnum, val text: LogText, val number1: Int, val number2: Int) {
    fun cardCheck(card_name: CardName): Boolean{
        return CardSet.cardNameHashmapFirst[card_name] == number1 || CardSet.cardNameHashmapSecond[card_name] == number1
    }

}

enum class LogText{
    ATTACK,
    USE_CARD_IN_COVER_AND_REACT,
    USE_CARD_IN_COVER,
    USE_CARD,
    USE_CARD_REACT,
    GET_LIFE_DAMAGE,
    GET_AURA_DAMAGE,
}