package com.sakurageto.protocol

import com.sakurageto.Connection
import com.sakurageto.card.CardSet
import com.sakurageto.gamelogic.Stratagem
import com.sakurageto.protocol.CommandEnum.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

suspend fun waitReconnect(player: Connection){
    player.disconnectTime = System.currentTimeMillis()
    while (true){
        delay(1000)
        if(player.disconnectTime == -1L){
            break
        }
    }
}

suspend fun send(player: Connection, data: String){
    try {
//        println("send message to ${player.socketPlayer}: $data")
        player.session.send(data)
        val frame = player.session.incoming.receive()
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val ack = json.decodeFromString<SakuraCardCommand>(text)
            }catch (e: Exception){
                if(!(player.gameEnd)){
                    waitReconnect(player)
                    send(player, data)
                }
            }
        }
    }catch (exception: Exception){
        if(!(player.gameEnd)){
            waitReconnect(player)
            send(player, data)
        }
    }
}

//send function
suspend fun sendReduceNapStart(player: Connection){
    val data = SakuraCardCommand(REDUCE_NAP_START, -1)
    send(player, Json.encodeToString(data))
}

suspend fun sendReduceNapEnd(player: Connection){
    val data = SakuraCardCommand(REDUCE_NAP_END, -1)
    send(player, Json.encodeToString(data))
}

suspend fun sendStartSelectEnchantment(player: Connection){
    val data = SakuraCardCommand(SELECT_ENCHANTMENT_START, -1)
    send(player, Json.encodeToString(data))
}

