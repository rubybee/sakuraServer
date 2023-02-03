package com.sakurageto.protocol

import com.sakurageto.gamelogic.MegamiEnum
enum class CommandEnum {
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
    SET_SHRINK_YOUR,
    SET_SHRINK_OTHER,
    SET_CONCENTRATION_YOUR,
    SET_CONCENTRATION_OTHER,
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
    DESTRUCTION_NOT_NORMAL_ENCHANTMENT_YOUR,
    DESTRUCTION_NOT_NORMAL_ENCHANTMENT_OTHER,

    POP_USED_YOUR,
    POP_USED_OTHER,
    POP_SPECIAL_YOUR,
    POP_SPECIAL_OTHER,
    POP_PLAYING_YOUR,
    POP_PLAYING_OTHER,
    POP_COVER_YOUR,
    POP_COVER_OTHER,
    POP_DISCARD_YOUR,
    POP_DISCARD_OTHER,
    POP_HAND_YOUR,
    POP_HAND_OTHER,
    POP_DECK_YOUR,
    POP_DECK_OTHER,
    POP_ENCHANTMENT_YOUR,
    POP_ENCHANTMENT_OTHER,
    POP_SEAL_YOUR,
    POP_SEAL_OTHER,

    SEAL_YOUR,
    SEAL_OTHER,
    IN_ENCHANTMENT_CARD_YOUR,
    SPECIAL_YOUR,
    ENCHANTMENT_CARD_YOUR,
    DISCARD_CARD_YOUR,
    USED_CARD_YOUR,
    COVER_CARD_YOUR,
    DECK_TOP_YOUR,
    DECK_BELOW_YOUR,
    PLAYING_CARD_YOUR,
    HAND_YOUR,
    IN_ENCHANTMENT_CARD_OTHER,
    SPECIAL_OTHER,
    DISCARD_CARD_OTHER,
    USED_CARD_OTHER,
    DECK_TOP_OTHER,
    DECK_BELOW_OTHER,
    COVER_CARD_OTHER,
    PLAYING_CARD_OTHER,
    ENCHANTMENT_CARD_OTHER,
    HAND_OTHER,

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
    REACT_USE_CARD_HAND,
    REACT_USE_CARD_SPECIAL,
    MOVE_TOKEN,
    USE_CARD_YOUR,
    USE_CARD_OTHER,
    USE_CARD_YOUR_REACTION,
    USE_CARD_OTHER_REACTION,
    CARD_HAND_TO_DECK_BELOW_YOUR,
    CARD_HAND_TO_DECK_BELOW_OTHER,
    CARD_HAND_TO_DECK_UPPER_YOUR,
    CARD_HAND_TO_DECK_UPPER_OTHER,
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
    SHOW_HAND_ALL_YOUR,
    SHOW_HAND_ALL_OTHER,
    SHOW_HAND_YOUR,
    SHOW_HAND_OTHER,
    SHOW_COVER_YOUR,
    SHOW_COVER_OTHER,

    SELECT_CARD_EFFECT,
    SELECT_ONE,
    SELECT_TWO,
    SELECT_NOT,
    SELECT_THREE,
    SELECT_FOUR,

    //from mooembuck
    SELECT_AURA_DAMAGE_PLACE,

    SELECT_CARD_REASON_CARD_EFFECT,
    SELECT_CARD_REASON_INSTALLATION,

