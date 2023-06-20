package com.sakurageto.protocol

enum class CommandEnum {
    ACK,
    NULL,
    SELECT_MODE_YOUR,
    SELECT_MODE_OTHER,
    END_OF_SELECTMODE,
    SELECT_MEGAMI,
    CHECK_MEGAMI,
    SELECT_BAN,
    CHECK_YOUR,
    CHECK_ANOTHER,
    SELECT_CARD,
    END_SELECT_CARD,
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
    REDUCE_NAP_END,
    SELECT_ENCHANTMENT_START,
    SELECT_ENCHANTMENT_YOUR,
    SELECT_ENCHANTMENT_OTHER,
    SELECT_ENCHANTMENT_END,
    DESTRUCTION_ENCHANTMENT_YOUR,
    DESTRUCTION_ENCHANTMENT_OTHER,
    NOT_READY_SOLDIER_ZONE_YOUR,
    NOT_READY_SOLDIER_ZONE_OTHER,
    READY_SOLDIER_ZONE_YOUR,
    READY_SOLDIER_ZONE_OTHER,

    POP_POISON_BAG_YOUR,
    POP_POISON_BAG_OTHER,
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
    POP_ADDITIONAL_YOUR,
    POP_ADDITIONAL_OTHER,
    POP_NOT_READY_SOLDIER_ZONE_YOUR,
    POP_NOT_READY_SOLDIER_ZONE_OTHER,
    POP_READY_SOLDIER_ZONE_YOUR,
    POP_READY_SOLDIER_ZONE_OTHER,
    POP_IDEA_YOUR,
    POP_IDEA_OTHER,
    POP_END_IDEA_YOUR,
    POP_END_IDEA_OTHER,

    POISON_BAG_YOUR,
    POISON_BAG_OTHER,
    SEAL_YOUR,
    SEAL_OTHER,
    SPECIAL_YOUR,
    ENCHANTMENT_CARD_YOUR,
    DISCARD_CARD_YOUR,
    USED_CARD_YOUR,
    COVER_CARD_YOUR,
    DECK_TOP_YOUR,
    DECK_BELOW_YOUR,
    PLAYING_CARD_YOUR,
    HAND_YOUR,
    OUT_OF_GAME_YOUR,
    TRANSFORM_YOUR,
    ADDITIONAL_YOUR,
    IDEA_YOUR,
    END_IDEA_YOUR,
    SPECIAL_OTHER,
    DISCARD_CARD_OTHER,
    USED_CARD_OTHER,
    DECK_TOP_OTHER,
    DECK_BELOW_OTHER,
    COVER_CARD_OTHER,
    PLAYING_CARD_OTHER,
    ENCHANTMENT_CARD_OTHER,
    HAND_OTHER,
    OUT_OF_GAME_OTHER,
    TRANSFORM_OTHER,
    ADDITIONAL_OTHER,
    IDEA_OTHER,
    END_IDEA_OTHER,


