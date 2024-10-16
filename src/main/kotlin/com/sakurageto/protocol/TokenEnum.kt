package com.sakurageto.protocol

import com.sakurageto.card.INDEX_LACERATION_AURA
import com.sakurageto.card.INDEX_LACERATION_FLARE
import com.sakurageto.card.INDEX_LACERATION_LIFE
import com.sakurageto.card.basicenum.PlayerEnum
import java.lang.Exception

enum class TokenEnum(var real_number: Int){
    SAKURA_TOKEN(0),
    YOUR_ARTIFICIAL_SAKURA_TOKEN(1),
    YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN(2),
    YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN(3),
    OTHER_ARTIFICIAL_SAKURA_TOKEN(4),
    OTHER_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN(5),
    OTHER_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN(6),
    FREEZE_TOKEN(7),
    SEED_TOKEN(8),
    LACERATION_YOUR(9),
    LACERATION_OTHER(10);

    fun opposite(): TokenEnum {
        return when(this){
            SAKURA_TOKEN -> SAKURA_TOKEN
            YOUR_ARTIFICIAL_SAKURA_TOKEN -> OTHER_ARTIFICIAL_SAKURA_TOKEN
            YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN -> OTHER_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN
            YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN -> OTHER_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN
            OTHER_ARTIFICIAL_SAKURA_TOKEN -> YOUR_ARTIFICIAL_SAKURA_TOKEN
            OTHER_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN -> YOUR_ARTIFICIAL_SAKURA_TOKEN_ON_TOKEN
            OTHER_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN -> YOUR_ARTIFICIAL_SAKURA_TOKEN_OUT_TOKEN
            FREEZE_TOKEN -> FREEZE_TOKEN
            SEED_TOKEN -> SEED_TOKEN
            LACERATION_YOUR -> LACERATION_OTHER
            LACERATION_OTHER -> LACERATION_YOUR
        }
    }

    companion object {
        fun fromInt(value: Int) = TokenEnum.values().first { it.real_number == value }

        fun getLaceration(getDamage: PlayerEnum, giveDamage: PlayerEnum) = if(getDamage == giveDamage) LACERATION_YOUR
        else LACERATION_OTHER

        fun Int.toLacerationLocation() = when(this){
            INDEX_LACERATION_AURA -> LocationEnum.AURA_YOUR
            INDEX_LACERATION_FLARE -> LocationEnum.FLARE_YOUR
            INDEX_LACERATION_LIFE -> LocationEnum.LIFE_YOUR
            else -> throw Exception("invalid int value to laceration index: $this")
        }
    }
}