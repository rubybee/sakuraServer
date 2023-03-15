package com.sakurageto.gamelogic

import com.sakurageto.card.PlayerEnum

class Listener(
    var player: PlayerEnum,
    var cardNumber: Int,
    var action: suspend (gameStatus: GameStatus, card_number: Int, numberPara1: Int, numberPara2: Int, booleanPara1: Boolean, booleanPara2: Boolean) -> Boolean
){
    suspend fun doAction(gameStatus: GameStatus, numberPara1: Int, numberPara2: Int, booleanPara1: Boolean, booleanPara2: Boolean): Boolean{
        return this.action(gameStatus, this.cardNumber, numberPara1, numberPara2, booleanPara1, booleanPara2)
    }
    // when Listener.action return true -> pop listener at list
}