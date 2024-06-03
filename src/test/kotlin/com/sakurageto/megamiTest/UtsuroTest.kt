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

class UtsuroTest: ApplicationTest() {
    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.UTSURO_WON_WOL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.UTSURO_BLACK_WAVE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.UTSURO_HARVEST, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.UTSURO_PRESSURE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.UTSURO_SHADOW_WING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.UTSURO_SHADOW_WALL, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.UTSURO_YUE_HOE_JU, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.FULL_POWER)
        cardTypeTest(CardName.UTSURO_HOE_MYEOL, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.UTSURO_HEO_WI, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.REACTION)
        cardTypeTest(CardName.UTSURO_JONG_MAL, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.UTSURO_MA_SIG, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)

        cardTypeTest(CardName.UTSURO_BITE_DUST, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.UTSURO_MANG_A, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.UTSURO_ANNIHILATION_SHADOW, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        cardTypeTest(CardName.UTSURO_SILENT_WALK, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.UTSURO_DE_MISE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
    }

    @Test
    fun wonwolTest() = runTest{
        resetValue(0, 5, 10, 10, 4, 12)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun blackWaveTest() = runTest {
        resetValue(0, 5, 10, 10, 4, 0)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM + SECOND_PLAYER_START_NUMBER
        )))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_BLACK_WAVE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_BLACK_WAVE, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR))
        assertEquals(4, gameStatus.player2.aura)
    }

    @Test
    fun harvestTest() = runTest {
        resetValue(3, 2, 10, 10, 4, 0)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_APDO, LocationEnum.ENCHANTMENT_ZONE)
        addReactData(PlayerEnum.PLAYER2)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            SECOND_PLAYER_START_NUMBER + NUMBER_YURINA_APDO
        )))
        addReactData(PlayerEnum.PLAYER2)
        addReactData(PlayerEnum.PLAYER1)

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_HARVEST, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_HARVEST, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun pressureTest() = runTest {
        resetValue(3, 2, 10, 10, 4, 12)

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_PRESSURE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_PRESSURE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, gameStatus.player2.shrink)
    }

    @Test
    fun shadowWingTest() = runTest {
        resetValue(3, 2, 10, 10, 4, 12)

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_SHADOW_WING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_SHADOW_WING, LocationEnum.HAND)

        assertEquals(6, gameStatus.getAdjustDistance())
        assertEquals(4, gameStatus.getAdjustSwellDistance())
    }

    @Test
    fun shadowWallTest() = runTest {
        resetValue(3, 2, 10, 10, 4, 12)

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_SHADOW_WALL, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.UTSURO_SHADOW_WALL, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.TOKOYO_BITSUNERIGI, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun yueHoeJuTest() = runTest {
        resetValue(0, 5, 10, 10, 4, 15)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_YUE_HOE_JU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_YUE_HOE_JU, LocationEnum.HAND)

        assertEquals(2, gameStatus.player2.aura)

        gameStatus.endPhase(); startPhase()
        gameStatus.endPhase(); startPhase()

        assertEquals(2, gameStatus.player2.aura)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun hoeMyeolTest() = runTest {
        resetValue(0, 5, 10, 10, 4, 24)

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_HOE_MYEOL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_HOE_MYEOL, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun heoWiDistanceTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 4)
        gameStatus.player2.flare = 3

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_HEO_WI, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.UTSURO_HEO_WI, LocationEnum.SPECIAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_HARVEST, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_HARVEST, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun heoWiAfterAttackTest() = runTest {
        resetValue(0, 5, 10, 10, 5, 0)
        gameStatus.player2.flare = 3

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_CHAM + SECOND_PLAYER_START_NUMBER
        )))

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_HEO_WI, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.UTSURO_HEO_WI, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_BLACK_WAVE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_BLACK_WAVE, LocationEnum.HAND)

        assertEquals(false, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR))
        assertEquals(4, gameStatus.player2.aura)
    }

    @Test
    fun heoWiEnchantmentTest() = runTest {
        resetValue(0, 3, 10, 10, 4, 2)

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_HEO_WI, LocationEnum.ENCHANTMENT_ZONE)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.enchantmentCard[NUMBER_YURINA_APDO]?.getNap())

        startPhase()

        assertEquals(3, gameStatus.player2.aura)
    }

    @Test
    fun jongMalTest() = runTest {
        resetValue(0, 3, 10, 10, 4, 3)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_JONG_MAL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_JONG_MAL, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)

        addReactData(PlayerEnum.PLAYER1)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(true, gameStatus.endCurrentPhase)
    }

    @Test
    fun maSigTest() = runTest {
        resetValue(0, 3, 10, 10, 4, 2)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_MA_SIG, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_MA_SIG, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)

        startPhase()

        assertEquals(2, gameStatus.player2.aura)
    }


    @Test
    fun biteDustTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 0)
        gameStatus.player2.flare = 2

        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_BITE_DUST, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_BITE_DUST, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun kururuSikTest() = runTest {
        resetValue(0, 2, 10, 10, 4, 9)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_MANG_A, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_DE_MISE, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_ANNIHILATION_SHADOW, LocationEnum.ADDITIONAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_SILENT_WALK, LocationEnum.ADDITIONAL_CARD)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_REVERBERATE_DEVICE_KURURUSIK, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player2.flare)

        gameStatus.endPhase()

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.UTSURO_MANG_A, LocationEnum.YOUR_USED_CARD))
        assertEquals(5, gameStatus.player1.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.UTSURO_DE_MISE, LocationEnum.HAND)
                || haveCard(PlayerEnum.PLAYER1, CardName.UTSURO_DE_MISE, LocationEnum.DECK))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.UTSURO_SILENT_WALK, LocationEnum.HAND)
                || haveCard(PlayerEnum.PLAYER1, CardName.UTSURO_SILENT_WALK, LocationEnum.DECK))
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.UTSURO_ANNIHILATION_SHADOW, LocationEnum.HAND)
                || haveCard(PlayerEnum.PLAYER1, CardName.UTSURO_ANNIHILATION_SHADOW, LocationEnum.DECK))
    }

    @Test
    fun mangATest() = runTest {
        resetValue(0, 0, 10, 10, 4, 12)

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_MANG_A, LocationEnum.YOUR_USED_CARD)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_WON_WOL, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun annihilationShadowTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 12)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_MANG_A, LocationEnum.YOUR_USED_CARD)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_ANNIHILATION_SHADOW, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_ANNIHILATION_SHADOW, LocationEnum.HAND)

        assertEquals(4, gameStatus.player2.life)
    }

    @Test
    fun silentWalkTest() = runTest {
        resetValue(5, 2, 10, 10, 4, 12)
        gameStatus.player1.fullAction = true

        addReactData(PlayerEnum.PLAYER2)
        addReactData(PlayerEnum.PLAYER2)
        addReactData(PlayerEnum.PLAYER2)
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_BACKWARD))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_BACKWARD))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_WIND_AROUND))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_INCUBATE))


        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_SILENT_WALK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_SILENT_WALK, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(4, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player1.flare)
    }

    @Test
    fun demiseTest() = runTest {
        resetValue(0, 0, 10, 10, 0, 0)
        gameStatus.player2.concentration = 2

        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_HARVEST, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.UTSURO_SHADOW_WING, LocationEnum.DECK)

        addCard(PlayerEnum.PLAYER1, CardName.UTSURO_DE_MISE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.UTSURO_DE_MISE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.normalCardDeck.size)
        assertEquals(0, gameStatus.player2.hand.size)
        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(0, gameStatus.player2.concentration)
    }


}