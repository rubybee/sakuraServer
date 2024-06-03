package com.sakurageto.gamelogic.buff.other

import com.sakurageto.card.MadeAttack
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameStatus

class OtherBuff(
    val cardNumber: Int, val tag: OtherBuffTag, val condition: suspend (PlayerEnum, GameStatus, MadeAttack) -> Boolean,
    val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
)