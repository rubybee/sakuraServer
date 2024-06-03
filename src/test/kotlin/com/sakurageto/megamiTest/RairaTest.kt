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

class RairaTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.RAIRA
        MegamiEnum.RAIRA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.RAIRA_BEAST_NAIL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RAIRA_STORM_SURGE_ATTACK, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RAIRA_REINCARNATION_NAIL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RAIRA_WIND_RUN, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RAIRA_WISDOM_OF_STORM_SURGE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RAIRA_HOWLING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.RAIRA_WIND_KICK, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.RAIRA_THUNDER_WIND_PUNCH, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RAIRA_SUMMON_THUNDER, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.RAIRA_WIND_CONSEQUENCE_BALL, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RAIRA_WIND_ATTACK, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RAIRA_WIND_ZEN_KAI, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RAIRA_WIND_CELESTIAL_SPHERE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.RAIRA_CIRCULAR_CIRCUIT, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.REACTION)

        cardTypeTest(CardName.RAIRA_STORM, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RAIRA_FURIOUS_STORM, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.FULL_POWER)
        cardTypeTest(CardName.RAIRA_JIN_PUNG_JE_CHEON_UI, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
    }


    @Test
    fun stormSurgeTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.windGauge = 4; gameStatus.player1.thunderGauge = 8
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM_SURGE_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM_SURGE_ATTACK, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
    }

    @Test
    fun reincarnationNailTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 4)
        addReactData(PlayerEnum.PLAYER2)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RAIRA_STORM_SURGE_ATTACK
        )))

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM_SURGE_ATTACK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_REINCARNATION_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_REINCARNATION_NAIL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM_SURGE_ATTACK, LocationEnum.DECK))
    }

    @Test
    fun windRunTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 4)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_RUN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_RUN, LocationEnum.HAND)

        assertEquals(1, gameStatus.distanceToken)
    }

    @Test
    fun wisdomOfSurgeTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.windGauge = 2; gameStatus.player1.thunderGauge = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_ILSUM
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WISDOM_OF_STORM_SURGE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_WISDOM_OF_STORM_SURGE, LocationEnum.HAND)

        assertEquals(3, gameStatus.player1.thunderGauge)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DECK))
    }

    @Test
    fun howlingTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 10; gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_HOWLING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_HOWLING, LocationEnum.HAND)

        assertEquals(20, gameStatus.player1.thunderGauge)
        assertEquals(0, gameStatus.player1.hand.size)
        assertEquals(1, gameStatus.player1.coverCard.size)
    }

    @Test
    fun thunderWindPunchTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 4)
        gameStatus.player1.windGauge = 4; gameStatus.player1.thunderGauge = 4; gameStatus.player1.flare = 3
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_THUNDER_WIND_PUNCH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_THUNDER_WIND_PUNCH, LocationEnum.SPECIAL_CARD)

        assertEquals(8, gameStatus.player2.life)

        gameStatus.endPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_THUNDER_WIND_PUNCH, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun summonThunderTest() = runTest {
        resetValue(0, 5, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 20; gameStatus.player1.flare = 6; gameStatus.player1.fullAction = true
        for(i in 1..10){
            addReactData(PlayerEnum.PLAYER2)
        }

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_SUMMON_THUNDER, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_SUMMON_THUNDER, LocationEnum.SPECIAL_CARD)

        assertEquals(5, gameStatus.player2.life)
    }

    @Test
    fun windConsequenceBallTest() = runTest {
        resetValue(0, 5, 10, 10, 2, 4)
        gameStatus.player1.flare = 2; gameStatus.player1.windGauge = 12
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ATTACK, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ZEN_KAI, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_CELESTIAL_SPHERE, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_CONSEQUENCE_BALL, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_CONSEQUENCE_BALL, LocationEnum.SPECIAL_CARD)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ATTACK, LocationEnum.SPECIAL_CARD))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ZEN_KAI, LocationEnum.SPECIAL_CARD))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_CELESTIAL_SPHERE, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun windZenKaiTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 4)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ATTACK, LocationEnum.YOUR_USED_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RAIRA_WIND_ATTACK
        )))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ZEN_KAI, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ZEN_KAI, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_WIND_ATTACK, LocationEnum.SPECIAL_CARD)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun circularCircuitTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 4)
        gameStatus.player2.thunderGauge = 0; gameStatus.player2.thunderGauge = 0; gameStatus.player2.flare = 2

        addCard(PlayerEnum.PLAYER2, CardName.RAIRA_CIRCULAR_CIRCUIT, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.RAIRA_CIRCULAR_CIRCUIT, LocationEnum.SPECIAL_CARD)
        for(i in 1..3){
            player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        }

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player2.thunderGauge)
        assertEquals(3, gameStatus.distanceToken)
    }

    @Test
    fun stormTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 10

        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun stormForceThunderOneTest() = runTest {
        resetValue(0, 5, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 10

        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FOUR))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(9, gameStatus.player1.thunderGauge)
    }

    @Test
    fun stormForceThunderTwoTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 10

        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FIVE))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(8, gameStatus.player1.thunderGauge)
    }

    @Test
    fun stormForceThunderThreeTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.thunderGauge = 10

        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SIX))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(7, gameStatus.player1.thunderGauge)
    }

    @Test
    fun stormForceWindOneTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.windGauge = 10

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)

        assertEquals(3, gameStatus.distanceToken)
        assertEquals(9, gameStatus.player1.windGauge)
    }

    @Test
    fun stormForceWindTwoTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.windGauge = 10

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RAIRA_WISDOM_OF_STORM_SURGE
        )))

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_WISDOM_OF_STORM_SURGE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_WISDOM_OF_STORM_SURGE, LocationEnum.COVER_CARD))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND))
        assertEquals(8, gameStatus.player1.windGauge)
    }

    @Test
    fun stormForceWindThreeTest() = runTest {
        resetValue(0, 4, 10, 10, 2, 4)
        gameStatus.player1.windGauge = 10; gameStatus.player2.concentration = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_STORM, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.concentration)
        assertEquals(0, gameStatus.player2.concentration)
        assertEquals(7, gameStatus.player1.windGauge)
    }

    @Test
    fun furiousStormTest() = runTest {
        resetValue(0, 3, 10, 10, 2, 4)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RAIRA_FURIOUS_STORM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RAIRA_FURIOUS_STORM, LocationEnum.HAND)

        assertEquals(2, gameStatus.player1.enchantmentCard[NUMBER_RAIRA_FURIOUS_STORM]?.getNap())
        assertEquals(1, gameStatus.player1.thunderGauge)

        gameStatus.endPhase(); startPhase()

        assertEquals(0, gameStatus.player2.aura)

        addReactData(PlayerEnum.PLAYER1)
        addCard(PlayerEnum.PLAYER2, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)

        assertEquals(10, gameStatus.player1.life)

        addReactData(PlayerEnum.PLAYER1)
        addCard(PlayerEnum.PLAYER2, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.RAIRA_BEAST_NAIL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player1.life)

        addReactData(PlayerEnum.PLAYER2)
        gameStatus.endPhase()
        assertEquals(9, gameStatus.player2.life)
    }
}