package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class HonokaTest: ApplicationTest() {
    @Test
    fun spiritSikTest() = runTest{
        resetValue(0, 0, 10, 10, 8, 0)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SPIRIT_SIK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SPIRIT_SIK, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.DECK))
    }

    @Test
    fun guardianSpiritSikTest() = runTest{
        resetValue(0, 1, 10, 10, 3, 1)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.DECK))
    }

    @Test
    fun assaultSikTest() = runTest {
        resetValue(0, 2, 9, 10, 5, 1)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_DIVINE_OUKA, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_ASSAULT_SPIRIT_SIK, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_DIVINE_OUKA, LocationEnum.DECK))
    }

    @Test
    fun oukaTest() = runTest {
        resetValue(0, 3, 10, 10, 4, 2)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_DIVINE_OUKA, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_DIVINE_OUKA, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(2, gameStatus.player1.aura)
    }

    @Test
    fun sakuraBlizzardTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 2)

        addReactData(PlayerEnum.PLAYER2)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_BLIZZARD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_BLIZZARD, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun yukiGongJinTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 2)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HONOKA_SAKURA_BLIZZARD
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))


        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_BLIZZARD, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_UI_GI_GONG_JIN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_UI_GI_GONG_JIN, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(9, gameStatus.player1.life)
        assertEquals(2, gameStatus.player1.normalCardDeck.size)
    }

    @Test
    fun sakuraWingTest() = runTest {
        resetValue(0, 2, 9, 10, 3, 2)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_REGENERATION, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_WING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_WING, LocationEnum.HAND)

        assertEquals(5, gameStatus.distanceToken)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_REGENERATION, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun regenerationTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_WING, LocationEnum.ADDITIONAL_CARD)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_REGENERATION, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_REGENERATION, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player1.flare)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_WING, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun sakuraAmuletTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)

        addCard(PlayerEnum.PLAYER2, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.HONOKA_SAKURA_WING, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.HONOKA_SAKURA_AMULET, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.HONOKA_SAKURA_AMULET, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HONOKA_SAKURA_WING + SECOND_PLAYER_START_NUMBER
        )))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_BLIZZARD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_BLIZZARD, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.DECK))
    }

    @Test
    fun sparkleTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_HONOKA_SPARKLE, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun commandTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 3)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_COMMAND, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_COMMAND, LocationEnum.HAND)

        gameStatus.endPhase()

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun tailWindTest() = runTest {
        resetValue(0, 0, 10, 10, 9, 3)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_TAIL_WIND, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_TAIL_WIND, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SPIRIT_SIK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SPIRIT_SIK, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun chestWillingTest() = runTest {
        resetValue(0, 0, 10, 10, 9, 3)
        gameStatus.player1.flare = 5

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_HAND_FLOWER, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_CHEST_WILLINGNESS, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_CHEST_WILLINGNESS, LocationEnum.SPECIAL_CARD)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_HAND_FLOWER, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun handFlowerTest() = runTest {
        resetValue(0, 0, 10, 10, 9, 10)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_A_NEW_OPENING, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_HAND_FLOWER, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_HAND_FLOWER, LocationEnum.SPECIAL_CARD)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        gameStatus.endPhase()

        assertEquals(1, gameStatus.player1.usedSpecialCard[NUMBER_HONOKA_HAND_FLOWER]?.getNap())

        for(i in 1..4){
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
            gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_WIND_AROUND, -1)
        }

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_A_NEW_OPENING, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun newOpeningTest() = runTest {
        resetValue(5, 4, 5, 5, 5, 0)
        gameStatus.player1.flare = 10
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_A_NEW_OPENING, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_A_NEW_OPENING, LocationEnum.SPECIAL_CARD)

        gameStatus.endPhase()

        assertEquals(0, gameStatus.player2.life)
    }

    @Test
    fun underFlagTest() = runTest {
        resetValue(5, 2, 10, 10, 5, 0)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_COMMAND, LocationEnum.ENCHANTMENT_ZONE)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HONOKA_COMMAND
        )))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_UNDER_THE_NAME_OF_FLAG, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_UNDER_THE_NAME_OF_FLAG, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.flare)
        assertEquals(5, gameStatus.player1.enchantmentCard[NUMBER_HONOKA_COMMAND]?.getNap())
    }

    @Test
    fun fourSeasonTest() = runTest {
        suspend fun returnTest(){
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

            addCard(PlayerEnum.PLAYER1, CardName.HONOKA_REGENERATION, LocationEnum.ADDITIONAL_CARD)

            addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_WING, LocationEnum.HAND)
            useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_WING, LocationEnum.HAND)

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOUR_SEASON_BACK, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 0, 10, 10, 5, 5)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SPIRIT_SIK, LocationEnum.COVER_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HONOKA_SPIRIT_SIK
        )))
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_COMMAND, LocationEnum.YOUR_DECK_TOP)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))


        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOUR_SEASON_BACK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOUR_SEASON_BACK, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(1, gameStatus.player1.aura)

        returnTest()
    }

    @Test
    fun fullBloomTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_COMMAND, LocationEnum.DECK)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SPIRIT_SIK, LocationEnum.DECK)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 5)))

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FULL_BLOOM_PATH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_FULL_BLOOM_PATH, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)

        startPhase()

        assertEquals(1, gameStatus.player1.aura)

        resetValue(5, 0, 10, 10, 0, 0)

        gameStatus.endPhase(); startPhase()

        assertEquals(1, gameStatus.player1.flare)
    }

    @Test
    fun sakuraSwordTest() = runTest {
        resetValue(0, 1, 10, 10, 5, 1)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SHADOW_HAND, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SWORD, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SWORD, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_SHADOW_HAND, LocationEnum.DECK))
    }

    @Test
    fun shadowHandLifeTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 1)
        gameStatus.player2.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SWORD, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SHADOW_HAND, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SHADOW_HAND, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SWORD, LocationEnum.DECK))
    }

    @Test
    fun shadowHandAuraTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 1)
        gameStatus.player2.flare = 2

        addCard(PlayerEnum.PLAYER2, CardName.HONOKA_SPIRIT_SIK, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SWORD, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SHADOW_HAND, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SHADOW_HAND, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.HONOKA_SPIRIT_SIK, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun eyeOpenFiveTest() = runTest {
        resetValue(0, 1, 9, 10, 3, 2)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_EYE_OPEN_ALONE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_EYE_OPEN_ALONE, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.DECK))
    }

    @Test
    fun eyeOpenSixTest() = runTest {
        resetValue(0, 1, 9, 10, 3, 3)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FACING_SHADOW, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_EYE_OPEN_ALONE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_EYE_OPEN_ALONE, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_FACING_SHADOW, LocationEnum.DECK))
    }

    @Test
    fun followTraceZeroTest() = runTest {
        resetValue(0, 0, 9, 10, 10, 0)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_INCUBATE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun followTraceOneTest() = runTest {
        resetValue(0, 0, 9, 10, 10, 1)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_HOLD_HANDS, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_FOLLOW_TRACE, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_HOLD_HANDS, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun facingShadowTwelveTest() = runTest {
        resetValue(0, 0, 10, 10, 10, 11)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_WALK_OLD_LOAD, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FACING_SHADOW, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_FACING_SHADOW, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_WALK_OLD_LOAD, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun facingShadowElevenTest() = runTest {
        resetValue(0, 0, 10, 10, 10, 10)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_HOLD_HANDS, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_FACING_SHADOW, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_FACING_SHADOW, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_HOLD_HANDS, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun sakuraBrightlyTest() = runTest {
        suspend fun returnTest() {
            gameStatus.endPhase()

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 5, 5, 5, 5, 10)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, LocationEnum.SPECIAL_CARD)
        gameStatus.player1.specialCardDeck[NUMBER_HONOKA_SAKURA_SHINING_BRIGHTLY]?.addNap(6)

        addReactData(PlayerEnum.PLAYER2)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_SAKURA_SHINING_BRIGHTLY, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(3, gameStatus.player2.life)
        assertEquals(7, gameStatus.player1.usedSpecialCard[NUMBER_HONOKA_SAKURA_SHINING_BRIGHTLY]?.getNap())

        returnTest()
    }

    @Test
    fun holdHands() = runTest {
        resetValue(0, 5, 5, 5, 5, 10)
        gameStatus.player1.flare = 5

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_HOLD_HANDS, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_HOLD_HANDS, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(5, gameStatus.player1.aura)
    }

    @Test
    fun walkOldRoad() = runTest {
        resetValue(0, 5, 5, 5, 5, 10)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_WALK_OLD_LOAD, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.HONOKA_WALK_OLD_LOAD, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(false, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_WALK_OLD_LOAD, LocationEnum.YOUR_USED_CARD))
        assertEquals(true, gameStatus.player2.nextMainPhaseSkip)
    }


}