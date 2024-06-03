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
import kotlin.test.assertNotEquals

class SaineTest: ApplicationTest() {
    @Test
    fun cardTypeTest() = runTest {
        cardTypeTest(CardName.SAINE_DOUBLEBEGI, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SAINE_HURUBEGI, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.SAINE_MOOGECHOO, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.SAINE_GANPA, CardClass.NORMAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.SAINE_GWONYUCK, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.SAINE_CHOONGEMJUNG, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.REACTION)
        cardTypeTest(CardName.SAINE_MOOEMBUCK, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.FULL_POWER)
        cardTypeTest(CardName.SAINE_YULDONGHOGEK, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.SAINE_HANGMUNGGONGJIN, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)
        cardTypeTest(CardName.SAINE_EMMOOSHOEBING, CardClass.SPECIAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.SAINE_JONGGEK, CardClass.SPECIAL, CardType.ATTACK, SubType.REACTION)

        cardTypeTest(CardName.SAI_TOKO_ENSEMBLE, CardClass.NORMAL, CardType.ATTACK, SubType.REACTION)
        cardTypeTest(CardName.SAINE_ACCOMPANIMENT, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.SAINE_DUET_TAN_JU_BING_MYEONG, CardClass.SPECIAL, CardType.BEHAVIOR, SubType.NONE)

        cardTypeTest(CardName.SAINE_BETRAYAL, CardClass.NORMAL, CardType.ATTACK, SubType.NONE)
        cardTypeTest(CardName.SAINE_FLOWING_WALL, CardClass.NORMAL, CardType.ENCHANTMENT, SubType.NONE)
        cardTypeTest(CardName.SAINE_JEOL_CHANG_JEOL_HWA, CardClass.SPECIAL, CardType.ATTACK, SubType.REACTION)
    }

