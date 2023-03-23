package com.sakurageto.gamelogic

enum class MegamiEnum(var real_number: Int) {
    NONE(0),
    YURINA(10),
    SAINE(20),
    HIMIKA(30),
    TOKOYO(40),
    OBORO(50),
    YUKIHI(60),
    SHINRA(70),
    HAGANE(80),
    CHIKAGE(90),
    KURURU(100),
    THALLYA(110);

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

enum class Stratagem(){
    SHIN_SAN,
    GUE_MO,
}