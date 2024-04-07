package com.sakurageto.card

import com.sakurageto.gamelogic.*
import com.sakurageto.gamelogic.GameStatus.Companion.END_PHASE
import com.sakurageto.gamelogic.GameStatus.Companion.START_PHASE
import com.sakurageto.gamelogic.log.EventLog
import com.sakurageto.gamelogic.log.LogText
import com.sakurageto.gamelogic.megamispecial.Kikou
import com.sakurageto.gamelogic.megamispecial.Stratagem
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.gamelogic.megamispecial.YatsuhaJourney
import com.sakurageto.gamelogic.megamispecial.storyboard.Act
import com.sakurageto.protocol.*
import io.ktor.network.sockets.*
import kotlinx.coroutines.delay
import java.util.EnumMap
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.random.Random

object CardSet {
    private val cardNumberHashmap = numberHashmapInit()
    private val cardDataHashmap = EnumMap<CardName, CardData>(CardName::class.java)
    private val cardDataHashmapV8_1 = EnumMap<CardName, CardData>(CardName::class.java)
    private val cardDataHashmapV8_2 = EnumMap<CardName, CardData>(CardName::class.java)
    private val cardDataHashmapV9 = EnumMap<CardName, CardData>(CardName::class.java)

    fun Pair<Int, Int>?.adjustRange(parameter: Int): Pair<Int, Int>{
        if (this == null) return Pair(0, 0)
        else return Pair(this.first + parameter, this.second + parameter)
    }

    fun Int.toCardName(): CardName = cardNumberHashmap[this]?: CardName.CARD_UNNAME
    fun CardName.toCardData(version: GameVersion): CardData{
        return when(version){
            GameVersion.VERSION_7_2 -> {
                cardDataHashmap[this]?: unused
            }
            GameVersion.VERSION_8_1 -> {
                if(this in cardDataHashmapV8_1) {
                    cardDataHashmapV8_1[this]!!
                }
                else{
                    this.toCardData(GameVersion.VERSION_7_2)
                }
            }
            GameVersion.VERSION_8_2 -> {
                if(this in cardDataHashmapV8_2) {
                    cardDataHashmapV8_2[this]!!
                }
                else{
                    this.toCardData(GameVersion.VERSION_8_1)
                }
            }
            GameVersion.VERSION_9 -> {
                if(this in cardDataHashmapV9) {
                    cardDataHashmapV9[this]!!
                }
                else{
                    this.toCardData(GameVersion.VERSION_8_2)
                }
            }
        }

    }

    private fun hashMapTest(){
        val cardNameList = CardName.values()
        val exceptionSet = setOf(CardName.CARD_UNNAME, CardName.POISON_ANYTHING, CardName.SOLDIER_ANYTHING,
            CardName.PARTS_ANYTHING)
        for(cardName in cardNameList){
            if(cardName in exceptionSet) {
                continue
            }
            if(cardName.toCardData(GameVersion.VERSION_7_2) == unused){
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

    fun thisCardMoveTextCheck(cardName: CardName, originalCardName: CardName)
            = cardName == originalCardName || dupligearCheck(cardName) || cardName == CardName.HAGANE_SOFT_ATTACK

    fun getPlayerAllNormalCardExceptEnchantment(player: PlayerEnum, game_status: GameStatus): List<Card>{
        val ownerPlayer = game_status.getPlayer(player)
        return ownerPlayer.hand.values + ownerPlayer.normalCardDeck + ownerPlayer.discard + ownerPlayer.coverCard +
        ownerPlayer.readySoldierZone.values + ownerPlayer.notReadySoldierZone.values + ownerPlayer.additionalHand.values +
        ((ownerPlayer.memory?.values) ?: emptyList()) +
        game_status.getPlayer(PlayerEnum.PLAYER1).sealZone.values.filter {
            it.player == player
        } + game_status.getPlayer(PlayerEnum.PLAYER2).sealZone.values.filter {
            it.player == player
        }
    }

    fun getPlayerAllNormalCard(player: PlayerEnum, game_status: GameStatus): List<Card>{
        return getPlayerAllNormalCardExceptEnchantment(player, game_status) +
                game_status.getPlayer(PlayerEnum.PLAYER1).enchantmentCard.values.filter {
                    it.player == player
                } +
                game_status.getPlayer(PlayerEnum.PLAYER2).enchantmentCard.values.filter {
                    it.player == player
                }
    }

    fun makeCard(player: PlayerEnum, game_status: GameStatus, location: LocationEnum, cardName: CardName): Card{
        val nowPlayer = game_status.getPlayer(player)
        val card = Card.cardMakerByName(game_status.getPlayer(player).firstTurn, cardName, player, location, game_status.version)
        for(enchantment in nowPlayer.enchantmentCard.values){
            if(enchantment.card_data.card_name == CardName.MIZUKI_MIZUKI_BATTLE_CRY){
                if(card.card_data.sub_type == SubType.FULL_POWER){
                    replaceFullPowerToTermination(card)
                }
                else{
                    card.card_data.effect?.let {
                        for (text in it){
                            if(text === terminationText){
                                removeTermination(card)
                                break
                            }
                        }
                    }
                }
                break
            }
        }
        return card
    }

    private fun numberHashmapInit(): Map<Int, CardName>{
        val cardTempNumberHashmap = hashMapOf<Int, CardName>()

        //for number -> card name
        cardTempNumberHashmap[NUMBER_CARD_UNAME] = CardName.CARD_UNNAME
        cardTempNumberHashmap[NUMBER_POISON_ANYTHING] = CardName.POISON_ANYTHING
        cardTempNumberHashmap[NUMBER_SOLDIER_ANYTHING] = CardName.SOLDIER_ANYTHING
        cardTempNumberHashmap[NUMBER_PARTS_ANYTHING] = CardName.PARTS_ANYTHING

        cardTempNumberHashmap[NUMBER_YURINA_CHAM] = CardName.YURINA_CHAM
        cardTempNumberHashmap[NUMBER_YURINA_ILSUM] = CardName.YURINA_ILSUM
        cardTempNumberHashmap[NUMBER_YURINA_JARUCHIGI] = CardName.YURINA_JARUCHIGI
        cardTempNumberHashmap[NUMBER_YURINA_GUHAB] = CardName.YURINA_GUHAB
        cardTempNumberHashmap[NUMBER_YURINA_GIBACK] = CardName.YURINA_GIBACK
        cardTempNumberHashmap[NUMBER_YURINA_APDO] = CardName.YURINA_APDO
        cardTempNumberHashmap[NUMBER_YURINA_GIYENBANJO] = CardName.YURINA_GIYENBANJO
        cardTempNumberHashmap[NUMBER_YURINA_WOLYUNGNACK] = CardName.YURINA_WOLYUNGNACK
        cardTempNumberHashmap[NUMBER_YURINA_POBARAM] = CardName.YURINA_POBARAM
        cardTempNumberHashmap[NUMBER_YURINA_JJOCKBAE] = CardName.YURINA_JJOCKBAE
        cardTempNumberHashmap[NUMBER_YURINA_JURUCK] = CardName.YURINA_JURUCK
        cardTempNumberHashmap[NUMBER_YURINA_NAN_TA] = CardName.YURINA_NAN_TA
        cardTempNumberHashmap[NUMBER_YURINA_BEAN_BULLET] = CardName.YURINA_BEAN_BULLET
        cardTempNumberHashmap[NUMBER_YURINA_NOT_COMPLETE_POBARAM] = CardName.YURINA_NOT_COMPLETE_POBARAM
        cardTempNumberHashmap[NUMBER_YURINA_QUESTION_ANSWER] = CardName.YURINA_QUESTION_ANSWER
        cardTempNumberHashmap[NUMBER_YURINA_AHUM] = CardName.YURINA_AHUM
        cardTempNumberHashmap[NUMBER_YURINA_KANZA_DO] = CardName.YURINA_KANZA_DO

        cardTempNumberHashmap[NUMBER_SAINE_DOUBLEBEGI] = CardName.SAINE_DOUBLEBEGI
        cardTempNumberHashmap[NUMBER_SAINE_HURUBEGI] = CardName.SAINE_HURUBEGI
        cardTempNumberHashmap[NUMBER_SAINE_MOOGECHOO] = CardName.SAINE_MOOGECHOO
        cardTempNumberHashmap[NUMBER_SAINE_GANPA] = CardName.SAINE_GANPA
        cardTempNumberHashmap[NUMBER_SAINE_GWONYUCK] = CardName.SAINE_GWONYUCK
        cardTempNumberHashmap[NUMBER_SAINE_CHOONGEMJUNG] = CardName.SAINE_CHOONGEMJUNG
        cardTempNumberHashmap[NUMBER_SAINE_MOOEMBUCK] = CardName.SAINE_MOOEMBUCK
        cardTempNumberHashmap[NUMBER_SAINE_YULDONGHOGEK] = CardName.SAINE_YULDONGHOGEK
        cardTempNumberHashmap[NUMBER_SAINE_HANGMUNGGONGJIN] = CardName.SAINE_HANGMUNGGONGJIN
        cardTempNumberHashmap[NUMBER_SAINE_EMMOOSHOEBING] = CardName.SAINE_EMMOOSHOEBING
        cardTempNumberHashmap[NUMBER_SAINE_JONGGEK] = CardName.SAINE_JONGGEK
        cardTempNumberHashmap[NUMBER_SAINE_SOUND_OF_ICE] = CardName.SAINE_SOUND_OF_ICE
        cardTempNumberHashmap[NUMBER_SAINE_ACCOMPANIMENT] = CardName.SAINE_ACCOMPANIMENT
        cardTempNumberHashmap[NUMBER_SAINE_DUET_TAN_JU_BING_MYEONG] = CardName.SAINE_DUET_TAN_JU_BING_MYEONG
        cardTempNumberHashmap[NUMBER_SAINE_BETRAYAL] = CardName.SAINE_BETRAYAL
        cardTempNumberHashmap[NUMBER_SAINE_FLOWING_WALL] = CardName.SAINE_FLOWING_WALL
        cardTempNumberHashmap[NUMBER_SAINE_JEOL_CHANG_JEOL_HWA] = CardName.SAINE_JEOL_CHANG_JEOL_HWA
        cardTempNumberHashmap[NUMBER_SAI_TOKO_ENSEMBLE] = CardName.SAI_TOKO_ENSEMBLE

        cardTempNumberHashmap[NUMBER_HIMIKA_SHOOT] = CardName.HIMIKA_SHOOT
        cardTempNumberHashmap[NUMBER_HIMIKA_RAPIDFIRE] = CardName.HIMIKA_RAPIDFIRE
        cardTempNumberHashmap[NUMBER_HIMIKA_MAGNUMCANON] = CardName.HIMIKA_MAGNUMCANON
        cardTempNumberHashmap[NUMBER_HIMIKA_FULLBURST] = CardName.HIMIKA_FULLBURST
        cardTempNumberHashmap[NUMBER_HIMIKA_BACKSTEP] = CardName.HIMIKA_BACKSTEP
        cardTempNumberHashmap[NUMBER_HIMIKA_BACKDRAFT] = CardName.HIMIKA_BACKDRAFT
        cardTempNumberHashmap[NUMBER_HIMIKA_SMOKE] = CardName.HIMIKA_SMOKE
        cardTempNumberHashmap[NUMBER_HIMIKA_REDBULLET] = CardName.HIMIKA_REDBULLET
        cardTempNumberHashmap[NUMBER_HIMIKA_CRIMSONZERO] = CardName.HIMIKA_CRIMSONZERO
        cardTempNumberHashmap[NUMBER_HIMIKA_SCARLETIMAGINE] = CardName.HIMIKA_SCARLETIMAGINE
        cardTempNumberHashmap[NUMBER_HIMIKA_BURMILIONFIELD] = CardName.HIMIKA_BURMILIONFIELD
        cardTempNumberHashmap[NUMBER_HIMIKA_FIRE_WAVE] = CardName.HIMIKA_FIRE_WAVE
        cardTempNumberHashmap[NUMBER_HIMIKA_SAT_SUI] = CardName.HIMIKA_SAT_SUI
        cardTempNumberHashmap[NUMBER_HIMIKA_EN_TEN_HIMIKA] = CardName.HIMIKA_EN_TEN_HIMIKA

        cardTempNumberHashmap[NUMBER_TOKOYO_BITSUNERIGI] = CardName.TOKOYO_BITSUNERIGI
        cardTempNumberHashmap[NUMBER_TOKOYO_WOOAHHANTAGUCK] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardTempNumberHashmap[NUMBER_TOKOYO_RUNNING_RABBIT] = CardName.TOKOYO_RUNNING_RABBIT
        cardTempNumberHashmap[NUMBER_TOKOYO_POETDANCE] = CardName.TOKOYO_POETDANCE
        cardTempNumberHashmap[NUMBER_TOKOYO_FLIPFAN] = CardName.TOKOYO_FLIPFAN
        cardTempNumberHashmap[NUMBER_TOKOYO_WINDSTAGE] = CardName.TOKOYO_WINDSTAGE
        cardTempNumberHashmap[NUMBER_TOKOYO_SUNSTAGE] = CardName.TOKOYO_SUNSTAGE
        cardTempNumberHashmap[NUMBER_TOKOYO_KUON] = CardName.TOKOYO_KUON
        cardTempNumberHashmap[NUMBER_TOKOYO_THOUSANDBIRD] = CardName.TOKOYO_THOUSANDBIRD
        cardTempNumberHashmap[NUMBER_TOKOYO_ENDLESSWIND] = CardName.TOKOYO_ENDLESSWIND
        cardTempNumberHashmap[NUMBER_TOKOYO_TOKOYOMOON] = CardName.TOKOYO_TOKOYOMOON
        cardTempNumberHashmap[NUMBER_TOKOYO_FLOWING_PLAY] = CardName.TOKOYO_FLOWING_PLAY
        cardTempNumberHashmap[NUMBER_TOKOYO_SOUND_OF_SUN] = CardName.TOKOYO_SOUND_OF_SUN
        cardTempNumberHashmap[NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG] = CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG
        cardTempNumberHashmap[NUMBER_TOKOYO_PASSING_FEAR] = CardName.TOKOYO_PASSING_FEAR
        cardTempNumberHashmap[NUMBER_TOKOYO_RELIC_EYE] = CardName.TOKOYO_RELIC_EYE
        cardTempNumberHashmap[NUMBER_TOKOYO_EIGHT_SAKURA_IN_VAIN] = CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN

        cardTempNumberHashmap[NUMBER_OBORO_WIRE] = CardName.OBORO_WIRE
        cardTempNumberHashmap[NUMBER_OBORO_SHADOWCALTROP] = CardName.OBORO_SHADOWCALTROP
        cardTempNumberHashmap[NUMBER_OBORO_ZANGEKIRANBU] = CardName.OBORO_ZANGEKIRANBU
        cardTempNumberHashmap[NUMBER_OBORO_NINJAWALK] = CardName.OBORO_NINJAWALK
        cardTempNumberHashmap[NUMBER_OBORO_INDUCE] = CardName.OBORO_INDUCE
        cardTempNumberHashmap[NUMBER_OBORO_CLONE] = CardName.OBORO_CLONE
        cardTempNumberHashmap[NUMBER_OBORO_BIOACTIVITY] = CardName.OBORO_BIOACTIVITY
        cardTempNumberHashmap[NUMBER_OBORO_KUMASUKE] = CardName.OBORO_KUMASUKE
        cardTempNumberHashmap[NUMBER_OBORO_TOBIKAGE] = CardName.OBORO_TOBIKAGE
        cardTempNumberHashmap[NUMBER_OBORO_ULOO] = CardName.OBORO_ULOO
        cardTempNumberHashmap[NUMBER_OBORO_MIKAZRA] = CardName.OBORO_MIKAZRA
        cardTempNumberHashmap[NUMBER_OBORO_SHURIKEN] = CardName.OBORO_SHURIKEN
        cardTempNumberHashmap[NUMBER_OBORO_AMBUSH] = CardName.OBORO_AMBUSH
        cardTempNumberHashmap[NUMBER_OBORO_BRANCH_OF_DIVINE] = CardName.OBORO_BRANCH_OF_DIVINE
        cardTempNumberHashmap[NUMBER_OBORO_LAST_CRYSTAL] = CardName.OBORO_LAST_CRYSTAL
        cardTempNumberHashmap[NUMBER_OBORO_HOLOGRAM_KUNAI] = CardName.OBORO_HOLOGRAM_KUNAI
        cardTempNumberHashmap[NUMBER_OBORO_GIGASUKE] = CardName.OBORO_GIGASUKE
        cardTempNumberHashmap[NUMBER_OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI] = CardName.OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI
        cardTempNumberHashmap[NUMBER_OBORO_MAIN_PARTS_X] = CardName.OBORO_MAIN_PARTS_X
        cardTempNumberHashmap[NUMBER_OBORO_MAIN_PARTS_Y] = CardName.OBORO_MAIN_PARTS_Y
        cardTempNumberHashmap[NUMBER_OBORO_MAIN_PARTS_Z] = CardName.OBORO_MAIN_PARTS_Z
        cardTempNumberHashmap[NUMBER_OBORO_CUSTOM_PARTS_A] = CardName.OBORO_CUSTOM_PARTS_A
        cardTempNumberHashmap[NUMBER_OBORO_CUSTOM_PARTS_B] = CardName.OBORO_CUSTOM_PARTS_B
        cardTempNumberHashmap[NUMBER_OBORO_CUSTOM_PARTS_C] = CardName.OBORO_CUSTOM_PARTS_C
        cardTempNumberHashmap[NUMBER_OBORO_CUSTOM_PARTS_D] = CardName.OBORO_CUSTOM_PARTS_D

        cardTempNumberHashmap[NUMBER_YUKIHI_YUKIHI] = CardName.YUKIHI_YUKIHI
        cardTempNumberHashmap[NUMBER_YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardTempNumberHashmap[NUMBER_YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardTempNumberHashmap[NUMBER_YUKIHI_PUSH_OUT_SLASH_PULL] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardTempNumberHashmap[NUMBER_YUKIHI_SWING_SLASH_STAB] = CardName.YUKIHI_SWING_SLASH_STAB
        cardTempNumberHashmap[NUMBER_YUKIHI_TURN_UMBRELLA] = CardName.YUKIHI_TURN_UMBRELLA
        cardTempNumberHashmap[NUMBER_YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardTempNumberHashmap[NUMBER_YUKIHI_MAKE_CONNECTION] = CardName.YUKIHI_MAKE_CONNECTION
        cardTempNumberHashmap[NUMBER_YUKIHI_FLUTTERING_SNOWFLAKE] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardTempNumberHashmap[NUMBER_YUKIHI_SWAYING_LAMPLIGHT] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardTempNumberHashmap[NUMBER_YUKIHI_CLINGY_MIND] = CardName.YUKIHI_CLINGY_MIND
        cardTempNumberHashmap[NUMBER_YUKIHI_SWIRLING_GESTURE] = CardName.YUKIHI_SWIRLING_GESTURE
        cardTempNumberHashmap[NUMBER_YUKIHI_HELP_SLASH_THREAT] = CardName.YUKIHI_HELP_SLASH_THREAT
        cardTempNumberHashmap[NUMBER_YUKIHI_THREAD_SLASH_RAW_THREAD] = CardName.YUKIHI_THREAD_SLASH_RAW_THREAD
        cardTempNumberHashmap[NUMBER_YUKIHI_FLUTTERING_COLLAR] = CardName.YUKIHI_FLUTTERING_COLLAR

        cardTempNumberHashmap[NUMBER_SHINRA_SHINRA] = CardName.SHINRA_SHINRA
        cardTempNumberHashmap[NUMBER_SHINRA_IBLON] = CardName.SHINRA_IBLON
        cardTempNumberHashmap[NUMBER_SHINRA_BANLON] = CardName.SHINRA_BANLON
        cardTempNumberHashmap[NUMBER_SHINRA_KIBEN] = CardName.SHINRA_KIBEN
        cardTempNumberHashmap[NUMBER_SHINRA_INYONG] = CardName.SHINRA_INYONG
        cardTempNumberHashmap[NUMBER_SHINRA_SEONDONG] = CardName.SHINRA_SEONDONG
        cardTempNumberHashmap[NUMBER_SHINRA_JANGDAM] = CardName.SHINRA_JANGDAM
        cardTempNumberHashmap[NUMBER_SHINRA_NONPA] = CardName.SHINRA_NONPA
        cardTempNumberHashmap[NUMBER_SHINRA_WANJEON_NONPA] = CardName.SHINRA_WANJEON_NONPA
        cardTempNumberHashmap[NUMBER_SHINRA_DASIG_IHAE] = CardName.SHINRA_DASIG_IHAE
        cardTempNumberHashmap[NUMBER_SHINRA_CHEONJI_BANBAG] = CardName.SHINRA_CHEONJI_BANBAG
        cardTempNumberHashmap[NUMBER_SHINRA_SAMRA_BAN_SHO] = CardName.SHINRA_SAMRA_BAN_SHO
        cardTempNumberHashmap[NUMBER_SHINRA_ZHEN_YEN] = CardName.SHINRA_ZHEN_YEN
        cardTempNumberHashmap[NUMBER_SHINRA_SA_DO] = CardName.SHINRA_SA_DO
        cardTempNumberHashmap[NUMBER_SHINRA_ZEN_CHI_KYO_TEN] = CardName.SHINRA_ZEN_CHI_KYO_TEN

        cardTempNumberHashmap[NUMBER_HAGANE_CENTRIFUGAL_ATTACK] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardTempNumberHashmap[NUMBER_HAGANE_FOUR_WINDED_EARTHQUAKE] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardTempNumberHashmap[NUMBER_HAGANE_GROUND_BREAKING] = CardName.HAGANE_GROUND_BREAKING
        cardTempNumberHashmap[NUMBER_HAGANE_HYPER_RECOIL] = CardName.HAGANE_HYPER_RECOIL
        cardTempNumberHashmap[NUMBER_HAGANE_WON_MU_RUYN] = CardName.HAGANE_WON_MU_RUYN
        cardTempNumberHashmap[NUMBER_HAGANE_RING_A_BELL] = CardName.HAGANE_RING_A_BELL
        cardTempNumberHashmap[NUMBER_HAGANE_GRAVITATION_FIELD] = CardName.HAGANE_GRAVITATION_FIELD
        cardTempNumberHashmap[NUMBER_HAGANE_GRAND_SKY_HOLE_CRASH] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardTempNumberHashmap[NUMBER_HAGANE_GRAND_BELL_MEGALOBEL] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardTempNumberHashmap[NUMBER_HAGANE_GRAND_GRAVITATION_ATTRACT] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardTempNumberHashmap[NUMBER_HAGANE_GRAND_MOUNTAIN_RESPECT] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT
        cardTempNumberHashmap[NUMBER_HAGANE_BONFIRE] = CardName.HAGANE_BONFIRE
        cardTempNumberHashmap[NUMBER_HAGANE_WHEEL_SKILL] = CardName.HAGANE_WHEEL_SKILL
        cardTempNumberHashmap[NUMBER_HAGANE_GRAND_SOFT_MATERIAL] = CardName.HAGANE_GRAND_SOFT_MATERIAL
        cardTempNumberHashmap[NUMBER_HAGANE_SOFT_ATTACK] = CardName.HAGANE_SOFT_ATTACK

        cardTempNumberHashmap[NUMBER_CHIKAGE_THROW_KUNAI] = CardName.CHIKAGE_THROW_KUNAI
        cardTempNumberHashmap[NUMBER_CHIKAGE_POISON_NEEDLE] = CardName.CHIKAGE_POISON_NEEDLE
        cardTempNumberHashmap[NUMBER_CHIKAGE_TO_ZU_CHU] = CardName.CHIKAGE_TO_ZU_CHU
        cardTempNumberHashmap[NUMBER_CHIKAGE_CUTTING_NECK] = CardName.CHIKAGE_CUTTING_NECK
        cardTempNumberHashmap[NUMBER_CHIKAGE_POISON_SMOKE] = CardName.CHIKAGE_POISON_SMOKE
        cardTempNumberHashmap[NUMBER_CHIKAGE_TIP_TOEING] = CardName.CHIKAGE_TIP_TOEING
        cardTempNumberHashmap[NUMBER_CHIKAGE_MUDDLE] = CardName.CHIKAGE_MUDDLE
        cardTempNumberHashmap[NUMBER_CHIKAGE_DEADLY_POISON] = CardName.CHIKAGE_DEADLY_POISON
        cardTempNumberHashmap[NUMBER_CHIKAGE_HAN_KI_POISON] = CardName.CHIKAGE_HAN_KI_POISON
        cardTempNumberHashmap[NUMBER_CHIKAGE_REINCARNATION_POISON] = CardName.CHIKAGE_REINCARNATION_POISON
        cardTempNumberHashmap[NUMBER_CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
        cardTempNumberHashmap[NUMBER_CHIKAGE_TRICK_UMBRELLA] = CardName.CHIKAGE_TRICK_UMBRELLA
        cardTempNumberHashmap[NUMBER_CHIKAGE_STRUGGLE] = CardName.CHIKAGE_STRUGGLE
        cardTempNumberHashmap[NUMBER_CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON] = CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON
        cardTempNumberHashmap[NUMBER_POISON_PARALYTIC] = CardName.POISON_PARALYTIC
        cardTempNumberHashmap[NUMBER_POISON_HALLUCINOGENIC] = CardName.POISON_HALLUCINOGENIC
        cardTempNumberHashmap[NUMBER_POISON_RELAXATION] = CardName.POISON_RELAXATION
        cardTempNumberHashmap[NUMBER_POISON_DEADLY_1] = CardName.POISON_DEADLY_1
        cardTempNumberHashmap[NUMBER_POISON_DEADLY_2] = CardName.POISON_DEADLY_2
        cardTempNumberHashmap[NUMBER_CHIKAGE_HIDDEN_WEAPON] = CardName.CHIKAGE_HIDDEN_WEAPON

        cardTempNumberHashmap[NUMBER_KURURU_ELEKITTEL] = CardName.KURURU_ELEKITTEL
        cardTempNumberHashmap[NUMBER_KURURU_ACCELERATOR] = CardName.KURURU_ACCELERATOR
        cardTempNumberHashmap[NUMBER_KURURU_KURURUOONG] = CardName.KURURU_KURURUOONG
        cardTempNumberHashmap[NUMBER_KURURU_TORNADO] = CardName.KURURU_TORNADO
        cardTempNumberHashmap[NUMBER_KURURU_REGAINER] = CardName.KURURU_REGAINER
        cardTempNumberHashmap[NUMBER_KURURU_MODULE] = CardName.KURURU_MODULE
        cardTempNumberHashmap[NUMBER_KURURU_REFLECTOR] = CardName.KURURU_REFLECTOR
        cardTempNumberHashmap[NUMBER_KURURU_DRAIN_DEVIL] = CardName.KURURU_DRAIN_DEVIL
        cardTempNumberHashmap[NUMBER_KURURU_BIG_GOLEM] = CardName.KURURU_BIG_GOLEM
        cardTempNumberHashmap[NUMBER_KURURU_INDUSTRIA] = CardName.KURURU_INDUSTRIA
        cardTempNumberHashmap[NUMBER_KURURU_DUPLICATED_GEAR_1] = CardName.KURURU_DUPLICATED_GEAR_1
        cardTempNumberHashmap[NUMBER_KURURU_DUPLICATED_GEAR_2] = CardName.KURURU_DUPLICATED_GEAR_2
        cardTempNumberHashmap[NUMBER_KURURU_DUPLICATED_GEAR_3] = CardName.KURURU_DUPLICATED_GEAR_3
        cardTempNumberHashmap[NUMBER_KURURU_KANSHOUSOUCHI_KURURUSIK] = CardName.KURURU_KANSHOUSOUCHI_KURURUSIK
        cardTempNumberHashmap[NUMBER_KURURU_ANALYZE] = CardName.KURURU_ANALYZE
        cardTempNumberHashmap[NUMBER_KURURU_DAUZING] = CardName.KURURU_DAUZING
        cardTempNumberHashmap[NUMBER_KURURU_LAST_RESEARCH] = CardName.KURURU_LAST_RESEARCH
        cardTempNumberHashmap[NUMBER_KURURU_GRAND_GULLIVER] = CardName.KURURU_GRAND_GULLIVER
        cardTempNumberHashmap[NUMBER_KURURU_BLASTER] = CardName.KURURU_BLASTER
        cardTempNumberHashmap[NUMBER_KURURU_RAILGUN] = CardName.KURURU_RAILGUN
        cardTempNumberHashmap[NUMBER_KURURU_CONNECT_DIVE] = CardName.KURURU_CONNECT_DIVE

        cardTempNumberHashmap[NUMBER_THALLYA_BURNING_STEAM] = CardName.THALLYA_BURNING_STEAM
        cardTempNumberHashmap[NUMBER_THALLYA_WAVING_EDGE] = CardName.THALLYA_WAVING_EDGE
        cardTempNumberHashmap[NUMBER_THALLYA_SHIELD_CHARGE] = CardName.THALLYA_SHIELD_CHARGE
        cardTempNumberHashmap[NUMBER_THALLYA_STEAM_CANNON] = CardName.THALLYA_STEAM_CANNON
        cardTempNumberHashmap[NUMBER_THALLYA_STUNT] = CardName.THALLYA_STUNT
        cardTempNumberHashmap[NUMBER_THALLYA_ROARING] = CardName.THALLYA_ROARING
        cardTempNumberHashmap[NUMBER_THALLYA_TURBO_SWITCH] = CardName.THALLYA_TURBO_SWITCH
        cardTempNumberHashmap[NUMBER_THALLYA_ALPHA_EDGE] = CardName.THALLYA_ALPHA_EDGE
        cardTempNumberHashmap[NUMBER_THALLYA_OMEGA_BURST] = CardName.THALLYA_OMEGA_BURST
        cardTempNumberHashmap[NUMBER_THALLYA_THALLYA_MASTERPIECE] = CardName.THALLYA_THALLYA_MASTERPIECE
        cardTempNumberHashmap[NUMBER_THALLYA_JULIA_BLACKBOX] = CardName.THALLYA_JULIA_BLACKBOX
        cardTempNumberHashmap[NUMBER_FORM_YAKSHA] = CardName.FORM_YAKSHA
        cardTempNumberHashmap[NUMBER_FORM_NAGA] = CardName.FORM_NAGA
        cardTempNumberHashmap[NUMBER_FORM_GARUDA] = CardName.FORM_GARUDA
        cardTempNumberHashmap[NUMBER_THALLYA_QUICK_CHANGE] = CardName.THALLYA_QUICK_CHANGE
        cardTempNumberHashmap[NUMBER_THALLYA_BLACKBOX_NEO] = CardName.THALLYA_BLACKBOX_NEO
        cardTempNumberHashmap[NUMBER_THALLYA_OMNIS_BLASTER] = CardName.THALLYA_OMNIS_BLASTER
        cardTempNumberHashmap[NUMBER_FORM_KINNARI] = CardName.FORM_KINNARI
        cardTempNumberHashmap[NUMBER_FORM_ASURA] = CardName.FORM_ASURA
        cardTempNumberHashmap[NUMBER_FORM_DEVA] = CardName.FORM_DEVA

        cardTempNumberHashmap[NUMBER_RAIRA_BEAST_NAIL] = CardName.RAIRA_BEAST_NAIL
        cardTempNumberHashmap[NUMBER_RAIRA_STORM_SURGE_ATTACK] = CardName.RAIRA_STORM_SURGE_ATTACK
        cardTempNumberHashmap[NUMBER_RAIRA_REINCARNATION_NAIL] = CardName.RAIRA_REINCARNATION_NAIL
        cardTempNumberHashmap[NUMBER_RAIRA_WIND_RUN] = CardName.RAIRA_WIND_RUN
        cardTempNumberHashmap[NUMBER_RAIRA_WISDOM_OF_STORM_SURGE] = CardName.RAIRA_WISDOM_OF_STORM_SURGE
        cardTempNumberHashmap[NUMBER_RAIRA_HOWLING] = CardName.RAIRA_HOWLING
        cardTempNumberHashmap[NUMBER_RAIRA_WIND_KICK] = CardName.RAIRA_WIND_KICK
        cardTempNumberHashmap[NUMBER_RAIRA_THUNDER_WIND_PUNCH] = CardName.RAIRA_THUNDER_WIND_PUNCH
        cardTempNumberHashmap[NUMBER_RAIRA_SUMMON_THUNDER] = CardName.RAIRA_SUMMON_THUNDER
        cardTempNumberHashmap[NUMBER_RAIRA_WIND_CONSEQUENCE_BALL] = CardName.RAIRA_WIND_CONSEQUENCE_BALL
        cardTempNumberHashmap[NUMBER_RAIRA_CIRCULAR_CIRCUIT] = CardName.RAIRA_CIRCULAR_CIRCUIT
        cardTempNumberHashmap[NUMBER_RAIRA_WIND_ATTACK] = CardName.RAIRA_WIND_ATTACK
        cardTempNumberHashmap[NUMBER_RAIRA_WIND_ZEN_KAI] = CardName.RAIRA_WIND_ZEN_KAI
        cardTempNumberHashmap[NUMBER_RAIRA_WIND_CELESTIAL_SPHERE] = CardName.RAIRA_WIND_CELESTIAL_SPHERE
        cardTempNumberHashmap[NUMBER_RAIRA_STORM] = CardName.RAIRA_STORM
        cardTempNumberHashmap[NUMBER_RAIRA_FURIOUS_STORM] = CardName.RAIRA_FURIOUS_STORM
        cardTempNumberHashmap[NUMBER_RAIRA_JIN_PUNG_JE_CHEON_UI] = CardName.RAIRA_JIN_PUNG_JE_CHEON_UI

        cardTempNumberHashmap[NUMBER_UTSURO_WON_WOL] = CardName.UTSURO_WON_WOL
        cardTempNumberHashmap[NUMBER_UTSURO_BLACK_WAVE] = CardName.UTSURO_BLACK_WAVE
        cardTempNumberHashmap[NUMBER_UTSURO_HARVEST] = CardName.UTSURO_HARVEST
        cardTempNumberHashmap[NUMBER_UTSURO_PRESSURE] = CardName.UTSURO_PRESSURE
        cardTempNumberHashmap[NUMBER_UTSURO_SHADOW_WING] = CardName.UTSURO_SHADOW_WING
        cardTempNumberHashmap[NUMBER_UTSURO_SHADOW_WALL] = CardName.UTSURO_SHADOW_WALL
        cardTempNumberHashmap[NUMBER_UTSURO_YUE_HOE_JU] = CardName.UTSURO_YUE_HOE_JU
        cardTempNumberHashmap[NUMBER_UTSURO_HOE_MYEOL] = CardName.UTSURO_HOE_MYEOL
        cardTempNumberHashmap[NUMBER_UTSURO_HEO_WI] = CardName.UTSURO_HEO_WI
        cardTempNumberHashmap[NUMBER_UTSURO_JONG_MAL] = CardName.UTSURO_JONG_MAL
        cardTempNumberHashmap[NUMBER_UTSURO_MA_SIG] = CardName.UTSURO_MA_SIG
        cardTempNumberHashmap[NUMBER_UTSURO_BITE_DUST] = CardName.UTSURO_BITE_DUST
        cardTempNumberHashmap[NUMBER_UTSURO_REVERBERATE_DEVICE_KURURUSIK] = CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK
        cardTempNumberHashmap[NUMBER_UTSURO_MANG_A] = CardName.UTSURO_MANG_A
        cardTempNumberHashmap[NUMBER_UTSURO_ANNIHILATION_SHADOW] = CardName.UTSURO_ANNIHILATION_SHADOW
        cardTempNumberHashmap[NUMBER_UTSURO_SILENT_WALK] = CardName.UTSURO_SILENT_WALK
        cardTempNumberHashmap[NUMBER_UTSURO_DE_MISE] = CardName.UTSURO_DE_MISE

        cardTempNumberHashmap[NUMBER_HONOKA_SPIRIT_SIK] = CardName.HONOKA_SPIRIT_SIK
        cardTempNumberHashmap[NUMBER_HONOKA_GUARDIAN_SPIRIT_SIK] = CardName.HONOKA_GUARDIAN_SPIRIT_SIK
        cardTempNumberHashmap[NUMBER_HONOKA_ASSAULT_SPIRIT_SIK] = CardName.HONOKA_ASSAULT_SPIRIT_SIK
        cardTempNumberHashmap[NUMBER_HONOKA_DIVINE_OUKA] = CardName.HONOKA_DIVINE_OUKA
        cardTempNumberHashmap[NUMBER_HONOKA_SAKURA_BLIZZARD] = CardName.HONOKA_SAKURA_BLIZZARD
        cardTempNumberHashmap[NUMBER_HONOKA_UI_GI_GONG_JIN] = CardName.HONOKA_UI_GI_GONG_JIN
        cardTempNumberHashmap[NUMBER_HONOKA_SAKURA_WING] = CardName.HONOKA_SAKURA_WING
        cardTempNumberHashmap[NUMBER_HONOKA_REGENERATION] = CardName.HONOKA_REGENERATION
        cardTempNumberHashmap[NUMBER_HONOKA_SAKURA_AMULET] = CardName.HONOKA_SAKURA_AMULET
        cardTempNumberHashmap[NUMBER_HONOKA_HONOKA_SPARKLE] = CardName.HONOKA_HONOKA_SPARKLE
        cardTempNumberHashmap[NUMBER_HONOKA_COMMAND] = CardName.HONOKA_COMMAND
        cardTempNumberHashmap[NUMBER_HONOKA_TAIL_WIND] = CardName.HONOKA_TAIL_WIND
        cardTempNumberHashmap[NUMBER_HONOKA_CHEST_WILLINGNESS] = CardName.HONOKA_CHEST_WILLINGNESS
        cardTempNumberHashmap[NUMBER_HONOKA_HAND_FLOWER] = CardName.HONOKA_HAND_FLOWER
        cardTempNumberHashmap[NUMBER_HONOKA_A_NEW_OPENING] = CardName.HONOKA_A_NEW_OPENING
        cardTempNumberHashmap[NUMBER_HONOKA_UNDER_THE_NAME_OF_FLAG] = CardName.HONOKA_UNDER_THE_NAME_OF_FLAG
        cardTempNumberHashmap[NUMBER_HONOKA_FOUR_SEASON_BACK] = CardName.HONOKA_FOUR_SEASON_BACK
        cardTempNumberHashmap[NUMBER_HONOKA_FULL_BLOOM_PATH] = CardName.HONOKA_FULL_BLOOM_PATH
        cardTempNumberHashmap[NUMBER_HONOKA_SAKURA_SWORD] = CardName.HONOKA_SAKURA_SWORD
        cardTempNumberHashmap[NUMBER_HONOKA_SHADOW_HAND] = CardName.HONOKA_SHADOW_HAND
        cardTempNumberHashmap[NUMBER_HONOKA_EYE_OPEN_ALONE] = CardName.HONOKA_EYE_OPEN_ALONE
        cardTempNumberHashmap[NUMBER_HONOKA_FOLLOW_TRACE] = CardName.HONOKA_FOLLOW_TRACE
        cardTempNumberHashmap[NUMBER_HONOKA_FACING_SHADOW] = CardName.HONOKA_FACING_SHADOW
        cardTempNumberHashmap[NUMBER_HONOKA_SAKURA_SHINING_BRIGHTLY] = CardName.HONOKA_SAKURA_SHINING_BRIGHTLY
        cardTempNumberHashmap[NUMBER_HONOKA_HOLD_HANDS] = CardName.HONOKA_HOLD_HANDS
        cardTempNumberHashmap[NUMBER_HONOKA_WALK_OLD_LOAD] = CardName.HONOKA_WALK_OLD_LOAD

        cardTempNumberHashmap[NUMBER_KORUNU_SNOW_BLADE] = CardName.KORUNU_SNOW_BLADE
        cardTempNumberHashmap[NUMBER_KORUNU_REVOLVING_BLADE] = CardName.KORUNU_REVOLVING_BLADE
        cardTempNumberHashmap[NUMBER_KORUNU_BLADE_DANCE] = CardName.KORUNU_BLADE_DANCE
        cardTempNumberHashmap[NUMBER_KORUNU_RIDE_SNOW] = CardName.KORUNU_RIDE_SNOW
        cardTempNumberHashmap[NUMBER_KORUNU_ABSOLUTE_ZERO] = CardName.KORUNU_ABSOLUTE_ZERO
        cardTempNumberHashmap[NUMBER_KORUNU_FROSTBITE] = CardName.KORUNU_FROSTBITE
        cardTempNumberHashmap[NUMBER_KORUNU_FROST_THORN_BUSH] = CardName.KORUNU_FROST_THORN_BUSH
        cardTempNumberHashmap[NUMBER_KORUNU_CONLU_RUYANPEH] = CardName.KORUNU_CONLU_RUYANPEH
        cardTempNumberHashmap[NUMBER_KORUNU_LETAR_LERA] = CardName.KORUNU_LETAR_LERA
        cardTempNumberHashmap[NUMBER_KORUNU_UPASTUM] = CardName.KORUNU_UPASTUM
        cardTempNumberHashmap[NUMBER_KORUNU_PORUCHARTO] = CardName.KORUNU_PORUCHARTO

        cardTempNumberHashmap[NUMBER_YATSUHA_STAR_NAIL] = CardName.YATSUHA_STAR_NAIL
        cardTempNumberHashmap[NUMBER_YATSUHA_DARKNESS_GILL] = CardName.YATSUHA_DARKNESS_GILL
        cardTempNumberHashmap[NUMBER_YATSUHA_MIRROR_DEVIL] = CardName.YATSUHA_MIRROR_DEVIL
        cardTempNumberHashmap[NUMBER_YATSUHA_GHOST_STEP] = CardName.YATSUHA_GHOST_STEP
        cardTempNumberHashmap[NUMBER_YATSUHA_WILLING] = CardName.YATSUHA_WILLING
        cardTempNumberHashmap[NUMBER_YATSUHA_CONTRACT] = CardName.YATSUHA_CONTRACT
        cardTempNumberHashmap[NUMBER_YATSUHA_CLINGY_FLOWER] = CardName.YATSUHA_CLINGY_FLOWER
        cardTempNumberHashmap[NUMBER_YATSUHA_TWO_LEAP_MIRROR_DIVINE] = CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE
        cardTempNumberHashmap[NUMBER_YATSUHA_FOUR_LEAP_SONG] = CardName.YATSUHA_FOUR_LEAP_SONG
        cardTempNumberHashmap[NUMBER_YATSUHA_SIX_STAR_SEA] = CardName.YATSUHA_SIX_STAR_SEA
        cardTempNumberHashmap[NUMBER_YATSUHA_EIGHT_MIRROR_OTHER_SIDE] = CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE
        cardTempNumberHashmap[NUMBER_YATSUHA_HOLY_RAKE_HANDS] = CardName.YATSUHA_HOLY_RAKE_HANDS
        cardTempNumberHashmap[NUMBER_YATSUHA_ENTRANCE_OF_ABYSS] = CardName.YATSUHA_ENTRANCE_OF_ABYSS
        cardTempNumberHashmap[NUMBER_YATSUHA_TRUE_MONSTER] = CardName.YATSUHA_TRUE_MONSTER
        cardTempNumberHashmap[NUMBER_YATSUHA_GHOST_LINK] = CardName.YATSUHA_GHOST_LINK
        cardTempNumberHashmap[NUMBER_YATSUHA_RESOLUTION] = CardName.YATSUHA_RESOLUTION
        cardTempNumberHashmap[NUMBER_YATSUHA_PLEDGE] = CardName.YATSUHA_PLEDGE
        cardTempNumberHashmap[NUMBER_YATSUHA_VAIN_FLOWER] = CardName.YATSUHA_VAIN_FLOWER
        cardTempNumberHashmap[NUMBER_YATSUHA_EIGHT_MIRROR_VAIN_SAKURA] = CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA
        cardTempNumberHashmap[NUMBER_YATSUHA_UNFAMILIAR_WORLD] = CardName.YATSUHA_UNFAMILIAR_WORLD
        cardTempNumberHashmap[NUMBER_YATSUHA_COLORED_WORLD] = CardName.YATSUHA_COLORED_WORLD
        cardTempNumberHashmap[NUMBER_YATSUHA_SHES_CHERRY_BLOSSOM_WORLD] = CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD
        cardTempNumberHashmap[NUMBER_YATSUHA_SHES_EGO_AND_DETERMINATION] = CardName.YATSUHA_SHES_EGO_AND_DETERMINATION

        cardTempNumberHashmap[NUMBER_HATSUMI_WATER_BALL] = CardName.HATSUMI_WATER_BALL
        cardTempNumberHashmap[NUMBER_HATSUMI_WATER_CURRENT] = CardName.HATSUMI_WATER_CURRENT
        cardTempNumberHashmap[NUMBER_HATSUMI_STRONG_ACID] = CardName.HATSUMI_STRONG_ACID
        cardTempNumberHashmap[NUMBER_HATSUMI_TSUNAMI] = CardName.HATSUMI_TSUNAMI
        cardTempNumberHashmap[NUMBER_HATSUMI_JUN_BI_MAN_TAN] = CardName.HATSUMI_JUN_BI_MAN_TAN
        cardTempNumberHashmap[NUMBER_HATSUMI_COMPASS] = CardName.HATSUMI_COMPASS
        cardTempNumberHashmap[NUMBER_HATSUMI_CALL_WAVE] = CardName.HATSUMI_CALL_WAVE
        cardTempNumberHashmap[NUMBER_HATSUMI_ISANA_HAIL] = CardName.HATSUMI_ISANA_HAIL
        cardTempNumberHashmap[NUMBER_HATSUMI_OYOGIBI_FIRE] = CardName.HATSUMI_OYOGIBI_FIRE
        cardTempNumberHashmap[NUMBER_HATSUMI_KIRAHARI_LIGHTHOUSE] = CardName.HATSUMI_KIRAHARI_LIGHTHOUSE
        cardTempNumberHashmap[NUMBER_HATSUMI_MIOBIKI_ROUTE] = CardName.HATSUMI_MIOBIKI_ROUTE
        cardTempNumberHashmap[NUMBER_HATSUMI_TORPEDO] = CardName.HATSUMI_TORPEDO
        cardTempNumberHashmap[NUMBER_HATSUMI_SAGIRI_HAIL] = CardName.HATSUMI_SAGIRI_HAIL
        cardTempNumberHashmap[NUMBER_HATSUMI_WADANAKA_ROUTE] = CardName.HATSUMI_WADANAKA_ROUTE

        cardTempNumberHashmap[NUMBER_MIZUKI_JIN_DU] = CardName.MIZUKI_JIN_DU
        cardTempNumberHashmap[NUMBER_MIZUKI_BAN_GONG] = CardName.MIZUKI_BAN_GONG
        cardTempNumberHashmap[NUMBER_MIZUKI_SHOOTING_DOWN] = CardName.MIZUKI_SHOOTING_DOWN
        cardTempNumberHashmap[NUMBER_MIZUKI_HO_LYEONG] = CardName.MIZUKI_HO_LYEONG
        cardTempNumberHashmap[NUMBER_MIZUKI_BANG_BYEOG] = CardName.MIZUKI_BANG_BYEOG
        cardTempNumberHashmap[NUMBER_MIZUKI_OVERPOWERING_GO_FORWARD] = CardName.MIZUKI_OVERPOWERING_GO_FORWARD
        cardTempNumberHashmap[NUMBER_MIZUKI_JEON_JANG] = CardName.MIZUKI_JEON_JANG
        cardTempNumberHashmap[NUMBER_MIZUKI_HACHIRYU_CHEONJUGAK] = CardName.MIZUKI_HACHIRYU_CHEONJUGAK
        cardTempNumberHashmap[NUMBER_MIZUKI_HIJAMARU_TRIPLET] = CardName.MIZUKI_HIJAMARU_TRIPLET
        cardTempNumberHashmap[NUMBER_MIZUKI_TARTENASHI_DAESUMUN] = CardName.MIZUKI_TARTENASHI_DAESUMUN
        cardTempNumberHashmap[NUMBER_MIZUKI_MIZUKI_BATTLE_CRY] = CardName.MIZUKI_MIZUKI_BATTLE_CRY
        cardTempNumberHashmap[NUMBER_KODAMA_TU_SIN] = CardName.KODAMA_TU_SIN
        cardTempNumberHashmap[NUMBER_SOLDIER_SPEAR_1] = CardName.SOLDIER_SPEAR_1
        cardTempNumberHashmap[NUMBER_SOLDIER_SPEAR_2] = CardName.SOLDIER_SPEAR_2
        cardTempNumberHashmap[NUMBER_SOLDIER_SHIELD] = CardName.SOLDIER_SHIELD
        cardTempNumberHashmap[NUMBER_SOLDIER_HORSE] = CardName.SOLDIER_HORSE

        cardTempNumberHashmap[NUMBER_MEGUMI_GONG_SUM] = CardName.MEGUMI_GONG_SUM
        cardTempNumberHashmap[NUMBER_MEGUMI_TA_CHEOG] = CardName.MEGUMI_TA_CHEOG
        cardTempNumberHashmap[NUMBER_MEGUMI_SHELL_ATTACK] = CardName.MEGUMI_SHELL_ATTACK
        cardTempNumberHashmap[NUMBER_MEGUMI_POLE_THRUST] = CardName.MEGUMI_POLE_THRUST
        cardTempNumberHashmap[NUMBER_MEGUMI_REED] = CardName.MEGUMI_REED
        cardTempNumberHashmap[NUMBER_MEGUMI_BALSAM] = CardName.MEGUMI_BALSAM
        cardTempNumberHashmap[NUMBER_MEGUMI_WILD_ROSE] = CardName.MEGUMI_WILD_ROSE
        cardTempNumberHashmap[NUMBER_MEGUMI_ROOT_OF_CAUSALITY] = CardName.MEGUMI_ROOT_OF_CAUSALITY
        cardTempNumberHashmap[NUMBER_MEGUMI_BRANCH_OF_POSSIBILITY] = CardName.MEGUMI_BRANCH_OF_POSSIBILITY
        cardTempNumberHashmap[NUMBER_MEGUMI_FRUIT_OF_END] = CardName.MEGUMI_FRUIT_OF_END
        cardTempNumberHashmap[NUMBER_MEGUMI_MEGUMI_PALM] = CardName.MEGUMI_MEGUMI_PALM

        cardTempNumberHashmap[NUMBER_KANAWE_IMAGE] = CardName.KANAWE_IMAGE
        cardTempNumberHashmap[NUMBER_KANAWE_SCREENPLAY] = CardName.KANAWE_SCREENPLAY
        cardTempNumberHashmap[NUMBER_KANAWE_PRODUCTION] = CardName.KANAWE_PRODUCTION
        cardTempNumberHashmap[NUMBER_KANAWE_PUBLISH] = CardName.KANAWE_PUBLISH
        cardTempNumberHashmap[NUMBER_KANAWE_AFTERGLOW] = CardName.KANAWE_AFTERGLOW
        cardTempNumberHashmap[NUMBER_KANAWE_IMPROMPTU] = CardName.KANAWE_IMPROMPTU
        cardTempNumberHashmap[NUMBER_KANAWE_SEAL] = CardName.KANAWE_SEAL
        cardTempNumberHashmap[NUMBER_KANAWE_VAGUE_STORY] = CardName.KANAWE_VAGUE_STORY
        cardTempNumberHashmap[NUMBER_KANAWE_INFINITE_STARLIGHT] = CardName.KANAWE_INFINITE_STARLIGHT
        cardTempNumberHashmap[NUMBER_KANAWE_BEND_OVER_THIS_NIGHT] = CardName.KANAWE_BEND_OVER_THIS_NIGHT
        cardTempNumberHashmap[NUMBER_KANAWE_DISTANT_SKY] = CardName.KANAWE_DISTANT_SKY
        cardTempNumberHashmap[NUMBER_KANAWE_KANAWE] = CardName.KANAWE_KANAWE

        cardTempNumberHashmap[NUMBER_IDEA_SAL_JIN] = CardName.IDEA_SAL_JIN
        cardTempNumberHashmap[NUMBER_IDEA_SAKURA_WAVE] = CardName.IDEA_SAKURA_WAVE
        cardTempNumberHashmap[NUMBER_IDEA_WHISTLE] = CardName.IDEA_WHISTLE
        cardTempNumberHashmap[NUMBER_IDEA_MYEONG_JEON] = CardName.IDEA_MYEONG_JEON
        cardTempNumberHashmap[NUMBER_IDEA_EMPHASIZING] = CardName.IDEA_EMPHASIZING
        cardTempNumberHashmap[NUMBER_IDEA_POSITIONING] = CardName.IDEA_POSITIONING

        cardTempNumberHashmap[NUMBER_KAMUWI_RED_BLADE] = CardName.KAMUWI_RED_BLADE
        cardTempNumberHashmap[NUMBER_KAMUWI_FLUTTERING_BLADE] = CardName.KAMUWI_FLUTTERING_BLADE
        cardTempNumberHashmap[NUMBER_KAMUWI_SI_KEN_LAN_JIN] = CardName.KAMUWI_SI_KEN_LAN_JIN
        cardTempNumberHashmap[NUMBER_KAMUWI_CUT_DOWN] = CardName.KAMUWI_CUT_DOWN
        cardTempNumberHashmap[NUMBER_KAMUWI_THREADING_THORN] = CardName.KAMUWI_THREADING_THORN
        cardTempNumberHashmap[NUMBER_KAMUWI_KE_SYO_LAN_LYU] = CardName.KAMUWI_KE_SYO_LAN_LYU
        cardTempNumberHashmap[NUMBER_KAMUWI_BLOOD_WAVE] = CardName.KAMUWI_BLOOD_WAVE
        cardTempNumberHashmap[NUMBER_KAMUWI_LAMP] = CardName.KAMUWI_LAMP
        cardTempNumberHashmap[NUMBER_KAMUWI_DAWN] = CardName.KAMUWI_DAWN
        cardTempNumberHashmap[NUMBER_KAMUWI_GRAVEYARD] = CardName.KAMUWI_GRAVEYARD
        cardTempNumberHashmap[NUMBER_KAMUWI_KATA_SHIRO] = CardName.KAMUWI_KATA_SHIRO
        cardTempNumberHashmap[NUMBER_KAMUWI_LOGIC] = CardName.KAMUWI_LOGIC

        cardTempNumberHashmap[NUMBER_RENRI_FALSE_STAB] = CardName.RENRI_FALSE_STAB
        cardTempNumberHashmap[NUMBER_RENRI_TEMPORARY_EXPEDIENT] = CardName.RENRI_TEMPORARY_EXPEDIENT
        cardTempNumberHashmap[NUMBER_RENRI_BLACK_AND_WHITE] = CardName.RENRI_BLACK_AND_WHITE
        cardTempNumberHashmap[NUMBER_RENRI_IRRITATING_GESTURE] = CardName.RENRI_IRRITATING_GESTURE
        cardTempNumberHashmap[NUMBER_RENRI_FLOATING_CLOUDS] = CardName.RENRI_FLOATING_CLOUDS
        cardTempNumberHashmap[NUMBER_RENRI_FISHING] = CardName.RENRI_FISHING
        cardTempNumberHashmap[NUMBER_RENRI_PULLING_FISHING] = CardName.RENRI_PULLING_FISHING
        cardTempNumberHashmap[NUMBER_RENRI_RU_RU_RA_RA_RI] = CardName.RENRI_RU_RU_RA_RA_RI
        cardTempNumberHashmap[NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA] = CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA
        cardTempNumberHashmap[NUMBER_RENRI_O_RI_RE_TE_RA_RE_RU] = CardName.RENRI_O_RI_RE_TE_RA_RE_RU
        cardTempNumberHashmap[NUMBER_RENRI_RENRI_THE_END] = CardName.RENRI_RENRI_THE_END
        cardTempNumberHashmap[NUMBER_RENRI_ENGRAVED_GARMENT] = CardName.RENRI_ENGRAVED_GARMENT
        cardTempNumberHashmap[NUMBER_KIRIKO_SHAMANISTIC_MUSIC] = CardName.KIRIKO_SHAMANISTIC_MUSIC
        cardTempNumberHashmap[NUMBER_RENRI_DECEPTION_FOG] = CardName.RENRI_DECEPTION_FOG
        cardTempNumberHashmap[NUMBER_RENRI_SIN_SOO] = CardName.RENRI_SIN_SOO
        cardTempNumberHashmap[NUMBER_RENRI_FALSE_WEAPON] = CardName.RENRI_FALSE_WEAPON
        cardTempNumberHashmap[NUMBER_RENRI_ESSENCE_OF_BLADE] = CardName.RENRI_ESSENCE_OF_BLADE
        cardTempNumberHashmap[NUMBER_RENRI_FIRST_SAKURA_ORDER] = CardName.RENRI_FIRST_SAKURA_ORDER
        cardTempNumberHashmap[NUMBER_RENRI_RI_RA_RU_RI_RA_RO] = CardName.RENRI_RI_RA_RU_RI_RA_RO

        cardTempNumberHashmap[NUMBER_AKINA_ABACUS_STONE] = CardName.AKINA_ABACUS_STONE
        cardTempNumberHashmap[NUMBER_AKINA_THREAT] = CardName.AKINA_THREAT
        cardTempNumberHashmap[NUMBER_AKINA_TRADE] = CardName.AKINA_TRADE
        cardTempNumberHashmap[NUMBER_AKINA_SPECULATION] = CardName.AKINA_SPECULATION
        cardTempNumberHashmap[NUMBER_AKINA_CALC] = CardName.AKINA_CALC
        cardTempNumberHashmap[NUMBER_AKINA_TURN_OFF_TABLE] = CardName.AKINA_TURN_OFF_TABLE
        cardTempNumberHashmap[NUMBER_AKINA_DIRECT_FINANCING] = CardName.AKINA_DIRECT_FINANCING
        cardTempNumberHashmap[NUMBER_AKINA_OPEN_CUTTING_METHOD] = CardName.AKINA_OPEN_CUTTING_METHOD
        cardTempNumberHashmap[NUMBER_AKINA_SU_LYO_SUL] = CardName.AKINA_SU_LYO_SUL
        cardTempNumberHashmap[NUMBER_AKINA_AKINA_ACCURATE_CALC] = CardName.AKINA_AKINA_ACCURATE_CALC
        cardTempNumberHashmap[NUMBER_AKINA_AKINA_ACCURATE_CALC_START_PHASE] = CardName.AKINA_AKINA_ACCURATE_CALC

        cardTempNumberHashmap[NUMBER_SHISUI_SAW_BLADE_CUT_DOWN] = CardName.SHISUI_SAW_BLADE_CUT_DOWN
        cardTempNumberHashmap[NUMBER_SHISUI_PENETRATE_SAW_BLADE] = CardName.SHISUI_PENETRATE_SAW_BLADE
        cardTempNumberHashmap[NUMBER_SHISUI_REBELLION_ATTACK] = CardName.SHISUI_REBELLION_ATTACK
        cardTempNumberHashmap[NUMBER_SHISUI_IRON_RESISTANCE] = CardName.SHISUI_IRON_RESISTANCE
        cardTempNumberHashmap[NUMBER_SHISUI_THORNY_PATH] = CardName.SHISUI_THORNY_PATH
        cardTempNumberHashmap[NUMBER_SHISUI_IRON_POWDER_WIND_AROUND] = CardName.SHISUI_IRON_POWDER_WIND_AROUND
        cardTempNumberHashmap[NUMBER_SHISUI_BLACK_ARMOR] = CardName.SHISUI_BLACK_ARMOR
        cardTempNumberHashmap[NUMBER_SHISUI_PADMA_CUT_DOWN] = CardName.SHISUI_PADMA_CUT_DOWN
        cardTempNumberHashmap[NUMBER_SHISUI_UPALA_TEAR] = CardName.SHISUI_UPALA_TEAR
        cardTempNumberHashmap[NUMBER_SHISUI_ABUDA_EAT] = CardName.SHISUI_ABUDA_EAT
        cardTempNumberHashmap[NUMBER_SHISUI_SHISUI_PLACE_OF_DEATH] = CardName.SHISUI_SHISUI_PLACE_OF_DEATH

        cardTempNumberHashmap[NUMBER_MISORA_BOW_SPILLING] = CardName.MISORA_BOW_SPILLING
        cardTempNumberHashmap[NUMBER_MISORA_AIMING_KICK] = CardName.MISORA_AIMING_KICK
        cardTempNumberHashmap[NUMBER_MISORA_WIND_HOLE] = CardName.MISORA_WIND_HOLE
        cardTempNumberHashmap[NUMBER_MISORA_GAP_SI_EUL_SI] = CardName.MISORA_GAP_SI_EUL_SI
        cardTempNumberHashmap[NUMBER_MISORA_PRECISION] = CardName.MISORA_PRECISION
        cardTempNumberHashmap[NUMBER_MISORA_TRACKING_ATTACK] = CardName.MISORA_TRACKING_ATTACK
        cardTempNumberHashmap[NUMBER_MISORA_SKY_WING] = CardName.MISORA_SKY_WING
        cardTempNumberHashmap[NUMBER_MISORA_ENDLESS_END] = CardName.MISORA_ENDLESS_END
        cardTempNumberHashmap[NUMBER_MISORA_CLOUD_EMBROIDERED_CLOUD] = CardName.MISORA_CLOUD_EMBROIDERED_CLOUD
        cardTempNumberHashmap[NUMBER_MISORA_SHADOW_SHADY_SHADOW] = CardName.MISORA_SHADOW_SHADY_SHADOW
        cardTempNumberHashmap[NUMBER_MISORA_SKY_BEYOND_SKY] = CardName.MISORA_SKY_BEYOND_SKY


        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CARD_UNAME] = CardName.CARD_UNNAME
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_ANYTHING] = CardName.POISON_ANYTHING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_ANYTHING] = CardName.SOLDIER_ANYTHING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_PARTS_ANYTHING] = CardName.PARTS_ANYTHING

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_CHAM] = CardName.YURINA_CHAM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_ILSUM] = CardName.YURINA_ILSUM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_JARUCHIGI] = CardName.YURINA_JARUCHIGI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_GUHAB] = CardName.YURINA_GUHAB
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_GIBACK] = CardName.YURINA_GIBACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_APDO] = CardName.YURINA_APDO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_GIYENBANJO] = CardName.YURINA_GIYENBANJO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_WOLYUNGNACK] = CardName.YURINA_WOLYUNGNACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_POBARAM] = CardName.YURINA_POBARAM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_JJOCKBAE] = CardName.YURINA_JJOCKBAE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_JURUCK] = CardName.YURINA_JURUCK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_NAN_TA] = CardName.YURINA_NAN_TA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_BEAN_BULLET] = CardName.YURINA_BEAN_BULLET
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_NOT_COMPLETE_POBARAM] = CardName.YURINA_NOT_COMPLETE_POBARAM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_QUESTION_ANSWER] = CardName.YURINA_QUESTION_ANSWER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_AHUM] = CardName.YURINA_AHUM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_KANZA_DO] = CardName.YURINA_KANZA_DO

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_DOUBLEBEGI] = CardName.SAINE_DOUBLEBEGI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_HURUBEGI] = CardName.SAINE_HURUBEGI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_MOOGECHOO] = CardName.SAINE_MOOGECHOO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_GANPA] = CardName.SAINE_GANPA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_GWONYUCK] = CardName.SAINE_GWONYUCK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_CHOONGEMJUNG] = CardName.SAINE_CHOONGEMJUNG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_MOOEMBUCK] = CardName.SAINE_MOOEMBUCK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_YULDONGHOGEK] = CardName.SAINE_YULDONGHOGEK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_HANGMUNGGONGJIN] = CardName.SAINE_HANGMUNGGONGJIN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_EMMOOSHOEBING] = CardName.SAINE_EMMOOSHOEBING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_JONGGEK] = CardName.SAINE_JONGGEK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_SOUND_OF_ICE] = CardName.SAINE_SOUND_OF_ICE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_ACCOMPANIMENT] = CardName.SAINE_ACCOMPANIMENT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_DUET_TAN_JU_BING_MYEONG] = CardName.SAINE_DUET_TAN_JU_BING_MYEONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_BETRAYAL] = CardName.SAINE_BETRAYAL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_FLOWING_WALL] = CardName.SAINE_FLOWING_WALL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAINE_JEOL_CHANG_JEOL_HWA] = CardName.SAINE_JEOL_CHANG_JEOL_HWA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SAI_TOKO_ENSEMBLE] = CardName.SAI_TOKO_ENSEMBLE

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SHOOT] = CardName.HIMIKA_SHOOT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_RAPIDFIRE] = CardName.HIMIKA_RAPIDFIRE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_MAGNUMCANON] = CardName.HIMIKA_MAGNUMCANON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_FULLBURST] = CardName.HIMIKA_FULLBURST
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_BACKSTEP] = CardName.HIMIKA_BACKSTEP
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_BACKDRAFT] = CardName.HIMIKA_BACKDRAFT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SMOKE] = CardName.HIMIKA_SMOKE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_REDBULLET] = CardName.HIMIKA_REDBULLET
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_CRIMSONZERO] = CardName.HIMIKA_CRIMSONZERO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SCARLETIMAGINE] = CardName.HIMIKA_SCARLETIMAGINE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_BURMILIONFIELD] = CardName.HIMIKA_BURMILIONFIELD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_FIRE_WAVE] = CardName.HIMIKA_FIRE_WAVE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_SAT_SUI] = CardName.HIMIKA_SAT_SUI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HIMIKA_EN_TEN_HIMIKA] = CardName.HIMIKA_EN_TEN_HIMIKA

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_BITSUNERIGI] = CardName.TOKOYO_BITSUNERIGI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_WOOAHHANTAGUCK] = CardName.TOKOYO_WOOAHHANTAGUCK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_RUNNING_RABBIT] = CardName.TOKOYO_RUNNING_RABBIT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_POETDANCE] = CardName.TOKOYO_POETDANCE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_FLIPFAN] = CardName.TOKOYO_FLIPFAN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_WINDSTAGE] = CardName.TOKOYO_WINDSTAGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_SUNSTAGE] = CardName.TOKOYO_SUNSTAGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_KUON] = CardName.TOKOYO_KUON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_THOUSANDBIRD] = CardName.TOKOYO_THOUSANDBIRD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_ENDLESSWIND] = CardName.TOKOYO_ENDLESSWIND
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_TOKOYOMOON] = CardName.TOKOYO_TOKOYOMOON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_FLOWING_PLAY] = CardName.TOKOYO_FLOWING_PLAY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_SOUND_OF_SUN] = CardName.TOKOYO_SOUND_OF_SUN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG] = CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_PASSING_FEAR] = CardName.TOKOYO_PASSING_FEAR
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_RELIC_EYE] = CardName.TOKOYO_RELIC_EYE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_TOKOYO_EIGHT_SAKURA_IN_VAIN] = CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_WIRE] = CardName.OBORO_WIRE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_SHADOWCALTROP] = CardName.OBORO_SHADOWCALTROP
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_ZANGEKIRANBU] = CardName.OBORO_ZANGEKIRANBU
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_NINJAWALK] = CardName.OBORO_NINJAWALK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_INDUCE] = CardName.OBORO_INDUCE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CLONE] = CardName.OBORO_CLONE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_BIOACTIVITY] = CardName.OBORO_BIOACTIVITY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_KUMASUKE] = CardName.OBORO_KUMASUKE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_TOBIKAGE] = CardName.OBORO_TOBIKAGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_ULOO] = CardName.OBORO_ULOO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MIKAZRA] = CardName.OBORO_MIKAZRA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_SHURIKEN] = CardName.OBORO_SHURIKEN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_AMBUSH] = CardName.OBORO_AMBUSH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_BRANCH_OF_DIVINE] = CardName.OBORO_BRANCH_OF_DIVINE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_LAST_CRYSTAL] = CardName.OBORO_LAST_CRYSTAL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_HOLOGRAM_KUNAI] = CardName.OBORO_HOLOGRAM_KUNAI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_GIGASUKE] = CardName.OBORO_GIGASUKE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI] = CardName.OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MAIN_PARTS_X] = CardName.OBORO_MAIN_PARTS_X
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MAIN_PARTS_Y] = CardName.OBORO_MAIN_PARTS_Y
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MAIN_PARTS_Z] = CardName.OBORO_MAIN_PARTS_Z
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_A] = CardName.OBORO_CUSTOM_PARTS_A
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_B] = CardName.OBORO_CUSTOM_PARTS_B
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_C] = CardName.OBORO_CUSTOM_PARTS_C
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_D] = CardName.OBORO_CUSTOM_PARTS_D

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_YUKIHI] = CardName.YUKIHI_YUKIHI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE] = CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_PUSH_OUT_SLASH_PULL] = CardName.YUKIHI_PUSH_OUT_SLASH_PULL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_SWING_SLASH_STAB] = CardName.YUKIHI_SWING_SLASH_STAB
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_TURN_UMBRELLA] = CardName.YUKIHI_TURN_UMBRELLA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN] = CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_MAKE_CONNECTION] = CardName.YUKIHI_MAKE_CONNECTION
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_FLUTTERING_SNOWFLAKE] = CardName.YUKIHI_FLUTTERING_SNOWFLAKE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_SWAYING_LAMPLIGHT] = CardName.YUKIHI_SWAYING_LAMPLIGHT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_CLINGY_MIND] = CardName.YUKIHI_CLINGY_MIND
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_SWIRLING_GESTURE] = CardName.YUKIHI_SWIRLING_GESTURE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_HELP_SLASH_THREAT] = CardName.YUKIHI_HELP_SLASH_THREAT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_THREAD_SLASH_RAW_THREAD] = CardName.YUKIHI_THREAD_SLASH_RAW_THREAD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YUKIHI_FLUTTERING_COLLAR] = CardName.YUKIHI_FLUTTERING_COLLAR

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SHINRA] = CardName.SHINRA_SHINRA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_IBLON] = CardName.SHINRA_IBLON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_BANLON] = CardName.SHINRA_BANLON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_KIBEN] = CardName.SHINRA_KIBEN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_INYONG] = CardName.SHINRA_INYONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SEONDONG] = CardName.SHINRA_SEONDONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_JANGDAM] = CardName.SHINRA_JANGDAM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_NONPA] = CardName.SHINRA_NONPA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_WANJEON_NONPA] = CardName.SHINRA_WANJEON_NONPA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_DASIG_IHAE] = CardName.SHINRA_DASIG_IHAE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_CHEONJI_BANBAG] = CardName.SHINRA_CHEONJI_BANBAG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SAMRA_BAN_SHO] = CardName.SHINRA_SAMRA_BAN_SHO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_ZHEN_YEN] = CardName.SHINRA_ZHEN_YEN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_SA_DO] = CardName.SHINRA_SA_DO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHINRA_ZEN_CHI_KYO_TEN] = CardName.SHINRA_ZEN_CHI_KYO_TEN

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_CENTRIFUGAL_ATTACK] = CardName.HAGANE_CENTRIFUGAL_ATTACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_FOUR_WINDED_EARTHQUAKE] = CardName.HAGANE_FOUR_WINDED_EARTHQUAKE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GROUND_BREAKING] = CardName.HAGANE_GROUND_BREAKING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_HYPER_RECOIL] = CardName.HAGANE_HYPER_RECOIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_WON_MU_RUYN] = CardName.HAGANE_WON_MU_RUYN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_RING_A_BELL] = CardName.HAGANE_RING_A_BELL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAVITATION_FIELD] = CardName.HAGANE_GRAVITATION_FIELD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_SKY_HOLE_CRASH] = CardName.HAGANE_GRAND_SKY_HOLE_CRASH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_BELL_MEGALOBEL] = CardName.HAGANE_GRAND_BELL_MEGALOBEL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_GRAVITATION_ATTRACT] = CardName.HAGANE_GRAND_GRAVITATION_ATTRACT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_MOUNTAIN_RESPECT] = CardName.HAGANE_GRAND_MOUNTAIN_RESPECT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_BONFIRE] = CardName.HAGANE_BONFIRE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_WHEEL_SKILL] = CardName.HAGANE_WHEEL_SKILL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_GRAND_SOFT_MATERIAL] = CardName.HAGANE_GRAND_SOFT_MATERIAL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HAGANE_SOFT_ATTACK] = CardName.HAGANE_SOFT_ATTACK

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_THROW_KUNAI] = CardName.CHIKAGE_THROW_KUNAI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_POISON_NEEDLE] = CardName.CHIKAGE_POISON_NEEDLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_TO_ZU_CHU] = CardName.CHIKAGE_TO_ZU_CHU
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_CUTTING_NECK] = CardName.CHIKAGE_CUTTING_NECK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_POISON_SMOKE] = CardName.CHIKAGE_POISON_SMOKE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_TIP_TOEING] = CardName.CHIKAGE_TIP_TOEING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_MUDDLE] = CardName.CHIKAGE_MUDDLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_DEADLY_POISON] = CardName.CHIKAGE_DEADLY_POISON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_HAN_KI_POISON] = CardName.CHIKAGE_HAN_KI_POISON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_REINCARNATION_POISON] = CardName.CHIKAGE_REINCARNATION_POISON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE] = CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_TRICK_UMBRELLA] = CardName.CHIKAGE_TRICK_UMBRELLA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_STRUGGLE] = CardName.CHIKAGE_STRUGGLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON] = CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_PARALYTIC] = CardName.POISON_PARALYTIC
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_HALLUCINOGENIC] = CardName.POISON_HALLUCINOGENIC
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_RELAXATION] = CardName.POISON_RELAXATION
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_1] = CardName.POISON_DEADLY_1
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_2] = CardName.POISON_DEADLY_2
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_CHIKAGE_HIDDEN_WEAPON] = CardName.CHIKAGE_HIDDEN_WEAPON

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_ELEKITTEL] = CardName.KURURU_ELEKITTEL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_ACCELERATOR] = CardName.KURURU_ACCELERATOR
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_KURURUOONG] = CardName.KURURU_KURURUOONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_TORNADO] = CardName.KURURU_TORNADO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_REGAINER] = CardName.KURURU_REGAINER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_MODULE] = CardName.KURURU_MODULE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_REFLECTOR] = CardName.KURURU_REFLECTOR
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DRAIN_DEVIL] = CardName.KURURU_DRAIN_DEVIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_BIG_GOLEM] = CardName.KURURU_BIG_GOLEM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_INDUSTRIA] = CardName.KURURU_INDUSTRIA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DUPLICATED_GEAR_1] = CardName.KURURU_DUPLICATED_GEAR_1
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DUPLICATED_GEAR_2] = CardName.KURURU_DUPLICATED_GEAR_2
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DUPLICATED_GEAR_3] = CardName.KURURU_DUPLICATED_GEAR_3
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_KANSHOUSOUCHI_KURURUSIK] = CardName.KURURU_KANSHOUSOUCHI_KURURUSIK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_ANALYZE] = CardName.KURURU_ANALYZE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_DAUZING] = CardName.KURURU_DAUZING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_LAST_RESEARCH] = CardName.KURURU_LAST_RESEARCH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_GRAND_GULLIVER] = CardName.KURURU_GRAND_GULLIVER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_BLASTER] = CardName.KURURU_BLASTER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_RAILGUN] = CardName.KURURU_RAILGUN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KURURU_CONNECT_DIVE] = CardName.KURURU_CONNECT_DIVE

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_BURNING_STEAM] = CardName.THALLYA_BURNING_STEAM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_WAVING_EDGE] = CardName.THALLYA_WAVING_EDGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_SHIELD_CHARGE] = CardName.THALLYA_SHIELD_CHARGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_STEAM_CANNON] = CardName.THALLYA_STEAM_CANNON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_STUNT] = CardName.THALLYA_STUNT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_ROARING] = CardName.THALLYA_ROARING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_TURBO_SWITCH] = CardName.THALLYA_TURBO_SWITCH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_ALPHA_EDGE] = CardName.THALLYA_ALPHA_EDGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_OMEGA_BURST] = CardName.THALLYA_OMEGA_BURST
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_THALLYA_MASTERPIECE] = CardName.THALLYA_THALLYA_MASTERPIECE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_JULIA_BLACKBOX] = CardName.THALLYA_JULIA_BLACKBOX
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_YAKSHA] = CardName.FORM_YAKSHA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_NAGA] = CardName.FORM_NAGA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_GARUDA] = CardName.FORM_GARUDA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_QUICK_CHANGE] = CardName.THALLYA_QUICK_CHANGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_BLACKBOX_NEO] = CardName.THALLYA_BLACKBOX_NEO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_THALLYA_OMNIS_BLASTER] = CardName.THALLYA_OMNIS_BLASTER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_KINNARI] = CardName.FORM_KINNARI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_ASURA] = CardName.FORM_ASURA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_FORM_DEVA] = CardName.FORM_DEVA

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_BEAST_NAIL] = CardName.RAIRA_BEAST_NAIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_STORM_SURGE_ATTACK] = CardName.RAIRA_STORM_SURGE_ATTACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_REINCARNATION_NAIL] = CardName.RAIRA_REINCARNATION_NAIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_RUN] = CardName.RAIRA_WIND_RUN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WISDOM_OF_STORM_SURGE] = CardName.RAIRA_WISDOM_OF_STORM_SURGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_HOWLING] = CardName.RAIRA_HOWLING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_KICK] = CardName.RAIRA_WIND_KICK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_THUNDER_WIND_PUNCH] = CardName.RAIRA_THUNDER_WIND_PUNCH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_SUMMON_THUNDER] = CardName.RAIRA_SUMMON_THUNDER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_CONSEQUENCE_BALL] = CardName.RAIRA_WIND_CONSEQUENCE_BALL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_CIRCULAR_CIRCUIT] = CardName.RAIRA_CIRCULAR_CIRCUIT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_ATTACK] = CardName.RAIRA_WIND_ATTACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_ZEN_KAI] = CardName.RAIRA_WIND_ZEN_KAI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_WIND_CELESTIAL_SPHERE] = CardName.RAIRA_WIND_CELESTIAL_SPHERE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_STORM] = CardName.RAIRA_STORM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_FURIOUS_STORM] = CardName.RAIRA_FURIOUS_STORM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RAIRA_JIN_PUNG_JE_CHEON_UI] = CardName.RAIRA_JIN_PUNG_JE_CHEON_UI

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_WON_WOL] = CardName.UTSURO_WON_WOL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_BLACK_WAVE] = CardName.UTSURO_BLACK_WAVE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_HARVEST] = CardName.UTSURO_HARVEST
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_PRESSURE] = CardName.UTSURO_PRESSURE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_SHADOW_WING] = CardName.UTSURO_SHADOW_WING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_SHADOW_WALL] = CardName.UTSURO_SHADOW_WALL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_YUE_HOE_JU] = CardName.UTSURO_YUE_HOE_JU
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_HOE_MYEOL] = CardName.UTSURO_HOE_MYEOL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_HEO_WI] = CardName.UTSURO_HEO_WI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_JONG_MAL] = CardName.UTSURO_JONG_MAL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_MA_SIG] = CardName.UTSURO_MA_SIG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_BITE_DUST] = CardName.UTSURO_BITE_DUST
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_REVERBERATE_DEVICE_KURURUSIK] = CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_MANG_A] = CardName.UTSURO_MANG_A
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_ANNIHILATION_SHADOW] = CardName.UTSURO_ANNIHILATION_SHADOW
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_SILENT_WALK] = CardName.UTSURO_SILENT_WALK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_UTSURO_DE_MISE] = CardName.UTSURO_DE_MISE

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SPIRIT_SIK] = CardName.HONOKA_SPIRIT_SIK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_GUARDIAN_SPIRIT_SIK] = CardName.HONOKA_GUARDIAN_SPIRIT_SIK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_ASSAULT_SPIRIT_SIK] = CardName.HONOKA_ASSAULT_SPIRIT_SIK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_DIVINE_OUKA] = CardName.HONOKA_DIVINE_OUKA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_BLIZZARD] = CardName.HONOKA_SAKURA_BLIZZARD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_UI_GI_GONG_JIN] = CardName.HONOKA_UI_GI_GONG_JIN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_WING] = CardName.HONOKA_SAKURA_WING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_REGENERATION] = CardName.HONOKA_REGENERATION
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_AMULET] = CardName.HONOKA_SAKURA_AMULET
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_HONOKA_SPARKLE] = CardName.HONOKA_HONOKA_SPARKLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_COMMAND] = CardName.HONOKA_COMMAND
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_TAIL_WIND] = CardName.HONOKA_TAIL_WIND
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_CHEST_WILLINGNESS] = CardName.HONOKA_CHEST_WILLINGNESS
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_HAND_FLOWER] = CardName.HONOKA_HAND_FLOWER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_A_NEW_OPENING] = CardName.HONOKA_A_NEW_OPENING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_UNDER_THE_NAME_OF_FLAG] = CardName.HONOKA_UNDER_THE_NAME_OF_FLAG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FOUR_SEASON_BACK] = CardName.HONOKA_FOUR_SEASON_BACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FULL_BLOOM_PATH] = CardName.HONOKA_FULL_BLOOM_PATH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_SWORD] = CardName.HONOKA_SAKURA_SWORD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SHADOW_HAND] = CardName.HONOKA_SHADOW_HAND
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_EYE_OPEN_ALONE] = CardName.HONOKA_EYE_OPEN_ALONE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FOLLOW_TRACE] = CardName.HONOKA_FOLLOW_TRACE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_FACING_SHADOW] = CardName.HONOKA_FACING_SHADOW
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_SAKURA_SHINING_BRIGHTLY] = CardName.HONOKA_SAKURA_SHINING_BRIGHTLY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_HOLD_HANDS] = CardName.HONOKA_HOLD_HANDS
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HONOKA_WALK_OLD_LOAD] = CardName.HONOKA_WALK_OLD_LOAD

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_SNOW_BLADE] = CardName.KORUNU_SNOW_BLADE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_REVOLVING_BLADE] = CardName.KORUNU_REVOLVING_BLADE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_BLADE_DANCE] = CardName.KORUNU_BLADE_DANCE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_RIDE_SNOW] = CardName.KORUNU_RIDE_SNOW
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_ABSOLUTE_ZERO] = CardName.KORUNU_ABSOLUTE_ZERO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_FROSTBITE] = CardName.KORUNU_FROSTBITE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_FROST_THORN_BUSH] = CardName.KORUNU_FROST_THORN_BUSH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_CONLU_RUYANPEH] = CardName.KORUNU_CONLU_RUYANPEH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_LETAR_LERA] = CardName.KORUNU_LETAR_LERA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_UPASTUM] = CardName.KORUNU_UPASTUM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KORUNU_PORUCHARTO] = CardName.KORUNU_PORUCHARTO

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_STAR_NAIL] = CardName.YATSUHA_STAR_NAIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_DARKNESS_GILL] = CardName.YATSUHA_DARKNESS_GILL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_MIRROR_DEVIL] = CardName.YATSUHA_MIRROR_DEVIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_GHOST_STEP] = CardName.YATSUHA_GHOST_STEP
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_WILLING] = CardName.YATSUHA_WILLING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_CONTRACT] = CardName.YATSUHA_CONTRACT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_CLINGY_FLOWER] = CardName.YATSUHA_CLINGY_FLOWER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_TWO_LEAP_MIRROR_DIVINE] = CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_FOUR_LEAP_SONG] = CardName.YATSUHA_FOUR_LEAP_SONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_SIX_STAR_SEA] = CardName.YATSUHA_SIX_STAR_SEA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_EIGHT_MIRROR_OTHER_SIDE] = CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_HOLY_RAKE_HANDS] = CardName.YATSUHA_HOLY_RAKE_HANDS
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_ENTRANCE_OF_ABYSS] = CardName.YATSUHA_ENTRANCE_OF_ABYSS
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_TRUE_MONSTER] = CardName.YATSUHA_TRUE_MONSTER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_GHOST_LINK] = CardName.YATSUHA_GHOST_LINK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_RESOLUTION] = CardName.YATSUHA_RESOLUTION
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_PLEDGE] = CardName.YATSUHA_PLEDGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_VAIN_FLOWER] = CardName.YATSUHA_VAIN_FLOWER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_EIGHT_MIRROR_VAIN_SAKURA] = CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_UNFAMILIAR_WORLD] = CardName.YATSUHA_UNFAMILIAR_WORLD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_COLORED_WORLD] = CardName.YATSUHA_COLORED_WORLD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_SHES_CHERRY_BLOSSOM_WORLD] = CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_SHES_EGO_AND_DETERMINATION] = CardName.YATSUHA_SHES_EGO_AND_DETERMINATION

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_WATER_BALL] = CardName.HATSUMI_WATER_BALL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_WATER_CURRENT] = CardName.HATSUMI_WATER_CURRENT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_STRONG_ACID] = CardName.HATSUMI_STRONG_ACID
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_TSUNAMI] = CardName.HATSUMI_TSUNAMI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_JUN_BI_MAN_TAN] = CardName.HATSUMI_JUN_BI_MAN_TAN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_COMPASS] = CardName.HATSUMI_COMPASS
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_CALL_WAVE] = CardName.HATSUMI_CALL_WAVE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_ISANA_HAIL] = CardName.HATSUMI_ISANA_HAIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_OYOGIBI_FIRE] = CardName.HATSUMI_OYOGIBI_FIRE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_KIRAHARI_LIGHTHOUSE] = CardName.HATSUMI_KIRAHARI_LIGHTHOUSE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_MIOBIKI_ROUTE] = CardName.HATSUMI_MIOBIKI_ROUTE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_TORPEDO] = CardName.HATSUMI_TORPEDO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_SAGIRI_HAIL] = CardName.HATSUMI_SAGIRI_HAIL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_HATSUMI_WADANAKA_ROUTE] = CardName.HATSUMI_WADANAKA_ROUTE

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_JIN_DU] = CardName.MIZUKI_JIN_DU
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_BAN_GONG] = CardName.MIZUKI_BAN_GONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_SHOOTING_DOWN] = CardName.MIZUKI_SHOOTING_DOWN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_HO_LYEONG] = CardName.MIZUKI_HO_LYEONG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_BANG_BYEOG] = CardName.MIZUKI_BANG_BYEOG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_OVERPOWERING_GO_FORWARD] = CardName.MIZUKI_OVERPOWERING_GO_FORWARD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_JEON_JANG] = CardName.MIZUKI_JEON_JANG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_HACHIRYU_CHEONJUGAK] = CardName.MIZUKI_HACHIRYU_CHEONJUGAK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_HIJAMARU_TRIPLET] = CardName.MIZUKI_HIJAMARU_TRIPLET
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_TARTENASHI_DAESUMUN] = CardName.MIZUKI_TARTENASHI_DAESUMUN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MIZUKI_MIZUKI_BATTLE_CRY] = CardName.MIZUKI_MIZUKI_BATTLE_CRY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KODAMA_TU_SIN] = CardName.KODAMA_TU_SIN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_1] = CardName.SOLDIER_SPEAR_1
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_2] = CardName.SOLDIER_SPEAR_2
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SHIELD] = CardName.SOLDIER_SHIELD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_HORSE] = CardName.SOLDIER_HORSE

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_GONG_SUM] = CardName.MEGUMI_GONG_SUM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_TA_CHEOG] = CardName.MEGUMI_TA_CHEOG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_SHELL_ATTACK] = CardName.MEGUMI_SHELL_ATTACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_POLE_THRUST] = CardName.MEGUMI_POLE_THRUST
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_REED] = CardName.MEGUMI_REED
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_BALSAM] = CardName.MEGUMI_BALSAM
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_WILD_ROSE] = CardName.MEGUMI_WILD_ROSE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_ROOT_OF_CAUSALITY] = CardName.MEGUMI_ROOT_OF_CAUSALITY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_BRANCH_OF_POSSIBILITY] = CardName.MEGUMI_BRANCH_OF_POSSIBILITY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_FRUIT_OF_END] = CardName.MEGUMI_FRUIT_OF_END
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MEGUMI_MEGUMI_PALM] = CardName.MEGUMI_MEGUMI_PALM

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_IMAGE] = CardName.KANAWE_IMAGE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_SCREENPLAY] = CardName.KANAWE_SCREENPLAY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_PRODUCTION] = CardName.KANAWE_PRODUCTION
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_PUBLISH] = CardName.KANAWE_PUBLISH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_AFTERGLOW] = CardName.KANAWE_AFTERGLOW
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_IMPROMPTU] = CardName.KANAWE_IMPROMPTU
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_SEAL] = CardName.KANAWE_SEAL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_VAGUE_STORY] = CardName.KANAWE_VAGUE_STORY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_INFINITE_STARLIGHT] = CardName.KANAWE_INFINITE_STARLIGHT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_BEND_OVER_THIS_NIGHT] = CardName.KANAWE_BEND_OVER_THIS_NIGHT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_DISTANT_SKY] = CardName.KANAWE_DISTANT_SKY
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KANAWE_KANAWE] = CardName.KANAWE_KANAWE

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_SAL_JIN] = CardName.IDEA_SAL_JIN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_SAKURA_WAVE] = CardName.IDEA_SAKURA_WAVE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_WHISTLE] = CardName.IDEA_WHISTLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_MYEONG_JEON] = CardName.IDEA_MYEONG_JEON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_EMPHASIZING] = CardName.IDEA_EMPHASIZING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_IDEA_POSITIONING] = CardName.IDEA_POSITIONING

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_RED_BLADE] = CardName.KAMUWI_RED_BLADE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_FLUTTERING_BLADE] = CardName.KAMUWI_FLUTTERING_BLADE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_SI_KEN_LAN_JIN] = CardName.KAMUWI_SI_KEN_LAN_JIN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_CUT_DOWN] = CardName.KAMUWI_CUT_DOWN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_THREADING_THORN] = CardName.KAMUWI_THREADING_THORN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_KE_SYO_LAN_LYU] = CardName.KAMUWI_KE_SYO_LAN_LYU
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_BLOOD_WAVE] = CardName.KAMUWI_BLOOD_WAVE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_LAMP] = CardName.KAMUWI_LAMP
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_DAWN] = CardName.KAMUWI_DAWN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_GRAVEYARD] = CardName.KAMUWI_GRAVEYARD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_KATA_SHIRO] = CardName.KAMUWI_KATA_SHIRO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KAMUWI_LOGIC] = CardName.KAMUWI_LOGIC

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FALSE_STAB] = CardName.RENRI_FALSE_STAB
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_TEMPORARY_EXPEDIENT] = CardName.RENRI_TEMPORARY_EXPEDIENT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_BLACK_AND_WHITE] = CardName.RENRI_BLACK_AND_WHITE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_IRRITATING_GESTURE] = CardName.RENRI_IRRITATING_GESTURE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FLOATING_CLOUDS] = CardName.RENRI_FLOATING_CLOUDS
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FISHING] = CardName.RENRI_FISHING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_PULLING_FISHING] = CardName.RENRI_PULLING_FISHING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_RU_RU_RA_RA_RI] = CardName.RENRI_RU_RU_RA_RA_RI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA] = CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_O_RI_RE_TE_RA_RE_RU] = CardName.RENRI_O_RI_RE_TE_RA_RE_RU
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_RENRI_THE_END] = CardName.RENRI_RENRI_THE_END
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_ENGRAVED_GARMENT] = CardName.RENRI_ENGRAVED_GARMENT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_KIRIKO_SHAMANISTIC_MUSIC] = CardName.KIRIKO_SHAMANISTIC_MUSIC
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_DECEPTION_FOG] = CardName.RENRI_DECEPTION_FOG
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_SIN_SOO] = CardName.RENRI_SIN_SOO
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FALSE_WEAPON] = CardName.RENRI_FALSE_WEAPON
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_ESSENCE_OF_BLADE] = CardName.RENRI_ESSENCE_OF_BLADE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FIRST_SAKURA_ORDER] = CardName.RENRI_FIRST_SAKURA_ORDER
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_RI_RA_RU_RI_RA_RO] = CardName.RENRI_RI_RA_RU_RI_RA_RO

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_ABACUS_STONE] = CardName.AKINA_ABACUS_STONE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_THREAT] = CardName.AKINA_THREAT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_TRADE] = CardName.AKINA_TRADE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_SPECULATION] = CardName.AKINA_SPECULATION
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_CALC] = CardName.AKINA_CALC
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_TURN_OFF_TABLE] = CardName.AKINA_TURN_OFF_TABLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_DIRECT_FINANCING] = CardName.AKINA_DIRECT_FINANCING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_OPEN_CUTTING_METHOD] = CardName.AKINA_OPEN_CUTTING_METHOD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_SU_LYO_SUL] = CardName.AKINA_SU_LYO_SUL
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_AKINA_ACCURATE_CALC] = CardName.AKINA_AKINA_ACCURATE_CALC
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_AKINA_AKINA_ACCURATE_CALC_START_PHASE] = CardName.AKINA_AKINA_ACCURATE_CALC

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_SAW_BLADE_CUT_DOWN] = CardName.SHISUI_SAW_BLADE_CUT_DOWN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_PENETRATE_SAW_BLADE] = CardName.SHISUI_PENETRATE_SAW_BLADE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_REBELLION_ATTACK] = CardName.SHISUI_REBELLION_ATTACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_IRON_RESISTANCE] = CardName.SHISUI_IRON_RESISTANCE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_THORNY_PATH] = CardName.SHISUI_THORNY_PATH
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_IRON_POWDER_WIND_AROUND] = CardName.SHISUI_IRON_POWDER_WIND_AROUND
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_BLACK_ARMOR] = CardName.SHISUI_BLACK_ARMOR
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_PADMA_CUT_DOWN] = CardName.SHISUI_PADMA_CUT_DOWN
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_UPALA_TEAR] = CardName.SHISUI_UPALA_TEAR
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_ABUDA_EAT] = CardName.SHISUI_ABUDA_EAT
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_SHISUI_SHISUI_PLACE_OF_DEATH] = CardName.SHISUI_SHISUI_PLACE_OF_DEATH

        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_BOW_SPILLING] = CardName.MISORA_BOW_SPILLING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_AIMING_KICK] = CardName.MISORA_AIMING_KICK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_WIND_HOLE] = CardName.MISORA_WIND_HOLE
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_GAP_SI_EUL_SI] = CardName.MISORA_GAP_SI_EUL_SI
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_PRECISION] = CardName.MISORA_PRECISION
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_TRACKING_ATTACK] = CardName.MISORA_TRACKING_ATTACK
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_SKY_WING] = CardName.MISORA_SKY_WING
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_ENDLESS_END] = CardName.MISORA_ENDLESS_END
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_CLOUD_EMBROIDERED_CLOUD] = CardName.MISORA_CLOUD_EMBROIDERED_CLOUD
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_SHADOW_SHADY_SHADOW] = CardName.MISORA_SHADOW_SHADY_SHADOW
        cardTempNumberHashmap[SECOND_PLAYER_START_NUMBER + NUMBER_MISORA_SKY_BEYOND_SKY] = CardName.MISORA_SKY_BEYOND_SKY

        return cardTempNumberHashmap.toMap()
    }

    fun dataHashmapInit(){
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
        cardDataHashmap[CardName.SAI_TOKO_ENSEMBLE] = ensemble

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
        cardDataHashmap[CardName.TOKOYO_RUNNING_RABBIT] = runningrabbit
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
        cardDataHashmap[CardName.OBORO_HOLOGRAM_KUNAI] = holoKunai
        cardDataHashmap[CardName.OBORO_GIGASUKE] = gigasuke
        cardDataHashmap[CardName.OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI] = electricsouchi
        cardDataHashmap[CardName.OBORO_MAIN_PARTS_X] = mainPartsX
        cardDataHashmap[CardName.OBORO_MAIN_PARTS_Y] = mainPartsY
        cardDataHashmap[CardName.OBORO_MAIN_PARTS_Z] = mainPartsZ
        cardDataHashmap[CardName.OBORO_CUSTOM_PARTS_A] = customPartsA
        cardDataHashmap[CardName.OBORO_CUSTOM_PARTS_B] = customPartsB
        cardDataHashmap[CardName.OBORO_CUSTOM_PARTS_C] = customPartsC
        cardDataHashmap[CardName.OBORO_CUSTOM_PARTS_D] = customPartsD

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

        cardDataHashmap[CardName.YURINA_QUESTION_ANSWER] = questionAnswer
        cardDataHashmap[CardName.YURINA_AHUM] = ahum
        cardDataHashmap[CardName.YURINA_KANZA_DO] = kanzaDo

        cardDataHashmap[CardName.YATSUHA_UNFAMILIAR_WORLD] = unfamiliarWorld
        cardDataHashmap[CardName.YATSUHA_COLORED_WORLD] = coloredWorld
        cardDataHashmap[CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD] = shesCherryBlossomWorld
        cardDataHashmap[CardName.YATSUHA_SHES_EGO_AND_DETERMINATION] = shesEgoAndDetermination

        cardDataHashmap[CardName.AKINA_AKINA] = akina
        cardDataHashmap[CardName.AKINA_ABACUS_STONE] = abacusStone
        cardDataHashmap[CardName.AKINA_THREAT] = threat
        cardDataHashmap[CardName.AKINA_TRADE] = trade
        cardDataHashmap[CardName.AKINA_SPECULATION] = speculation
        cardDataHashmap[CardName.AKINA_CALC] = calc
        cardDataHashmap[CardName.AKINA_TURN_OFF_TABLE] = turnOffTable
        cardDataHashmap[CardName.AKINA_DIRECT_FINANCING] = directFinancing
        cardDataHashmap[CardName.AKINA_OPEN_CUTTING_METHOD] = openCuttingMethod
        cardDataHashmap[CardName.AKINA_GRAND_CALC_AND_MANUAL] = grandCalcAndManual
        cardDataHashmap[CardName.AKINA_SU_LYO_SUL] = sulyosul
        cardDataHashmap[CardName.AKINA_AKINA_ACCURATE_CALC] = accurateCalc

        cardDataHashmap[CardName.SHISUI_SAW_BLADE_CUT_DOWN] = sawBladeCutDown
        cardDataHashmap[CardName.SHISUI_PENETRATE_SAW_BLADE] = penetrateSawBlade
        cardDataHashmap[CardName.SHISUI_REBELLION_ATTACK] = rebellionAttack
        cardDataHashmap[CardName.SHISUI_IRON_RESISTANCE] = ironResistance
        cardDataHashmap[CardName.SHISUI_THORNY_PATH] = thornyPath
        cardDataHashmap[CardName.SHISUI_IRON_POWDER_WIND_AROUND] = ironPowderWindAround
        cardDataHashmap[CardName.SHISUI_BLACK_ARMOR] = blackArmor
        cardDataHashmap[CardName.SHISUI_PADMA_CUT_DOWN] = padmaCutDown
        cardDataHashmap[CardName.SHISUI_UPALA_TEAR] = upalaTear
        cardDataHashmap[CardName.SHISUI_ABUDA_EAT] = abudaEat
        cardDataHashmap[CardName.SHISUI_SHISUI_PLACE_OF_DEATH] = shisuiPlaceOfDeath

        cardDataHashmap[CardName.CHIKAGE_HIDDEN_WEAPON] = hiddenWeapon

        cardDataHashmap[CardName.RENRI_DECEPTION_FOG] = deceptionFog
        cardDataHashmap[CardName.RENRI_SIN_SOO] = sinSoo
        cardDataHashmap[CardName.RENRI_FALSE_WEAPON] = falseWeapon
        cardDataHashmap[CardName.RENRI_ESSENCE_OF_BLADE] = essenceOfBlade
        cardDataHashmap[CardName.RENRI_FIRST_SAKURA_ORDER] = firstSakuraOrder
        cardDataHashmap[CardName.RENRI_RI_RA_RU_RI_RA_RO] = riRaRuRiRaRo

        cardDataHashmap[CardName.RENRI_DECEPTION_FOG] = deceptionFog
        cardDataHashmap[CardName.RENRI_SIN_SOO] = sinSoo
        cardDataHashmap[CardName.RENRI_FALSE_WEAPON] = falseWeapon
        cardDataHashmap[CardName.RENRI_ESSENCE_OF_BLADE] = essenceOfBlade
        cardDataHashmap[CardName.RENRI_FIRST_SAKURA_ORDER] = firstSakuraOrder
        cardDataHashmap[CardName.RENRI_RI_RA_RU_RI_RA_RO] = riRaRuRiRaRo

        cardDataHashmap[CardName.MISORA_MISORA] = misora
        cardDataHashmap[CardName.MISORA_BOW_SPILLING] = bowSpilling
        cardDataHashmap[CardName.MISORA_AIMING_KICK] = aimingKick
        cardDataHashmap[CardName.MISORA_WIND_HOLE] = windHole
        cardDataHashmap[CardName.MISORA_GAP_SI_EUL_SI] = gapSiEulSi
        cardDataHashmap[CardName.MISORA_PRECISION] = precision
        cardDataHashmap[CardName.MISORA_TRACKING_ATTACK] = trackingAttack
        cardDataHashmap[CardName.MISORA_SKY_WING] = skyWing
        cardDataHashmap[CardName.MISORA_ENDLESS_END] = endlessEnd
        cardDataHashmap[CardName.MISORA_CLOUD_EMBROIDERED_CLOUD] = cloudEmbroideredCloud
        cardDataHashmap[CardName.MISORA_SHADOW_SHADY_SHADOW] = shadowShadyShadow
        cardDataHashmap[CardName.MISORA_SKY_BEYOND_SKY] = skyBeyondSky


        cardDataHashmapV8_1[CardName.HAGANE_RING_A_BELL] = ringABellV8_1
        cardDataHashmapV8_1[CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA] = eightMirrorVainSakuraV8_1
        cardDataHashmapV8_1[CardName.HATSUMI_CALL_WAVE] = callWaveV8_1
        cardDataHashmapV8_1[CardName.MEGUMI_BRANCH_OF_POSSIBILITY] = branchPossibilityV8_1
        cardDataHashmapV8_1[CardName.YUKIHI_FLUTTERING_SNOWFLAKE] = flutteringSnowflakeV8_1

        cardDataHashmapV8_2[CardName.SAINE_BETRAYAL] = betrayerV8_2
        cardDataHashmapV8_2[CardName.OBORO_ULOO] = ulooV8_2
        cardDataHashmapV8_2[CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS] = hiddenFireV8_2
        cardDataHashmapV8_2[CardName.SHINRA_SA_DO] = sadoV8_2
        cardDataHashmapV8_2[CardName.CHIKAGE_HIDDEN_WEAPON] = hiddenWeapon
        cardDataHashmapV8_2[CardName.KURURU_LAST_RESEARCH] = lastResearchV8_2
        cardDataHashmapV8_2[CardName.THALLYA_STEAM_CANNON] = steamCanonV8_2
        cardDataHashmapV8_2[CardName.RAIRA_STORM_SURGE_ATTACK] = stormSurgeAttackV8_2
        cardDataHashmapV8_2[CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK] = deviceKururusikV8_2
        cardDataHashmapV8_2[CardName.KANAWE_VAGUE_STORY] = vagueStoryV8_2
        cardDataHashmapV8_2[CardName.KAMUWI_FLUTTERING_BLADE] = flutteringBladeV8_2
        cardDataHashmapV8_2[CardName.KAMUWI_LOGIC] = logicV8_2
        cardDataHashmapV8_2[CardName.RENRI_FISHING] = fishingV8_2
        cardDataHashmapV8_2[CardName.AKINA_DIRECT_FINANCING] = directFinancingV8_2
        cardDataHashmapV8_2[CardName.AKINA_AKINA_ACCURATE_CALC] = accurateCalcV8_2
        cardDataHashmapV8_2[CardName.SHISUI_BLACK_ARMOR] = blackArmorV8_2
        cardDataHashmapV8_2[CardName.SHISUI_PADMA_CUT_DOWN] = padmaCutDownV8_2

        cardDataHashmapV9[CardName.SAINE_ACCOMPANIMENT] = accompanimentV9
        cardDataHashmapV9[CardName.SAINE_DUET_TAN_JU_BING_MYEONG] = duetTanJuBingMyeongV9
        cardDataHashmapV9[CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG] = duetChitanYangMyeongV9
        cardDataHashmapV9[CardName.TOKOYO_FLOWING_PLAY] = flowingPlayV9
        cardDataHashmapV9[CardName.YUKIHI_FLUTTERING_SNOWFLAKE] =  flutteringSnowflakeV9
        cardDataHashmapV9[CardName.SHINRA_SA_DO] = sadoV9
        cardDataHashmapV9[CardName.AKINA_DIRECT_FINANCING] = directFinancingV9
        cardDataHashmapV9[CardName.AKINA_AKINA_ACCURATE_CALC] = accurateCalcV9
        cardDataHashmapV9[CardName.SHISUI_IRON_POWDER_WIND_AROUND] = ironPowderWindAroundV9
        cardDataHashmapV9[CardName.SHISUI_PADMA_CUT_DOWN] = padmaCutDownV9
        cardDataHashmapV9[CardName.SHISUI_UPALA_TEAR] = upalaTear
        cardDataHashmapV9[CardName.SHISUI_PADMA_CUT_DOWN] = padmaCutDownV8_2
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

    val terminationText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TERMINATION, null)
    val chasmText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHASM, null)
    val onlyCanUseReactText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) {_, _, _, react_attack->
        if((react_attack != null && react_attack.isItReact)) 1
        else 0
    }

    /**
     * only use enchantment have inDeployment distance change text
     */
    val whenDistanceChangeText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_DEPLOYMENT) { _, _, game_status, _ ->
        if(game_status.getAdjustDistance() != game_status.beforeDistance){
            game_status.whenDistanceChange()
        }
        null
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
    private val pobaram = CardData(CardClass.SPECIAL, CardName.YURINA_POBARAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.REACTION)
    private val jjockbae = CardData(CardClass.SPECIAL, CardName.YURINA_JJOCKBAE, MegamiEnum.YURINA, CardType.BEHAVIOR, SubType.NONE)
    private val juruck = CardData(CardClass.SPECIAL, CardName.YURINA_JURUCK, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULL_POWER)

    private fun gulSa(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getPlayerLife(player) <= 3
    }
    private fun yurinaCardInit(){
        cham.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        ilsom.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        ilsom.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                gulSa(buff_player, buff_game_status)
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })


        jaru_chigi.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jaru_chigi.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            if (gulSa(player, game_status)) {
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, _ -> true}, {_, _, attack ->
                    attack.auraPlusMinus(1)
                }))
            }
            null
        })


        guhab.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 4, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        guhab.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, buff_game_status, _ ->
                buff_game_status.getAdjustDistance() <= 2
            }, {_, _, madeAttack ->
                madeAttack.run {
                    auraPlusMinus(-1); lifePlusMinus(-1)
                }
            }))
            null
        })


        giback.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        giback.addText(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        apdo.addText(chasmText)
        apdo.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.YURINA_APDO,
                            NUMBER_YURINA_APDO_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(1, 2, 3, 4), 3,  999,  MegamiEnum.YURINA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        giyenbanzo.setEnchantment(4)
        giyenbanzo.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                { buff_player, buff_game_status, _ -> gulSa(buff_player, buff_game_status)}, { _, _, madeAttack ->
                if(madeAttack.megami != MegamiEnum.YURINA) madeAttack.run {
                    Chogek(); auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })


        wolyungnack.setSpecial(7)
        wolyungnack.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 4, 4,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        pobaram.setSpecial(3)
        pobaram.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 2, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        pobaram.addText(terminationText)
        pobaram.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-2)
                }))
            null
        })


        jjockbae.setSpecial(2)
        jjockbae.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.dustToAura(player, 5, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        jjockbae.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                beforeAfterLife, _, _ ->
                val beforeLife = beforeAfterLife / 100
                val afterLife = beforeAfterLife % 100
                if(beforeLife > 3 && afterLife <= 3){
                    gameStatus.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })


        juruck.setSpecial(5)
        juruck.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 5, 5,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        juruck.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, player, game_status, _ ->
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
        doublebegi.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(palSang(player, game_status)){
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.SAINE_DOUBLEBEGI,
                                NUMBER_SAINE_DOUBLEBEGI_ADDITIONAL_ATTACK, CardClass.NULL,
                                sortedSetOf(4, 5), 2,  1,  MegamiEnum.SAINE,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                    )){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })


        hurubegi.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        moogechoo.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        moogechoo.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        ganpa.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_USE_REACT) { _, player, game_status, _ ->
            if(palSang(player, game_status)) 1
            else 0
        })
        ganpa.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_SAINE_GANPA)
                if(selectDustToDistance(nowCommand, game_status, player, game_status.getCardOwner(card_number), card_number)) break
            }
            null
        })


        gwonyuck.setEnchantment(2)
        gwonyuck.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE) { _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        gwonyuck.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) { _, _, _, _ ->
            1
        })


        choongemjung.setEnchantment(1)
        choongemjung.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-1)
                }))
            null
        })
        choongemjung.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.SAINE_CHOONGEMJUNG, NUMBER_SAINE_CHOONGEMJUNG_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 1,  999,   MegamiEnum.SAINE,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        choongemjung.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        mooembuck.setEnchantment(5)
        mooembuck.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE) { _, _, _, _ ->
            null
        })


        yuldonghogek.setSpecial(6)
        yuldonghogek.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.SAINE_YULDONGHOGEK,
                            NUMBER_SAINE_YULDONGHOGEK_ADDITIONAL_1, CardClass.NULL,
                            sortedSetOf(3, 4), 1,  1, MegamiEnum.SAINE,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        yuldonghogek.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.SAINE_YULDONGHOGEK,
                            NUMBER_SAINE_YULDONGHOGEK_ADDITIONAL_2, CardClass.NULL,
                            sortedSetOf(4, 5), 1,  1, MegamiEnum.SAINE,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        yuldonghogek.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.SAINE_YULDONGHOGEK,
                            NUMBER_SAINE_YULDONGHOGEK_ADDITIONAL_3, CardClass.NULL,
                            sortedSetOf(3, 4, 5), 2,  2,  MegamiEnum.SAINE,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )) {
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        hangmunggongjin.setSpecial(8)
        hangmunggongjin.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) { card_number, player, game_status, _->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card ->
                (card.card_data.card_name == CardName.SAINE_HANGMUNGGONGJIN)}, { cost, _, _ ->
                cost - game_status.getPlayerAura(player.opposite())
            }))
            null
        })
        hangmunggongjin.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.auraToDistance(player.opposite(), 2,
                Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        emmooshoebing.setSpecial(2)
        emmooshoebing.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        emmooshoebing.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.auraPlusMinus(-1); attack.lifePlusMinus(-1)
                }))
            null
        })
        emmooshoebing.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(game_status.getPlayerAura(player) <= 1) 1
            else 0
        })


        jonggek.setSpecial(5)
        jonggek.setAttack(DistanceType.CONTINUOUS, Pair(1, 5), null, 5, 5,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jonggek.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) { _, _, _, reactAttack->
            if(reactAttack != null && reactAttack.card_class == CardClass.SPECIAL) 1
            else 0
        })
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

    private fun yeonwha(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.gameLogger.playerUseCardNumber(player) >= 3
    }

    private fun himikaCardInit(){
        shoot.setAttack(DistanceType.CONTINUOUS, Pair(4, 10), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        rapidfire.setAttack(DistanceType.CONTINUOUS, Pair(6, 8), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        rapidfire.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {conditionPlayer, conditionGameStatus, _ -> yeonwha(conditionPlayer, conditionGameStatus) },
                {_, _, attack ->
                    attack.auraPlusMinus(1); attack.lifePlusMinus(1)
                }))
            null
        }))


        magnumcanon.setAttack(DistanceType.CONTINUOUS, Pair(5, 8), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        magnumcanon.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.lifeToDust(player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number),
                card_number)
            null
        })


        fullburst.setAttack(DistanceType.CONTINUOUS, Pair(5, 9), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        fullburst.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, {_, _, _ ->
                true }, {_, _, attack ->
                attack.setBothSideDamage()
            }))
            null
        }))


        backstep.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            game_status.drawCard(player, 1)
            null
        })
        backstep.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        backdraft.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })
        backdraft.addText(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        smoke.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_MOVE_TOKEN_USING_ARROW){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number * 100 + 99
        })


        redbullet.setSpecial(0)
        redbullet.setAttack(DistanceType.CONTINUOUS, Pair(5, 10), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        crimsonzero.setSpecial(5)
        crimsonzero.setAttack(DistanceType.CONTINUOUS, Pair(0, 2), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        crimsonzero.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, {_, _, _ ->
                true }, {_, _, attack ->
                attack.setBothSideDamage()
            }))
            null
        }))
        crimsonzero.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            if(game_status.getAdjustDistance() == 0){
                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, {_, _, _ ->
                    true }, {_, _, attack ->
                    attack.canNotReact()
                }))
            }
            null
        }))


        scarletimagine.setSpecial(3)
        scarletimagine.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            game_status.drawCard(player, 2)
            null
        })
        scarletimagine.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            game_status.coverCard(player, player, NUMBER_HIMIKA_SCARLETIMAGINE)
            null
        })


        burmilionfield.setSpecial(2)
        burmilionfield.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _->
            if(yeonwha(player, game_status)){
                game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        burmilionfield.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(game_status.getPlayerHandSize(player) == 0) 1
            else 0
        })
    }

    private fun kyochi(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.getConcentrationValue(player) == 2
    }

    private val bitsunerigi = CardData(CardClass.NORMAL, CardName.TOKOYO_BITSUNERIGI, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val wooahhantaguck = CardData(CardClass.NORMAL, CardName.TOKOYO_WOOAHHANTAGUCK, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.REACTION)
    private val runningrabbit = CardData(CardClass.NORMAL, CardName.TOKOYO_RUNNING_RABBIT, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)
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
        bitsunerigi.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.TOKOYO_BITSUNERIGI) && kyochi(player, game_status)){
                game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_TOP, card_number)
            }
            null
        })


        wooahhantaguck.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wooahhantaguck.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, react_attack ->
            if(kyochi(player, game_status) && react_attack?.card_class != CardClass.SPECIAL){
                react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })


        runningrabbit.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
            if(game_status.getAdjustDistance() <= 3){
                game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        poetdance.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        poetdance.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
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


        flipfan.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
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
        flipfan.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
            game_status.dustToAura(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        windstage.setEnchantment(2)
        windstage.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.distanceToAura(player, 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        windstage.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.auraToDistance(player, 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        sunstage.setEnchantment(2)
        sunstage.addText(terminationText)
        sunstage.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setConcentration(player, 2)
            null
        })
        sunstage.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.TOKOYO_SUNSTAGE,
                        NUMBER_TOKOYO_SUNSTAGE_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(3, 4, 5, 6), 999,  1,  MegamiEnum.TOKOYO,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        kuon.setSpecial(5)
        kuon.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kuon.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
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
        thousandbird.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            game_status.deckReconstruct(player, false)
            null
        })


        endlesswind.setSpecial(1)
        endlesswind.setAttack(DistanceType.CONTINUOUS, Pair(3, 8), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        endlesswind.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) ret@{ _, player, game_status, _ ->
            val selected = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_TOKOYO_ENDLESSWIND, 1)
            { card, _ -> card.card_data.card_type != CardType.ATTACK && card.card_data.canDiscard}?: run {
                game_status.showSome(player.opposite(), CommandEnum.SHOW_HAND_YOUR)
                return@ret null
            }

            game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
            }
            null
        })
        endlesswind.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(kyochi(player, game_status)) 1
            else 0
        })


        tokoyomoon.setSpecial(2)
        tokoyomoon.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _->
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

    private val installation = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INSTALLATION) {_, _, _, _->
        null
    }

    private fun oboroCardInit(){
        wire.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wire.addText(installation)


        shadowcaltrop.setAttack(DistanceType.CONTINUOUS, Pair(2, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        shadowcaltrop.addText(installation)
        shadowcaltrop.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if (game_status.gameLogger.checkThisCardUseInCover(player, card_number)){
                game_status.coverCard(player.opposite(), player, NUMBER_OBORO_SHADOWCALTROP)
            }
            null
        })


        zangekiranbu.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        zangekiranbu.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {condition_player, condition_game_status, _ ->
                condition_game_status.gameLogger.checkThisTurnGetAuraDamage(condition_player.opposite())
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                } }))
            null
        })


        ninjawalk.addText(installation)
        ninjawalk.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        ninjawalk.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { card_number, player, game_status, _ ->
            if (game_status.gameLogger.checkThisCardUseInCover(player, card_number)){
                game_status.useInstallationOnce(player)
            }
            null
        })


        induce.addText(installation)
        induce.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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


        clone.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { _, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_OBORO_CLONE)
                {card, _ -> card.card_data.sub_type != SubType.FULL_POWER && card.card_data.card_type != CardType.UNDEFINED}
                if(selected == null){
                    game_status.showSome(player, CommandEnum.SHOW_COVER_YOUR)
                    break
                }
                else{
                    if(selected.size == 1){
                        val selectNumber = selected[0]
                        val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                        game_status.useCardFromNotFullAction(player, card, LocationEnum.COVER_CARD, false, null,
                            isCost = true, isConsume = true)
                        if(game_status.getEndTurn(player)) {
                            break
                        }
                        val secondCard = game_status.getCardFrom(player, selectNumber, LocationEnum.DISCARD_YOUR)?: break
                        game_status.useCardFromNotFullAction(player, secondCard, LocationEnum.DISCARD_YOUR, false, null,
                            isCost = true, isConsume = true)
                        break
                    }
                }
            }
            null
        })


        bioactivity.setEnchantment(4)
        bioactivity.addText(installation)
        bioactivity.addText(chasmText)
        bioactivity.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RETURN_OTHER_CARD) { _, player, game_status, _ ->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.YOUR_USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_OBORO_BIOACTIVITY, 1) { _, _ -> true }?.let { selected ->
                game_status.returnSpecialCard(player, selected[0])
            }
            null
        })


        kumasuke.setSpecial(4)
        kumasuke.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kumasuke.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            for (i in 1..game_status.getPlayer(player).coverCard.size){
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.OBORO_KUMASUKE,
                                NUMBER_OBORO_KUMASUKE_ADDITIONAL + i - 1, CardClass.NULL,
                                sortedSetOf(3, 4),2,  2,  MegamiEnum.OBORO,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                    )){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })


        tobikage.setSpecial(4)
        tobikage.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{ _, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_OBORO_TOBIKAGE)
                {card, _ -> card.card_data.sub_type != SubType.FULL_POWER && card.card_data.card_type != CardType.UNDEFINED} ?: run {
                    return@ret null
                }
                if(selected.size == 1){
                    val selectNumber = selected[0]
                    val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: continue
                    game_status.useCardFromNotFullAction(player, card, LocationEnum.COVER_CARD, false, react_attack,
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
        uloo.addText(Text(TextEffectTimingTag.USED, TextEffectTag.INSTALLATION_INFINITE, null))


        mikazra.setSpecial(0)
        mikazra.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        mikazra.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        mikazra.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
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
        yukihi.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.MEGAMI_YOUR, changeUmbrellaText)
            null
        })


        hiddenNeedle.umbrellaMark = true
        hiddenNeedle.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 3, 1)
        hiddenNeedle.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 2)


        hiddenFire.umbrellaMark = true
        hiddenFire.setAttackFold(DistanceType.CONTINUOUS, Pair(5, 6), null, 1, 1)
        hiddenFire.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 1)
        hiddenFire.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) {card_number, player, game_status, _ ->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS)){
                game_status.movePlayingCard(player, LocationEnum.HAND, card_number)
            }
            game_status.changeUmbrella(player)
            null
        })


        pushOut.umbrellaMark = true
        pushOut.setAttackFold(DistanceType.CONTINUOUS, Pair(2, 5), null, 1, 1)
        pushOut.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 1)
        pushOut.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_PUSH_OUT_SLASH_PULL)
                if(selectDustToDistance(nowCommand, game_status, player, game_status.getCardOwner(card_number), card_number)) break
            }
            null
        })
        pushOut.addTextUnfold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
            game_status.distanceToDust(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        swing.umbrellaMark = true
        swing.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 5, 999)
        swing.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 999, 2)


        turnUmbrella.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_UMBRELLA_CHANGE) { card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_TURN_UMBRELLA)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.showSome(player, CommandEnum.SHOW_HAND_SOME_YOUR, card_number)
                    game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
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
        backwardStep.addTextFold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        backwardStep.addTextUnfold(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        makeConnection.setEnchantment(2)
        makeConnection.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        makeConnection.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        flutteringSnowflake.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 1), null, 0, 0)
        flutteringSnowflake.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateUmbrellaListener(player, Listener(player, card_number){gameStatus, cardNumber, _, _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
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
        clingyMind.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number, 1, RangeBufTag.CHANGE_IMMEDIATE, { _, _, _ -> true}, {_, _, madeAttack ->
                if(madeAttack.megami.equal(MegamiEnum.YUKIHI)){
                    val parameter = when {
                        madeAttack.kururuChangeRangeUpper -> {
                            if (madeAttack.kururuChange2X) {
                                2
                            } else {
                                1
                            }
                        }
                        madeAttack.kururuChangeRangeUnder -> {
                            if (madeAttack.kururuChange2X) {
                                -2
                            } else {
                                -1
                            }
                        }
                        else -> {
                            0
                        }
                    }

                    when(madeAttack.card_name){
                        CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE -> {
                            madeAttack.run {
                                addRange(hiddenNeedle.distanceContUnfold.adjustRange(parameter))
                                addRange(hiddenNeedle.distanceContFold.adjustRange(parameter))
                            }
                        }
                        CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS -> {
                            val hiddenFireHand = CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS.toCardData(game_status.version)
                            madeAttack.run {
                                addRange(hiddenFireHand.distanceContUnfold.adjustRange(parameter))
                                addRange(hiddenFireHand.distanceContFold.adjustRange(parameter))
                            }
                        }
                        CardName.YUKIHI_PUSH_OUT_SLASH_PULL -> {
                            madeAttack.run {
                                addRange(pushOut.distanceContUnfold.adjustRange(parameter))
                                addRange(pushOut.distanceContFold.adjustRange(parameter))
                            }
                        }
                        CardName.YUKIHI_SWING_SLASH_STAB -> {
                            madeAttack.run {
                                addRange(swing.distanceContUnfold.adjustRange(parameter))
                                addRange(swing.distanceContFold.adjustRange(parameter))
                            }
                        }
                        CardName.YUKIHI_FLUTTERING_SNOWFLAKE -> {
                            val snowFlake = CardName.YUKIHI_FLUTTERING_SNOWFLAKE.toCardData(game_status.version)
                            madeAttack.run {
                                addRange(snowFlake.distanceContUnfold.adjustRange(parameter))
                                addRange(snowFlake.distanceContFold.adjustRange(parameter))
                            }
                        }
                        CardName.YUKIHI_SWAYING_LAMPLIGHT -> {
                            madeAttack.run {
                                addRange(swayingLamplight.distanceContUnfold.adjustRange(parameter))
                                addRange(swayingLamplight.distanceContFold.adjustRange(parameter))
                            }
                        }
                        CardName.YUKIHI_HELP_SLASH_THREAT -> {
                            madeAttack.run {
                                addRange(helpOrThreat.distanceContUnfold.adjustRange(parameter))
                                addRange(helpOrThreat.distanceContFold.adjustRange(parameter))
                            }
                        }
                        CardName.YUKIHI_THREAD_SLASH_RAW_THREAD -> {
                            madeAttack.run {
                                addRange(threadOrRawThread.distanceContFold.adjustRange(parameter))
                                addRange(threadOrRawThread.distanceContUnfold.adjustRange(parameter))
                            }
                        }
                        else -> {}
                    }
                }
            }))
            null
        })


        swirlingGesture.setSpecial(1)
        swirlingGesture.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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

    suspend fun prepareStratagem(player: PlayerEnum, game_status: GameStatus){
        if(!game_status.getPlayer(player).justRunStratagem){
            setStratagemByUser(game_status, player)
        }
    }

    suspend fun sealCard(player: PlayerEnum, game_status: GameStatus, seal_card_number: Int, sealed_card: Card){
        val nowPlayer = game_status.getPlayer(player)
        nowPlayer.sealInformation[seal_card_number]?.add(sealed_card.card_number) ?: run {
            nowPlayer.sealInformation[seal_card_number] = mutableListOf(sealed_card.card_number)
        }
        game_status.insertCardTo(player, sealed_card, LocationEnum.SEAL_ZONE, true)
    }

    suspend fun unSealCard(player: PlayerEnum, game_status: GameStatus, seal_card_number: Int, returnPlace: LocationEnum){
        val nowPlayer = game_status.getPlayer(player)
        val otherPlayer = game_status.getPlayer(player.opposite())
        nowPlayer.sealInformation[seal_card_number]?.let { sealedList ->
            sealedList.toList().forEach { sealedCardNumber ->
                game_status.popCardFrom(player, sealedCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                    game_status.insertCardTo(it.player, it, returnPlace, true)
                }
            }
        }
        nowPlayer.sealInformation.remove(seal_card_number)

        otherPlayer.sealInformation[seal_card_number]?.let { sealedList ->
            sealedList.toList().forEach { sealedCardNumber ->
                game_status.popCardFrom(player.opposite(), sealedCardNumber, LocationEnum.SEAL_ZONE, true)?.let {
                    game_status.insertCardTo(it.player, it, returnPlace, true)
                }
            }
        }
        otherPlayer.sealInformation.remove(seal_card_number)
    }

    private suspend fun iblonEffect(player: PlayerEnum, game_status: GameStatus){
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
        shinra.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).stratagem == null){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.MEGAMI_YOUR, setStratagemText)
            }
            null
        })


        iblon.setAttack(DistanceType.CONTINUOUS, Pair(2, 7), null, 2, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        iblon.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.EFFECT_INSTEAD_DAMAGE) ret@{ _, player, game_status, attack ->
            if(game_status.getPlayer(player.opposite()).normalCardDeck.size >= 2){
                if(attack?.editedLaceration == true){
                    while(true){
                        when(game_status.receiveCardEffectSelect(game_status.turnPlayer, NUMBER_IBLON_LACERATION)){
                            CommandEnum.SELECT_ONE -> {
                                iblonEffect(player, game_status)
                                return@ret 1
                            }
                            CommandEnum.SELECT_TWO -> {
                                return@ret 0
                            }
                            else -> {}
                        }
                    }
                    @Suppress("UNREACHABLE_CODE")
                    1
                }
                else{
                    iblonEffect(player, game_status)
                    1
                }
            }
            else{
                0
            }
        })


        banlon.setAttack(DistanceType.CONTINUOUS, Pair(2, 7), null, 1, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        banlon.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, _, react_attack ->
            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { player, game_status, attack ->
                attack.card_class != CardClass.SPECIAL &&
                        attack.getDamage(game_status, player,  game_status.getPlayerAttackBuff(player)).first >= 3
            }, { _, _, attack ->
                attack.makeNoDamage()
            }))
            null
        })
        banlon.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD){ _, player, game_status, _ ->
            game_status.drawCard(player.opposite(), 1)
            null
        })


        kiben.setAttack(DistanceType.CONTINUOUS, Pair(3, 8), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kiben.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RUN_STRATAGEM){ _, player, game_status, _ ->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.deckToCoverCard(player.opposite(), 3)
                    prepareStratagem(player, game_status)
                }
                Stratagem.GUE_MO -> {
                    val beforeJustRunNoCondition = game_status.getPlayer(player).justRunStratagem
                    while (true){
                        val list = game_status.selectCardFrom(player.opposite(), player, player,
                            listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_SHINRA_KIBEN)
                        { card, _ -> card.card_data.card_type != CardType.UNDEFINED }?: break
                        if (list.isNotEmpty()){
                            game_status.getPlayer(player).justRunStratagem = false
                            if (list.size == 1){
                                game_status.popCardFrom(player.opposite(), list[0], LocationEnum.DISCARD_YOUR, true)?.let {
                                    game_status.useCardFromNotFullAction(player, it, LocationEnum.DISCARD_OTHER, false, null,
                                        isCost = true, isConsume = true)
                                }?: continue
                                break
                            }
                            game_status.getPlayer(player).justRunStratagem = beforeJustRunNoCondition
                        }
                        else{
                            break
                        }
                    }
                    prepareStratagem(player, game_status)
                }
                null -> {}
            }
            null
        })


        inyong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { _, player, game_status, _->
            while(true){
                val selected = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_INYONG)
                {_, _ -> true} ?: break
                if(selected.size == 0) break
                if(selected.size == 1){
                    val selectNumber = selected[0]
                    val card = game_status.getCardFrom(player.opposite(), selectNumber, LocationEnum.HAND)?: continue
                    if(card.card_data.card_type != CardType.ATTACK && card.card_data.card_type != CardType.UNDEFINED) continue
                    while(true){
                        val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_SHINRA_INYONG)
                        if(nowCommand == CommandEnum.SELECT_ONE){
                            if(card.card_data.card_type != CardType.UNDEFINED){
                                game_status.useCardFromNotFullAction(player, card, LocationEnum.HAND_OTHER, false, null,
                                    isCost = true, isConsume = true)
                            }
                            break
                        }
                        else if(nowCommand == CommandEnum.SELECT_TWO){
                            game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, true)?.let {
                                game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, true)
                            }
                            break
                        }
                    }
                    if(card.card_data.sub_type == SubType.FULL_POWER) game_status.endCurrentPhase = true
                    break
                }
            }
            null
        })


        seondong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) { card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    prepareStratagem(player, game_status)
                }
                Stratagem.GUE_MO -> {
                    game_status.distanceToAura(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    prepareStratagem(player, game_status)
                }
                null -> {}
            }
            null
        })


        jangdam.setEnchantment(2)
        jangdam.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.RUN_STRATAGEM) { card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.addConcentration(player)
                    game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let{
                        game_status.insertCardTo(it.player, it, LocationEnum.YOUR_DECK_TOP, true)
                    }
                    prepareStratagem(player, game_status)
                }
                Stratagem.GUE_MO -> {
                    if (game_status.getPlayer(player.opposite()).hand.size <= 1){
                        game_status.setShrink(player.opposite())
                        game_status.drawCard(player.opposite(), 3)
                        game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                            listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                            NUMBER_SHINRA_JANGDAM, 2)
                        {_, _ -> true}?.let { selected ->
                            when (selected.size) {
                                1 -> {
                                    game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                                        game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                                    }
                                }
                                2 -> {
                                    game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                                        game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR,
                                            publicForOther = true, discardCheck = false)
                                    }
                                    game_status.popCardFrom(player.opposite(), selected[1], LocationEnum.HAND, true)?.let {
                                        game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR,
                                            true)
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                    prepareStratagem(player, game_status)
                }
                null -> {}
            }
            null
        })


        nonpa.setEnchantment(4)
        nonpa.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.DISCARD_YOUR),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_NONPA, 1)
            {card, _ -> !(card.isSoftAttack)}?.let {selected ->
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                    sealCard(player, game_status, card_number, it)
                }
            }
            null
        })
        nonpa.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            unSealCard(player, game_status, card_number, LocationEnum.DISCARD_OTHER)
            null
        })


        wanjeonNonpa.setSpecial(2)
        wanjeonNonpa.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){ card_number, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.DISCARD_YOUR),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_WANJEON_NONPA, 1)
            {card, _ -> !(card.isSoftAttack)}?.let {selected ->
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                    sealCard(player.opposite(), game_status, card_number, it)
                }
            }
            null
        })


        dasicIhae.setSpecial(2)
        dasicIhae.addText(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) { card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    game_status.selectCardFrom(player, player, player, listOf(LocationEnum.DISCARD_YOUR, LocationEnum.YOUR_USED_CARD),
                        CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SHINRA_DASIG_IHAE, 1)
                        {card, _ -> card.card_data.card_type == CardType.ENCHANTMENT}?.let { selected ->
                        game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?.also {
                            game_status.useCardFromNotFullAction(player, it, LocationEnum.DISCARD_YOUR, false, null,
                                isCost = true, isConsume = false
                            )
                        }?: game_status.getCardFrom(player, selected[0], LocationEnum.YOUR_USED_CARD)?.also {
                            game_status.useCardFromNotFullAction(player, it, LocationEnum.YOUR_USED_CARD, false, null,
                                isCost = true, isConsume = false
                            )
                        }?.let {card ->
                            if(card.card_data.sub_type == SubType.FULL_POWER){
                                game_status.endCurrentPhase = true
                            }
                        }
                    }
                    prepareStratagem(player, game_status)
                }
                Stratagem.GUE_MO -> {
                    game_status.selectCardFrom(player.opposite(), player, player,
                        listOf(LocationEnum.ENCHANTMENT_ZONE), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_SHINRA_CHEONJI_BANBAG, 1)
                    {card, _ -> card.card_data.card_class != CardClass.SPECIAL}?.let { selected ->
                        game_status.getCardFrom(player.opposite(), selected[0], LocationEnum.ENCHANTMENT_ZONE)?.let { card ->
                            game_status.cardToDust(player.opposite(), card.getNap(), card, false, card_number)
                            if(card.isItDestruction()){
                                game_status.enchantmentDestruction(player.opposite(), card)
                            }
                        }
                    }
                    prepareStratagem(player, game_status)
                }
                null -> {}
            }
            null
        })


        cheonjiBanBag.setSpecial(2)
        cheonjiBanBag.setEnchantment(5)
        cheonjiBanBag.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, { _, _, attack ->
                attack.run {
                    tempEditedLifeDamage = getEditedAuraDamage(); tempEditedAuraDamage = getEditedLifeDamage()
                }
            }))
            null
        })


        samraBanSho.setSpecial(6)
        samraBanSho.setEnchantment(6)
        samraBanSho.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.dustToLife(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        samraBanSho.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_ENCHANTMENT_DESTRUCTION_YOUR){ _, player, game_status, _ ->
            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                null, null, NUMBER_SHINRA_SAMRA_BAN_SHO)
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_SHINRA_SAMRA_BAN_SHO, -1))
            null
        })
        samraBanSho.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN){ _, player, game_status, _ ->
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

    private val centrifugalText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) { _, player, game_status, _->
        if(centrifugal(player, game_status)) 1
        else 0
    }

    private val centrifugalLogText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_LOG) { card_number, player, game_status, _->
        game_status.gameLogger.insert(EventLog(player, LogText.USE_CENTRIFUGAL, card_number, card_number))
        null
    }

    private suspend fun centrifugal(player: PlayerEnum, game_status: GameStatus): Boolean{
        return game_status.startTurnDistance + 2 <= game_status.getAdjustDistance() && !(game_status.gameLogger.checkThisTurnDoAttack(player))
    }

    private fun checkAllSpecialCardUsed(player: PlayerEnum, game_status: GameStatus, except: Int): Boolean{
        val nowPlayer = game_status.getPlayer(player)
        if(nowPlayer.specialCardDeck.isEmpty()){
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
        centrifugalAttack.addText(centrifugalText)
        centrifugalAttack.addText(centrifugalLogText)
        centrifugalAttack.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            if (player == game_status.turnPlayer) {
                game_status.getPlayer(player).hand.values.filter { card ->
                    card.card_data.canCover
                }.forEach { card ->
                    game_status.popCardFrom(player, card.card_number, LocationEnum.HAND, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                    }
                }

                game_status.getPlayer(player.opposite()).hand.values.filter { card ->
                    card.card_data.canCover
                }.forEach { card ->
                    game_status.popCardFrom(player.opposite(), card.card_number, LocationEnum.HAND, false)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                    }
                }

                game_status.setConcentration(player, 0)
            }
            null
        })
        centrifugalAttack.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.END_CURRENT_PHASE) { _, _, game_status, _ ->
            game_status.endCurrentPhase = true
            null
        })


        fourWindedEarthquake.setAttack(DistanceType.CONTINUOUS, Pair(0, 6), null, 1, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        fourWindedEarthquake.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            if (abs(game_status.getAdjustDistance() - game_status.startTurnDistance) >= 2)  {
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
        groundBreaking.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setConcentration(player.opposite(), 0)
            game_status.setShrink(player.opposite())
            null
        })


        hyperRecoil.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
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


        wonMuRuyn.addText(centrifugalText)
        wonMuRuyn.addText(centrifugalLogText)
        wonMuRuyn.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            if(game_status.getPlayerFlare(player.opposite()) >= 3){
                game_status.flareToAura(player.opposite(), player, 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        ringABell.addText(centrifugalText)
        ringABell.addText(centrifugalLogText)
        ringABell.addText(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_HAGANE_RING_A_BELL)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.showPlayersSelectResult(player.opposite(), NUMBER_HAGANE_RING_A_BELL, 0)
                        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, _ ->
                            true}, {_, _, attack ->
                            if(attack.getEditedAuraDamage() < 3){
                                attack.lifePlusMinus(1)
                            }
                            else{
                                attack.auraPlusMinus(2)
                            }
                        }))
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                            game_status.showPlayersSelectResult(player.opposite(), NUMBER_HAGANE_RING_A_BELL, 1)
                        game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET, { _, _, _ -> true},
                            { _, _, attack -> attack.canNotReact()
                            })
                        )
                        break
                    }
                    else -> {}
                }
            }
            null
        })


        gravitationField.setEnchantment(2)
        gravitationField.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        gravitationField.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) { _, _, _, _ ->
            -1
        })


        grandSkyHoleCrash.setSpecial(4)
        grandSkyHoleCrash.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1000, 1000,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = true)
        grandSkyHoleCrash.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        grandBellMegalobel.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            if(checkAllSpecialCardUsed(player, game_status, card_number)){
                game_status.dustToLife(player, 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        grandGravitationAttract.setSpecial(5)
        grandGravitationAttract.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.distanceToFlare(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        grandGravitationAttract.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ card_number, player, game_status, _ ->
            if(!game_status.gameLogger.checkThisCardUsed(player, card_number) && game_status.gameLogger.checkUseCentrifugal(player)) 1
            else 0
        })


        grandMountainRespect.setSpecial(4)
        grandMountainRespect.addText(centrifugalText)
        grandMountainRespect.addText(centrifugalLogText)
        grandMountainRespect.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { _, player, game_status, _ ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HAGANE_GRAND_MOUNTAIN_RESPECT)
                {card, _ -> card.card_data.sub_type != SubType.FULL_POWER && card.card_data.card_type != CardType.UNDEFINED}?: break
                if(selected.size == 0) break
                else if(selected.size <= 2){
                    game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?.let {
                        game_status.useCardFromNotFullAction(player, it, LocationEnum.DISCARD_YOUR, false, null,
                            isCost = true, isConsume = true)
                    }?: continue

                    if(game_status.getEndTurn(player) || game_status.endCurrentPhase) break

                    if(selected.size == 2){
                        game_status.getCardFrom(player, selected[1], LocationEnum.DISCARD_YOUR)?.let {
                            game_status.useCardFromNotFullAction(player, it, LocationEnum.DISCARD_YOUR, false, null,
                                isCost = true, isConsume = true)
                        }?: break
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

    private val poisonParalytic = CardData(CardClass.POISON, CardName.POISON_PARALYTIC, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)
    private val poisonHallucinogenic = CardData(CardClass.POISON, CardName.POISON_HALLUCINOGENIC, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)
    private val poisonRelaxation = CardData(CardClass.POISON, CardName.POISON_RELAXATION, MegamiEnum.NONE, CardType.ENCHANTMENT, SubType.NONE)
    private val poisonDeadly1 = CardData(CardClass.POISON, CardName.POISON_DEADLY_1, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)
    private val poisonDeadly2 = CardData(CardClass.POISON, CardName.POISON_DEADLY_2, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.NONE)

    private fun makeAllPoisonList(player: PlayerEnum, game_status: GameStatus): MutableList<Int>{
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

        game_status.getPlayer(player).poisonBag[CardName.POISON_DEADLY_1]?.let {
            cardList.add(it.card_number)
        }?: game_status.getPlayer(player).poisonBag[CardName.POISON_DEADLY_2]?.let {
            cardList.add(it.card_number)
        }

        return cardList
    }

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

    private fun chikageCardInit(){
        throwKunai.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        poisonNeedle.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        poisonNeedle.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_CHIKAGE_POISON_NEEDLE, 1)[0].let { poison ->
                    game_status.popCardFrom(player, poison, LocationEnum.POISON_BAG, false)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_TOP, publicForOther = true, publicForYour = false)
                    }
                }
            }
            null
        })


        toZuChu.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 1, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        toZuChu.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.auraToDistance(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.getPlayer(player.opposite()).canNotGoForward = true
            null
        })


        cuttingNeck.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 2, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        cuttingNeck.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
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


        poisonSmoke.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            val cardList = makePoisonList(player, game_status)
            if(cardList.size != 0){
                game_status.selectCardFrom(player, cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_CHIKAGE_POISON_SMOKE, 1)[0].let { poison ->
                    game_status.popCardFrom(player, poison, LocationEnum.POISON_BAG, true)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.HAND, true)
                    }
                }

            }
            null
        })


        tipToeing.setEnchantment(4)
        tipToeing.addText(chasmText)
        tipToeing.addText(whenDistanceChangeText)
        tipToeing.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_DISTANCE){ _, _, _, _->
            -2
        })


        muddle.setEnchantment(2)
        muddle.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_GO_BACKWARD_OTHER){ _, _, _, _ ->
            1
        })
        muddle.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_BREAK_AWAY_OTHER){ _, _, _, _ ->
            1
        })


        deadlyPoison.setSpecial(3)
        deadlyPoison.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            game_status.popCardFrom(player, game_status.getCardNumber(player, CardName.POISON_DEADLY_1), LocationEnum.POISON_BAG, true)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_TOP, true)
            }?:
            game_status.popCardFrom(player, game_status.getCardNumber(player, CardName.POISON_DEADLY_2), LocationEnum.POISON_BAG, true)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_TOP, true)
            }
            null
        })


        hankiPoison.setSpecial(2)
        hankiPoison.setEnchantment(5)
        hankiPoison.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
                { condition_player, condition_game_status, condition_attack ->
                val damage = condition_attack.getDamage(condition_game_status, condition_player,  condition_game_status.getPlayerAttackBuff(condition_player))
                damage.first == 999 || damage.second == 999
            }, { _, _, attack ->
                attack.makeNotValid()
            }))
            null
        })
        hankiPoison.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            game_status.addThisTurnOtherBuff(player.opposite(), OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
                { condition_player, condition_game_status, condition_attack ->
                    val damage = condition_attack.getDamage(condition_game_status, condition_player, game_status.getPlayerAttackBuff(player))
                    damage.first == 999 || damage.second == 999
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            null
        })


        reincarnationPoison.setSpecial(1)
        reincarnationPoison.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        reincarnationPoison.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(game_status.getPlayerHandSize(player.opposite()) >= 2) 1
            else 0
        })


        chikageWayOfLive.setSpecial(5)
        chikageWayOfLive.setEnchantment(4)
        chikageWayOfLive.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.ADD_LISTENER) { card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                         _, _, damage ->
                if(damage){
                    gameStatus.popCardFrom(player, cardNumber, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                        gameStatus.cardToDust(player, it.getNap(), it, false, cardNumber)
                        gameStatus.gameLogger.insert(EventLog(player, LogText.END_EFFECT, cardNumber, -1))
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
        chikageWayOfLive.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.GAME_END) { card_number, player, game_status, _ ->
            if(checkAllSpecialCardUsed(player, game_status, card_number)){
                game_status.gameEnd(player, null)
            }
            null
        })


        poisonParalytic.canCover = false
        poisonParalytic.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).didBasicOperation) 0
            else 1
        })
        poisonParalytic.addText(Text(TextEffectTimingTag.USING, TextEffectTag.END_CURRENT_PHASE) { _, _, game_status, _ ->
            game_status.endCurrentPhase = true
            null
        })
        poisonParalytic.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.POISON_PARALYTIC)){
                game_status.movePlayingCard(player, LocationEnum.POISON_BAG, card_number)
            }
            null
        })


        poisonHallucinogenic.canCover = false
        poisonHallucinogenic.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.flareToDust(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        poisonHallucinogenic.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.POISON_HALLUCINOGENIC)){
                game_status.movePlayingCard(player, LocationEnum.POISON_BAG, card_number)
            }
            null
        })


        poisonRelaxation.canCover = false
        poisonRelaxation.setEnchantment(3)
        poisonRelaxation.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_USE_ATTACK){ _, _, _, _ ->
            1
        })
        poisonRelaxation.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.POISON_RELAXATION)){
                game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                    game_status.insertCardTo(it.player.opposite(), it, LocationEnum.POISON_BAG, true)
                }
            }
            null
        })


        poisonDeadly1.canCover = false
        poisonDeadly1.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.auraToDust(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })

        poisonDeadly2.canCover = false
        poisonDeadly2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.auraToDust(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
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

    private val bigGolemText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { _, player, game_status, _ ->
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
                                    null, null, NUMBER_KURURU_BIG_GOLEM) != -1){
                                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KURURU_BIG_GOLEM, -1))
                                game_status.deckReconstruct(player, false)
                            }
                        }
                        else{
                            if(game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                                    null, null, NUMBER_KURURU_BIG_GOLEM) != -1){
                                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KURURU_BIG_GOLEM, -1))
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

    private fun getKikou(player: PlayerEnum, game_status: GameStatus): Kikou {
        val result = Kikou()
        val nowPlayer = game_status.getPlayer(player)
        for (card in nowPlayer.enchantmentCard.values + nowPlayer.usedSpecialCard.values + nowPlayer.discard) {
            calcKikou(card.card_data, result)
        }
        return result
    }

    private fun getKikou(player: PlayerEnum, game_status: GameStatus, condition: (Card) -> Boolean): Kikou {
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
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_KURURUOONG, 1
                ) { _, _ -> true }?: return
                game_status.popCardFrom(player, selected[0], LocationEnum.COVER_CARD, false)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                }
            }
            CommandEnum.SELECT_THREE -> {
                val selected = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_TORNADO, 1
                ) { _, _ -> true }?: return
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
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
                    card.card_data.megami != MegamiEnum.KURURU && card.card_data.card_type != CardType.UNDEFINED}?: break
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

                game_status.useCardFromNotFullAction(player, card, location, false, null,
                    isCost = false, isConsume = true)
                break
            }
        }
    }

    private fun duplicateCardDataForIndustria(card_data: CardData, card_name: CardName): CardData{
        val result = CardData(CardClass.NORMAL, card_name, card_data.megami, card_data.card_type, card_data.sub_type)
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

            distanceType = card_data.distanceType
            distanceCont = card_data.distanceCont
            distanceUncont = card_data.distanceUncont
            lifeDamage =  card_data.lifeDamage
            auraDamage = card_data.auraDamage

            charge = card_data.charge

            cost = card_data.cost

            effect = card_data.effect
            canCover = card_data.canCover
            canDiscard = card_data.canDiscard

            effect = card_data.effect

            growing = card_data.growing
        }
        return result
    }

    private fun kururuCardInit(){
        elekittel.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.behavior >= 3 && kikou.reaction >= 2) {
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, NUMBER_KURURU_ELEKITTEL)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KURURU_ELEKITTEL, -1))
            }
            null
        })


        accelerator.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { _, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.enchantment >= 1 && kikou.behavior >= 2) {
                while(true){
                    val list = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_KURURU_ACCELERATOR
                    ) { card, _ -> card.card_data.sub_type == SubType.FULL_POWER && card.card_data.card_type != CardType.UNDEFINED }?: break
                    if(list.size == 1){
                        game_status.getCardFrom(player, list[0], LocationEnum.HAND)?.let { card ->
                            game_status.useCardFromNotFullAction(player, card, LocationEnum.HAND, false, null,
                                isCost = true, isConsume = true)
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


        kururuoong.addText(onlyCanUseReactText)
        kururuoong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
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


        tornado.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 2) {
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(10, 999), false,
                        null, null, NUMBER_KURURU_TORNADO)
                }
                else{
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(5, 999), false,
                        null, null, NUMBER_KURURU_TORNADO)
                }
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KURURU_TORNADO, -1))
            }
            if(kikou.enchantment >= 2){
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 1, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 2), false,
                        null, null, NUMBER_KURURU_TORNADO)
                }
                else{
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                        null, null, NUMBER_KURURU_TORNADO)
                }
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KURURU_TORNADO, -1))
            }
            null
        })


        regainer.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_USE_COVER) { _, _, _, _ ->
            null
        })
        regainer.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.enchantment >= 1 && kikou.reaction >= 1) {
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 2, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    regainer(NUMBER_KURURU_BLASTER, card_number, player, game_status, 2)
                    regainer(NUMBER_KURURU_BLASTER, card_number, player, game_status, 2)
                }
                else{
                    regainer(NUMBER_KURURU_REGAINER, card_number, player, game_status, 1)
                }
            }
            null
        })


        module.setEnchantment(3)
        module.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_USE_BEHAVIOR_END){ _, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, NUMBER_KURURU_MODULE)
            null
        })


        reflector.setEnchantment(0)
        reflector.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        reflector.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            if(game_status.gameLogger.checkThisTurnAttackNumber(player.opposite()) == 1){
                game_status.addThisTurnOtherBuff(player.opposite(), OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
                    { _, _, _ -> true}, { _, _, attack ->
                        attack.makeNotValid()
                    })
                )
            }
            null
        })


        drainDevil.setSpecial(2)
        drainDevil.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        drainDevil.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_SPECIAL_RETURN_YOUR) { card_number, player, game_status, _ ->
            if(!game_status.getPlayer(player).endTurn){
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
        bigGolem.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.reaction >= 1 && kikou.fullPower >= 2){
               game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, bigGolemText)
            }
            null
        })
        bigGolem.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_USE_FULL_POWER_YOUR_END) { _, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, NUMBER_KURURU_BIG_GOLEM)
            null
        })


        industria.setSpecial(1)
        industria.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ card_number, player, game_status, _ ->
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
                        val copyCard = game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?:
                        game_status.popCardFrom(player, list[0], LocationEnum.HAND, true)?: continue
                        sealCard(player, game_status, card_number, copyCard)
                        break
                    }
                    else if(list.size == 0){
                        break
                    }
                }
            }
            null
        })
        industria.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _->
            val industriaCard = game_status.getCardFrom(player, CardName.KURURU_DUPLICATED_GEAR_1, LocationEnum.ADDITIONAL_CARD)?:
            game_status.getCardFrom(player, CardName.KURURU_DUPLICATED_GEAR_2, LocationEnum.ADDITIONAL_CARD)?:
            game_status.getCardFrom(player, CardName.KURURU_DUPLICATED_GEAR_3, LocationEnum.ADDITIONAL_CARD)
            if(industriaCard != null){
                game_status.moveAdditionalCard(player, industriaCard.card_data.card_name, LocationEnum.YOUR_DECK_BELOW)
            }

            val ownerPlayer = game_status.getPlayer(game_status.getCardOwner(card_number))
            val duplicateCardData: CardData? = ownerPlayer.sealZone[ownerPlayer.sealInformation[card_number]?.get(0)]?.card_data
            if(duplicateCardData != null){
                for(card in getPlayerAllNormalCardExceptEnchantment(player, game_status)){
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
        industria.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateReconstructListener(player, Listener(player, card_number) {gameStatus, cardNumber, _, _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
        industria.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_THIS_CARD_RETURN){ _, player, game_status, _ ->
            for(card in getPlayerAllNormalCardExceptEnchantment(player, game_status)){
                when(card.card_number.toCardName()){
                    CardName.KURURU_DUPLICATED_GEAR_1 -> card.card_data = dupliGear1
                    CardName.KURURU_DUPLICATED_GEAR_2 -> card.card_data = dupliGear2
                    CardName.KURURU_DUPLICATED_GEAR_3 -> card.card_data = dupliGear3
                    else -> {}
                }
            }
            null
        })


        dupliGear1.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, _, _ ->
            0
        })


        dupliGear2.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, _, _ ->
            0
        })


        dupliGear3.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, _, _ ->
            0
        })


        kanshousouchiKururusik.setSpecial(3)
        kanshousouchiKururusik.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
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
        kanshousouchiKururusik.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { card_number, player, game_status, _->
            while(true){
                val list = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.YOUR_USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_DUPLICATED_GEAR_3){ card, _ -> card.card_data.card_type != CardType.UNDEFINED
                    true
                }?: break
                if(list.size == 1){
                    game_status.getCardFrom(player.opposite(), list[0], LocationEnum.YOUR_USED_CARD)?.let { card ->
                        game_status.useCardFromNotFullAction(player, card, LocationEnum.OTHER_USED_CARD, false, null,
                            isCost = true, isConsume = false)
                    }
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

    val afterAttackManeuverBaseActionText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
        maneuver(player, game_status, true)
        null
    }

    val afterAttackManeuverText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
        maneuver(player, game_status, false)
        null
    }

    private val combustCheckText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_CHECK){ _, player, game_status, _ ->
        if(combustCondition(game_status, player)) 1
        else 0
    }

    private val combustText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST){_, player, game_status, _ ->
        game_status.combust(player, 1)
        null
    }

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

    private fun combustCondition(game_status: GameStatus, player: PlayerEnum): Boolean = (game_status.getPlayer(player).artificialToken?: 0) != 0

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
            game_status.gameLogger.insert(EventLog(player, LogText.TRANSFORM, -1, -1))
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
        burningSteam.addText(afterAttackManeuverText)


        wavingEdge.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wavingEdge.addText(combustCheckText)
        wavingEdge.addText(combustText)
        wavingEdge.addText(afterAttackManeuverText)


        shieldCharge.setAttack(DistanceType.CONTINUOUS, Pair(1, 1), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shieldCharge.addText(combustCheckText)
        shieldCharge.addText(combustText)
        shieldCharge.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        shieldCharge.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })


        steamCanon.setAttack(DistanceType.CONTINUOUS, Pair(2, 8), null, 3, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        steamCanon.addText(combustCheckText)
        steamCanon.addText(combustText)


        stunt.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })
        stunt.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.auraToFlare(player, player,2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        roaring.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION){ _, player, game_status, _ ->
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
        roaring.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION){ _, player, game_status, _ ->
            if(game_status.getPlayer(player).concentration >= 2 && game_status.canUseConcentration(player)){
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


        turboSwitch.addText(combustCheckText)
        turboSwitch.addText(combustText)
        turboSwitch.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            maneuver(player, game_status, false)
            null
        })


        alphaEdge.setSpecial(1)
        alphaEdge.setAttack(DistanceType.DISCONTINUOUS, null, mutableListOf(1, 3, 5, 7), 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        alphaEdge.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateManeuverListener(player, Listener(player, card_number) {_, cardNumber, _,
                                                                                        _, _, _ ->
                game_status.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })


        omegaBurst.setSpecial(4)
        omegaBurst.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, react_attack ->
            val x = game_status.getPlayer(player).artificialTokenBurn
            game_status.restoreArtificialToken(player, x)
            react_attack?.addOtherBuff( OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE,
                { nowPlayer, gameStatus, attack ->
                    val damage = attack.getDamage(gameStatus, nowPlayer, game_status.getPlayerAttackBuff(player))
                    (damage.first == 999) || (damage.first <= x)
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            null
        })


        masterPiece.setSpecial(1)
        masterPiece.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MANEUVER) { card_number, player, game_status, _ ->
            if(game_status.turnPlayer == player){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_THALLYA_THALLYA_MASTERPIECE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.dustToDistance(1, Arrow.BOTH_DIRECTION, player,
                                game_status.getCardOwner(card_number), card_number)
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.distanceToDust(1, Arrow.BOTH_DIRECTION, player,
                                game_status.getCardOwner(card_number), card_number)
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
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
        juliaBlackbox.addText(Text(TextEffectTimingTag.USING, TextEffectTag.TRANSFORM) { card_number, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken?: 0) == 0){
                transform(player, game_status)
                game_status.restoreArtificialToken(player, 2)
            }
            else{
                game_status.movePlayingCard(player, LocationEnum.SPECIAL_CARD, card_number)
            }
            null
        })


        formYaksha.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) { _, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            if(player == PlayerEnum.PLAYER1) game_status.player2NextStartPhaseDraw = 1
            else game_status.player1NextStartPhaseDraw = 1
            null
        })
        formYaksha.addText(Text(TextEffectTimingTag.USED, TextEffectTag.FORBID_BASIC_OPERATION_YOUR) { _, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken?: 0) == 0) 1
            else 0
        })


        formNaga.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) { card_number, player, game_status, _ ->
            val otherFlare = game_status.getPlayer(player.opposite()).flare
            if(otherFlare >= 3){
                game_status.flareToDust(player.opposite(), otherFlare - 2, Arrow.BOTH_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
            }
            null
        })


        formGaruda.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) { _, player, game_status, _ ->
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
        stormSurgeAttack.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    val auraDamage = gameStatus.getPlayer(nowPlayer).thunderGauge?.let {
                        if((game_status.getPlayer(nowPlayer).windGauge?:0) > it) it
                        else (game_status.getPlayer(nowPlayer).windGauge?: 0)
                    }?: 0
                    tempEditedAuraDamage = auraDamage
                }
            }))
            null
        })


        reincarnationNail.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        reincarnationNail.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
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


        windRun.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
            if(game_status.getAdjustDistance() >= 3){
                game_status.distanceToDust(2, Arrow.BOTH_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        wisdomOfStormSurge.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            game_status.getPlayer(player).thunderGauge?.let{
                if((game_status.getPlayer(player).windGauge?: 0) + it >= 4){
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
        wisdomOfStormSurge.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) { _, player, game_status, _->
            game_status.gaugeIncreaseRequest(player, NUMBER_RAIRA_WISDOM_OF_STORM_SURGE)
            null
        })


        howling.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) { _, player, game_status, _->
            game_status.setShrink(player.opposite())
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_HOWLING)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.thunderGaugeIncrease(player)
                        game_status.windGaugeIncrease(player)
                    }
                    CommandEnum.SELECT_TWO -> {
                        val hand = game_status.getPlayer(player).hand
                        hand.keys
                            .filter { cardNumber -> hand[cardNumber]?.card_data?.canCover == true }
                            .forEach { cardNumber ->
                                game_status.popCardFrom(player, cardNumber, LocationEnum.HAND, false)?.let {
                                    game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
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


        windKick.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
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
        thunderWindPunch.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        thunderWindPunch.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                (buff_game_status.getPlayer(buff_player).thunderGauge ?: 0) >= 4
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })
        thunderWindPunch.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if((game_status.getPlayer(player).windGauge ?: 0) >= 4) 1
            else 0
        })


        summonThunder.setSpecial(6)
        summonThunder.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _->
            for (i in 1..ceil(((game_status.getPlayer(player).thunderGauge?: 0) / 2.0)).toInt()){
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.RAIRA_SUMMON_THUNDER,
                                NUMBER_RAIRA_SUMMON_THUNDER_ADDITIONAL + i - 1, CardClass.NULL,
                                sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 1,  1, MegamiEnum.RAIRA,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                    )){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })


        windConsequenceBall.setSpecial(2)
        windConsequenceBall.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _->
            val windGauge = game_status.getPlayer(player).windGauge?: 0
            if(windGauge >= 3){
                game_status.moveAdditionalCard(player, CardName.RAIRA_WIND_ATTACK, LocationEnum.SPECIAL_CARD)
            }
            if(windGauge >= 7){
                game_status.moveAdditionalCard(player, CardName.RAIRA_WIND_ZEN_KAI, LocationEnum.SPECIAL_CARD)
            }
            if(windGauge >= 12){
                game_status.moveAdditionalCard(player, CardName.RAIRA_WIND_CELESTIAL_SPHERE, LocationEnum.SPECIAL_CARD)
            }
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })


        circularCircuit.setSpecial(2)
        circularCircuit.setEnchantment(3)
        circularCircuit.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE) { card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_CIRCULAR_CIRCUIT)){
                    CommandEnum.SELECT_ONE -> {
                        while(true){
                            val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_RAIRA_WIND_ATTACK)
                            if(selectDustToDistance(nowCommand, game_status, player,
                                    game_status.getCardOwner(card_number), card_number)) {
                                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                                break
                            }
                        }
                        game_status.gaugeIncreaseRequest(player, NUMBER_RAIRA_WIND_ZEN_KAI)
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
        windZenKai.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ _, player, game_status, _->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.YOUR_USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_RAIRA_WIND_ZEN_KAI, 1){ _, _ ->
                true
            }?.let {selected ->
                game_status.returnSpecialCard(player, selected[0])
            }
            null
        })
        windZenKai.addText(Text(TextEffectTimingTag.USED, TextEffectTag.COST_BUFF){ card_number, player, game_status, _ ->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true}, { cost, _, _ ->
                if (cost <= 0){
                    0
                }
                else{
                    cost - 1
                }
            }))
            null
        })


        windCelestialSphere.setSpecial(4)
        windCelestialSphere.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
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
                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                    break
                }
                CommandEnum.SELECT_TWO -> {
                    game_status.flareToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                        game_status.getCardOwner(card_number), card_number)
                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
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
    private suspend fun moveResourceToDust(player: PlayerEnum, game_status: GameStatus, card_number: Int, number: Int){
        val nowPlayer = game_status.getPlayer(player)
        var moveAura = 0; var moveLife = 0; var moveFlare = 0
        val canMove = nowPlayer.life + nowPlayer.aura + nowPlayer.flare

        if (canMove <= number){
            moveAura = nowPlayer.aura; moveLife = nowPlayer.life; moveFlare = nowPlayer.flare
        }
        else{
            for (nowMove in 1..number){
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
        }

        if(moveLife > 0){
            game_status.lifeToDust(player, moveLife, Arrow.NULL, player,
                game_status.getCardOwner(card_number), card_number)
        }
        if(moveFlare > 0){
            game_status.flareToDust(player, moveFlare, Arrow.NULL, player,
                game_status.getCardOwner(card_number), card_number)
        }
        if(moveAura > 0){
            game_status.auraToDust(player, moveAura, Arrow.NULL, player,
                game_status.getCardOwner(card_number), card_number)
        }

        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
    }

    private fun utsuroCardInit(){
        wonwol.setAttack(DistanceType.CONTINUOUS, Pair(5, 7), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        wonwol.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        blackWave.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { _, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_UTSURO_BLACK_WAVE, 1)
            { _, _ -> true }?.let { selected ->
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let{ card ->
                    game_status.insertCardTo(player.opposite(), card, LocationEnum.DISCARD_YOUR, true)
                }
            }
            null
        })


        harvest.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 999, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        harvest.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status, NUMBER_UTSURO_HARVEST, 2)
            null
        })
        harvest.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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


        pressure.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) ret@{ _, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status, NUMBER_UTSURO_PRESSURE, 1)
            null
        })
        pressure.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) ret@{ _, player, game_status, _ ->
            if(hoejin(game_status)){
                game_status.setShrink(player.opposite())
            }
            null
        })


        shadowWing.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_THIS_TURN_DISTANCE) { _, _, game_status, _ ->
            game_status.addThisTurnDistance(2)
            null
        })
        shadowWing.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_THIS_TURN_SWELL_DISTANCE) { _, _, game_status, _ ->
            game_status.addThisTurnSwellDistance(2)
            null
        })


        shadowWall.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) ret@{ card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {_, _, attack ->
                    attack.lifePlusMinus(-1)
                }))
            null
        })


        yueHoeJu.setEnchantment(2)
        yueHoeJu.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.auraToDust(player.opposite(), 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        yueHoeJu.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            if(hoejin(game_status)){
                game_status.dustToAura(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
                game_status.lifeToDust(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        hoeMyeol.setSpecial(24)
        hoeMyeol.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) { card_number, player, game_status, _->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card ->
                (card.card_data.card_name == CardName.UTSURO_HOE_MYEOL)}, {cost, _, gameStatus ->
                cost - gameStatus.dust
            }))
            null
        })
        hoeMyeol.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.lifeToDust(player.opposite(), 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number
            )
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })


        heoWi.setSpecial(3)
        heoWi.setEnchantment(3)
        heoWi.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, game_status, react_attack ->
            react_attack?.addRangeBuff(game_status.useBuffNumberCounter(), RangeBuff(card_number,1, RangeBufTag.MINUS_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(-1, true)
                }))
            null
        })
        heoWi.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player.opposite(), RangeBuff(card_number,1, RangeBufTag.MINUS_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(-1, true)
            }))
            null
        })
        heoWi.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.AFTER_ATTACK_EFFECT_INVALID_OTHER){ _, _, _, _ ->
            null
        })
        heoWi.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_DEPLOYMENT_OTHER){ _, player, game_status, _ ->
            game_status.getPlayer(player.opposite()).napBuff -= 1
            null
        })
        heoWi.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.AFTER_DESTRUCTION_EFFECT_INVALID_OTHER){ _, _, _, _ ->
            null
        })


        jongMal.setSpecial(2)
        jongMal.setEnchantment(3)
        jongMal.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_GET_DAMAGE_BY_ATTACK){ card_number, player, game_status, _ ->
            game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?.let { card ->
                game_status.cardToDust(player, card.getNap(), card, false, card_number)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                if(card.isItDestruction()){
                    game_status.enchantmentDestruction(player, card)
                }
            }
            null
        })
        jongMal.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.END_CURRENT_PHASE) { _, _, game_status, _ ->
            game_status.endCurrentPhase = true
            null
        })
        jongMal.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, _, game_status, _ ->
            if(hoejin(game_status)){
                1
            }
            else{
                0
            }
        })


        maSig.setSpecial(4)
        maSig.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, masigText)
            null
        })
    }

    private val nanta = CardData(CardClass.NORMAL, CardName.YURINA_NAN_TA, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val beanBullet = CardData(CardClass.NORMAL, CardName.YURINA_BEAN_BULLET, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.REACTION)
    private val beanBulletText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
        game_status.setShrink(player.opposite())
        null
    }
    private val notCompletePobaram = CardData(CardClass.SPECIAL, CardName.YURINA_NOT_COMPLETE_POBARAM, MegamiEnum.YURINA, CardType.ATTACK, SubType.REACTION)

    private fun yurinaA1CardInit(){
        nanta.setAttack(DistanceType.CONTINUOUS, Pair(2, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        nanta.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        beanBullet.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.YURINA_BEAN_BULLET,
                        NUMBER_YURINA_BEAN_BULLET_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4), 1,  999,  MegamiEnum.YURINA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                        ).addTextAndReturn(beanBulletText)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        notCompletePobaram.setSpecial(5)
        notCompletePobaram.addText(terminationText)
        notCompletePobaram.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 3, 999,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        notCompletePobaram.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, _, react_attack ->
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
        soundOfIce.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, react_attack ->
            if((react_attack != null && react_attack.isItReact)){
                game_status.auraToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            else{
                game_status.auraToDust(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        accompaniment.setEnchantment(4)
        accompaniment.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            if(!(game_status.gameLogger.checkThisTurnDoAttack(player.opposite()))){
                game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                    { buff_player, buff_game_status, _ ->
                        isUsedSomeOtherMegamisSpecial(buff_game_status, buff_player, MegamiEnum.SAINE)
                            },
                    { _, _, madeAttack ->
                        madeAttack.auraPlusMinus(-1)
                }))
            }
            null
        })
        accompaniment.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.COST_BUFF){ card_number, player, game_status, _ ->
            if(isUsedSomeMegamisSpecial(game_status, player, MegamiEnum.SAINE)){
                game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true}, { cost, _, _ ->
                    if(cost <= 0){
                        0
                    }
                    else{
                        cost - 1
                    }
                }))
            }
            null
        })


        duetTanJuBingMyeong.setSpecial(2)
        duetTanJuBingMyeong.addText(terminationText)
        duetTanJuBingMyeong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _->
            game_status.setShrink(player.opposite())
            null
        })
        duetTanJuBingMyeong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
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
        duetTanJuBingMyeong.addText(Text(TextEffectTimingTag.USED, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, attack ->
                attack.megami != MegamiEnum.SAINE
            }, {_, _, attack ->
                attack.apply {
                    lifePlusMinus(1)
                }
            }))
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, { _, _, attack ->
                attack.megami != MegamiEnum.SAINE },
                { _, _, attack ->
                    attack.makeInevitable()
                })
            )
            null
        })
        duetTanJuBingMyeong.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                        _, reconstruct, damage ->
                if(!reconstruct && damage){
                    gameStatus.returnSpecialCard(player, cardNumber)
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
        fireWave.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {conditionPlayer, conditionGameStatus, _ -> yeonwha(conditionPlayer, conditionGameStatus) },
                {_, _, attack ->
                    attack.lifePlusMinus(1)
                }))
            null
        }))


        satSui.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            if (game_status.getPlayer(player).hand.size == 0) {
                game_status.auraToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        enTenHimika.setSpecial(5)
        enTenHimika.setAttack(DistanceType.CONTINUOUS, Pair(0, 7), null, 1000, 1000,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        enTenHimika.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {_, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = 8 - gameStatus.getAdjustDistance()
                    tempEditedAuraDamage = temp
                    tempEditedLifeDamage = temp
                }
            }))
            null
        })
        enTenHimika.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.GAME_END) { _, player, game_status, _ ->
            game_status.gameEnd(null, player)
            null
        })
    }

    private val flowingPlay = CardData(CardClass.NORMAL, CardName.TOKOYO_FLOWING_PLAY, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val soundOfSun = CardData(CardClass.NORMAL, CardName.TOKOYO_SOUND_OF_SUN, MegamiEnum.TOKOYO, CardType.ENCHANTMENT, SubType.NONE)
    private val duetChitanYangMyeong = CardData(CardClass.SPECIAL, CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)

    private val duetChitanYangMyeongText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){_, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG)){
                CommandEnum.SELECT_ONE -> {
                    game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_TOKOYO_SOUND_OF_SUN, 1
                    ) { _, _ -> true }?.let { selected ->
                        game_status.popCardFrom(player, selected[0], LocationEnum.COVER_CARD, false)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                    }?: continue
                    break
                }
                CommandEnum.SELECT_TWO -> {
                    game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG, 1
                    ) { card, _ -> card.card_data.card_type == CardType.BEHAVIOR }?.let{  selected ->
                        game_status.popCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                        }
                    }?: continue
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
        flowingPlay.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, buffRet@{ buff_player, buff_game_status, _ ->
                isUsedSomeMegamisSpecial(buff_game_status, buff_player, MegamiEnum.TOKOYO)
            }, { _, _, attack ->
                attack.canNotReact()
            }))
            null
        })
        flowingPlay.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(kyochi(player, game_status) && isUsedSomeOtherMegamisSpecial(game_status, player, MegamiEnum.TOKOYO)){
                if(thisCardMoveTextCheck(card_number.toCardName(), CardName.TOKOYO_FLOWING_PLAY)){
                    game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_TOP, card_number)
                }
            }
            null
        })


        soundOfSun.setEnchantment(2)
        soundOfSun.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_USE_REACT_CARD_YOUR_END) { card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
            null
        })
        soundOfSun.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { _, player, game_status, _ ->
            if(game_status.turnPlayer == player.opposite()) 1
            else 0
        })


        duetChitanYangMyeong.setSpecial(1)
        duetChitanYangMyeong.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, duetChitanYangMyeongText)
            null
        })
        duetChitanYangMyeong.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                        _, reconstruct, damage ->
                if(!reconstruct && damage){
                    gameStatus.returnSpecialCard(player, cardNumber)
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
    private val fourSeason = CardData(CardClass.SPECIAL, CardName.HONOKA_FOUR_SEASON_BACK, MegamiEnum.HONOKA, CardType.BEHAVIOR, SubType.REACTION)
    private val bloomPath = CardData(CardClass.SPECIAL, CardName.HONOKA_FULL_BLOOM_PATH, MegamiEnum.HONOKA, CardType.ENCHANTMENT, SubType.NONE)

    private val commandText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
        if(game_status.addPreAttackZone(
                player, MadeAttack(CardName.HONOKA_COMMAND, NUMBER_HONOKA_COMMAND_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(1, 2, 3, 4, 5), 1,  1, MegamiEnum.HONOKA,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                    )
            ) ){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }

    private val handFlowerText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) {_, player, game_status, _ ->
        game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_HONOKA_HAND_FLOWER)
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

        if(game_status.addPreAttackZone(
                player, MadeAttack(CardName.HONOKA_A_NEW_OPENING,
                    NUMBER_HONOKA_A_NEW_OPENING_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 1000,  1000, MegamiEnum.HONOKA,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                    )
            )){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }

    private fun dupligearCheck(cardName: CardName) = cardName == CardName.KURURU_DUPLICATED_GEAR_3
            || cardName == CardName.KURURU_DUPLICATED_GEAR_2
            || cardName == CardName.KURURU_DUPLICATED_GEAR_1

    private fun checkCardName(card_number: Int, cardName: CardName) = card_number.toCardName() == cardName

    private suspend fun requestCardChange(player: PlayerEnum, card_number: Int, game_status: GameStatus): Boolean{
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

    suspend fun checkCanCardChange(player: PlayerEnum, game_status: GameStatus, card_number: Int,
                                   original_card_name: CardName, change_card_name: CardName): Boolean{
        if(game_status.getCardFrom(player, change_card_name, LocationEnum.ADDITIONAL_CARD) != null){
            if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, original_card_name)){
                return true
            }
        }
        return false
    }

    fun honokaCardInit(){
        spiritSik.setAttack(DistanceType.CONTINUOUS, Pair(2, 8), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        spiritSik.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_SPIRIT_SIK, CardName.HONOKA_GUARDIAN_SPIRIT_SIK)){
                if(requestCardChange(player, NUMBER_HONOKA_SPIRIT_SIK, game_status)){
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
        guardianSik.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        guardianSik.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, CardName.HONOKA_ASSAULT_SPIRIT_SIK)){
                if(requestCardChange(player, NUMBER_HONOKA_GUARDIAN_SPIRIT_SIK, game_status)){
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
        assaultSik.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToLife(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        assaultSik.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_ASSAULT_SPIRIT_SIK, CardName.HONOKA_DIVINE_OUKA)){
                if(requestCardChange(player, NUMBER_HONOKA_ASSAULT_SPIRIT_SIK, game_status)){
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
        divineOuka.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToAura(player, 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        sakuraBlizzard.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        sakuraBlizzard.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        yuGiGongJin.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
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
        yuGiGongJin.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
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
        yuGiGongJin.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.HONOKA_UI_GI_GONG_JIN)){
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


        sakuraWing.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
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
        sakuraWing.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_SAKURA_WING, CardName.HONOKA_REGENERATION)){
                game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                game_status.moveAdditionalCard(player, CardName.HONOKA_REGENERATION, LocationEnum.DISCARD_YOUR)
            }
            null
        })


        regeneration.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.dustToFlare(player, 1, Arrow.ONE_DIRECTION,
                player, game_status.getCardOwner(card_number), card_number
            )
            null
        })
        regeneration.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_REGENERATION, CardName.HONOKA_SAKURA_WING)){
                game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_WING, LocationEnum.DISCARD_YOUR)
            }
            null
        })


        sakuraAmulet.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, react_attack ->
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
        sakuraAmulet.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_SAKURA_AMULET, CardName.HONOKA_HONOKA_SPARKLE)) {
                if(requestCardChange(player, NUMBER_HONOKA_ASSAULT_SPIRIT_SIK, game_status)){
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
        command.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){ card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, commandText)
            null
        })


        tailWind.setEnchantment(3)
        tailWind.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack -> attack.plusMinusRange(1, false)
                }))
            null
        })


        chestWilling.setSpecial(5)
        chestWilling.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_CHEST_WILLINGNESS, CardName.HONOKA_HAND_FLOWER)){
                game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                game_status.moveAdditionalCard(player, CardName.HONOKA_HAND_FLOWER, LocationEnum.YOUR_USED_CARD)?.let {
                    game_status.returnSpecialCard(player, it.card_number)
                }
            }
            null
        })


        handFlower.setSpecial(0)
        handFlower.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_HONOKA_HAND_FLOWER)
            null
        })
        handFlower.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, handFlowerText)
            null
        })
        handFlower.addText(Text(TextEffectTimingTag.USED, TextEffectTag.CONDITION_ADD_DO_WIND_AROUND) ret@{ _, player, game_status, _ ->
            if(game_status.getPlayerAura(player) != 0 || game_status.dust != 0) 1
            else 0
        })
        handFlower.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DO_WIND_AROUND) ret@{ card_number, player, game_status, _ ->
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
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                            if(it.getNap() == 5){
                                if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_HAND_FLOWER)){
                                    game_status.getCardFrom(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.ADDITIONAL_CARD)?.let { additionalCard ->
                                        game_status.cardToFlare(player, it.getNap(), it, card_number, LocationEnum.YOUR_USED_CARD)
                                        game_status.popCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD, true)
                                        game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                                        additionalCard.special_card_state = SpecialCardEnum.PLAYED
                                        game_status.moveAdditionalCard(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.YOUR_USED_CARD)
                                        game_status.returnSpecialCard(player, additionalCard.card_number)
                                        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
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
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                            if(it.getNap() == 5){
                                if(game_status.getCardOwner(card_number) == player && checkCardName(card_number, CardName.HONOKA_HAND_FLOWER)){
                                    game_status.getCardFrom(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.ADDITIONAL_CARD)?.let { additionalCard ->
                                        game_status.cardToFlare(player, it.getNap() , it, card_number, LocationEnum.YOUR_USED_CARD)
                                        game_status.popCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD, true)
                                        game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                                        additionalCard.special_card_state = SpecialCardEnum.PLAYED
                                        game_status.moveAdditionalCard(player, CardName.HONOKA_A_NEW_OPENING, LocationEnum.YOUR_USED_CARD)
                                        game_status.returnSpecialCard(player, additionalCard.card_number)
                                        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                                    }
                                }
                            }
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


        newOpening.setSpecial(5)
        newOpening.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, newOpeningText)
            null
        })


        underFlag.setSpecial(4)
        underFlag.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 3, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        underFlag.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADD_TEXT_TO_ATTACK) { _, player, game_status, _ ->
            while (true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HONOKA_UNDER_THE_NAME_OF_FLAG
                ) { _, _ -> true }?: break
                if (list.size == 1){
                    game_status.getPlayer(player).preAttackCard?.addTextAndReturn(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
                        list[0]
                    })
                    game_status.getPlayer(player).preAttackCard?.addTextAndReturn(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
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
        fourSeason.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
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
        fourSeason.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
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
        fourSeason.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_HONOKA_FULL_BLOOM_PATH, 1
            ) { card, _ -> card.card_data.canCover }?.let { selected ->
                game_status.popCardFrom(player, selected[0], LocationEnum.HAND, false)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
                    game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                        CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_HONOKA_FOUR_SEASON_BACK)
                }
            }
            null
        })
        fourSeason.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addAdditionalListener(player, Listener(player, card_number)ret@{gameStatus, cardNumber, _, _, _, _ ->
                while(true){
                    when(gameStatus.receiveCardEffectSelect(player, NUMBER_HONOKA_FULL_BLOOM_PATH)){
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
                    gameStatus.moveAdditionalCard(player, CardName.HONOKA_FOUR_SEASON_BACK, LocationEnum.SPECIAL_CARD)
                }
                true
            })
            null
        })


        bloomPath.setSpecial(2)
        bloomPath.setEnchantment(5)
        bloomPath.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE) { _, player, game_status, _ ->
            if(game_status.getPlayerAura(player) >= 5){
                LocationEnum.FLARE_YOUR.real_number
            }
            else{
                LocationEnum.AURA_YOUR.real_number
            }
        })
    }

    private val shuriken = CardData(CardClass.NORMAL, CardName.OBORO_SHURIKEN, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val ambush  = CardData(CardClass.NORMAL, CardName.OBORO_AMBUSH, MegamiEnum.OBORO, CardType.ATTACK, SubType.FULL_POWER)
    private val branchOfDivine = CardData(CardClass.SPECIAL, CardName.OBORO_BRANCH_OF_DIVINE, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.FULL_POWER)
    private val lastCrystal = CardData(CardClass.SPECIAL, CardName.OBORO_LAST_CRYSTAL, MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.NONE)

    private val shurikenText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD) { card_number, player, game_status, _ ->
        if(game_status.getPlayer(PlayerEnum.PLAYER1).coverCard.size + game_status.getPlayer(PlayerEnum.PLAYER2).coverCard.size >= 5){
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
        shuriken.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD) { card_number, player, game_status, _ ->
            if(game_status.getCardFrom(player, card_number, LocationEnum.DISCARD_YOUR)?.isSoftAttack == false){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.DISCARD_YOUR, shurikenText)
            }
            null
        })


        ambush.setAttack(DistanceType.CONTINUOUS, Pair(1, 3), null, 4, 3,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        ambush.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {attack_player, attack_game_status, attack ->
                val temp = attack_game_status.getPlayer(attack_player.opposite()).coverCard.size
                attack.apply {
                    auraPlusMinus(-temp); lifePlusMinus(-temp)
                }
            }))
            null
        })


        branchOfDivine.setSpecial(0)
        branchOfDivine.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.outToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.outToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        branchOfDivine.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            game_status.moveAdditionalCard(player, CardName.OBORO_LAST_CRYSTAL, LocationEnum.SPECIAL_CARD)
            null
        })


        lastCrystal.setSpecial(3)
        lastCrystal.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, player, game_status, _->
            if(game_status.getPlayer(player).loseCounter == 1) 1
            else 0
        })
        lastCrystal.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_LOSE_GAME)ret@{ card_number, player, game_status, _->
            val nowPlayer = game_status.getPlayer(player)
            if(nowPlayer.loseCounter == 0){
                nowPlayer.loseCounter += 1
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_OBORO_LAST_CRYSTAL)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.lifeToDust(player, nowPlayer.life, Arrow.NULL, player,
                                game_status.getCardOwner(card_number), card_number, true)
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                            val useSuccess = game_status.getCardFrom(player, card_number, LocationEnum.SPECIAL_CARD)?.let {
                                game_status.useCardFrom(player, it, LocationEnum.SPECIAL_CARD, false, null,
                                    isCost = true, isConsume = true
                                )
                            }?: game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                                game_status.useCardFrom(player, it, LocationEnum.YOUR_USED_CARD, false, null,
                                    isCost = true, isConsume = true
                                )
                            }?: false
                            nowPlayer.loseCounter += 1
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
        lastCrystal.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            game_status.coverCard(player, player, NUMBER_OBORO_LAST_CRYSTAL)
            null
        })
        lastCrystal.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
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
        trickUmbrella.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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


        struggle.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).hand.size >= 2){
                game_status.addConcentration(player)
            }
            null
        })
        struggle.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_CHIKAGE_STRUGGLE)
                if(selectDustToDistance(nowCommand, game_status, player,
                        game_status.getCardOwner(card_number), card_number)) break
            }
            null
        })


        zanZeNoConnectionPoison.setSpecial(4)
        zanZeNoConnectionPoison.setAttack(DistanceType.CONTINUOUS, Pair(0, 1), null, 4, 1000,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        zanZeNoConnectionPoison.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    val temp = gameStatus.getPlayer(nowPlayer.opposite()).hand.size * 2
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

        nowPlayer.normalCardDeck.filterNot { card -> card.card_data.isItSpecial() }.forEach{ card ->
            game_status.popCardFrom(player, card.card_number, LocationEnum.DECK, false)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, false)
            }
        }

        nowPlayer.hand.values.filterNot { it.card_data.isItSpecial() }.forEach { card ->
            game_status.popCardFrom(player, card.card_number, LocationEnum.HAND, false)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, false)
            }
        }

        nowPlayer.coverCard.filterNot { it.card_data.isItSpecial() }.forEach { card ->
            game_status.popCardFrom(player, card.card_number, LocationEnum.COVER_CARD, false)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, false)
            }
        }

        nowPlayer.discard.filterNot { it.card_data.isItSpecial() }.forEach { card ->
            game_status.popCardFrom(player, card.card_number, LocationEnum.DISCARD_YOUR, true)?.let {
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, true)
            }
        }

        nowPlayer.enchantmentCard.values.filterNot {
                it.card_data.isItSpecial()
        }.forEach { card ->
            if((card.getNap() ?: 0) >= 1){
                game_status.cardToDust(player, card.getNap(), card, false, EventLog.DEMISE)
            }
            game_status.popCardFrom(player, card.card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                card.effectText(player, game_status, null, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
                game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, true)
            }
        }

        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.DEMISE, -1))

        game_status.moveAdditionalCard(player, CardName.UTSURO_ANNIHILATION_SHADOW, LocationEnum.DISCARD_YOUR)
        game_status.moveAdditionalCard(player, CardName.UTSURO_SILENT_WALK, LocationEnum.DISCARD_YOUR)
        game_status.moveAdditionalCard(player, CardName.UTSURO_DE_MISE, LocationEnum.DISCARD_YOUR)

        game_status.deckReconstruct(player, false)
    }

    private fun utsuroA1CardInit(){
        biteDust.setAttack(DistanceType.CONTINUOUS, Pair(3, 6), null, 2, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = false)
        biteDust.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) { card_number, player, game_status, _ ->
            game_status.flareToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        deviceKururusik.setSpecial(2)
        deviceKururusik.setEnchantment(2)
        deviceKururusik.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { _, player, game_status, _ ->
            if(game_status.dust >= 13 && game_status.getPlayerLife(player) <= 6) 0
            else 1
        })
        deviceKururusik.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(game_status.nowPhase == START_PHASE) {
                reviveDemise(player, game_status)
                game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
                }
                game_status.moveAdditionalCard(player, CardName.UTSURO_MANG_A, LocationEnum.YOUR_USED_CARD)?.let {
                    it.special_card_state = SpecialCardEnum.PLAYED
                    game_status.addMainPhaseListener(player, Listener(player, it.card_number) {gameStatus, cardNumber, _,
                                                                                                            _, _, _ ->
                        gameStatus.returnSpecialCard(player, cardNumber)
                        true
                    })
                }
                game_status.drawCard(player, 1)
            }
            null
        })


        mangA.setSpecial(6)
        mangA.addText(Text(TextEffectTimingTag.USED, TextEffectTag.CAN_NOT_GET_DAMAGE) { _, _, _, _ ->
            1
        })
        mangA.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addMainPhaseListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                        _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })


        annihilationShadow.setAttack(DistanceType.CONTINUOUS, Pair(0, 3), null, 999, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        annihilationShadow.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            moveResourceToDust(player.opposite(), game_status,NUMBER_UTSURO_ANNIHILATION_SHADOW, 6)
            null
        })


        silentWalk.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ _, player, game_status, _ ->
            for(i in 1..5){
                val command = game_status.requestAndDoBasicOperation(player,
                    NUMBER_UTSURO_SILENT_WALK, hashSetOf(CommandEnum.ACTION_GO_FORWARD))
                if(command == CommandEnum.SELECT_NOT){
                    break
                }
            }
            null
        })
        silentWalk.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.UTSURO_SILENT_WALK,
                            NUMBER_UTSURO_SILENT_WALK_ADDITIONAL_1, CardClass.NULL,
                            sortedSetOf(4, 5, 6, 7, 8, 9, 10), 3,  2, MegamiEnum.UTSURO,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        silentWalk.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.UTSURO_SILENT_WALK,
                            NUMBER_UTSURO_SILENT_WALK_ADDITIONAL_2, CardClass.NULL,
                            sortedSetOf(5, 6, 7, 8, 9, 10), 1,  1, MegamiEnum.UTSURO,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        silentWalk.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.UTSURO_SILENT_WALK,
                            NUMBER_UTSURO_SILENT_WALK_ADDITIONAL_3, CardClass.NULL,
                            sortedSetOf(6, 7, 8, 9, 10), 1,  1, MegamiEnum.UTSURO,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )) {
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        deMise.setEnchantment(2)
        deMise.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            val otherPlayer = game_status.getPlayer(player.opposite())

            otherPlayer.hand.keys.toList().forEach {cardNumber ->
                game_status.popCardFrom(player.opposite(), cardNumber, LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }

            while(otherPlayer.normalCardDeck.isNotEmpty()){
                game_status.popCardFrom(player.opposite(), 1, LocationEnum.YOUR_DECK_TOP, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }
            null
        })
        deMise.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setConcentration(player.opposite(), 0)
            game_status.setShrink(player.opposite())
            null
        })
    }

    private val snowBlade = CardData(CardClass.NORMAL, CardName.KORUNU_SNOW_BLADE, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val revolvingBlade = CardData(CardClass.NORMAL, CardName.KORUNU_REVOLVING_BLADE, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val bladeDance = CardData(CardClass.NORMAL, CardName.KORUNU_BLADE_DANCE, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val snowRide = CardData(CardClass.NORMAL, CardName.KORUNU_RIDE_SNOW, MegamiEnum.KORUNU, CardType.BEHAVIOR, SubType.NONE)
    private val absoluteZero = CardData(CardClass.NORMAL, CardName.KORUNU_ABSOLUTE_ZERO, MegamiEnum.KORUNU, CardType.BEHAVIOR, SubType.REACTION)
    private val frostbite = CardData(CardClass.NORMAL, CardName.KORUNU_FROSTBITE, MegamiEnum.KORUNU, CardType.ENCHANTMENT, SubType.NONE)
    private val thornBush = CardData(CardClass.NORMAL, CardName.KORUNU_FROST_THORN_BUSH, MegamiEnum.KORUNU, CardType.ENCHANTMENT, SubType.NONE)
    private val conluRuyanpeh = CardData(CardClass.SPECIAL, CardName.KORUNU_CONLU_RUYANPEH, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val letarLera = CardData(CardClass.SPECIAL, CardName.KORUNU_LETAR_LERA, MegamiEnum.KORUNU, CardType.BEHAVIOR, SubType.REACTION)
    private val upastum = CardData(CardClass.SPECIAL, CardName.KORUNU_UPASTUM, MegamiEnum.KORUNU, CardType.ATTACK, SubType.NONE)
    private val porucharto = CardData(CardClass.SPECIAL, CardName.KORUNU_PORUCHARTO, MegamiEnum.KORUNU, CardType.ENCHANTMENT, SubType.NONE)

    private fun korunuCardInit(){
        snowBlade.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        snowBlade.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.FREEZE) { _, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })


        revolvingBlade.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        revolvingBlade.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_REACTED_AFTER) { card_number, player, game_status, _->
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
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
            null
        })


        bladeDance.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        bladeDance.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player.opposite()).checkAuraFull()
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })


        snowRide.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
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


        absoluteZero.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.KORUNU_ABSOLUTE_ZERO,
                            NUMBER_KORUNU_ABSOLUTE_ZERO_ADDITIONAL, CardClass.NULL,
                                sortedSetOf(2, 3, 4, 5), 1,  2, MegamiEnum.KORUNU,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                            )
                    ) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }
                val freezePlayer = game_status.getPlayer(player.opposite())
                game_status.outToAuraFreeze(player.opposite(), freezePlayer.maxAura - freezePlayer.aura - freezePlayer.freezeToken)
            }
            null
        })
        absoluteZero.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_KORUNU_ABSOLUTE_ZERO)
            if(game_status.getPlayer(player.opposite()).freezeToken >= 3){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                        CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_KORUNU_ABSOLUTE_ZERO)
            }
            null
        })


        frostbite.setEnchantment(2)
        frostbite.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.FREEZE) { _, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })
        frostbite.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_INCUBATE_OTHER){ _, _, _, _ ->
            1
        })


        thornBush.setEnchantment(2)
        thornBush.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            if(!(game_status.gameLogger.checkThisTurnDoAttackNotSpecial(player))){
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
        thornBush.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { reason, player, game_status, _ ->
            var result = 0
            if(reason == EventLog.NORMAL_NAP_PROCESS && game_status.turnPlayer == player && game_status.getPlayer(player.opposite()).freezeToken >= 1) {
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_FROST_THORN_BUSH)){
                        CommandEnum.SELECT_ONE -> {
                            result = 1
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            result = 0
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
        conluRuyanpeh.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { _, player, game_status, _ ->
            val freezePlayer = game_status.getPlayer(player.opposite())
            game_status.outToAuraFreeze(player.opposite(), freezePlayer.maxAura - freezePlayer.aura - freezePlayer.freezeToken)
            null
        })


        letarLera.setSpecial(2)
        letarLera.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, react_attack ->
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
        upastum.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.FREEZE) { _, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })
        upastum.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addAuraListener(player.opposite(), Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                        _, before_full, after_full ->
                if(!before_full && after_full){
                    gameStatus.returnSpecialCard(player, cardNumber)
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
        porucharto.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_USE_REACT) { _, _, _, _ ->
            1
        })
        porucharto.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) { card_number, player, game_status, react_attack->
            if((react_attack != null && react_attack.isItReact)){
                game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, card ->
                    (card.card_data.card_name == CardName.KORUNU_PORUCHARTO)}, {_, _, _ ->
                    4
                }))
            }
            null
        })
        porucharto.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_KORUNU_UPASTUM)
                if(selectDustToDistance(nowCommand, game_status, player,
                        game_status.getCardOwner(card_number), card_number)) break
            }
            null
        })
        porucharto.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.FREEZE) { _, player, game_status, _ ->
            game_status.outToAuraFreeze(player.opposite(), 1)
            null
        })
        porucharto.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.USE_CARD) { card_number, player, game_status, _ ->
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
    private val willing = CardData(CardClass.NORMAL, CardName.YATSUHA_WILLING, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.REACTION)
    private val contract = CardData(CardClass.NORMAL, CardName.YATSUHA_CONTRACT, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.REACTION)
    private val clingyFlower = CardData(CardClass.NORMAL, CardName.YATSUHA_CLINGY_FLOWER, MegamiEnum.YATSUHA, CardType.ENCHANTMENT, SubType.NONE)
    private val clingyFlowerText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) { card_number, player, game_status, _ ->
        game_status.auraToAura(player.opposite(), player, 2, Arrow.ONE_DIRECTION, player,
            game_status.getCardOwner(card_number), card_number)
        null
    }
    private val twoLeapMirrorDivine = CardData(CardClass.SPECIAL, CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.REACTION)
    private val fourLeapSong = CardData(CardClass.SPECIAL, CardName.YATSUHA_FOUR_LEAP_SONG, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val sixStarSea = CardData(CardClass.SPECIAL, CardName.YATSUHA_SIX_STAR_SEA, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.NONE)
    private val eightMirrorOtherSide = CardData(CardClass.SPECIAL, CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE, MegamiEnum.YATSUHA, CardType.ENCHANTMENT, SubType.NONE)

    private val contractText = Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
        if(game_status.getPlayerFlare(player) >= game_status.getPlayerFlare(player.opposite())){
            game_status.auraToFlare(player, player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
        }
        null
    }

    private fun yatsuhaCardInit(){
        starNail.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 2,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        starNail.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.auraToFlare(player, player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        darknessGill.setAttack(DistanceType.CONTINUOUS, Pair(4, 4), null, 3, 1,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        darknessGill.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, gameStatus, attack ->
                attack.lifePlusMinus(gameStatus.getMirror())
            }))
            null
        })
        darknessGill.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setShrink(player)
            null
        })


        mirrorDevil.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 5, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        mirrorDevil.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.lifeToDust(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        ghostStep.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        ghostStep.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
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


        willing.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
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


        contract.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.flareToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        contract.addText(Text(TextEffectTimingTag.USING, TextEffectTag.ADD_END_PHASE_EFFECT) { card_number, player, game_status, _ ->
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
        clingyFlower.addText(chasmText)
        clingyFlower.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.THIS_CARD_NAP_CHANGE) { _, player, game_status, _ ->
            game_status.getPlayer(player).napBuff -= game_status.getMirror()
            null
        })
        clingyFlower.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.YATSUHA_CLINGY_FLOWER,
                        NUMBER_YATSUHA_CLINGY_FLOWER_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(1, 2, 3, 4), 0,  0, MegamiEnum.YATSUHA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        ).addTextAndReturn(clingyFlowerText)
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        twoLeapMirrorDivine.setSpecial(4)
        twoLeapMirrorDivine.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, react_attack ->
            if(game_status.getPlayerLife(player.opposite()) > game_status.getPlayerLife(player)){
                react_attack?.let {attack ->
                    val damage = attack.getDamage(game_status, player.opposite(), game_status.getPlayerAttackBuff(player.opposite()))
                    attack.rangeCheck(-1, game_status, player.opposite())
                    attack.activeOtherBuff(game_status, player, game_status.getPlayerOtherBuff(player.opposite()))
                    if(game_status.addPreAttackZone(player, MadeAttack(CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE,
                            NUMBER_YATSUHA_TWO_LEAP_MIRROR_DIVINE_ADDITIONAL, attack.card_class,
                            attack.editedDistance, damage.first, damage.second, attack.megami,
                            cannotReactNormal = attack.editedCannotReactNormal,
                            cannotReactSpecial = attack.editedCannotReactSpecial,
                            cannotReact = attack.editedCannotReact,
                            chogek = attack.editedChogek, inevitable = attack.editedInevitable).also {
                            attack.copyAfterAttackTo(it)
                        })){
                        game_status.afterMakeAttack(card_number, player, attack)
                    }
                }
            }
            null
        })
        twoLeapMirrorDivine.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, reactedAttack ->
            if(reactedAttack?.card_class != CardClass.SPECIAL){
                reactedAttack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })


        fourLeapSong.setSpecial(2)
        fourLeapSong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{ card_number, player, game_status, _->
            while (true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_YATSUHA_FOUR_LEAP_SONG)
                {card, _ -> card.card_data.card_class != CardClass.SPECIAL && card.card_data.card_type != CardType.UNDEFINED}?: break
                if (selected.size == 1){
                    game_status.getCardFrom(player, selected[0], LocationEnum.ENCHANTMENT_ZONE)?.also {
                        game_status.cardToDust(player, it.getNap() , it, false, card_number)
                        if(it.isItDestruction()){
                            game_status.enchantmentDestruction(player, it)
                        }
                    }?: game_status.getCardFrom(player.opposite(), selected[0], LocationEnum.ENCHANTMENT_ZONE)?.also {
                        game_status.cardToDust(player.opposite(), it.getNap() , it, false, card_number)
                        if(it.isItDestruction()){
                            game_status.enchantmentDestruction(player.opposite(), it)
                        }
                    }

                    if(game_status.endCurrentPhase){
                        break
                    }

                    val (location, card) = game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?.let {
                        Pair(LocationEnum.DISCARD_YOUR, it)
                    }?: game_status.getCardFrom(player.opposite(), selected[0], LocationEnum.DISCARD_YOUR)?.let {
                        Pair(LocationEnum.DISCARD_OTHER, it)
                    }?: break

                    while (true){
                        when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_FOUR_LEAP_SONG)){
                            CommandEnum.SELECT_ONE -> {
                                if(game_status.useCardFromNotFullAction(player, card, location, false, null,
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
        sixStarSea.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        eightMirrorOtherSide.addText(terminationText)
        eightMirrorOtherSide.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_ARROW_BOTH){ _, _, _, _ ->
            1
        })
        eightMirrorOtherSide.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
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
        zhenYen.addText(Text(TextEffectTimingTag.USING, TextEffectTag.RUN_STRATAGEM) { card_number, player, game_status, react_attack->
            if((react_attack != null && react_attack.isItReact) || game_status.getPlayer(player).justRunStratagem){
                when(game_status.getStratagem(player)){
                    Stratagem.SHIN_SAN -> {
                        if(game_status.getPlayer(player.opposite()).normalCardDeck.size >= 3){
                            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1),
                                false, null, null, NUMBER_SHINRA_ZHEN_YEN)
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_SHINRA_ZHEN_YEN, -1))
                        }
                        prepareStratagem(player, game_status)
                    }
                    Stratagem.GUE_MO -> {
                        if(game_status.getPlayer(player.opposite()).normalCardDeck.size <= 3){
                            game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(2, 999),
                                false, null, null, card_number)
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                        }
                        prepareStratagem(player, game_status)
                    }
                    null -> {}
                }
            }
            null
        })


        sado.setEnchantment(2)
        sado.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.RUN_STRATAGEM) { card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.SHINRA_SA_DO,
                                    NUMBER_SHINRA_SA_DO_ADDITIONAL_1, CardClass.NULL,
                                    sortedSetOf(1, 3, 5), 2,  2,  MegamiEnum.SHINRA,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                                )
                        ) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    if(game_status.endCurrentPhase){
                        game_status.getPlayer(player).stratagem = null
                    }
                    else{
                        prepareStratagem(player, game_status)
                    }
                }
                Stratagem.GUE_MO -> {
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.SHINRA_SA_DO,
                                    NUMBER_SHINRA_SA_DO_ADDITIONAL_2, CardClass.NULL,
                                    sortedSetOf(2, 4, 6), 2, 2,  MegamiEnum.SHINRA,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                                )
                        ) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    if(game_status.endCurrentPhase){
                        game_status.getPlayer(player).stratagem = null
                    }
                    else{
                        prepareStratagem(player, game_status)
                    }
                }
                null -> {}
            }
            null
        })
        sado.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _->
            when(game_status.getStratagem(player)){
                Stratagem.SHIN_SAN -> {
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.SHINRA_SA_DO,
                                    NUMBER_SHINRA_SA_DO_ADDITIONAL_1, CardClass.NULL,
                                    sortedSetOf(1, 3, 5), 2,  2,  MegamiEnum.SHINRA,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                                )
                        ) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    if(game_status.endCurrentPhase){
                        game_status.getPlayer(player).stratagem = null
                    }
                    else{
                        prepareStratagem(player, game_status)
                    }
                }
                Stratagem.GUE_MO -> {
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.SHINRA_SA_DO,
                                    NUMBER_SHINRA_SA_DO_ADDITIONAL_2, CardClass.NULL,
                                    sortedSetOf(2, 4, 6), 2, 2,  MegamiEnum.SHINRA,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                                )
                        ) ){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                    if(game_status.endCurrentPhase){
                        game_status.getPlayer(player).stratagem = null
                    }
                    else{
                        prepareStratagem(player, game_status)
                    }
                }
                null -> {}
            }
            null
        })


        zenChiKyoTen.setSpecial(4)
        zenChiKyoTen.setAttack(DistanceType.CONTINUOUS, Pair(0, 5), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        zenChiKyoTen.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) ret@{ _, player, game_status, _ ->
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
        zenChiKyoTen.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.RUN_STRATAGEM) ret@{ _, player, game_status, _ ->
            val usedSet = mutableSetOf<Int>()
            game_status.getPlayer(player).justRunStratagem = true
            while(true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_SHINRA_ZEN_CHI_KYO_TEN
                ) { card, _ -> card.card_number !in usedSet && card.thisCardHaveStratagem() }?: break
                if(list.size == 1){
                    game_status.getCardFrom(player, list[0], LocationEnum.DISCARD_YOUR)?.runStratagem(player, game_status)
                    usedSet.add(list[0])
                    if(game_status.endCurrentPhase){
                        break
                    }
                }
                else if(list.size == 0){
                    break
                }
            }
            game_status.getPlayer(player).justRunStratagem = false
            null
        })
    }

    private val analyze = CardData(CardClass.NORMAL, CardName.KURURU_ANALYZE, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val dauzing = CardData(CardClass.NORMAL, CardName.KURURU_DAUZING, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val lastResearch = CardData(CardClass.SPECIAL, CardName.KURURU_LAST_RESEARCH, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val grandGulliver = CardData(CardClass.SPECIAL, CardName.KURURU_GRAND_GULLIVER, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)

    private suspend fun analyzeWhenNotAttack(player: PlayerEnum, game_status: GameStatus){
        game_status.selectCardFrom(player.opposite(), player.opposite(), player,
            listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            NUMBER_KURURU_DUPLICATED_GEAR_2, 1
        ) { _, _ -> true }?.let {selected ->
            game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, false)?.let{
                game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
            }
        }
    }

    private suspend fun analyzeYour(player: PlayerEnum, game_status: GameStatus){
        game_status.selectCardFrom(player, player, player,
            listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            NUMBER_KURURU_ANALYZE, 1
        ) { _, _ -> true }?.let { selected ->
            game_status.popCardFrom(player, selected[0], LocationEnum.COVER_CARD, true)?.let{
                game_status.insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                if(it.card_data.card_type == CardType.ATTACK){
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                        null, null, NUMBER_KURURU_ANALYZE)
                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KURURU_ANALYZE, -1))
                }
                else{
                    analyzeWhenNotAttack(player, game_status)
                }
            }
        }
    }

    private suspend fun analyzeOther(player: PlayerEnum, game_status: GameStatus){
        game_status.popCardFrom(player.opposite(),
            game_status.getPlayer(player.opposite()).coverCard.random(Random(System.currentTimeMillis())).card_number,
            LocationEnum.COVER_CARD, true)?.let{
            game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
            if(it.card_data.card_type == CardType.ATTACK){
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, NUMBER_KURURU_ANALYZE)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KURURU_ANALYZE, -1))
            }
            else{
                analyzeWhenNotAttack(player, game_status)
            }
        }
    }

    private suspend fun greatDiscovery(lastResearch: Int, player: PlayerEnum, game_status: GameStatus){
        game_status.removeImmediateReconstructListener(player, lastResearch)
        game_status.endPhaseEffect.remove(lastResearch)
        game_status.showSome(player.opposite(), CommandEnum.SHOW_SPECIAL_YOUR)

//        delay(20000)

        while(true) {
            when (game_status.receiveCardEffectSelect(player, NUMBER_KURURU_LAST_RESEARCH)) {
                CommandEnum.SELECT_ONE -> {
                    val unused = game_status.getPlayer(player).unselectedSpecialCard
                    game_status.moveOutCard(player, unused, LocationEnum.SPECIAL_CARD)
                    unused.clear()
                    break
                }

                CommandEnum.SELECT_TWO -> {
                    val unused = game_status.getPlayer(player.opposite()).unselectedSpecialCard
                    game_status.moveOutCard(player, unused, LocationEnum.SPECIAL_CARD)
                    unused.clear()
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
        analyze.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.enchantment >= 1 && kikou.attack >= 1 && kikou.reaction >= 1) {
                val otherSize = game_status.getPlayer(player.opposite()).coverCard.size
                val yourSize = game_status.getPlayer(player).coverCard.size
                if(yourSize != 0 || otherSize != 0){
                    if(yourSize == 0){
                        analyzeOther(player, game_status)
                    }
                    else if(otherSize == 0){
                        analyzeYour(player, game_status)
                    }
                    else{
                        while(true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_KURURU_ANALYZE)){
                                CommandEnum.SELECT_ONE -> {
                                    analyzeYour(player, game_status)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    analyzeOther(player, game_status)
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


        dauzing.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ _, player, game_status, _ ->
            game_status.popCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP, true)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
            }

            game_status.selectCardFrom(player.opposite(), player, player,
                listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_KURURU_DAUZING, 1
            ) { card, _ -> card.card_data.card_type != CardType.UNDEFINED }?.let { selected ->
                game_status.getCardFrom(player.opposite(), selected[0], LocationEnum.DISCARD_YOUR)?.let {
                    val kikou = getKikou(player, game_status)
                    val conditionKikou = Kikou(1, 0, 0, 0, 0).apply {
                        add(it)
                    }
                    if(kikou.attack >= conditionKikou.attack && kikou.enchantment >= conditionKikou.enchantment &&
                        kikou.behavior >= conditionKikou.behavior && kikou.reaction >= conditionKikou.reaction &&
                        kikou.fullPower >= conditionKikou.fullPower){
                        game_status.useCardFromNotFullAction(player, it, LocationEnum.DISCARD_OTHER, false, null,
                            isCost = true, isConsume = true)
                    }
                }
            }
            null
        })


        lastResearch.setSpecial(1)
        lastResearch.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ card_number, player, game_status, _ ->
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
                            EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                        if(it.getNap() == 2){
                            game_status.cardToDust(player, 2, it, false,
                                EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                            greatDiscovery(card_number, player, game_status)
                            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
                            game_status.getPlayer(player).afterCardUseTermination = true
                        }
                    }
                }
            }
            null
        })


        lastResearch.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateReconstructListener(player, Listener(player, card_number) {gameStatus, cardNumber, _, _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })


        grandGulliver.setSpecial(null)
        grandGulliver.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_X) { _, player, game_status, _->
            game_status.getPlayerFlare(player)
        })
        grandGulliver.addText(Text(TextEffectTimingTag.USED, TextEffectTag.COST_BUFF){ card_number, player, game_status, _ ->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ ->
                true}, { _, _, _ -> 0 }))
            null
        })
    }

    private val betrayer = CardData(CardClass.NORMAL, CardName.SAINE_BETRAYAL, MegamiEnum.SAINE, CardType.ATTACK, SubType.NONE)
    private val flowingWall = CardData(CardClass.NORMAL, CardName.SAINE_FLOWING_WALL, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private val flowingWallText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) {card_number, player, game_status, _ ->
        game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(2, 999), false,
            null, null, NUMBER_SAINE_FLOWING_WALL)
        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_SAINE_FLOWING_WALL, -1))
        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_TEMP_BUT_NOT_REMOVE_WHEN_UNUSED, {_, _, _ ->
            true
        }, {_, _, attack ->
            attack.lifePlusMinus(1)
        }))
        null
    }
    private val jeolChangJeolWha = CardData(CardClass.SPECIAL, CardName.SAINE_JEOL_CHANG_JEOL_HWA, MegamiEnum.SAINE, CardType.ATTACK, SubType.REACTION)

    private fun saineA2CardInit(){
        betrayer.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 1, 3,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)


        flowingWall.setEnchantment(2)
        flowingWall.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.DAMAGE_AURA_REPLACEABLE_HERE) { _, _, _, _ ->
            null
        })
        flowingWall.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(palSang(player, game_status) && game_status.turnPlayer == player){
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.SAINE_FLOWING_WALL,
                            NUMBER_SAINE_FLOWING_WALL_ADDITIONAL, CardClass.NULL,
                                sortedSetOf(0, 1, 2, 3, 4, 5), 0,  0, MegamiEnum.SAINE,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                            ).addTextAndReturn(flowingWallText)
                    )){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })


        jeolChangJeolWha.setSpecial(1)
        jeolChangJeolWha.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 2, 999,
            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jeolChangJeolWha.addText(terminationText)
        jeolChangJeolWha.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.END_CURRENT_PHASE) { _, _, _, react_attack ->
            react_attack?.afterAttackCompleteEffect?.add(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { _, attack_player, in_game_status, _ ->
                if(in_game_status.getPlayerAura(attack_player.opposite()) == 0){
                    in_game_status.endCurrentPhase = true
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

    private val callWaveText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
        game_status.getPlayer(player.opposite()).normalCardDeck.filter {card ->
            card.card_data.canCover
        }.forEach { card ->
            game_status.popCardFrom(player, card.card_number, LocationEnum.DECK, false)?.let {
                game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, false)
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
        waterBall.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {condition_player, condition_game_status, _ ->
                isTailWind(condition_player, condition_game_status)
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(2); lifePlusMinus(2)
                }
            }))
            null
        })
        waterBall.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        waterCurrent.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        waterCurrent.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                LocationEnum.DISTANCE.real_number
            }
            else{
                null
            }
        })
        waterCurrent.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                LocationEnum.DISTANCE.real_number
            }
            else{
                null
            }
        })


        strongAcid.setAttack(DistanceType.CONTINUOUS, Pair(5, 6), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        strongAcid.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                LocationEnum.DISTANCE.real_number
            }
            else{
                null
            }
        })


        tsunami.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
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


        junBiManTen.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.dustToAura(player, 3, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        junBiManTen.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){ _, player, game_status, _ ->
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
        compass.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addRangeBuff(card_number, RangeBuff(card_number,1, RangeBufTag.DELETE_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack ->
                    attack.tempEditedDistance.add(5)
                })
            )
            null
        })
        compass.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.ADD_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack ->
                    attack.tempEditedDistance.add(5)
                }))
            null
        })
        compass.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            game_status.addThisTurnRangeBuff(player.opposite(), RangeBuff(card_number,1, RangeBufTag.DELETE_IMMEDIATE, {_, _, _ -> true},
                { _, _, attack ->
                    attack.tempEditedDistance.add(5)
                }))
            null
        })
        compass.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        callWave.setEnchantment(1)
        callWave.addText(chasmText)
        callWave.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { _, player, game_status, _ ->
            if(game_status.turnPlayer == player && isTailWind(player, game_status)) 0
            else 1
        })
        callWave.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.HATSUMI_CALL_WAVE,
                        NUMBER_HATSUMI_CALL_WAVE, CardClass.NULL,
                        sortedSetOf(2, 3, 4, 5, 6, 7), 1,  999, MegamiEnum.HATSUMI,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    ).addTextAndReturn(callWaveText)
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        isanaHail.setSpecial(4)
        isanaHail.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        isanaHail.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {conditionPlayer, conditionGameStatus, _ -> isTailWind(conditionPlayer, conditionGameStatus)})
                {_, _, attack ->
                    attack.lifePlusMinus(2)
                })
            null
        }))
        isanaHail.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                game_status.dustToDistance(2, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        isanaHail.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(isHeadWind(player, game_status)){
                game_status.movePlayingCard(player, LocationEnum.SPECIAL_CARD, card_number)
            }
            null
        })


        oyogibiFire.setSpecial(2)
        oyogibiFire.setAttack(DistanceType.CONTINUOUS, Pair(5, 6), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        oyogibiFire.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_THIS_CARD_RETURN){ _, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, NUMBER_HATSUMI_OYOGIBI_FIRE)
            null
        })
        oyogibiFire.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addDistanceListener(player, Listener(player, card_number) {gameStatus, cardNumber, _, _, _, _ ->
                if(gameStatus.startTurnDistance - gameStatus.getAdjustDistance() >= 2){
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
        kirahariLighthouse.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            if(!isHeadWind(player, game_status)){
                game_status.setShrink(player)
            }
            null
        })
        kirahariLighthouse.addText(Text(TextEffectTimingTag.USED, TextEffectTag.CHANGE_SWELL_DISTANCE) { _, _, _, _ ->
            1
        })
        kirahariLighthouse.addText(Text(TextEffectTimingTag.USED, TextEffectTag.HATSUMI_LIGHTHOUSE) { _, _, _, _ ->
            1
        })
        kirahariLighthouse.addText(Text(TextEffectTimingTag.USED, TextEffectTag.AFTER_HATSUMI_LIGHTHOUSE) { card_number, player, game_status, _ ->
            game_status.returnSpecialCard(player, card_number)
            null
        })


        miobikiRoute.setSpecial(2)
        miobikiRoute.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
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
        miobikiRoute.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, player, game_status, _ ->
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
    private val jeonJang = CardData(CardClass.NORMAL, CardName.MIZUKI_JEON_JANG, MegamiEnum.KODAMA, CardType.ENCHANTMENT, SubType.NONE)
    private val hachiryuCheonjugak = CardData(CardClass.SPECIAL, CardName.MIZUKI_HACHIRYU_CHEONJUGAK, MegamiEnum.MIZUKI, CardType.ENCHANTMENT, SubType.REACTION)
    private val hijamaruTriplet = CardData(CardClass.SPECIAL, CardName.MIZUKI_HIJAMARU_TRIPLET, MegamiEnum.MIZUKI, CardType.ATTACK, SubType.NONE)
    private val tartenashiDaesumun = CardData(CardClass.SPECIAL, CardName.MIZUKI_TARTENASHI_DAESUMUN, MegamiEnum.MIZUKI, CardType.BEHAVIOR, SubType.NONE)
    private val mizukiBattleCry = CardData(CardClass.SPECIAL, CardName.MIZUKI_MIZUKI_BATTLE_CRY, MegamiEnum.MIZUKI, CardType.ENCHANTMENT, SubType.FULL_POWER)

    private val tusin = CardData(CardClass.NORMAL, CardName.KODAMA_TU_SIN, MegamiEnum.KODAMA, CardType.ATTACK, SubType.NONE)
    private val spearSoldier1 = CardData(CardClass.SOLDIER, CardName.SOLDIER_SPEAR_1, MegamiEnum.NONE, CardType.ATTACK, SubType.NONE)
    private val spearSoldier2 = CardData(CardClass.SOLDIER, CardName.SOLDIER_SPEAR_2, MegamiEnum.NONE, CardType.ATTACK, SubType.NONE)
    private val shieldSoldier = CardData(CardClass.SOLDIER, CardName.SOLDIER_SHIELD, MegamiEnum.NONE, CardType.BEHAVIOR, SubType.REACTION)
    private val horseSoldier = CardData(CardClass.SOLDIER, CardName.SOLDIER_HORSE, MegamiEnum.NONE, CardType.ENCHANTMENT, SubType.NONE)


    private fun replaceFullPowerToTermination(original: Card){
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

            distanceType = cardData.distanceType
            distanceCont = cardData.distanceCont
            distanceUncont = cardData.distanceUncont
            lifeDamage =  cardData.lifeDamage
            auraDamage = cardData.auraDamage

            charge = cardData.charge

            cost = cardData.cost

            effect = mutableListOf<Text>().apply {
                add(terminationText)
                cardData.effect?.let { duplicateEffect ->
                    for(text in duplicateEffect){
                        add(text)
                    }
                }
            }
            canCover = cardData.canCover
            canDiscard = cardData.canDiscard

            growing = cardData.growing
        }
        original.beforeCardData = original.card_data
        original.card_data = result
    }
    private fun removeTermination(original: Card){
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

            distanceType = cardData.distanceType
            distanceCont = cardData.distanceCont
            distanceUncont = cardData.distanceUncont
            lifeDamage =  cardData.lifeDamage
            auraDamage = cardData.auraDamage

            charge = cardData.charge

            cost = cardData.cost

            effect = mutableListOf<Text>().apply {
                cardData.effect?.let { duplicateEffect ->
                    for(text in duplicateEffect){
                        if(text === terminationText){
                            continue
                        }
                        add(text)
                    }
                }
            }
            canCover = cardData.canCover
            canDiscard = cardData.canDiscard

            growing = cardData.growing
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

    private fun fixed(game_status: GameStatus) = !(game_status.isThisTurnDistanceChange)

    private fun mizukiCardInit(){
        jinDu.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        jinDu.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            draft(player, game_status)
            null
        })


        banGong.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        banGong.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player).lastTurnReact
            }, {_, _, attack ->
                attack.auraPlusMinus(2); attack.lifePlusMinus(1)
            }))
            null
        })
        banGong.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        shootingDown.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, game_status, react_attack ->
            if(fixed(game_status)){
                react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })


        hoLyeong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            draft(player, game_status)
            null
        })
        hoLyeong.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).lastTurnReact){
                game_status.addConcentration(player)
            }
            null
        })


        bangByeog.addText(terminationText)
        bangByeog.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, player, game_status, react_attack->
            if(!game_status.getPlayer(player).thisTurnReact && react_attack?.card_class != CardClass.SPECIAL &&
                        react_attack?.subType != SubType.FULL_POWER){
                react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
            }
            null
        })


        overpoweringGoForward.addText(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION){ _, player, game_status, _->
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
        jeonJang.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            if(fixed(game_status) && !(game_status.gameLogger.checkThisTurnDoAttackNotSpecial(player))){
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
        hachiryuCheonjugak.addText(terminationText)
        hachiryuCheonjugak.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                true
            }, { _, _, attack ->
                attack.makeNotValid()
            }))
            null
        })
        hachiryuCheonjugak.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
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
        hijamaruTriplet.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, player, game_status, _ ->
            if(!(game_status.gameLogger.checkThisTurnDoAttack(player))) 1
            else 0
        })
        hijamaruTriplet.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addTerminationListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                        _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })


        tartenashiDaesumun.setSpecial(3)
        tartenashiDaesumun.addText(terminationText)
        tartenashiDaesumun.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){ _, player, game_status, _ ->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_MIZUKI_TARTENASHI_DAESUMUN, 1
            ) { _, _ -> true }?.let{ selected ->
                game_status.popCardFrom(player, selected[0], LocationEnum.HAND, false)?.let { card ->
                    game_status.insertCardTo(player, card, LocationEnum.READY_SOLDIER_ZONE, false)
                }
            }
            game_status.moveAdditionalCard(player, CardName.KODAMA_TU_SIN, LocationEnum.READY_SOLDIER_ZONE)
            null
        })
        tartenashiDaesumun.addText(Text(TextEffectTimingTag.USED, TextEffectTag.NEXT_ATTACK_ENCHANTMENT){ card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {condition_player, condition_game_status, attack ->
                attack.card_class != CardClass.NULL && condition_game_status.gameLogger.checkThisCardUseInSoldier(condition_player, attack.card_number)
            }, {_, _, attack ->
                attack.apply {
                    auraPlusMinus(1)
                }
            }))
            null
        })
        tartenashiDaesumun.addText(Text(TextEffectTimingTag.USED, TextEffectTag.REMOVE_TERMINATION_REACTION_USE_IN_SOLDIER){ _, _, _, _ ->
            1
        })


        mizukiBattleCry.setSpecial(5)
        mizukiBattleCry.setEnchantment(5)
        mizukiBattleCry.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MIZUKI_BATTLE_CRY){ _, player, game_status, _ ->
            for(card in getPlayerAllNormalCard(player, game_status)){
                if(card.beforeCardData != null){
                    break
                }
                else{
                    if(card.card_data.card_class == CardClass.SOLDIER || card.card_data.card_class == CardClass.NORMAL){
                        if(card.card_data.sub_type == SubType.FULL_POWER){
                            replaceFullPowerToTermination(card)
                        }
                        else{
                            card.card_data.effect?.let {
                                for (text in it){
                                    if(text === terminationText){
                                        removeTermination(card)
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
        mizukiBattleCry.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).enchantmentCard.values.none {card ->
                card.isThisCardHaveTag(TextEffectTag.MIZUKI_BATTLE_CRY)
            }){
                for(card in getPlayerAllNormalCard(player, game_status)){
                    card.beforeCardData?.let {
                        card.card_data = it
                        card.beforeCardData = null
                    }
                }
            }


            null
        })


        tusin.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        tusin.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, buff_game_status, _ ->
                fixed(buff_game_status)
            }, {_, _, madeAttack ->
                madeAttack.run {
                    lifePlusMinus(1)
                }
            }))
            null
        })
        tusin.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
            null
        })


        spearSoldier1.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        spearSoldier1.addText(terminationText)
        spearSoldier1.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player).lastTurnReact
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })


        spearSoldier2.setAttack(DistanceType.CONTINUOUS, Pair(3, 3), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        spearSoldier2.addText(terminationText)
        spearSoldier2.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                buff_game_status.getPlayer(buff_player).lastTurnReact
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })


        shieldSoldier.addText(terminationText)
        shieldSoldier.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, _, react_attack->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, attack ->
                attack.subType != SubType.FULL_POWER },
                {_, _, attack ->
                    attack.auraPlusMinus(-1)
                }))
            null
        })


        horseSoldier.setEnchantment(2)
        horseSoldier.addText(terminationText)
        horseSoldier.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            game_status.addThisTurnOtherBuff(player.opposite(), OtherBuff(card_number, 1, OtherBuffTag.LOSE_IMMEDIATE,
                { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.editedCannotReactSpecial = false
                    attack.editedCannotReactNormal = false
                    attack.editedCannotReact = false
                }))
            null
        })
        horseSoldier.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
    }

    private val helpOrThreat = CardData(CardClass.NORMAL, CardName.YUKIHI_HELP_SLASH_THREAT, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val threadOrRawThread = CardData(CardClass.NORMAL, CardName.YUKIHI_THREAD_SLASH_RAW_THREAD, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val flutteringCollar = CardData(CardClass.SPECIAL, CardName.YUKIHI_FLUTTERING_COLLAR, MegamiEnum.YUKIHI, CardType.ENCHANTMENT, SubType.NONE)
    private val flutteringCollarText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR) { card_number, player, game_status, _->
        if(game_status.addPreAttackZone(
                player, MadeAttack(CardName.YUKIHI_FLUTTERING_COLLAR,
                    NUMBER_YUKIHI_FLUTTERING_COLLAR_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(0, 1, 2, 3, 4, 5), 2,  2, MegamiEnum.YUKIHI,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    )
            ) ){
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
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.YUKIHI_THREAD_SLASH_RAW_THREAD)){
                game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_BELOW, card_number)
            }
            null
        })
        threadOrRawThread.addTextUnfold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.USE_CARD) {card_number, player, game_status, _->
            if(!(game_status.gameLogger.checkThisTurnUseCard(player) { card -> card != card_number })){
                game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_YUKIHI_HELP_SLASH_THREAT, 1){
                    card, _ -> card.card_data.sub_type != SubType.FULL_POWER &&
                        card.card_data.megami != game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.card_data?.megami
                        && card.card_data.card_type != CardType.UNDEFINED
                }?.let { selected ->
                    game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?.let { card ->
                        game_status.useCardFromNotFullAction(player, card, LocationEnum.DISCARD_YOUR, false, null,
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
        flutteringCollar.addTextFold(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_AFTER_CARD_USE) ret@{card_number, player, game_status, _->
            if(game_status.turnPlayer == player){
                val usedCard = game_status.cardForEffect?: return@ret null
                if(usedCard.card_data.megami != MegamiEnum.YUKIHI && game_status.gameLogger.countCardUseCount(player, usedCard.card_number) == 1){
                    if(game_status.gameLogger.checkThisTurnUseCardCondition(player){ cardNumber, megamiNumber ->
                            val megami = MegamiEnum.fromInt(megamiNumber)
                            if(megami.equal(MegamiEnum.YUKIHI)) 2
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

    val attackAsuraText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
        game_status.setShrink(player)
        null
    }

    private fun thallyaA1CardInit(){
        quickChange.setEnchantment(3)
        quickChange.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            game_status.restoreArtificialToken(player, 1)
            null
        })
        quickChange.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
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
        quickChange.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.ACTIVE_TRANSFORM_BELOW_THIS_CARD, null))
        quickChange.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            unSealCard(player, game_status, card_number, LocationEnum.ADDITIONAL_CARD)
            null
        })


        blackboxNeo.setSpecial(1)
        blackboxNeo.addText(terminationText)
        blackboxNeo.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ card_number, player, game_status, _ ->
            game_status.restoreArtificialToken(player, 1)
            if(game_status.getPlayer(player).artificialTokenBurn == 0){
                game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let{
                    game_status.dustToCard(player, 1, it, EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                    if(it.getNap() == 2){
                        game_status.cardToDust(player, 2, it, false,
                            EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                        transform(player, game_status)
                    }
                }
            }
            null
        })
        blackboxNeo.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if((game_status.getPlayer(player).artificialToken ?: 0) <= 3 || game_status.gameLogger.checkThisTurnTransform(player)) 1
            else 0
        })


        omnisBlaster.setSpecial(null)
        omnisBlaster.setAttack(DistanceType.CONTINUOUS, Pair(3, 10), null, 1000, 1000,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        omnisBlaster.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.run {
                    tempEditedLifeDamage = gameStatus.getPlayer(nowPlayer).transformNumber
                    tempEditedAuraDamage = gameStatus.getPlayer(nowPlayer).transformNumber
                }
            }))
            null
        })
        omnisBlaster.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_X) { _, player, game_status, _->
            game_status.getPlayer(player).transformNumber
        })


        formKinnari.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) { _, player, game_status, _ ->
            while(game_status.getPlayer(player.opposite()).normalCardDeck.isNotEmpty()){
                game_status.popCardFrom(player.opposite(), 1, LocationEnum.YOUR_DECK_TOP, false)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                }
            }
            null
        })
        formKinnari.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_OTHER) { card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_FORM_KINNARI)){
                    CommandEnum.SELECT_ONE -> {
                        if(game_status.addPreAttackZone(
                                player, MadeAttack(CardName.FORM_KINNARI,
                                    NUMBER_FORM_KINNARI, CardClass.NULL,
                                    sortedSetOf(2, 4, 6), 2,  2,  MegamiEnum.THALLYA,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                                )
                            ) ){
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
            null
        })


        formAsura.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) { _, player, game_status, _ ->
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


        formDeva.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_TRANSFORM) { _, player, game_status, _ ->
            game_status.restoreArtificialToken(player, 2)
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_FORM_DEVA)
            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_FORM_DEVA)
            null
        })
        formDeva.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DISCARD_NUMBER_CHANGE_OTHER) { _, player, game_status, _ ->
            val otherPlayer = game_status.getPlayer(player.opposite())
            if(otherPlayer.discard.size != 0 && otherPlayer.discard.size % 2 == 0){
                game_status.addConcentration(player)
            }
            null
        })
    }

    private val storm = CardData(CardClass.NORMAL, CardName.RAIRA_STORM, MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val furiousStorm = CardData(CardClass.NORMAL, CardName.RAIRA_FURIOUS_STORM, MegamiEnum.RAIRA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val jinPungJeCheonUi = CardData(CardClass.SPECIAL, CardName.RAIRA_JIN_PUNG_JE_CHEON_UI, MegamiEnum.RAIRA, CardType.BEHAVIOR, SubType.NONE)
    private val furiousStormText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){ card_number, player, game_status, _ ->
        if(game_status.addPreAttackZone(
                player, MadeAttack(CardName.RAIRA_FURIOUS_STORM,
                    NUMBER_RAIRA_FURIOUS_STORM_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(0, 1, 2, 3, 4), 1,  1, MegamiEnum.RAIRA,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                    )
            ) ){
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
                                    PlayerEnum.PLAYER1, EventLog.STORM_FORCE)) {
                                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.STORM_FORCE, -1))
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
                        game_status.addThisTurnAttackBuff(player, Buff(NUMBER_RAIRA_STORM, 1, BufTag.PLUS_MINUS,
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
                        if(game_status.addPreAttackZone(
                                player, MadeAttack(CardName.RAIRA_JIN_PUNG_JE_CHEON_UI,
                                        NUMBER_RAIRA_STORM_FORCE_ATTACK, CardClass.NULL,
                                        sortedSetOf(0, 1, 2, 3, 4), 1,  1, MegamiEnum.RAIRA,
                                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                                    )
                            ) ){
                            game_status.afterMakeAttack(NUMBER_RAIRA_STORM_FORCE_ATTACK, player, null)
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
                        game_status.addThisTurnAttackBuff(player, Buff(NUMBER_RAIRA_JIN_PUNG_JE_CHEON_UI, 1, BufTag.PLUS_MINUS,
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
        storm.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.STORM_FORCE) { _, player, game_status, _ ->
            stormForce(player, game_status)
            null
        })


        furiousStorm.setEnchantment(0)
        furiousStorm.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        furiousStorm.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR){ card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, furiousStormText)
            null
        })
        furiousStorm.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_OTHER){ card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_OTHER, furiousStormText)
            null
        })
        furiousStorm.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                { buff_player, buff_game_status, buff_attack -> buff_game_status.gameLogger.isThisAttackFirst(buff_player, buff_attack.card_number)
                }, { _, _, madeAttack ->
                madeAttack.lifePlusMinus(-1)
            }))
            null
        })


        jinPungJeCheonUi.setSpecial(2)
        jinPungJeCheonUi.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_RAIRA_GAUGE) { _, player, game_status, _->
            game_status.windGaugeIncrease(player)
            game_status.thunderGaugeIncrease(player)
            game_status.getPlayer(player).canNotCharge = true
            null
        })
        jinPungJeCheonUi.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _->
            game_status.setShrink(player.opposite())
            null
        })
        jinPungJeCheonUi.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MAIN_PHASE_YOUR) { _, player, game_status, _->
            if(!game_status.getPlayer(player).fullAction){
                game_status.isThisTurnDoAction = true
                stormForce(player, game_status)
                stormForce(player, game_status)
            }
            null
        })
        jinPungJeCheonUi.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_THIS_CARD_RETURN) { _, player, game_status, _->
            if(game_status.getPlayer(player).usedSpecialCard.values.none { card ->
                    card.card_data.card_name == CardName.RAIRA_JIN_PUNG_JE_CHEON_UI
                }){
                game_status.getPlayer(player).canNotCharge = false
            }
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
    private val branchPossibility = CardData(CardClass.SPECIAL, CardName.MEGUMI_BRANCH_OF_POSSIBILITY, MegamiEnum.MEGUMI, CardType.ATTACK, SubType.REACTION)
    private val fruitEnd = CardData(CardClass.SPECIAL, CardName.MEGUMI_FRUIT_OF_END, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.NONE)
    private val megumiPalm = CardData(CardClass.SPECIAL, CardName.MEGUMI_MEGUMI_PALM, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.NONE)

    private val wildRoseText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR) { _, player, game_status, _ ->
        game_status.requestAndDoBasicOperation(player, NUMBER_MEGUMI_WILD_ROSE)
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
        if(game_status.addPreAttackZone(
                player, MadeAttack(CardName.MEGUMI_FRUIT_OF_END,
                    NUMBER_MEGUMI_FRUIT_OF_END_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(5), 5, 5, MegamiEnum.MEGUMI,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = true
                    )
            ) ){
            game_status.afterMakeAttack(card_number, player, null)
        }
        game_status.getPlayer(player).enchantmentCard[card_number]?.let { card ->
            game_status.cardToDust(player, card.getNap(), card, false, card_number)
            game_status.enchantmentDestruction(player, card)
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
        }
        null
    }

    private fun megumiCardInit(){
        gongSum.setAttack(DistanceType.CONTINUOUS, Pair(4, 8), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        gongSum.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, attack ->
                if((gameStatus.getPlayer(nowPlayer).notReadySeed ?: 0) == 0){
                    attack.apply {
                        auraPlusMinus(1); lifePlusMinus(1)
                    }
                }
            }))
            null
        })


        taCheog.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        taCheog.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, attack ->
                if(gameStatus.getPlayer(nowPlayer).enchantmentCard.values.any {card ->
                        card.getSeedToken() > 0
                    }){
                    attack.apply {
                        auraPlusMinus(1); lifePlusMinus(1)
                    }
                }
            }))
            null
        })


        shellAttack.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shellAttack.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.ADD_GROWING) { _, player, game_status, _ ->
            game_status.getPlayer(player).nextEnchantmentGrowing += 2
            null
        })


        poleThrust.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        poleThrust.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_AURA_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        poleThrust.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.AFTER_LIFE_DAMAGE_PLACE_CHANGE){ _, _, _, _ ->
            LocationEnum.DISTANCE.real_number
        })
        poleThrust.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_BASIC_OPERATION_INVALID) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).enchantmentCard.size > 0){
                game_status.getPlayer(player.opposite()).isNextBasicOperationInvalid = true
            }
            null
        })


        reed.setEnchantment(1)
        reed.growing = 1
        reed.addText(whenDistanceChangeText)
        reed.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number),
                card_number)
            null
        })
        reed.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_SWELL_DISTANCE) { card_number, player, game_status, _ ->
            game_status.getPlayer(player).enchantmentCard[card_number]?.getSeedToken()?: 0
        })
        reed.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_DISTANCE){ card_number, player, game_status, _ ->
            game_status.getPlayer(player).enchantmentCard[card_number]?.getSeedToken()?: 0
        })


        balsam.setEnchantment(1)
        balsam.growing = 2
        balsam.addText(chasmText)
        balsam.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_MAIN_PHASE_YOUR) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.MEGUMI_BALSAM,
                            NUMBER_MEGUMI_BALSAM_ADDITIONAL_1, CardClass.NULL,
                            sortedSetOf(1, 2, 3), 2,  1, MegamiEnum.MEGUMI,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        balsam.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_MAIN_PHASE_OTHER) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.MEGUMI_BALSAM,
                            NUMBER_MEGUMI_BALSAM_ADDITIONAL_2, CardClass.NULL,
                            sortedSetOf(3, 4, 5), 2,  1, MegamiEnum.MEGUMI,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        balsam.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })


        wildRose.setEnchantment(0)
        wildRose.growing = 2
        wildRose.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, NUMBER_MEGUMI_WILD_ROSE)
            null
        })
        wildRose.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, wildRoseText)
            null
        })
        wildRose.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { _, player, game_status, _ ->
            if(game_status.turnPlayer == player.opposite()) 1
            else 0
        })
        wildRose.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_OTHER_PLAYER_CHANGE_DISTANCE_TOKEN) { card_number, player, game_status, _ ->
            if(!(game_status.getPlayer(player.opposite()).isMoveDistanceToken)){
                game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number),
                    card_number)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
            }
            null
        })


        rootCausality.setSpecial(1)
        rootCausality.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        rootCausality.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            game_status.notReadyToReadySeed(player, 1)
            null
        })
        rootCausality.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(game_status.getPlayer(player).readySeed == 0) 1
            else 0
        })


        branchPossibility.setSpecial(3)
        branchPossibility.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 0, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        branchPossibility.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {attackPlayer, gameStatus, attack ->
                attack.auraPlusMinus(gameStatus.getTotalSeedNumber(attackPlayer))
            }))
            null
        })
        branchPossibility.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {attackPlayer, gameStatus, attack ->
                    attack.auraPlusMinus(gameStatus.getTotalSeedNumber(attackPlayer.opposite()) * -1)
                }))
            null
        })


        fruitEnd.setSpecial(4)
        fruitEnd.setEnchantment(2)
        fruitEnd.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { _, _, game_status, _ ->
            if(game_status.nowPhase == GameStatus.MAIN_PHASE) 1
            else 0
        })
        fruitEnd.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.OTHER_CARD_NAP_LOCATION_HERE) { _, _, _, _ ->
            null
        })
        fruitEnd.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).enchantmentCard[card_number]?.getSeedToken() == 5){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, fruitEndText)
            }
            null
        })
        fruitEnd.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_END_PHASE_OTHER) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).enchantmentCard[card_number]?.getSeedToken() == 5){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_OTHER, fruitEndText)
            }
            null
        })


        megumiPalm.setSpecial(3)
        megumiPalm.setEnchantment(0)
        megumiPalm.growing = 5
        megumiPalm.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.ADD_BUFF){ card_number, player, game_status, _ ->
            if(!(game_status.gameLogger.isPlayerMakeOverAuraDamageOver3(player))){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, condition_attack ->
                    condition_attack.getEditedAuraDamage() <= 3
                }, {_, _, madeAttack ->
                    madeAttack.apply {
                        auraPlusMinus(1); lifePlusMinus(1)
                    }
                }))
            }
            null
        })
        megumiPalm.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, condition_attack ->
                condition_attack.getEditedAuraDamage() <= 3
            }, {_, _, madeAttack ->
                madeAttack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        megumiPalm.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_OTHER){ card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, condition_attack ->
                condition_attack.getEditedAuraDamage() <= 3
            }, {_, _, madeAttack ->
                madeAttack.apply {
                    auraPlusMinus(1); lifePlusMinus(1)
                }
            }))
            null
        })
        megumiPalm.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
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
                    null, null, EventLog.ACT_DAMAGE)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.ACT_DAMAGE, -1))
            }
            Act.COLOR_PURPLE -> {
                game_status.requestAndDoBasicOperation(player, NUMBER_IDEA_SAL_JIN)
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
                EventLog.END_IDEA, LocationEnum.IDEA_YOUR)
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.END_IDEA, -1))
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
        saljin.addText(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkSaljin(false)){
                1
            }
            else {
                0
            }
        })
        saljin.addText(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkSaljin(true)){
                1
            }
            else {
                0
            }
        })
        saljin.addText(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        saljin.addText(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })


        sakuraWave.addText(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkSakuraWave()){
                1
            }
            else {
                0
            }
        })
        sakuraWave.addText(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkSakuraWaveFlipped()){
                1
            }
            else {
                0
            }
        })
        sakuraWave.addText(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
        sakuraWave.addText(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })


        whistle.addText(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkWhistle(false)){
                1
            }
            else {
                0
            }
        })
        whistle.addText(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkWhistle(true)){
                1
            }
            else {
                0
            }
        })
        whistle.addText(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        whistle.addText(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })


        myeongjeon.addText(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkMyeongJeon(false)){
                1
            }
            else {
                0
            }
        })
        myeongjeon.addText(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(game_status.gameLogger.checkMyeongJeon(true)){
                1
            }
            else {
                0
            }
        })
        myeongjeon.addText(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        myeongjeon.addText(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })


        emphasizing.addText(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.gameLogger.checkThisTurnUseFullPower() && !(game_status.gameLogger.checkThisTurnIdea(player))){
                1
            }
            else {
                0
            }
        })
        emphasizing.addText(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, player, game_status, _ ->
            if(game_status.gameLogger.checkThisTurnUseFullPower() && !(game_status.gameLogger.checkThisTurnIdea(player))){
                1
            }
            else {
                0
            }
        })
        emphasizing.addText(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
        emphasizing.addText(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })


        positioning.addText(Text(TextEffectTimingTag.IDEA_CONDITION, TextEffectTag.IDEA){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(abs(game_status.startTurnDistance - nowDistance) >= 2 && nowDistance <= 8){
                1
            }
            else {
                0
            }
        })
        positioning.addText(Text(TextEffectTimingTag.IDEA_CONDITION_FLIP, TextEffectTag.IDEA){ _, _, game_status, _ ->
            if(abs(game_status.startTurnDistance - game_status.getAdjustDistance()) >= 5){
                1
            }
            else {
                0
            }
        })
        positioning.addText(Text(TextEffectTimingTag.IDEA_PROCESS, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 2)
            null
        })
        positioning.addText(Text(TextEffectTimingTag.IDEA_PROCESS_FLIP, TextEffectTag.IDEA){ card_number, player, game_status, _ ->
            ideaProcess(card_number, player, game_status, 1)
            null
        })
    }

    private val kanawe = CardData(CardClass.SPECIAL, CardName.KANAWE_KANAWE, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val image = CardData(CardClass.NORMAL, CardName.KANAWE_IMAGE, MegamiEnum.KANAWE, CardType.ATTACK, SubType.NONE) //2000
    private val screenplay = CardData(CardClass.NORMAL, CardName.KANAWE_SCREENPLAY, MegamiEnum.KANAWE, CardType.ATTACK, SubType.NONE)
    private val production = CardData(CardClass.NORMAL, CardName.KANAWE_PRODUCTION, MegamiEnum.KANAWE, CardType.ATTACK, SubType.NONE)
    private val publish = CardData(CardClass.NORMAL, CardName.KANAWE_PUBLISH, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val afterglow = CardData(CardClass.NORMAL, CardName.KANAWE_AFTERGLOW, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val impromptu = CardData(CardClass.NORMAL, CardName.KANAWE_IMPROMPTU, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.REACTION)
    private val seal = CardData(CardClass.NORMAL, CardName.KANAWE_SEAL, MegamiEnum.KANAWE, CardType.ENCHANTMENT, SubType.NONE)
    private val vagueStory = CardData(CardClass.SPECIAL, CardName.KANAWE_VAGUE_STORY, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val infiniteStarlight = CardData(CardClass.SPECIAL, CardName.KANAWE_INFINITE_STARLIGHT, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val bendOverThisNight = CardData(CardClass.SPECIAL, CardName.KANAWE_BEND_OVER_THIS_NIGHT, MegamiEnum.KANAWE, CardType.ATTACK, SubType.REACTION)
    private val distantSky = CardData(CardClass.SPECIAL, CardName.KANAWE_DISTANT_SKY, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)

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
                            EventLog.END_IDEA, LocationEnum.IDEA_YOUR)
                        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, EventLog.END_IDEA, -1))
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
                    when(game_status.receiveCardEffectSelect(player, idea.card_number.toFirstPlayerCardNumber())){
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

                game_status.gameLogger.insert(EventLog(player, LogText.IDEA, idea.card_number, -1))
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, idea.card_number, -1))
                break
            }
        }

        return true
    }

    private fun kanaweCardInit(){
        kanawe.addText(Text(TextEffectTimingTag.USED, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).nowAct?.actColor == Act.COLOR_GOLD){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, condition_attack ->
                    condition_attack.card_class != CardClass.NULL
                }, {_, _, attack ->
                    attack.lifePlusMinus(1)
                }))
            }
            null
        })


        image.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1000, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        image.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                madeAttack.apply {
                    madeAttack.tempEditedAuraDamage = ceil(getActValue(nowPlayer, gameStatus) / 2.0).toInt()
                }
            }))
            null
        })
        image.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
        screenplay.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            readyIdea(player, game_status, LocationEnum.ADDITIONAL_CARD)
            null
        })
        screenplay.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_END_PHASE_YOUR_IN_DISCARD) { card_number, player, game_status, _ ->
            if(game_status.getCardFrom(player, card_number, LocationEnum.DISCARD_YOUR)?.isSoftAttack == false){
                game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.DISCARD_YOUR, screenPlayText)
            }
            null
        })


        production.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        production.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                if(getActColor(nowPlayer, gameStatus) == Act.COLOR_PURPLE){
                    madeAttack.lifePlusMinus(1)
                }
            }))
            null
        })
        production.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            game_status.getPlayer(player).canIdeaProcess = false
            null
        })


        publish.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_PUBLISH)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_KANAWE_PUBLISH)
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
        publish.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            if(game_status.getFullAction(player)){
                readyIdea(player, game_status, LocationEnum.ALL)
                game_status.setShrink(player.opposite())
            }
            null
        })


        afterglow.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            val actColor = getActColor(player, game_status)
            if(actColor == Act.COLOR_PURPLE || actColor == Act.COLOR_GREEN){
                game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_AFTERGLOW, 1
                ) { _, _ -> true }?.let {selected ->
                    game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, true)
                    }
                }
            }
            game_status.addConcentration(player.opposite())
            null
        })


        impromptu.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { _, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player, player, player, listOf(LocationEnum.HAND),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_IMPROMPTU
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
                        game_status.useCardFromNotFullAction(player, card, LocationEnum.HAND, false, react_attack,
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
        seal.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CAN_NOT_USE_CARD)
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
        seal.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT)
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
        seal.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })


        vagueStory.setSpecial(1)
        vagueStory.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)
            if(game_status.canUseConcentration(player) && nowPlayer.concentration >= 1){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_VAGUE_STORY)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.decreaseConcentration(player)
                            readyIdea(player, game_status, LocationEnum.ADDITIONAL_CARD)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.decreaseConcentration(player)
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
        vagueStory.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_OTHER){ card_number, player, game_status, _ ->
            if(!(game_status.getPlayer(player).beforeTurnIdeaProcess)){
                game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_OTHER, vagueStoryText)
            }

            null
        })


        infiniteStarlight.setSpecial(null)
        infiniteStarlight.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_X) { _, player, game_status, _->
            getActValue(player, game_status)
        })
        infiniteStarlight.addText(Text(TextEffectTimingTag.USING, TextEffectTag.EFFECT_ACT) { _, player, game_status, _ ->
            activeAct(player, game_status)
            null
        })
        infiniteStarlight.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_ACT_CHANGE){ card_number, player, game_status, _ ->
            game_status.returnSpecialCard(player, card_number)
            null
        })


        bendOverThisNight.setSpecial(4)
        bendOverThisNight.setAttack(DistanceType.CONTINUOUS, Pair(0, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        bendOverThisNight.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, reactedAttack ->
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
        distantSky.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
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
                            makeCard(player, game_status, LocationEnum.HAND, normal[0].toCardName()),
                            LocationEnum.HAND, true)
                    }
                    break
                }
                else{
                    continue
                }
            }
            null
        })
        distantSky.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)

            game_status.selectCardFrom(player, player, player, listOf(LocationEnum.NOT_SELECTED_SPECIAL),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_KANAWE_DISTANT_SKY, 1){ _, _ -> true }?.let { special ->
                game_status.getPlayer(player).unselectedSpecialCard.remove(special[0].toCardName())
                game_status.insertCardTo(player,
                    makeCard(player, game_status, LocationEnum.SPECIAL_CARD, special[0].toCardName()),
                    LocationEnum.SPECIAL_CARD, true)
            }
            null
        })
    }

    private val passingFear = CardData(CardClass.NORMAL, CardName.TOKOYO_PASSING_FEAR, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.REACTION)
    private val relicEye = CardData(CardClass.SPECIAL, CardName.TOKOYO_RELIC_EYE, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val eightSakuraInVain = CardData(CardClass.SPECIAL, CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)
    private val eightSakuraInVainText = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR) {card_number, player, game_status, _->
        if(game_status.getPlayer(player).aura >= 6){
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN,
                        NUMBER_TOKOYO_EIGHT_SAKURA_IN_VAIN_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8), 999, 1, MegamiEnum.TOKOYO,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
        }
        null
    }

    private fun tokoyoA2CardInit(){
        passingFear.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        passingFear.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.SELECT_DAMAGE_BY_ATTACKER) { _, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).concentration == 0){
                1
            }
            else{
                0
            }
        })
        passingFear.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, _, reactedAttack ->
            if(reactedAttack?.card_class != CardClass.SPECIAL){
                reactedAttack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                    {_, gameStatus, madeAttack ->
                        madeAttack.apply {
                            val (aura, life) = gameStatus.gameLogger.findGetDamageByThisAttack(card_number)
                            madeAttack.apply {
                                auraPlusMinus(aura * -1); lifePlusMinus(life * -1)
                            }
                        }
                    }))
            }
            null
        })


        relicEye.setSpecial(1)
        relicEye.setAttack(DistanceType.CONTINUOUS, Pair(3, 5), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        relicEye.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { card_number, player, game_status, _ ->
            game_status.flareToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        relicEye.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).concentration == 1) 1
            else 0
        })


        eightSakuraInVain.setSpecial(4)
        eightSakuraInVain.addText(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION){ _, player, game_status, _->
            for(i in 1..5){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_TOKOYO_EIGHT_SAKURA_IN_VAIN)
            }
            null
        })
        eightSakuraInVain.addText(Text(TextEffectTimingTag.USED, TextEffectTag.TOKOYO_EIGHT_SAKURA) { _, _, _, _->
            1
        })
        eightSakuraInVain.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR) { card_number, _, game_status, _->
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
        sakuraSword.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        sakuraSword.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_SAKURA_SWORD, CardName.HONOKA_SHADOW_HAND)){
                game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                if(requestDeckBelow(player, game_status)){
                    game_status.moveAdditionalCard(player, CardName.HONOKA_SHADOW_HAND, LocationEnum.YOUR_DECK_BELOW)
                }
                else{
                    game_status.moveAdditionalCard(player, CardName.HONOKA_SHADOW_HAND, LocationEnum.DISCARD_YOUR)
                }
            }
            null
        })


        shadowHand.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 1, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        shadowHand.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE) { _, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player, player, listOf(LocationEnum.HAND),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_HONOKA_SHADOW_HAND, 1)
            { _, _ -> true }?.let { selected ->
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }
            null
        })
        shadowHand.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) { card_number, player, game_status, _ ->
            game_status.flareToDust(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })
        shadowHand.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(checkCanCardChange(player, game_status, card_number, CardName.HONOKA_SHADOW_HAND, CardName.HONOKA_SAKURA_SWORD)){
                game_status.movePlayingCard(player, LocationEnum.ADDITIONAL_CARD, card_number)
                if(requestDeckBelow(player, game_status)){
                    game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_SWORD, LocationEnum.YOUR_DECK_BELOW)
                }
                else{
                    game_status.moveAdditionalCard(player, CardName.HONOKA_SAKURA_SWORD, LocationEnum.DISCARD_YOUR)
                }
            }
            null
        })


        eyeOpenAlone.setSpecial(3)
        eyeOpenAlone.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION,
                player, game_status.getCardOwner(card_number), card_number)
            null
        })
        eyeOpenAlone.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){ card_number, player, game_status, _ ->
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


        followTrace.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN)ret@{ _, player, game_status, _ ->
            val firstCommand = game_status.requestAndDoBasicOperation(player, NUMBER_HONOKA_FOLLOW_TRACE)
            if(firstCommand != CommandEnum.SELECT_NOT){
                game_status.requestAndDoBasicOperation(player, NUMBER_HONOKA_FOLLOW_TRACE, hashSetOf(firstCommand))
            }
            null
        })
        followTrace.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){ card_number, player, game_status, _ ->
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


        facingShadow.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).flare == 0){
                game_status.lifeToDust(player.opposite(),1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            else{
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
            }
            null
        })
        facingShadow.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
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
        sakuraShiningBrightly.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, madeAttack ->
                val damage = gameStatus.getCardFrom(nowPlayer, madeAttack.card_number, LocationEnum.PLAYING_ZONE_YOUR)
                    ?.getNap() ?: 0
                madeAttack.tempEditedAuraDamage = damage
            }))
            null
        })
        sakuraShiningBrightly.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                game_status.dustToCard(player, 1, it, EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
            }
            null
        })
        sakuraShiningBrightly.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ card_number, player, game_status, _ ->
            if(countTokenFive(game_status) >= 1) {
                game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                    game_status.gameLogger.insert(EventLog(player, LogText.MOVE_TOKEN, card_number, it.getNap()?: 0,
                        LocationEnum.YOUR_USED_CARD, LocationEnum.SPECIAL_CARD, false))
                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                }
                1
            }
            else 0
        })


        holdHands.setSpecial(5)
        holdHands.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.auraToAura(player.opposite(), player, 5, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        walkOldLoad.setSpecial(3)
        walkOldLoad.addText(Text(TextEffectTimingTag.USING, TextEffectTag.PHASE_SKIP){ card_number, player, game_status, _ ->
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
        val result = CardData(CardClass.NORMAL, card_name, megamiEnum, card_data.card_type, card_data.sub_type)
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

            distanceType = card_data.distanceType
            distanceCont = card_data.distanceCont
            distanceUncont = card_data.distanceUncont
            lifeDamage =  card_data.lifeDamage
            auraDamage = card_data.auraDamage

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

            growing = card_data.growing
        }
        return result
    }

    private fun haganeA1CardInit(){
        bonfire.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() >= 3){
                game_status.distanceToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
                game_status.distanceToFlare(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        wheelSkill.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){ _, player, game_status, _ ->
            if(abs(game_status.getAdjustDistance() - game_status.startTurnDistance) >= 2){
                game_status.drawCard(player, 1)
                game_status.addConcentration(player)
            }
            null
        })


        grandSoftMaterial.setSpecial(1)
        grandSoftMaterial.addText(terminationText)
        grandSoftMaterial.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD){ card_number, player, game_status, _ ->
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
                                    softenAttack.card_data.addText(softAttackText)
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
        grandSoftMaterial.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(game_status.gameLogger.checkThisTurnUseCard(player) { card -> card.toCardName() == CardName.HAGANE_SOFT_ATTACK }) 1
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
        redBlade.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK) { card_number, player, game_status, _->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_RED_BLADE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
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
        flutteringBlade.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK) { _, player, game_status, now_attack->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_FLUTTERING_BLADE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            now_attack?.tabooGaugeAmount = 1
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
        flutteringBlade.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHECK_THIS_ATTACK_VALUE) { card_number, player, game_status, now_attack ->
            if(now_attack?.tabooGaugeAmount == 1){
                if(game_status.getPlayer(player.opposite()).aura <= 4){
                    game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(1, 999), false,
                        null, null, card_number)
                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                }
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_KAMUWI_FLUTTERING_BLADE)
            }
            null
        })


        siKenLanJin.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        siKenLanJin.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK) { _, player, game_status, now_attack->
            if(game_status.getFullAction(player) && game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_SI_KEN_LAN_JIN)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            now_attack?.tabooGaugeAmount = 1
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
        siKenLanJin.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHECK_THIS_ATTACK_VALUE) { card_number, player, game_status, now_attack ->
            if(now_attack?.tabooGaugeAmount == 1){
                for (i in 1..3){
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.KAMUWI_SI_KEN_LAN_JIN,
                                    NUMBER_KAMUWI_SI_KEN_LAN_JIN_ADDITIONAL + i - 1, CardClass.NULL,
                                    sortedSetOf(2, 3, 4), 1,  1, MegamiEnum.KAMUWI,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                        )){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
            }
            null
        })

        cutDown.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        cutDown.addText(onlyCanUseReactText)
        cutDown.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, player, game_status, react_attack ->
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

        threadingThorn.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
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

        keSyoLanLyu.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
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
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.KAMUWI_KE_SYO_LAN_LYU,
                                NUMBER_KAMUWI_KE_SYO_LAN_LYU_ADDITIONAL_1, CardClass.NULL,
                                sortedSetOf(5, 6, 7, 8, 9), 4,  1, MegamiEnum.KAMUWI,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                            )
                    ) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }

            if(selectTwo){
                if(game_status.getAdjustDistance() >= 5){
                    game_status.distanceToDust(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
                }
            }

            if(selectThree){
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.KAMUWI_KE_SYO_LAN_LYU,
                                NUMBER_KAMUWI_KE_SYO_LAN_LYU_ADDITIONAL_2, CardClass.NULL,
                                sortedSetOf(2, 3, 4), 2,  2, MegamiEnum.KAMUWI,
                                cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                            )
                    ) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }

            if(selectFour){
                game_status.dustToAura(player, 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })

        bloodWave.setEnchantment(2)
        bloodWave.addText(chasmText)
        bloodWave.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_BLOOD_WAVE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            if(game_status.addPreAttackZone(
                                    player, MadeAttack(CardName.KAMUWI_BLOOD_WAVE,
                                        NUMBER_KAMUWI_BLOOD_WAVE_ADDITIONAL, CardClass.NULL,
                                            sortedSetOf(3), 2,  2, MegamiEnum.KAMUWI,
                                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false,
                                            chogek = false, inevitable = true
                                        )
                                ) ){
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

        bloodWave.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_GET_AURA_OTHER) { _, _, _, _ ->
            1
        })
        bloodWave.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_GET_AURA_OTHER_AFTER) { card_number, player, game_status, _ ->
            game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?.let {card ->
                game_status.cardToDust(player, 1, card, false, card_number)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                if(card.isItDestruction()){
                    game_status.enchantmentDestruction(player, card)
                }
            }
            1
        })

        lamp.setSpecial(5)
        lamp.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_LAMP)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.tabooGaugeIncrease(player, 3)
                        game_status.flareToDust(player, game_status.getPlayerFlare(player), Arrow.NULL, player,
                            game_status.getCardOwner(card_number), card_number)
                        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                        game_status.processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                            null, null, NUMBER_KAMUWI_LAMP)
                        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KAMUWI_LAMP, -1))
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
        dawn.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_REACTED) { card_number, player, game_status, this_attack ->
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
        dawn.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
            null
        })

        graveYard.setSpecial(3)
        graveYard.setEnchantment(4)
        graveYard.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_GRAVEYARD)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 2)
                            if(game_status.addPreAttackZone(
                                    player, MadeAttack(CardName.KAMUWI_GRAVEYARD,
                                        NUMBER_KAMUWI_GRAVEYARD_ADDITIONAL, CardClass.NULL,
                                            sortedSetOf(3, 4), 3,  3, MegamiEnum.KAMUWI,
                                            cannotReactNormal = true, cannotReactSpecial = false, cannotReact = false, chogek = false, inevitable = false
                                        )
                                ) ){
                                game_status.afterMakeAttack(card_number, player, null)
                            }
                            game_status.processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                                null, null, NUMBER_KAMUWI_GRAVEYARD)
                            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_KAMUWI_GRAVEYARD, -1))
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
        graveYard.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_WIN) { _, _, _, _ ->
            1
        })
        graveYard.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_NAP_REMOVE) { _, player, game_status, _ ->
            if(game_status.getPlayer(player.opposite()).life == 0){
                game_status.gameEnd(player, player.opposite())
            }
            null
        })

        kataShiro.setSpecial(1)
        kataShiro.setAttack(DistanceType.CONTINUOUS, Pair(0, 6), null, 0, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        kataShiro.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.ADD_COST) { _, player, game_status, react_attack ->
            game_status.getPlayer(player.opposite()).nextCostAddMegami = react_attack?.megami
            null
        })
        kataShiro.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_TABOO_CHANGE){ card_number, player, game_status, _ ->
            if((game_status.getPlayer(player).tabooGauge?: 1) % 6 == 0){
                game_status.returnSpecialCard(player, card_number)
            }
            null
        })

        logic.setSpecial(3)
        logic.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, player, game_status, _ ->
            if(game_status.getPlayerLife(player) <= 6) 1
            else 0
        })
        logic.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.dustToLife(player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        logic.addText(Text(TextEffectTimingTag.USED, TextEffectTag.KAMUWI_LOGIC){ _, _, _, _ ->
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
    private val orireterareru = CardData(CardClass.SPECIAL, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.REACTION)
    private val renriTheEnd = CardData(CardClass.SPECIAL, CardName.RENRI_RENRI_THE_END, MegamiEnum.RENRI, CardType.ENCHANTMENT, SubType.NONE)
    private val engravedGarment = CardData(CardClass.SPECIAL, CardName.RENRI_ENGRAVED_GARMENT, MegamiEnum.RENRI, CardType.UNDEFINED, SubType.NONE)
    private val shamanisticMusic = CardData(CardClass.SPECIAL, CardName.KIRIKO_SHAMANISTIC_MUSIC, MegamiEnum.KIRIKO, CardType.ATTACK, SubType.NONE)

    private val rururarariText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
        game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(1, 999), false,
            null, null, NUMBER_RURURARARI_ADDITIONAL)
        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_RURURARARI_ADDITIONAL, -1))
        game_status.returnSpecialCard(player, card_number)
        null
    }

    val perjureText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.PERJURE, null)

    private fun renriCardInit(){
        falseStab.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        falseStab.addText(perjureText)
        falseStab.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_WHEN_PERJURE_NOT_DISPROVE) ret@{ card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_RENRI_FALSE_STAB)){
                    CommandEnum.SELECT_ONE -> {
                        if(card_number != NUMBER_RENRI_FALSE_STAB){
                            game_status.getPlayer(player).preAttackCard?.canNotSelectAura = true
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
        temporaryExpedient.addText(perjureText)
        temporaryExpedient.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        blackAndWhite.addText(perjureText)
        blackAndWhite.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, game_status, react_attack ->
            if(game_status.gameLogger.checkThisTurnMoveDustToken()){
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


        irritatingGesture.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            game_status.setShrink(player.opposite())

            if(game_status.gameLogger.checkThisTurnFailDisprove(player.opposite())){
                while(true){
                    val list = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_RENRI_IRRITATING_GESTURE
                    ) { card, _ -> card.card_data.isPerjure()}?: break
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


        floatingClouds.addText(perjureText)
        floatingClouds.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_DISPROVE_FAIL) ret@{ _, player, game_status, _ ->
            game_status.chojoDamageProcess(player.opposite())
            null
        })
        floatingClouds.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            var count = 0
            game_status.getPlayer(player).enchantmentCard.values.filter { card ->
                card.card_data.card_class == CardClass.NORMAL
            }.forEach { _ ->
                count += 1
            }

            game_status.getPlayer(player).discard.filter { card ->
                card.card_data.card_class == CardClass.NORMAL
            }.forEach { _ ->
                count += 1
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


        fishing.addText(perjureText)
        fishing.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        pullingFishing.setEnchantment(3)
        pullingFishing.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() >= 2){
                game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })
        pullingFishing.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            if(game_status.getAdjustDistance() >= 2){
                game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        rururarari.setSpecial(4)
        rururarari.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 1, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        rururarari.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            if(game_status.gameLogger.checkThisTurnFailDisprove(player.opposite())){
                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, {_, _, _ ->
                    true }, {_, _, attack ->
                    attack.setBothSideDamage()
                }))
            }
            null
        }))
        rururarari.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, rururarariText)
            null
        })


        ranararomirerira.setSpecial(4)
        ranararomirerira.addText(onlyCanUseReactText)
        ranararomirerira.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{ _, player, game_status, react_attack ->
            while(true){
                val selected = game_status.selectCardFrom(player.opposite(), player, player,
                    listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA)
                {_, _ -> true}

                if(selected == null){
                    break
                }
                else if(selected.size == 1){
                    val selectedCard = game_status.getCardFrom(player.opposite(), selected[0], LocationEnum.HAND)?: continue
                    if (selectedCard.card_data.sub_type == SubType.FULL_POWER || selectedCard.card_data.megami != MegamiEnum.RENRI){
                        continue
                    }

                    if(selectedCard.card_data.card_type == CardType.UNDEFINED){
                        game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, true)
                        }
                    }
                    else{
                        while(true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_RENRI_RA_NA_RA_RO_MI_RE_RI_RA)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.useCardFromNotFullAction(player, selectedCard, LocationEnum.HAND_OTHER,
                                        false, react_attack,
                                        isCost = true, isConsume = true)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                                        game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, true)
                                    }
                                    break
                                }
                                else -> {
                                    continue
                                }
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
        orireterareru.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) ret@{ _, player, game_status, react_attack ->
            game_status.selectCardFrom(player, player, player, listOf(LocationEnum.NOT_SELECTED_NORMAL),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_RENRI_O_RI_RE_TE_RA_RE_RU, 1) {
                    card, _ -> card.card_number.isPerjure() && card.card_data.card_type != CardType.UNDEFINED
            }?.let { selected ->
                game_status.getPlayer(player).unselectedCard.remove(selected[0].toCardName())
                val useCard = makeCard(player, game_status, LocationEnum.OUT_OF_GAME, selected[0].toCardName())
                game_status.insertCardTo(player, useCard, LocationEnum.PLAYING_ZONE_YOUR, true)
                game_status.getPlayer(player).usingCard.remove(useCard)
                game_status.useCardFromNotFullAction(player, useCard, LocationEnum.PLAYING_ZONE_YOUR, false, react_attack,
                    isCost = true, isConsume = true, cardMoveCancel = true
                )
                game_status.getPlayer(player).usingCard.add(useCard)
                game_status.popCardFrom(player, useCard.card_number, LocationEnum.PLAYING_ZONE_YOUR, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
                }
            }
            null
        })
        orireterareru.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
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
        renriTheEnd.setEnchantment(3)
        renriTheEnd.addText(terminationText)
        renriTheEnd.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            game_status.moveAdditionalCard(player, CardName.RENRI_ENGRAVED_GARMENT, LocationEnum.SPECIAL_CARD)
            null
        })
        renriTheEnd.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { reason, player, game_status, _ ->
            if(game_status.turnPlayer == player && reason == EventLog.NORMAL_NAP_PROCESS) 0
            else 1
        })
        renriTheEnd.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_MOVE_TOKEN) ret@{ _, player, game_status, _ ->
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
                    engravedGarment.card_data = CardName.KIRIKO_SHAMANISTIC_MUSIC.toCardData(game_status.version)
                }
                1 -> {
                    engravedGarment.card_data = CardName.UTSURO_MANG_A.toCardData(game_status.version)
                    if(location == LocationEnum.YOUR_USED_CARD){
                        game_status.addMainPhaseListener(player, Listener(player, engravedGarment.card_number) {gameStatus, cardNumber, _,
                                                                                                _, _, _ ->
                            gameStatus.returnSpecialCard(player, cardNumber)
                            true
                        })
                    }
                }
                2 -> {
                    engravedGarment.card_data = CardName.SHINRA_WANJEON_NONPA.toCardData(game_status.version)
                }
                else -> {
                    engravedGarment.card_data = CardName.TOKOYO_KUON.toCardData(game_status.version)
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
    private val eightMirrorVainSakura = CardData(CardClass.SPECIAL, CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA, MegamiEnum.YATSUHA, CardType.ATTACK, SubType.NONE)

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
            ) {card, _ -> card.card_data.megami.equal(MegamiEnum.YATSUHA) && card.card_data.card_name in notCompleteSet}?: break
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
        holyRakeHand.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
           game_status.flareToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
               game_status.getCardOwner(card_number), card_number)
            null
        })


        entranceOfAbyss.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        entranceOfAbyss.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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
        trueMonster.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) { card_number, player, game_status, _ ->
            game_status.lifeToLife(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        ghostLink.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
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
        ghostLink.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.YATSUHA_GHOST_LINK,
                        NUMBER_YATSUHA_GHOST_LINK_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(3, 4, 5), 2,  1, MegamiEnum.YATSUHA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        resolution.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, react_attack->
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


        pledge.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _->
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
        vainFlower.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(countCompleteCard(game_status, player) <= 3){
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
        eightMirrorVainSakura.addText(terminationText)
        eightMirrorVainSakura.setAttack(DistanceType.CONTINUOUS, Pair(0, 8), null, 1, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false)
        eightMirrorVainSakura.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            changeCompleteCard(game_status, player)
            null
        })
        eightMirrorVainSakura.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR) { _, player, game_status, _->
            changeCompleteCard(game_status, player)
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
                                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                                    break
                                }
                            }
                            CommandEnum.SELECT_TWO -> {
                                if(game_status.getPlayerAura(player) >= 2){
                                    game_status.auraToCard(player, 2, it, card_number, LocationEnum.YOUR_USED_CARD)
                                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
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
                                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                                    break
                                }
                            }
                            CommandEnum.SELECT_TWO -> {
                                if(game_status.getPlayerAura(player) >= 1){
                                    game_status.auraToCard(player, 1, it, card_number, LocationEnum.PLAYING_ZONE_YOUR)
                                    game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
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
        blaster.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
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
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.KURURU_BLASTER,
                                    NUMBER_KURURU_BLASTER_ADDITIONAL_1, CardClass.NULL,
                                    sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 2,  2,
                                    MegamiEnum.KURURU, cannotReactNormal = false, cannotReactSpecial = false,
                                    cannotReact = false, chogek = false)
                        )){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
                else{
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.KURURU_BLASTER,
                                    NUMBER_KURURU_BLASTER_ADDITIONAL_1, CardClass.NULL,
                                    sortedSetOf(0, 1, 2, 3, 4, 5, 6), 1,  1, MegamiEnum.KURURU,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                        )){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
            }
            null
        })
        blaster.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
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
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.KURURU_BLASTER,
                                    NUMBER_KURURU_BLASTER_ADDITIONAL_2, CardClass.NULL,
                                    sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12), 2,  2,
                                    MegamiEnum.KURURU, cannotReactNormal = false, cannotReactSpecial = false,
                                    cannotReact = false, chogek = false)
                        )){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
                else{
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.KURURU_BLASTER,
                                    NUMBER_KURURU_BLASTER_ADDITIONAL_2, CardClass.NULL,
                                    sortedSetOf(0, 1, 2, 3, 4, 5, 6), 1,  1, MegamiEnum.KURURU,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                        )){
                        game_status.afterMakeAttack(card_number, player, null)
                    }
                }
            }
            null
        })


        railgun.setAttack(DistanceType.CONTINUOUS, Pair(2, 6), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        railgun.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 2){
                var connectDive = 0
                for(card in game_status.getPlayer(player).usedSpecialCard.values){
                    connectDive += card.effectAllValidEffect(card.card_number * 10 + 8, player, game_status, TextEffectTag.WHEN_RESOLVE_COG_EFFECT)
                }
                if(connectDive > 0){
                    game_status.showPlayersSelectResult(player.opposite(), NUMBER_KURURU_RAILGUN, 0)
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
                    game_status.showPlayersSelectResult(player.opposite(), NUMBER_KURURU_RAILGUN, 1)
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
        connectDive.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status) { card ->
                card.card_data.megami != MegamiEnum.KURURU
            }
            if(kikou.behavior >= 1 && kikou.reaction >= 1 && kikou.enchantment >= 1){
                game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                    while (true) {
                        when (game_status.receiveCardEffectSelect(player, NUMBER_KURURU_CONNECT_DIVE)) {
                            CommandEnum.SELECT_ONE -> {
                                if(game_status.dust >= 1){
                                    game_status.dustToCard(player, 1, it, EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                                    break
                                }
                            }
                            CommandEnum.SELECT_TWO -> {
                                if(game_status.getPlayerAura(player) >= 1){
                                    game_status.auraToCard(player, 1, it, EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
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
        connectDive.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, connectDiveText)
            null
        })
        connectDive.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_RESOLVE_COG_EFFECT) ret@{ card_number, player, game_status, _ ->
            val connectDiveNumber = card_number / 10
            val connectDive = game_status.getCardFrom(player, connectDiveNumber, LocationEnum.YOUR_USED_CARD)?: return@ret null
            if((connectDive.getNap()?: 0) >= 1){
                while(true){
                    val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_KURURU_CONNECT_DIVE * 100000 + card_number - connectDiveNumber * 10)
                    if(nowCommand == CommandEnum.SELECT_ONE){
                        game_status.cardToDust(player, 1, connectDive, false, connectDiveNumber)
                        game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, connectDiveNumber, -1))
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
        torpedo.addText(chasmText)
        torpedo.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.DIVING) { _, player, game_status, _ ->
            game_status.diving(player)
            null
        })
        torpedo.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.HATSUMI_TORPEDO,
                        NUMBER_HATSUMI_TORPEDO_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(1, 2, 3, 4, 5, 6, 7), 999,  1, MegamiEnum.HATSUMI,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false
                        )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        sagiriHail.setSpecial(3)
        sagiriHail.setEnchantment(4)
        sagiriHail.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, react_attack ->
            if(game_status.turnPlayer == player.opposite()) {
                react_attack?.addRangeBuff(
                    card_number, RangeBuff(card_number, 1, RangeBufTag.DELETE_IMMEDIATE, {_, _, condition_attack ->
                        condition_attack.editedDistance.size >= 3
                    }, { _, _, attack ->
                        val min = attack.editedDistance.first()
                        val max = attack.editedDistance.last()
                        for (i in attack.editedDistance) {
                            if (i == min || i == max) {
                                continue
                            }
                            attack.tempEditedDistance.add(i)
                        }
                    }))
            }
            null
        })
        sagiriHail.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
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
        wadanakaRoute.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.DIVING) { _, player, game_status, _ ->
            game_status.diving(player)
            game_status.setShrink(player.opposite())
            null
        })
        wadanakaRoute.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            if(isTailWind(player, game_status)){
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(1, 999), false,
                    null, null, NUMBER_HATSUMI_WADANAKA_ROUTE)
                game_status.processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, 1), false,
                    null, null, NUMBER_HATSUMI_WADANAKA_ROUTE)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_HATSUMI_WADANAKA_ROUTE, -1))
            }
            null
        })
        wadanakaRoute.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            if(isHeadWind(player, game_status)) 1
            else 0
        })
    }

    private val questionAnswer = CardData(CardClass.NORMAL, CardName.YURINA_QUESTION_ANSWER, MegamiEnum.YURINA, CardType.ATTACK, SubType.NONE)
    private val ahum = CardData(CardClass.NORMAL, CardName.YURINA_AHUM, MegamiEnum.YURINA, CardType.ENCHANTMENT, SubType.NONE)
    private val kanzaDo = CardData(CardClass.SPECIAL, CardName.YURINA_KANZA_DO, MegamiEnum.YURINA, CardType.ATTACK, SubType.FULL_POWER)

    suspend fun ahumEffect(player: PlayerEnum, game_status: GameStatus){
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_YURINA_AHUM)){
                CommandEnum.SELECT_ONE -> {
                    game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                        CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_YURINA_AHUM)
                    return
                }
                CommandEnum.SELECT_TWO -> {
                    game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                        CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_YURINA_AHUM)
                    game_status.doBasicOperation(player, CommandEnum.ACTION_INCUBATE,
                        CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_YURINA_AHUM)
                    return
                }
                CommandEnum.SELECT_THREE -> {
                    if(game_status.addPreAttackZone(
                            player, MadeAttack(CardName.YURINA_AHUM,
                                NUMBER_YURINA_AHUM_ADDITIONAL, CardClass.NULL,
                                    sortedSetOf(3, 4, 5), 2,  1,  MegamiEnum.YURINA,
                                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                                )
                        ) ){
                        game_status.afterMakeAttack(NUMBER_YURINA_AHUM, player, null)
                    }
                    return
                }
                CommandEnum.SELECT_NOT -> {
                    return
                }
                else -> {}
            }
        }
    }

    private fun yurinaA2CardInit(){
        questionAnswer.setAttack(DistanceType.CONTINUOUS, Pair(2, 5), null, 3, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        questionAnswer.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) { _, player, game_status, _ ->
            game_status.deckToCoverCard(player.opposite(), 3)
            val basicOperationSelected = game_status.requestAndDoBasicOperation(player, NUMBER_YURINA_QUESTION_ANSWER)
            if(game_status.canDoBasicOperation(player.opposite(), basicOperationSelected)){
                game_status.doBasicOperation(player.opposite(), basicOperationSelected, CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_YURINA_QUESTION_ANSWER)
            }
            null
        })


        ahum.setEnchantment(3)
        ahum.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_AFTER_ATTACK_RESOLVE_OTHER_USE_ATTACK_NUMBER){ attack_number, player, game_status, _ ->
            if(game_status.gameLogger.checkAhumAttack(player, attack_number)){
                ahumEffect(player, game_status)
            }
            null
        })
        ahum.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_AFTER_BASIC_OPERATION_OTHER_MOVE_AURA){ _, player, game_status, _ ->
            if(game_status.gameLogger.checkAhumBasicOperation(player)){
                ahumEffect(player, game_status)
            }
            null
        })


        kanzaDo.setSpecial(null)
        kanzaDo.setAttack(DistanceType.CONTINUOUS, Pair(0, 5), null, 1000, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = true)
        kanzaDo.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_X) { _, player, game_status, _->
            game_status.getPlayerFlare(player)
        })
        kanzaDo.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE, {_, _, _ ->
                true
            }, {nowPlayer, gameStatus, attack ->
                val x = gameStatus.getCardFrom(nowPlayer, attack.card_number, LocationEnum.PLAYING_ZONE_YOUR)?.numberForX
                attack.tempEditedAuraDamage = x?: 0
            }))
            null
        })
        kanzaDo.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DO_BASIC_OPERATION) { card_number, player, game_status, _ ->
            val x = game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.numberForX ?: 0
            for(i in 1..x){
                while(true){
                    val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YURINA_KANZA_DO)
                    if(nowCommand == CommandEnum.SELECT_ONE){
                        game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_YURINA_KANZA_DO)
                        break
                    }
                    else if(nowCommand == CommandEnum.SELECT_TWO){
                        game_status.doBasicOperation(player, CommandEnum.ACTION_INCUBATE,
                            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_YURINA_KANZA_DO)
                        break
                    }
                }
            }

            while(true){
                val selected = game_status.selectCardFrom(player, player, player, listOf(LocationEnum.YOUR_USED_CARD),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_YURINA_KANZA_DO)
                {_, _ -> true}?: break
                if(selected.size <= x){
                    for(selectedCardNumber in selected){
                        game_status.returnSpecialCard(player, selectedCardNumber)
                    }
                    break
                }
            }

            game_status.getPlayer(player).maxHand += x
            null
        })
    }

    private val unfamiliarWorld = CardData(CardClass.NORMAL, CardName.YATSUHA_UNFAMILIAR_WORLD, MegamiEnum.YATSUHA, CardType.ENCHANTMENT, SubType.NONE)
    private val coloredWorld = CardData(CardClass.SPECIAL, CardName.YATSUHA_COLORED_WORLD, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val shesCherryBlossomWorld = CardData(CardClass.SPECIAL, CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val shesEgoAndDetermination = CardData(CardClass.SPECIAL, CardName.YATSUHA_SHES_EGO_AND_DETERMINATION, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)

    private val coloredWorldText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, player, game_status, _ ->
        game_status.getPlayer(player).journey?.let {
            if(it.effectJourney(player, game_status)){
                it.moveJourney(player, game_status)
            }
            else{
                backHome(player, game_status, card_number)
            }
        }
        null
    }

    private suspend fun backHome(player: PlayerEnum, game_status: GameStatus, card_number: Int){
        fun backHomeCardCheck(card: Card, player: PlayerEnum) =
            card.card_data.card_class == CardClass.NORMAL && card.player == player

        suspend fun backAllCardToMemory(game_status: GameStatus, exceptCard: Int){
            val nowPlayer = game_status.getPlayer(player)

            nowPlayer.enchantmentCard.values.filter {card ->
                backHomeCardCheck(card, player)
            }.forEach { card ->
                game_status.cardToDust(player, card.getNap(), card, false, card_number)
                game_status.afterDestruction(player, card.card_number, LocationEnum.MEMORY_YOUR)
            }

            nowPlayer.discard.filter {card ->
                backHomeCardCheck(card, player)
            }.forEach { card ->
                game_status.popCardFrom(player, card.card_number, LocationEnum.DISCARD_YOUR, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.MEMORY_YOUR, true)
                }
            }

            nowPlayer.coverCard.filter { card ->
                backHomeCardCheck(card, player)
            }.forEach { card ->
                game_status.popCardFrom(player, card.card_number, LocationEnum.COVER_CARD, false)?.let {
                    game_status.insertCardTo(player, card, LocationEnum.MEMORY_YOUR, false)
                }
            }

            nowPlayer.coverCard.filter { card ->
                backHomeCardCheck(card, player)
            }.forEach { card ->
                game_status.popCardFrom(player, card.card_number, LocationEnum.COVER_CARD, false)?.let {
                    game_status.insertCardTo(player, card, LocationEnum.MEMORY_YOUR, false)
                }
            }

            nowPlayer.normalCardDeck.filter { card ->
                backHomeCardCheck(card, player)
            }.forEach { card ->
                game_status.popCardFrom(player, card.card_number, LocationEnum.DECK, false)?.let {
                    game_status.insertCardTo(player, card, LocationEnum.MEMORY_YOUR, false)
                }
            }

            nowPlayer.hand.values.filter { card ->
                backHomeCardCheck(card, player) && card.card_number != exceptCard
            }.forEach { card ->
                game_status.popCardFrom(player, card.card_number, LocationEnum.HAND, false)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.MEMORY_YOUR, false)
                }
            }

            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))

            game_status.journeyToDust(player, 1, NUMBER_YATSUHA_COLORED_WORLD)
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_YATSUHA_COLORED_WORLD, -1))

            game_status.moveAdditionalCard(player, CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD, LocationEnum.SPECIAL_CARD)
        }

        game_status.sendCommand(player, player.opposite(), CommandEnum.END_JOURNEY_YOUR)
        while(true){
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_YATSUHA_COLORED_WORLD){card, _ ->
                card.card_data.card_class == CardClass.NORMAL
            }?: break
            if (selected.size == 0){
                backAllCardToMemory(game_status, NUMBER_CARD_UNAME)
                break
            }
            else if(selected.size == 1){
                backAllCardToMemory(game_status, selected[0])
                break
            }
            else{
                continue
            }
        }
    }

    private suspend fun backFromMemory(player: PlayerEnum, game_status: GameStatus){
        game_status.selectCardFrom(player, player, player,
            listOf(LocationEnum.MEMORY_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_YATSUHA_AA1_ALT_DRAW, 1){_, _ ->
            true
        }?.let {selected ->
            game_status.getCardFrom(player, selected[0], LocationEnum.MEMORY_YOUR)?.let ret@{
                if(it.card_data.card_name in notCompleteSet){
                    while (true){
                        when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_AA1_CARD_CHANGE)){
                            CommandEnum.SELECT_ONE -> {
                                game_status.popCardFrom(player, it.card_number, LocationEnum.MEMORY_YOUR, true)
                                game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                                game_status.moveAdditionalCard(player, completeMap[it.card_data.card_name]!!, LocationEnum.HAND)
                                return@ret
                            }
                            CommandEnum.SELECT_NOT ->{
                                break
                            }
                            else -> {
                                continue
                            }
                        }
                    }
                }
                game_status.popCardFrom(player, it.card_number, LocationEnum.MEMORY_YOUR, false)
                game_status.insertCardTo(player, it, LocationEnum.HAND, false)
            }
        }
    }

    private suspend fun startJourney(player: PlayerEnum, game_status: GameStatus, reason: Int){
        val nowPlayer = game_status.getPlayer(player)

        game_status.dustToJourney(player, 1, reason)

        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_COLORED_WORLD)){
                CommandEnum.SELECT_ONE -> {
                    nowPlayer.journey = YatsuhaJourney(1)
                    break
                }
                CommandEnum.SELECT_TWO -> {
                    nowPlayer.journey = YatsuhaJourney(2)
                    break
                }
                CommandEnum.SELECT_THREE -> {
                    nowPlayer.journey = YatsuhaJourney(3)
                    break
                }
                CommandEnum.SELECT_FOUR -> {
                    nowPlayer.journey = YatsuhaJourney(4)
                    break
                }
                else -> {}
            }
        }

        nowPlayer.journey?.startJourney(player, game_status)
    }

    @Suppress("UNREACHABLE_CODE")
    private fun yatsuhaA2CardInit(){
        unfamiliarWorld.setEnchantment(1)
        unfamiliarWorld.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADJUST_NAP) ret@{ card_number, player, game_status, _ ->
            if (game_status.getPlayer(player).isUseCard){
                return@ret null
            }
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_UNFAMILIAR_WORLD)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                            game_status.outToCard(player, 1, it, card_number)
                        }
                        return@ret 0
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {}
                }
            }
            return@ret null
        })
        unfamiliarWorld.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.DO_BASIC_OPERATION) { card_number, player, game_status, _ ->
            for(i in 1..2){
                game_status.doBasicOperation(player, CommandEnum.ACTION_GO_FORWARD,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_YATSUHA_UNFAMILIAR_WORLD)
            }

            game_status.popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?.let {
                game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
            }

            game_status.moveAdditionalCard(player, CardName.YATSUHA_COLORED_WORLD, LocationEnum.SPECIAL_CARD)
            null
        })


        coloredWorld.setSpecial(2)
        coloredWorld.addText(Text(TextEffectTimingTag.USING, TextEffectTag.JOURNEY) { card_number, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)

            if(nowPlayer.journey == null && (nowPlayer.haveSpecificMegami(MegamiEnum.YATSUHA_AA1))){
                startJourney(player, game_status, card_number)
            }
            null
        })
        coloredWorld.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, coloredWorldText)
            null
        })
        coloredWorld.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_OTHER){ card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_OTHER, coloredWorldText)
            null
        })


        shesCherryBlossomWorld.setSpecial(0)
        shesCherryBlossomWorld.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            backFromMemory(player, game_status)
            null
        })
        shesCherryBlossomWorld.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DRAW_CARD) ret@{ _, player, game_status, _->
            if((game_status.getPlayer(player).memory?.size ?: 0) > 0){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_AA1_ALT_DRAW)){
                        CommandEnum.SELECT_ONE -> {
                            backFromMemory(player, game_status)
                            return@ret 1
                        }
                        CommandEnum.SELECT_NOT -> {
                            return@ret 0
                        }
                        else -> {}
                    }
                }
            }
            0
        })
        shesCherryBlossomWorld.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_GET_DAMAGE_BY_DECK_RECONSTRUCT) ret@{ _, player, game_status, _->
            if((game_status.getPlayer(player).memory?.size ?: 0) > 0){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_AA1_ALT_RECONSTRUCT_DAMAGE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.selectCardFrom(player, player, player,
                                listOf(LocationEnum.MEMORY_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                                NUMBER_YATSUHA_AA1_ALT_RECONSTRUCT_DAMAGE, 1){_, _ ->
                                true
                            }?.let { selected ->
                                game_status.popCardFrom(player, selected[0], LocationEnum.MEMORY_YOUR, true)?.let {
                                    game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
                                }
                            }
                            return@ret 1
                        }
                        CommandEnum.SELECT_NOT -> {
                            return@ret 0
                        }
                        else -> {}
                    }
                }
            }
            0
        })


        shesEgoAndDetermination.setSpecial(4)
        shesEgoAndDetermination.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            while (true){
                val list = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.HAND, LocationEnum.DISCARD_YOUR, LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_YATSUHA_SHES_EGO_AND_DETERMINATION
                ){card, _ -> card.card_data.megami.equal(MegamiEnum.YATSUHA) && card.card_data.card_name in notCompleteSet}?: break
                if (list.size == 1){
                    var isGoDeckTop = false

                    while (true){
                        when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_SHES_EGO_AND_DETERMINATION)){
                            CommandEnum.SELECT_ONE -> {
                                isGoDeckTop = true
                                break
                            }
                            CommandEnum.SELECT_NOT -> {
                                break
                            }
                            else -> {}
                        }
                    }

                    var zone = LocationEnum.HAND

                    val card = game_status.popCardFrom(player, list[0], LocationEnum.HAND, true)?.also {
                        game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                    }?: game_status.popCardFrom(player, list[0], LocationEnum.DISCARD_YOUR, true)?.also {
                        game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                        zone = LocationEnum.DISCARD_YOUR
                    }?: game_status.popCardFrom(player, list[0], LocationEnum.COVER_CARD, true)?.also {
                        game_status.insertCardTo(player, it, LocationEnum.ADDITIONAL_CARD, true)
                        zone = LocationEnum.COVER_CARD
                    }?: break

                    if(isGoDeckTop) game_status.moveAdditionalCard(player, completeMap[card.card_data.card_name]!!, LocationEnum.YOUR_DECK_TOP)
                    else game_status.moveAdditionalCard(player, completeMap[card.card_data.card_name]!!, zone)
                    break
                }
                else if(list.size == 0){
                    break
                }
            }
            null
        })
        shesEgoAndDetermination.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_GET_ATTACK) ret@{ card_number, player, game_status, react_attack->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_YATSUHA_ABSOLUTE_DAMAGE_INVALID)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.selectCardFrom(player, player, player,
                            listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                            NUMBER_YATSUHA_ABSOLUTE_DAMAGE_INVALID, 1){card, _ ->
                            card.card_data.megami != MegamiEnum.YATSUHA
                        }?.let { selected ->
                            game_status.popCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
                            }
                            game_status.popCardFrom(player, selected[0], LocationEnum.HAND, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                            }
                            react_attack?.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                                true
                            }, { _, _, attack ->
                                attack.makeNotValid()
                            }))
                            return@ret 1
                        }?: return@ret 0
                    }
                    CommandEnum.SELECT_NOT -> {
                        return@ret 0
                    }
                    else -> {}
                }
            }
            0
        })
    }

    private val akina = CardData(CardClass.SPECIAL, CardName.AKINA_AKINA, MegamiEnum.AKINA, CardType.BEHAVIOR, SubType.NONE)
    private val abacusStone = CardData(CardClass.NORMAL, CardName.AKINA_ABACUS_STONE, MegamiEnum.AKINA, CardType.ATTACK, SubType.NONE)
    private val threat = CardData(CardClass.NORMAL, CardName.AKINA_THREAT, MegamiEnum.AKINA, CardType.ATTACK, SubType.NONE)
    private val trade = CardData(CardClass.NORMAL, CardName.AKINA_TRADE, MegamiEnum.AKINA, CardType.ATTACK, SubType.NONE)
    private val speculation = CardData(CardClass.NORMAL, CardName.AKINA_SPECULATION, MegamiEnum.AKINA, CardType.BEHAVIOR, SubType.NONE)
    private val calc = CardData(CardClass.NORMAL, CardName.AKINA_CALC, MegamiEnum.AKINA, CardType.BEHAVIOR, SubType.REACTION)
    private val turnOffTable = CardData(CardClass.NORMAL, CardName.AKINA_TURN_OFF_TABLE, MegamiEnum.AKINA, CardType.ENCHANTMENT, SubType.NONE)
    private val directFinancing = CardData(CardClass.NORMAL, CardName.AKINA_DIRECT_FINANCING, MegamiEnum.AKINA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val openCuttingMethod = CardData(CardClass.SPECIAL, CardName.AKINA_OPEN_CUTTING_METHOD, MegamiEnum.AKINA, CardType.BEHAVIOR, SubType.NONE)
    private val grandCalcAndManual = CardData(CardClass.SPECIAL, CardName.AKINA_GRAND_CALC_AND_MANUAL, MegamiEnum.AKINA, CardType.ATTACK, SubType.REACTION)
    private val sulyosul = CardData(CardClass.SPECIAL, CardName.AKINA_SU_LYO_SUL, MegamiEnum.AKINA, CardType.ENCHANTMENT, SubType.NONE)
    private val accurateCalc = CardData(CardClass.SPECIAL, CardName.AKINA_AKINA_ACCURATE_CALC, MegamiEnum.AKINA, CardType.BEHAVIOR, SubType.NONE)

    private val calcRangeBuffEffect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit = { _, _, attack ->
        val newAttackRange = sortedSetOf<Int>()
        for(distance in attack.editedDistance){
            newAttackRange.add(distance - 1)
        }
        attack.editedDistance = newAttackRange
    }
    private val openCuttingMethodAttackText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_REACTED) { _, _, _, this_attack ->
        this_attack?.addOtherBuff(OtherBuff(this_attack.card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
            true
        }, { _, _, attack ->
            attack.makeNotValid()
        }))
        0
    }
    private val accurateCalcText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_AKINA_ACCURATE_CALC_START_PHASE)){
                CommandEnum.SELECT_ONE -> {
                    game_status.doBasicOperation(player, CommandEnum.ACTION_INCUBATE,
                        CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_AKINA_AKINA_ACCURATE_CALC)
                    break
                }
                CommandEnum.SELECT_NOT -> {
                    break
                }
                else -> {}
            }
        }
        0
    }
    private val investmentRightText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.INVESTMENT_RIGHT, null)

    private suspend fun investment(game_status: GameStatus, player: PlayerEnum){
        if(game_status.getPlayer(player).isRecoupThisTurn){
            return
        }
        while(true){
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.DISCARD_YOUR, LocationEnum.YOUR_USED_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_AKINA_AKINA
            ) {card, _ -> card.isThisCardHaveTag(TextEffectTag.INVESTMENT_RIGHT)}?: return
            if (selected.size == 0){
                break
            }
            else if(selected.size > 1){
                continue
            }
            else{
                game_status.popCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, true)
                }?: game_status.returnSpecialCard(player, selected[0])
                if(game_status.investmentTokenMove(player)){
                    game_status.setMarketPrice(player, game_status.getPlayer(player).getMarketPrice() + 1)
                }
                break
            }
        }
    }

    private suspend fun recoup(game_status: GameStatus, player: PlayerEnum){
        val nowPlayer = game_status.getPlayer(player)
        if((nowPlayer.flow?: 0) > 0){
            nowPlayer.isRecoupThisTurn = true
            game_status.flowToDust(player, 1)
            if(game_status.recoupTokenMove(player)){
                game_status.setMarketPrice(player, (nowPlayer.marketPrice?: 0) - 2)
            }
            game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_AKINA_AKINA, -1))
        }
    }

    fun akinaCardInit(){
        akina.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MAIN_PHASE_YOUR) ret@{ _, player, game_status, _ ->
            var canRecoup = true
            for(card in game_status.getPlayer(player).usedSpecialCard.values){
                if(card.effectAllValidEffect(player, game_status, TextEffectTag.WHEN_MAIN_PHASE_RECOUP_YOUR) == 1){
                    canRecoup = false
                    break
                }
            }
            if(canRecoup){
                if((game_status.getPlayer(player).flow?: 0) > 0){
                    while(true){
                        val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_AKINA_AKINA)
                        if(nowCommand == CommandEnum.SELECT_ONE){
                            recoup(game_status, player)
                            return@ret null
                        }
                        else if(nowCommand == CommandEnum.SELECT_NOT){
                            break
                        }
                    }
                }
            }
            investment(game_status, player)
            null
        })
        akina.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR) { _, player, game_status, _ ->
            investment(game_status, player)
            null
        })


        abacusStone.setAttack(DistanceType.CONTINUOUS, Pair(1, 6), null, 1, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        abacusStone.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DO_BASIC_OPERATION) { card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_ABACUS_STONE)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.addConcentration(player)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        recoup(game_status, player)
                        break
                    }
                    CommandEnum.SELECT_THREE -> {
                        while(true){
                            when(game_status.receiveCardEffectSelect(player, NUMBER_ARROW_FLOW_TO_FLARE)){
                                CommandEnum.SELECT_ONE -> {
                                    game_status.flowToFlare(player, player, 1, card_number)
                                    break
                                }
                                CommandEnum.SELECT_TWO -> {
                                    game_status.flareToFlow(player, 1)
                                }
                                else -> {}
                            }
                        }
                        break
                    }
                    else -> {}
                }
            }
            null
        })


        threat.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 999, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        threat.addText(investmentRightText)
        threat.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {buff_player, buff_game_status, _ ->
                val buffPlayer = buff_game_status.getPlayer(buff_player)
                val otherPlayer = buff_game_status.getPlayer(buff_player.opposite())
                buffPlayer.getCapital() >= otherPlayer.getCapital()
            }, {_, _, attack ->
                attack.lifePlusMinus(1)
            }))
            null
        })


        trade.setAttack(DistanceType.CONTINUOUS, Pair(1, 5), null, 2, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        trade.addText(terminationText)
        trade.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).getCapital() >= game_status.getPlayer(player.opposite()).getCapital() + 3){
                val megami = game_status.getPlayingCardMegami(player, card_number)?: MegamiEnum.AKINA
                while(true){
                    val selected = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_AKINA_TRADE)
                    {card, _ -> card.card_data.megami != megami && !(card.isSoftAttack)}?: break
                    when(selected.size){
                        0 -> {
                            break
                        }
                        1 -> {
                            game_status.popCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.HAND, true)
                            }
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
        trade.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).getCapital() > game_status.getPlayer(player.opposite()).getCapital()){
                game_status.requestAndDoBasicOperation(player, NUMBER_AKINA_TRADE)
            }
            null
        })


        speculation.addText(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_SPECULATION)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.auraToFlow(player, 2, Arrow.ONE_DIRECTION,
                            player, game_status.getCardOwner(card_number), card_number)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.dustToAura(player, 2, Arrow.ONE_DIRECTION, player,
                            game_status.getCardOwner(card_number), card_number)
                        break
                    }
                    else -> {}
                }
            }
            null
        })


        calc.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        calc.addText(Text(TextEffectTimingTag.USING, TextEffectTag.REACT_ATTACK_STATUS_CHANGE){ card_number, _, game_status, react_attack ->
            react_attack?.addRangeBuff(game_status.useBuffNumberCounter(), RangeBuff(card_number,1, RangeBufTag.CHANGE_AFTER_IMMEDIATE, {_, _, _ -> true},
                calcRangeBuffEffect))
            null
        })
        calc.addText(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, _, game_status, _ ->
            game_status.addThisTurnRangeBuff(PlayerEnum.PLAYER1, RangeBuff(card_number,999,
                RangeBufTag.CHANGE_AFTER, { _, _, _ -> true}, calcRangeBuffEffect))
            game_status.addThisTurnRangeBuff(PlayerEnum.PLAYER2, RangeBuff(card_number,999,
                RangeBufTag.CHANGE_AFTER, { _, _, _ -> true}, calcRangeBuffEffect))
            null
        })


        turnOffTable.setEnchantment(2)
        turnOffTable.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(nowDistance in 0..3) 1
            else 0
        })
        turnOffTable.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.flareToDistance(player.opposite(), 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        turnOffTable.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.distanceToFlare(player.opposite(), 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        directFinancing.setEnchantment(2)
        directFinancing.addText(investmentRightText)
        directFinancing.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.auraToAura(player.opposite(), player, 2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })
        directFinancing.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK)ret@{ card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_DIRECT_FINANCING)){
                    CommandEnum.SELECT_ONE -> {
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        return@ret null
                    }
                    else -> {}
                }
            }

            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.AKINA_DIRECT_FINANCING,
                        NUMBER_AKINA_DIRECT_FINANCING_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(2, 3, 4, 5), 1,  0,  MegamiEnum.AKINA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                        )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        openCuttingMethod.setSpecial(NUMBER_MARKET_PRICE)
        openCuttingMethod.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MAKE_ATTACK){ card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.AKINA_OPEN_CUTTING_METHOD,
                        NUMBER_AKINA_OPEN_CUTTING_METHOD_ADDITIONAL, CardClass.NULL,
                            sortedSetOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10), 999,  1, MegamiEnum.AKINA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false,
                            chogek = false, damageNotChange = true).addTextAndReturn(openCuttingMethodAttackText)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })
        openCuttingMethod.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD){ card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).getCapital() > game_status.getPlayer(player.opposite()).getCapital()){
                game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                    game_status.useCardFrom(player, it, LocationEnum.PLAYING_ZONE_YOUR, false, null,
                        isCost = true, isConsume = true
                    )
                }
            }
            null
        })


        grandCalcAndManual.setSpecial(0)
        grandCalcAndManual.setAttack(DistanceType.CONTINUOUS, Pair(0, 10), null, 2, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        grandCalcAndManual.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.flareToAura(player, player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            game_status.flowToAura(player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            game_status.lifeToAura(player, player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })


        sulyosul.setSpecial(1)
        sulyosul.setEnchantment(1)
        sulyosul.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.lifeToSelfFlare(player, 4, reconstruct = false, damage = false,
                arrow = Arrow.NULL, user = player, card_owner = game_status.getCardOwner(card_number),
                reason = card_number
            )
            null
        })
        sulyosul.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { reason, _, _, _ ->
            if(reason.toCardName() == CardName.AKINA_SU_LYO_SUL) 0
            else 1
        })
        sulyosul.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_LOSE_GAME_ENCHANTMENT) ret@{ card_number, player, game_status, _->
            val nowPlayer = game_status.getPlayer(player)
            if(nowPlayer.life == 0){
                game_status.selfFlareToLife(player, 4, Arrow.NULL, player, game_status.getCardOwner(card_number), card_number)
                game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?.let { card ->
                    game_status.cardToDust(player, card.getNap(), card, false, card_number)
                    if(card.isItDestruction()){
                        game_status.enchantmentDestruction(player, card)
                    }
                    game_status.popCardFrom(player, card.card_number, LocationEnum.YOUR_USED_CARD, true)
                    game_status.insertCardTo(player, card, LocationEnum.OUT_OF_GAME, true)
                }
                return@ret 1
            }
            null
        })


        accurateCalc.setSpecial(NUMBER_MARKET_PRICE)
        accurateCalc.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(nowDistance in 0..7) 1
            else 0
        })
        accurateCalc.addText(investmentRightText)
        accurateCalc.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_AKINA_ACCURATE_CALC)){
                    CommandEnum.SELECT_ONE -> {
                        recoup(game_status, player)
                        for(i in 1..2){
                            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_AKINA_AKINA_ACCURATE_CALC)
                        }
                        game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
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
        accurateCalc.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, accurateCalcText)
            null
        })
    }

    private val sawBladeCutDown = CardData(CardClass.NORMAL, CardName.SHISUI_SAW_BLADE_CUT_DOWN, MegamiEnum.SHISUI, CardType.ATTACK, SubType.NONE)
    private val penetrateSawBlade = CardData(CardClass.NORMAL, CardName.SHISUI_PENETRATE_SAW_BLADE, MegamiEnum.SHISUI, CardType.ATTACK, SubType.NONE)
    private val rebellionAttack = CardData(CardClass.NORMAL, CardName.SHISUI_REBELLION_ATTACK, MegamiEnum.SHISUI, CardType.ATTACK, SubType.REACTION)
    private val ironResistance = CardData(CardClass.NORMAL, CardName.SHISUI_IRON_RESISTANCE, MegamiEnum.SHISUI, CardType.ATTACK, SubType.FULL_POWER)
    private val thornyPath = CardData(CardClass.NORMAL, CardName.SHISUI_THORNY_PATH, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.NONE)
    private val ironPowderWindAround = CardData(CardClass.NORMAL, CardName.SHISUI_IRON_POWDER_WIND_AROUND, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.NONE)
    private val blackArmor = CardData(CardClass.NORMAL, CardName.SHISUI_BLACK_ARMOR, MegamiEnum.SHISUI, CardType.ENCHANTMENT, SubType.REACTION)

    private val padmaCutDown = CardData(CardClass.SPECIAL, CardName.SHISUI_PADMA_CUT_DOWN, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.REACTION)
    private val upalaTear = CardData(CardClass.SPECIAL, CardName.SHISUI_UPALA_TEAR, MegamiEnum.SHISUI, CardType.ATTACK, SubType.NONE)
    private val abudaEat = CardData(CardClass.SPECIAL, CardName.SHISUI_ABUDA_EAT, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.REACTION)
    private val shisuiPlaceOfDeath = CardData(CardClass.SPECIAL, CardName.SHISUI_SHISUI_PLACE_OF_DEATH, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.FULL_POWER)

    private val padmaCutDownEffectText = Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
        game_status.processAllLacerationDamageCancelAble(player.opposite())
        game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
            {_, _, _ -> true},
            {_, _, attack ->
                val count = game_status.gameLogger.countGetDamage(player.opposite())
                attack.lifePlusMinus(ceil(count / 2.0).toInt())
            }))
        if(game_status.addPreAttackZone(
                player.opposite(), MadeAttack(CardName.SHISUI_PADMA_CUT_DOWN,
                    NUMBER_SHISUI_PADMA_CUT_DOWN_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(1, 2, 3, 4), 2, 1,  MegamiEnum.SHISUI,
                        cannotReactNormal = true, cannotReactSpecial = false,
                        cannotReact = false, chogek = false)
            )){
            game_status.afterMakeAttack(card_number, player.opposite(), null)
        }
        null
    }

    private val penetrateSawBladeAttackText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.CAN_NOT_CHOOSE_AURA_DAMAGE) {_, player, game_status, _ ->
        val damagePlayer = game_status.getPlayer(player.opposite())
        if(damagePlayer.getLacerationToken(player)[INDEX_LACERATION_AURA] >= damagePlayer.aura){
            1
        }
        else{
            0
        }
    }

    suspend fun selectLaceration(select_player: PlayerEnum, give_player: PlayerEnum, get_player: PlayerEnum, game_status: GameStatus,
        reason: Int){
        while(true){
            when(game_status.receiveCardEffectSelect(select_player, reason)){
                CommandEnum.SELECT_ONE -> {
                    game_status.addLacerationToken(get_player, give_player, INDEX_LACERATION_AURA, 1)
                    break
                }
                CommandEnum.SELECT_TWO -> {
                    game_status.addLacerationToken(get_player, give_player, INDEX_LACERATION_FLARE, 1)
                    break
                }
                CommandEnum.SELECT_THREE -> {
                    game_status.addLacerationToken(get_player, give_player, INDEX_LACERATION_LIFE, 1)
                    break
                }
                else -> {}
            }
        }
    }



    fun shisuiCardInit(){
        sawBladeCutDown.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        penetrateSawBlade.setAttack(DistanceType.CONTINUOUS, Pair(2, 3), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, isLaceration = true)
        penetrateSawBlade.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.SHISUI_PENETRATE_SAW_BLADE,
                            NUMBER_PENETRATE_ADDITIONAL_ATTACK, CardClass.NULL,
                            sortedSetOf(2, 3), 1,  2,  MegamiEnum.SHISUI,
                            cannotReactNormal = false, cannotReactSpecial = false,
                            cannotReact = false, chogek = false, isLaceration = true).addTextAndReturn(penetrateSawBladeAttackText)
                )){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        rebellionAttack.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, isLaceration = false)
        rebellionAttack.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {condition_player, condition_game_status, _ -> condition_game_status.gameLogger.countGetDamage(condition_player) >= 1},
                {_, _, attack ->
                    attack.auraPlusMinus(1); attack.lifePlusMinus(1)
                }))
            null
        }))
        rebellionAttack.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _ ->
            if(game_status.gameLogger.countGetDamage(player) >= 2){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_SHISUI_REBELLION_ATTACK)
            }
            null
        })


        ironResistance.setAttack(DistanceType.CONTINUOUS, Pair(1, 7), null, 2, 3,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false, isLaceration = true)
        ironResistance.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            selectLaceration(player, player, player, game_status, NUMBER_SHISUI_IRON_RESISTANCE)
            null
        })


        thornyPath.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.distanceToDust(2, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            if(game_status.getAdjustDistance() == 0){
                game_status.addLacerationToken(player, player, INDEX_LACERATION_LIFE, 1)
            }
            else{
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_SHISUI_THORNY_PATH)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.addLacerationToken(player, player, INDEX_LACERATION_AURA, 1)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.addLacerationToken(player, player, INDEX_LACERATION_FLARE, 1)
                            break
                        }
                        else -> {}
                    }
                }
            }
            null
        })


        ironPowderWindAround.addText(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _ ->
            for(i in 1..2){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_SHISUI_IRON_POWDER_WIND_AROUND)
            }
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_SHISUI_IRON_POWDER_WIND_AROUND)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.addLacerationToken(player, player, INDEX_LACERATION_AURA, 1)
                        game_status.addLacerationToken(player.opposite(), player, INDEX_LACERATION_AURA, 1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.addLacerationToken(player, player, INDEX_LACERATION_FLARE, 1)
                        game_status.addLacerationToken(player.opposite(), player, INDEX_LACERATION_FLARE, 1)
                        break
                    }
                    else -> {}
                }
            }
            null
        })


        blackArmor.setEnchantment(0)
        blackArmor.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                val count = game_status.gameLogger.countGetDamage(player) * 2
                game_status.dustToCard(player, count, it, card_number)
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, card_number, -1))
                if(count >= 4) game_status.dustToCard(player, 1, it, card_number)
            }
            null
        })
        blackArmor.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, { _, _, _ ->
                true
            }, { _, _, attack ->
                attack.lifePlusMinus(-1)
            }))
            null
        })
        blackArmor.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.lifePlusMinus(-1)
                }))
            null
        })
        blackArmor.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE) { card_number, player, game_status, _->
            game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let { card ->
                game_status.cardToDust(player, 2, card, false, card_number)
                if(card.isItDestruction()){
                    game_status.enchantmentDestruction(player, card)
                }
            }
            null
        })


        padmaCutDown.setSpecial(3)
        padmaCutDown.addText(Text(TextEffectTimingTag.USING, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE) { card_number, player, game_status, react_attack ->
            if(react_attack == null){
                padmaCutDownEffectText.effect!!(card_number, player, game_status, null)
            }
            else{
                react_attack.afterAttackCompleteEffect.add(padmaCutDownEffectText)
            }
            null
        })


        upalaTear.setSpecial(-2)
        upalaTear.setAttack(DistanceType.CONTINUOUS, Pair(1, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, isLaceration = true)
        upalaTear.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) { card_number, player, game_status, _ ->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET,
                { condition_player, condition_game_status, condition_attack ->
                    val damage = condition_attack.getDamage(condition_game_status, condition_player, condition_game_status.getPlayerAttackBuff(condition_player))
                    damage.first <= 2
                },
                { _, _, attack ->
                    attack.editedLaceration = true
                })
            )
            null
        })
        upalaTear.addText(Text(TextEffectTimingTag.USED, TextEffectTag.RETURN){ _, player, game_status, _ ->
            val nowPlayer = game_status.getPlayer(player)
            if(nowPlayer.aura + nowPlayer.flare <= 6) 1
            else 0
        })


        abudaEat.setSpecial(2)
        abudaEat.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, react_attack ->
            if(react_attack != null){
                react_attack.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))
                val damage = react_attack.getDamage(game_status, player, game_status.getPlayerAttackBuff(player))
                val chosen = game_status.damageSelect(player, CommandEnum.CHOOSE_CARD_DAMAGE, damage, laceration = true)
                if(chosen == CommandEnum.CHOOSE_LIFE){
                    game_status.addLacerationToken(player, player, INDEX_LACERATION_LIFE, damage.second)
                }
                else{
                    game_status.addLacerationToken(player, player, INDEX_LACERATION_AURA, damage.first)
                }
            }
            null
        })
        abudaEat.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addDamageListener(player, Listener(player, card_number) {gameStatus, cardNumber, _,
                                                                                        _, _, _ ->
                if(gameStatus.gameLogger.countGetDamage(player) == 3) {
                    gameStatus.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })


        shisuiPlaceOfDeath.setSpecial(-2)
        shisuiPlaceOfDeath.setEnchantment(2)
        shisuiPlaceOfDeath.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            val number = game_status.getPlayerFlare(player.opposite()) - game_status.getPlayerFlare(player)
            if(number > 0){
                game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                    game_status.dustToCard(player, number, it, card_number)
                }
            }
            null
        })
        shisuiPlaceOfDeath.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_LOSE) { _, _, _, _ ->
            1
        })
        shisuiPlaceOfDeath.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_USE_CONCENTRATION_OTHER) { _, player, game_status, _ ->
            if(game_status.getPlayerLife(player) == 0) 1
            else 0
        })
        shisuiPlaceOfDeath.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_GET_OUT_ENCHANTMENT) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).isLose()){
                game_status.gameEnd(player, player.opposite())
            }
            null
        })

    }

    private val ringABellV8_1 = CardData(CardClass.NORMAL, CardName.HAGANE_RING_A_BELL, MegamiEnum.HAGANE, CardType.BEHAVIOR, SubType.NONE)
    private val eightMirrorVainSakuraV8_1 = CardData(CardClass.SPECIAL, CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA, MegamiEnum.YATSUHA, CardType.BEHAVIOR, SubType.NONE)
    private val callWaveV8_1 = CardData(CardClass.NORMAL, CardName.HATSUMI_CALL_WAVE, MegamiEnum.HATSUMI, CardType.ENCHANTMENT,SubType.NONE)
    private val branchPossibilityV8_1 = CardData(CardClass.SPECIAL, CardName.MEGUMI_BRANCH_OF_POSSIBILITY, MegamiEnum.MEGUMI, CardType.ENCHANTMENT, SubType.REACTION)
    private val flutteringSnowflakeV8_1 = CardData(CardClass.SPECIAL, CardName.YUKIHI_FLUTTERING_SNOWFLAKE, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)

    private val branchPossibilityV8Text = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_YOUR){card_number, player, game_status, _ ->
        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
            true
        }, {attackPlayer, gameStatus, attack ->
            attack.auraPlusMinus(gameStatus.getTotalSeedNumber(attackPlayer))
        }))
        if(game_status.addPreAttackZone(
                player, MadeAttack(CardName.MEGUMI_BRANCH_OF_POSSIBILITY,
                    NUMBER_MEGUMI_BRANCH_OF_POSSIBILITY_ADDITIONAL, CardClass.NULL,
                    sortedSetOf(1, 2, 3, 4, 5), 0,  1, MegamiEnum.MEGUMI,
                    cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
            )){
            game_status.afterMakeAttack(card_number, player, null)
        }
        null
    }

    private val callWaveV8Text = Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR){_, player, game_status, _ ->
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

    private fun v8hypen1CardInit(){
        ringABellV8_1.addText(centrifugalText)
        ringABellV8_1.addText(centrifugalLogText)
        ringABellV8_1.addText(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_HAGANE_RING_A_BELL)
                if(nowCommand == CommandEnum.SELECT_ONE){
                    game_status.showPlayersSelectResult(player.opposite(), NUMBER_HAGANE_RING_A_BELL, 0)
                    game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS, {_, _, _ ->
                        true}, {_, _, attack ->
                        attack.run {
                            auraPlusMinus(2); lifePlusMinus(1)
                        }
                    }))
                    break
                }
                else if(nowCommand == CommandEnum.SELECT_TWO){
                    game_status.showPlayersSelectResult(player.opposite(), NUMBER_HAGANE_RING_A_BELL, 1)
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

        branchPossibilityV8_1.setSpecial(3)
        branchPossibilityV8_1.setEnchantment(2)
        branchPossibilityV8_1.growing = 1
        branchPossibilityV8_1.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, _, _, react_attack ->
            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                {attackPlayer, gameStatus, attack ->
                    attack.auraPlusMinus(gameStatus.getTotalSeedNumber(attackPlayer.opposite()) * -1)
                }))
            null
        })
        branchPossibilityV8_1.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_OTHER){ card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_OTHER, branchPossibilityV8Text)
            null
        })

        eightMirrorVainSakuraV8_1.setSpecial(1)
        eightMirrorVainSakuraV8_1.addText(terminationText)
        eightMirrorVainSakuraV8_1.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(nowDistance in 0..7) 1
            else 0
        })
        eightMirrorVainSakuraV8_1.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            if(changeCompleteCard(game_status, player)){
                game_status.setShrink(player)
            }
            null
        })
        eightMirrorVainSakuraV8_1.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR) { _, player, game_status, _->
            if(changeCompleteCard(game_status, player)){
                game_status.setShrink(player)
            }
            null
        })

        callWaveV8_1.setEnchantment(1)
        callWaveV8_1.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CAN_NOT_MOVE_TOKEN) { _, player, game_status, _ ->
            if(game_status.turnPlayer == player && isTailWind(player, game_status)) 0
            else 1
        })
        callWaveV8_1.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_START_PHASE_YOUR){ card_number, _, game_status, _ ->
            game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.ENCHANTMENT_YOUR, callWaveV8Text)
            null
        })
        callWaveV8_1.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD){ _, player, game_status, _ ->
            while (true){
                val selected = game_status.selectCardFrom(player, player, player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_HATSUMI_CALL_WAVE
                ) { _, _ -> true }?: break
                if (selected.size == 1){
                    game_status.popCardFrom(player, selected[0], LocationEnum.COVER_CARD, false)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_TOP, false)
                    }
                    break
                }
                else if(selected.size == 0){
                    break
                }
            }
            null
        })
        callWaveV8_1.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.DO_BASIC_OPERATION){ _, player, game_status, _ ->
            game_status.requestAndDoBasicOperation(player, NUMBER_HATSUMI_CALL_WAVE)
            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.HATSUMI_CALL_WAVE,
                        NUMBER_HATSUMI_CALL_WAVE_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(2, 3, 4, 5, 6, 7), 1,  999,  MegamiEnum.HATSUMI,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    )
                ) ){
                game_status.afterMakeAttack(NUMBER_HATSUMI_CALL_WAVE, player, null)
            }
            null
        })

        flutteringSnowflakeV8_1.umbrellaMark = true
        flutteringSnowflakeV8_1.setSpecial(2)
        flutteringSnowflakeV8_1.setAttackFold(DistanceType.CONTINUOUS, Pair(3, 6), null, 3, 1)
        flutteringSnowflakeV8_1.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 0, 0)
        flutteringSnowflakeV8_1.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        flutteringSnowflakeV8_1.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateUmbrellaListener(player, Listener(player, card_number){gameStatus, cardNumber, _, _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })
    }

    private val betrayerV8_2 = CardData(CardClass.NORMAL, CardName.SAINE_BETRAYAL, MegamiEnum.SAINE, CardType.ATTACK, SubType.NONE)
    private val ulooV8_2 = CardData(CardClass.SPECIAL, CardName.OBORO_ULOO, MegamiEnum.OBORO, CardType.ENCHANTMENT, SubType.NONE)
    private val hiddenFireV8_2 = CardData(CardClass.NORMAL, CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val sadoV8_2 = CardData(CardClass.NORMAL, CardName.SHINRA_SA_DO, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val hiddenWeapon = CardData(CardClass.NORMAL, CardName.CHIKAGE_HIDDEN_WEAPON, MegamiEnum.CHIKAGE, CardType.ATTACK, SubType.REACTION)
    private val lastResearchV8_2 = CardData(CardClass.SPECIAL, CardName.KURURU_LAST_RESEARCH, MegamiEnum.KURURU, CardType.BEHAVIOR, SubType.NONE)
    private val steamCanonV8_2 = CardData(CardClass.NORMAL, CardName.THALLYA_STEAM_CANNON, MegamiEnum.THALLYA, CardType.ATTACK, SubType.NONE)
    private val stormSurgeAttackV8_2 = CardData(CardClass.NORMAL, CardName.RAIRA_STORM_SURGE_ATTACK, MegamiEnum.RAIRA, CardType.ATTACK, SubType.NONE)
    private val deviceKururusikV8_2 = CardData(CardClass.SPECIAL, CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK, MegamiEnum.UTSURO, CardType.ATTACK, SubType.NONE)
    private val vagueStoryV8_2 = CardData(CardClass.SPECIAL, CardName.KANAWE_VAGUE_STORY, MegamiEnum.KANAWE, CardType.BEHAVIOR, SubType.NONE)
    private val flutteringBladeV8_2 = CardData(CardClass.NORMAL, CardName.KAMUWI_FLUTTERING_BLADE, MegamiEnum.KAMUWI, CardType.ATTACK, SubType.NONE)
    private val logicV8_2 = CardData(CardClass.SPECIAL, CardName.KAMUWI_LOGIC, MegamiEnum.KAMUWI, CardType.BEHAVIOR, SubType.NONE)
    private val fishingV8_2 = CardData(CardClass.NORMAL, CardName.RENRI_FISHING, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.REACTION)
    private val directFinancingV8_2 = CardData(CardClass.NORMAL, CardName.AKINA_DIRECT_FINANCING, MegamiEnum.AKINA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val accurateCalcV8_2 = CardData(CardClass.SPECIAL, CardName.AKINA_AKINA_ACCURATE_CALC, MegamiEnum.AKINA, CardType.BEHAVIOR, SubType.NONE)
    private val blackArmorV8_2 = CardData(CardClass.NORMAL, CardName.SHISUI_BLACK_ARMOR, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.REACTION)
    private val padmaCutDownV8_2 = CardData(CardClass.SPECIAL, CardName.SHISUI_PADMA_CUT_DOWN, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.REACTION)

    private val sadoV8_2ShinsanText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
        var coverOrDiscardNumber = -1
        var handCardNumber = -1
        while (true){
            val selected = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SADO_V8_SHINSAN_COVER, 1
            ) {_, _ -> true}?: break
            if (selected.size == 1){
                coverOrDiscardNumber = selected[0]
                break
            }
        }
        while (true){
            val selected = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SADO_V8_SHINSAN_HAND, 1
            ) {_, _ -> true}?: break
            if (selected.size == 1){
                handCardNumber = selected[0]
                break
            }
        }
        if(handCardNumber == -1){
            if(coverOrDiscardNumber != -1){
                game_status.popCardFrom(player.opposite(), coverOrDiscardNumber, LocationEnum.COVER_CARD, false)?.let{
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                }?: game_status.popCardFrom(player.opposite(), coverOrDiscardNumber, LocationEnum.DISCARD_YOUR, false)?.let{
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                }
            }
        }
        else if(coverOrDiscardNumber == -1){
            game_status.popCardFrom(player.opposite(), handCardNumber, LocationEnum.HAND, false)?.let{
                game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
            }
        }
        else{
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_SADO_V8_SELECT_ORDER)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.popCardFrom(player.opposite(), coverOrDiscardNumber, LocationEnum.COVER_CARD, false)?.let{
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }?: game_status.popCardFrom(player.opposite(), coverOrDiscardNumber, LocationEnum.DISCARD_YOUR, false)?.let{
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }

                        game_status.popCardFrom(player.opposite(), handCardNumber, LocationEnum.HAND, false)?.let{
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.popCardFrom(player.opposite(), handCardNumber, LocationEnum.HAND, false)?.let{
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }

                        game_status.popCardFrom(player.opposite(), coverOrDiscardNumber, LocationEnum.COVER_CARD, false)?.let{
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }?: game_status.popCardFrom(player.opposite(), coverOrDiscardNumber, LocationEnum.DISCARD_YOUR, false)?.let{
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
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
    private val sadoV8_2GuemoText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) ret@{ _, player, game_status, _ ->
        game_status.popCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP, true)?.let {
            game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
        }?: return@ret null

        game_status.popCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP, false)?.let {
            game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
        }
        null
    }
    private val lastResearchReuseText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_KURURU_LAST_RESEARCH_REUSE)){
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

    private val vagueStoryV8Hypen2Text = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_START_PHASE_OTHER) { card_number, player, game_status, _ ->
        if(game_status.returnSpecialCard(player, card_number)){
            game_status.addConcentration(player.opposite())
        }
        null
    }

    private val padmaCutDownV8Hypen2EffectText = Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
        game_status.processAllLacerationDamage(player.opposite())
        game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
            {_, _, _ -> true},
            {_, _, attack ->
                val count = game_status.gameLogger.countGetDamage(player.opposite())
                val plus = count / 2
                attack.lifePlusMinus(plus)
            }))
        if(game_status.addPreAttackZone(
                player.opposite(), MadeAttack(CardName.SHISUI_PADMA_CUT_DOWN,
                    NUMBER_SHISUI_PADMA_CUT_DOWN_ADDITIONAL, CardClass.NULL,
                    sortedSetOf(1, 2, 3, 4), 2, 1,  MegamiEnum.SHISUI,
                    cannotReactNormal = true, cannotReactSpecial = false,
                    cannotReact = false, chogek = false)
            )){
            game_status.afterMakeAttack(card_number, player.opposite(), null)
        }
        null
    }

    private suspend fun ulooV8Hypen2Effect(player: PlayerEnum, game_status: GameStatus){
        while(true){
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_ULOO_USE_CARD_EFFECT
            ) { card, _ -> !(card.isItAttack()) && card.isItInstallation() }?: break
            if(selected.size == 1){
                val selectNumber = selected[0]
                val card = game_status.getCardFrom(player, selectNumber, LocationEnum.COVER_CARD)?: break
                game_status.useCardFromNotFullAction(player, card, LocationEnum.COVER_CARD, false, null,
                    isCost = true, isConsume = true)
                break
            }
            else if(selected.size == 0){
                break
            }
            else{
                continue
            }
        }
    }

    private suspend fun sadoV8Hypen2MakeAttackEffect(player: PlayerEnum, game_status: GameStatus, card_number: Int){
        when(game_status.getStratagem(player)){
            Stratagem.SHIN_SAN -> {
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.SHINRA_SA_DO,
                            NUMBER_SHINRA_SA_DO_ADDITIONAL_1, CardClass.NULL,
                            sortedSetOf(0, 2, 4), 2,  1,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false,
                            inevitable = true
                        ).addTextAndReturn(sadoV8_2ShinsanText)
                    ) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }

                if(game_status.endCurrentPhase){
                    game_status.getPlayer(player).stratagem = null
                    return
                }
                if(!game_status.getPlayer(player).justRunStratagem){
                    setStratagemByUser(game_status, player)
                }
            }
            Stratagem.GUE_MO -> {
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.SHINRA_SA_DO,
                            NUMBER_SHINRA_SA_DO_ADDITIONAL_2, CardClass.NULL,
                            sortedSetOf(1, 3, 5), 2, 2,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false,
                            inevitable = true
                        ).addTextAndReturn(sadoV8_2GuemoText)
                    ) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }

                if(game_status.endCurrentPhase){
                    game_status.getPlayer(player).stratagem = null
                    return
                }
                if(!game_status.getPlayer(player).justRunStratagem){
                    setStratagemByUser(game_status, player)
                }
            }
            null -> {}
        }
    }

    fun flutteringBladeV8Hypen2AddAttackBuffEffect(card_number: Int, player: PlayerEnum, game_status: GameStatus){
        game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
            true
        }, {_, _, attack ->
            attack.apply {
                auraPlusMinus(1); lifePlusMinus(1)
            }
        }))
    }

    fun v8hypen2CardInit(){
        betrayerV8_2.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = true, cannotReact = false, chogek = false)
        betrayerV8_2.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            if(game_status.getPlayerAura(player.opposite()) <= 1){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.CHANGE_EACH_IMMEDIATE, {_, _, _ -> true}, { _, _, attack ->
                    attack.run {
                        tempEditedLifeDamage = getEditedAuraDamage(); tempEditedAuraDamage = getEditedLifeDamage()
                    }
                }))
            }
            null
        }))


        ulooV8_2.setSpecial(2)
        ulooV8_2.setEnchantment(3)
        ulooV8_2.addText((Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_OBORO_ULOO
            ) { _, _ -> true }?.let {selected ->
                for (cardNumber in selected){
                    game_status.popCardFrom(player, cardNumber, LocationEnum.DISCARD_YOUR, true)?.let { card ->
                        game_status.insertCardTo(player, card, LocationEnum.COVER_CARD, true)
                    }
                }
            }
            null
        }))
        ulooV8_2.addText((Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_ARROW_BOTH) { card_number, player, game_status, _->
            if(game_status.gameLogger.checkThisCardUseInCover(player, card_number)){
                1
            }
            else{
                0
            }
        }))
        ulooV8_2.addText((Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR_AFTER_INSTALLATION) { _, player, game_status, _->
            ulooV8Hypen2Effect(player, game_status)
            null
        }))
        ulooV8_2.addText((Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR_AFTER_INSTALLATION) { _, player, game_status, _->
            ulooV8Hypen2Effect(player, game_status)
            null
        }))


        hiddenFireV8_2.umbrellaMark = true
        hiddenFireV8_2.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 6), null, 1, 1)
        hiddenFireV8_2.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 1), null, 1, 1)
        hiddenFireV8_2.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_UMBRELLA) {_, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS)
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
        hiddenFireV8_2.addTextUnfold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_UMBRELLA) {_, player, game_status, _ ->
            while(true){
                val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS)
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


        sadoV8_2.setEnchantment(2)
        sadoV8_2.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).concentration >= 1 && game_status.canUseConcentration(player)){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_SHINRA_SA_DO)){
                        CommandEnum.SELECT_ONE -> {
                            if(game_status.decreaseConcentration(player)){
                                setStratagemByUser(game_status, player)
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
            null
        })
        sadoV8_2.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.RUN_STRATAGEM) { card_number, player, game_status, _->
            sadoV8Hypen2MakeAttackEffect(player, game_status, card_number)
            null
        })
        sadoV8_2.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _->
            sadoV8Hypen2MakeAttackEffect(player, game_status, card_number)
            null
        })


        hiddenWeapon.setAttack(DistanceType.CONTINUOUS, Pair(1, 5), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        hiddenWeapon.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            if(game_status.getFullAction(player)){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true
                }, {_, _, attack ->
                    attack.auraPlusMinus(1); attack.lifePlusMinus(2)
                }))
            }
            null
        }))
        hiddenWeapon.addText((Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            if(game_status.getFullAction(player)){
                val cardList = makeAllPoisonList(player, game_status)
                if(cardList.size != 0){
                    game_status.selectCardFrom(player.opposite(), cardList, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_CHIKAGE_HIDDEN_WEAPON, 1)[0].let { poison ->
                        game_status.popCardFrom(player, poison, LocationEnum.POISON_BAG, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.HAND, publicForOther = true, publicForYour = false)
                        }
                    }
                }
            }
            null
        }))
        hiddenWeapon.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _->
            if(game_status.getPlayer(player.opposite()).hand.values.any { card ->
                    card.card_number.isPoison()
                }){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_CHIKAGE_HIDDEN_WEAPON)
            }
            null
        })


        lastResearchV8_2.setSpecial(2)
        lastResearchV8_2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ card_number, player, game_status, _ ->
            val kikou = getKikou(player, game_status)
            if(kikou.attack >= 1){
                val selectedByOther = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                    listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                    NUMBER_KURURU_LAST_RESEARCH, 1
                ) { _, _ -> true }?: run{
                    game_status.popCardFrom(player.opposite(), 0, LocationEnum.YOUR_DECK_TOP, false)?.let {
                        game_status.insertCardTo(player.opposite(), it, LocationEnum.COVER_CARD, false)
                    }
                    game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                        listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                        NUMBER_KURURU_LAST_RESEARCH, 1
                    ){ _, _ -> true }?: return@ret null
                }

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
                            EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                        if(it.getNap() == 2){
                            game_status.cardToDust(player, 2, it, false,
                                EventLog.IGNORE, LocationEnum.PLAYING_ZONE_YOUR)
                            greatDiscovery(card_number, player, game_status)
                            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
                            game_status.getPlayer(player).afterCardUseTermination = true
                        }
                    }
                }
            }

            if(!game_status.getImmediateReconstructListener(player).any {
                it.cardNumber == card_number
            }){
                game_status.addImmediateReconstructListener(player, Listener(player, card_number) {gameStatus, cardNumber, _, _, _, _ ->
                    if(cardNumber !in gameStatus.endPhaseEffect){
                        gameStatus.endPhaseEffect[cardNumber] = if(player == PlayerEnum.PLAYER1) {
                            Pair(CardEffectLocation.TEMP_PLAYER1, lastResearchReuseText)
                        } else{
                            Pair(CardEffectLocation.TEMP_PLAYER2, lastResearchReuseText)
                        }
                    }
                    false
                })
            }
            null
        })
        lastResearchV8_2.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_THIS_CARD_RETURN){ card_number, player, game_status, _ ->
            game_status.removeImmediateReconstructListener(player, card_number)
            game_status.endPhaseEffect.remove(card_number)
            null
        })
        lastResearchV8_2.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_USE_FULL_POWER_YOUR) { card_number, player, game_status, _ ->
            if(card_number !in game_status.endPhaseEffect){
                game_status.endPhaseEffect[card_number] = if(player == PlayerEnum.PLAYER1) {
                    Pair(CardEffectLocation.TEMP_PLAYER1, lastResearchReuseText)
                } else{
                    Pair(CardEffectLocation.TEMP_PLAYER2, lastResearchReuseText)
                }
            }
            null
        })


        steamCanonV8_2.setAttack(DistanceType.CONTINUOUS, Pair(3, 7), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        steamCanonV8_2.addText(combustCheckText)
        steamCanonV8_2.addText(combustText)
        steamCanonV8_2.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            if(game_status.getFullAction(player)){
                game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true
                }, {_, _, attack ->
                    attack.apply {
                        auraPlusMinus(2); lifePlusMinus(2)
                    }
                }))
                game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.PLUS_IMMEDIATE, {_, _, _ ->
                    true}, { _, _, attack ->
                        attack.apply {
                            plusMinusRange(1, true); plusMinusRange(1, false)
                        }
                    }))
            }
            null
        })
        steamCanonV8_2.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _->
            if(!(game_status.getFullAction(player))){
                game_status.restoreArtificialToken(player, 2)
            }
            null
        })


        stormSurgeAttackV8_2.setAttack(DistanceType.CONTINUOUS, Pair(1, 2), null, 1000, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        stormSurgeAttackV8_2.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
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


        deviceKururusikV8_2.setSpecial(2)
        deviceKururusikV8_2.setAttack(DistanceType.CONTINUOUS, Pair(3, 10), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        deviceKururusikV8_2.addText(terminationText)
        deviceKururusikV8_2.addText(Text(TextEffectTimingTag.USED, TextEffectTag.END_PHASE_ADDITIONAL_CHECK) { _, _, _, _ ->
            1
        })
        deviceKururusikV8_2.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_ADDITIONAL_CHECK) { card_number, player, game_status, _ ->
            if(game_status.dust >= 13) {
                reviveDemise(player, game_status)
                game_status.popCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
                }
                game_status.moveAdditionalCard(player, CardName.UTSURO_MANG_A, LocationEnum.YOUR_USED_CARD)?.let {
                    it.special_card_state = SpecialCardEnum.PLAYED
                    game_status.addMainPhaseListener(player, Listener(player, it.card_number) {gameStatus, cardNumber, _,
                                                                                               _, _, _ ->
                        gameStatus.returnSpecialCard(player, cardNumber)
                        true
                    })
                }
                game_status.drawCard(player, 1)
                if(game_status.getPlayerLife(player) >= 6){
                    game_status.lifeToDust(player, game_status.getPlayerLife(player) - 5, Arrow.NULL, player,
                        game_status.getCardOwner(card_number), card_number)
                }
                1
            }
            else{
                null
            }
        })


        vagueStoryV8_2.setSpecial(1)
        vagueStoryV8_2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_KANAWE_VAGUE_STORY)){
                    CommandEnum.SELECT_ONE -> {
                        readyIdea(player, game_status, LocationEnum.ADDITIONAL_CARD)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        if(readyIdea(player, game_status, LocationEnum.END_IDEA_YOUR)){
                            game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
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
        vagueStoryV8_2.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_START_PHASE_OTHER){ card_number, player, game_status, _ ->
            if(!(game_status.getPlayer(player).beforeTurnIdeaProcess)){
                game_status.startPhaseEffect[card_number] = Pair(CardEffectLocation.USED_OTHER, vagueStoryV8Hypen2Text)
            }

            null
        })


        flutteringBladeV8_2.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        flutteringBladeV8_2.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_AFTER_MAKE_ATTACK) { card_number, player, game_status, now_attack->
            if(game_status.getPlayer(player).tabooGauge != null){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_FLUTTERING_BLADE)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            now_attack?.tabooGaugeAmount = 1
                            game_status.showPlayersSelectResult(player.opposite(), NUMBER_KAMUWI_FLUTTERING_BLADE, 0)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.tabooGaugeIncrease(player, 1)
                            flutteringBladeV8Hypen2AddAttackBuffEffect(card_number, player, game_status)
                            game_status.showPlayersSelectResult(player.opposite(), NUMBER_KAMUWI_FLUTTERING_BLADE, 1)
                            break
                        }
                        CommandEnum.SELECT_THREE -> {
                            game_status.tabooGaugeIncrease(player, 2)
                            now_attack?.tabooGaugeAmount = 1
                            flutteringBladeV8Hypen2AddAttackBuffEffect(card_number, player, game_status)
                            game_status.showPlayersSelectResult(player.opposite(), NUMBER_KAMUWI_FLUTTERING_BLADE, 2)
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
        flutteringBladeV8_2.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHECK_THIS_ATTACK_VALUE) { card_number, player, game_status, now_attack ->
            if(now_attack?.tabooGaugeAmount == 1){
                if(game_status.getPlayer(player.opposite()).aura <= 4){
                    game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION,
                        player, game_status.getCardOwner(card_number), card_number)
                }
            }
            null
        })


        logicV8_2.setSpecial(3)
        logicV8_2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            for(i in 1..3){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_KAMUWI_LOGIC)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
                        }
                        CommandEnum.SELECT_TWO -> {
                            if(game_status.getPlayerLife(player) <= 8){
                                if(game_status.tabooGaugeIncrease(player, 1)){
                                    game_status.auraToLife(player, player, 1,
                                        Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
                                }
                            }
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


        fishingV8_2.addText(perjureText)
        fishingV8_2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.distanceToFlare(player, 1, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)
            null
        })


        directFinancingV8_2.setEnchantment(2)
        directFinancingV8_2.addText(investmentRightText)
        directFinancingV8_2.addText(chasmText)
        directFinancingV8_2.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            if(game_status.getConcentrationValue(player) >= 1 && game_status.canUseConcentration(player)){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_DIRECT_FINANCING_REDUCE_CONCENTRATION)){
                        CommandEnum.SELECT_ONE -> {
                            if(game_status.getConcentrationValue(player) == 2){
                                game_status.decreaseConcentration(player)
                            }
                            game_status.decreaseConcentration(player)
                            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
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
        directFinancingV8_2.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK)ret@{ card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_DIRECT_FINANCING)){
                    CommandEnum.SELECT_ONE -> {
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        return@ret null
                    }
                    else -> {}
                }
            }

            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.AKINA_DIRECT_FINANCING,
                        NUMBER_AKINA_DIRECT_FINANCING_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(2, 3, 4, 5), 1,  0,  MegamiEnum.AKINA,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        accurateCalcV8_2.setSpecial(NUMBER_MARKET_PRICE)
        accurateCalcV8_2.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(nowDistance in 0..6) 1
            else 0
        })
        accurateCalcV8_2.addText(investmentRightText)
        accurateCalcV8_2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_AKINA_ACCURATE_CALC)){
                    CommandEnum.SELECT_ONE -> {
                        recoup(game_status, player)
                        for(i in 1..2){
                            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_AKINA_AKINA_ACCURATE_CALC)
                        }
                        game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
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
        accurateCalcV8_2.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MAIN_PHASE_RECOUP_YOUR) ret@{ _, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_ACCURATE_CALC_INCUBATE)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.doBasicOperation(player, CommandEnum.ACTION_INCUBATE,
                            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_AKINA_AKINA_ACCURATE_CALC)
                        return@ret 1
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {}
                }
            }
            null
        })


        blackArmorV8_2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) ret@{ card_number, player, game_status, react_attack ->
            val count = game_status.gameLogger.countGetDamage(player)
            val beforeSelected = hashSetOf<CommandEnum>()

            for(i in 1..count){
                while(true){
                    val nowSelect = game_status.receiveCardEffectSelect(player, NUMBER_SHISUI_BLACK_ARMOR)
                    if(nowSelect in beforeSelected){
                        continue
                    }
                    when(nowSelect){
                        CommandEnum.SELECT_ONE -> {
                            beforeSelected.add(CommandEnum.SELECT_ONE)
                            react_attack?.addAttackBuff(Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ -> true },
                                {_, _, attack ->
                                    attack.lifePlusMinus(-1)
                                }))
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            beforeSelected.add(CommandEnum.SELECT_TWO)
                            game_status.setShrink(player.opposite())
                            break
                        }
                        CommandEnum.SELECT_THREE -> {
                            beforeSelected.add(CommandEnum.SELECT_THREE)
                            selectLaceration(player.opposite(), player, player.opposite(), game_status, NUMBER_SHISUI_BLACK_ARMOR_SELECT_LACERATION)
                            break
                        }
                        CommandEnum.SELECT_NOT -> {
                            return@ret null
                        }
                        else -> {}
                    }
                }
            }

            null
        })


        padmaCutDownV8_2.setSpecial(3)
        padmaCutDownV8_2.addText(Text(TextEffectTimingTag.USING, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE) { card_number, player, game_status, react_attack ->
            if(react_attack == null){
                padmaCutDownV8Hypen2EffectText.effect!!(card_number, player, game_status, null)
            }
            else{
                react_attack.afterAttackCompleteEffect.add(padmaCutDownV8Hypen2EffectText)
            }
            null
        })
    }

    private val deceptionFog = CardData(CardClass.NORMAL, CardName.RENRI_DECEPTION_FOG, MegamiEnum.RENRI, CardType.UNDEFINED, SubType.NONE)
    private val sinSoo = CardData(CardClass.NORMAL, CardName.RENRI_SIN_SOO, MegamiEnum.RENRI, CardType.BEHAVIOR, SubType.NONE)
    private val falseWeapon = CardData(CardClass.NORMAL, CardName.RENRI_FALSE_WEAPON, MegamiEnum.RENRI, CardType.ATTACK, SubType.NONE)
    private val essenceOfBlade = CardData(CardClass.NORMAL, CardName.RENRI_ESSENCE_OF_BLADE, MegamiEnum.ZANKA, CardType.BEHAVIOR, SubType.NONE)
    private val firstSakuraOrder = CardData(CardClass.NORMAL, CardName.RENRI_FIRST_SAKURA_ORDER, MegamiEnum.OUKA, CardType.BEHAVIOR, SubType.NONE)
    private val riRaRuRiRaRo = CardData(CardClass.SPECIAL, CardName.RENRI_RI_RA_RU_RI_RA_RO, MegamiEnum.RENRI, CardType.ENCHANTMENT, SubType.NONE)

    private val returnRelicText = Text(TextEffectTimingTag.USING, TextEffectTag.WHEN_AFTER_CARD_USE) ret@{card_number, player, game_status, _->
        val relicCard = game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?: return@ret null
        game_status.getPlayer(player).relic?.let {
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_RETURN_RELIC)){
                    CommandEnum.SELECT_ONE -> {
                        if(game_status.movePlayingCard(player, LocationEnum.RELIC_YOUR, card_number)){
                            game_status.popCardFrom(player, CardName.RENRI_SIN_SOO, LocationEnum.RELIC_YOUR, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                            }
                            relicCard.effectText(player, game_status, null, TextEffectTag.WHEN_THIS_CARD_RELIC_RETURN)
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
    }

    private val returnText = Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_DISPROVE_FAIL) ret@{card_number, _, game_status, _ ->
        val usedCard = game_status.cardForEffect?: return@ret null
        usedCard.cardUseEndEffect[card_number] = returnRelicText
        null
    }

    private val riRaRuRiRaRoReuseText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, player, game_status, _ ->
        while(true){
            when(game_status.receiveCardEffectSelect(player, NUMBER_RENRI_RI_RA_RU_RI_RA_RO)){
                CommandEnum.SELECT_ONE -> {
                    val card = game_status.getCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE)?: break
                    game_status.useCardFrom(player, card, LocationEnum.ENCHANTMENT_ZONE, false, null,
                        isCost = true, isConsume = true, -2)
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

    suspend fun useAnotherCard(player: PlayerEnum, game_status: GameStatus, cardName: CardName, react_attack: MadeAttack?){
        val useCard = makeCard(player, game_status, LocationEnum.OUT_OF_GAME, cardName)
        if(useCard.card_data.sub_type == SubType.FULL_POWER && !game_status.getFullAction(player)){
            return
        }
        game_status.useCardFrom(player, useCard, LocationEnum.ALL, false, react_attack,
            isCost = true, isConsume = true
        )
    }

    private fun renriA1CardInit(){
        deceptionFog.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.TREAT_AS_DIFFERENT_CARD) ret@{ card_number, player, game_status, react_attack ->
            val temp = game_status.selectCardFrom(player.opposite(), player, player,
                listOf(LocationEnum.ALL_NORMAL), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                NUMBER_RENRI_DECEPTION_FOG, 1) { card, _ ->
                card.card_data.card_type != CardType.ENCHANTMENT && card.card_data.megami != MegamiEnum.RENRI
                        && card.card_data.card_type != CardType.UNDEFINED
            } ?: return@ret null
            val card = temp[0]
            val cardName = card.toCardName()
            game_status.showPlayersSelectResult(player.opposite(), NUMBER_RENRI_DECEPTION_FOG, card)
            val otherPlayer = game_status.getPlayer(player.opposite())

            if(otherPlayer.isDiscardHave(cardName) || otherPlayer.isDeckHave(cardName) || otherPlayer.isCoverHave(cardName)){
                while(true){
                    when(game_status.receiveCardEffectSelect(player.opposite(), NUMBER_RENRI_DECEPTION_FOG)){
                        CommandEnum.SELECT_ONE -> {
                            val location = if(otherPlayer.isDiscardHave(cardName)) LocationEnum.DISCARD_YOUR.real_number
                            else if(otherPlayer.isDeckHave(cardName)) LocationEnum.DECK.real_number
                            else LocationEnum.COVER_CARD.real_number
                            game_status.showPlayersSelectResult(player, NUMBER_RENRI_DECEPTION_FOG, location)
                            game_status.popCardFrom(player, card_number, LocationEnum.HAND, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.DISCARD_YOUR, true)
                            }
                        }
                        CommandEnum.SELECT_NOT -> {
                            game_status.popCardFrom(player, card_number, LocationEnum.HAND, true)?.let {
                                game_status.insertCardTo(player, it, LocationEnum.PLAYING_ZONE_YOUR, true)
                            }
                            useAnotherCard(player, game_status, cardName, react_attack)
                            game_status.movePlayingCard(player, LocationEnum.DISCARD_YOUR, card_number)
                        }
                        else -> {
                            break
                        }
                    }
                }
            }
            else{
                delay(Random(System.currentTimeMillis()).nextLong() % 4000 + 4000)
                game_status.popCardFrom(player, card_number, LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.PLAYING_ZONE_YOUR, true)
                }
                useAnotherCard(player, game_status, cardName, react_attack)
                game_status.movePlayingCard(player, LocationEnum.DISCARD_YOUR, card_number)
            }
            1
        })

        sinSoo.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ card_number, player, game_status, _ ->
            if((card_number.toCardName() == CardName.RENRI_SIN_SOO)){
                game_status.popCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR, true)?.let {sinSoo ->
                    game_status.insertCardTo(player, sinSoo, LocationEnum.YOUR_DECK_TOP, true)
                    game_status.getPlayer(player).relic?.let { relic ->
                        if(relic.size > 0){
                            val selected = game_status.selectCardFrom(player, relic.keys.toMutableList(), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
                                NUMBER_RENRI_SIN_SOO, 1)[0]
                            game_status.popCardFrom(player, 0, LocationEnum.YOUR_DECK_TOP, false)?.let {sinSoo ->
                                game_status.insertCardTo(player, sinSoo, LocationEnum.RELIC_YOUR, false)
                                game_status.popCardFrom(player, selected, LocationEnum.RELIC_YOUR, false)?.let {relic ->
                                    game_status.insertCardTo(player, relic, LocationEnum.YOUR_DECK_TOP, false)
                                }
                            }
                        }
                    }
                }
            }
            null
        })
        sinSoo.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR) ret@{ card_number, player, game_status, _ ->
            if((card_number.toCardName() == CardName.RENRI_SIN_SOO)){
                game_status.getCardFrom(player, card_number, LocationEnum.DISCARD_YOUR)?.let{
                    game_status.useCardFrom(player, it, LocationEnum.DISCARD_YOUR, false, null,
                        isCost = true, isConsume = true
                    )
                }
            }
            null
        })

        falseWeapon.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = true, cannotReact = false, chogek = false)
        falseWeapon.addText(returnText)
        falseWeapon.addText(perjureText)
        falseWeapon.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS,
                { condition_player, condition_game_status, condition_attack ->
                    condition_game_status.gameLogger.checkThisCardUseWhen(condition_player, condition_attack.card_number) == 3
                }, { _, _, attack ->
                    attack.lifePlusMinus(1)
                })
            )
            null
        })

        essenceOfBlade.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 3, 1,
            cannotReactNormal = false, cannotReactSpecial = true, cannotReact = false, chogek = false)
        essenceOfBlade.addText(returnText)
        essenceOfBlade.addText(perjureText)
        essenceOfBlade.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                {condition_player, condition_game_status, _ ->
                    var result = false
                    for(card in condition_game_status.getPlayer(condition_player).enchantmentCard.values){
                        if(card.card_data.card_name == CardName.RENRI_RI_RA_RU_RI_RA_RO && (card.getNap() ?: 0) > 0) {
                            result = true
                            break
                        }
                    }
                    result
                },
                {_, _, attack ->
                    attack.lifePlusMinus(1)
                }))

            null
        })
        essenceOfBlade.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_RELIC_RETURN) { _, player, game_status, _->
            game_status.setConcentration(player.opposite(), 0)
            null
        })

        firstSakuraOrder.addText(returnText)
        firstSakuraOrder.addText(perjureText)
        firstSakuraOrder.addText(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _->
            game_status.requestAndDoBasicOperation(player, NUMBER_RENRI_FIRST_SAKURA_ORDER)
            null
        })
        firstSakuraOrder.addText(Text(TextEffectTimingTag.USING, TextEffectTag.WHEN_THIS_CARD_NOT_DISPROVE) { _, player, game_status, _->
            game_status.requestAndDoBasicOperation(player, NUMBER_RENRI_FIRST_SAKURA_ORDER)
            null
        })
        firstSakuraOrder.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
            if(game_status.dust <= 5){
                game_status.selectCardFrom(player, player, player, listOf(LocationEnum.COVER_CARD),
                    CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_RENRI_FIRST_SAKURA_ORDER, 1
                ) {_, _ -> true}?.let {selected ->
                    game_status.popCardFrom(player, selected[0], LocationEnum.COVER_CARD, false)?.let{
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                    }
                }
            }
            null
        })
        firstSakuraOrder.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_THIS_CARD_RELIC_RETURN) { card_number, player, game_status, _->
            game_status.dustToLife(player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            null
        })

        riRaRuRiRaRo.setSpecial(0)
        riRaRuRiRaRo.setEnchantment(0)
        riRaRuRiRaRo.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.ADJUST_NAP_CONTAIN_OTHER_PLACE) { card_number, player, game_status, _ ->
            game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                game_status.lifeToCard(player, 1, it, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD,
                    reconstruct = false, damage = false, reason = card_number
                )
            }
            2
        })
        riRaRuRiRaRo.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, riRaRuRiRaRoReuseText)
            null
        })
        riRaRuRiRaRo.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.WHEN_MAIN_PHASE_YOUR) { _, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_RI_RA_RU_RI_RA_RO_DRAW_CARD)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.drawCard(player, 1)
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
        riRaRuRiRaRo.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHOJO_DAMAGE_CHANGE_OTHER) { _, _, _, _->
            21 // 20 means 2 aura / 1 means 1 life(20 + 1)
        })
        riRaRuRiRaRo.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.RIRARURIRARO_EFFECT) { _, _, _, _->
            1
        })
    }

    private val flutteringSnowflakeV9 = CardData(CardClass.SPECIAL, CardName.YUKIHI_FLUTTERING_SNOWFLAKE, MegamiEnum.YUKIHI, CardType.ATTACK, SubType.NONE)
    private val sadoV9 = CardData(CardClass.NORMAL, CardName.SHINRA_SA_DO, MegamiEnum.SHINRA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val directFinancingV9 = CardData(CardClass.NORMAL, CardName.AKINA_DIRECT_FINANCING, MegamiEnum.AKINA, CardType.ENCHANTMENT, SubType.FULL_POWER)
    private val accurateCalcV9 = CardData(CardClass.SPECIAL, CardName.AKINA_AKINA_ACCURATE_CALC, MegamiEnum.AKINA, CardType.BEHAVIOR, SubType.NONE)
    private val ironPowderWindAroundV9 = CardData(CardClass.NORMAL, CardName.SHISUI_IRON_POWDER_WIND_AROUND, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.NONE)
    private val padmaCutDownV9 = CardData(CardClass.SPECIAL, CardName.SHISUI_PADMA_CUT_DOWN, MegamiEnum.SHISUI, CardType.BEHAVIOR, SubType.REACTION)
    private val ensemble = CardData(CardClass.NORMAL, CardName.SAI_TOKO_ENSEMBLE, MegamiEnum.SAI_TOKO, CardType.ATTACK, SubType.REACTION)
    private val flowingPlayV9 = CardData(CardClass.NORMAL, CardName.TOKOYO_FLOWING_PLAY, MegamiEnum.TOKOYO, CardType.ATTACK, SubType.NONE)
    private val duetChitanYangMyeongV9 = CardData(CardClass.SPECIAL, CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG, MegamiEnum.TOKOYO, CardType.BEHAVIOR, SubType.NONE)
    private val accompanimentV9 = CardData(CardClass.NORMAL, CardName.SAINE_ACCOMPANIMENT, MegamiEnum.SAINE, CardType.ENCHANTMENT, SubType.NONE)
    private val duetTanJuBingMyeongV9 = CardData(CardClass.SPECIAL, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, MegamiEnum.SAINE, CardType.BEHAVIOR, SubType.NONE)

    private val sadoV9ShinsanText = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
        val hand = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
            listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_SADO_V8_SHINSAN_HAND, 1
        ) {_, _ -> true}?.let {selected ->
            selected[0]
        }

       val discardOrCover = game_status.selectCardFrom(player.opposite(), player.opposite(), player,
            listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            NUMBER_SADO_V8_SHINSAN_COVER, 1
        ) {_, _ -> true}?.let {selected ->
            selected[0]
        }

        if(discardOrCover == null){
            game_status.popCardFrom(player.opposite(), hand?: -1, LocationEnum.HAND, false)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
            }
        }
        else if(hand == null){
            game_status.popCardFrom(player.opposite(), discardOrCover, LocationEnum.DISCARD_YOUR, false)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
            }?: game_status.popCardFrom(player.opposite(), discardOrCover, LocationEnum.COVER_CARD, false)?.let {
                game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
            }
        }
        else{
            while(true){
                when(game_status.receiveCardEffectSelect(player.opposite(), NUMBER_SADO_PRIORITY)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.popCardFrom(player.opposite(), discardOrCover, LocationEnum.DISCARD_YOUR, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }?: game_status.popCardFrom(player.opposite(), discardOrCover, LocationEnum.COVER_CARD, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                        game_status.popCardFrom(player.opposite(), hand, LocationEnum.HAND, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.popCardFrom(player.opposite(), hand, LocationEnum.HAND, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                        game_status.popCardFrom(player.opposite(), discardOrCover, LocationEnum.DISCARD_YOUR, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }?: game_status.popCardFrom(player.opposite(), discardOrCover, LocationEnum.COVER_CARD, false)?.let {
                            game_status.insertCardTo(player.opposite(), it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                        break
                    }
                    else -> {}
                }
            }
        }
        null
    }

    private suspend fun sadoV9MakeAttackEffect(player: PlayerEnum, game_status: GameStatus, card_number: Int){
        when(game_status.getStratagem(player)){
            Stratagem.SHIN_SAN -> {
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.SHINRA_SA_DO,
                            NUMBER_SHINRA_SA_DO_ADDITIONAL_1, CardClass.NULL,
                            sortedSetOf(1, 3, 5), 2,  2,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false,
                            inevitable = true
                        ).addTextAndReturn(sadoV9ShinsanText)
                    ) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }

                if(game_status.endCurrentPhase){
                    game_status.getPlayer(player).stratagem = null
                    return
                }
                if(!game_status.getPlayer(player).justRunStratagem){
                    setStratagemByUser(game_status, player)
                }
            }
            Stratagem.GUE_MO -> {
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.SHINRA_SA_DO,
                            NUMBER_SHINRA_SA_DO_ADDITIONAL_2, CardClass.NULL,
                            sortedSetOf(0, 2, 4), 2, 2,  MegamiEnum.SHINRA,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false,
                            inevitable = true
                        ).addTextAndReturn(sadoV8_2GuemoText)
                    ) ){
                    game_status.afterMakeAttack(card_number, player, null)
                }

                if(game_status.endCurrentPhase){
                    game_status.getPlayer(player).stratagem = null
                    return
                }
                if(!game_status.getPlayer(player).justRunStratagem){
                    setStratagemByUser(game_status, player)
                }
            }
            null -> {}
        }
    }

    fun isUsedSomeMegamisSpecial(game_status: GameStatus, player: PlayerEnum, megami: MegamiEnum): Boolean{
        return game_status.getPlayer(player).usedSpecialCard.values.any { card ->
            card.card_data.megami.equal(megami)
        }
    }

    fun isUsedSomeOtherMegamisSpecial(game_status: GameStatus, player: PlayerEnum, megami: MegamiEnum): Boolean{
        return game_status.getPlayer(player).usedSpecialCard.values.any { card ->
            !(card.card_data.megami.equal(megami))
        }
    }


    private fun v9CardInit(){
        flutteringSnowflakeV9.umbrellaMark = true
        flutteringSnowflakeV9.setSpecial(2)
        flutteringSnowflakeV9.setAttackFold(DistanceType.CONTINUOUS, Pair(4, 5), null, 3, 1)
        flutteringSnowflakeV9.setAttackUnfold(DistanceType.CONTINUOUS, Pair(0, 2), null, 0, 0)
        flutteringSnowflakeV9.addTextFold(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _ ->
            game_status.addConcentration(player)
            null
        })
        flutteringSnowflakeV9.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateUmbrellaListener(player, Listener(player, card_number){gameStatus, cardNumber, _, _, _, _ ->
                gameStatus.returnSpecialCard(player, cardNumber)
                true
            })
            null
        })


        sadoV9.setEnchantment(2)
        sadoV9.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _ ->
            if(game_status.getPlayer(player).concentration >= 1 && game_status.canUseConcentration(player)){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_SHINRA_SA_DO)){
                        CommandEnum.SELECT_ONE -> {
                            if(game_status.decreaseConcentration(player)){
                                setStratagemByUser(game_status, player)
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
            null
        })
        sadoV9.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.RUN_STRATAGEM) { card_number, player, game_status, _->
            sadoV9MakeAttackEffect(player, game_status, card_number)
            null
        })
        sadoV9.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _->
            sadoV9MakeAttackEffect(player, game_status, card_number)
            null
        })


        directFinancingV9.setEnchantment(2)
        directFinancingV9.addText(investmentRightText)
        directFinancingV9.addText(chasmText)
        directFinancingV9.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            if(game_status.getConcentrationValue(player) >= 1 && game_status.canUseConcentration(player)){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_DIRECT_FINANCING_REDUCE_CONCENTRATION)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.decreaseConcentration(player)
                            game_status.auraToAura(player.opposite(), player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
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
        directFinancingV9.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MAKE_ATTACK)ret@{ card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_DIRECT_FINANCING)){
                    CommandEnum.SELECT_ONE -> {
                        break
                    }
                    CommandEnum.SELECT_NOT -> {
                        return@ret null
                    }
                    else -> {}
                }
            }

            if(game_status.addPreAttackZone(
                    player, MadeAttack(CardName.AKINA_DIRECT_FINANCING,
                        NUMBER_AKINA_DIRECT_FINANCING_ADDITIONAL, CardClass.NULL,
                        sortedSetOf(2, 3, 4, 5), 1,  0,  MegamiEnum.AKINA,
                        cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false
                    )
                ) ){
                game_status.afterMakeAttack(card_number, player, null)
            }
            null
        })


        accurateCalcV9.setSpecial(NUMBER_MARKET_PRICE)
        accurateCalcV9.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(nowDistance in 0..7) 1
            else 0
        })
        accurateCalcV9.addText(investmentRightText)
        accurateCalcV9.addText(Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_AKINA_ACCURATE_CALC)){
                    CommandEnum.SELECT_ONE -> {
                        recoup(game_status, player)
                        for(i in 1..2){
                            game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                                CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_AKINA_AKINA_ACCURATE_CALC)
                        }
                        game_status.movePlayingCard(player, LocationEnum.OUT_OF_GAME, card_number)
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
        accurateCalcV9.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MAIN_PHASE_RECOUP_YOUR) ret@{ _, player, game_status, _ ->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_AKINA_ACCURATE_CALC_INCUBATE)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.doBasicOperation(player, CommandEnum.ACTION_INCUBATE,
                            CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_AKINA_AKINA_ACCURATE_CALC)
                        return@ret 1
                    }
                    CommandEnum.SELECT_NOT -> {
                        break
                    }
                    else -> {}
                }
            }
            null
        })


        ironPowderWindAroundV9.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(nowDistance in 0..4) 1
            else 0
        })
        ironPowderWindAroundV9.addText(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _ ->
            for(i in 1..2){
                game_status.doBasicOperation(player, CommandEnum.ACTION_WIND_AROUND,
                    CommandEnum.BASIC_OPERATION_CAUSE_BY_CARD + NUMBER_SHISUI_IRON_POWDER_WIND_AROUND)
            }

            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_SHISUI_IRON_POWDER_WIND_AROUND)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.addLacerationToken(player, player, INDEX_LACERATION_AURA, 1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.addLacerationToken(player, player, INDEX_LACERATION_FLARE, 1)
                        break
                    }
                    else -> {}
                }
            }

            while(true){
                when(game_status.receiveCardEffectSelect(player.opposite(), NUMBER_SHISUI_IRON_POWDER_WIND_AROUND)){
                    CommandEnum.SELECT_ONE -> {
                        game_status.addLacerationToken(player.opposite(), player, INDEX_LACERATION_AURA, 1)
                        break
                    }
                    CommandEnum.SELECT_TWO -> {
                        game_status.addLacerationToken(player.opposite(), player, INDEX_LACERATION_FLARE, 1)
                        break
                    }
                    else -> {}
                }
            }
            null
        })


        padmaCutDownV9.setSpecial(3)
        padmaCutDownV9.addText(Text(TextEffectTimingTag.USING, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE) { card_number, player, game_status, react_attack ->
            if(react_attack == null){
                padmaCutDownEffectText.effect!!(card_number, player, game_status, null)
            }
            else{
                react_attack.afterAttackCompleteEffect.add(padmaCutDownEffectText)
            }
            null
        })


        ensemble.setAttack(DistanceType.CONTINUOUS, Pair(2, 5), null, 3, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        ensemble.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN){ card_number, player, game_status, _ ->
            if(isUsedSomeMegamisSpecial(game_status, player, MegamiEnum.SAINE)){
                game_status.auraToDust(player.opposite(), 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }

            if(palSang(player, game_status) || kyochi(player, game_status)){
                game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }

            if(isUsedSomeMegamisSpecial(game_status, player, MegamiEnum.TOKOYO)){
                game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        flowingPlayV9.setAttack(DistanceType.CONTINUOUS, Pair(5, 5), null, 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        flowingPlayV9.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, buffRet@{ buff_player, buff_game_status, _ ->
                isUsedSomeMegamisSpecial(buff_game_status, buff_player, MegamiEnum.TOKOYO)
            }, { _, _, attack ->
                attack.canNotReact()
            }))
            null
        })
        flowingPlayV9.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            if(kyochi(player, game_status) || isUsedSomeOtherMegamisSpecial(game_status, player, MegamiEnum.TOKOYO)) {
                if(thisCardMoveTextCheck(card_number.toCardName(), CardName.TOKOYO_FLOWING_PLAY)){
                    game_status.movePlayingCard(player, LocationEnum.YOUR_DECK_TOP, card_number)
                }
            }
            null
        })


        duetChitanYangMyeongV9.setSpecial(2)
        duetChitanYangMyeongV9.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF){ card_number, player, game_status, _ ->
            if(kyochi(player, game_status)){
                game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true}, { cost, _, _ ->
                    if(cost <= 0){
                        0
                    }
                    else{
                        cost - 1
                    }
                }))
            }
            null
        })
        duetChitanYangMyeongV9.addText(Text(TextEffectTimingTag.USING, TextEffectTag.AFTER_OTHER_ATTACK_COMPLETE) { _, player, game_status, _ ->
            game_status.getPlayer(player).canNotUseConcentration = true
            null
        })
        duetChitanYangMyeongV9.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_REACT_YOUR) { card_number, player, game_status, _ ->
            if((game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.
                card_data?.megami?: MegamiEnum.NONE) != MegamiEnum.TOKOYO){
                while(true){
                    val selected = game_status.selectCardFrom(player, player, player,
                        listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_TOKOYO_DUET_CHI_TAN_YANG_MYEONG
                    ) {card, from -> !(from == LocationEnum.DISCARD_YOUR && card.isSoftAttack)}?: break
                    if (selected.size == 1){
                        game_status.popCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                        }?: game_status.popCardFrom(player, selected[0], LocationEnum.COVER_CARD, false)?.let {
                            game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, false)
                        }
                        break
                    }
                }

            }
            null
        })
        duetChitanYangMyeongV9.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {gameStatus, cardNumber, reason,
                                                                                        _, reconstruct, damage ->
                if(!reconstruct && damage && reason !in damageNotAttackSet){
                    gameStatus.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })


        accompanimentV9.setEnchantment(4)
        accompanimentV9.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER){ card_number, player, game_status, _ ->
            if(!(game_status.gameLogger.checkThisTurnDoAttack(player.opposite()))){
                game_status.addThisTurnAttackBuff(player.opposite(), Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE,
                    {buff_player, buff_game_status, _ ->
                        isUsedSomeOtherMegamisSpecial(buff_game_status, buff_player, MegamiEnum.SAINE) || palSang(buff_player, buff_game_status)
                    },
                    { _, _, madeAttack ->
                        madeAttack.auraPlusMinus(-1)
                    }))
                game_status.addThisTurnOtherBuff(player.opposite(), OtherBuff(card_number, 1, OtherBuffTag.LOSE_IMMEDIATE,
                    { buff_player, buff_game_status, _ ->
                        isUsedSomeOtherMegamisSpecial(buff_game_status, buff_player, MegamiEnum.SAINE) || palSang(buff_player, buff_game_status)
                    }, { _, _, attack ->
                        attack.editedCannotReactSpecial = false
                        attack.editedCannotReactNormal = false
                        attack.editedCannotReact = false
                    })
                )
            }
            null
        })
        accompanimentV9.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.COST_BUFF){ card_number, player, game_status, _ ->
            if(isUsedSomeMegamisSpecial(game_status, player, MegamiEnum.SAINE)){
                game_status.addThisTurnCostBuff(player, CostBuff(card_number, 999, BufTag.PLUS_MINUS, {_, _, _ ->
                    true}, { cost, _, _ ->
                    if(cost <= 0){
                        0
                    }
                    else{
                        cost - 1
                    }
                }))
            }
            null
        })
        accompanimentV9.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.COST_BUFF){ card_number, player, game_status, _ ->
            if(isUsedSomeMegamisSpecial(game_status, player, MegamiEnum.SAINE)){
                game_status.addThisTurnCostBuff(player, CostBuff(card_number, 999, BufTag.PLUS_MINUS, {_, _, _ ->
                    true}, { cost, _, _ ->
                    if(cost <= 0){
                        0
                    }
                    else{
                        cost - 1
                    }
                }))
            }
            null
        })


        duetTanJuBingMyeongV9.setSpecial(2)
        duetTanJuBingMyeongV9.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF){ card_number, player, game_status, _ ->
            if(palSang(player, game_status)){
                game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                    true}, { cost, _, _ ->
                    if(cost <= 0){
                        0
                    }
                    else{
                        cost - 1
                    }
                }))
            }
            null
        })
        duetTanJuBingMyeongV9.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CAN_NOT_USE_CARD) { _, player, game_status, _->
            game_status.getPlayer(player).canNotAttack = true
            null
        })
        duetTanJuBingMyeongV9.addText(Text(TextEffectTimingTag.USED, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, attack ->
                attack.megami != MegamiEnum.SAINE
            }, {_, _, attack ->
                attack.apply {
                    lifePlusMinus(1)
                }
            }))
            null
        })
        duetTanJuBingMyeongV9.addText(Text(TextEffectTimingTag.USED, TextEffectTag.IMMEDIATE_RETURN){ card_number, player, game_status, _ ->
            game_status.addImmediateLifeListener(player, Listener(player, card_number) {gameStatus, cardNumber, reason,
                                                                                        _, reconstruct, damage ->
                if(!reconstruct && damage && reason !in damageNotAttackSet){
                    gameStatus.returnSpecialCard(player, cardNumber)
                    true
                }
                else{
                    false
                }
            })
            null
        })
    }

    private val misora = CardData(CardClass.SPECIAL, CardName.MISORA_MISORA, MegamiEnum.MISORA, CardType.BEHAVIOR, SubType.NONE)
    private val bowSpilling = CardData(CardClass.NORMAL, CardName.MISORA_BOW_SPILLING, MegamiEnum.MISORA, CardType.ATTACK, SubType.NONE)
    private val aimingKick = CardData(CardClass.NORMAL, CardName.MISORA_AIMING_KICK, MegamiEnum.MISORA, CardType.ATTACK, SubType.NONE)
    private val windHole = CardData(CardClass.NORMAL, CardName.MISORA_WIND_HOLE, MegamiEnum.MISORA, CardType.ATTACK, SubType.REACTION)
    private val gapSiEulSi = CardData(CardClass.NORMAL, CardName.MISORA_GAP_SI_EUL_SI, MegamiEnum.MISORA, CardType.ATTACK, SubType.FULL_POWER)
    private val precision = CardData(CardClass.NORMAL, CardName.MISORA_PRECISION, MegamiEnum.MISORA, CardType.BEHAVIOR, SubType.NONE)
    private val trackingAttack = CardData(CardClass.NORMAL, CardName.MISORA_TRACKING_ATTACK, MegamiEnum.MISORA, CardType.BEHAVIOR, SubType.NONE)
    private val skyWing = CardData(CardClass.NORMAL, CardName.MISORA_SKY_WING, MegamiEnum.MISORA, CardType.ENCHANTMENT, SubType.NONE)
    private val endlessEnd = CardData(CardClass.SPECIAL, CardName.MISORA_ENDLESS_END, MegamiEnum.MISORA, CardType.ATTACK, SubType.NONE)
    private val cloudEmbroideredCloud = CardData(CardClass.SPECIAL, CardName.MISORA_CLOUD_EMBROIDERED_CLOUD, MegamiEnum.MISORA, CardType.ENCHANTMENT, SubType.NONE)
    private val shadowShadyShadow = CardData(CardClass.SPECIAL, CardName.MISORA_SHADOW_SHADY_SHADOW, MegamiEnum.MISORA, CardType.ENCHANTMENT, SubType.REACTION)
    private val skyBeyondSky = CardData(CardClass.SPECIAL, CardName.MISORA_SKY_BEYOND_SKY, MegamiEnum.MISORA, CardType.ENCHANTMENT, SubType.FULL_POWER)

    private val setAimingText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { _, player, game_status, _ ->
        while(true){
            val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_MISORA_MISORA)
            if(nowCommand == CommandEnum.SELECT_ONE){
                game_status.setAiming(player, game_status.getAdjustDistance())
                break
            }
            else if(nowCommand == CommandEnum.SELECT_NOT){
                break
            }
        }
        null
    }

    private val endlessEndText = Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR){card_number, player, game_status, _ ->
        game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
            game_status.dustToCard(player, 1, it, card_number, LocationEnum.YOUR_USED_CARD)
        }
        game_status.returnSpecialCard(player, card_number)
        null
    }

    private suspend fun isAimingCorrectly(player: PlayerEnum, game_status: GameStatus, attack: MadeAttack): Boolean{
        return attack.rangeCheck(game_status.getAdjustDistance(), game_status, player) &&
                attack.rangeCheckAfterApplyBUff(game_status.getPlayer(player).aiming?: -999)
    }

    private fun misoraCardInit(){
        misora.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.MEGAMI_YOUR, setAimingText)
            null
        })
        misora.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_MAIN_PHASE_END_YOUR) { _, player, game_status, _ ->
            if((game_status.gameLogger.checkThisTurnDoAttack(player))){
                game_status.setAiming(player, -1)
            }
            null
        })


        bowSpilling.setAttack(DistanceType.CONTINUOUS, Pair(4, 7), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        bowSpilling.addText((Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.INSERT_IMMEDIATE,
                {buff_player, buff_game_status, attack -> isAimingCorrectly(buff_player, buff_game_status, attack) },
                {_, _, attack ->
                    attack.run {
                        tempEditedAuraDamage = 999
                    }
                }))
            null
        }))
        bowSpilling.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            if(game_status.getPlayer(player).aiming == game_status.getAdjustDistance()){
                game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player,
                    game_status.getCardOwner(card_number), card_number)
            }
            null
        })


        aimingKick.setAttack(DistanceType.CONTINUOUS, Pair(2, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        aimingKick.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { _, player, game_status, _ ->
            game_status.getPlayer(player).aiming?.let { aiming ->
                while(true){
                    val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_MISORA_AIMING_KICK)
                    if(nowCommand == CommandEnum.SELECT_ONE){
                        game_status.setAiming(player, aiming + 1)
                        break
                    }
                    else if(nowCommand == CommandEnum.SELECT_TWO){
                        game_status.setAiming(player, aiming - 1)
                        break
                    }
                    else if(nowCommand == CommandEnum.SELECT_NOT){
                        break
                    }
                }
            }
            null
        })


        windHole.setAttack(DistanceType.CONTINUOUS, Pair(2, 5), null, 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        windHole.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.getPlayer(player).aiming?.let { aiming ->
                val nowDistance = game_status.getAdjustDistance()
                if(nowDistance > aiming){
                    game_status.distanceToDust(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
                }
                else if(nowDistance == aiming){
                    game_status.dustToAura(player, 1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
                }
                else{
                    game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
                }
            }
            null
        })


        gapSiEulSi.setAttack(DistanceType.CONTINUOUS, Pair(5, 15), null, 5, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false, isTrace = true)
        gapSiEulSi.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            game_status.selectCardFrom(player.opposite(), player.opposite(), player,
                listOf(LocationEnum.HAND), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_MISORA_GAP_SI_EUL_SI, 1
            ) {card, _ -> card.card_data.card_type == CardType.ATTACK}?.let {selected ->
                game_status.popCardFrom(player.opposite(), selected[0], LocationEnum.HAND, true)?.let {
                    game_status.insertCardTo(player.opposite(), it, LocationEnum.DISCARD_YOUR, true)
                }
            }?: run {
                game_status.showSome(player.opposite(), CommandEnum.SHOW_HAND_YOUR)
                game_status.deckToCoverCard(player.opposite(), 3)
            }
            null
        })


        precision.addText(Text(TextEffectTimingTag.USING, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            game_status.addConcentration(player)
            game_status.addThisTurnAttackBuff(player, Buff(card_number,1, BufTag.PLUS_MINUS, {buff_player, buff_game_status, attack ->
                (attack.megami != MegamiEnum.MISORA) && (attack.getEditedAuraDamage() != 999) &&
                        isAimingCorrectly(buff_player, buff_game_status, attack)
            },
                { _, _, attack -> attack.run{
                    auraPlusMinus(1); lifePlusMinus(1)
                }
                }))
            null
        })


        trackingAttack.addText(Text(TextEffectTimingTag.USING, TextEffectTag.USE_CARD) { card_number, player, game_status, _ ->
            game_status.selectCardFrom(player, player, player, listOf(LocationEnum.NOT_SELECTED_NORMAL, LocationEnum.COVER_CARD),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_MISORA_TRACKING_ATTACK, 1) {
                    card, from ->
                        card.card_data.sub_type != SubType.FULL_POWER && card.card_data.card_type == CardType.ATTACK &&
                                ((from == LocationEnum.NOT_SELECTED_NORMAL && card.card_data.megami != MegamiEnum.MISORA)
                                        || from == LocationEnum.COVER_CARD)
            }?.let { selected ->
                game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET, { _, _, _ ->true
                    }, { _, _, attack ->
                        attack.isTrace = true
                    })
                )

                game_status.getCardFrom(player, selected[0], LocationEnum.COVER_CARD)?.let {card ->
                    game_status.useCardFromNotFullAction(player, card, LocationEnum.COVER_CARD, false, null,
                        isCost = true, isConsume = true)
                    1
                }?: run {
                    game_status.getPlayer(player).unselectedCard.remove(selected[0].toCardName())
                    val useCard = makeCard(player, game_status, LocationEnum.OUT_OF_GAME, selected[0].toCardName())
                    game_status.insertCardTo(player, useCard, LocationEnum.PLAYING_ZONE_YOUR, true)
                    game_status.getPlayer(player).usingCard.remove(useCard)
                    game_status.useCardFromNotFullAction(player, useCard, LocationEnum.PLAYING_ZONE_YOUR, false, null,
                        isCost = true, isConsume = true, cardMoveCancel = true
                    )
                    game_status.getPlayer(player).usingCard.add(useCard)
                    game_status.popCardFrom(player, useCard.card_number, LocationEnum.PLAYING_ZONE_YOUR, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.OUT_OF_GAME, true)
                    }
                }
            }
            null
        })


        skyWing.setEnchantment(2)
        skyWing.addText(terminationText)
        skyWing.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION){ _, _, game_status, _ ->
            val nowDistance = game_status.getAdjustDistance()
            if(nowDistance in 0..3) 1
            else 0
        })
        skyWing.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.auraToDistance(player.opposite(), 2, Arrow.ONE_DIRECTION, player,
                game_status.getCardOwner(card_number), card_number)

            null
        })
        skyWing.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_THIS_TURN_DISTANCE) { _, _, game_status, _ ->
            game_status.addThisTurnDistance(1)
            null
        })
        skyWing.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.CHANGE_THIS_TURN_SWELL_DISTANCE) { _, _, game_status, _ ->
            game_status.addThisTurnSwellDistance(1)
            null
        })


        endlessEnd.setSpecial(2)
        endlessEnd.setAttack(DistanceType.DISCONTINUOUS, null, mutableListOf(), 999, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = true, chogek = false, isTrace = true)
        endlessEnd.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.NEXT_ATTACK_ENCHANTMENT) { card_number, player, game_status, _ ->
            val sakuraToken = game_status.getCardFrom(player, card_number, LocationEnum.SPECIAL_CARD)?.let {
                it.getNap()?: 0
            }?: game_status.getCardFrom(player, card_number, LocationEnum.YOUR_USED_CARD)?.let {
                it.getNap()?: 0
            }?: game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.let {
                it.getNap()?: 0
            }?: game_status.getCardFrom(player.opposite(), card_number, LocationEnum.SPECIAL_CARD)?.let {
                it.getNap()?: 0
            }?: game_status.getCardFrom(player.opposite(), card_number, LocationEnum.YOUR_USED_CARD)?.let {
                it.getNap()?: 0
            }?: 0

            game_status.addThisTurnRangeBuff(player, RangeBuff(card_number,1, RangeBufTag.CARD_CHANGE_IMMEDIATE, {_, _, _ -> true}
            ) { _, _, attack ->
                attack.run {
                    editedDistance.add(sakuraToken * 2 + 3)
                }
            })
            null
        })
        endlessEnd.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            val sakuraToken = game_status.getCardFrom(player, card_number, LocationEnum.PLAYING_ZONE_YOUR)?.getNap()?: 0

            if(sakuraToken >= 2){
                game_status.deckToCoverCard(player.opposite(), 1000)
            }
            null
        })
        endlessEnd.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_END_PHASE_YOUR) { card_number, _, game_status, _ ->
            game_status.endPhaseEffect[card_number] = Pair(CardEffectLocation.USED_YOUR, endlessEndText)
            null
        })


        cloudEmbroideredCloud.setSpecial(1)
        cloudEmbroideredCloud.setEnchantment(1)
        cloudEmbroideredCloud.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_DISTANCE){ _, player, game_status, _->
            if(game_status.getPlayer(player.opposite()).aiming != null &&
                game_status.getPlayer(player.opposite()).enchantmentCard.values.any { card ->
                    card.card_data.card_name == CardName.MISORA_CLOUD_EMBROIDERED_CLOUD
                }){
                0
            }
            else{
                game_status.getPlayer(player).aiming?.let {
                    it - game_status.getTokenDistance()
                }?: 0
            }
        })
        cloudEmbroideredCloud.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_GO_FORWARD_YOUR) { _, _, _, _ ->
            1
        })
        cloudEmbroideredCloud.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.FORBID_BREAK_AWAY_YOUR) { _, _, _, _ ->
            1
        })


        shadowShadyShadow.setSpecial(2)
        shadowShadyShadow.setEnchantment(3)
        shadowShadyShadow.addText(Text(TextEffectTimingTag.START_DEPLOYMENT, TextEffectTag.REACT_ATTACK_STATUS_CHANGE) { card_number, player, game_status, react_attack ->
            if(react_attack?.card_class == CardClass.NORMAL &&
                react_attack.rangeCheck(game_status.getPlayer(player).aiming?: -999, game_status, player.opposite())){

                react_attack.addOtherBuff(OtherBuff(card_number, 1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ ->
                    true
                }, { _, _, attack ->
                    attack.makeNotValid()
                }))

                game_status.popCardFrom(player.opposite(), card_number, LocationEnum.PLAYING_ZONE_YOUR, false)?.let {sealCard ->
                    sealCard(player, game_status, card_number, sealCard)
                    1
                }?: run {
                    game_status.getPlayer(player.opposite()).usingCard.getOrNull(0)?.let {
                        game_status.popCardFrom(player.opposite(), it.card_number, LocationEnum.PLAYING_ZONE_YOUR, false)?.let { sealCard ->
                            sealCard(player, game_status, card_number, sealCard)
                        }
                    }
                }
            }


            null
        })
        shadowShadyShadow.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_CARD) { card_number, player, game_status, _ ->
            unSealCard(player, game_status, card_number, LocationEnum.DISCARD_OTHER)
            null
        })


        skyBeyondSky.setSpecial(5)
        skyBeyondSky.setEnchantment(2)
        skyBeyondSky.addText(whenDistanceChangeText)
        skyBeyondSky.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.USING_CONDITION) { card_number, player, game_status, _->
            if (game_status.getPlayer(player).usedSpecialCard[card_number] == null) 1
            else 0
        })
        skyBeyondSky.addText(Text(TextEffectTimingTag.IN_DEPLOYMENT, TextEffectTag.CHANGE_DISTANCE){ _, _, _, _->
            5
        })
        skyBeyondSky.addText(Text(TextEffectTimingTag.AFTER_DESTRUCTION, TextEffectTag.MOVE_TOKEN) { card_number, player, game_status, _ ->
            game_status.lifeToDistance(player.opposite(), 1, false,
                Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
            game_status.auraToDistance(player.opposite(), 1, Arrow.ONE_DIRECTION,
                player, game_status.getCardOwner(card_number), card_number)
            game_status.flareToDistance(player.opposite(), 1, Arrow.ONE_DIRECTION,
                player, game_status.getCardOwner(card_number), card_number)
            null
        })
    }

    private val holoKunai = CardData(CardClass.NORMAL, CardName.OBORO_HOLOGRAM_KUNAI, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val gigasuke = CardData(CardClass.SPECIAL, CardName.OBORO_GIGASUKE, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val electricsouchi = CardData(CardClass.SPECIAL, CardName.OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI,
        MegamiEnum.OBORO, CardType.BEHAVIOR, SubType.FULL_POWER)

    private val mainPartsX = CardData(CardClass.MAIN_PARTS, CardName.OBORO_MAIN_PARTS_X, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val mainPartsY = CardData(CardClass.MAIN_PARTS, CardName.OBORO_MAIN_PARTS_Y, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)
    private val mainPartsZ = CardData(CardClass.MAIN_PARTS, CardName.OBORO_MAIN_PARTS_Z, MegamiEnum.OBORO, CardType.ATTACK, SubType.NONE)

    private val customPartsA = CardData(CardClass.CUSTOM_PARTS, CardName.OBORO_CUSTOM_PARTS_A, MegamiEnum.OBORO, CardType.UNDEFINED, SubType.NONE)
    private val customPartsB = CardData(CardClass.CUSTOM_PARTS, CardName.OBORO_CUSTOM_PARTS_B, MegamiEnum.OBORO, CardType.UNDEFINED, SubType.NONE)
    private val customPartsC = CardData(CardClass.CUSTOM_PARTS, CardName.OBORO_CUSTOM_PARTS_C, MegamiEnum.OBORO, CardType.UNDEFINED, SubType.NONE)
    private val customPartsD = CardData(CardClass.CUSTOM_PARTS, CardName.OBORO_CUSTOM_PARTS_D, MegamiEnum.OBORO, CardType.UNDEFINED, SubType.NONE)

    private val afterAttackDustDistance = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) {card_number, player, game_status, _->
        game_status.dustToDistance(1, Arrow.ONE_DIRECTION, player, game_status.getCardOwner(card_number), card_number)
        null
    }
    private val afterAttackDustBothDistance = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_TOKEN) {card_number, player, game_status, _->
        while(true){
            val nowCommand = game_status.receiveCardEffectSelect(player, NUMBER_PARTS_MOVE_TOKEN)
            if(selectDustToDistance(nowCommand, game_status, player,
                    game_status.getCardOwner(card_number), card_number)) break
        }
        null
    }
    private val afterAttackAddConcentration = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.CHANGE_CONCENTRATION) { _, player, game_status, _->
        game_status.addConcentration(player)
        null
    }

    private val customPartsDLv1Text = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
        while(true){
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_PARTS_D_LV_1)
            {_, _ -> true}?: break
            if(selected.size == 0){
                break
            }
            else if(selected.size == 1){
                game_status.popCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR, true)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.COVER_CARD, true)
                }
                break
            }
        }
        null
    }
    private val customPartsDLv2Text = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
        while(true){
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_PARTS_D_LV_2)
            {_, _ -> true}?: break
            if(selected.size == 0){
                break
            }
            else if(selected.size == 1){
                for(card_number in selected){
                    game_status.popCardFrom(player, card_number, LocationEnum.COVER_CARD, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                    }
                }
                break
            }
        }
        null
    }
    private val customPartsDLv3Text = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
        while(true){
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.COVER_CARD), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_PARTS_D_LV_3)
            {_, _ -> true}?: break
            if(selected.size == 0){
                break
            }
            else if(selected.size <= 2){
                for(card_number in selected){
                    game_status.popCardFrom(player, card_number, LocationEnum.COVER_CARD, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                    }
                }
                break
            }
        }
        null
    }
    private val customPartsDLv4Text = Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _->
        while(true){
            val selected = game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.COVER_CARD, LocationEnum.DISCARD_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_PARTS_D_LV_4)
            {card, from -> !(from == LocationEnum.DISCARD_YOUR && card.isSoftAttack) }?: break
            if(selected.size == 0){
                break
            }
            else if(selected.size <= 2){
                for(card_number in selected){
                    game_status.popCardFrom(player, card_number, LocationEnum.DISCARD_YOUR, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                    }?: game_status.popCardFrom(player, card_number, LocationEnum.COVER_CARD, true)?.let {
                        game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, true)
                    }
                }
                break
            }
        }
        null
    }


    private suspend fun assemblePart(reason: Int, player: PlayerEnum, game_status: GameStatus){
        game_status.selectCardFrom(player, player, player,
            listOf(LocationEnum.UNASSEMBLY_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, reason, 1)
        {_, _ -> true} ?.let { selected ->
            game_status.popCardFrom(player, selected[0], LocationEnum.UNASSEMBLY_YOUR, false)?.let{
                game_status.insertCardTo(player, it, LocationEnum.ASSEMBLY_YOUR, false)
            }
        }

        while(true){
            if(game_status.getPlayer(player).getAssemblyZoneSize() < 6){
                break
            }
            disassemblePart(NUMBER_DISASSEMBLE_REASON_OVER_CARD, player, game_status)
        }
    }

    private suspend fun disassemblePart(reason: Int, player: PlayerEnum, game_status: GameStatus){
        game_status.selectCardFrom(player, player, player,
            listOf(LocationEnum.ASSEMBLY_YOUR), CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, reason)
        {_, _ -> true} ?.let { selected ->
            for (card_number in selected){
                game_status.popCardFrom(player, card_number, LocationEnum.ASSEMBLY_YOUR, true)?.let{
                    game_status.insertCardTo(player, it, LocationEnum.UNASSEMBLY_YOUR, true)
                }
            }
        }
    }

    private fun oboroA2CardInit() {
        holoKunai.setAttack(DistanceType.DISCONTINUOUS, null, mutableListOf(1, 3, 5), 1, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        holoKunai.addText(installation)
        holoKunai.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MOVE_CARD) { _, player, game_status, _ ->
            assemblePart(NUMBER_OBORO_HOLOGRAM_KUNAI, player, game_status)
            null
        })
        holoKunai.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_AFTER_CARD_USE_AND_MOVE_DISCARD_CONDITION) { card_number, _, _, _ ->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.OBORO_HOLOGRAM_KUNAI)){
                1
            }
            else{
                null
            }
        })
        holoKunai.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.WHEN_AFTER_CARD_USE_AND_MOVE_DISCARD) { card_number, player, game_status, _ ->
            if(thisCardMoveTextCheck(card_number.toCardName(), CardName.OBORO_HOLOGRAM_KUNAI)){
                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_OBORO_HOLOGRAM_KUNAI)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.movePlayingCard(player, LocationEnum.COVER_CARD, card_number)
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


        gigasuke.setSpecial(16)
        gigasuke.setAttack(DistanceType.CONTINUOUS, Pair(3, 4), null, 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        gigasuke.addText(Text(TextEffectTimingTag.CONSTANT_EFFECT, TextEffectTag.COST_BUFF) { card_number, player, game_status, _->
            game_status.addThisTurnCostBuff(player, CostBuff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, card ->
                (card.card_data.card_name == CardName.OBORO_GIGASUKE)}, { cost, buffPlayer, gameStatus ->
                cost - gameStatus.getPlayer(buffPlayer).run {
                    getAssemblyZoneSize() + this.coverCard.size
                }
            }))
            null
        })
        gigasuke.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.MAKE_ATTACK) { card_number, player, game_status, _ ->
            for (i in 1..3){
                if(game_status.addPreAttackZone(
                        player, MadeAttack(CardName.OBORO_GIGASUKE,
                            NUMBER_OBORO_GIGASUKE_ADDITIONAL + i - 1, CardClass.NULL,
                            sortedSetOf(3, 4),2,  1,  MegamiEnum.OBORO,
                            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
                    )){
                    game_status.afterMakeAttack(card_number, player, null)
                }
            }
            null
        })


        electricsouchi.setSpecial(0)
        electricsouchi.addText(Text(TextEffectTimingTag.USING, TextEffectTag.DO_BASIC_OPERATION) { _, player, game_status, _->
            assemblePart(NUMBER_OBORO_ELECTRICSOUCHI_ASSEMBLE, player, game_status)
            game_status.requestAndDoBasicOperation(player, NUMBER_OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI)
            null
        })
        electricsouchi.addText(Text(TextEffectTimingTag.USED, TextEffectTag.WHEN_DECK_RECONSTRUCT_YOUR) { _, player, game_status, _->
            while(true){
                when(game_status.receiveCardEffectSelect(player, NUMBER_OBORO_BLURRY_DOCUMENT_ELECTRICSOUCHI)){
                    CommandEnum.SELECT_ONE -> {
                        val nowPlayer = game_status.getPlayer(player)
                        while(true){
                            val partsNumber = nowPlayer.getAssemblyZoneSize()
                            if(partsNumber <= 1){
                                break
                            }
                            disassemblePart(NUMBER_OBORO_ELECTRICSOUCHI_DISASSEMBLE, player, game_status)
                        }
                        val assembleNumber = nowPlayer.coverCard.size / 2 + nowPlayer.coverCard.size % 2
                        for (i in 1..assembleNumber){
                            assemblePart(NUMBER_OBORO_ELECTRICSOUCHI_ASSEMBLE, player, game_status)
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
        })


        mainPartsX.setAttack(DistanceType.CONTINUOUS, Pair(4, 5), null, 2, 2,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        mainPartsY.setAttack(DistanceType.DISCONTINUOUS, null, mutableListOf(3, 6), 2, 1,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)


        mainPartsZ.setAttack(DistanceType.CONTINUOUS, Pair(0, 2), null, 1, 0,
            cannotReactNormal = false, cannotReactSpecial = false, cannotReact = false, chogek = false)
        mainPartsZ.addText(Text(TextEffectTimingTag.AFTER_ATTACK, TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE) { _, player, game_status, _ ->
            game_status.setShrink(player.opposite())
            null
        })


        customPartsA.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_1) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ -> true},
                { _, _, attack -> attack.canNotReactEnchantment()
                })
            )
            null
        })
        customPartsA.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_2) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ -> true},
                { _, _, attack -> attack.canNotReactAttack()
                })
            )
            null
        })
        customPartsA.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_3) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ -> true},
                { _, _, attack -> attack.canNotReactBehavior()
                })
            )
            null
        })
        customPartsA.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_4) { card_number, player, game_status, _->
            game_status.addThisTurnOtherBuff(player, OtherBuff(card_number,1, OtherBuffTag.GET_IMMEDIATE, { _, _, _ -> true},
                { _, _, attack -> attack.canNotReactNormal()
                })
            )
            null
        })


        customPartsB.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_2) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, _, attack ->
                attack.lifePlusMinus(1)
            }))
            null
        })
        customPartsB.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_3) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, _, attack ->
                attack.auraPlusMinus(1)
            }))
            null
        })
        customPartsB.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_4) { card_number, player, game_status, _->
            game_status.addThisTurnAttackBuff(player, Buff(card_number, 1, BufTag.PLUS_MINUS_IMMEDIATE, {_, _, _ ->
                true
            }, {_, _, attack ->
                attack.auraPlusMinus(1); attack.lifePlusMinus(1)
            }))
            null
        })


        customPartsC.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_1) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(afterAttackDustDistance)
            null
        })
        customPartsC.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_2) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(afterAttackDustBothDistance)
            null
        })
        customPartsC.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_3) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(afterAttackDustBothDistance)
            null
        })
        customPartsC.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_4) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(afterAttackDustBothDistance)
            mainParts?.addTextAndReturn(afterAttackAddConcentration)
            null
        })


        customPartsD.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_1) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(customPartsDLv1Text)
            null
        })
        customPartsD.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_2) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(customPartsDLv2Text)
            null
        })
        customPartsD.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_3) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(customPartsDLv3Text)
            null
        })
        customPartsD.addText(Text(TextEffectTimingTag.USING, TextEffectTag.CUSTOM_PART_LV_4) { _, _, _, mainParts->
            mainParts?.addTextAndReturn(customPartsDLv4Text)
            null
        })
    }

    init {
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
        yatsuhaA2CardInit()
        akinaCardInit()
        shisuiCardInit()
        renriA1CardInit()
        misoraCardInit()
        oboroA2CardInit()

        v8hypen1CardInit()
        v8hypen2CardInit()
        v9CardInit()

        dataHashmapInit()
        hashMapTest()
    }
}

private val damageNotAttackSet = setOf(
    NUMBER_SHISUI_SHISUI, // laceration damage
    EventLog.CHOJO, EventLog.ACT_DAMAGE,

    NUMBER_SAINE_FLOWING_WALL,
    NUMBER_SHINRA_SAMRA_BAN_SHO,
    NUMBER_SHINRA_ZHEN_YEN,
    NUMBER_KURURU_BIG_GOLEM,
    NUMBER_KURURU_TORNADO,
    NUMBER_KURURU_ELEKITTEL,
    NUMBER_KURURU_ANALYZE,
    NUMBER_HATSUMI_WADANAKA_ROUTE,
    NUMBER_RURURARARI_ADDITIONAL,
    NUMBER_KAMUWI_LAMP,
    NUMBER_KAMUWI_GRAVEYARD
)

private val poisonSet = setOf(NUMBER_POISON_ANYTHING, NUMBER_POISON_PARALYTIC, NUMBER_POISON_HALLUCINOGENIC,
    NUMBER_POISON_RELAXATION, NUMBER_POISON_DEADLY_1, NUMBER_POISON_DEADLY_2,
    SECOND_PLAYER_START_NUMBER + NUMBER_POISON_PARALYTIC, SECOND_PLAYER_START_NUMBER + NUMBER_POISON_HALLUCINOGENIC,
    SECOND_PLAYER_START_NUMBER + NUMBER_POISON_RELAXATION, SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_1,
    SECOND_PLAYER_START_NUMBER + NUMBER_POISON_DEADLY_2)

fun Int.isPoison() = this in poisonSet

private val soldierSet = setOf(NUMBER_SOLDIER_HORSE, NUMBER_SOLDIER_SHIELD, NUMBER_SOLDIER_SPEAR_1,
    NUMBER_SOLDIER_SPEAR_2, SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_HORSE,
    SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SHIELD, SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_1,
    SECOND_PLAYER_START_NUMBER + NUMBER_SOLDIER_SPEAR_2)

fun Int.isSoldier() = this in soldierSet

private val perjureSet = setOf(
    NUMBER_RENRI_FALSE_STAB, NUMBER_RENRI_TEMPORARY_EXPEDIENT, NUMBER_RENRI_BLACK_AND_WHITE,
    NUMBER_RENRI_FLOATING_CLOUDS, NUMBER_RENRI_FISHING, SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FALSE_STAB,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_TEMPORARY_EXPEDIENT,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_BLACK_AND_WHITE,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FLOATING_CLOUDS,
    SECOND_PLAYER_START_NUMBER + NUMBER_RENRI_FISHING)

fun Int.isPerjure() = this in perjureSet

private val customPartsSet = setOf(
    NUMBER_OBORO_CUSTOM_PARTS_A, NUMBER_OBORO_CUSTOM_PARTS_B, NUMBER_OBORO_CUSTOM_PARTS_C, NUMBER_OBORO_CUSTOM_PARTS_D,
    NUMBER_OBORO_MAIN_PARTS_X, NUMBER_OBORO_MAIN_PARTS_Y, NUMBER_OBORO_MAIN_PARTS_Z,
    SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_A,
    SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_B,
    SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_C,
    SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_CUSTOM_PARTS_D,
    SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MAIN_PARTS_X,
    SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MAIN_PARTS_Y,
    SECOND_PLAYER_START_NUMBER + NUMBER_OBORO_MAIN_PARTS_Z,
)

fun Int.isParts() = this in customPartsSet

fun Int.toPrivate(): Int{
    return if(this.isPoison()){
        NUMBER_POISON_ANYTHING
    } else if(this.isSoldier()){
        NUMBER_SOLDIER_ANYTHING
    } else if(this.isParts()){
        NUMBER_PARTS_ANYTHING
    } else{
        NUMBER_CARD_UNAME
    }
}

fun Int.toFirstPlayerCardNumber() = if (this > SECOND_PLAYER_START_NUMBER) {
    this - SECOND_PLAYER_START_NUMBER
}
else {
    this
}

