package com.sakurageto.gamelogic

enum class GameVersion(val real_number: Int) {
    VERSION_7_2(0),
    VERSION_8_1(1),
    VERSION_8_2(2),
    VERSION_9(3);

    fun isHigherThen(version: GameVersion): Boolean{
        return this.real_number > version.real_number
    }

    companion object {
        fun fromInt(value: Int) = GameVersion.values().first { it.real_number == value }
    }
}