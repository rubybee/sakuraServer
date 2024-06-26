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
import kotlin.test.assertNotEquals

class N10KururuAnotherTest: ApplicationTest() {
    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.KURURU_ANALYZE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KURURU_DAUZING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KURURU_LAST_RESEARCH, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KURURU_GRAND_GULLIVER, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)

        cardTypeTest(CardName.KURURU_BLASTER, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KURURU_RAILGUN, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KURURU_CONNECT_DIVE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
    }

    @Test
    fun analyzeAttackTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 3)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.COVER_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ANALYZE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_ANALYZE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun analyzeNotAttackTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 3)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ANALYZE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_ANALYZE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.hand.size)
    }

    @Test
    fun dauzingTest() = runTest {
        resetValue(0, 3, 10, 10, 3, 3)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GUHAB, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.YOUR_DECK_TOP)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_GUHAB + SECOND_PLAYER_START_NUMBER
        )))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DAUZING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_DAUZING, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.normalCardDeck.size)
    }

    @Test
    fun lastResearchTest() = runTest {
        resetValue(0, 3, 10, 10, 3, 3)
        gameStatus.player2.megamiOne = MegamiEnum.YURINA; gameStatus.player2.megamiTwo = MegamiEnum.SAINE
        gameStatus.player1.flare = 3
        gameStatus.player2.unselectedSpecialCard = mutableSetOf(CardName.YURINA_WOLYUNGNACK, CardName.YURINA_POBARAM)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_GRAND_GULLIVER, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_APDO, LocationEnum.COVER_CARD)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_APDO
        )))

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_LAST_RESEARCH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_LAST_RESEARCH, LocationEnum.SPECIAL_CARD)
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, false)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.COVER_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_GIBACK
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        gameStatus.endPhase()

        assertEquals(3, gameStatus.player1.specialCardDeck.size)
    }

    @Test
    fun grandDiscoverTest() = runTest {
        resetValue(0, 3, 10, 10, 4, 4)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_GRAND_GULLIVER, LocationEnum.YOUR_USED_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)

        assertEquals(6, gameStatus.player2.life)
    }

    @Test
    fun blasterTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 4)

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.DISCARD_YOUR)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_BLASTER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_BLASTER, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun railgunTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 4)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_BLASTER, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.DISCARD_YOUR)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_RAILGUN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_RAILGUN, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun connectDiveTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 4)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.DISCARD_YOUR)
        for(i in 1..3){
            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        }

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_CONNECT_DIVE, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_CONNECT_DIVE, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.usedSpecialCard[NUMBER_KURURU_CONNECT_DIVE]?.getNap())
        gameStatus.player1.usedSpecialCard[NUMBER_KURURU_CONNECT_DIVE]?.addNap(1)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_BLASTER, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_BLASTER, LocationEnum.HAND)

        assertEquals(6, gameStatus.player2.life)
    }

    private suspend fun connectDiveSetting() {
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_CONNECT_DIVE, LocationEnum.YOUR_USED_CARD)

        gameStatus.player1.usedSpecialCard[NUMBER_KURURU_CONNECT_DIVE]?.addNap(3)
    }

    @Test
    fun tornadoTest() = runTest {
        connectDiveSetting()
        resetValue(0, 11, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.aura)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun regainerTest() = runTest {
        connectDiveSetting()
        resetValue(0, 2, 10, 10, 4, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FIVE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FIVE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(4, gameStatus.player2.life)
    }

    @Test
    fun reflectorTest() = runTest {
        connectDiveSetting()
        resetValue(2, 0, 10, 10, 3, 8)

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.DISCARD_YOUR)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REFLECTOR, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REFLECTOR, LocationEnum.HAND)

        assertEquals(8, gameStatus.player1.enchantmentCard[NUMBER_KURURU_REFLECTOR]?.getNap())
    }

    @Test
    fun bigGolemTest() = runTest {
        connectDiveSetting()
        resetValue(0, 5, 10, 10, 3, 3)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DRAIN_DEVIL, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_BIG_GOLEM, LocationEnum.YOUR_USED_CARD)

        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)

        assertEquals(2, gameStatus.distanceToken)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_END_PHASE_EFFECT_ORDER, NUMBER_KURURU_CONNECT_DIVE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        gameStatus.endPhase()

        assertEquals(8, gameStatus.player2.life)
        assertNotEquals(0, gameStatus.player1.normalCardDeck.size)
    }
}