package com.sakurageto.gamelogic

import com.sakurageto.card.CardName

enum class MegamiEnum(var real_number: Int) {
    NONE(0),
    KODAMA(1),
    KIRIKO(2),
    YURINA(10),
    YURINA_A1(11),
    YURINA_A2(12),
    SAINE(20),
    SAINE_A1(21),
    SAINE_A2(22),
    HIMIKA(30),
    HIMIKA_A1(31),
    TOKOYO(40),
    TOKOYO_A1(41),
    TOKOYO_A2(42),
    OBORO(50),
    OBORO_A1(51),
    YUKIHI(60),
    YUKIHI_A1(61),
    SHINRA(70),
    SHINRA_A1(71),
    HAGANE(80),
    HAGANE_A1(81),
    CHIKAGE(90),
    CHIKAGE_A1(91),
    KURURU(100),
    KURURU_A1(101),
    KURURU_A2(102),
    THALLYA(110),
    THALLYA_A1(111),
    RAIRA(120),
    RAIRA_A1(121),
    UTSURO(130),
    UTSURO_A1(131),
    HONOKA(140),
    HONOKA_A1(141),
    KORUNU(150),
    YATSUHA(160),
    YATSUHA_A1(161),
    YATSUHA_AA1(162),
    HATSUMI(170),
    HATSUMI_A1(171),
    MIZUKI(180),
    MEGUMI(190),
    KANAWE(200),
    KAMUWI(210),
    RENRI(220),
    AKINA(230),
    SHISUI(240);

    fun changeNormalMegami(): MegamiEnum{
        val anotherNumber = this.real_number % 10
        return if(anotherNumber == 0){
            this
        } else{
            fromInt(this.real_number - anotherNumber)
        }
    }

    fun getAllNormalCardName(): List<CardName>{
        return CardName.returnNormalCardNameByMegami(this)
    }

    fun getAllAdditionalCardName(): List<CardName>{
        return CardName.returnAdditionalCardNameByMegami(this)
    }

    fun getAllSpecialCardName(): List<CardName>{
        return CardName.returnSpecialCardNameByMegami(this)
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