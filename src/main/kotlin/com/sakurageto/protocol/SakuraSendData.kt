package com.sakurageto.protocol

import kotlinx.serialization.Serializable

@Serializable
data class SakuraSendData(
    val command: CommandEnum,
    val data: MutableList<Int>?)
