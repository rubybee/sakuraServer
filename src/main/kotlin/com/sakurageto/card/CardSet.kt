package com.sakurageto.card

import com.sakurageto.gamelogic.*
import com.sakurageto.gamelogic.GameStatus.Companion.END_PHASE
import com.sakurageto.gamelogic.GameStatus.Companion.START_PHASE
import com.sakurageto.gamelogic.GameStatus.Companion.START_PHASE_REDUCE_NAP
import com.sakurageto.gamelogic.log.Log
import com.sakurageto.gamelogic.log.LogText
import com.sakurageto.gamelogic.storyboard.Act
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.sendSimpleCommand
import io.ktor.network.sockets.*
import java.util.EnumMap
import kotlin.collections.HashMap
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

data class Kikou(var attack: Int = 0, var behavior: Int = 0, var enchantment: Int = 0, var reaction: Int = 0, var fullPower: Int = 0){
    fun add(card: Card){
        when(card.card_data.card_type){
            CardType.ATTACK -> this.attack += 1
            CardType.BEHAVIOR -> this.behavior += 1
            CardType.ENCHANTMENT -> this.enchantment += 1
            CardType.UNDEFINED -> {}
        }
        when(card.card_data.sub_type){
            SubType.FULL_POWER -> this.fullPower += 1
            SubType.REACTION -> this.reaction += 1
            SubType.NONE -> {}
            SubType.UNDEFINED -> {}
        }
    }
}

object CardSet {
    private val cardNumberHashmap = HashMap<Int, CardName>()
    private val cardDataHashmap = EnumMap<CardName, CardData>(CardName::class.java)

    fun Int.toCardName(): CardName = cardNumberHashmap[this]?: CardName.CARD_UNNAME
    fun CardName.toCardData(): CardData = cardDataHashmap[this]?: unused

