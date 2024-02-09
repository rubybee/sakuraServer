package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus

class Text(
    val timing_tag: TextEffectTimingTag, val tag: TextEffectTag, val effect:
    (suspend (Int, PlayerEnum, GameStatus, MadeAttack?) -> Int?)?
)

enum class TextEffectTimingTag {
    CONSTANT_EFFECT,
    AFTER_DESTRUCTION,
    START_DEPLOYMENT,
    IN_DEPLOYMENT,
    USED,
    USING,
    AFTER_ATTACK,


    IDEA_CONDITION_FLIP,
    IDEA_CONDITION,
    IDEA_PROCESS,
    IDEA_PROCESS_FLIP,
}

enum class TextEffectTag {
    NULL,

    //basic tag that exist in real game
    CHASM,
    RETURN,
    TERMINATION,
    IMMEDIATE_RETURN,

    //Special megami effect
    WHEN_MANEUVER,
    TRANSFORM,
    CHANGE_RAIRA_GAUGE,
    INSTALLATION,
    INSTALLATION_INFINITE,
    RUN_STRATAGEM,
    FREEZE,
    CHANGE_ARROW_BOTH,
    INVESTMENT_RIGHT,
    PERJURE,
    CHANGE_UMBRELLA,

    //just define what this effect do
    ADD_BUFF,
    EFFECT_ACT,
    IDEA,
    DO_BASIC_OPERATION,
    THIS_CARD_NAP_CHANGE,
    ADD_END_PHASE_EFFECT,
    REACT_ATTACK_STATUS_CHANGE,
    MOVE_CARD,
    USE_CARD,
    MAKE_ATTACK,
    MOVE_TOKEN,
    RETURN_OTHER_CARD,
    CHANGE_CONCENTRATION,
    ADD_LISTENER,
    ADD_LOG,
    ADD_TEXT_TO_ATTACK,
    CHANGE_THIS_TURN_DISTANCE,
    CHANGE_THIS_TURN_SWELL_DISTANCE,
    GAME_END,
    END_CURRENT_PHASE,
    STORM_FORCE,
    NEXT_BASIC_OPERATION_INVALID,
    PHASE_SKIP,
    ADD_COST,
    DIVING,
    CAN_NOT_USE_CARD,
    JOURNEY,

    //description when this effect use
    WHEN_USE_BEHAVIOR_END,
    WHEN_SPECIAL_RETURN_YOUR,
    CAN_NOT_USE_CONCENTRATION_OTHER,
    WHEN_USE_FULL_POWER_YOUR_END,
    WHEN_USE_FULL_POWER_YOUR,
    WHEN_THIS_CARD_RETURN,
    WHEN_CHOOSE_AURA_DAMAGE,
    WHEN_TRANSFORM,
    WHEN_DEPLOYMENT_OTHER,
    WHEN_ENCHANTMENT_DESTRUCTION_YOUR,
    WHEN_GET_DAMAGE_BY_ATTACK,
    WHEN_USE_REACT_CARD_YOUR_END,
    WHEN_DO_WIND_AROUND,
    WHEN_LOSE_GAME,
    WHEN_LOSE_GAME_ENCHANTMENT,
    WHEN_CHOOSE_LIFE_DAMAGE,
    WHEN_THIS_CARD_REACTED_AFTER,
    WHEN_THIS_CARD_REACTED,
    WHEN_THIS_CARD_GET_OUT_ENCHANTMENT,
    WHEN_AFTER_CARD_USE,
    WHEN_DECK_RECONSTRUCT_OTHER,
    WHEN_DISCARD_NUMBER_CHANGE_OTHER,
    WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN,
    WHEN_ACT_CHANGE,
    WHEN_TABOO_CHANGE,
    WHEN_THIS_CARD_NAP_REMOVE,
    WHEN_THIS_CARD_WHEN_PERJURE_NOT_DISPROVE,
    WHEN_THIS_CARD_NOT_DISPROVE,
    WHEN_MOVE_TOKEN,
    WHEN_DECK_RECONSTRUCT_YOUR,
    WHEN_DECK_RECONSTRUCT_YOUR_AFTER_INSTALLATION,
    WHEN_RESOLVE_COG_EFFECT,
    WHEN_AFTER_ATTACK_RESOLVE_OTHER_USE_ATTACK_NUMBER,
    WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA,
    WHEN_DRAW_CARD,
    WHEN_GET_DAMAGE_BY_DECK_RECONSTRUCT,
    WHEN_GET_ATTACK,
    WHEN_THIS_CARD_DISPROVE_FAIL,
    WHEN_MAIN_PHASE_RECOUP_YOUR,
    WHEN_THIS_CARD_RELIC_RETURN,
    WHEN_UMBRELLA_CHANGE,
    WHEN_REACT_YOUR,
    WHEN_MAIN_PHASE_END_YOUR,
    RIRARURIRARO_EFFECT,
    AFTER_DEPLOYMENT,
    WHEN_AFTER_CARD_USE_AND_MOVE_DISCARD,

