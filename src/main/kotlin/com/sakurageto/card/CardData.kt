package com.sakurageto.card

import com.sakurageto.gamelogic.MegamiEnum

class CardData(
    val card_class: CardClass,
    val card_name: CardName,
    val megami: MegamiEnum,
    val card_type: CardType,
    val sub_type: SubType,
) {
    //attack
    var distance_type: DistanceType? = null
    var distance_cont: Pair<Int, Int>? = null
    var distance_uncont: Array<Boolean>? = null
    var life_damage: Int? = null
    var aura_damage: Int? = null

    //Enchantment
    var charge: Int? = null

    //Special
    var cost: Int? = null

    //general
    var effect: MutableList<Text>? = null

    fun setAttack(distance_type: DistanceType, distance_cont: Pair<Int, Int>?, distance_uncont: MutableList<Int>?,
                  life_damage: Int, aura_damage: Int){
        this.distance_type = distance_type
        if(distance_type == DistanceType.CONTINUOUS){
            this.distance_cont = Pair(distance_cont!!.first, distance_cont!!.second)
        }
        else{
            this.distance_uncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
            for (i in distance_uncont!!){
                this.distance_uncont!![i] = true
            }
        }
        this.life_damage = life_damage
        this.aura_damage = aura_damage
    }

    fun setEnchantment(charge: Int){
        this.charge = charge
    }

    fun setSpecial(cost: Int){
        this.charge = cost
    }

    fun addtext(text: Text){
        if(this.effect == null){
            this.effect = mutableListOf()
        }
        this.effect!!.add(text)
    }

}