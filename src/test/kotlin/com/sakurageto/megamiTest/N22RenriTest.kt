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
import com.sakurageto.protocol.SakuraBaseData
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class N22RenriTest: ApplicationTest() {
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
        gameStatus.player1.megamiOne = MegamiEnum.RENRI
        MegamiEnum.RENRI.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)

        gameStatus.player1.unselectedCard.add(CardName.RENRI_FALSE_STAB)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_TEMPORARY_EXPEDIENT)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_FISHING)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_BLACK_AND_WHITE)
        gameStatus.player1.unselectedCard.add(CardName.RENRI_IRRITATING_GESTURE)
    }

    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.RENRI_FALSE_STAB, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RENRI_TEMPORARY_EXPEDIENT, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RENRI_BLACK_AND_WHITE, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.RENRI_IRRITATING_GESTURE, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RENRI_FLOATING_CLOUDS, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.RENRI_FISHING, CardClass.NORMAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.RENRI_PULLING_FISHING, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)

        cardTypeTest(CardName.RENRI_RU_RU_RA_RA_RI, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.RENRI_O_RI_RE_TE_RA_RE_RU, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.REACTION)
        cardTypeTest(CardName.RENRI_RENRI_THE_END, CardClass.SPECIAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.RENRI_ENGRAVED_GARMENT, CardClass.SPECIAL, CardType.UNDEFINED, SubType.UNDEFINED)
        cardTypeTest(CardName.KIRIKO_SHAMANISTIC_MUSIC, CardClass.SPECIAL, CardType.ATTACK, SubType.NONE)
    }

    @Test
    fun falseStabTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 0)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun falseStabPerjuryTest() = runTest {
        resetValue(0, 5, 10, 10, 2, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, NUMBER_YURINA_CHAM, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun temporaryExpedientTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 0)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.DISCARD_YOUR)

        addReactData(PlayerEnum.PLAYER2)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_TEMPORARY_EXPEDIENT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_TEMPORARY_EXPEDIENT, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun temporaryExpedientPerjuryTest() = runTest {
        resetValue(0, 2, 10, 10, 3, 0)

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_TEMPORARY_EXPEDIENT, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_TEMPORARY_EXPEDIENT, NUMBER_YURINA_CHAM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.COVER_CARD))
    }

    @Test
    fun blackAndWhiteTest() = runTest {
        resetValue(0, 2, 3, 10, 2, 4)

        gameStatus.doBasicOperation(PlayerEnum.PLAYER2, CommandEnum.ACTION_BREAK_AWAY, -1)

        addCard(PlayerEnum.PLAYER2, CardName.RENRI_BLACK_AND_WHITE, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.RENRI_BLACK_AND_WHITE, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.life)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun blackAndWhitePerjuryTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 0)

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_BLACK_AND_WHITE, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_BLACK_AND_WHITE, NUMBER_YURINA_CHAM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.COVER_CARD))
    }

    @Test
    fun irritatingGesturePerjuryTest() = runTest {
        resetValue(0, 2, 10, 10, 2, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, NUMBER_RENRI_FALSE_STAB, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.DISCARD_YOUR))

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_IRRITATING_GESTURE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_IRRITATING_GESTURE, LocationEnum.HAND)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.HAND))
        assertEquals(true, gameStatus.player2.shrink)
    }

    @Test
    fun floatingCloudsPerjuryTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FLOATING_CLOUDS, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FLOATING_CLOUDS, NUMBER_RENRI_FLOATING_CLOUDS, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.COVER_CARD))
    }

    @Test
    fun fishingPerjuryTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 0)

        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FISHING, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FISHING, NUMBER_RENRI_FISHING, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(2, gameStatus.distanceToken)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_FISHING, LocationEnum.COVER_CARD))
    }

    @Test
    fun pullingFishingTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 5)

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_PULLING_FISHING, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_PULLING_FISHING, LocationEnum.HAND)

        assertEquals(2, gameStatus.distanceToken)

        startPhase()
        startPhase()
        startPhase()

        assertEquals(1, gameStatus.distanceToken)
    }

    @Test
    fun rururarariTest() = runTest {
        suspend fun returnTest() {
            gameStatus.endPhase()

            assertEquals(0, gameStatus.player2.aura)
            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_RU_RU_RA_RA_RI, LocationEnum.SPECIAL_CARD))
        }

        resetValue(0, 5, 10, 10, 2, 5)
        gameStatus.player1.flare = 4

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addReactData(PlayerEnum.PLAYER2)

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, LocationEnum.HAND)
        useCardPerjury(PlayerEnum.PLAYER1, CardName.RENRI_FALSE_STAB, NUMBER_RENRI_FALSE_STAB, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.RENRI_RU_RU_RA_RA_RI, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_RU_RU_RA_RA_RI, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(1, gameStatus.player2.aura)
        assertEquals(8, gameStatus.player2.life)

        returnTest()
    }

    @Test
    fun ranararomireriraTest() = runTest {
        resetValue(5, 1, 10, 10, 3, 5)
        gameStatus.player1.flare = 4

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.RENRI_RA_NA_RA_RO_MI_RE_RI_RA, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(8, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR))
    }

    @Test
    fun orireterareruTest() = runTest {
        suspend fun returnTest() {
            addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.DISCARD_YOUR)
            addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
            addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.DISCARD_YOUR)

            gameStatus.endPhase()

            assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, LocationEnum.SPECIAL_CARD))
        }

        resetValue(5, 1, 10, 10, 3, 5)
        gameStatus.player1.flare = 2

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER1, CardName.RENRI_O_RI_RE_TE_RA_RE_RU, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_RENRI_FISHING
        )))
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.flare)
        assertEquals(10, gameStatus.player1.life)

        returnTest()
    }

    @Test
    fun renriTheEndTest() = runTest {
        resetValue(1, 2, 10, 10, 3, 5)
        gameStatus.player1.flare = 1

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_ENGRAVED_GARMENT, LocationEnum.ADDITIONAL_CARD)

        addCard(PlayerEnum.PLAYER1, CardName.RENRI_RENRI_THE_END, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.RENRI_RENRI_THE_END, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)

        gameStatus.endPhase(); startPhase()

        gameStatus.player1.flare = 5

        addReactData(PlayerEnum.PLAYER1, CardName.RENRI_ENGRAVED_GARMENT, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)

        assertEquals(3, gameStatus.player1.enchantmentCard[NUMBER_RENRI_RENRI_THE_END]?.getNap())
        assertEquals(9, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(10, gameStatus.player1.life)

        gameStatus.endPhase(); startPhase()

        assertEquals(2, gameStatus.player1.enchantmentCard[NUMBER_RENRI_RENRI_THE_END]?.getNap())
        assertEquals(CardName.SHINRA_WANJEON_NONPA,
            gameStatus.getCardFrom(PlayerEnum.PLAYER1, NUMBER_RENRI_ENGRAVED_GARMENT, LocationEnum.YOUR_USED_CARD)?.card_data?.card_name)

        resetValue(0, 0, 10, 10, 3, 3)

        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.SELECT_ENCHANTMENT_END))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.DECK_RECONSTRUCT_NO))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
        gameStatus.startPhase()

        assertEquals(10, gameStatus.player1.life)

        player1Connection.putReceiveData(makeData(CommandEnum.FULL_POWER_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_END_TURN))

        gameStatus.mainPhase()

        assertEquals(CardName.UTSURO_MANG_A,
            gameStatus.getCardFrom(PlayerEnum.PLAYER1, NUMBER_RENRI_ENGRAVED_GARMENT, LocationEnum.SPECIAL_CARD)?.card_data?.card_name)

        startPhase()
        resetValue(0, 2, 10, 10, 3, 3)
        gameStatus.player1.flare = 3

        useCard(PlayerEnum.PLAYER1, CardName.RENRI_ENGRAVED_GARMENT, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(8, gameStatus.player2.life)
    }


}