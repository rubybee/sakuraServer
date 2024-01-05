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
    var distanceType: DistanceType? = null
    var distanceCont: Pair<Int, Int>? = null
    var distanceUncont: Array<Boolean>? = null
    var lifeDamage: Int? = null
    var auraDamage: Int? = null
    var isLaceration: Boolean = false

    //Enchantment
    var charge: Int? = null

    //Special
    var cost: Int? = null

    //general
    var effect: MutableList<Text>? = null
    var canCover = true
    var canDiscard = true

    var cannotReactNormal: Boolean = false
    var cannotReactSpecial: Boolean = false
    var cannotReact: Boolean = false
    var chogek: Boolean = false
    var inevitable: Boolean = false

    //for yukihi
    var umbrellaMark: Boolean = false

    var effectFold: MutableList<Text>? = null
    var effectUnfold: MutableList<Text>? = null

    var distanceTypeFold: DistanceType? = null
    var distanceContFold: Pair<Int, Int>? = null
    var distanceUncontFold: Array<Boolean>? = null
    var lifeDamageFold: Int? = null
    var auraDamageFold: Int? = null

    var distanceTypeUnfold: DistanceType? = null
    var distanceContUnfold: Pair<Int, Int>? = null
    var distanceUncontUnfold: Array<Boolean>? = null
    var lifeDamageUnfold: Int? = null
    var auraDamageUnfold: Int? = null

    //for megumi
    var growing: Int = 0

    /***
     damage 999 means -, damage 1000 means X, Y
     */
    fun setAttack(distance_type: DistanceType, distance_cont: Pair<Int, Int>?, distance_uncont: MutableList<Int>?,
                  aura_damage: Int, life_damage: Int, cannotReactNormal: Boolean, cannotReactSpecial: Boolean,
                  cannotReact: Boolean, chogek: Boolean, inevitable: Boolean = false, isLaceration: Boolean = false){
        this.distanceType = distance_type
        if(distance_type == DistanceType.CONTINUOUS){
            this.distanceCont = Pair(distance_cont!!.first, distance_cont.second)
        }
        else{
            this.distanceUncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false)
            for (i in distance_uncont!!){
                this.distanceUncont!![i] = true
            }
        }
        this.lifeDamage = life_damage
        this.auraDamage = aura_damage
        this.cannotReactNormal = cannotReactNormal
        this.cannotReactSpecial = cannotReactSpecial
        this.cannotReact = cannotReact
        this.chogek = chogek
        this.inevitable = inevitable
        this.isLaceration = isLaceration
    }

    fun setAttackFold(distance_type: DistanceType, distance_cont: Pair<Int, Int>?, distance_uncont: MutableList<Int>?,
                  aura_damage: Int, life_damage: Int){
        this.distanceTypeFold = distance_type
        if(distance_type == DistanceType.CONTINUOUS){
            this.distanceContFold = Pair(distance_cont!!.first, distance_cont.second)
        }
        else{
            this.distanceUncontFold = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
            for (i in distance_uncont!!){
                this.distanceUncont!![i] = true
            }
        }
        this.lifeDamageFold = life_damage
        this.auraDamageFold = aura_damage
    }

    fun setAttackUnfold(distance_type: DistanceType, distance_cont: Pair<Int, Int>?, distance_uncont: MutableList<Int>?,
                        aura_damage: Int, life_damage: Int){
        this.distanceTypeUnfold = distance_type
        if(distance_type == DistanceType.CONTINUOUS){
            this.distanceContUnfold = Pair(distance_cont!!.first, distance_cont.second)
        }
        else{
            this.distanceUncontUnfold = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
            for (i in distance_uncont!!){
                this.distanceUncont!![i] = true
            }
        }
        this.lifeDamageUnfold = life_damage
        this.auraDamageUnfold = aura_damage
    }

    fun addTextFold(text: Text){
        this.effectFold?.add(text) ?: run {
            this.effectFold = mutableListOf()
        }
    }

    fun addTextUnfold(text: Text){
        this.effectUnfold?.add(text) ?: run {
            effectUnfold = mutableListOf()
        }
    }

    fun setEnchantment(charge: Int){
        this.charge = charge
    }

    /**
     * @param cost : minus means laceration
     *
     * : null means X
     */
    fun setSpecial(cost: Int?){
        this.cost = cost
    }

    fun addtext(text: Text){
        if(this.effect == null){
            this.effect = mutableListOf()
        }
        this.effect!!.add(text)
    }

    fun isItSpecial(): Boolean{
        return this.card_class == CardClass.SPECIAL
    }

    fun isPerjure(): Boolean{
        effect?.let {
            for (text in it){
                if(text === CardSet.perjureText) return true
            }
        }
        return false
    }
}