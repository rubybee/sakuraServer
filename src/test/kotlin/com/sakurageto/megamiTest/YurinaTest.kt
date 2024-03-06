package com.sakurageto.megamiTest

import com.sakurageto.ApplicationTest
import com.sakurageto.card.*
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.SakuraArrayData
import com.sakurageto.protocol.SakuraBaseData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class YurinaTest: ApplicationTest() {
    @Test
    fun chamTest() = runTest {
        gameStatus.distanceToken = 3
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_LIFE, 0))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_CHAM, LocationEnum.HAND)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun ilsomTest() = runTest {
        gameStatus.player1.life = 3; gameStatus.player2.aura = 5; gameStatus.distanceToken = 3
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(2, gameStatus.player2.aura)
    }

    @Test
    fun jaruchigiTest() = runTest {
        gameStatus.player1.life = 3; gameStatus.distanceToken = 2; gameStatus.player2.aura = 5
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_JARUCHIGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_JARUCHIGI, LocationEnum.HAND)
        gameStatus.distanceToken = 3
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun guhabTest() = runTest {
        gameStatus.distanceToken = 2; gameStatus.player2.aura = 5; gameStatus.player2.life = 10
        gameStatus.player1.fullAction = true
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GUHAB, LocationEnum.HAND)
        assertEquals(2, gameStatus.player2.aura)
    }

    @Test
    fun gibackTest() = runTest {
        resetValue(5, 5, 10, 10, 3, 0)
        gameStatus.player1.concentration = 0
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GIBACK, LocationEnum.HAND)
        assertEquals(gameStatus.player1.concentration, 1)
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_LIFE, 0))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun apdoTest() = runTest {
        resetValue(0,  5, 10, 10, 4, 0)
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)
        assertEquals(2, gameStatus.player2.aura)
    }

    @Test
    fun giyenbanzoTest() = runTest {
        resetValue(0,  5, 3, 10, 4, 10)
        gameStatus.player1.fullAction = true
        player1Connection.putReceiveData(SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 4)))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_GIYENBANJO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_GIYENBANJO, LocationEnum.HAND)
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_LIFE))
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_DOUBLEBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_DOUBLEBEGI, LocationEnum.HAND)
        assertEquals(2, gameStatus.player2.aura)
        assertEquals(8, gameStatus.player2.life)
    }

    @Test
    fun pobaramTest() = runTest {
        resetValue(2,  2, 3, 10, 4, 10)
        gameStatus.player1.flare = 7; gameStatus.player2.flare = 3
        player2Connection.putReceiveData(makeData(PlayerEnum.PLAYER2, CommandEnum.REACT_USE_CARD_SPECIAL, CardName.YURINA_POBARAM))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_POBARAM, LocationEnum.SPECIAL_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_WOLYUNGNACK, LocationEnum.SPECIAL_CARD)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(0, gameStatus.player2.flare)
        assertEquals(0, gameStatus.player1.aura)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(true, gameStatus.player2.endTurn)
    }

    @Test
    fun jjockbaeTest() = runTest {
        resetValue(0,  5, 4, 10, 4, 10)
        gameStatus.player1.flare = 2
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_JJOCKBAE, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_JJOCKBAE, LocationEnum.SPECIAL_CARD)
        assertEquals(0, gameStatus.player1.flare)
        assertEquals(5, gameStatus.player1.aura)
        player2Connection.putReceiveData(makeData(PlayerEnum.PLAYER2, CommandEnum.REACT_USE_CARD_HAND, CardName.SAINE_HURUBEGI))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_LIFE))
        player1Connection.putReceiveData(makeData(CommandEnum.CHOOSE_LIFE))
        addCard(PlayerEnum.PLAYER2, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.SAINE_HURUBEGI, LocationEnum.HAND)
        assertEquals(3, gameStatus.player1.life)
        assertEquals(9, gameStatus.player2.life)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_JJOCKBAE, LocationEnum.SPECIAL_CARD))
    }

    @Test
    fun juruckTest() = runTest {
        resetValue(0,  4, 4, 6, 4, 10)
        gameStatus.player1.flare = 5; gameStatus.player1.fullAction = true
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_JURUCK, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_JURUCK, LocationEnum.SPECIAL_CARD)
        assertEquals(5, gameStatus.player1.flare)
        gameStatus.player1.life = 3
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_JURUCK, LocationEnum.SPECIAL_CARD)
        assertEquals(1, gameStatus.player2.life)
    }

    @Test
    fun nantaTest() = runTest {
        resetValue(0,  1, 3, 6, 2, 10)
        player2Connection.putReceiveData(makeData(PlayerEnum.PLAYER2, CommandEnum.REACT_USE_CARD_SPECIAL, CardName.YURINA_POBARAM))
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_NAN_TA, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_POBARAM, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_NAN_TA, LocationEnum.HAND)
        assertEquals(3, gameStatus.player2.life)
    }

    @Test
    fun beanBulletTest() = runTest {
        resetValue(0,  1, 3, 6, 2, 0)
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_BEAN_BULLET, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_BEAN_BULLET, LocationEnum.HAND)
        assertEquals(0, gameStatus.player2.aura)
        assertEquals(true, gameStatus.player2.shrink)
    }

    @Test
    fun questionAnswerTest() = runTest {
        resetValue(0, 0, 10, 10, 5, 5)
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_NAP, mutableListOf(0, 3)))
        player1Connection.putReceiveData(makeData(CommandEnum.ACTION_GO_FORWARD))
        player1Connection.putReceiveData(makeData(CommandEnum.SELECT_THREE))
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_CHAM, LocationEnum.YOUR_DECK_TOP)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_AHUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_AHUM, LocationEnum.HAND)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_QUESTION_ANSWER, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_QUESTION_ANSWER, LocationEnum.HAND)
        assertEquals(1, gameStatus.player1.aura)
        assertEquals(3, gameStatus.distanceToken)
        assertEquals(0, gameStatus.player2.normalCardDeck.size)
        assertEquals(9, gameStatus.player2.life)
    }

    @Test
    fun kanzaDoTest() = runTest {
        resetValue(0, 4, 10, 10, 5, 5)
        gameStatus.player1.flare = 5; gameStatus.player1.fullAction = true
        for(i in 1..5){
            player1Connection.putReceiveData(makeData(CommandEnum.SELECT_ONE))
        }
        player1Connection.putReceiveData(makeData(PlayerEnum.PLAYER1, CommandEnum.SELECT_CARD_REASON_CARD_EFFECT,
            mutableListOf(CardName.YURINA_POBARAM, CardName.YURINA_WOLYUNGNACK)
        ))
        player2Connection.putReceiveData(makeData(CommandEnum.REACT_NO))
        player2Connection.putReceiveData(makeData(CommandEnum.CHOOSE_AURA))
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_POBARAM, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_WOLYUNGNACK, LocationEnum.YOUR_USED_CARD)
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_KANZA_DO, LocationEnum.SPECIAL_CARD)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_KANZA_DO, LocationEnum.SPECIAL_CARD)
        assertEquals(5, gameStatus.player1.aura)
        assertEquals(2, gameStatus.player1.specialCardDeck.size)
        assertEquals(8, gameStatus.player2.life)
    }
}