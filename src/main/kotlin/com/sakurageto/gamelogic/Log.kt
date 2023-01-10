package com.sakurageto.gamelogic

import com.sakurageto.card.PlayerEnum

//number1, number2 used to express location and card number
class Log(val player: PlayerEnum, val text: LogText, val number1: Int, val number2: Int) {
}

enum class LogText{
    USE_CARD,
    USE_CARD_REACT
}