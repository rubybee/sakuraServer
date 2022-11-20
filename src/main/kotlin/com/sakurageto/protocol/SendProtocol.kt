package com.sakurageto.protocol

import com.sakurageto.card.CardName
import kotlinx.serialization.Serializable

@Serializable
data class SakuraCardCommand(
    val command: CommandEnum,
    val card: CardName?,
)

@Serializable
data class SakuraCardSetSend(
    val command: CommandEnum,
    val normal_card: MutableList<CardName>?,
    var special_card: MutableList<CardName>?,
)

@Serializable
data class SakuraSendData(
    val command: CommandEnum,
    val data: MutableList<Int>?)

