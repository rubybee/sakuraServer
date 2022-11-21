package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.card.*
import com.sakurageto.protocol.*
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus, val player1_socket: Connection, val player2_socket: Connection) {

    var distance = 10
    var dust = 0

    var player1_life_listner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()
    var player2_life_listner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()

    fun setFirstTurn(player: PlayerEnum){
        when(player){
            PlayerEnum.PLAYER1 -> {
                player2.concentration = 1
            }
            PlayerEnum.PLAYER2 -> {
                player1.concentration = 1
            }
        }
    }


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

    fun addConcentration(player: PlayerEnum): Pair<SakuraCardCommand?, SakuraCardCommand?>{
        var data_player1: SakuraCardCommand? = null
        var data_player2: SakuraCardCommand? = null
        when (player){
            PlayerEnum.PLAYER1 -> {
                when(player1.addConcentration()){
                    0 -> {
                        data_player1 = SakuraCardCommand(CommandEnum.ADD_CONCENTRATION_YOUR, null)
                        data_player2 = SakuraCardCommand(CommandEnum.ADD_CONCENTRATION_OTHER, null)
                    }
                    1 -> {
                        data_player1 = SakuraCardCommand(CommandEnum.REMOVE_SHRINK_YOUR, null)
                        data_player2 = SakuraCardCommand(CommandEnum.REMOVE_SHRINK_OTHER, null)
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                when(player2.addConcentration()){
                    0 -> {
                        data_player1 = SakuraCardCommand(CommandEnum.ADD_CONCENTRATION_OTHER, null)
                        data_player2 = SakuraCardCommand(CommandEnum.ADD_CONCENTRATION_YOUR, null)
                    }
                    1 -> {
                        data_player1 = SakuraCardCommand(CommandEnum.REMOVE_SHRINK_YOUR, null)
                        data_player2 = SakuraCardCommand(CommandEnum.REMOVE_SHRINK_OTHER, null)
                    }
                }
            }
        }

        return Pair(data_player1, data_player2)
    }

    suspend fun processTextDestruction(text: Text){
        when(text.tag){
            TextEffectTag.NEXT_ATTACK_ENCHANTMENT -> TODO()
            TextEffectTag.CHANGE_CONCENTRATION -> TODO()
            TextEffectTag.CHASM -> TODO()
            TextEffectTag.MAKE_ATTACK -> TODO()
            TextEffectTag.MOVE_SAKURA_TOKEN -> TODO()
            TextEffectTag.IMMEDIATE_RETURN -> TODO()
            TextEffectTag.END_TURN -> TODO()
            TextEffectTag.USING_CONDITION -> TODO()
            TextEffectTag.DO_NOT_NAP -> TODO()
        }
    }

    suspend fun afterCardUsed(card: Card){
        when(card.player){
            PlayerEnum.PLAYER1 -> {
                when(card.card_data.card_class){
                    CardClass.SPECIAL -> {
                        player1.used_special_card.addLast(card)
                        sendUsed(player1_socket, player2_socket, card.card_data.card_name)
                    }
                    CardClass.NORMAL -> {
                        player1.discard.addLast(card)
                        sendDiscard(player1_socket, player2_socket, card.card_data.card_name)
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                when(card.card_data.card_class){
                    CardClass.SPECIAL -> {
                        player2.used_special_card.addLast(card)
                        sendUsed(player1_socket, player2_socket, card.card_data.card_name)
                    }
                    CardClass.NORMAL -> {
                        player2.discard.addLast(card)
                        sendDiscard(player2_socket, player1_socket, card.card_data.card_name)
                    }
                }
            }
        }
    }

    suspend fun enchantmentDestruction(player: PlayerEnum, card: Card){
        val effects = card.destructionEnchantmentNormaly()
        for(i in effects){
            processTextDestruction(i)
        }

        when(player){
            PlayerEnum.PLAYER1 -> {
                player1.enchantment_card.remove(card.card_data.card_name)
            }
            PlayerEnum.PLAYER2 -> {
                player2.enchantment_card.remove(card.card_data.card_name)
            }
        }

        afterCardUsed(card)
    }

    suspend fun enchantmentReduceAll(player: PlayerEnum){
        sendReduceNapStart(player1_socket)
        sendReduceNapStart(player2_socket)

        var player1_card: HashMap<CardName, Boolean> = HashMap()
        var player2_card: HashMap<CardName, Boolean> = HashMap()

        for(i in player1.enchantment_card){
            if(i.value.reduceNapNormaly()){
                sendReduceNapSelf(player1_socket, i.key)
                sendReduceNapOther(player2_socket, i.key)
            }
            if(i.value.isItDestruction()){
                player1_card.put(i.key, true)
            }
        }
        for(i in player2.enchantment_card){
            if(i.value.reduceNapNormaly()){
                sendReduceNapOther(player1_socket, i.key)
                sendReduceNapSelf(player2_socket, i.key)
            }
            if(i.value.isItDestruction()){
                player2_card.put(i.key, true)
            }
        }

        sendReduceNapEnd(player1_socket)
        sendReduceNapEnd(player2_socket)



        when(player){
            PlayerEnum.PLAYER1 -> {
                sendStartSelectEnchantment(player1_socket)
                sendRequestEnchantmentCard(player1_socket, player1_card.keys.toMutableList(), player2_card.keys.toMutableList())
                while(true){
                    val receive = receiveEnchantment(player1_socket)
                    when(receive.first){
                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
                            if(player1_card.containsKey(receive.second)){
                                val card = player1.enchantment_card[receive.second]
                                sendDestructionEnchant(player1_socket, player2_socket, receive.second!!)
                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
                            if(player2_card.containsKey(receive.second)){
                                val card = player2.enchantment_card[receive.second]
                                sendDestructionEnchant(player2_socket, player1_socket, receive.second!!)
                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_END -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                sendStartSelectEnchantment(player2_socket)
                sendRequestEnchantmentCard(player2_socket, player2_card.keys.toMutableList(), player1_card.keys.toMutableList())
                while(true){
                    val receive = receiveEnchantment(player2_socket)
                    when(receive.first){
                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
                            if(player2_card.containsKey(receive.second)){
                                val card = player2.enchantment_card[receive.second]
                                sendDestructionEnchant(player2_socket, player1_socket, receive.second!!)
                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
                            if(player1_card.containsKey(receive.second)){
                                val card = player1.enchantment_card[receive.second]
                                sendDestructionEnchant(player1_socket, player2_socket, receive.second!!)
                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_END -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
        }



    }

    fun drawCard(player: PlayerEnum, number: Int): MutableList<CardName>{
        var return_list = mutableListOf<CardName>()
        when (player){
            PlayerEnum.PLAYER1 -> {
                for(i in 1..number){
                    return_list.add(player1.normal_card_deck.first().card_data.card_name)
                    player1.hand.add(player1.normal_card_deck.first())
                    player1.normal_card_deck.removeFirst()
                }
            }
            PlayerEnum.PLAYER2 -> {
                for(i in 1..number){
                    return_list.add(player2.normal_card_deck.first().card_data.card_name)
                    player2.hand.add(player2.normal_card_deck.first())
                    player2.normal_card_deck.removeFirst()
                }
            }
        }
        return return_list
    }

    fun insertHandToDeck(player: PlayerEnum, card_name: CardName): Boolean{
        when(player){
            PlayerEnum.PLAYER1 -> {
                for(i in player1.hand.indices){
                    if(player1.hand[i].card_data.card_name == card_name){
                        player1.normal_card_deck.addLast(player1.hand[i])
                        player1.hand.removeAt(i)
                        return true
                    }
                }
            }

            PlayerEnum.PLAYER2 -> {
                for(i in player2.hand.indices){
                    if(player2.hand[i].card_data.card_name == card_name){
                        player2.normal_card_deck.addLast(player2.hand[i])
                        player2.hand.removeAt(i)
                        return true
                    }
                }
            }
        }
        return false
    }

}