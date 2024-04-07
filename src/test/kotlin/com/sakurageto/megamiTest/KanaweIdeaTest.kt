package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class KanaweIdeaTest: ApplicationTest() {
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

    suspend fun readyIdea(cardName: CardName, flipped: Boolean) {
        resetValue(0, 0, 10, 10, 3, 2)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        player1Connection.putReceiveData(makeData(
            CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            cardName.toCardNumber(true)
        )))
        if(flipped){
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        }
        else{
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        }

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_PUBLISH, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_PUBLISH, LocationEnum.HAND)
    }

    @Test
    fun saljinTest() = runTest {
        readyIdea(CardName.IDEA_SAL_JIN, false)
        resetValue(0, 0, 10, 10, 4, 2)

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_SHADOW_WALL, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.UTSURO_SHADOW_WALL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun saljinFlippedTest() = runTest {
        readyIdea(CardName.IDEA_SAL_JIN, true)
        resetValue(0, 0, 10, 10, 3, 2)

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_SHADOW_WALL, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.UTSURO_SHADOW_WALL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun sakuraWaveTest() = runTest {
        readyIdea(CardName.IDEA_SAKURA_WAVE, false)
        resetValue(0, 3, 10, 10, 4, 2)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ACT, 1))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD, -1))
        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun sakuraWaveFlippedTest() = runTest {
        readyIdea(CardName.IDEA_SAKURA_WAVE, true)
        resetValue(0, 3, 10, 10, 4, 2)
        gameStatus.player1.flare = 8

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ACT, 1))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD, -1))
        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun whistleTest() = runTest {
        readyIdea(CardName.IDEA_WHISTLE, false)
        resetValue(0, 0, 10, 10, 4, 2)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun whistleFlippedTest() = runTest {
        readyIdea(CardName.IDEA_WHISTLE, true)
        resetValue(0, 0, 10, 10, 3, 2)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ACT, 1))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD, -1))
        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun myeongJeonTest() = runTest {
        readyIdea(CardName.IDEA_MYEONG_JEON, false)
        resetValue(0, 0, 10, 10, 3, 2)

        addCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KANAWE_IMAGE, LocationEnum.HAND)

        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun myeongJeonFlippedTest() = runTest {
        readyIdea(CardName.IDEA_MYEONG_JEON, true)
        resetValue(0, 0, 10, 10, 6, 2)

        addCard(PlayerEnum.PLAYER1, CardName.HAGANE_BONFIRE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HAGANE_BONFIRE, LocationEnum.HAND)

        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun emphasizingTest() = runTest {
        readyIdea(CardName.IDEA_EMPHASIZING, false)
        resetValue(0, 0, 10, 10, 4, 2)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ACT, 1))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD, -1))
        gameStatus.endPhase()
        assertEquals(0, gameStatus.player1.ideaCardStage)

        startPhase()

        gameStatus.player1.fullAction = true
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)

        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

    @Test
    fun positioningFlippedTest() = runTest {
        readyIdea(CardName.IDEA_POSITIONING, true)
        resetValue(0, 0, 10, 10, 10, 2)
        gameStatus.startTurnDistance = 10

        for(i in 1..5){
            gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GO_FORWARD, -1)
        }

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ACT, 1))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD, -1))
        gameStatus.endPhase()
        assertEquals(1, gameStatus.player1.ideaCardStage)
    }

}