    MAKE_ATTACK_COMPLETE_YOUR,
    MAKE_ATTACK_COMPLETE_OTHER,
    ATTACK_INFORMATION_YOUR,
    ATTACK_INFORMATION_OTHER,
    REACT_REQUEST,
    REACT_NO,
    CHOOSE_DAMAGE,
    CHOOSE_CARD_DAMAGE_OTHER,
    CHOOSE_CHOJO,
    CHOOSE_CARD_DAMAGE,
    CHOOSE_AURA,
    CHOOSE_LIFE,
    REACT_USE_CARD_HAND,
    REACT_USE_CARD_SPECIAL,
    REACT_USE_CARD_SOLDIER,
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
    SELECT_SPROUT,
    SELECT_GROWING,
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
    ACTION_USE_CARD_COVER,
    ACTION_USE_CARD_SOLDIER,
    ACTION_END_TURN,
    ACTION_GO_FORWARD,
    ACTION_GO_BACKWARD,
    ACTION_WIND_AROUND,
    ACTION_INCUBATE,
    ACTION_BREAK_AWAY,
    ACTION_YAKSHA,
    ACTION_NAGA,
    ACTION_GARUDA,
    ACTION_ASURA,
    ACTION_GO_FORWARD_YOUR,
    ACTION_GO_BACKWARD_YOUR,
    ACTION_WIND_AROUND_YOUR,
    ACTION_INCUBATE_YOUR,
    ACTION_BREAK_AWAY_YOUR,
    ACTION_YAKSHA_YOUR,
    ACTION_NAGA_YOUR,
    ACTION_GARUDA_YOUR,
    ACTION_ASURA_YOUR,
    ACTION_GO_FORWARD_OTHER,
    ACTION_GO_BACKWARD_OTHER,
    ACTION_WIND_AROUND_OTHER,
    ACTION_INCUBATE_OTHER,
    ACTION_BREAK_AWAY_OTHER,
    ACTION_YAKSHA_OTHER,
    ACTION_NAGA_OTHER,
    ACTION_GARUDA_OTHER,
    ACTION_ASURA_OTHER,
    GAME_END_WINNER,
    GAME_END_LOSER,
    COVER_CARD_SELECT,
    SHOW_HAND_ALL_YOUR,
    SHOW_HAND_ALL_OTHER,
    SHOW_HAND_YOUR,
    SHOW_HAND_OTHER,
    SHOW_COVER_YOUR,
    SHOW_COVER_OTHER,
    SHOW_DECK_TOP_YOUR,
    SHOW_DECK_TOP_OTHER,

    SELECT_CARD_EFFECT,
    SELECT_NOT,
    SELECT_ONE,
    SELECT_TWO,
    SELECT_THREE,
    SELECT_FOUR,
    SELECT_FIVE,
    SELECT_SIX,
    SELECT_SEVEN,
    SELECT_EIGHT,
    SELECT_NINE,
    SELECT_TEN,

    //from mooembuck
    SELECT_AURA_DAMAGE_PLACE,

    SELECT_CARD_REASON_CARD_EFFECT,
    SELECT_CARD_REASON_INSTALLATION,
    SELECT_AFTER_CARD_USED_EFFECT_ORDER,
    SELECT_END_PHASE_EFFECT_ORDER,
    SELECT_START_PHASE_EFFECT_ORDER,

    CHANGE_UMBRELLA_YOUR,
    CHANGE_UMBRELLA_OTHER,
    STRATAGEM_SET_YOUR,
    STRATAGEM_SET_OTHER,
    STRATAGEM_GET_YOUR,
    STRATAGEM_GET_OTHER,

    REQUEST_BASIC_OPERATION,
    GET_DAMAGE_AURA_YOUR,
    GET_DAMAGE_LIFE_YOUR,
    GET_DAMAGE_AURA_OTHER,
    GET_DAMAGE_LIFE_OTHER,

    //for raira
    INCREASE_THUNDER_GAUGE_YOUR,
    INCREASE_THUNDER_GAUGE_OTHER,
    INCREASE_WIND_GAUGE_YOUR,
    INCREASE_WIND_GAUGE_OTHER,
    SET_THUNDER_GAUGE_YOUR,
    SET_WIND_GAUGE_YOUR,
    SET_THUNDER_GAUGE_OTHER,
    SET_WIND_GAUGE_OTHER,
    SELECT_WIND_ONE,
    SELECT_WIND_TWO,
    SELECT_WIND_THREE,
    SELECT_THUNDER_ONE,
    SELECT_THUNDER_TWO,
    SELECT_THUNDER_THREE,
    //for raira

    SET_TAIL_WIND_YOUR,
    SET_TAIL_WIND_OTHER,
    SET_HEAD_WIND_YOUR,
    SET_HEAD_WIND_OTHER,

    SET_IDEA_STAGE_YOUR,
    SET_IDEA_STAGE_OTHER,
    SET_IDEA_FLIP_YOUR,
    SET_IDEA_FLIP_OTHER,

    SET_ACT_YOUR,
    SET_ACT_OTHER,

    REDUCE_THIS_TURN_DISTANCE,
    ADD_THIS_TURN_DISTANCE,
    REDUCE_THIS_TURN_SWELL_DISTANCE,
    ADD_THIS_TURN_SWELL_DISTANCE,

