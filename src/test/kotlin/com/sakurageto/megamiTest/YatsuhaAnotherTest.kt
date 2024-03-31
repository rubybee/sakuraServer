package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class YatsuhaAnotherTest: ApplicationTest() {
    @Test
    fun holyRakeHandTest() = runTest{
        cardTypeTest(CardName.YATSUHA_HOLY_RAKE_HANDS, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        resetValue(0, 1, 10, 10, 4, 12)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.flare)
        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun entranceOfAbyssTest() = runTest {
        cardTypeTest(CardName.YATSUHA_ENTRANCE_OF_ABYSS, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        resetValue(4, 4, 10, 10, 4, 12)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.HAND)

        assertEquals(6, gameStatus.player2.life)
    }

    @Test
    fun trueMonsterTest() = runTest {
        cardTypeTest(CardName.YATSUHA_TRUE_MONSTER, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        resetValue(3, 3, 9, 9, 3, 12)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_TRUE_MONSTER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_TRUE_MONSTER, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun ghostLinkTest() = runTest {
        cardTypeTest(CardName.YATSUHA_GHOST_LINK, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        resetValue(0, 1, 10, 10, 3, 12)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_GHOST_LINK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_GHOST_LINK, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(4, gameStatus.getAdjustDistance())
        assertEquals(3, gameStatus.getAdjustSwellDistance())
    }

    @Test
    fun resolutionTest() = runTest {
        cardTypeTest(CardName.YATSUHA_RESOLUTION, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        resetValue(1, 0, 9, 10, 4, 12)

        addCard(PlayerEnum.PLAYER2, CardName.YATSUHA_RESOLUTION, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.YATSUHA_RESOLUTION, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun pledgeTest() = runTest {
        cardTypeTest(CardName.YATSUHA_PLEDGE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        resetValue(0, 1, 9, 10, 4, 12)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_PLEDGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_PLEDGE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(1, gameStatus.player1.flare)
    }

    @Test
    fun pledgeTwoTest() = runTest {
        resetValue(0, 1, 9, 10, 4, 12)
        gameStatus.player2.flare = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_PLEDGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_PLEDGE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun vainFlowerTest() = runTest {
        cardTypeTest(CardName.YATSUHA_VAIN_FLOWER, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        resetValue(0, 0, 9, 10, 4, 0)

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_STAR_NAIL
        )))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_VAIN_FLOWER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_VAIN_FLOWER, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.HAND))
    }

    @Test
    fun vainFlowerFourTest() = runTest {
        resetValue(0, 0, 9, 10, 4, 0)

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CLINGY_FLOWER, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_DARKNESS_GILL, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CONTRACT, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_STAR_NAIL
        )))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_VAIN_FLOWER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_VAIN_FLOWER, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun eightMirrorVainSakuraTest() = runTest {
        cardTypeTest(CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        resetValue(0, 0, 9, 10, 4, 0)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_STAR_NAIL
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_DARKNESS_GILL
        )))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_DARKNESS_GILL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_EIGHT_MIRROR_VAIN_SAKURA, LocationEnum.SPECIAL_CARD)

        assertEquals(true, gameStatus.player1.shrink)

        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, false)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.HAND))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.HAND))
    }

    @Test
    fun unfamiliarWorldTest() = runTest {
        cardTypeTest(CardName.YATSUHA_UNFAMILIAR_WORLD, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        resetValue(0, 0, 10, 10, 4, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_COLORED_WORLD, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_UNFAMILIAR_WORLD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_UNFAMILIAR_WORLD, LocationEnum.HAND)

        assertEquals(2, gameStatus.player1.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_COLORED_WORLD, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun coloredWorldTest() = runTest {
        cardTypeTest(CardName.YATSUHA_COLORED_WORLD, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        resetValue(5, 3, 10, 10, 4, 0)
        MegamiEnum.YATSUHA_AA1.settingForAnother(PlayerEnum.PLAYER1, gameStatus)
        gameStatus.player1.flare = 2; gameStatus.player1.megamiOne = MegamiEnum.YATSUHA_AA1

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_COLORED_WORLD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_COLORED_WORLD, LocationEnum.HAND)

        startPhase()

        assertEquals(2, gameStatus.player1.concentration)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.ENCHANTMENT_ZONE)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addReactData(PlayerEnum.PLAYER2)
        startPhase()

        assertEquals(0, gameStatus.player2.aura)

        startPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_CONTRACT, LocationEnum.DECK)
        startPhase()

        assertEquals(4, gameStatus.player1.hand.size)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_STAR_NAIL
        )))
        startPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.HAND))
        assertEquals(3, gameStatus.player1.memory?.size)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun shesCherryBlossomWorldTest() = runTest {
        cardTypeTest(CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        resetValue(5, 3, 10, 10, 4, 0)
        MegamiEnum.YATSUHA_AA1.settingForAnother(PlayerEnum.PLAYER1, gameStatus)
        gameStatus.player1.megamiOne = MegamiEnum.YATSUHA_AA1

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_STAR_NAIL, LocationEnum.MEMORY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_DARKNESS_GILL, LocationEnum.MEMORY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_GHOST_STEP, LocationEnum.MEMORY_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.MEMORY_YOUR)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_DARKNESS_GILL
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SHES_CHERRY_BLOSSOM_WORLD, LocationEnum.SPECIAL_CARD)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.HAND))

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_STAR_NAIL
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        gameStatus.drawCard(PlayerEnum.PLAYER1, 1)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_HOLY_RAKE_HANDS, LocationEnum.HAND))

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, true)

        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun shesEgoAndDeterminationTest() = runTest {
        suspend fun reactTest() {
            resetValue(0, 0, 4, 10, 0, 0)
            gameStatus.player2.fullAction = true
            gameStatus.player2.flare = 5

            addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

            addCard(PlayerEnum.PLAYER2, CardName.HIMIKA_EN_TEN_HIMIKA, LocationEnum.SPECIAL_CARD)
            useCard(PlayerEnum.PLAYER2, CardName.HIMIKA_EN_TEN_HIMIKA, LocationEnum.SPECIAL_CARD)

            assertEquals(4, gameStatus.player1.life)
        }

        cardTypeTest(CardName.YATSUHA_SHES_EGO_AND_DETERMINATION, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        resetValue(5, 3, 10, 10, 4, 0)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_DARKNESS_GILL, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YATSUHA_DARKNESS_GILL
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SHES_EGO_AND_DETERMINATION, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SHES_EGO_AND_DETERMINATION, LocationEnum.SPECIAL_CARD)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YATSUHA_ENTRANCE_OF_ABYSS, LocationEnum.DECK))

        reactTest()
    }


}