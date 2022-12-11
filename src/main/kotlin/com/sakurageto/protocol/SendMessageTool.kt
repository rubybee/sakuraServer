package com.sakurageto.protocol

import com.sakurageto.Connection
import com.sakurageto.card.CardName
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//send function
suspend fun UsedCardReturn(player: Connection, card_name: CardName) {
    val data = SakuraCardCommand(CommandEnum.RETURN_SPECIAL_CARD, card_name)
    val send_data = Json.encodeToString(data)
    player.session.send(send_data)
}

suspend fun sendReduceNapStart(player: Connection){
    val data = SakuraCardCommand(CommandEnum.REDUCE_NAP_START, null)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendReduceNapEnd(player: Connection){
    val data = SakuraCardCommand(CommandEnum.REDUCE_NAP_END, null)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendStartSelectEnchantment(player: Connection){
    val data = SakuraCardCommand(CommandEnum.SELECT_ENCHANTMENT_START, null)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendRequestEnchantmentCard(player: Connection, card_list_your: MutableList<CardName>, card_list_other: MutableList<CardName>){
    val data_your = SakuraCardSetSend(CommandEnum.SELECT_ENCHANTMENT_YOUR, card_list_your, null)
    val data_other = SakuraCardSetSend(CommandEnum.SELECT_ENCHANTMENT_OTHER, card_list_other, null)
    player.session.send(Json.encodeToString(data_your))
    player.session.send(Json.encodeToString(data_other))
}

suspend fun sendDestructionEnchant(mine: Connection, other: Connection, card_name: CardName){
    val data_your = SakuraCardCommand(CommandEnum.DESTRUCTION_ENCHANTMENT_YOUR, card_name)
    val data_other = SakuraCardCommand(CommandEnum.DESTRUCTION0_ENCHANTMENT_OTHER, card_name)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}
suspend fun sendEnchantmentZone(mine: Connection, other: Connection, card_name: CardName){
    val data_your = SakuraCardCommand(CommandEnum.ENCHANTMENT_CARD_YOUR, card_name)
    val data_other = SakuraCardCommand(CommandEnum.ENCHANTMENT_CARD_OTHER, card_name)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}
suspend fun sendDiscardZone(mine: Connection, other: Connection, card_name: CardName){
    val data_your = SakuraCardCommand(CommandEnum.DISCARD_CARD_YOUR, card_name)
    val data_other = SakuraCardCommand(CommandEnum.DISCARD_CARD_OTHER, card_name)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendUsedZone(mine: Connection, other: Connection, card_name: CardName){
    val data_your = SakuraCardCommand(CommandEnum.USED_CARD_YOUR, card_name)
    val data_other = SakuraCardCommand(CommandEnum.USED_CARD_OTHER, card_name)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun makeAttackComplete(mine: Connection, other: Connection, card_name: CardName){
    val data_your = SakuraCardCommand(CommandEnum.MAKE_ATTACK_COMPLETE_YOUR, card_name)
    val data_other = SakuraCardCommand(CommandEnum.MAKE_ATTACK_COMPLETE_OTHER, card_name)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendAttackInformation(mine: Connection, other: Connection, data: MutableList<Int>) {
    val data_your = SakuraSendData(CommandEnum.ATTACK_INFORMATION_YOUR, data)
    val data_other = SakuraSendData(CommandEnum.ATTACK_INFORMATION_OTHER, data)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendChooseDamage(mine: Connection, command: CommandEnum, aura: Int, life: Int){
    val pre_data = SakuraCardCommand(CommandEnum.CHOOSE_DAMAGE, null)
    val data = SakuraSendData(command, mutableListOf(aura, life))
    mine.session.send(Json.encodeToString(pre_data))
    mine.session.send(Json.encodeToString(data))
}
suspend fun sendRequestReact(mine: Connection){
    val data = SakuraCardCommand(CommandEnum.REACT_REQUEST, null)
    mine.session.send(Json.encodeToString(data))
}

suspend fun sendMoveToken(mine: Connection, other: Connection, from: LocationEnum, to: LocationEnum, number: Int, card_name: CardName?){
    val pre_data = SakuraCardCommand(CommandEnum.MOVE_TOKEN, card_name)
    mine.session.send(Json.encodeToString(pre_data))
    other.session.send(Json.encodeToString(pre_data))
    val data_your = SakuraSendData(CommandEnum.MOVE_TOKEN, mutableListOf(from.real_number, to.real_number, number))
    val data_other = SakuraSendData(CommandEnum.MOVE_TOKEN, mutableListOf(from.Opposite().real_number, to.Opposite().real_number, number))
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendAddConcentration(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.ADD_CONCENTRATION_YOUR, null)
    val data_other = SakuraSendData(CommandEnum.ADD_CONCENTRATION_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendDecreaseConcentration(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.DECREASE_CONCENTRATION_YOUR, null)
    val data_other = SakuraSendData(CommandEnum.DECREASE_CONCENTRATION_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendUseCardMeesage(mine: Connection, other: Connection, reaction: Boolean, card_name: CardName){
    val data_your = if(reaction) SakuraCardCommand(CommandEnum.USE_CARD_YOUR_REACTION, card_name) else SakuraCardCommand(CommandEnum.USE_CARD_YOUR, card_name)
    val data_other = if(reaction) SakuraCardCommand(CommandEnum.USE_CARD_OTHER_REACTION, card_name) else SakuraCardCommand(CommandEnum.USE_CARD_OTHER, card_name)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendRemoveShrink(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.REMOVE_SHRINK_YOUR, null)
    val data_other = SakuraSendData(CommandEnum.REMOVE_SHRINK_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendHandToDeck(mine: Connection, other: Connection, card_name: CardName, public: Boolean){
    val data_your = SakuraCardCommand(CommandEnum.CARD_HAND_TO_DECK_YOUR, card_name)
    val data_other = if(public) SakuraCardCommand(CommandEnum.CARD_HAND_TO_DECK_OTHER, card_name) else SakuraCardCommand(CommandEnum.CARD_HAND_TO_DECK_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendHandToCover(mine: Connection, other: Connection, card_name: CardName, public: Boolean){
    val data_your = SakuraCardCommand(CommandEnum.CARD_HAND_TO_COVER_YOUR, card_name)
    val data_other = if(public) SakuraCardCommand(CommandEnum.CARD_HAND_TO_COVER_OTHER, card_name) else SakuraCardCommand(CommandEnum.CARD_HAND_TO_COVER_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendDrawCard(mine: Connection, other: Connection, card_name: CardName){
    val data_your = SakuraCardCommand(CommandEnum.DRAW_CARD_YOUR, card_name)
    val data_other = SakuraCardCommand(CommandEnum.DRAW_CARD_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendMuligunEnd(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.MULIGUN_END, null)
    val data_other = SakuraCardCommand(CommandEnum.MULIGUN_END, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendStartPhaseStart(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.START_START_PHASE_YOUR, null)
    val data_other = SakuraCardCommand(CommandEnum.START_START_PHASE_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendDeckReconstruct(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.DECK_RECONSTRUCT_YOUR, null)
    val data_other = SakuraCardCommand(CommandEnum.DECK_RECONSTRUCT_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendMainPhaseStart(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.START_MAIN_PHASE_YOUR, null)
    val data_other = SakuraCardCommand(CommandEnum.START_MAIN_PHASE_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendEndPhaseStart(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.START_END_PHASE_YOUR, null)
    val data_other = SakuraCardCommand(CommandEnum.START_END_PHASE_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendActionRequest(mine: Connection){
    val data_your = SakuraCardCommand(CommandEnum.ACTION_REQUEST, null)
    mine.session.send(Json.encodeToString(data_your))
}

suspend fun sendDoBasicAction(mine: Connection, other: Connection, command: CommandEnum){
    val data_your = SakuraCardCommand(command, null)
    val data_other = SakuraCardCommand(command.Opposite(), null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendGameEnd(winner: Connection, loser: Connection){
    val data_winner = SakuraCardCommand(CommandEnum.GAME_END_WINNER, null)
    val data_loser = SakuraCardCommand(CommandEnum.GAME_END_LOSER, null)
    winner.session.send(Json.encodeToString(data_winner))
    loser.session.send(Json.encodeToString(data_loser))
}

suspend fun sendCoverCardSelect(player: Connection){
    val data = SakuraCardCommand(CommandEnum.COVER_CARD_SELECT, null)
    player.session.send(Json.encodeToString(data))
}

//receive function
suspend fun receiveEnchantment(player: Connection): Pair<CommandEnum, CardName?> {
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.SELECT_ENCHANTMENT_OTHER || data.command == CommandEnum.SELECT_ENCHANTMENT_YOUR){
                return Pair(data.command, data.card)
            }
            else if(data.command == CommandEnum.SELECT_ENCHANTMENT_END){
                return Pair(data.command, null)
            }
        }
    }
    return Pair(CommandEnum.SELECT_ENCHANTMENT_END, null)
}

suspend fun receiveReact(player: Connection): Pair<CommandEnum, CardName?> {
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.USE_CARD_IN_HAND || data.command == CommandEnum.USE_CARD_IN_SPEICAL){
                return Pair(data.command, data.card)
            }
            else if(data.command == CommandEnum.DO_NOT_REACT){
                return Pair(data.command, null)
            }
        }
    }
    return Pair(CommandEnum.DO_NOT_REACT, null)
}

suspend fun receiveChooseDamage(player: Connection): CommandEnum {
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.CHOOSE_AURA || data.command == CommandEnum.CHOOSE_LIFE){
                return data.command
            }
            else {
                continue
            }
        }
    }
    return CommandEnum.CHOOSE_AURA
}

suspend fun receiveNapInformation(player: Connection, total: Int, card_name: CardName): Pair<Int, Int> {
    val pre_data = SakuraCardCommand(CommandEnum.SELECT_NAP, card_name)
    player.session.send(Json.encodeToString(pre_data))
    val data = SakuraSendData(CommandEnum.SELECT_NAP, mutableListOf(total))
    player.session.send(Json.encodeToString(data))
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraSendData>(text)
            if (data.command == CommandEnum.SELECT_NAP){
                return Pair(data.data!![0], data.data!![1])
            }
            else {
                continue
            }
        }
    }
    return Pair(0, 0)
}

suspend fun receiveReconstructRequest(player: Connection): Boolean{
    val pre_data = SakuraCardCommand(CommandEnum.DECK_RECONSTRUCT_REQUEST, null)
    player.session.send(Json.encodeToString(pre_data))
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.DECK_RECONSTRUCT_NO){
                return false
            }
            else if(data.command == CommandEnum.DECK_RECONSTRUCT_YES){
                return true
            }
            else {
                continue
            }
        }
    }
    return false
}

suspend fun receiveFullPowerRequest(player: Connection): Boolean{
    val pre_data = SakuraCardCommand(CommandEnum.FULL_POWER_REQUEST, null)
    player.session.send(Json.encodeToString(pre_data))
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.FULL_POWER_NO){
                return false
            }
            else if(data.command == CommandEnum.FULL_POWER_YES){
                return true
            }
            else {
                continue
            }
        }
    }
    return false
}

suspend fun receiveFullPowerActionRequest(player: Connection): Pair<CommandEnum, CardName>{
    sendActionRequest(player)
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.ACTION_USE_CARD_HAND ||
                data.command == CommandEnum.ACTION_USE_CARD_SPECIAL ||
                data.command == CommandEnum.ACTION_END_TURN){
                return Pair(data.command, data.card?: CardName.CARD_UNNAME)
            }
            else {
                continue
            }
        }
    }
    return Pair(CommandEnum.FIRST_TURN, CardName.CARD_UNNAME)
}

suspend fun receiveActionRequest(player: Connection): Pair<CommandEnum, CardName>{
    sendActionRequest(player)
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.ACTION_USE_CARD_HAND ||
                data.command == CommandEnum.ACTION_USE_CARD_SPECIAL ||
                data.command == CommandEnum.ACTION_GO_FORWARD ||
                data.command == CommandEnum.ACTION_GO_BACKWARD ||
                data.command == CommandEnum.ACTION_WIND_AROUND ||
                data.command == CommandEnum.ACTION_INCUBATE ||
                data.command == CommandEnum.ACTION_BREAK_AWAY ||
                data.command == CommandEnum.ACTION_END_TURN
            ){
                return Pair(data.command, data.card?: CardName.CARD_UNNAME)
            }
            else {
                continue
            }
        }
    }
    return Pair(CommandEnum.FIRST_TURN, CardName.CARD_UNNAME)
}

suspend fun receiveCoverCardSelect(player: Connection): CardName{
    sendCoverCardSelect(player)
    for(frame in player.session.incoming){
        if (frame is Frame.Text) {
            val text = frame.readText()
            val data = Json.decodeFromString<SakuraCardCommand>(text)
            if (data.command == CommandEnum.COVER_CARD_SELECT){
                return data.card?: CardName.CARD_UNNAME
            }
            else {
                continue
            }
        }
    }
    return CardName.CARD_UNNAME
}