    CONDITION_ADD_DO_WIND_AROUND,

    //description when this effect use this is exist in real game
    WHEN_START_PHASE_YOUR,
    WHEN_START_PHASE_OTHER,
    WHEN_END_PHASE_YOUR,
    WHEN_END_PHASE_OTHER,
    WHEN_MAIN_PHASE_YOUR,
    WHEN_MAIN_PHASE_OTHER,
    WHEN_END_PHASE_YOUR_IN_DISCARD,

    CUSTOM_PART_LV_1,
    CUSTOM_PART_LV_2,
    CUSTOM_PART_LV_3,
    CUSTOM_PART_LV_4,

    //check effect when some process is running
    END_PHASE_ADDITIONAL_CHECK,
    WHEN_END_PHASE_ADDITIONAL_CHECK,
    USING_CONDITION,
    CAN_USE_REACT,
    CAN_USE_COVER,
    COST_CHECK,
    COST,
    FORBID_GO_BACKWARD_OTHER,
    FORBID_BREAK_AWAY_OTHER,
    FORBID_GO_FORWARD_YOUR,
    FORBID_BREAK_AWAY_YOUR,
    FORBID_INCUBATE_OTHER,
    FORBID_MOVE_TOKEN_USING_ARROW, //return FromLocationEnum * 100 + ToLocationEnum (if anywhere it will be 99)
    FORBID_BASIC_OPERATION_YOUR,
    FORBID_GET_AURA_OTHER,
    FORBID_GET_AURA_OTHER_AFTER,
    AFTER_AURA_DAMAGE_PLACE_CHANGE,
    AFTER_LIFE_DAMAGE_PLACE_CHANGE,
    AFTER_OTHER_ATTACK_COMPLETE,
    AFTER_ATTACK_EFFECT_INVALID_OTHER,
    AFTER_HATSUMI_LIGHTHOUSE,
    AFTER_DESTRUCTION_EFFECT_INVALID_OTHER,
    NEXT_ATTACK_ENCHANTMENT,
    NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK,
    NEXT_ATTACK_ENCHANTMENT_OTHER,
    CAN_NOT_CHOOSE_AURA_DAMAGE,
    CAN_NOT_WIN,
    CAN_NOT_LOSE,
    CAN_NOT_GET_DAMAGE,
    CAN_NOT_MOVE_TOKEN,
    CAN_NOT_USE_ATTACK,
    CHECK_THIS_ATTACK_VALUE,
    OTHER_CARD_NAP_LOCATION_HERE,
    THIS_CARD_NAP_LOCATION_CHANGE,
    ADJUST_NAP,
    ADJUST_NAP_CONTAIN_OTHER_PLACE,
    COST_BUFF,
    COST_X,
    CHANGE_SWELL_DISTANCE,
    DAMAGE_AURA_REPLACEABLE_HERE,
    EFFECT_INSTEAD_DAMAGE,
    CHANGE_DISTANCE,
    ACTIVE_TRANSFORM_BELOW_THIS_CARD,
    SELECT_DAMAGE_BY_ATTACKER,
    CHOJO_DAMAGE_CHANGE_OTHER,
    WHEN_AFTER_CARD_USE_AND_MOVE_DISCARD_CONDITION,

    TREAT_AS_DIFFERENT_CARD,
    HATSUMI_LIGHTHOUSE,
    ADD_GROWING,
    REMOVE_TERMINATION_REACTION_USE_IN_SOLDIER,
    MIZUKI_BATTLE_CRY,
    TOKOYO_EIGHT_SAKURA,
    KAMUWI_LOGIC;
}