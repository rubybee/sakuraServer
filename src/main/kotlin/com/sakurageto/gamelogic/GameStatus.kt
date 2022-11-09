package com.sakurageto.gamelogic

import com.sakurageto.card.AttackBuff
import com.sakurageto.card.PlayerEnum
import com.sakurageto.card.RangeBuff

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus) {

    var distance = 10

    fun getPlayerLife(player: PlayerEnum): Int{
        when (player){
            PlayerEnum.PLAYER1 -> return player1.life
            PlayerEnum.PLAYER2 -> return player2.life
        }
    }

    fun addThisTurnAttackBuff(player: PlayerEnum, effect: AttackBuff){
        when (player){
            PlayerEnum.PLAYER1 -> player1.addAttackBuff(effect)
            PlayerEnum.PLAYER2 -> player2.addAttackBuff(effect)
        }
    }

    fun addThisTurnRangeBuff(player: PlayerEnum, effect: RangeBuff){
        when (player){
            PlayerEnum.PLAYER1 -> player1.addRangeBuff(effect)
            PlayerEnum.PLAYER2 -> player2.addRangeBuff(effect)
        }
    }

    fun addConcentration(player: PlayerEnum, number: Int){
        when (player){
            PlayerEnum.PLAYER1 -> player1.addConcentration(number)
            PlayerEnum.PLAYER2 -> player2.addConcentration(number)
        }
    }

}