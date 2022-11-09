package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus

class AttackBuff(
val tag: AttackBufTag, val condition: (PlayerEnum, GameStatus) -> Boolean, val effect: (MadeAttack) -> Unit
) {

}

class RangeBuff(
    val tag: RangeBufTag, val condition: (PlayerEnum, GameStatus) -> Boolean, val effect: (MadeAttack) -> Unit
) {

}