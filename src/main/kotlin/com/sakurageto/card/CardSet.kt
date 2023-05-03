package com.sakurageto.card

import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.gamelogic.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import java.util.EnumMap
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.ceil

data class Kikou(var attack: Int = 0, var behavior: Int = 0, var enchantment: Int = 0, var reaction: Int = 0, var fullPower: Int = 0)

object CardSet {
    private val cardNumberHashmap = HashMap<Int, CardName>()
    private val cardDataHashmap = EnumMap<CardName, CardData>(CardName::class.java)

    const val SHINRA_SHINRA_CARD_NUMBER = 100001
    fun Int.toCardName(): CardName = cardNumberHashmap[this]?: CardName.CARD_UNNAME
    fun CardName.toCardData(): CardData = cardDataHashmap[this]?: unused

    private fun hashMapTest(){
        val cardNameList = CardName.values()
        for(cardName in cardNameList){
            if(cardName == CardName.CARD_UNNAME || cardName == CardName.POISON_ANYTHING) continue
            if(cardName.toCardData() == unused){
                println("cardDataHashmap don't have card name: $cardName")
            }
            val firstNumber = cardName.toCardNumber(true)
            if(firstNumber == -1){
                println("cardNameHashmapFirst don't have card name: $cardName")
                if(firstNumber.toCardName() == CardName.CARD_UNNAME){
                    println("cardNumberHashMap don't have card number: $firstNumber")
                }
            }
            val secondNumber = cardName.toCardNumber(false)
            if(secondNumber == -1){
                println("cardNameHashmapSecond don't have card name: $cardName")
                if(secondNumber.toCardName() == CardName.CARD_UNNAME){
                    println("cardNumberHashMap don't have card number: $secondNumber")
                }
            }
        }
    }

