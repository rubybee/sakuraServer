package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class YatsuhaOriginTest: ApplicationTest() {
    @Test
    fun starNailTest() = runTest{
        cardTypeTest(CardName.YATSUHA_STAR_NAIL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        resetValue(1, 2, 10, 10, 4, 12)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(3, gameStatus.player2.flare)
        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun darknessGillTest() = runTest{
        cardTypeTest(CardName.YATSUHA_DARKNESS_GILL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        resetValue(2, 2, 10, 10, 4, 12)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_DARKNESS_GILL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_DARKNESS_GILL, LocationEnum.HAND)

        assertEquals(6, gameStatus.player2.life)
        assertEquals(true, gameStatus.player1.shrink)
    }

    @Test
    fun mirrorDevilTest() = runTest {
        cardTypeTest(CardName.YATSUHA_MIRROR_DEVIL, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        resetValue(0, 4, 10, 10, 3, 12)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_MIRROR_DEVIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_MIRROR_DEVIL, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(9, gameStatus.player1.life)
    }

    @Test
    fun ghostStepTest() = runTest {
        cardTypeTest(CardName.YATSUHA_GHOST_STEP, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        resetValue(0, 4, 10, 10, 3, 12)

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_GHOST_STEP, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_GHOST_STEP, LocationEnum.HAND)

        assertEquals(4, gameStatus.getAdjustDistance())
        assertEquals(3, gameStatus.getAdjustSwellDistance())
        assertEquals(1, gameStatus.player1.concentration)
    }

    @Test
    fun willingTest() = runTest {
        cardTypeTest(CardName.YATSUHA_WILLING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        resetValue(1, 1, 10, 10, 3, 12)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_WILLING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_WILLING, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(1, gameStatus.player2.flare)
        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun contractTest() = runTest {
        cardTypeTest(CardName.YATSUHA_CONTRACT, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        resetValue(0, 0, 10, 10, 3, 12)
        gameStatus.player2.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CONTRACT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CONTRACT, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.flare)

        gameStatus.endPhase()

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player2.flare)
    }

    @Test
    fun clingyFlowerTest() = runTest {
        cardTypeTest(CardName.YATSUHA_CLINGY_FLOWER, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        resetValue(2, 2, 10, 10, 1, 12)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CLINGY_FLOWER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CLINGY_FLOWER, LocationEnum.HAND)

        assertEquals(4, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)
    }

    @Test
    fun twoLeafMirrorDivineTest() = runTest {
        cardTypeTest(CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        resetValue(0, 0, 7, 6, 4, 12)
        gameStatus.player2.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.YATSUHA_TWO_LEAP_MIRROR_DIVINE, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(4, gameStatus.player1.life)
        assertEquals(6, gameStatus.player2.life)
    }

    @Test
    fun fourLeafSongTest() = runTest {
        cardTypeTest(CardName.YATSUHA_FOUR_LEAP_SONG, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        resetValue(4, 0, 10, 10, 3, 12)
        gameStatus.player1.flare = 2
        addCard(PlayerEnum.PLAYER2, CardName.YATSUHA_CLINGY_FLOWER, LocationEnum.ENCHANTMENT_ZONE)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            SECOND_PLAYER_START_NUMBER + NUMBER_YATSUHA_CLINGY_FLOWER
        )))
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_FOUR_LEAP_SONG, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_FOUR_LEAP_SONG, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(4, gameStatus.player1.aura)
    }

    @Test
    fun sixStarSeaTest() = runTest {
        cardTypeTest(CardName.YATSUHA_SIX_STAR_SEA, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        resetValue(5, 5, 10, 10, 4, 12)
        gameStatus.player1.flare = 5

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SIX_STAR_SEA, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SIX_STAR_SEA, LocationEnum.SPECIAL_CARD)

        assertEquals(6, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.flare)
    }

    @Test
    fun eightMirrorOtherSideTest() = runTest {
        cardTypeTest(CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        resetValue(0, 0, 9, 10, 3, 12)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_EIGHT_MIRROR_OTHER_SIDE, LocationEnum.SPECIAL_CARD)
        gameStatus.endPhase()

        gameStatus.player1.fullAction = true
        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_MIRROR_DEVIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_MIRROR_DEVIL, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)
    }
}