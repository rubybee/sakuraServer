package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.gamelogic.Umbrella
import java.util.SortedSet

class MadeAttack(
    var card_name: CardName,
    var card_number: Int,
    var card_class: CardClass,
    private val distance: SortedSet<Int>,
    private val aura_damage: Int,
    private val life_damage: Int,
    var megami: MegamiEnum,
    private val cannotReactNormal: Boolean,
    private val cannotReactSpecial: Boolean,
    private val cannotReact: Boolean,
    private val chogek: Boolean,
    private val inevitable: Boolean = false,
    val subType: SubType = SubType.NONE,
    val damageNotChange: Boolean = false,
    var isLaceration: Boolean = false
) {
    var editedChogek = false

    var isItReact = true

    fun Chogek(){
        editedChogek = true
    }

    fun auraPlusMinus(number: Int){
        if(tempEditedAuraDamage < 999){
            tempEditedAuraDamage += number
        }

    }

    fun lifePlusMinus(number: Int) {
        if (tempEditedLifeDamage < 999) {
            tempEditedLifeDamage += number
        }
    }


    var tempEditedAuraDamage = -1
    var tempEditedLifeDamage = -1
    private var editedAuraDamage = -1

    fun getEditedAuraDamage() = editedAuraDamage

    private var editedLifeDamage = -1

    fun getEditedLifeDamage() = editedLifeDamage

    private val thisTempAttackBuff: AttackBuffQueue = AttackBuffQueue()
    private val thisTurnAttackBuff: AttackBuffQueue = AttackBuffQueue()

    fun addTempAttackBuff(tempAttackBuff: AttackBuffQueue){
        thisTempAttackBuff.addAllBuff(tempAttackBuff)
        tempAttackBuff.clearBuff()
    }

    fun addAttackBuff(buff: Buff){
        thisTempAttackBuff.addAttackBuff(buff)
    }

    suspend fun getDamage(game_status: GameStatus, player: PlayerEnum, continuousAttackBuff: AttackBuffQueue): Pair<Int, Int>{
        editedChogek = chogek
        editedAuraDamage = aura_damage; tempEditedAuraDamage = aura_damage
        editedLifeDamage = life_damage; tempEditedLifeDamage = life_damage
        if(damageNotChange){
            return Pair(editedAuraDamage, editedLifeDamage)
        }
        thisTurnAttackBuff.addAllBuff(continuousAttackBuff)
        continuousAttackBuff.clearBuff()
        for(index in 0 until AttackBuffQueue.buffQueueNumber){
            val tempQueue: ArrayDeque<Buff> = ArrayDeque()
            thisTempAttackBuff.applyBuff(index, player, game_status, this, tempQueue)
            thisTurnAttackBuff.applyBuff(index, player, game_status, this, tempQueue, continuousAttackBuff)
            for(buff in tempQueue){
                buff.effect(player, game_status, this)
            }
            editedAuraDamage = tempEditedAuraDamage
            editedLifeDamage = tempEditedLifeDamage
        }
        if(!editedChogek && editedAuraDamage >= 5 && editedAuraDamage != 999) editedAuraDamage = 5
        return Pair(editedAuraDamage, editedLifeDamage)
    }

    var kururuChangeRangeUpper = false
    var kururuChangeRangeUnder = false
    var kururuChange2X = false

    var isItValid= true
    var isItDamage = true
    var canNotSelectAura = false

    fun makeNoDamage(){
        isItDamage = false
    }

    fun makeNotValid(){
        isItValid = false
    }

    fun makeInevitable(){
        editedInevitable = true
    }

    var bothSideDamage = false

    fun setBothSideDamage(){
        bothSideDamage = true
    }

    var editedInevitable = false
    var editedCannotReactNormal = false
    var editedCannotReactSpecial = false
    var editedCannotReact = false

    private val thisTempOtherBuff = OtherBuffQueue()
    private val thisTurnOtherBuff = OtherBuffQueue()

    fun addTempOtherBuff(tempOtherBuff: OtherBuffQueue){
        thisTempOtherBuff.addAllBuff(tempOtherBuff)
        tempOtherBuff.clearBuff()
    }

    fun addOtherBuff(buff: OtherBuff){
        thisTempOtherBuff.addOtherBuff(buff)
    }

    suspend fun activeOtherBuff(game_status: GameStatus, player: PlayerEnum, continuousOtherBuff: OtherBuffQueue){
        editedInevitable = inevitable
        editedCannotReactNormal = cannotReactNormal
        editedCannotReactSpecial = cannotReactSpecial
        editedCannotReact = cannotReact
        thisTurnOtherBuff.addAllBuff(continuousOtherBuff)
        continuousOtherBuff.clearBuff()
        for(index in 0 until OtherBuffQueue.buffQueueNumber){
            val tempQueue: ArrayDeque<OtherBuff> = ArrayDeque()
            thisTempOtherBuff.applyBuff(index, player, game_status, this, tempQueue)
            thisTurnOtherBuff.applyBuff(index, player, game_status, this, tempQueue, continuousOtherBuff)
            for(buff in tempQueue){
                buff.effect(player, game_status, this)
            }
        }
    }

    suspend fun effectText(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, tag: TextEffectTag): Int?{
        this.effect?.let {
            for(text in it){
                if(text.tag == tag){
                    return text.effect!!(this.card_number, player, game_status, react_attack)
                }
            }
        }
        return null
    }

    suspend fun effectText(card_number: Int, player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, tag: TextEffectTag): Int?{
        this.effect?.let {
            for(text in it){
                if(text.tag == tag){
                    return text.effect!!(card_number, player, game_status, react_attack)
                }
            }
        }
        return null
    }

    suspend fun canReactByThisCard(card: Card, game_status: GameStatus, player: PlayerEnum, continuousOtherBuff: OtherBuffQueue): Boolean{
        activeOtherBuff(game_status, player, continuousOtherBuff)

        if(this.editedCannotReactSpecial){
            if(card.card_data.card_class == CardClass.SPECIAL){
                return false
            }
        }
        else if(this.editedCannotReact){
            return false
        }
        else if(this.editedCannotReactNormal){
            if(card.card_data.card_class == CardClass.NORMAL){
                return false
            }
        }
        return true
    }

    fun canNotReactNormal(){
        editedCannotReactNormal = true
    }

    fun canNotReact(){
        editedCannotReact = true
    }

    var editedDistance: SortedSet<Int> = sortedSetOf()

    private val thisTurnRangeBuff = RangeBuffQueue()
    private val thisTempRangeBuff = RangeBuffQueue()

    fun returnWhenBuffDoNotUse(rangeBuff: RangeBuffQueue){
        thisTurnRangeBuff.cleanNotUsedBuff(rangeBuff)
    }

    fun addTempRangeBuff(tempRangeBuff: RangeBuffQueue){
        thisTempRangeBuff.addAllBuff(tempRangeBuff)
        tempRangeBuff.clearBuff()
    }

    fun addRangeBuff(buff_number: Int, buff: RangeBuff){
        thisTempRangeBuff.addRangeBuff(buff_number, buff)
    }

    val tempEditedDistance = mutableListOf<Int>()

    suspend fun rangeCheck(now_range: Int, game_status: GameStatus, player: PlayerEnum, continuousRangeBuff: RangeBuffQueue): Boolean{
        editedDistance = distance.toSortedSet()
        thisTurnRangeBuff.addAllBuff(continuousRangeBuff)
        continuousRangeBuff.clearBuff()
        for(index in 0 until RangeBuffQueue.buffQueueNumber) {
            val tempQueue: ArrayDeque<RangeBuff> = ArrayDeque()
            thisTempRangeBuff.applyBuff(index, player, game_status, this, tempQueue)
            thisTurnRangeBuff.applyBuff(index, player, game_status, this, tempQueue, continuousRangeBuff)
            for (buff in tempQueue) {
                buff.effect(player, game_status, this)
            }
            if(index == RangeBuffQueue.INDEX_ADD){
                for(i in tempEditedDistance){
                    editedDistance.add(i)
                }
                tempEditedDistance.clear()
            }
            else if(index == RangeBuffQueue.INDEX_DELETE){
                for(i in tempEditedDistance){
                    editedDistance.remove(i)
                }
                tempEditedDistance.clear()
            }
        }
        return now_range in editedDistance
    }

    //closable true -> increment range from left
    fun plusMinusRange(number: Int, closable: Boolean){
        if(number > 0){
            if(closable){
                val min = editedDistance.first()
                for(i in min - number until min){
                    editedDistance.add(i)
                }
            }
            else{
                val max = editedDistance.last()
                for(i in max + 1 .. max + number){
                    editedDistance.add(i)
                }
            }
        }
        else if(number < 0){
            if(closable){
                val min = editedDistance.first()
                for(i in min until min + number){
                    editedDistance.remove(i)
                }
            }
            else{
                val max = editedDistance.last()
                for(i in max + number + 1 .. max){
                    editedDistance.remove(i)
                }
            }
        }

    }

    fun addRange(range: Pair<Int, Int>){
        for(i in range.first..range.second){
            editedDistance.add(i)
        }
    }

    var effect: MutableList<Text>? = null


    fun addTextAndReturn(text: Text): MadeAttack{
        if(effect == null) effect = mutableListOf()
        effect!!.add(text)
        return this
    }

    fun addTextAndReturn(umbrella: Umbrella?, card_data: CardData): MadeAttack{
        if(card_data.umbrellaMark){
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

                }
            }
        }

        card_data.effect?.let {
            this.effect = mutableListOf()
            for(text in it){
                this.effect!!.add(text)
            }
        }
        return this
    }


    //{-1, 1, 2, 3, 4, 5, -1, 3, 5, 20, 0, 0, 0, 100}
    //{uncont, distance..., uncont, auro, life, megami, reactable, reactable_normal, reactable_special, cardNumber}
    //{-2, 1, 4, -2, 4, 5, -1, 3, 5, 20, 0, 0, 0, 100}
    //{cont, distance..., cont, auro, life, megami, reactable, reactable_normal, reactable_special, cardNumber}
    fun toInformation(): MutableList<Int>{
        val returnData = mutableListOf<Int>()
        returnData.add(-1)
        for(i in distance){
            returnData.add(i)
        }
        returnData.add(-1)
        returnData.add(aura_damage)
        returnData.add(life_damage)
        returnData.add(megami.real_number)
        if(cannotReact) returnData.add(1) else returnData.add(0)
        if(cannotReactNormal) returnData.add(1) else returnData.add(0)
        if(cannotReactSpecial) returnData.add(1) else returnData.add(0)
        returnData.add(card_name.toCardNumber(true))

        return returnData
    }

    suspend fun afterAttackProcess(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, damageSelect: DamageSelect){
        this.effect?.let{
            for(text in it){
                for(card in game_status.getPlayer(player.opposite()).enchantmentCard.values){
                    if(card.canUseEffectCheck(TextEffectTag.AFTER_ATTACK_EFFECT_INVALID_OTHER)){
                        return
                    }
                }
                if(text.timing_tag == TextEffectTimingTag.AFTER_ATTACK){
                    if(text.tag == TextEffectTag.WHEN_CHOOSE_AURA_DAMAGE){
                        if(damageSelect == DamageSelect.AURA){
                            text.effect!!(this.card_number, player, game_status, react_attack)
                        }
                    }
                    else if(text.tag == TextEffectTag.WHEN_CHOOSE_LIFE_DAMAGE){
                        if(damageSelect == DamageSelect.LIFE){
                            text.effect!!(this.card_number, player, game_status, react_attack)
                        }
                    }
                    else{
                        text.effect!!(this.card_number, player, game_status, react_attack)
                    }
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

    fun addValidEffect(effectTag: TextEffectTag, queue: HashMap<Int, Text>){
        this.effect?.let {
            for(text in it){
                if(text.tag == effectTag) {
                    queue[this.card_number] = text
                    return
                }
            }
        }
    }

    fun copyAfterAttackTo(madeAttack: MadeAttack){
        effect?.let {
            for(text in it){
                if(text.timing_tag == TextEffectTimingTag.AFTER_ATTACK){
                    if(madeAttack.effect == null){
                        madeAttack.effect = mutableListOf()
                    }
                    else{
                        madeAttack.effect!!.add(text)
                    }
                }
            }
        }
    }

}