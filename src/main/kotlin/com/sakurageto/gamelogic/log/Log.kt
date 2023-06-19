package com.sakurageto.gamelogic.log

import com.sakurageto.card.CardName
import com.sakurageto.card.PlayerEnum
import com.sakurageto.protocol.LocationEnum

//number1, number2 used to express location and card number
/**
 when (text == ATTACK): number1 means attack number number2 means attack's type(0 == null, 1 == normal, 2 == special)
 when (text == MOVE_TOKEN) number1 is reason of move(card's number), number2 is number of moves, boolean is arrow

 파기후에 endeffect

 사용후에 endeffect

 텍스트 내에 processdamage가 존재할경우 처리해줘야됨
 used, constanteffect, in_deployment 같은 형태로 사용될 경우 처리해줘야됨
 */
class Log(val player: PlayerEnum, val text: LogText, val number1: Int, val number2: Int, val resource: LocationEnum = LocationEnum.DUST,
          val destination: LocationEnum = LocationEnum.DUST, val boolean: Boolean = false) {
    fun cardCheck(card_name: CardName): Boolean{
        return card_name.toCardNumber(true) == number1 || card_name.toCardNumber(false) == number1
    }

    fun isTextUseCard() = this.text == LogText.USE_CARD || this.text == LogText.USE_CARD_IN_SOLDIER ||
            this.text == LogText.USE_CARD_REACT || this.text == LogText.USE_CARD_IN_COVER || this.text == LogText.USE_CARD_IN_COVER_AND_REACT

    companion object{
        const val SPECIAL_COST = 0
        const val AFTER_DESTRUCTION_PROCESS = 1
        const val NORMAL_NAP_PROCESS = 2
        const val CHOJO = 3
        const val DECK_RECONSTRUCT_DAMAGE = 4
        const val BASIC_OPERATION = 5 //don't have end_effect log
        const val NORMAL_NAP_COST = 6
        const val DEMISE = 7
        const val IGNORE = 8 //for playing zone token
        const val STORM_FORCE = 9
        const val ACT_DAMAGE = 10
        const val END_IDEA = 11
    }
}

enum class LogText{
    MOVE_TOKEN,
    END_EFFECT,
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