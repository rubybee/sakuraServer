package com.sakurageto.gamelogic.megamispecial.storyboard

object StoryBoard {
    private val act15 = Act(0, Act.COLOR_END, mutableListOf(), mutableListOf())
    private val act14 = Act(0, Act.COLOR_GREEN, mutableListOf(), mutableListOf(15))
    private val act13 = Act(0, Act.COLOR_PURPLE, mutableListOf(), mutableListOf(15))
    private val act12 = Act(2, Act.COLOR_RED, mutableListOf(), mutableListOf(15))
    private val act11 = Act(3, Act.COLOR_RED, mutableListOf(14), mutableListOf())
    private val act10 = Act(0, Act.COLOR_PURPLE, mutableListOf(12, 13, 14), mutableListOf())
    private val act9 = Act(5, Act.COLOR_PURPLE, mutableListOf(), mutableListOf(11))
    private val act8 = Act(4, Act.COLOR_GOLD, mutableListOf(11), mutableListOf())
    private val act7 = Act(6, Act.COLOR_GREEN, mutableListOf(10), mutableListOf())
    private val act6 = Act(4, Act.COLOR_RED, mutableListOf(10), mutableListOf())
    private val act5 = Act(3, Act.COLOR_RED, mutableListOf(9), mutableListOf())
    private val act4 = Act(4, Act.COLOR_PURPLE, mutableListOf(6, 7), mutableListOf(8))
    private val act3 = Act(2, Act.COLOR_RED, mutableListOf(6), mutableListOf())
    private val act2 = Act(2, Act.COLOR_GREEN, mutableListOf(3, 4), mutableListOf())
    private val act1 = Act(0, Act.COLOR_PURPLE, mutableListOf(3, 4), mutableListOf())
    private val act0 = Act(2, Act.COLOR_WHITE, mutableListOf(1, 2, 5), mutableListOf())

    private val actHashMap = HashMap<Int, Act>()

    fun init(){
        actHashMap[0] = act0
        actHashMap[1] = act1
        actHashMap[2] = act2
        actHashMap[3] = act3
        actHashMap[4] = act4
        actHashMap[5] = act5
        actHashMap[6] = act6
        actHashMap[7] = act7
        actHashMap[8] = act8
        actHashMap[9] = act9
        actHashMap[10] = act10
        actHashMap[11] = act11
        actHashMap[12] = act12
        actHashMap[13] = act13
        actHashMap[14] = act14
        actHashMap[15] = act15
    }

    fun getActByNumber(number: Int): Act {
        return actHashMap[number]?: throw Exception("Act $number not exist")
    }
}