package com.sakurageto.card

import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.MegamiEnum
import com.sakurageto.gamelogic.Umbrella

class MadeAttack(
    var card_name: CardName,
    var card_number: Int,
    var card_class: CardClass,
    private val distance_type: DistanceType,
    private val aura_damage: Int,
    private val life_damage: Int,
    private val distance_cont: Pair<Int, Int>?,
    private val distance_uncont: Array<Boolean>?,
    var megami: MegamiEnum,
    private val cannotReactNormal: Boolean,
    private val cannotReactSpecial: Boolean,
    private val cannotReact: Boolean,
    private val chogek: Boolean,
    private val inevitable: Boolean = false,
    val subType: SubType = SubType.NONE
) {
    var editedChogek = false

    var isItReact = true

    fun Chogek(){
        editedChogek = true
    }

    fun auraPlusMinus(number: Int){
        if(editedAuraDamage < 999){
            editedAuraDamage += number
        }

    }

    fun lifePlusMinus(number: Int) {
        if (editedLifeDamage < 999) {
            editedLifeDamage += number
        }
    }

    var editedAuraDamage = -1
    var editedLifeDamage = -1

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
        editedAuraDamage = aura_damage
        editedLifeDamage = life_damage
        thisTurnAttackBuff.addAllBuff(continuousAttackBuff)
        continuousAttackBuff.clearBuff()
        for(index in 0 until AttackBuffQueue.buffQueueNumber){
            val tempQueue: ArrayDeque<Buff> = ArrayDeque()
            thisTempAttackBuff.applyBuff(index, player, game_status, this, tempQueue)
            thisTurnAttackBuff.applyBuff(index, player, game_status, this, tempQueue, continuousAttackBuff)
            for(buff in tempQueue){
                buff.effect(player, game_status, this)
            }
        }
        if(!editedChogek && editedAuraDamage >= 5 && editedAuraDamage != 999) editedAuraDamage = 5
        return Pair(editedAuraDamage, editedLifeDamage)
    }

    var kururuChangeRangeUpper = false
    var kururuChangeRangeUnder = false

    var isItValid= true
    var isItDamage = true

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

    var editedDistanceType = DistanceType.CONTINUOUS
    var editedDistanceCont: Pair<Int, Int>? = null
    var editedDistanceUncont: Array<Boolean>? = null

    private val thisTurnRangeBuff = RangeBuffQueue()
    private val thisTempRangeBuff = RangeBuffQueue()

    fun returnWhenBuffDoNotUse(rangeBuff: RangeBuffQueue){
        thisTurnRangeBuff.cleanNotUsedBuff(rangeBuff)
    }

    fun addTempRangeBuff(tempRangeBuff: RangeBuffQueue){
        thisTempRangeBuff.addAllBuff(tempRangeBuff)
        tempRangeBuff.clearBuff()
    }

    fun addRangeBuff(buff: RangeBuff){
        thisTempRangeBuff.addRangeBuff(buff)
    }

    suspend fun rangeCheck(now_range: Int, game_status: GameStatus, player: PlayerEnum, continuousRangeBuff: RangeBuffQueue): Boolean{
        editedDistanceType = distance_type
        editedDistanceCont = distance_cont
        editedDistanceUncont = distance_uncont
        thisTurnRangeBuff.addAllBuff(continuousRangeBuff)
        continuousRangeBuff.clearBuff()
        for(index in 0 until RangeBuffQueue.buffQueueNumber) {
            val tempQueue: ArrayDeque<RangeBuff> = ArrayDeque()
            thisTempRangeBuff.applyBuff(index, player, game_status, this, tempQueue)
            thisTurnRangeBuff.applyBuff(index, player, game_status, this, tempQueue, continuousRangeBuff)
            for (buff in tempQueue) {
                buff.effect(player, game_status, this)
            }
        }

        return when(editedDistanceType){
            DistanceType.DISCONTINUOUS -> editedDistanceUncont!![now_range]
            DistanceType.CONTINUOUS -> editedDistanceCont!!.first <= now_range && now_range <= editedDistanceCont!!.second
        }
    }

    //closable true -> increment range from left
    fun plusMinusRange(number: Int, closable: Boolean){
        if(number >= 0){
            when(editedDistanceType){
                DistanceType.DISCONTINUOUS -> {
                    if (closable) {
                        var min = -1
                        for (i in 0..10) {
                            if(editedDistanceUncont!![i]){
                                min = i
                                break
                            }
                        }
                        if(min != -1){
                            for (i in min - 1 downTo  min - number){
                                if(i < 0) continue
                                editedDistanceUncont!![i] = true
                            }
                        }
                    }
                    else{
                        var max = 11
                        for (i in 10 downTo 0) {
                            if(editedDistanceUncont!![i]){
                                max = i
                                break
                            }
                        }
                        if(max != 11){
                            for (i in max + 1..max + number){
                                editedDistanceUncont!![i] = true
                            }
                        }
                    }
                }
                DistanceType.CONTINUOUS -> {
                    if (closable){
                        if(editedDistanceCont!!.first == 0){
                            return
                        }
                        else{
                            var now = editedDistanceCont!!.first
                            now -= number
                            if(now < 0){
                                now = 0
                            }
                            editedDistanceCont = editedDistanceCont!!.copy(first = now)
                        }
                    }
                    else{
                        var now = editedDistanceCont!!.second
                        now += number
                        editedDistanceCont = editedDistanceCont!!.copy(second = now)
                    }
                }
            }
        }
        else{

            when(editedDistanceType){
                DistanceType.DISCONTINUOUS -> {
                    if (closable) {
                        var min = -1
                        for (i in 0..10) {
                            if(editedDistanceUncont!![i]){
                                min = i
                                break
                            }
                        }
                        if(min != -1){
                            for (i in min + number + 1..min){
                                if(i < 0) continue
                                editedDistanceUncont!![i] = false
                            }
                        }
                    }
                    else{
                        var max = 11
                        for (i in 10 downTo 0) {
                            if(editedDistanceUncont!![i]){
                                max = i
                                break
                            }
                        }
                        if(max != 11){
                            for (i in max + number + 1..max){
                                editedDistanceUncont!![i] = false
                            }
                        }
                    }
                }
                DistanceType.CONTINUOUS -> {
                    if (closable){
                        if(editedDistanceCont!!.first == 0){
                            return
                        }
                        else{
                            var now = editedDistanceCont!!.first
                            now -= number
                            if(now < 0){
                                now = 0
                            }
                            editedDistanceCont = editedDistanceCont!!.copy(first = now)
                        }
                    }
                    else{
                        var now = editedDistanceCont!!.second
                        now += number
                        editedDistanceCont = editedDistanceCont!!.copy(second = now)
                    }
                    if(editedDistanceCont!!.first < editedDistanceCont!!.second){
                        editedDistanceType = DistanceType.DISCONTINUOUS
                        editedDistanceUncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
                        editedDistanceCont = null
                    }
                }
            }
        }


    }

    fun addRange(range: Pair<Int, Int>){
        when(editedDistanceType){
            DistanceType.DISCONTINUOUS -> {
                for(i in range.first..range.second){
                    editedDistanceUncont!![i] = true
                }
            }
            DistanceType.CONTINUOUS -> {
                if(range.first <= editedDistanceCont!!.first){
                    if(range.second < editedDistanceCont!!.first){
                        editedDistanceType = DistanceType.DISCONTINUOUS
                        editedDistanceUncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
                        for(i in editedDistanceCont!!.first..editedDistanceCont!!.second){
                            editedDistanceUncont!![i] = true
                        }
                        for(i in range.first..range.second){
                            editedDistanceUncont!![i] = true
                        }
                    }
                    else if(editedDistanceCont!!.second < range.second){
                        editedDistanceCont = range
                    }
                    else{
                        editedDistanceCont = editedDistanceCont!!.copy(first = range.first)
                    }
                }
                else{
                    if(range.first > editedDistanceCont!!.second){
                        editedDistanceType = DistanceType.DISCONTINUOUS
                        editedDistanceUncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
                        for(i in editedDistanceCont!!.first..editedDistanceCont!!.second){
                            editedDistanceUncont!![i] = true
                        }
                        for(i in range.first..range.second){
                            editedDistanceUncont!![i] = true
                        }
                    }
                    else if(range.second > editedDistanceCont!!.second){
                        editedDistanceCont = editedDistanceCont!!.copy(second = range.second)
                    }
                }
            }
        }
    }

    fun deleteRange(range: Pair<Int, Int>){
        when(editedDistanceType){
            DistanceType.DISCONTINUOUS -> {
                for(i in range.first..range.second){
                    editedDistanceUncont!![i] = false
                }
            }
            DistanceType.CONTINUOUS -> {
                if(range.second >= editedDistanceCont!!.first) {
                    if(range.first > editedDistanceCont!!.first){
                        if(range.second >= editedDistanceCont!!.second){
                            editedDistanceCont = editedDistanceCont!!.copy(second = range.first - 1)
                        }
                        else{
                            editedDistanceType = DistanceType.DISCONTINUOUS
                            editedDistanceUncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
                            for(i in editedDistanceCont!!.first..editedDistanceCont!!.second){
                                editedDistanceUncont!![i] = true
                            }
                            for(i in range.first..range.second){
                                editedDistanceUncont!![i] = false
                            }
                        }
                    }
                    else{
                        if(range.second >= editedDistanceCont!!.second){
                            editedDistanceType = DistanceType.DISCONTINUOUS
                            editedDistanceUncont = arrayOf(false, false, false, false, false, false, false, false, false, false, false)
                            editedDistanceCont = null
                        }
                        else{
                            editedDistanceCont = editedDistanceCont!!.copy(first = range.second + 1)
                        }
                    }
                }
            }
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
    fun Information(): MutableList<Int>{
        val returnData = mutableListOf<Int>()
        when(distance_type){
            DistanceType.DISCONTINUOUS -> {
                returnData.add(-1)
                for(i in distance_uncont!!.indices){
                    if(distance_uncont[i]) returnData.add(i)
                }
                returnData.add(-1)
            }
            DistanceType.CONTINUOUS -> {
                returnData.add(-2)
                returnData.add(distance_cont!!.first)
                returnData.add(distance_cont.second)
                returnData.add(-2)
            }
        }
        returnData.add(aura_damage)
        returnData.add(life_damage)
        returnData.add(megami.real_number)
        if(cannotReact) returnData.add(1) else returnData.add(0)
        if(cannotReactNormal) returnData.add(1) else returnData.add(0)
        if(cannotReactSpecial) returnData.add(1) else returnData.add(0)
        returnData.add(card_number)

        return returnData
    }

    suspend fun afterAttackProcess(player: PlayerEnum, game_status: GameStatus, react_attack: MadeAttack?, damageSelect: DamageSelect){
        for(card in game_status.getPlayer(player.opposite()).enchantmentCard.values){
            if(card.canUseEffectCheck(TextEffectTag.AFTER_ATTACK_EFFECT_INVALID_OTHER)){
                return
            }
        }
        this.effect?.let{
            for(text in it){
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