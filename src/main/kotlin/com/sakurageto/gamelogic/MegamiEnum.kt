package com.sakurageto.gamelogic

enum class MegamiEnum(var real_number: Int) {
    YURINA(10),
    SAINE(20),
    HIMIKA(30),
    TOKOYO(40);

    companion object {
        fun fromInt(value: Int) = MegamiEnum.values().first { it.real_number == value }
    }
}