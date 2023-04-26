package com.sakurageto.gamelogic

enum class MegamiEnum(var real_number: Int) {
    NONE(0),
    YURINA(10),
    YURINA_A1(11),
    SAINE(20),
    SAINE_A1(21),
    HIMIKA(30),
    HIMIKA_A1(31),
    TOKOYO(40),
    TOKOYO_A1(41),
    OBORO(50),
    YUKIHI(60),
    SHINRA(70),
    HAGANE(80),
    CHIKAGE(90),
    KURURU(100),
    THALLYA(110),
    RAIRA(120),
    UTSURO(130);

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