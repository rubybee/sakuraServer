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

class N19MegumiTest: ApplicationTest() {
    private suspend fun useReed(){
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(0, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 1)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(1, 0)))

        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_REED, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_REED, LocationEnum.HAND)
    }

    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.MEGUMI
        MegamiEnum.MEGUMI.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() {
        cardTypeTest(CardName.MEGUMI_GONG_SUM, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MEGUMI_TA_CHEOG, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MEGUMI_SHELL_ATTACK, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MEGUMI_POLE_THRUST, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.MEGUMI_REED, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.MEGUMI_BALSAM, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.MEGUMI_WILD_ROSE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.FULL_POWER)

        cardTypeTest(CardName.MEGUMI_ROOT_OF_CAUSALITY, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.MEGUMI_BRANCH_OF_POSSIBILITY, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.REACTION)
        cardTypeTest(CardName.MEGUMI_FRUIT_OF_END, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.MEGUMI_MEGUMI_PALM, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
    }

    @Test
    fun gongSumTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 0)
        gameStatus.player1.notReadySeed = null

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_GONG_SUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_GONG_SUM, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun taCheogTest() = runTest {
        useReed()
        resetValue(0, 2, 10, 10, 3, 0)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_TA_CHEOG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_TA_CHEOG, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun shellAttackTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 10)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_SHELL_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_SHELL_ATTACK, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(0, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(1, 0)))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)

        assertEquals(3, gameStatus.player1.enchantmentCard[NUMBER_YURINA_APDO]?.getNap())
    }

    @Test
    fun poleThrust() = runTest {
        resetValue(0, 0, 10, 10, 3, 10)

        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_REED, LocationEnum.ENCHANTMENT_ZONE)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_POLE_THRUST, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_POLE_THRUST, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(4, gameStatus.distanceToken)

        gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_GO_FORWARD, -1)

        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun reedTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 10)

        useReed()

        assertEquals(3, gameStatus.getAdjustSwellDistance())
        assertEquals(6, gameStatus.getAdjustDistance())
    }

    @Test
    fun balsamTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 10)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(0, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 1)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(1, 0)))

        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_BALSAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_BALSAM, LocationEnum.HAND)

        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.FULL_POWER_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_END_TURN))

        gameStatus.mainPhase()

        assertEquals(9, gameStatus.player2.life)

        gameStatus.endPhase()
        addReactData(PlayerEnum.PLAYER2)
        player2Connection.putReceiveData(makeData(CommandEnum.FULL_POWER_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.ACTION_END_TURN))

        gameStatus.mainPhase()

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun wildRoseTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 10)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(0, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 1)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(1, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))

        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_WILD_ROSE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_WILD_ROSE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.aura)

        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        gameStatus.endPhase()
        assertEquals(2, gameStatus.player1.aura)

        startPhase()
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.MEGUMI_WILD_ROSE, LocationEnum.ENCHANTMENT_ZONE))

        gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_GO_FORWARD, -1)
        assertEquals(4, gameStatus.distanceToken)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_GO_FORWARD, -1)
        assertEquals(3, gameStatus.distanceToken)
    }

    @Test
    fun rootCausalityTest() = runTest {
        suspend fun returnTest(){
            gameStatus.player1.readySeed = 0

            gameStatus.endPhase()

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.MEGUMI_ROOT_OF_CAUSALITY, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 0, 10, 10, 3, 10)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_ROOT_OF_CAUSALITY, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_ROOT_OF_CAUSALITY, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.readySeed)

        returnTest()
    }

    @Test
    fun branchPossibilityTest() = runTest {
        resetValue(2, 3, 10, 10, 2, 10)
        gameStatus.player1.flare = 3; gameStatus.player2.flare = 10

        useReed()
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_BRANCH_OF_POSSIBILITY, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.REACT_USE_CARD_SPECIAL, NUMBER_MEGUMI_BRANCH_OF_POSSIBILITY))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(0, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 2)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(1, 0)))

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(0, gameStatus.player1.aura)
        assertEquals(10, gameStatus.player1.life)

        gameStatus.endPhase(); startPhase()

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun fruitEndTest() = runTest {
        resetValue(0, 5, 10, 10, 4, 10)
        gameStatus.player1.flare = 4

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(1, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(0, 0)))
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_FRUIT_OF_END, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_FRUIT_OF_END, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        gameStatus.dust = 0

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(1, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(0, 0)))
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_REED, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_REED, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(1, 0)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(0, 0)))
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_BALSAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_BALSAM, LocationEnum.HAND)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(1, 0)))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_CHOONGEMJUNG, LocationEnum.HAND)

        gameStatus.dust = 0
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SPROUT, mutableListOf(1, 0)))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_GWONYUCK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_GWONYUCK, LocationEnum.HAND)

        gameStatus.player1.aura = 2
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP_LOCATION, mutableListOf(NUMBER_MEGUMI_FRUIT_OF_END)))
        startPhase()

        assertEquals(5, gameStatus.player1.enchantmentCard[NUMBER_MEGUMI_FRUIT_OF_END]?.getSeedToken())

        gameStatus.distanceToken = 5
        gameStatus.endPhase()
        assertEquals(5, gameStatus.player2.life)
    }

    @Test
    fun megumiPalmTest() = runTest {
        resetValue(5, 5, 10, 10, 4, 10)
        gameStatus.player1.flare = 3

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_GROWING, mutableListOf(1, 0)))
        addCard(PlayerEnum.PLAYER1, CardName.MEGUMI_MEGUMI_PALM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.MEGUMI_MEGUMI_PALM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)

        gameStatus.player1.flare = 5

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SIX_STAR_SEA, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YATSUHA_SIX_STAR_SEA, LocationEnum.SPECIAL_CARD)

        assertEquals(5, gameStatus.player2.life)
    }
}