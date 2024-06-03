package com.sakurageto.protocol

import com.sakurageto.plugins.makeBugReportFile

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
    ALL_NORMAL_EXCEPT_ADDITIONAL(61),
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

    suspend fun oppositeLocation(): LocationEnum {
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