suspend fun sendRequestEnchantmentCard(player: Connection, card_list_your: MutableList<Int>, card_list_other: MutableList<Int>){
    val dataYourPre = SakuraCardCommand(SELECT_ENCHANTMENT_YOUR, -1)
    val dataOtherPre = SakuraCardCommand(SELECT_ENCHANTMENT_OTHER, -1)
    val dataYour = SakuraSendData(SELECT_ENCHANTMENT_YOUR, card_list_your)
    val dataOther = SakuraSendData(SELECT_ENCHANTMENT_OTHER, card_list_other)
    send(player, Json.encodeToString(dataYourPre))
    send(player, Json.encodeToString(dataYour))
    send(player, Json.encodeToString(dataOtherPre))
    send(player, Json.encodeToString(dataOther))
}
suspend fun sendDestructionEnchant(mine: Connection, other: Connection, card_number: Int){
    val dataYour = SakuraCardCommand(DESTRUCTION_ENCHANTMENT_YOUR, card_number)
    val dataOther = SakuraCardCommand(DESTRUCTION_ENCHANTMENT_OTHER, card_number)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendPopCardZone(mine: Connection, other: Connection, card_number: Int, public: Boolean, command: CommandEnum){
    val dataYour = SakuraCardCommand(command, card_number)
    val dataOther = if(public) SakuraCardCommand(command.Opposite(), card_number) else SakuraCardCommand(command.Opposite(), if(CardSet.isPoison(card_number)) 1 else 0)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendAddCardZone(mine: Connection, other: Connection, card_number: Int, publicForOther: Boolean, command: CommandEnum, publicForYour: Boolean = true){
    val dataYour = if(publicForYour)SakuraCardCommand(command, card_number)
    else SakuraCardCommand(command, if(CardSet.isPoison(card_number)) 1 else if(CardSet.isSolder(card_number)) 2 else 0)
    val dataOther = if(publicForOther) SakuraCardCommand(command.Opposite(), card_number)
    else SakuraCardCommand(command.Opposite(), if(CardSet.isPoison(card_number)) 1 else if(CardSet.isSolder(card_number)) 2 else 0)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun makeAttackComplete(mine: Connection, other: Connection, card_number: Int){
    val dataYour = SakuraCardCommand(MAKE_ATTACK_COMPLETE_YOUR, card_number)
    val dataOther = SakuraCardCommand(MAKE_ATTACK_COMPLETE_OTHER, card_number)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendAttackInformation(mine: Connection, other: Connection, data: MutableList<Int>) {
    val dataYour = SakuraSendData(ATTACK_INFORMATION_YOUR, data)
    val dataOther = SakuraSendData(ATTACK_INFORMATION_OTHER, data)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendChooseDamage(mine: Connection, command: CommandEnum, aura: Int, life: Int){
    val preData = SakuraCardCommand(CHOOSE_DAMAGE, -1)
    val data = SakuraSendData(command, mutableListOf(aura, life))
    send(mine, Json.encodeToString(preData))
    send(mine, Json.encodeToString(data))
}
suspend fun sendRequestReact(mine: Connection){
    val data = SakuraCardCommand(REACT_REQUEST, -1)
    send(mine, Json.encodeToString(data))
}

suspend fun sendMoveToken(mine: Connection, other: Connection, what: TokenEnum, from: LocationEnum, to: LocationEnum, number: Int, card_number: Int){
    if(number <= 0) return
    val preData = SakuraCardCommand(MOVE_TOKEN, card_number)
    send(mine, Json.encodeToString(preData))
    send(other, Json.encodeToString(preData))
    val dataYour = SakuraSendData(MOVE_TOKEN, mutableListOf(what.real_number, from.real_number, to.real_number, number, card_number))
    val dataOther = SakuraSendData(MOVE_TOKEN, mutableListOf(what.opposite().real_number, from.Opposite().real_number, to.Opposite().real_number, number, card_number))
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendAddConcentration(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(ADD_CONCENTRATION_YOUR, -1)
    val dataOther = SakuraCardCommand(ADD_CONCENTRATION_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendDecreaseConcentration(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(DECREASE_CONCENTRATION_YOUR, -1)
    val dataOther = SakuraCardCommand(DECREASE_CONCENTRATION_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendUseCardMeesage(mine: Connection, other: Connection, reaction: Boolean, card_number: Int){
    val dataYour = if(reaction) SakuraCardCommand(USE_CARD_YOUR_REACTION, card_number) else SakuraCardCommand(
        USE_CARD_YOUR, card_number)
    val dataOther = if(reaction) SakuraCardCommand(USE_CARD_OTHER_REACTION, card_number) else SakuraCardCommand(
        USE_CARD_OTHER, card_number)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendSetConcentration(mine: Connection, other: Connection, number: Int){
    val dataYour = SakuraCardCommand(SET_CONCENTRATION_YOUR, number)
    val dataOther = SakuraCardCommand(SET_CONCENTRATION_OTHER, number)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendSetShrink(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(SET_SHRINK_YOUR, -1)
    val dataOther = SakuraCardCommand(SET_SHRINK_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendRemoveShrink(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(REMOVE_SHRINK_YOUR, -1)
    val dataOther = SakuraCardCommand(REMOVE_SHRINK_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendHandToDeck(mine: Connection, other: Connection, card_number: Int, public: Boolean, below: Boolean){
    val dataYour = SakuraCardCommand(if (below) CARD_HAND_TO_DECK_BELOW_YOUR else CARD_HAND_TO_DECK_UPPER_YOUR, card_number)
    val dataOther = if(public) SakuraCardCommand(if (below) CARD_HAND_TO_DECK_BELOW_OTHER else CARD_HAND_TO_DECK_UPPER_OTHER, card_number)
    else SakuraCardCommand(if (below) CARD_HAND_TO_DECK_BELOW_OTHER else CARD_HAND_TO_DECK_UPPER_OTHER, if(CardSet.isPoison(card_number)) 1 else 0)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendDrawCard(mine: Connection, other: Connection, card_number: Int){
    val dataYour = SakuraCardCommand(DRAW_CARD_YOUR, card_number)
    val dataOther = SakuraCardCommand(DRAW_CARD_OTHER, if(CardSet.isPoison(card_number)) 1 else 0)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendMuligunEnd(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(MULIGUN_END, -1)
    val dataOther = SakuraCardCommand(MULIGUN_END, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendStartPhaseStart(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(START_START_PHASE_YOUR, -1)
    val dataOther = SakuraCardCommand(START_START_PHASE_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendDeckReconstruct(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(DECK_RECONSTRUCT_YOUR, -1)
    val dataOther = SakuraCardCommand(DECK_RECONSTRUCT_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendMainPhaseStart(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(START_MAIN_PHASE_YOUR, -1)
    val dataOther = SakuraCardCommand(START_MAIN_PHASE_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendEndPhaseStart(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(START_END_PHASE_YOUR, -1)
    val dataOther = SakuraCardCommand(START_END_PHASE_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendActionRequest(mine: Connection){
    val dataYour = SakuraCardCommand(ACTION_REQUEST, -1)
    send(mine, Json.encodeToString(dataYour))
}

suspend fun sendDoBasicAction(mine: Connection, other: Connection, command: CommandEnum, card: Int){
    val dataYour = SakuraCardCommand(command, if(card == -1) -1 else if(card >= 200000) card - 200000 else card)
    val dataOther = SakuraCardCommand(command.Opposite(), if(card == -1) -1 else if(card >= 200000) card - 200000 else 0)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendGameEnd(winner: Connection, loser: Connection){
    val dataWinner = SakuraCardCommand(GAME_END_WINNER, -1)
    val dataLoser = SakuraCardCommand(GAME_END_LOSER, -1)
    send(winner, Json.encodeToString(dataWinner))
    send(loser, Json.encodeToString(dataLoser))
}

suspend fun sendCardEffectOrder(player: Connection, command: CommandEnum, list: MutableList<Int>){
    val preData = SakuraCardCommand(command, -1)
    val data = SakuraSendData(command, list)
    send(player, Json.encodeToString(preData))
    send(player, Json.encodeToString(data))
}

suspend fun sendCoverCardSelect(player: Connection, list: MutableList<Int>, reason: Int){
    val preData = SakuraCardCommand(COVER_CARD_SELECT, reason)
    val data = SakuraSendData(COVER_CARD_SELECT, list)
    send(player, Json.encodeToString(preData))
    send(player, Json.encodeToString(data))
}

suspend fun sendCardEffectSelect(player: Connection, card_number: Int, command: CommandEnum = SELECT_CARD_EFFECT){
    val data = SakuraCardCommand(command, card_number)
    send(player, Json.encodeToString(data))
}

suspend fun sendAuraDamageSelect(player: Connection, auraDamage: Int){
    val data = SakuraCardCommand(SELECT_AURA_DAMAGE_PLACE, auraDamage)
    send(player, Json.encodeToString(data))
}

suspend fun sendAuraDamagePlaceInformation(player: Connection, list: MutableList<Int>){
    val data = SakuraSendData(SELECT_AURA_DAMAGE_PLACE, list)
    send(player, Json.encodeToString(data))
}

suspend fun sendPreCardSelect(player: Connection, reason: CommandEnum, card_number: Int){
    val data = SakuraCardCommand(reason, card_number)
    send(player, Json.encodeToString(data))
}

suspend fun sendCardSelect(player: Connection, list: MutableList<Int>, reason: CommandEnum){
    val data = SakuraSendData(reason, list)
    send(player, Json.encodeToString(data))
}

suspend fun sendShowInformation(command: CommandEnum, show_player: Connection, look_player: Connection, list: MutableList<Int>){
    val preDataShow = SakuraCardCommand(command)
    val preDataLook = SakuraCardCommand(command.Opposite())
    val data = SakuraSendData(command.Opposite(), list)
    send(show_player, Json.encodeToString(preDataShow))
    send(look_player, Json.encodeToString(preDataLook))
    send(look_player, Json.encodeToString(data))
}

suspend fun sendChangeUmbrella(mine: Connection, other: Connection){
    val dataYour = SakuraCardCommand(CHANGE_UMBRELLA_YOUR, -1)
    val dataOther = SakuraCardCommand(CHANGE_UMBRELLA_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendSetStratagem(mine: Connection, other: Connection, stratagem: Stratagem){
    val dataYour = SakuraCardCommand(STRATAGEM_SET_YOUR, if(stratagem == Stratagem.SHIN_SAN) 0 else 1)
    val dataOther = SakuraCardCommand(STRATAGEM_SET_OTHER, -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendGetStratagem(mine: Connection, other: Connection, stratagem: Stratagem){
    val dataYour = SakuraCardCommand(STRATAGEM_GET_YOUR, if(stratagem == Stratagem.SHIN_SAN) 0 else 1)
    val dataOther = SakuraCardCommand(STRATAGEM_GET_OTHER, if(stratagem == Stratagem.SHIN_SAN) 0 else 1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendRequestBasicOperation(mine: Connection, card_number: Int){
    val data = SakuraCardCommand(REQUEST_BASIC_OPERATION, card_number)
    send(mine, Json.encodeToString(data))
}

suspend fun sendSimpleCommand(mine: Connection, command: CommandEnum){
    val dataYour = SakuraCardCommand(command, -1)
    send(mine, Json.encodeToString(dataYour))
}

suspend fun sendSimpleCommand(mine: Connection, other: Connection, command: CommandEnum){
    val dataYour = SakuraCardCommand(command, -1)
    val dataOther = SakuraCardCommand(command.Opposite(), -1)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

suspend fun sendSimpleCommand(mine: Connection, other: Connection, command: CommandEnum, subNumber: Int){
    val dataYour = SakuraCardCommand(command, subNumber)
    val dataOther = SakuraCardCommand(command.Opposite(), subNumber)
    send(mine, Json.encodeToString(dataYour))
    send(other, Json.encodeToString(dataOther))
}

//receive function
suspend fun waitUntil(player: Connection, wait_command: CommandEnum): SakuraSendData {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraSendData>(text)
                    if (data.command == wait_command) {
                        return data
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return waitUntil(player, wait_command)
        }
    }
}

suspend fun waitCardSetUntil(player: Connection, wait_command: CommandEnum): SakuraCardSetSend {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true){
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try{
                    val data = json.decodeFromString<SakuraCardSetSend>(text)
                    if (data.command == wait_command){
                        return data
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return waitCardSetUntil(player, wait_command)
        }
    }
}

suspend fun receiveEnchantment(player: Connection): Pair<CommandEnum, Int> {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true){
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == SELECT_ENCHANTMENT_OTHER || data.command == SELECT_ENCHANTMENT_YOUR){
                        return Pair(data.command, data.card)
                    }
                    else if(data.command == SELECT_ENCHANTMENT_END){
                        return Pair(data.command, -1)
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception){
            waitReconnect(player)
            return receiveEnchantment(player)
        }
    }
}

suspend fun receiveReact(player: Connection): Pair<CommandEnum, Int> {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true){
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == REACT_USE_CARD_HAND || data.command == REACT_USE_CARD_SPECIAL || data.command == REACT_USE_CARD_SOLDIER){
                        return Pair(data.command, data.card)
                    }
                    else if(data.command == REACT_NO){
                        return Pair(data.command, -1)
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception){
            waitReconnect(player)
            return receiveReact(player)
        }
    }
}

suspend fun receiveChooseDamage(player: Connection): CommandEnum {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true){
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == CHOOSE_AURA || data.command == CHOOSE_LIFE){
                        return data.command
                    }
                    else {
                        continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception){
            waitReconnect(player)
            return receiveChooseDamage(player)
        }
    }
}

suspend fun receiveNapInformation(player: Connection, total: Int, card_number: Int): Pair<Int, Int> {
    player.session.send(Json.encodeToString(SakuraCardCommand(SELECT_NAP, card_number)))
    player.session.send(Json.encodeToString(SakuraSendData(SELECT_NAP, mutableListOf(total))))
    return receiveNapInformationMain(player, total, card_number)
}

suspend fun receiveNapInformationMain(player: Connection, total: Int, card_number: Int): Pair<Int, Int> {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true){
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraSendData>(text)
                    if (data.command == SELECT_NAP){
                        data.data?.let {
                            if(it.size >= 2){
                                return(Pair(it[0], it[1]))
                            }
                        }
                    }
                    else {
                        continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception){
            waitReconnect(player)
            return receiveNapInformationMain(player, total, card_number)
        }
    }
}

suspend fun receiveReconstructRequest(player: Connection): Boolean{
    player.session.send(Json.encodeToString(SakuraCardCommand(DECK_RECONSTRUCT_REQUEST, -1)))
    return receiveReconstructRequestMain(player)
}

suspend fun receiveReconstructRequestMain(player: Connection): Boolean{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true){
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                return try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == DECK_RECONSTRUCT_NO) {
                        false
                    } else if (data.command == DECK_RECONSTRUCT_YES) {
                        true
                    } else {
                        continue
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        } catch (e: Exception){
            waitReconnect(player)
            return receiveReconstructRequestMain(player)
        }
    }
}

suspend fun receiveFullPowerRequest(player: Connection): Boolean{
    send(player, Json.encodeToString(SakuraCardCommand(FULL_POWER_REQUEST, -1)))
    return receiveFullPowerRequestMain(player)
}

suspend fun receiveFullPowerRequestMain(player: Connection): Boolean {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                return try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == FULL_POWER_NO) {
                        false
                    } else if (data.command == FULL_POWER_YES) {
                        true
                    } else {
                        continue
                    }
                } catch (e: Exception) {
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveFullPowerRequestMain(player)
        }
    }
}

suspend fun receiveFullPowerActionRequest(player: Connection): Pair<CommandEnum, Int>{
    sendActionRequest(player)
    return receiveFullPowerActionRequestMain(player)
}

suspend fun receiveFullPowerActionRequestMain(player: Connection): Pair<CommandEnum, Int>{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try{
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == ACTION_USE_CARD_HAND ||
                        data.command == ACTION_USE_CARD_SPECIAL ||
                        data.command == ACTION_USE_CARD_COVER ||
                        data.command == ACTION_USE_CARD_SOLDIER ||
                        data.command == ACTION_END_TURN
                    ){
                        return Pair(data.command, data.card)
                    }
                    else {
                        sendActionRequest(player)
                        continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveFullPowerActionRequestMain(player)
        }
    }
}

suspend fun receiveActionRequest(player: Connection): Pair<CommandEnum, Int>{
    sendActionRequest(player)
    return receiveActionRequestMain(player)
}

suspend fun receiveActionRequestMain(player: Connection): Pair<CommandEnum, Int>{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == ACTION_USE_CARD_HAND ||
                        data.command == ACTION_USE_CARD_SPECIAL ||
                        data.command == ACTION_USE_CARD_COVER ||
                        data.command == ACTION_USE_CARD_SOLDIER ||
                        data.command == ACTION_GO_FORWARD ||
                        data.command == ACTION_GO_BACKWARD ||
                        data.command == ACTION_WIND_AROUND ||
                        data.command == ACTION_INCUBATE ||
                        data.command == ACTION_BREAK_AWAY ||
                        data.command == ACTION_GARUDA ||
                        data.command == ACTION_YAKSHA ||
                        data.command == ACTION_NAGA ||
                        data.command == ACTION_ASURA ||
                        data.command == ACTION_END_TURN
                    ){
                        return Pair(data.command, data.card)
                    }
                    else {
                        sendActionRequest(player)
                        continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveActionRequestMain(player)
        }
    }
}

suspend fun receiveCardEffectOrder(player: Connection, command: CommandEnum, list: MutableList<Int>): Int{
    sendCardEffectOrder(player, command, list)
    return receiveCardEffectOrderMain(player, command, list)
}

suspend fun receiveCardEffectOrderMain(player: Connection, command: CommandEnum, list: MutableList<Int>): Int{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == command){
                        return data.card
                    }
                    else {
                        continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveCoverCardSelectMain(player, list)
        }
    }
}

suspend fun receiveCoverCardSelect(player: Connection, list: MutableList<Int>, reason: Int): Int{
    sendCoverCardSelect(player, list, reason)
    return receiveCoverCardSelectMain(player, list)
}

suspend fun receiveCoverCardSelectMain(player: Connection, list: MutableList<Int>): Int{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    if (data.command == COVER_CARD_SELECT){
                        return data.card
                    }
                    else {
                        continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveCoverCardSelectMain(player, list)
        }
    }
}

suspend fun receiveCardEffectSelect(player: Connection, card_number: Int, command: CommandEnum = SELECT_CARD_EFFECT): CommandEnum{
    sendCardEffectSelect(player, card_number, command)
    return receiveCardEffectSelectMain(player, card_number)
}

suspend fun receiveCardEffectSelectMain(player: Connection, card_number: Int): CommandEnum{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    when(data.command){
                        SELECT_ONE, SELECT_TWO, SELECT_THREE, SELECT_FOUR, SELECT_FIVE, SELECT_SIX, SELECT_SEVEN, SELECT_EIGHT, SELECT_NOT -> return data.command //will be added
                        else -> continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveCardEffectSelectMain(player, card_number)
        }
    }
}

//receive data like( [LOCATION_ENUM.AURA, 3, CARD_NUMBER, 2, CARD_NUMBER, 2] )
suspend fun receiveAuraDamageSelect(player: Connection, place_list: MutableList<Int>, auraDamage: Int): MutableList<Int>?{
    sendAuraDamageSelect(player, auraDamage)
    sendAuraDamagePlaceInformation(player, place_list)
    return receiveAuraDamageSelectMain(player, place_list)
}

suspend fun receiveAuraDamageSelectMain(player: Connection, place_list: MutableList<Int>): MutableList<Int>?{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraSendData>(text)
                    when(data.command){
                        SELECT_AURA_DAMAGE_PLACE -> return data.data //will be added
                        else -> continue
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveAuraDamageSelectMain(player, place_list)
        }
    }
}

suspend fun receiveSelectCard(player: Connection, card_list: MutableList<Int>, reason: CommandEnum, card_number: Int): MutableList<Int>?{
    sendPreCardSelect(player, reason, card_number)
    sendCardSelect(player, card_list, reason)
    return receiveSelectCardMain(player, card_list, reason, card_number)
}

suspend fun receiveSelectCardMain(player: Connection, card_list: MutableList<Int>, reason: CommandEnum, card_number: Int): MutableList<Int>?{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                try {
                    val data = json.decodeFromString<SakuraSendData>(text)
                    if(data.command == reason){
                        var flag = true
                        if(data.data != null){
                            for (cardNumber in data.data){
                                if(!card_list.contains(cardNumber)) {
                                    flag = false
                                    break
                                }
                            }
                        }
                        if(flag) return data.data
                        sendPreCardSelect(player, reason, card_number)
                        sendCardSelect(player, card_list, reason)
                    }
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception){
            waitReconnect(player)
            return receiveSelectCardMain(player, card_list, reason, card_number)
        }
    }
}

suspend fun receiveBasicOperation(player: Connection, card_number: Int): CommandEnum{
    sendRequestBasicOperation(player, card_number)
    return receiveBasicOperationMain(player)
}

suspend fun receiveBasicOperationMain(player: Connection): CommandEnum{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    while (true) {
        try {
            val frame = player.session.incoming.receive()
            if (frame is Frame.Text) {
                val text = frame.readText()
                return try {
                    val data = json.decodeFromString<SakuraCardCommand>(text)
                    data.command
                }catch (e: Exception){
                    continue
                }
            }
        } catch (e: Exception) {
            waitReconnect(player)
            return receiveBasicOperationMain(player)
        }
    }
}