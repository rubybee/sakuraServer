package com.sakurageto.gamelogic

import com.sakurageto.card.AttackBuff
import com.sakurageto.card.MadeAttack
import com.sakurageto.card.PlayerEnum
import com.sakurageto.card.RangeBuff

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus) {

    var distance = 10
    var dust = 0

    fun dustToAura(player: PlayerEnum, number: Int){
        if(number > dust){
            dust -= when (player){
                PlayerEnum.PLAYER1 -> player1.plusAura(dust)
                PlayerEnum.PLAYER2 -> player2.plusAura(dust)
            }
        }
        else{
            dust -= when (player){
                PlayerEnum.PLAYER1 -> player1.plusAura(number)
                PlayerEnum.PLAYER2 -> player2.plusAura(number)
            }
        }
    }

    fun addPreAttackZone(player: PlayerEnum, attack: MadeAttack){
        when (player){
            PlayerEnum.PLAYER1 -> player1.addPreAttackZone(attack)
            PlayerEnum.PLAYER2 -> player2.addPreAttackZone(attack)
        }
    }
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