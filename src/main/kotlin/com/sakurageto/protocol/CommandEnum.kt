package com.sakurageto.protocol

import com.sakurageto.card.INDEX_LACERATION_AURA
import com.sakurageto.card.INDEX_LACERATION_FLARE
import com.sakurageto.card.INDEX_LACERATION_LIFE
import com.sakurageto.card.PlayerEnum
import com.sakurageto.plugins.makeBugReportFile
import java.lang.Exception
import java.util.*

enum class CommandEnum() {
    ACK,
    NULL,
    SELECT_VERSION_YOUR,
    SELECT_VERSION_OTHER,
    SELECT_MODE_YOUR,
    SELECT_MODE_OTHER,
    SET_VERSION,
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
    ANVIL_YOUR,
    ANVIL_OTHER,
    MEMORY_YOUR,
    MEMORY_OTHER,
    RELIC_YOUR,
    RELIC_OTHER,
    UNASSEMBLY_YOUR,
    UNASSEMBLY_OTHER,
    ASSEMBLY_YOUR,
    ASSEMBLY_OTHER,

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
    POP_MEMORY_YOUR,
    POP_MEMORY_OTHER,
    POP_RELIC_YOUR,
    POP_RELIC_OTHER,
    POP_UNASSEMBLY_YOUR,
    POP_UNASSEMBLY_OTHER,
    POP_ASSEMBLY_YOUR,
    POP_ASSEMBLY_OTHER,

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
    ACTION_USE_CARD_PERJURY,
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

    SHOW_HAND_YOUR,
    SHOW_HAND_OTHER,
    SHOW_HAND_SOME_YOUR,
    SHOW_HAND_SOME_OTHER,
    SHOW_COVER_YOUR,
    SHOW_COVER_OTHER,
    SHOW_DECK_TOP_YOUR,
    SHOW_DECK_TOP_OTHER,
    SHOW_SPECIAL_YOUR,
    SHOW_SPECIAL_OTHER,
    SHOW_ASSEMBLY_YOUR,
    SHOW_ASSEMBLY_OTHER,

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
    SELECT_ACT,

    SET_TABOO_GAUGE_YOUR,
    SET_TABOO_GAUGE_OTHER,

    SELECT_NAP_LOCATION,

    CHOOSE_DISPROVE,
    SHOW_DISPROVE_RESULT,

    DIVING_YOUR,
    DIVING_OTHER,

    DIVING_REQUEST,
    DIVING_FORWARD,
    DIVING_BACKWARD,
    DIVING_SHOW,

    SET_JOURNEY_YOUR,
    SET_JOURNEY_OTHER,

    END_JOURNEY_YOUR,
    END_JOURNEY_OTHER,

    SET_MARKET_PRICE_YOUR,
    SET_MARKET_PRICE_OTHER,

    SET_AIMING_YOUR,
    SET_AIMING_OTHER,

    USE_CUSTOM_PARTS_YOUR,
    USE_CUSTOM_PARTS_OTHER,

    SHOW_SELECT_RESULT;

    fun isBasicOperation() = this in basicOperationSet

    suspend fun oppositeCommand(): CommandEnum{
        return oppositeMap[this]?: run {
            makeBugReportFile("oppositeCommand() do not support command: $this")
            NULL
        }
    }

