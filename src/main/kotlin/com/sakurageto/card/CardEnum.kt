package com.sakurageto.card

import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.gamelogic.MegamiEnum.*
import java.util.EnumMap

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
    POISON_ANYTHING,
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
    POISON_DEADLY_2,

    KURURU_ELEKITTEL,
    KURURU_ACCELERATOR,
    KURURU_KURURUOONG,
    KURURU_TORNADO,
    KURURU_REGAINER,
    KURURU_MODULE,
    KURURU_REFLECTOR,

    KURURU_DRAIN_DEVIL,
    KURURU_BIG_GOLEM,
    KURURU_INDUSTRIA,
    KURURU_KANSHOUSOUCHI_KURURUSIK,

    KURURU_DUPLICATED_GEAR_1,
    KURURU_DUPLICATED_GEAR_2,
    KURURU_DUPLICATED_GEAR_3,

    THALLYA_BURNING_STEAM,
    THALLYA_WAVING_EDGE,
    THALLYA_SHIELD_CHARGE,
    THALLYA_STEAM_CANNON,
    THALLYA_STUNT,
    THALLYA_ROARING,
    THALLYA_TURBO_SWITCH,

    THALLYA_ALPHA_EDGE,
    THALLYA_OMEGA_BURST,
    THALLYA_THALLYA_MASTERPIECE,
    THALLYA_JULIA_BLACKBOX,

    FORM_YAKSHA,
    FORM_NAGA,
    FORM_GARUDA,

    RAIRA_BEAST_NAIL,
    RAIRA_STORM_SURGE_ATTACK,
    RAIRA_REINCARNATION_NAIL,
    RAIRA_WIND_RUN,
    RAIRA_WISDOM_OF_STORM_SURGE,
    RAIRA_HOWLING,
    RAIRA_WIND_KICK,

    RAIRA_THUNDER_WIND_PUNCH,
    RAIRA_SUMMON_THUNDER,
    RAIRA_WIND_CONSEQUENCE_BALL,
    RAIRA_CIRCULAR_CIRCUIT,
    RAIRA_WIND_ATTACK,
    RAIRA_WIND_ZEN_KAI,
    RAIRA_WIND_CELESTIAL_SPHERE,

    UTSURO_WON_WOL,
    UTSURO_BLACK_WAVE,
    UTSURO_HARVEST,
    UTSURO_PRESSURE,
    UTSURO_SHADOW_WING,
    UTSURO_SHADOW_WALL,
    UTSURO_YUE_HOE_JU,

    UTSURO_HOE_MYEOL,
    UTSURO_HEO_WI,
    UTSURO_JONG_MAL,
    UTSURO_MA_SIG,

    YURINA_NAN_TA,
    YURINA_BEAN_BULLET,
    YURINA_NOT_COMPLETE_POBARAM,

    SAINE_SOUND_OF_ICE,
    SAINE_ACCOMPANIMENT,
    SAINE_DUET_TAN_JU_BING_MYEONG,

    HIMIKA_FIRE_WAVE,
    HIMIKA_SAT_SUI,
    HIMIKA_EN_TEN_HIMIKA,

    TOKOYO_FLOWING_PLAY,
    TOKOYO_SOUND_OF_SUN,
    TOKOYO_DUET_CHI_TAN_YANG_MYEONG,

    HONOKA_SPIRIT_SIK,
    HONOKA_GUARDIAN_SPIRIT_SIK,
    HONOKA_ASSAULT_SPIRIT_SIK,
    HONOKA_DIVINE_OUKA,
    HONOKA_SAKURA_BLIZZARD,
    HONOKA_UI_GI_GONG_JIN,
    HONOKA_SAKURA_WING,
    HONOKA_REGENERATION,
    HONOKA_SAKURA_AMULET,
    HONOKA_HONOKA_SPARKLE,
    HONOKA_COMMAND,
    HONOKA_TAIL_WIND,
    HONOKA_CHEST_WILLINGNESS,
    HONOKA_HAND_FLOWER,
    HONOKA_A_NEW_OPENING,
    HONOKA_UNDER_THE_NAME_OF_FLAG,
    HONOKA_FOUR_SEASON_BACK,
    HONOKA_FULL_BLOOM_PATH;

    fun toCardNumber(firstTurn: Boolean): Int{
        return if(firstTurn){
            cardNameHashmapFirst[this]
        } else{
            cardNameHashmapSecond[this]
        }?: -1
    }

    companion object {
        private val cardNameHashmapFirst = EnumMap<CardName, Int>(CardName::class.java).apply {
            //for first turn player 0~9999
            put(YURINA_CHAM, 100)
            put(YURINA_ILSUM, 101)
            put(YURINA_JARUCHIGI, 102)
            put(YURINA_GUHAB, 103)
            put(YURINA_GIBACK, 104)
            put(YURINA_APDO, 105)
            put(YURINA_GIYENBANJO, 106)
            put(YURINA_WOLYUNGNACK, 107)
            put(YURINA_POBARAM, 108)
            put(YURINA_JJOCKBAE, 109)
            put(YURINA_JURUCK, 110)
            put(YURINA_NAN_TA, 111)
            put(YURINA_BEAN_BULLET, 112)
            put(YURINA_NOT_COMPLETE_POBARAM, 113)

            put(SAINE_DOUBLEBEGI, 200)
            put(SAINE_HURUBEGI, 201)
            put(SAINE_MOOGECHOO, 202)
            put(SAINE_GANPA, 203)
            put(SAINE_GWONYUCK, 204)
            put(SAINE_CHOONGEMJUNG, 205)
            put(SAINE_MOOEMBUCK, 206)
            put(SAINE_YULDONGHOGEK, 207)
            put(SAINE_HANGMUNGGONGJIN, 208)
            put(SAINE_EMMOOSHOEBING, 209)
            put(SAINE_JONGGEK, 210)
            put(SAINE_SOUND_OF_ICE, 211)
            put(SAINE_ACCOMPANIMENT, 212)
            put(SAINE_DUET_TAN_JU_BING_MYEONG, 213)

            put(HIMIKA_SHOOT, 300)
            put(HIMIKA_RAPIDFIRE, 301)
            put(HIMIKA_MAGNUMCANON, 302)
            put(HIMIKA_FULLBURST, 303)
            put(HIMIKA_BACKSTEP, 304)
            put(HIMIKA_BACKDRAFT, 305)
            put(HIMIKA_SMOKE, 306)
            put(HIMIKA_REDBULLET, 307)
            put(HIMIKA_CRIMSONZERO, 308)
            put(HIMIKA_SCARLETIMAGINE, 309)
            put(HIMIKA_BURMILIONFIELD, 310)
            put(HIMIKA_FIRE_WAVE, 311)
            put(HIMIKA_SAT_SUI, 312)
            put(HIMIKA_EN_TEN_HIMIKA, 313)

            put(TOKOYO_BITSUNERIGI, 400)
            put(TOKOYO_WOOAHHANTAGUCK, 401)
            put(TOKOYO_RUNNINGRABIT, 402)
            put(TOKOYO_POETDANCE, 403)
            put(TOKOYO_FLIPFAN, 404)
            put(TOKOYO_WINDSTAGE, 405)
            put(TOKOYO_SUNSTAGE, 406)
            put(TOKOYO_KUON, 407)
            put(TOKOYO_THOUSANDBIRD, 408)
            put(TOKOYO_ENDLESSWIND, 409)
            put(TOKOYO_TOKOYOMOON, 410)
            put(TOKOYO_FLOWING_PLAY, 411)
            put(TOKOYO_SOUND_OF_SUN, 412)
            put(TOKOYO_DUET_CHI_TAN_YANG_MYEONG, 413)

            put(OBORO_WIRE, 500)
            put(OBORO_SHADOWCALTROP, 501)
            put(OBORO_ZANGEKIRANBU, 502)
            put(OBORO_NINJAWALK, 503)
            put(OBORO_INDUCE, 504)
            put(OBORO_CLONE, 505)
            put(OBORO_BIOACTIVITY, 506)
            put(OBORO_KUMASUKE, 507)
            put(OBORO_TOBIKAGE, 508)
            put(OBORO_ULOO, 509)
            put(OBORO_MIKAZRA, 510)

            put(YUKIHI_YUKIHI, 100000)
            put(YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, 600)
            put(YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, 601)
            put(YUKIHI_PUSH_OUT_SLASH_PULL, 602)
            put(YUKIHI_SWING_SLASH_STAB, 603)
            put(YUKIHI_TURN_UMBRELLA, 604)
            put(YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, 605)
            put(YUKIHI_MAKE_CONNECTION, 606)
            put(YUKIHI_FLUTTERING_SNOWFLAKE, 607)
            put(YUKIHI_SWAYING_LAMPLIGHT, 608)
            put(YUKIHI_CLINGY_MIND, 609)
            put(YUKIHI_SWIRLING_GESTURE, 610)

            put(SHINRA_SHINRA, CardSet.SHINRA_SHINRA_CARD_NUMBER)
            put(SHINRA_IBLON, 700)
            put(SHINRA_BANLON, 701)
            put(SHINRA_KIBEN, 702)
            put(SHINRA_INYONG, 703)
            put(SHINRA_SEONDONG, 704)
            put(SHINRA_JANGDAM, 705)
            put(SHINRA_NONPA, 706)
            put(SHINRA_WANJEON_NONPA, 707)
            put(SHINRA_DASIG_IHAE, 708)
            put(SHINRA_CHEONJI_BANBAG, 709)
            put(SHINRA_SAMRA_BAN_SHO, 710)

            put(HAGANE_CENTRIFUGAL_ATTACK, 800)
            put(HAGANE_FOUR_WINDED_EARTHQUAKE, 801)
            put(HAGANE_GROUND_BREAKING, 802)
            put(HAGANE_HYPER_RECOIL, 803)
            put(HAGANE_WON_MU_RUYN, 804)
            put(HAGANE_RING_A_BELL, 805)
            put(HAGANE_GRAVITATION_FIELD, 806)
            put(HAGANE_GRAND_SKY_HOLE_CRASH, 807)
            put(HAGANE_GRAND_BELL_MEGALOBEL, 808)
            put(HAGANE_GRAND_GRAVITATION_ATTRACT, 809)
            put(HAGANE_GRAND_MOUNTAIN_RESPECT, 810)

            put(CHIKAGE_THROW_KUNAI, 900)
            put(CHIKAGE_POISON_NEEDLE, 901)
            put(CHIKAGE_TO_ZU_CHU, 902)
            put(CHIKAGE_CUTTING_NECK, 903)
            put(CHIKAGE_POISON_SMOKE, 904)
            put(CHIKAGE_TIP_TOEING, 905)
            put(CHIKAGE_MUDDLE, 906)
            put(CHIKAGE_DEADLY_POISON, 907)
            put(CHIKAGE_HAN_KI_POISON, 908)
            put(CHIKAGE_REINCARNATION_POISON, 909)
            put(CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE, 910)
            put(POISON_PARALYTIC, 995)
            put(POISON_HALLUCINOGENIC, 996)
            put(POISON_RELAXATION, 997)
            put(POISON_DEADLY_1, 998)
            put(POISON_DEADLY_2, 999)

            put(KURURU_ELEKITTEL, 1000)
            put(KURURU_ACCELERATOR, 1001)
            put(KURURU_KURURUOONG, 1002)
            put(KURURU_TORNADO, 1003)
            put(KURURU_REGAINER, 1004)
            put(KURURU_MODULE, 1005)
            put(KURURU_REFLECTOR, 1006)
            put(KURURU_DRAIN_DEVIL, 1007)
            put(KURURU_BIG_GOLEM, 1008)
            put(KURURU_INDUSTRIA, 1009)
            put(KURURU_DUPLICATED_GEAR_1, 1010)
            put(KURURU_DUPLICATED_GEAR_2, 1011)
            put(KURURU_DUPLICATED_GEAR_3, 1012)
            put(KURURU_KANSHOUSOUCHI_KURURUSIK, 1013)

            put(THALLYA_BURNING_STEAM, 1100)
            put(THALLYA_WAVING_EDGE, 1101)
            put(THALLYA_SHIELD_CHARGE, 1102)
            put(THALLYA_STEAM_CANNON, 1103)
            put(THALLYA_STUNT, 1104)
            put(THALLYA_ROARING, 1105)
            put(THALLYA_TURBO_SWITCH, 1106)
            put(THALLYA_ALPHA_EDGE, 1107)
            put(THALLYA_OMEGA_BURST, 1108)
            put(THALLYA_THALLYA_MASTERPIECE, 1109)
            put(THALLYA_JULIA_BLACKBOX, 1110)
            put(FORM_YAKSHA, 1111)
            put(FORM_NAGA, 1112)
            put(FORM_GARUDA, 1113)

            put(RAIRA_BEAST_NAIL, 1200)
            put(RAIRA_STORM_SURGE_ATTACK, 1201)
            put(RAIRA_REINCARNATION_NAIL, 1202)
            put(RAIRA_WIND_RUN, 1203)
            put(RAIRA_WISDOM_OF_STORM_SURGE, 1204)
            put(RAIRA_HOWLING, 1205)
            put(RAIRA_WIND_KICK, 1206)
            put(RAIRA_THUNDER_WIND_PUNCH, 1207)
            put(RAIRA_SUMMON_THUNDER, 1208)
            put(RAIRA_WIND_CONSEQUENCE_BALL, 1209)
            put(RAIRA_CIRCULAR_CIRCUIT, 1210)
            put(RAIRA_WIND_ATTACK, 1211)
            put(RAIRA_WIND_ZEN_KAI, 1212)
            put(RAIRA_WIND_CELESTIAL_SPHERE, 1213)

            put(UTSURO_WON_WOL, 1300)
            put(UTSURO_BLACK_WAVE, 1301)
            put(UTSURO_HARVEST, 1302)
            put(UTSURO_PRESSURE, 1303)
            put(UTSURO_SHADOW_WING, 1304)
            put(UTSURO_SHADOW_WALL, 1305)
            put(UTSURO_YUE_HOE_JU, 1306)
            put(UTSURO_HOE_MYEOL, 1307)
            put(UTSURO_HEO_WI, 1308)
            put(UTSURO_JONG_MAL, 1309)
            put(UTSURO_MA_SIG, 1310)
        }
        private val cardNameHashmapSecond = EnumMap<CardName, Int>(CardName::class.java).apply {
            //for second turn player 10000~19999
            put(YURINA_CHAM, 10100)
            put(YURINA_ILSUM, 10101)
            put(YURINA_JARUCHIGI, 10102)
            put(YURINA_GUHAB, 10103)
            put(YURINA_GIBACK, 10104)
            put(YURINA_APDO, 10105)
            put(YURINA_GIYENBANJO, 10106)
            put(YURINA_WOLYUNGNACK, 10107)
            put(YURINA_POBARAM, 10108)
            put(YURINA_JJOCKBAE, 10109)
            put(YURINA_JURUCK, 10110)
            put(YURINA_NAN_TA, 10111)
            put(YURINA_BEAN_BULLET, 10112)
            put(YURINA_NOT_COMPLETE_POBARAM, 10113)

            put(SAINE_DOUBLEBEGI, 10200)
            put(SAINE_HURUBEGI, 10201)
            put(SAINE_MOOGECHOO, 10202)
            put(SAINE_GANPA, 10203)
            put(SAINE_GWONYUCK, 10204)
            put(SAINE_CHOONGEMJUNG, 10205)
            put(SAINE_MOOEMBUCK, 10206)
            put(SAINE_YULDONGHOGEK, 10207)
            put(SAINE_HANGMUNGGONGJIN, 10208)
            put(SAINE_EMMOOSHOEBING, 10209)
            put(SAINE_JONGGEK, 10210)
            put(SAINE_SOUND_OF_ICE, 10211)
            put(SAINE_ACCOMPANIMENT, 10212)
            put(SAINE_DUET_TAN_JU_BING_MYEONG, 10213)

            put(HIMIKA_SHOOT, 10300)
            put(HIMIKA_RAPIDFIRE, 10301)
            put(HIMIKA_MAGNUMCANON, 10302)
            put(HIMIKA_FULLBURST, 10303)
            put(HIMIKA_BACKSTEP, 10304)
            put(HIMIKA_BACKDRAFT, 10305)
            put(HIMIKA_SMOKE, 10306)
            put(HIMIKA_REDBULLET, 10307)
            put(HIMIKA_CRIMSONZERO, 10308)
            put(HIMIKA_SCARLETIMAGINE, 10309)
            put(HIMIKA_BURMILIONFIELD, 10310)
            put(HIMIKA_FIRE_WAVE, 10311)
            put(HIMIKA_SAT_SUI, 10312)
            put(HIMIKA_EN_TEN_HIMIKA, 10313)

            put(TOKOYO_BITSUNERIGI, 10400)
            put(TOKOYO_WOOAHHANTAGUCK, 10401)
            put(TOKOYO_RUNNINGRABIT, 10402)
            put(TOKOYO_POETDANCE, 10403)
            put(TOKOYO_FLIPFAN, 10404)
            put(TOKOYO_WINDSTAGE, 10405)
            put(TOKOYO_SUNSTAGE, 10406)
            put(TOKOYO_KUON, 10407)
            put(TOKOYO_THOUSANDBIRD, 10408)
            put(TOKOYO_ENDLESSWIND, 10409)
            put(TOKOYO_TOKOYOMOON, 10410)
            put(TOKOYO_FLOWING_PLAY, 411)
            put(TOKOYO_SOUND_OF_SUN, 412)
            put(TOKOYO_DUET_CHI_TAN_YANG_MYEONG, 413)

            put(OBORO_WIRE, 10500)
            put(OBORO_SHADOWCALTROP, 10501)
            put(OBORO_ZANGEKIRANBU, 10502)
            put(OBORO_NINJAWALK, 10503)
            put(OBORO_INDUCE, 10504)
            put(OBORO_CLONE, 10505)
            put(OBORO_BIOACTIVITY, 10506)
            put(OBORO_KUMASUKE, 10507)
            put(OBORO_TOBIKAGE, 10508)
            put(OBORO_ULOO, 10509)
            put(OBORO_MIKAZRA, 10510)

            put(YUKIHI_YUKIHI, 200000)
            put(YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, 10600)
            put(YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, 10601)
            put(YUKIHI_PUSH_OUT_SLASH_PULL, 10602)
            put(YUKIHI_SWING_SLASH_STAB, 10603)
            put(YUKIHI_TURN_UMBRELLA, 10604)
            put(YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, 10605)
            put(YUKIHI_MAKE_CONNECTION, 10606)
            put(YUKIHI_FLUTTERING_SNOWFLAKE, 10607)
            put(YUKIHI_SWAYING_LAMPLIGHT, 10608)
            put(YUKIHI_CLINGY_MIND, 10609)
            put(YUKIHI_SWIRLING_GESTURE, 10610)

            put(SHINRA_SHINRA, CardSet.SHINRA_SHINRA_CARD_NUMBER)
            put(SHINRA_IBLON, 10700)
            put(SHINRA_BANLON, 10701)
            put(SHINRA_KIBEN, 10702)
            put(SHINRA_INYONG, 10703)
            put(SHINRA_SEONDONG, 10704)
            put(SHINRA_JANGDAM, 10705)
            put(SHINRA_NONPA, 10706)
            put(SHINRA_WANJEON_NONPA, 10707)
            put(SHINRA_DASIG_IHAE, 10708)
            put(SHINRA_CHEONJI_BANBAG, 10709)
            put(SHINRA_SAMRA_BAN_SHO, 10710)

            put(HAGANE_CENTRIFUGAL_ATTACK, 10800)
            put(HAGANE_FOUR_WINDED_EARTHQUAKE, 10801)
            put(HAGANE_GROUND_BREAKING, 10802)
            put(HAGANE_HYPER_RECOIL, 10803)
            put(HAGANE_WON_MU_RUYN, 10804)
            put(HAGANE_RING_A_BELL, 10805)
            put(HAGANE_GRAVITATION_FIELD, 10806)
            put(HAGANE_GRAND_SKY_HOLE_CRASH, 10807)
            put(HAGANE_GRAND_BELL_MEGALOBEL, 10808)
            put(HAGANE_GRAND_GRAVITATION_ATTRACT, 10809)
            put(HAGANE_GRAND_MOUNTAIN_RESPECT, 10810)

            put(CHIKAGE_THROW_KUNAI, 10900)
            put(CHIKAGE_POISON_NEEDLE, 10901)
            put(CHIKAGE_TO_ZU_CHU, 10902)
            put(CHIKAGE_CUTTING_NECK, 10903)
            put(CHIKAGE_POISON_SMOKE, 10904)
            put(CHIKAGE_TIP_TOEING, 10905)
            put(CHIKAGE_MUDDLE, 10906)
            put(CHIKAGE_DEADLY_POISON, 10907)
            put(CHIKAGE_HAN_KI_POISON, 10908)
            put(CHIKAGE_REINCARNATION_POISON, 10909)
            put(CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE, 10910)
            put(POISON_PARALYTIC, 10995)
            put(POISON_HALLUCINOGENIC, 10996)
            put(POISON_RELAXATION, 10997)
            put(POISON_DEADLY_1, 10998)
            put(POISON_DEADLY_2, 10999)

            put(KURURU_ELEKITTEL, 11000)
            put(KURURU_ACCELERATOR, 11001)
            put(KURURU_KURURUOONG, 11002)
            put(KURURU_TORNADO, 11003)
            put(KURURU_REGAINER, 11004)
            put(KURURU_MODULE, 11005)
            put(KURURU_REFLECTOR, 11006)
            put(KURURU_DRAIN_DEVIL, 11007)
            put(KURURU_BIG_GOLEM, 11008)
            put(KURURU_INDUSTRIA, 11009)
            put(KURURU_DUPLICATED_GEAR_1, 11010)
            put(KURURU_DUPLICATED_GEAR_2, 11011)
            put(KURURU_DUPLICATED_GEAR_3, 11012)
            put(KURURU_KANSHOUSOUCHI_KURURUSIK, 11013)

            put(THALLYA_BURNING_STEAM, 11100)
            put(THALLYA_WAVING_EDGE, 11101)
            put(THALLYA_SHIELD_CHARGE, 11102)
            put(THALLYA_STEAM_CANNON, 11103)
            put(THALLYA_STUNT, 11104)
            put(THALLYA_ROARING, 11105)
            put(THALLYA_TURBO_SWITCH, 11106)
            put(THALLYA_ALPHA_EDGE, 11107)
            put(THALLYA_OMEGA_BURST, 11108)
            put(THALLYA_THALLYA_MASTERPIECE, 11109)
            put(THALLYA_JULIA_BLACKBOX, 11110)
            put(FORM_YAKSHA, 11111)
            put(FORM_NAGA, 11112)
            put(FORM_GARUDA, 11113)

            put(RAIRA_BEAST_NAIL, 11200)
            put(RAIRA_STORM_SURGE_ATTACK, 11201)
            put(RAIRA_REINCARNATION_NAIL, 11202)
            put(RAIRA_WIND_RUN, 11203)
            put(RAIRA_WISDOM_OF_STORM_SURGE, 11204)
            put(RAIRA_HOWLING, 11205)
            put(RAIRA_WIND_KICK, 11206)
            put(RAIRA_THUNDER_WIND_PUNCH, 11207)
            put(RAIRA_SUMMON_THUNDER, 11208)
            put(RAIRA_WIND_CONSEQUENCE_BALL, 11209)
            put(RAIRA_CIRCULAR_CIRCUIT, 11210)
            put(RAIRA_WIND_ATTACK, 11211)
            put(RAIRA_WIND_ZEN_KAI, 11212)
            put(RAIRA_WIND_CELESTIAL_SPHERE, 11213)

            put(UTSURO_WON_WOL, 11300)
            put(UTSURO_BLACK_WAVE, 11301)
            put(UTSURO_HARVEST, 11302)
            put(UTSURO_PRESSURE, 11303)
            put(UTSURO_SHADOW_WING, 11304)
            put(UTSURO_SHADOW_WALL, 11305)
            put(UTSURO_YUE_HOE_JU, 11306)
            put(UTSURO_HOE_MYEOL, 11307)
            put(UTSURO_HEO_WI, 11308)
            put(UTSURO_JONG_MAL, 11309)
            put(UTSURO_MA_SIG, 11310)
        }

        fun returnNormalCardNameByMegami(megami_name: MegamiEnum):List<CardName>{
            return when (megami_name){
                NONE -> listOf()
                YURINA -> listOf(
                    YURINA_CHAM, YURINA_ILSUM, YURINA_JARUCHIGI, YURINA_GUHAB, YURINA_GIBACK,
                    YURINA_APDO, YURINA_GIYENBANJO
                )
                SAINE -> listOf(
                    SAINE_HURUBEGI, SAINE_DOUBLEBEGI, SAINE_MOOGECHOO, SAINE_GANPA, SAINE_GWONYUCK,
                    SAINE_CHOONGEMJUNG, SAINE_MOOEMBUCK
                )
                HIMIKA -> listOf(
                    HIMIKA_SHOOT, HIMIKA_RAPIDFIRE, HIMIKA_MAGNUMCANON, HIMIKA_FULLBURST,
                    HIMIKA_BACKSTEP, HIMIKA_BACKDRAFT, HIMIKA_SMOKE
                )
                TOKOYO -> listOf(
                    TOKOYO_BITSUNERIGI, TOKOYO_WOOAHHANTAGUCK, TOKOYO_RUNNINGRABIT, TOKOYO_POETDANCE,
                    TOKOYO_FLIPFAN, TOKOYO_WINDSTAGE, TOKOYO_SUNSTAGE
                )
                OBORO -> listOf(
                    OBORO_WIRE, OBORO_SHADOWCALTROP, OBORO_ZANGEKIRANBU, OBORO_NINJAWALK,
                    OBORO_INDUCE, OBORO_CLONE, OBORO_BIOACTIVITY
                )
                YUKIHI -> listOf(
                    YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS,
                    YUKIHI_PUSH_OUT_SLASH_PULL, YUKIHI_SWING_SLASH_STAB, YUKIHI_TURN_UMBRELLA,
                    YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, YUKIHI_MAKE_CONNECTION
                )
                SHINRA -> listOf(
                    SHINRA_IBLON, SHINRA_BANLON,
                    SHINRA_KIBEN, SHINRA_INYONG, SHINRA_SEONDONG,
                    SHINRA_JANGDAM, SHINRA_NONPA
                )
                HAGANE -> listOf(
                    HAGANE_CENTRIFUGAL_ATTACK, HAGANE_FOUR_WINDED_EARTHQUAKE,
                    HAGANE_GROUND_BREAKING, HAGANE_HYPER_RECOIL,
                    HAGANE_WON_MU_RUYN, HAGANE_RING_A_BELL,
                    HAGANE_GRAVITATION_FIELD
                )
                CHIKAGE -> listOf(
                    CHIKAGE_THROW_KUNAI, CHIKAGE_POISON_NEEDLE, CHIKAGE_TO_ZU_CHU,
                    CHIKAGE_CUTTING_NECK, CHIKAGE_POISON_SMOKE, CHIKAGE_TIP_TOEING,
                    CHIKAGE_MUDDLE
                )
                KURURU -> listOf(
                    KURURU_ELEKITTEL, KURURU_ACCELERATOR, KURURU_KURURUOONG,
                    KURURU_TORNADO, KURURU_REGAINER, KURURU_MODULE,
                    KURURU_REFLECTOR
                )
                THALLYA -> listOf(
                    THALLYA_BURNING_STEAM, THALLYA_WAVING_EDGE, THALLYA_SHIELD_CHARGE,
                    THALLYA_STEAM_CANNON, THALLYA_STUNT, THALLYA_ROARING,
                    THALLYA_TURBO_SWITCH
                )
                RAIRA -> listOf(
                    RAIRA_BEAST_NAIL, RAIRA_STORM_SURGE_ATTACK, RAIRA_REINCARNATION_NAIL,
                    RAIRA_WIND_RUN, RAIRA_WISDOM_OF_STORM_SURGE, RAIRA_HOWLING,
                    RAIRA_WIND_KICK
                )

                UTSURO -> listOf(
                    UTSURO_WON_WOL, UTSURO_BLACK_WAVE, UTSURO_HARVEST,
                    UTSURO_PRESSURE, UTSURO_SHADOW_WING, UTSURO_SHADOW_WALL,
                    UTSURO_YUE_HOE_JU
                )
                YURINA_A1 -> listOf(
                    YURINA_NAN_TA, YURINA_ILSUM, YURINA_JARUCHIGI, YURINA_GUHAB, YURINA_GIBACK,
                    YURINA_BEAN_BULLET, YURINA_GIYENBANJO
                )
                SAINE_A1 -> listOf(
                    SAINE_HURUBEGI, SAINE_DOUBLEBEGI, SAINE_SOUND_OF_ICE, SAINE_GANPA, SAINE_GWONYUCK,
                    SAINE_ACCOMPANIMENT, SAINE_MOOEMBUCK
                )
                HIMIKA_A1 -> listOf(
                    HIMIKA_SHOOT, HIMIKA_FIRE_WAVE, HIMIKA_MAGNUMCANON, HIMIKA_FULLBURST,
                    HIMIKA_SAT_SUI, HIMIKA_BACKDRAFT, HIMIKA_SMOKE
                )
                TOKOYO_A1 -> listOf(
                    TOKOYO_FLOWING_PLAY, TOKOYO_WOOAHHANTAGUCK, TOKOYO_RUNNINGRABIT, TOKOYO_POETDANCE,
                    TOKOYO_SOUND_OF_SUN, TOKOYO_WINDSTAGE, TOKOYO_SUNSTAGE
                )

                HONOKA -> listOf(
                    HONOKA_SPIRIT_SIK, HONOKA_SAKURA_BLIZZARD, HONOKA_UI_GI_GONG_JIN,
                    HONOKA_SAKURA_WING, HONOKA_SAKURA_AMULET, HONOKA_COMMAND,
                    HONOKA_TAIL_WIND,
                )
            }
        }

        fun returnSpecialCardNameByMegami(megami_name: MegamiEnum): List<CardName> {
            return when (megami_name){
                NONE -> listOf()
                YURINA -> listOf(
                    YURINA_WOLYUNGNACK, YURINA_POBARAM, YURINA_JJOCKBAE, YURINA_JURUCK
                )
                SAINE -> listOf(
                    SAINE_YULDONGHOGEK, SAINE_HANGMUNGGONGJIN, SAINE_EMMOOSHOEBING, SAINE_JONGGEK
                )
                HIMIKA -> listOf(
                    HIMIKA_REDBULLET, HIMIKA_CRIMSONZERO, HIMIKA_SCARLETIMAGINE, HIMIKA_BURMILIONFIELD
                )
                TOKOYO -> listOf(
                    TOKOYO_KUON, TOKOYO_THOUSANDBIRD, TOKOYO_ENDLESSWIND, TOKOYO_TOKOYOMOON
                )
                OBORO -> listOf(
                    OBORO_KUMASUKE, OBORO_TOBIKAGE, OBORO_ULOO, OBORO_MIKAZRA
                )
                YUKIHI -> listOf(
                    YUKIHI_FLUTTERING_SNOWFLAKE, YUKIHI_SWAYING_LAMPLIGHT, YUKIHI_CLINGY_MIND, YUKIHI_SWIRLING_GESTURE
                )
                SHINRA -> listOf(
                    SHINRA_WANJEON_NONPA, SHINRA_DASIG_IHAE, SHINRA_CHEONJI_BANBAG, SHINRA_SAMRA_BAN_SHO
                )
                HAGANE -> listOf(
                    HAGANE_GRAND_SKY_HOLE_CRASH, HAGANE_GRAND_BELL_MEGALOBEL, HAGANE_GRAND_GRAVITATION_ATTRACT,
                    HAGANE_GRAND_MOUNTAIN_RESPECT
                )
                CHIKAGE -> listOf(
                    CHIKAGE_DEADLY_POISON, CHIKAGE_HAN_KI_POISON,
                    CHIKAGE_REINCARNATION_POISON, CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
                )
                KURURU -> listOf(
                    KURURU_DRAIN_DEVIL, KURURU_BIG_GOLEM, KURURU_INDUSTRIA,
                    KURURU_KANSHOUSOUCHI_KURURUSIK
                )
                THALLYA -> listOf(
                    THALLYA_ALPHA_EDGE, THALLYA_OMEGA_BURST, THALLYA_THALLYA_MASTERPIECE,
                    THALLYA_JULIA_BLACKBOX,
                )
                RAIRA -> listOf(
                    RAIRA_THUNDER_WIND_PUNCH, RAIRA_SUMMON_THUNDER, RAIRA_WIND_CONSEQUENCE_BALL,
                    RAIRA_CIRCULAR_CIRCUIT
                )
                UTSURO -> listOf(
                    UTSURO_HOE_MYEOL, UTSURO_HEO_WI, UTSURO_JONG_MAL,
                    UTSURO_MA_SIG
                )
                YURINA_A1 -> listOf(
                    YURINA_WOLYUNGNACK, YURINA_NOT_COMPLETE_POBARAM, YURINA_JJOCKBAE, YURINA_JURUCK
                )
                SAINE_A1 -> listOf(
                    SAINE_YULDONGHOGEK, SAINE_DUET_TAN_JU_BING_MYEONG, SAINE_EMMOOSHOEBING, SAINE_JONGGEK
                )
                HIMIKA_A1 -> listOf(
                    HIMIKA_REDBULLET, HIMIKA_EN_TEN_HIMIKA, HIMIKA_SCARLETIMAGINE, HIMIKA_BURMILIONFIELD
                )
                TOKOYO_A1 -> listOf(
                    TOKOYO_KUON, TOKOYO_DUET_CHI_TAN_YANG_MYEONG, TOKOYO_ENDLESSWIND, TOKOYO_TOKOYOMOON
                )
                HONOKA -> listOf(
                    HONOKA_CHEST_WILLINGNESS, HONOKA_UNDER_THE_NAME_OF_FLAG, HONOKA_FOUR_SEASON_BACK,
                    HONOKA_FULL_BLOOM_PATH
                )
            }
        }

        fun returnAdditionalCardNameByMegami(megami_name: MegamiEnum): List<CardName> {
            return when (megami_name){
                NONE -> listOf()
                YURINA -> listOf()
                SAINE -> listOf()
                HIMIKA -> listOf()
                TOKOYO -> listOf()
                OBORO -> listOf()
                YUKIHI -> listOf()
                SHINRA -> listOf()
                HAGANE -> listOf()
                CHIKAGE -> listOf()
                KURURU -> listOf(
                    KURURU_DUPLICATED_GEAR_1, KURURU_DUPLICATED_GEAR_2, KURURU_DUPLICATED_GEAR_3
                )
                THALLYA -> listOf(
                    FORM_YAKSHA, FORM_NAGA, FORM_GARUDA
                )
                RAIRA -> listOf(
                    RAIRA_WIND_ATTACK, RAIRA_WIND_ZEN_KAI, RAIRA_WIND_CELESTIAL_SPHERE
                )
                UTSURO -> listOf()
                YURINA_A1 -> listOf()
                SAINE_A1 -> listOf()
                HIMIKA_A1 -> listOf()
                TOKOYO_A1 -> listOf()
                HONOKA -> listOf(
                    HONOKA_GUARDIAN_SPIRIT_SIK, HONOKA_ASSAULT_SPIRIT_SIK, HONOKA_DIVINE_OUKA,
                    HONOKA_REGENERATION, HONOKA_HONOKA_SPARKLE, HONOKA_HAND_FLOWER, HONOKA_A_NEW_OPENING
                )
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

enum class DamageSelect {
    NULL,
    AURA,
    LIFE,
    BOTH
}