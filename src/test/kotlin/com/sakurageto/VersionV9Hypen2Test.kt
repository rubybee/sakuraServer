package com.sakurageto

import com.sakurageto.card.*
import com.sakurageto.card.basicenum.*
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.gamelogic.log.GameLog
import com.sakurageto.gamelogic.log.LogEnum
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.SakuraBaseData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class VersionV9Hypen2Test: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.version = GameVersion.VERSION_9_2
    }

    @Test
    fun cardTypeTest() {
        cardTypeTest(CardName.YURINA_GIBACK, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_TURN_UMBRELLA, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.THALLYA_WAVING_EDGE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.THALLYA_THALLYA_MASTERPIECE, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.RAIRA_HOWLING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.HONOKA_ASSAULT_SPIRIT_SIK, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RENRI_SIN_SOO, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.AKINA_THREAT, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
    }

    @Test
    fun gibackTest() = runTest {
        resetValue(0, 0, 8, 9, 4, 2)

        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        assertEquals(gameStatus.player1.aura, 2)

        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_LIFE, 0))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun turnUmbrellaTest() = runTest {
        gameStatus.player1.megamiOne = MegamiEnum.YUKIHI
        MegamiEnum.YUKIHI.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
        resetValue(0, 0, 10, 10, 5, 5)

        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_TURN_UMBRELLA, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        gameStatus.endPhase()

        assertEquals(1, gameStatus.player1.aura)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_TURN_UMBRELLA, LocationEnum.HAND)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(Umbrella.FOLD, gameStatus.player1.umbrella)
    }

    @Test
    fun grandMountainTest() = runTest {
        resetValue(0, 4, 8, 10, 5, 2)
        gameStatus.startTurnDistance = gameStatus.distanceToken
        gameStatus.distanceToken += 2
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DECK)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HAGANE_RING_A_BELL, NUMBER_HAGANE_CENTRIFUGAL_ATTACK
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(7, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.normalCardDeck.size)
        assertEquals(0, gameStatus.player2.normalCardDeck.size)

        gameStatus.endPhase()

        assertEquals(3, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun centrifugalTest() = runTest {
        resetValue(0, 4, 8, 10, 2, 2)
        gameStatus.startTurnDistance = gameStatus.distanceToken
        gameStatus.distanceToken += 2
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)

        gameStatus.endCurrentPhase = false
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)

        gameStatus.gameLogger.insert(GameLog(PlayerEnum.PLAYER1, LogEnum.START_PHASE, -1, -1))

        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        assertEquals(4, gameStatus.player2.life)
    }

    @Test
    fun centrifugalBeforeVersionTest() = runTest {
        gameStatus.version = GameVersion.VERSION_9_1
        resetValue(0, 4, 8, 10, 2, 2)
        gameStatus.startTurnDistance = gameStatus.distanceToken
        gameStatus.distanceToken += 2
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)

        gameStatus.endCurrentPhase = false
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)

        gameStatus.gameLogger.insert(GameLog(PlayerEnum.PLAYER1, LogEnum.START_PHASE, -1, -1))

        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun wavingEdgeTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 4)
        gameStatus.player1.megamiOne = MegamiEnum.THALLYA
        MegamiEnum.THALLYA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(5, gameStatus.getAdjustDistance())
        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun masterPieceTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 4)
        gameStatus.player1.megamiOne = MegamiEnum.THALLYA
        MegamiEnum.THALLYA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
        gameStatus.player1.artificialTokenBurn = 2
        gameStatus.player1.artificialToken = 3
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_THALLYA_MASTERPIECE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_THALLYA_MASTERPIECE, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)

        assertEquals(3, gameStatus.player1.artificialToken)
        assertEquals(1, gameStatus.player1.artificialTokenBurn)
    }

    @Test
    fun howlingOneTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.windGauge = 0; gameStatus.player1.thunderGauge = 6; gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_HOWLING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_HOWLING, LocationEnum.HAND)

        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(7, gameStatus.player1.thunderGauge)
        assertEquals(1, gameStatus.player1.windGauge)
        assertEquals(1, gameStatus.player1.hand.size)
        assertEquals(3, gameStatus.player2.aura)
    }

    @Test
    fun howlingTwoTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 10; gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_HOWLING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_HOWLING, LocationEnum.HAND)

        assertEquals(false, gameStatus.player2.shrink)
        assertEquals(20, gameStatus.player1.thunderGauge)
        assertEquals(1, gameStatus.player1.hand.size)
        assertEquals(3, gameStatus.player2.aura)
    }

    @Test
    fun assaultSikTest() = runTest {
        resetValue(0, 2, 9, 10, 5, 1)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_DIVINE_OUKA, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(9, gameStatus.player1.life)
    }

    @Test
    fun assaultSikChangeTest() = runTest {
        resetValue(0, 2, 9, 10, 5, 1)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_DIVINE_OUKA, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_DIVINE_OUKA, LocationEnum.DECK))
    }

    @Test
    fun sinsooTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 0)
        gameStatus.player1.megamiOne = MegamiEnum.RENRI_A1
        MegamiEnum.RENRI_A1.settingForAnother(PlayerEnum.PLAYER1, gameStatus)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RENRI_FALSE_WEAPON
        )))

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_SIN_SOO, LocationEnum.DISCARD_YOUR)
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, true)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_WEAPON, LocationEnum.DECK))
    }

    @Test
    fun threatTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)
        gameStatus.player1.flow = 1

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }
}