    SELECT_ARROW_DIRECTION,
    SHOW_SPECIAL_YOUR,
    SHOW_SPECIAL_OTHER,
    SELECT_ACT,

    SELECT_NAP_LOCATION;

    fun Opposite(): CommandEnum{
        when(this){
            ADD_CONCENTRATION_YOUR -> return ADD_CONCENTRATION_OTHER
            ADD_CONCENTRATION_OTHER -> return ADD_CONCENTRATION_YOUR
            REMOVE_SHRINK_YOUR -> return REMOVE_SHRINK_OTHER
            REMOVE_SHRINK_OTHER -> return REMOVE_SHRINK_YOUR
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
            SEAL_YOUR -> return SEAL_OTHER
            SEAL_OTHER -> return SEAL_YOUR
            POP_SEAL_YOUR -> return POP_SEAL_OTHER
            POP_SEAL_OTHER -> return POP_SEAL_YOUR
            POP_POISON_BAG_YOUR -> return POP_POISON_BAG_OTHER
            POP_POISON_BAG_OTHER -> return POP_POISON_BAG_YOUR
            POISON_BAG_YOUR -> return POISON_BAG_OTHER
            POISON_BAG_OTHER -> return POISON_BAG_YOUR
            POP_ADDITIONAL_YOUR -> return POP_ADDITIONAL_OTHER
            POP_ADDITIONAL_OTHER -> return POP_ADDITIONAL_YOUR
            GET_DAMAGE_AURA_YOUR -> return GET_DAMAGE_AURA_OTHER
            GET_DAMAGE_LIFE_YOUR -> return GET_DAMAGE_LIFE_OTHER
            GET_DAMAGE_AURA_OTHER -> return GET_DAMAGE_AURA_YOUR
            GET_DAMAGE_LIFE_OTHER -> return GET_DAMAGE_LIFE_YOUR
            OUT_OF_GAME_OTHER -> return OUT_OF_GAME_YOUR
            OUT_OF_GAME_YOUR -> return OUT_OF_GAME_OTHER
            TRANSFORM_OTHER -> return TRANSFORM_YOUR
            TRANSFORM_YOUR -> return TRANSFORM_OTHER
            ACTION_YAKSHA_YOUR -> return ACTION_YAKSHA_OTHER
            ACTION_NAGA_YOUR -> return ACTION_NAGA_OTHER
            ACTION_GARUDA_YOUR -> return ACTION_GARUDA_OTHER
            ACTION_ASURA_YOUR -> return ACTION_ASURA_OTHER
            ACTION_YAKSHA_OTHER -> return ACTION_YAKSHA_YOUR
            ACTION_NAGA_OTHER -> return ACTION_NAGA_YOUR
            ACTION_ASURA_OTHER -> return ACTION_ASURA_YOUR
            ACTION_GARUDA_OTHER -> return ACTION_GARUDA_YOUR
            SET_THUNDER_GAUGE_YOUR -> return SET_THUNDER_GAUGE_OTHER
            SET_WIND_GAUGE_YOUR -> return SET_WIND_GAUGE_OTHER
            SET_THUNDER_GAUGE_OTHER -> return SET_THUNDER_GAUGE_YOUR
            SET_WIND_GAUGE_OTHER -> return SET_WIND_GAUGE_YOUR
            INCREASE_THUNDER_GAUGE_YOUR -> return INCREASE_THUNDER_GAUGE_OTHER
            INCREASE_THUNDER_GAUGE_OTHER -> return INCREASE_THUNDER_GAUGE_YOUR
            INCREASE_WIND_GAUGE_YOUR -> return INCREASE_WIND_GAUGE_OTHER
            INCREASE_WIND_GAUGE_OTHER -> return INCREASE_WIND_GAUGE_YOUR
            ADDITIONAL_YOUR -> return ADDITIONAL_OTHER
            SHOW_SPECIAL_YOUR -> return SHOW_SPECIAL_OTHER
            SHOW_SPECIAL_OTHER -> return SHOW_SPECIAL_YOUR
            SET_TAIL_WIND_YOUR -> return SET_TAIL_WIND_OTHER
            SET_TAIL_WIND_OTHER -> return SET_TAIL_WIND_YOUR
            SET_HEAD_WIND_YOUR -> return SET_HEAD_WIND_OTHER
            SET_HEAD_WIND_OTHER -> return SET_HEAD_WIND_YOUR
            SHOW_DECK_TOP_YOUR -> return SHOW_DECK_TOP_OTHER
            SHOW_DECK_TOP_OTHER -> return SHOW_DECK_TOP_YOUR
            NOT_READY_SOLDIER_ZONE_YOUR -> return NOT_READY_SOLDIER_ZONE_OTHER
            NOT_READY_SOLDIER_ZONE_OTHER -> return NOT_READY_SOLDIER_ZONE_YOUR
            READY_SOLDIER_ZONE_YOUR -> return READY_SOLDIER_ZONE_OTHER
            READY_SOLDIER_ZONE_OTHER -> return READY_SOLDIER_ZONE_YOUR
            POP_NOT_READY_SOLDIER_ZONE_YOUR -> return POP_NOT_READY_SOLDIER_ZONE_OTHER
            POP_NOT_READY_SOLDIER_ZONE_OTHER -> return POP_NOT_READY_SOLDIER_ZONE_YOUR
            POP_READY_SOLDIER_ZONE_YOUR -> return POP_READY_SOLDIER_ZONE_OTHER
            POP_READY_SOLDIER_ZONE_OTHER -> return POP_READY_SOLDIER_ZONE_YOUR
            SET_IDEA_STAGE_YOUR -> return SET_IDEA_STAGE_OTHER
            SET_IDEA_STAGE_OTHER -> return SET_IDEA_STAGE_YOUR
            IDEA_YOUR -> return IDEA_OTHER
            IDEA_OTHER -> return IDEA_YOUR
            END_IDEA_YOUR -> return END_IDEA_OTHER
            END_IDEA_OTHER -> return END_IDEA_YOUR
            POP_IDEA_YOUR -> return POP_IDEA_OTHER
            POP_IDEA_OTHER -> return POP_IDEA_YOUR
            POP_END_IDEA_YOUR -> return POP_END_IDEA_OTHER
            POP_END_IDEA_OTHER -> return POP_END_IDEA_YOUR
            SET_IDEA_FLIP_YOUR -> return SET_IDEA_FLIP_OTHER
            SET_IDEA_FLIP_OTHER -> return SET_IDEA_FLIP_YOUR
            SET_ACT_YOUR -> return SET_ACT_OTHER
            SET_ACT_OTHER -> return SET_ACT_YOUR
            else -> TODO()
        }
    }
}

