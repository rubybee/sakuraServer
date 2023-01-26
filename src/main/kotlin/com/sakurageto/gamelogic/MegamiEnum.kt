package com.sakurageto.gamelogic

enum class MegamiEnum(var real_number: Int) {
    YURINA(10),
    SAINE(20),
    HIMIKA(30),
    TOKOYO(40),
    OBORO(50),
    YUKIHI(60);

    companion object {
        fun fromInt(value: Int) = MegamiEnum.values().first { it.real_number == value }
    }
}

enum class Umbrella(){
    FOLD,
    UNFOLD;

    fun opposite(): Umbrella{
        return if (this == FOLD){
            UNFOLD
        } else{
            FOLD
        }
    }
}