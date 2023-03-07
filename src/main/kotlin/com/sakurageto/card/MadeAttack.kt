package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.gamelogic.Umbrella

class MadeAttack(
    var card_number: Int,
    var card_class: CardClass,
    var distance_type: DistanceType,
    var aura_damage: Int,
    var life_damage: Int,
    var distance_cont: Pair<Int, Int>?,
    var distance_uncont: Array<Boolean>?,
    var megami: MegamiEnum
) {
    var isItValid= true
    var isItDamage = true

    var tempForGrandSkyHole: Int? = null

    fun makeNoDamage(){
        isItDamage = false
    }

    fun makeNotValid(){
        isItValid = false
    }

    var bothSideDamage = false

    fun setBothSideDamage(){
        bothSideDamage = true
    }

    constructor(card_number: Int, card_class: CardClass, distance_type: DistanceType, aura_damage: Int, life_damage: Int,
                distance_cont: Pair<Int, Int>?, distance_uncont: Array<Boolean>?, megami: MegamiEnum,
                cannot_react_normal: Boolean, cannot_react_special: Boolean, cannot_react: Boolean):
            this(card_number, card_class, distance_type, aura_damage, life_damage, distance_cont, distance_uncont, megami){
                this.cannot_react_normal = cannot_react_normal
                this.cannot_react_special = cannot_react_special
                this.cannot_react = cannot_react
            }

    var cannot_react_normal = false
    var cannot_react_special = false
    var cannot_react = false

    var effect: MutableList<Text>? = null

    fun addTextAndReturn(umbrella: Umbrella?, card_data: CardData): MadeAttack{
        when(umbrella){
            Umbrella.FOLD -> {
                card_data.effectFold?.let {
                    this.effect = mutableListOf()
                    for(text in it){
                        this.effect!!.add(text)
                    }
                }
            }
            Umbrella.UNFOLD -> {
                card_data.effectUnfold?.let {
                    this.effect = mutableListOf()
                    for(text in it){
                        this.effect!!.add(text)
                    }
                }
            }
            null -> {
                card_data.effect?.let {
                    this.effect = mutableListOf()
                    for(text in it){
                        this.effect!!.add(text)
                    }
                }
            }
        }
        return this
    }

    var chogek = false

    fun Chogek(){
        chogek = true
    }

    fun auraPlusMinus(number: Int){
        if(aura_damage < 999){
            aura_damage += number
            if(aura_damage < 0){
                aura_damage = 0
            }
        }

    }

    fun lifePlusMinus(number: Int){
        if(life_damage < 999){
            life_damage += number
            if(life_damage < 0){
                life_damage = 0
            }
        }
    }
    fun canNotReactNormal(){
        cannot_react_normal = true
    }

    fun canNotReact(){
        cannot_react = true
    }

    //closable true -> increment range from left
    fun plusMinusRange(number: Int, closable: Boolean){
        when(distance_type){
            DistanceType.DISCONTINUOUS -> {
                if (closable) {
                    var min = -1
                    for (i in 0..10) {
                        if(distance_uncont!![i]){
                            min = i
                            break
                        }
                    }
                    if(min != -1){
                        for (i in min - 1 downTo  min - number){
                            if(i < 0) continue
                            distance_uncont!![i] = true
                        }
                    }
                }
                else{
                    var max = 11
                    for (i in 10 downTo 0) {
                        if(distance_uncont!![i]){
                            max = i
                            break
                        }
                    }
                    if(max != 11){
                        for (i in max + 1..max + number){
                            distance_uncont!![i] = true
                        }
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

    fun addRange(range: Pair<Int, Int>){
        when(distance_type){
            DistanceType.DISCONTINUOUS -> {
                for(i in range.first..range.second){
                    distance_uncont!![i] = true
                }
            }
            DistanceType.CONTINUOUS -> {
                if(range.first <= distance_cont!!.first){
                    if(range.second < distance_cont!!.first){
                        distance_type = DistanceType.DISCONTINUOUS
                        distance_uncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
                        for(i in distance_cont!!.first..distance_cont!!.second){
                            distance_uncont!![i] = true
                        }
                        for(i in range.first..range.second){
                            distance_uncont!![i] = true
                        }
                    }
                    else if(distance_cont!!.second < range.second){
                        distance_cont = range
                    }
                    else{
                        distance_cont = distance_cont!!.copy(first = range.first)
                    }
                }
                else{
                    if(range.first > distance_cont!!.second){
                        distance_type = DistanceType.DISCONTINUOUS
                        distance_uncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
                        for(i in distance_cont!!.first..distance_cont!!.second){
                            distance_uncont!![i] = true
                        }
                        for(i in range.first..range.second){
                            distance_uncont!![i] = true
                        }
                    }
                    else if(range.second > distance_cont!!.second){
                        distance_cont = distance_cont!!.copy(second = range.second)
                    }
                }
            }
        }
    }

    //it is true if it can use
    fun rangeCheck(now_range: Int): Boolean{
        return when(distance_type){
            DistanceType.DISCONTINUOUS -> distance_uncont!![now_range]
            DistanceType.CONTINUOUS -> distance_cont!!.first <= now_range && now_range <= distance_cont!!.second
        }
    }

    //{-1, 1, 2, 3, 4, 5, -1, 3, 5, 20, 0, 0, 0, 100}
    //{uncont, distance..., uncont, auro, life, megami, reactable, reactable_normal, reactable_special, cardNumber}
    //{-2, 1, 4, -2, 4, 5, -1, 3, 5, 20, 0, 0, 0, 100}
    //{cont, distance..., cont, auro, life, megami, reactable, reactable_normal, reactable_special, cardNumber}
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
        return_data.add(card_number)

        return return_data
    }

    suspend fun afterAttackProcess(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?){
        this.effect?.let{
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.AFTER_ATTACK){
                    text.effect!!(this.card_number, player, game_status, react_attack)
                }
            }
        }
    }

    suspend fun beforeProcessDamageCheck(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?): Boolean{
        this.effect?.let {
            for(text in it){
                if(text.tag == TextEffectTag.EFFECT_INSTEAD_DAMAGE){
                    return text.effect!!(this.card_number, player, game_status, react_attack) != 1
                }
            }
        }
        return true
    }

}