package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.card.basicenum.MegamiEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class N8HaganeTest: ApplicationTest() {
    private fun makeCentrifugal(){
        gameStatus.startTurnDistance = gameStatus.distanceToken
        gameStatus.distanceToken += 2
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.HAGANE_CENTRIFUGAL_ATTACK, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HAGANE_FOUR_WINDED_EARTHQUAKE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GROUND_BREAKING, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        cardTypeTest(CardName.HAGANE_HYPER_RECOIL, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_WON_MU_RUYN, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_RING_A_BELL, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GRAVITATION_FIELD, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GRAND_SKY_HOLE_CRASH, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GRAND_BELL_MEGALOBEL, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GRAND_GRAVITATION_ATTRACT, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)

        cardTypeTest(CardName.HAGANE_BONFIRE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_WHEEL_SKILL, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_GRAND_SOFT_MATERIAL, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HAGANE_SOFT_ATTACK, CardClass.NORMAL, CardType.UNDEFINED, SubType.UNDEFINED)
    }

    @Test
    fun centrifugalAttackTest() = runTest {
        resetValue(0, 4, 10, 10, 0, 0)
        makeCentrifugal()
        gameStatus.player1.concentration = 1

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.concentration)
        assertEquals(1, gameStatus.player1.coverCard.size)
        assertEquals(1, gameStatus.player2.coverCard.size)
        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun fourWindedEarthquakeTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 0)
        gameStatus.startTurnDistance = 4

        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GO_FORWARD, -1)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GO_FORWARD, -1)
        addCard(PlayerEnum.PLAYER2, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_FOUR_WINDED_EARTHQUAKE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_FOUR_WINDED_EARTHQUAKE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(0, gameStatus.player2.hand.size)
        assertEquals(1, gameStatus.player2.discard.size)
    }

    @Test
    fun groundBreakingTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 0)
        gameStatus.player1.fullAction = true; gameStatus.player2.concentration = 2

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GROUND_BREAKING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GROUND_BREAKING, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(0, gameStatus.player2.concentration)
    }

    @Test
    fun hyperRecoilUp4Test() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_HYPER_RECOIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_HYPER_RECOIL, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun hyperRecoilUnder5Test() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)
        gameStatus.player2.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_HYPER_RECOIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_HYPER_RECOIL, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun wonMuRyunTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)
        gameStatus.player2.flare = 3
        makeCentrifugal()

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_WON_MU_RUYN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_WON_MU_RUYN, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.flare)
        assertEquals(2, gameStatus.player1.aura)
    }

    @Test
    fun ringABellFirstTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)
        makeCentrifugal()

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)

        assertEquals(6, gameStatus.player2.life)
    }

    @Test
    fun ringABellSecondTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)
        makeCentrifugal()

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_GANPA, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_GANPA, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun gravitationFieldTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAVITATION_FIELD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAVITATION_FIELD, LocationEnum.HAND)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GO_FORWARD, -1)

        assertEquals(1, gameStatus.distanceToken)
    }

    @Test
    fun grandSkyHoleCrashTest() = runTest {
        resetValue(0, 5, 10, 10, 2, 2)
        makeCentrifugal()
        gameStatus.distanceToken = 8
        gameStatus.player1.flare = 4

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SKY_HOLE_CRASH, LocationEnum.SPECIAL_CARD)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun grandBellMegalobelTest() = runTest {
        resetValue(0, 0, 8, 10, 2, 2)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_BELL_MEGALOBEL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_BELL_MEGALOBEL, LocationEnum.SPECIAL_CARD)

        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun grandGravityAttractTest() = runTest {
        suspend fun returnTest(){
            gameStatus.endPhase(); startPhase()
            gameStatus.endPhase(); startPhase()
            startPhase(); makeCentrifugal()

            addCard(PlayerEnum.PLAYER1, CardName.HAGANE_WON_MU_RUYN, LocationEnum.HAND)
            useCard(PlayerEnum.PLAYER1, CardName.HAGANE_WON_MU_RUYN, LocationEnum.HAND)

            gameStatus.endPhase()
            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_GRAVITATION_ATTRACT, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 0, 8, 10, 5, 2)
        gameStatus.player1.flare = 5

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_GRAVITATION_ATTRACT, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_GRAVITATION_ATTRACT, LocationEnum.SPECIAL_CARD)

        assertEquals(2, gameStatus.distanceToken)
        assertEquals(3, gameStatus.player1.flare)

        returnTest()
    }

    @Test
    fun grandMountainTest() = runTest {
        resetValue(0, 4, 8, 10, 5, 2)
        gameStatus.player1.flare = 4; makeCentrifugal()

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_RING_A_BELL, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_CENTRIFUGAL_ATTACK, LocationEnum.DISCARD_YOUR)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HAGANE_RING_A_BELL, NUMBER_HAGANE_CENTRIFUGAL_ATTACK
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_MOUNTAIN_RESPECT, LocationEnum.SPECIAL_CARD)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun bonfireTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_BONFIRE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_BONFIRE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(1, gameStatus.player2.flare)
        assertEquals(3, gameStatus.distanceToken)
    }

    @Test
    fun wheelSkillTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)
        makeCentrifugal()

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_BONFIRE, LocationEnum.YOUR_DECK_BELOW)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_WHEEL_SKILL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_WHEEL_SKILL, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.hand.size)
        assertEquals(1, gameStatus.player1.concentration)
    }

    @Test
    fun grandSoftMaterialTest() = runTest {
        suspend fun softAttackTest(){
            gameStatus.endPhase(); startPhase()
            gameStatus.endPhase(); startPhase()

            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
                NUMBER_OBORO_MAIN_PARTS_X)))
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

            useCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.HAND)

            assertEquals(9, gameStatus.player2.life)
        }

        suspend fun returnTest(){
            gameStatus.endPhase()

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SOFT_MATERIAL, LocationEnum.SPECIAL_CARD))
        }

        resetValue(2, 3, 10, 10, 6, 0)
        MegamiEnum.OBORO_A2.settingForAnother(PlayerEnum.PLAYER1, gameStatus)
        gameStatus.player1.flare = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_OBORO_HOLOGRAM_KUNAI)))

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_HOLOGRAM_KUNAI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SOFT_MATERIAL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_GRAND_SOFT_MATERIAL, LocationEnum.SPECIAL_CARD)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HAGANE_SOFT_ATTACK, LocationEnum.DECK))

        softAttackTest()

        returnTest()
    }
}