package com.sakurageto.protocol

import com.sakurageto.Connection
import com.sakurageto.card.CardName
import com.sakurageto.card.PlayerEnum
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

//send tool
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
    val data = SakuraCardCommand(CommandEnum.REDUCE_NAP_SELF, card_name)
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

suspend fun requestEnchantmentCard(player: Connection, card_list_your: MutableList<CardName>, card_list_other: MutableList<CardName>){
    val data_your = SakuraCardSetSend(CommandEnum.SELECT_ENCHANTMENT_YOUR, card_list_your, null)
    val data_other = SakuraCardSetSend(CommandEnum.SELECT_ENCHANTMENT_OTHER, card_list_other, null)
    player.session.send(Json.encodeToString(data_your))
    player.session.send(Json.encodeToString(data_other))
}


//receive tool
