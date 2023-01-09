package com.sakurageto.card

import com.sakurageto.gamelogic.MegamiEnum

enum class PlayerEnum {
    PLAYER1,
    PLAYER2;

    fun Opposite(): PlayerEnum{
        return if(this == PLAYER1){
            PLAYER2
        } else{
            PLAYER1
        }
    }
}

enum class SpecialCardEnum {
    UNUSED,
    PLAYING,
    PLAYED
}

enum class CardClass {
    SPECIAL,
    NORMAL
}

enum class CardType {
    ATTACK,
    BEHAVIOR,
    ENCHANTMENT,
    UNDEFINED
}

enum class SubType {
    FULLPOWER,
    REACTION,
    NONE,
    UNDEFINED,
}

enum class DistanceType {
    DISCONTINUOUS,
    CONTINUOUS
}

enum class TextEffectTimingTag {
    CONSTANT_EFFECT,
    AFTER_DESTRUCTION,
    START_DEPLOYMENT,
    IN_DEPLOYMENT,
    USED,
    USING,
    AFTER_ATTACK,
}

enum class TextEffectTag {
    NEXT_ATTACK_ENCHANTMENT,
    CHANGE_CONCENTRATION,
    CHASM,
    MAKE_ATTACK,
    MOVE_SAKURA_TOKEN,
    IMMEDIATE_RETURN,
    RETURN,
    TERMINATION,
    USING_CONDITION,
    DO_NOT_NAP,
    THIS_CARD_NAP_LOCATION_CHANGE,
    OTHER_CARD_NAP_LOCATION_CHANGE,
    CAN_REACTABLE,
    REACT_ATTACK_REDUCE,
    REACT_ATTACK_NO_DAMAGE,
    REACT_ATTACK_INVALID,
    IF_REACT_BUFF,
    ADJUST_NAP,
    COST_BUFF,
    COST_X,
    CHANGE_SWELL_DISTANCE,
    DAMAGE_AURA_REPLACEABLE_HERE,
}

enum class CardName {
    CARD_UNNAME,

    YURINA_CHAM,
    YURINA_ILSUM,
    YURINA_JARUCHIGI,
    YURINA_GUHAB,
    YURINA_GIBACK,
    YURINA_APDO,
    YURINA_GIYENBANJO,

    YURINA_WOLYUNGNACK,
    YURINA_POBARAM,
    YURINA_JJOCKBAE,
    YURINA_JURUCK,

    SAINE_DOUBLEBEGI,
    SAINE_HURUBEGI,
    SAINE_MOOGECHOO,
    SAINE_GANPA,
    SAINE_GWONYUCK,
    SAINE_CHOONGEMJUNG,
    SAINE_MOOEMBUCK,

    SAINE_YULDONGHOGEK,
    SAINE_HANGMUNGGONGJIN,
    SAINE_EMMOOSHOEBING,
    SAINE_JONGGEK,

    HIMIKA_SHOOT,
    HIMIKA_RAPIDFIRE,
    HIMIKA_MAGNUMCANON,
    HIMIKA_FULLBURST,
    HIMIKA_BACKSTEP,
    HIMIKA_BACKDRAFT,
    HIMIKA_SMOKE,

    HIMIKA_REDBULLET,
    HIMIKA_CRIMSONZERO,
    HIMIKA_SCARLETIMAGINE,
    HIMIKA_BURMILIONFIELD,

    TOKOYO_BITSUNERIGI,
    TOKOYO_WOOAHHANTAGUCK,
    TOKOYO_RUNNINGRABIT,
    TOKOYO_POETDANCE,
    TOKOYO_FLIPFAN,
    TOKOYO_WINDSTAGE,
    TOKOYO_SUNSTAGE,

    TOKOYO_KUON,
    TOKOYO_THOUSANDBIRD,
    TOKOYO_ENDLESSWIND,
    TOKOYO_TOKOYOMOON;

    companion object {
        fun returnNormalCardNameByMegami(megami_name: MegamiEnum):List<CardName>{
            when (megami_name){
                MegamiEnum.YURINA -> return listOf<CardName>(
                    YURINA_CHAM, YURINA_ILSUM, YURINA_JARUCHIGI, YURINA_GUHAB, YURINA_GIBACK,
                    YURINA_APDO, YURINA_GIYENBANJO
                )
                MegamiEnum.SAINE -> return listOf<CardName>(
                    SAINE_HURUBEGI, SAINE_DOUBLEBEGI, SAINE_MOOGECHOO, SAINE_GANPA, SAINE_GWONYUCK,
                    SAINE_CHOONGEMJUNG, SAINE_MOOEMBUCK
                )
                MegamiEnum.HIMIKA -> return listOf<CardName>(
                    HIMIKA_SHOOT, HIMIKA_RAPIDFIRE, HIMIKA_MAGNUMCANON, HIMIKA_FULLBURST,
                    HIMIKA_BACKSTEP, HIMIKA_BACKDRAFT, HIMIKA_SMOKE
                )
                MegamiEnum.TOKOYO -> return listOf<CardName>(
                    TOKOYO_BITSUNERIGI, TOKOYO_WOOAHHANTAGUCK, TOKOYO_RUNNINGRABIT, TOKOYO_POETDANCE,
                    TOKOYO_FLIPFAN, TOKOYO_WINDSTAGE, TOKOYO_SUNSTAGE
                )
            }
        }

        fun returnSpecialCardNameByMegami(megami_name: MegamiEnum): List<CardName> {
            return when (megami_name){
                MegamiEnum.YURINA -> listOf<CardName>(
                    YURINA_WOLYUNGNACK, YURINA_POBARAM, YURINA_JJOCKBAE, YURINA_JURUCK
                )

                MegamiEnum.SAINE -> listOf<CardName>(
                    SAINE_YULDONGHOGEK, SAINE_HANGMUNGGONGJIN, SAINE_EMMOOSHOEBING, SAINE_JONGGEK
                )

                MegamiEnum.HIMIKA -> listOf<CardName>(
                    HIMIKA_REDBULLET, HIMIKA_CRIMSONZERO, HIMIKA_SCARLETIMAGINE, HIMIKA_BURMILIONFIELD
                )

                MegamiEnum.TOKOYO -> listOf<CardName>(
                    TOKOYO_KUON, TOKOYO_THOUSANDBIRD, TOKOYO_ENDLESSWIND, TOKOYO_TOKOYOMOON
                )
            }
        }

        fun returnAdditionalCardNameByMegami(megami_name: MegamiEnum): List<CardName> {
            return when (megami_name){
                MegamiEnum.YURINA -> listOf<CardName>()
                MegamiEnum.SAINE -> listOf<CardName>()
                MegamiEnum.HIMIKA -> listOf<CardName>()
                MegamiEnum.TOKOYO -> listOf<CardName>()
            }
        }

    }
}