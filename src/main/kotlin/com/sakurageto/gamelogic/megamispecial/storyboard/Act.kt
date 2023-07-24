package com.sakurageto.gamelogic.megamispecial.storyboard

/**
 nextact and nextacttrial's elements must not be changed
 */
class Act(val actValue: Int, val actColor: Byte, private val nextAct: MutableList<Int>, private val nextActTrial: MutableList<Int>) {
    companion object{
        const val COLOR_WHITE: Byte = 0
        const val COLOR_PURPLE: Byte = 1
        const val COLOR_GREEN: Byte = 2
        const val COLOR_RED: Byte = 3
        const val COLOR_GOLD: Byte = 4
        const val COLOR_END: Byte = 5
    }

    fun getNextActTrial() = nextActTrial

    fun getNextAct() = nextAct
}