package com.sakurageto.gamelogic.buff.range

import com.sakurageto.card.MadeAttack
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameStatus

class RangeBuff(
    val cardNumber: Int, var counter: Int, val tag: RangeBuffTag, val condition: (PlayerEnum, GameStatus, MadeAttack) -> Boolean, val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
)