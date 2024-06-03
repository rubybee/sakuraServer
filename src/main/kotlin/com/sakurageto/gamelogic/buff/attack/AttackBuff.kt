package com.sakurageto.gamelogic.buff.attack

import com.sakurageto.card.MadeAttack
import com.sakurageto.card.basicenum.PlayerEnum
import com.sakurageto.gamelogic.GameStatus

class AttackBuff(
    val cardNumber: Int, val tag: AttackBuffTag, val condition: suspend (PlayerEnum, GameStatus, MadeAttack) -> Boolean,
    val effect: suspend (PlayerEnum, GameStatus, MadeAttack) -> Unit
)