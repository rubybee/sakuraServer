package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus

class Text(
    val timing_tag: TextEffectTimingTag, val tag: TextEffectTag, val effect:
    (suspend (Int, PlayerEnum, GameStatus, MadeAttack?) -> Int?)?
)