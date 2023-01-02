package com.sakurageto.protocol

import com.sakurageto.gamelogic.MegamiEnum
enum class CommandEnum() {
    NULL,
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
    SELECT_CARD_OTHER_PLAYERS,
    END_SELECT_CARD,
    RETURN_SPECIAL_CARD,
    FIRST_TURN,
    SECOND_TURN,
    MULIGUN,
    MULIGUN_END,
    ADD_CONCENTRATION_YOUR,
    ADD_CONCENTRATION_OTHER,
    DECREASE_CONCENTRATION_YOUR,
    DECREASE_CONCENTRATION_OTHER,
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
    DESTRUCTION_ENCHANTMENT_OTHER,
    DESTRUCTION_NOT_NORMALY_ENCHANTENT_YOUR,
    DESTRUCTION_NOT_NORMALY_ENCHANTMENT_OTHER,
    ENCHANTMENT_CARD_YOUR,
    ENCHANTMENT_CARD_OTHER,
    DISCARD_CARD_YOUR,
    USED_CARD_YOUR,
    COVER_CARD_YOUR,
    DISCARD_CARD_OTHER,
    USED_CARD_OTHER,
    COVER_CARD_OTHER,
    MAKE_ATTACK_COMPLETE_YOUR,
    MAKE_ATTACK_COMPLETE_OTHER,
    ATTACK_INFORMATION_YOUR,
    ATTACK_INFORMATION_OTHER,
    REACT_REQUEST,
    REACT_NO,
    CHOOSE_DAMAGE,
    CHOOSE_CHOJO,
    CHOOSE_CARD_DAMAGE,
    CHOOSE_AURA,
    CHOOSE_LIFE,
    REACT_USE_CARD_IN_HAND,
    REACT_USE_CARD_IN_SPEICAL,
    MOVE_TOKEN,
    USE_CARD_YOUR,
    USE_CARD_OTHER,
    USE_CARD_YOUR_REACTION,
    USE_CARD_OTHER_REACTION,
    CARD_HAND_TO_COVER_YOUR,
    CARD_HAND_TO_COVER_OTHER,
    CARD_HAND_TO_DECK_YOUR,
    CARD_HAND_TO_DECK_OTHER,
    DRAW_CARD_YOUR,
    DRAW_CARD_OTHER,
    SELECT_NAP,
    START_START_PHASE_YOUR,
    START_START_PHASE_OTHER,
    START_MAIN_PHASE_YOUR,
    START_MAIN_PHASE_OTHER,
    START_END_PHASE_YOUR,
    START_END_PHASE_OTHER,
    DECK_RECONSTRUCT_YOUR,
    DECK_RECONSTRUCT_OTHER,
    DECK_RECONSTRUCT_REQUEST,
    DECK_RECONSTRUCT_YES,
    DECK_RECONSTRUCT_NO,
    FULL_POWER_REQUEST,
    FULL_POWER_YES,
    FULL_POWER_NO,
    ACTION_REQUEST,
    ACTION_USE_CARD_HAND,
    ACTION_USE_CARD_SPECIAL,
    ACTION_END_TURN,
    ACTION_GO_FORWARD,
    ACTION_GO_BACKWARD,
    ACTION_WIND_AROUND,
    ACTION_INCUBATE,
    ACTION_BREAK_AWAY,
    ACTION_GO_FORWARD_YOUR,
    ACTION_GO_BACKWARD_YOUR,
    ACTION_WIND_AROUND_YOUR,
    ACTION_INCUBATE_YOUR,
    ACTION_BREAK_AWAY_YOUR,
    ACTION_GO_FORWARD_OTHER,
    ACTION_GO_BACKWARD_OTHER,
    ACTION_WIND_AROUND_OTHER,
    ACTION_INCUBATE_OTHER,
    ACTION_BREAK_AWAY_OTHER,
    GAME_END_WINNER,
    GAME_END_LOSER,
    COVER_CARD_SELECT,
    ;

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
            DESTRUCTION_ENCHANTMENT_YOUR -> return DESTRUCTION_ENCHANTMENT_OTHER
            DESTRUCTION_ENCHANTMENT_OTHER -> return DESTRUCTION_ENCHANTMENT_YOUR
            DISCARD_CARD_YOUR -> return DISCARD_CARD_OTHER
            USED_CARD_YOUR -> return USED_CARD_OTHER
            DISCARD_CARD_OTHER -> return DISCARD_CARD_YOUR
            USED_CARD_OTHER -> return USED_CARD_YOUR
            MAKE_ATTACK_COMPLETE_YOUR -> return MAKE_ATTACK_COMPLETE_OTHER
            MAKE_ATTACK_COMPLETE_OTHER -> return MAKE_ATTACK_COMPLETE_YOUR
            ATTACK_INFORMATION_YOUR -> return ATTACK_INFORMATION_OTHER
            ATTACK_INFORMATION_OTHER -> return ATTACK_INFORMATION_YOUR
            USE_CARD_YOUR -> return USE_CARD_OTHER
            USE_CARD_OTHER -> return USE_CARD_YOUR
            USE_CARD_YOUR_REACTION -> return USE_CARD_OTHER_REACTION
            USE_CARD_OTHER_REACTION -> return USE_CARD_YOUR_REACTION
            CARD_HAND_TO_DECK_YOUR -> return CARD_HAND_TO_DECK_OTHER
            CARD_HAND_TO_DECK_OTHER -> return CARD_HAND_TO_DECK_YOUR
            ACTION_GO_FORWARD_YOUR -> return ACTION_GO_FORWARD_OTHER
            ACTION_GO_BACKWARD_YOUR -> return ACTION_GO_BACKWARD_OTHER
            ACTION_WIND_AROUND_YOUR -> return ACTION_WIND_AROUND_OTHER
            ACTION_INCUBATE_YOUR -> return ACTION_INCUBATE_OTHER
            ACTION_BREAK_AWAY_YOUR -> return ACTION_BREAK_AWAY_OTHER
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
    DUST(6),
    YOUR_CARD(7),
    OTHER_CARD(8),
    DISTANCE(9);

    fun Opposite(): LocationEnum{
        when(this){
            YOUR_AURA -> return OTHER_AURA
            OTHER_AURA -> return YOUR_AURA
            YOUR_FLARE -> return OTHER_FLARE
            OTHER_FLARE -> return YOUR_FLARE
            YOUR_LIFE -> return OTHER_LIFE
            OTHER_LIFE -> return YOUR_LIFE
            DUST -> return DUST
            YOUR_CARD -> return OTHER_CARD
            OTHER_CARD -> return YOUR_CARD
            DISTANCE -> return DISTANCE
        }
    }
    companion object {
        fun fromInt(value: Int) = MegamiEnum.values().first { it.real_number == value }
    }
}