package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.gamelogic.megamispecial.Stratagem
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class ShinraTest: ApplicationTest() {
    @Before
    fun setting(){
        gameStatus.player1.megamiOne = MegamiEnum.SHINRA
        MegamiEnum.SHINRA.settingForOriginal(PlayerEnum.PLAYER1, gameStatus)
    }

    @Test
    fun iblonTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.YOUR_DECK_TOP)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)

        assertEquals(2, gameStatus.player2.coverCard.size)
    }

    @Test
    fun banlonTest() = runTest {
        resetValue(1, 2, 10, 10, 4, 0)

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.HAND)

        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(10, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player1.hand.size)
    }

    @Test
    fun kibenShinsanTest() = runTest {
        resetValue(1, 2, 10, 10, 4, 0)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GIBACK, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_KIBEN, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_KIBEN, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.normalCardDeck.size)
    }

    @Test
    fun kibenGuemoTest() = runTest {
        resetValue(1, 3, 10, 10, 4, 0)
        gameStatus.player1.stratagem = Stratagem.GUE_MO
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GUHAB, LocationEnum.DISCARD_YOUR)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_GUHAB + SECOND_PLAYER_START_NUMBER)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_KIBEN, LocationEnum.HAND)

        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_KIBEN, LocationEnum.HAND)

        assertEquals(6, gameStatus.player2.life)
    }


    @Test
    fun inyongTest() = runTest {
        resetValue(1, 3, 10, 10, 4, 0)

        addCard(PlayerEnum.PLAYER2, CardName.YURINA_GUHAB, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_GUHAB + SECOND_PLAYER_START_NUMBER)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_INYONG, LocationEnum.HAND)

        assertEquals(7, gameStatus.player2.life)
        assertEquals(true, gameStatus.endCurrentPhase)
    }

    @Test
    fun jangdamShinsanTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_JANGDAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_JANGDAM, LocationEnum.HAND)

        assertEquals(1, gameStatus.player1.normalCardDeck.size)
        assertEquals(1, gameStatus.player1.concentration)
    }

    @Test
    fun jangdamGuemoTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 0)
        gameStatus.player1.stratagem = Stratagem.GUE_MO

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_JANGDAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_JANGDAM, LocationEnum.HAND)

        assertEquals(true, gameStatus.player2.shrink)
        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun nonpaTest() = runTest {
        resetValue(0, 0, 10, 10, 0, 1)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.DISCARD_YOUR)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_NONPA, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_NONPA, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.discard.size)

        gameStatus.endPhase(); startPhase()

        assertEquals(1, gameStatus.player2.discard.size)
    }

    @Test
    fun dasicIhaeShinsanTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 3)
        gameStatus.player1.flare = 2

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SHINRA_CHEONJI_BANBAG)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_CHEONJI_BANBAG, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_DASIG_IHAE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_DASIG_IHAE, LocationEnum.SPECIAL_CARD)

        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.SHINRA_CHEONJI_BANBAG, LocationEnum.ENCHANTMENT_ZONE))
    }

    @Test
    fun dasicIhaeGuemoTest() = runTest {
        resetValue(3, 0, 10, 10, 3, 3)
        gameStatus.player1.flare = 2; gameStatus.player1.stratagem = Stratagem.GUE_MO


        addCard(PlayerEnum.PLAYER2, CardName.YURINA_APDO, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_YURINA_APDO + SECOND_PLAYER_START_NUMBER)))
        addReactData(PlayerEnum.PLAYER1, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_DASIG_IHAE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_DASIG_IHAE, LocationEnum.SPECIAL_CARD)

        assertEquals(false, haveCard(PlayerEnum.PLAYER2, CardName.YURINA_APDO, LocationEnum.ENCHANTMENT_ZONE))
        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun cheonjiBanbag() = runTest {
        resetValue(0, 0, 10, 10, 3, 3)
        gameStatus.player1.flare = 2; gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_CHEONJI_BANBAG, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)

        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_CHEONJI_BANBAG, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player1.flare)
    }

    @Test
    fun samraBanShoTest() = runTest {
        resetValue(0, 0, 8, 10, 3, 2)
        gameStatus.player1.flare = 6

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_SAMRA_BAN_SHO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_SAMRA_BAN_SHO, LocationEnum.SPECIAL_CARD)

        assertEquals(10, gameStatus.player1.life)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_JANGDAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_JANGDAM, LocationEnum.HAND)

        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun zhenYenShinsanTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 2)
        gameStatus.player2.stratagem = Stratagem.SHIN_SAN

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_BANLON, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_JANGDAM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_NONPA, LocationEnum.YOUR_DECK_TOP)

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_ZHEN_YEN, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SHINRA_ZHEN_YEN, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)

        assertEquals(9, gameStatus.player1.life)
    }

    @Test
    fun zhenYenGuemoTest() = runTest {
        resetValue(2, 0, 10, 10, 3, 2)
        gameStatus.player2.stratagem = Stratagem.GUE_MO

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_ZHEN_YEN, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SHINRA_ZHEN_YEN, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_IBLON, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun sadoShinsanTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 4)
        gameStatus.player1.fullAction = true

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_IBLON, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.DISCARD_YOUR)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NOT))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SHINRA_IBLON + SECOND_PLAYER_START_NUMBER)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SHINRA_BANLON + SECOND_PLAYER_START_NUMBER)))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))


        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_SA_DO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_SA_DO, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(2, gameStatus.player2.normalCardDeck.size)
    }

    @Test
    fun sadoGuemoTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 4)
        gameStatus.player1.fullAction = true; gameStatus.player1.concentration = 1

        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_IBLON, LocationEnum.YOUR_DECK_BELOW)
        addCard(PlayerEnum.PLAYER2, CardName.SHINRA_BANLON, LocationEnum.YOUR_DECK_BELOW)

        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_TWO))
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_SA_DO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_SA_DO, LocationEnum.HAND)

        assertEquals(8, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.normalCardDeck.size)
    }

    @Test
    fun zenChiKyoTenTest() = runTest {
        resetValue(0, 1, 10, 10, 2, 4)
        gameStatus.player1.fullAction = true; gameStatus.player1.flare = 4; gameStatus.player1.stratagem = Stratagem.GUE_MO

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_SA_DO, LocationEnum.COVER_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_ZHEN_YEN, LocationEnum.COVER_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SHINRA_SA_DO, NUMBER_SHINRA_ZHEN_YEN
        )))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SHINRA_ZHEN_YEN)))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, mutableListOf(
            NUMBER_SHINRA_SA_DO
        )))

        addCard(PlayerEnum.PLAYER1, CardName.SHINRA_ZEN_CHI_KYO_TEN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SHINRA_ZEN_CHI_KYO_TEN, LocationEnum.SPECIAL_CARD)

        assertEquals(0, gameStatus.player1.flare)
        assertEquals(6, gameStatus.player2.life)
        assertEquals(0, gameStatus.player2.aura)
    }

}