package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class HimikaTest: ApplicationTest() {
    private suspend fun useTwoCard(){
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_GANPA, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_GANPA, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.HIMIKA_SHOOT, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_RAPIDFIRE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_MAGNUMCANON, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_FULLBURST, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        cardTypeTest(CardName.HIMIKA_BACKSTEP, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_BACKDRAFT, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_SMOKE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_REDBULLET, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_CRIMSONZERO, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_SCARLETIMAGINE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_BURMILIONFIELD, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)

        cardTypeTest(CardName.HIMIKA_FIRE_WAVE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_SAT_SUI, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.HIMIKA_EN_TEN_HIMIKA, CardClass.SPECIAL, CardType.ATTACK, SubType.FULL_POWER)
    }

    @Test
    fun fullBurstTest() = runTest {
        resetValue(0, 3, 10, 10, 9, 0)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_FULLBURST, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_FULLBURST, LocationEnum.HAND)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun backStepTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKSTEP, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.YOUR_DECK_TOP)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKSTEP, LocationEnum.HAND)
        assertEquals(1, gameStatus.player1.hand.size)
        assertEquals(6, gameStatus.distanceToken)
        assertEquals(4, gameStatus.dust)
    }

    @Test
    fun smokeTest() = runTest {
        resetValue(0, 3, 10, 10, 5, 3)

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SMOKE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_GANPA, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 3)))
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SMOKE, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_GANPA, LocationEnum.HAND)
        assertEquals(0, gameStatus.dust)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun backDraftTest() = runTest{
        useTwoCard()
        resetValue(0, 3, 10, 10, 5, 5)

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKDRAFT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BACKDRAFT, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun crimsonZeroTest() = runTest {
        resetValue(0, 2, 10, 10, 0, 5)
        gameStatus.player1.flare = 5; gameStatus.player2.flare = 2

        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_CRIMSONZERO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_CRIMSONZERO, LocationEnum.SPECIAL_CARD)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.flare)
    }

    @Test
    fun scarletImagineTest() = runTest {
        resetValue(0, 2, 10, 10, 0, 5)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_MAGNUMCANON, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SCARLETIMAGINE, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(PlayerEnum.PLAYER1, CommandEnum.COVER_CARD_SELECT, CardName.HIMIKA_SHOOT))
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SCARLETIMAGINE, LocationEnum.SPECIAL_CARD)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(1, gameStatus.player1.hand.size)
        assertEquals(0, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun burmilionfieldTest() = runTest {
        useTwoCard()
        resetValue(0, 2, 10, 10, 5, 5)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BURMILIONFIELD, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BURMILIONFIELD, LocationEnum.SPECIAL_CARD)
        gameStatus.endPhase()
        assertEquals(7, gameStatus.distanceToken)
        assertEquals(5, gameStatus.dust)
        assertEquals(0, gameStatus.player1.flare)
        assertNotEquals(null, getCard(PlayerEnum.PLAYER1, CardName.HIMIKA_BURMILIONFIELD, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun fireWaveTest() = runTest {
        useTwoCard()
        resetValue(0, 1, 10, 10, 3, 5)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_FIRE_WAVE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_FIRE_WAVE, LocationEnum.HAND)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun satSuiTest() = runTest {
        resetValue(0, 2, 10, 10, 0, 0)

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SAT_SUI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SAT_SUI, LocationEnum.HAND)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(2, gameStatus.dust)
    }

    @Test
    fun enTenTest() = runTest {
        resetValue(0, 4, 10, 10, 3, 4)
        gameStatus.player1.flare = 5; gameStatus.player1.fullAction = true
        gameStatus.player2.flare = 2

        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_EN_TEN_HIMIKA, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_EN_TEN_HIMIKA, LocationEnum.SPECIAL_CARD)
        assertEquals(5, gameStatus.player2.life)
        assertEquals(true, gameStatus.gameEnd)
    }
}