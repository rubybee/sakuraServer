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
}

enum class TextEffectTag {
    NEXT_ATTACK_ENCHANTMENT,
    CHANGE_CONCENTRATION,
    CHASM,
    MAKE_ATTACK,
    MOVE_SAKURA_TOKEN,
    IMMEDIATE_RETURN,
    RETURN,
    TERMINATION,
    USING_CONDITION,
    DO_NOT_NAP,
    THIS_CARD_NAP_LOCATION_CHANGE,
    OTHER_ENCHANTMENT_ZONE_CARD_NAP_LOCATION_CHANGE,
    CAN_REACTABLE,
    REACT_ATTACK_REDUCE,
    REACT_ATTACK_NO_DAMAGE,
    REACT_ATTACK_INVALID,
    IF_REACT_BUFF,
    DRAW_CARD,
    CARD_TO_COVER,
    ADJUST_NAP,
    COST_BUFF,
    COST_X,
    CHANGE_SWELL_DISTANCE,
    DAMAGE_AURA_REPLACEABLE_HERE,
    MAKE_SHRINK,
    FORBID_MOVE_TOKEN,
    CARD_DISCARD_PLACE_CHANGE,
    MOVE_CARD,
    RECONSTRUCT,
}