enum class TokenEnum(var real_number: Int){
    SAKURA_TOKEN(0),
    YOUR_ARTIFICIAL_SAKURA_TOKEN(1),
    YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN(2),
    YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN(3),
    OTHER_ARTIFICIAL_SAKURA_TOKEN(4),
    OTHER_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN(5),
    OTHER_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN(6),
    FREEZE_TOKEN(7),
    SEED_TOKEN(8);

    fun opposite(): TokenEnum{
        return when(this){
            SAKURA_TOKEN -> SAKURA_TOKEN
            YOUR_ARTIFICIAL_SAKURA_TOKEN -> OTHER_ARTIFICIAL_SAKURA_TOKEN
            YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN -> OTHER_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN
            YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN -> OTHER_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN
            OTHER_ARTIFICIAL_SAKURA_TOKEN -> YOUR_ARTIFICIAL_SAKURA_TOKEN
            OTHER_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN -> YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN
            OTHER_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN -> YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN
            FREEZE_TOKEN -> FREEZE_TOKEN
            SEED_TOKEN -> SEED_TOKEN
        }
    }

    companion object {
        fun fromInt(value: Int) = TokenEnum.values().first { it.real_number == value }
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
    MACHINE_YOUR(26),
    MACHINE_OTHER(27),
    MACHINE_BURN_YOUR(30),
    MACHINE_BURN_OTHER(31),


    //they are all only used to select card move location
    COVER_CARD(10),
    DISCARD_YOUR(11),
    DECK(12),
    HAND(13),
    HAND_OTHER(29),
    YOUR_DECK_TOP(14),
    OTHER_DECK_TOP(15),
    YOUR_DECK_BELOW(16),
    OTHER_DECK_BELOW(17),
    PLAYING_ZONE_YOUR(18),
    SPECIAL_CARD(19),
    YOUR_USED_CARD(20),
    OTHER_USED_CARD(32),
    ENCHANTMENT_ZONE(21),
    SEAL_ZONE(22),
    POISON_BAG(23),
    ADDITIONAL_CARD(24),
    OUT_OF_GAME(25),
    TRANSFORM(28),
    DISCARD_OTHER(32),

    ALL(33),
    PLAYING_ZONE_OTHER(34),
    READY_SOLDIER_ZONE(35),
    NOT_READY_SOLDIER_ZONE(36),

    READY_DIRT_ZONE_YOUR(37),
    READY_DIRT_ZONE_OTHER(38),
    NOT_READY_DIRT_ZONE_YOUR(39),
    NOT_READY_DIRT_ZONE_OTHER(40),

    IDEA_YOUR(41),
    IDEA_OTHER(42),
    END_IDEA_YOUR(43),
    END_IDEA_OTHER(44),

    NOT_SELECTED_NORMAL(45),
    NOT_SELECTED_SPECIAL(46);

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
            MACHINE_YOUR -> MACHINE_OTHER
            MACHINE_BURN_YOUR -> MACHINE_BURN_OTHER
            OUT_OF_GAME -> OUT_OF_GAME
            YOUR_USED_CARD -> OTHER_USED_CARD
            OTHER_USED_CARD -> YOUR_USED_CARD
            PLAYING_ZONE_YOUR -> PLAYING_ZONE_OTHER
            PLAYING_ZONE_OTHER -> PLAYING_ZONE_YOUR
            READY_DIRT_ZONE_YOUR -> READY_DIRT_ZONE_OTHER
            READY_DIRT_ZONE_OTHER -> READY_DIRT_ZONE_YOUR
            NOT_READY_DIRT_ZONE_YOUR -> NOT_READY_DIRT_ZONE_OTHER
            NOT_READY_DIRT_ZONE_OTHER -> NOT_READY_DIRT_ZONE_YOUR
            IDEA_YOUR -> IDEA_OTHER
            IDEA_OTHER -> IDEA_YOUR
            END_IDEA_YOUR -> END_IDEA_OTHER
            END_IDEA_OTHER -> END_IDEA_YOUR
            else -> DISCARD_YOUR
        }
    }
    companion object {
        fun fromInt(value: Int) = LocationEnum.values().first { it.real_number == value }
    }
}

