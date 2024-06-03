package com.sakurageto

import com.sakurageto.card.*
import com.sakurageto.card.basicenum.*
import com.sakurageto.gamelogic.megamispecial.Stratagem
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class EdgeCaseTest : ApplicationTest() {
    @Test
    fun answerQuestionTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 0)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_QUESTION_ANSWER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_QUESTION_ANSWER, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(2, gameStatus.distanceToken)
        assertEquals(0, gameStatus.dust)
        assertEquals(0, gameStatus.player1.flare)
    }

    @Test
    fun ahumTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 2)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_AHUM, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_MOOEMBUCK, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_POBARAM, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_POBARAM, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(false, gameStatus.canDoBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_WIND_AROUND))
    }

    @Test
    fun kanZaDoTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 5)
        gameStatus.player1.flare = 5
        gameStatus.player1.fullAction = true

        for(i in 1..5){
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        }

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_GRAND_GULLIVER, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_KANZA_DO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_KANZA_DO, LocationEnum.SPECIAL_CARD)

        assertEquals(5, gameStatus.player1.flare)
        assertEquals(5, gameStatus.player1.aura)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun gwonyuckTest() = runTest {
        resetValue(0, 0, 10, 10, 10, 5)

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_GWONYUCK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_GWONYUCK, LocationEnum.HAND)

        startPhase()

        assertEquals(2, gameStatus.player1.enchantmentCard[NUMBER_SAINE_GWONYUCK]?.getNap())
    }

    @Test
    fun lacerationMooembuckTest() = runTest {
        resetValue(5, 0, 10, 10, 10, 5)
        gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER2)[INDEX_LACERATION_AURA] = 10
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_FOUR))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_AURA_DAMAGE_PLACE, mutableListOf(
            0, 4, 206, 4
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_AURA_DAMAGE_PLACE, mutableListOf(
            0, 5, 206, 5
        )))

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_MOOEMBUCK, LocationEnum.ENCHANTMENT_ZONE)
        gameStatus.processAllLacerationDamage(PlayerEnum.PLAYER2)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.SAINE_MOOEMBUCK, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun flowingWallTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 5)

        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_FLOWING_WALL, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun backDraftTest() = runTest {
        resetValue(5, 2, 10, 10, 3, 11)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKSTEP, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKSTEP, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKSTEP, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKSTEP, LocationEnum.HAND)

        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_HURUBEGI, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER2, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKDRAFT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKDRAFT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)


        assertEquals(8, gameStatus.player2.life)

        resetValue(5, 2, 10, 10, 5, 11)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun passingFearTest() = runTest {
        resetValue(2, 2, 10, 10, 2, 11)
        gameStatus.player2.concentration = 2

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)

        assertEquals(2, gameStatus.gameLogger.findGetDamageByThisAttack(NUMBER_TOKOYO_PASSING_FEAR).first)

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)

        assertEquals(1, gameStatus.gameLogger.findGetDamageByThisAttack(NUMBER_TOKOYO_PASSING_FEAR).second)
    }

    @Test
    fun tobikageSoundOfIceTest() = runTest {
        resetValue(2, 2, 10, 10, 3, 4)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_SOUND_OF_ICE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.aura)
    }

    @Test
    fun tobikageOnlyReactTest() = runTest {
        resetValue(2, 2, 10, 10, 4, 4)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_CUT_DOWN, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(2, gameStatus.player2.aura)
    }

    @Test
    fun ambushBanlonTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 4)
        gameStatus.player1.flare = 4
        gameStatus.player2.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SHINRA_BANLON
        )))

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_BANLON, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER2, CardName.OBORO_AMBUSH, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.OBORO_AMBUSH, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun iblonTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 4)

        addCard(PlayerEnum.PLAYER2, CardName.OBORO_WIRE, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER2, CardName.POISON_DEADLY_1, LocationEnum.DECK)

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.normalCardDeck.size)
    }

    @Test
    fun banlonTest() = runTest {
        resetValue(0, 0, 4, 4, 3, 4)

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_CHEONJI_BANBAG, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(4, gameStatus.player2.life)
    }

    @Test
    fun inyongBitsunerigiTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)
        gameStatus.player1.concentration = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_TOKOYO_BITSUNERIGI + SECOND_PLAYER_START_NUMBER
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.TOKOYO_BITSUNERIGI, LocationEnum.DECK))
    }

    @Test
    fun inyongRelaxPoisonTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)
        gameStatus.player1.concentration = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_TOKOYO_BITSUNERIGI + SECOND_PLAYER_START_NUMBER
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.POISON_RELAXATION, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND))
    }

    @Test
    fun inyongYukihiTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 4)
        gameStatus.player1.concentration = 2
        gameStatus.player2.umbrella = Umbrella.FOLD

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE + SECOND_PLAYER_START_NUMBER
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2,
            CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun sadoTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 4)
        gameStatus.player1.flare = 4
        gameStatus.player2.fullAction = true
        gameStatus.player2.stratagem = Stratagem.SHIN_SAN

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_HEO_WI, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.UTSURO_HEO_WI, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_SA_DO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.SHINRA_SA_DO, LocationEnum.HAND)

        assertEquals(2, gameStatus.player2.enchantmentCard[NUMBER_SHINRA_SA_DO + SECOND_PLAYER_START_NUMBER]?.getNap())
    }

    @Test
    fun grandSkyHoleCrashTest() = runTest {
        resetValue(0, 5, 10, 10, 5, 4)
        gameStatus.startTurnDistance = 0
        gameStatus.player1.flare = 4
        gameStatus.player2.umbrella = Umbrella.UNFOLD

        addCard(PlayerEnum.PLAYER2, CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player2.aura)
    }

    @Test
    fun xyChangeTest() = runTest {
        resetValue(0, 3, 10, 10, 5, 4)
        gameStatus.startTurnDistance = 0
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_CHEONJI_BANBAG, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player2.aura)
    }

    @Test
    fun xyChangeTest2() = runTest {
        resetValue(0, 1, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 10; gameStatus.player1.windGauge = 10

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_CHEONJI_BANBAG, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM_SURGE_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM_SURGE_ATTACK, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.life)
    }

    @Test
    fun softAttackReturnDeckTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)
        gameStatus.player1.concentration = 2
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SOFT_MATERIAL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SOFT_MATERIAL, LocationEnum.SPECIAL_CARD)
        gameStatus.drawCard(PlayerEnum.PLAYER1, 1)
        gameStatus.player1.endTurn = false
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun softAttackBlackWaveTest() = runTest {
        resetValue(1, 0, 10, 10, 4, 0)
        gameStatus.player1.concentration = 2
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SOFT_MATERIAL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SOFT_MATERIAL, LocationEnum.SPECIAL_CARD)
        gameStatus.drawCard(PlayerEnum.PLAYER1, 1)
        gameStatus.player1.endTurn = false

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_BLACK_WAVE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.UTSURO_BLACK_WAVE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.HAND))
    }

    @Test
    fun trickUmbrellaInyongTest() = runTest {
        resetValue(0, 0, 10, 10, 6, 4)

        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_TRICK_UMBRELLA, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.HAGANE_BONFIRE, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_CHIKAGE_TRICK_UMBRELLA + SECOND_PLAYER_START_NUMBER
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun regainerAdditionalCostTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 4)
        gameStatus.player2.flare = 1

        addCard(PlayerEnum.PLAYER2, CardName.KAMUWI_KATA_SHIRO, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.KAMUWI_KATA_SHIRO, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_BURNING_STEAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_BURNING_STEAM, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_THALLYA_WAVING_EDGE
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FIVE))

        gameStatus.player1.fullAction = true
        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_HAN_KI_POISON, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun shieldChargeNoEmptyPlaceTest() = runTest {
        resetValue(0, 0, 10, 10, 9, 4)
        gameStatus.thisTurnDistanceChangeValue = -8
        gameStatus.player1.megamiOne = MegamiEnum.THALLYA
        MegamiEnum.THALLYA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_SHIELD_CHARGE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun circularCircuitTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)
        gameStatus.player1.thunderGauge = 0; gameStatus.player1.windGauge = 0

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_CIRCULAR_CIRCUIT, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER2, CardName.HIMIKA_SMOKE, LocationEnum.ENCHANTMENT_ZONE)

        player1Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        player1Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.SAINE_DOUBLEBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.SAINE_DOUBLEBEGI, LocationEnum.HAND)

        assertEquals(2, gameStatus.player1.thunderGauge)
        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun harvestCanNotMoveTokenTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)
        gameStatus.turnPlayer = PlayerEnum.PLAYER2

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_SOUND_OF_SUN, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_HARVEST, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.UTSURO_HARVEST, LocationEnum.HAND)

        assertEquals(2, gameStatus.player1.enchantmentCard[NUMBER_TOKOYO_SOUND_OF_SUN]?.getNap())
    }

    @Test
    fun chestWillingDrainDevilTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 4)
        gameStatus.player1.flare = 5

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_HAND_FLOWER, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DRAIN_DEVIL, LocationEnum.YOUR_USED_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_CHEST_WILLINGNESS, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_CHEST_WILLINGNESS, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)
    }

    @Test
    fun fullBloomCanNotMoveTokenTest() = runTest {
        resetValue(4, 0, 10, 10, 2, 4)
        gameStatus.player1.freezeToken = 1

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FULL_BLOOM_PATH, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_COMMAND, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SPIRIT_SIK, LocationEnum.DECK)

        startPhase()

        assertEquals(5, gameStatus.player1.enchantmentCard[NUMBER_HONOKA_FULL_BLOOM_PATH]?.getNap())
    }

    @Test
    fun revolvingBladeTobikageTest() = runTest {
        resetValue(4, 0, 10, 10, 2, 4)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_SOUND_OF_ICE, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.KORUNU_REVOLVING_BLADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.KORUNU_REVOLVING_BLADE, LocationEnum.HAND)

        assertEquals(6, gameStatus.getAdjustDistance())
    }

    @Test
    fun lifeLoseCardWinTest() = runTest {
        resetValue(0, 4, 1, 3, 2, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_MIRROR_DEVIL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_MIRROR_DEVIL, LocationEnum.SPECIAL_CARD)

        assertEquals(true, gameStatus.gameEnd)
    }

    @Test
    fun contractTest() = runTest {
        resetValue(0, 0, 1, 3, 2, 5)
        gameStatus.player2.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_COMMAND, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CONTRACT, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CONTRACT, LocationEnum.SPECIAL_CARD)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_END_PHASE_EFFECT_ORDER, NUMBER_HONOKA_COMMAND))
        gameStatus.endPhase()

        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun fourLeafJeolChangJeolWhaTest() = runTest {
        resetValue(4, 0, 10, 10, 2, 4)
        gameStatus.player1.flare = 4
        gameStatus.player2.flare = 1

        addCard(PlayerEnum.PLAYER2, CardName.SAINE_JEOL_CHANG_JEOL_HWA, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_JEOL_CHANG_JEOL_HWA, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_FOUR_LEAP_SONG, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_FOUR_LEAP_SONG, LocationEnum.SPECIAL_CARD)

        assertEquals(false, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.ENCHANTMENT_ZONE))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun eightSongMirrorInyongTest() = runTest {
        resetValue(4, 0, 10, 10, 4, 4)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE, LocationEnum.ENCHANTMENT_ZONE)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_STAR_NAIL
        )))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_INYONG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.SHINRA_INYONG, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(1, gameStatus.player2.aura)
        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun eightSongMirrorSakuraBlizzardTest() = runTest {
        resetValue(1, 0, 10, 10, 4, 4)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE, LocationEnum.ENCHANTMENT_ZONE)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_BLIZZARD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_BLIZZARD, LocationEnum.HAND)

        assertEquals(5, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun loseReactGetReactTest() = runTest {
        resetValue(1, 0, 10, 10, 4, 4)
        gameStatus.player2.concentration = 2

        addCard(PlayerEnum.PLAYER2, CardName.SOLDIER_HORSE, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun gongSumTest() = runTest {
        resetValue(1, 2, 10, 10, 4, 4)

        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_GONG_SUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_GONG_SUM, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun impromptuTobikageTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 6)
        gameStatus.player1.flare = 4
        gameStatus.player1.concentration = 2

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMPROMPTU, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.KORUNU_REVOLVING_BLADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.KORUNU_REVOLVING_BLADE, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)
        assertEquals(8, gameStatus.getAdjustDistance())
    }

    @Test
    fun bloodWaveMoveTokenButNotMoveTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 10)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_BLOOD_WAVE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_BLOOD_WAVE, LocationEnum.HAND)

        gameStatus.player2.aura = 5

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WINDSTAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WINDSTAGE, LocationEnum.HAND)

        assertEquals(2, gameStatus.player1.enchantmentCard[NUMBER_KAMUWI_BLOOD_WAVE]?.getNap())
        assertEquals(3, gameStatus.distanceToken)
    }

    @Test
    fun dawnBangByeogTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 2)
        gameStatus.player1.flare = 6

        addCard(PlayerEnum.PLAYER2, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.MIZUKI_BANG_BYEOG, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(false, gameStatus.player2.endTurn)
    }

    @Test
    fun orireterareruUsedCardReuseTest() = runTest {
        resetValue(5, 1, 10, 10, 3, 5)
        gameStatus.player1.flare = 4

        gameStatus.player1.megamiOne = MegamiEnum.RENRI
        MegamiEnum.RENRI.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)

        gameStatus.player1.unselectedCard.add(CardName.RENRI_FALSE_STAB)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_TEMPORARY_EXPEDIENT)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_FISHING)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_BLACK_AND_WHITE)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_IRRITATING_GESTURE)

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RENRI_FISHING
        )))
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RENRI_FISHING
        )))
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.flare)
    }

    @Test
    fun deceptionFogTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)
        gameStatus.player1.megamiOne = MegamiEnum.SAINE_A2
        gameStatus.player1.megamiTwo = MegamiEnum.OBORO
        gameStatus.player2.megamiOne = MegamiEnum.RENRI_A1
        gameStatus.player2.megamiTwo = MegamiEnum.OBORO
        gameStatus.player1.flare = 5; gameStatus.player2.flare = 5

        //use deceptionFog()
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SAINE_DOUBLEBEGI
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addReactData(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SAINE_DOUBLEBEGI
        )))
        addReactData(PlayerEnum.PLAYER2, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SAINE_DOUBLEBEGI + SECOND_PLAYER_START_NUMBER
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_LIFE))
        addReactData(PlayerEnum.PLAYER1, CardName.SAINE_JEOL_CHANG_JEOL_HWA, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_FLOWING_WALL, LocationEnum.ENCHANTMENT_ZONE)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_JEOL_CHANG_JEOL_HWA, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_DOUBLEBEGI, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_DOUBLEBEGI, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)

        assertEquals(9, gameStatus.player1.life)
        assertEquals(0, gameStatus.player1.usingCard.size)
        assertEquals(0, gameStatus.player2.usingCard.size)
    }

    @Test
    fun deceptionFogSealTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 4)
        gameStatus.player1.canNotUseCardName1 = Pair(NUMBER_KANAWE_SEAL, CardName.YURINA_CHAM)
        gameStatus.player1.canNotUseCardName2 = Pair(NUMBER_KANAWE_SEAL, CardName.RENRI_DECEPTION_FOG)
        gameStatus.player2.megamiOne = MegamiEnum.YURINA; gameStatus.player2.megamiTwo = MegamiEnum.KANAWE
        gameStatus.player2.nowAct = StoryBoard.getActByNumber(12)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_ILSUM
        )))
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun upalaTearsBothDamageTest() = runTest {
        resetValue(1, 1, 10, 10, 3, 2)
        gameStatus.player1.flare = 5

        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_UPALA_TEAR, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_UPALA_TEAR, LocationEnum.SPECIAL_CARD)

        gameStatus.distanceToken = 1
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_CRIMSONZERO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_CRIMSONZERO, LocationEnum.SPECIAL_CARD)

        assertEquals(3, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_LIFE])
        assertEquals(2, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_AURA])
    }

    @Test
    fun misoraTrackingAttack() = runTest {
        resetValue(0, 4, 10, 10, 8, 1)
        gameStatus.player1.concentration = 2
        gameStatus.player1.aiming = 4
        gameStatus.player1.unselectedCard.add(CardName.TOKOYO_BITSUNERIGI)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_TOKOYO_BITSUNERIGI
        )))

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(false, CardName.TOKOYO_BITSUNERIGI in gameStatus.player1.unselectedCard)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.DISCARD_YOUR))
        assertEquals(1, gameStatus.player1.discard.size)
        assertEquals(0, gameStatus.player1.normalCardDeck.size)
        assertEquals(0, gameStatus.player1.hand.size)
        assertEquals(0, gameStatus.player1.coverCard.size)
    }

    @Test
    fun tobikageMisoraTrackingAttack() = runTest {
        resetValue(0, 4, 10, 10, 4, 1)
        gameStatus.player1.flare = 4
        gameStatus.player1.concentration = 2
        gameStatus.player1.aiming = 4
        gameStatus.player1.unselectedCard.add(CardName.TOKOYO_BITSUNERIGI)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_MISORA_TRACKING_ATTACK
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_TOKOYO_BITSUNERIGI
        )))

        addCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.OBORO_TOBIKAGE, LocationEnum.SPECIAL_CARD)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(false, CardName.TOKOYO_BITSUNERIGI in gameStatus.player1.unselectedCard)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.MISORA_TRACKING_ATTACK, LocationEnum.DISCARD_YOUR))
        assertEquals(1, gameStatus.player1.discard.size)
        assertEquals(0, gameStatus.player1.normalCardDeck.size)
        assertEquals(0, gameStatus.player1.hand.size)
        assertEquals(0, gameStatus.player1.coverCard.size)
    }
}