    CHANGE_UMBRELLA_YOUR,
    CHANGE_UMBRELLA_OTHER,
    STRATAGEM_SET_YOUR,
    STRATAGEM_SET_OTHER,
    STRATAGEM_GET_YOUR,
    STRATAGEM_GET_OTHER;

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
            COVER_CARD_YOUR -> return COVER_CARD_OTHER
            COVER_CARD_OTHER -> return COVER_CARD_YOUR
            ENCHANTMENT_CARD_YOUR -> return ENCHANTMENT_CARD_OTHER
            ENCHANTMENT_CARD_OTHER -> return ENCHANTMENT_CARD_YOUR
            DISCARD_CARD_YOUR -> return DISCARD_CARD_OTHER
            USED_CARD_YOUR -> return USED_CARD_OTHER
            DISCARD_CARD_OTHER -> return DISCARD_CARD_YOUR
            USED_CARD_OTHER -> return USED_CARD_YOUR
            DECK_TOP_YOUR -> return DECK_TOP_OTHER
            DECK_BELOW_YOUR -> return DECK_BELOW_OTHER
            DECK_TOP_OTHER -> return DECK_TOP_YOUR
            DECK_BELOW_OTHER -> return DECK_BELOW_YOUR
            POP_COVER_YOUR -> return POP_COVER_OTHER
            POP_PLAYING_YOUR -> return POP_PLAYING_OTHER
            POP_DISCARD_YOUR -> return POP_DISCARD_OTHER
            POP_HAND_YOUR -> return POP_HAND_OTHER
            POP_DECK_YOUR -> return POP_DECK_OTHER
            POP_COVER_OTHER -> return POP_COVER_YOUR
            POP_HAND_OTHER -> return POP_HAND_YOUR
            POP_DECK_OTHER -> return POP_DECK_YOUR
            POP_PLAYING_OTHER -> return POP_PLAYING_YOUR
            POP_DISCARD_OTHER -> return POP_DISCARD_YOUR
            MAKE_ATTACK_COMPLETE_YOUR -> return MAKE_ATTACK_COMPLETE_OTHER
            MAKE_ATTACK_COMPLETE_OTHER -> return MAKE_ATTACK_COMPLETE_YOUR
            ATTACK_INFORMATION_YOUR -> return ATTACK_INFORMATION_OTHER
            ATTACK_INFORMATION_OTHER -> return ATTACK_INFORMATION_YOUR
            USE_CARD_YOUR -> return USE_CARD_OTHER
            USE_CARD_OTHER -> return USE_CARD_YOUR
            USE_CARD_YOUR_REACTION -> return USE_CARD_OTHER_REACTION
            USE_CARD_OTHER_REACTION -> return USE_CARD_YOUR_REACTION
            CARD_HAND_TO_DECK_BELOW_YOUR -> return CARD_HAND_TO_DECK_BELOW_OTHER
            CARD_HAND_TO_DECK_BELOW_OTHER -> return CARD_HAND_TO_DECK_BELOW_YOUR
            CARD_HAND_TO_DECK_UPPER_YOUR -> return CARD_HAND_TO_DECK_UPPER_OTHER
            CARD_HAND_TO_DECK_UPPER_OTHER -> return CARD_HAND_TO_DECK_UPPER_YOUR
            ACTION_GO_FORWARD_YOUR -> return ACTION_GO_FORWARD_OTHER
            ACTION_GO_BACKWARD_YOUR -> return ACTION_GO_BACKWARD_OTHER
            ACTION_WIND_AROUND_YOUR -> return ACTION_WIND_AROUND_OTHER
            ACTION_INCUBATE_YOUR -> return ACTION_INCUBATE_OTHER
            ACTION_BREAK_AWAY_YOUR -> return ACTION_BREAK_AWAY_OTHER
            PLAYING_CARD_YOUR -> return PLAYING_CARD_OTHER
            PLAYING_CARD_OTHER -> return PLAYING_CARD_YOUR
            POP_SPECIAL_YOUR -> return POP_SPECIAL_OTHER
            POP_SPECIAL_OTHER -> return POP_SPECIAL_YOUR
            SHOW_HAND_ALL_YOUR -> return SHOW_HAND_ALL_OTHER
            SHOW_HAND_YOUR -> return SHOW_HAND_OTHER
            SHOW_COVER_YOUR -> return SHOW_COVER_OTHER
            POP_USED_YOUR -> return POP_USED_OTHER
            POP_USED_OTHER -> return POP_USED_YOUR
            SPECIAL_YOUR -> return SPECIAL_OTHER
            SPECIAL_OTHER -> return SPECIAL_YOUR
            POP_ENCHANTMENT_YOUR -> return POP_ENCHANTMENT_OTHER
            POP_ENCHANTMENT_OTHER -> return POP_ENCHANTMENT_YOUR
            HAND_YOUR -> return HAND_OTHER
            HAND_OTHER -> return HAND_YOUR
            IN_ENCHANTMENT_CARD_YOUR -> return IN_ENCHANTMENT_CARD_OTHER
            IN_ENCHANTMENT_CARD_OTHER -> return IN_ENCHANTMENT_CARD_YOUR
            SEAL_YOUR -> return SEAL_OTHER
            SEAL_OTHER -> return SEAL_YOUR
            POP_SEAL_YOUR -> return POP_SEAL_OTHER
            POP_SEAL_OTHER -> return POP_SEAL_YOUR
            else -> return TODO()
        }
    }
}

enum class TokenEnum(var real_number: Int){
    SAKURA_TOKEN(0);

    companion object {
        fun fromInt(value: Int) = MegamiEnum.values().first { it.real_number == value }
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
    YOUR_ENCHANTMENT_ZONE_CARD(7),
    OTHER_ENCHANTMENT_ZONE_CARD(8),
    DISTANCE(9),

    //they are all only used to select card move location
    COVER_CARD(10),
    DISCARD(11),
    DECK(12),
    HAND(13),
    YOUR_DECK_TOP(14),
    OTHER_DECK_TOP(15),
    YOUR_DECK_BELOW(16),
    OTHER_DECK_BELOW(17),
    PLAYING_ZONE(18),
    SPECIAL_CARD(19),
    USED_CARD(20),
    ENCHANTMENT_ZONE(21),
    SEAL_ZONE(22);

    fun Opposite(): LocationEnum{
        return when(this){
            YOUR_AURA -> OTHER_AURA
            OTHER_AURA -> YOUR_AURA
            YOUR_FLARE -> OTHER_FLARE
            OTHER_FLARE -> YOUR_FLARE
            YOUR_LIFE -> OTHER_LIFE
            OTHER_LIFE -> YOUR_LIFE
            DUST -> DUST
            YOUR_ENCHANTMENT_ZONE_CARD -> OTHER_ENCHANTMENT_ZONE_CARD
            OTHER_ENCHANTMENT_ZONE_CARD -> YOUR_ENCHANTMENT_ZONE_CARD
            DISTANCE -> DISTANCE
            YOUR_DECK_TOP -> OTHER_DECK_TOP
            YOUR_DECK_BELOW -> OTHER_DECK_BELOW
            else -> DISCARD
        }
    }
    companion object {
        fun fromInt(value: Int) = MegamiEnum.values().first { it.real_number == value }
    }
}