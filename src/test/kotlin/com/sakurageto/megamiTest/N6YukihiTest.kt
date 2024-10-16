package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.card.basicenum.CardClass
import com.sakurageto.card.basicenum.CardType
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.card.basicenum.SubType
import com.sakurageto.card.basicenum.MegamiEnum
import com.sakurageto.gamelogic.megamispecial.Umbrella
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class N6YukihiTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.YUKIHI
        MegamiEnum.YUKIHI.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_PUSH_OUT_SLASH_PULL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_SWING_SLASH_STAB, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        cardTypeTest(CardName.YUKIHI_TURN_UMBRELLA, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_BACK_WARD_STEP_SLASH_DIG_IN, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.YUKIHI_MAKE_CONNECTION, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_FLUTTERING_SNOWFLAKE, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_SWAYING_LAMPLIGHT, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_CLINGY_MIND, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.FULL_POWER)
        cardTypeTest(CardName.YUKIHI_SWIRLING_GESTURE, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)

        cardTypeTest(CardName.YUKIHI_HELP_SLASH_THREAT, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_THREAD_SLASH_RAW_THREAD, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.YUKIHI_FLUTTERING_COLLAR, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
    }

    @Test
    fun hiddenNeedleFoldTest() = runTest {
        resetValue(0, 2, 10, 10, 6,6)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun hiddenNeedleUnfoldTest() = runTest {
        resetValue(0, 0, 10, 10, 0, 0)
        gameStatus.player1.umbrella = Umbrella.UNFOLD

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HIDDEN_NEEDLE_SLASH_HOLD_NEEDLE, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun hiddenFireTest() = runTest {
        resetValue(0, 0, 10, 10, 6,6)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HIDDEN_FIRE_SLASH_CLAP_HANDS, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(Umbrella.UNFOLD, gameStatus.player1.umbrella)
    }

    @Test
    fun pushOutTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 0)
        gameStatus.player1.umbrella = Umbrella.UNFOLD

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_PUSH_OUT_SLASH_PULL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_PUSH_OUT_SLASH_PULL, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(0, gameStatus.distanceToken)
    }

    @Test
    fun swingTest() = runTest {
        resetValue(0, 5, 10, 10, 6,6)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_SWING_SLASH_STAB, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_SWING_SLASH_STAB, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
    }

    @Test
    fun turnUmbrellaTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)

        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_TURN_UMBRELLA, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        gameStatus.endPhase()

        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun clingyMindTest() = runTest {
        suspend fun flutteringFlowerTest(){
            resetValue(0, 2, 10, 10, 0, 10)
            gameStatus.player1.flare = 2

            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
            addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_FLUTTERING_SNOWFLAKE, LocationEnum.SPECIAL_CARD)
            useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_FLUTTERING_SNOWFLAKE, LocationEnum.SPECIAL_CARD)

            assertEquals(0, gameStatus.player1.flare)
            assertEquals(1, gameStatus.player1.concentration)
            assertEquals(9, gameStatus.player2.life)
        }

        suspend fun swayingLampLightTest(){
            resetValue(0, 3, 10, 10, 4, 10)
            gameStatus.player1.flare = 5
            gameStatus.player1.umbrella = Umbrella.UNFOLD

            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
            addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_SWAYING_LAMPLIGHT, LocationEnum.SPECIAL_CARD)
            useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_SWAYING_LAMPLIGHT, LocationEnum.SPECIAL_CARD)

            assertEquals(0, gameStatus.player1.flare)
            assertEquals(5, gameStatus.player2.life)
        }

        suspend fun helpThreatTest(){
            resetValue(0, 1, 10, 10, 4, 10)

            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
            addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HELP_SLASH_THREAT, LocationEnum.HAND)
            useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_HELP_SLASH_THREAT, LocationEnum.HAND)

            assertEquals(8, gameStatus.player2.life)
        }

        resetValue(0, 2, 10, 10, 0, 10)
        gameStatus.player1.flare = 3; gameStatus.player1.fullAction = true
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 7)))

        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_CLINGY_MIND, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_CLINGY_MIND, LocationEnum.SPECIAL_CARD)
        assertEquals(0, gameStatus.player1.flare)

        flutteringFlowerTest()

        swayingLampLightTest()

        helpThreatTest()
    }

    @Test
    fun threadRawThreadFoldTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 10)

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_RAPIDFIRE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_THREAD_SLASH_RAW_THREAD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_THREAD_SLASH_RAW_THREAD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_RAPIDFIRE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun threadRawThreadUnfoldTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 10)
        gameStatus.player1.umbrella = Umbrella.UNFOLD

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_OBORO_WIRE)))

        addCard(PlayerEnum.PLAYER1, CardName.OBORO_WIRE, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_THREAD_SLASH_RAW_THREAD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_THREAD_SLASH_RAW_THREAD, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun flutteringCollarTest() = runTest {
        suspend fun unfoldTest(){
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

            gameStatus.endPhase(); startPhase()
            addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
            gameStatus.endPhase(); startPhase()


            assertEquals(8, gameStatus.player2.life)
        }

        resetValue(0, 3, 10, 10, 3, 10)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RUNNING_RABBIT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YUKIHI_FLUTTERING_COLLAR, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 1)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        useCard(PlayerEnum.PLAYER1, CardName.YUKIHI_FLUTTERING_COLLAR, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_RUNNING_RABBIT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(4, gameStatus.player1.enchantmentCard[NUMBER_YUKIHI_FLUTTERING_COLLAR]?.getNap())
        assertEquals(2, gameStatus.player1.concentration)

        unfoldTest()
    }
}