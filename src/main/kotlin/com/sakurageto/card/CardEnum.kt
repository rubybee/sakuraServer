package com.sakurageto.card

import com.sakurageto.gamelogic.MegamiEnum

enum class PlayerEnum {
    PLAYER1,
    PLAYER2;

    fun opposite(): PlayerEnum{
        return if(this == PLAYER1){
            PLAYER2
        } else{
            PLAYER1
        }
    }
}

enum class SpecialCardEnum {
    UNUSED,
    PLAYING,
    PLAYED
}

enum class CardClass {
    SPECIAL,
    NORMAL
}

enum class CardType {
    ATTACK,
    BEHAVIOR,
    ENCHANTMENT,
    UNDEFINED
}

enum class SubType {
    FULL_POWER,
    REACTION,
    NONE,
    UNDEFINED,
}

enum class DistanceType {
    DISCONTINUOUS,
    CONTINUOUS
}

enum class CardName {
    CARD_UNNAME,

    YURINA_CHAM,
    YURINA_ILSUM,
    YURINA_JARUCHIGI,
    YURINA_GUHAB,
    YURINA_GIBACK,
    YURINA_APDO,
    YURINA_GIYENBANJO,

    YURINA_WOLYUNGNACK,
    YURINA_POBARAM,
    YURINA_JJOCKBAE,
    YURINA_JURUCK,

    SAINE_DOUBLEBEGI,
    SAINE_HURUBEGI,
    SAINE_MOOGECHOO,
    SAINE_GANPA,
    SAINE_GWONYUCK,
    SAINE_CHOONGEMJUNG,
    SAINE_MOOEMBUCK,

    SAINE_YULDONGHOGEK,
    SAINE_HANGMUNGGONGJIN,
    SAINE_EMMOOSHOEBING,
    SAINE_JONGGEK,

    HIMIKA_SHOOT,
    HIMIKA_RAPIDFIRE,
    HIMIKA_MAGNUMCANON,
    HIMIKA_FULLBURST,
    HIMIKA_BACKSTEP,
    HIMIKA_BACKDRAFT,
    HIMIKA_SMOKE,

    HIMIKA_REDBULLET,
    HIMIKA_CRIMSONZERO,
    HIMIKA_SCARLETIMAGINE,
    HIMIKA_BURMILIONFIELD,

    TOKOYO_BITSUNERIGI,
    TOKOYO_WOOAHHANTAGUCK,
    TOKOYO_RUNNINGRABIT,
    TOKOYO_POETDANCE,
    TOKOYO_FLIPFAN,
    TOKOYO_WINDSTAGE,
    TOKOYO_SUNSTAGE,

    TOKOYO_KUON,
    TOKOYO_THOUSANDBIRD,
    TOKOYO_ENDLESSWIND,
    TOKOYO_TOKOYOMOON,

    OBORO_WIRE,
    OBORO_SHADOWCALTROP,
    OBORO_ZANGEKIRANBU,
    OBORO_NINJAWALK,
    OBORO_INDUCE,
    OBORO_CLONE,
    OBORO_BIOACTIVITY,

    OBORO_KUMASUKE,
    OBORO_TOBIKAGE,
    OBORO_ULOO,
    OBORO_MIKAZRA,

    YUKIHI_YUKIHI,
    YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE,
    YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS,
    YUKIHI_PUSH_OUT_SLASH_PULL,
    YUKIHI_SWING_SLASH_STAB,
    YUKIHI_TURN_UMBRELLA,
    YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN,
    YUKIHI_MAKE_CONNECTION,

    YUKIHI_FLUTTERING_SNOWFLAKE,
    YUKIHI_SWAYING_LAMPLIGHT,
    YUKIHI_CLINGY_MIND,
    YUKIHI_SWIRLING_GESTURE,

    SHINRA_SHINRA,
    SHINRA_IBLON,
    SHINRA_BANLON,
    SHINRA_KIBEN,
    SHINRA_INYONG,
    SHINRA_SEONDONG,
    SHINRA_JANGDAM,
    SHINRA_NONPA,

