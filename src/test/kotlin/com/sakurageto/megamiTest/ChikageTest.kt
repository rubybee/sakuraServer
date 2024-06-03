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
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ChikageTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.CHIKAGE
        MegamiEnum.CHIKAGE.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.CHIKAGE_THROW_KUNAI, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_POISON_NEEDLE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_TO_ZU_CHU, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.CHIKAGE_HIDDEN_WEAPON, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.CHIKAGE_POISON_SMOKE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_TIP_TOEING, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_MUDDLE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_DEADLY_POISON, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_HAN_KI_POISON, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.REACTION)
        cardTypeTest(CardName.CHIKAGE_REINCARNATION_POISON, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_YAMIKURA_CHIKAGE_WAY_OF_LIVE, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.FULL_POWER)

        cardTypeTest(CardName.POISON_PARALYTIC, CardClass.POISON, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.POISON_HALLUCINOGENIC, CardClass.POISON, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.POISON_RELAXATION, CardClass.POISON, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.POISON_DEADLY_1, CardClass.POISON, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.POISON_DEADLY_2, CardClass.POISON, CardType.BEHAVIOR, SubType.NONE)

        cardTypeTest(CardName.CHIKAGE_TRICK_UMBRELLA, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_STRUGGLE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
    }

    @Test
    fun poisonNeedleTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 0)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_POISON_RELAXATION + SECOND_PLAYER_START_NUMBER
        )))

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_POISON_NEEDLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_POISON_NEEDLE, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.POISON_RELAXATION, LocationEnum.DECK))
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun toZuChuTest() = runTest {
        resetValue(3, 1, 10, 10, 3, 1)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun hiddenWeaponTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 1)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER2, CardName.POISON_HALLUCINOGENIC, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_POISON_RELAXATION + SECOND_PLAYER_START_NUMBER
        )))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_HIDDEN_WEAPON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_HIDDEN_WEAPON, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.POISON_RELAXATION, LocationEnum.HAND))
        assertEquals(7, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun poisonSmokeTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 1)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_POISON_RELAXATION + SECOND_PLAYER_START_NUMBER
        )))

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_POISON_SMOKE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_POISON_SMOKE, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.POISON_RELAXATION, LocationEnum.HAND))
    }

    @Test
    fun tipToeingTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TIP_TOEING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TIP_TOEING, LocationEnum.HAND)

        assertEquals(2, gameStatus.getAdjustDistance())
    }

    @Test
    fun muddleTest() = runTest {
        resetValue(0, 3, 10, 10, 2, 4)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_MUDDLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_MUDDLE, LocationEnum.HAND)

        assertEquals(2, gameStatus.getAdjustDistance())

        gameStatus.endPhase(); startPhase()

        gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_BREAK_AWAY, -1)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_GO_BACKWARD, -1)

        assertEquals(2, gameStatus.distanceToken)
    }

    @Test
    fun deadlyPoisonTest() = runTest {
        resetValue(0, 3, 10, 10, 2, 4)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_DEADLY_POISON, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_DEADLY_POISON, LocationEnum.SPECIAL_CARD)

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.POISON_DEADLY_1, LocationEnum.DECK))
    }

    @Test
    fun hankiPoisonTest() = runTest {
        resetValue(3, 1, 10, 10, 3, 5)
        gameStatus.player2.flare = 2

        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_HAN_KI_POISON, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.CHIKAGE_HAN_KI_POISON, LocationEnum.SPECIAL_CARD)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 5)))
        addReactData(PlayerEnum.PLAYER2, CardName.CHIKAGE_HAN_KI_POISON, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)

        assertEquals(1, gameStatus.player2.aura)
        assertEquals(3, gameStatus.distanceToken)
    }

    @Test
    fun reincarnationPoisonTest() = runTest {
        suspend fun returnTest(){
            gameStatus.endPhase()

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_REINCARNATION_POISON, LocationEnum.SPECIAL_CARD))
        }

        resetValue(3, 0, 10, 10, 3, 5)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER2, CardName.POISON_DEADLY_1, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.POISON_RELAXATION, LocationEnum.HAND)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_REINCARNATION_POISON, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_REINCARNATION_POISON, LocationEnum.SPECIAL_CARD)

        assertEquals(8, gameStatus.player2.life)

        returnTest()
    }

    @Test
    fun poisonParalyticConditionTest() = runTest {
        resetValue(3, 0, 10, 10, 3, 5)

        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GO_FORWARD, -1)

        addCard(PlayerEnum.PLAYER1, CardName.POISON_PARALYTIC, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.POISON_PARALYTIC, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.POISON_PARALYTIC, LocationEnum.HAND))
    }

    @Test
    fun poisonParalyticTest() = runTest {
        resetValue(3, 0, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.POISON_PARALYTIC, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.POISON_PARALYTIC, LocationEnum.HAND)

        assertEquals(true, gameStatus.endCurrentPhase)
    }

    @Test
    fun poisonHallucinogenicTest() = runTest {
        resetValue(3, 0, 10, 10, 3, 5)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.POISON_HALLUCINOGENIC, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.POISON_HALLUCINOGENIC, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
    }

    @Test
    fun poisonRelaxationTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.POISON_RELAXATION, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.POISON_RELAXATION, LocationEnum.HAND)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND))
    }

    @Test
    fun poisonDeadlyTest() = runTest {
        resetValue(3, 0, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.POISON_DEADLY_1, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.POISON_DEADLY_1, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun trickUmbrellaTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_MUDDLE, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TRICK_UMBRELLA, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_TRICK_UMBRELLA, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun struggleTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_MUDDLE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_STRUGGLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_STRUGGLE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.concentration)
        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun zanZeNoConnectionPoisonTest() = runTest {
        resetValue(0, 3, 10, 10, 1, 5)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_TO_ZU_CHU, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_MUDDLE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.CHIKAGE_STRUGGLE, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.CHIKAGE_ZAN_ZE_NO_CONNECTION_POISON, LocationEnum.SPECIAL_CARD)

        assertEquals(4, gameStatus.player2.life)
    }




}