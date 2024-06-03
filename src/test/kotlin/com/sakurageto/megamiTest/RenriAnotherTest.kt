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

class RenriAnotherTest: ApplicationTest() {
    private suspend fun useCardPerjury(playerEnum: PlayerEnum, card_name: CardName, original_card: Int, location: LocationEnum){
        when(location){
            LocationEnum.HAND -> {
                gameStatus.getCardFrom(playerEnum, original_card, LocationEnum.HAND)?.let {
                    gameStatus.useCardPerjury(playerEnum, it, card_name.toCardNumber(true), LocationEnum.HAND)
                }
            }

            LocationEnum.READY_SOLDIER_ZONE -> {
                gameStatus.getCardFrom(playerEnum, original_card, LocationEnum.READY_SOLDIER_ZONE)?.let {
                    gameStatus.useCardPerjury(playerEnum, it, card_name.toCardNumber(true), LocationEnum.READY_SOLDIER_ZONE)
                }
            }
            else -> {}
        }
    }

    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.RENRI_A1
        MegamiEnum.RENRI_A1.settingForAnother(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.RENRI_DECEPTION_FOG, CardClass.NORMAL, CardType.UNDEFINED, SubType.UNDEFINED)
        cardTypeTest(CardName.RENRI_SIN_SOO, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RENRI_RI_RA_RU_RI_RA_RO, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)

        cardTypeTest(CardName.RENRI_FALSE_WEAPON, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RENRI_ESSENCE_OF_BLADE, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RENRI_FIRST_SAKURA_ORDER, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
    }


    @Test
    fun deceptionFogTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 0)
        gameStatus.player1.megamiTwo = MegamiEnum.HONOKA
        gameStatus.player2.megamiOne = MegamiEnum.HONOKA; gameStatus.player2.megamiTwo = MegamiEnum.YURINA

        addCard(PlayerEnum.PLAYER1, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.ADDITIONAL_CARD)
        player1Connection.putReceiveData(makeData(
            CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_HONOKA_SPIRIT_SIK
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.HONOKA_GUARDIAN_SPIRIT_SIK, LocationEnum.DECK))
        assertEquals(false, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun deceptionFogDiveTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)
        gameStatus.player1.megamiTwo = MegamiEnum.HONOKA
        gameStatus.player2.megamiOne = MegamiEnum.HATSUMI_A1; gameStatus.player2.megamiTwo = MegamiEnum.YURINA
        gameStatus.player2.forwardDiving = true

        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_WATER_BALL, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(
            CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
                NUMBER_HATSUMI_WATER_BALL
            )))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)

        assertEquals(true, gameStatus.player2.forwardDiving)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.DISCARD_YOUR))
        assertEquals(0, gameStatus.gameLogger.playerUseCardNumber(PlayerEnum.PLAYER1))
    }

    @Test
    fun deceptionFogLighthouseLightHouseTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)
        gameStatus.player1.megamiTwo = MegamiEnum.HONOKA
        gameStatus.player2.megamiOne = MegamiEnum.HATSUMI_A1; gameStatus.player2.megamiTwo = MegamiEnum.YURINA

        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.HATSUMI_WATER_BALL, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(
            CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
                NUMBER_HATSUMI_WATER_BALL
            )))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_DECEPTION_FOG, LocationEnum.DISCARD_YOUR))
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.HATSUMI_KIRAHARI_LIGHTHOUSE, LocationEnum.YOUR_USED_CARD))
        assertEquals(0, gameStatus.gameLogger.playerUseCardNumber(PlayerEnum.PLAYER1))
    }

    @Test
    fun sinsooTest() = runTest {
        resetValue(0, 0, 10, 10, 2, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RENRI_FALSE_WEAPON
        )))

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_SIN_SOO, LocationEnum.DISCARD_YOUR)
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, true)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_WEAPON, LocationEnum.DECK))
    }

    @Test
    fun riRaRuRiRaRoTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 3)

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_RI_RA_RU_RI_RA_RO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_RI_RA_RU_RI_RA_RO, LocationEnum.SPECIAL_CARD)
        gameStatus.endPhase()
        assertEquals(9, gameStatus.player1.life)
        assertEquals(3, gameStatus.player1.enchantmentCard[NUMBER_RENRI_RI_RA_RU_RI_RA_RO]?.getNap())

        gameStatus.chojoDamageProcess(PlayerEnum.PLAYER2)
        assertEquals(9, gameStatus.player2.life)

        gameStatus.endPhase()
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_SIN_SOO, LocationEnum.DECK)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.FULL_POWER_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_END_TURN))
        gameStatus.mainPhase()
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_SIN_SOO, LocationEnum.HAND))

        resetValue(0, 5, 10, 10, 2, 0)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, NUMBER_YURINA_CHAM, LocationEnum.HAND)
        assertEquals(1, gameStatus.player1.concentration)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun falseWeaponTest() = runTest {
        resetValue(0, 4, 10, 10, 3, 3)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RENRI_FALSE_WEAPON
        )))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.COVER_CARD)
        gameStatus.deckReconstruct(PlayerEnum.PLAYER1, true)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_WEAPON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_WEAPON, LocationEnum.HAND)
        assertEquals(2, gameStatus.player2.aura)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun essenceOfBladeTest() = runTest {
        resetValue(0, 4, 10, 10, 3, 3)
        gameStatus.player2.concentration = 2

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_RI_RA_RU_RI_RA_RO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_RI_RA_RU_RI_RA_RO, LocationEnum.SPECIAL_CARD)
        gameStatus.endPhase()

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_SIN_SOO, LocationEnum.RELIC_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_ESSENCE_OF_BLADE, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_ESSENCE_OF_BLADE, NUMBER_RENRI_ESSENCE_OF_BLADE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.concentration)
        assertEquals(8, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_SIN_SOO, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun firstSakuraOrderTest() = runTest {
        resetValue(0, 4, 10, 10, 6, 3)

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_RI_RA_RU_RI_RA_RO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_RI_RA_RU_RI_RA_RO, LocationEnum.SPECIAL_CARD)
        gameStatus.endPhase()

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RENRI_FALSE_STAB
        )))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_SIN_SOO, LocationEnum.RELIC_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FIRST_SAKURA_ORDER, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FIRST_SAKURA_ORDER, NUMBER_YURINA_CHAM, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.concentration)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.DECK))

        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FIRST_SAKURA_ORDER, NUMBER_RENRI_FIRST_SAKURA_ORDER, LocationEnum.HAND)
        assertEquals(2, gameStatus.player1.aura)
    }


}