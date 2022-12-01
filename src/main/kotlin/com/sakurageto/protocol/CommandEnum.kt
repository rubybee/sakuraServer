package com.sakurageto.protocol

import com.sakurageto.gamelogic.MegamiEnum

enum class CommandEnum() {
    SELECT_MODE,
    END_OF_SELECTMODE,
    SELECT_MEGAMI,
    END_OF_SELECT_MEGAMI,
    CHECK_MEGAMI,
    SELECT_BAN,
    END_SELECT_BAN,
    CHECK_YOUR,
    CHECK_ANOTHER,
    SELECT_CARD,
    END_SELECT_CARD,
    RETURN_SPECIAL_CARD,
    FIRST_TURN,
    SECOND_TURN,
    DRAW,
    MULIGUN,
    MULIGUN_END,
    START_TURN,
    ADD_CONCENTRATION_YOUR,
    ADD_CONCENTRATION_OTHER,
    REMOVE_SHRINK_YOUR,
    REMOVE_SHRINK_OTHER,
    REDUCE_NAP_START,
    REDUCE_NAP_YOUR,
    REDUCE_NAP_OTHER,
    REDUCE_NAP_END,
    SELECT_ENCHANTMENT_START,
    SELECT_ENCHANTMENT_YOUR,
    SELECT_ENCHANTMENT_OTHER,
    SELECT_ENCHANTMENT_END,
    DESTRUCTION_ENCHANTMENT_YOUR,
    DESTRUCTION0_ENCHANTMENT_OTHER,
    DISCARD_CARD_YOUR,
    USED_CARD_YOUR,
    DISCARD_CARD_OTHER,
    USED_CARD_OTHER,
    MAKE_ATTACK_COMPLETE_YOUR,
    MAKE_ATTACK_COMPLETE_OTHER,
    ATTACK_INFORMATION_YOUR,
    ATTACK_INFORMATION_OTHER,
    REACT_REQUEST,
    CHOOSE_DAMAGE,
    CHOOSE_CHOJO,
    CHOOSE_AURA,
    CHOOSE_LIFE,
    USE_CARD_IN_HAND,
    USE_CARD_IN_SPEICAL,
    DO_NOT_REACT,
    MOVE_TOKEN;

    fun Opposite(): CommandEnum{
        when(this){
            ADD_CONCENTRATION_YOUR -> return ADD_CONCENTRATION_OTHER
            ADD_CONCENTRATION_OTHER -> return ADD_CONCENTRATION_YOUR
            REMOVE_SHRINK_YOUR -> return REMOVE_SHRINK_OTHER
            REMOVE_SHRINK_OTHER -> return REMOVE_SHRINK_YOUR
            REDUCE_NAP_YOUR -> return REDUCE_NAP_OTHER
            REDUCE_NAP_OTHER -> return REDUCE_NAP_YOUR
            SELECT_ENCHANTMENT_YOUR -> return SELECT_ENCHANTMENT_OTHER
            SELECT_ENCHANTMENT_OTHER -> return SELECT_ENCHANTMENT_YOUR
            DESTRUCTION_ENCHANTMENT_YOUR -> return DESTRUCTION0_ENCHANTMENT_OTHER
            DESTRUCTION0_ENCHANTMENT_OTHER -> return DESTRUCTION_ENCHANTMENT_YOUR
            DISCARD_CARD_YOUR -> return DISCARD_CARD_OTHER
            USED_CARD_YOUR -> return USED_CARD_OTHER
            DISCARD_CARD_OTHER -> return DISCARD_CARD_YOUR
            USED_CARD_OTHER -> return USED_CARD_YOUR
            MAKE_ATTACK_COMPLETE_YOUR -> return MAKE_ATTACK_COMPLETE_OTHER
            MAKE_ATTACK_COMPLETE_OTHER -> return MAKE_ATTACK_COMPLETE_YOUR
            ATTACK_INFORMATION_YOUR -> return ATTACK_INFORMATION_OTHER
            ATTACK_INFORMATION_OTHER -> return ATTACK_INFORMATION_YOUR
            else -> return DISCARD_CARD_YOUR
        }
    }
}

enum class LocationEnum(var real_number: Int){
    YOUR_AURA(0),
    OTHER_AURA(1),
    YOUR_FLARE(2),
    OTHER_FLARE(3),
    YOUR_LIFE(4),
    OTHER_LIFE(5),
    DUST(6);

    fun Opposite(): LocationEnum{
        when(this){
            YOUR_AURA -> return LocationEnum.OTHER_AURA
            OTHER_AURA -> return LocationEnum.YOUR_AURA
            YOUR_FLARE -> return LocationEnum.OTHER_FLARE
            OTHER_FLARE -> return LocationEnum.YOUR_FLARE
            YOUR_LIFE -> return LocationEnum.OTHER_LIFE
            OTHER_LIFE -> return LocationEnum.YOUR_LIFE
            DUST -> return LocationEnum.DUST
        }
    }
    companion object {
        fun fromInt(value: Int) = MegamiEnum.values().first { it.real_number == value }
    }
}