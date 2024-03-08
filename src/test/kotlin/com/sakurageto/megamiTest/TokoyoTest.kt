package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class TokoyoTest: ApplicationTest() {
    fun setKyoChi(playerEnum: PlayerEnum){
        gameStatus.getPlayer(playerEnum).concentration = 2
    }

    @Test
    fun bitsunerigiTest() = runTest{
        resetValue(0, 0, 10, 10, 4, 4)
        setKyoChi(PlayerEnum.PLAYER1)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertNotEquals(null, getCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.DECK))
    }

    @Test
    fun wooahhantaguckTest() = runTest {
        resetValue(1, 1, 10, 10, 4, 4)
        setKyoChi(PlayerEnum.PLAYER2)

        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)

        assertEquals(9, gameStatus.player1.life)
        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun runningRabbitTest() = runTest {
        resetValue(1, 1, 10, 10, 4, 4)

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RUNNING_RABBIT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RUNNING_RABBIT, LocationEnum.HAND)
        assertEquals(4, gameStatus.distanceToken)

        gameStatus.distanceToken = 3
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RUNNING_RABBIT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RUNNING_RABBIT, LocationEnum.HAND)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun poetDanceFirstEffectTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)
        gameStatus.player2.flare = 1

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_POETDANCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.TOKOYO_POETDANCE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.concentration)
        assertEquals(1, gameStatus.player2.aura)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun poetDanceSecondEffectTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 4)

        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_POETDANCE, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_POETDANCE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(5, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(1, gameStatus.player2.concentration)
    }

    @Test
    fun flipFanTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_FLIPFAN, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_POETDANCE, LocationEnum.COVER_CARD)

        player1Connection.putReceiveData(makeData(PlayerEnum.PLAYER1, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            mutableListOf(CardName.TOKOYO_BITSUNERIGI, CardName.TOKOYO_POETDANCE)))
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_FLIPFAN, LocationEnum.HAND)

        assertEquals(CardName.TOKOYO_BITSUNERIGI, getCard(PlayerEnum.PLAYER1, CardName.CARD_UNNAME, LocationEnum.YOUR_DECK_TOP)?.card_data?.card_name)
        assertNotEquals(null, getCard(PlayerEnum.PLAYER1, CardName.TOKOYO_POETDANCE, LocationEnum.DECK))
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player1.discard.size)
        assertEquals(0, gameStatus.player1.coverCard.size)
    }

    @Test
    fun windStageTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 2)))
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WINDSTAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WINDSTAGE, LocationEnum.HAND)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(3, gameStatus.distanceToken)

        gameStatus.endPhase(); startPhase()
        gameStatus.endPhase(); startPhase()

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun sunStageTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 2)))
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_SUNSTAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_SUNSTAGE, LocationEnum.HAND)

        assertEquals(true, gameStatus.player1.endTurn)
        assertEquals(2, gameStatus.player1.concentration)

        gameStatus.endPhase(); startPhase()
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        gameStatus.endPhase(); startPhase()

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun kuonTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 5)
        gameStatus.player2.flare = 5

        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_KUON, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_KUON, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)

        assertEquals(9, gameStatus.player1.life)
        assertEquals(10, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun thousandBirdTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 5)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_SUNSTAGE, LocationEnum.COVER_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_THOUSANDBIRD, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_THOUSANDBIRD, LocationEnum.SPECIAL_CARD)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(2, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun endlessWindTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)
        gameStatus.player1.flare = 1; setKyoChi(PlayerEnum.PLAYER1)

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_RUNNING_RABBIT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_ENDLESSWIND, LocationEnum.SPECIAL_CARD)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        player2Connection.putReceiveData(makeData(PlayerEnum.PLAYER2, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            CardName.TOKOYO_RUNNING_RABBIT)))
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_ENDLESSWIND, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.hand.size)
    }

    @Test
    fun tokoyoMoonTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)
        gameStatus.player1.flare = 2; gameStatus.player2.concentration = 2

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_TOKOYOMOON, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_TOKOYOMOON, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player2.concentration)
        assertEquals(2, gameStatus.player1.concentration)
        assertEquals(true, gameStatus.player2.shrink)
    }

    @Test
    fun flowingPlayTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)
        gameStatus.player2.flare = 5

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_TOKOYOMOON, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_JONGGEK, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_FLOWING_PLAY, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_KUON, LocationEnum.SPECIAL_CARD)

        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_KUON, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_FLOWING_PLAY, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)
        assertEquals(6, gameStatus.player2.flare)
        assertEquals(1, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun ensembleTokoyoTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 3)
        setKyoChi(PlayerEnum.PLAYER1)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAI_TOKO_ENSEMBLE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_KUON, LocationEnum.YOUR_USED_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SAI_TOKO_ENSEMBLE, LocationEnum.HAND)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun duetChiTanYangMyeongTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 3)
        gameStatus.player1.flare = 1; setKyoChi(PlayerEnum.PLAYER1)

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_DUET_CHI_TAN_YANG_MYEONG, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)

        gameStatus.endPhase(); startPhase()

        addReactData(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(PlayerEnum.PLAYER1, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            mutableListOf(CardName.TOKOYO_BITSUNERIGI)))
        addReactData(PlayerEnum.PLAYER1, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_FLOWING_PLAY, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.TOKOYO_FLOWING_PLAY, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.specialCardDeck.size)
        assertEquals(1, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun passingFearAttackTest() = runTest {
        resetValue(2, 2, 10, 10, 2, 2)

        player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_LIFE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun passingFearReactTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 4)
        setKyoChi(PlayerEnum.PLAYER2)

        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_LIFE))
        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_PASSING_FEAR, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(8, gameStatus.player1.life)
        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun relicEyeTest() = runTest {
        resetValue(1, 1, 10, 10, 3, 3)
        gameStatus.player2.flare = 1; gameStatus.player1.flare = 1; gameStatus.player2.concentration = 1

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RELIC_EYE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RELIC_EYE, LocationEnum.SPECIAL_CARD)

        assertEquals(2, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(0, gameStatus.player2.flare)

        gameStatus.endPhase()

        assertEquals(1, gameStatus.player1.specialCardDeck.size)
    }

    @Test
    fun eightSakuraVeinTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_EIGHT_SAKURA_IN_VAIN, LocationEnum.SPECIAL_CARD)

        assertEquals(5, gameStatus.player1.aura)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 2)))
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WINDSTAGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WINDSTAGE, LocationEnum.HAND)

        assertEquals(7, gameStatus.player1.aura)
    }
}