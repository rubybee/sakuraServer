package com.sakurageto

import com.sakurageto.card.*
import com.sakurageto.card.basicenum.*
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class VersionV10Test: ApplicationTest() {
    @Before
    fun setting() {
        gameStatus.version = GameVersion.VERSION_10
    }

    @Test
    fun cardTypeTest() {
        cardTypeTest(CardName.SHINRA_BANLON, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RAIRA_HOWLING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.AKINA_THREAT, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.AKINA_CALC, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.AKINA_TURN_OFF_TABLE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.SHISUI_IRON_RESISTANCE, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        cardTypeTest(CardName.THALLYA_WAVING_EDGE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
    }

    @Test
    fun banlonTest() = runTest {
        resetValue(1, 0, 10, 10, 3, 0)

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player1.hand.size)
    }

    @Test
    fun grandMountainOneTest() = runTest {
        resetValue(0, 4, 8, 10, 5, 2)
        gameStatus.startTurnDistance = gameStatus.distanceToken
        gameStatus.distanceToken += 2
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DECK)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(3, gameStatus.player1.discard.size)
    }

    @Test
    fun grandMountainTwoTest() = runTest {
        resetValue(0, 4, 8, 10, 5, 2)
        gameStatus.startTurnDistance = gameStatus.distanceToken
        gameStatus.distanceToken += 2
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DECK)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HAGANE_RING_A_BELL
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HAGANE_CENTRIFUGAL_ATTACK
        )))

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(7, gameStatus.player2.life)
        assertEquals(2, gameStatus.player1.coverCard.size)
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
        assertEquals(false, gameStatus.gameLogger.checkThisTurnDoAttack(PlayerEnum.PLAYER1))
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
        assertEquals(false, gameStatus.gameLogger.checkThisTurnDoAttack(PlayerEnum.PLAYER1))
    }

    @Test
    fun threatTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)
        gameStatus.player1.flow = 1

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun threatDamageTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)
        gameStatus.player1.flow = 3

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun calcTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 2)

        addCard(PlayerEnum.PLAYER1, CardName.AKINA_CALC, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_CALC, LocationEnum.HAND)
        assertEquals(0, gameStatus.player1.concentration)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(8, gameStatus.player2.life)

        addReactData(PlayerEnum.PLAYER1)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(8, gameStatus.player1.life)
    }

    @Test
    fun turnOffTableTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)
        gameStatus.player2.flare = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_TURN_OFF_TABLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_TURN_OFF_TABLE, LocationEnum.HAND)

        assertEquals(5, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player2.flare)

        startPhase()
        assertEquals(5, gameStatus.distanceToken)
        assertEquals(1, gameStatus.player2.flare)

        startPhase()
        assertEquals(5, gameStatus.distanceToken)
        assertEquals(2, gameStatus.player2.flare)
    }

    @Test
    fun ironResistanceTest() = runTest {
        resetValue(0, 1, 10, 10, 1, 0)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_IRON_RESISTANCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_IRON_RESISTANCE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_AURA])
        assertEquals(0, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_LIFE])
    }

    @Test
    fun wavingEdgeTest() = runTest {
        gameStatus.player1.megamiOne = MegamiEnum.THALLYA
        MegamiEnum.THALLYA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
        resetValue(0, 2, 10, 10, 3, 4)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.THALLYA_WAVING_EDGE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(4, gameStatus.getAdjustDistance())
    }
}