    companion object{
        const val BASIC_OPERATION_CAUSE_BY_CARD = 200000

        private val basicOperationSet: EnumSet<CommandEnum> = EnumSet.of(
            ACTION_GO_FORWARD, ACTION_GO_BACKWARD, ACTION_WIND_AROUND, ACTION_INCUBATE, ACTION_BREAK_AWAY,
            ACTION_NAGA, ACTION_YAKSHA, ACTION_GARUDA, ACTION_ASURA, SELECT_NOT
        )

        private val oppositeMap = mapOf(
            ADD_CONCENTRATION_YOUR to ADD_CONCENTRATION_OTHER,
            ADD_CONCENTRATION_OTHER to ADD_CONCENTRATION_YOUR,
            REMOVE_SHRINK_YOUR to REMOVE_SHRINK_OTHER,
            REMOVE_SHRINK_OTHER to REMOVE_SHRINK_YOUR,
            SELECT_ENCHANTMENT_YOUR to SELECT_ENCHANTMENT_OTHER,
            SELECT_ENCHANTMENT_OTHER to SELECT_ENCHANTMENT_YOUR,
            DESTRUCTION_ENCHANTMENT_YOUR to DESTRUCTION_ENCHANTMENT_OTHER,
            DESTRUCTION_ENCHANTMENT_OTHER to DESTRUCTION_ENCHANTMENT_YOUR,
            COVER_CARD_YOUR to COVER_CARD_OTHER,
            COVER_CARD_OTHER to COVER_CARD_YOUR,
            ENCHANTMENT_CARD_YOUR to ENCHANTMENT_CARD_OTHER,
            ENCHANTMENT_CARD_OTHER to ENCHANTMENT_CARD_YOUR,
            DISCARD_CARD_YOUR to DISCARD_CARD_OTHER,
            USED_CARD_YOUR to USED_CARD_OTHER,
            DISCARD_CARD_OTHER to DISCARD_CARD_YOUR,
            USED_CARD_OTHER to USED_CARD_YOUR,
            DECK_TOP_YOUR to DECK_TOP_OTHER,
            DECK_BELOW_YOUR to DECK_BELOW_OTHER,
            DECK_TOP_OTHER to DECK_TOP_YOUR,
            DECK_BELOW_OTHER to DECK_BELOW_YOUR,
            POP_COVER_YOUR to POP_COVER_OTHER,
            POP_PLAYING_YOUR to POP_PLAYING_OTHER,
            POP_DISCARD_YOUR to POP_DISCARD_OTHER,
            POP_HAND_YOUR to POP_HAND_OTHER,
            POP_DECK_YOUR to POP_DECK_OTHER,
            POP_COVER_OTHER to POP_COVER_YOUR,
            POP_HAND_OTHER to POP_HAND_YOUR,
            POP_DECK_OTHER to POP_DECK_YOUR,
            POP_PLAYING_OTHER to POP_PLAYING_YOUR,
            POP_DISCARD_OTHER to POP_DISCARD_YOUR,
            MAKE_ATTACK_COMPLETE_YOUR to MAKE_ATTACK_COMPLETE_OTHER,
            MAKE_ATTACK_COMPLETE_OTHER to MAKE_ATTACK_COMPLETE_YOUR,
            ATTACK_INFORMATION_YOUR to ATTACK_INFORMATION_OTHER,
            ATTACK_INFORMATION_OTHER to ATTACK_INFORMATION_YOUR,
            USE_CARD_YOUR to USE_CARD_OTHER,
            USE_CARD_OTHER to USE_CARD_YOUR,
            USE_CARD_YOUR_REACTION to USE_CARD_OTHER_REACTION,
            USE_CARD_OTHER_REACTION to USE_CARD_YOUR_REACTION,
            CARD_HAND_TO_DECK_BELOW_YOUR to CARD_HAND_TO_DECK_BELOW_OTHER,
            CARD_HAND_TO_DECK_BELOW_OTHER to CARD_HAND_TO_DECK_BELOW_YOUR,
            CARD_HAND_TO_DECK_UPPER_YOUR to CARD_HAND_TO_DECK_UPPER_OTHER,
            CARD_HAND_TO_DECK_UPPER_OTHER to CARD_HAND_TO_DECK_UPPER_YOUR,
            ACTION_GO_FORWARD_YOUR to ACTION_GO_FORWARD_OTHER,
            ACTION_GO_BACKWARD_YOUR to ACTION_GO_BACKWARD_OTHER,
            ACTION_WIND_AROUND_YOUR to ACTION_WIND_AROUND_OTHER,
            ACTION_INCUBATE_YOUR to ACTION_INCUBATE_OTHER,
            ACTION_BREAK_AWAY_YOUR to ACTION_BREAK_AWAY_OTHER,
            PLAYING_CARD_YOUR to PLAYING_CARD_OTHER,
            PLAYING_CARD_OTHER to PLAYING_CARD_YOUR,
            POP_SPECIAL_YOUR to POP_SPECIAL_OTHER,
            POP_SPECIAL_OTHER to POP_SPECIAL_YOUR,
            SHOW_HAND_YOUR to SHOW_HAND_OTHER,
            SHOW_HAND_SOME_YOUR to SHOW_HAND_SOME_OTHER,
            SHOW_COVER_YOUR to SHOW_COVER_OTHER,
            POP_USED_YOUR to POP_USED_OTHER,
            POP_USED_OTHER to POP_USED_YOUR,
            SPECIAL_YOUR to SPECIAL_OTHER,
            SPECIAL_OTHER to SPECIAL_YOUR,
            POP_ENCHANTMENT_YOUR to POP_ENCHANTMENT_OTHER,
            POP_ENCHANTMENT_OTHER to POP_ENCHANTMENT_YOUR,
            HAND_YOUR to HAND_OTHER,
            HAND_OTHER to HAND_YOUR,
            SEAL_YOUR to SEAL_OTHER,
            SEAL_OTHER to SEAL_YOUR,
            POP_SEAL_YOUR to POP_SEAL_OTHER,
            POP_SEAL_OTHER to POP_SEAL_YOUR,
            POP_POISON_BAG_YOUR to POP_POISON_BAG_OTHER,
            POP_POISON_BAG_OTHER to POP_POISON_BAG_YOUR,
            POISON_BAG_YOUR to POISON_BAG_OTHER,
            POISON_BAG_OTHER to POISON_BAG_YOUR,
            POP_ADDITIONAL_YOUR to POP_ADDITIONAL_OTHER,
            POP_ADDITIONAL_OTHER to POP_ADDITIONAL_YOUR,
            GET_DAMAGE_AURA_YOUR to GET_DAMAGE_AURA_OTHER,
            GET_DAMAGE_LIFE_YOUR to GET_DAMAGE_LIFE_OTHER,
            GET_DAMAGE_AURA_OTHER to GET_DAMAGE_AURA_YOUR,
            GET_DAMAGE_LIFE_OTHER to GET_DAMAGE_LIFE_YOUR,
            OUT_OF_GAME_OTHER to OUT_OF_GAME_YOUR,
            OUT_OF_GAME_YOUR to OUT_OF_GAME_OTHER,
            TRANSFORM_OTHER to TRANSFORM_YOUR,
            TRANSFORM_YOUR to TRANSFORM_OTHER,
            ACTION_YAKSHA_YOUR to ACTION_YAKSHA_OTHER,
            ACTION_NAGA_YOUR to ACTION_NAGA_OTHER,
            ACTION_GARUDA_YOUR to ACTION_GARUDA_OTHER,
            ACTION_ASURA_YOUR to ACTION_ASURA_OTHER,
            ACTION_YAKSHA_OTHER to ACTION_YAKSHA_YOUR,
            ACTION_NAGA_OTHER to ACTION_NAGA_YOUR,
            ACTION_ASURA_OTHER to ACTION_ASURA_YOUR,
            ACTION_GARUDA_OTHER to ACTION_GARUDA_YOUR,
            SET_THUNDER_GAUGE_YOUR to SET_THUNDER_GAUGE_OTHER,
            SET_WIND_GAUGE_YOUR to SET_WIND_GAUGE_OTHER,
            SET_THUNDER_GAUGE_OTHER to SET_THUNDER_GAUGE_YOUR,
            SET_WIND_GAUGE_OTHER to SET_WIND_GAUGE_YOUR,
            INCREASE_THUNDER_GAUGE_YOUR to INCREASE_THUNDER_GAUGE_OTHER,
            INCREASE_THUNDER_GAUGE_OTHER to INCREASE_THUNDER_GAUGE_YOUR,
            INCREASE_WIND_GAUGE_YOUR to INCREASE_WIND_GAUGE_OTHER,
            INCREASE_WIND_GAUGE_OTHER to INCREASE_WIND_GAUGE_YOUR,
            ADDITIONAL_YOUR to ADDITIONAL_OTHER,
            SHOW_SPECIAL_YOUR to SHOW_SPECIAL_OTHER,
            SHOW_SPECIAL_OTHER to SHOW_SPECIAL_YOUR,
            SET_TAIL_WIND_YOUR to SET_TAIL_WIND_OTHER,
            SET_TAIL_WIND_OTHER to SET_TAIL_WIND_YOUR,
            SET_HEAD_WIND_YOUR to SET_HEAD_WIND_OTHER,
            SET_HEAD_WIND_OTHER to SET_HEAD_WIND_YOUR,
            SHOW_DECK_TOP_YOUR to SHOW_DECK_TOP_OTHER,
            SHOW_DECK_TOP_OTHER to SHOW_DECK_TOP_YOUR,
            NOT_READY_SOLDIER_ZONE_YOUR to NOT_READY_SOLDIER_ZONE_OTHER,
            NOT_READY_SOLDIER_ZONE_OTHER to NOT_READY_SOLDIER_ZONE_YOUR,
            READY_SOLDIER_ZONE_YOUR to READY_SOLDIER_ZONE_OTHER,
            READY_SOLDIER_ZONE_OTHER to READY_SOLDIER_ZONE_YOUR,
            POP_NOT_READY_SOLDIER_ZONE_YOUR to POP_NOT_READY_SOLDIER_ZONE_OTHER,
            POP_NOT_READY_SOLDIER_ZONE_OTHER to POP_NOT_READY_SOLDIER_ZONE_YOUR,
            POP_READY_SOLDIER_ZONE_YOUR to POP_READY_SOLDIER_ZONE_OTHER,
            POP_READY_SOLDIER_ZONE_OTHER to POP_READY_SOLDIER_ZONE_YOUR,
            SET_IDEA_STAGE_YOUR to SET_IDEA_STAGE_OTHER,
            SET_IDEA_STAGE_OTHER to SET_IDEA_STAGE_YOUR,
            IDEA_YOUR to IDEA_OTHER,
            IDEA_OTHER to IDEA_YOUR,
            END_IDEA_YOUR to END_IDEA_OTHER,
            END_IDEA_OTHER to END_IDEA_YOUR,
            POP_IDEA_YOUR to POP_IDEA_OTHER,
            POP_IDEA_OTHER to POP_IDEA_YOUR,
            POP_END_IDEA_YOUR to POP_END_IDEA_OTHER,
            POP_END_IDEA_OTHER to POP_END_IDEA_YOUR,
            SET_IDEA_FLIP_YOUR to SET_IDEA_FLIP_OTHER,
            SET_IDEA_FLIP_OTHER to SET_IDEA_FLIP_YOUR,
            SET_ACT_YOUR to SET_ACT_OTHER,
            SET_ACT_OTHER to SET_ACT_YOUR,
            ADD_THIS_TURN_DISTANCE to ADD_THIS_TURN_DISTANCE,
            REDUCE_THIS_TURN_DISTANCE to REDUCE_THIS_TURN_DISTANCE,
            ADD_THIS_TURN_SWELL_DISTANCE to ADD_THIS_TURN_SWELL_DISTANCE,
            REDUCE_THIS_TURN_SWELL_DISTANCE to REDUCE_THIS_TURN_SWELL_DISTANCE,
            ANVIL_YOUR to ANVIL_OTHER,
            ANVIL_OTHER to ANVIL_YOUR,
            SET_TABOO_GAUGE_YOUR to SET_TABOO_GAUGE_OTHER,
            SET_TABOO_GAUGE_OTHER to SET_TABOO_GAUGE_YOUR,
            SHOW_DISPROVE_RESULT to SHOW_DISPROVE_RESULT,
            DIVING_YOUR to DIVING_OTHER,
            DIVING_OTHER to DIVING_YOUR,
            SET_JOURNEY_YOUR to SET_JOURNEY_OTHER,
            SET_JOURNEY_OTHER to SET_JOURNEY_YOUR,
            MEMORY_YOUR to MEMORY_OTHER,
            MEMORY_OTHER to MEMORY_YOUR,
            POP_MEMORY_YOUR to POP_MEMORY_OTHER,
            POP_MEMORY_OTHER to POP_MEMORY_YOUR,
            END_JOURNEY_YOUR to END_JOURNEY_OTHER,
            END_JOURNEY_OTHER to END_JOURNEY_YOUR,
            SET_MARKET_PRICE_YOUR to SET_MARKET_PRICE_OTHER,
            SET_MARKET_PRICE_OTHER to SET_MARKET_PRICE_YOUR,
            POP_RELIC_YOUR to POP_RELIC_OTHER,
            POP_RELIC_OTHER to POP_RELIC_YOUR,
            RELIC_YOUR to RELIC_OTHER,
            RELIC_OTHER to RELIC_YOUR,
            SET_AIMING_YOUR to SET_AIMING_OTHER,
            SET_AIMING_OTHER to SET_AIMING_YOUR,
            UNASSEMBLY_YOUR to UNASSEMBLY_OTHER,
            UNASSEMBLY_OTHER to UNASSEMBLY_YOUR,
            ASSEMBLY_YOUR to ASSEMBLY_OTHER,
            ASSEMBLY_OTHER to ASSEMBLY_YOUR,
            USE_CUSTOM_PARTS_YOUR to USE_CUSTOM_PARTS_OTHER,
            USE_CUSTOM_PARTS_OTHER to USE_CUSTOM_PARTS_YOUR,
            SHOW_ASSEMBLY_YOUR to SHOW_ASSEMBLY_OTHER,
            SHOW_ASSEMBLY_OTHER to SHOW_ASSEMBLY_YOUR,
            POP_ASSEMBLY_YOUR to POP_ASSEMBLY_OTHER,
            POP_ASSEMBLY_OTHER to POP_ASSEMBLY_YOUR,
            POP_UNASSEMBLY_YOUR to POP_UNASSEMBLY_OTHER,
            POP_UNASSEMBLY_OTHER to POP_UNASSEMBLY_YOUR
        )

        val cardEffectSelectCommandSet =
            setOf(SELECT_ONE, SELECT_TWO, SELECT_THREE, SELECT_FOUR, SELECT_FIVE, SELECT_SIX, SELECT_SEVEN, SELECT_EIGHT, SELECT_NINE, SELECT_TEN, SELECT_NOT)

        val reactCommandSet =
            setOf(REACT_USE_CARD_HAND, REACT_USE_CARD_SPECIAL, REACT_USE_CARD_SOLDIER)
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
    SEED_TOKEN(8),
    LACERATION_YOUR(9),
    LACERATION_OTHER(10);

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
            LACERATION_YOUR -> LACERATION_OTHER
            LACERATION_OTHER -> LACERATION_YOUR
        }
    }

    companion object {
        fun fromInt(value: Int) = TokenEnum.values().first { it.real_number == value }

        fun getLaceration(getDamage: PlayerEnum, giveDamage: PlayerEnum) = if(getDamage == giveDamage) LACERATION_YOUR
        else LACERATION_OTHER

        fun Int.toLacerationLocation() = when(this){
            INDEX_LACERATION_AURA -> LocationEnum.AURA_YOUR
            INDEX_LACERATION_FLARE -> LocationEnum.FLARE_YOUR
            INDEX_LACERATION_LIFE -> LocationEnum.LIFE_YOUR
            else -> throw Exception("invalid int value to laceration index: $this")
        }
    }
}

