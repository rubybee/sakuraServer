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

class N10KururuOriginTest: ApplicationTest() {
    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.KURURU_ELEKITTEL, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KURURU_ACCELERATOR, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KURURU_KURURUOONG, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.KURURU_TORNADO, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.KURURU_REGAINER, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.KURURU_MODULE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.KURURU_REFLECTOR, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.KURURU_DRAIN_DEVIL, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.KURURU_BIG_GOLEM, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KURURU_INDUSTRIA, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KURURU_DUPLICATED_GEAR_1, CardClass.NORMAL, CardType.UNDEFINED, SubType.UNDEFINED)
        cardTypeTest(CardName.KURURU_DUPLICATED_GEAR_2, CardClass.NORMAL, CardType.UNDEFINED, SubType.UNDEFINED)
        cardTypeTest(CardName.KURURU_DUPLICATED_GEAR_3, CardClass.NORMAL, CardType.UNDEFINED, SubType.UNDEFINED)
        cardTypeTest(CardName.KURURU_KANSHOUSOUCHI_KURURUSIK, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
    }

    @Test
    fun elekittelTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_POETDANCE, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_INDUCE, LocationEnum.DISCARD_YOUR)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ELEKITTEL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_ELEKITTEL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun tornadoTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun acceleratorTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_POETDANCE, LocationEnum.DISCARD_YOUR)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_KURURU_TORNADO)))
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ACCELERATOR, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_ACCELERATOR, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun kururuoongTest() = runTest {
        resetValue(0, 5, 10, 10, 4, 5)

        addCard(PlayerEnum.PLAYER2, CardName.KURURU_KURURUOONG, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.KURURU_ELEKITTEL, LocationEnum.COVER_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.KURURU_KURURUOONG, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_KURURU_ELEKITTEL + SECOND_PLAYER_START_NUMBER)))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR))
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.KURURU_ELEKITTEL, LocationEnum.DECK))
    }

    @Test
    fun regainerOneTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun regainerUseCoverTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.COVER_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.COVER_CARD)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun regainerTwoTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun regainerThreeTest() = runTest {
        resetValue(0, 3, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun regainerFourTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FOUR))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun regainerFiveTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FIVE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun regainerSixTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SIX))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun regainerSevenTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_APDO
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SEVEN))

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(3, gameStatus.player1.enchantmentCard[NUMBER_YURINA_APDO]?.getNap())
    }

    @Test
    fun regainerEightTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 5)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.DISCARD_YOUR)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_APDO
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_EIGHT))

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.enchantmentCard[NUMBER_YURINA_APDO]?.getNap())
    }

    @Test
    fun moduleTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_MODULE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_MODULE, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ELEKITTEL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_ELEKITTEL, LocationEnum.HAND)

        assertEquals(2, gameStatus.distanceToken)
    }

    @Test
    fun reflectorTest() = runTest {
        resetValue(2, 0, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.DISCARD_YOUR)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REFLECTOR, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_REFLECTOR, LocationEnum.HAND)

        assertEquals(4, gameStatus.player1.enchantmentCard[NUMBER_KURURU_REFLECTOR]?.getNap())

        gameStatus.endPhase(); startPhase()

        addReactData(PlayerEnum.PLAYER1, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER1, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun drainDevilTest() = runTest {
        suspend fun returnTest() {
            resetValue(0, 5, 10, 10, 3, 3)
            gameStatus.player1.flare = 1
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf()))
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

            addCard(PlayerEnum.PLAYER1, CardName.KURURU_INDUSTRIA, LocationEnum.SPECIAL_CARD)
            useCard(PlayerEnum.PLAYER1, CardName.KURURU_INDUSTRIA, LocationEnum.SPECIAL_CARD)

            gameStatus.deckReconstruct(PlayerEnum.PLAYER1, false)

            assertEquals(1, gameStatus.player1.aura)
            assertEquals(4, gameStatus.player2.aura)
        }

        resetValue(0, 5, 10, 10, 3, 3)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DRAIN_DEVIL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_DRAIN_DEVIL, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(4, gameStatus.player2.aura)

        returnTest()
    }

    @Test
    fun bigGolemTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 3)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REGAINER, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DRAIN_DEVIL, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_BIG_GOLEM, LocationEnum.YOUR_USED_CARD)

        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_TORNADO, LocationEnum.HAND)

        assertEquals(2, gameStatus.distanceToken)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        gameStatus.endPhase()

        assertEquals(9, gameStatus.player2.life)
        assertNotEquals(0, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun industriaTest() = runTest {
        resetValue(2, 0, 10, 10, 3, 5)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DUPLICATED_GEAR_1, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DUPLICATED_GEAR_2, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_DUPLICATED_GEAR_3, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ELEKITTEL, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_KURURU_ELEKITTEL)))

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_INDUSTRIA, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_INDUSTRIA, LocationEnum.SPECIAL_CARD)
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, false)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_INDUSTRIA, LocationEnum.SPECIAL_CARD)
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, false)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_INDUSTRIA, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_POBARAM, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.OBORO_INDUCE, LocationEnum.DISCARD_YOUR)
        gameStatus.drawCard(PlayerEnum.PLAYER1, 3)

        useCard(PlayerEnum.PLAYER1, CardName.KURURU_DUPLICATED_GEAR_1, LocationEnum.HAND)
        assertEquals(10, gameStatus.player2.life)

        useCard(PlayerEnum.PLAYER1, CardName.KURURU_DUPLICATED_GEAR_2, LocationEnum.HAND)
        assertEquals(9, gameStatus.player2.life)

        useCard(PlayerEnum.PLAYER1, CardName.KURURU_DUPLICATED_GEAR_3, LocationEnum.HAND)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun kanshousouchiKururusikTest() = runTest{
        resetValue(0, 3, 10, 10, 3, 5)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_KURURUOONG, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ELEKITTEL, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_ACCELERATOR, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_MODULE, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.KURURU_REFLECTOR, LocationEnum.DISCARD_YOUR)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_WOLYUNGNACK + SECOND_PLAYER_START_NUMBER
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_WOLYUNGNACK + SECOND_PLAYER_START_NUMBER
        )))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.KURURU_KANSHOUSOUCHI_KURURUSIK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KURURU_KANSHOUSOUCHI_KURURUSIK, LocationEnum.SPECIAL_CARD)

        assertEquals(6, gameStatus.player2.life)
    }
}