package com.sakurageto.card

import com.sakurageto.gamelogic.MegamiEnum

class MadeAttack(
    var distance_type: DistanceType,
    var life_damage: Int,
    var aura_damage: Int,
    var distance_cont: Pair<Int, Int>?,
    var distance_uncont: Array<Boolean>?,
    var megami: MegamiEnum
) {

    var cannot_react_normal = false
    var cannot_react_special = false
    var cannot_react = false

    var chogek = false

    fun Chogek(){
        chogek = true
    }

    fun auraPlusMinus(number: Int){
        if(aura_damage != 999){
            aura_damage += number
            if(aura_damage!! < 0){
                aura_damage = 0
            }
        }

    }

    fun lifePlusMinus(number: Int){
        if(life_damage != 999){
            life_damage += number
            if(life_damage < 0){
                life_damage = 0
            }
        }
    }
    fun canNotReactNormal(){
        cannot_react_normal = true
    }

    //closable true -> increment range from left
    fun addRange(number: Int, closable: Boolean){
        when(distance_type){
            DistanceType.DISCONTINUOUS -> {
                if (closable) {
                    var min = 0
                    for (i in 0..10) {
                        if(distance_uncont!![i]){
                            min = i
                            break
                        }
                    }
                    for (i in min - number until min){
                        distance_uncont!![i] = true
                    }
                }
                else{
                    var max = 10
                    for (i in 10 downTo 0) {
                        if(distance_uncont!![i]){
                            max = i
                            break
                        }
                    }
                    for (i in max + 1..max + number){
                        distance_uncont!![i] = true
                    }
                }
            }
            DistanceType.CONTINUOUS -> {
                if (closable){
                    if(distance_cont!!.first == 0){
                        return
                    }
                    else{
                        var now = distance_cont!!.first
                        now -= number
                        if(now < 0){
                            now = 0
                        }
                        distance_cont = distance_cont!!.copy(first = now)
                    }
                }
                else{
                    var now = distance_cont!!.second
                    now += number
                    distance_cont = distance_cont!!.copy(second = now)
                }
            }
        }
    }

    fun rangeCheck(now_range: Int): Boolean{
        when(distance_type){
            DistanceType.DISCONTINUOUS -> return distance_uncont!![now_range]
            DistanceType.CONTINUOUS -> return distance_cont!!.first <= now_range && now_range <= distance_cont!!.second
        }
    }

    //{-1, 1, 2, 3, 4, 5, -1, 3, 5, 20, 0, 0, 0}
    //{uncont, distance..., uncont, auro, life, megami, reactable}
    fun Information(): MutableList<Int>{
        var return_data = mutableListOf<Int>()
        when(distance_type){
            DistanceType.DISCONTINUOUS -> {
                return_data.add(-1)
                for(i in distance_uncont!!.indices){
                    if(distance_uncont!![i]) return_data.add(i)
                }
                return_data.add(-1)
            }
            DistanceType.CONTINUOUS -> {
                return_data.add(-2)
                return_data.add(distance_cont!!.first)
                return_data.add(distance_cont!!.second)
                return_data.add(-2)
            }
        }
        return_data.add(aura_damage)
        return_data.add(life_damage)
        return_data.add(megami.real_number)
        if(cannot_react) return_data.add(1) else return_data.add(0)
        if(cannot_react_normal) return_data.add(1) else return_data.add(0)
        if(cannot_react_special) return_data.add(1) else return_data.add(0)

        return return_data
    }

}