enum class LocationEnum(var real_number: Int){
    AURA_YOUR(0),
    AURA_OTHER(1),
    FLARE_YOUR(2),
    FLARE_OTHER(3),
    LIFE_YOUR(4),
    LIFE_OTHER(5),
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
    NOT_SELECTED_SPECIAL(46),

    ANVIL_YOUR(47),
    ANVIL_OTHER(48),
    ALL_NORMAL(49),
    NOT_SELECTED_NORMAL_CARD(50),

    MEMORY_YOUR(51),
    MEMORY_OTHER(52),

    FLOW_YOUR(53),
    FLOW_OTHER(54),

    RELIC_YOUR(55),
    RELIC_OTHER(56),

    ASSEMBLY_YOUR(57),
    ASSEMBLY_OTHER(58),
    UNASSEMBLY_YOUR(59),
    UNASSEMBLY_OTHER(60);

    suspend fun oppositeLocation(): LocationEnum{
        return oppositeMap[this]?: run {
            makeBugReportFile("oppositeCommand() do not support command: $this")
            this
        }
    }

    companion object {
        fun fromInt(value: Int) = LocationEnum.values().first { it.real_number == value }

        private val oppositeMap = mapOf(
            AURA_YOUR to AURA_OTHER,
            AURA_OTHER to AURA_YOUR,
            FLARE_YOUR to FLARE_OTHER,
            FLARE_OTHER to FLARE_YOUR,
            LIFE_YOUR to LIFE_OTHER,
            LIFE_OTHER to LIFE_YOUR,
            DUST to DUST,
            YOUR_ENCHANTMENT_ZONE_CARD to OTHER_ENCHANTMENT_ZONE_CARD,
            OTHER_ENCHANTMENT_ZONE_CARD to YOUR_ENCHANTMENT_ZONE_CARD,
            DISTANCE to DISTANCE,
            YOUR_DECK_TOP to OTHER_DECK_TOP,
            YOUR_DECK_BELOW to OTHER_DECK_BELOW,
            MACHINE_YOUR to MACHINE_OTHER,
            MACHINE_BURN_YOUR to MACHINE_BURN_OTHER,
            OUT_OF_GAME to OUT_OF_GAME,
            YOUR_USED_CARD to OTHER_USED_CARD,
            OTHER_USED_CARD to YOUR_USED_CARD,
            PLAYING_ZONE_YOUR to PLAYING_ZONE_OTHER,
            PLAYING_ZONE_OTHER to PLAYING_ZONE_YOUR,
            READY_DIRT_ZONE_YOUR to READY_DIRT_ZONE_OTHER,
            READY_DIRT_ZONE_OTHER to READY_DIRT_ZONE_YOUR,
            NOT_READY_DIRT_ZONE_YOUR to NOT_READY_DIRT_ZONE_OTHER,
            NOT_READY_DIRT_ZONE_OTHER to NOT_READY_DIRT_ZONE_YOUR,
            IDEA_YOUR to IDEA_OTHER,
            IDEA_OTHER to IDEA_YOUR,
            END_IDEA_YOUR to END_IDEA_OTHER,
            END_IDEA_OTHER to END_IDEA_YOUR,
            ANVIL_YOUR to ANVIL_OTHER,
            ANVIL_OTHER to ANVIL_YOUR,
            MEMORY_YOUR to MEMORY_OTHER,
            MEMORY_OTHER to MEMORY_YOUR,
            FLOW_YOUR to FLOW_OTHER,
            FLOW_OTHER to FLOW_YOUR,
            RELIC_YOUR to RELIC_OTHER,
            RELIC_OTHER to RELIC_YOUR,
            ASSEMBLY_YOUR to ASSEMBLY_OTHER,
            ASSEMBLY_OTHER to ASSEMBLY_YOUR,
            UNASSEMBLY_YOUR to UNASSEMBLY_OTHER,
            UNASSEMBLY_OTHER to UNASSEMBLY_YOUR
        )

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
    LIFE_OTHER_TO_DISTANCE(22),
    DUST_TO_OUT(23),
    OUT_TO_DUST(24),
    LIFE_YOUR_TO_LIFE_OTHER(25),
    LIFE_YOUR_TO_OUT(26),
    LIFE_OTHER_TO_OUT(27),
    AURA_YOUR_TO_FLOW(28),
    LIFE_YOUR_TO_AURA_YOUR(29),
    LIFE_YOUR_TO_AURA_OTHER(30),
    LIFE_OTHER_TO_AURA_YOUR(31),
    LIFE_OTHER_TO_AURA_OTHER(32);


    fun encode(value: Int) = this.real_number * 100 + value

    companion object {
        fun fromInt(value: Int) = LocToLoc.values().first { it.real_number == value }

    }
}