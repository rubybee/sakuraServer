package com.sakurageto.protocol

import com.sakurageto.card.CardName
import com.sakurageto.card.MadeAttack
import kotlinx.serialization.Serializable

@Serializable
data class SakuraCardCommand(
    val command: CommandEnum = CommandEnum.NULL,
    val card: CardName? = null,
)

@Serializable
data class SakuraCardSetSend(
    val command: CommandEnum = CommandEnum.NULL,
    val normal_card: MutableList<CardName>? = null,
    var special_card: MutableList<CardName>? = null,
)

@Serializable
data class SakuraSendData(
    val command: CommandEnum = CommandEnum.NULL,
    val data: MutableList<Int>? = null
)


