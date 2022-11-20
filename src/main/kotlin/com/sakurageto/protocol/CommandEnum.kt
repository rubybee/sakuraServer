package com.sakurageto.protocol

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
    REDUCE_NAP_SELF,
    REDUCE_NAP_OTHER,
    REDUCE_NAP_END,
    SELECT_ENCHANTMENT_START,
    SELECT_ENCHANTMENT_YOUR,
    SELECT_ENCHANTMENT_OTHER,
    SELECT_ENCHANTMENT_END,
}