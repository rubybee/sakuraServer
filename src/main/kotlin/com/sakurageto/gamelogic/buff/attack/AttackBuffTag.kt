package com.sakurageto.gamelogic.buff.attack

enum class AttackBuffTag {
    CARD_CHANGE,
    INSERT,
    CHANGE_EACH,
    MULTIPLE,
    DIVIDE,
    PLUS_MINUS,
    CARD_CHANGE_IMMEDIATE,
    INSERT_IMMEDIATE,
    CHANGE_EACH_IMMEDIATE,
    MULTIPLE_IMMEDIATE,
    DIVIDE_IMMEDIATE,
    PLUS_MINUS_IMMEDIATE,
    /***
     * this tag is needed to not affect buff at now attack(different with not immediate buff)
     * but not remove when unused(different with immediate buff)
     */
    PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED
}