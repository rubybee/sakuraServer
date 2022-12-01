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

suspend fun sendStartTurn(player: Connection){
    val data = SakuraSendData(CommandEnum.START_TURN, null)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendReduceNapStart(player: Connection){
    val data = SakuraCardCommand(CommandEnum.REDUCE_NAP_START, null)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendReduceNapSelf(player: Connection, card_name: CardName){
    val data = SakuraCardCommand(CommandEnum.REDUCE_NAP_YOUR, card_name)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendReduceNapOther(player: Connection, card_name: CardName){
    val data = SakuraCardCommand(CommandEnum.REDUCE_NAP_OTHER, card_name)
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

suspend fun sendDiscard(mine: Connection, other: Connection, card_name: CardName){
    val data_your = SakuraCardCommand(CommandEnum.DISCARD_CARD_YOUR, card_name)
    val data_other = SakuraCardCommand(CommandEnum.DISCARD_CARD_OTHER, card_name)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendUsed(mine: Connection, other: Connection, card_name: CardName){
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

suspend fun sendMoveToken(mine: Connection, other: Connection, from: LocationEnum, to: LocationEnum, number: Int){
    val pre_data = SakuraCardCommand(CommandEnum.MOVE_TOKEN, null)
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

suspend fun sendRemoveShrink(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(CommandEnum.REMOVE_SHRINK_YOUR, null)
    val data_other = SakuraSendData(CommandEnum.REMOVE_SHRINK_OTHER, null)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
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