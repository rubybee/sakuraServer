package com.sakurageto.gamelogic

enum class GameMode(var real_number: Int){
    SSANG_JANG_YO_LAN(0),
    SAM_SEUB_IL_SA(1);

    companion object {
        fun fromInt(value: Int) = GameMode.values().first { it.real_number == value }
    }
}