//SELECT_ONE MEANS I SELECT AURA TO OUT
enum class LocToLoc(var real_number: Int){
    AURA_YOUR_TO_OUT(0),
    FLARE_YOUR_TO_OUT(1),
    AURA_OTHER_TO_AURA_YOUR(2),
    AURA_YOUR_TO_DISTANCE(3),
    AURA_YOUR_TO_FLARE_OTHER(4),
    DISTANCE_TO_FLARE_YOUR(5),
    AURA_OTHER_TO_DISTANCE(6),
    DISTANCE_TO_DUST(7),
    DUST_TO_LIFE_YOUR(8),
    YOUR_LIFE_TO_YOUR_FLARE(9),
    LIFE_YOUR_TO_DISTANCE(10),
    DUST_TO_AURA_YOUR(11),
    DUST_TO_FLARE_YOUR(12),
    DUST_TO_LIFE_OTHER(13),
    DUST_TO_FLARE_OTHER(14),
    DUST_TO_AURA_OTHER(15),
    DISTANCE_TO_FLARE_OTHER(16),
    AURA_OTHER_TO_OUT(17),
    FLARE_OTHER_TO_OUT(18),
    AURA_OTHER_TO_FLARE_YOUR(19),
    AURA_YOUR_TO_FLARE_YOUR(20),
    AURA_OTHER_TO_FLARE_OTHER(21),
    LIFE_OTHER_TO_DISTANCE(22);


    fun encode(value: Int) = this.real_number + value * 100

    companion object {
        fun fromInt(value: Int) = LocToLoc.values().first { it.real_number == value }

    }
}