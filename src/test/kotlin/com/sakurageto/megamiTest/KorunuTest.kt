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

class KorunuTest: ApplicationTest() {
    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.KORUNU_SNOW_BLADE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KORUNU_REVOLVING_BLADE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KORUNU_BLADE_DANCE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KORUNU_RIDE_SNOW, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KORUNU_ABSOLUTE_ZERO, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.KORUNU_FROSTBITE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.KORUNU_FROST_THORN_BUSH, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.KORUNU_CONLU_RUYANPEH, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KORUNU_LETAR_LERA, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.KORUNU_UPASTUM, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KORUNU_PORUCHARTO, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
    }

    @Test
    fun snowBladeTest() = runTest{
        resetValue(0, 0, 10, 10, 4, 12)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_SNOW_BLADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_SNOW_BLADE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player2.freezeToken)
    }

    @Test
    fun revolvingBladeTest() = runTest{
        resetValue(0, 0, 10, 10, 3, 1)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_BEAN_BULLET, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.YURINA_BEAN_BULLET, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_REVOLVING_BLADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_REVOLVING_BLADE, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(1, gameStatus.distanceToken)
    }

    @Test
    fun bladeDanceTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 12)
        gameStatus.player2.freezeToken = 5

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun rideSnowTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 12)
        gameStatus.player2.freezeToken = 5

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_RIDE_SNOW, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_RIDE_SNOW, LocationEnum.HAND)

        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun absoluteZeroTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 12)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_ABSOLUTE_ZERO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_ABSOLUTE_ZERO, LocationEnum.HAND)

        assertEquals(5, gameStatus.player2.freezeToken)
        assertEquals(2, gameStatus.player1.aura)
    }

    @Test
    fun frostBiteTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 12)

        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_FROSTBITE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_FROSTBITE, LocationEnum.HAND)

        gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_INCUBATE, -1)

        assertEquals(1, gameStatus.player2.freezeToken)
    }

    @Test
    fun frostThornBushTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 12)
        gameStatus.player2.freezeToken = 5

        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_FROST_THORN_BUSH, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_FROST_THORN_BUSH, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        startPhase()

        assertEquals(2, gameStatus.player1.enchantmentCard[NUMBER_KORUNU_FROST_THORN_BUSH]?.getNap())
    }

    @Test
    fun conluRuyanpehTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 12)
        gameStatus.player1.flare = 4

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_CONLU_RUYANPEH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_CONLU_RUYANPEH, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(5, gameStatus.player2.freezeToken)
    }

    @Test
    fun letarLeraNotFullTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 12)
        gameStatus.player1.flare = 2

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_LETAR_LERA, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_LETAR_LERA, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(3, gameStatus.player2.aura)
        assertEquals(1, gameStatus.distanceToken)
    }

    @Test
    fun letarLeraFullTest() = runTest {
        resetValue(5, 0, 10, 10, 5, 12)
        gameStatus.player2.flare = 2

        addCard(PlayerEnum.PLAYER2, CardName.KORUNU_LETAR_LERA, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.KORUNU_LETAR_LERA, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun upastumTest() = runTest {
        suspend fun returnTest() {
            addReactData(PlayerEnum.PLAYER2)
            addCard(PlayerEnum.PLAYER1, CardName.KORUNU_SNOW_BLADE, LocationEnum.HAND)
            useCard(PlayerEnum.PLAYER1, CardName.KORUNU_SNOW_BLADE, LocationEnum.HAND)

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KORUNU_UPASTUM, LocationEnum.SPECIAL_CARD))
        }
        resetValue(0, 1, 10, 10, 4, 12)
        gameStatus.player2.freezeToken = 3

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_UPASTUM, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_UPASTUM, LocationEnum.SPECIAL_CARD)
        assertEquals(4, gameStatus.player2.freezeToken)
        returnTest()
    }

    @Test
    fun poruchartoTest() = runTest {
        suspend fun reuseTest(){
            gameStatus.player2.flare = 2
            player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            startPhase()

            assertEquals(4, gameStatus.player2.enchantmentCard[NUMBER_KORUNU_PORUCHARTO +
                    SECOND_PLAYER_START_NUMBER]?.getNap())
        }

        resetValue(0, 0, 10, 10, 5, 12)
        gameStatus.player2.flare = 4

        addCard(PlayerEnum.PLAYER2, CardName.KORUNU_PORUCHARTO, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.KORUNU_PORUCHARTO, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KORUNU_BLADE_DANCE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(10, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.freezeToken)

        reuseTest()
    }
}