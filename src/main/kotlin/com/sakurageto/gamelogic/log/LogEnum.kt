package com.sakurageto.gamelogic.log

enum class LogEnum{
    MOVE_TOKEN,
    END_EFFECT,
    IDEA,
    ATTACK,
    ATTACK_DAMAGE,
    TRANSFORM,
    USE_CARD_IN_COVER_AND_REACT,
    USE_CARD_IN_COVER,
    USE_CARD_IN_SOLDIER,
    USE_CARD_IN_SOLDIER_PERJURE,
    USE_CARD,
    USE_CARD_PERJURE,
    USE_CENTRIFUGAL,
    GET_LIFE_DAMAGE,
    GET_AURA_DAMAGE,
    DAMAGE_PROCESS_START,
    START_PROCESS_ATTACK_DAMAGE,
    FAIL_DISPROVE,
    GET_FLARE_DAMAGE,
    START_PHASE,
    MAIN_PHASE,
    END_PHASE;

    fun isPhaseLog(): Boolean{
        return this == START_PHASE || this == MAIN_PHASE || this == END_PHASE
    }
}