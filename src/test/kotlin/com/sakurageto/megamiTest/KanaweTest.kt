package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.card.basicenum.MegamiEnum
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class KanaweTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.KANAWE
        MegamiEnum.KANAWE.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)

        gameStatus.player1.additionalHand[CardName.IDEA_SAL_JIN] =
            Card.cardMakerByName(true, CardName.IDEA_SAL_JIN, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9)
        gameStatus.player1.additionalHand[CardName.IDEA_SAKURA_WAVE] =
            Card.cardMakerByName(true, CardName.IDEA_SAKURA_WAVE, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9)
        gameStatus.player1.additionalHand[CardName.IDEA_WHISTLE] =
            Card.cardMakerByName(true, CardName.IDEA_WHISTLE, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9)
        gameStatus.player1.additionalHand[CardName.IDEA_MYEONG_JEON] =
            Card.cardMakerByName(true, CardName.IDEA_MYEONG_JEON, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9)
        gameStatus.player1.additionalHand[CardName.IDEA_EMPHASIZING] =
            Card.cardMakerByName(true, CardName.IDEA_EMPHASIZING, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9)
        gameStatus.player1.additionalHand[CardName.IDEA_POSITIONING] =
            Card.cardMakerByName(true, CardName.IDEA_POSITIONING, PlayerEnum.PLAYER1, LocationEnum.ADDITIONAL_CARD, GameVersion.VERSION_9)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.KANAWE_IMAGE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KANAWE_SCREENPLAY, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KANAWE_PRODUCTION, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KANAWE_PUBLISH, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KANAWE_AFTERGLOW, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KANAWE_IMPROMPTU, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.KANAWE_SEAL, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.KANAWE_VAGUE_STORY, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KANAWE_INFINITE_STARLIGHT, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KANAWE_BEND_OVER_THIS_NIGHT, CardClass.SPECIAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.KANAWE_DISTANT_SKY, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
    }

    @Test
    fun imageTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 1)
        gameStatus.player1.nowAct = StoryBoard.getActByNumber(7)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMAGE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun screenPlayTest() = runTest {
        suspend fun returnTest() {
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            gameStatus.endPhase()

            assertEquals(1, gameStatus.player2.concentration)
            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KANAWE_SCREENPLAY, LocationEnum.DECK))
        }

        resetValue(0, 0, 10, 10, 8, 1)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_IDEA_SAL_JIN
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_SCREENPLAY, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_SCREENPLAY, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(NUMBER_IDEA_SAL_JIN, gameStatus.player1.ideaCard?.card_number)
        assertEquals(false, gameStatus.player1.isIdeaCardFlipped)

        returnTest()
    }

    @Test
    fun productionTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 1)
        gameStatus.player1.nowAct = StoryBoard.getActByNumber(9)

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_PRODUCTION, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_PRODUCTION, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(false, gameStatus.player1.canIdeaProcess)
    }

    @Test
    fun publishTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_IDEA_SAKURA_WAVE
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_PUBLISH, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_PUBLISH, LocationEnum.HAND)

        assertEquals(2, gameStatus.distanceToken)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(NUMBER_IDEA_SAKURA_WAVE, gameStatus.player1.ideaCard?.card_number)
        assertEquals(true, gameStatus.player1.isIdeaCardFlipped)
    }

    @Test
    fun afterGlowTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 1)
        gameStatus.player1.nowAct = StoryBoard.getActByNumber(2)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_AFTERGLOW, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_AFTERGLOW, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.concentration)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.DECK))
    }

    @Test
    fun impromptuTest() = runTest {
        resetValue(0, 1, 10, 10, 6, 1)
        gameStatus.player1.nowAct = StoryBoard.getActByNumber(2)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HIMIKA_RAPIDFIRE
        )))

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_RAPIDFIRE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMPROMPTU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMPROMPTU, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HIMIKA_RAPIDFIRE, LocationEnum.HAND))
    }

    @Test
    fun sealTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 3)
        gameStatus.player2.megamiOne = MegamiEnum.YURINA; gameStatus.player2.megamiTwo = MegamiEnum.SAINE
        gameStatus.turnPlayer = PlayerEnum.PLAYER2
        gameStatus.player2.flare = 8

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_WOLYUNGNACK
        )))

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_SEAL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_SEAL, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)

        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun vagueStoryTest() = runTest {
        suspend fun returnTest() {
            gameStatus.player2.concentration = 0

            gameStatus.endPhase()
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            startPhase()

            assertEquals(2, gameStatus.player2.concentration)
            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KANAWE_VAGUE_STORY, LocationEnum.SPECIAL_CARD))
        }
        resetValue(0, 0, 10, 10, 4, 3)
        gameStatus.player1.flare = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_IDEA_SAL_JIN
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_VAGUE_STORY, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_VAGUE_STORY, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(NUMBER_IDEA_SAL_JIN, gameStatus.player1.ideaCard?.card_number)

        returnTest()
    }

    @Test
    fun infiniteStarLightTest() = runTest {
        resetValue(0, 0, 10, 10, 0, 0)
        gameStatus.player1.flare = 2
        gameStatus.player1.nowAct = StoryBoard.getActByNumber(3)

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_INFINITE_STARLIGHT, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_INFINITE_STARLIGHT, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun infiniteStarLightReturnTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 1)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_IDEA_WHISTLE
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_SCREENPLAY, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_SCREENPLAY, LocationEnum.HAND)
        gameStatus.player1.ideaCardStage = 1
        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_INFINITE_STARLIGHT, LocationEnum.YOUR_USED_CARD)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMAGE, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_END_PHASE_EFFECT_ORDER, NUMBER_KANAWE_SCREENPLAY))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ACT, 1))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        gameStatus.endPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KANAWE_INFINITE_STARLIGHT, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun bendOverThisNightTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_BEND_OVER_THIS_NIGHT, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.KANAWE_BEND_OVER_THIS_NIGHT, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(10, gameStatus.player1.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.DECK))
    }

    @Test
    fun distantSkyTest() = runTest {
        gameStatus.player1.unselectedCard.add(CardName.KANAWE_IMAGE)
        gameStatus.player1.unselectedSpecialCard.add(CardName.KANAWE_BEND_OVER_THIS_NIGHT)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1,CardName.KANAWE_SEAL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_DISTANT_SKY, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_DISTANT_SKY, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMAGE, LocationEnum.HAND))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KANAWE_BEND_OVER_THIS_NIGHT, LocationEnum.SPECIAL_CARD))
    }
}