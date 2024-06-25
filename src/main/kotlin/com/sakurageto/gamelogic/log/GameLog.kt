package com.sakurageto.gamelogic.log

import com.sakurageto.card.basicenum.PlayerEnum
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
class GameLog(val player: PlayerEnum, val text: LogEnum, val number1: Int, val number2: Int, val resource: LocationEnum = LocationEnum.DUST,
              val destination: LocationEnum = LocationEnum.DUST, val boolean: Boolean = false, var boolean2: Boolean = false) {

    fun isTextUseCard() = this.text == LogEnum.USE_CARD || this.text == LogEnum.USE_CARD_IN_SOLDIER ||
            this.text == LogEnum.USE_CARD_IN_COVER || this.text == LogEnum.USE_CARD_PERJURE ||
            this.text == LogEnum.USE_CARD_IN_SOLDIER_PERJURE

    fun isAhumBasicOperation(ahumPlayer: PlayerEnum) =
        text == LogEnum.MOVE_TOKEN && number1 == BASIC_OPERATION && player == ahumPlayer.opposite() &&
                isMoveAura()

    fun isMoveAura() =
        (destination == LocationEnum.AURA_YOUR || resource == LocationEnum.AURA_YOUR) && number2 >= 1

    fun isMoveAuraForAttack(player: PlayerEnum) =
        player == this.player && (destination == LocationEnum.AURA_YOUR || resource == LocationEnum.AURA_YOUR ||
                destination == LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD || resource == LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD)
                && number2 >= 1

    fun isGetDamageLog() = this.text == LogEnum.GET_LIFE_DAMAGE || this.text == LogEnum.GET_AURA_DAMAGE
            || this.text == LogEnum.GET_FLARE_DAMAGE

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

        const val ATTACK_NUMBER_NULL = 0
        const val ATTACK_NUMBER_NORMAL = 1
        const val ATTACK_NUMBER_SPECIAL = 2
        const val ATTACK_NUMBER_SOLDIER = 3
        const val ATTACK_NUMBER_IDEA = 4
        const val ATTACK_NUMBER_POISON = 5
        const val ATTACK_NUMBER_PARTS = 6
    }
}

