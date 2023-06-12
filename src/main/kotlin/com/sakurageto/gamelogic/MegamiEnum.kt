package com.sakurageto.gamelogic

import com.sakurageto.card.CardName

enum class MegamiEnum(var real_number: Int) {
    NONE(0),
    KODAMA(1),
    YURINA(10),
    YURINA_A1(11),
    SAINE(20),
    SAINE_A1(21),
    SAINE_A2(22),
    HIMIKA(30),
    HIMIKA_A1(31),
    TOKOYO(40),
    TOKOYO_A1(41),
    OBORO(50),
    OBORO_A1(51),
    YUKIHI(60),
    YUKIHI_A1(61),
    SHINRA(70),
    SHINRA_A1(71),
    HAGANE(80),
    CHIKAGE(90),
    CHIKAGE_A1(91),
    KURURU(100),
    KURURU_A1(101),
    THALLYA(110),
    THALLYA_A1(111),
    RAIRA(120),
    RAIRA_A1(121),
    UTSURO(130),
    UTSURO_A1(131),
    HONOKA(140),
    KORUNU(150),
    YATSUHA(160),
    HATSUMI(170),
    MIZUKI(180),
    MEGUMI(190),
    KANAWE(200);


    fun getAllNormalCardName(): List<CardName>{
        return CardName.returnNormalCardNameByMegami(this)
    }

    fun getAllAdditionalCardName(): List<CardName>{
        return CardName.returnAdditionalCardNameByMegami(this)
    }

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