    SHINRA_WANJEON_NONPA,
    SHINRA_DASIG_IHAE,
    SHINRA_CHEONJI_BANBAG,
    SHINRA_SAMRA_BAN_SHO,

    HAGANE_CENTRIFUGAL_ATTACK,
    HAGANE_FOUR_WINDED_EARTHQUAKE,
    HAGANE_GROUND_BREAKING,
    HAGANE_HYPER_RECOIL,
    HAGANE_WON_MU_RUYN,
    HAGANE_RING_A_BELL,
    HAGANE_GRAVITATION_FIELD,

    HAGANE_GRAND_SKY_HOLE_CRASH,
    HAGANE_GRAND_BELL_MEGALOBEL,
    HAGANE_GRAND_GRAVITATION_ATTRACT,
    HAGANE_GRAND_MOUNTAIN_RESPECT,

    CHIKAGE_THROW_KUNAI,
    CHIKAGE_POISON_NEEDLE,
    CHIKAGE_TO_ZU_CHU,
    CHIKAGE_CUTTING_NECK,
    CHIKAGE_POISON_SMOKE,
    CHIKAGE_TIP_TOEING,
    CHIKAGE_MUDDLE,

    CHIKAGE_DEADLY_POISON,
    CHIKAGE_HAN_KI_POISON,
    CHIKAGE_REINCARNATION_POISON,
    CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE,

    POISON_PARALYTIC,
    POISON_HALLUCINOGENIC,
    POISON_RELAXATION,
    POISON_DEADLY_1,
    POISON_DEADLY_2;


