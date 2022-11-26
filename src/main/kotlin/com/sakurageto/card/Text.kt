package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus

class Text(
    val timing_tag: TextEffectTimingTag, val tag: TextEffectTag, val effect:
    ((PlayerEnum, GameStatus, MadeAttack?) -> Boolean?)?
) {

}