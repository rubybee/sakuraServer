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

class KamuwiTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.KAMUWI
        MegamiEnum.KAMUWI.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.KAMUWI_RED_BLADE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_FLUTTERING_BLADE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_SI_KEN_LAN_JIN, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_CUT_DOWN, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.KAMUWI_THREADING_THORN, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_KE_SYO_LAN_LYU, CardClass.NORMAL, CardType.BEHAVIOR, SubType.FULL_POWER)
        cardTypeTest(CardName.KAMUWI_BLOOD_WAVE, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_LAMP, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_DAWN, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_GRAVEYARD, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.KAMUWI_KATA_SHIRO, CardClass.SPECIAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.KAMUWI_LOGIC, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
    }

    @Test
    fun redBladeTest() = runTest {
        resetValue(0, 3, 10, 10, 3, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_RED_BLADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_RED_BLADE, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.tabooGauge)
    }

    @Test
    fun flutteringBladeTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_FLUTTERING_BLADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_FLUTTERING_BLADE, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player2.aura)
        assertEquals(2, gameStatus.player1.tabooGauge)
    }

    @Test
    fun siKenLanJinTest() = runTest {
        resetValue(0, 4, 10, 10, 3, 0)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_SI_KEN_LAN_JIN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_SI_KEN_LAN_JIN, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.tabooGauge)
    }

    @Test
    fun cutDownTest() = runTest {
        resetValue(1, 1, 10, 3, 4, 4)
        gameStatus.player2.fullAction = true; gameStatus.player2.flare = 5

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_CUT_DOWN, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER1, CardName.KAMUWI_CUT_DOWN, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_JURUCK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_JURUCK, LocationEnum.SPECIAL_CARD)

        assertEquals(10, gameStatus.player1.life)
        assertEquals(4, gameStatus.player1.tabooGauge)
        assertEquals(0, gameStatus.player2.flare)
    }

    @Test
    fun threadingThornFiveTest() = runTest {
        resetValue(0, 4, 10, 10, 5, 0)

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_THREADING_THORN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_THREADING_THORN, LocationEnum.HAND)

        assertEquals(3, gameStatus.distanceToken)
        assertEquals(2, gameStatus.dust)
    }

    @Test
    fun threadingThornFourTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 0)

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_THREADING_THORN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_THREADING_THORN, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun threadingThornOneTest() = runTest {
        resetValue(0, 2, 10, 10, 1, 2)

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_THREADING_THORN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_THREADING_THORN, LocationEnum.HAND)

        assertEquals(3, gameStatus.distanceToken)
        assertEquals(0, gameStatus.dust)
    }

    @Test
    fun keSyoLanLyuTest() = runTest {
        resetValue(0, 3, 10, 10, 8, 2)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_KE_SYO_LAN_LYU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_KE_SYO_LAN_LYU, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(6, gameStatus.distanceToken)
    }

    @Test
    fun keSyoLanLyuRemainTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 2)
        gameStatus.player1.fullAction = true

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FOUR))

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_KE_SYO_LAN_LYU, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_KE_SYO_LAN_LYU, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(0, gameStatus.dust)
    }

    @Test
    fun bloodWaveTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 2)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_BLOOD_WAVE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_BLOOD_WAVE, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)

        gameStatus.player2.flare = 2
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_JJOCKBAE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_JJOCKBAE, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player2.aura)
        assertEquals(1, gameStatus.player1.enchantmentCard[NUMBER_KAMUWI_BLOOD_WAVE]?.getNap())
    }

    @Test
    fun lampTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)
        gameStatus.player1.flare = 7

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_LAMP, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_LAMP, LocationEnum.SPECIAL_CARD)

        assertEquals(9, gameStatus.player1.life)
        assertEquals(1, gameStatus.player1.flare)
        assertEquals(3, gameStatus.player1.tabooGauge)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun dawnNotReactTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 2)
        gameStatus.player1.flare = 6

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(6, gameStatus.player2.life)
        assertEquals(false, haveCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.YOUR_USED_CARD))
    }

    @Test
    fun dawnReactNormalTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 2)
        gameStatus.player1.flare = 6

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_WOOAHHANTAGUCK, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun dawnReactSpecialTest() = runTest {
        resetValue(0, 4, 10, 10, 3, 2)
        gameStatus.player1.flare = 6; gameStatus.player2.flare = 5

        addCard(PlayerEnum.PLAYER2, CardName.TOKOYO_KUON, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.TOKOYO_KUON, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_DAWN, LocationEnum.SPECIAL_CARD)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun graveYardTest() = runTest {
        resetValue(0, 2, 10, 3, 3, 2)
        gameStatus.player1.flare = 3

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_GRAVEYARD, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_GRAVEYARD, LocationEnum.SPECIAL_CARD)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player1.life)
        assertEquals(0, gameStatus.player2.life)
        assertEquals(2, gameStatus.player1.tabooGauge)
        assertEquals(false, gameStatus.gameEnd)
    }

    @Test
    fun kataShiroTest() = runTest {
        suspend fun returnTest() {
            gameStatus.tabooGaugeIncrease(PlayerEnum.PLAYER1, 6)

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.KAMUWI_KATA_SHIRO, LocationEnum.SPECIAL_CARD))
        }

        resetValue(3, 2, 10, 3, 3, 2)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_KATA_SHIRO, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.KAMUWI_KATA_SHIRO, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player1.flare)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(8, gameStatus.player1.life)
        assertEquals(0, gameStatus.player2.hand.size)

        returnTest()
    }

    @Test
    fun logicTest() = runTest {
        resetValue(3, 2, 7, 3, 1, 2)
        gameStatus.player1.flare = 3

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.KAMUWI_LOGIC, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.KAMUWI_LOGIC, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(2, gameStatus.player1.tabooGauge)
        assertEquals(9, gameStatus.player1.life)
        assertEquals(0, gameStatus.distanceToken)
    }
}