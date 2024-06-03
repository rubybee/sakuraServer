package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.SakuraBaseData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class HatsumiTest: ApplicationTest() {
    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.HATSUMI_WATER_BALL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_WATER_CURRENT, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_STRONG_ACID, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_TSUNAMI, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.HATSUMI_JUN_BI_MAN_TAN, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.HATSUMI_COMPASS, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_CALL_WAVE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_ISANA_HAIL, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_OYOGIBI_FIRE, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_MIOBIKI_ROUTE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)

        cardTypeTest(CardName.HATSUMI_TORPEDO, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.HATSUMI_SAGIRI_HAIL, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.REACTION)
        cardTypeTest(CardName.HATSUMI_WADANAKA_ROUTE, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
    }

    @Test
    fun waterBallTailWindTest() = runTest{
        resetValue(0, 1, 10, 10, 3, 12)
        gameStatus.player1.isThisTurnTailWind = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_BALL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_BALL, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun waterBallHeadWindTest() = runTest{
        resetValue(0, 0, 10, 10, 3, 12)
        gameStatus.player1.isThisTurnTailWind = false

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_BALL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_BALL, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun waterCurrentTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 12)
        gameStatus.player1.isThisTurnTailWind = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_CURRENT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_CURRENT, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun waterCurrentFullPowerTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 12)
        gameStatus.player1.isThisTurnTailWind = true
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_CURRENT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_CURRENT, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.flare)
        assertEquals(6, gameStatus.distanceToken)
    }

    @Test
    fun strongAcidTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 12)
        gameStatus.player1.isThisTurnTailWind = false

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_STRONG_ACID, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_STRONG_ACID, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun tsunamiTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 10)
        gameStatus.player1.isThisTurnTailWind = false
        gameStatus.player2.flare = 1

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_TSUNAMI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_TSUNAMI, LocationEnum.HAND)

        assertEquals(5, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun junBiManTenTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 10)
        gameStatus.player1.fullAction = true
        gameStatus.player1.isThisTurnTailWind = false

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_BALL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_CURRENT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_STRONG_ACID, LocationEnum.DECK)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_JUN_BI_MAN_TAN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_JUN_BI_MAN_TAN, LocationEnum.HAND)

        assertEquals(3, gameStatus.player1.aura)
        assertEquals(3, gameStatus.player1.hand.size)

        player1Connection.putReceiveData(makeData(CommandEnum.COVER_CARD_SELECT, mutableListOf(
            NUMBER_HATSUMI_WATER_BALL
        )))

        gameStatus.endPhase()

        assertEquals(3, gameStatus.player1.hand.size)
    }

    @Test
    fun compassTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 1)

        addCard(PlayerEnum.PLAYER2, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_HURUBEGI, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_STRONG_ACID, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_CURRENT, LocationEnum.DECK)

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_COMPASS, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_COMPASS, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)

        startPhase()

        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun callWaveTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 10)
        gameStatus.player1.isThisTurnTailWind = false

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_CURRENT, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WATER_BALL, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_COMPASS, LocationEnum.DECK)

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_CALL_WAVE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_CALL_WAVE, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HATSUMI_WATER_CURRENT
        )))
        startPhase()

        assertEquals(2, gameStatus.player1.hand.size)

        gameStatus.player1.isThisTurnTailWind = true
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HATSUMI_WATER_BALL
        )))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.SELECT_ENCHANTMENT_END))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.DECK_RECONSTRUCT_NO))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
        addReactData(PlayerEnum.PLAYER2)
        gameStatus.startPhase()

        assertEquals(4, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player2.aura)
    }

    @Test
    fun isanaHailTailWindTest() = runTest{
        resetValue(0, 2, 10, 10, 3, 12)
        gameStatus.player1.isThisTurnTailWind = true
        gameStatus.player1.flare = 4

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_ISANA_HAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_ISANA_HAIL, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun isanaHailHeadWindTest() = runTest{
        resetValue(0, 2, 10, 10, 3, 12)
        gameStatus.player1.isThisTurnTailWind = false
        gameStatus.player1.flare = 4

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_ISANA_HAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_ISANA_HAIL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun oyogibiFireTest() = runTest {
        suspend fun returnTest() {
            gameStatus.endPhase()
            startPhase()

            player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
            gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_GO_FORWARD, -1)
            gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_GO_FORWARD, -1)

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HATSUMI_OYOGIBI_FIRE, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 1, 10, 10, 5, 12)
        gameStatus.player1.flare = 2

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_OYOGIBI_FIRE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_OYOGIBI_FIRE, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(8, gameStatus.player2.life)
        returnTest()
    }

    @Test
    fun kirahariLighthouseTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 12)
        gameStatus.player1.flare = 1
        gameStatus.player1.isThisTurnTailWind = true

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, LocationEnum.SPECIAL_CARD)

        assertEquals(true, gameStatus.player1.shrink)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(3, gameStatus.getAdjustSwellDistance())

        gameStatus.endPhase()

        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_TSUNAMI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.HATSUMI_TSUNAMI, LocationEnum.HAND)

        assertEquals(4, gameStatus.getAdjustDistance())
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun miobikiRouteTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 12)
        gameStatus.player1.flare = 2
        gameStatus.player1.isThisTurnTailWind = false

        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_WATER_CURRENT, LocationEnum.DECK)

        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_MIOBIKI_ROUTE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_MIOBIKI_ROUTE, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.HATSUMI_WATER_CURRENT, LocationEnum.DISCARD_YOUR))

        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_WATER_BALL, LocationEnum.DECK)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        startPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.HATSUMI_WATER_BALL, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun torpedoTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_TORPEDO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_TORPEDO, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)

        addReactData(PlayerEnum.PLAYER1)
        addCard(PlayerEnum.PLAYER2, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player1.life)

        gameStatus.endPhase(); startPhase()
        assertEquals(true, gameStatus.player1.isThisTurnTailWind)
    }

    @Test
    fun sagiriHailTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 10)
        gameStatus.player2.flare = 3

        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_SAGIRI_HAIL, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.HATSUMI_SAGIRI_HAIL, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_COMPASS, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_COMPASS, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(10, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun wadanakaRouteTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 10)
        gameStatus.player1.flare = 2; gameStatus.turnPlayer = PlayerEnum.PLAYER2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WADANAKA_ROUTE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_WADANAKA_ROUTE, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(true, gameStatus.player1.forwardDiving)
        assertEquals(true, gameStatus.player2.shrink)

        gameStatus.player1.flare = 1
        addCard(PlayerEnum.PLAYER1, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_TSUNAMI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.HATSUMI_TSUNAMI, LocationEnum.HAND)

        assertEquals(true, gameStatus.player1.forwardDiving)
    }
}