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

class ShisuiTest: ApplicationTest() {
    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.SHISUI_SAW_BLADE_CUT_DOWN, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SHISUI_PENETRATE_SAW_BLADE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SHISUI_REBELLION_ATTACK, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.SHISUI_IRON_RESISTANCE, CardClass.NORMAL, CardType.ATTACK, SubType.FULL_POWER)
        cardTypeTest(CardName.SHISUI_THORNY_PATH, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.SHISUI_IRON_POWDER_WIND_AROUND, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.SHISUI_BLACK_ARMOR, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.SHISUI_PADMA_CUT_DOWN, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.SHISUI_UPALA_TEAR, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SHISUI_ABUDA_EAT, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.SHISUI_SHISUI_PLACE_OF_DEATH, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.FULL_POWER)
    }

    @Test
    fun lacerationTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 0)
        gameStatus.player2.flare = 1

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FOUR))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_FIVE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_SIX))

        gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_LIFE] = 1
        gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_AURA] = 1
        gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_FLARE] = 1

        startPhase()

        assertEquals(1, gameStatus.player2.flare)
        assertEquals(1, gameStatus.player2.aura)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(3, gameStatus.gameLogger.countGetDamage(PlayerEnum.PLAYER2))
    }

    @Test
    fun sawBladeCutDownTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 0)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_SAW_BLADE_CUT_DOWN, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_SAW_BLADE_CUT_DOWN, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun penetrateSawBladeTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 0)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_PENETRATE_SAW_BLADE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_PENETRATE_SAW_BLADE, LocationEnum.HAND)

        assertEquals(2, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_LIFE])
        assertEquals(1, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_AURA])
    }

    @Test
    fun rebellionAttackTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 1)

        gameStatus.processDamage(
            PlayerEnum.PLAYER1, CommandEnum.CHOOSE_AURA, Pair(1, 1), false,
            null, null, -1)
        gameStatus.processDamage(
            PlayerEnum.PLAYER1, CommandEnum.CHOOSE_AURA, Pair(1, 1), false,
            null, null, -1)
        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_REBELLION_ATTACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_REBELLION_ATTACK, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(1, gameStatus.player1.aura)
    }

    @Test
    fun ironResistanceTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 0)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_IRON_RESISTANCE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_IRON_RESISTANCE, LocationEnum.HAND)

        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(1, gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_AURA])
        assertEquals(3, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_LIFE])
    }

    @Test
    fun thornyPathTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 0)

        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_THORNY_PATH, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_THORNY_PATH, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_LIFE])
        assertEquals(0, gameStatus.distanceToken)
        assertEquals(2, gameStatus.dust)
    }

    @Test
    fun ironPowderWindAroundTest() = runTest {
        resetValue(0, 1, 10, 10, 4, 2)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))

        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_IRON_POWDER_WIND_AROUND, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_IRON_POWDER_WIND_AROUND, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_AURA])
        assertEquals(1, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_FLARE])
        assertEquals(2, gameStatus.player1.aura)
    }

    @Test
    fun blackArmorTest() = runTest {
        resetValue(0, 0, 10, 10, 4, 2)

        gameStatus.processDamage(
            PlayerEnum.PLAYER2, CommandEnum.CHOOSE_AURA, Pair(1, 1), false,
            null, null, -1)
        gameStatus.processDamage(
            PlayerEnum.PLAYER2, CommandEnum.CHOOSE_AURA, Pair(1, 1), false,
            null, null, -1)
        addCard(PlayerEnum.PLAYER2, CardName.SHISUI_BLACK_ARMOR, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SHISUI_BLACK_ARMOR, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER2)[INDEX_LACERATION_AURA])
        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun padmaCutDownTest() = runTest {
        resetValue(1, 5, 10, 10, 4, 2)
        gameStatus.player2.flare = 3

        gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER2)[INDEX_LACERATION_AURA] = 2
        addCard(PlayerEnum.PLAYER2, CardName.SHISUI_PADMA_CUT_DOWN, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.SHISUI_PADMA_CUT_DOWN, LocationEnum.SPECIAL_CARD)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_FOUR))

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(8, gameStatus.player1.life)
    }

    @Test
    fun upalaTearTest() = runTest {
        suspend fun returnTest() {
            gameStatus.endPhase()

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.SHISUI_UPALA_TEAR, LocationEnum.SPECIAL_CARD))
        }

        resetValue(1, 1, 10, 10, 3, 2)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_UPALA_TEAR, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_UPALA_TEAR, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(3, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_LIFE])
        assertEquals(2, gameStatus.player1.getLacerationToken(PlayerEnum.PLAYER1)[INDEX_LACERATION_FLARE])

        returnTest()
    }

    @Test
    fun abudaEatTest() = runTest {
        suspend fun returnTest() {
            gameStatus.processDamage(
                PlayerEnum.PLAYER2, CommandEnum.CHOOSE_AURA, Pair(1, 1), false,
                null, null, -1)
            gameStatus.processDamage(
                PlayerEnum.PLAYER2, CommandEnum.CHOOSE_AURA, Pair(1, 1), false,
                null, null, -1)
            assertEquals(false, haveCard(PlayerEnum.PLAYER2, CardName.SHISUI_ABUDA_EAT, LocationEnum.SPECIAL_CARD))
            gameStatus.processDamage(
                PlayerEnum.PLAYER2, CommandEnum.CHOOSE_AURA, Pair(1, 1), false,
                null, null, -1)
            assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.SHISUI_ABUDA_EAT, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 2, 10, 10, 3, 2)
        gameStatus.player2.flare = 2

        addCard(PlayerEnum.PLAYER2, CardName.SHISUI_ABUDA_EAT, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.SHISUI_ABUDA_EAT, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.flare)
        assertEquals(2, gameStatus.player2.getLacerationToken(PlayerEnum.PLAYER2)[INDEX_LACERATION_AURA])
        assertEquals(10, gameStatus.player2.life)

        returnTest()
    }

    @Test
    fun shisuiPlaceOfDeathTest() = runTest {
        resetValue(1, 1, 1, 10, 3, 2)
        gameStatus.player1.flare = 2; gameStatus.player2.flare = 10
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SHISUI_SHISUI_PLACE_OF_DEATH, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHISUI_SHISUI_PLACE_OF_DEATH, LocationEnum.SPECIAL_CARD)

        assertEquals(2, gameStatus.player1.flare)
        assertEquals(2, gameStatus.player2.flare)
        assertEquals(10, gameStatus.player1.enchantmentCard[NUMBER_SHISUI_SHISUI_PLACE_OF_DEATH]?.getNap())

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(false, gameStatus.gameEnd)
    }
}