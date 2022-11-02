package com.sakurageto.card

class MadeAttack(
    var distance_type: DistanceType,
    var life_damage: Int,
    var aura_damage: Int,
    var distance_cont: Pair<Int, Int>?,
    var distance_uncont: Array<Boolean>?,
) {

}