package com.sakurageto.gamelogic

import com.sakurageto.card.AttackBuff
import com.sakurageto.card.MadeAttack
import com.sakurageto.card.PlayerEnum
import com.sakurageto.card.RangeBuff
import com.sakurageto.Connection
import com.sakurageto.protocol.UsedCardReturn

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus, val player1_socket: Connection, val player2_socket: Connection) {

    var distance = 10
    var dust = 0

    var player1_life_listner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()
    var player2_life_listner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()


    fun addImmediateLifeListner(player: PlayerEnum, listner: ImmediateBackListner){
        when(player){
            PlayerEnum.PLAYER1 -> player1_life_listner.addLast(listner)
            PlayerEnum.PLAYER2 -> player2_life_listner.addLast(listner)
        }
    }
    //return endgame
    suspend fun lifeToSelfFlare(player: PlayerEnum, number: Int, reconstruct: Boolean): Boolean{
        var now_player: PlayerStatus

        when(player){
            PlayerEnum.PLAYER1 -> now_player = player1
            PlayerEnum.PLAYER2 -> now_player = player2
        }

        val before = now_player.life

        if(now_player.life > number){
            now_player.life -= number
            now_player.flare += number
        }
        else{
            return true
        }

        when(player){
            PlayerEnum.PLAYER1 -> {
                for(i in 0..player1_life_listner.size){
                    val now = player1_life_listner.first()
                    player1_life_listner.removeFirst()
                    if(now.IsItBack(before, now_player.life, reconstruct)){
                        if(now_player.usedToSpecial(now.card_name)){
                            UsedCardReturn(player1_socket, now.card_name)
                        }
                    }
                    else{
                        player1_life_listner.addLast(now)
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                for(i in 0..player2_life_listner.size){
                    val now = player2_life_listner.first()
                    player2_life_listner.removeFirst()
                    if(now.IsItBack(before, now_player.life, reconstruct)){
                        if(now_player.usedToSpecial(now.card_name)){
                            UsedCardReturn(player2_socket, now.card_name)
                        }
                    }
                    else{
                        player2_life_listner.addLast(now)
                    }
                }
            }
        }

        return false
    }

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