package com.sakurageto.protocol

import com.sakurageto.Connection
import com.sakurageto.card.CardName
import io.ktor.websocket.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

suspend fun UsedCardReturn(player: Connection, card_name: CardName) {
    val data = SakuraCardCommand(CommandEnum.RETURN_SPECIAL_CARD, card_name)
    val send_data = Json.encodeToString(data)
    player.session.send(send_data)
}