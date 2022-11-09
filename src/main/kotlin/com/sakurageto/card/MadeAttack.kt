package com.sakurageto.card

class MadeAttack(
    var distance_type: DistanceType,
    var life_damage: Int,
    var aura_damage: Int,
    var distance_cont: Pair<Int, Int>?,
    var distance_uncont: Array<Boolean>?,
) {

    var cannot_react_normal = false
    var cannot_react_special = false
    var cannot_react = false

    fun auraPlusMinus(number: Int){
        aura_damage += number
        if(aura_damage < 0){
            aura_damage = 0
        }
    }

    fun lifePlusMinus(number: Int){
        life_damage += number
        if(life_damage < 0){
            life_damage = 0
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
                    for (i in min - number..min - 1){
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

}