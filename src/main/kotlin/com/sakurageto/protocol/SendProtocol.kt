package com.sakurageto.protocol

import com.sakurageto.card.CardName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

interface SakuraData

@Serializable
data class SakuraBaseData(
    val command: CommandEnum = CommandEnum.NULL,
    val card: Int = -1,
): SakuraData {
    override fun toString(): String{
        return json.encodeToString(this)
    }
}

@Serializable
data class SakuraCardSetData(
    val command: CommandEnum = CommandEnum.NULL,
    val normal_card: MutableList<CardName>? = null,
    var special_card: MutableList<CardName>? = null,
): SakuraData{
    override fun toString(): String {
        return json.encodeToString(this)
    }
}

@Serializable
data class SakuraArrayData(
    val command: CommandEnum = CommandEnum.NULL,
    val data: MutableList<Int>? = null
): SakuraData{
    override fun toString(): String {
        return json.encodeToString(this)
    }
}


