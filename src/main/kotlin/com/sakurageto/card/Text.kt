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
    //basic tag that exist in real game
    CHASM,
    RETURN,
    TERMINATION,
    IMMEDIATE_RETURN,

    //Special megami effect
    WHEN_MANEUVER,
    TRANSFORM,
    MANEUVER,
    CHANGE_RAIRA_GAUGE,
    INSTALLATION,
    INSTALLATION_INFINITE,
    RUN_STRATAGEM,
    FREEZE,
    CHANGE_ARROW_BOTH,

    //just define what this effect do
    EFFECT_ACT,
    IDEA,
    DO_BASIC_OPERATION,
    THIS_CARD_NAP_CHANGE,
    ADD_END_PHASE_EFFECT,
    GET_ADDITIONAL_CARD,
    DAMAGE,
    RECONSTRUCT,
    REACT_ATTACK_CHANGE,
    REACT_ATTACK_NO_DAMAGE,
    REACT_ATTACK_INVALID,
    DRAW_CARD,
    USE_CARD,
    MOVE_CARD,
    CHANGE_CONCENTRATION,
    MAKE_ATTACK,
    MOVE_SAKURA_TOKEN,
    RETURN_OTHER_CARD,
    MAKE_SHRINK,
    ADD_LISTENER,
    SEAL_CARD,
    CARD_DISCARD_PLACE_CHANGE,
    ADD_LOG,
    ADD_TEXT_TO_ATTACK,
    INSERT_POISON,
    CHANGE_THIS_TURN_DISTANCE,
    CHANGE_THIS_TURN_SWELL_DISTANCE,
    GAME_END,
    END_CURRENT_PHASE,
    STORM_FORCE,
    NEXT_BASIC_OPERATION_INVALID,
    PHASE_SKIP,

    //description when this effect use
    WHEN_USE_BEHAVIOR_END,
    WHEN_SPECIAL_RETURN_YOUR,
    WHEN_FULL_POWER_USED_YOUR,
    WHEN_THIS_CARD_RETURN,
    WHEN_CHOOSE_AURA_DAMAGE,
    WHEN_TRANSFORM,
    WHEN_DEPLOYMENT_OTHER,
    WHEN_ENCHANTMENT_DESTRUCTION_YOUR,
    WHEN_GET_DAMAGE_BY_ATTACK,
    WHEN_USE_REACT_CARD_YOUR_END,
    WHEN_DO_WIND_AROUND,
    WHEN_LOSE_GAME,
    WHEN_CHOOSE_LIFE_DAMAGE,
    WHEN_THIS_CARD_REACTED,
    WHEN_THIS_CARD_GET_OUT_ENCHANTMENT,
    WHEN_AFTER_CARD_USE,
    WHEN_DECK_RECONSTRUCT_OTHER,
    WHEN_DISCARD_NUMBER_CHANGE_OTHER,
    WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN,
    WHEN_ACT_CHANGE,

    CONDITION_ADD_DO_WIND_AROUND,

    //description when this effect use this is exist in real game
    WHEN_START_PHASE_YOUR,
    WHEN_START_PHASE_OTHER,
    WHEN_END_PHASE_YOUR,
    WHEN_END_PHASE_OTHER,
    WHEN_MAIN_PHASE_YOUR,
    WHEN_MAIN_PHASE_OTHER,
    WHEN_END_PHASE_YOUR_IN_DISCARD,

    //check effect when some process is running
    OTHER_CARD_NAP_LOCATION_HERE,
    DO_NOT_GET_DAMAGE,
    DO_NOT_MOVE_TOKEN,
    FORBID_BASIC_OPERATION,
    NEXT_ATTACK_ENCHANTMENT,
    NEXT_ATTACK_ENCHANTMENT_OTHER,
    USING_CONDITION,
    DO_NOT_NAP,
    THIS_CARD_NAP_LOCATION_CHANGE,
    CAN_REACTABLE,
    ADJUST_NAP,
    COST_BUFF,
    COST_X,
    CHANGE_SWELL_DISTANCE,
    DAMAGE_AURA_REPLACEABLE_HERE,
    FORBID_MOVE_TOKEN_USING_ARROW,
    SHOW_HAND_WHEN_CHANGE_UMBRELLA,
    EFFECT_INSTEAD_DAMAGE,
    CHANGE_DISTANCE,
    FORBID_GO_BACKWARD_OTHER,
    FORBID_BREAK_AWAY_OTHER,
    FORBID_INCUBATE_OTHER,
    CAN_NOT_USE_ATTACK,
    CAN_USE_COVER,
    COST_CHECK,
    COST,
    AFTER_AURA_DAMAGE_PLACE_CHANGE,
    AFTER_LIFE_DAMAGE_PLACE_CHANGE,
    AFTER_OTHER_ATTACK_COMPLETE,
    AFTER_ATTACK_EFFECT_INVALID_OTHER,
    AFTER_HATSUMI_LIGHTHOUSE,
    AFTER_DESTRUCTION_EFFECT_INVALID_OTHER,
    ACTIVE_TRANSFORM_BELOW_THIS_CARD,
    SELECT_DAMAGE_BY_ATTACKER,

    HATSUMI_LIGHTHOUSE,
    ADD_GROWING,
    REMOVE_REACTIONS_TERMINATION,
    MIZUKI_BATTLE_CRY,
    TOKOYO_EIGHT_SAKURA;
}