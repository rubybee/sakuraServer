package com.sakurageto

import com.sakurageto.card.CardName
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum
import com.sakurageto.protocol.SakuraArrayData
import com.sakurageto.protocol.SakuraBaseData
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class BasicOperationTest: ApplicationTest() {
    @Test
    fun chasmTest() = runTest {
        gameStatus.distanceToken = 3; gameStatus.dust = 10
        player1Connection.putReceiveData(SakuraArrayData(CommandEnum.SELECT_NAP, mutableListOf(0, 2))) //aura, dust
        addCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.HAND)
        gameStatus.endPhase(); startPhase(); gameStatus.nowPhase = GameStatus.MAIN_PHASE
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.REACT_NO, -1))
        player1Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_LIFE, -1))
        addCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        useCard(PlayerEnum.PLAYER2, CardName.YURINA_ILSUM, LocationEnum.HAND)
        assertEquals(true, haveCard(PlayerEnum.PLAYER1, CardName.YURINA_APDO, LocationEnum.COVER_CARD))
    }

    @Test
    fun goForwardTest() = runTest{
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GO_FORWARD, -1)
        assertEquals(4, gameStatus.player1.aura)
        assertEquals(9, gameStatus.distanceToken)
    }

    @Test
    fun goBackWardTest() = runTest {
        resetValue(3, 3, 10, 10, 9, 0)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_GO_BACKWARD, -1)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(10, gameStatus.distanceToken)
    }

    @Test
    fun windAroundTest() = runTest {
        resetValue(3, 3, 10, 10, 10, 10)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_WIND_AROUND, -1)
        assertEquals(4, gameStatus.player1.aura)
        assertEquals(9, gameStatus.dust)
    }

    @Test
    fun incubateTest() = runTest {
        resetValue(3, 3, 10, 10, 10, 10)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_INCUBATE, -1)
        assertEquals(2, gameStatus.player1.aura)
        assertEquals(1, gameStatus.player1.flare)
    }

    @Test
    fun breakAwayTest() = runTest {
        resetValue(3, 3, 10, 10, 3, 1)
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_BREAK_AWAY, -1)
        assertEquals(1, gameStatus.dust)
        assertEquals(3, gameStatus.distanceToken)

        gameStatus.distanceToken = 2
        gameStatus.doBasicOperation(PlayerEnum.PLAYER1, CommandEnum.ACTION_BREAK_AWAY, -1)
        assertEquals(0, gameStatus.dust)
        assertEquals(3, gameStatus.distanceToken)
    }
}