    companion object {
        fun returnNormalCardNameByMegami(megami_name: MegamiEnum):List<CardName>{
            return when (megami_name){
                MegamiEnum.NONE -> listOf()
                MegamiEnum.YURINA -> listOf(
                    YURINA_CHAM, YURINA_ILSUM, YURINA_JARUCHIGI, YURINA_GUHAB, YURINA_GIBACK,
                    YURINA_APDO, YURINA_GIYENBANJO
                )
                MegamiEnum.SAINE -> listOf(
                    SAINE_HURUBEGI, SAINE_DOUBLEBEGI, SAINE_MOOGECHOO, SAINE_GANPA, SAINE_GWONYUCK,
                    SAINE_CHOONGEMJUNG, SAINE_MOOEMBUCK
                )
                MegamiEnum.HIMIKA -> listOf(
                    HIMIKA_SHOOT, HIMIKA_RAPIDFIRE, HIMIKA_MAGNUMCANON, HIMIKA_FULLBURST,
                    HIMIKA_BACKSTEP, HIMIKA_BACKDRAFT, HIMIKA_SMOKE
                )
                MegamiEnum.TOKOYO -> listOf(
                    TOKOYO_BITSUNERIGI, TOKOYO_WOOAHHANTAGUCK, TOKOYO_RUNNINGRABIT, TOKOYO_POETDANCE,
                    TOKOYO_FLIPFAN, TOKOYO_WINDSTAGE, TOKOYO_SUNSTAGE
                )
                MegamiEnum.OBORO -> listOf(
                    OBORO_WIRE, OBORO_SHADOWCALTROP, OBORO_ZANGEKIRANBU, OBORO_NINJAWALK,
                    OBORO_INDUCE, OBORO_CLONE, OBORO_BIOACTIVITY
                )
                MegamiEnum.YUKIHI -> listOf(
                    YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS,
                    YUKIHI_PUSH_OUT_SLASH_PULL, YUKIHI_SWING_SLASH_STAB, YUKIHI_TURN_UMBRELLA,
                    YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, YUKIHI_MAKE_CONNECTION
                )
                MegamiEnum.SHINRA -> listOf(
                    SHINRA_IBLON, SHINRA_BANLON,
                    SHINRA_KIBEN, SHINRA_INYONG, SHINRA_SEONDONG,
                    SHINRA_JANGDAM, SHINRA_NONPA
                )
                MegamiEnum.HAGANE -> listOf(
                    HAGANE_CENTRIFUGAL_ATTACK, HAGANE_FOUR_WINDED_EARTHQUAKE,
                    HAGANE_GROUND_BREAKING, HAGANE_HYPER_RECOIL,
                    HAGANE_WON_MU_RUYN, HAGANE_RING_A_BELL,
                    HAGANE_GRAVITATION_FIELD
                )
                MegamiEnum.CHIKAGE -> listOf(
                    CHIKAGE_THROW_KUNAI, CHIKAGE_POISON_NEEDLE, CHIKAGE_TO_ZU_CHU,
                    CHIKAGE_CUTTING_NECK, CHIKAGE_POISON_SMOKE, CHIKAGE_TIP_TOEING,
                    CHIKAGE_MUDDLE
                )
            }
        }

        fun returnSpecialCardNameByMegami(megami_name: MegamiEnum): List<CardName> {
            return when (megami_name){
                MegamiEnum.NONE -> listOf()
                MegamiEnum.YURINA -> listOf(
                    YURINA_WOLYUNGNACK, YURINA_POBARAM, YURINA_JJOCKBAE, YURINA_JURUCK
                )

                MegamiEnum.SAINE -> listOf(
                    SAINE_YULDONGHOGEK, SAINE_HANGMUNGGONGJIN, SAINE_EMMOOSHOEBING, SAINE_JONGGEK
                )

                MegamiEnum.HIMIKA -> listOf(
                    HIMIKA_REDBULLET, HIMIKA_CRIMSONZERO, HIMIKA_SCARLETIMAGINE, HIMIKA_BURMILIONFIELD
                )

                MegamiEnum.TOKOYO -> listOf(
                    TOKOYO_KUON, TOKOYO_THOUSANDBIRD, TOKOYO_ENDLESSWIND, TOKOYO_TOKOYOMOON
                )

                MegamiEnum.OBORO -> listOf(
                    OBORO_KUMASUKE, OBORO_TOBIKAGE, OBORO_ULOO, OBORO_MIKAZRA
                )

                MegamiEnum.YUKIHI -> listOf(
                    YUKIHI_FLUTTERING_SNOWFLAKE, YUKIHI_SWAYING_LAMPLIGHT, YUKIHI_CLINGY_MIND, YUKIHI_SWIRLING_GESTURE
                )

                MegamiEnum.SHINRA -> listOf(
                    SHINRA_WANJEON_NONPA, SHINRA_DASIG_IHAE, SHINRA_CHEONJI_BANBAG, SHINRA_SAMRA_BAN_SHO
                )

                MegamiEnum.HAGANE -> listOf(
                    HAGANE_GRAND_SKY_HOLE_CRASH, HAGANE_GRAND_BELL_MEGALOBEL, HAGANE_GRAND_GRAVITATION_ATTRACT,
                    HAGANE_GRAND_MOUNTAIN_RESPECT
                )

                MegamiEnum.CHIKAGE -> listOf(
                    CHIKAGE_DEADLY_POISON, CHIKAGE_HAN_KI_POISON,
                    CHIKAGE_REINCARNATION_POISON, CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
                )
            }
        }

        fun returnAdditionalCardNameByMegami(megami_name: MegamiEnum): List<CardName> {
            return when (megami_name){
                MegamiEnum.NONE -> listOf()
                MegamiEnum.YURINA -> listOf()
                MegamiEnum.SAINE -> listOf()
                MegamiEnum.HIMIKA -> listOf()
                MegamiEnum.TOKOYO -> listOf()
                MegamiEnum.OBORO -> listOf()
                MegamiEnum.YUKIHI -> listOf()
                MegamiEnum.SHINRA -> listOf()
                MegamiEnum.HAGANE -> listOf()
                MegamiEnum.CHIKAGE -> listOf()
            }
        }

        fun returnPoisonCardName(): List<CardName> {
            return listOf(
                POISON_PARALYTIC, POISON_HALLUCINOGENIC, POISON_RELAXATION,
                POISON_DEADLY_1, POISON_DEADLY_2
            )
        }
    }
}