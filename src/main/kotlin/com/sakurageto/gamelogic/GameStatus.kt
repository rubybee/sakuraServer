package com.sakurageto.gamelogic

import com.sakurageto.card.PlayerEnum

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus) {
    fun getPlayerLife(player: PlayerEnum): Int{
        when (player){
            PlayerEnum.PLAYER1 -> return player1.life
            PlayerEnum.PLAYER2 -> return player2.life
        }
    }

}