    private fun hashMapInit(){
        //for number -> card name
        cardNumberHashmap[0] = CardName.CARD_UNNAME
        cardNumberHashmap[1] = CardName.POISON_ANYTHING
        cardNumberHashmap[100] = CardName.YURINA_CHAM
        cardNumberHashmap[101] = CardName.YURINA_ILSUM
        cardNumberHashmap[102] = CardName.YURINA_JARUCHIGI
        cardNumberHashmap[103] = CardName.YURINA_GUHAB
        cardNumberHashmap[104] = CardName.YURINA_GIBACK
        cardNumberHashmap[105] = CardName.YURINA_APDO
        cardNumberHashmap[106] = CardName.YURINA_GIYENBANJO
        cardNumberHashmap[107] = CardName.YURINA_WOLYUNGNACK
        cardNumberHashmap[108] = CardName.YURINA_POBARAM
        cardNumberHashmap[109] = CardName.YURINA_JJOCKBAE
        cardNumberHashmap[110] = CardName.YURINA_JURUCK
        cardNumberHashmap[111] = CardName.YURINA_NAN_TA
        cardNumberHashmap[112] = CardName.YURINA_BEAN_BULLET
        cardNumberHashmap[113] = CardName.YURINA_NOT_COMPLETE_POBARAM

        cardNumberHashmap[200] = CardName.SAINE_DOUBLEBEGI
        cardNumberHashmap[201] = CardName.SAINE_HURUBEGI
        cardNumberHashmap[202] = CardName.SAINE_MOOGECHOO
        cardNumberHashmap[203] = CardName.SAINE_GANPA
        cardNumberHashmap[204] = CardName.SAINE_GWONYUCK
        cardNumberHashmap[205] = CardName.SAINE_CHOONGEMJUNG
        cardNumberHashmap[206] = CardName.SAINE_MOOEMBUCK
        cardNumberHashmap[207] = CardName.SAINE_YULDONGHOGEK
        cardNumberHashmap[208] = CardName.SAINE_HANGMUNGGONGJIN
        cardNumberHashmap[209] = CardName.SAINE_EMMOOSHOEBING
        cardNumberHashmap[210] = CardName.SAINE_JONGGEK
        cardNumberHashmap[211] = CardName.SAINE_SOUND_OF_ICE
        cardNumberHashmap[212] = CardName.SAINE_ACCOMPANIMENT
        cardNumberHashmap[213] = CardName.SAINE_DUET_TAN_JU_BING_MYEONG

        cardNumberHashmap[300] = CardName.HIMIKA_SHOOT
        cardNumberHashmap[301] = CardName.HIMIKA_RAPIDFIRE
        cardNumberHashmap[302] = CardName.HIMIKA_MAGNUMCANON
        cardNumberHashmap[303] = CardName.HIMIKA_FULLBURST
        cardNumberHashmap[304] = CardName.HIMIKA_BACKSTEP
        cardNumberHashmap[305] = CardName.HIMIKA_BACKDRAFT
        cardNumberHashmap[306] = CardName.HIMIKA_SMOKE
        cardNumberHashmap[307] = CardName.HIMIKA_REDBULLET
        cardNumberHashmap[308] = CardName.HIMIKA_CRIMSONZERO
        cardNumberHashmap[309] = CardName.HIMIKA_SCARLETIMAGINE
        cardNumberHashmap[310] = CardName.HIMIKA_BURMILIONFIELD
        cardNumberHashmap[311] = CardName.HIMIKA_FIRE_WAVE
        cardNumberHashmap[312] = CardName.HIMIKA_SAT_SUI
        cardNumberHashmap[313] = CardName.HIMIKA_EN_TEN_HIMIKA

        cardNumberHashmap[400] = CardName.TOKOYO_BITSUNERIGI
        cardNumberHashmap[401] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardNumberHashmap[402] = CardName.TOKOYO_RUNNINGRABIT
        cardNumberHashmap[403] = CardName.TOKOYO_POETDANCE
        cardNumberHashmap[404] = CardName.TOKOYO_FLIPFAN
        cardNumberHashmap[405] = CardName.TOKOYO_WINDSTAGE
        cardNumberHashmap[406] = CardName.TOKOYO_SUNSTAGE
        cardNumberHashmap[407] = CardName.TOKOYO_KUON
        cardNumberHashmap[408] = CardName.TOKOYO_THOUSANDBIRD
        cardNumberHashmap[409] = CardName.TOKOYO_ENDLESSWIND
        cardNumberHashmap[410] = CardName.TOKOYO_TOKOYOMOON
        cardNumberHashmap[411] = CardName.TOKOYO_FLOWING_PLAY
        cardNumberHashmap[412] = CardName.TOKOYO_SOUND_OF_SUN
        cardNumberHashmap[413] = CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG

        cardNumberHashmap[500] = CardName.OBORO_WIRE
        cardNumberHashmap[501] = CardName.OBORO_SHADOWCALTROP
        cardNumberHashmap[502] = CardName.OBORO_ZANGEKIRANBU
        cardNumberHashmap[503] = CardName.OBORO_NINJAWALK
        cardNumberHashmap[504] = CardName.OBORO_INDUCE
        cardNumberHashmap[505] = CardName.OBORO_CLONE
        cardNumberHashmap[506] = CardName.OBORO_BIOACTIVITY
        cardNumberHashmap[507] = CardName.OBORO_KUMASUKE
        cardNumberHashmap[508] = CardName.OBORO_TOBIKAGE
        cardNumberHashmap[509] = CardName.OBORO_ULOO
        cardNumberHashmap[510] = CardName.OBORO_MIKAZRA

        cardNumberHashmap[100000] = CardName.YUKIHI_YUKIHI
        cardNumberHashmap[600] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardNumberHashmap[601] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardNumberHashmap[602] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardNumberHashmap[603] = CardName.YUKIHI_SWING_SLASH_STAB
        cardNumberHashmap[604] = CardName.YUKIHI_TURN_UMBRELLA
        cardNumberHashmap[605] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardNumberHashmap[606] = CardName.YUKIHI_MAKE_CONNECTION
        cardNumberHashmap[607] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardNumberHashmap[608] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardNumberHashmap[609] = CardName.YUKIHI_CLINGY_MIND
        cardNumberHashmap[610] = CardName.YUKIHI_SWIRLING_GESTURE

        cardNumberHashmap[SHINRA_SHINRA_CARD_NUMBER] = CardName.SHINRA_SHINRA
        cardNumberHashmap[700] = CardName.SHINRA_IBLON
        cardNumberHashmap[701] = CardName.SHINRA_BANLON
        cardNumberHashmap[702] = CardName.SHINRA_KIBEN
        cardNumberHashmap[703] = CardName.SHINRA_INYONG
        cardNumberHashmap[704] = CardName.SHINRA_SEONDONG
        cardNumberHashmap[705] = CardName.SHINRA_JANGDAM
        cardNumberHashmap[706] = CardName.SHINRA_NONPA
        cardNumberHashmap[707] = CardName.SHINRA_WANJEON_NONPA
        cardNumberHashmap[708] = CardName.SHINRA_DASIG_IHAE
        cardNumberHashmap[709] = CardName.SHINRA_CHEONJI_BANBAG
        cardNumberHashmap[710] = CardName.SHINRA_SAMRA_BAN_SHO

        cardNumberHashmap[800] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardNumberHashmap[801] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardNumberHashmap[802] = CardName.HAGANE_GROUND_BREAKING
        cardNumberHashmap[803] = CardName.HAGANE_HYPER_RECOIL
        cardNumberHashmap[804] = CardName.HAGANE_WON_MU_RUYN
        cardNumberHashmap[805] = CardName.HAGANE_RING_A_BELL
        cardNumberHashmap[806] = CardName.HAGANE_GRAVITATION_FIELD
        cardNumberHashmap[807] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardNumberHashmap[808] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardNumberHashmap[809] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardNumberHashmap[810] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT

        cardNumberHashmap[900] = CardName.CHIKAGE_THROW_KUNAI
        cardNumberHashmap[901] = CardName.CHIKAGE_POISON_NEEDLE
        cardNumberHashmap[902] = CardName.CHIKAGE_TO_ZU_CHU
        cardNumberHashmap[903] = CardName.CHIKAGE_CUTTING_NECK
        cardNumberHashmap[904] = CardName.CHIKAGE_POISON_SMOKE
        cardNumberHashmap[905] = CardName.CHIKAGE_TIP_TOEING
        cardNumberHashmap[906] = CardName.CHIKAGE_MUDDLE
        cardNumberHashmap[907] = CardName.CHIKAGE_DEADLY_POISON
        cardNumberHashmap[908] = CardName.CHIKAGE_HAN_KI_POISON
        cardNumberHashmap[909] = CardName.CHIKAGE_REINCARNATION_POISON
        cardNumberHashmap[910] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
        cardNumberHashmap[995] = CardName.POISON_PARALYTIC
        cardNumberHashmap[996] = CardName.POISON_HALLUCINOGENIC
        cardNumberHashmap[997] = CardName.POISON_RELAXATION
        cardNumberHashmap[998] = CardName.POISON_DEADLY_1
        cardNumberHashmap[999] = CardName.POISON_DEADLY_2

        cardNumberHashmap[1000] = CardName.KURURU_ELEKITTEL
        cardNumberHashmap[1001] = CardName.KURURU_ACCELERATOR
        cardNumberHashmap[1002] = CardName.KURURU_KURURUOONG
        cardNumberHashmap[1003] = CardName.KURURU_TORNADO
        cardNumberHashmap[1004] = CardName.KURURU_REGAINER
        cardNumberHashmap[1005] = CardName.KURURU_MODULE
        cardNumberHashmap[1006] = CardName.KURURU_REFLECTOR
        cardNumberHashmap[1007] = CardName.KURURU_DRAIN_DEVIL
        cardNumberHashmap[1008] = CardName.KURURU_BIG_GOLEM
        cardNumberHashmap[1009] = CardName.KURURU_INDUSTRIA
        cardNumberHashmap[1010] = CardName.KURURU_DUPLICATED_GEAR_1
        cardNumberHashmap[1011] = CardName.KURURU_DUPLICATED_GEAR_2
        cardNumberHashmap[1012] = CardName.KURURU_DUPLICATED_GEAR_3
        cardNumberHashmap[1013] = CardName.KURURU_KANSHOUSOUCHI_KURURUSIK

        cardNumberHashmap[1100] = CardName.THALLYA_BURNING_STEAM
        cardNumberHashmap[1101] = CardName.THALLYA_WAVING_EDGE
        cardNumberHashmap[1102] = CardName.THALLYA_SHIELD_CHARGE
        cardNumberHashmap[1103] = CardName.THALLYA_STEAM_CANNON
        cardNumberHashmap[1104] = CardName.THALLYA_STUNT
        cardNumberHashmap[1105] = CardName.THALLYA_ROARING
        cardNumberHashmap[1106] = CardName.THALLYA_TURBO_SWITCH
        cardNumberHashmap[1107] = CardName.THALLYA_ALPHA_EDGE
        cardNumberHashmap[1108] = CardName.THALLYA_OMEGA_BURST
        cardNumberHashmap[1109] = CardName.THALLYA_THALLYA_MASTERPIECE
        cardNumberHashmap[1110] = CardName.THALLYA_JULIA_BLACKBOX
        cardNumberHashmap[1111] = CardName.FORM_YAKSHA
        cardNumberHashmap[1112] = CardName.FORM_NAGA
        cardNumberHashmap[1113] = CardName.FORM_GARUDA

        cardNumberHashmap[1200] = CardName.RAIRA_BEAST_NAIL
        cardNumberHashmap[1201] = CardName.RAIRA_STORM_SURGE_ATTACK
        cardNumberHashmap[1202] = CardName.RAIRA_REINCARNATION_NAIL
        cardNumberHashmap[1203] = CardName.RAIRA_WIND_RUN
        cardNumberHashmap[1204] = CardName.RAIRA_WISDOM_OF_STORM_SURGE
        cardNumberHashmap[1205] = CardName.RAIRA_HOWLING
        cardNumberHashmap[1206] = CardName.RAIRA_WIND_KICK
        cardNumberHashmap[1207] = CardName.RAIRA_THUNDER_WIND_PUNCH
        cardNumberHashmap[1208] = CardName.RAIRA_SUMMON_THUNDER
        cardNumberHashmap[1209] = CardName.RAIRA_WIND_CONSEQUENCE_BALL
        cardNumberHashmap[1210] = CardName.RAIRA_CIRCULAR_CIRCUIT
        cardNumberHashmap[1211] = CardName.RAIRA_WIND_ATTACK
        cardNumberHashmap[1212] = CardName.RAIRA_WIND_ZEN_KAI
        cardNumberHashmap[1213] = CardName.RAIRA_WIND_CELESTIAL_SPHERE

        cardNumberHashmap[1300] = CardName.UTSURO_WON_WOL
        cardNumberHashmap[1301] = CardName.UTSURO_BLACK_WAVE
        cardNumberHashmap[1302] = CardName.UTSURO_HARVEST
        cardNumberHashmap[1303] = CardName.UTSURO_PRESSURE
        cardNumberHashmap[1304] = CardName.UTSURO_SHADOW_WING
        cardNumberHashmap[1305] = CardName.UTSURO_SHADOW_WALL
        cardNumberHashmap[1306] = CardName.UTSURO_YUE_HOE_JU
        cardNumberHashmap[1307] = CardName.UTSURO_HOE_MYEOL
        cardNumberHashmap[1308] = CardName.UTSURO_HEO_WI
        cardNumberHashmap[1309] = CardName.UTSURO_JONG_MAL
        cardNumberHashmap[1310] = CardName.UTSURO_MA_SIG


        cardNumberHashmap[10100] = CardName.YURINA_CHAM
        cardNumberHashmap[10101] = CardName.YURINA_ILSUM
        cardNumberHashmap[10102] = CardName.YURINA_JARUCHIGI
        cardNumberHashmap[10103] = CardName.YURINA_GUHAB
        cardNumberHashmap[10104] = CardName.YURINA_GIBACK
        cardNumberHashmap[10105] = CardName.YURINA_APDO
        cardNumberHashmap[10106] = CardName.YURINA_GIYENBANJO
        cardNumberHashmap[10107] = CardName.YURINA_WOLYUNGNACK
        cardNumberHashmap[10108] = CardName.YURINA_POBARAM
        cardNumberHashmap[10109] = CardName.YURINA_JJOCKBAE
        cardNumberHashmap[10110] = CardName.YURINA_JURUCK
        cardNumberHashmap[10111] = CardName.YURINA_NAN_TA
        cardNumberHashmap[10112] = CardName.YURINA_BEAN_BULLET
        cardNumberHashmap[10113] = CardName.YURINA_NOT_COMPLETE_POBARAM

        cardNumberHashmap[10200] = CardName.SAINE_DOUBLEBEGI
        cardNumberHashmap[10201] = CardName.SAINE_HURUBEGI
        cardNumberHashmap[10202] = CardName.SAINE_MOOGECHOO
        cardNumberHashmap[10203] = CardName.SAINE_GANPA
        cardNumberHashmap[10204] = CardName.SAINE_GWONYUCK
        cardNumberHashmap[10205] = CardName.SAINE_CHOONGEMJUNG
        cardNumberHashmap[10206] = CardName.SAINE_MOOEMBUCK
        cardNumberHashmap[10207] = CardName.SAINE_YULDONGHOGEK
        cardNumberHashmap[10208] = CardName.SAINE_HANGMUNGGONGJIN
        cardNumberHashmap[10209] = CardName.SAINE_EMMOOSHOEBING
        cardNumberHashmap[10210] = CardName.SAINE_JONGGEK
        cardNumberHashmap[10211] = CardName.SAINE_SOUND_OF_ICE
        cardNumberHashmap[10212] = CardName.SAINE_ACCOMPANIMENT
        cardNumberHashmap[10213] = CardName.SAINE_DUET_TAN_JU_BING_MYEONG

        cardNumberHashmap[10300] = CardName.HIMIKA_SHOOT
        cardNumberHashmap[10301] = CardName.HIMIKA_RAPIDFIRE
        cardNumberHashmap[10302] = CardName.HIMIKA_MAGNUMCANON
        cardNumberHashmap[10303] = CardName.HIMIKA_FULLBURST
        cardNumberHashmap[10304] = CardName.HIMIKA_BACKSTEP
        cardNumberHashmap[10305] = CardName.HIMIKA_BACKDRAFT
        cardNumberHashmap[10306] = CardName.HIMIKA_SMOKE
        cardNumberHashmap[10307] = CardName.HIMIKA_REDBULLET
        cardNumberHashmap[10308] = CardName.HIMIKA_CRIMSONZERO
        cardNumberHashmap[10309] = CardName.HIMIKA_SCARLETIMAGINE
        cardNumberHashmap[10310] = CardName.HIMIKA_BURMILIONFIELD
        cardNumberHashmap[10311] = CardName.HIMIKA_FIRE_WAVE
        cardNumberHashmap[10312] = CardName.HIMIKA_SAT_SUI
        cardNumberHashmap[10313] = CardName.HIMIKA_EN_TEN_HIMIKA

        cardNumberHashmap[10400] = CardName.TOKOYO_BITSUNERIGI
        cardNumberHashmap[10401] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardNumberHashmap[10402] = CardName.TOKOYO_RUNNINGRABIT
        cardNumberHashmap[10403] = CardName.TOKOYO_POETDANCE
        cardNumberHashmap[10404] = CardName.TOKOYO_FLIPFAN
        cardNumberHashmap[10405] = CardName.TOKOYO_WINDSTAGE
        cardNumberHashmap[10406] = CardName.TOKOYO_SUNSTAGE
        cardNumberHashmap[10407] = CardName.TOKOYO_KUON
        cardNumberHashmap[10408] = CardName.TOKOYO_THOUSANDBIRD
        cardNumberHashmap[10409] = CardName.TOKOYO_ENDLESSWIND
        cardNumberHashmap[10410] = CardName.TOKOYO_TOKOYOMOON
        cardNumberHashmap[10411] = CardName.TOKOYO_FLOWING_PLAY
        cardNumberHashmap[10412] = CardName.TOKOYO_SOUND_OF_SUN
        cardNumberHashmap[10413] = CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG

        cardNumberHashmap[10500] = CardName.OBORO_WIRE
        cardNumberHashmap[10501] = CardName.OBORO_SHADOWCALTROP
        cardNumberHashmap[10502] = CardName.OBORO_ZANGEKIRANBU
        cardNumberHashmap[10503] = CardName.OBORO_NINJAWALK
        cardNumberHashmap[10504] = CardName.OBORO_INDUCE
        cardNumberHashmap[10505] = CardName.OBORO_CLONE
        cardNumberHashmap[10506] = CardName.OBORO_BIOACTIVITY
        cardNumberHashmap[10507] = CardName.OBORO_KUMASUKE
        cardNumberHashmap[10508] = CardName.OBORO_TOBIKAGE
        cardNumberHashmap[10509] = CardName.OBORO_ULOO
        cardNumberHashmap[10510] = CardName.OBORO_MIKAZRA

        cardNumberHashmap[200000] = CardName.YUKIHI_YUKIHI
        cardNumberHashmap[10600] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardNumberHashmap[10601] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardNumberHashmap[10602] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardNumberHashmap[10603] = CardName.YUKIHI_SWING_SLASH_STAB
        cardNumberHashmap[10604] = CardName.YUKIHI_TURN_UMBRELLA
        cardNumberHashmap[10605] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardNumberHashmap[10606] = CardName.YUKIHI_MAKE_CONNECTION
        cardNumberHashmap[10607] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardNumberHashmap[10608] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardNumberHashmap[10609] = CardName.YUKIHI_CLINGY_MIND
        cardNumberHashmap[10610] = CardName.YUKIHI_SWIRLING_GESTURE

        cardNumberHashmap[SHINRA_SHINRA_CARD_NUMBER] = CardName.SHINRA_SHINRA
        cardNumberHashmap[10700] = CardName.SHINRA_IBLON
        cardNumberHashmap[10701] = CardName.SHINRA_BANLON
        cardNumberHashmap[10702] = CardName.SHINRA_KIBEN
        cardNumberHashmap[10703] = CardName.SHINRA_INYONG
        cardNumberHashmap[10704] = CardName.SHINRA_SEONDONG
        cardNumberHashmap[10705] = CardName.SHINRA_JANGDAM
        cardNumberHashmap[10706] = CardName.SHINRA_NONPA
        cardNumberHashmap[10707] = CardName.SHINRA_WANJEON_NONPA
        cardNumberHashmap[10708] = CardName.SHINRA_DASIG_IHAE
        cardNumberHashmap[10709] = CardName.SHINRA_CHEONJI_BANBAG
        cardNumberHashmap[10710] = CardName.SHINRA_SAMRA_BAN_SHO

        cardNumberHashmap[10800] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardNumberHashmap[10801] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardNumberHashmap[10802] = CardName.HAGANE_GROUND_BREAKING
        cardNumberHashmap[10803] = CardName.HAGANE_HYPER_RECOIL
        cardNumberHashmap[10804] = CardName.HAGANE_WON_MU_RUYN
        cardNumberHashmap[10805] = CardName.HAGANE_RING_A_BELL
        cardNumberHashmap[10806] = CardName.HAGANE_GRAVITATION_FIELD
        cardNumberHashmap[10807] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardNumberHashmap[10808] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardNumberHashmap[10809] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardNumberHashmap[10810] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT

        cardNumberHashmap[10900] = CardName.CHIKAGE_THROW_KUNAI
        cardNumberHashmap[10901] = CardName.CHIKAGE_POISON_NEEDLE
        cardNumberHashmap[10902] = CardName.CHIKAGE_TO_ZU_CHU
        cardNumberHashmap[10903] = CardName.CHIKAGE_CUTTING_NECK
        cardNumberHashmap[10904] = CardName.CHIKAGE_POISON_SMOKE
        cardNumberHashmap[10905] = CardName.CHIKAGE_TIP_TOEING
        cardNumberHashmap[10906] = CardName.CHIKAGE_MUDDLE
        cardNumberHashmap[10907] = CardName.CHIKAGE_DEADLY_POISON
        cardNumberHashmap[10908] = CardName.CHIKAGE_HAN_KI_POISON
        cardNumberHashmap[10909] = CardName.CHIKAGE_REINCARNATION_POISON
        cardNumberHashmap[10910] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
        cardNumberHashmap[10995] = CardName.POISON_PARALYTIC
        cardNumberHashmap[10996] = CardName.POISON_HALLUCINOGENIC
        cardNumberHashmap[10997] = CardName.POISON_RELAXATION
        cardNumberHashmap[10998] = CardName.POISON_DEADLY_1
        cardNumberHashmap[10999] = CardName.POISON_DEADLY_2

        cardNumberHashmap[11000] = CardName.KURURU_ELEKITTEL
        cardNumberHashmap[11001] = CardName.KURURU_ACCELERATOR
        cardNumberHashmap[11002] = CardName.KURURU_KURURUOONG
        cardNumberHashmap[11003] = CardName.KURURU_TORNADO
        cardNumberHashmap[11004] = CardName.KURURU_REGAINER
        cardNumberHashmap[11005] = CardName.KURURU_MODULE
        cardNumberHashmap[11006] = CardName.KURURU_REFLECTOR
        cardNumberHashmap[11007] = CardName.KURURU_DRAIN_DEVIL
        cardNumberHashmap[11008] = CardName.KURURU_BIG_GOLEM
        cardNumberHashmap[11009] = CardName.KURURU_INDUSTRIA
        cardNumberHashmap[11010] = CardName.KURURU_DUPLICATED_GEAR_1
        cardNumberHashmap[11011] = CardName.KURURU_DUPLICATED_GEAR_2
        cardNumberHashmap[11012] = CardName.KURURU_DUPLICATED_GEAR_3
        cardNumberHashmap[11013] = CardName.KURURU_KANSHOUSOUCHI_KURURUSIK

        cardNumberHashmap[11100] = CardName.THALLYA_BURNING_STEAM
        cardNumberHashmap[11101] = CardName.THALLYA_WAVING_EDGE
        cardNumberHashmap[11102] = CardName.THALLYA_SHIELD_CHARGE
        cardNumberHashmap[11103] = CardName.THALLYA_STEAM_CANNON
        cardNumberHashmap[11104] = CardName.THALLYA_STUNT
        cardNumberHashmap[11105] = CardName.THALLYA_ROARING
        cardNumberHashmap[11106] = CardName.THALLYA_TURBO_SWITCH
        cardNumberHashmap[11107] = CardName.THALLYA_ALPHA_EDGE
        cardNumberHashmap[11108] = CardName.THALLYA_OMEGA_BURST
        cardNumberHashmap[11109] = CardName.THALLYA_THALLYA_MASTERPIECE
        cardNumberHashmap[11110] = CardName.THALLYA_JULIA_BLACKBOX
        cardNumberHashmap[11111] = CardName.FORM_YAKSHA
        cardNumberHashmap[11112] = CardName.FORM_NAGA
        cardNumberHashmap[11113] = CardName.FORM_GARUDA

        cardNumberHashmap[11200] = CardName.RAIRA_BEAST_NAIL
        cardNumberHashmap[11201] = CardName.RAIRA_STORM_SURGE_ATTACK
        cardNumberHashmap[11202] = CardName.RAIRA_REINCARNATION_NAIL
        cardNumberHashmap[11203] = CardName.RAIRA_WIND_RUN
        cardNumberHashmap[11204] = CardName.RAIRA_WISDOM_OF_STORM_SURGE
        cardNumberHashmap[11205] = CardName.RAIRA_HOWLING
        cardNumberHashmap[11206] = CardName.RAIRA_WIND_KICK
        cardNumberHashmap[11207] = CardName.RAIRA_THUNDER_WIND_PUNCH
        cardNumberHashmap[11208] = CardName.RAIRA_SUMMON_THUNDER
        cardNumberHashmap[11209] = CardName.RAIRA_WIND_CONSEQUENCE_BALL
        cardNumberHashmap[11210] = CardName.RAIRA_CIRCULAR_CIRCUIT
        cardNumberHashmap[11211] = CardName.RAIRA_WIND_ATTACK
        cardNumberHashmap[11212] = CardName.RAIRA_WIND_ZEN_KAI
        cardNumberHashmap[11213] = CardName.RAIRA_WIND_CELESTIAL_SPHERE

        cardNumberHashmap[11300] = CardName.UTSURO_WON_WOL
        cardNumberHashmap[11301] = CardName.UTSURO_BLACK_WAVE
        cardNumberHashmap[11302] = CardName.UTSURO_HARVEST
        cardNumberHashmap[11303] = CardName.UTSURO_PRESSURE
        cardNumberHashmap[11304] = CardName.UTSURO_SHADOW_WING
        cardNumberHashmap[11305] = CardName.UTSURO_SHADOW_WALL
        cardNumberHashmap[11306] = CardName.UTSURO_YUE_HOE_JU
        cardNumberHashmap[11307] = CardName.UTSURO_HOE_MYEOL
        cardNumberHashmap[11308] = CardName.UTSURO_HEO_WI
        cardNumberHashmap[11309] = CardName.UTSURO_JONG_MAL
        cardNumberHashmap[11310] = CardName.UTSURO_MA_SIG


        cardDataHashmap[CardName.CARD_UNNAME] = unused
        cardDataHashmap[CardName.POISON_ANYTHING] = unused

        cardDataHashmap[CardName.YURINA_CHAM] = cham
        cardDataHashmap[CardName.YURINA_ILSUM] = ilsom
        cardDataHashmap[CardName.YURINA_JARUCHIGI] = jaru_chigi
        cardDataHashmap[CardName.YURINA_GUHAB] = guhab
        cardDataHashmap[CardName.YURINA_GIBACK] = giback
        cardDataHashmap[CardName.YURINA_APDO] = apdo
        cardDataHashmap[CardName.YURINA_GIYENBANJO] = giyenbanzo
        cardDataHashmap[CardName.YURINA_WOLYUNGNACK] = wolyungnack
        cardDataHashmap[CardName.YURINA_POBARAM] = pobaram
        cardDataHashmap[CardName.YURINA_JJOCKBAE] = jjockbae
        cardDataHashmap[CardName.YURINA_JURUCK] = juruck

        cardDataHashmap[CardName.SAINE_DOUBLEBEGI] = doublebegi
        cardDataHashmap[CardName.SAINE_HURUBEGI] = hurubegi
        cardDataHashmap[CardName.SAINE_MOOGECHOO] = moogechoo
        cardDataHashmap[CardName.SAINE_GANPA] = ganpa
        cardDataHashmap[CardName.SAINE_GWONYUCK] = gwonyuck
        cardDataHashmap[CardName.SAINE_CHOONGEMJUNG] = choongemjung
        cardDataHashmap[CardName.SAINE_MOOEMBUCK] = mooembuck
        cardDataHashmap[CardName.SAINE_YULDONGHOGEK] = yuldonghogek
        cardDataHashmap[CardName.SAINE_HANGMUNGGONGJIN] = hangmunggongjin
        cardDataHashmap[CardName.SAINE_EMMOOSHOEBING] = emmooshoebing
        cardDataHashmap[CardName.SAINE_JONGGEK] = jonggek

        cardDataHashmap[CardName.HIMIKA_SHOOT] = shoot
        cardDataHashmap[CardName.HIMIKA_RAPIDFIRE] = rapidfire
        cardDataHashmap[CardName.HIMIKA_MAGNUMCANON] = magnumcanon
        cardDataHashmap[CardName.HIMIKA_FULLBURST] = fullburst
        cardDataHashmap[CardName.HIMIKA_BACKSTEP] = backstep
        cardDataHashmap[CardName.HIMIKA_BACKDRAFT] = backdraft
        cardDataHashmap[CardName.HIMIKA_SMOKE] = smoke
        cardDataHashmap[CardName.HIMIKA_REDBULLET] = redbullet
        cardDataHashmap[CardName.HIMIKA_CRIMSONZERO] = crimsonzero
        cardDataHashmap[CardName.HIMIKA_SCARLETIMAGINE] = scarletimagine
        cardDataHashmap[CardName.HIMIKA_BURMILIONFIELD] = burmilionfield

        cardDataHashmap[CardName.TOKOYO_BITSUNERIGI] = bitsunerigi
        cardDataHashmap[CardName.TOKOYO_WOOAHHANTAGUCK] = wooahhantaguck
        cardDataHashmap[CardName.TOKOYO_RUNNINGRABIT] = runningrabit
        cardDataHashmap[CardName.TOKOYO_POETDANCE] = poetdance
        cardDataHashmap[CardName.TOKOYO_FLIPFAN] = flipfan
        cardDataHashmap[CardName.TOKOYO_WINDSTAGE] = windstage
        cardDataHashmap[CardName.TOKOYO_SUNSTAGE] = sunstage
        cardDataHashmap[CardName.TOKOYO_KUON] = kuon
        cardDataHashmap[CardName.TOKOYO_THOUSANDBIRD] = thousandbird
        cardDataHashmap[CardName.TOKOYO_ENDLESSWIND] = endlesswind
        cardDataHashmap[CardName.TOKOYO_TOKOYOMOON] = tokoyomoon

        cardDataHashmap[CardName.OBORO_WIRE] = wire
        cardDataHashmap[CardName.OBORO_SHADOWCALTROP] = shadowcaltrop
        cardDataHashmap[CardName.OBORO_ZANGEKIRANBU] = zangekiranbu
        cardDataHashmap[CardName.OBORO_NINJAWALK] = ninjawalk
        cardDataHashmap[CardName.OBORO_INDUCE] = induce
        cardDataHashmap[CardName.OBORO_CLONE] = clone
        cardDataHashmap[CardName.OBORO_BIOACTIVITY] = bioactivity
        cardDataHashmap[CardName.OBORO_KUMASUKE] = kumasuke
        cardDataHashmap[CardName.OBORO_TOBIKAGE] = tobikage
        cardDataHashmap[CardName.OBORO_ULOO] = uloo
        cardDataHashmap[CardName.OBORO_MIKAZRA] = mikazra

        cardDataHashmap[CardName.YUKIHI_YUKIHI] = yukihi
        cardDataHashmap[CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE] = hiddenNeedle
        cardDataHashmap[CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = hiddenFire
        cardDataHashmap[CardName.YUKIHI_PUSH_OUT_SLASH_PULL] = pushOut
        cardDataHashmap[CardName.YUKIHI_SWING_SLASH_STAB] = swing
        cardDataHashmap[CardName.YUKIHI_TURN_UMBRELLA] = turnUmbrella
        cardDataHashmap[CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN] = backwardStep
        cardDataHashmap[CardName.YUKIHI_MAKE_CONNECTION] = makeConnection
        cardDataHashmap[CardName.YUKIHI_FLUTTERING_SNOWFLAKE] = flutteringSnowflake
        cardDataHashmap[CardName.YUKIHI_SWAYING_LAMPLIGHT] = swayingLamplight
        cardDataHashmap[CardName.YUKIHI_CLINGY_MIND] = clingyMind
        cardDataHashmap[CardName.YUKIHI_SWIRLING_GESTURE] = swirlingGesture

        cardDataHashmap[CardName.SHINRA_SHINRA] = shinra
        cardDataHashmap[CardName.SHINRA_IBLON] = iblon
        cardDataHashmap[CardName.SHINRA_BANLON] = banlon
        cardDataHashmap[CardName.SHINRA_KIBEN] = kiben
        cardDataHashmap[CardName.SHINRA_INYONG] = inyong
        cardDataHashmap[CardName.SHINRA_SEONDONG] = seondong
        cardDataHashmap[CardName.SHINRA_JANGDAM] = jangdam
        cardDataHashmap[CardName.SHINRA_NONPA] = nonpa
        cardDataHashmap[CardName.SHINRA_WANJEON_NONPA] = wanjeonNonpa
        cardDataHashmap[CardName.SHINRA_DASIG_IHAE] = dasicIhae
        cardDataHashmap[CardName.SHINRA_CHEONJI_BANBAG] = cheonjiBanBag
        cardDataHashmap[CardName.SHINRA_SAMRA_BAN_SHO] = samraBanSho

        cardDataHashmap[CardName.HAGANE_CENTRIFUGAL_ATTACK] = centrifugalAttack
        cardDataHashmap[CardName.HAGANE_FOUR_WINDED_EARTHQUAKE] = fourWindedEarthquake
        cardDataHashmap[CardName.HAGANE_GROUND_BREAKING] = groundBreaking
        cardDataHashmap[CardName.HAGANE_HYPER_RECOIL] = hyperRecoil
        cardDataHashmap[CardName.HAGANE_WON_MU_RUYN] = wonMuRuyn
        cardDataHashmap[CardName.HAGANE_RING_A_BELL] = ringABell
        cardDataHashmap[CardName.HAGANE_GRAVITATION_FIELD] = gravitationField
        cardDataHashmap[CardName.HAGANE_GRAND_SKY_HOLE_CRASH] = grandSkyHoleCrash
        cardDataHashmap[CardName.HAGANE_GRAND_BELL_MEGALOBEL] = grandBellMegalobel
        cardDataHashmap[CardName.HAGANE_GRAND_GRAVITATION_ATTRACT] = grandGravitationAttract
        cardDataHashmap[CardName.HAGANE_GRAND_MOUNTAIN_RESPECT] = grandMountainRespect

        cardDataHashmap[CardName.CHIKAGE_THROW_KUNAI] = throwKunai
        cardDataHashmap[CardName.CHIKAGE_POISON_NEEDLE] = poisonNeedle
        cardDataHashmap[CardName.CHIKAGE_TO_ZU_CHU] = toZuChu
        cardDataHashmap[CardName.CHIKAGE_CUTTING_NECK] = cuttingNeck
        cardDataHashmap[CardName.CHIKAGE_POISON_SMOKE] = poisonSmoke
        cardDataHashmap[CardName.CHIKAGE_TIP_TOEING] = tipToeing
        cardDataHashmap[CardName.CHIKAGE_MUDDLE] = muddle
        cardDataHashmap[CardName.CHIKAGE_DEADLY_POISON] = deadlyPoison
        cardDataHashmap[CardName.CHIKAGE_HAN_KI_POISON] = hankiPoison
        cardDataHashmap[CardName.CHIKAGE_REINCARNATION_POISON] = reincarnationPoison
        cardDataHashmap[CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE] = chikageWayOfLive

        cardDataHashmap[CardName.POISON_PARALYTIC] = poisonParalytic
        cardDataHashmap[CardName.POISON_HALLUCINOGENIC] = poisonHallucinogenic
        cardDataHashmap[CardName.POISON_RELAXATION] = poisonRelaxation
        cardDataHashmap[CardName.POISON_DEADLY_1] = poisonDeadly1
        cardDataHashmap[CardName.POISON_DEADLY_2] = poisonDeadly2

        cardDataHashmap[CardName.KURURU_ELEKITTEL] = elekittel
        cardDataHashmap[CardName.KURURU_ACCELERATOR] = accelerator
        cardDataHashmap[CardName.KURURU_KURURUOONG] = kururuoong
        cardDataHashmap[CardName.KURURU_TORNADO] = tornado
        cardDataHashmap[CardName.KURURU_REGAINER] = regainer
        cardDataHashmap[CardName.KURURU_MODULE] = module
        cardDataHashmap[CardName.KURURU_REFLECTOR] = reflector
        cardDataHashmap[CardName.KURURU_DRAIN_DEVIL] = drainDevil
        cardDataHashmap[CardName.KURURU_BIG_GOLEM] = bigGolem
        cardDataHashmap[CardName.KURURU_INDUSTRIA] = industria
        cardDataHashmap[CardName.KURURU_KANSHOUSOUCHI_KURURUSIK] = kanshousouchiKururusik
        cardDataHashmap[CardName.KURURU_DUPLICATED_GEAR_1] = dupliGear1
        cardDataHashmap[CardName.KURURU_DUPLICATED_GEAR_2] = dupliGear2
        cardDataHashmap[CardName.KURURU_DUPLICATED_GEAR_3] = dupliGear3

        cardDataHashmap[CardName.THALLYA_BURNING_STEAM] = burningSteam
        cardDataHashmap[CardName.THALLYA_WAVING_EDGE] = wavingEdge
        cardDataHashmap[CardName.THALLYA_SHIELD_CHARGE] = shieldCharge
        cardDataHashmap[CardName.THALLYA_STEAM_CANNON] = steamCanon
        cardDataHashmap[CardName.THALLYA_STUNT] = stunt
        cardDataHashmap[CardName.THALLYA_ROARING] = roaring
        cardDataHashmap[CardName.THALLYA_TURBO_SWITCH] = turboSwitch
        cardDataHashmap[CardName.THALLYA_ALPHA_EDGE] = alphaEdge
        cardDataHashmap[CardName.THALLYA_OMEGA_BURST] = omegaBurst
        cardDataHashmap[CardName.THALLYA_THALLYA_MASTERPIECE] = masterPiece
        cardDataHashmap[CardName.THALLYA_JULIA_BLACKBOX] = juliaBlackbox
        cardDataHashmap[CardName.FORM_GARUDA] = formGaruda
        cardDataHashmap[CardName.FORM_NAGA] = formNaga
        cardDataHashmap[CardName.FORM_YAKSHA] = formYaksha

        cardDataHashmap[CardName.RAIRA_BEAST_NAIL] = beastNail
        cardDataHashmap[CardName.RAIRA_STORM_SURGE_ATTACK] = stormSurgeAttack
        cardDataHashmap[CardName.RAIRA_REINCARNATION_NAIL] = reincarnationNail
        cardDataHashmap[CardName.RAIRA_WIND_RUN] = windRun
        cardDataHashmap[CardName.RAIRA_WISDOM_OF_STORM_SURGE] = wisdomOfStormSurge
        cardDataHashmap[CardName.RAIRA_HOWLING] = howling
        cardDataHashmap[CardName.RAIRA_WIND_KICK] = windKick
        cardDataHashmap[CardName.RAIRA_THUNDER_WIND_PUNCH] = thunderWindPunch
        cardDataHashmap[CardName.RAIRA_SUMMON_THUNDER] = summonThunder
        cardDataHashmap[CardName.RAIRA_WIND_CONSEQUENCE_BALL] = windConsequenceBall
        cardDataHashmap[CardName.RAIRA_WIND_ATTACK] = windAttack
        cardDataHashmap[CardName.RAIRA_WIND_ZEN_KAI] = windZenKai
        cardDataHashmap[CardName.RAIRA_WIND_CELESTIAL_SPHERE] = windCelestialSphere
        cardDataHashmap[CardName.RAIRA_CIRCULAR_CIRCUIT] = circularCircuit

        cardDataHashmap[CardName.UTSURO_WON_WOL] = wonwol
        cardDataHashmap[CardName.UTSURO_BLACK_WAVE] = blackWave
        cardDataHashmap[CardName.UTSURO_HARVEST] = harvest
        cardDataHashmap[CardName.UTSURO_PRESSURE] = pressure
        cardDataHashmap[CardName.UTSURO_SHADOW_WING] = shadowWing
        cardDataHashmap[CardName.UTSURO_SHADOW_WALL] = shadowWall
        cardDataHashmap[CardName.UTSURO_YUE_HOE_JU] = yueHoeJu
        cardDataHashmap[CardName.UTSURO_HOE_MYEOL] = hoeMyeol
        cardDataHashmap[CardName.UTSURO_HEO_WI] = heoWi
        cardDataHashmap[CardName.UTSURO_JONG_MAL] = jongMal
        cardDataHashmap[CardName.UTSURO_MA_SIG] = maSig

        cardDataHashmap[CardName.YURINA_NAN_TA] = nanta
        cardDataHashmap[CardName.YURINA_BEAN_BULLET] = beanBullet
        cardDataHashmap[CardName.YURINA_NOT_COMPLETE_POBARAM] = notCompletePobaram

        cardDataHashmap[CardName.SAINE_SOUND_OF_ICE] = soundOfIce
        cardDataHashmap[CardName.SAINE_ACCOMPANIMENT] = accompaniment
        cardDataHashmap[CardName.SAINE_DUET_TAN_JU_BING_MYEONG] = duetTanJuBingMyeong

        cardDataHashmap[CardName.HIMIKA_FIRE_WAVE] = fireWave
        cardDataHashmap[CardName.HIMIKA_SAT_SUI] = satSui
        cardDataHashmap[CardName.HIMIKA_EN_TEN_HIMIKA] = enTenHimika

        cardDataHashmap[CardName.TOKOYO_FLOWING_PLAY] = flowingPlay
        cardDataHashmap[CardName.TOKOYO_SOUND_OF_SUN] = soundOfSun
        cardDataHashmap[CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG] = duetChitanYangMyeong
    }

    private suspend fun selectDustToDistance(nowCommand: CommandEnum, game_status: GameStatus): Boolean{
        if(nowCommand == CommandEnum.SELECT_ONE){
            game_status.dustToDistance(1)
            return true
        }
        else if(nowCommand == CommandEnum.SELECT_TWO){
            game_status.distanceToDust(1)
            return true
        }
        return false
    }

    private val unused = CardData(CardClass.NORMAL, CardName.CARD_UNNAME, MegamiEnum.YURINA, CardType.UNDEFINED, SubType.NONE)

    private val cham = CardData(CardClass.NORMAL, CardName.YURINA_CHAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val ilsom = CardData(CardClass.NORMAL, CardName.YURINA_ILSUM, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val jaru_chigi = CardData(CardClass.NORMAL, CardName.YURINA_JARUCHIGI, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val guhab = CardData(CardClass.NORMAL, CardName.YURINA_GUHAB, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULL_POWER)
    private val giback = CardData(CardClass.NORMAL, CardName.YURINA_GIBACK, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    private val apdo = CardData(CardClass.NORMAL, CardName.YURINA_APDO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.NONE)
    private val giyenbanzo = CardData(CardClass.NORMAL, CardName.YURINA_GIYENBANJO, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val wolyungnack = CardData(CardClass.SPECIAL, CardName.YURINA_WOLYUNGNACK, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val jjockbae = CardData(CardClass.SPECIAL, CardName.YURINA_JJOCKBAE, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    private val pobaram = CardData(CardClass.SPECIAL, CardName.YURINA_POBARAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.REACTION)
    private val juruck = CardData(CardClass.SPECIAL, CardName.YURINA_JURUCK, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULL_POWER)

    private fun gulSa(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getPlayerLife(player) <= 3
    }
    private fun yurinaCardInit(){
        cham.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        ilsom.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        ilsom.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                gulSa(buff_player, buff_game_status)
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })
        jaru_chigi.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jaru_chigi.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _ ->
            if (gulSa(player, game_status)) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, _ -> true}, {_, _, attack ->
                    attack.auraPlusMinus(1)
                }))
            }
            null
        })
        guhab.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 4, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        guhab.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, buff_game_status, _ ->
                buff_game_status.getAdjustDistance(player) <= 2
            }, {_, _, madeAttack ->
                madeAttack.run {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        giback.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS, {_, _, attack -> (attack.megami != MegamiEnum.YURINA) && (attack.card_class != CardClass.SPECIAL)},
                { _, _, attack -> attack.plusMinusRange(1, true)
            }))
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET, { _, _, attack -> (attack.megami != MegamiEnum.YURINA) && (attack.card_class != CardClass.SPECIAL)},
                { _, _, attack -> attack.canNotReactNormal()
                })
            )
            null
        })
        apdo.setEnchantment(2)
        apdo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        apdo.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.YURINA_APDO, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 3,  999, Pair(1, 4), null, MegamiEnum.YURINA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                )) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        giyenbanzo.setEnchantment(4)
        giyenbanzo.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, { buff_player, buff_game_status, _ -> gulSa(buff_player, buff_game_status)}, { _, _, madeAttack ->
                if(madeAttack.megami != MegamiEnum.YURINA) madeAttack.run {
                    Chogek(); auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        wolyungnack.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 4,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wolyungnack.setSpecial(7)
        pobaram.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 2, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        pobaram.setSpecial(3)
        pobaram.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, _, _, reactedAttack ->
            reactedAttack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-2)
                }))
            null
        })
        pobaram.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION){_, _, _, _->
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.dustToAura(player, 5)
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {_, cardNumber, beforeLife,
                afterLife, _, _ ->
                if(beforeLife > 3 && afterLife < 3){
                    game_status.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })
        jjockbae.setSpecial(2)
        juruck.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 5, 5,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        juruck.setSpecial(5)
        juruck.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, player, game_status, _ ->
            if(gulSa(player, game_status)) 1
            else 0
        })
    }

    private val doublebegi = CardData(CardClass.NORMAL, CardName.SAINE_DOUBLEBEGI, MegamiEnum.SAINE, CardType.ATTACK, SubType.NONE)
    private val hurubegi = CardData(CardClass.NORMAL, CardName.SAINE_HURUBEGI, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val moogechoo = CardData(CardClass.NORMAL, CardName.SAINE_MOOGECHOO, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val ganpa = CardData(CardClass.NORMAL, CardName.SAINE_GANPA, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)
    private val gwonyuck = CardData(CardClass.NORMAL, CardName.SAINE_GWONYUCK, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private val choongemjung = CardData(CardClass.NORMAL, CardName.SAINE_CHOONGEMJUNG, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.REACTION)
    private val mooembuck = CardData(CardClass.NORMAL, CardName.SAINE_MOOEMBUCK, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val yuldonghogek = CardData(CardClass.SPECIAL, CardName.SAINE_YULDONGHOGEK, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)
    private val hangmunggongjin = CardData(CardClass.SPECIAL, CardName.SAINE_HANGMUNGGONGJIN, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)
    private val emmooshoebing = CardData(CardClass.SPECIAL, CardName.SAINE_EMMOOSHOEBING, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)
    private val jonggek = CardData(CardClass.SPECIAL, CardName.SAINE_JONGGEK, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)

    private fun palSang(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getPlayerAura(player) <= 1
    }
    private fun saineCardInit(){
        doublebegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        doublebegi.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(palSang(player, game_status)){
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_DOUBLEBEGI, card_number, CardClass.NORMAL,
                        DistanceType.CONTINUOUS, 2,  1, Pair(4, 5), null, MegamiEnum.SAINE,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })
        hurubegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        moogechoo.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        moogechoo.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.dustToDistance(1)
            }
            null
        })
        ganpa.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_REACTABLE) {_, player, game_status, _ ->
            if(palSang(player, game_status)) 1
            else 0
        })
        ganpa.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, 203)
                if(selectDustToDistance(nowCommand, game_status)) break
            }
            null
        })
        gwonyuck.setEnchantment(2)
        gwonyuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE) {_, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        gwonyuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) {_, _, _, _ ->
            1
        })
        choongemjung.setEnchantment(1)
        choongemjung.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_CHANGE) { card_number, _, _, reactedAttack ->
            reactedAttack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-1)
                }))
            null
        })
        choongemjung.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_CHOONGEMJUNG, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 1,  999, Pair(0, 10), null, MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        choongemjung.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        mooembuck.setEnchantment(5)
        //-1 means every nap token can use as aura
        mooembuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE) {_, _, _, _ ->
            null
        })
        yuldonghogek.setSpecial(6)
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_YULDONGHOGEK, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 1,  1, Pair(3, 4), null, MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number,  player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_YULDONGHOGEK, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 1,  1, Pair(4, 5), null, MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_YULDONGHOGEK, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 2,  2, Pair(3, 5), null, MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))) {
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        hangmunggongjin.setSpecial(8)
        hangmunggongjin.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) {card_number, player, game_status, _->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card ->
                (card.card_data.card_name == CardName.SAINE_HANGMUNGGONGJIN)}, {cost ->
                cost - game_status.getPlayerAura(player.opposite())
            }))
            null
        })
        hangmunggongjin.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.auraToDistance(player.opposite(), 2)
            null
        })
        emmooshoebing.setSpecial(2)
        emmooshoebing.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        emmooshoebing.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, _, _, reactedAttack ->
            reactedAttack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-1); attack.lifePlusMinus(-1)
                }))
            null
        })
        //return 1 means it can be return 0 means it can't be return
        emmooshoebing.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayerAura(player) <= 1) 1
            else 0
        })
        jonggek.setSpecial(5)
        jonggek.setAttack(DistanceType.CONTINUOUS, Pair(1, 5), null, 5, 5,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jonggek.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, _, _, reactedAttack->
            if(reactedAttack != null && reactedAttack.card_class == CardClass.SPECIAL) 1
            else 0
        })
    }

    private fun yeonwhaAttack(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.logger.playerUseCardNumber(player) >= 2
    }

    private fun yeonwha(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.logger.playerUseCardNumber(player) >= 3
    }

    private val shoot = CardData(CardClass.NORMAL, CardName.HIMIKA_SHOOT, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val rapidfire = CardData(CardClass.NORMAL, CardName.HIMIKA_RAPIDFIRE, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val magnumcanon = CardData(CardClass.NORMAL, CardName.HIMIKA_MAGNUMCANON, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val fullburst = CardData(CardClass.NORMAL, CardName.HIMIKA_FULLBURST, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.FULL_POWER)
    private val backstep = CardData(CardClass.NORMAL, CardName.HIMIKA_BACKSTEP, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)
    private val backdraft = CardData(CardClass.NORMAL, CardName.HIMIKA_BACKDRAFT, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)
    private val smoke = CardData(CardClass.NORMAL, CardName.HIMIKA_SMOKE, MegamiEnum.HIMIKA, CardType.ENCHANTMENT, SubType.NONE)
    private val redbullet = CardData(CardClass.SPECIAL, CardName.HIMIKA_REDBULLET, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val crimsonzero = CardData(CardClass.SPECIAL, CardName.HIMIKA_CRIMSONZERO, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val scarletimagine = CardData(CardClass.SPECIAL, CardName.HIMIKA_SCARLETIMAGINE, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)
    private val burmilionfield = CardData(CardClass.SPECIAL, CardName.HIMIKA_BURMILIONFIELD, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)

    private fun himikaCardInit(){
        shoot.setAttack(DistanceType.CONTINUOUS, Pair(4, 10), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        rapidfire.setAttack(DistanceType.CONTINUOUS, Pair(6, 8), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        rapidfire.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {conditionPlayer, conditionGameStatus, _ -> yeonwhaAttack(conditionPlayer, conditionGameStatus)},
                {_, _, attack ->
                    attack.auraPlusMinus(1); attack.lifePlusMinus(1)
                }))
            null
        }))
        magnumcanon.setAttack(DistanceType.CONTINUOUS, Pair(5, 8), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        magnumcanon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.lifeToDust(player, 1)){
                game_status.gameEnd(null, player)
            }
            null
        })
        fullburst.setAttack(DistanceType.CONTINUOUS, Pair(5, 9), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        fullburst.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, _, madeAttack ->
                madeAttack.setBothSideDamage()
            }))
            null
        }))
        backstep.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _ ->
            game_status.drawCard(player, 1)
            null
        })
        backstep.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        backdraft.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })
        backdraft.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(yeonwha(player, game_status)){
                game_status.addThisTurnAttackBuff(player, Buff(card_number,1, BufTag.PLUS_MINUS, {_, _, attack -> (attack.megami != MegamiEnum.HIMIKA) && (attack.editedAuraDamage != 999)},
                    { _, _, attack -> attack.run{
                        auraPlusMinus(1); lifePlusMinus(1)
                    }
                    }))
            }
            null
        })
        smoke.setEnchantment(3)
        //FORBID_MOVE_TOKEN return FromLocationEnum * 100 + ToLocationEnum (if anywhere it will be 99)
        smoke.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_MOVE_TOKEN){_, _, _, _ ->
            LocationEnum.DISTANCE.real_number * 100 + 99
            null
        })
        redbullet.setAttack(DistanceType.CONTINUOUS, Pair(5, 10), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        redbullet.setSpecial(0)
        crimsonzero.setAttack(DistanceType.CONTINUOUS, Pair(0, 2), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        crimsonzero.setSpecial(5)
        crimsonzero.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, _, attack ->
                attack.setBothSideDamage()
            }))
            null
        }))
        crimsonzero.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(game_status.getAdjustDistance(player) == 0){
                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, {_, _, _ ->
                    true }, {_, _, attack ->
                    attack.canNotReact()
                }))
            }
            null
        }))
        scarletimagine.setSpecial(3)
        scarletimagine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _->
            game_status.drawCard(player, 3)
            null
        })
        scarletimagine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            game_status.coverCard(player, player, CardName.HIMIKA_SCARLETIMAGINE.toCardNumber(true))
            null
        })
        burmilionfield.setSpecial(2)
        burmilionfield.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _->
            if(yeonwha(player, game_status)){
                game_status.dustToDistance(2)
            }
            null
        })
        burmilionfield.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayerHandSize(player) == 0) 1
            else 0
        })
    }

    private fun kyochi(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getConcentration(player) == 2
    }

    private val bitsunerigi = CardData(CardClass.NORMAL, CardName.TOKOYO_BITSUNERIGI, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val wooahhantaguck = CardData(CardClass.NORMAL, CardName.TOKOYO_WOOAHHANTAGUCK, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.REACTION)
    private val runningrabit = CardData(CardClass.NORMAL, CardName.TOKOYO_RUNNINGRABIT, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)
    private val poetdance = CardData(CardClass.NORMAL, CardName.TOKOYO_POETDANCE, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.REACTION)
    private val flipfan = CardData(CardClass.NORMAL, CardName.TOKOYO_FLIPFAN, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val windstage = CardData(CardClass.NORMAL, CardName.TOKOYO_WINDSTAGE, MegamiEnum.TOKOYO, CardType.ENCHANTMENT, SubType.NONE)
    private val sunstage = CardData(CardClass.NORMAL, CardName.TOKOYO_SUNSTAGE, MegamiEnum.TOKOYO, CardType.ENCHANTMENT, SubType.NONE)
    private val kuon = CardData(CardClass.SPECIAL, CardName.TOKOYO_KUON, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.REACTION)
    private val thousandbird = CardData(CardClass.SPECIAL, CardName.TOKOYO_THOUSANDBIRD, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val endlesswind = CardData(CardClass.SPECIAL, CardName.TOKOYO_ENDLESSWIND, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val tokoyomoon = CardData(CardClass.SPECIAL, CardName.TOKOYO_TOKOYOMOON, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)

    private fun tokoyoCardInit(){
        bitsunerigi.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        bitsunerigi.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            if(kyochi(player, game_status)){
                game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_TOP, card_number)
            }
            null
        })
        wooahhantaguck.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wooahhantaguck.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, reactedAttack ->
            if(kyochi(player, game_status) && reactedAttack?.card_class != CardClass.SPECIAL){
                reactedAttack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })
        runningrabit.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            if(game_status.getAdjustDistance(player)<= 3){
                game_status.dustToDistance(2)
            }
            null
        })
        poetdance.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        poetdance.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, 403)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.flareToAura(player, player, 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToDistance(player, 1)
                    break
                }
            }
            null
        })
        flipfan.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            while (true){
                val set = mutableSetOf<Int>()
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 404
                ) { true }?: break
                set.addAll(list)
                if (set.size <= 2){
                    if(list.isNotEmpty()){
                        for (cardNumber in list){
                            game_status.popCardFrom(player, cardNumber, LocationEnum.DISCARD, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                            }?: game_status.popCardFrom(player, cardNumber, LocationEnum.COVER_CARD, false)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                            }
                        }
                    }
                    break
                }
            }
            null
        })
        flipfan.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            game_status.dustToAura(player, 2)
            null
        })
        windstage.setEnchantment(2)
        windstage.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.distanceToAura(player, 2)
            null
        })
        windstage.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.auraToDistance(player, 2)
            null
        })
        sunstage.setEnchantment(2)
        sunstage.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION){_, _, _, _->
            null
        })
        sunstage.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.setConcentration(player, 2)
            null
        })
        sunstage.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.TOKOYO_SUNSTAGE, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 999,  1, Pair(3, 6), null, MegamiEnum.TOKOYO,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        kuon.setSpecial(5)
        kuon.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kuon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {card_number, _, _, reactedAttack ->
            reactedAttack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET, { _, _, _ ->
                true
            }, { _, _, attack ->
                attack.makeNotValid()
            }))
            null
        })
        thousandbird.setSpecial(2)
        thousandbird.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        thousandbird.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RECONSTRUCT) {_, player, game_status, _ ->
            game_status.deckReconstruct(player, false)
            null
        })
        endlesswind.setSpecial(1)
        endlesswind.setAttack(DistanceType.CONTINUOUS, Pair(3, 8), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        endlesswind.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player.opposite(), player.opposite(), listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 409)
                { card -> card.card_data.card_type != CardType.ATTACK && card.card_data.canDiscard}
                if(list == null){
                    game_status.showSome(player.opposite(), CommandEnum.SHOW_HAND_ALL_YOUR, -1)
                    break
                }
                else{
                    if (list.size == 1){
                        val card = game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, true)?: continue
                        game_status.insertCardTo(player.opposite(), card, LocationEnum.DISCARD, true)
                        break
                    }
                }
            }
            null
        })
        endlesswind.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(kyochi(player, game_status)) 1
            else 0
        })
        tokoyomoon.setSpecial(2)
        tokoyomoon.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            game_status.setConcentration(player, 2)
            game_status.setConcentration(player.opposite(), 0)
            game_status.setShrink(player.opposite())
            null
        })
    }

    private val wire = CardData(CardClass.NORMAL, CardName.OBORO_WIRE, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val shadowcaltrop = CardData(CardClass.NORMAL, CardName.OBORO_SHADOWCALTROP, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val zangekiranbu = CardData(CardClass.NORMAL, CardName.OBORO_ZANGEKIRANBU, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULL_POWER)
    private val ninjawalk = CardData(CardClass.NORMAL, CardName.OBORO_NINJAWALK, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.NONE)
    private val induce = CardData(CardClass.NORMAL, CardName.OBORO_INDUCE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.REACTION)
    private val clone = CardData(CardClass.NORMAL, CardName.OBORO_CLONE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val bioactivity = CardData(CardClass.NORMAL, CardName.OBORO_BIOACTIVITY, MegamiEnum.OBORO, CardType.ENCHANTMENT, SubType.NONE)
    private val kumasuke = CardData(CardClass.SPECIAL, CardName.OBORO_KUMASUKE, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULL_POWER)
    private val tobikage = CardData(CardClass.SPECIAL, CardName.OBORO_TOBIKAGE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.REACTION)
    private val uloo = CardData(CardClass.SPECIAL, CardName.OBORO_ULOO, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.NONE)
    private val mikazra = CardData(CardClass.SPECIAL, CardName.OBORO_MIKAZRA, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)

    private fun oboroCardInit(){
        wire.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wire.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION) {_, _, _, _->
            null
        })
        shadowcaltrop.setAttack(DistanceType.CONTINUOUS, Pair(2, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        shadowcaltrop.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION) {_, _, _, _->
            null
        })
        shadowcaltrop.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if (game_status.logger.checkThisCardUseInCover(player, card_number)){
                game_status.coverCard(player.opposite(), player, CardName.OBORO_SHADOWCALTROP.toCardNumber(true))
            }
            null
        })
        zangekiranbu.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        zangekiranbu.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if (game_status.logger.checkThisTurnGetAuraDamage(player.opposite())) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true
                }, {_, _, attack ->
                    attack.apply {
                        auraPlusMinus(1); lifePlusMinus(1)
                    } }))
            }
            null
        })
        ninjawalk.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        ninjawalk.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        ninjawalk.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if (game_status.logger.checkThisCardUseInCover(player, card_number)){
                game_status.useInstallationOnce(player)
            }
            null
        })
        induce.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        induce.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, 504)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.distanceToAura(player.opposite(), 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToFlare(player.opposite(), player.opposite(), 1)
                    break
                }
            }
            null
        })
        clone.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 505)
                {card -> card.card_data.sub_type != SubType.FULL_POWER}
                if(selected == null){
                    game_status.showSome(player, CommandEnum.SHOW_COVER_YOUR, -1)
                    break
                }
                else{
                    if(selected.size == 1){
                        val selectNumber = selected[0]
                        val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                        game_status.useCardFrom(player, card, LocationEnum.COVER_CARD, false, null,
                            isCost = true, isConsume = true)
                        if(game_status.getEndTurn(player)) break
                        val secondCard = game_status.getCardFrom(player, selectNumber, LocationEnum.DISCARD)?: break
                        game_status.useCardFrom(player, secondCard, LocationEnum.DISCARD, false, null,
                            isCost = true, isConsume = true)
                        break
                    }
                }
            }
            null
        })
        bioactivity.setEnchantment(4)
        bioactivity.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        bioactivity.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        bioactivity.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RETURN_OTHER_CARD) {_, player, game_status, _ ->
            while(true) {
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    506) { true }?: break
                if(selected.size == 1){
                    game_status.returnSpecialCard(player, selected[0])
                    break
                }
            }
            null
        })
        kumasuke.setSpecial(4)
        kumasuke.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kumasuke.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            for (i in 1..game_status.getPlayer(player).cover_card.size){
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.OBORO_KUMASUKE, card_number, CardClass.NORMAL,
                        DistanceType.CONTINUOUS, 2,  2, Pair(3, 4), null, MegamiEnum.OBORO,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))){
                    game_status.afterMakeAttack(card_number, player, null)
                }

            }
            null
        })
        tobikage.setSpecial(4)
        tobikage.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{_, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 508)
                {card -> card.card_data.sub_type != SubType.FULL_POWER}?: run {
                    game_status.showSome(player, CommandEnum.SHOW_COVER_YOUR, -1)
                    return@ret null
                }
                if(selected.size == 1){
                    val selectNumber = selected[0]
                    val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                    game_status.useCardFrom(player, card, LocationEnum.COVER_CARD, true, react_attack,
                        isCost = true, isConsume = true)
                    break
                }
            }
            null
        })
        uloo.setSpecial(4)
        uloo.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.INSTALLATION_INFINITE, null))
        mikazra.setSpecial(0)
        mikazra.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        mikazra.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.dustToFlare(player, 1)
            null
        })
        mikazra.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayerFlare(player) == 0) 1
            else 0
        })
    }

    private val yukihi = CardData(CardClass.SPECIAL, CardName.YUKIHI_YUKIHI, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.NONE)

    private val hiddenNeedle = CardData(CardClass.NORMAL, CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val hiddenFire = CardData(CardClass.NORMAL, CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val pushOut = CardData(CardClass.NORMAL, CardName.YUKIHI_PUSH_OUT_SLASH_PULL, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val swing = CardData(CardClass.NORMAL, CardName.YUKIHI_SWING_SLASH_STAB, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.FULL_POWER)
    private val turnUmbrella = CardData(CardClass.NORMAL, CardName.YUKIHI_TURN_UMBRELLA, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.NONE)
    private val backwardStep = CardData(CardClass.NORMAL, CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.REACTION)
    private val makeConnection = CardData(CardClass.NORMAL, CardName.YUKIHI_MAKE_CONNECTION, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.NONE)
    private val flutteringSnowflake = CardData(CardClass.SPECIAL, CardName.YUKIHI_FLUTTERING_SNOWFLAKE, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val swayingLamplight = CardData(CardClass.SPECIAL, CardName.YUKIHI_SWAYING_LAMPLIGHT, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val clingyMind = CardData(CardClass.SPECIAL, CardName.YUKIHI_CLINGY_MIND, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val swirlingGesture = CardData(CardClass.SPECIAL, CardName.YUKIHI_SWIRLING_GESTURE, MegamiEnum.YUKIHI, CardType.BEHAVIOR, SubType.REACTION)

    private fun yukihiCardInit(){
        yukihi.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { _, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, 200000)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.changeUmbrella(player)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    //not change
                    break
                }
            }
            null
        })
        hiddenNeedle.umbrellaMark = true
        hiddenNeedle.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 3, 1)
        hiddenNeedle.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 2)
        hiddenFire.umbrellaMark = true
        hiddenFire.setAttackFold(DistanceType.CONTINUOUS, Pair(5, 6), null, 1, 1)
        hiddenFire.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 1)
        hiddenFire.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.HAND, card_number)
            game_status.changeUmbrella(player)
            null
        })
        pushOut.umbrellaMark = true
        pushOut.setAttackFold(DistanceType.CONTINUOUS, Pair(2, 5), null, 1, 1)
        pushOut.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 1)
        pushOut.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, 602)
                if(selectDustToDistance(nowCommand, game_status)) break
            }
            null
        })
        pushOut.addTextUnfold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, _, game_status, _->
            game_status.distanceToDust(2)
            null
        })
        swing.umbrellaMark = true
        swing.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 5, 999)
        swing.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 999, 2)
        turnUmbrella.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.SHOW_HAND_WHEN_CHANGE_UMBRELLA) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, 604)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.showSome(player, CommandEnum.SHOW_HAND_YOUR, card_number)
                    game_status.dustToAura(player, 1)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    //not show
                    break
                }
            }
            null
        })
        backwardStep.umbrellaMark = true
        backwardStep.addTextFold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, _, game_status, _ ->
            game_status.dustToDistance(1)
            null
        })
        backwardStep.addTextUnfold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, _, game_status, _ ->
            game_status.distanceToDust(1)
            null
        })
        makeConnection.setEnchantment(2)
        makeConnection.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.getUmbrella(player) == Umbrella.UNFOLD) game_status.dustToDistance(1)
            else game_status.distanceToDust(1)
            null
        })
        makeConnection.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.getUmbrella(player) == Umbrella.UNFOLD) game_status.distanceToDust(1)
            else game_status.dustToDistance(1)
            null
        })
        flutteringSnowflake.umbrellaMark = true
        flutteringSnowflake.setSpecial(2)
        flutteringSnowflake.setAttackFold(DistanceType.CONTINUOUS, Pair(3, 5), null, 3, 1)
        flutteringSnowflake.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 0, 0)
        flutteringSnowflake.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateUmbrellaListener(player, Listener(player, card_number){_, cardNumber, _, _, _, _ ->
                game_status.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
        swayingLamplight.umbrellaMark = true
        swayingLamplight.setSpecial(5)
        swayingLamplight.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 0, 0)
        swayingLamplight.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 0), null, 4, 5)
        clingyMind.setSpecial(3)
        clingyMind.setEnchantment(7)
        clingyMind.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number, 1, RangeBufTag.CHANGE_IMMEDIATE, { _, _, _ -> true}, {_, _, madeAttack ->
                if(madeAttack.megami == MegamiEnum.YUKIHI){
                    when{
                        madeAttack.kururuChangeRangeUpper -> {
                            when(madeAttack.card_name){
                                CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                                    madeAttack.run { addRange(Pair(1, 3)); addRange(Pair(5, 7))}
                                }
                                CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                                    madeAttack.run { addRange(Pair(1, 3)); addRange(Pair(6, 7))}
                                }
                                CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                                    madeAttack.run { addRange(Pair(1, 3)); addRange(Pair(3, 6))}
                                }
                                CardName.YUKIHI_SWING_SLASH_STAB -> {
                                    madeAttack.run { addRange(Pair(1, 3)); addRange(Pair(5, 7))}
                                }
                                CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                                    madeAttack.run { addRange(Pair(1, 2)); addRange(Pair(4, 6))}
                                }
                                CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                                    madeAttack.run { addRange(Pair(1, 3)); addRange(Pair(5, 7))}
                                }
                                else -> {}
                            }
                        }
                        madeAttack.kururuChangeRangeUnder -> {
                            when(madeAttack.card_name){
                                CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                                    madeAttack.run { addRange(Pair(0, 1)); addRange(Pair(3, 5))}
                                }
                                CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                                    madeAttack.run { addRange(Pair(0, 1)); addRange(Pair(4, 5))}
                                }
                                CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                                    madeAttack.run { addRange(Pair(0, 1)); addRange(Pair(1, 4))}
                                }
                                CardName.YUKIHI_SWING_SLASH_STAB -> {
                                    madeAttack.run { addRange(Pair(0, 1)); addRange(Pair(3, 5))}
                                }
                                CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                                    madeAttack.run { addRange(Pair(0, 0)); addRange(Pair(2, 4))}
                                }
                                CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                                    madeAttack.run { addRange(Pair(0, 0)); addRange(Pair(3, 5))}
                                }
                                else -> {}
                            }
                        }
                        else -> {
                            when(madeAttack.card_name){
                                CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                                    madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(4, 6))}
                                }
                                CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                                    madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(5, 6))}
                                }
                                CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                                    madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(2, 5))}
                                }
                                CardName.YUKIHI_SWING_SLASH_STAB -> {
                                    madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(4, 6))}
                                }
                                CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                                    madeAttack.run { addRange(Pair(0, 2)); addRange(Pair(3, 6))}
                                }
                                CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                                    madeAttack.run { addRange(Pair(0, 0)); addRange(Pair(4, 6))}
                                }
                                else -> {}
                            }
                        }
                    }
                }

            }))
            null
        })
        swirlingGesture.setSpecial(1)
        swirlingGesture.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.changeUmbrella(player)
            game_status.dustToAura(player, 1)
            null
        })
    }

    private suspend fun setStratagemByUser(game_status: GameStatus, player: PlayerEnum, card_number: Int){
        while(true){
            val nowCommand = game_status.receiveCardEffectSelect(player, SHINRA_SHINRA_CARD_NUMBER)
            if(nowCommand == CommandEnum.SELECT_ONE){
                game_status.setStratagem(player, Stratagem.SHIN_SAN)
                break

            }
            else if(nowCommand == CommandEnum.SELECT_TWO){
                game_status.setStratagem(player, Stratagem.GUE_MO)
                break
            }
        }
    }

    private val shinra = CardData(CardClass.SPECIAL, CardName.SHINRA_SHINRA, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val iblon = CardData(CardClass.NORMAL, CardName.SHINRA_IBLON, MegamiEnum.SHINRA, CardType.ATTACK, SubType.NONE)
    private val banlon = CardData(CardClass.NORMAL, CardName.SHINRA_BANLON, MegamiEnum.SHINRA, CardType.ATTACK, SubType.REACTION)
    private val kiben = CardData(CardClass.NORMAL, CardName.SHINRA_KIBEN, MegamiEnum.SHINRA, CardType.ATTACK, SubType.FULL_POWER)
    private val inyong = CardData(CardClass.NORMAL, CardName.SHINRA_INYONG, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val seondong = CardData(CardClass.NORMAL, CardName.SHINRA_SEONDONG, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.REACTION)
    private val jangdam = CardData(CardClass.NORMAL, CardName.SHINRA_JANGDAM, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.NONE)
    private val nonpa = CardData(CardClass.NORMAL, CardName.SHINRA_NONPA, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.NONE)
    private val wanjeonNonpa = CardData(CardClass.SPECIAL, CardName.SHINRA_WANJEON_NONPA, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val dasicIhae = CardData(CardClass.SPECIAL, CardName.SHINRA_DASIG_IHAE, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.NONE)
    private val cheonjiBanBag = CardData(CardClass.SPECIAL, CardName.SHINRA_CHEONJI_BANBAG, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val samraBanSho = CardData(CardClass.SPECIAL, CardName.SHINRA_SAMRA_BAN_SHO, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.NONE)

    private fun shinraCardInit(){
        shinra.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).stratagem == null){
                setStratagemByUser(game_status, player, card_number)
            }
            null
        })
        iblon.setAttack(DistanceType.CONTINUOUS, Pair(2, 7), null, 2, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        iblon.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.EFFECT_INSTEAD_DAMAGE){_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).normalCardDeck.size >= 2){
                var index = 0
                val cardOne = game_status.getCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP)!!
                if(cardOne.card_data.canCover){
                    game_status.popCardFrom(player.opposite(), cardOne.card_number, LocationEnum.DECK, false)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                    }
                }
                else index = 1
                val cardTwo = game_status.getCardFrom(player.opposite(), index, LocationEnum.YOUR_DECK_TOP)!!
                if(cardTwo.card_data.canCover){
                    game_status.popCardFrom(player.opposite(), cardTwo.card_number, LocationEnum.DECK, false)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                    }
                }
                1
            }
            else{
                0
            }
        })
        banlon.setAttack(DistanceType.CONTINUOUS, Pair(2, 7), null, 1, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        banlon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_NO_DAMAGE){card_number, _, _, reactedAttack ->
            reactedAttack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET, { player, game_status, attack ->
                val damage = attack.getDamage(game_status, player,  game_status.getPlayerAttackBuff(player))
                damage.first >= 3
            }, { _, _, attack ->
                attack.makeNoDamage()
            }))
            null
        })
        banlon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DRAW_CARD){_, player, game_status, _ ->
            game_status.drawCard(player.opposite(), 1)
            null
        })
        kiben.setAttack(DistanceType.CONTINUOUS, Pair(3, 8), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kiben.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RUN_STRATAGEM){card_number, player, game_status, _ ->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    var index = 0
                    for(i in 1..3){
                        game_status.getPlayer(player.opposite()).normalCardDeck.getOrNull(index)?.let let@{
                            if(it.card_data.canCover) {
                                val card = game_status.popCardFrom(player.opposite(), it.card_number, LocationEnum.DECK, false)?: return@let
                                game_status.insertCardTo(player.opposite(), card, LocationEnum.COVER_CARD, false)
                            }
                            else {
                                index += 1
                                return@let
                            }

                        }?: break
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    while (true){
                        val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 702)
                        {true}?: break
                        if (list.isNotEmpty()){
                            if (list.size == 1){
                                game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD, true)?.let {
                                    game_status.useCardFrom(player, it, LocationEnum.DISCARD, false, null,
                                        isCost = true, isConsume = true)
                                }?: continue
                                break
                            }
                        }
                        else{
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        inyong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _->
            while(true){
                val selected = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 703)
                {true} ?: break
                if(selected.size == 0) break
                if(selected.size == 1){
                    val selectNumber = selected[0]
                    val card = game_status.getCardFrom(player.opposite(), selectNumber, LocationEnum.HAND)?: continue
                    if(card.card_data.card_type != CardType.ATTACK) continue
                    while(true){
                        val nowCommand = game_status.receiveCardEffectSelect(player, 703)
                        if(nowCommand == CommandEnum.SELECT_ONE){
                            game_status.useCardFrom(player, card, LocationEnum.OTHER_HAND, false, null,
                                isCost = true, isConsume = true)
                            break
                        }
                        else if(nowCommand == CommandEnum.SELECT_TWO){
                            game_status.insertCardTo(player.opposite(), game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, true)!!,
                                LocationEnum.COVER_CARD, true)
                            break
                        }
                    }
                    if(card.card_data.sub_type == SubType.FULL_POWER) game_status.setEndTurn(player, true)
                    break
                }
            }
            null
        })
        seondong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) {_, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.dustToDistance(1)
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    game_status.distanceToAura(player.opposite(), 1)
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        jangdam.setEnchantment(2)
        jangdam.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RUN_STRATAGEM) {card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.addConcentration(player)
                    game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let{
                        game_status.insertCardTo(it.player, it, LocationEnum.YOUR_DECK_TOP, true)
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    if (game_status.getPlayer(player.opposite()).hand.size <= 1){
                        game_status.setShrink(player.opposite())
                        game_status.drawCard(player.opposite(), 3)
                        while (true){
                            val list = game_status.selectCardFrom(player.opposite(), player.opposite(), listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 705, 2)
                            {true}?: break
                            for (cardNumber in list){
                                game_status.popCardFrom(player.opposite(), cardNumber, LocationEnum.HAND, true)?.let {
                                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD, true)
                                }
                            }
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        nonpa.setEnchantment(4)
        nonpa.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.SEAL_CARD) {card_number, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 706)
                {true}?: break
                if (list.size == 1){
                    game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD, true)?.let {
                        game_status.getPlayer(player).sealInformation[it.card_number] = card_number
                        game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                    }
                    break
                }
            }
            null
        })
        nonpa.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.SEAL_CARD) {card_number, player, game_status, _ ->
            val player1 = game_status.getPlayer(player)
            val player2 = game_status.getPlayer(player.opposite())
            for(sealCardNumber in player1.sealZone.keys){
                if(player1.sealInformation[sealCardNumber] == card_number){
                    game_status.popCardFrom(player, sealCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        game_status.insertCardTo(it.player, it, LocationEnum.DISCARD, true)
                    }
                }
            }
            for(sealCardNumber in player2.sealZone.keys){
                if(player2.sealInformation[sealCardNumber] == card_number){
                    game_status.popCardFrom(player.opposite(), sealCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        game_status.insertCardTo(it.player, it, LocationEnum.DISCARD, true)
                    }
                }
            }
            null
        })
        wanjeonNonpa.setSpecial(2)
        wanjeonNonpa.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.SEAL_CARD){card_number, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).discard.size != 0){
                while (true){
                    val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 707)
                    {true}?: break
                    if (list.size == 1){
                        game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD, true)?.let {
                            game_status.getPlayer(player).sealInformation[it.card_number] = card_number
                            game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                        }
                        break
                    }
                }
            }
            null
        })
        dasicIhae.setSpecial(2)
        dasicIhae.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) {card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    while (true){
                        val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.DISCARD, LocationEnum.USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 708)
                        {card -> card.card_data.card_type == CardType.ENCHANTMENT}?: break
                        if (list.size == 1){
                            val card = game_status.popCardFrom(player, list[0], LocationEnum.DISCARD, true)?:
                            game_status.popCardFrom(player, list[0], LocationEnum.USED_CARD, true)?: continue
                            if(card.card_data.card_class == CardClass.SPECIAL) card.special_card_state = SpecialCardEnum.PLAYING
                            if(!card.textUseCheck(player, game_status, null)) break
                            card.use(player, game_status, null)
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                Stratagem.GUE_MO -> {
                    while (true){
                        val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.ENCHANTMENT_ZONE), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 709)
                        {card -> card.card_data.card_type == CardType.ENCHANTMENT}?: break
                        if (list.size == 1){
                            val card = game_status.popCardFrom(player.opposite(), list[0], LocationEnum.ENCHANTMENT_ZONE, true)?: continue
                            game_status.cardToDust(player.opposite(), card.nap, card)
                            game_status.enchantmentDestruction(player.opposite(), card)
                            break
                        }
                    }
                    setStratagemByUser(game_status, player, SHINRA_SHINRA_CARD_NUMBER)
                }
                null -> {}
            }
            null
        })
        cheonjiBanBag.setSpecial(2)
        cheonjiBanBag.setEnchantment(5)
        cheonjiBanBag.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, { _, _, attack ->
                attack.run {
                    val temp = editedAuraDamage; editedAuraDamage = editedLifeDamage; editedLifeDamage = temp
                }
            }))
            null
        })
        samraBanSho.setSpecial(6)
        samraBanSho.setEnchantment(6)
        samraBanSho.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.dustToLife(player, 2)
            null
        })
        samraBanSho.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_ENCHANTMENT_DESTRUCTION_YOUR){ _, player, game_status, _ ->
            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false, null, null)
            null
        })
        samraBanSho.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.gameEnd(null, player)
            null
        })
    }

    private fun centrifugal(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.startTurnDistance + 1 < game_status.thisTurnDistance && !game_status.logger.checkThisTurnDoAttack(player)
    }

    private fun checkAllSpecialCardUsed(player: PlayerEnum, game_status: GameStatus, except: Int): Boolean{
        val nowPlayer = game_status.getPlayer(player)
        if(nowPlayer.special_card_deck.isEmpty()){
            for (card in game_status.player1.enchantmentCard.values){
                if(card.card_number == except) continue
                if(card.player == player && card.special_card_state != null) return false
            }
            for (card in game_status.player1.usingCard){
                if(card.card_number == except) continue
                if(card.player == player && card.special_card_state != null) return false
            }
            for (card in game_status.player2.enchantmentCard.values){
                if(card.card_number == except) continue
                if(card.player == player && card.special_card_state != null) return false
            }
            for (card in game_status.player2.usingCard){
                if(card.card_number == except) continue
                if(card.player == player && card.special_card_state != null) return false
            }
            return true
        }
        else{
            return false
        }
    }

    private val centrifugalAttack = CardData(CardClass.NORMAL, CardName.HAGANE_CENTRIFUGAL_ATTACK, MegamiEnum.HAGANE, CardType.ATTACK, SubType.NONE)
    private val fourWindedEarthquake = CardData(CardClass.NORMAL, CardName.HAGANE_FOUR_WINDED_EARTHQUAKE, MegamiEnum.HAGANE, CardType.ATTACK, SubType.NONE)
    private val groundBreaking = CardData(CardClass.NORMAL, CardName.HAGANE_GROUND_BREAKING, MegamiEnum.HAGANE, CardType.ATTACK, SubType.FULL_POWER)
    private val hyperRecoil = CardData(CardClass.NORMAL, CardName.HAGANE_HYPER_RECOIL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val wonMuRuyn = CardData(CardClass.NORMAL, CardName.HAGANE_WON_MU_RUYN, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val ringABell = CardData(CardClass.NORMAL, CardName.HAGANE_RING_A_BELL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val gravitationField = CardData(CardClass.NORMAL, CardName.HAGANE_GRAVITATION_FIELD, MegamiEnum.HAGANE, CardType.ENCHANTMENT, SubType.NONE)
    private val grandSkyHoleCrash = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, MegamiEnum.HAGANE, CardType.ATTACK, SubType.NONE)
    private val grandBellMegalobel = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_BELL_MEGALOBEL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val grandGravitationAttract = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_GRAVITATION_ATTRACT, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val grandMountainRespect = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)


    private fun haganeCardInit(){
        centrifugalAttack.setAttack(DistanceType.CONTINUOUS, Pair(2, 6), null, 5, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        centrifugalAttack.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        centrifugalAttack.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        centrifugalAttack.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            if (player == game_status.turnPlayer) {
                for(card in game_status.getPlayer(player).hand.values){
                    if(card.card_data.canCover){
                        game_status.popCardFrom(player, card.card_number, LocationEnum.HAND, false)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                        }
                    }
                }
                for(card in game_status.getPlayer(player.opposite()).hand.values){
                    if(card.card_data.canCover){
                        game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                        }
                    }
                }
                game_status.setConcentration(player, 0)
            }
            null
        })
        centrifugalAttack.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.END_CURRENT_PHASE) {_, _, game_status, _ ->
            game_status.endCurrentPhase = true
            null
        })
        fourWindedEarthquake.setAttack(DistanceType.CONTINUOUS, Pair(0, 6), null, 1, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        fourWindedEarthquake.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            if (game_status.startTurnDistance + 1 < game_status.thisTurnDistance || game_status.startTurnDistance - 1 > game_status.thisTurnDistance) {
                game_status.getPlayer(player.opposite()).hand.values.randomOrNull()?.let { card ->
                    game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, true)?.let{
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD, true)
                    }
                }
            }
            null
        })
        groundBreaking.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 2, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        groundBreaking.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.setConcentration(player.opposite(), 0)
            game_status.setShrink(player.opposite())
            null
        })
        hyperRecoil.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            if(game_status.getAdjustDistance(player) >= 5){
                game_status.distanceToFlare(player, 1)
            }
            else{
                game_status.flareToDistance(player.opposite(), 1)
            }
            null
        })
        wonMuRuyn.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        wonMuRuyn.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        wonMuRuyn.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            if(game_status.getPlayerFlare(player.opposite()) >= 3){
                game_status.flareToAura(player.opposite(), player, 2)
            }
            null
        })
        ringABell.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        ringABell.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        ringABell.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, 805)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, _ ->
                    true}, {_, _, attack ->
                        attack.run {
                            auraPlusMinus(2); lifePlusMinus(1)
                        }
                    }))
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS, {_, _, _ -> true},
                        { _, _, attack -> attack.plusMinusRange(1, false)
                        }))
                    game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET, { _, _, _ -> true},
                        { _, _, attack -> attack.canNotReact()
                        })
                    )
                    break
                }
            }
            null
        })
        gravitationField.setEnchantment(2)
        gravitationField.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                game_status.distanceToAura(player, 2)
            }
            game_status.distanceToAura(player, 1)
            null
        })
        gravitationField.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) {_, _, _, _ ->
            -1
        })
        grandSkyHoleCrash.setSpecial(4)
        grandSkyHoleCrash.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1000, 1000,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = true)
        grandSkyHoleCrash.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = abs(gameStatus.getAdjustDistance(nowPlayer) - gameStatus.startTurnDistance)
                    editedAuraDamage = temp
                    editedLifeDamage = if(temp % 2 == 0) temp / 2 else temp / 2 + 1
                }
            }))
            null
        })
        grandBellMegalobel.setSpecial(2)
        grandBellMegalobel.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(checkAllSpecialCardUsed(player, game_status, card_number)){
                game_status.dustToLife(player, 2)
            }
            null
        })
        grandGravitationAttract.setSpecial(5)
        grandGravitationAttract.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.distanceToFlare(player, 3)
            null
        })
        grandGravitationAttract.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){card_number, player, game_status, _ ->
            if(!game_status.logger.checkThisCardUsed(player, card_number) && game_status.logger.checkUseCentrifugal(player)) 1
            else 0
        })
        grandMountainRespect.setSpecial(4)
        grandMountainRespect.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _->
            if(centrifugal(player, game_status)) 1
            else 0
        })
        grandMountainRespect.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) {card_number, player, game_status, _->
            game_status.logger.insert(Log(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
            null
        })
        grandMountainRespect.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 810)
                {card -> card.card_data.sub_type != SubType.FULL_POWER}?: break
                if(selected.size == 0) break
                else if(selected.size <= 2){
                    val card = game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD)?: continue
                    game_status.useCardFrom(player, card, LocationEnum.DISCARD, false, null,
                        isCost = true, isConsume = true)
                    if(game_status.getEndTurn(player)) break
                    if(selected.size == 2){
                        val secondCard = game_status.getCardFrom(player, selected[1], LocationEnum.DISCARD)?: break
                        game_status.useCardFrom(player, secondCard, LocationEnum.DISCARD, false, null,
                            isCost = true, isConsume = true)
                    }
                    break
                }
            }
            null
        })
    }

    private val throwKunai = CardData(CardClass.NORMAL, CardName.CHIKAGE_THROW_KUNAI, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.NONE)
    private val poisonNeedle = CardData(CardClass.NORMAL, CardName.CHIKAGE_POISON_NEEDLE, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.NONE)
    private val toZuChu = CardData(CardClass.NORMAL, CardName.CHIKAGE_TO_ZU_CHU, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.REACTION)
    private val cuttingNeck = CardData(CardClass.NORMAL, CardName.CHIKAGE_CUTTING_NECK, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.FULL_POWER)
    private val poisonSmoke = CardData(CardClass.NORMAL, CardName.CHIKAGE_POISON_SMOKE, MegamiEnum.CHIKAGE, CardType.BEHAVIOR, SubType.NONE)
    private val tipToeing = CardData(CardClass.NORMAL, CardName.CHIKAGE_TIP_TOEING, MegamiEnum.CHIKAGE, CardType.ENCHANTMENT, SubType.NONE)
    private val muddle = CardData(CardClass.NORMAL, CardName.CHIKAGE_MUDDLE, MegamiEnum.CHIKAGE, CardType.ENCHANTMENT, SubType.NONE)
    private val deadlyPoison = CardData(CardClass.SPECIAL, CardName.CHIKAGE_DEADLY_POISON, MegamiEnum.CHIKAGE, CardType.BEHAVIOR, SubType.NONE)
    private val hankiPoison = CardData(CardClass.SPECIAL, CardName.CHIKAGE_HAN_KI_POISON, MegamiEnum.CHIKAGE, CardType.ENCHANTMENT, SubType.REACTION)
    private val reincarnationPoison = CardData(CardClass.SPECIAL, CardName.CHIKAGE_REINCARNATION_POISON, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.NONE)
    private val chikageWayOfLive = CardData(CardClass.SPECIAL, CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE, MegamiEnum.CHIKAGE, CardType.ENCHANTMENT, SubType.FULL_POWER)

    private val poisonParalytic = CardData(CardClass.NORMAL, CardName.POISON_PARALYTIC, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)
    private val poisonHallucinogenic = CardData(CardClass.NORMAL, CardName.POISON_HALLUCINOGENIC, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)
    private val poisonRelaxation = CardData(CardClass.NORMAL, CardName.POISON_RELAXATION, MegamiEnum.NONE, CardType.ENCHANTMENT, SubType.NONE)
    private val poisonDeadly1 = CardData(CardClass.NORMAL, CardName.POISON_DEADLY_1, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)
    private val poisonDeadly2 = CardData(CardClass.NORMAL, CardName.POISON_DEADLY_2, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)

    private fun makePoisonList(player: PlayerEnum, game_status: GameStatus): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        game_status.getPlayer(player).poisonBag[CardName.POISON_PARALYTIC]?.let {
            cardList.add(it.card_number)
        }
        game_status.getPlayer(player).poisonBag[CardName.POISON_HALLUCINOGENIC]?.let {
            cardList.add(it.card_number)
        }
        game_status.getPlayer(player).poisonBag[CardName.POISON_RELAXATION]?.let {
            cardList.add(it.card_number)
        }
        return cardList
    }

    private fun cardUsedCheck(card: Card, player: PlayerEnum): Boolean = card.player == player && card.card_data.card_class == CardClass.SPECIAL &&
            card.special_card_state != SpecialCardEnum.PLAYED && card.card_data.card_name != CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE

    private fun chikageCardInit(){
        throwKunai.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        poisonNeedle.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        poisonNeedle.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.INSERT_POISON) {card_number, player, game_status, _ ->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 901, 1)[0]
                game_status.popCardFrom(player, get, LocationEnum.POISON_BAG, false)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_TOP, false)
                }
            }
            null
        })
        toZuChu.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 1, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        toZuChu.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.auraToDistance(player, 1)
            game_status.dustToDistance(1)
            game_status.getPlayer(player.opposite()).canNotGoForward = true
            null
        })
        cuttingNeck.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 2, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        cuttingNeck.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.INSERT_POISON) {_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).hand.size >= 2){
                val list = game_status.selectCardFrom(player.opposite(), player.opposite(),
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 903, 1
                ) { true }
                game_status.popCardFrom(player.opposite(), list!![0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD, true)
                }
            }
            null
        })
        poisonSmoke.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.INSERT_POISON) {_, player, game_status, _->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 904, 1)[0]
                game_status.popCardFrom(player, get, LocationEnum.POISON_BAG, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.HAND, true)
                }
            }
            null
        })
        tipToeing.setEnchantment(4)
        tipToeing.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null))
        tipToeing.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_DISTANCE){_, _, _, _->
            -2
        })
        muddle.setEnchantment(2)
        muddle.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_GO_BACKWARD_OTHER){_, _, _, _ ->
            1
        })
        muddle.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_BREAK_AWAY_OTHER){ _, _, _, _ ->
            1
        })
        deadlyPoison.setSpecial(3)
        deadlyPoison.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.INSERT_POISON) {_, player, game_status, _ ->
            val getCard = game_status.popCardFrom(player, game_status.getCardNumber(player, CardName.POISON_DEADLY_1), LocationEnum.POISON_BAG, false)?:
            game_status.popCardFrom(player, game_status.getCardNumber(player, CardName.POISON_DEADLY_2), LocationEnum.POISON_BAG, false)
            if(getCard != null){
                game_status.insertCardTo(player.opposite(), getCard, LocationEnum.YOUR_DECK_TOP, false)
            }
            null
        })
        hankiPoison.setSpecial(2)
        hankiPoison.setEnchantment(5)
        hankiPoison.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_CHANGE) { card_number, _, _, reactedAttack ->
            reactedAttack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { player, game_status, attack ->
                val damage = attack.getDamage(game_status, player,  game_status.getPlayerAttackBuff(player))
                damage.first == 999 || damage.second == 999
            }, { _, _, attack ->
                attack.makeNotValid()
            }))
            null
        })
        hankiPoison.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            game_status.addThisTurnOtherBuff(player.opposite(), OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
                { nowPlayer, gameStatus, attack ->
                    val damage = attack.getDamage(gameStatus, nowPlayer, game_status.getPlayerAttackBuff(player))
                    damage.first == 999 || damage.second == 999
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            null
        })
        reincarnationPoison.setSpecial(1)
        reincarnationPoison.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        reincarnationPoison.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayerHandSize(player.opposite()) >= 2) 1
            else 0
        })
        chikageWayOfLive.setSpecial(5)
        chikageWayOfLive.setEnchantment(4)
        chikageWayOfLive.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.ADD_LISTENER) {card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                         _, _, damage ->
                if(damage){
                    gameStatus.popCardFrom(player, cardNumber, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                        gameStatus.cardToDust(player, it.nap, it)
                        gameStatus.insertCardTo(it.player, it, LocationEnum.SPECIAL_CARD, true)
                    }
                    true
                }
                else{
                    false
                }
            })
            null
        })
        chikageWayOfLive.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.GAME_END) {card_number, player, game_status, _ ->
            game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let ret@{
                for(card in game_status.getPlayer(PlayerEnum.PLAYER1).enchantmentCard.values){
                    if(cardUsedCheck(card, player)){
                        return@ret
                    }
                }
                for(card in game_status.getPlayer(PlayerEnum.PLAYER2).enchantmentCard.values){
                    if(cardUsedCheck(card, player)){
                        return@ret
                    }
                }
                for(card in game_status.getPlayer(player).special_card_deck.values){
                    if(cardUsedCheck(card, player)){
                        return@ret
                    }
                }
                game_status.gameEnd(player, null)
            }

            null
        })
        poisonParalytic.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, player, game_status, _ ->
            if(game_status.getPlayer(player).didBasicOperation) 0
            else 1
        })
        poisonParalytic.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.END_CURRENT_PHASE) {_, _, game_status, _ ->
            game_status.endCurrentPhase = true
            null
        })
        poisonParalytic.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.POISON_BAG, card_number)
            null
        })
        poisonHallucinogenic.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.flareToDust(player, 2)
            null
        })
        poisonHallucinogenic.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.POISON_BAG, card_number)
            null
        })
        poisonRelaxation.setEnchantment(3)
        poisonRelaxation.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_USE_ATTACK){_, _, _, _ ->
            null
        })
        poisonRelaxation.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                game_status.insertCardTo(it.player.opposite(), it, LocationEnum.POISON_BAG, true)
            }

            null
        })
        poisonDeadly1.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.auraToDust(player, 3)
            null
        })
        poisonDeadly2.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.auraToDust(player, 3)
            null
        })
        poisonDeadly1.canCover = false
        poisonDeadly2.canCover = false
        poisonRelaxation.canCover = false
        poisonHallucinogenic.canCover = false
        poisonParalytic.canCover = false
    }

    private val elekittel = CardData(CardClass.NORMAL, CardName.KURURU_ELEKITTEL, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val accelerator = CardData(CardClass.NORMAL, CardName.KURURU_ACCELERATOR, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val kururuoong = CardData(CardClass.NORMAL, CardName.KURURU_KURURUOONG, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.REACTION)
    private val tornado = CardData(CardClass.NORMAL, CardName.KURURU_TORNADO, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val regainer = CardData(CardClass.NORMAL, CardName.KURURU_REGAINER, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val module = CardData(CardClass.NORMAL, CardName.KURURU_MODULE, MegamiEnum.KURURU, CardType.ENCHANTMENT, SubType.NONE)
    private val reflector = CardData(CardClass.NORMAL, CardName.KURURU_REFLECTOR, MegamiEnum.KURURU, CardType.ENCHANTMENT, SubType.NONE)
    private val drainDevil = CardData(CardClass.SPECIAL, CardName.KURURU_DRAIN_DEVIL, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.REACTION)
    private val bigGolem = CardData(CardClass.SPECIAL, CardName.KURURU_BIG_GOLEM, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val industria = CardData(CardClass.SPECIAL, CardName.KURURU_INDUSTRIA, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val dupliGear1 = CardData(CardClass.NORMAL, CardName.KURURU_DUPLICATED_GEAR_1, MegamiEnum.NONE, CardType.UNDEFINED, SubType.NONE)
    private val dupliGear2 = CardData(CardClass.NORMAL, CardName.KURURU_DUPLICATED_GEAR_2, MegamiEnum.NONE, CardType.UNDEFINED, SubType.NONE)
    private val dupliGear3 = CardData(CardClass.NORMAL, CardName.KURURU_DUPLICATED_GEAR_3, MegamiEnum.NONE, CardType.UNDEFINED, SubType.NONE)
    private val kanshousouchiKururusik = CardData(CardClass.SPECIAL, CardName.KURURU_KANSHOUSOUCHI_KURURUSIK, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)

    private fun calcKikou(card_data: CardData, kikou: Kikou){
        when(card_data.card_type){
            CardType.ATTACK -> kikou.attack += 1
            CardType.BEHAVIOR -> kikou.behavior += 1
            CardType.ENCHANTMENT -> kikou.enchantment += 1
            else -> {}
        }
        when(card_data.sub_type){
            SubType.FULL_POWER -> kikou.fullPower += 1
            SubType.REACTION -> kikou.reaction += 1
            else -> {}
        }
    }

    private fun getKikou(player: PlayerEnum, game_status: GameStatus): Kikou{
        val result = Kikou()
        val nowPlayer = game_status.getPlayer(player)
        for (card in nowPlayer.enchantmentCard.values + nowPlayer.usedSpecialCard.values + nowPlayer.discard) {
            calcKikou(card.card_data, result)
        }
        return result
    }

    private suspend fun kururuoong(card_number: Int, player: PlayerEnum, command: CommandEnum, game_status: GameStatus){
        when (command) {
            CommandEnum.SELECT_ONE -> {
                game_status.drawCard(player, 1)
            }
            CommandEnum.SELECT_TWO -> {
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1002, 1
                ) { true }?: return
                game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, false)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                }
            }
            CommandEnum.SELECT_THREE -> {
                val list = game_status.selectCardFrom(player.opposite(), player.opposite(), listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1003, 1
                ) { true }?: return
                game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD, true)
                }
            }
            else -> {}
        }
    }

    private fun duplicateCardData(card_data: CardData, card_name: CardName): CardData{
        val result = CardData(card_data.card_class, card_name, card_data.megami, card_data.card_type, card_data.sub_type)
        result.run {
            umbrellaMark = card_data.umbrellaMark

            effectFold = card_data.effectFold
            effectUnfold = card_data.effectUnfold

            distanceTypeFold = card_data.distanceTypeFold
            distanceContFold = card_data.distanceContFold
            distanceUncontFold = card_data.distanceUncontFold
            lifeDamageFold = card_data.lifeDamageFold
            auraDamageFold = card_data.auraDamageFold

            distanceTypeUnfold = card_data.distanceTypeUnfold
            distanceContUnfold = card_data.distanceContUnfold
            distanceUncontUnfold = card_data.distanceUncontUnfold
            lifeDamageUnfold = card_data.lifeDamageUnfold
            auraDamageUnfold = card_data.auraDamageUnfold

            distance_type = card_data.distance_type
            distance_cont = card_data.distance_cont
            distance_uncont = card_data.distance_uncont
            life_damage =  card_data.life_damage
            aura_damage = card_data.aura_damage

            charge = card_data.charge

            cost = card_data.cost

            effect = card_data.effect
            canCover = card_data.canCover
            canDiscard = card_data.canDiscard

            effect = card_data.effect
        }
        return result
    }

    private fun kururuCardInit(){
        elekittel.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DAMAGE) {_, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.behavior >= 3 && kikou.reaction >= 2) {
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null)
            }
            null
        })
        accelerator.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.enchantment >= 1 && kikou.behavior >= 2) {
                while(true){
                    val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1001
                    ) { card -> card.card_data.sub_type == SubType.FULL_POWER }?: break
                    if(list.size == 1){
                        val card = game_status.getCardFrom(player, list[0], LocationEnum.HAND)?: continue
                        game_status.useCardFrom(player, card, LocationEnum.HAND, false, null,
                            isCost = true, isConsume = true)
                        break
                    }
                    else if(list.size == 0){
                        break
                    }
                }
            }
            null
        })
        kururuoong.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, _, _, react_attack ->
            if(react_attack == null) 0
            else 1
        })
        kururuoong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            val firstCommand = game_status.receiveCardEffectSelect(player, 1002)
            if(firstCommand != CommandEnum.SELECT_NOT){
                kururuoong(card_number, player, firstCommand, game_status)
                while(true){
                    val secondCommand = game_status.receiveCardEffectSelect(player, 1002)
                    if(firstCommand == secondCommand) continue
                    kururuoong(card_number, player, secondCommand, game_status)
                    break
                }
            }
            null
        })
        tornado.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DAMAGE) {_, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 2) {
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(5, 999), false,
                null, null)
            }
            if(kikou.enchantment >= 2){
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                null, null)
            }
            null
        })
        regainer.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.enchantment >= 1 && kikou.reaction >= 1) {
                while(true){
                    val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD, LocationEnum.USED_CARD, LocationEnum.DISCARD),
                        CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1004)
                    { card -> card.card_data.sub_type != SubType.FULL_POWER && card.special_card_state != SpecialCardEnum.UNUSED &&
                            card.card_data.megami != MegamiEnum.KURURU}?: break
                    if(list.size == 0) {
                        break
                    }
                    else if(list.size > 1) {
                        continue
                    }
                    else{
                        var location = LocationEnum.DISCARD
                        val card = game_status.getCardFrom(player, list[0], LocationEnum.DISCARD) ?:
                        game_status.getCardFrom(player, list[0], LocationEnum.USED_CARD).let {
                            location = LocationEnum.USED_CARD
                            it
                        }?:game_status.getCardFrom(player, list[0], LocationEnum.COVER_CARD).let {
                            location = LocationEnum.COVER_CARD
                            it
                        }?: continue

                        while(true){
                            when(game_status.receiveCardEffectSelect(player, 1004)){
                                CommandEnum.SELECT_ONE -> {
                                    if(card.card_data.card_type == CardType.ATTACK){
                                        game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                            { _, _, attack -> attack.run {
                                                kururuChangeRangeUpper = true
                                                when(editedDistanceType){
                                                    DistanceType.DISCONTINUOUS -> {
                                                        for (i in 9 downTo 0) {
                                                            if(editedDistanceUncont!![i]){
                                                                editedDistanceUncont!![i + 1] = true
                                                                editedDistanceUncont!![i] = false
                                                            }
                                                        }
                                                    }
                                                    DistanceType.CONTINUOUS -> {
                                                        editedDistanceCont = Pair(editedDistanceCont!!.first + 1, editedDistanceCont!!.second + 1)
                                                    }
                                                }
                                            }
                                            }))
                                    }
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    if(card.card_data.card_type == CardType.ATTACK){
                                        game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                            { _, _, attack -> attack.run {
                                                kururuChangeRangeUnder = true
                                                when(editedDistanceType){
                                                    DistanceType.DISCONTINUOUS -> {
                                                        for (i in 1..10) {
                                                            if(editedDistanceUncont!![i]){
                                                                editedDistanceUncont!![i - 1] = true
                                                                editedDistanceUncont!![i] = false
                                                            }
                                                        }
                                                    }
                                                    DistanceType.CONTINUOUS -> {
                                                        editedDistanceCont = Pair(editedDistanceCont!!.first - 1, editedDistanceCont!!.second - 1)
                                                    }
                                                }
                                            }
                                            }))
                                    }
                                    break
                                }
                                CommandEnum.SELECT_THREE -> {
                                    if(card.card_data.card_type == CardType.ATTACK){
                                        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                            {_, _, attack ->
                                            attack.auraPlusMinus(1)
                                        }))
                                    }
                                    break
                                }
                                CommandEnum.SELECT_FOUR -> {
                                    if(card.card_data.card_type == CardType.ATTACK){
                                        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                            {_, _, attack ->
                                            attack.auraPlusMinus(-1)
                                        }))
                                    }
                                    break
                                }
                                CommandEnum.SELECT_FIVE -> {
                                    if(card.card_data.card_type == CardType.ATTACK){
                                        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                            {_, _, attack ->
                                                attack.lifePlusMinus(1)
                                        }))
                                    }
                                    break
                                }
                                CommandEnum.SELECT_SIX -> {
                                    if(card.card_data.card_type == CardType.ATTACK){
                                        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                            {_, _, attack ->
                                                attack.lifePlusMinus(-1)
                                        }))
                                    }
                                    break
                                }
                                CommandEnum.SELECT_SEVEN -> {
                                    if(card.card_data.card_type == CardType.ENCHANTMENT){
                                        game_status.getPlayer(player).napBuff += 1
                                    }
                                    break
                                }
                                CommandEnum.SELECT_EIGHT -> {
                                    if(card.card_data.card_type == CardType.ENCHANTMENT){
                                        game_status.getPlayer(player).napBuff -= 1
                                    }
                                    break
                                }
                                CommandEnum.SELECT_NOT -> {
                                    break
                                }
                                else -> continue
                            }
                        }

                        game_status.useCardFrom(player, card, location, false, null,
                            isCost = false, isConsume = true)
                        break
                    }
                }
            }
            null
        })
        regainer.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_USE_COVER) {_, _, _, _ ->
            null
        })
        module.setEnchantment(3)
        module.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_USE_BEHAVIOR_END){ card_number, player, game_status, _ ->
            if(!(game_status.getEndTurn(player))){
                while(true){
                    when(val command = game_status.requestBasicOperation(player, card_number)){
                        CommandEnum.ACTION_GO_FORWARD, CommandEnum.ACTION_GO_BACKWARD, CommandEnum.ACTION_WIND_AROUND,
                        CommandEnum.ACTION_INCUBATE, CommandEnum.ACTION_BREAK_AWAY, CommandEnum.ACTION_NAGA, CommandEnum.ACTION_YAKSHA,
                        CommandEnum.ACTION_GARUDA -> {
                            if(game_status.canDoBasicOperation(player, command)){
                                game_status.doBasicOperation(player, command, card_number)
                                break
                            }
                            else{
                                continue
                            }
                        }
                        CommandEnum.SELECT_NOT -> break
                        else -> {}
                    }
                }
            }
            null
        })
        reflector.setEnchantment(0)
        reflector.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 1 && kikou.reaction >= 1) {
                game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE)?.let {
                    game_status.dustToCard(player, 4, it)
                }
            }
            null
        })
        reflector.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            if(game_status.logger.checkThisTurnAttackNumber(player.opposite()) == 1){
                game_status.addThisTurnOtherBuff(player.opposite(), OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
                    { _, _, _ -> true}, { _, _, attack ->
                        attack.makeNotValid()
                    })
                )
            }
            null
        })
        drainDevil.setSpecial(2)
        drainDevil.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            game_status.auraToAura(player.opposite(), player, 1)
            null
        })
        drainDevil.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_SPECIAL_RETURN_YOUR) { card_number, player, game_status, _ ->
            if(!game_status.getPlayer(player).end_turn){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, 1007)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.getCardFrom(player, card_number, LocationEnum.USED_CARD)?.let {
                                game_status.useCardFrom(player, it, LocationEnum.USED_CARD, false, null,
                                    isCost = false, isConsume = true)
                            }
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {
                            continue
                        }
                    }
                }
            }
            null
        })
        bigGolem.setSpecial(4)
        bigGolem.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.reaction >= 1 && kikou.fullPower >= 2){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, 1008)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                            null, null)
                            game_status.deckReconstruct(player, false)
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {
                            continue
                        }
                    }
                }
            }
            null
        })
        bigGolem.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_FULL_POWER_USED_YOUR) { card_number, player, game_status, _ ->
            while(true){
                when(val command = game_status.requestBasicOperation(player, card_number)){
                    CommandEnum.ACTION_GO_FORWARD, CommandEnum.ACTION_GO_BACKWARD, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.ACTION_INCUBATE, CommandEnum.ACTION_BREAK_AWAY, CommandEnum.ACTION_NAGA,
                    CommandEnum.ACTION_YAKSHA, CommandEnum.ACTION_GARUDA  -> {
                        if(game_status.canDoBasicOperation(player, command)){
                            game_status.doBasicOperation(player, command, card_number)
                            break
                        }
                        else{
                            continue
                        }
                    }
                    CommandEnum.SELECT_NOT -> break
                    else -> {}
                }
            }
            null
        })
        industria.setSpecial(1)
        industria.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.SEAL_CARD) ret@{card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player){
                for(card in game_status.getPlayer(player).sealInformation.values){
                    if(card == card_number) return@ret null
                }
                while (true){
                    val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.HAND, LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1009
                    ) { card -> card.card_data.card_type != CardType.ENCHANTMENT }?: break
                    if (list.size == 1){
                        game_status.popCardFrom(player, list[0], LocationEnum.DISCARD, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                            game_status.getPlayer(player).sealInformation[it.card_number] = card_number
                        }?: game_status.popCardFrom(player, list[0], LocationEnum.HAND, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                            game_status.getPlayer(player).sealInformation[it.card_number] = card_number
                        }?: continue
                        break
                    }
                    else if(list.size == 0){
                        break
                    }
                }
            }
            null
        })
        industria.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            val getCard = game_status.getCardFrom(player, CardName.KURURU_DUPLICATED_GEAR_1, LocationEnum.ADDITIONAL_CARD)?:
            game_status.getCardFrom(player, CardName.KURURU_DUPLICATED_GEAR_2, LocationEnum.ADDITIONAL_CARD)?:
            game_status.getCardFrom(player, CardName.KURURU_DUPLICATED_GEAR_3, LocationEnum.ADDITIONAL_CARD)
            if(getCard != null){
                game_status.moveAdditionalCard(player, getCard.card_data.card_name, LocationEnum.YOUR_DECK_BELOW)
            }
            val ownerPlayer = game_status.getPlayer(game_status.getCardOwner(card_number))
            var duplicateCardData: CardData? = null
            for(card in ownerPlayer.sealZone.keys){
                if(ownerPlayer.sealInformation[card] == card_number){
                    duplicateCardData = ownerPlayer.sealZone[card]!!.card_data
                }
            }
            if(duplicateCardData != null){
                for(card in ownerPlayer.hand.values + ownerPlayer.normalCardDeck + ownerPlayer.discard + ownerPlayer.cover_card
                        + game_status.getPlayer(PlayerEnum.PLAYER2).sealZone.values.filter {
                            it.player == game_status.getCardOwner(card_number)
                } + game_status.getPlayer(PlayerEnum.PLAYER1).sealZone.values.filter {
                            it.player == game_status.getCardOwner(card_number)
                }){
                    when(card.card_number.toCardName()){
                        CardName.KURURU_DUPLICATED_GEAR_1 -> card.card_data = duplicateCardData(duplicateCardData, CardName.KURURU_DUPLICATED_GEAR_1)
                        CardName.KURURU_DUPLICATED_GEAR_2 -> card.card_data = duplicateCardData(duplicateCardData, CardName.KURURU_DUPLICATED_GEAR_2)
                        CardName.KURURU_DUPLICATED_GEAR_3 -> card.card_data = duplicateCardData(duplicateCardData, CardName.KURURU_DUPLICATED_GEAR_3)
                        else -> {}
                    }
                }
            }
            null
        })
        industria.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateReconstructListener(player, Listener(player, card_number) {gameStatus, cardNumber, _, _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
        industria.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_THIS_CARD_RETURN){ card_number, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)
            for(card in nowPlayer.hand.values + nowPlayer.normalCardDeck + nowPlayer.discard + nowPlayer.cover_card
                    + nowPlayer.sealZone.values + game_status.getPlayer(PlayerEnum.PLAYER2).sealZone.values.filter {
                it.player == game_status.getCardOwner(card_number)
            } + game_status.getPlayer(PlayerEnum.PLAYER1).sealZone.values.filter {
                it.player == game_status.getCardOwner(card_number)
            }){
                when(card.card_number.toCardName()){
                    CardName.KURURU_DUPLICATED_GEAR_1 -> card.card_data = dupliGear1
                    CardName.KURURU_DUPLICATED_GEAR_2 -> card.card_data = dupliGear2
                    CardName.KURURU_DUPLICATED_GEAR_3 -> card.card_data = dupliGear3
                    else -> {}
                }
            }
            null
        })
        dupliGear1.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, _, _, _ ->
            0
        })
        dupliGear2.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, _, _, _ ->
            0
        })
        dupliGear3.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, _, _, _ ->
            0
        })
        kanshousouchiKururusik.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 2 && kikou.behavior >= 3 && kikou.enchantment >= 2){
                while(true){
                    val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.SPECIAL_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1013){
                        true
                    }?: break
                    if(list.size == 1){
                        game_status.popCardFrom(player.opposite(), list[0], LocationEnum.SPECIAL_CARD, true)?.let {
                            it.special_card_state = SpecialCardEnum.PLAYED
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.USED_CARD, true)
                        }
                        break
                    }
                    else if(list.size == 0){
                        break
                    }
                }
            }
            null
        })
        kanshousouchiKururusik.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _->
            while(true){
                val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1012){
                    true
                }?: break
                if(list.size == 1){
                    val card = game_status.getCardFrom(player.opposite(), list[0], LocationEnum.USED_CARD)?: continue
                    game_status.useCardFrom(player, card, LocationEnum.USED_CARD, false, null,
                        isCost = true, isConsume = false)
                    game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
                    break
                }
                else if(list.size == 0){
                    break
                }
            }
            null
        })
    }

    val attackYakshaText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MANEUVER) {_, player, game_status, _ ->
        maneuver(player, game_status, true)
        null
    }

    private val burningSteam = CardData(CardClass.NORMAL, CardName.THALLYA_BURNING_STEAM, MegamiEnum.THALLYA, CardType.ATTACK, SubType.NONE)
    private val wavingEdge = CardData(CardClass.NORMAL, CardName.THALLYA_WAVING_EDGE, MegamiEnum.THALLYA, CardType.ATTACK, SubType.NONE)
    private val shieldCharge = CardData(CardClass.NORMAL, CardName.THALLYA_SHIELD_CHARGE, MegamiEnum.THALLYA, CardType.ATTACK, SubType.NONE)
    private val steamCanon = CardData(CardClass.NORMAL, CardName.THALLYA_STEAM_CANNON, MegamiEnum.THALLYA, CardType.ATTACK, SubType.FULL_POWER)
    private val stunt = CardData(CardClass.NORMAL, CardName.THALLYA_STUNT, MegamiEnum.THALLYA, CardType.BEHAVIOR, SubType.NONE)
    private val roaring = CardData(CardClass.NORMAL, CardName.THALLYA_ROARING, MegamiEnum.THALLYA, CardType.BEHAVIOR, SubType.NONE)
    private val turboSwitch = CardData(CardClass.NORMAL, CardName.THALLYA_TURBO_SWITCH, MegamiEnum.THALLYA, CardType.BEHAVIOR, SubType.REACTION)
    private val alphaEdge = CardData(CardClass.SPECIAL, CardName.THALLYA_ALPHA_EDGE, MegamiEnum.THALLYA, CardType.ATTACK, SubType.NONE)
    private val omegaBurst = CardData(CardClass.SPECIAL, CardName.THALLYA_OMEGA_BURST, MegamiEnum.THALLYA, CardType.BEHAVIOR, SubType.REACTION)
    private val masterPiece = CardData(CardClass.SPECIAL, CardName.THALLYA_THALLYA_MASTERPIECE, MegamiEnum.THALLYA, CardType.BEHAVIOR, SubType.NONE)
    private val juliaBlackbox = CardData(CardClass.SPECIAL, CardName.THALLYA_JULIA_BLACKBOX, MegamiEnum.THALLYA, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val formYaksha = CardData(CardClass.SPECIAL, CardName.FORM_YAKSHA, MegamiEnum.THALLYA, CardType.UNDEFINED, SubType.NONE)
    private val formNaga = CardData(CardClass.SPECIAL, CardName.FORM_NAGA, MegamiEnum.THALLYA, CardType.UNDEFINED, SubType.NONE)
    private val formGaruda = CardData(CardClass.SPECIAL, CardName.FORM_GARUDA, MegamiEnum.THALLYA, CardType.UNDEFINED, SubType.NONE)

    private suspend fun maneuver(player: PlayerEnum, gameStatus: GameStatus, basicAction: Boolean) {
        if(gameStatus.getPlayer(player).artificialToken == 0) return
        var changeToken = false
        if(gameStatus.distanceToken - gameStatus.player1ArtificialTokenOn - gameStatus.player2ArtificialTokenOn > 0){
            while (true){
                changeToken = when(gameStatus.receiveCardEffectSelect(player, 1100)){
                    CommandEnum.SELECT_ONE -> {
                        gameStatus.addArtificialTokenAtDistance(player, true, 1)
                        true
                    }

                    CommandEnum.SELECT_TWO -> {
                        if(gameStatus.distanceToken + gameStatus.player1ArtificialTokenOut + gameStatus.player2ArtificialTokenOut > 9) break
                        gameStatus.addArtificialTokenAtDistance(player, false, 1)
                        true
                    }
                    else -> continue
                }
                break
            }
        }
        else{
            if(gameStatus.distanceToken + gameStatus.player1ArtificialTokenOut + gameStatus.player2ArtificialTokenOut < 10){
                gameStatus.addArtificialTokenAtDistance(player, false, 1)
                changeToken = true
            }
        }
        if(changeToken){
            gameStatus.getManeuverListener(player)?.let {
                if(!it.isEmpty()){
                    for(i in 1..it.size){
                        if(it.isEmpty()) break
                        val now = it.first()
                        it.removeFirst()
                        if(!(now.doAction(gameStatus, -1, -1,
                                booleanPara1 = false, booleanPara2 = false))){
                            it.addLast(now)
                        }
                    }
                }
            }
            if(!basicAction){
                for(card in gameStatus.getPlayer(player).usedSpecialCard.values){
                    card.effectAllValidEffect(player, gameStatus, TextEffectTag.WHEN_MANEUVER)
                }
            }
        }
    }

    private fun combustCondition(game_status: GameStatus, player: PlayerEnum): Boolean = game_status.getPlayer(player).artificialToken != 0

    private fun makeTransformListOrigin(player: PlayerEnum, game_status: GameStatus): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        game_status.getPlayer(player).additional_hand[CardName.FORM_YAKSHA]?.let {
            cardList.add(it.card_number)
        }
        game_status.getPlayer(player).additional_hand[CardName.FORM_NAGA]?.let {
            cardList.add(it.card_number)
        }
        game_status.getPlayer(player).additional_hand[CardName.FORM_GARUDA]?.let {
            cardList.add(it.card_number)
        }
        return cardList
    }

    private fun thallyaCardInit(){
        burningSteam.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        burningSteam.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MANEUVER) {_, player, game_status, _ ->
            maneuver(player, game_status, false)
            null
        })
        wavingEdge.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wavingEdge.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_CHECK){ _, player, game_status, _ ->
            if(combustCondition(game_status, player)) 1
            else 0
        })
        wavingEdge.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST){ _, player, game_status, _ ->
            game_status.combust(player, 1)
            null
        })
        wavingEdge.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MANEUVER) { _, player, game_status, _ ->
            maneuver(player, game_status, false)
            null
        })
        shieldCharge.setAttack(DistanceType.CONTINUOUS, Pair(1, 1), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shieldCharge.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_CHECK){_, player, game_status, _ ->
            if(combustCondition(game_status, player)) 1
            else 0
        })
        shieldCharge.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST){_, player, game_status, _ ->
            game_status.combust(player, 1)
            null
        })
        shieldCharge.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        shieldCharge.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        steamCanon.setAttack(DistanceType.CONTINUOUS, Pair(2, 8), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        steamCanon.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_CHECK){_, player, game_status, _ ->
            if(combustCondition(game_status, player)) 1
            else 0
        })
        steamCanon.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST){_, player, game_status, _ ->
            game_status.combust(player, 1)
            null
        })
        stunt.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })
        stunt.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.auraToFlare(player, player,2)
            null
        })
        roaring.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION){_, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken ?: 0) >= 2){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, 1105)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.combust(player, 2)
                            game_status.addConcentration(player)
                            game_status.decreaseConcentration(player.opposite())
                            game_status.setShrink(player.opposite())
                        }
                        CommandEnum.SELECT_NOT -> {}
                        else -> continue
                    }
                    break
                }
            }
            null
        })
        roaring.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION){_, player, game_status, _ ->
            if(game_status.getPlayer(player).concentration >= 2){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, 1106)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.decreaseConcentration(player)
                            game_status.decreaseConcentration(player)
                            game_status.restoreArtificialToken(player, 3)
                        }
                        CommandEnum.SELECT_NOT -> {}
                        else -> continue
                    }
                    break
                }
            }
            null
        })
        turboSwitch.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_CHECK){_, player, game_status, _ ->
            if(combustCondition(game_status, player)) 1
            else 0
        })
        turboSwitch.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST){_, player, game_status, _ ->
            game_status.combust(player, 1)
            null
        })
        turboSwitch.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MANEUVER) {_, player, game_status, _ ->
            maneuver(player, game_status, false)
            null
        })
        alphaEdge.setSpecial(1)
        alphaEdge.setAttack(DistanceType.DISCONTINUOUS, null, mutableListOf(1, 3, 5, 7), 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        alphaEdge.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateManeuverListener(player, Listener(player, card_number) {_, cardNumber, _,
                                                                                        _, _, _ ->
                game_status.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
        omegaBurst.setSpecial(4)
        omegaBurst.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, reactedAttack ->
            val number = game_status.getPlayer(player).artificialTokenBurn
            game_status.restoreArtificialToken(player, number)
            reactedAttack?.addOtherBuff( OtherBuff(card_number, 1, OtherBuffTag.GET,
                { nowPlayer, gameStatus, attack ->
                    val damage = attack.getDamage(gameStatus, nowPlayer, game_status.getPlayerAttackBuff(player))
                    (damage.first == 999) || (damage.first <= number)
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            null
        })
        masterPiece.setSpecial(1)
        masterPiece.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MANEUVER) {card_number, player, game_status, _ ->
            if(game_status.turnPlayer == player){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, 1109)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.dustToDistance(1)
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.distanceToDust(1)
                        }
                        CommandEnum.SELECT_NOT -> {
                        }
                        else -> {
                            continue
                        }
                    }
                    break
                }
            }
            null
        })
        juliaBlackbox.setSpecial(2)
        juliaBlackbox.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.TRANSFORM) {card_number, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken?: 0) == 0){
                val cardList = makeTransformListOrigin(player, game_status)
                if(cardList.size != 0){
                    val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1110, 1)[0]
                    game_status.getCardFrom(player, get, LocationEnum.ADDITIONAL_CARD)?.let {
                        game_status.moveAdditionalCard(player, get.toCardName(), LocationEnum.TRANSFORM)
                        it.special_card_state = SpecialCardEnum.PLAYED
                        it.effectText(player, game_status, null, TextEffectTag.WHEN_TRANSFORM)
                    }
                }
            }
            else{
                game_status.movePlayingCard(player, LocationEnum.SPECIAL_CARD, card_number)
            }
            null
        })
        formYaksha.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {_, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            if(player == PlayerEnum.PLAYER1) game_status.player1NextTurnDraw = 1
            else game_status.player2NextTurnDraw = 1
            null
        })
        formYaksha.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.FORBID_BASIC_OPERATION) {_, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken?: 0) == 0) 1
            else 0
        })
        formNaga.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {_, player, game_status, _ ->
            val otherFlare = game_status.getPlayer(player.opposite()).flare
            if(otherFlare >= 3){
                game_status.flareToDust(player.opposite(), otherFlare - 2)
            }
            null
        })
        formGaruda.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {_, player, game_status, _ ->
            game_status.drawCard(player, 2)
            game_status.getPlayer(player).max_hand = 99999
            null
        })
    }

    private val beastNail = CardData(CardClass.NORMAL, CardName.RAIRA_BEAST_NAIL, MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val stormSurgeAttack = CardData(CardClass.NORMAL, CardName.RAIRA_STORM_SURGE_ATTACK, MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val reincarnationNail = CardData(CardClass.NORMAL, CardName.RAIRA_REINCARNATION_NAIL, MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val windRun = CardData(CardClass.NORMAL, CardName.RAIRA_WIND_RUN, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.NONE)
    private val wisdomOfStormSurge = CardData(CardClass.NORMAL, CardName.RAIRA_WISDOM_OF_STORM_SURGE, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.NONE)
    private val howling = CardData(CardClass.NORMAL, CardName.RAIRA_HOWLING, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val windKick = CardData(CardClass.NORMAL, CardName.RAIRA_WIND_KICK, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val thunderWindPunch = CardData(CardClass.SPECIAL, CardName.RAIRA_THUNDER_WIND_PUNCH, MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val summonThunder = CardData(CardClass.SPECIAL, CardName.RAIRA_SUMMON_THUNDER, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val windConsequenceBall = CardData(CardClass.SPECIAL, CardName.RAIRA_WIND_CONSEQUENCE_BALL, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.NONE)
    private val circularCircuit = CardData(CardClass.SPECIAL, CardName.RAIRA_CIRCULAR_CIRCUIT, MegamiEnum.RAIRA, CardType.ENCHANTMENT, SubType.REACTION)
    private val windAttack = CardData(CardClass.SPECIAL, CardName.RAIRA_WIND_ATTACK , MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val windZenKai = CardData(CardClass.SPECIAL, CardName.RAIRA_WIND_ZEN_KAI, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.NONE)
    private val windCelestialSphere = CardData(CardClass.SPECIAL, CardName.RAIRA_WIND_CELESTIAL_SPHERE, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.REACTION)

    private fun rairaCardInit(){
        beastNail.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        stormSurgeAttack.setAttack(DistanceType.CONTINUOUS, Pair(2, 2), null, 1000, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        stormSurgeAttack.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    val auraDamage = gameStatus.getPlayer(nowPlayer).thunderGauge?.let {
                        if(game_status.getPlayer(nowPlayer).windGauge!! > it) it
                        else game_status.getPlayer(nowPlayer).windGauge!!
                    }?: 0
                    editedAuraDamage = auraDamage
                }
            }))
            null
        })
        reincarnationNail.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        reincarnationNail.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            while(true){
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1202
                ) { card -> card.card_data.card_type == CardType.ATTACK}?: break
                when(list.size){
                    0 -> break
                    1 -> {
                        game_status.popCardFrom(player, list[0], LocationEnum.DISCARD, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_TOP, true)
                        }
                        break
                    }
                }
            }
            null
        })
        windRun.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _->
            if(game_status.getAdjustDistance(player) >= 3){
                game_status.distanceToDust(2)
            }
            null
        })
        wisdomOfStormSurge.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            game_status.getPlayer(player).thunderGauge?.let{
                if(game_status.getPlayer(player).windGauge!! + it >= 4){
                    while(true){
                        val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1204
                        ) { card -> card.card_data.megami != MegamiEnum.RAIRA}?: break
                        when(list.size){
                            0 -> break
                            1 -> {
                                game_status.popCardFrom(player, list[0], LocationEnum.DISCARD, true)?.let { card ->
                                    game_status.insertCardTo(player, card, LocationEnum.YOUR_DECK_TOP, true)
                                }
                                break
                            }
                        }
                    }
                }
            }
            null
        })
        wisdomOfStormSurge.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) {card_number, player, game_status, _->
            game_status.gaugeIncreaseRequest(player, card_number)
            null
        })
        howling.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) {card_number, player, game_status, _->
            game_status.setShrink(player.opposite())
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1205)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.gaugeIncrease(player, true)
                        game_status.gaugeIncrease(player, false)
                    }
                    CommandEnum.SELECT_TWO -> {
                        for(card in game_status.getPlayer(player).hand.keys){
                            game_status.popCardFrom(player, card, LocationEnum.HAND, false)?.let {
                                if(it.card_data.canCover) game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                            }
                        }
                        game_status.getPlayer(player).thunderGauge?.let {
                            game_status.setGauge(player, true, it * 2)
                        }
                    }
                    else -> {
                        continue
                    }
                }
                break
            }
            null
        })
        windKick.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1206)){
                   CommandEnum.SELECT_ONE -> {
                       game_status.dustToDistance(3)
                   }
                    CommandEnum.SELECT_TWO -> {
                        game_status.distanceToDust(3)
                    }
                    else -> {
                        continue
                    }
                }
                break
            }
            null
        })
        thunderWindPunch.setSpecial(3)
        thunderWindPunch.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        thunderWindPunch.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                (buff_game_status.getPlayer(buff_player).thunderGauge ?: 0) >= 4
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })
        thunderWindPunch.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if((game_status.getPlayer(player).windGauge ?: 0) >= 4) 1
            else 0
        })
        summonThunder.setSpecial(6)
        summonThunder.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _->
            for (i in 1..ceil(((game_status.getPlayer(player).thunderGauge?: 0) / 2.0)).toInt()){
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.RAIRA_SUMMON_THUNDER, card_number, CardClass.NORMAL,
                        DistanceType.CONTINUOUS, 1,  1, Pair(0, 10), null, MegamiEnum.OBORO,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false))){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })
        windConsequenceBall.setSpecial(2)
        windConsequenceBall.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            val nowWind = game_status.getPlayer(player).windGauge?: 0
            if(nowWind >= 3){
                game_status.moveAdditionalCard(player, CardName.RAIRA_WIND_ATTACK, LocationEnum.SPECIAL_CARD)
            }
            if(nowWind >= 7){
                game_status.moveAdditionalCard(player, CardName.RAIRA_WIND_ATTACK, LocationEnum.SPECIAL_CARD)
            }
            if(nowWind >= 12){
                game_status.moveAdditionalCard(player, CardName.RAIRA_WIND_CELESTIAL_SPHERE, LocationEnum.SPECIAL_CARD)
            }
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })
        circularCircuit.setSpecial(2)
        circularCircuit.setEnchantment(3)
        circularCircuit.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE) {card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1210)){
                    CommandEnum.SELECT_ONE -> {
                        while(true){
                            val nowCommand = game_status.receiveCardEffectSelect(player, 1211)
                            if(selectDustToDistance(nowCommand, game_status)) break
                        }
                        game_status.gaugeIncreaseRequest(player, 1212)
                    }
                    CommandEnum.SELECT_NOT -> {}
                    else -> {
                        continue
                    }
                }
                break
            }
            null
        })
        windAttack.setSpecial(1)
        windAttack.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        windZenKai.setSpecial(1)
        windZenKai.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{card_number, player, game_status, _->
            val selected = game_status.selectCardFrom(player, player, listOf(LocationEnum.USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                1212, 1){
                true
            }?: return@ret null
            game_status.returnSpecialCard(player, selected[0])
            null
        })
        windZenKai.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.COST_BUFF){card_number, player, game_status, _ ->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true}, {cost ->
                cost - 1
            }))
            null
        })
        windCelestialSphere.setSpecial(4)
        windCelestialSphere.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1213)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.distanceToDust(1)
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.distanceToDust(2)
                    }
                    CommandEnum.SELECT_THREE -> {
                        game_status.distanceToDust(3)
                    }
                    CommandEnum.SELECT_FOUR -> {
                        game_status.distanceToDust(4)
                    }
                    CommandEnum.SELECT_FIVE -> {
                        game_status.distanceToDust(5)
                    }
                    CommandEnum.SELECT_SIX -> {
                        game_status.dustToDistance(1)
                    }
                    CommandEnum.SELECT_SEVEN -> {
                        game_status.dustToDistance(2)
                    }
                    CommandEnum.SELECT_EIGHT -> {
                        game_status.dustToDistance(3)
                    }
                    CommandEnum.SELECT_NINE -> {
                        game_status.dustToDistance(4)
                    }
                    CommandEnum.SELECT_TEN -> {
                        game_status.dustToDistance(5)
                    }
                    CommandEnum.SELECT_NOT -> {}
                    else -> {
                        continue
                    }
                }
                break
            }
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })
    }

    private val wonwol = CardData(CardClass.NORMAL, CardName.UTSURO_WON_WOL, MegamiEnum.UTSURO, CardType.ATTACK, SubType.NONE)
    private val blackWave = CardData(CardClass.NORMAL, CardName.UTSURO_BLACK_WAVE, MegamiEnum.UTSURO, CardType.ATTACK, SubType.NONE)
    private val harvest = CardData(CardClass.NORMAL, CardName.UTSURO_HARVEST, MegamiEnum.UTSURO, CardType.ATTACK, SubType.NONE)
    private val pressure = CardData(CardClass.NORMAL, CardName.UTSURO_PRESSURE, MegamiEnum.UTSURO, CardType.BEHAVIOR, SubType.NONE)
    private val shadowWing = CardData(CardClass.NORMAL, CardName.UTSURO_SHADOW_WING, MegamiEnum.UTSURO, CardType.BEHAVIOR, SubType.NONE)
    private val shadowWall = CardData(CardClass.NORMAL, CardName.UTSURO_SHADOW_WALL, MegamiEnum.UTSURO, CardType.BEHAVIOR, SubType.REACTION)
    private val yueHoeJu = CardData(CardClass.NORMAL, CardName.UTSURO_YUE_HOE_JU, MegamiEnum.UTSURO, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val hoeMyeol = CardData(CardClass.SPECIAL, CardName.UTSURO_HOE_MYEOL, MegamiEnum.UTSURO, CardType.BEHAVIOR, SubType.NONE)
    private val heoWi = CardData(CardClass.SPECIAL, CardName.UTSURO_HEO_WI, MegamiEnum.UTSURO, CardType.ENCHANTMENT, SubType.REACTION)
    private val jongMal = CardData(CardClass.SPECIAL, CardName.UTSURO_JONG_MAL, MegamiEnum.UTSURO, CardType.ENCHANTMENT, SubType.NONE)
    private val maSig = CardData(CardClass.SPECIAL, CardName.UTSURO_MA_SIG, MegamiEnum.UTSURO, CardType.BEHAVIOR, SubType.NONE)

    private fun hoejin(game_status: GameStatus) = game_status.dust >= 12

    //return is used to check kanae's go dong,
    private suspend fun moveResourceToDust(player: PlayerEnum, game_status: GameStatus, card_number: Int, number: Int): Int{
        val nowPlayer = game_status.getPlayer(player)
        var moveAura = 0
        var moveLife = 0
        var moveFlare = 0
        var canMove = nowPlayer.life + nowPlayer.aura + nowPlayer.flare
        if (canMove > number){
            canMove = number
        }
        for (nowMove in 1..canMove){
            while(true){
                when(game_status.receiveCardEffectSelect(player, card_number)){
                    CommandEnum.SELECT_ONE -> {
                        if(nowPlayer.aura >= (moveAura + 1)){
                            moveAura += 1
                            break
                        }
                        else{
                            continue
                        }
                    }
                    CommandEnum.SELECT_TWO -> {
                        if(nowPlayer.life >= (moveLife + 1)){
                            moveLife += 1
                            break
                        }
                        else{
                            continue
                        }
                    }
                    CommandEnum.SELECT_THREE -> {
                        if(nowPlayer.flare >= (moveFlare + 1)){
                            moveFlare += 1
                            break
                        }
                        else{
                            continue
                        }
                    }
                    else -> {
                        continue
                    }
                }
            }
        }
        game_status.lifeToDust(player, moveLife)
        game_status.flareToDust(player, moveFlare)
        game_status.auraToDust(player, moveAura)
        return canMove
    }

    private fun utsuroCardInit(){
        wonwol.setAttack(DistanceType.CONTINUOUS, Pair(5, 7), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wonwol.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE,
                {_, conditionGameStatus, _ -> hoejin(conditionGameStatus)},
                { _, _, attack ->
                    attack.plusMinusRange(1, true)
                }))
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE,
                {_, conditionGameStatus, _ -> hoejin(conditionGameStatus) },
                {_, _, attack ->
                    attack.run {
                        editedAuraDamage = 999
                    }
                }))
            null
        }))
        blackWave.setAttack(DistanceType.CONTINUOUS, Pair(4, 7), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        blackWave.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { card_number, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1301)
                { true }
                if(list == null){
                    break
                }
                else{
                    if (list.size == 1){
                        val card = game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, true)?: continue
                        game_status.insertCardTo(player.opposite(), card, LocationEnum.DISCARD, true)
                        break
                    }
                }
            }
            null
        })
        harvest.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 999, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        harvest.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) { card_number, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status,1302, 1)
            null
        })
        harvest.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) ret@{ card_number, player, game_status, _ ->
            while (true){
                val cardList = game_status.selectCardFrom(player.opposite(), player, listOf(LocationEnum.ENCHANTMENT_ZONE), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1302
                ) { true }?: return@ret null
                if(cardList.size == 1){
                    game_status.getCardFrom(player.opposite(), cardList[0], LocationEnum.ENCHANTMENT_ZONE)?.let {
                        game_status.cardToDust(player.opposite(), 2, it)
                        if(it.nap == 0){
                            game_status.enchantmentDestruction(player.opposite(), it)
                        }
                    }?: continue
                    break
                }
                else if(cardList.size > 2){
                    continue
                }
                else{
                    break
                }
            }
            null
        })
        pressure.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) ret@{ card_number, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status, 1303, 1)
            null
        })
        pressure.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) ret@{ _, player, game_status, _ ->
            if(hoejin(game_status)){
                game_status.setShrink(player.opposite())
            }
            null
        })
        shadowWing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_THIS_TURN_DISTANCE) ret@{ _, _, game_status, _ ->
            game_status.thisTurnDistance += 2
            null
        })
        shadowWing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_THIS_TURN_SWELL_DISTANCE) ret@{ _, _, game_status, _ ->
            game_status.thisTurnSwellDistance += 2
            null
        })
        shadowWall.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_CHANGE) ret@{ card_number, _, _, reactedAttack ->
            reactedAttack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.lifePlusMinus(-1)
                }))
            null
        })
        yueHoeJu.setEnchantment(2)
        yueHoeJu.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.auraToDust(player.opposite(), 3)
            null
        })
        yueHoeJu.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            if(hoejin(game_status)){
                game_status.dustToAura(player.opposite(), 2)
                game_status.lifeToDust(player.opposite(), 1)
            }
            null
        })
        hoeMyeol.setSpecial(24)
        hoeMyeol.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) {card_number, player, game_status, _->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card ->
                (card.card_data.card_name == CardName.UTSURO_HOE_MYEOL)}, {cost ->
                cost - game_status.dust
            }))
            null
        })
        hoeMyeol.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) ret@{card_number, player, game_status, _ ->
            game_status.lifeToDust(player.opposite(), 3)
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })
        heoWi.setSpecial(3)
        heoWi.setEnchantment(3)
        heoWi.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_CHANGE) { card_number, _, _, reactedAttack ->
            reactedAttack?.addRangeBuff(RangeBuff(card_number,1, RangeBufTag.MINUS, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(-1, true)
                }))
            null
        })
        heoWi.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player.opposite(), RangeBuff(card_number,1, RangeBufTag.MINUS, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(-1, true)
            }))
            null
        })
        heoWi.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.AFTER_ATTACK_EFFECT_INVALID_OTHER){_, _, _, _ ->
            null
        })
        heoWi.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_DEPLOYMENT_OTHER){_, player, game_status, _ ->
            game_status.getPlayer(player.opposite()).napBuff -= 1
            null
        })
        heoWi.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.AFTER_DESTRUCTION_EFFECT_INVALID_OTHER){_, _, _, _ ->
            null
        })
        jongMal.setSpecial(2)
        jongMal.setEnchantment(3)
        jongMal.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_GET_DAMAGE_BY_ATTACK){card_number, player, game_status, _ ->
            game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let { card ->
                game_status.cardToDust(player, card.nap, card)
                game_status.enchantmentDestruction(player, card)
            }
            null
        })
        jongMal.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.END_CURRENT_PHASE) {_, _, game_status, _ ->
            game_status.endCurrentPhase = true
            null
        })
        maSig.setSpecial(4)
        maSig.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player.opposite(), 1310)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.auraToDust(player.opposite(), 1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.flareToDust(player.opposite(), 2)
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
    }

    private val nanta = CardData(CardClass.NORMAL, CardName.YURINA_NAN_TA, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val beanBullet = CardData(CardClass.NORMAL, CardName.YURINA_BEAN_BULLET, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.REACTION)
    private val beanBulletText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
        game_status.setShrink(player.opposite())
        null
    }
    private val notCompletePobaram = CardData(CardClass.SPECIAL, CardName.YURINA_NOT_COMPLETE_POBARAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.REACTION)

    private fun yurinaA1CardInit(){
        nanta.setAttack(DistanceType.CONTINUOUS, Pair(2, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        nanta.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                gulSa(buff_player, buff_game_status)
            }, {_, _, attack ->
                attack.lifePlusMinus(2)
            }))
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, {buff_player, buff_game_status, _ ->
                gulSa(buff_player, buff_game_status)
            }, { _, _, attack ->
                attack.canNotReact()
            }))
            null
        })
        beanBullet.setEnchantment(1)
        beanBullet.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.YURINA_BEAN_BULLET, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 1,  999, Pair(0, 4), null, MegamiEnum.YURINA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                ).addTextAndReturn(beanBulletText))){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        notCompletePobaram.setSpecial(5)
        notCompletePobaram.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 3, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        notCompletePobaram.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, _, _, reactedAttack ->
            reactedAttack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-3)
                }))
            null
        })
        notCompletePobaram.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION){_, _, _, _->
            null
        })
    }

    private val soundOfIce = CardData(CardClass.NORMAL, CardName.SAINE_SOUND_OF_ICE, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.REACTION)
    private val accompaniment = CardData(CardClass.NORMAL, CardName.SAINE_ACCOMPANIMENT, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private val duetTanJuBingMyeong = CardData(CardClass.SPECIAL, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)


    private fun saineA1CardInit(){
        soundOfIce.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, react_attack ->
            if(react_attack == null){
                game_status.auraToDust(player.opposite(), 1)
            }
            else{
                game_status.auraToDust(player.opposite(), 2)
            }
            null
        })
        accompaniment.setEnchantment(4)
        accompaniment.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            if(!(game_status.logger.checkThisTurnDoAttack(player.opposite()))){
                game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                    buffRet@{ buff_player, buff_game_status, _ ->
                        for(card in buff_game_status.getPlayer(buff_player.opposite()).usedSpecialCard.values) {
                            if (card.card_data.megami != MegamiEnum.SAINE) {
                                return@buffRet true
                            }
                        }
                        return@buffRet false
                            },
                    { _, _, madeAttack ->
                        madeAttack.auraPlusMinus(-1)
                }))
            }
            null
        })
        accompaniment.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.COST_BUFF){card_number, player, game_status, _ ->
            for(card in game_status.getPlayer(player).usedSpecialCard.values) {
                if (card.card_data.megami == MegamiEnum.SAINE) {
                    game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                        true}, {cost ->
                        cost - 1
                    }))
                    break
                }
            }
            null
        })
        duetTanJuBingMyeong.setSpecial(2)
        duetTanJuBingMyeong.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION){_, _, _, _->
            null
        })
        duetTanJuBingMyeong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _->
            game_status.setShrink(player.opposite())
            null
        })
        duetTanJuBingMyeong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 213)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.drawCard(player, 1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.addConcentration(player)
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        duetTanJuBingMyeong.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, attack ->
                attack.megami != MegamiEnum.SAINE
            }, {_, _, attack ->
                attack.apply {
                    lifePlusMinus(1);
                }
            }))
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET, { _, _, attack ->
                attack.megami != MegamiEnum.SAINE },
                { _, _, attack ->
                    attack.makeInevitable()
                })
            )
            null
        })
        duetTanJuBingMyeong.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {_, cardNumber, _,
                                                                                        _, reconstruct, damage ->
                if(!reconstruct && damage){
                    game_status.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })
    }

    private val fireWave = CardData(CardClass.NORMAL, CardName.HIMIKA_FIRE_WAVE, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.NONE)
    private val satSui = CardData(CardClass.NORMAL, CardName.HIMIKA_SAT_SUI, MegamiEnum.HIMIKA, CardType.BEHAVIOR, SubType.NONE)
    private val enTenHimika = CardData(CardClass.SPECIAL, CardName.HIMIKA_EN_TEN_HIMIKA, MegamiEnum.HIMIKA, CardType.ATTACK, SubType.FULL_POWER)

    private fun himikaA1CardInit(){
        fireWave.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        fireWave.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {conditionPlayer, conditionGameStatus, _ -> yeonwhaAttack(conditionPlayer, conditionGameStatus)},
                {_, _, attack ->
                    attack.lifePlusMinus(1)
                }))
            null
        }))
        satSui.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            if (game_status.getPlayer(player).hand.size == 0) {
                game_status.auraToDust(player.opposite(), 2)
            }
            null
        })
        enTenHimika.setAttack(DistanceType.CONTINUOUS, Pair(0, 7), null, 1000, 1000,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        enTenHimika.setSpecial(5)
        enTenHimika.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = abs(gameStatus.getAdjustDistance(nowPlayer) - 8)
                    editedAuraDamage = temp
                    editedLifeDamage = temp
                }
            }))
            null
        })
        enTenHimika.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GAME_END) {_, player, game_status, _ ->
            game_status.gameEnd(null, player)
            null
        })
    }

    private val flowingPlay = CardData(CardClass.NORMAL, CardName.TOKOYO_FLOWING_PLAY, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val soundOfSun = CardData(CardClass.NORMAL, CardName.TOKOYO_SOUND_OF_SUN, MegamiEnum.TOKOYO, CardType.ENCHANTMENT, SubType.NONE)
    private val duetChitanYangMyeong = CardData(CardClass.SPECIAL, CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)

    private fun tokoyoA1CardInit(){
        flowingPlay.setAttack(DistanceType.CONTINUOUS, Pair(5, 5), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        flowingPlay.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, buffRet@{ buff_player, buff_game_status, _ ->
                for(card in buff_game_status.getPlayer(buff_player).usedSpecialCard.values) {
                    if (card.card_data.megami == MegamiEnum.TOKOYO) {
                        return@buffRet true
                    }
                }
                return@buffRet false
            }, { _, _, attack ->
                attack.canNotReact()
            }))
            null
        })
        flowingPlay.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CARD_DISCARD_PLACE_CHANGE) {card_number, player, game_status, _ ->
            if(kyochi(player, game_status)){
                for(card in game_status.getPlayer(player).usedSpecialCard.values) {
                    if (card.card_data.megami != MegamiEnum.TOKOYO) {
                        game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_TOP, card_number)
                        break
                    }
                }
            }
            null
        })
        soundOfSun.setEnchantment(2)
        soundOfSun.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_USE_REACT_CARD_YOUR_END) {_, player, game_status, _ ->
            game_status.dustToAura(player, 1)
            null
        })
        soundOfSun.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_MOVE_TOKEN) {_, player, game_status, _ ->
            if(game_status.turnPlayer == player) 1
            else 0
        })
        duetChitanYangMyeong.setSpecial(1)
        duetChitanYangMyeong.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 413)){
                    CommandEnum.SELECT_ONE -> {
                        val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 412, 1
                        ) { true }?: break
                        game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, false)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.DISCARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 413, 1
                        ) { card -> card.card_data.card_type == CardType.BEHAVIOR }?: break
                        game_status.popCardFrom(player, list[0], LocationEnum.DISCARD, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                        }
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        duetChitanYangMyeong.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {_, cardNumber, _,
                                                                                        _, reconstruct, damage ->
                if(!reconstruct && damage){
                    game_status.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })
    }

    private val spiritSik = CardData(CardClass.NORMAL, CardName.HONOKA_SPIRIT_SIK, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val guardianSik = CardData(CardClass.NORMAL, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, MegamiEnum.HONOKA, CardType.ATTACK, SubType.REACTION)
    private val assaultSik = CardData(CardClass.NORMAL, CardName.HONOKA_ASSAULT_SPIRIT_SIK, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val divineOuka = CardData(CardClass.NORMAL, CardName.HONOKA_DIVINE_OUKA, MegamiEnum.HONOKA, CardType.ATTACK, SubType.FULL_POWER)
    private val sakuraBlizzard = CardData(CardClass.NORMAL, CardName.HONOKA_SAKURA_BLIZZARD, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val yuGiGongJin = CardData(CardClass.NORMAL, CardName.HONOKA_UI_GI_GONG_JIN, MegamiEnum.HONOKA, CardType.ATTACK, SubType.FULL_POWER)
    private val sakuraWing = CardData(CardClass.NORMAL, CardName.HONOKA_SAKURA_WING, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val regeneration = CardData(CardClass.NORMAL, CardName.HONOKA_REGENERATION, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val sakuraAmulet = CardData(CardClass.NORMAL, CardName.HONOKA_SAKURA_AMULET, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.REACTION)
    private val honokaSparkle = CardData(CardClass.NORMAL, CardName.HONOKA_HONOKA_SPARKLE, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val command = CardData(CardClass.NORMAL, CardName.HONOKA_COMMAND, MegamiEnum.HONOKA, CardType.ENCHANTMENT, SubType.NONE)
    private val tailWind = CardData(CardClass.NORMAL, CardName.HONOKA_TAIL_WIND, MegamiEnum.HONOKA, CardType.ENCHANTMENT, SubType.NONE)
    private val chestWilling = CardData(CardClass.SPECIAL, CardName.HONOKA_CHEST_WILLINGNESS, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val handFlower = CardData(CardClass.SPECIAL, CardName.HONOKA_HAND_FLOWER, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val newOpening = CardData(CardClass.SPECIAL, CardName.HONOKA_A_NEW_OPENING, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val underFlag = CardData(CardClass.SPECIAL, CardName.HONOKA_UNDER_THE_NAME_OF_FLAG, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val fourSeason = CardData(CardClass.SPECIAL, CardName.HONOKA_UNDER_THE_NAME_OF_FLAG, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val bloomPath = CardData(CardClass.SPECIAL, CardName.HONOKA_FULL_BLOOM_PATH, MegamiEnum.HONOKA, CardType.ENCHANTMENT, SubType.NONE)

    private fun checkCardName(card_number: Int, cardName: CardName) = card_number.toCardName() == cardName

    private suspend fun requestCardChange(player: PlayerEnum, card_number: Int, game_status: GameStatus): Boolean{
        if(game_status.getCardOwner(card_number) == player) return false
        while (true){
            return when(game_status.receiveCardEffectSelect(player, card_number)){
                CommandEnum.SELECT_ONE -> {
                    true
                }
                CommandEnum.SELECT_TWO -> {
                    false
                }
                else -> {
                    continue
                }
            }
        }
    }

    private suspend fun requestDeckBelow(player: PlayerEnum, game_status: GameStatus): Boolean{
        while (true){
            return when(game_status.receiveCardEffectSelect(player, 1403)){
                CommandEnum.SELECT_ONE -> {
                    true
                }
                CommandEnum.SELECT_TWO -> {
                    false
                }
                else -> {
                    continue
                }
            }
        }
    }

    private fun countTokenFive(player: PlayerEnum?, game_status: GameStatus): Int{
        var count = 0
        val player1 = game_status.getPlayer(player?: PlayerEnum.PLAYER1)
        val player2 = game_status.getPlayer((player?: PlayerEnum.PLAYER1).opposite())
        if(player1.aura == 5) count += 1
        if(player1.flare == 5) count += 1
        if(player1.life == 5) count += 1
        if(game_status.countToken(player?: PlayerEnum.PLAYER1, LocationEnum.USED_CARD) == 5) count += 1
        if(game_status.countToken(player?: PlayerEnum.PLAYER1, LocationEnum.ENCHANTMENT_ZONE) == 5) count += 1
        if(game_status.dust == 5) count += 1
        if(game_status.distanceToken == 5) count += 1
        if(player != null) return count
        if(player2.aura == 5) count += 1
        if(player2.flare == 5) count += 1
        if(player2.life == 5) count += 1
        if(game_status.countToken(PlayerEnum.PLAYER2, LocationEnum.USED_CARD) == 5) count += 1
        if(game_status.countToken(PlayerEnum.PLAYER2, LocationEnum.ENCHANTMENT_ZONE) == 5) count += 1
        return count
    }

    fun honokaCardInit(){
        spiritSik.setAttack(DistanceType.CONTINUOUS, Pair(2, 8), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        spiritSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(checkCardName(card_number, CardName.HONOKA_SPIRIT_SIK) && requestCardChange(player, 1400, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.DISCARD)
                    }
                }
            }
            null
        })
        guardianSik.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        guardianSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.dustToAura(player, 1)
            null
        })
        guardianSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(checkCardName(card_number, CardName.HONOKA_GUARDIAN_SPIRIT_SIK) && requestCardChange(player, 1401, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.DISCARD)
                    }
                }
            }
            null
        })
        assaultSik.setAttack(DistanceType.CONTINUOUS, Pair(5, 5), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        assaultSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.dustToLife(player, 1)
            null
        })
        assaultSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(checkCardName(card_number, CardName.HONOKA_ASSAULT_SPIRIT_SIK) && requestCardChange(player, 1402, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_DIVINE_OUKA, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_DIVINE_OUKA, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_DIVINE_OUKA, LocationEnum.DISCARD)
                    }
                }
            }
            null
        })
        divineOuka.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 4, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        divineOuka.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.dustToAura(player, 2)
            null
        })
        sakuraBlizzard.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        sakuraBlizzard.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player.opposite(), 1404)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.distanceToAura(player, 1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.auraToDistance(player.opposite(), 1)
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        yuGiGongJin.setAttack(DistanceType.CONTINUOUS, Pair(2, 9), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        yuGiGongJin.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DRAW_CARD) {card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1405)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.drawCard(player, 1)
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        yuGiGongJin.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            while (true){
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1405
                ) { true }?: break
                if (list.size == 0){
                    break
                }
                else if (list.size == 1){
                    game_status.popCardFrom(player, list[0], LocationEnum.HAND, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                    }
                    break
                }
                else {
                    continue
                }
            }
            null
        })
        yuGiGongJin.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1407)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_BELOW, card_number)
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        sakuraWing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            while (true){
                when(game_status.receiveCardEffectSelect(player, 1406)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.dustToDistance(2)
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        game_status.distanceToDust(2)
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        sakuraWing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(checkCardName(card_number, CardName.HONOKA_SAKURA_WING)){
                game_status.getCardFrom(player, CardName.HONOKA_REGENERATION, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    game_status.moveAdditionalCard(player, CardName.HONOKA_REGENERATION, LocationEnum.DISCARD)
                }
            }
            null
        })
        regeneration.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.dustToAura(player, 1)
            game_status.dustToFlare(player, 1)
            null
        })
        regeneration.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(checkCardName(card_number, CardName.HONOKA_REGENERATION)){
                game_status.getCardFrom(player, CardName.HONOKA_SAKURA_WING, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_WING, LocationEnum.DISCARD)
                }
            }
            null
        })
        sakuraAmulet.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, react_attack ->
            while (true){
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1408
                ) { card -> card.card_data.canCover }?: break
                if (list.size == 0){
                    break
                }
                else if (list.size == 1){
                    game_status.popCardFrom(player, list[0], LocationEnum.HAND, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                    }
                    if(react_attack?.card_class != CardClass.SPECIAL){
                        react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET, { _, _, _ ->
                            true
                        }, { _, _, attack ->
                            attack.makeNotValid()
                        }))
                    }
                    break
                }
                else {
                    continue
                }
            }
            null
        })
        sakuraAmulet.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(checkCardName(card_number, CardName.HONOKA_SAKURA_AMULET) && requestCardChange(player, 1408, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.DISCARD)
                    }
                }
            }
            null
        })
        honokaSparkle.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        command.setEnchantment(3)
        command.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.HONOKA_COMMAND, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 1,  1, Pair(1, 5), null, MegamiEnum.HONOKA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                )) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        tailWind.setEnchantment(3)
        tailWind.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(1, false)
                }))
            null
        })
        chestWilling.setSpecial(5)
        chestWilling.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(checkCardName(card_number, CardName.HONOKA_CHEST_WILLINGNESS)){
                game_status.getCardFrom(player, CardName.HONOKA_HAND_FLOWER, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    it.special_card_state = SpecialCardEnum.PLAYED
                    game_status.moveAdditionalCard(player, CardName.HONOKA_HAND_FLOWER, LocationEnum.USED_CARD)
                    game_status.returnSpecialCard(player, it.card_number)
                }
            }
            null
        })
        handFlower.setSpecial(0)
        handFlower.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,  201413)
            null
        })
        handFlower.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) {_, player, game_status, _ ->
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND, 201413)
            null
        })
        handFlower.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.CONDITION_ADD_DO_WIND_AROUND) ret@{_, player, game_status, _ ->
            if(game_status.getPlayerAura(player) != 0 || game_status.dust != 0) 1
            else 0
        })
        handFlower.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DO_WIND_AROUND) ret@{card_number, player, game_status, _ ->
            if(game_status.getPlayerAura(player) == 0 && game_status.dust == 0){
                return@ret 0
            }
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1413)){
                    CommandEnum.SELECT_ONE -> {
                        if (game_status.getPlayerAura(player) == 0){
                            continue
                        }
                        game_status.getCardFrom(player, card_number, LocationEnum.USED_CARD)?.let {
                            game_status.auraToCard(player, 1, it, LocationEnum.USED_CARD)
                            if(it.nap == 5){
                                if(checkCardName(card_number, CardName.HONOKA_HAND_FLOWER)){
                                    game_status.getCardFrom(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.ADDITIONAL_CARD)?.let { additionalCard ->
                                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                                        additionalCard.special_card_state = SpecialCardEnum.PLAYED
                                        game_status.moveAdditionalCard(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.USED_CARD)
                                        game_status.returnSpecialCard(player, additionalCard.card_number)
                                    }
                                }
                            }
                        }
                        return@ret 1
                    }
                    CommandEnum.SELECT_TWO -> {
                        if (game_status.dust == 0){
                            continue
                        }
                        game_status.getCardFrom(player, card_number, LocationEnum.USED_CARD)?.let {
                            game_status.dustToCard(player, 1, it, LocationEnum.USED_CARD)
                            if(it.nap == 5){
                                if(checkCardName(card_number, CardName.HONOKA_HAND_FLOWER)){
                                    game_status.getCardFrom(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.ADDITIONAL_CARD)?.let { additionalCard ->
                                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                                        additionalCard.special_card_state = SpecialCardEnum.PLAYED
                                        game_status.moveAdditionalCard(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.USED_CARD)
                                        game_status.returnSpecialCard(player, additionalCard.card_number)
                                    }
                                }
                            }
                        }
                        return@ret 1
                    }
                    CommandEnum.SELECT_NOT -> {
                        if(game_status.dust == 0 || game_status.getPlayerAura(player) == game_status.getPlayer(player).maxAura) continue
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            0
        })
        newOpening.setSpecial(5)
        newOpening.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {_, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = countTokenFive(null, gameStatus)
                    editedAuraDamage = temp
                    editedLifeDamage = temp
                }
            }))
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.HONOKA_A_NEW_OPENING, card_number, CardClass.NORMAL,
                    DistanceType.CONTINUOUS, 1000,  1000, Pair(0, 10), null, MegamiEnum.YURINA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                ))){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        underFlag.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        underFlag.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_TEXT_TO_ATTACK) { card_number, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1415
                ) { true }?: break
                if (list.size == 1){
                    game_status.getPlayer(player).pre_attack_card?.addTextAndReturn(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
                        list[0]
                    })
                    game_status.getPlayer(player).pre_attack_card?.addTextAndReturn(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
                        list[0]
                    })
                    break
                }
                else if(list.size == 0) {
                    break
                }
            }
            null
        })
        fourSeason.setSpecial(1)
        fourSeason.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            while (true){
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1416
                ) { true }?: break
                if (list.size == 1){
                    game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                    }
                    break
                }
                else if (list.size == 0){
                    break
                }
            }
            null
        })
        fourSeason.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.MOVE_CARD) ret@{_, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, 1416)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.drawCard(player, 1)
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        fourSeason.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            while (true){
                val list = game_status.selectCardFrom(player, player, listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 1417
                ) { card -> card.card_data.canCover }?: break
                if (list.size == 1){
                    game_status.popCardFrom(player, list[0], LocationEnum.HAND, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                    }
                    break
                }
            }
            null
        })
        fourSeason.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addAdditionalListener(player, Listener(player, card_number)ret@{gameStatus, cardNumber, _, _, _, _ ->
                while(true){
                    when(game_status.receiveCardEffectSelect(player, 1417)){
                        CommandEnum.SELECT_ONE -> {
                            return@ret false
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {
                            continue
                        }
                    }
                }
                gameStatus.popCardFrom(player, cardNumber, LocationEnum.USED_CARD, true)?.let {
                    gameStatus.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                    gameStatus.moveAdditionalCard(player, CardName.HONOKA_FOUR_SEASON_BACK, LocationEnum.SPECIAL_CARD)
                }
                true
            })
            null
        })
        bloomPath.setSpecial(2)
        bloomPath.setEnchantment(5)
        bloomPath.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE) {_, player, game_status, _ ->
            if(game_status.getPlayerAura(player) >= 5){
                LocationEnum.YOUR_FLARE.real_number
            }
            else{
                LocationEnum.YOUR_AURA.real_number
            }
        })

    }

    fun init(){
        yurinaCardInit()
        saineCardInit()
        himikaCardInit()
        tokoyoCardInit()
        oboroCardInit()
        yukihiCardInit()
        shinraCardInit()
        haganeCardInit()
        chikageCardInit()
        kururuCardInit()
        thallyaCardInit()
        rairaCardInit()
        utsuroCardInit()
        yurinaA1CardInit()
        saineA1CardInit()
        himikaA1CardInit()
        tokoyoA1CardInit()
        honokaCardInit()

        hashMapInit()
        hashMapTest()
    }

    fun isPoison(card_number: Int): Boolean{
        return when(card_number){
            1, 995, 996, 997, 998, 999, 10995, 10996, 10997, 10998, 10999 -> true
            else -> false
        }
    }
}