    private fun hashMapTest(){
        val cardNameList = CardName.values()
        for(cardName in cardNameList){
            if(cardName == CardName.CARD_UNNAME || cardName == CardName.POISON_ANYTHING || cardName == CardName.SOLDIER_ANYTHING) {
                continue
            }
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
        cardNumberHashmap[NUMBER_CARD_UNAME] = CardName.CARD_UNNAME
        cardNumberHashmap[NUMBER_POISON_ANYTHING] = CardName.POISON_ANYTHING
        cardNumberHashmap[NUMBER_SOLDIER_ANYTHING] = CardName.SOLDIER_ANYTHING

        cardNumberHashmap[NUMBER_YURINA_CHAM] = CardName.YURINA_CHAM
        cardNumberHashmap[NUMBER_YURINA_ILSUM] = CardName.YURINA_ILSUM
        cardNumberHashmap[NUMBER_YURINA_JARUCHIGI] = CardName.YURINA_JARUCHIGI
        cardNumberHashmap[NUMBER_YURINA_GUHAB] = CardName.YURINA_GUHAB
        cardNumberHashmap[NUMBER_YURINA_GIBACK] = CardName.YURINA_GIBACK
        cardNumberHashmap[NUMBER_YURINA_APDO] = CardName.YURINA_APDO
        cardNumberHashmap[NUMBER_YURINA_GIYENBANJO] = CardName.YURINA_GIYENBANJO
        cardNumberHashmap[NUMBER_YURINA_WOLYUNGNACK] = CardName.YURINA_WOLYUNGNACK
        cardNumberHashmap[NUMBER_YURINA_POBARAM] = CardName.YURINA_POBARAM
        cardNumberHashmap[NUMBER_YURINA_JJOCKBAE] = CardName.YURINA_JJOCKBAE
        cardNumberHashmap[NUMBER_YURINA_JURUCK] = CardName.YURINA_JURUCK
        cardNumberHashmap[NUMBER_YURINA_NAN_TA] = CardName.YURINA_NAN_TA
        cardNumberHashmap[NUMBER_YURINA_BEAN_BULLET] = CardName.YURINA_BEAN_BULLET
        cardNumberHashmap[NUMBER_YURINA_NOT_COMPLETE_POBARAM] = CardName.YURINA_NOT_COMPLETE_POBARAM

        cardNumberHashmap[NUMBER_SAINE_DOUBLEBEGI] = CardName.SAINE_DOUBLEBEGI
        cardNumberHashmap[NUMBER_SAINE_HURUBEGI] = CardName.SAINE_HURUBEGI
        cardNumberHashmap[NUMBER_SAINE_MOOGECHOO] = CardName.SAINE_MOOGECHOO
        cardNumberHashmap[NUMBER_SAINE_GANPA] = CardName.SAINE_GANPA
        cardNumberHashmap[NUMBER_SAINE_GWONYUCK] = CardName.SAINE_GWONYUCK
        cardNumberHashmap[NUMBER_SAINE_CHOONGEMJUNG] = CardName.SAINE_CHOONGEMJUNG
        cardNumberHashmap[NUMBER_SAINE_MOOEMBUCK] = CardName.SAINE_MOOEMBUCK
        cardNumberHashmap[NUMBER_SAINE_YULDONGHOGEK] = CardName.SAINE_YULDONGHOGEK
        cardNumberHashmap[NUMBER_SAINE_HANGMUNGGONGJIN] = CardName.SAINE_HANGMUNGGONGJIN
        cardNumberHashmap[NUMBER_SAINE_EMMOOSHOEBING] = CardName.SAINE_EMMOOSHOEBING
        cardNumberHashmap[NUMBER_SAINE_JONGGEK] = CardName.SAINE_JONGGEK
        cardNumberHashmap[NUMBER_SAINE_SOUND_OF_ICE] = CardName.SAINE_SOUND_OF_ICE
        cardNumberHashmap[NUMBER_SAINE_ACCOMPANIMENT] = CardName.SAINE_ACCOMPANIMENT
        cardNumberHashmap[NUMBER_SAINE_DUET_TAN_JU_BING_MYEONG] = CardName.SAINE_DUET_TAN_JU_BING_MYEONG
        cardNumberHashmap[NUMBER_SAINE_BETRAYAL] = CardName.SAINE_BETRAYAL
        cardNumberHashmap[NUMBER_SAINE_FLOWING_WALL] = CardName.SAINE_FLOWING_WALL
        cardNumberHashmap[NUMBER_SAINE_JEOL_CHANG_JEOL_HWA] = CardName.SAINE_JEOL_CHANG_JEOL_HWA

        cardNumberHashmap[NUMBER_HIMIKA_SHOOT] = CardName.HIMIKA_SHOOT
        cardNumberHashmap[NUMBER_HIMIKA_RAPIDFIRE] = CardName.HIMIKA_RAPIDFIRE
        cardNumberHashmap[NUMBER_HIMIKA_MAGNUMCANON] = CardName.HIMIKA_MAGNUMCANON
        cardNumberHashmap[NUMBER_HIMIKA_FULLBURST] = CardName.HIMIKA_FULLBURST
        cardNumberHashmap[NUMBER_HIMIKA_BACKSTEP] = CardName.HIMIKA_BACKSTEP
        cardNumberHashmap[NUMBER_HIMIKA_BACKDRAFT] = CardName.HIMIKA_BACKDRAFT
        cardNumberHashmap[NUMBER_HIMIKA_SMOKE] = CardName.HIMIKA_SMOKE
        cardNumberHashmap[NUMBER_HIMIKA_REDBULLET] = CardName.HIMIKA_REDBULLET
        cardNumberHashmap[NUMBER_HIMIKA_CRIMSONZERO] = CardName.HIMIKA_CRIMSONZERO
        cardNumberHashmap[NUMBER_HIMIKA_SCARLETIMAGINE] = CardName.HIMIKA_SCARLETIMAGINE
        cardNumberHashmap[NUMBER_HIMIKA_BURMILIONFIELD] = CardName.HIMIKA_BURMILIONFIELD
        cardNumberHashmap[NUMBER_HIMIKA_FIRE_WAVE] = CardName.HIMIKA_FIRE_WAVE
        cardNumberHashmap[NUMBER_HIMIKA_SAT_SUI] = CardName.HIMIKA_SAT_SUI
        cardNumberHashmap[NUMBER_HIMIKA_EN_TEN_HIMIKA] = CardName.HIMIKA_EN_TEN_HIMIKA

        cardNumberHashmap[NUMBER_TOKOYO_BITSUNERIGI] = CardName.TOKOYO_BITSUNERIGI
        cardNumberHashmap[NUMBER_TOKOYO_WOOAHHANTAGUCK] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardNumberHashmap[NUMBER_TOKOYO_RUNNINGRABIT] = CardName.TOKOYO_RUNNINGRABIT
        cardNumberHashmap[NUMBER_TOKOYO_POETDANCE] = CardName.TOKOYO_POETDANCE
        cardNumberHashmap[NUMBER_TOKOYO_FLIPFAN] = CardName.TOKOYO_FLIPFAN
        cardNumberHashmap[NUMBER_TOKOYO_WINDSTAGE] = CardName.TOKOYO_WINDSTAGE
        cardNumberHashmap[NUMBER_TOKOYO_SUNSTAGE] = CardName.TOKOYO_SUNSTAGE
        cardNumberHashmap[NUMBER_TOKOYO_KUON] = CardName.TOKOYO_KUON
        cardNumberHashmap[NUMBER_TOKOYO_THOUSANDBIRD] = CardName.TOKOYO_THOUSANDBIRD
        cardNumberHashmap[NUMBER_TOKOYO_ENDLESSWIND] = CardName.TOKOYO_ENDLESSWIND
        cardNumberHashmap[NUMBER_TOKOYO_TOKOYOMOON] = CardName.TOKOYO_TOKOYOMOON
        cardNumberHashmap[NUMBER_TOKOYO_FLOWING_PLAY] = CardName.TOKOYO_FLOWING_PLAY
        cardNumberHashmap[NUMBER_TOKOYO_SOUND_OF_SUN] = CardName.TOKOYO_SOUND_OF_SUN
        cardNumberHashmap[NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG] = CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG
        cardNumberHashmap[NUMBER_TOKOYO_PASSING_FEAR] = CardName.TOKOYO_PASSING_FEAR
        cardNumberHashmap[NUMBER_TOKOYO_RELIC_EYE] = CardName.TOKOYO_RELIC_EYE
        cardNumberHashmap[NUMBER_TOKOYO_EIGHT_SAKURA_IN_VAIN] = CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN

        cardNumberHashmap[NUMBER_OBORO_WIRE] = CardName.OBORO_WIRE
        cardNumberHashmap[NUMBER_OBORO_SHADOWCALTROP] = CardName.OBORO_SHADOWCALTROP
        cardNumberHashmap[NUMBER_OBORO_ZANGEKIRANBU] = CardName.OBORO_ZANGEKIRANBU
        cardNumberHashmap[NUMBER_OBORO_NINJAWALK] = CardName.OBORO_NINJAWALK
        cardNumberHashmap[NUMBER_OBORO_INDUCE] = CardName.OBORO_INDUCE
        cardNumberHashmap[NUMBER_OBORO_CLONE] = CardName.OBORO_CLONE
        cardNumberHashmap[NUMBER_OBORO_BIOACTIVITY] = CardName.OBORO_BIOACTIVITY
        cardNumberHashmap[NUMBER_OBORO_KUMASUKE] = CardName.OBORO_KUMASUKE
        cardNumberHashmap[NUMBER_OBORO_TOBIKAGE] = CardName.OBORO_TOBIKAGE
        cardNumberHashmap[NUMBER_OBORO_ULOO] = CardName.OBORO_ULOO
        cardNumberHashmap[NUMBER_OBORO_MIKAZRA] = CardName.OBORO_MIKAZRA
        cardNumberHashmap[NUMBER_OBORO_SHURIKEN] = CardName.OBORO_SHURIKEN
        cardNumberHashmap[NUMBER_OBORO_AMBUSH] = CardName.OBORO_AMBUSH
        cardNumberHashmap[NUMBER_OBORO_BRANCH_OF_DIVINE] = CardName.OBORO_BRANCH_OF_DIVINE
        cardNumberHashmap[NUMBER_OBORO_LAST_CRYSTAL] = CardName.OBORO_LAST_CRYSTAL

        cardNumberHashmap[NUMBER_YUKIHI_YUKIHI] = CardName.YUKIHI_YUKIHI
        cardNumberHashmap[NUMBER_YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardNumberHashmap[NUMBER_YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardNumberHashmap[NUMBER_YUKIHI_PUSH_OUT_SLASH_PULL] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardNumberHashmap[NUMBER_YUKIHI_SWING_SLASH_STAB] = CardName.YUKIHI_SWING_SLASH_STAB
        cardNumberHashmap[NUMBER_YUKIHI_TURN_UMBRELLA] = CardName.YUKIHI_TURN_UMBRELLA
        cardNumberHashmap[NUMBER_YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardNumberHashmap[NUMBER_YUKIHI_MAKE_CONNECTION] = CardName.YUKIHI_MAKE_CONNECTION
        cardNumberHashmap[NUMBER_YUKIHI_FLUTTERING_SNOWFLAKE] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardNumberHashmap[NUMBER_YUKIHI_SWAYING_LAMPLIGHT] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardNumberHashmap[NUMBER_YUKIHI_CLINGY_MIND] = CardName.YUKIHI_CLINGY_MIND
        cardNumberHashmap[NUMBER_YUKIHI_SWIRLING_GESTURE] = CardName.YUKIHI_SWIRLING_GESTURE
        cardNumberHashmap[NUMBER_YUKIHI_HELP_SLASH_THREAT] = CardName.YUKIHI_HELP_SLASH_THREAT
        cardNumberHashmap[NUMBER_YUKIHI_THREAD_SLASH_RAW_THREAD] = CardName.YUKIHI_THREAD_SLASH_RAW_THREAD
        cardNumberHashmap[NUMBER_YUKIHI_FLUTTERING_COLLAR] = CardName.YUKIHI_FLUTTERING_COLLAR

        cardNumberHashmap[NUMBER_SHINRA_SHINRA] = CardName.SHINRA_SHINRA
        cardNumberHashmap[NUMBER_SHINRA_IBLON] = CardName.SHINRA_IBLON
        cardNumberHashmap[NUMBER_SHINRA_BANLON] = CardName.SHINRA_BANLON
        cardNumberHashmap[NUMBER_SHINRA_KIBEN] = CardName.SHINRA_KIBEN
        cardNumberHashmap[NUMBER_SHINRA_INYONG] = CardName.SHINRA_INYONG
        cardNumberHashmap[NUMBER_SHINRA_SEONDONG] = CardName.SHINRA_SEONDONG
        cardNumberHashmap[NUMBER_SHINRA_JANGDAM] = CardName.SHINRA_JANGDAM
        cardNumberHashmap[NUMBER_SHINRA_NONPA] = CardName.SHINRA_NONPA
        cardNumberHashmap[NUMBER_SHINRA_WANJEON_NONPA] = CardName.SHINRA_WANJEON_NONPA
        cardNumberHashmap[NUMBER_SHINRA_DASIG_IHAE] = CardName.SHINRA_DASIG_IHAE
        cardNumberHashmap[NUMBER_SHINRA_CHEONJI_BANBAG] = CardName.SHINRA_CHEONJI_BANBAG
        cardNumberHashmap[NUMBER_SHINRA_SAMRA_BAN_SHO] = CardName.SHINRA_SAMRA_BAN_SHO
        cardNumberHashmap[NUMBER_SHINRA_ZHEN_YEN] = CardName.SHINRA_ZHEN_YEN
        cardNumberHashmap[NUMBER_SHINRA_SA_DO] = CardName.SHINRA_SA_DO
        cardNumberHashmap[NUMBER_SHINRA_ZEN_CHI_KYO_TEN] = CardName.SHINRA_ZEN_CHI_KYO_TEN

        cardNumberHashmap[NUMBER_HAGANE_CENTRIFUGAL_ATTACK] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardNumberHashmap[NUMBER_HAGANE_FOUR_WINDED_EARTHQUAKE] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardNumberHashmap[NUMBER_HAGANE_GROUND_BREAKING] = CardName.HAGANE_GROUND_BREAKING
        cardNumberHashmap[NUMBER_HAGANE_HYPER_RECOIL] = CardName.HAGANE_HYPER_RECOIL
        cardNumberHashmap[NUMBER_HAGANE_WON_MU_RUYN] = CardName.HAGANE_WON_MU_RUYN
        cardNumberHashmap[NUMBER_HAGANE_RING_A_BELL] = CardName.HAGANE_RING_A_BELL
        cardNumberHashmap[NUMBER_HAGANE_GRAVITATION_FIELD] = CardName.HAGANE_GRAVITATION_FIELD
        cardNumberHashmap[NUMBER_HAGANE_GRAND_SKY_HOLE_CRASH] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardNumberHashmap[NUMBER_HAGANE_GRAND_BELL_MEGALOBEL] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardNumberHashmap[NUMBER_HAGANE_GRAND_GRAVITATION_ATTRACT] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardNumberHashmap[NUMBER_HAGANE_GRAND_MOUNTAIN_RESPECT] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT
        cardNumberHashmap[NUMBER_HAGANE_BONFIRE] = CardName.HAGANE_BONFIRE
        cardNumberHashmap[NUMBER_HAGANE_WHEEL_SKILL] = CardName.HAGANE_WHEEL_SKILL
        cardNumberHashmap[NUMBER_HAGANE_GRAND_SOFT_MATERIAL] = CardName.HAGANE_GRAND_SOFT_MATERIAL
        cardNumberHashmap[NUMBER_HAGANE_SOFT_ATTACK] = CardName.HAGANE_SOFT_ATTACK

        cardNumberHashmap[NUMBER_CHIKAGE_THROW_KUNAI] = CardName.CHIKAGE_THROW_KUNAI
        cardNumberHashmap[NUMBER_CHIKAGE_POISON_NEEDLE] = CardName.CHIKAGE_POISON_NEEDLE
        cardNumberHashmap[NUMBER_CHIKAGE_TO_ZU_CHU] = CardName.CHIKAGE_TO_ZU_CHU
        cardNumberHashmap[NUMBER_CHIKAGE_CUTTING_NECK] = CardName.CHIKAGE_CUTTING_NECK
        cardNumberHashmap[NUMBER_CHIKAGE_POISON_SMOKE] = CardName.CHIKAGE_POISON_SMOKE
        cardNumberHashmap[NUMBER_CHIKAGE_TIP_TOEING] = CardName.CHIKAGE_TIP_TOEING
        cardNumberHashmap[NUMBER_CHIKAGE_MUDDLE] = CardName.CHIKAGE_MUDDLE
        cardNumberHashmap[NUMBER_CHIKAGE_DEADLY_POISON] = CardName.CHIKAGE_DEADLY_POISON
        cardNumberHashmap[NUMBER_CHIKAGE_HAN_KI_POISON] = CardName.CHIKAGE_HAN_KI_POISON
        cardNumberHashmap[NUMBER_CHIKAGE_REINCARNATION_POISON] = CardName.CHIKAGE_REINCARNATION_POISON
        cardNumberHashmap[NUMBER_CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
        cardNumberHashmap[NUMBER_CHIKAGE_TRICK_UMBRELLA] = CardName.CHIKAGE_TRICK_UMBRELLA
        cardNumberHashmap[NUMBER_CHIKAGE_STRUGGLE] = CardName.CHIKAGE_STRUGGLE
        cardNumberHashmap[NUMBER_CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON] = CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON
        cardNumberHashmap[NUMBER_POISON_PARALYTIC] = CardName.POISON_PARALYTIC
        cardNumberHashmap[NUMBER_POISON_HALLUCINOGENIC] = CardName.POISON_HALLUCINOGENIC
        cardNumberHashmap[NUMBER_POISON_RELAXATION] = CardName.POISON_RELAXATION
        cardNumberHashmap[NUMBER_POISON_DEADLY_1] = CardName.POISON_DEADLY_1
        cardNumberHashmap[NUMBER_POISON_DEADLY_2] = CardName.POISON_DEADLY_2

        cardNumberHashmap[NUMBER_KURURU_ELEKITTEL] = CardName.KURURU_ELEKITTEL
        cardNumberHashmap[NUMBER_KURURU_ACCELERATOR] = CardName.KURURU_ACCELERATOR
        cardNumberHashmap[NUMBER_KURURU_KURURUOONG] = CardName.KURURU_KURURUOONG
        cardNumberHashmap[NUMBER_KURURU_TORNADO] = CardName.KURURU_TORNADO
        cardNumberHashmap[NUMBER_KURURU_REGAINER] = CardName.KURURU_REGAINER
        cardNumberHashmap[NUMBER_KURURU_MODULE] = CardName.KURURU_MODULE
        cardNumberHashmap[NUMBER_KURURU_REFLECTOR] = CardName.KURURU_REFLECTOR
        cardNumberHashmap[NUMBER_KURURU_DRAIN_DEVIL] = CardName.KURURU_DRAIN_DEVIL
        cardNumberHashmap[NUMBER_KURURU_BIG_GOLEM] = CardName.KURURU_BIG_GOLEM
        cardNumberHashmap[NUMBER_KURURU_INDUSTRIA] = CardName.KURURU_INDUSTRIA
        cardNumberHashmap[NUMBER_KURURU_DUPLICATED_GEAR_1] = CardName.KURURU_DUPLICATED_GEAR_1
        cardNumberHashmap[NUMBER_KURURU_DUPLICATED_GEAR_2] = CardName.KURURU_DUPLICATED_GEAR_2
        cardNumberHashmap[NUMBER_KURURU_DUPLICATED_GEAR_3] = CardName.KURURU_DUPLICATED_GEAR_3
        cardNumberHashmap[NUMBER_KURURU_KANSHOUSOUCHI_KURURUSIK] = CardName.KURURU_KANSHOUSOUCHI_KURURUSIK
        cardNumberHashmap[NUMBER_KURURU_ANALYZE] = CardName.KURURU_ANALYZE
        cardNumberHashmap[NUMBER_KURURU_DAUZING] = CardName.KURURU_DAUZING
        cardNumberHashmap[NUMBER_KURURU_LAST_RESEARCH] = CardName.KURURU_LAST_RESEARCH
        cardNumberHashmap[NUMBER_KURURU_GRAND_GULLIVER] = CardName.KURURU_GRAND_GULLIVER
        cardNumberHashmap[NUMBER_KURURU_BLASTER] = CardName.KURURU_BLASTER
        cardNumberHashmap[NUMBER_KURURU_RAILGUN] = CardName.KURURU_RAILGUN
        cardNumberHashmap[NUMBER_KURURU_CONNECT_DIVE] = CardName.KURURU_CONNECT_DIVE

        cardNumberHashmap[NUMBER_THALLYA_BURNING_STEAM] = CardName.THALLYA_BURNING_STEAM
        cardNumberHashmap[NUMBER_THALLYA_WAVING_EDGE] = CardName.THALLYA_WAVING_EDGE
        cardNumberHashmap[NUMBER_THALLYA_SHIELD_CHARGE] = CardName.THALLYA_SHIELD_CHARGE
        cardNumberHashmap[NUMBER_THALLYA_STEAM_CANNON] = CardName.THALLYA_STEAM_CANNON
        cardNumberHashmap[NUMBER_THALLYA_STUNT] = CardName.THALLYA_STUNT
        cardNumberHashmap[NUMBER_THALLYA_ROARING] = CardName.THALLYA_ROARING
        cardNumberHashmap[NUMBER_THALLYA_TURBO_SWITCH] = CardName.THALLYA_TURBO_SWITCH
        cardNumberHashmap[NUMBER_THALLYA_ALPHA_EDGE] = CardName.THALLYA_ALPHA_EDGE
        cardNumberHashmap[NUMBER_THALLYA_OMEGA_BURST] = CardName.THALLYA_OMEGA_BURST
        cardNumberHashmap[NUMBER_THALLYA_THALLYA_MASTERPIECE] = CardName.THALLYA_THALLYA_MASTERPIECE
        cardNumberHashmap[NUMBER_THALLYA_JULIA_BLACKBOX] = CardName.THALLYA_JULIA_BLACKBOX
        cardNumberHashmap[NUMBER_FORM_YAKSHA] = CardName.FORM_YAKSHA
        cardNumberHashmap[NUMBER_FORM_NAGA] = CardName.FORM_NAGA
        cardNumberHashmap[NUMBER_FORM_GARUDA] = CardName.FORM_GARUDA
        cardNumberHashmap[NUMBER_THALLYA_QUICK_CHANGE] = CardName.THALLYA_QUICK_CHANGE
        cardNumberHashmap[NUMBER_THALLYA_BLACKBOX_NEO] = CardName.THALLYA_BLACKBOX_NEO
        cardNumberHashmap[NUMBER_THALLYA_OMNIS_BLASTER] = CardName.THALLYA_OMNIS_BLASTER
        cardNumberHashmap[NUMBER_FORM_KINNARI] = CardName.FORM_KINNARI
        cardNumberHashmap[NUMBER_FORM_ASURA] = CardName.FORM_ASURA
        cardNumberHashmap[NUMBER_FORM_DEVA] = CardName.FORM_DEVA

        cardNumberHashmap[NUMBER_RAIRA_BEAST_NAIL] = CardName.RAIRA_BEAST_NAIL
        cardNumberHashmap[NUMBER_RAIRA_STORM_SURGE_ATTACK] = CardName.RAIRA_STORM_SURGE_ATTACK
        cardNumberHashmap[NUMBER_RAIRA_REINCARNATION_NAIL] = CardName.RAIRA_REINCARNATION_NAIL
        cardNumberHashmap[NUMBER_RAIRA_WIND_RUN] = CardName.RAIRA_WIND_RUN
        cardNumberHashmap[NUMBER_RAIRA_WISDOM_OF_STORM_SURGE] = CardName.RAIRA_WISDOM_OF_STORM_SURGE
        cardNumberHashmap[NUMBER_RAIRA_HOWLING] = CardName.RAIRA_HOWLING
        cardNumberHashmap[NUMBER_RAIRA_WIND_KICK] = CardName.RAIRA_WIND_KICK
        cardNumberHashmap[NUMBER_RAIRA_THUNDER_WIND_PUNCH] = CardName.RAIRA_THUNDER_WIND_PUNCH
        cardNumberHashmap[NUMBER_RAIRA_SUMMON_THUNDER] = CardName.RAIRA_SUMMON_THUNDER
        cardNumberHashmap[NUMBER_RAIRA_WIND_CONSEQUENCE_BALL] = CardName.RAIRA_WIND_CONSEQUENCE_BALL
        cardNumberHashmap[NUMBER_RAIRA_CIRCULAR_CIRCUIT] = CardName.RAIRA_CIRCULAR_CIRCUIT
        cardNumberHashmap[NUMBER_RAIRA_WIND_ATTACK] = CardName.RAIRA_WIND_ATTACK
        cardNumberHashmap[NUMBER_RAIRA_WIND_ZEN_KAI] = CardName.RAIRA_WIND_ZEN_KAI
        cardNumberHashmap[NUMBER_RAIRA_WIND_CELESTIAL_SPHERE] = CardName.RAIRA_WIND_CELESTIAL_SPHERE
        cardNumberHashmap[NUMBER_RAIRA_STORM] = CardName.RAIRA_STORM
        cardNumberHashmap[NUMBER_RAIRA_FURIOUS_STORM] = CardName.RAIRA_FURIOUS_STORM
        cardNumberHashmap[NUMBER_RAIRA_JIN_PUNG_JE_CHEON_UI] = CardName.RAIRA_JIN_PUNG_JE_CHEON_UI

        cardNumberHashmap[NUMBER_UTSURO_WON_WOL] = CardName.UTSURO_WON_WOL
        cardNumberHashmap[NUMBER_UTSURO_BLACK_WAVE] = CardName.UTSURO_BLACK_WAVE
        cardNumberHashmap[NUMBER_UTSURO_HARVEST] = CardName.UTSURO_HARVEST
        cardNumberHashmap[NUMBER_UTSURO_PRESSURE] = CardName.UTSURO_PRESSURE
        cardNumberHashmap[NUMBER_UTSURO_SHADOW_WING] = CardName.UTSURO_SHADOW_WING
        cardNumberHashmap[NUMBER_UTSURO_SHADOW_WALL] = CardName.UTSURO_SHADOW_WALL
        cardNumberHashmap[NUMBER_UTSURO_YUE_HOE_JU] = CardName.UTSURO_YUE_HOE_JU
        cardNumberHashmap[NUMBER_UTSURO_HOE_MYEOL] = CardName.UTSURO_HOE_MYEOL
        cardNumberHashmap[NUMBER_UTSURO_HEO_WI] = CardName.UTSURO_HEO_WI
        cardNumberHashmap[NUMBER_UTSURO_JONG_MAL] = CardName.UTSURO_JONG_MAL
        cardNumberHashmap[NUMBER_UTSURO_MA_SIG] = CardName.UTSURO_MA_SIG
        cardNumberHashmap[NUMBER_UTSURO_BITE_DUST] = CardName.UTSURO_BITE_DUST
        cardNumberHashmap[NUMBER_UTSURO_REVERBERATE_DEVICE_KURURUSIK] = CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK
        cardNumberHashmap[NUMBER_UTSURO_MANG_A] = CardName.UTSURO_MANG_A
        cardNumberHashmap[NUMBER_UTSURO_ANNIHILATION_SHADOW] = CardName.UTSURO_ANNIHILATION_SHADOW
        cardNumberHashmap[NUMBER_UTSURO_SILENT_WALK] = CardName.UTSURO_SILENT_WALK
        cardNumberHashmap[NUMBER_UTSURO_DE_MISE] = CardName.UTSURO_DE_MISE

        cardNumberHashmap[NUMBER_HONOKA_SPIRIT_SIK] = CardName.HONOKA_SPIRIT_SIK
        cardNumberHashmap[NUMBER_HONOKA_GUARDIAN_SPIRIT_SIK] = CardName.HONOKA_GUARDIAN_SPIRIT_SIK
        cardNumberHashmap[NUMBER_HONOKA_ASSAULT_SPIRIT_SIK] = CardName.HONOKA_ASSAULT_SPIRIT_SIK
        cardNumberHashmap[NUMBER_HONOKA_DIVINE_OUKA] = CardName.HONOKA_DIVINE_OUKA
        cardNumberHashmap[NUMBER_HONOKA_SAKURA_BLIZZARD] = CardName.HONOKA_SAKURA_BLIZZARD
        cardNumberHashmap[NUMBER_HONOKA_UI_GI_GONG_JIN] = CardName.HONOKA_UI_GI_GONG_JIN
        cardNumberHashmap[NUMBER_HONOKA_SAKURA_WING] = CardName.HONOKA_SAKURA_WING
        cardNumberHashmap[NUMBER_HONOKA_REGENERATION] = CardName.HONOKA_REGENERATION
        cardNumberHashmap[NUMBER_HONOKA_SAKURA_AMULET] = CardName.HONOKA_SAKURA_AMULET
        cardNumberHashmap[NUMBER_HONOKA_HONOKA_SPARKLE] = CardName.HONOKA_HONOKA_SPARKLE
        cardNumberHashmap[NUMBER_HONOKA_COMMAND] = CardName.HONOKA_COMMAND
        cardNumberHashmap[NUMBER_HONOKA_TAIL_WIND] = CardName.HONOKA_TAIL_WIND
        cardNumberHashmap[NUMBER_HONOKA_CHEST_WILLINGNESS] = CardName.HONOKA_CHEST_WILLINGNESS
        cardNumberHashmap[NUMBER_HONOKA_HAND_FLOWER] = CardName.HONOKA_HAND_FLOWER
        cardNumberHashmap[NUMBER_HONOKA_A_NEW_OPENING] = CardName.HONOKA_A_NEW_OPENING
        cardNumberHashmap[NUMBER_HONOKA_UNDER_THE_NAME_OF_FLAG] = CardName.HONOKA_UNDER_THE_NAME_OF_FLAG
        cardNumberHashmap[NUMBER_HONOKA_FOUR_SEASON_BACK] = CardName.HONOKA_FOUR_SEASON_BACK
        cardNumberHashmap[NUMBER_HONOKA_FULL_BLOOM_PATH] = CardName.HONOKA_FULL_BLOOM_PATH
        cardNumberHashmap[NUMBER_HONOKA_SAKURA_SWORD] = CardName.HONOKA_SAKURA_SWORD
        cardNumberHashmap[NUMBER_HONOKA_SHADOW_HAND] = CardName.HONOKA_SHADOW_HAND
        cardNumberHashmap[NUMBER_HONOKA_EYE_OPEN_ALONE] = CardName.HONOKA_EYE_OPEN_ALONE
        cardNumberHashmap[NUMBER_HONOKA_FOLLOW_TRACE] = CardName.HONOKA_FOLLOW_TRACE
        cardNumberHashmap[NUMBER_HONOKA_FACING_SHADOW] = CardName.HONOKA_FACING_SHADOW
        cardNumberHashmap[NUMBER_HONOKA_SAKURA_SHINING_BRIGHTLY] = CardName.HONOKA_SAKURA_SHINING_BRIGHTLY
        cardNumberHashmap[NUMBER_HONOKA_HOLD_HANDS] = CardName.HONOKA_HOLD_HANDS
        cardNumberHashmap[NUMBER_HONOKA_WALK_OLD_LOAD] = CardName.HONOKA_WALK_OLD_LOAD

        cardNumberHashmap[NUMBER_KORUNU_SNOW_BLADE] = CardName.KORUNU_SNOW_BLADE
        cardNumberHashmap[NUMBER_KORUNU_REVOLVING_BLADE] = CardName.KORUNU_REVOLVING_BLADE
        cardNumberHashmap[NUMBER_KORUNU_BLADE_DANCE] = CardName.KORUNU_BLADE_DANCE
        cardNumberHashmap[NUMBER_KORUNU_RIDE_SNOW] = CardName.KORUNU_RIDE_SNOW
        cardNumberHashmap[NUMBER_KORUNU_ABSOLUTE_ZERO] = CardName.KORUNU_ABSOLUTE_ZERO
        cardNumberHashmap[NUMBER_KORUNU_FROSTBITE] = CardName.KORUNU_FROSTBITE
        cardNumberHashmap[NUMBER_KORUNU_FROST_THORN_BUSH] = CardName.KORUNU_FROST_THORN_BUSH
        cardNumberHashmap[NUMBER_KORUNU_CONLU_RUYANPEH] = CardName.KORUNU_CONLU_RUYANPEH
        cardNumberHashmap[NUMBER_KORUNU_LETAR_LERA] = CardName.KORUNU_LETAR_LERA
        cardNumberHashmap[NUMBER_KORUNU_UPASTUM] = CardName.KORUNU_UPASTUM
        cardNumberHashmap[NUMBER_KORUNU_PORUCHARTO] = CardName.KORUNU_PORUCHARTO

        cardNumberHashmap[NUMBER_YATSUHA_STAR_NAIL] = CardName.YATSUHA_STAR_NAIL
        cardNumberHashmap[NUMBER_YATSUHA_DARKNESS_GILL] = CardName.YATSUHA_DARKNESS_GILL
        cardNumberHashmap[NUMBER_YATSUHA_MIRROR_DEVIL] = CardName.YATSUHA_MIRROR_DEVIL
        cardNumberHashmap[NUMBER_YATSUHA_GHOST_STEP] = CardName.YATSUHA_GHOST_STEP
        cardNumberHashmap[NUMBER_YATSUHA_WILLING] = CardName.YATSUHA_WILLING
        cardNumberHashmap[NUMBER_YATSUHA_CONTRACT] = CardName.YATSUHA_CONTRACT
        cardNumberHashmap[NUMBER_YATSUHA_CLINGY_FLOWER] = CardName.YATSUHA_CLINGY_FLOWER
        cardNumberHashmap[NUMBER_YATSUHA_TWO_LEAP_MIRROR_DIVINE] = CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE
        cardNumberHashmap[NUMBER_YATSUHA_FOUR_LEAP_SONG] = CardName.YATSUHA_FOUR_LEAP_SONG
        cardNumberHashmap[NUMBER_YATSUHA_SIX_STAR_SEA] = CardName.YATSUHA_SIX_STAR_SEA
        cardNumberHashmap[NUMBER_YATSUHA_EIGHT_MIRROR_OTHER_SIDE] = CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE
        cardNumberHashmap[NUMBER_YATSUHA_HOLY_RAKE_HANDS] = CardName.YATSUHA_HOLY_RAKE_HANDS
        cardNumberHashmap[NUMBER_YATSUHA_ENTRANCE_OF_ABYSS] = CardName.YATSUHA_ENTRANCE_OF_ABYSS
        cardNumberHashmap[NUMBER_YATSUHA_TRUE_MONSTER] = CardName.YATSUHA_TRUE_MONSTER
        cardNumberHashmap[NUMBER_YATSUHA_GHOST_LINK] = CardName.YATSUHA_GHOST_LINK
        cardNumberHashmap[NUMBER_YATSUHA_RESOLUTION] = CardName.YATSUHA_RESOLUTION
        cardNumberHashmap[NUMBER_YATSUHA_PLEDGE] = CardName.YATSUHA_PLEDGE
        cardNumberHashmap[NUMBER_YATSUHA_VAIN_FLOWER] = CardName.YATSUHA_VAIN_FLOWER
        cardNumberHashmap[NUMBER_YATSUHA_EIGHT_MIRROR_VAIN_SAKURA] = CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA

        cardNumberHashmap[NUMBER_HATSUMI_WATER_BALL] = CardName.HATSUMI_WATER_BALL
        cardNumberHashmap[NUMBER_HATSUMI_WATER_CURRENT] = CardName.HATSUMI_WATER_CURRENT
        cardNumberHashmap[NUMBER_HATSUMI_STRONG_ACID] = CardName.HATSUMI_STRONG_ACID
        cardNumberHashmap[NUMBER_HATSUMI_TSUNAMI] = CardName.HATSUMI_TSUNAMI
        cardNumberHashmap[NUMBER_HATSUMI_JUN_BI_MAN_TAN] = CardName.HATSUMI_JUN_BI_MAN_TAN
        cardNumberHashmap[NUMBER_HATSUMI_COMPASS] = CardName.HATSUMI_COMPASS
        cardNumberHashmap[NUMBER_HATSUMI_CALL_WAVE] = CardName.HATSUMI_CALL_WAVE
        cardNumberHashmap[NUMBER_HATSUMI_ISANA_HAIL] = CardName.HATSUMI_ISANA_HAIL
        cardNumberHashmap[NUMBER_HATSUMI_OYOGIBI_FIRE] = CardName.HATSUMI_OYOGIBI_FIRE
        cardNumberHashmap[NUMBER_HATSUMI_KIRAHARI_LIGHTHOUSE] = CardName.HATSUMI_KIRAHARI_LIGHTHOUSE
        cardNumberHashmap[NUMBER_HATSUMI_MIOBIKI_ROUTE] = CardName.HATSUMI_MIOBIKI_ROUTE
        cardNumberHashmap[NUMBER_HATSUMI_TORPEDO] = CardName.HATSUMI_TORPEDO
        cardNumberHashmap[NUMBER_HATSUMI_SAGIRI_HAIL] = CardName.HATSUMI_SAGIRI_HAIL
        cardNumberHashmap[NUMBER_HATSUMI_WADANAKA_ROUTE] = CardName.HATSUMI_WADANAKA_ROUTE

        cardNumberHashmap[NUMBER_MIZUKI_JIN_DU] = CardName.MIZUKI_JIN_DU
        cardNumberHashmap[NUMBER_MIZUKI_BAN_GONG] = CardName.MIZUKI_BAN_GONG
        cardNumberHashmap[NUMBER_MIZUKI_SHOOTING_DOWN] = CardName.MIZUKI_SHOOTING_DOWN
        cardNumberHashmap[NUMBER_MIZUKI_HO_LYEONG] = CardName.MIZUKI_HO_LYEONG
        cardNumberHashmap[NUMBER_MIZUKI_BANG_BYEOG] = CardName.MIZUKI_BANG_BYEOG
        cardNumberHashmap[NUMBER_MIZUKI_OVERPOWERING_GO_FORWARD] = CardName.MIZUKI_OVERPOWERING_GO_FORWARD
        cardNumberHashmap[NUMBER_MIZUKI_JEON_JANG] = CardName.MIZUKI_JEON_JANG
        cardNumberHashmap[NUMBER_MIZUKI_HACHIRYU_CHEONJUGAK] = CardName.MIZUKI_HACHIRYU_CHEONJUGAK
        cardNumberHashmap[NUMBER_MIZUKI_HIJAMARU_TRIPLET] = CardName.MIZUKI_HIJAMARU_TRIPLET
        cardNumberHashmap[NUMBER_MIZUKI_TARTENASHI_DAESUMUN] = CardName.MIZUKI_TARTENASHI_DAESUMUN
        cardNumberHashmap[NUMBER_MIZUKI_MIZUKI_BATTLE_CRY] = CardName.MIZUKI_MIZUKI_BATTLE_CRY
        cardNumberHashmap[NUMBER_KODAMA_TU_SIN] = CardName.KODAMA_TU_SIN
        cardNumberHashmap[NUMBER_SOLDIER_SPEAR_1] = CardName.SOLDIER_SPEAR_1
        cardNumberHashmap[NUMBER_SOLDIER_SPEAR_2] = CardName.SOLDIER_SPEAR_2
        cardNumberHashmap[NUMBER_SOLDIER_SHIELD] = CardName.SOLDIER_SHIELD
        cardNumberHashmap[NUMBER_SOLDIER_HORSE] = CardName.SOLDIER_HORSE

        cardNumberHashmap[NUMBER_MEGUMI_GONG_SUM] = CardName.MEGUMI_GONG_SUM
        cardNumberHashmap[NUMBER_MEGUMI_TA_CHEOG] = CardName.MEGUMI_TA_CHEOG
        cardNumberHashmap[NUMBER_MEGUMI_SHELL_ATTACK] = CardName.MEGUMI_SHELL_ATTACK
        cardNumberHashmap[NUMBER_MEGUMI_POLE_THRUST] = CardName.MEGUMI_POLE_THRUST
        cardNumberHashmap[NUMBER_MEGUMI_REED] = CardName.MEGUMI_REED
        cardNumberHashmap[NUMBER_MEGUMI_BALSAM] = CardName.MEGUMI_BALSAM
        cardNumberHashmap[NUMBER_MEGUMI_WILD_ROSE] = CardName.MEGUMI_WILD_ROSE
        cardNumberHashmap[NUMBER_MEGUMI_ROOT_OF_CAUSALITY] = CardName.MEGUMI_ROOT_OF_CAUSALITY
        cardNumberHashmap[NUMBER_MEGUMI_BRANCH_OF_POSSIBILITY] = CardName.MEGUMI_BRANCH_OF_POSSIBILITY
        cardNumberHashmap[NUMBER_MEGUMI_FRUIT_OF_END] = CardName.MEGUMI_FRUIT_OF_END
        cardNumberHashmap[NUMBER_MEGUMI_MEGUMI_PALM] = CardName.MEGUMI_MEGUMI_PALM

        cardNumberHashmap[NUMBER_KANAWE_IMAGE] = CardName.KANAWE_IMAGE
        cardNumberHashmap[NUMBER_KANAWE_SCREENPLAY] = CardName.KANAWE_SCREENPLAY
        cardNumberHashmap[NUMBER_KANAWE_PRODUCTION] = CardName.KANAWE_PRODUCTION
        cardNumberHashmap[NUMBER_KANAWE_PUBLISH] = CardName.KANAWE_PUBLISH
        cardNumberHashmap[NUMBER_KANAWE_AFTERGLOW] = CardName.KANAWE_AFTERGLOW
        cardNumberHashmap[NUMBER_KANAWE_IMPROMPTU] = CardName.KANAWE_IMPROMPTU
        cardNumberHashmap[NUMBER_KANAWE_SEAL] = CardName.KANAWE_SEAL
        cardNumberHashmap[NUMBER_KANAWE_VAGUE_STORY] = CardName.KANAWE_VAGUE_STORY
        cardNumberHashmap[NUMBER_KANAWE_INFINITE_STARLIGHT] = CardName.KANAWE_INFINITE_STARLIGHT
        cardNumberHashmap[NUMBER_KANAWE_BEND_OVER_THIS_NIGHT] = CardName.KANAWE_BEND_OVER_THIS_NIGHT
        cardNumberHashmap[NUMBER_KANAWE_DISTANT_SKY] = CardName.KANAWE_DISTANT_SKY
        cardNumberHashmap[NUMBER_KANAWE_KANAWE] = CardName.KANAWE_KANAWE

        cardNumberHashmap[NUMBER_IDEA_SAL_JIN] = CardName.IDEA_SAL_JIN
        cardNumberHashmap[NUMBER_IDEA_SAKURA_WAVE] = CardName.IDEA_SAKURA_WAVE
        cardNumberHashmap[NUMBER_IDEA_WHISTLE] = CardName.IDEA_WHISTLE
        cardNumberHashmap[NUMBER_IDEA_MYEONG_JEON] = CardName.IDEA_MYEONG_JEON
        cardNumberHashmap[NUMBER_IDEA_EMPHASIZING] = CardName.IDEA_EMPHASIZING
        cardNumberHashmap[NUMBER_IDEA_POSITIONING] = CardName.IDEA_POSITIONING

        cardNumberHashmap[NUMBER_KAMUWI_RED_BLADE] = CardName.KAMUWI_RED_BLADE
        cardNumberHashmap[NUMBER_KAMUWI_FLUTTERING_BLADE] = CardName.KAMUWI_FLUTTERING_BLADE
        cardNumberHashmap[NUMBER_KAMUWI_SI_KEN_LAN_JIN] = CardName.KAMUWI_SI_KEN_LAN_JIN
        cardNumberHashmap[NUMBER_KAMUWI_CUT_DOWN] = CardName.KAMUWI_CUT_DOWN
        cardNumberHashmap[NUMBER_KAMUWI_THREADING_THORN] = CardName.KAMUWI_THREADING_THORN
        cardNumberHashmap[NUMBER_KAMUWI_KE_SYO_LAN_LYU] = CardName.KAMUWI_KE_SYO_LAN_LYU
        cardNumberHashmap[NUMBER_KAMUWI_BLOOD_WAVE] = CardName.KAMUWI_BLOOD_WAVE
        cardNumberHashmap[NUMBER_KAMUWI_LAMP] = CardName.KAMUWI_LAMP
        cardNumberHashmap[NUMBER_KAMUWI_DAWN] = CardName.KAMUWI_DAWN
        cardNumberHashmap[NUMBER_KAMUWI_GRAVEYARD] = CardName.KAMUWI_GRAVEYARD
        cardNumberHashmap[NUMBER_KAMUWI_KATA_SHIRO] = CardName.KAMUWI_KATA_SHIRO
        cardNumberHashmap[NUMBER_KAMUWI_LOGIC] = CardName.KAMUWI_LOGIC

        cardNumberHashmap[NUMBER_RENRI_FALSE_STAB] = CardName.RENRI_FALSE_STAB
        cardNumberHashmap[NUMBER_RENRI_TEMPORARY_EXPEDIENT] = CardName.RENRI_TEMPORARY_EXPEDIENT
        cardNumberHashmap[NUMBER_RENRI_BLACK_AND_WHITE] = CardName.RENRI_BLACK_AND_WHITE
        cardNumberHashmap[NUMBER_RENRI_IRRITATING_GESTURE] = CardName.RENRI_IRRITATING_GESTURE
        cardNumberHashmap[NUMBER_RENRI_FLOATING_CLOUDS] = CardName.RENRI_FLOATING_CLOUDS
        cardNumberHashmap[NUMBER_RENRI_FISHING] = CardName.RENRI_FISHING
        cardNumberHashmap[NUMBER_RENRI_PULLING_FISHING] = CardName.RENRI_PULLING_FISHING
        cardNumberHashmap[NUMBER_RENRI_RU_RU_RA_RA_RI] = CardName.RENRI_RU_RU_RA_RA_RI
        cardNumberHashmap[NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA] = CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA
        cardNumberHashmap[NUMBER_RENRI_O_RI_RE_TE_RA_RE_RU] = CardName.RENRI_O_RI_RE_TE_RA_RE_RU
        cardNumberHashmap[NUMBER_RENRI_RENRI_THE_END] = CardName.RENRI_RENRI_THE_END
        cardNumberHashmap[NUMBER_RENRI_ENGRAVED_GARMENT] = CardName.RENRI_ENGRAVED_GARMENT
        cardNumberHashmap[NUMBER_KIRIKO_SHAMANISTIC_MUSIC] = CardName.KIRIKO_SHAMANISTIC_MUSIC


        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CARD_UNAME] = CardName.CARD_UNNAME
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_ANYTHING] = CardName.POISON_ANYTHING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_ANYTHING] = CardName.SOLDIER_ANYTHING

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_CHAM] = CardName.YURINA_CHAM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_ILSUM] = CardName.YURINA_ILSUM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_JARUCHIGI] = CardName.YURINA_JARUCHIGI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_GUHAB] = CardName.YURINA_GUHAB
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_GIBACK] = CardName.YURINA_GIBACK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_APDO] = CardName.YURINA_APDO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_GIYENBANJO] = CardName.YURINA_GIYENBANJO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_WOLYUNGNACK] = CardName.YURINA_WOLYUNGNACK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_POBARAM] = CardName.YURINA_POBARAM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_JJOCKBAE] = CardName.YURINA_JJOCKBAE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_JURUCK] = CardName.YURINA_JURUCK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_NAN_TA] = CardName.YURINA_NAN_TA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_BEAN_BULLET] = CardName.YURINA_BEAN_BULLET
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_NOT_COMPLETE_POBARAM] = CardName.YURINA_NOT_COMPLETE_POBARAM

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_DOUBLEBEGI] = CardName.SAINE_DOUBLEBEGI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_HURUBEGI] = CardName.SAINE_HURUBEGI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_MOOGECHOO] = CardName.SAINE_MOOGECHOO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_GANPA] = CardName.SAINE_GANPA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_GWONYUCK] = CardName.SAINE_GWONYUCK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_CHOONGEMJUNG] = CardName.SAINE_CHOONGEMJUNG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_MOOEMBUCK] = CardName.SAINE_MOOEMBUCK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_YULDONGHOGEK] = CardName.SAINE_YULDONGHOGEK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_HANGMUNGGONGJIN] = CardName.SAINE_HANGMUNGGONGJIN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_EMMOOSHOEBING] = CardName.SAINE_EMMOOSHOEBING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_JONGGEK] = CardName.SAINE_JONGGEK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_SOUND_OF_ICE] = CardName.SAINE_SOUND_OF_ICE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_ACCOMPANIMENT] = CardName.SAINE_ACCOMPANIMENT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_DUET_TAN_JU_BING_MYEONG] = CardName.SAINE_DUET_TAN_JU_BING_MYEONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_BETRAYAL] = CardName.SAINE_BETRAYAL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_FLOWING_WALL] = CardName.SAINE_FLOWING_WALL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_JEOL_CHANG_JEOL_HWA] = CardName.SAINE_JEOL_CHANG_JEOL_HWA

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SHOOT] = CardName.HIMIKA_SHOOT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_RAPIDFIRE] = CardName.HIMIKA_RAPIDFIRE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_MAGNUMCANON] = CardName.HIMIKA_MAGNUMCANON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_FULLBURST] = CardName.HIMIKA_FULLBURST
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_BACKSTEP] = CardName.HIMIKA_BACKSTEP
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_BACKDRAFT] = CardName.HIMIKA_BACKDRAFT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SMOKE] = CardName.HIMIKA_SMOKE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_REDBULLET] = CardName.HIMIKA_REDBULLET
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_CRIMSONZERO] = CardName.HIMIKA_CRIMSONZERO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SCARLETIMAGINE] = CardName.HIMIKA_SCARLETIMAGINE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_BURMILIONFIELD] = CardName.HIMIKA_BURMILIONFIELD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_FIRE_WAVE] = CardName.HIMIKA_FIRE_WAVE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SAT_SUI] = CardName.HIMIKA_SAT_SUI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_EN_TEN_HIMIKA] = CardName.HIMIKA_EN_TEN_HIMIKA

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_BITSUNERIGI] = CardName.TOKOYO_BITSUNERIGI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_WOOAHHANTAGUCK] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_RUNNINGRABIT] = CardName.TOKOYO_RUNNINGRABIT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_POETDANCE] = CardName.TOKOYO_POETDANCE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_FLIPFAN] = CardName.TOKOYO_FLIPFAN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_WINDSTAGE] = CardName.TOKOYO_WINDSTAGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_SUNSTAGE] = CardName.TOKOYO_SUNSTAGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_KUON] = CardName.TOKOYO_KUON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_THOUSANDBIRD] = CardName.TOKOYO_THOUSANDBIRD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_ENDLESSWIND] = CardName.TOKOYO_ENDLESSWIND
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_TOKOYOMOON] = CardName.TOKOYO_TOKOYOMOON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_FLOWING_PLAY] = CardName.TOKOYO_FLOWING_PLAY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_SOUND_OF_SUN] = CardName.TOKOYO_SOUND_OF_SUN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG] = CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_PASSING_FEAR] = CardName.TOKOYO_PASSING_FEAR
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_RELIC_EYE] = CardName.TOKOYO_RELIC_EYE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_EIGHT_SAKURA_IN_VAIN] = CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_WIRE] = CardName.OBORO_WIRE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_SHADOWCALTROP] = CardName.OBORO_SHADOWCALTROP
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_ZANGEKIRANBU] = CardName.OBORO_ZANGEKIRANBU
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_NINJAWALK] = CardName.OBORO_NINJAWALK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_INDUCE] = CardName.OBORO_INDUCE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CLONE] = CardName.OBORO_CLONE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_BIOACTIVITY] = CardName.OBORO_BIOACTIVITY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_KUMASUKE] = CardName.OBORO_KUMASUKE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_TOBIKAGE] = CardName.OBORO_TOBIKAGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_ULOO] = CardName.OBORO_ULOO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MIKAZRA] = CardName.OBORO_MIKAZRA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_SHURIKEN] = CardName.OBORO_SHURIKEN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_AMBUSH] = CardName.OBORO_AMBUSH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_BRANCH_OF_DIVINE] = CardName.OBORO_BRANCH_OF_DIVINE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_LAST_CRYSTAL] = CardName.OBORO_LAST_CRYSTAL

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_YUKIHI] = CardName.YUKIHI_YUKIHI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_PUSH_OUT_SLASH_PULL] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_SWING_SLASH_STAB] = CardName.YUKIHI_SWING_SLASH_STAB
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_TURN_UMBRELLA] = CardName.YUKIHI_TURN_UMBRELLA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_MAKE_CONNECTION] = CardName.YUKIHI_MAKE_CONNECTION
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_FLUTTERING_SNOWFLAKE] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_SWAYING_LAMPLIGHT] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_CLINGY_MIND] = CardName.YUKIHI_CLINGY_MIND
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_SWIRLING_GESTURE] = CardName.YUKIHI_SWIRLING_GESTURE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_HELP_SLASH_THREAT] = CardName.YUKIHI_HELP_SLASH_THREAT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_THREAD_SLASH_RAW_THREAD] = CardName.YUKIHI_THREAD_SLASH_RAW_THREAD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_FLUTTERING_COLLAR] = CardName.YUKIHI_FLUTTERING_COLLAR

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SHINRA] = CardName.SHINRA_SHINRA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_IBLON] = CardName.SHINRA_IBLON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_BANLON] = CardName.SHINRA_BANLON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_KIBEN] = CardName.SHINRA_KIBEN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_INYONG] = CardName.SHINRA_INYONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SEONDONG] = CardName.SHINRA_SEONDONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_JANGDAM] = CardName.SHINRA_JANGDAM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_NONPA] = CardName.SHINRA_NONPA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_WANJEON_NONPA] = CardName.SHINRA_WANJEON_NONPA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_DASIG_IHAE] = CardName.SHINRA_DASIG_IHAE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_CHEONJI_BANBAG] = CardName.SHINRA_CHEONJI_BANBAG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SAMRA_BAN_SHO] = CardName.SHINRA_SAMRA_BAN_SHO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_ZHEN_YEN] = CardName.SHINRA_ZHEN_YEN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SA_DO] = CardName.SHINRA_SA_DO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_ZEN_CHI_KYO_TEN] = CardName.SHINRA_ZEN_CHI_KYO_TEN

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_CENTRIFUGAL_ATTACK] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_FOUR_WINDED_EARTHQUAKE] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GROUND_BREAKING] = CardName.HAGANE_GROUND_BREAKING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_HYPER_RECOIL] = CardName.HAGANE_HYPER_RECOIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_WON_MU_RUYN] = CardName.HAGANE_WON_MU_RUYN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_RING_A_BELL] = CardName.HAGANE_RING_A_BELL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAVITATION_FIELD] = CardName.HAGANE_GRAVITATION_FIELD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_SKY_HOLE_CRASH] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_BELL_MEGALOBEL] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_GRAVITATION_ATTRACT] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_MOUNTAIN_RESPECT] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_BONFIRE] = CardName.HAGANE_BONFIRE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_WHEEL_SKILL] = CardName.HAGANE_WHEEL_SKILL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_SOFT_MATERIAL] = CardName.HAGANE_GRAND_SOFT_MATERIAL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_SOFT_ATTACK] = CardName.HAGANE_SOFT_ATTACK

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_THROW_KUNAI] = CardName.CHIKAGE_THROW_KUNAI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_POISON_NEEDLE] = CardName.CHIKAGE_POISON_NEEDLE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_TO_ZU_CHU] = CardName.CHIKAGE_TO_ZU_CHU
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_CUTTING_NECK] = CardName.CHIKAGE_CUTTING_NECK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_POISON_SMOKE] = CardName.CHIKAGE_POISON_SMOKE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_TIP_TOEING] = CardName.CHIKAGE_TIP_TOEING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_MUDDLE] = CardName.CHIKAGE_MUDDLE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_DEADLY_POISON] = CardName.CHIKAGE_DEADLY_POISON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_HAN_KI_POISON] = CardName.CHIKAGE_HAN_KI_POISON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_REINCARNATION_POISON] = CardName.CHIKAGE_REINCARNATION_POISON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_TRICK_UMBRELLA] = CardName.CHIKAGE_TRICK_UMBRELLA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_STRUGGLE] = CardName.CHIKAGE_STRUGGLE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON] = CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_PARALYTIC] = CardName.POISON_PARALYTIC
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_HALLUCINOGENIC] = CardName.POISON_HALLUCINOGENIC
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_RELAXATION] = CardName.POISON_RELAXATION
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_1] = CardName.POISON_DEADLY_1
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_2] = CardName.POISON_DEADLY_2

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_ELEKITTEL] = CardName.KURURU_ELEKITTEL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_ACCELERATOR] = CardName.KURURU_ACCELERATOR
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_KURURUOONG] = CardName.KURURU_KURURUOONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_TORNADO] = CardName.KURURU_TORNADO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_REGAINER] = CardName.KURURU_REGAINER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_MODULE] = CardName.KURURU_MODULE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_REFLECTOR] = CardName.KURURU_REFLECTOR
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DRAIN_DEVIL] = CardName.KURURU_DRAIN_DEVIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_BIG_GOLEM] = CardName.KURURU_BIG_GOLEM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_INDUSTRIA] = CardName.KURURU_INDUSTRIA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DUPLICATED_GEAR_1] = CardName.KURURU_DUPLICATED_GEAR_1
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DUPLICATED_GEAR_2] = CardName.KURURU_DUPLICATED_GEAR_2
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DUPLICATED_GEAR_3] = CardName.KURURU_DUPLICATED_GEAR_3
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_KANSHOUSOUCHI_KURURUSIK] = CardName.KURURU_KANSHOUSOUCHI_KURURUSIK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_ANALYZE] = CardName.KURURU_ANALYZE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DAUZING] = CardName.KURURU_DAUZING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_LAST_RESEARCH] = CardName.KURURU_LAST_RESEARCH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_GRAND_GULLIVER] = CardName.KURURU_GRAND_GULLIVER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_BLASTER] = CardName.KURURU_BLASTER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_RAILGUN] = CardName.KURURU_RAILGUN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_CONNECT_DIVE] = CardName.KURURU_CONNECT_DIVE

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_BURNING_STEAM] = CardName.THALLYA_BURNING_STEAM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_WAVING_EDGE] = CardName.THALLYA_WAVING_EDGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_SHIELD_CHARGE] = CardName.THALLYA_SHIELD_CHARGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_STEAM_CANNON] = CardName.THALLYA_STEAM_CANNON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_STUNT] = CardName.THALLYA_STUNT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_ROARING] = CardName.THALLYA_ROARING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_TURBO_SWITCH] = CardName.THALLYA_TURBO_SWITCH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_ALPHA_EDGE] = CardName.THALLYA_ALPHA_EDGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_OMEGA_BURST] = CardName.THALLYA_OMEGA_BURST
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_THALLYA_MASTERPIECE] = CardName.THALLYA_THALLYA_MASTERPIECE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_JULIA_BLACKBOX] = CardName.THALLYA_JULIA_BLACKBOX
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_YAKSHA] = CardName.FORM_YAKSHA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_NAGA] = CardName.FORM_NAGA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_GARUDA] = CardName.FORM_GARUDA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_QUICK_CHANGE] = CardName.THALLYA_QUICK_CHANGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_BLACKBOX_NEO] = CardName.THALLYA_BLACKBOX_NEO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_OMNIS_BLASTER] = CardName.THALLYA_OMNIS_BLASTER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_KINNARI] = CardName.FORM_KINNARI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_ASURA] = CardName.FORM_ASURA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_DEVA] = CardName.FORM_DEVA

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_BEAST_NAIL] = CardName.RAIRA_BEAST_NAIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_STORM_SURGE_ATTACK] = CardName.RAIRA_STORM_SURGE_ATTACK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_REINCARNATION_NAIL] = CardName.RAIRA_REINCARNATION_NAIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_RUN] = CardName.RAIRA_WIND_RUN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WISDOM_OF_STORM_SURGE] = CardName.RAIRA_WISDOM_OF_STORM_SURGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_HOWLING] = CardName.RAIRA_HOWLING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_KICK] = CardName.RAIRA_WIND_KICK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_THUNDER_WIND_PUNCH] = CardName.RAIRA_THUNDER_WIND_PUNCH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_SUMMON_THUNDER] = CardName.RAIRA_SUMMON_THUNDER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_CONSEQUENCE_BALL] = CardName.RAIRA_WIND_CONSEQUENCE_BALL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_CIRCULAR_CIRCUIT] = CardName.RAIRA_CIRCULAR_CIRCUIT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_ATTACK] = CardName.RAIRA_WIND_ATTACK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_ZEN_KAI] = CardName.RAIRA_WIND_ZEN_KAI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_CELESTIAL_SPHERE] = CardName.RAIRA_WIND_CELESTIAL_SPHERE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_STORM] = CardName.RAIRA_STORM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_FURIOUS_STORM] = CardName.RAIRA_FURIOUS_STORM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_JIN_PUNG_JE_CHEON_UI] = CardName.RAIRA_JIN_PUNG_JE_CHEON_UI

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_WON_WOL] = CardName.UTSURO_WON_WOL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_BLACK_WAVE] = CardName.UTSURO_BLACK_WAVE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_HARVEST] = CardName.UTSURO_HARVEST
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_PRESSURE] = CardName.UTSURO_PRESSURE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_SHADOW_WING] = CardName.UTSURO_SHADOW_WING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_SHADOW_WALL] = CardName.UTSURO_SHADOW_WALL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_YUE_HOE_JU] = CardName.UTSURO_YUE_HOE_JU
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_HOE_MYEOL] = CardName.UTSURO_HOE_MYEOL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_HEO_WI] = CardName.UTSURO_HEO_WI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_JONG_MAL] = CardName.UTSURO_JONG_MAL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_MA_SIG] = CardName.UTSURO_MA_SIG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_BITE_DUST] = CardName.UTSURO_BITE_DUST
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_REVERBERATE_DEVICE_KURURUSIK] = CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_MANG_A] = CardName.UTSURO_MANG_A
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_ANNIHILATION_SHADOW] = CardName.UTSURO_ANNIHILATION_SHADOW
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_SILENT_WALK] = CardName.UTSURO_SILENT_WALK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_DE_MISE] = CardName.UTSURO_DE_MISE

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SPIRIT_SIK] = CardName.HONOKA_SPIRIT_SIK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_GUARDIAN_SPIRIT_SIK] = CardName.HONOKA_GUARDIAN_SPIRIT_SIK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_ASSAULT_SPIRIT_SIK] = CardName.HONOKA_ASSAULT_SPIRIT_SIK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_DIVINE_OUKA] = CardName.HONOKA_DIVINE_OUKA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_BLIZZARD] = CardName.HONOKA_SAKURA_BLIZZARD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_UI_GI_GONG_JIN] = CardName.HONOKA_UI_GI_GONG_JIN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_WING] = CardName.HONOKA_SAKURA_WING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_REGENERATION] = CardName.HONOKA_REGENERATION
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_AMULET] = CardName.HONOKA_SAKURA_AMULET
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_HONOKA_SPARKLE] = CardName.HONOKA_HONOKA_SPARKLE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_COMMAND] = CardName.HONOKA_COMMAND
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_TAIL_WIND] = CardName.HONOKA_TAIL_WIND
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_CHEST_WILLINGNESS] = CardName.HONOKA_CHEST_WILLINGNESS
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_HAND_FLOWER] = CardName.HONOKA_HAND_FLOWER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_A_NEW_OPENING] = CardName.HONOKA_A_NEW_OPENING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_UNDER_THE_NAME_OF_FLAG] = CardName.HONOKA_UNDER_THE_NAME_OF_FLAG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FOUR_SEASON_BACK] = CardName.HONOKA_FOUR_SEASON_BACK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FULL_BLOOM_PATH] = CardName.HONOKA_FULL_BLOOM_PATH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_SWORD] = CardName.HONOKA_SAKURA_SWORD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SHADOW_HAND] = CardName.HONOKA_SHADOW_HAND
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_EYE_OPEN_ALONE] = CardName.HONOKA_EYE_OPEN_ALONE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FOLLOW_TRACE] = CardName.HONOKA_FOLLOW_TRACE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FACING_SHADOW] = CardName.HONOKA_FACING_SHADOW
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_SHINING_BRIGHTLY] = CardName.HONOKA_SAKURA_SHINING_BRIGHTLY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_HOLD_HANDS] = CardName.HONOKA_HOLD_HANDS
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_WALK_OLD_LOAD] = CardName.HONOKA_WALK_OLD_LOAD

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_SNOW_BLADE] = CardName.KORUNU_SNOW_BLADE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_REVOLVING_BLADE] = CardName.KORUNU_REVOLVING_BLADE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_BLADE_DANCE] = CardName.KORUNU_BLADE_DANCE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_RIDE_SNOW] = CardName.KORUNU_RIDE_SNOW
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_ABSOLUTE_ZERO] = CardName.KORUNU_ABSOLUTE_ZERO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_FROSTBITE] = CardName.KORUNU_FROSTBITE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_FROST_THORN_BUSH] = CardName.KORUNU_FROST_THORN_BUSH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_CONLU_RUYANPEH] = CardName.KORUNU_CONLU_RUYANPEH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_LETAR_LERA] = CardName.KORUNU_LETAR_LERA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_UPASTUM] = CardName.KORUNU_UPASTUM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_PORUCHARTO] = CardName.KORUNU_PORUCHARTO

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_STAR_NAIL] = CardName.YATSUHA_STAR_NAIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_DARKNESS_GILL] = CardName.YATSUHA_DARKNESS_GILL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_MIRROR_DEVIL] = CardName.YATSUHA_MIRROR_DEVIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_GHOST_STEP] = CardName.YATSUHA_GHOST_STEP
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_WILLING] = CardName.YATSUHA_WILLING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_CONTRACT] = CardName.YATSUHA_CONTRACT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_CLINGY_FLOWER] = CardName.YATSUHA_CLINGY_FLOWER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_TWO_LEAP_MIRROR_DIVINE] = CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_FOUR_LEAP_SONG] = CardName.YATSUHA_FOUR_LEAP_SONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_SIX_STAR_SEA] = CardName.YATSUHA_SIX_STAR_SEA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_EIGHT_MIRROR_OTHER_SIDE] = CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_HOLY_RAKE_HANDS] = CardName.YATSUHA_HOLY_RAKE_HANDS
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_ENTRANCE_OF_ABYSS] = CardName.YATSUHA_ENTRANCE_OF_ABYSS
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_TRUE_MONSTER] = CardName.YATSUHA_TRUE_MONSTER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_GHOST_LINK] = CardName.YATSUHA_GHOST_LINK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_RESOLUTION] = CardName.YATSUHA_RESOLUTION
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_PLEDGE] = CardName.YATSUHA_PLEDGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_VAIN_FLOWER] = CardName.YATSUHA_VAIN_FLOWER
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_EIGHT_MIRROR_VAIN_SAKURA] = CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_WATER_BALL] = CardName.HATSUMI_WATER_BALL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_WATER_CURRENT] = CardName.HATSUMI_WATER_CURRENT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_STRONG_ACID] = CardName.HATSUMI_STRONG_ACID
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_TSUNAMI] = CardName.HATSUMI_TSUNAMI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_JUN_BI_MAN_TAN] = CardName.HATSUMI_JUN_BI_MAN_TAN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_COMPASS] = CardName.HATSUMI_COMPASS
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_CALL_WAVE] = CardName.HATSUMI_CALL_WAVE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_ISANA_HAIL] = CardName.HATSUMI_ISANA_HAIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_OYOGIBI_FIRE] = CardName.HATSUMI_OYOGIBI_FIRE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_KIRAHARI_LIGHTHOUSE] = CardName.HATSUMI_KIRAHARI_LIGHTHOUSE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_MIOBIKI_ROUTE] = CardName.HATSUMI_MIOBIKI_ROUTE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_TORPEDO] = CardName.HATSUMI_TORPEDO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_SAGIRI_HAIL] = CardName.HATSUMI_SAGIRI_HAIL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_WADANAKA_ROUTE] = CardName.HATSUMI_WADANAKA_ROUTE

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_JIN_DU] = CardName.MIZUKI_JIN_DU
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_BAN_GONG] = CardName.MIZUKI_BAN_GONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_SHOOTING_DOWN] = CardName.MIZUKI_SHOOTING_DOWN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_HO_LYEONG] = CardName.MIZUKI_HO_LYEONG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_BANG_BYEOG] = CardName.MIZUKI_BANG_BYEOG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_OVERPOWERING_GO_FORWARD] = CardName.MIZUKI_OVERPOWERING_GO_FORWARD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_JEON_JANG] = CardName.MIZUKI_JEON_JANG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_HACHIRYU_CHEONJUGAK] = CardName.MIZUKI_HACHIRYU_CHEONJUGAK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_HIJAMARU_TRIPLET] = CardName.MIZUKI_HIJAMARU_TRIPLET
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_TARTENASHI_DAESUMUN] = CardName.MIZUKI_TARTENASHI_DAESUMUN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_MIZUKI_BATTLE_CRY] = CardName.MIZUKI_MIZUKI_BATTLE_CRY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KODAMA_TU_SIN] = CardName.KODAMA_TU_SIN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_1] = CardName.SOLDIER_SPEAR_1
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_2] = CardName.SOLDIER_SPEAR_2
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SHIELD] = CardName.SOLDIER_SHIELD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_HORSE] = CardName.SOLDIER_HORSE

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_GONG_SUM] = CardName.MEGUMI_GONG_SUM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_TA_CHEOG] = CardName.MEGUMI_TA_CHEOG
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_SHELL_ATTACK] = CardName.MEGUMI_SHELL_ATTACK
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_POLE_THRUST] = CardName.MEGUMI_POLE_THRUST
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_REED] = CardName.MEGUMI_REED
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_BALSAM] = CardName.MEGUMI_BALSAM
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_WILD_ROSE] = CardName.MEGUMI_WILD_ROSE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_ROOT_OF_CAUSALITY] = CardName.MEGUMI_ROOT_OF_CAUSALITY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_BRANCH_OF_POSSIBILITY] = CardName.MEGUMI_BRANCH_OF_POSSIBILITY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_FRUIT_OF_END] = CardName.MEGUMI_FRUIT_OF_END
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_MEGUMI_PALM] = CardName.MEGUMI_MEGUMI_PALM

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_IMAGE] = CardName.KANAWE_IMAGE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_SCREENPLAY] = CardName.KANAWE_SCREENPLAY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_PRODUCTION] = CardName.KANAWE_PRODUCTION
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_PUBLISH] = CardName.KANAWE_PUBLISH
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_AFTERGLOW] = CardName.KANAWE_AFTERGLOW
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_IMPROMPTU] = CardName.KANAWE_IMPROMPTU
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_SEAL] = CardName.KANAWE_SEAL
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_VAGUE_STORY] = CardName.KANAWE_VAGUE_STORY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_INFINITE_STARLIGHT] = CardName.KANAWE_INFINITE_STARLIGHT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_BEND_OVER_THIS_NIGHT] = CardName.KANAWE_BEND_OVER_THIS_NIGHT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_DISTANT_SKY] = CardName.KANAWE_DISTANT_SKY
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_KANAWE] = CardName.KANAWE_KANAWE

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_SAL_JIN] = CardName.IDEA_SAL_JIN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_SAKURA_WAVE] = CardName.IDEA_SAKURA_WAVE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_WHISTLE] = CardName.IDEA_WHISTLE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_MYEONG_JEON] = CardName.IDEA_MYEONG_JEON
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_EMPHASIZING] = CardName.IDEA_EMPHASIZING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_POSITIONING] = CardName.IDEA_POSITIONING

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_RED_BLADE] = CardName.KAMUWI_RED_BLADE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_FLUTTERING_BLADE] = CardName.KAMUWI_FLUTTERING_BLADE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_SI_KEN_LAN_JIN] = CardName.KAMUWI_SI_KEN_LAN_JIN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_CUT_DOWN] = CardName.KAMUWI_CUT_DOWN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_THREADING_THORN] = CardName.KAMUWI_THREADING_THORN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_KE_SYO_LAN_LYU] = CardName.KAMUWI_KE_SYO_LAN_LYU
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_BLOOD_WAVE] = CardName.KAMUWI_BLOOD_WAVE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_LAMP] = CardName.KAMUWI_LAMP
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_DAWN] = CardName.KAMUWI_DAWN
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_GRAVEYARD] = CardName.KAMUWI_GRAVEYARD
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_KATA_SHIRO] = CardName.KAMUWI_KATA_SHIRO
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_LOGIC] = CardName.KAMUWI_LOGIC

        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FALSE_STAB] = CardName.RENRI_FALSE_STAB
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_TEMPORARY_EXPEDIENT] = CardName.RENRI_TEMPORARY_EXPEDIENT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_BLACK_AND_WHITE] = CardName.RENRI_BLACK_AND_WHITE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_IRRITATING_GESTURE] = CardName.RENRI_IRRITATING_GESTURE
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FLOATING_CLOUDS] = CardName.RENRI_FLOATING_CLOUDS
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FISHING] = CardName.RENRI_FISHING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_PULLING_FISHING] = CardName.RENRI_PULLING_FISHING
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_RU_RU_RA_RA_RI] = CardName.RENRI_RU_RU_RA_RA_RI
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA] = CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_O_RI_RE_TE_RA_RE_RU] = CardName.RENRI_O_RI_RE_TE_RA_RE_RU
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_RENRI_THE_END] = CardName.RENRI_RENRI_THE_END
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_ENGRAVED_GARMENT] = CardName.RENRI_ENGRAVED_GARMENT
        cardNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KIRIKO_SHAMANISTIC_MUSIC] = CardName.KIRIKO_SHAMANISTIC_MUSIC

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
        cardDataHashmap[CardName.SAINE_BETRAYAL] = betrayer
        cardDataHashmap[CardName.SAINE_FLOWING_WALL] = flowingWall
        cardDataHashmap[CardName.SAINE_JEOL_CHANG_JEOL_HWA] = jeolChangJeolWha

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
        cardDataHashmap[CardName.YUKIHI_HELP_SLASH_THREAT] = helpOrThreat
        cardDataHashmap[CardName.YUKIHI_THREAD_SLASH_RAW_THREAD] = threadOrRawThread
        cardDataHashmap[CardName.YUKIHI_FLUTTERING_COLLAR] = flutteringCollar

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
        cardDataHashmap[CardName.SHINRA_ZHEN_YEN] = zhenYen
        cardDataHashmap[CardName.SHINRA_SA_DO] = sado
        cardDataHashmap[CardName.SHINRA_ZEN_CHI_KYO_TEN] = zenChiKyoTen

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
        cardDataHashmap[CardName.KURURU_ANALYZE] = analyze
        cardDataHashmap[CardName.KURURU_DAUZING] = dauzing
        cardDataHashmap[CardName.KURURU_LAST_RESEARCH] = lastResearch
        cardDataHashmap[CardName.KURURU_GRAND_GULLIVER] = grandGulliver

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
        cardDataHashmap[CardName.THALLYA_QUICK_CHANGE] = quickChange
        cardDataHashmap[CardName.THALLYA_BLACKBOX_NEO] = blackboxNeo
        cardDataHashmap[CardName.THALLYA_OMNIS_BLASTER] = omnisBlaster
        cardDataHashmap[CardName.FORM_KINNARI] = formKinnari
        cardDataHashmap[CardName.FORM_ASURA] = formAsura
        cardDataHashmap[CardName.FORM_DEVA] = formDeva

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
        cardDataHashmap[CardName.RAIRA_STORM] = storm
        cardDataHashmap[CardName.RAIRA_FURIOUS_STORM] = furiousStorm
        cardDataHashmap[CardName.RAIRA_JIN_PUNG_JE_CHEON_UI] = jinPungJeCheonUi

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

        cardDataHashmap[CardName.HONOKA_SPIRIT_SIK] = spiritSik
        cardDataHashmap[CardName.HONOKA_GUARDIAN_SPIRIT_SIK] = guardianSik
        cardDataHashmap[CardName.HONOKA_ASSAULT_SPIRIT_SIK] = assaultSik
        cardDataHashmap[CardName.HONOKA_DIVINE_OUKA] = divineOuka
        cardDataHashmap[CardName.HONOKA_SAKURA_BLIZZARD] = sakuraBlizzard
        cardDataHashmap[CardName.HONOKA_UI_GI_GONG_JIN]= yuGiGongJin
        cardDataHashmap[CardName.HONOKA_SAKURA_WING]= sakuraWing
        cardDataHashmap[CardName.HONOKA_REGENERATION] = regeneration
        cardDataHashmap[CardName.HONOKA_SAKURA_AMULET] = sakuraAmulet
        cardDataHashmap[CardName.HONOKA_HONOKA_SPARKLE] = honokaSparkle
        cardDataHashmap[CardName.HONOKA_COMMAND] = command
        cardDataHashmap[CardName.HONOKA_TAIL_WIND]= tailWind
        cardDataHashmap[CardName.HONOKA_CHEST_WILLINGNESS] = chestWilling
        cardDataHashmap[CardName.HONOKA_HAND_FLOWER]= handFlower
        cardDataHashmap[CardName.HONOKA_A_NEW_OPENING] = newOpening
        cardDataHashmap[CardName.HONOKA_UNDER_THE_NAME_OF_FLAG] = underFlag
        cardDataHashmap[CardName.HONOKA_FOUR_SEASON_BACK] = fourSeason
        cardDataHashmap[CardName.HONOKA_FULL_BLOOM_PATH] = bloomPath
        cardDataHashmap[CardName.HONOKA_SAKURA_SWORD] = sakuraSword
        cardDataHashmap[CardName.HONOKA_SHADOW_HAND] = shadowHand
        cardDataHashmap[CardName.HONOKA_EYE_OPEN_ALONE] = eyeOpenAlone
        cardDataHashmap[CardName.HONOKA_FOLLOW_TRACE] = followTrace
        cardDataHashmap[CardName.HONOKA_FACING_SHADOW] = facingShadow
        cardDataHashmap[CardName.HONOKA_SAKURA_SHINING_BRIGHTLY] = sakuraShiningBrightly
        cardDataHashmap[CardName.HONOKA_HOLD_HANDS] = holdHands
        cardDataHashmap[CardName.HONOKA_WALK_OLD_LOAD] = walkOldLoad

        cardDataHashmap[CardName.OBORO_SHURIKEN] = shuriken
        cardDataHashmap[CardName.OBORO_AMBUSH] = ambush
        cardDataHashmap[CardName.OBORO_BRANCH_OF_DIVINE] = branchOfDivine
        cardDataHashmap[CardName.OBORO_LAST_CRYSTAL] = lastCrystal

        cardDataHashmap[CardName.CHIKAGE_TRICK_UMBRELLA] = trickUmbrella
        cardDataHashmap[CardName.CHIKAGE_STRUGGLE] = struggle
        cardDataHashmap[CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON] = zanZeNoConnectionPoison

        cardDataHashmap[CardName.UTSURO_BITE_DUST] = biteDust
        cardDataHashmap[CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK] = deviceKururusik
        cardDataHashmap[CardName.UTSURO_MANG_A] = mangA
        cardDataHashmap[CardName.UTSURO_ANNIHILATION_SHADOW] = annihilationShadow
        cardDataHashmap[CardName.UTSURO_SILENT_WALK] = silentWalk
        cardDataHashmap[CardName.UTSURO_DE_MISE] = deMise

        cardDataHashmap[CardName.KORUNU_SNOW_BLADE] = snowBlade
        cardDataHashmap[CardName.KORUNU_REVOLVING_BLADE] = revolvingBlade
        cardDataHashmap[CardName.KORUNU_BLADE_DANCE] = bladeDance
        cardDataHashmap[CardName.KORUNU_RIDE_SNOW] = snowRide
        cardDataHashmap[CardName.KORUNU_ABSOLUTE_ZERO] = absoluteZero
        cardDataHashmap[CardName.KORUNU_FROSTBITE] = frostbite
        cardDataHashmap[CardName.KORUNU_FROST_THORN_BUSH] = thornBush
        cardDataHashmap[CardName.KORUNU_CONLU_RUYANPEH] = conluRuyanpeh
        cardDataHashmap[CardName.KORUNU_LETAR_LERA] = letarLera
        cardDataHashmap[CardName.KORUNU_UPASTUM] = upastum
        cardDataHashmap[CardName.KORUNU_PORUCHARTO] = porucharto

        cardDataHashmap[CardName.YATSUHA_STAR_NAIL] = starNail
        cardDataHashmap[CardName.YATSUHA_DARKNESS_GILL] = darknessGill
        cardDataHashmap[CardName.YATSUHA_MIRROR_DEVIL] = mirrorDevil
        cardDataHashmap[CardName.YATSUHA_GHOST_STEP] = ghostStep
        cardDataHashmap[CardName.YATSUHA_WILLING] = willing
        cardDataHashmap[CardName.YATSUHA_CONTRACT] = contract
        cardDataHashmap[CardName.YATSUHA_CLINGY_FLOWER] = clingyFlower
        cardDataHashmap[CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE] = twoLeapMirrorDivine
        cardDataHashmap[CardName.YATSUHA_FOUR_LEAP_SONG] = fourLeapSong
        cardDataHashmap[CardName.YATSUHA_SIX_STAR_SEA] = sixStarSea
        cardDataHashmap[CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE] = eightMirrorOtherSide

        cardDataHashmap[CardName.HATSUMI_WATER_BALL] = waterBall
        cardDataHashmap[CardName.HATSUMI_WATER_CURRENT] = waterCurrent
        cardDataHashmap[CardName.HATSUMI_STRONG_ACID] = strongAcid
        cardDataHashmap[CardName.HATSUMI_TSUNAMI] = tsunami
        cardDataHashmap[CardName.HATSUMI_JUN_BI_MAN_TAN] = junBiManTen
        cardDataHashmap[CardName.HATSUMI_COMPASS] = compass
        cardDataHashmap[CardName.HATSUMI_CALL_WAVE] = callWave
        cardDataHashmap[CardName.HATSUMI_ISANA_HAIL] = isanaHail
        cardDataHashmap[CardName.HATSUMI_OYOGIBI_FIRE] = oyogibiFire
        cardDataHashmap[CardName.HATSUMI_KIRAHARI_LIGHTHOUSE] = kirahariLighthouse
        cardDataHashmap[CardName.HATSUMI_MIOBIKI_ROUTE] = miobikiRoute

        cardDataHashmap[CardName.MIZUKI_JIN_DU] = jinDu
        cardDataHashmap[CardName.MIZUKI_BAN_GONG] = banGong
        cardDataHashmap[CardName.MIZUKI_SHOOTING_DOWN] = shootingDown
        cardDataHashmap[CardName.MIZUKI_HO_LYEONG] = hoLyeong
        cardDataHashmap[CardName.MIZUKI_BANG_BYEOG] = bangByeog
        cardDataHashmap[CardName.MIZUKI_OVERPOWERING_GO_FORWARD] = overpoweringGoForward
        cardDataHashmap[CardName.MIZUKI_JEON_JANG] = jeonJang
        cardDataHashmap[CardName.MIZUKI_HACHIRYU_CHEONJUGAK] = hachiryuCheonjugak
        cardDataHashmap[CardName.MIZUKI_HIJAMARU_TRIPLET] = hijamaruTriplet
        cardDataHashmap[CardName.MIZUKI_TARTENASHI_DAESUMUN] = tartenashiDaesumun
        cardDataHashmap[CardName.MIZUKI_MIZUKI_BATTLE_CRY] = mizukiBattleCry
        cardDataHashmap[CardName.KODAMA_TU_SIN] = tusin
        cardDataHashmap[CardName.SOLDIER_SPEAR_1] = spearSoldier1
        cardDataHashmap[CardName.SOLDIER_SPEAR_2] = spearSoldier2
        cardDataHashmap[CardName.SOLDIER_SHIELD] = shieldSoldier
        cardDataHashmap[CardName.SOLDIER_HORSE] = horseSoldier

        cardDataHashmap[CardName.MEGUMI_GONG_SUM] = gongSum
        cardDataHashmap[CardName.MEGUMI_TA_CHEOG] = taCheog
        cardDataHashmap[CardName.MEGUMI_SHELL_ATTACK] = shellAttack
        cardDataHashmap[CardName.MEGUMI_POLE_THRUST] = poleThrust
        cardDataHashmap[CardName.MEGUMI_REED] = reed
        cardDataHashmap[CardName.MEGUMI_BALSAM] = balsam
        cardDataHashmap[CardName.MEGUMI_WILD_ROSE] = wildRose
        cardDataHashmap[CardName.MEGUMI_ROOT_OF_CAUSALITY] = rootCausality
        cardDataHashmap[CardName.MEGUMI_BRANCH_OF_POSSIBILITY] = branchPossibility
        cardDataHashmap[CardName.MEGUMI_FRUIT_OF_END] = fruitEnd
        cardDataHashmap[CardName.MEGUMI_MEGUMI_PALM] = megumiPalm

        cardDataHashmap[CardName.KANAWE_IMAGE] = image
        cardDataHashmap[CardName.KANAWE_SCREENPLAY] = screenplay
        cardDataHashmap[CardName.KANAWE_PRODUCTION] = production
        cardDataHashmap[CardName.KANAWE_PUBLISH] = publish
        cardDataHashmap[CardName.KANAWE_AFTERGLOW] = afterglow
        cardDataHashmap[CardName.KANAWE_IMPROMPTU] = impromptu
        cardDataHashmap[CardName.KANAWE_SEAL] = seal
        cardDataHashmap[CardName.KANAWE_VAGUE_STORY] = vagueStory
        cardDataHashmap[CardName.KANAWE_INFINITE_STARLIGHT] = infiniteStarlight
        cardDataHashmap[CardName.KANAWE_BEND_OVER_THIS_NIGHT] = bendOverThisNight
        cardDataHashmap[CardName.KANAWE_DISTANT_SKY] = distantSky
        cardDataHashmap[CardName.KANAWE_KANAWE] = kanawe

        cardDataHashmap[CardName.IDEA_SAL_JIN] = saljin
        cardDataHashmap[CardName.IDEA_SAKURA_WAVE] = sakuraWave
        cardDataHashmap[CardName.IDEA_WHISTLE] = whistle
        cardDataHashmap[CardName.IDEA_MYEONG_JEON] = myeongjeon
        cardDataHashmap[CardName.IDEA_EMPHASIZING] = emphasizing
        cardDataHashmap[CardName.IDEA_POSITIONING] = positioning

        cardDataHashmap[CardName.TOKOYO_PASSING_FEAR] = passingFear
        cardDataHashmap[CardName.TOKOYO_RELIC_EYE] = relicEye
        cardDataHashmap[CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN] = eightSakuraInVain

        cardDataHashmap[CardName.HAGANE_BONFIRE] = bonfire
        cardDataHashmap[CardName.HAGANE_WHEEL_SKILL] = wheelSkill
        cardDataHashmap[CardName.HAGANE_GRAND_SOFT_MATERIAL] = grandSoftMaterial
        cardDataHashmap[CardName.HAGANE_SOFT_ATTACK] = softAttack

        cardDataHashmap[CardName.KAMUWI_RED_BLADE] = redBlade
        cardDataHashmap[CardName.KAMUWI_FLUTTERING_BLADE] = flutteringBlade
        cardDataHashmap[CardName.KAMUWI_SI_KEN_LAN_JIN] = siKenLanJin
        cardDataHashmap[CardName.KAMUWI_CUT_DOWN] = cutDown
        cardDataHashmap[CardName.KAMUWI_THREADING_THORN] = threadingThorn
        cardDataHashmap[CardName.KAMUWI_KE_SYO_LAN_LYU] = keSyoLanLyu
        cardDataHashmap[CardName.KAMUWI_BLOOD_WAVE] = bloodWave
        cardDataHashmap[CardName.KAMUWI_LAMP] = lamp
        cardDataHashmap[CardName.KAMUWI_DAWN] = dawn
        cardDataHashmap[CardName.KAMUWI_GRAVEYARD] = graveYard
        cardDataHashmap[CardName.KAMUWI_KATA_SHIRO] = kataShiro
        cardDataHashmap[CardName.KAMUWI_LOGIC] = logic

        cardDataHashmap[CardName.RENRI_FALSE_STAB] = falseStab
        cardDataHashmap[CardName.RENRI_TEMPORARY_EXPEDIENT] = temporaryExpedient
        cardDataHashmap[CardName.RENRI_BLACK_AND_WHITE] = blackAndWhite
        cardDataHashmap[CardName.RENRI_IRRITATING_GESTURE] = irritatingGesture
        cardDataHashmap[CardName.RENRI_FLOATING_CLOUDS] = floatingClouds
        cardDataHashmap[CardName.RENRI_FISHING] = fishing
        cardDataHashmap[CardName.RENRI_PULLING_FISHING] = pullingFishing
        cardDataHashmap[CardName.RENRI_RU_RU_RA_RA_RI] = rururarari
        cardDataHashmap[CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA] = ranararomirerira
        cardDataHashmap[CardName.RENRI_O_RI_RE_TE_RA_RE_RU] = orireterareru
        cardDataHashmap[CardName.RENRI_RENRI_THE_END] = renriTheEnd
        cardDataHashmap[CardName.RENRI_ENGRAVED_GARMENT] = engravedGarment
        cardDataHashmap[CardName.KIRIKO_SHAMANISTIC_MUSIC] = shamanisticMusic

        cardDataHashmap[CardName.YATSUHA_HOLY_RAKE_HANDS] = holyRakeHand
        cardDataHashmap[CardName.YATSUHA_ENTRANCE_OF_ABYSS] = entranceOfAbyss
        cardDataHashmap[CardName.YATSUHA_TRUE_MONSTER] = trueMonster
        cardDataHashmap[CardName.YATSUHA_GHOST_LINK] = ghostLink
        cardDataHashmap[CardName.YATSUHA_RESOLUTION] = resolution
        cardDataHashmap[CardName.YATSUHA_PLEDGE] = pledge
        cardDataHashmap[CardName.YATSUHA_VAIN_FLOWER] = vainFlower
        cardDataHashmap[CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA] = eightMirrorVainSakura

        cardDataHashmap[CardName.KURURU_BLASTER] = blaster
        cardDataHashmap[CardName.KURURU_RAILGUN] = railgun
        cardDataHashmap[CardName.KURURU_CONNECT_DIVE] = connectDive

        cardDataHashmap[CardName.HATSUMI_TORPEDO] = torpedo
        cardDataHashmap[CardName.HATSUMI_SAGIRI_HAIL] = sagiriHail
        cardDataHashmap[CardName.HATSUMI_WADANAKA_ROUTE] = wadanakaRoute
    }

    private suspend fun selectDustToDistance(nowCommand: CommandEnum, game_status: GameStatus,
                                             user: PlayerEnum, owner: PlayerEnum, card_number: Int): Boolean{
        if(nowCommand == CommandEnum.SELECT_ONE){
            game_status.dustToDistance(1, Arrow.BOTH_DIRECTION, user, owner, card_number)
            return true
        }
        else if(nowCommand == CommandEnum.SELECT_TWO){
            game_status.distanceToDust(1, Arrow.BOTH_DIRECTION, user, owner, card_number)
            return true
        }
        return false
    }

    val termination = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION, null)
    val chasm = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null)

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
                buff_game_status.getAdjustDistance() <= 2
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
        apdo.addtext(chasm)
        apdo.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.YURINA_APDO, card_number, CardClass.NULL,
                    sortedSetOf(1, 2, 3, 4), 3,  999,  MegamiEnum.YURINA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ), null) ){
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
        pobaram.addtext(termination)
        pobaram.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-2)
                }))
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.dustToAura(player, 5, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        jjockbae.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {_, cardNumber, beforeLife,
                afterLife, _, _ ->
                if(beforeLife > 3 && afterLife <= 3){
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
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_DOUBLEBEGI, card_number, CardClass.NULL,
                        sortedSetOf(4, 5), 2,  1,  MegamiEnum.SAINE,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })
        hurubegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        moogechoo.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        moogechoo.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        ganpa.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_REACTABLE) {_, player, game_status, _ ->
            if(palSang(player, game_status)) 1
            else 0
        })
        ganpa.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_SAINE_GANPA)
                if(selectDustToDistance(nowCommand, game_status, player, game_status.getCardOwner(card_number), card_number)) break
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
        choongemjung.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-1)
                }))
            null
        })
        choongemjung.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_CHOONGEMJUNG, card_number, CardClass.NULL,
                    sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 1,  999,   MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        choongemjung.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        mooembuck.setEnchantment(5)
        //-1 means every nap token can use as aura
        mooembuck.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE) {_, _, _, _ ->
            null
        })
        yuldonghogek.setSpecial(6)
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_YULDONGHOGEK, card_number, CardClass.NULL,
                    sortedSetOf(3, 4), 1,  1, MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number,  player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_YULDONGHOGEK, card_number, CardClass.NULL,
                    sortedSetOf(4, 5), 1,  1, MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        yuldonghogek.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_YULDONGHOGEK, card_number, CardClass.NULL,
                    sortedSetOf(3, 4, 5), 2,  2,  MegamiEnum.SAINE,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)) {
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
        hangmunggongjin.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.auraToDistance(player.opposite(), 2,
                Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        emmooshoebing.setSpecial(2)
        emmooshoebing.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        emmooshoebing.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
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
        jonggek.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, _, _, reactAttack->
            if(reactAttack != null && reactAttack.card_class == CardClass.SPECIAL) 1
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
        magnumcanon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.lifeToDust(player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number),
                card_number)
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
        backstep.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        backdraft.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })
        backdraft.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(yeonwha(player, game_status)){
                game_status.addThisTurnAttackBuff(player, Buff(card_number,1, BufTag.PLUS_MINUS, {_, _, attack ->
                    (attack.megami != MegamiEnum.HIMIKA) && (attack.getEditedAuraDamage() != 999)},
                    { _, _, attack -> attack.run{
                        auraPlusMinus(1); lifePlusMinus(1)
                    }
                    }))
            }
            null
        })
        smoke.setEnchantment(3)
        //FORBID_MOVE_TOKEN return FromLocationEnum * 100 + ToLocationEnum (if anywhere it will be 99)
        smoke.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_MOVE_TOKEN_USING_ARROW){ _, _, _, _ ->
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
            if(game_status.getAdjustDistance() == 0){
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
        burmilionfield.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {card_number, player, game_status, _->
            if(yeonwha(player, game_status)){
                game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
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
            if((card_number.toCardName() == CardName.TOKOYO_BITSUNERIGI || dupligearCheck(card_number.toCardName())) && kyochi(player, game_status)){
                game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_TOP, card_number)
            }
            null
        })
        wooahhantaguck.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wooahhantaguck.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, react_attack ->
            if(kyochi(player, game_status) && react_attack?.card_class != CardClass.SPECIAL){
                react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })
        runningrabit.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            if(game_status.getAdjustDistance() <= 3){
                game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        poetdance.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        poetdance.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_TOKOYO_POETDANCE)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.flareToAura(player, player, 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToDistance(player, 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    break
                }
            }
            null
        })
        flipfan.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            while (true){
                val set = mutableSetOf<Int>()
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_TOKOYO_FLIPFAN
                ) {card, from -> !(from == LocationEnum.DISCARD_YOUR && card.isSoftAttack)}?: break
                set.addAll(list)
                if (set.size <= 2){
                    if(list.isNotEmpty()){
                        for (cardNumber in list){
                            game_status.popCardFrom(player, cardNumber, LocationEnum.DISCARD_YOUR, true)?.let {
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
        flipfan.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            game_status.dustToAura(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        windstage.setEnchantment(2)
        windstage.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.distanceToAura(player, 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        windstage.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.auraToDistance(player, 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        sunstage.setEnchantment(2)
        sunstage.addtext(termination)
        sunstage.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.setConcentration(player, 2)
            null
        })
        sunstage.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.TOKOYO_SUNSTAGE, card_number, CardClass.NULL,
                    sortedSetOf(3, 4, 5, 6), 999,  1,  MegamiEnum.TOKOYO,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        kuon.setSpecial(5)
        kuon.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kuon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {card_number, _, _, react_attack ->
            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
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
        endlesswind.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_TOKOYO_ENDLESSWIND)
                { card, _ -> card.card_data.card_type != CardType.ATTACK && card.card_data.canDiscard}
                if(list == null){
                    game_status.showSome(player.opposite(), CommandEnum.SHOW_HAND_ALL_YOUR, -1)
                    break
                }
                else{
                    if (list.size == 1){
                        val card = game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, true)?: continue
                        game_status.insertCardTo(player.opposite(), card, LocationEnum.DISCARD_YOUR, true)
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
        ninjawalk.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
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
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_OBORO_INDUCE)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.distanceToAura(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.auraToFlare(player.opposite(), player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    break
                }
            }
            null
        })
        clone.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {_, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_OBORO_CLONE)
                {card, _ -> card.card_data.sub_type != SubType.FULL_POWER}
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
                        val secondCard = game_status.getCardFrom(player, selectNumber, LocationEnum.DISCARD_YOUR)?: break
                        game_status.useCardFrom(player, secondCard, LocationEnum.DISCARD_YOUR, false, null,
                            isCost = true, isConsume = true)
                        break
                    }
                }
            }
            null
        })
        bioactivity.setEnchantment(4)
        bioactivity.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION, null))
        bioactivity.addtext(chasm)
        bioactivity.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RETURN_OTHER_CARD) {_, player, game_status, _ ->
            while(true) {
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.YOUR_USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_OBORO_BIOACTIVITY) { _, _ -> true }?: break
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
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.OBORO_KUMASUKE, card_number, CardClass.NULL,
                        sortedSetOf(3, 4),2,  2,  MegamiEnum.OBORO,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })
        tobikage.setSpecial(4)
        tobikage.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{_, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_OBORO_TOBIKAGE)
                {card, _ -> card.card_data.sub_type != SubType.FULL_POWER} ?: run {
                    return@ret null
                }
                if(selected.size == 1){
                    val selectNumber = selected[0]
                    val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                    game_status.useCardFrom(player, card, LocationEnum.COVER_CARD, false, react_attack,
                        isCost = true, isConsume = true)
                    break
                }
                if(selected.size == 0){
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
        mikazra.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
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

    private val changeUmbrellaText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { _, player, game_status, _ ->
        while(true){
            val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_YUKIHI)
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
    }

    private fun yukihiCardInit(){
        yukihi.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.MEGAMI_YOUR, changeUmbrellaText)
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
        pushOut.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_PUSH_OUT_SLASH_PULL)
                if(selectDustToDistance(nowCommand, game_status, player, game_status.getCardOwner(card_number), card_number)) break
            }
            null
        })
        pushOut.addTextUnfold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            game_status.distanceToDust(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        swing.umbrellaMark = true
        swing.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 5, 999)
        swing.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 999, 2)
        turnUmbrella.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.SHOW_HAND_WHEN_CHANGE_UMBRELLA) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_TURN_UMBRELLA)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.showSome(player, CommandEnum.SHOW_HAND_YOUR, card_number)
                    game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
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
        backwardStep.addTextFold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        backwardStep.addTextUnfold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        makeConnection.setEnchantment(2)
        makeConnection.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(game_status.getUmbrella(player) == Umbrella.UNFOLD) {
                game_status.dustToDistance(1,
                    Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            else {
                game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        makeConnection.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(game_status.getUmbrella(player) == Umbrella.UNFOLD) {
                game_status.distanceToDust(1,
                    Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            else {
                game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
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
                            if(madeAttack.kururuChange2X){
                                when(madeAttack.card_name){
                                    CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                                        madeAttack.run { addRange(Pair(2, 4)); addRange(Pair(6, 8))}
                                    }
                                    CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                                        madeAttack.run { addRange(Pair(2, 4)); addRange(Pair(7, 8))}
                                    }
                                    CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                                        madeAttack.run { addRange(Pair(2, 4)); addRange(Pair(4, 7))}
                                    }
                                    CardName.YUKIHI_SWING_SLASH_STAB -> {
                                        madeAttack.run { addRange(Pair(2, 4)); addRange(Pair(6, 8))}
                                    }
                                    CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                                        madeAttack.run { addRange(Pair(2, 4)); addRange(Pair(5, 8))}
                                    }
                                    CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                                        madeAttack.run { addRange(Pair(2, 2)); addRange(Pair(6, 8))}
                                    }
                                    CardName.YUKIHI_HELP_SLASH_THREAT -> {
                                        madeAttack.run { addRange(Pair(3, 4)); addRange(Pair(5, 7))}
                                    }
                                    CardName.YUKIHI_THREAD_SLASH_RAW_THREAD -> {
                                        madeAttack.run { addRange(Pair(2, 6)); addRange(Pair(4, 10))}
                                    }
                                    else -> {}
                                }
                            }
                            else{
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
                                        madeAttack.run { addRange(Pair(1, 3)); addRange(Pair(4, 7))}
                                    }
                                    CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                                        madeAttack.run { addRange(Pair(1, 1)); addRange(Pair(5, 7))}
                                    }
                                    CardName.YUKIHI_HELP_SLASH_THREAT -> {
                                        madeAttack.run { addRange(Pair(2, 3)); addRange(Pair(4, 6))}
                                    }
                                    CardName.YUKIHI_THREAD_SLASH_RAW_THREAD -> {
                                        madeAttack.run { addRange(Pair(1, 5)); addRange(Pair(3, 9))}
                                    }
                                    else -> {}
                                }
                            }
                        }
                        madeAttack.kururuChangeRangeUnder -> {
                            if(madeAttack.kururuChange2X){
                                when(madeAttack.card_name){
                                    CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                                        madeAttack.run { addRange(Pair(-2, 0)); addRange(Pair(2, 4))}
                                    }
                                    CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                                        madeAttack.run { addRange(Pair(-2, 0)); addRange(Pair(3, 4))}
                                    }
                                    CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                                        madeAttack.run { addRange(Pair(-2, 0)); addRange(Pair(0, 3))}
                                    }
                                    CardName.YUKIHI_SWING_SLASH_STAB -> {
                                        madeAttack.run { addRange(Pair(-2, 0)); addRange(Pair(2, 4))}
                                    }
                                    CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                                        madeAttack.run { addRange(Pair(-2, 0)); addRange(Pair(1, 4))}
                                    }
                                    CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                                        madeAttack.run { addRange(Pair(-2, -2)); addRange(Pair(2, 4))}
                                    }
                                    CardName.YUKIHI_HELP_SLASH_THREAT -> {
                                        madeAttack.run { addRange(Pair(-1, 0)); addRange(Pair(1, 3))}
                                    }
                                    CardName.YUKIHI_THREAD_SLASH_RAW_THREAD -> {
                                        madeAttack.run { addRange(Pair(-2, 2)); addRange(Pair(0, 6))}
                                    }
                                    else -> {}
                                }
                            }
                            else{
                                when(madeAttack.card_name){
                                    CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                                        madeAttack.run { addRange(Pair(-1, 1)); addRange(Pair(3, 5))}
                                    }
                                    CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                                        madeAttack.run { addRange(Pair(-1, 1)); addRange(Pair(4, 5))}
                                    }
                                    CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                                        madeAttack.run { addRange(Pair(-1, 1)); addRange(Pair(1, 4))}
                                    }
                                    CardName.YUKIHI_SWING_SLASH_STAB -> {
                                        madeAttack.run { addRange(Pair(-1, 1)); addRange(Pair(3, 5))}
                                    }
                                    CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                                        madeAttack.run { addRange(Pair(-1, 1)); addRange(Pair(2, 5))}
                                    }
                                    CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                                        madeAttack.run { addRange(Pair(-1, -1)); addRange(Pair(3, 5))}
                                    }
                                    CardName.YUKIHI_HELP_SLASH_THREAT -> {
                                        madeAttack.run { addRange(Pair(0, 1)); addRange(Pair(2, 4))}
                                    }
                                    CardName.YUKIHI_THREAD_SLASH_RAW_THREAD -> {
                                        madeAttack.run { addRange(Pair(-1, 3)); addRange(Pair(1, 7))}
                                    }
                                    else -> {}
                                }
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
                                CardName.YUKIHI_HELP_SLASH_THREAT -> {
                                    madeAttack.run { addRange(Pair(1, 2)); addRange(Pair(3, 5))}
                                }
                                CardName.YUKIHI_THREAD_SLASH_RAW_THREAD -> {
                                    madeAttack.run { addRange(Pair(0, 4)); addRange(Pair(2, 8))}
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
        swirlingGesture.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.changeUmbrella(player)
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
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

    private val setStratagemText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { _, player, game_status, _ ->
        setStratagemByUser(game_status, player)
        null
    }

    private suspend fun setStratagemByUser(game_status: GameStatus, player: PlayerEnum){
        while(true){
            val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_SHINRA_SHINRA)
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

    private fun shinraCardInit(){
        shinra.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).stratagem == null){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.MEGAMI_YOUR, setStratagemText)
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
        banlon.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_NO_DAMAGE){card_number, _, _, react_attack ->
            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { player, game_status, attack ->
                val damage = attack.getDamage(game_status, player,  game_status.getPlayerAttackBuff(player))
                damage.first >= 3 && attack.card_class != CardClass.SPECIAL
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
        kiben.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RUN_STRATAGEM){_, player, game_status, _ ->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.getPlayer(player.opposite()).deckToCoverCard(game_status, 3)
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                Stratagem.GUE_MO -> {
                    val beforeJustRunNoCondition = game_status.getPlayer(player).justRunNoCondition
                    while (true){
                        val list = game_status.selectCardFrom(player.opposite(), player, player,
                            listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_SHINRA_KIBEN)
                        { _, _ -> true }?: break
                        if (list.isNotEmpty()){
                            game_status.getPlayer(player).justRunNoCondition = true
                            if (list.size == 1){
                                game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD_YOUR, true)?.let {
                                    game_status.useCardFrom(player, it, LocationEnum.DISCARD_OTHER, false, null,
                                        isCost = true, isConsume = true)
                                }?: continue
                                break
                            }
                            game_status.getPlayer(player).justRunNoCondition = beforeJustRunNoCondition
                        }
                        else{
                            break
                        }
                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                null -> {}
            }
            null
        })
        inyong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {_, player, game_status, _->
            while(true){
                val selected = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_INYONG)
                {_, _ -> true} ?: break
                if(selected.size == 0) break
                if(selected.size == 1){
                    val selectNumber = selected[0]
                    val card = game_status.getCardFrom(player.opposite(), selectNumber, LocationEnum.HAND)?: continue
                    if(card.card_data.card_type != CardType.ATTACK) continue
                    while(true){
                        val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_SHINRA_INYONG)
                        if(nowCommand == CommandEnum.SELECT_ONE){
                            game_status.useCardFrom(player, card, LocationEnum.HAND_OTHER, false, null,
                                isCost = true, isConsume = true)
                            break
                        }
                        else if(nowCommand == CommandEnum.SELECT_TWO){
                            game_status.insertCardTo(player.opposite(), game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, true)!!,
                                LocationEnum.COVER_CARD, true)
                            break
                        }
                    }
                    if(card.card_data.sub_type == SubType.FULL_POWER) game_status.endCurrentPhase = true
                    break
                }
            }
            null
        })
        seondong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) {card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                Stratagem.GUE_MO -> {
                    game_status.distanceToAura(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
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
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                Stratagem.GUE_MO -> {
                    if (game_status.getPlayer(player.opposite()).hand.size <= 1){
                        game_status.setShrink(player.opposite())
                        game_status.drawCard(player.opposite(), 3)
                        game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                            listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                            NUMBER_SHINRA_JANGDAM, 2)
                        {_, _ -> true}?.let { selected ->
                            if(selected.size == 1){
                                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                                }
                            }
                            else{
                                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true,
                                    discardCheck = false)?.let {
                                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                                }
                                game_status.popCardFrom(player.opposite(), selected[1], LocationEnum.HAND, true)?.let {
                                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                                }
                            }
                        }
                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                null -> {}
            }
            null
        })
        nonpa.setEnchantment(4)
        nonpa.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.SEAL_CARD) {card_number, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.DISCARD_YOUR),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_NONPA, 1)
            {card, _ -> !(card.isSoftAttack)}?.let {selected ->
                val nowPlayer = game_status.getPlayer(player)
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                    nowPlayer.sealInformation[card_number]?.add(it.card_number) ?: run {
                        nowPlayer.sealInformation[card_number] = mutableListOf(it.card_number)
                    }
                    game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                }
            }
            null
        })
        nonpa.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.SEAL_CARD) {card_number, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)
            val otherPlayer = game_status.getPlayer(player)
            nowPlayer.sealInformation[card_number]?.let { sealedList ->
                for(sealedCardNumber in sealedList){
                    game_status.popCardFrom(player, sealedCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        game_status.insertCardTo(it.player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                }
            }
            nowPlayer.sealInformation.remove(card_number)

            otherPlayer.sealInformation[card_number]?.let { sealedList ->
                for(sealedCardNumber in sealedList){
                    game_status.popCardFrom(player.opposite(), sealedCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        game_status.insertCardTo(it.player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                }
            }
            otherPlayer.sealInformation.remove(card_number)
            null
        })
        wanjeonNonpa.setSpecial(2)
        wanjeonNonpa.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.SEAL_CARD){card_number, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.DISCARD_YOUR),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_WANJEON_NONPA, 1)
            {card, _ -> !(card.isSoftAttack)}?.let {selected ->
                val nowPlayer = game_status.getPlayer(player)
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                    nowPlayer.sealInformation[card_number]?.add(it.card_number) ?: run {
                        nowPlayer.sealInformation[card_number] = mutableListOf(it.card_number)
                    }
                    game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                }
            }
            null
        })
        dasicIhae.setSpecial(2)
        dasicIhae.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) {card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.selectCardFrom(player, player, player, listOf(LocationEnum.DISCARD_YOUR, LocationEnum.YOUR_USED_CARD),
                        CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_DASIG_IHAE, 1)
                        {card, _ -> card.card_data.card_type == CardType.ENCHANTMENT}?.let { selected ->
                        game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?.also {
                            game_status.useCardFrom(player, it, LocationEnum.DISCARD_YOUR, false, null,
                                isCost = true, isConsume = false
                            )
                        }?: game_status.getCardFrom(player, selected[0], LocationEnum.YOUR_USED_CARD)?.also {
                            game_status.useCardFrom(player, it, LocationEnum.YOUR_USED_CARD, false, null,
                                isCost = true, isConsume = false
                            )
                        }?.let {card ->
                            if(card.card_data.sub_type == SubType.FULL_POWER){
                                game_status.endCurrentPhase = true
                            }
                        }
                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                Stratagem.GUE_MO -> {
                    game_status.selectCardFrom(player.opposite(), player, player,
                        listOf(LocationEnum.ENCHANTMENT_ZONE), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_SHINRA_CHEONJI_BANBAG, 1)
                    {card, _ -> card.card_data.card_class != CardClass.SPECIAL}?.let { selected ->
                        game_status.getCardFrom(player.opposite(), selected[0], LocationEnum.ENCHANTMENT_ZONE)?.let { card ->
                            game_status.cardToDust(player.opposite(), card.getNap(), card, false, card_number)
                            game_status.enchantmentDestruction(player.opposite(), card)
                        }

                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
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
                    val temp = getEditedLifeDamage(); tempEditedLifeDamage = getEditedAuraDamage(); tempEditedAuraDamage = temp
                }
            }))
            null
        })
        samraBanSho.setSpecial(6)
        samraBanSho.setEnchantment(6)
        samraBanSho.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.dustToLife(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        samraBanSho.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_ENCHANTMENT_DESTRUCTION_YOUR){ card_number, player, game_status, _ ->
            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                null, null, card_number)
            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            null
        })
        samraBanSho.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            game_status.gameEnd(null, player)
            null
        })
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

    private suspend fun centrifugal(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.startTurnDistance + 1 < game_status.getAdjustDistance() && !game_status.logger.checkThisTurnDoAttack(player)
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
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
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
        hyperRecoil.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() >= 5){
                game_status.distanceToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            else{
                game_status.flareToDistance(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
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
        wonMuRuyn.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(game_status.getPlayerFlare(player.opposite()) >= 3){
                game_status.flareToAura(player.opposite(), player, 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
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
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_HAGANE_RING_A_BELL)
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
        gravitationField.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                game_status.distanceToAura(player, 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            else{
                game_status.distanceToAura(player, 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
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
            }, {_, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = abs(gameStatus.getAdjustDistance() - gameStatus.startTurnDistance)
                    tempEditedAuraDamage = temp
                    tempEditedLifeDamage = if(temp % 2 == 0) temp / 2 else temp / 2 + 1
                }
            }))
            null
        })
        grandBellMegalobel.setSpecial(2)
        grandBellMegalobel.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(checkAllSpecialCardUsed(player, game_status, card_number)){
                game_status.dustToLife(player, 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        grandGravitationAttract.setSpecial(5)
        grandGravitationAttract.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.distanceToFlare(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
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
        grandMountainRespect.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {_, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HAGANE_GRAND_MOUNTAIN_RESPECT)
                {card, _ -> card.card_data.sub_type != SubType.FULL_POWER}?: break
                if(selected.size == 0) break
                else if(selected.size <= 2){
                    val card = game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?: continue
                    game_status.useCardFrom(player, card, LocationEnum.DISCARD_YOUR, false, null,
                        isCost = true, isConsume = true)
                    if(game_status.getEndTurn(player)) break
                    if(selected.size == 2){
                        val secondCard = game_status.getCardFrom(player, selected[1], LocationEnum.DISCARD_YOUR)?: break
                        game_status.useCardFrom(player, secondCard, LocationEnum.DISCARD_YOUR, false, null,
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
        poisonNeedle.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.INSERT_POISON) {_, player, game_status, _ ->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_CHIKAGE_POISON_NEEDLE, 1)[0]
                game_status.popCardFrom(player, get, LocationEnum.POISON_BAG, false)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_TOP, publicForOther = true, publicForYour = false)
                }
            }
            null
        })
        toZuChu.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 1, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        toZuChu.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.auraToDistance(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.getPlayer(player.opposite()).canNotGoForward = true
            null
        })
        cuttingNeck.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 2, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        cuttingNeck.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.INSERT_POISON) {_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).hand.size >= 2){
                game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_CHIKAGE_CUTTING_NECK, 1
                ) {_, _ -> true}?.let { selected ->
                    game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                    }
                }

            }
            null
        })
        poisonSmoke.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.INSERT_POISON) {_, player, game_status, _->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_CHIKAGE_POISON_SMOKE, 1)[0]
                game_status.popCardFrom(player, get, LocationEnum.POISON_BAG, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.HAND, true)
                }
            }
            null
        })
        tipToeing.setEnchantment(4)
        tipToeing.addtext(chasm)
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
            game_status.popCardFrom(player, game_status.getCardNumber(player, CardName.POISON_DEADLY_1), LocationEnum.POISON_BAG, false)?:
            game_status.popCardFrom(player, game_status.getCardNumber(player, CardName.POISON_DEADLY_2), LocationEnum.POISON_BAG, false)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_TOP, false)
            }
            null
        })
        hankiPoison.setSpecial(2)
        hankiPoison.setEnchantment(5)
        hankiPoison.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { player, game_status, attack ->
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
                        gameStatus.cardToDust(player, it.getNap(), it, false, cardNumber)
                        gameStatus.logger.insert(Log(player, LogText.END_EFFECT, cardNumber, -1))
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
        poisonHallucinogenic.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.flareToDust(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
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
        poisonDeadly1.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.auraToDust(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        poisonDeadly2.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.auraToDust(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
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

    private val biggolemText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
        val kikou = getKikou(player, game_status)
        if(kikou.reaction >= 1 && kikou.fullPower >= 2){
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_KURURU_BIG_GOLEM)){
                    CommandEnum.SELECT_ONE -> {
                        var connectDive = 0
                        for(card in game_status.getPlayer(player).usedSpecialCard.values){
                            connectDive += card.effectAllValidEffect(card.card_number * 10 + 4, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                        }
                        if(connectDive > 0){
                            if(game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 2), false,
                                    null, null, card_number) != -1){
                                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                                game_status.deckReconstruct(player, false)
                            }
                        }
                        else{
                            if(game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                                    null, null, card_number) != -1){
                                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                                game_status.deckReconstruct(player, false)
                            }
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
    }

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

    private fun getKikou(player: PlayerEnum, game_status: GameStatus, condition: (Card) -> Boolean): Kikou{
        val result = Kikou()
        val nowPlayer = game_status.getPlayer(player)
        for (card in nowPlayer.enchantmentCard.values + nowPlayer.usedSpecialCard.values + nowPlayer.discard) {
            if(condition(card)){
                calcKikou(card.card_data, result)
            }
        }
        return result
    }

    private suspend fun kururuoong(player: PlayerEnum, command: CommandEnum, game_status: GameStatus){
        when (command) {
            CommandEnum.SELECT_ONE -> {
                game_status.drawCard(player, 1)
            }
            CommandEnum.SELECT_TWO -> {
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_KURURUOONG, 1
                ) { _, _ -> true }?: return
                game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, false)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                }
            }
            CommandEnum.SELECT_THREE -> {
                val list = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_TORNADO, 1
                ) { _, _ -> true }?: return
                game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }
            else -> {}
        }
    }

    private suspend fun regainer(effect_number: Int, card_number: Int, player: PlayerEnum, game_status: GameStatus, plusMinus: Int){
        while(true){
            val list = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.COVER_CARD, LocationEnum.YOUR_USED_CARD, LocationEnum.DISCARD_YOUR),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KURURU_REGAINER)
            { card, _ -> card.card_data.sub_type != SubType.FULL_POWER && card.special_card_state != SpecialCardEnum.UNUSED &&
                    card.card_data.megami != MegamiEnum.KURURU}?: break
            if(list.size == 0) {
                break
            }
            else if(list.size > 1) {
                continue
            }
            else{
                var location = LocationEnum.DISCARD_YOUR
                val card = game_status.getCardFrom(player, list[0], LocationEnum.DISCARD_YOUR) ?:
                game_status.getCardFrom(player, list[0], LocationEnum.YOUR_USED_CARD).let {
                    location = LocationEnum.YOUR_USED_CARD
                    it
                }?:game_status.getCardFrom(player, list[0], LocationEnum.COVER_CARD).let {
                    location = LocationEnum.COVER_CARD
                    it
                }?: continue

                while(true){
                    when(game_status.receiveCardEffectSelect(player, effect_number)){
                        CommandEnum.SELECT_ONE -> {
                            if(card.card_data.card_type == CardType.ATTACK){
                                game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                    { _, _, attack -> attack.run {
                                        kururuChangeRangeUpper = true
                                        if(plusMinus > 1){
                                            kururuChange2X = true
                                        }
                                        val tempSet = editedDistance.toSortedSet()
                                        editedDistance.clear()
                                        for(i in tempSet){
                                            editedDistance.add(i + plusMinus)
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
                                        if(plusMinus > 1){
                                            kururuChange2X = true
                                        }
                                        val tempSet = editedDistance.toSortedSet()
                                        editedDistance.clear()
                                        for(i in tempSet){
                                            editedDistance.add(i - plusMinus)
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
                                        attack.auraPlusMinus(plusMinus)
                                    }))
                            }
                            break
                        }
                        CommandEnum.SELECT_FOUR -> {
                            if(card.card_data.card_type == CardType.ATTACK){
                                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                    {_, _, attack ->
                                        attack.auraPlusMinus(-plusMinus)
                                    }))
                            }
                            break
                        }
                        CommandEnum.SELECT_FIVE -> {
                            if(card.card_data.card_type == CardType.ATTACK){
                                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                    {_, _, attack ->
                                        attack.lifePlusMinus(plusMinus)
                                    }))
                            }
                            break
                        }
                        CommandEnum.SELECT_SIX -> {
                            if(card.card_data.card_type == CardType.ATTACK){
                                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true},
                                    {_, _, attack ->
                                        attack.lifePlusMinus(-plusMinus)
                                    }))
                            }
                            break
                        }
                        CommandEnum.SELECT_SEVEN -> {
                            if(card.card_data.card_type == CardType.ENCHANTMENT){
                                game_status.getPlayer(player).napBuff += plusMinus
                            }
                            break
                        }
                        CommandEnum.SELECT_EIGHT -> {
                            if(card.card_data.card_type == CardType.ENCHANTMENT){
                                game_status.getPlayer(player).napBuff -= plusMinus
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

    private fun duplicateCardDataForIndustria(card_data: CardData, card_name: CardName): CardData{
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

            growing = card_data.growing

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
        elekittel.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DAMAGE) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.behavior >= 3 && kikou.reaction >= 2) {
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            null
        })
        accelerator.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {_, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.enchantment >= 1 && kikou.behavior >= 2) {
                while(true){
                    val list = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_KURURU_ACCELERATOR
                    ) { card, _ -> card.card_data.sub_type == SubType.FULL_POWER }?: break
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
        kururuoong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            val firstCommand = game_status.receiveCardEffectSelect(player, NUMBER_KURURU_KURURUOONG)
            if(firstCommand != CommandEnum.SELECT_NOT){
                kururuoong(player, firstCommand, game_status)
                while(true){
                    val secondCommand = game_status.receiveCardEffectSelect(player, NUMBER_KURURU_KURURUOONG)
                    if(firstCommand == secondCommand) continue
                    kururuoong(player, secondCommand, game_status)
                    break
                }
            }
            null
        })
        tornado.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DAMAGE) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 2) {
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(10, 999), false,
                        null, null, card_number)
                }
                else{
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(5, 999), false,
                        null, null, card_number)
                }
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            if(kikou.enchantment >= 2){
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 1, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 2), false,
                        null, null, card_number)
                }
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                null, null, card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            null
        })
        regainer.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.enchantment >= 1 && kikou.reaction >= 1) {
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 2, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    regainer(1018, card_number, player, game_status, 2)
                    regainer(1018, card_number, player, game_status, 2)
                }
                else{
                    regainer(1004, card_number, player, game_status, 1)
                }
            }
            null
        })
        regainer.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_USE_COVER) {_, _, _, _ ->
            null
        })
        module.setEnchantment(3)
        module.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_USE_BEHAVIOR_END){ _, player, game_status, _ ->
            if(!(game_status.getEndTurn(player))){
                game_status.requestAndDoBasicOperation(player, 1005)
            }
            null
        })
        reflector.setEnchantment(0)
        reflector.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 1 && kikou.reaction >= 1) {
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 3, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                        game_status.dustToCard(player, 8, it, card_number)
                    }
                }
                else{
                    game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                        game_status.dustToCard(player, 4, it, card_number)
                    }
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
        drainDevil.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        drainDevil.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_SPECIAL_RETURN_YOUR) { card_number, player, game_status, _ ->
            if(!game_status.getPlayer(player).end_turn){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KURURU_DRAIN_DEVIL)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                                game_status.useCardFrom(player, it, LocationEnum.YOUR_USED_CARD, false, null,
                                    isCost = true, isConsume = false)
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
               game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, biggolemText)
            }
            null
        })
        bigGolem.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_FULL_POWER_USED_YOUR) { _, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, 1008)
            null
        })
        industria.setSpecial(1)
        industria.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.SEAL_CARD) ret@{card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player){
                val nowPlayer = game_status.getPlayer(player)
                if(card_number in nowPlayer.sealInformation){
                    return@ret null
                }
                while (true){
                    val list = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.HAND, LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_KURURU_INDUSTRIA
                    ) { card, _ -> card.card_data.card_type != CardType.ENCHANTMENT && !(card.isSoftAttack) }?: break
                    if (list.size == 1){
                        game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.SEAL_ZONE, true)
                            nowPlayer.sealInformation[card_number]?.add(it.card_number) ?: run {
                                nowPlayer.sealInformation[card_number] = mutableListOf(it.card_number)
                            }
                        }?: game_status.popCardFrom(player, list[0], LocationEnum.HAND, true)?.let {
                            nowPlayer.sealInformation[card_number]?.add(it.card_number) ?: run {
                                nowPlayer.sealInformation[card_number] = mutableListOf(it.card_number)
                            }
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
            val duplicateCardData: CardData? = ownerPlayer.sealZone[ownerPlayer.sealInformation[card_number]?.get(0)]?.card_data
            if(duplicateCardData != null){
                for(card in ownerPlayer.hand.values + ownerPlayer.normalCardDeck + ownerPlayer.discard + ownerPlayer.cover_card
                        + ownerPlayer.readySoldierZone.values + ownerPlayer.notReadySoldierZone.values
                        + game_status.getPlayer(PlayerEnum.PLAYER2).sealZone.values.filter {
                            it.player == game_status.getCardOwner(card_number)
                } + game_status.getPlayer(PlayerEnum.PLAYER1).sealZone.values.filter {
                            it.player == game_status.getCardOwner(card_number)
                }){
                    when(card.card_number.toCardName()){
                        CardName.KURURU_DUPLICATED_GEAR_1 -> card.card_data = duplicateCardDataForIndustria(duplicateCardData, CardName.KURURU_DUPLICATED_GEAR_1)
                        CardName.KURURU_DUPLICATED_GEAR_2 -> card.card_data = duplicateCardDataForIndustria(duplicateCardData, CardName.KURURU_DUPLICATED_GEAR_2)
                        CardName.KURURU_DUPLICATED_GEAR_3 -> card.card_data = duplicateCardDataForIndustria(duplicateCardData, CardName.KURURU_DUPLICATED_GEAR_3)
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
        kanshousouchiKururusik.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 2 && kikou.behavior >= 3 && kikou.enchantment >= 2){
                while(true){
                    val list = game_status.selectCardFrom(player.opposite(), player, player,
                        listOf(LocationEnum.SPECIAL_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_KURURU_KANSHOUSOUCHI_KURURUSIK)
                    { _, _ ->
                        true
                    }?: break
                    if(list.size == 1){
                        game_status.popCardFrom(player.opposite(), list[0], LocationEnum.SPECIAL_CARD, true)?.let {
                            it.special_card_state = SpecialCardEnum.PLAYED
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_USED_CARD, true)
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
                val list = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.YOUR_USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_DUPLICATED_GEAR_3){ _, _ ->
                    true
                }?: break
                if(list.size == 1){
                    val card = game_status.getCardFrom(player.opposite(), list[0], LocationEnum.YOUR_USED_CARD)?: continue
                    game_status.useCardFrom(player, card, LocationEnum.YOUR_USED_CARD, false, null,
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
                changeToken = when(gameStatus.receiveCardEffectSelect(player, NUMBER_THALLYA_BURNING_STEAM)){
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

    private fun makeTransformList(player: PlayerEnum, game_status: GameStatus): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        val nowPlayer = game_status.getPlayer(player)
        nowPlayer.additionalHand[CardName.FORM_YAKSHA]?.let {
            cardList.add(it.card_number)
        }
        nowPlayer.additionalHand[CardName.FORM_KINNARI]?.let {
            cardList.add(it.card_number)
        }
        nowPlayer.additionalHand[CardName.FORM_ASURA]?.let {
            cardList.add(it.card_number)
        }
        nowPlayer.additionalHand[CardName.FORM_DEVA]?.let {
            cardList.add(it.card_number)
        }
        nowPlayer.additionalHand[CardName.FORM_NAGA]?.let {
            cardList.add(it.card_number)
        }
        nowPlayer.additionalHand[CardName.FORM_GARUDA]?.let {
            cardList.add(it.card_number)
        }
        return cardList
    }

    private suspend fun transform(player: PlayerEnum, game_status: GameStatus){
        val cardList = makeTransformList(player, game_status)
        game_status.getPlayer(player).transformNumber += 1
        if(cardList.size != 0){
            game_status.logger.insert(Log(player, LogText.TRANSFORM, -1, -1))
            val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_THALLYA_JULIA_BLACKBOX
                , 1)[0]
            game_status.getCardFrom(player, get, LocationEnum.ADDITIONAL_CARD)?.let {
                game_status.moveAdditionalCard(player, get.toCardName(), LocationEnum.TRANSFORM)
                it.special_card_state = SpecialCardEnum.PLAYED
                it.effectText(player, game_status, null, TextEffectTag.WHEN_TRANSFORM)
            }
        }
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
        stunt.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.auraToFlare(player, player,2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        roaring.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION){_, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken ?: 0) >= 2){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_THALLYA_ROARING)){
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
                    when(game_status.receiveCardEffectSelect(player, NUMBER_THALLYA_TURBO_SWITCH)){
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
        omegaBurst.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, react_attack ->
            val number = game_status.getPlayer(player).artificialTokenBurn
            game_status.restoreArtificialToken(player, number)
            react_attack?.addOtherBuff( OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
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
                    when(game_status.receiveCardEffectSelect(player, NUMBER_THALLYA_THALLYA_MASTERPIECE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.dustToDistance(1, Arrow.BOTH_DIRECTION, player,
                                game_status.getCardOwner(card_number), card_number)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.distanceToDust(1, Arrow.BOTH_DIRECTION, player,
                                game_status.getCardOwner(card_number), card_number)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
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
                transform(player, game_status)
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
        formNaga.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {card_number, player, game_status, _ ->
            val otherFlare = game_status.getPlayer(player.opposite()).flare
            if(otherFlare >= 3){
                game_status.flareToDust(player.opposite(), otherFlare - 2, Arrow.BOTH_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            null
        })
        formGaruda.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {_, player, game_status, _ ->
            game_status.drawCard(player, 2)
            game_status.getPlayer(player).maxHand = 99999
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
                    tempEditedAuraDamage = auraDamage
                }
            }))
            null
        })
        reincarnationNail.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        reincarnationNail.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            while(true){
                val list = game_status.selectCardFrom(player, player, player, listOf(LocationEnum.DISCARD_YOUR),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_RAIRA_REINCARNATION_NAIL
                ) { card, _ -> card.card_data.card_type == CardType.ATTACK && !(card.isSoftAttack)}?: break
                if(list.size == 0){
                    break
                }
                else if(list.size == 1){
                    game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_TOP, true)
                    }
                    break
                }
            }
            null
        })
        windRun.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            if(game_status.getAdjustDistance() >= 3){
                game_status.distanceToDust(2, Arrow.BOTH_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        wisdomOfStormSurge.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            game_status.getPlayer(player).thunderGauge?.let{
                if(game_status.getPlayer(player).windGauge!! + it >= 4){
                    while(true){
                        val list = game_status.selectCardFrom(player, player, player,
                            listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                            NUMBER_RAIRA_WISDOM_OF_STORM_SURGE
                        ) { card, _ -> card.card_data.megami != MegamiEnum.RAIRA && !(card.isSoftAttack)}?: break
                        if(list.size == 0){
                            break
                        }
                        else if(list.size == 1){
                            game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?.let {card ->
                                game_status.insertCardTo(player, card, LocationEnum.YOUR_DECK_TOP, true)
                            }
                            break
                        }
                    }
                }
            }
            null
        })
        wisdomOfStormSurge.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) {_, player, game_status, _->
            game_status.gaugeIncreaseRequest(player, NUMBER_RAIRA_WISDOM_OF_STORM_SURGE)
            null
        })
        howling.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) {_, player, game_status, _->
            game_status.setShrink(player.opposite())
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_HOWLING)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.thunderGaugeIncrease(player)
                        game_status.windGaugeIncrease(player)
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
                when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_WIND_KICK)){
                   CommandEnum.SELECT_ONE -> {
                       game_status.dustToDistance(3, Arrow.BOTH_DIRECTION, player,
                           game_status.getCardOwner(card_number), card_number)
                   }
                    CommandEnum.SELECT_TWO -> {
                        game_status.distanceToDust(3, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
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
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.RAIRA_SUMMON_THUNDER, card_number, CardClass.NULL,
                        sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 1,  1, MegamiEnum.RAIRA,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
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
                when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_CIRCULAR_CIRCUIT)){
                    CommandEnum.SELECT_ONE -> {
                        while(true){
                            val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_WIND_ATTACK)
                            if(selectDustToDistance(nowCommand, game_status, player,
                                    game_status.getCardOwner(card_number), card_number)) break
                        }
                        game_status.gaugeIncreaseRequest(player, NUMBER_RAIRA_WIND_ZEN_KAI)
                        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
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
        windZenKai.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{_, player, game_status, _->
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.YOUR_USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_RAIRA_WIND_ZEN_KAI, 1){ _, _ ->
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
                when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_WIND_CELESTIAL_SPHERE)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.distanceToDust(1, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.distanceToDust(2, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_THREE -> {
                        game_status.distanceToDust(3, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_FOUR -> {
                        game_status.distanceToDust(4, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_FIVE -> {
                        game_status.distanceToDust(5, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_SIX -> {
                        game_status.dustToDistance(1, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_SEVEN -> {
                        game_status.dustToDistance(2, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_EIGHT -> {
                        game_status.dustToDistance(3, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_NINE -> {
                        game_status.dustToDistance(4, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                    }
                    CommandEnum.SELECT_TEN -> {
                        game_status.dustToDistance(5, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
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

    private val masigText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player.opposite(), NUMBER_UTSURO_MA_SIG)){
                CommandEnum.SELECT_ONE -> {
                    game_status.auraToDust(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                    break
                }
                CommandEnum.SELECT_TWO -> {
                    game_status.flareToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                    break
                }
                else -> {
                    continue
                }
            }
        }
        null
    }

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
        game_status.lifeToDust(player, moveLife, Arrow.NULL, player,
            game_status.getCardOwner(card_number), card_number)
        game_status.flareToDust(player, moveFlare, Arrow.NULL, player,
            game_status.getCardOwner(card_number), card_number)
        game_status.auraToDust(player, moveAura, Arrow.NULL, player,
            game_status.getCardOwner(card_number), card_number)
        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
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
                        tempEditedAuraDamage = 999
                    }
                }))
            null
        }))
        blackWave.setAttack(DistanceType.CONTINUOUS, Pair(4, 7), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        blackWave.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) {_, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_UTSURO_BLACK_WAVE)
                { _, _ -> true }?: break
                if (list.size == 1){
                    val card = game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, true)?: continue
                    game_status.insertCardTo(player.opposite(), card, LocationEnum.DISCARD_YOUR, true)
                    break
                }
            }
            null
        })
        harvest.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 999, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        harvest.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) { card_number, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status, NUMBER_UTSURO_HARVEST, 2)
            null
        })
        harvest.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) { card_number, player, game_status, _ ->
            while (true){
                val cardList = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.ENCHANTMENT_ZONE), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_UTSURO_HARVEST
                ) { _, _ -> true }?: break
                if(cardList.size == 1){
                    game_status.getCardFrom(player.opposite(), cardList[0], LocationEnum.ENCHANTMENT_ZONE)?.let {
                        game_status.cardToDust(player.opposite(), 2, it, false, card_number)
                        if(it.isItDestruction()){
                            game_status.enchantmentDestruction(player.opposite(), it)
                        }
                    }?: continue
                    break
                }
                else if(cardList.size == 0){
                    break
                }
            }
            null
        })
        pressure.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) ret@{ _, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status, NUMBER_UTSURO_PRESSURE, 1)
            null
        })
        pressure.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) ret@{ _, player, game_status, _ ->
            if(hoejin(game_status)){
                game_status.setShrink(player.opposite())
            }
            null
        })
        shadowWing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_THIS_TURN_DISTANCE) { _, _, game_status, _ ->
            game_status.addThisTurnDistance(2)
            null
        })
        shadowWing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_THIS_TURN_SWELL_DISTANCE) { _, _, game_status, _ ->
            game_status.addThisTurnSwellDistance(2)
            null
        })
        shadowWall.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_CHANGE) ret@{ card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.lifePlusMinus(-1)
                }))
            null
        })
        yueHoeJu.setEnchantment(2)
        yueHoeJu.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.auraToDust(player.opposite(), 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        yueHoeJu.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(hoejin(game_status)){
                game_status.dustToAura(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
                game_status.lifeToDust(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
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
        hoeMyeol.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.lifeToDust(player.opposite(), 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number
            )
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })
        heoWi.setSpecial(3)
        heoWi.setEnchantment(3)
        heoWi.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addRangeBuff(RangeBuff(card_number,1, RangeBufTag.MINUS_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(-1, true)
                }))
            null
        })
        heoWi.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player.opposite(), RangeBuff(card_number,1, RangeBufTag.MINUS_IMMEDIATE, {_, _, _ -> true},
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
            game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?.let { card ->
                game_status.cardToDust(player, card.getNap(), card, false, card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                game_status.enchantmentDestruction(player, card)
            }
            null
        })
        jongMal.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.END_CURRENT_PHASE) {_, _, game_status, _ ->
            game_status.endCurrentPhase = true
            null
        })
        maSig.setSpecial(4)
        maSig.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, masigText)
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
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.YURINA_BEAN_BULLET, card_number, CardClass.NULL,
                    sortedSetOf(0, 1, 2, 3, 4), 1,  999,  MegamiEnum.YURINA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                ).addTextAndReturn(beanBulletText), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        notCompletePobaram.setSpecial(5)
        notCompletePobaram.addtext(termination)
        notCompletePobaram.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 3, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        notCompletePobaram.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-3)
                }))
            null
        })
    }

    private val soundOfIce = CardData(CardClass.NORMAL, CardName.SAINE_SOUND_OF_ICE, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.REACTION)
    private val accompaniment = CardData(CardClass.NORMAL, CardName.SAINE_ACCOMPANIMENT, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private val duetTanJuBingMyeong = CardData(CardClass.SPECIAL, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)


    private fun saineA1CardInit(){
        soundOfIce.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, react_attack ->
            if((react_attack != null && react_attack.isItReact)){
                game_status.auraToDust(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            else{
                game_status.auraToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
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
        duetTanJuBingMyeong.addtext(termination)
        duetTanJuBingMyeong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _->
            game_status.setShrink(player.opposite())
            null
        })
        duetTanJuBingMyeong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD) {_, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_SAINE_DUET_TAN_JU_BING_MYEONG)){
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
                    lifePlusMinus(1)
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
        satSui.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if (game_status.getPlayer(player).hand.size == 0) {
                game_status.auraToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        enTenHimika.setAttack(DistanceType.CONTINUOUS, Pair(0, 7), null, 1000, 1000,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        enTenHimika.setSpecial(5)
        enTenHimika.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {_, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = abs(gameStatus.getAdjustDistance() - 8)
                    tempEditedAuraDamage = temp
                    tempEditedLifeDamage = temp
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
    private val duetChitanYangMyeongText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG)){
                CommandEnum.SELECT_ONE -> {
                    val list = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_TOKOYO_SOUND_OF_SUN, 1
                    ) { _, _ -> true }?: break
                    game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                    }
                    break
                }
                CommandEnum.SELECT_TWO -> {
                    val list = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG, 1
                    ) { card, _ -> card.card_data.card_type == CardType.BEHAVIOR }?: break
                    game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?.let {
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
    }

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
        soundOfSun.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_USE_REACT_CARD_YOUR_END) {card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            null
        })
        soundOfSun.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_MOVE_TOKEN) {_, player, game_status, _ ->
            if(game_status.turnPlayer == player) 1
            else 0
        })
        duetChitanYangMyeong.setSpecial(1)
        duetChitanYangMyeong.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, duetChitanYangMyeongText)
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
    private val fourSeason = CardData(CardClass.SPECIAL, CardName.HONOKA_FOUR_SEASON_BACK, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val bloomPath = CardData(CardClass.SPECIAL, CardName.HONOKA_FULL_BLOOM_PATH, MegamiEnum.HONOKA, CardType.ENCHANTMENT, SubType.NONE)

    private val commandText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
        if(game_status.addPreAttackZone(player, MadeAttack(CardName.HONOKA_COMMAND, card_number, CardClass.NULL,
                sortedSetOf(1, 2, 3, 4, 5), 1,  1, MegamiEnum.HONOKA,
                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
            ), null) ){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }

    private val handFlowerText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) {_, player, game_status, _ ->
        game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 1413)
        null
    }

    private val newOpeningText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
            true
        }, {_, gameStatus, madeAttack ->
            madeAttack.run {
                val temp = countTokenFive(gameStatus)
                tempEditedAuraDamage = temp
                tempEditedLifeDamage = temp
            }
        }))
        if(game_status.addPreAttackZone(player, MadeAttack(CardName.HONOKA_A_NEW_OPENING, card_number, CardClass.NORMAL,
                sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 1000,  1000, MegamiEnum.HONOKA,
                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
            ), null)){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }

    private fun dupligearCheck(cardName: CardName) = cardName == CardName.KURURU_DUPLICATED_GEAR_3
            || cardName == CardName.KURURU_DUPLICATED_GEAR_2
            || cardName == CardName.KURURU_DUPLICATED_GEAR_1

    private fun checkCardName(card_number: Int, cardName: CardName) = card_number.toCardName() == cardName

    private suspend fun requestCardChange(player: PlayerEnum, card_number: Int, game_status: GameStatus): Boolean{
        if(game_status.getCardOwner(card_number) == player) return false
        while (true){
            return when(game_status.receiveCardEffectSelect(player, card_number)){
                CommandEnum.SELECT_ONE -> {
                    true
                }
                CommandEnum.SELECT_NOT -> {
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
            return when(game_status.receiveCardEffectSelect(player, NUMBER_HONOKA_DIVINE_OUKA)){
                CommandEnum.SELECT_ONE -> {
                    true
                }
                CommandEnum.SELECT_NOT -> {
                    false
                }
                else -> {
                    continue
                }
            }
        }
    }

    private fun countTokenFive(game_status: GameStatus): Int{
        var count = 0
        val player1 = game_status.getPlayer(PlayerEnum.PLAYER1)
        val player2 = game_status.getPlayer(PlayerEnum.PLAYER2)

        if(player1.aura == 5) count += 1
        if(player1.flare == 5) count += 1
        if(player1.life == 5) count += 1
        if(game_status.countToken(PlayerEnum.PLAYER1, LocationEnum.YOUR_USED_CARD) == 5) count += 1
        if(game_status.countToken(PlayerEnum.PLAYER1, LocationEnum.ENCHANTMENT_ZONE) == 5) count += 1
        if(game_status.dust == 5) count += 1
        if(game_status.distanceToken == 5) count += 1
        if(player2.aura == 5) count += 1
        if(player2.flare == 5) count += 1
        if(player2.life == 5) count += 1
        if(game_status.countToken(PlayerEnum.PLAYER2, LocationEnum.YOUR_USED_CARD) == 5) count += 1
        if(game_status.countToken(PlayerEnum.PLAYER2, LocationEnum.ENCHANTMENT_ZONE) == 5) count += 1
        return count
    }

    fun honokaCardInit(){
        spiritSik.setAttack(DistanceType.CONTINUOUS, Pair(2, 8), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        spiritSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_SPIRIT_SIK)
                && requestCardChange(player, NUMBER_HONOKA_SPIRIT_SIK, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.DISCARD_YOUR)
                    }
                }
            }
            null
        })
        guardianSik.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        guardianSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        guardianSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_GUARDIAN_SPIRIT_SIK)
                && requestCardChange(player, NUMBER_HONOKA_GUARDIAN_SPIRIT_SIK, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.DISCARD_YOUR)
                    }
                }
            }
            null
        })
        assaultSik.setAttack(DistanceType.CONTINUOUS, Pair(5, 5), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        assaultSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToLife(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        assaultSik.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_ASSAULT_SPIRIT_SIK)
                && requestCardChange(player, NUMBER_HONOKA_ASSAULT_SPIRIT_SIK, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_DIVINE_OUKA, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_DIVINE_OUKA, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_DIVINE_OUKA, LocationEnum.DISCARD_YOUR)
                    }
                }
            }
            null
        })
        divineOuka.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 4, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        divineOuka.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToAura(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        sakuraBlizzard.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        sakuraBlizzard.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player.opposite(), NUMBER_HONOKA_SAKURA_BLIZZARD)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.distanceToAura(player, 1, Arrow.ONE_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.auraToDistance(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
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
        yuGiGongJin.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DRAW_CARD) {_, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_HONOKA_UI_GI_GONG_JIN)){
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
        yuGiGongJin.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            while (true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HONOKA_UI_GI_GONG_JIN
                ) { _, _ -> true }?: break
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
            if(card_number.toCardName() == CardName.HONOKA_UI_GI_GONG_JIN || dupligearCheck(card_number.toCardName())){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_HONOKA_REGENERATION)){
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
            }
            null
        })
        sakuraWing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            while (true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_HONOKA_SAKURA_WING)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.dustToDistance(2, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.distanceToDust(2, Arrow.BOTH_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
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
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_SAKURA_WING)){
                game_status.getCardFrom(player, CardName.HONOKA_REGENERATION, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    game_status.moveAdditionalCard(player, CardName.HONOKA_REGENERATION, LocationEnum.DISCARD_YOUR)
                }
            }
            null
        })
        regeneration.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.dustToFlare(player, 1, Arrow.ONE_DIRECTION,
                player, game_status.getCardOwner(card_number), card_number
            )
            null
        })
        regeneration.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_REGENERATION)){
                game_status.getCardFrom(player, CardName.HONOKA_SAKURA_WING, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_WING, LocationEnum.DISCARD_YOUR)
                }
            }
            null
        })
        sakuraAmulet.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, react_attack ->
            while (true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HONOKA_SAKURA_AMULET
                ) { card, _ -> card.card_data.canCover }?: break
                if (list.size == 0){
                    break
                }
                else if (list.size == 1){
                    game_status.popCardFrom(player, list[0], LocationEnum.HAND, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                    }
                    if(react_attack?.card_class != CardClass.SPECIAL){
                        react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
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
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_SAKURA_AMULET)
                && requestCardChange(player, NUMBER_HONOKA_SAKURA_AMULET, game_status)){
                game_status.getCardFrom(player, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.DISCARD_YOUR)
                    }
                }
            }
            null
        })
        honokaSparkle.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        command.setEnchantment(3)
        command.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, commandText)
            null
        })
        tailWind.setEnchantment(3)
        tailWind.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(1, false)
                }))
            null
        })
        chestWilling.setSpecial(5)
        chestWilling.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_CHEST_WILLINGNESS)){
                game_status.getCardFrom(player, CardName.HONOKA_HAND_FLOWER, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    it.special_card_state = SpecialCardEnum.PLAYED
                    game_status.moveAdditionalCard(player, CardName.HONOKA_HAND_FLOWER, LocationEnum.YOUR_USED_CARD)
                    game_status.returnSpecialCard(player, it.card_number)
                }
            }
            null
        })
        handFlower.setSpecial(0)
        handFlower.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 1413)
            null
        })
        handFlower.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) {card_number, player, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, handFlowerText)
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
                when(game_status.receiveCardEffectSelect(player, NUMBER_HONOKA_HAND_FLOWER)){
                    CommandEnum.SELECT_ONE -> {
                        if (game_status.getPlayerAura(player) == 0){
                            continue
                        }
                        game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                            game_status.auraToCard(player, 1, it, card_number, LocationEnum.YOUR_USED_CARD)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                            if(it.getNap() == 5){
                                if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_HAND_FLOWER)){
                                    game_status.getCardFrom(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.ADDITIONAL_CARD)?.let { additionalCard ->
                                        game_status.cardToFlare(player, it.getNap(), it, card_number, LocationEnum.YOUR_USED_CARD)
                                        game_status.popCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD, true)
                                        game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                                        additionalCard.special_card_state = SpecialCardEnum.PLAYED
                                        game_status.moveAdditionalCard(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.YOUR_USED_CARD)
                                        game_status.returnSpecialCard(player, additionalCard.card_number)
                                        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
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
                        game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                            game_status.dustToCard(player, 1, it, card_number, LocationEnum.YOUR_USED_CARD)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                            if(it.getNap() == 5){
                                if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_HAND_FLOWER)){
                                    game_status.getCardFrom(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.ADDITIONAL_CARD)?.let { additionalCard ->
                                        game_status.cardToFlare(player, it.getNap() , it, card_number, LocationEnum.YOUR_USED_CARD)
                                        game_status.popCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD, true)
                                        game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                                        additionalCard.special_card_state = SpecialCardEnum.PLAYED
                                        game_status.moveAdditionalCard(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.YOUR_USED_CARD)
                                        game_status.returnSpecialCard(player, additionalCard.card_number)
                                        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
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
        newOpening.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, newOpeningText)
            null
        })
        underFlag.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        underFlag.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_TEXT_TO_ATTACK) { _, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HONOKA_UNDER_THE_NAME_OF_FLAG
                ) { _, _ -> true }?: break
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
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HONOKA_FOUR_SEASON_BACK
                ) { _, _ -> true }?: break
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
        fourSeason.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_HONOKA_FOUR_SEASON_BACK)){
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
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HONOKA_FULL_BLOOM_PATH, 1
                ) { card, _ -> card.card_data.canCover }?: break
                game_status.popCardFrom(player, list[0], LocationEnum.HAND, false)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                }
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 1416)
                break
            }
            null
        })
        fourSeason.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addAdditionalListener(player, Listener(player, card_number)ret@{gameStatus, cardNumber, _, _, _, _ ->
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_HONOKA_FULL_BLOOM_PATH)){
                        CommandEnum.SELECT_ONE -> {
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            return@ret false
                        }
                        else -> {
                            continue
                        }
                    }
                }

                gameStatus.popCardFrom(player, cardNumber, LocationEnum.YOUR_USED_CARD, true)?.let {
                    it.special_card_state = SpecialCardEnum.UNUSED
                    gameStatus.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                    println("{${it.card_data.card_name}}")
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

    private val shuriken = CardData(CardClass.NORMAL, CardName.OBORO_SHURIKEN, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val ambush  = CardData(CardClass.NORMAL, CardName.OBORO_AMBUSH, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULL_POWER)
    private val branchOfDivine = CardData(CardClass.SPECIAL, CardName.OBORO_BRANCH_OF_DIVINE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val lastCrystal = CardData(CardClass.SPECIAL, CardName.OBORO_LAST_CRYSTAL, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.NONE)

    private val shurikenText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD) { card_number, player, game_status, _ ->
        if(game_status.getPlayer(PlayerEnum.PLAYER1).cover_card.size + game_status.getPlayer(PlayerEnum.PLAYER2).cover_card.size >= 5){
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_OBORO_SHURIKEN)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.popCardFrom(player, card_number, LocationEnum.DISCARD_YOUR, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.HAND, true)
                    }
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_NOT){
                    break
                }
            }
        }
        null
    }

    private fun oboroA1CardInit(){
        shuriken.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shuriken.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD) { card_number, player, game_status, _ ->
            if(game_status.getCardFrom(player, card_number, LocationEnum.DISCARD_YOUR)?.isSoftAttack == false){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.DISCARD_YOUR, shurikenText)
            }
            null
        })
        ambush.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 4, 3,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        ambush.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {attack_player, attack_game_status, attack ->
                val temp = attack_game_status.getPlayer(attack_player.opposite()).cover_card.size
                attack.apply {
                    auraPlusMinus(-temp); lifePlusMinus(-temp)
                }
            }))
            null
        })
        branchOfDivine.setSpecial(0)
        branchOfDivine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.outToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.outToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        branchOfDivine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })
        branchOfDivine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player)
            game_status.getCardFrom(player, CardName.OBORO_LAST_CRYSTAL, LocationEnum.ADDITIONAL_CARD)?.let {
                game_status.moveAdditionalCard(player, CardName.OBORO_LAST_CRYSTAL, LocationEnum.SPECIAL_CARD)
            }
            null
        })
        lastCrystal.setSpecial(3)
        lastCrystal.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, player, game_status, _->
            if(game_status.getPlayer(player).loseCounter) 1
            else 0
        })
        lastCrystal.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_LOSE_GAME)ret@{card_number, player, game_status, _->
            val nowPlayer = game_status.getPlayer(player)
            if(!(nowPlayer.loseCounter)){
                nowPlayer.loseCounter = true
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_OBORO_LAST_CRYSTAL)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.lifeToDust(player, nowPlayer.life, Arrow.NULL, player,
                                game_status.getCardOwner(card_number), card_number, true)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                            val useSuccess = game_status.getCardFrom(player, card_number, LocationEnum.SPECIAL_CARD)?.let {
                                game_status.useCardFrom(player, it, LocationEnum.SPECIAL_CARD, false, null,
                                    isCost = true, isConsume = true
                                )
                            }?: game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                                game_status.useCardFrom(player, it, LocationEnum.YOUR_USED_CARD, false, null,
                                    isCost = true, isConsume = true
                                )
                            }?: false
                            if(useSuccess) return@ret 1
                            else return@ret 0
                        }
                        CommandEnum.SELECT_NOT -> {
                            return@ret 0
                        }
                        else -> {
                            continue
                        }
                    }
                }
                @Suppress("UNREACHABLE_CODE")
                0
            }
            else{
                0
            }
        })
        lastCrystal.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            game_status.coverCard(player, player, CardName.OBORO_LAST_CRYSTAL.toCardNumber(true))
            null
        })
        lastCrystal.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.dustToLife(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
    }

    private val trickUmbrella = CardData(CardClass.NORMAL, CardName.CHIKAGE_TRICK_UMBRELLA, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.NONE)
    private val struggle = CardData(CardClass.NORMAL, CardName.CHIKAGE_STRUGGLE, MegamiEnum.CHIKAGE, CardType.BEHAVIOR, SubType.NONE)
    private val zanZeNoConnectionPoison = CardData(CardClass.SPECIAL, CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.NONE)

    private fun chikageA1CardInit(){
        trickUmbrella.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = true)
        trickUmbrella.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE,
                {condition_player, condition_game_status, _ ->
                    condition_game_status.getPlayer(condition_player.opposite()).hand.size >= 2
                },
                { _, _, attack ->
                    attack.apply {
                        plusMinusRange(2, true); plusMinusRange(2, false)
                    }
                }))
            null
        })
        struggle.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).hand.size >= 2){
                game_status.addConcentration(player)
            }
            null
        })
        struggle.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_CHIKAGE_STRUGGLE)
                if(selectDustToDistance(nowCommand, game_status, player,
                        game_status.getCardOwner(card_number), card_number)) break
            }
            null
        })
        zanZeNoConnectionPoison.setAttack(DistanceType.CONTINUOUS, Pair(0, 1), null, 4, 1000,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        zanZeNoConnectionPoison.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = gameStatus.getPlayer(nowPlayer).hand.size * 2
                    tempEditedLifeDamage = temp
                }
            }))
            null
        })
    }

    private val biteDust = CardData(CardClass.NORMAL, CardName.UTSURO_BITE_DUST, MegamiEnum.UTSURO, CardType.ATTACK, SubType.NONE)
    private val deviceKururusik = CardData(CardClass.SPECIAL, CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK, MegamiEnum.UTSURO, CardType.ENCHANTMENT, SubType.NONE)
    private val mangA = CardData(CardClass.SPECIAL, CardName.UTSURO_MANG_A, MegamiEnum.UTSURO, CardType.BEHAVIOR, SubType.NONE)
    private val annihilationShadow = CardData(CardClass.NORMAL, CardName.UTSURO_ANNIHILATION_SHADOW, MegamiEnum.UTSURO, CardType.ATTACK, SubType.FULL_POWER)
    private val silentWalk = CardData(CardClass.NORMAL, CardName.UTSURO_SILENT_WALK, MegamiEnum.UTSURO, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val deMise = CardData(CardClass.NORMAL, CardName.UTSURO_DE_MISE, MegamiEnum.UTSURO, CardType.ENCHANTMENT, SubType.NONE)


    private suspend fun reviveDemise(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)

        for(card in nowPlayer.normalCardDeck.filter {
                card -> !(card.card_data.isItSpecial())
        }){
            game_status.popCardFrom(player, card.card_number, LocationEnum.DECK, false)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, false)
            }
        }

        for(card in nowPlayer.hand.values.filter {
                card -> !(card.card_data.isItSpecial())
        }){
            game_status.popCardFrom(player, card.card_number, LocationEnum.HAND, false)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, false)
            }
        }

        for(card in nowPlayer.cover_card.filter {
                card -> !(card.card_data.isItSpecial())
        }){
            game_status.popCardFrom(player, card.card_number, LocationEnum.COVER_CARD, false)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, false)
            }
        }

        for(card in nowPlayer.discard.filter {
                card -> !(card.card_data.isItSpecial())
        }){
            game_status.popCardFrom(player, card.card_number, LocationEnum.DISCARD_YOUR, true)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, true)
            }
        }

        for(card in nowPlayer.enchantmentCard.values.filter {
            card -> !(card.card_data.isItSpecial())
        }){
            if((card.getNap() ?: 0) >= 1){
                game_status.cardToDust(player, card.getNap(), card, false, Log.DEMISE)
            }
            game_status.popCardFrom(player, card.card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                card.effectText(player, game_status, null, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, true)
            }
        }

        game_status.logger.insert(Log(player, LogText.END_EFFECT, Log.DEMISE, -1))
        game_status.moveAdditionalCard(player, CardName.UTSURO_ANNIHILATION_SHADOW, LocationEnum.DISCARD_YOUR)
        game_status.moveAdditionalCard(player, CardName.UTSURO_SILENT_WALK, LocationEnum.DISCARD_YOUR)
        game_status.moveAdditionalCard(player, CardName.UTSURO_DE_MISE, LocationEnum.DISCARD_YOUR)

        game_status.deckReconstruct(player, false)
    }

    private fun utsuroA1CardInit(){
        biteDust.setAttack(DistanceType.CONTINUOUS, Pair(3, 6), null, 2, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = false)
        biteDust.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) {card_number, player, game_status, _ ->
            game_status.flareToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        deviceKururusik.setSpecial(2)
        deviceKururusik.setEnchantment(2)
        deviceKururusik.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_MOVE_TOKEN) {_, player, game_status, _ ->
            if(game_status.dust >= 13 && game_status.getPlayerLife(player) <= 6) 1
            else 0
        })
        deviceKururusik.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.GET_ADDITIONAL_CARD) {_, player, game_status, _ ->
            if(game_status.nowPhase == START_PHASE || game_status.nowPhase == START_PHASE_REDUCE_NAP) {
                reviveDemise(player, game_status)
                game_status.moveAdditionalCard(player, CardName.UTSURO_MANG_A, LocationEnum.YOUR_USED_CARD)?.let {
                    it.special_card_state = SpecialCardEnum.PLAYED
                }
                game_status.drawCard(player, 1)
            }
            null
        })
        mangA.setSpecial(6)
        mangA.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.DO_NOT_GET_DAMAGE) {_, _, _, _ ->
            1
        })
        mangA.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addMainPhaseListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                        _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
        annihilationShadow.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 999, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        annihilationShadow.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) { _, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status,NUMBER_UTSURO_ANNIHILATION_SHADOW, 6)
            null
        })
        silentWalk.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            for(i in 1..5){
                val command = game_status.requestAndDoBasicOperation(player, 1315, hashSetOf(CommandEnum.ACTION_GO_FORWARD))
                if(command == CommandEnum.SELECT_NOT){
                    break
                }
            }
            null
        })
        silentWalk.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.UTSURO_SILENT_WALK, card_number, CardClass.NULL,
                    sortedSetOf(4, 5, 6, 7, 8, 9, 10), 3,  2, MegamiEnum.UTSURO,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        silentWalk.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number,  player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.UTSURO_SILENT_WALK, card_number, CardClass.NULL,
                    sortedSetOf(5, 6, 7, 8, 9, 10), 1,  1, MegamiEnum.UTSURO,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        silentWalk.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.UTSURO_SILENT_WALK, card_number, CardClass.NULL,
                    sortedSetOf(6, 7, 8, 9, 10), 1,  1, MegamiEnum.UTSURO,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)) {
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        deMise.setEnchantment(2)
        deMise.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            val otherPlayer = game_status.getPlayer(player.opposite())
            for (card in otherPlayer.hand.keys){
                game_status.popCardFrom(player.opposite(), card, LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }
            for (card in otherPlayer.normalCardDeck){
                game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }
            null
        })
        deMise.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.setConcentration(player.opposite(), 0)
            null
        })
        deMise.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })
    }

    private val snowBlade = CardData(CardClass.NORMAL, CardName.KORUNU_SNOW_BLADE, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val revolvingBlade = CardData(CardClass.NORMAL, CardName.KORUNU_REVOLVING_BLADE, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val bladeDance = CardData(CardClass.NORMAL, CardName.KORUNU_BLADE_DANCE, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val snowRide = CardData(CardClass.NORMAL, CardName.KORUNU_RIDE_SNOW, MegamiEnum.KORUNU, CardType.BEHAVIOR, SubType.NONE)
    private val absoluteZero = CardData(CardClass.NORMAL, CardName.KORUNU_ABSOLUTE_ZERO, MegamiEnum.KORUNU, CardType.BEHAVIOR, SubType.NONE)
    private val frostbite = CardData(CardClass.NORMAL, CardName.KORUNU_FROSTBITE, MegamiEnum.KORUNU, CardType.ENCHANTMENT, SubType.NONE)
    private val thornBush = CardData(CardClass.NORMAL, CardName.KORUNU_FROST_THORN_BUSH, MegamiEnum.KORUNU, CardType.ENCHANTMENT, SubType.NONE)
    private val conluRuyanpeh = CardData(CardClass.SPECIAL, CardName.KORUNU_CONLU_RUYANPEH, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val letarLera = CardData(CardClass.SPECIAL, CardName.KORUNU_LETAR_LERA, MegamiEnum.KORUNU, CardType.BEHAVIOR, SubType.REACTION)
    private val upastum = CardData(CardClass.SPECIAL, CardName.KORUNU_UPASTUM, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val porucharto = CardData(CardClass.SPECIAL, CardName.KORUNU_PORUCHARTO, MegamiEnum.KORUNU, CardType.ENCHANTMENT, SubType.NONE)

    private fun korunuCardInit(){
        snowBlade.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        snowBlade.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.FREEZE) {_, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })
        revolvingBlade.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        revolvingBlade.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_REACTED_AFTER) { card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_REVOLVING_BLADE)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.dustToDistance(2, Arrow.BOTH_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.distanceToDust(2, Arrow.BOTH_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    break
                }
            }
            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            null
        })
        bladeDance.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        bladeDance.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player.opposite()).checkAuraFull()
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        snowRide.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_BLADE_DANCE)
                if(selectDustToDistance(nowCommand, game_status, player,
                        game_status.getCardOwner(card_number), card_number)){
                    break
                }
            }
            if(game_status.getPlayer(player.opposite()).checkAuraFull()){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_RIDE_SNOW)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                                game_status.getCardOwner(card_number), card_number)
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        absoluteZero.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.KORUNU_ABSOLUTE_ZERO, card_number, CardClass.NULL,
                        sortedSetOf(2, 3, 4, 5), 1,  2, MegamiEnum.KORUNU,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    ), null) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }
                val freezePlayer = game_status.getPlayer(player.opposite())
                game_status.outToAuraFreeze(player.opposite(), freezePlayer.maxAura - freezePlayer.aura - freezePlayer.freezeToken)
            }
            null
        })
        absoluteZero.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 1504)
            if(game_status.getPlayer(player.opposite()).freezeToken >= 3){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                        CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 1504)
            }
            null
        })
        frostbite.setEnchantment(2)
        frostbite.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.FREEZE) {_, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })
        frostbite.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_INCUBATE_OTHER){_, _, _, _ ->
            1
        })
        thornBush.setEnchantment(2)
        thornBush.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            if(!(game_status.logger.checkThisTurnDoAttackNotSpecial(player))){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {
                        _, _, attack -> attack.card_class != CardClass.SPECIAL },
                    { _, _, madeAttack ->
                        madeAttack.apply {
                            auraPlusMinus(1); lifePlusMinus(1)
                        }
                    }))
            }
            null
        })
        thornBush.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_NAP) {_, player, game_status, _ ->
            var result = 1
            if(game_status.turnPlayer == player && game_status.getPlayer(player.opposite()).freezeToken >= 1) {
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_FROST_THORN_BUSH)){
                        CommandEnum.SELECT_ONE -> {
                            result = 0
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            result = 1
                            break
                        }
                        else -> {
                            continue
                        }
                    }
                }
            }
            result
        })
        conluRuyanpeh.setSpecial(4)
        conluRuyanpeh.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        conluRuyanpeh.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { _, player, game_status, _ ->
            val freezePlayer = game_status.getPlayer(player.opposite())
            game_status.outToAuraFreeze(player.opposite(), freezePlayer.maxAura - freezePlayer.aura - freezePlayer.freezeToken)
            null
        })
        letarLera.setSpecial(2)
        letarLera.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, react_attack ->
            if(game_status.getPlayer(player.opposite()).checkAuraFull()){
                react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            else{
                game_status.distanceToAura(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        upastum.setSpecial(0)
        upastum.setAttack(DistanceType.CONTINUOUS, Pair(3, 6), null, 0, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        upastum.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.FREEZE) {_, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })
        upastum.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addAuraListener(player, Listener(player, card_number) {_, cardNumber, _,
                                                                                        _, before_full, after_full ->
                if(!before_full && after_full){
                    game_status.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })
        porucharto.setSpecial(2)
        porucharto.setEnchantment(1)
        porucharto.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_REACTABLE) {_, _, _, _ ->
            1
        })
        porucharto.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) {card_number, player, game_status, react_attack->
            if((react_attack != null && react_attack.isItReact)){
                game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card ->
                    (card.card_data.card_name == CardName.KORUNU_PORUCHARTO)}, {cost ->
                    cost + 2
                }))
            }
            null
        })
        porucharto.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) { card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_UPASTUM)
                if(selectDustToDistance(nowCommand, game_status, player,
                        game_status.getCardOwner(card_number), card_number)) break
            }
            null
        })
        porucharto.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.FREEZE) { _, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })
        porucharto.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.USE_CARD) {card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_PORUCHARTO)){
                    CommandEnum.SELECT_ONE -> {
                        val card = game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?: break
                        game_status.useCardFrom(player, card, LocationEnum.ENCHANTMENT_ZONE, false, null,
                            isCost = true, isConsume = true, 4)
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {

                    }
                }

            }
            null
        })
    }

    private val starNail = CardData(CardClass.NORMAL, CardName.YATSUHA_STAR_NAIL, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.NONE)
    private val darknessGill = CardData(CardClass.NORMAL, CardName.YATSUHA_DARKNESS_GILL, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.NONE)
    private val mirrorDevil = CardData(CardClass.NORMAL, CardName.YATSUHA_MIRROR_DEVIL, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.FULL_POWER)
    private val ghostStep = CardData(CardClass.NORMAL, CardName.YATSUHA_GHOST_STEP, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val willing = CardData(CardClass.NORMAL, CardName.YATSUHA_WILLING, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val contract = CardData(CardClass.NORMAL, CardName.YATSUHA_CONTRACT, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val clingyFlower = CardData(CardClass.NORMAL, CardName.YATSUHA_CLINGY_FLOWER, MegamiEnum.YATSUHA, CardType.ENCHANTMENT, SubType.NONE)
    private val clingyFlowerText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_SHRINK) {card_number, player, game_status, _ ->
        game_status.auraToAura(player, player.opposite(), 2, Arrow.ONE_DIRECTION, player,
            game_status.getCardOwner(card_number), card_number)
        null
    }
    private val twoLeapMirrorDivine = CardData(CardClass.SPECIAL, CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.REACTION)
    private val fourLeapSong = CardData(CardClass.SPECIAL, CardName.YATSUHA_FOUR_LEAP_SONG, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val sixStarSea = CardData(CardClass.SPECIAL, CardName.YATSUHA_SIX_STAR_SEA, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.NONE)
    private val eightMirrorOtherSide = CardData(CardClass.SPECIAL, CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE, MegamiEnum.YATSUHA, CardType.ENCHANTMENT, SubType.NONE)

    private val contractText = Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
        if(game_status.getPlayerFlare(player) >= game_status.getPlayerFlare(player.opposite())){
            game_status.auraToFlare(player, player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
        }
        null
    }

    private fun yatsuhaCardInit(){
        starNail.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 2,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        starNail.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.auraToFlare(player, player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        darknessGill.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 3, 1,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        darknessGill.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, gameStatus, attack ->
                attack.lifePlusMinus(gameStatus.getMirror())
            }))
            null
        })
        darknessGill.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            game_status.setShrink(player)
            null
        })
        mirrorDevil.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 5, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        mirrorDevil.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.lifeToDust(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        ghostStep.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        ghostStep.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            while (true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_GHOST_STEP)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.addThisTurnDistance(1)
                        game_status.addThisTurnSwellDistance(1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.addThisTurnDistance(-1)
                        game_status.addThisTurnSwellDistance(-1)
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        willing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _->
            while (true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_WILLING)){
                    CommandEnum.SELECT_ONE -> {
                        while (true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_CONTRACT)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.auraToFlare(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToAura(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        while (true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_CLINGY_FLOWER)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.auraToFlare(player.opposite(), player.opposite(), 1,
                                        Arrow.BOTH_DIRECTION, player, game_status.getCardOwner(card_number),
                                        card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToAura(player.opposite(), player.opposite(), 1,
                                        Arrow.BOTH_DIRECTION, player, game_status.getCardOwner(card_number),
                                        card_number)
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        break
                    }
                    CommandEnum.SELECT_THREE -> {
                        while (true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_CONTRACT)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.auraToFlare(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToAura(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        while (true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_CLINGY_FLOWER)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.auraToFlare(player.opposite(), player.opposite(), 1,
                                        Arrow.BOTH_DIRECTION, player, game_status.getCardOwner(card_number),
                                        card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToAura(player.opposite(), player.opposite(), 1,
                                        Arrow.BOTH_DIRECTION, player, game_status.getCardOwner(card_number),
                                        card_number)
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        contract.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.flareToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        contract.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.ADD_END_PHASE_EFFECT) {card_number, player, game_status, _ ->
            if(game_status.nowPhase == END_PHASE){
                if(player == PlayerEnum.PLAYER1){
                    game_status.nextEndPhaseEffect[card_number] = Pair(CardEffectLocation.TEMP_PLAYER1, contractText)
                }
                else{
                    game_status.nextEndPhaseEffect[card_number] = Pair(CardEffectLocation.TEMP_PLAYER2, contractText)
                }
            }
            else{
                if(player == PlayerEnum.PLAYER1){
                    game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.TEMP_PLAYER1, contractText)
                }
                else{
                    game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.TEMP_PLAYER2, contractText)
                }
            }
            null
        })
        clingyFlower.setEnchantment(3)
        clingyFlower.addtext(chasm)
        clingyFlower.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.THIS_CARD_NAP_CHANGE) {_, player, game_status, _ ->
            game_status.getPlayer(player).napBuff -= game_status.getMirror()
            null
        })
        clingyFlower.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.YATSUHA_CLINGY_FLOWER, card_number, CardClass.NULL,
                    sortedSetOf(1, 2, 3, 4), 0,  0, MegamiEnum.YATSUHA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ).addTextAndReturn(clingyFlowerText), null) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        twoLeapMirrorDivine.setSpecial(4)
        twoLeapMirrorDivine.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, react_attack ->
            react_attack?.let {attack ->
                val damage = attack.getDamage(game_status, player.opposite(), game_status.getPlayerAttackBuff(player.opposite()))
                attack.rangeCheck(-1, game_status, player.opposite(), game_status.getPlayerRangeBuff(player.opposite()))
                attack.activeOtherBuff(game_status, player, game_status.getPlayerOtherBuff(player.opposite()))
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE, card_number, attack.card_class,
                        attack.editedDistance, damage.first, damage.second, attack.megami,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false,
                        chogek = attack.editedChogek).also {
                            attack.copyAfterAttackTo(it)
                    }, attack)){
                    game_status.afterMakeAttack(card_number, player, attack)
                }
            }
            null
        })
        twoLeapMirrorDivine.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, reactedAttack ->
            if(reactedAttack?.card_class != CardClass.SPECIAL){
                reactedAttack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })
        fourLeapSong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{card_number, player, game_status, _->
            while (true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_YATSUHA_FOUR_LEAP_SONG)
                {card, _ -> card.card_data.card_class != CardClass.SPECIAL}?: break
                if (list.size == 1){
                    game_status.getCardFrom(player, list[0], LocationEnum.ENCHANTMENT_ZONE)?.also {
                        game_status.cardToDust(player, it.getNap() , it, false, card_number)
                        if(it.isItDestruction()){
                            game_status.enchantmentDestruction(player, it)
                        }
                    }?: game_status.getCardFrom(player.opposite(), list[0], LocationEnum.ENCHANTMENT_ZONE)?.also {
                        game_status.cardToDust(player.opposite(), it.getNap() , it, false, card_number)
                        if(it.isItDestruction()){
                            game_status.enchantmentDestruction(player.opposite(), it)
                        }
                    }

                    if(game_status.endCurrentPhase){
                        break
                    }

                    val (location, card) = game_status.getCardFrom(player, list[0], LocationEnum.DISCARD_YOUR)?.let {
                        Pair(LocationEnum.DISCARD_YOUR, it)
                    }?: game_status.getCardFrom(player.opposite(), list[0], LocationEnum.DISCARD_YOUR)?.let {
                        Pair(LocationEnum.DISCARD_OTHER, it)
                    }?: break

                    while (true){
                        when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_FOUR_LEAP_SONG)){
                            CommandEnum.SELECT_ONE -> {
                                if(game_status.useCardFrom(player, card, location, false, null,
                                        isCost = true, isConsume = true)){
                                    if(card.card_data.sub_type == SubType.FULL_POWER){
                                        game_status.getPlayer(player).afterCardUseTermination = true
                                    }
                                }
                                break
                            }
                            CommandEnum.SELECT_NOT -> {
                                break
                            }
                            else -> {

                            }
                        }
                    }
                    break
                }
            }
            null
        })
        sixStarSea.setSpecial(5)
        sixStarSea.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 3, 1,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = true)
        sixStarSea.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, gameStatus, attack ->
                attack.apply {
                    lifePlusMinus(gameStatus.getMirror()); auraPlusMinus(gameStatus.getMirror())
                }
            }))
            null
        })
        eightMirrorOtherSide.setSpecial(2)
        eightMirrorOtherSide.setEnchantment(5)
        eightMirrorOtherSide.addtext(termination)
        eightMirrorOtherSide.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_ARROW_BOTH){_, _, _, _ ->
            1
        })
        eightMirrorOtherSide.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
            }
            null
        })
    }

    private val zhenYen = CardData(CardClass.NORMAL, CardName.SHINRA_ZHEN_YEN, MegamiEnum.SHINRA, CardType.BEHAVIOR, SubType.REACTION)
    private val sado = CardData(CardClass.NORMAL, CardName.SHINRA_SA_DO, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val zenChiKyoTen = CardData(CardClass.SPECIAL, CardName.SHINRA_ZEN_CHI_KYO_TEN, MegamiEnum.SHINRA, CardType.ATTACK, SubType.FULL_POWER)

    fun shinraA1CardInit(){
        zhenYen.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) {card_number, player, game_status, react_attack->
            if((react_attack != null && react_attack.isItReact)|| game_status.getPlayer(player).justRunNoCondition){
                when(game_status.getStratagem(player)){
                    Stratagem.SHIN_SAN -> {
                        if(game_status.getPlayer(player.opposite()).normalCardDeck.size >= 3){
                            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1),
                                false, null, null, card_number)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                        }
                        if(!game_status.getPlayer(player).justRunNoCondition){
                            setStratagemByUser(game_status, player)
                        }
                    }
                    Stratagem.GUE_MO -> {
                        if(game_status.getPlayer(player.opposite()).normalCardDeck.size <= 3){
                            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(2, 999),
                                false, null, null, card_number)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                        }
                        if(!game_status.getPlayer(player).justRunNoCondition){
                            setStratagemByUser(game_status, player)
                        }
                    }
                    null -> {}
                }
            }
            null
        })
        sado.setEnchantment(2)
        sado.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.RUN_STRATAGEM) ret@{card_number, player, game_status, react_attack->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.SHINRA_SA_DO, card_number, CardClass.NULL,
                            sortedSetOf(1, 3, 5), 2,  2,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        ), null) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    game_status.getPlayer(player).stratagem = null
                    if(game_status.endCurrentPhase){
                        return@ret null
                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                Stratagem.GUE_MO -> {
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.SHINRA_SA_DO, card_number, CardClass.NULL,
                            sortedSetOf(2, 4, 6), 2, 2,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        ), null) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    game_status.getPlayer(player).stratagem = null
                    if(game_status.endCurrentPhase){
                        return@ret null
                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                null -> {}
            }
            null
        })
        sado.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RUN_STRATAGEM) ret@{card_number, player, game_status, react_attack->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.SHINRA_SA_DO, card_number, CardClass.NULL,
                            sortedSetOf(1, 3, 5), 2,  2,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        ), null) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    game_status.getPlayer(player).stratagem = null
                    if(game_status.endCurrentPhase){
                        return@ret null
                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                Stratagem.GUE_MO -> {
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.SHINRA_SA_DO, card_number, CardClass.NULL,
                            sortedSetOf(2, 4, 6), 2, 2,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        ), null) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    game_status.getPlayer(player).stratagem = null
                    if(game_status.endCurrentPhase){
                        return@ret null
                    }
                    if(!game_status.getPlayer(player).justRunNoCondition){
                        setStratagemByUser(game_status, player)
                    }
                }
                null -> {}
            }
            null
        })
        zenChiKyoTen.setSpecial(4)
        zenChiKyoTen.setAttack(DistanceType.CONTINUOUS, Pair(0, 5), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        zenChiKyoTen.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) ret@{_, player, game_status, _ ->
            val set = mutableSetOf<Int>()
            val list = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.COVER_CARD, LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_SHINRA_SA_DO
            ) { _, _ -> true }?: return@ret null
            set.addAll(list)
            if(list.isNotEmpty()){
                for (cardNumber in list){
                    game_status.popCardFrom(player, cardNumber, LocationEnum.COVER_CARD, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                    }?: game_status.popCardFrom(player, cardNumber, LocationEnum.HAND, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                    }
                }
            }
            null
        })
        zenChiKyoTen.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RUN_STRATAGEM) ret@{_, player, game_status, _ ->
            val usedSet = mutableSetOf<Int>()
            game_status.getPlayer(player).justRunNoCondition = true
            while(true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_SHINRA_ZEN_CHI_KYO_TEN
                ) { card, _ -> card.card_number !in usedSet && card.thisCardHaveStratagem() }?: break
                if(list.size == 1){
                    game_status.getCardFrom(player, list[0], LocationEnum.DISCARD_YOUR)?.runStratagem(player, game_status)
                    usedSet.add(list[0])
                }
                else if(list.size == 0){
                    break
                }
            }
            game_status.getPlayer(player).justRunNoCondition = false
            null
        })
    }

    private val analyze = CardData(CardClass.NORMAL, CardName.KURURU_ANALYZE, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val dauzing = CardData(CardClass.NORMAL, CardName.KURURU_DAUZING, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val lastResearch = CardData(CardClass.SPECIAL, CardName.KURURU_LAST_RESEARCH, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val grandGulliver = CardData(CardClass.SPECIAL, CardName.KURURU_GRAND_GULLIVER, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)

    private suspend fun analyzeWhenNotAttack(player: PlayerEnum, game_status: GameStatus){
        val list = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
            listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            NUMBER_KURURU_DUPLICATED_GEAR_2, 1
        ) { _, _ -> true }?: return
        game_status.popCardFrom(player.opposite(), list[0], LocationEnum.HAND, false)?.let{
            game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
        }
    }

    private suspend fun analyzeYour(player: PlayerEnum, game_status: GameStatus, card_number: Int){
        val list = game_status.selectCardFrom(player, player, player,
            listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            NUMBER_KURURU_ANALYZE, 1
        ) { _, _ -> true }?: return
        game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, true)?.let{
            game_status.insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
            if(it.card_data.card_type == CardType.ATTACK){
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            else{
                analyzeWhenNotAttack(player, game_status)
            }
        }
    }

    private suspend fun analyzeOther(player: PlayerEnum, game_status: GameStatus, card_number: Int){
        game_status.popCardFrom(player.opposite(),
            game_status.getPlayer(player.opposite()).cover_card.random(Random(System.currentTimeMillis())).card_number,
            LocationEnum.COVER_CARD, true)?.let{
            game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
            if(it.card_data.card_type == CardType.ATTACK){
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            else{
                analyzeWhenNotAttack(player, game_status)
            }
        }
    }

    private suspend fun greatDiscovery(player: PlayerEnum, game_status: GameStatus){
        game_status.showSome(player.opposite(), CommandEnum.SHOW_SPECIAL_YOUR, -1)
        while(true) {
            when (game_status.receiveCardEffectSelect(player, NUMBER_KURURU_LAST_RESEARCH)) {
                CommandEnum.SELECT_ONE -> {
                    val unused = game_status.getPlayer(player).unselectedSpecialCard
                    game_status.moveOutCard(player, unused, LocationEnum.SPECIAL_CARD)
                    for(i in 1..unused.size){
                        unused.removeFirst()
                    }
                    break
                }

                CommandEnum.SELECT_TWO -> {
                    val unused = game_status.getPlayer(player.opposite()).unselectedSpecialCard
                    game_status.moveOutCard(player, unused, LocationEnum.SPECIAL_CARD)
                    for(i in 1..unused.size){
                        unused.removeFirst()
                    }
                    break
                }

                else -> {
                    continue
                }
            }
        }
        game_status.moveAdditionalCard(player, CardName.KURURU_GRAND_GULLIVER, LocationEnum.SPECIAL_CARD)
    }

    private fun kururuA1CardInit(){
        analyze.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.behavior >= 1 && kikou.attack >= 1 && kikou.reaction >= 1) {
                val otherSize = game_status.getPlayer(player.opposite()).cover_card.size
                val yourSize = game_status.getPlayer(player).cover_card.size
                if(yourSize != 0 || otherSize != 0){
                    if(yourSize == 0){
                        analyzeOther(player, game_status, card_number)
                    }
                    else if(otherSize == 0){
                        analyzeYour(player, game_status, card_number)
                    }
                    else{
                        while(true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_KURURU_ANALYZE)){
                                CommandEnum.SELECT_ONE -> {
                                    analyzeYour(player, game_status, card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    analyzeOther(player, game_status, card_number)
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }

                        }
                    }
                }
            }
            null
        })
        dauzing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{_, player, game_status, _ ->
            game_status.popCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP, true)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
            }
            val list = game_status.selectCardFrom(player.opposite(), player, player,
                listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_KURURU_DAUZING, 1
            ) { _, _ -> true }?: return@ret null
            game_status.getCardFrom(player.opposite(), list[0], LocationEnum.DISCARD_YOUR)?.let {
                val kikou = getKikou(player, game_status)
                val conditionKikou = Kikou(1, 0, 0, 0, 0).apply {
                    add(it)
                }
                if(kikou.attack >= conditionKikou.attack && kikou.enchantment >= conditionKikou.enchantment &&
                    kikou.behavior >= conditionKikou.behavior && kikou.reaction >= conditionKikou.reaction &&
                        kikou.fullPower >= conditionKikou.fullPower){
                    game_status.useCardFrom(player, it, LocationEnum.DISCARD_OTHER, false, null,
                        isCost = true, isConsume = true)
                }
            }
            null
        })
        lastResearch.setSpecial(1)
        lastResearch.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 1){
                val selectedByOther = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_LAST_RESEARCH, 1
                ) { _, _ -> true }?: return@ret null
                val selectedByYour = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.ALL_NORMAL), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_GRAND_GULLIVER, 1
                ) { _, _ -> true }?: return@ret null
                game_status.popCardFrom(player.opposite(), selectedByOther[0], LocationEnum.COVER_CARD, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
                if(selectedByYour[0].toCardName() == selectedByOther[0].toCardName()){
                    game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let{
                        game_status.dustToCard(player, 1, it,
                            Log.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                        if(it.getNap() == 2){
                            game_status.cardToDust(player, 2, it, false,
                                Log.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                            greatDiscovery(player, game_status)
                            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
                            game_status.getPlayer(player).afterCardUseTermination = true
                        }
                    }
                }
            }
            null
        })
        lastResearch.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addImmediateReconstructListener(player, Listener(player, card_number) {gameStatus, cardNumber, _, _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
        grandGulliver.setSpecial(null)
        grandGulliver.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_X) {_, player, game_status, _->
            game_status.getPlayerFlare(player)
        })
        grandGulliver.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) {_, _, _, _->
            0
        })
        grandGulliver.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.COST_BUFF){card_number, player, game_status, _ ->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true}, { 0 }))
            null
        })
    }

    private val betrayer = CardData(CardClass.NORMAL, CardName.SAINE_BETRAYAL, MegamiEnum.SAINE, CardType.ATTACK, SubType.NONE)
    private val flowingWall = CardData(CardClass.NORMAL, CardName.SAINE_FLOWING_WALL, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private val flowingWallText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
        game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(2, 999), false,
            null, null, card_number)
        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
            true
        }, {_, _, attack ->
            attack.auraPlusMinus(1)
        }))
        null
    }
    private val jeolChangJeolWha = CardData(CardClass.SPECIAL, CardName.SAINE_JEOL_CHANG_JEOL_HWA, MegamiEnum.SAINE, CardType.ATTACK, SubType.NONE)

    private fun saineA2CardInit(){
        betrayer.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 1, 3,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        flowingWall.setEnchantment(2)
        flowingWall.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE) {_, _, _, _ ->
            null
        })
        flowingWall.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(palSang(player, game_status) && game_status.turnPlayer == player){
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.SAINE_FLOWING_WALL, card_number, CardClass.NULL,
                        sortedSetOf(0, 1, 2, 3, 4, 5), 0,  0, MegamiEnum.SAINE,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    ).addTextAndReturn(flowingWallText), null)){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })
        jeolChangJeolWha.setSpecial(1)
        jeolChangJeolWha.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 2, 999,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jeolChangJeolWha.addtext(termination)
        jeolChangJeolWha.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {_, player, game_status, react_attack ->
            react_attack?.addTextAndReturn(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { _, in_player, in_game_status, _ ->
                if(in_game_status.getPlayerAura(in_player.opposite()) == 0){
                    game_status.endCurrentPhase = true
                }
                null
            })
            null
        })

    }

    private val waterBall = CardData(CardClass.NORMAL, CardName.HATSUMI_WATER_BALL, MegamiEnum.HATSUMI, CardType.ATTACK, SubType.NONE)
    private val waterCurrent = CardData(CardClass.NORMAL, CardName.HATSUMI_WATER_CURRENT, MegamiEnum.HATSUMI, CardType.ATTACK, SubType.NONE)
    private val strongAcid = CardData(CardClass.NORMAL, CardName.HATSUMI_STRONG_ACID, MegamiEnum.HATSUMI, CardType.ATTACK, SubType.NONE)
    private val tsunami = CardData(CardClass.NORMAL, CardName.HATSUMI_TSUNAMI, MegamiEnum.HATSUMI, CardType.BEHAVIOR, SubType.REACTION)
    private val junBiManTen = CardData(CardClass.NORMAL, CardName.HATSUMI_JUN_BI_MAN_TAN, MegamiEnum.HATSUMI, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val compass = CardData(CardClass.NORMAL, CardName.HATSUMI_COMPASS, MegamiEnum.HATSUMI, CardType.ENCHANTMENT, SubType.NONE)
    private val callWave = CardData(CardClass.NORMAL, CardName.HATSUMI_CALL_WAVE, MegamiEnum.HATSUMI, CardType.ENCHANTMENT,SubType.NONE)
    private val isanaHail = CardData(CardClass.SPECIAL, CardName.HATSUMI_ISANA_HAIL, MegamiEnum.HATSUMI, CardType.ATTACK, SubType.NONE)
    private val oyogibiFire = CardData(CardClass.SPECIAL, CardName.HATSUMI_OYOGIBI_FIRE, MegamiEnum.HATSUMI, CardType.ATTACK, SubType.NONE)
    private val kirahariLighthouse = CardData(CardClass.SPECIAL, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, MegamiEnum.HATSUMI, CardType.BEHAVIOR, SubType.NONE)
    private val miobikiRoute = CardData(CardClass.SPECIAL, CardName.HATSUMI_MIOBIKI_ROUTE, MegamiEnum.HATSUMI, CardType.BEHAVIOR, SubType.NONE)

    private fun isTailWind(player: PlayerEnum, game_status: GameStatus) = game_status.getPlayer(player).isThisTurnTailWind
    private fun isHeadWind(player: PlayerEnum, game_status: GameStatus) = !game_status.getPlayer(player).isThisTurnTailWind

    private val callWaveText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR){_, player, game_status, _ ->
        while (true){
            val list = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_HATSUMI_CALL_WAVE
            ) { _, _ -> true }?: break
            if (list.size == 1){
                game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, false)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_TOP, false)
                }
                break
            }
            else if(list.size == 0){
                break
            }
        }
        null
    }

    private val miobikiRouteText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_HATSUMI_MIOBIKI_ROUTE)){
                CommandEnum.SELECT_ONE -> {
                    game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                        game_status.useCardFrom(player, it, LocationEnum.YOUR_USED_CARD, false, null,
                            isCost = true, isConsume = false)
                    }
                    break
                }
                CommandEnum.SELECT_NOT -> {
                    break
                }
                else -> {}
            }
        }
        null
    }

    private fun hatsumiCardInit(){
        waterBall.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 0, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        waterBall.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {condition_player, condition_game_status, _ ->
                isTailWind(condition_player, condition_game_status)
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(2); lifePlusMinus(2)
                }
            }))
            null
        })
        waterBall.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                while (true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_HATSUMI_WATER_BALL)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player,
                                game_status.getCardOwner(card_number), card_number)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.distanceToDust(2, Arrow.ONE_DIRECTION, player,
                                game_status.getCardOwner(card_number), card_number)
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
        waterCurrent.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        waterCurrent.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(game_status.getFullAction(player)){
                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET, { _, _, _ -> true},
                    { _, _, attack -> attack.canNotReactNormal()
                    })
                )
            }
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {condition_player, condition_game_status, _ ->
                isTailWind(condition_player, condition_game_status)
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        waterCurrent.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                LocationEnum.DISTANCE.real_number
            }
            else{
                null
            }
        })
        waterCurrent.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                LocationEnum.DISTANCE.real_number
            }
            else{
                null
            }
        })
        strongAcid.setAttack(DistanceType.CONTINUOUS, Pair(5, 6), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        strongAcid.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                LocationEnum.DISTANCE.real_number
            }
            else{
                null
            }
        })
        tsunami.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() <= 4){
                if(isHeadWind(player, game_status)){
                    game_status.flareToDistance(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                }
                else{
                    game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                }
            }
            null
        })
        junBiManTen.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.dustToAura(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        junBiManTen.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){_, player, game_status, _ ->
            while (true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_HATSUMI_JUN_BI_MAN_TAN)){
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
            if(isHeadWind(player, game_status)){
                game_status.getPlayer(player).maxHand += 1
            }
            null
        })
        compass.setEnchantment(3)
        compass.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.ADD_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack ->
                    attack.tempEditedDistance.add(5)
                }))
            null
        })
        compass.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player.opposite(), RangeBuff(card_number,1, RangeBufTag.DELETE_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack ->
                    attack.tempEditedDistance.add(5)
                }))
            null
        })
        compass.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        callWave.setEnchantment(1)
        callWave.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_NAP) {_, player, game_status, _ ->
            if(game_status.turnPlayer == player && isTailWind(player, game_status)) 1
            else 0
        })
        callWave.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, callWaveText)
            null
        })
        callWave.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD){_, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HATSUMI_CALL_WAVE
                ) { _, _ -> true }?: break
                if (list.size == 1){
                    game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_TOP, false)
                    }
                    break
                }
                else if(list.size == 0){
                    break
                }
            }
            null
        })
        callWave.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.DO_BASIC_OPERATION){_, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, 1706)
            null
        })
        callWave.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.HATSUMI_CALL_WAVE, card_number, CardClass.NULL,
                    sortedSetOf(2, 3, 4, 5, 6, 7), 1,  999, MegamiEnum.HATSUMI,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ), null) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        isanaHail.setSpecial(4)
        isanaHail.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        isanaHail.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {conditionPlayer, conditionGameStatus, _ -> isTailWind(conditionPlayer, conditionGameStatus)})
                {_, _, attack ->
                    attack.lifePlusMinus(2)
                })
            null
        }))
        isanaHail.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
                game_status.movePlayingCard(player, LocationEnum.SPECIAL_CARD, card_number)
            }
            null
        })
        isanaHail.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                game_status.movePlayingCard(player, LocationEnum.SPECIAL_CARD, card_number)
            }
            null
        })
        oyogibiFire.setSpecial(2)
        oyogibiFire.setAttack(DistanceType.CONTINUOUS, Pair(5, 6), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        oyogibiFire.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_THIS_CARD_RETURN){ _, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, 1708)
            null
        })
        oyogibiFire.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addDistanceListener(player, Listener(player, card_number) {gameStatus, cardNumber, _, _, _, _ ->
                if(game_status.startTurnDistance - game_status.getAdjustDistance() >= 2){
                    gameStatus.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })
        kirahariLighthouse.setSpecial(1)
        kirahariLighthouse.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            if(!isHeadWind(player, game_status)){
                game_status.setShrink(player)
            }
            null
        })
        kirahariLighthouse.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.CHANGE_SWELL_DISTANCE) {_, _, _, _ ->
            1
        })
        kirahariLighthouse.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.HATSUMI_LIGHTHOUSE) {_, _, _, _ ->
            1
        })
        kirahariLighthouse.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.AFTER_HATSUMI_LIGHTHOUSE) {card_number, player, game_status, _ ->
            game_status.returnSpecialCard(player, card_number)
            null
        })
        miobikiRoute.setSpecial(2)
        miobikiRoute.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                game_status.setShrink(player.opposite())
                game_status.getCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP)?.let {showCard ->
                    if(showCard.card_data.card_type == CardType.ATTACK){
                        game_status.popCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP, true)?.let {popCard ->
                            game_status.insertCardTo(player.opposite(), popCard, LocationEnum.DISCARD_YOUR, true)
                        }
                    }
                    else{
                        game_status.showSome(player.opposite(), CommandEnum.SHOW_DECK_TOP_YOUR, showCard.card_number)
                    }
                }
            }
            null
        })
        miobikiRoute.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, miobikiRouteText)
            }
            null
        })
    }

    private val jinDu = CardData(CardClass.NORMAL, CardName.MIZUKI_JIN_DU, MegamiEnum.MIZUKI, CardType.ATTACK, SubType.NONE)
    private val banGong = CardData(CardClass.NORMAL, CardName.MIZUKI_BAN_GONG, MegamiEnum.MIZUKI, CardType.ATTACK, SubType.NONE)
    private val shootingDown = CardData(CardClass.NORMAL, CardName.MIZUKI_SHOOTING_DOWN, MegamiEnum.KODAMA, CardType.ATTACK, SubType.REACTION)
    private val hoLyeong = CardData(CardClass.NORMAL, CardName.MIZUKI_HO_LYEONG, MegamiEnum.MIZUKI, CardType.BEHAVIOR, SubType.NONE)
    private val bangByeog = CardData(CardClass.NORMAL, CardName.MIZUKI_BANG_BYEOG, MegamiEnum.MIZUKI, CardType.BEHAVIOR, SubType.REACTION)
    private val overpoweringGoForward = CardData(CardClass.NORMAL, CardName.MIZUKI_OVERPOWERING_GO_FORWARD, MegamiEnum.MIZUKI, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val jeonJang = CardData(CardClass.NORMAL, CardName.MIZUKI_JEON_JANG, MegamiEnum.MIZUKI, CardType.ENCHANTMENT, SubType.NONE)
    private val hachiryuCheonjugak = CardData(CardClass.SPECIAL, CardName.MIZUKI_HACHIRYU_CHEONJUGAK, MegamiEnum.MIZUKI, CardType.ENCHANTMENT, SubType.REACTION)
    private val hijamaruTriplet = CardData(CardClass.SPECIAL, CardName.MIZUKI_HIJAMARU_TRIPLET, MegamiEnum.MIZUKI, CardType.ATTACK, SubType.NONE)
    private val tartenashiDaesumun = CardData(CardClass.SPECIAL, CardName.MIZUKI_TARTENASHI_DAESUMUN, MegamiEnum.MIZUKI, CardType.BEHAVIOR, SubType.NONE)
    private val mizukiBattleCry = CardData(CardClass.SPECIAL, CardName.MIZUKI_MIZUKI_BATTLE_CRY, MegamiEnum.MIZUKI, CardType.ENCHANTMENT, SubType.FULL_POWER)

    private val tusin = CardData(CardClass.NORMAL, CardName.KODAMA_TU_SIN, MegamiEnum.KODAMA, CardType.ATTACK, SubType.NONE)
    private val spearSoldier1 = CardData(CardClass.SOLDIER, CardName.SOLDIER_SPEAR_1, MegamiEnum.NONE, CardType.ATTACK, SubType.NONE)
    private val spearSoldier2 = CardData(CardClass.SOLDIER, CardName.SOLDIER_SPEAR_2, MegamiEnum.NONE, CardType.ATTACK, SubType.NONE)
    private val shieldSoldier = CardData(CardClass.SOLDIER, CardName.SOLDIER_SHIELD, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.REACTION)
    private val horseSoldier = CardData(CardClass.SOLDIER, CardName.SOLDIER_HORSE, MegamiEnum.NONE, CardType.ENCHANTMENT, SubType.NONE)


    private fun duplicateCardDataForFullPower(original: Card){
        val cardData = original.card_data
        val result = CardData(cardData.card_class, cardData.card_name, cardData.megami, cardData.card_type, SubType.NONE)
        result.run {
            umbrellaMark = cardData.umbrellaMark

            effectFold = cardData.effectFold
            effectUnfold = cardData.effectUnfold

            distanceTypeFold = cardData.distanceTypeFold
            distanceContFold = cardData.distanceContFold
            distanceUncontFold = cardData.distanceUncontFold
            lifeDamageFold = cardData.lifeDamageFold
            auraDamageFold = cardData.auraDamageFold

            distanceTypeUnfold = cardData.distanceTypeUnfold
            distanceContUnfold = cardData.distanceContUnfold
            distanceUncontUnfold = cardData.distanceUncontUnfold
            lifeDamageUnfold = cardData.lifeDamageUnfold
            auraDamageUnfold = cardData.auraDamageUnfold

            growing = cardData.growing

            distance_type = cardData.distance_type
            distance_cont = cardData.distance_cont
            distance_uncont = cardData.distance_uncont
            life_damage =  cardData.life_damage
            aura_damage = cardData.aura_damage

            charge = cardData.charge

            cost = cardData.cost

            effect = mutableListOf<Text>().apply {
                cardData.effect?.let { duplicateEffect ->
                    for(text in duplicateEffect){
                        add(text)
                    }
                }
                add(termination)
            }
            canCover = cardData.canCover
            canDiscard = cardData.canDiscard
        }
        original.beforeCardData = original.card_data
        original.card_data = result
    }
    private fun duplicateCardDataForTermination(original: Card){
        val cardData = original.card_data
        val result = CardData(cardData.card_class, cardData.card_name, cardData.megami, cardData.card_type, cardData.sub_type)
        result.run {
            umbrellaMark = cardData.umbrellaMark

            effectFold = cardData.effectFold
            effectUnfold = cardData.effectUnfold

            distanceTypeFold = cardData.distanceTypeFold
            distanceContFold = cardData.distanceContFold
            distanceUncontFold = cardData.distanceUncontFold
            lifeDamageFold = cardData.lifeDamageFold
            auraDamageFold = cardData.auraDamageFold

            distanceTypeUnfold = cardData.distanceTypeUnfold
            distanceContUnfold = cardData.distanceContUnfold
            distanceUncontUnfold = cardData.distanceUncontUnfold
            lifeDamageUnfold = cardData.lifeDamageUnfold
            auraDamageUnfold = cardData.auraDamageUnfold

            growing = cardData.growing

            distance_type = cardData.distance_type
            distance_cont = cardData.distance_cont
            distance_uncont = cardData.distance_uncont
            life_damage =  cardData.life_damage
            aura_damage = cardData.aura_damage

            charge = cardData.charge

            cost = cardData.cost

            effect = mutableListOf<Text>().apply {
                cardData.effect?.let { duplicateEffect ->
                    for(text in duplicateEffect){
                        if(text === termination){
                            continue
                        }
                        add(text)
                    }
                }
            }
            canCover = cardData.canCover
            canDiscard = cardData.canDiscard
        }
        original.beforeCardData = original.card_data
        original.card_data = result
    }

    private suspend fun draft(player: PlayerEnum, game_status: GameStatus){
        game_status.selectCardFrom(player, player, player, listOf(LocationEnum.NOT_READY_SOLDIER_ZONE),
            CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_MIZUKI_JIN_DU, 1
        ) { _, _ -> true }?.let {
            game_status.popCardFrom(player, it[0], LocationEnum.NOT_READY_SOLDIER_ZONE, false)?.let {soldier ->
                game_status.insertCardTo(player, soldier, LocationEnum.READY_SOLDIER_ZONE, false)
            }
        }
    }

    private fun fixed(game_status: GameStatus) = !(game_status.thisTurnDistanceChange)

    private fun mizukiCardInit(){
        jinDu.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jinDu.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            draft(player, game_status)
            null
        })
        banGong.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        banGong.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player).lastTurnReact
            }, {_, _, attack ->
                attack.auraPlusMinus(2); attack.lifePlusMinus(1)
            }))
            null
        })
        banGong.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(game_status.getFullAction(player)){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true
                }, {_, _, attack ->
                    attack.auraPlusMinus(1); attack.lifePlusMinus(1)
                }))
            }
            null
        })
        shootingDown.setAttack(DistanceType.CONTINUOUS, Pair(1, 5), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shootingDown.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {card_number, _, game_status, react_attack ->
            if(fixed(game_status)){
                react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })
        hoLyeong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            draft(player, game_status)
            null
        })
        hoLyeong.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            if(game_status.getPlayer(player).lastTurnReact){
                game_status.getConcentration(player)
            }
            null
        })
        bangByeog.addtext(termination)
        bangByeog.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_INVALID){card_number, player, game_status, react_attack->
            if(!(game_status.getPlayer(player).thisTurnReact && react_attack?.card_class != CardClass.SPECIAL &&
                        react_attack?.subType != SubType.FULL_POWER)){
                react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })
        overpoweringGoForward.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION){_, player, game_status, _->
            for(i in 1..3){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_MIZUKI_OVERPOWERING_GO_FORWARD)){
                        CommandEnum.SELECT_ONE -> {
                            draft(player, game_status)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.doBasicOperation(player, CommandEnum.ACTION_GO_FORWARD,
                                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_MIZUKI_OVERPOWERING_GO_FORWARD)
                            break
                        }
                        CommandEnum.SELECT_THREE -> {
                            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_MIZUKI_OVERPOWERING_GO_FORWARD)
                            break
                        }
                        else -> {

                        }
                    }
                }
            }
            null
        })

        jeonJang.setEnchantment(3)
        jeonJang.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            if(fixed(game_status) && !(game_status.logger.checkThisTurnDoAttackNotSpecial(player))){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {
                        _, _, attack -> attack.card_class != CardClass.SPECIAL },
                    { _, _, madeAttack ->
                        madeAttack.apply {
                            auraPlusMinus(1); lifePlusMinus(1)
                        }
                    }))
            }
            null
        })
        hachiryuCheonjugak.setSpecial(5)
        hachiryuCheonjugak.setEnchantment(3)
        hachiryuCheonjugak.addtext(termination)
        hachiryuCheonjugak.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_INVALID) { card_number, _, _, react_attack ->
            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                true
            }, { _, _, attack ->
                attack.makeNotValid()
            }))
            null
        })
        hachiryuCheonjugak.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {
                    _, _, attack -> attack.card_class == CardClass.SOLDIER || attack.megami != MegamiEnum.MIZUKI},
                { _, _, madeAttack ->
                    madeAttack.apply {
                        lifePlusMinus(1)
                    }
                }))
            null
        })
        hijamaruTriplet.setSpecial(2)
        hijamaruTriplet.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        hijamaruTriplet.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, player, game_status, _ ->
            if(!(game_status.logger.checkThisTurnDoAttack(player))) 1
            else 0
        })
        hijamaruTriplet.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){card_number, player, game_status, _ ->
            game_status.addTerminationListener(player, Listener(player, card_number) {_, cardNumber, _,
                                                                                        _, _, _ ->
                game_status.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
        tartenashiDaesumun.setSpecial(3)
        tartenashiDaesumun.addtext(termination)
        tartenashiDaesumun.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){_, player, game_status, _ ->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_MIZUKI_TARTENASHI_DAESUMUN, 1
            ) { _, _ -> true }?.let{ selected ->
                game_status.popCardFrom(player, selected[0], LocationEnum.HAND, false)?.let { card ->
                    game_status.insertCardTo(player, card, LocationEnum.READY_SOLDIER_ZONE, false)
                }
            }
            game_status.popCardFrom(player, CardName.KODAMA_TU_SIN, LocationEnum.ADDITIONAL_CARD, true)?.let {card ->
                game_status.insertCardTo(player, card, LocationEnum.READY_SOLDIER_ZONE, false)
            }
            null
        })
        tartenashiDaesumun.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {condition_player, condition_game_status, attack ->
                attack.card_class != CardClass.NULL && condition_game_status.logger.checkThisCardUseInSoldier(condition_player, attack.card_number)
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(1)
                }
            }))
            null
        })
        tartenashiDaesumun.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.REMOVE_REACTIONS_TERMINATION){_, _, _, _ ->
            1
        })
        mizukiBattleCry.setSpecial(5)
        mizukiBattleCry.setEnchantment(5)
        mizukiBattleCry.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MIZUKI_BATTLE_CRY){_, player, game_status, _ ->
            val cardPlayer = game_status.getPlayer(player)
            val notCardPlayer = game_status.getPlayer(player.opposite())
            for(card in cardPlayer.hand.values + cardPlayer.normalCardDeck + cardPlayer.discard + cardPlayer.cover_card
                    + cardPlayer.notReadySoldierZone.values + cardPlayer.readySoldierZone.values + cardPlayer.additionalHand.values
                    + notCardPlayer.sealZone.values.filter {
                it.player == player
            } + cardPlayer.sealZone.values.filter {
                it.player == player
            } + cardPlayer.enchantmentCard.values.filter {
                it.player == player
            } + notCardPlayer.enchantmentCard.values.filter {
                it.player == player
            }){
                if(card.beforeCardData != null){
                    break
                }
                else{
                    if(card.card_data.card_class == CardClass.SOLDIER || card.card_data.card_class == CardClass.NORMAL){
                        if(card.card_data.sub_type == SubType.FULL_POWER){
                            duplicateCardDataForFullPower(card)
                        }
                        else{
                            card.card_data.effect?.let {
                                for (text in it){
                                    if(text === termination){
                                        duplicateCardDataForTermination(card)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }
            null
        })
        mizukiBattleCry.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
        ret@{ _, player, game_status, _ ->
            for(card in game_status.getPlayer(player).enchantmentCard.values){
                card.card_data.effect?.let{
                    for(text in it){
                        if(text.tag == TextEffectTag.MIZUKI_BATTLE_CRY){
                            return@ret null
                        }
                    }

                }
            }
            val cardPlayer = game_status.getPlayer(player)
            val notCardPlayer = game_status.getPlayer(player.opposite())
            for(card in cardPlayer.hand.values + cardPlayer.normalCardDeck + cardPlayer.discard + cardPlayer.cover_card
                    + cardPlayer.notReadySoldierZone.values + cardPlayer.readySoldierZone.values + cardPlayer.additionalHand.values
                    + notCardPlayer.sealZone.values.filter {
                it.player == player
            } + cardPlayer.sealZone.values.filter {
                it.player == player
            } + cardPlayer.enchantmentCard.values.filter {
                it.player == player
            } + notCardPlayer.enchantmentCard.values.filter {
                it.player == player
            }){
                card.beforeCardData?.let {
                    card.card_data = it
                    card.beforeCardData = null
                }
            }
            null
        })
        tusin.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        tusin.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, buff_game_status, _ ->
                fixed(buff_game_status)
            }, {_, _, madeAttack ->
                madeAttack.run {
                    lifePlusMinus(1)
                }
            }))
            null
        })
        tusin.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
            null
        })
        spearSoldier1.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        spearSoldier1.addtext(termination)
        spearSoldier1.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player).lastTurnReact
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })
        spearSoldier2.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        spearSoldier2.addtext(termination)
        spearSoldier2.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player).lastTurnReact
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })
        shieldSoldier.addtext(termination)
        shieldSoldier.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_CHANGE){card_number, _, _, react_attack->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, attack ->
                attack.subType != SubType.FULL_POWER },
                {_, _, attack ->
                    attack.auraPlusMinus(-1)
                }))
            null
        })
        horseSoldier.setEnchantment(2)
        horseSoldier.addtext(termination)
        horseSoldier.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            game_status.addThisTurnOtherBuff(player.opposite(), OtherBuff(card_number, 1, OtherBuffTag.LOSE_IMMEDIATE,
                { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.editedCannotReactNormal = false
                    attack.editedCannotReact = false
                }))
            null
        })
        horseSoldier.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
    }

    private val helpOrThreat = CardData(CardClass.NORMAL, CardName.YUKIHI_HELP_SLASH_THREAT, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val threadOrRawThread = CardData(CardClass.NORMAL, CardName.YUKIHI_THREAD_SLASH_RAW_THREAD, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val flutteringCollar = CardData(CardClass.SPECIAL, CardName.YUKIHI_FLUTTERING_COLLAR, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.NONE)
    private val flutteringCollarText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR) { card_number, player, game_status, _->
        if(game_status.addPreAttackZone(player, MadeAttack(CardName.YUKIHI_FLUTTERING_COLLAR, card_number, CardClass.NULL,
                sortedSetOf(0, 1, 2, 3, 4, 5), 2,  2, MegamiEnum.YUKIHI,
                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
            ), null) ){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }
    private val textForFlutteringCollar = Text(TextEffectTimingTag.USING, TextEffectTag.WHEN_AFTER_CARD_USE) {card_number, player, game_status, _->
        game_status.addConcentration(player)
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_FLUTTERING_COLLAR)){
                CommandEnum.SELECT_ONE -> {
                    game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?.let {
                        game_status.dustToCard(player, 3, it, card_number)
                    }
                    break
                }
                CommandEnum.SELECT_NOT -> {
                    break
                }
                else -> {}
            }
        }
        null
    }

    private fun yukihiA1CardInit(){
        helpOrThreat.umbrellaMark = true
        helpOrThreat.setAttackFold(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1)
        helpOrThreat.setAttackUnfold(DistanceType.CONTINUOUS, Pair(1, 2), null, 1, 2)
        helpOrThreat.addTextFold(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {condition_player, condition_game_status, _ ->
                var token = 0
                for(card in condition_game_status.getPlayer(condition_player).enchantmentCard.values){
                    card.getNap()?.let {
                        token += it
                    }
                }
                token >= 4
            }, {_, _, madeAttack ->
                madeAttack.lifePlusMinus(1)
            }))
            null
        })
        helpOrThreat.addTextUnfold(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                var token = 0
                for(card in game_status.getPlayer(player).enchantmentCard.values){
                    card.getNap()?.let {
                        token += it
                    }
                }
                token >= 4
            }, {_, _, madeAttack ->
                madeAttack.auraPlusMinus(1)
            }))
            null
        })
        threadOrRawThread.umbrellaMark = true
        threadOrRawThread.setAttackFold(DistanceType.CONTINUOUS, Pair(2, 8), null, 1, 1)
        threadOrRawThread.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 4), null, 0, 0)
        threadOrRawThread.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS, {_, _, attack -> (attack.megami != MegamiEnum.YUKIHI)},
                { _, _, attack -> attack.apply {
                    plusMinusRange(1, true); plusMinusRange(1, false)
                }
                }))
            null
        })
        threadOrRawThread.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_BELOW, card_number)
            null
        })
        threadOrRawThread.addTextUnfold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.USE_CARD) {card_number, player, game_status, _->
            if(!(game_status.logger.checkThisTurnUseCard(player) { card -> card != card_number })){
                game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_YUKIHI_HELP_SLASH_THREAT, 1){
                    card, _ -> card.card_data.sub_type != SubType.FULL_POWER &&
                        card.card_data.megami != game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.card_data?.megami
                }?.let { selected ->
                    game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?.let { card ->
                        game_status.useCardFrom(player, card, LocationEnum.DISCARD_YOUR, false, null,
                            isCost = true, isConsume = true
                        )
                    }
                }
            }
            null
        })
        flutteringCollar.umbrellaMark = true
        flutteringCollar.setSpecial(4)
        flutteringCollar.setEnchantment(1)
        flutteringCollar.addTextFold(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_AFTER_CARD_USE) ret@{card_number, player, game_status, card->
            if(game_status.turnPlayer == player){
                val usedCard = game_status.cardForEffect?: return@ret null
                if(usedCard.card_data.megami != MegamiEnum.YUKIHI && game_status.logger.countCardUseCount(player, usedCard.card_number) == 1){
                    if(game_status.logger.checkThisTurnUseCardCondition(player){ cardNumber, megamiNumber ->
                            val megami = MegamiEnum.fromInt(megamiNumber)
                            if(megami == MegamiEnum.YUKIHI) 2
                            else if(cardNumber == usedCard.card_number) 1
                            else 0
                    }){
                        usedCard.cardUseEndEffect[card_number] = textForFlutteringCollar
                    }
                }
            }
            null
        })
        flutteringCollar.addTextUnfold(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR) {card_number, _, game_status, _->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, flutteringCollarText)
            null
        })
    }

    private val quickChange = CardData(CardClass.NORMAL, CardName.THALLYA_QUICK_CHANGE, MegamiEnum.THALLYA, CardType.ENCHANTMENT, SubType.NONE)
    private val blackboxNeo = CardData(CardClass.SPECIAL, CardName.THALLYA_BLACKBOX_NEO, MegamiEnum.THALLYA, CardType.BEHAVIOR, SubType.NONE)
    private val omnisBlaster = CardData(CardClass.SPECIAL, CardName.THALLYA_OMNIS_BLASTER, MegamiEnum.THALLYA, CardType.ATTACK, SubType.NONE)
    private val formKinnari = CardData(CardClass.SPECIAL, CardName.FORM_KINNARI, MegamiEnum.THALLYA, CardType.UNDEFINED, SubType.NONE)
    private val formAsura = CardData(CardClass.SPECIAL, CardName.FORM_ASURA, MegamiEnum.THALLYA, CardType.UNDEFINED, SubType.NONE)
    private val formDeva = CardData(CardClass.SPECIAL, CardName.FORM_DEVA, MegamiEnum.THALLYA, CardType.UNDEFINED, SubType.NONE)

    val attackAsuraText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
        game_status.setShrink(player)
        null
    }

    private fun thallyaA1CardInit(){
        quickChange.setEnchantment(3)
        quickChange.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) { _, player, game_status, _ ->
            game_status.restoreArtificialToken(player, 1)
            null
        })
        quickChange.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.SEAL_CARD) { card_number, player, game_status, _ ->
            val cardList = makeTransformList(player, game_status)
            if(cardList.size != 0){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_THALLYA_QUICK_CHANGE, 1)[0]
                game_status.getCardFrom(player, get, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.moveAdditionalCard(player, get.toCardName(), LocationEnum.SEAL_ZONE)
                    val nowPlayer = game_status.getPlayer(player)
                    nowPlayer.sealInformation[card_number]?.add(it.card_number) ?: run {
                        nowPlayer.sealInformation[card_number] = mutableListOf(it.card_number)
                    }
                    it.special_card_state = SpecialCardEnum.PLAYED
                }
            }
            null
        })
        quickChange.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.ACTIVE_TRANSFORM_BELOW_THIS_CARD, null))
        quickChange.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.SEAL_CARD) {card_number, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)
            val otherPlayer = game_status.getPlayer(player)
            nowPlayer.sealInformation[card_number]?.let { sealedList ->
                for(sealedCardNumber in sealedList){
                    game_status.popCardFrom(player, sealedCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        it.special_card_state = SpecialCardEnum.UNUSED
                        game_status.insertCardTo(it.player, it, LocationEnum.ADDITIONAL_CARD, true)
                    }
                }
            }
            nowPlayer.sealInformation.remove(card_number)

            otherPlayer.sealInformation[card_number]?.let { sealedList ->
                for(sealedCardNumber in sealedList){
                    game_status.popCardFrom(player.opposite(), sealedCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                        it.special_card_state = SpecialCardEnum.UNUSED
                        game_status.insertCardTo(it.player, it, LocationEnum.ADDITIONAL_CARD, true)
                    }
                }
            }
            otherPlayer.sealInformation.remove(card_number)
            null
        })
        blackboxNeo.setSpecial(1)
        blackboxNeo.addtext(termination)
        blackboxNeo.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{card_number, player, game_status, _ ->
            game_status.restoreArtificialToken(player, 1)
            if(game_status.getPlayer(player).artificialTokenBurn == 0){
                game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let{
                    game_status.dustToCard(player, 1, it, Log.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                    if(it.getNap() == 2){
                        game_status.cardToDust(player, 2, it, false,
                            Log.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                        transform(player, game_status)
                    }
                }
            }
            null
        })
        blackboxNeo.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken ?: 0) <= 3 || game_status.logger.checkThisTurnTransform(player)) 1
            else 0
        })
        omnisBlaster.setSpecial(null)
        omnisBlaster.setAttack(DistanceType.CONTINUOUS, Pair(3, 10), null, 1000, 1000,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        omnisBlaster.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    tempEditedAuraDamage = gameStatus.getPlayer(nowPlayer).transformNumber
                    tempEditedAuraDamage = gameStatus.getPlayer(nowPlayer).transformNumber
                }
            }))
            null
        })
        omnisBlaster.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_X) {_, player, game_status, _->
            game_status.getPlayer(player).transformNumber
        })
        formKinnari.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {_, player, game_status, _ ->
            for(i in 1..game_status.getPlayer(player.opposite()).normalCardDeck.size){
                game_status.popCardFrom(player.opposite(), 1, LocationEnum.YOUR_DECK_TOP, false)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                }
            }
            null
        })
        formKinnari.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_OTHER) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.FORM_KINNARI, card_number, CardClass.NULL,
                    sortedSetOf(2, 4, 6), 2,  2,  MegamiEnum.THALLYA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ), null) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        formAsura.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {_, player, game_status, _ ->
            for(i in 1..2){
                game_status.selectCardFrom(player.opposite(), player.opposite(), player, listOf(LocationEnum.COVER_CARD),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_FORM_ASURA, 1){_, _ -> true}?.let {selected ->
                    game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.COVER_CARD, true)?.let { card ->
                        game_status.insertCardTo(player.opposite(), card, LocationEnum.DISCARD_YOUR, true)
                    }
                }?: break
            }
            null
        })
        formDeva.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) {_, player, game_status, _ ->
            game_status.restoreArtificialToken(player, 2)
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 1119)
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 1119)
            null
        })
        formDeva.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER) {_, player, game_status, _ ->
            val otherPlayer = game_status.getPlayer(player.opposite())
            if(otherPlayer.normalCardDeck.size != 0 && otherPlayer.normalCardDeck.size % 2 == 0){
                game_status.addConcentration(player)
            }
            null
        })
    }

    private val storm = CardData(CardClass.NORMAL, CardName.RAIRA_STORM, MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val furiousStorm = CardData(CardClass.NORMAL, CardName.RAIRA_FURIOUS_STORM, MegamiEnum.RAIRA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val jinPungJeCheonUi = CardData(CardClass.SPECIAL, CardName.RAIRA_JIN_PUNG_JE_CHEON_UI, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.NONE)
    private val furiousStormText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){ card_number, player, game_status, _ ->
        if(game_status.addPreAttackZone(player, MadeAttack(CardName.RAIRA_FURIOUS_STORM, card_number, CardClass.NULL,
                sortedSetOf(0, 1, 2, 3, 4), 1,  1, MegamiEnum.RAIRA,
                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
            ), null) ){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }

    /**
     user can select stromforce effect not use or use
     */
    private suspend fun stormForce(player: PlayerEnum, game_status: GameStatus){
        val nowPlayer = game_status.getPlayer(player)
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_STORM, CommandEnum.SELECT_CARD_EFFECT)){
                CommandEnum.SELECT_ONE -> {
                    if((game_status.getPlayer(player).windGauge ?: 0) >= 1){
                        while(true){
                            val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_JIN_PUNG_JE_CHEON_UI)
                            if(selectDustToDistance(nowCommand, game_status, player,
                                    PlayerEnum.PLAYER1, Log.STORM_FORCE)) {
                                game_status.logger.insert(Log(player, LogText.END_EFFECT, Log.STORM_FORCE, -1))
                                break
                            }
                        }
                        game_status.setGauge(player, false, nowPlayer.windGauge!! - 1)
                        sendSimpleCommand(game_status.getSocket(player.opposite()), CommandEnum.SELECT_WIND_ONE)
                        break
                    }
                    else{
                        continue
                    }
                }
                CommandEnum.SELECT_TWO -> {
                    if((game_status.getPlayer(player).windGauge ?: 0) >= 2){
                        game_status.drawCard(player, 1)
                        game_status.selectCardFrom(player, player, player,
                            listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                            NUMBER_RAIRA_STORM, 1
                        ) { card, _ -> card.card_data.canCover }?.let { selected ->
                            game_status.popCardFrom(player, selected[0], LocationEnum.HAND, false)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                            }
                        }
                        game_status.setGauge(player, false, nowPlayer.windGauge!! - 2)
                        sendSimpleCommand(game_status.getSocket(player.opposite()), CommandEnum.SELECT_WIND_TWO)
                        break
                    }
                    else{
                        continue
                    }
                }
                CommandEnum.SELECT_THREE -> {
                    if((game_status.getPlayer(player).windGauge ?: 0) >= 3){
                        game_status.addConcentration(player)
                        if(game_status.getPlayer(player.opposite()).concentration != 0){
                            game_status.setConcentration(player.opposite(), game_status.getPlayer(player.opposite()).concentration - 1)
                        }
                        game_status.setGauge(player, false, nowPlayer.windGauge!! - 3)
                        sendSimpleCommand(game_status.getSocket(player.opposite()), CommandEnum.SELECT_WIND_THREE)
                        break
                    }
                    else{
                        continue
                    }
                }
                CommandEnum.SELECT_FOUR -> {
                    if((game_status.getPlayer(player).thunderGauge ?: 0) >= 1){
                        game_status.addThisTurnAttackBuff(player, Buff(1214, 1, BufTag.PLUS_MINUS,
                            {_, _, _ -> true},
                            {_, _, attack ->
                                attack.auraPlusMinus(1)
                            }))
                        game_status.setGauge(player, true, nowPlayer.thunderGauge!! - 1)
                        sendSimpleCommand(game_status.getSocket(player.opposite()), CommandEnum.SELECT_THUNDER_ONE)
                        break
                    }
                    else{
                        continue
                    }
                }
                CommandEnum.SELECT_FIVE -> {
                    if((game_status.getPlayer(player).thunderGauge ?: 0) >= 2){
                        if(game_status.addPreAttackZone(player, MadeAttack(CardName.RAIRA_JIN_PUNG_JE_CHEON_UI,
                                Log.STORM_FORCE, CardClass.NULL,
                                sortedSetOf(0, 1, 2, 3, 4), 1,  1, MegamiEnum.RAIRA,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                            ), null) ){
                            game_status.afterMakeAttack(1216, player, null)
                        }
                        game_status.setGauge(player, true, nowPlayer.thunderGauge!! - 2)
                        sendSimpleCommand(game_status.getSocket(player.opposite()), CommandEnum.SELECT_THUNDER_TWO)
                        break
                    }
                    else{
                        continue
                    }
                }
                CommandEnum.SELECT_SIX -> {
                    if((game_status.getPlayer(player).thunderGauge ?: 0) >= 3){
                        game_status.addThisTurnAttackBuff(player, Buff(1216, 1, BufTag.PLUS_MINUS,
                            {_, _, attack -> attack.getEditedAuraDamage() != 999},
                            {_, _, attack ->
                                attack.lifePlusMinus(1)
                            }))
                        game_status.setGauge(player, true, nowPlayer.thunderGauge!! - 3)
                        sendSimpleCommand(game_status.getSocket(player.opposite()), CommandEnum.SELECT_THUNDER_THREE)
                        break
                    }
                    else{
                        continue
                    }
                }
                CommandEnum.SELECT_NOT -> {
                    break
                }
                else -> {}
            }
        }
    }


    private fun rairaA1CardInit(){
        storm.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        storm.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.STORM_FORCE) {_, player, game_status, _ ->
            stormForce(player, game_status)
            null
        })
        furiousStorm.setEnchantment(0)
        furiousStorm.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) { card_number, player, game_status, _ ->
            for(i in 1..3){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_FURIOUS_STORM)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.gaugeIncreaseRequest(player, NUMBER_RAIRA_WISDOM_OF_STORM_SURGE)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.getPlayer(player).getCardFromPlaying(card_number)?.let {
                                game_status.dustToCard(player, 1, it, card_number)
                            }
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        furiousStorm.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, furiousStormText)
            null
        })
        furiousStorm.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_OTHER){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.RAIRA_FURIOUS_STORM, card_number, CardClass.NULL,
                    sortedSetOf(0, 1, 2, 3, 4), 1,  1, MegamiEnum.RAIRA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                ), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        furiousStorm.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                { buff_player, buff_game_status, buff_attack -> buff_game_status.logger.isThisAttackFirst(buff_player, buff_attack.card_number)
                }, { _, _, madeAttack ->
                madeAttack.lifePlusMinus(-1)
            }))
            null
        })
        jinPungJeCheonUi.setSpecial(2)
        jinPungJeCheonUi.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) {_, player, game_status, _->
            game_status.windGaugeIncrease(player)
            game_status.thunderGaugeIncrease(player)
            game_status.getPlayer(player).notCharge = true
            null
        })
        jinPungJeCheonUi.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _->
            game_status.setShrink(player.opposite())
            null
        })
        jinPungJeCheonUi.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MAIN_PHASE_YOUR) {_, player, game_status, _->
            if(!game_status.getPlayer(player).fullAction){
                stormForce(player, game_status)
                stormForce(player, game_status)
            }
            null
        })
        jinPungJeCheonUi.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_THIS_CARD_RETURN) {_, player, game_status, _->
            game_status.getPlayer(player).notCharge = false
            null
        })
    }

    private val gongSum = CardData(CardClass.NORMAL, CardName.MEGUMI_GONG_SUM, MegamiEnum.MEGUMI, CardType.ATTACK, SubType.NONE)
    private val taCheog = CardData(CardClass.NORMAL, CardName.MEGUMI_TA_CHEOG, MegamiEnum.MEGUMI, CardType.ATTACK, SubType.NONE)
    private val shellAttack = CardData(CardClass.NORMAL, CardName.MEGUMI_SHELL_ATTACK, MegamiEnum.MEGUMI, CardType.ATTACK, SubType.NONE)
    private val poleThrust = CardData(CardClass.NORMAL, CardName.MEGUMI_POLE_THRUST, MegamiEnum.MEGUMI, CardType.ATTACK, SubType.REACTION)
    private val reed = CardData(CardClass.NORMAL, CardName.MEGUMI_REED, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.NONE)
    private val balsam = CardData(CardClass.NORMAL, CardName.MEGUMI_BALSAM, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.NONE)
    private val wildRose = CardData(CardClass.NORMAL, CardName.MEGUMI_WILD_ROSE, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val rootCausality = CardData(CardClass.SPECIAL, CardName.MEGUMI_ROOT_OF_CAUSALITY, MegamiEnum.MEGUMI, CardType.ATTACK, SubType.NONE)
    private val branchPossibility = CardData(CardClass.SPECIAL, CardName.MEGUMI_BRANCH_OF_POSSIBILITY, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.REACTION)
    private val fruitEnd = CardData(CardClass.SPECIAL, CardName.MEGUMI_FRUIT_OF_END, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.NONE)
    private val megumiPalm = CardData(CardClass.SPECIAL, CardName.MEGUMI_MEGUMI_PALM, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.NONE)

    private val wildRoseText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR) { _, player, game_status, _ ->
        game_status.requestAndDoBasicOperation(player, 1906, hashSetOf())
        null
    }


    private val branchPossibilityText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
            true
        }, {attackPlayer, gameStatus, attack ->
            attack.auraPlusMinus(gameStatus.getTotalSeedNumber(attackPlayer))
        }))
        if(game_status.addPreAttackZone(player, MadeAttack(CardName.MEGUMI_BRANCH_OF_POSSIBILITY, card_number, CardClass.NULL,
                sortedSetOf(1, 2, 3, 4, 5), 0,  1, MegamiEnum.MEGUMI,
                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }

    private val fruitEndText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
        game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.LOSE_IMMEDIATE,
            { _, _, _ ->
                true
            }, { _, _, attack ->
                attack.apply {
                    isItValid = true; isItDamage = true
                }
            }))
        if(game_status.addPreAttackZone(player, MadeAttack(CardName.MEGUMI_FRUIT_OF_END, card_number, CardClass.NULL,
                sortedSetOf(5), 5, 5, MegamiEnum.MEGUMI,
                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = true
            ), null) ){
            game_status.afterMakeAttack(card_number, player, null)
        }
        val card = game_status.getPlayer(player).enchantmentCard[card_number]
        game_status.cardToDust(player, card!!.getNap(), card, false, card_number)
        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
        null
    }

    private fun megumiCardInit(){
        gongSum.setAttack(DistanceType.CONTINUOUS, Pair(4, 8), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        gongSum.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, attack ->
                if(gameStatus.getPlayer(nowPlayer).notReadySeed == null || game_status.getPlayer(nowPlayer).notReadySeed == 0){
                    attack.apply {
                        auraPlusMinus(1); lifePlusMinus(1)
                    }
                }
            }))
            null
        })
        taCheog.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        taCheog.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, attack ->
                for(card in gameStatus.getPlayer(nowPlayer).enchantmentCard.values){
                    if(card.getSeedToken() > 0){
                        attack.apply {
                            auraPlusMinus(1); lifePlusMinus(1)
                        }
                        break
                    }
                }
            }))
            null
        })
        shellAttack.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shellAttack.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.ADD_GROWING) {_, player, game_status, _ ->
            game_status.getPlayer(player).nextEnchantmentGrowing += 2
            null
        })
        poleThrust.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        poleThrust.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        poleThrust.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        poleThrust.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_BASIC_OPERATION_INVALID) { _, player, game_status, _ ->
            game_status.getPlayer(player.opposite()).isNextBasicOperationInvalid = true
            null
        })
        reed.setEnchantment(1)
        reed.growing = 1
        reed.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number),
                card_number)
            null
        })
        reed.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) {card_number, player, game_status, _ ->
            game_status.getPlayer(player).enchantmentCard[card_number]!!.getNap()
        })
        reed.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_DISTANCE){card_number, player, game_status, _ ->
            game_status.getPlayer(player).enchantmentCard[card_number]!!.getNap()
        })
        balsam.setEnchantment(1)
        balsam.growing = 2
        balsam.addtext(chasm)
        balsam.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_MAIN_PHASE_YOUR) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.MEGUMI_BALSAM, card_number, CardClass.NULL,
                    sortedSetOf(1, 2, 3), 2,  1, MegamiEnum.MEGUMI,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ), null) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        balsam.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_MAIN_PHASE_OTHER) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.MEGUMI_BALSAM, card_number, CardClass.NULL,
                    sortedSetOf(3, 4, 5), 2,  1, MegamiEnum.MEGUMI,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ), null) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        balsam.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_SHRINK) {_, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })
        wildRose.setEnchantment(0)
        wildRose.growing = 2
        wildRose.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.DO_BASIC_OPERATION) {_, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, 1906)
            null
        })
        wildRose.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR) {card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, wildRoseText)
            null
        })
        wildRose.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_MOVE_TOKEN) {_, player, game_status, _ ->
            if(game_status.turnPlayer == player) 1
            else 0
        })
        wildRose.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN) { card_number, player, game_status, _ ->
            if(!(game_status.getPlayer(player).isMoveDistanceToken)){
                game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number),
                    card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            null
        })
        rootCausality.setSpecial(1)
        rootCausality.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        rootCausality.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.notReadyToReadySeed(player, 1)
            null
        })
        rootCausality.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayer(player).readySeed == 0) 1
            else 0
        })
        branchPossibility.setSpecial(3)
        branchPossibility.setEnchantment(2)
        branchPossibility.growing = 1
        branchPossibility.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {attackPlayer, gameStatus, attack ->
                    attack.auraPlusMinus(gameStatus.getTotalSeedNumber(attackPlayer.opposite()) * -1)
                }))
            null
        })
        branchPossibility.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, branchPossibilityText)
            null
        })
        fruitEnd.setSpecial(3)
        fruitEnd.setEnchantment(2)
        fruitEnd.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_MOVE_TOKEN) {_, _, game_status, _ ->
            if(game_status.nowPhase != GameStatus.MAIN_PHASE) 1
            else 0
        })
        fruitEnd.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.OTHER_CARD_NAP_LOCATION_HERE) {card_number, _, _, _ ->
            card_number
        })
        fruitEnd.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).enchantmentCard[card_number]?.getSeedToken() == 5){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, fruitEndText)
            }
            null
        })
        fruitEnd.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_OTHER) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).enchantmentCard[card_number]?.getSeedToken() == 5){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_OTHER, fruitEndText)
            }
            null
        })
        megumiPalm.setSpecial(3)
        megumiPalm.setEnchantment(0)
        megumiPalm.growing = 5
        megumiPalm.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, condition_attack ->
                condition_attack.getEditedAuraDamage() <= 3
            }, {_, _, madeAttack ->
                madeAttack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        megumiPalm.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_OTHER){card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, condition_attack ->
                condition_attack.getEditedAuraDamage() <= 3
            }, {_, _, madeAttack ->
                madeAttack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        megumiPalm.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
        { card_number, player, game_status, _ ->
            game_status.removeThisTurnAttackBuff(player, BufTag.PLUS_MINUS, card_number)
            null
        })
    }

    private val saljin = CardData(CardClass.IDEA, CardName.IDEA_SAL_JIN, MegamiEnum.KANAWE, CardType.UNDEFINED, SubType.NONE) //9000
    private val sakuraWave = CardData(CardClass.IDEA, CardName.IDEA_SAKURA_WAVE, MegamiEnum.KANAWE, CardType.UNDEFINED, SubType.NONE)
    private val whistle = CardData(CardClass.IDEA, CardName.IDEA_WHISTLE, MegamiEnum.KANAWE, CardType.UNDEFINED, SubType.NONE)
    private val myeongjeon = CardData(CardClass.IDEA, CardName.IDEA_MYEONG_JEON, MegamiEnum.KANAWE, CardType.UNDEFINED, SubType.NONE)
    private val emphasizing = CardData(CardClass.IDEA, CardName.IDEA_EMPHASIZING, MegamiEnum.KANAWE, CardType.UNDEFINED, SubType.NONE)
    private val positioning = CardData(CardClass.IDEA, CardName.IDEA_POSITIONING, MegamiEnum.KANAWE, CardType.UNDEFINED, SubType.NONE)

    private suspend fun activeAct(player: PlayerEnum, game_status: GameStatus){
        when(game_status.getPlayer(player).nowAct?.actColor){
            Act.COLOR_RED -> {
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, Log.ACT_DAMAGE)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, Log.ACT_DAMAGE, -1))
            }
            Act.COLOR_PURPLE -> {
                game_status.requestAndDoBasicOperation(player, 9000)
            }
            Act.COLOR_GREEN -> {
                game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_IDEA_SAL_JIN, 1
                ) { _, _ -> true }?.let {selected ->
                    game_status.popCardFrom(player, selected[0], LocationEnum.COVER_CARD, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                    }
                }
            }
            Act.COLOR_END -> {
                game_status.gameEnd(player, null)
            }
            else -> {}
        }
    }

    private suspend fun nextAct(player: PlayerEnum, game_status: GameStatus, nextAct: Int){
        game_status.setAct(player, nextAct)
        for(card in game_status.getPlayer(player).usedSpecialCard.values){
            card.effectAllValidEffect(player, game_status, TextEffectTag.WHEN_ACT_CHANGE)
        }
        activeAct(player, game_status)
    }

    private suspend fun completeIdea(card_number: Int, player: PlayerEnum, game_status: GameStatus){
        game_status.popCardFrom(player, card_number, LocationEnum.IDEA_YOUR, true)?.let {card ->
            game_status.cardToDust(player, card.getNap(), card, false,
                Log.END_IDEA, LocationEnum.IDEA_YOUR)
            game_status.logger.insert(Log(player, LogText.END_EFFECT, Log.END_IDEA, -1))
            game_status.insertCardTo(player, card, LocationEnum.END_IDEA_YOUR, true)
        }
        val chosenAct = game_status.selectAct(player)
        if(chosenAct != -1){
            nextAct(player, game_status, chosenAct)
        }
    }

    private suspend fun ideaProcess(card_number: Int, player: PlayerEnum, game_status: GameStatus, maxStage: Int){
        game_status.processIdeaStage(player)
        game_status.getPlayer(player).ideaProcess = true
        if(game_status.getPlayer(player).ideaCardStage == maxStage){
            completeIdea(card_number, player, game_status)
        }
    }

    private fun kanaweIdeaInit(){
        saljin.addtext(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){_, _, game_status, _ ->
            if(game_status.logger.checkSaljin(false)){
                1
            }
            else {
                0
            }
        })
        saljin.addtext(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){_, _, game_status, _ ->
            if(game_status.logger.checkSaljin(true)){
                1
            }
            else {
                0
            }
        })
        saljin.addtext(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        saljin.addtext(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
        sakuraWave.addtext(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){_, _, game_status, _ ->
            if(game_status.logger.checkSakuraWave()){
                1
            }
            else {
                0
            }
        })
        sakuraWave.addtext(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){_, _, game_status, _ ->
            if(game_status.logger.checkSakuraWaveFlipped()){
                1
            }
            else {
                0
            }
        })
        sakuraWave.addtext(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
        sakuraWave.addtext(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
        whistle.addtext(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.logger.checkWhistle(false)){
                1
            }
            else {
                0
            }
        })
        whistle.addtext(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.logger.checkWhistle(true)){
                1
            }
            else {
                0
            }
        })
        whistle.addtext(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        whistle.addtext(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
        myeongjeon.addtext(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.logger.checkMyeongJeon(false)){
                1
            }
            else {
                0
            }
        })
        myeongjeon.addtext(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.logger.checkMyeongJeon(true)){
                1
            }
            else {
                0
            }
        })
        myeongjeon.addtext(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        myeongjeon.addtext(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        emphasizing.addtext(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.logger.checkThisTurnUseFullPower() && !(game_status.logger.checkThisTurnIdea(player))){
                1
            }
            else {
                0
            }
        })
        emphasizing.addtext(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.logger.checkThisTurnUseFullPower() && !(game_status.logger.checkThisTurnIdea(player))){
                1
            }
            else {
                0
            }
        })
        emphasizing.addtext(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
        emphasizing.addtext(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        positioning.addtext(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){_, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(abs(game_status.startTurnDistance - nowDistance) >= 2 && nowDistance <= 8){
                1
            }
            else {
                0
            }
        })
        positioning.addtext(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(abs(game_status.startTurnDistance - game_status.getAdjustDistance()) >= 5){
                1
            }
            else {
                0
            }
        })
        positioning.addtext(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        positioning.addtext(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
    }

    private val image = CardData(CardClass.NORMAL, CardName.KANAWE_IMAGE, MegamiEnum.KANAWE, CardType.ATTACK, SubType.NONE) //2000
    private val screenplay = CardData(CardClass.NORMAL, CardName.KANAWE_SCREENPLAY, MegamiEnum.KANAWE, CardType.ATTACK, SubType.NONE)
    private val production = CardData(CardClass.NORMAL, CardName.KANAWE_PRODUCTION, MegamiEnum.KANAWE, CardType.ATTACK, SubType.NONE)
    private val publish = CardData(CardClass.NORMAL, CardName.KANAWE_PUBLISH, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val afterglow = CardData(CardClass.NORMAL, CardName.KANAWE_AFTERGLOW, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val impromptu = CardData(CardClass.NORMAL, CardName.KANAWE_IMPROMPTU, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val seal = CardData(CardClass.NORMAL, CardName.KANAWE_SEAL, MegamiEnum.KANAWE, CardType.ENCHANTMENT, SubType.NONE)
    private val vagueStory = CardData(CardClass.SPECIAL, CardName.KANAWE_VAGUE_STORY, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val infiniteStarlight = CardData(CardClass.SPECIAL, CardName.KANAWE_INFINITE_STARLIGHT, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val bendOverThisNight = CardData(CardClass.SPECIAL, CardName.KANAWE_BEND_OVER_THIS_NIGHT, MegamiEnum.KANAWE, CardType.ATTACK, SubType.REACTION)
    private val distantSky = CardData(CardClass.SPECIAL, CardName.KANAWE_DISTANT_SKY, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val kanawe = CardData(CardClass.SPECIAL, CardName.KANAWE_KANAWE, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)

    private val screenPlayText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD) { card_number, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_SCREENPLAY)){
                CommandEnum.SELECT_ONE -> {
                    game_status.popCardFrom(player, card_number, LocationEnum.DISCARD_YOUR, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                    }
                    game_status.addConcentration(player.opposite())
                    break
                }
                CommandEnum.SELECT_NOT -> {
                    break
                }
                else -> {}
            }
        }
        null
    }

    private val vagueStoryText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_START_PHASE_OTHER) { card_number, player, game_status, _ ->
        game_status.returnSpecialCard(player, card_number)
        null
    }

    private fun getActValue(player: PlayerEnum, game_status: GameStatus) = game_status.getPlayer(player).nowAct?.actValue ?: 0

    private fun getActColor(player: PlayerEnum, game_status: GameStatus) = game_status.getPlayer(player).nowAct?.actColor ?: 0

    private fun makeIdeaList(player: PlayerEnum, game_status: GameStatus, from: LocationEnum): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        val nowPlayer = game_status.getPlayer(player)
        if(from == LocationEnum.ALL || from == LocationEnum.ADDITIONAL_CARD){
            nowPlayer.additionalHand[CardName.IDEA_SAL_JIN]?.let {
                cardList.add(it.card_number)
            }
            nowPlayer.additionalHand[CardName.IDEA_SAKURA_WAVE]?.let {
                cardList.add(it.card_number)
            }
            nowPlayer.additionalHand[CardName.IDEA_WHISTLE]?.let {
                cardList.add(it.card_number)
            }
            nowPlayer.additionalHand[CardName.IDEA_MYEONG_JEON]?.let {
                cardList.add(it.card_number)
            }
            nowPlayer.additionalHand[CardName.IDEA_POSITIONING]?.let {
                cardList.add(it.card_number)
            }
            nowPlayer.additionalHand[CardName.IDEA_EMPHASIZING]?.let {
                cardList.add(it.card_number)
            }
        }

        if(from == LocationEnum.ALL || from == LocationEnum.END_IDEA_YOUR){
            for(card in nowPlayer.endIdeaCards.keys){
                cardList.add(card)
            }
        }
        return cardList
    }

    private suspend fun readyIdea(player: PlayerEnum, game_status: GameStatus, from: LocationEnum): Boolean{
        val nowPlayer = game_status.getPlayer(player)
        if(game_status.dust == 0 && game_status.getPlayerAura(player) == 0 && nowPlayer.ideaCard == null){
            return false
        }
        val cardList = makeIdeaList(player, game_status, from)
        if(cardList.size != 0){
            while(true){
                val get = game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KANAWE_SCREENPLAY)
                if(get.size == 0) return false
                if(get.size != 1) continue

                var newFrom: LocationEnum = LocationEnum.ADDITIONAL_CARD
                val idea = game_status.getCardFrom(player, get[0], LocationEnum.ADDITIONAL_CARD)?.apply {
                    newFrom = LocationEnum.ADDITIONAL_CARD
                }?: game_status.getCardFrom(player, get[0], LocationEnum.END_IDEA_YOUR)?.apply {
                    newFrom = LocationEnum.END_IDEA_YOUR
                }?: return false

                nowPlayer.ideaCardStage = 0

                if(nowPlayer.ideaCard != null){
                    game_status.popCardFrom(player, -1, LocationEnum.IDEA_YOUR, true)?.let {beforeIdea ->
                        game_status.cardToDust(player, beforeIdea.getNap(), beforeIdea, false,
                            Log.END_IDEA, LocationEnum.IDEA_YOUR)
                        game_status.logger.insert(Log(player, LogText.END_EFFECT, Log.END_IDEA, -1))
                        game_status.insertCardTo(player, beforeIdea, LocationEnum.ADDITIONAL_CARD, true)
                    }
                }

                if(newFrom == LocationEnum.ADDITIONAL_CARD){
                    game_status.moveAdditionalCard(player, idea.card_data.card_name, LocationEnum.IDEA_YOUR)
                }
                else{
                    game_status.popCardFrom(player, idea.card_number, LocationEnum.END_IDEA_YOUR, true)?.let{
                        game_status.insertCardTo(player, it, LocationEnum.IDEA_YOUR, true)
                    }
                }

                while(true){
                    when(game_status.receiveCardEffectSelect(player, idea.card_number)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.getPlayer(player).isIdeaCardFlipped = false
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.getPlayer(player).isIdeaCardFlipped = true
                            game_status.sendCommand(player, player.opposite(), CommandEnum.SET_IDEA_FLIP_YOUR)
                            break
                        }
                        else -> {}
                    }
                }

                if(game_status.dust == 0){
                    game_status.auraToCard(player, 1, idea, idea.card_number, LocationEnum.IDEA_YOUR)

                }
                else if(game_status.getPlayerAura(player) == 0){
                    game_status.dustToCard(player, 1, idea, idea.card_number, LocationEnum.IDEA_YOUR)
                }
                else{
                    while(true){
                        when(game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_PRODUCTION)){
                            CommandEnum.SELECT_ONE -> {
                                game_status.dustToCard(player, 1, idea, idea.card_number, LocationEnum.IDEA_YOUR)
                                break
                            }
                            CommandEnum.SELECT_TWO -> {
                                game_status.auraToCard(player, 1, idea, idea.card_number, LocationEnum.IDEA_YOUR)
                                break
                            }
                            else -> {}
                        }
                    }
                }

                game_status.logger.insert(Log(player, LogText.END_EFFECT, idea.card_number, -1))
                break
            }
        }

        return true
    }

    private fun kanaweCardInit(){
        image.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1000, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        image.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.apply {
                    val now = getActValue(nowPlayer, gameStatus)
                    if (now % 2 == 0){
                        madeAttack.tempEditedAuraDamage = now / 2
                    }
                    else{
                        madeAttack.tempEditedLifeDamage = (now + 1) / 2
                    }
                }
            }))
            null
        })
        image.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(getActValue(player, game_status) % 2 == 0){
                while(true){
                    val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_IMAGE)
                    if(selectDustToDistance(nowCommand, game_status, player,
                            game_status.getCardOwner(card_number), card_number)) break
                }
            }
            null
        })
        screenplay.setAttack(DistanceType.CONTINUOUS, Pair(2, 8), null, 0, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        screenplay.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            readyIdea(player, game_status, LocationEnum.ADDITIONAL_CARD)
            null
        })
        screenplay.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD) { card_number, player, game_status, _ ->
            if(game_status.getCardFrom(player, card_number, LocationEnum.DISCARD_YOUR)?.isSoftAttack == false){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.DISCARD_YOUR, screenPlayText)
            }
            null
        })
        production.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        production.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                if(getActColor(nowPlayer, gameStatus) == Act.COLOR_PURPLE){
                    madeAttack.lifePlusMinus(1)
                }
            }))
            null
        })
        production.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            game_status.getPlayer(player).canIdeaProcess = false
            null
        })
        publish.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_PUBLISH)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 2003)
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
            game_status.requestAndDoBasicOperation(player, NUMBER_KANAWE_PUBLISH, hashSetOf(CommandEnum.ACTION_WIND_AROUND))
            null
        })
        publish.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                readyIdea(player, game_status, LocationEnum.ALL)
                game_status.setShrink(player.opposite())
            }
            null
        })
        afterglow.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _ ->
            val actColor = getActColor(player, game_status)
            if(actColor == Act.COLOR_PURPLE || actColor == Act.COLOR_GREEN){
                game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_AFTERGLOW, 1
                ) { _, _ -> true }?.let {selected ->
                    game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, true)
                    }
                }
                game_status.addConcentration(player.opposite())
            }
            null
        })
        impromptu.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) {_, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, 2005
                ) { card, _ ->
                    card.card_data.megami != MegamiEnum.KANAWE && card.card_data.sub_type != SubType.FULL_POWER
                            && card.card_data.card_type == CardType.ATTACK
                }?: break
                if(selected.size == 0){
                    break
                }
                else if(selected.size == 1){
                    val selectNumber = selected[0]
                    game_status.getCardFrom(player, selectNumber, LocationEnum.HAND)?.let { card ->
                        game_status.useCardFrom(player, card, LocationEnum.HAND, false, react_attack,
                            isCost = true, isConsume = true)
                        if(getActColor(player, game_status) == Act.COLOR_GREEN){
                            if(!(card.isSoftAttack)){
                                game_status.popCardFrom(player, card.card_number, LocationEnum.DISCARD_YOUR, true)?.let {
                                    game_status.insertCardTo(player, it, LocationEnum.HAND, true)
                                }
                            }
                        }
                    }
                    break
                }
                else{
                    continue
                }
            }
            null
        })
        seal.setEnchantment(3)
        seal.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CAN_NOT_USE_CARD)
        ret@{ card_number, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.ALL),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_SEAL, 1
            ) { _, _ -> true }?.let { selected ->
                val sealedPlayer = game_status.getPlayer(player.opposite())
                if(sealedPlayer.canNotUseCardName1 == null){
                    sealedPlayer.canNotUseCardName1 = Pair(card_number, selected[0].toCardName())
                }
                else{
                    sealedPlayer.canNotUseCardName2 = Pair(card_number, selected[0].toCardName())
                }
            }
            null
        })
        seal.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
        { card_number, player, game_status, _ ->
            val sealedPlayer = game_status.getPlayer(player.opposite())
            if(sealedPlayer.canNotUseCardName1?.first == card_number){
                sealedPlayer.canNotUseCardName1 = sealedPlayer.canNotUseCardName2
                sealedPlayer.canNotUseCardName2 = null
            }
            else if(sealedPlayer.canNotUseCardName2?.first == card_number){
                sealedPlayer.canNotUseCardName2 = null
            }
            null
        })
        seal.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.getConcentration(player)
            null
        })
        vagueStory.setSpecial(1)
        vagueStory.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)
            if(nowPlayer.concentration >= 1){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_VAGUE_STORY)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.setConcentration(player, nowPlayer.concentration - 1)
                            readyIdea(player, game_status, LocationEnum.ADDITIONAL_CARD)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.setConcentration(player, nowPlayer.concentration - 1)
                            if(readyIdea(player, game_status, LocationEnum.END_IDEA_YOUR)){
                                game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
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
        vagueStory.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_OTHER){card_number, player, game_status, _ ->
            if(!(game_status.getPlayer(player).beforeTurnIdeaProcess)){
                game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_OTHER, vagueStoryText)
            }

            null
        })
        infiniteStarlight.setSpecial(null)
        infiniteStarlight.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_X) {_, player, game_status, _->
            getActValue(player, game_status)
        })
        infiniteStarlight.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.EFFECT_ACT) {_, player, game_status, _ ->
            activeAct(player, game_status)
            null
        })
        infiniteStarlight.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_ACT_CHANGE){card_number, player, game_status, _ ->
            game_status.returnSpecialCard(player, card_number)
            null
        })
        bendOverThisNight.setSpecial(4)
        bendOverThisNight.setAttack(DistanceType.CONTINUOUS, Pair(0, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        bendOverThisNight.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_INVALID) {card_number, player, game_status, reactedAttack ->
            if(reactedAttack?.card_class != CardClass.SPECIAL){
                reactedAttack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            if(reactedAttack?.card_class == CardClass.NORMAL){
                game_status.movePlayingCard(player.opposite(), LocationEnum.YOUR_DECK_TOP, reactedAttack.card_number)
            }
            null
        })
        distantSky.setSpecial(2)
        distantSky.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            while(true) {
                val selected = game_status.selectCardFrom(player, player, player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_INFINITE_STARLIGHT
                ) { card, _ -> card.card_data.card_class == CardClass.NORMAL }?: break
                if(selected.size == 0){
                    break
                }
                else if(selected.size == 1){
                    game_status.popCardFrom(player, selected[0], LocationEnum.HAND, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
                    }

                    game_status.selectCardFrom(player, player, player, listOf(LocationEnum.NOT_SELECTED_NORMAL),
                        CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_BEND_OVER_THIS_NIGHT, 1){_, _ -> true }?.let { normal ->
                        game_status.getPlayer(player).unselectedCard.remove(normal[0].toCardName())
                        game_status.insertCardTo(player,
                            Card.cardMakerByName(game_status.getPlayer(player).firstTurn, normal[0].toCardName(), player),
                        LocationEnum.HAND, true)
                    }
                    break
                }
                else{
                    continue
                }
            }

            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)

            game_status.selectCardFrom(player, player, player, listOf(LocationEnum.NOT_SELECTED_SPECIAL),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_DISTANT_SKY, 1){ _, _ -> true }?.let { special ->
                game_status.getPlayer(player).unselectedSpecialCard.remove(special[0].toCardName())
                game_status.insertCardTo(player,
                    Card.cardMakerByName(game_status.getPlayer(player).firstTurn, special[0].toCardName(), player),
                    LocationEnum.SPECIAL_CARD, true)
            }
            null
        })
        kanawe.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).nowAct?.actColor == Act.COLOR_GOLD){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, condition_attack ->
                    condition_attack.card_class != CardClass.NULL
                }, {_, _, attack ->
                    attack.lifePlusMinus(1)
                }))
            }
            null
        })
    }

    private val passingFear = CardData(CardClass.NORMAL, CardName.TOKOYO_PASSING_FEAR, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.REACTION) //414
    private val relicEye = CardData(CardClass.SPECIAL, CardName.TOKOYO_RELIC_EYE, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val eightSakuraInVain = CardData(CardClass.SPECIAL, CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)
    private val eightSakuraInVainText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR) {card_number, player, game_status, _->
        if(game_status.getPlayer(player).aura >= 6){
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN, card_number, CardClass.NULL,
                    sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8), 999, 1, MegamiEnum.TOKOYO,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                ), null) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
        }
        null
    }

    private fun tokoyoA2CardInit(){
        passingFear.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        passingFear.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.SELECT_DAMAGE_BY_ATTACKER) {_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).concentration == 0){
                1
            }
            else{
                0
            }
        })
        passingFear.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, _, _, reactedAttack ->
            reactedAttack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, gameStatus, madeAttack ->
                    madeAttack.apply {
                        val (aura, life) = gameStatus.logger.findGetDamageByThisAttack(card_number)
                        madeAttack.apply {
                            auraPlusMinus(aura * -1); lifePlusMinus(life * -1)
                        }
                    }
                }))
            null
        })
        relicEye.setSpecial(1)
        relicEye.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        relicEye.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { card_number, player, game_status, _ ->
            game_status.flareToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        relicEye.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).concentration == 1) 1
            else 0
        })
        eightSakuraInVain.setSpecial(4)
        eightSakuraInVain.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION){_, player, game_status, _->
            for(i in 1..5){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 201416)
            }
            null
        })
        eightSakuraInVain.addTextUnfold(Text(TextEffectTimingTag.USED, TextEffectTag.TOKOYO_EIGHT_SAKURA) {_, _, _, _->
            1
        })
        eightSakuraInVain.addTextUnfold(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR) {card_number, _, game_status, _->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, eightSakuraInVainText)
            null
        })
    }

    private val sakuraSword = CardData(CardClass.NORMAL, CardName.HONOKA_SAKURA_SWORD, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val shadowHand = CardData(CardClass.NORMAL, CardName.HONOKA_SHADOW_HAND, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val eyeOpenAlone = CardData(CardClass.SPECIAL, CardName.HONOKA_EYE_OPEN_ALONE, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val followTrace = CardData(CardClass.NORMAL, CardName.HONOKA_FOLLOW_TRACE, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val facingShadow = CardData(CardClass.NORMAL, CardName.HONOKA_FACING_SHADOW, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val sakuraShiningBrightly = CardData(CardClass.SPECIAL, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, MegamiEnum.HONOKA, CardType.ATTACK, SubType.NONE)
    private val holdHands = CardData(CardClass.SPECIAL, CardName.HONOKA_HOLD_HANDS, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)
    private val walkOldLoad = CardData(CardClass.SPECIAL, CardName.HONOKA_WALK_OLD_LOAD, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.NONE)

    private fun honokaA1CardInit(){
        sakuraSword.setAttack(DistanceType.CONTINUOUS, Pair(4, 6), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        sakuraSword.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        sakuraSword.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_SAKURA_SWORD)){
                game_status.getCardFrom(player, CardName.HONOKA_SHADOW_HAND, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_SHADOW_HAND, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_SHADOW_HAND, LocationEnum.DISCARD_YOUR)
                    }
                }
            }
            null
        })
        shadowHand.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 1, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shadowHand.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { _, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.HAND),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_HONOKA_SHADOW_HAND, 1)
            { _, _ -> true }?.let { selected ->
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }
            null
        })
        shadowHand.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) {card_number, player, game_status, _ ->
            game_status.flareToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        shadowHand.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GET_ADDITIONAL_CARD) {card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_SHADOW_HAND)){
                game_status.getCardFrom(player, CardName.HONOKA_SAKURA_SWORD, LocationEnum.ADDITIONAL_CARD)?.let {
                    game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                    if(requestDeckBelow(player, game_status)){
                        game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_SWORD, LocationEnum.YOUR_DECK_BELOW)
                    }
                    else{
                        game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_SWORD, LocationEnum.DISCARD_YOUR)
                    }
                }
            }
            null
        })
        eyeOpenAlone.setSpecial(3)
        eyeOpenAlone.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION,
                player, game_status.getCardOwner(card_number), card_number)
            null
        })
        eyeOpenAlone.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_EYE_OPEN_ALONE)){
                if(game_status.dust <= 5){
                    game_status.getCardFrom(player, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.ADDITIONAL_CARD)?.let{
                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                        game_status.moveAdditionalCard(player, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.YOUR_DECK_BELOW)
                    }
                }
                else{
                    game_status.getCardFrom(player, CardName.HONOKA_FACING_SHADOW, LocationEnum.ADDITIONAL_CARD)?.let{
                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                        game_status.moveAdditionalCard(player, CardName.HONOKA_FACING_SHADOW, LocationEnum.YOUR_DECK_BELOW)
                    }
                }
            }
            null
        })
        followTrace.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){_, player, game_status, _ ->
            val firstCommand = game_status.requestAndDoBasicOperation(player, 1421)
            game_status.requestAndDoBasicOperation(player, 1421, hashSetOf(firstCommand))
            null
        })
        followTrace.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_FOLLOW_TRACE)){
                if(game_status.dust == 0){
                    game_status.getCardFrom(player, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, LocationEnum.ADDITIONAL_CARD)?.let{
                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                        game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, LocationEnum.YOUR_USED_CARD)
                        game_status.returnSpecialCard(player, it.card_number)
                    }
                }
                else{
                    game_status.getCardFrom(player, CardName.HONOKA_HOLD_HANDS, LocationEnum.ADDITIONAL_CARD)?.let{
                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                        game_status.moveAdditionalCard(player, CardName.HONOKA_HOLD_HANDS, LocationEnum.YOUR_USED_CARD)
                        game_status.returnSpecialCard(player, it.card_number)
                    }
                }
            }
            null
        })
        facingShadow.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player.opposite(), NUMBER_HONOKA_FACING_SHADOW)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.flareToDust(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.lifeToDust(player.opposite(),1, Arrow.ONE_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        facingShadow.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_FACING_SHADOW)){
                if(game_status.dust >= 12){
                    game_status.getCardFrom(player, CardName.HONOKA_WALK_OLD_LOAD, LocationEnum.ADDITIONAL_CARD)?.let{
                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                        game_status.moveAdditionalCard(player, CardName.HONOKA_WALK_OLD_LOAD, LocationEnum.YOUR_USED_CARD)
                        game_status.returnSpecialCard(player, it.card_number)
                    }
                }
                else{
                    game_status.getCardFrom(player, CardName.HONOKA_HOLD_HANDS, LocationEnum.ADDITIONAL_CARD)?.let{
                        game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                        game_status.moveAdditionalCard(player, CardName.HONOKA_HOLD_HANDS, LocationEnum.YOUR_USED_CARD)
                        game_status.returnSpecialCard(player, it.card_number)
                    }
                }
            }
            null
        })
        sakuraShiningBrightly.setSpecial(1)
        sakuraShiningBrightly.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 1000, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = true)
        sakuraShiningBrightly.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                val damage = gameStatus.getCardFrom(nowPlayer, madeAttack.card_number, LocationEnum.PLAYING_ZONE_YOUR)
                    ?.getNap() ?: 0
                madeAttack.tempEditedAuraDamage = damage
            }))
            null
        })
        sakuraShiningBrightly.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                game_status.dustToCard(player, 1, it, Log.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
            }
            null
        })
        sakuraShiningBrightly.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){card_number, player, game_status, _ ->
            if(countTokenFive(game_status) >= 1) {
                game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                    game_status.logger.insert(Log(player, LogText.MOVE_TOKEN, card_number, it.getNap()?: 0,
                        LocationEnum.YOUR_USED_CARD, LocationEnum.SPECIAL_CARD, false))
                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                }
                1
            }
            else 0
        })
        holdHands.setSpecial(5)
        holdHands.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.auraToAura(player.opposite(), player, 5, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        walkOldLoad.setSpecial(3)
        walkOldLoad.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.PHASE_SKIP){card_number, player, game_status, _ ->
            game_status.getPlayer(player.opposite()).nextMainPhaseSkip = true
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })
    }

    private val bonfire = CardData(CardClass.NORMAL, CardName.HAGANE_BONFIRE, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val wheelSkill = CardData(CardClass.NORMAL, CardName.HAGANE_WHEEL_SKILL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val grandSoftMaterial = CardData(CardClass.SPECIAL, CardName.HAGANE_GRAND_SOFT_MATERIAL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val softAttack = CardData(CardClass.NORMAL, CardName.HAGANE_SOFT_ATTACK, MegamiEnum.HAGANE, CardType.UNDEFINED, SubType.NONE)

    private val softAttackText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
        when(game_status.getPlayer(player).anvil?.getNap()?: 0){
            0 -> {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true
                }, {_, _, madeAttack ->
                    madeAttack.auraPlusMinus(1)
                }))
                game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE, {_, _, _ -> true},
                    { _, _, attack -> attack.plusMinusRange(1, false)
                    }))
            }
            1 -> {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true
                }, {_, _, madeAttack ->
                    madeAttack.apply {
                        auraPlusMinus(2); lifePlusMinus(1)
                    }
                }))
                game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE, {_, _, _ -> true},
                    { _, _, attack -> attack.plusMinusRange(2, false)
                    }))
            }
            2 -> {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true
                }, {_, _, madeAttack ->
                    madeAttack.apply {
                        auraPlusMinus(3); lifePlusMinus(2)
                    }
                }))
                game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE,
                    {_, _, _ ->
                        true
                    }, { _, _, attack -> attack.plusMinusRange(3, false)
                    }))
                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.LOSE_IMMEDIATE,
                    { _, _, _ ->
                        true
                    }, { _, _, attack ->
                        attack.apply {
                            isItValid = true; isItDamage = true
                        }
                    }))

            }
        }
        null
    }

    private fun duplicateCardDataForMaterial(card_data: CardData, card_name: CardName, megamiEnum: MegamiEnum): CardData{
        val result = CardData(card_data.card_class, card_name, megamiEnum, card_data.card_type, card_data.sub_type)
        result.run {
            umbrellaMark = card_data.umbrellaMark

            effectFold = if(card_data.effectFold == null) null
            else mutableListOf<Text>().apply {
                card_data.effectFold?.let {
                    for (text in it){
                        this.add(text)
                    }
                }
            }

            effectUnfold = if(card_data.effectUnfold == null) null
            else mutableListOf<Text>().apply {
                card_data.effectUnfold?.let {
                    for (text in it){
                        this.add(text)
                    }
                }
            }

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

            growing = card_data.growing

            distance_type = card_data.distance_type
            distance_cont = card_data.distance_cont
            distance_uncont = card_data.distance_uncont
            life_damage =  card_data.life_damage
            aura_damage = card_data.aura_damage

            charge = card_data.charge

            cost = card_data.cost

            effect = mutableListOf<Text>().apply {
                card_data.effect?.let {
                    for (text in it){
                        this.add(text)
                    }
                }
            }
            canCover = card_data.canCover
            canDiscard = card_data.canDiscard
        }
        return result
    }

    private fun haganeA1CardInit(){
        bonfire.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() <= 3){
                game_status.distanceToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
                game_status.distanceToFlare(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        wheelSkill.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.DRAW_CARD){_, player, game_status, _ ->
            if(abs(game_status.getAdjustDistance() - game_status.startTurnDistance) >= 2){
                game_status.drawCard(player, 1)
                game_status.getConcentration(player)
            }
            null
        })
        grandSoftMaterial.setSpecial(1)
        grandSoftMaterial.addtext(termination)
        grandSoftMaterial.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){card_number, player, game_status, _ ->
            if(player == game_status.getCardOwner(card_number)){
                if(game_status.getPlayer(player).anvil == null){
                    while(true){
                        val list = game_status.selectCardFrom(player, player, player, listOf(LocationEnum.HAND),
                            CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_HAGANE_GRAND_SOFT_MATERIAL
                        ) { card, _ -> card.card_data.card_type == CardType.ATTACK && card.card_data.megami != MegamiEnum.HAGANE }?: break
                        if (list.size == 1){
                            game_status.popCardFrom(player, list[0], LocationEnum.HAND, true)?.let { selectedAttack ->
                                game_status.insertCardTo(player, selectedAttack, LocationEnum.ANVIL_YOUR, true)
                                game_status.popCardFrom(player, CardName.HAGANE_SOFT_ATTACK, LocationEnum.ADDITIONAL_CARD, true)?.let {
                                    softenAttack ->
                                    softenAttack.card_data = duplicateCardDataForMaterial(selectedAttack.card_data,
                                        CardName.HAGANE_SOFT_ATTACK, MegamiEnum.HAGANE)
                                    softenAttack.card_data.addtext(softAttackText)
                                    softenAttack.isSoftAttack = true
                                    game_status.insertCardTo(player, softenAttack, LocationEnum.YOUR_DECK_BELOW, true)
                                }
                            }
                            break
                        }
                        else if(list.size == 0){
                            break
                        }
                        else{
                            continue
                        }
                    }
                }
                else{
                    game_status.dustToAnvil(player, 1)
                }
            }
            null
        })
        grandSoftMaterial.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(game_status.logger.checkThisTurnUseCard(player) { card -> card.toCardName() == CardName.HAGANE_SOFT_ATTACK }) 1
            else 0
        })
    }

    private val redBlade = CardData(CardClass.NORMAL, CardName.KAMUWI_RED_BLADE, MegamiEnum.KAMUWI, CardType.ATTACK, SubType.NONE)
    private val flutteringBlade = CardData(CardClass.NORMAL, CardName.KAMUWI_FLUTTERING_BLADE, MegamiEnum.KAMUWI, CardType.ATTACK, SubType.NONE)
    private val siKenLanJin = CardData(CardClass.NORMAL, CardName.KAMUWI_SI_KEN_LAN_JIN, MegamiEnum.KAMUWI, CardType.ATTACK, SubType.NONE)
    private val cutDown = CardData(CardClass.NORMAL, CardName.KAMUWI_CUT_DOWN, MegamiEnum.KAMUWI, CardType.ATTACK, SubType.REACTION)
    private val threadingThorn = CardData(CardClass.NORMAL, CardName.KAMUWI_THREADING_THORN, MegamiEnum.KAMUWI, CardType.BEHAVIOR, SubType.NONE)
    private val keSyoLanLyu = CardData(CardClass.NORMAL, CardName.KAMUWI_KE_SYO_LAN_LYU, MegamiEnum.KAMUWI, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val bloodWave = CardData(CardClass.NORMAL, CardName.KAMUWI_BLOOD_WAVE, MegamiEnum.KAMUWI, CardType.ENCHANTMENT, SubType.NONE)
    private val lamp = CardData(CardClass.SPECIAL, CardName.KAMUWI_LAMP, MegamiEnum.KAMUWI, CardType.BEHAVIOR, SubType.NONE)
    private val dawn = CardData(CardClass.SPECIAL, CardName.KAMUWI_DAWN, MegamiEnum.KAMUWI, CardType.ATTACK, SubType.NONE)
    private val graveYard = CardData(CardClass.SPECIAL, CardName.KAMUWI_GRAVEYARD, MegamiEnum.KAMUWI, CardType.ENCHANTMENT, SubType.NONE)
    private val kataShiro = CardData(CardClass.SPECIAL, CardName.KAMUWI_KATA_SHIRO, MegamiEnum.KAMUWI, CardType.ATTACK, SubType.REACTION)
    private val logic = CardData(CardClass.SPECIAL, CardName.KAMUWI_LOGIC, MegamiEnum.KAMUWI, CardType.BEHAVIOR, SubType.NONE)

    private fun kamuwiCardInit(){
        redBlade.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        redBlade.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK) {card_number, player, game_status, _->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_RED_BLADE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                                true
                            }, {_, _, attack ->
                                attack.apply {
                                    auraPlusMinus(1); lifePlusMinus(1)
                                }
                            }))
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        flutteringBlade.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        flutteringBlade.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_FLUTTERING_BLADE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            if(game_status.getPlayer(player).aura <= 4){
                                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(1, 999), false,
                                    null, null, card_number)
                                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                            }
                            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_KAMUWI_FLUTTERING_BLADE)
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        siKenLanJin.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        siKenLanJin.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.getFullAction(player) && game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_SI_KEN_LAN_JIN)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            for (i in 1..3){
                                if(game_status.addPreAttackZone(player, MadeAttack(CardName.KAMUWI_SI_KEN_LAN_JIN, card_number, CardClass.NULL,
                                        sortedSetOf(2, 3, 4), 1,  1, MegamiEnum.KAMUWI,
                                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                                    game_status.afterMakeAttack(card_number, player, null)
                                }
                            }
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        cutDown.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        cutDown.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, _, _, react_attack->
            if((react_attack != null && react_attack.isItReact)) 1
            else 0
        })
        cutDown.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_NO_DAMAGE){card_number, player, game_status, react_attack ->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_CUT_DOWN)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 2)
                            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
                                { _, _, _ ->
                                true
                            }, { _, _, attack ->
                                attack.makeNoDamage()
                            }))
                            if(react_attack?.card_class == CardClass.SPECIAL || react_attack?.subType == SubType.FULL_POWER){
                                game_status.tabooGaugeIncrease(player, 2)
                            }
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        threadingThorn.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            val distance = game_status.getAdjustDistance()
            if(distance >= 5){
                game_status.distanceToDust(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            else if(distance <= 1){
                game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            else{
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, condition_attack ->
                    (condition_attack.megami != MegamiEnum.KAMUWI) && (condition_attack.card_class != CardClass.SPECIAL)
                }, {_, _, attack ->
                    attack.auraPlusMinus(1)
                }))
                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET, { _, _, condition_attack ->
                    (condition_attack.megami != MegamiEnum.KAMUWI) && (condition_attack.card_class != CardClass.SPECIAL)},
                    { _, _, attack ->
                        attack.canNotReactNormal()
                    })
                )
            }
            null
        })
        keSyoLanLyu.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) { card_number, player, game_status, _ ->
            var (selectOne, selectTwo, selectThree, selectFour) = listOf(false, false, false, false)
            var firstCommand: CommandEnum
            var secondCommand: CommandEnum

            while(true){
                firstCommand = game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_KE_SYO_LAN_LYU)
                when(firstCommand){
                    CommandEnum.SELECT_ONE, CommandEnum.SELECT_TWO, CommandEnum.SELECT_THREE, CommandEnum.SELECT_FOUR
                    -> {
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }

            while(true){
                secondCommand = game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_KE_SYO_LAN_LYU)
                when(secondCommand){
                    firstCommand -> {
                        continue
                    }
                    CommandEnum.SELECT_ONE, CommandEnum.SELECT_TWO, CommandEnum.SELECT_THREE, CommandEnum.SELECT_FOUR
                    -> {
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }

            when(firstCommand){
                CommandEnum.SELECT_ONE -> selectOne = true
                CommandEnum.SELECT_TWO -> selectTwo = true
                CommandEnum.SELECT_THREE -> selectThree = true
                CommandEnum.SELECT_FOUR -> selectFour = true
                else -> {}
            }

            when(secondCommand){
                CommandEnum.SELECT_ONE -> selectOne = true
                CommandEnum.SELECT_TWO -> selectTwo = true
                CommandEnum.SELECT_THREE -> selectThree = true
                CommandEnum.SELECT_FOUR -> selectFour = true
                else -> {}
            }

            if(selectOne){
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.KAMUWI_KE_SYO_LAN_LYU, card_number, CardClass.NULL,
                        sortedSetOf(5, 6, 7, 8, 9), 4,  1, MegamiEnum.KAMUWI,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    ), null) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }

            if(selectTwo){
                if(game_status.getAdjustDistance() >= 5){
                    game_status.distanceToDust(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
                }
            }

            if(selectThree){
                if(game_status.addPreAttackZone(player, MadeAttack(CardName.KAMUWI_KE_SYO_LAN_LYU, card_number, CardClass.NULL,
                        sortedSetOf(2, 3, 4), 2,  2, MegamiEnum.KAMUWI,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    ), null) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }

            if(selectFour){
                game_status.dustToAura(player, 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        bloodWave.setEnchantment(2)
        bloodWave.addtext(chasm)
        bloodWave.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_BLOOD_WAVE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            if(game_status.addPreAttackZone(player, MadeAttack(CardName.KAMUWI_BLOOD_WAVE, card_number, CardClass.NULL,
                                    sortedSetOf(3), 2,  2, MegamiEnum.KAMUWI,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = true
                                ), null) ){
                                game_status.afterMakeAttack(card_number, player, null)
                            }
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        bloodWave.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_GET_AURA_OTHER) {_, _, _, _ ->
            1
        })
        bloodWave.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_GET_AURA_OTHER_AFTER) {card_number, player, game_status, _ ->
            game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?.let {card ->
                game_status.cardToDust(player, 1, card, false, card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                if(card.isItDestruction()){
                    game_status.enchantmentDestruction(player, card)
                }
            }
            1
        })
        lamp.setSpecial(5)
        lamp.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_LAMP)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.tabooGaugeIncrease(player, 3)
                        game_status.flareToDust(player, game_status.getPlayerFlare(player), Arrow.NULL, player,
                            game_status.getCardOwner(card_number), card_number)
                        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                        game_status.processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                            null, null, card_number)
                        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                        game_status.moveAdditionalCard(player, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {}
                }
            }
            null
        })
        dawn.setSpecial(6)
        dawn.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 6, 4,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = true)
        dawn.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_REACTED) { card_number, player, game_status, this_attack ->
            game_status.getCardFrom(player.opposite(), card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {react_card ->
                if(react_card.card_data.card_class == CardClass.NORMAL){
                    this_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                        {_, _, attack ->
                            attack.auraPlusMinus(-1)
                        }))
                }

                if(react_card.card_data.card_class == CardClass.SPECIAL){
                    this_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                        {_, _, attack ->
                            attack.apply {
                                auraPlusMinus(-1); lifePlusMinus(-1)
                            }
                        }))
                }
            }
            1
        })
        dawn.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })
        graveYard.setSpecial(3)
        graveYard.setEnchantment(4)
        graveYard.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_GRAVEYARD)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 2)
                            if(game_status.addPreAttackZone(player, MadeAttack(CardName.KAMUWI_GRAVEYARD, card_number, CardClass.NULL,
                                    sortedSetOf(3, 4), 3,  3, MegamiEnum.KAMUWI,
                                    cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = false
                                ), null) ){
                                game_status.afterMakeAttack(card_number, player, null)
                            }
                            game_status.processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                                null, null, card_number)
                            game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })
        graveYard.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_WIN) {_, _, _, _ ->
            1
        })
        graveYard.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE) {_, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).life == 0){
                game_status.gameEnd(player, player.opposite())
            }
            null
        })
        kataShiro.setSpecial(1)
        kataShiro.setAttack(DistanceType.CONTINUOUS, Pair(0, 6), null, 0, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kataShiro.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.ADD_COST) {card_number, player, game_status, react_attack ->
            game_status.getPlayer(player.opposite()).nextCostAddMegami = react_attack?.megami
            null
        })
        kataShiro.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_TABOO_CHANGE){card_number, player, game_status, _ ->
            if((game_status.getPlayer(player).tabooGauge?: 1) % 6 == 0){
                game_status.returnSpecialCard(player, card_number)
            }
            null
        })
        logic.setSpecial(3)
        logic.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, player, game_status, _ ->
            if(game_status.getPlayerLife(player) <= 6) 1
            else 0
        })
        logic.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.dustToLife(player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        logic.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.KAMUWI_LOGIC){_, _, _, _ ->
            1
        })
    }

    private val falseStab = CardData(CardClass.NORMAL, CardName.RENRI_FALSE_STAB, MegamiEnum.RENRI, CardType.ATTACK, SubType.NONE)
    private val temporaryExpedient = CardData(CardClass.NORMAL, CardName.RENRI_TEMPORARY_EXPEDIENT, MegamiEnum.RENRI, CardType.ATTACK, SubType.NONE)
    private val blackAndWhite = CardData(CardClass.NORMAL, CardName.RENRI_BLACK_AND_WHITE, MegamiEnum.RENRI, CardType.ATTACK, SubType.REACTION)
    private val irritatingGesture = CardData(CardClass.NORMAL, CardName.RENRI_IRRITATING_GESTURE, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.NONE)
    private val floatingClouds = CardData(CardClass.NORMAL, CardName.RENRI_FLOATING_CLOUDS, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.NONE)
    private val fishing = CardData(CardClass.NORMAL, CardName.RENRI_FISHING, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.REACTION)
    private val pullingFishing = CardData(CardClass.NORMAL, CardName.RENRI_PULLING_FISHING, MegamiEnum.RENRI, CardType.ENCHANTMENT, SubType.NONE)
    private val rururarari = CardData(CardClass.SPECIAL, CardName.RENRI_RU_RU_RA_RA_RI, MegamiEnum.RENRI, CardType.ATTACK, SubType.NONE)
    private val ranararomirerira = CardData(CardClass.SPECIAL, CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.REACTION)
    private val orireterareru = CardData(CardClass.SPECIAL, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.NONE)
    private val renriTheEnd = CardData(CardClass.SPECIAL, CardName.RENRI_RENRI_THE_END, MegamiEnum.RENRI, CardType.ENCHANTMENT, SubType.NONE)
    private val engravedGarment = CardData(CardClass.SPECIAL, CardName.RENRI_ENGRAVED_GARMENT, MegamiEnum.RENRI, CardType.UNDEFINED, SubType.NONE)
    private val shamanisticMusic = CardData(CardClass.SPECIAL, CardName.KIRIKO_SHAMANISTIC_MUSIC, MegamiEnum.KIRIKO, CardType.ATTACK, SubType.NONE)

    private val rururarariText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
        game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(1, 999), false,
            null, null, card_number)
        game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
        game_status.returnSpecialCard(player, card_number)
        null
    }

    private fun renriCardInit(){
        falseStab.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        falseStab.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_NOT_DISPROVE) ret@{card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_RENRI_FALSE_STAB)){
                    CommandEnum.SELECT_ONE -> {
                        if(card_number.toCardName() != CardName.RENRI_FALSE_STAB){
                            game_status.getPlayer(player).pre_attack_card?.canNotSelectAura = true
                        }
                        return@ret 1
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            0
        })
        temporaryExpedient.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        temporaryExpedient.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, attack ->
                var count = 0
                for(card in gameStatus.getPlayer(nowPlayer).enchantmentCard.values){
                    if(card.card_data.card_class == CardClass.NORMAL){
                        count += 1
                    }
                }
                for(card in gameStatus.getPlayer(nowPlayer).discard){
                    if(card.card_data.card_class == CardClass.NORMAL){
                        count += 1
                    }
                }
                if(count >= 3){
                    attack.auraPlusMinus(1)
                }
            }))
            null
        })
        blackAndWhite.setAttack(DistanceType.CONTINUOUS, Pair(1, 5), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        blackAndWhite.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_CHANGE){ card_number, player, game_status, react_attack ->
            if(game_status.logger.checkThisTurnMoveDustToken()){
                react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                    {_, _, attack ->
                        if(attack.getEditedAuraDamage() >= 3){
                            attack.auraPlusMinus(-1)
                        }
                        else{
                            attack.lifePlusMinus(-1)
                        }
                    }))
            }
            null
        })
        irritatingGesture.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            game_status.setShrink(player.opposite())
            if(game_status.logger.checkThisTurnFailDisprove(player.opposite())){
                while(true){
                    val list = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_RENRI_IRRITATING_GESTURE
                    ) { card, _ -> card.card_number.isPerjure() }?: break
                    if (list.size == 1){
                        game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?.let { selected ->
                            game_status.insertCardTo(player, selected, LocationEnum.HAND, true)
                        }
                        break
                    }
                    else if(list.size == 0){
                        break
                    }
                    else{
                        continue
                    }
                }
            }
            null
        })
        floatingClouds.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            var count = 0
            for(card in game_status.getPlayer(player).enchantmentCard.values){
                if(card.card_data.card_class == CardClass.NORMAL){
                    count += 1
                }
            }
            for(card in game_status.getPlayer(player).discard){
                if(card.card_data.card_class == CardClass.NORMAL){
                    count += 1
                }
            }
            if(count >= 3){
                game_status.selectCardFrom(player.opposite(), player.opposite(), player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_RENRI_FLOATING_CLOUDS, 1
                ) { card, _ -> card.card_data.canCover }?.let { selected ->
                    game_status.popCardFrom(player, selected[0], LocationEnum.HAND, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, true)
                    }
                }?: run{
                    game_status.setShrink(player.opposite())
                }
            }
            null
        })
        fishing.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        pullingFishing.setEnchantment(3)
        pullingFishing.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() >= 2){
                game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        pullingFishing.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() >= 2){
                game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        rururarari.setSpecial(4)
        rururarari.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        rururarari.addtext((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            if(game_status.logger.checkThisTurnFailDisprove(player.opposite())){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true
                }, {_, _, madeAttack ->
                    madeAttack.setBothSideDamage()
                }))
            }
            null
        }))
        rururarari.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) {card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, rururarariText)
            null
        })
        ranararomirerira.setSpecial(4)
        ranararomirerira.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, _, _, react_attack->
            if((react_attack != null && react_attack.isItReact)) 1
            else 0
        })
        ranararomirerira.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{_, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA)
                {card, _ -> card.card_data.sub_type != SubType.FULL_POWER && card.card_data.megami != MegamiEnum.RENRI}?: run {
                    return@ret null
                }
                if(selected.size == 1){
                    while(true){
                        when(game_status.receiveCardEffectSelect(player, NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA)){
                            CommandEnum.SELECT_ONE -> {
                                val selectNumber = selected[0]
                                game_status.getCardFrom(player.opposite(), selectNumber, LocationEnum.HAND)?.let {
                                    game_status.useCardFrom(player, it, LocationEnum.HAND_OTHER, false, react_attack,
                                        isCost = true, isConsume = true)
                                }
                                break
                            }
                            CommandEnum.SELECT_TWO -> {
                                val selectNumber = selected[0]
                                game_status.getCardFrom(player.opposite(), selectNumber, LocationEnum.HAND)?.let {
                                    game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, true)
                                }
                                break
                            }
                            else -> {
                                continue
                            }
                        }
                    }
                    break
                }
                else if(selected.size == 0){
                    break
                }
            }
            null
        })
        orireterareru.setSpecial(2)
        orireterareru.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{_, player, game_status, react_attack ->
            game_status.selectCardFrom(player, player, player, listOf(LocationEnum.NOT_SELECTED_NORMAL),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_RENRI_O_RI_RE_TE_RA_RE_RU, 1) {
                    card, _ -> card.card_number.isPerjure()
            }?.let { selected ->
                game_status.getPlayer(player).unselectedCard.remove(selected[0].toCardName())
                val useCard = Card.cardMakerByName(game_status.getPlayer(player).firstTurn,
                    selected[0].toCardName(), player)
                game_status.insertCardTo(player, useCard, LocationEnum.PLAYING_ZONE_YOUR, true)
                game_status.getPlayer(player).usingCard.remove(useCard)
                game_status.useCardFrom(player, useCard, LocationEnum.PLAYING_ZONE_YOUR, false, react_attack,
                    isCost = true, isConsume = true
                )
                game_status.insertCardTo(player, useCard, LocationEnum.OUT_OF_GAME, true)
            }
            null
        })
        orireterareru.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            var count = 0
            for(card in game_status.getPlayer(player).enchantmentCard.values){
                if(card.card_data.card_class == CardClass.NORMAL){
                    count += 1
                }
            }
            for(card in game_status.getPlayer(player).discard){
                if(card.card_data.card_class == CardClass.NORMAL){
                    count += 1
                }
            }

            if(count >= 3){
                1
            }
            else {
                0
            }
        })
        renriTheEnd.setSpecial(1)
        renriTheEnd.setEnchantment(2)
        renriTheEnd.addtext(termination)
        renriTheEnd.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            game_status.moveAdditionalCard(player, CardName.RENRI_ENGRAVED_GARMENT, LocationEnum.SPECIAL_CARD)
            null
        })
        renriTheEnd.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DO_NOT_MOVE_TOKEN) {_, player, game_status, _ ->
            if(game_status.turnPlayer == player && game_status.nowPhase == START_PHASE_REDUCE_NAP) 1
            else 0
        })
        renriTheEnd.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_MOVE_TOKEN) ret@{ _, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)
            var count = 0
            for(card in nowPlayer.enchantmentCard.values){
                if(card.card_data.card_name == CardName.RENRI_RENRI_THE_END){
                    count += card.getNap()?: 0
                }
            }

            val engravedGarmentCardNumber = CardName.RENRI_ENGRAVED_GARMENT.toCardNumber(nowPlayer.firstTurn)
            var location: LocationEnum = LocationEnum.SPECIAL_CARD
            val engravedGarment = game_status.getCardFrom(player, engravedGarmentCardNumber, LocationEnum.SPECIAL_CARD)?.apply {
                location = LocationEnum.SPECIAL_CARD
            }?: game_status.getCardFrom(player, engravedGarmentCardNumber, LocationEnum.YOUR_USED_CARD)?.apply {
                location = LocationEnum.YOUR_USED_CARD
            }?: return@ret null

            when (count) {
                0 -> {
                    game_status.removeMainPhaseListener(player, engravedGarmentCardNumber)
                    engravedGarment.card_data = shamanisticMusic
                }
                1 -> {
                    engravedGarment.card_data = mangA
                    if(location == LocationEnum.YOUR_USED_CARD){
                        game_status.addMainPhaseListener(player, Listener(player, engravedGarment.card_number) {gameStatus, cardNumber, _,
                                                                                                _, _, _ ->
                            gameStatus.returnSpecialCard(player, cardNumber)
                            true
                        })
                    }
                }
                2 -> {
                    engravedGarment.card_data = wanjeonNonpa
                }
                else -> {
                    engravedGarment.card_data = kuon
                }
            }
            null
        })
        engravedGarment.setSpecial(null)
        shamanisticMusic.setSpecial(3)
        shamanisticMusic.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
    }

    private val holyRakeHand = CardData(CardClass.NORMAL, CardName.YATSUHA_HOLY_RAKE_HANDS, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.NONE)
    private val entranceOfAbyss = CardData(CardClass.NORMAL, CardName.YATSUHA_ENTRANCE_OF_ABYSS, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.NONE)
    private val trueMonster = CardData(CardClass.NORMAL, CardName.YATSUHA_TRUE_MONSTER, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.FULL_POWER)
    private val ghostLink = CardData(CardClass.NORMAL, CardName.YATSUHA_GHOST_LINK, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val resolution = CardData(CardClass.NORMAL, CardName.YATSUHA_RESOLUTION, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.REACTION)
    private val pledge = CardData(CardClass.NORMAL, CardName.YATSUHA_PLEDGE, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.REACTION)
    private val vainFlower = CardData(CardClass.NORMAL, CardName.YATSUHA_VAIN_FLOWER, MegamiEnum.YATSUHA, CardType.ENCHANTMENT, SubType.NONE)
    private val eightMirrorVainSakura = CardData(CardClass.SPECIAL, CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)

    val notCompleteSet = setOf(CardName.YATSUHA_STAR_NAIL, CardName.YATSUHA_DARKNESS_GILL, CardName.YATSUHA_MIRROR_DEVIL,
        CardName.YATSUHA_GHOST_STEP, CardName.YATSUHA_WILLING, CardName.YATSUHA_CONTRACT, CardName.YATSUHA_CLINGY_FLOWER)

    val completeSet = setOf(CardName.YATSUHA_HOLY_RAKE_HANDS, CardName.YATSUHA_ENTRANCE_OF_ABYSS, CardName.YATSUHA_TRUE_MONSTER,
        CardName.YATSUHA_GHOST_LINK, CardName.YATSUHA_RESOLUTION, CardName.YATSUHA_PLEDGE, CardName.YATSUHA_VAIN_FLOWER)

    val completeMap =  EnumMap<CardName, CardName>(CardName::class.java).apply {
        put(CardName.YATSUHA_STAR_NAIL, CardName.YATSUHA_HOLY_RAKE_HANDS)
        put(CardName.YATSUHA_DARKNESS_GILL, CardName.YATSUHA_ENTRANCE_OF_ABYSS)
        put(CardName.YATSUHA_MIRROR_DEVIL, CardName.YATSUHA_TRUE_MONSTER)
        put(CardName.YATSUHA_GHOST_STEP, CardName.YATSUHA_GHOST_LINK)
        put(CardName.YATSUHA_WILLING, CardName.YATSUHA_RESOLUTION)
        put(CardName.YATSUHA_CONTRACT, CardName.YATSUHA_PLEDGE)
        put(CardName.YATSUHA_CLINGY_FLOWER, CardName.YATSUHA_VAIN_FLOWER)
    }

    private fun countCompleteCard(game_status: GameStatus, player: PlayerEnum): Int{
        var count = 0
        for(cardName in game_status.getPlayer(player).additionalHand.keys){
            if(cardName in notCompleteSet){
                count += 1
            }
        }
        return count
    }

    private suspend fun changeCompleteCard(game_status: GameStatus, player: PlayerEnum): Boolean{
        while (true){
            val list = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.HAND, LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_YATSUHA_VAIN_FLOWER
            ) {card, _ -> card.card_data.megami == MegamiEnum.YATSUHA && card.card_data.card_name in notCompleteSet}?: break
            if (list.size == 1){
                game_status.popCardFrom(player, list[0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                    game_status.moveAdditionalCard(player, completeMap[it.card_data.card_name]!!, LocationEnum.HAND)

                }?: game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                    game_status.moveAdditionalCard(player, completeMap[it.card_data.card_name]!!, LocationEnum.DISCARD_YOUR)
                }?: return false
                return true
            }
            else if(list.size == 0){
                break
            }
        }
        return false
    }


    private fun yatsuhaA1CardInit(){
        holyRakeHand.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        holyRakeHand.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, _ ->
           game_status.flareToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
               game_status.getCardOwner(card_number), card_number)
            null
        })
        entranceOfAbyss.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        entranceOfAbyss.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, gameStatus, attack ->
                attack.apply {
                    val mirror = gameStatus.getMirror()
                    lifePlusMinus(mirror); auraPlusMinus(mirror)
                }
            }))
            null
        })
        trueMonster.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 4, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        trueMonster.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) {card_number, player, game_status, _ ->
            game_status.lifeToLife(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        ghostLink.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {_, player, game_status, _ ->
            while (true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_GHOST_LINK)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.addThisTurnDistance(1)
                        game_status.addThisTurnSwellDistance(1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.addThisTurnDistance(-1)
                        game_status.addThisTurnSwellDistance(-1)
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        ghostLink.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.YATSUHA_GHOST_LINK, card_number, CardClass.NULL,
                    sortedSetOf(3, 4, 5), 2,  1, MegamiEnum.YATSUHA,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        resolution.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, react_attack->
            while (true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_RESOLUTION)){
                    CommandEnum.SELECT_ONE -> {
                        while (true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_HOLY_RAKE_HANDS)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.auraToFlare(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToAura(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        if(react_attack?.card_class != CardClass.SPECIAL){
                            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                                true
                            }, { nowPlayer, gameStatus, attack ->
                                val (aura, _) = attack.getDamage(gameStatus, nowPlayer.opposite(),
                                    gameStatus.getPlayerAttackBuff(nowPlayer.opposite()))
                                if(aura <= gameStatus.getMirror() + 1){
                                    attack.makeNotValid()
                                }
                            }))
                        }
                        break
                    }
                    CommandEnum.SELECT_THREE -> {
                        while (true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_HOLY_RAKE_HANDS)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.auraToFlare(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToAura(player, player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number)
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        if(react_attack?.card_class != CardClass.SPECIAL){
                            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                                true
                            }, { nowPlayer, gameStatus, attack ->
                                val (aura, _) = attack.getDamage(gameStatus, nowPlayer.opposite(),
                                    gameStatus.getPlayerAttackBuff(nowPlayer.opposite()))
                                if(aura <= gameStatus.getMirror() + 1){
                                    attack.makeNotValid()
                                }
                            }))
                        }
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        pledge.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN) {card_number, player, game_status, react_attack->
            while (true) {
                when (game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_PLEDGE)) {
                    CommandEnum.SELECT_ONE -> {
                        while (true) {
                            when (game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_ENTRANCE_OF_ABYSS)) {
                                CommandEnum.SELECT_ONE -> {
                                    game_status.auraToFlare(
                                        player.opposite(), player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number
                                    )
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToAura(
                                        player, player.opposite(), 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number
                                    )
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        while (true) {
                            when (game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_TRUE_MONSTER)) {
                                CommandEnum.SELECT_ONE -> {
                                    game_status.flareToAura(
                                        player.opposite(), player, 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number
                                    )
                                    break
                                }

                                CommandEnum.SELECT_TWO -> {
                                    game_status.auraToFlare(
                                        player, player.opposite(), 1, Arrow.BOTH_DIRECTION, player,
                                        game_status.getCardOwner(card_number), card_number
                                    )
                                    break
                                }
                                else -> {
                                    continue
                                }
                            }
                        }
                        break
                    }
                    else -> {
                        continue
                    }
                }
            }
            null
        })
        vainFlower.setEnchantment(3)
        vainFlower.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if(countCompleteCard(game_status, player) < 4){
                changeCompleteCard(game_status, player)
            }
            else{
                game_status.popCardFrom(player, card_number, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                }
                game_status.lifeToOut(player.opposite(), 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        eightMirrorVainSakura.setSpecial(1)
        eightMirrorVainSakura.addtext(termination)
        eightMirrorVainSakura.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){_, _, game_status, _ ->
            if(game_status.getAdjustDistance() <= 7) 1
            else 0
        })
        eightMirrorVainSakura.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) {_, player, game_status, _->
            if(changeCompleteCard(game_status, player)){
                game_status.setShrink(player)
            }
            null
        })
        eightMirrorVainSakura.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR) {_, player, game_status, _->
            if(changeCompleteCard(game_status, player)){
                game_status.setShrink(player)
            }
            null
        })
    }

    private val blaster = CardData(CardClass.NORMAL, CardName.KURURU_BLASTER, MegamiEnum.KURURU, CardType.ATTACK, SubType.NONE)
    private val railgun = CardData(CardClass.NORMAL, CardName.KURURU_RAILGUN, MegamiEnum.KURURU, CardType.ATTACK, SubType.NONE)
    private val connectDive = CardData(CardClass.SPECIAL, CardName.KURURU_CONNECT_DIVE, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)

    private val connectDiveText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
        val kikou = getKikou(player, game_status) { card ->
            card.card_data.megami != MegamiEnum.KURURU
        }
        if(kikou.behavior >= 1 && kikou.reaction >= 1 && kikou.enchantment >= 1){
            game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 5, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    while (true) {
                        when (game_status.receiveCardEffectSelect(player, NUMBER_KURURU_RAILGUN)) {
                            CommandEnum.SELECT_ONE -> {
                                if(game_status.dust >= 2){
                                    game_status.dustToCard(player, 2, it, card_number, LocationEnum.YOUR_USED_CARD)
                                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                                    break
                                }
                            }
                            CommandEnum.SELECT_TWO -> {
                                if(game_status.getPlayerAura(player) >= 2){
                                    game_status.auraToCard(player, 2, it, card_number, LocationEnum.YOUR_USED_CARD)
                                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                                    break
                                }
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
                else{
                    while (true) {
                        when (game_status.receiveCardEffectSelect(player, NUMBER_KURURU_CONNECT_DIVE)) {
                            CommandEnum.SELECT_ONE -> {
                                if(game_status.dust >= 1){
                                    game_status.dustToCard(player, 1, it, card_number, LocationEnum.PLAYING_ZONE_YOUR)
                                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                                    break
                                }
                            }
                            CommandEnum.SELECT_TWO -> {
                                if(game_status.getPlayerAura(player) >= 1){
                                    game_status.auraToCard(player, 1, it, card_number, LocationEnum.PLAYING_ZONE_YOUR)
                                    game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
                                    break
                                }
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
            }
        }
        null
    }

    private fun kururuA2CardInit(){
        blaster.setAttack(DistanceType.CONTINUOUS, Pair(2, 6), null, 0, 0,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        blaster.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            val megami = game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.card_data?.megami
            val kikou = getKikou(player, game_status){ card ->
                card.card_data.megami != megami
            }
            if(kikou.enchantment >= 1){
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 6, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.KURURU_BLASTER, card_number, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 2,  2,
                            MegamiEnum.KURURU, cannotReactNormal = false, cannotReactSpecial = false,
                            cannotReact = false, chogek = false), null)){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
                else{
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.KURURU_BLASTER, card_number, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4, 5, 6), 1,  1, MegamiEnum.KURURU,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
            }
            null
        })
        blaster.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            val megami = game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.card_data?.megami
            val kikou = getKikou(player, game_status) { card ->
                card.card_data.megami != megami
            }
            if (kikou.reaction >= 1 && kikou.behavior >= 1) {
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 7, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.KURURU_BLASTER, card_number, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 2,  2,
                            MegamiEnum.KURURU, cannotReactNormal = false, cannotReactSpecial = false,
                            cannotReact = false, chogek = false), null)){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
                else{
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.KURURU_BLASTER, card_number, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4, 5, 6), 1,  1, MegamiEnum.KURURU,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false), null)){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
            }
            null
        })
        railgun.setAttack(DistanceType.CONTINUOUS, Pair(2, 6), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        railgun.addtext(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) {card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 2){
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 8, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                        true
                    }, {_, _, attack ->
                        attack.auraPlusMinus(4)
                    }))
                }
                else{
                    game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                        true
                    }, {_, _, attack ->
                        attack.auraPlusMinus(2)
                    }))
                }
            }
            if(kikou.fullPower >= 1){
                game_status.getPlayer(player).afterCardUseTermination = true

                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 9, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                        true
                    }, {_, _, attack ->
                        attack.lifePlusMinus(2)
                    }))
                }
                else{
                    game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                        true
                    }, {_, _, attack ->
                        attack.lifePlusMinus(1)
                    }))
                }

                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.LOSE_IMMEDIATE,
                    { _, _, _ ->
                        true
                    }, { _, _, attack ->
                        attack.apply {
                            isItValid = true; isItDamage = true
                        }
                    }))
            }
            null
        })
        connectDive.setSpecial(1)
        connectDive.addtext(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_SAKURA_TOKEN){card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status) { card ->
                card.card_data.megami != MegamiEnum.KURURU
            }
            if(kikou.behavior >= 1 && kikou.reaction >= 1 && kikou.enchantment >= 1){
                game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                    while (true) {
                        when (game_status.receiveCardEffectSelect(player, NUMBER_KURURU_CONNECT_DIVE)) {
                            CommandEnum.SELECT_ONE -> {
                                if(game_status.dust >= 1){
                                    game_status.dustToCard(player, 1, it, Log.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                                    break
                                }
                            }
                            CommandEnum.SELECT_TWO -> {
                                if(game_status.getPlayerAura(player) >= 1){
                                    game_status.auraToCard(player, 1, it, Log.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                                    break
                                }
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
            }
            null
        })
        connectDive.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) {card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, connectDiveText)
            null
        })
        connectDive.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_RESOLVE_COG_EFFECT) ret@{card_number, player, game_status, _ ->
            val connectDiveNumber = card_number / 10
            val connectDive = game_status.getCardFrom(player, connectDiveNumber, LocationEnum.YOUR_USED_CARD)?: return@ret null
            if((connectDive.getNap()?: 0) >= 1){
                while(true){
                    val nowCommand = game_status.receiveCardEffectSelect(player, 102000000 + card_number - connectDiveNumber * 10)
                    if(nowCommand == CommandEnum.SELECT_ONE){
                        game_status.cardToDust(player, 1, connectDive, false, connectDiveNumber)
                        game_status.logger.insert(Log(player, LogText.END_EFFECT, connectDiveNumber, -1))
                        return@ret 1
                    }
                    else if(nowCommand == CommandEnum.SELECT_NOT){
                        break
                    }
                }
            }
            null
        })
    }

    private val torpedo = CardData(CardClass.NORMAL, CardName.HATSUMI_TORPEDO, MegamiEnum.HATSUMI, CardType.ENCHANTMENT, SubType.NONE)
    private val sagiriHail = CardData(CardClass.SPECIAL, CardName.HATSUMI_SAGIRI_HAIL, MegamiEnum.HATSUMI, CardType.ENCHANTMENT, SubType.REACTION)
    private val wadanakaRoute = CardData(CardClass.SPECIAL, CardName.HATSUMI_WADANAKA_ROUTE, MegamiEnum.HATSUMI, CardType.ENCHANTMENT, SubType.NONE)

    private fun hatsumiA1CardInit(){
        torpedo.setEnchantment(2)
        torpedo.addtext(chasm)
        torpedo.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.DIVING) { _, player, game_status, _ ->
            game_status.diving(player)
            null
        })
        torpedo.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(player, MadeAttack(CardName.HATSUMI_TORPEDO, card_number, CardClass.NULL,
                    sortedSetOf(1, 2, 3, 4, 5, 6, 7), 999,  1, MegamiEnum.HATSUMI,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                ), null) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        sagiriHail.setSpecial(3)
        sagiriHail.setEnchantment(4)
        sagiriHail.addtext(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){card_number, player, game_status, _ ->
            if(game_status.turnPlayer == player.opposite()){
                game_status.addThisTurnRangeBuff(player.opposite(), RangeBuff(card_number,1, RangeBufTag.DELETE_IMMEDIATE,
                    {_, _, condition_attack ->
                        condition_attack.editedDistance.size >= 3
                    },
                    { _, _, attack ->
                        val min = attack.editedDistance.first()
                        val max = attack.editedDistance.last()
                        for (i in attack.editedDistance){
                            if(i == min || i == max){
                                continue
                            }
                            attack.tempEditedDistance.add(i)
                        }
                }))
            }
            null
        })
        wadanakaRoute.setSpecial(2)
        wadanakaRoute.setEnchantment(2)
        wadanakaRoute.addtext(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.DIVING) { _, player, game_status, _ ->
            game_status.diving(player)
            game_status.setShrink(player.opposite())
            null
        })
        wadanakaRoute.addtext(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.DAMAGE) {card_number, player, game_status, _ ->
            if(isTailWind(player, game_status)){
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(1, 999), false,
                    null, null, card_number)
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, card_number)
                game_status.logger.insert(Log(player, LogText.END_EFFECT, card_number, -1))
            }
            null
        })
        wadanakaRoute.addtext(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){_, player, game_status, _ ->
            if(isHeadWind(player, game_status)) 1
            else 0
        })
    }

    private val questionAnswer = CardData(CardClass.NORMAL, CardName.YURINA_QUESTION_ANSWER, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)


    private fun yurinaA2CardInit(){
        questionAnswer.setAttack(DistanceType.CONTINUOUS, Pair(2, 5), null, 3, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        questionAnswer.addtext(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) { _, player, game_status, _ ->
            game_status.getPlayer(player.opposite()).deckToCoverCard(game_status, 3)
            val basicOperationSelected = game_status.requestAndDoBasicOperation(player, 114)
            if(game_status.canDoBasicOperation(player.opposite(), basicOperationSelected)){
                game_status.doBasicOperation(player.opposite(), basicOperationSelected, CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + 114)
            }
            null
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
        oboroA1CardInit()
        chikageA1CardInit()
        utsuroA1CardInit()
        korunuCardInit()
        yatsuhaCardInit()
        shinraA1CardInit()
        kururuA1CardInit()
        saineA2CardInit()
        hatsumiCardInit()
        mizukiCardInit()
        yukihiA1CardInit()
        thallyaA1CardInit()
        rairaA1CardInit()
        megumiCardInit()
        kanaweIdeaInit()
        kanaweCardInit()
        tokoyoA2CardInit()
        honokaA1CardInit()
        haganeA1CardInit()
        kamuwiCardInit()
        renriCardInit()
        yatsuhaA1CardInit()
        kururuA2CardInit()
        hatsumiA1CardInit()
        yurinaA2CardInit()

        hashMapInit()
        hashMapTest()
    }
}

private val poisonSet = hashSetOf(NUMBER_POISON_ANYTHING, NUMBER_POISON_PARALYTIC, NUMBER_POISON_HALLUCINOGENIC,
    NUMBER_POISON_RELAXATION, NUMBER_POISON_DEADLY_1, NUMBER_POISON_DEADLY_2,
    SECOND_PLAYER_START_NUMBER + NUMBER_POISON_PARALYTIC, SECOND_PLAYER_START_NUMBER + NUMBER_POISON_HALLUCINOGENIC,
    SECOND_PLAYER_START_NUMBER + NUMBER_POISON_RELAXATION, SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_1,
    SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_2)

fun Int.isPoison() = this in poisonSet

private val soldierSet = hashSetOf(NUMBER_SOLDIER_HORSE, NUMBER_SOLDIER_SHIELD, NUMBER_SOLDIER_SPEAR_1,
    NUMBER_SOLDIER_SPEAR_2, SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_HORSE,
    SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SHIELD, SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_1,
    SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_2)

fun Int.isSoldier() = this in soldierSet

private val perjureSet = hashSetOf(
    NUMBER_RENRI_FALSE_STAB, NUMBER_RENRI_TEMPORARY_EXPEDIENT, NUMBER_RENRI_BLACK_AND_WHITE,
    NUMBER_RENRI_FLOATING_CLOUDS, NUMBER_RENRI_FISHING, SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FALSE_STAB,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_TEMPORARY_EXPEDIENT,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_BLACK_AND_WHITE,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FLOATING_CLOUDS,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FISHING)

fun Int.isPerjure() = this in perjureSet

fun Int.toPrivate(): Int{
    return if(this.isPoison()){
        1
    } else if(this.isSoldier()){
        2
    } else{
        0
    }
}