package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.CardSet.toCardName
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.card.basicenum.MegamiEnum
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class N23AkinaTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.AKINA
        MegamiEnum.AKINA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.AKINA_ABACUS_STONE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.AKINA_THREAT, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.AKINA_TRADE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.AKINA_SPECULATION, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.AKINA_CALC, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.AKINA_TURN_OFF_TABLE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.AKINA_DIRECT_FINANCING, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.FULL_POWER)
        cardTypeTest(CardName.AKINA_OPEN_CUTTING_METHOD, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.AKINA_SU_LYO_SUL, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.AKINA_AKINA_ACCURATE_CALC, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.AKINA_GRAND_CALC_AND_MANUAL, CardClass.SPECIAL, CardType.ATTACK, SubType.REACTION)
    }

    @Test
    fun investmentTest() = runTest {
        resetValue(1, 1, 10, 10, 2, 0)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_DIRECT_FINANCING, LocationEnum.DISCARD_YOUR)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_AKINA_DIRECT_FINANCING
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.FULL_POWER_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_END_TURN))
        gameStatus.mainPhase()

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(3, gameStatus.player1.getMarketPrice())
        assertEquals(1, gameStatus.player1.flow)
    }

    @Test
    fun investmentDeckReconstructTest() = runTest {
        resetValue(1, 1, 10, 10, 2, 1)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.DISCARD_YOUR)
        gameStatus.player1.marketPrice = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_AKINA_THREAT
        )))
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, true)

        assertEquals(0, gameStatus.dust)
        assertEquals(2, gameStatus.player1.getMarketPrice())
        assertEquals(1, gameStatus.player1.flow)
    }

    @Test
    fun abacusStoneOneTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_ABACUS_STONE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_ABACUS_STONE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(1, gameStatus.player1.concentration)
    }

    @Test
    fun abacusStoneTwoTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 0)
        gameStatus.player1.flow = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_ABACUS_STONE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_ABACUS_STONE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(0, gameStatus.player1.flow)
        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun abacusStoneThreeTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 0)
        gameStatus.player1.flare = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_ABACUS_STONE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_ABACUS_STONE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.flow)
        assertEquals(0, gameStatus.player1.flare)
    }

    @Test
    fun threatTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)
        gameStatus.player1.flow = 1

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_THREAT, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(3, gameStatus.player1.getMarketPrice())
    }

    @Test
    fun tradeTest() = runTest {
        resetValue(3, 2, 10, 10, 4, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RAIRA_REINCARNATION_NAIL
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_REINCARNATION_NAIL, LocationEnum.DISCARD_YOUR)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_TRADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_TRADE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(2, gameStatus.player1.getMarketPrice())
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_REINCARNATION_NAIL, LocationEnum.HAND))
        assertEquals(true, gameStatus.player1.endTurn)
    }

    @Test
    fun speculationOneTest() = runTest {
        resetValue(2, 1, 10, 10, 2, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_SPECULATION, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_SPECULATION, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(2, gameStatus.player1.flow)
    }

    @Test
    fun speculationTwoTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 2)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_SPECULATION, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_SPECULATION, LocationEnum.HAND)

        assertEquals(2, gameStatus.player1.aura)
        assertEquals(0, gameStatus.dust)
    }

    @Test
    fun calcTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 2)

        addCard(PlayerEnum.PLAYER1, CardName.AKINA_CALC, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_CALC, LocationEnum.HAND)
        assertEquals(1, gameStatus.player1.concentration)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(8, gameStatus.player2.life)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(6, gameStatus.player2.life)

        addReactData(PlayerEnum.PLAYER1)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(8, gameStatus.player1.life)

        addReactData(PlayerEnum.PLAYER1)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(6, gameStatus.player1.life)
    }

    @Test
    fun turnOffTableTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)
        gameStatus.player2.flare = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_TURN_OFF_TABLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_TURN_OFF_TABLE, LocationEnum.HAND)

        assertEquals(5, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player2.flare)

        startPhase(); startPhase()

        assertEquals(4, gameStatus.distanceToken)
        assertEquals(1, gameStatus.player2.flare)
    }

    @Test
    fun directFinancingTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 2)
        gameStatus.player1.concentration = 1; gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.AKINA_DIRECT_FINANCING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_DIRECT_FINANCING, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.concentration)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)

        gameStatus.endPhase()
        for (card_number in NUMBER_YURINA_CHAM..NUMBER_YURINA_GIBACK){
            addCard(PlayerEnum.PLAYER2, card_number.toCardName(), LocationEnum.DECK)
        }
        startPhase(); startPhase()

        assertEquals(3, gameStatus.player1.getMarketPrice())
    }

    // 1, 1, 2, 3
    @Test
    fun openCuttingMethodTest() = runTest {
        resetValue(5, 2, 10, 10, 3, 2)
        gameStatus.player1.flare = 7; gameStatus.player1.marketPrice = 1

        addCard(PlayerEnum.PLAYER2, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_OPEN_CUTTING_METHOD, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_OPEN_CUTTING_METHOD, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(4, gameStatus.player1.marketPrice)
    }

    @Test
    fun grandCalcAndManualTest() = runTest {
        resetValue(2, 2, 10, 10, 3, 2)
        gameStatus.player1.flare = 1; gameStatus.player1.life = 1; gameStatus.player1.flow = 1

        addCard(PlayerEnum.PLAYER1, CardName.AKINA_GRAND_CALC_AND_MANUAL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_GRAND_CALC_AND_MANUAL, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(0, gameStatus.player1.life)
        assertEquals(0, gameStatus.player1.flow)
    }

    @Test
    fun sulyosulTest() = runTest {
        resetValue(2, 2, 7, 10, 3, 2)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.AKINA_SU_LYO_SUL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_SU_LYO_SUL, LocationEnum.SPECIAL_CARD)

        assertEquals(3, gameStatus.player1.life)
        assertEquals(4, gameStatus.player1.flare)

        gameStatus.processDamage(
            PlayerEnum.PLAYER1, CommandEnum.CHOOSE_CHOJO, Pair(5, 5), false,
        null, null, -1)

        assertEquals(4, gameStatus.player1.life)
        assertEquals(false, haveCard(PlayerEnum.PLAYER1, CardName.AKINA_SU_LYO_SUL, LocationEnum.YOUR_USED_CARD))
    }

    @Test
    fun sulyosulDieTest() = runTest {
        resetValue(2, 2, 3, 10, 3, 2)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.AKINA_SU_LYO_SUL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_SU_LYO_SUL, LocationEnum.SPECIAL_CARD)

        assertEquals(true, gameStatus.gameEnd)
    }

    @Test
    fun accurateCalcTest() = runTest {
        resetValue(2, 2, 7, 10, 7, 2)
        gameStatus.player1.flare = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addCard(PlayerEnum.PLAYER1, CardName.AKINA_AKINA_ACCURATE_CALC, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.AKINA_AKINA_ACCURATE_CALC, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.FULL_POWER_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_END_TURN))
        gameStatus.mainPhase()

        assertEquals(1, gameStatus.player1.flare)
    }

}