    @Test
    fun moogechooTest() = runTest {
        resetValue(1, 1, 10, 10, 3, 10)
        player2Connection.putReceiveData(makeData(PlayerEnum.PLAYER2, CommandEnum.REACT_USE_CARD_HAND, CardName.SAINE_MOOGECHOO))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_MOOGECHOO, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_MOOGECHOO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_MOOGECHOO, LocationEnum.HAND)
        assertEquals(9, gameStatus.player1.life)
        assertEquals(9, gameStatus.dust)
        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun ganpaTest() = runTest {
        resetValue(1, 2, 10, 10, 5, 10)
        gameStatus.player1.flare = 6
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_YULDONGHOGEK, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_GANPA, LocationEnum.HAND)
        for(i in 1..2){
            player2Connection.putReceiveData(makeData(PlayerEnum.PLAYER2, CommandEnum.REACT_USE_CARD_HAND, CardName.SAINE_GANPA))
            player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
            player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
            player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        }
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_YULDONGHOGEK, LocationEnum.SPECIAL_CARD)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(6, gameStatus.distanceToken)
        assertEquals(1, gameStatus.player2.aura)
        assertEquals(10, gameStatus.player2.life)
    }

    @Test
    fun gwonyuckTest() = runTest {
        resetValue(1, 1, 10, 10, 5, 5)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 2)))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_GWONYUCK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_GWONYUCK, LocationEnum.HAND)
        gameStatus.endPhase(); startPhase()
        assertEquals(6, gameStatus.distanceToken)
        assertEquals(3, gameStatus.getAdjustSwellDistance())
    }

    @Test
    fun choongemjungTest() = runTest {
        resetValue(1, 1, 10, 10, 4, 5)
        player2Connection.putReceiveData(makeData(PlayerEnum.PLAYER2, CommandEnum.REACT_USE_CARD_HAND, CardName.SAINE_CHOONGEMJUNG))
        player2Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 1)))
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        player1Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_CHOONGEMJUNG, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND)
        gameStatus.endPhase(); startPhase()
        assertEquals(0, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun mooembuckTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 10)
        gameStatus.player1.fullAction = true; gameStatus.player1.flare = 5
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 5)))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_JONGGEK, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_MOOEMBUCK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_MOOEMBUCK, LocationEnum.HAND)
        gameStatus.endPhase(); startPhase()
        addCard(PlayerEnum.PLAYER2, CardName.HIMIKA_RAPIDFIRE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.HIMIKA_MAGNUMCANON, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.HIMIKA_REDBULLET, LocationEnum.SPECIAL_CARD)
        player1Connection.putReceiveData(makeData(PlayerEnum.PLAYER1, CommandEnum.REACT_USE_CARD_SPECIAL, CardName.SAINE_JONGGEK))
        player1Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        useCard(PlayerEnum.PLAYER2, CardName.HIMIKA_MAGNUMCANON, LocationEnum.HAND)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)
        assertEquals(1, getCard(PlayerEnum.PLAYER1, CardName.SAINE_MOOEMBUCK, LocationEnum.ENCHANTMENT_ZONE)?.getNap())


        player1Connection.putReceiveData(makeData(PlayerEnum.PLAYER1, CommandEnum.REACT_USE_CARD_SPECIAL, CardName.SAINE_JONGGEK))
        player1Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        useCard(PlayerEnum.PLAYER2, CardName.HIMIKA_REDBULLET, LocationEnum.SPECIAL_CARD)
        assertEquals(4, gameStatus.player2.life)
        assertEquals(9, gameStatus.player1.life)


        gameStatus.distanceToken = 6
        player1Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        useCard(PlayerEnum.PLAYER2, CardName.HIMIKA_RAPIDFIRE, LocationEnum.HAND)
        assertEquals(7, gameStatus.player1.life)
    }

    @Test
    fun hangmunggongjinTest() = runTest {
        resetValue(0, 5, 10, 10, 3, 10)
        gameStatus.player1.flare = 3
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HANGMUNGGONGJIN, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_HANGMUNGGONGJIN, LocationEnum.SPECIAL_CARD)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(3, gameStatus.player2.aura)
        assertEquals(5, gameStatus.distanceToken)
    }

    @Test
    fun emmooshoebingTest() = runTest {
        resetValue(0, 0, 10, 10, 3, 10)
        gameStatus.player2.flare = 2
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(1, gameStatus.player1.flare)
        assertEquals(9, gameStatus.player1.life)
        assertEquals(9, gameStatus.player2.life)


        gameStatus.endPhase(); startPhase()
        gameStatus.endPhase(); startPhase()
        assertNotEquals(null, getCard(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun soundOfIceTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 10)

        addCard(PlayerEnum.PLAYER1, CardName.SAINE_SOUND_OF_ICE, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_SOUND_OF_ICE, LocationEnum.HAND)

        assertEquals(0, gameStatus.player2.aura)
        assertEquals(11, gameStatus.dust)
    }

    @Test
    fun soundOfIceReactTest() = runTest {
        resetValue(2, 1, 10, 10, 3, 10)

        addCard(PlayerEnum.PLAYER2, CardName.SAINE_SOUND_OF_ICE, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_SOUND_OF_ICE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)

        assertEquals(0, gameStatus.player1.aura)
    }

    @Test
    fun ensembleSaineTest() = runTest {
        resetValue(0, 1, 10, 10, 3, 3)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAI_TOKO_ENSEMBLE, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_EMMOOSHOEBING, LocationEnum.YOUR_USED_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SAI_TOKO_ENSEMBLE, LocationEnum.HAND)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(4, gameStatus.distanceToken)
    }

    @Test
    fun accompanimentTest() = runTest {
        resetValue(0, 3, 10, 10, 5, 5)
        gameStatus.player1.flare = 3; gameStatus.player2.flare = 2
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 4)))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_ACCOMPANIMENT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_POBARAM, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_JONGGEK, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_ACCOMPANIMENT, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        assertEquals(2, gameStatus.player2.aura)
        assertEquals(2, gameStatus.player1.flare)

        gameStatus.endPhase(); startPhase(); gameStatus.player2.fullAction = true
        addReactData(PlayerEnum.PLAYER1, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        for(i in 1..5){
            player2Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        }
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_KANZA_DO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_KANZA_DO, LocationEnum.SPECIAL_CARD)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(10, gameStatus.player1.life)
    }

    @Test
    fun tanJuBingMyeongTest() = runTest {
        resetValue(0, 2, 10, 10, 5, 5)
        gameStatus.player1.flare = 2
        addCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_DUET_TAN_JU_BING_MYEONG, LocationEnum.SPECIAL_CARD)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND)
        assertEquals(1, gameStatus.player1.flare)
        assertNotEquals(null, getCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND))

        gameStatus.endPhase(); startPhase()
        gameStatus.endPhase(); startPhase()

        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.HIMIKA_SHOOT, LocationEnum.HAND)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun betrayerTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)
        gameStatus.player2.flare = 2
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_BETRAYAL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_EMMOOSHOEBING, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_BETRAYAL, LocationEnum.HAND)
        assertEquals(7, gameStatus.player2.life)
    }

    @Test
    fun flowingWallTest() = runTest {
        resetValue(0,2, 10, 10, 5, 0)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addReactData(PlayerEnum.PLAYER2, CardName.CARD_UNNAME, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_FLOWING_WALL, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_BETRAYAL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_FLOWING_WALL, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_BETRAYAL, LocationEnum.HAND)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(6, gameStatus.player2.life)
    }

    @Test
    fun julChangWhaTest() = runTest{
        resetValue(2,3, 10, 10, 5, 5)
        gameStatus.player2.flare = 1
        addReactData(PlayerEnum.PLAYER2, CardName.SAINE_JEOL_CHANG_JEOL_HWA, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_JEOL_CHANG_JEOL_HWA, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(true, gameStatus.endCurrentPhase)
    }
}