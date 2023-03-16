package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.card.*
import com.sakurageto.card.CardSet.cardNameHashmapSecond
import com.sakurageto.card.CardSet.cardNameHashmapFirst
import com.sakurageto.card.CardSet.cardNumberHashmap
import com.sakurageto.card.CardSet.returnCardDataByName
import com.sakurageto.protocol.*
import io.ktor.websocket.*

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus, private val player1_socket: Connection, private val player2_socket: Connection) {
    companion object{
        const val swellDistance = 2
    }

    var turnPlayer = PlayerEnum.PLAYER1

    val logger = Logger()

    var endCurrentPhase: Boolean = false

    var startTurnDistance = 10

    suspend fun getAdjustSwellDistance(player: PlayerEnum): Int{
        var nowSwellDistance = swellDistance

        for(card in player1.enchantment_card.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player2.enchantment_card.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player1.usedSpecialCard.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player2.usedSpecialCard.values) nowSwellDistance += card.swellAdjust(player, this)

        return nowSwellDistance
    }

    suspend fun getAdjustDistance(player: PlayerEnum): Int{
        var distance = thisTurnDistance

        for(card in player1.enchantment_card.values){
            distance += card.effectAllMaintainCard(PlayerEnum.PLAYER1, this, TextEffectTag.CHANGE_DISTANCE)
        }
        for(card in player2.enchantment_card.values){
            distance += card.effectAllMaintainCard(PlayerEnum.PLAYER2, this, TextEffectTag.CHANGE_DISTANCE)
        }

        return distance
    }

    fun getDistance(): Int{
        return thisTurnDistance
    }

    var distanceToken = 10
    var thisTurnDistance = 10
    var dust = 0

    var player1Listener: ArrayDeque<Listener> = ArrayDeque()
    var player2Listener: ArrayDeque<Listener> = ArrayDeque()

    val player1UmbrellaListener: ArrayDeque<Listener> = ArrayDeque()
    val player2UmbrellaListener: ArrayDeque<Listener> = ArrayDeque()

    private var player1TempAttackBuff = AttackBuffQueue()
    private var player2TempAttackBuff = AttackBuffQueue()

    private var player1TempRangeBuff = RangeBuffQueue()
    private var player2TempRangeBuff = RangeBuffQueue()

    lateinit var first_turn: PlayerEnum

    fun getPlayer(player: PlayerEnum): PlayerStatus{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    fun getSocket(player: PlayerEnum): Connection{
        return if(player ==  PlayerEnum.PLAYER1) player1_socket else player2_socket
    }

    private fun getPlayerTempAttackBuff(player: PlayerEnum): AttackBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1TempAttackBuff else player2TempAttackBuff
    }

    private fun getPlayerAttackBuff(player: PlayerEnum): AttackBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1.attackBuff else player2.attackBuff
    }

    private fun getPlayerTempRangeBuff(player: PlayerEnum): RangeBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1.rangeBuff else player2.rangeBuff
    }

    private fun getPlayerRangeBuff(player: PlayerEnum): RangeBuffQueue{
        return if(player == PlayerEnum.PLAYER1) player1TempRangeBuff else player2TempRangeBuff
    }

    fun getCardNumber(player: PlayerEnum, card_name: CardName): Int{
        return if (getPlayer(player).first_turn) cardNameHashmapFirst[card_name]?: -1
        else cardNameHashmapSecond[card_name]?: -1
    }

    fun getPlayerLife(player: PlayerEnum): Int{
        return when (player){
            PlayerEnum.PLAYER1 -> player1.life
            PlayerEnum.PLAYER2 -> player2.life
        }
    }

    fun getPlayerAura(player: PlayerEnum): Int{
        return when (player){
            PlayerEnum.PLAYER1 -> player1.aura
            PlayerEnum.PLAYER2 -> player2.aura
        }
    }

    fun getPlayerFullAction(player: PlayerEnum): Boolean{
        return when (player){
            PlayerEnum.PLAYER1 -> player1.full_action
            PlayerEnum.PLAYER2 -> player2.full_action
        }
    }

    fun getPlayerFlare(player: PlayerEnum): Int{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.flare
            PlayerEnum.PLAYER2 -> player2.flare
        }
    }

    fun getEndTurn(player: PlayerEnum): Boolean{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.end_turn
            PlayerEnum.PLAYER2 -> player2.end_turn
        }
    }

    fun getPlayerHandSize(player: PlayerEnum): Int{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.hand.size
            PlayerEnum.PLAYER2 -> player2.hand.size
        }
    }

    fun getConcentration(player: PlayerEnum): Int{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.concentration
            PlayerEnum.PLAYER2 -> player2.concentration
        }
    }

    fun getFullAction(player: PlayerEnum): Boolean{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.full_action
            PlayerEnum.PLAYER2 -> player2.full_action
        }
    }
    
    suspend fun setConcentration(player: PlayerEnum, number: Int){
        when(player){
            PlayerEnum.PLAYER1 -> player1.concentration = number
            PlayerEnum.PLAYER2 -> player2.concentration = number
        }
        sendSetConcentration(getSocket(player), getSocket(player.opposite()), number)
    }

    fun setPlayerFullAction(player: PlayerEnum, full: Boolean){
        when (player){
            PlayerEnum.PLAYER1 -> player1.full_action = full
            PlayerEnum.PLAYER2 -> player2.full_action = full
        }
    }

    suspend fun setShrink(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.shrink = true
        sendSetShrink(getSocket(player), getSocket(player.opposite()))
    }

    fun setFirstTurn(player: PlayerEnum){
        first_turn = player
        when(player){
            PlayerEnum.PLAYER1 -> {
                player1.first_turn = true
                player2.concentration = 1
            }
            PlayerEnum.PLAYER2 -> {
                player2.first_turn = true
                player1.concentration = 1
            }
        }
    }

    fun setEndTurn(player: PlayerEnum, turn: Boolean){
        when(player){
            PlayerEnum.PLAYER1 -> player1.end_turn = turn
            PlayerEnum.PLAYER2 -> player2.end_turn = turn
        }
    }

    suspend fun receiveCardEffectSelect(player: PlayerEnum, card_number: Int): CommandEnum{
        return receiveCardEffectSelect(getSocket(player), card_number)
    }

    fun addImmediateLifeListener(player: PlayerEnum, listener: Listener){
        when(player){
            PlayerEnum.PLAYER1 -> player1Listener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2Listener.addLast(listener)
        }
    }

    fun addImmediateUmbrellaListener(player: PlayerEnum, listener: Listener){
        when(player){
            PlayerEnum.PLAYER1 -> player1UmbrellaListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2UmbrellaListener.addLast(listener)
        }
    }

    suspend fun moveTokenUsingInt(player: PlayerEnum, place: Int, number: Int, card: Card){
        if(place == 9) cardToDistance(player, number, card)
        else{
            TODO("부여패에서 다른곳으로 보내느 카드 추가시 이곳에서 추가해야 됨")
        }
    }

    //true means cannot move
    suspend fun moveTokenCheck(from: LocationEnum, to: LocationEnum): Boolean{
        var now: Int
        for(card in player1.enchantment_card.values){
            now = card.forbidTokenMove(PlayerEnum.PLAYER1, this)
            if(now == -1) continue
            if(now / 100 == from.real_number || now % 100 == to.real_number) return true
        }
        for(card in player2.enchantment_card.values){
            now = card.forbidTokenMove(PlayerEnum.PLAYER2, this)
            if(now == -1) continue
            if(now / 100 == from.real_number || now % 100 == to.real_number) return true
        }
        return false
    }


    suspend fun auraToDistance(player: PlayerEnum, number: Int){
        if(number == 0) return
        var value = number

        if(getPlayerAura(player) < value) value = getPlayerAura(player)
        if(distanceToken + value > 10) value = 10 - distanceToken

        getPlayer(player).aura -= value
        distanceToken += value
        thisTurnDistance += value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_AURA, LocationEnum.DISTANCE, value, -1)
    }

    suspend fun auraToFlare(player_aura: PlayerEnum, player_flare: PlayerEnum, number: Int){
        if(number == 0) return
        val auraPlayer = getPlayer(player_aura)
        val flarePlayer = getPlayer(player_flare)
        var value = number

        if(number > auraPlayer.aura){
            value = auraPlayer.aura
        }

        auraPlayer.aura -= value
        flarePlayer.flare += value

        if(player_aura == player_flare){
            sendMoveToken(getSocket(player_aura), getSocket(player_aura.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, value, -1)
        }
        else{
            sendMoveToken(getSocket(player_aura), getSocket(player_aura.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.OTHER_FLARE, value, -1)
        }
    }

    suspend fun cardToDistance(player: PlayerEnum, number: Int, card: Card){
        if(number == 0 || card.nap == 0 || card.nap == null) return
        var value = number

        if(distanceToken + value > 10) value = 10 - distanceToken

        distanceToken += value
        thisTurnDistance += value

        card.nap = card.nap!! - value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DISTANCE, value, card.card_number)
    }

    suspend fun distanceToFlare(player: PlayerEnum, number: Int){
        if(number == 0) return

        var value = number

        if(distanceToken < value){
            value = distanceToken
        }

        distanceToken -= value
        thisTurnDistance -= value

        getPlayer(player).flare += value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DISTANCE, LocationEnum.YOUR_FLARE, value, -1)
    }

    suspend fun distanceToDust(number: Int){
        if(moveTokenCheck(LocationEnum.DISTANCE, LocationEnum.DUST) || number == 0) return

        var value = number

        if(distanceToken < value){
            value = distanceToken
        }

        distanceToken -= value
        thisTurnDistance -= value

        dust += value

        sendMoveToken(player1_socket, player2_socket, TokenEnum.SAKURA_TOKEN,
            LocationEnum.DISTANCE, LocationEnum.DUST, value, -1)
    }

    suspend fun distanceToAura(player: PlayerEnum, number: Int){
        if(moveTokenCheck(LocationEnum.DISTANCE, LocationEnum.YOUR_AURA) || number == 0) return

        var value = number
        val nowPlayer = getPlayer(player)
        val emptyPlace = nowPlayer.maxAura - nowPlayer.aura - nowPlayer.freezeToken

        if(emptyPlace < value){
            value = emptyPlace
        }

        if(value > distanceToken){
            value = distanceToken
        }

        distanceToken -= value
        thisTurnDistance -= value

        if(thisTurnDistance < 0){
            thisTurnDistance = 0
        }

        nowPlayer.aura += value
        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DISTANCE, LocationEnum.YOUR_AURA, value, -1)
    }

    suspend fun cardToDustCheck(player: PlayerEnum, number: Int, card: Card): Boolean{
        val locationList = ArrayDeque<Int>()

        // TODO(other enchantment card check)

        card.card_data.effect?.let {
            for (text in it){
                if(text.tag == TextEffectTag.THIS_CARD_NAP_LOCATION_CHANGE){
                    locationList.addLast(text.effect!!(card.card_number, player, this, null)!!)
                }
                break
            }
        }

        return when (locationList.size) {
            0 -> true
            1 -> {
                moveTokenUsingInt(player, locationList[0], number, card)
                false
            }

            else -> {
                TODO("유저에게 리스트 전송, 그 중 숫자 받고 moveTokenUsingInt() call")
                //false
            }
        }
    }

    //메구미 카드의 경우 개시페이지 여부를 이곳에서 받아서 처리
    suspend fun cardToDust(player: PlayerEnum, number: Int?, card: Card){
        if(number == 0 || card.nap == 0 || card.nap == null || number == null) return

        if(cardToDustCheck(player, number, card)){
            dust += number

            card.nap = card.nap!! - number

            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DUST, number, card.card_number)
        }
    }

    //this two function is must check number before use
    suspend fun auraToCard(player: PlayerEnum, number: Int, card: Card){
        if(number == 0) return
        val nowPlayer = getPlayer(player)
        val value: Int

        if(nowPlayer.aura >= number){
            value = number
            nowPlayer.aura -= number
        }
        else{
            value = nowPlayer.aura
            nowPlayer.aura = 0
        }

        card.nap = card.nap?.let {
            it + value
        }?: value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_AURA, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, value, card.card_number)
    }

    //this three function is must check number before use
    suspend fun dustToCard(player: PlayerEnum, number: Int, card: Card){
        if(number == 0) return

        val value: Int

        if(dust >= number){
            value = number
            dust -= number
        }
        else{
            value = dust
            dust = 0
        }

        card.nap = card.nap?.let {
            it + value
        }?: value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DUST, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, value, card.card_number)
    }

    suspend fun dustToLife(player: PlayerEnum, number: Int){
        if(number == 0) return
        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.life + value > 10){
            value = 10 - nowPlayer.life
        }

        if(value > dust){
            value = dust
        }

        dust -= value
        nowPlayer.life += value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DUST, LocationEnum.YOUR_LIFE, value, -1)
    }

    suspend fun dustToDistance(number: Int){
        if(number == 0) return
        var value = number

        if(distanceToken + value > 10){
            value = 10 - distanceToken
        }

        if(value > dust){
            value = dust
        }

        distanceToken += value
        thisTurnDistance += value

        dust -= value

        sendMoveToken(player1_socket, player2_socket, TokenEnum.SAKURA_TOKEN,
            LocationEnum.DUST, LocationEnum.DISTANCE, value, -1)
    }

    suspend fun dustToAura(player: PlayerEnum, number: Int){
        if (number == 0) return
        val nowPlayer = getPlayer(player)
        var value = number

        if(number > dust){
            value = dust
        }

        if(value > nowPlayer.maxAura - nowPlayer.aura){
            value = nowPlayer.maxAura - nowPlayer.aura
        }

        nowPlayer.aura += value
        dust -= value
        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DUST, LocationEnum.YOUR_AURA, value, -1)
    }

    suspend fun dustToFlare(player: PlayerEnum, number: Int){
        if (number == 0) return
        val nowPlayer = getPlayer(player)
        var value = number
        if(number > dust){
            value = dust
        }
        nowPlayer.flare += value
        dust -= value
        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DUST, LocationEnum.YOUR_FLARE, value, -1)
    }

    suspend fun auraToDust(player: PlayerEnum, number: Int){
        if(number == 0) return

        val now_player = getPlayer(player)
        val value: Int

        if(now_player.aura >= number){
            value = number
            now_player.aura -= number
        }
        else{
            value = now_player.aura
            now_player.aura = 0
        }

        dust += value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_AURA, LocationEnum.DUST, value, -1)
    }

    suspend fun flareToDistance(player: PlayerEnum, number: Int){
        if(number == 0) return

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }
        if(10 - distanceToken < number){
            value = 10 - distanceToken
        }
        nowPlayer.flare -= value

        distanceToken += value
        thisTurnDistance += value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_FLARE, LocationEnum.DISTANCE, value, -1)
    }

    suspend fun flareToDust(player: PlayerEnum, number: Int){
        if(number == 0) return

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }
        nowPlayer.flare -= value

        dust += value

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_FLARE, LocationEnum.DUST, value, -1)
    }

    suspend fun flareToAura(flare_player: PlayerEnum, aura_player: PlayerEnum, number: Int){
        if(number == 0) return

        val flarePlayer = getPlayer(flare_player)
        val auraPlayer = getPlayer(aura_player)
        var value = number

        if(flarePlayer.flare < number){
            value = flarePlayer.flare
        }

        if(auraPlayer.maxAura - auraPlayer.aura > value){
            value = auraPlayer.maxAura - auraPlayer.aura
        }

        flarePlayer.flare -= value
        auraPlayer.aura += value

        if(flare_player == aura_player){
            sendMoveToken(getSocket(flare_player), getSocket(flare_player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_FLARE, LocationEnum.YOUR_AURA, value, -1)
        }
        else{
            sendMoveToken(getSocket(flare_player), getSocket(flare_player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_FLARE, LocationEnum.OTHER_AURA, value, -1)
        }

    }

    suspend fun chasmProcess(player: PlayerEnum){
        val now_player = getPlayer(player)

        for(enchantment_card in now_player.enchantment_card){
            if(enchantment_card.value.chasmCheck()){
                enchantmentDestructionNotNormally(player, enchantment_card.value)
            }
        }

    }

    suspend fun lifeListenerProcess(player: PlayerEnum, before: Int, reconstruct: Boolean, damage: Boolean){
        val nowPlayer = getPlayer(player)
        when(player){
            PlayerEnum.PLAYER1 -> {
                if(!player1Listener.isEmpty()){
                    for(i in 0..player1Listener.size){
                        if(player1Listener.isEmpty()) break
                        val now = player1Listener.first()
                        player1Listener.removeFirst()
                        if(!(now.doAction(this, before, nowPlayer.life, reconstruct, damage))){
                            player1Listener.addLast(now)
                        }
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(!player2Listener.isEmpty()){
                    for(i in 0..player2Listener.size){
                        if(player2Listener.isEmpty()) break
                        val now = player2Listener.first()
                        player2Listener.removeFirst()
                        if(!(now.doAction(this, before, nowPlayer.life, reconstruct, damage))){
                            player2Listener.addLast(now)
                        }
                    }
                }
            }
        }
    }

    suspend fun lifeToDust(player: PlayerEnum, number: Int): Boolean{
        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life > number){
            nowPlayer.life -= number
            dust += number
        }
        else{
            return true
        }

        lifeListenerProcess(player, before, reconstruct = false, damage = false)

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_LIFE, LocationEnum.DUST, number, -1)
        return false
    }

    //return endgame
    suspend fun lifeToSelfFlare(player: PlayerEnum, number: Int, reconstruct: Boolean, damage: Boolean){
        if(number == 0) {
            gameEnd(player.opposite())
        }

        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life > number){
            nowPlayer.life -= number
            nowPlayer.flare += number
        }
        else{
            gameEnd(player.opposite())
        }

        lifeListenerProcess(player, before, reconstruct, damage)

        sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_LIFE, LocationEnum.YOUR_FLARE, number, -1)
    }

    suspend fun addAllCardTextBuff(player: PlayerEnum){
        val mine = getPlayer(player)
        val other = getPlayer(player)
        for(card in mine.enchantment_card){
            card.value.effectAllMaintainCard(player, this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT)
        }
        for(card in mine.usedSpecialCard){
            card.value.effectAllMaintainCard(player, this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT)
        }
        for(card in other.enchantment_card){
            card.value.effectAllMaintainCard(player.opposite(), this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER)
        }
        for(card in other.usedSpecialCard){
            card.value.effectAllMaintainCard(player.opposite(), this, TextEffectTag.NEXT_ATTACK_ENCHANTMENT_OTHER)
        }
    }

    suspend fun addAllCardCostBuff(){
        for(card in player1.enchantment_card){
            card.value.addCostBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player1.usedSpecialCard){
            card.value.addCostBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player2.enchantment_card){
            card.value.addCostBuff(PlayerEnum.PLAYER2, this)
        }
        for(card in player2.usedSpecialCard){
            card.value.addCostBuff(PlayerEnum.PLAYER2, this)
        }
    }

    suspend fun applyAllCostBuff(player: PlayerEnum, cost: Int, card: Card): Int{
        val now_player = getPlayer(player)
        var now_cost = cost

        for(queue in now_player.cost_buf){
            val tempq: ArrayDeque<CostBuff> = ArrayDeque()
            for(buff in queue){
                if(buff.condition(player, this, card)){
                    buff.counter *= -1
                    tempq.add(buff)
                }
            }
            for(buff in tempq){
                now_cost = buff.effect(now_cost)
            }
        }

        return now_cost
    }

    private fun applyTempRangeBuff(player: PlayerEnum, madeAttack: MadeAttack, nowTempRangeBuffQueue: RangeBuffQueue){
        val nowPlayer = getPlayer(player)

        for(i in 0..4){
            val tempQueue: ArrayDeque<RangeBuff> = ArrayDeque()
            nowTempRangeBuffQueue.applyBuff(i, player, this, madeAttack, tempQueue)
            for(buff in tempQueue){
                buff.effect(nowPlayer.pre_attack_card!!)
            }
        }
    }

    suspend private fun applyTempAttackBuff(player: PlayerEnum, madeAttack: MadeAttack, nowTempBuffQueue: AttackBuffQueue){
        val nowPlayer = getPlayer(player)

        for(i in 0..4){
            val tempQueue: ArrayDeque<Buff> = ArrayDeque()
            nowTempBuffQueue.applyBuff(i, player, this, madeAttack, tempQueue)
            for(buff in tempQueue){
                buff.effect(nowPlayer.pre_attack_card!!)
            }
        }

        if(madeAttack.aura_damage != 999 && madeAttack.aura_damage > 5){
            if(!madeAttack.chogek){
                madeAttack.aura_damage = 5
            }
        }
    }

    suspend private fun applyAllAttackBuff(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        val nowTempBuffQueue = getPlayerTempAttackBuff(player)
        val nowBuffQueue = getPlayerAttackBuff(player)

        for(i in 0..4){
            val tempQueue: ArrayDeque<Buff> = ArrayDeque()
            nowTempBuffQueue.applyBuff(i, player, this, nowPlayer.pre_attack_card!!, tempQueue)
            nowBuffQueue.applyBuff(i, player, this, nowPlayer.pre_attack_card!!, tempQueue)
            for(buff in tempQueue){
                buff.effect(nowPlayer.pre_attack_card!!)
            }
        }

        if(nowPlayer.pre_attack_card!!.aura_damage != 999 && nowPlayer.pre_attack_card!!.aura_damage > 5){
            if(!nowPlayer.pre_attack_card!!.chogek){
                nowPlayer.pre_attack_card!!.aura_damage = 5
            }
        }
    }

    suspend fun attackCheck(player: PlayerEnum): Boolean{
        addAllCardTextBuff(player)

        val nowPlayer = getPlayer(player)
        val nowTempBuffQueue = getPlayerTempRangeBuff(player)
        val nowBuffQueue = getPlayerRangeBuff(player)

        for(i in 0..4){
            val tempQueue: ArrayDeque<RangeBuff> = ArrayDeque()
            nowTempBuffQueue.applyBuff(i, player, this, nowPlayer.pre_attack_card!!, tempQueue)
            nowBuffQueue.applyBuff(i, player, this, nowPlayer.pre_attack_card!!, tempQueue)
            for(buff in tempQueue){
                buff.effect(nowPlayer.pre_attack_card!!)
            }
        }

        return nowPlayer.pre_attack_card!!.rangeCheck(getAdjustDistance(player))
    }

    fun cleanAfterUseCost(){
        cleanCostBuff(player1.cost_buf)
        cleanCostBuff(player2.cost_buf)
    }

    fun cleanCostBuff(){
        cleanCostTempBuff(player1.cost_buf)
        cleanCostTempBuff(player2.cost_buf)
    }

    fun cleanAllBuffWhenUnused(player: PlayerEnum){
        when(player){
            PlayerEnum.PLAYER1 -> {
                player1.rangeBuff.resetCounter()
                player1TempRangeBuff.clearBuff()
                player1TempAttackBuff.clearBuff()
            }
            PlayerEnum.PLAYER2 -> {
                player2.rangeBuff.resetCounter()
                player2TempRangeBuff.clearBuff()
                player2TempAttackBuff.clearBuff()
            }
        }
    }

    suspend fun addPreAttackZone(player: PlayerEnum, attack: MadeAttack): Boolean{
        val nowPlayer = getPlayer(player)

        nowPlayer.addPreAttackZone(attack)

        return if(attackCheck(player)){
            getPlayerTempRangeBuff(player.opposite()).clearBuff()
            nowPlayer.rangeBuff.cleanUsedBuff()
            applyAllAttackBuff(player)
            true
        } else{
            nowPlayer.pre_attack_card = null
            cleanAllBuffWhenUnused(player)
            false
        }
    }

    fun addThisTurnCostBuff(player: PlayerEnum, effect: CostBuff){
        when (player){
            PlayerEnum.PLAYER1 -> player1.addCostBuff(effect)
            PlayerEnum.PLAYER2 -> player2.addCostBuff(effect)
        }
    }

    fun addThisTurnAttackBuff(player: PlayerEnum, effect: Buff){
        val nowPlayer = getPlayer(player)

        when(effect.tag){
            BufTag.CARD_CHANGE, BufTag.INSERT, BufTag.CHANGE_EACH, BufTag.MULTIPLE, BufTag.DIVIDE, BufTag.PLUS_MINUS -> {
                nowPlayer.attackBuff.addAttackBuff(effect)
            }
            else -> getPlayerTempAttackBuff(player).addAttackBuff(effect)
        }
    }

    fun addThisTurnRangeBuff(player: PlayerEnum, effect: RangeBuff){
        val nowPlayer = getPlayer(player)
        val nowTempRangeBuff = getPlayerTempRangeBuff(player)

        when(effect.tag){
            RangeBufTag.CARD_CHANGE, RangeBufTag.CHANGE, RangeBufTag.ADD, RangeBufTag.DELETE, RangeBufTag.PLUS, RangeBufTag.MINUS-> {
                nowPlayer.rangeBuff.addRangeBuff(effect)
            }
            else -> nowTempRangeBuff.addRangeBuff(effect)
        }
    }

    suspend fun addConcentration(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        when(now_player.addConcentration()){
            0 -> {
                sendAddConcentration(now_socket, other_socket)
            }
            1 -> {
                sendRemoveShrink(now_socket, other_socket)
            }
        }
    }

    suspend fun decreaseConcentration(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        if(now_player.decreaseConcentration()) sendDecreaseConcentration(now_socket, other_socket)
    }

    suspend fun reactCheck(player: PlayerEnum, card: Card, react_attack: MadeAttack): Boolean{
        if(card.canUseAtReact(player, this)){
            if(card.canReactable(react_attack)){
                return true
            }
        }

        return false
    }

    suspend fun useCardFrom(player: PlayerEnum, card: Card, location: LocationEnum, react: Boolean, react_attack: MadeAttack?,
                            reactAttackBuffQueue: AttackBuffQueue?, reactRangeBuffQueue: RangeBuffQueue?, isCost: Boolean): Boolean{
        val cost = card.canUse(player, this, react_attack, isCost)
        if(cost != -2){
            if(location == LocationEnum.COVER_CARD && react) logger.insert(Log(player, LogText.USE_CARD_IN_COVER_AND_REACT, card.card_number, card.card_number))
            else if(location == LocationEnum.COVER_CARD) logger.insert(Log(player, LogText.USE_CARD_IN_COVER, card.card_number, card.card_number))
            else if(react) logger.insert(Log(player, LogText.USE_CARD_REACT, card.card_number, card.card_number))
            else logger.insert(Log(player, LogText.USE_CARD, card.card_number, card.card_number))
            if(cost == -1){
                popCardFrom(player, card.card_number, location, true)
                insertCardTo(player, card, LocationEnum.PLAYING_ZONE, true)
                sendUseCardMeesage(getSocket(player), getSocket(player.opposite()), react, card.card_number)
                card.use(player, this, react_attack, reactAttackBuffQueue, reactRangeBuffQueue)
                return true
            }
            if(cost >= 0){
                card.special_card_state = SpecialCardEnum.PLAYING
                flareToDust(player, cost)
                cleanAfterUseCost()
                popCardFrom(player, card.card_number, LocationEnum.SPECIAL_CARD, true)
                insertCardTo(player, card, LocationEnum.PLAYING_ZONE, true)
                sendUseCardMeesage(getSocket(player), getSocket(player.opposite()), react, card.card_number)
                card.use(player, this, react_attack, reactAttackBuffQueue, reactRangeBuffQueue)
                return true
            }
        }
        return false
    }

    suspend fun afterMakeAttack(card_number: Int, player: PlayerEnum, react_attack: MadeAttack?,
                                reactAttackBuffQueue: AttackBuffQueue?, reactRangeBuffQueue: RangeBuffQueue?){
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())
        val nowPlayer = getPlayer(player)
        val otherPlayer = getPlayer(player.opposite())

        if(nowPlayer.pre_attack_card == null){
            return
        }

        val nowTempAttackBuff = getPlayerTempAttackBuff(player)
        val nowTempRangeBuff = getPlayerTempRangeBuff(player)

        when(player){
            PlayerEnum.PLAYER1 -> {
                player1TempRangeBuff = RangeBuffQueue()
                player1TempAttackBuff = AttackBuffQueue()
            }
            PlayerEnum.PLAYER2 -> {
                player2TempRangeBuff = RangeBuffQueue()
                player2TempAttackBuff = AttackBuffQueue()
            }
        }

        val nowAttack = nowPlayer.pre_attack_card!!
        nowPlayer.pre_attack_card = null

        logger.insert(Log(player, LogText.ATTACK, nowAttack.card_number, nowAttack.card_number))

        makeAttackComplete(nowSocket, otherSocket, card_number)
        sendAttackInformation(nowSocket, otherSocket, nowAttack.Information())
        if(!otherPlayer.end_turn && react_attack == null){
            while(true){
                sendRequestReact(otherSocket)
                val react = receiveReact(otherSocket)
                if(react.first == CommandEnum.REACT_USE_CARD_HAND){
                    val card = otherPlayer.getCardFromHand(react.second)?: continue
                    if(reactCheck(player.opposite(), card, nowAttack)){
                        if(useCardFrom(player.opposite(), card, LocationEnum.HAND, true, nowAttack, nowTempAttackBuff, nowTempRangeBuff, true)) break
                    }

                }
                else if(react.first == CommandEnum.REACT_USE_CARD_SPECIAL){
                    val card = otherPlayer.getCardFromSpecial(react.second)?: continue
                    if(reactCheck(player.opposite(), card, nowAttack)){
                        if(useCardFrom(player.opposite(), card, LocationEnum.SPECIAL_CARD, true, nowAttack, nowTempAttackBuff, nowTempRangeBuff, true)) break
                    }
                }
                else{
                    break
                }
            }
        }

        if(endCurrentPhase){
            endCurrentPhase = false
            return
        }

        applyTempAttackBuff(player, nowAttack, nowTempAttackBuff)
        applyTempRangeBuff(player, nowAttack, nowTempRangeBuff)
        if(nowAttack.isItValid && nowAttack.rangeCheck(getAdjustDistance(player))){
            if(nowAttack.beforeProcessDamageCheck(player, this, react_attack)){
                if(nowAttack.isItDamage){
                    sendChooseDamage(otherSocket, CommandEnum.CHOOSE_CARD_DAMAGE, nowAttack.aura_damage, nowAttack.life_damage)
                    val chosen = receiveChooseDamage(otherSocket)
                    if(nowAttack.bothSideDamage){
                        processDamage(player.opposite(), CommandEnum.CHOOSE_AURA, Pair(nowAttack.aura_damage, 999), false)
                        processDamage(player.opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, nowAttack.life_damage), false)
                    }
                    else{
                        processDamage(player.opposite(), chosen, Pair(nowAttack.aura_damage, nowAttack.life_damage), false)
                    }
                }
            }
            react_attack?.let {
                applyTempAttackBuff(player.opposite(), it, reactAttackBuffQueue!!)
                applyTempRangeBuff(player.opposite(), it, reactRangeBuffQueue!!)
            }
            nowAttack.afterAttackProcess(player, this, react_attack)
        }
    }

    suspend fun movePlayingCard(player: PlayerEnum, place: LocationEnum?, card_number: Int){
        val card = popCardFrom(player, card_number, LocationEnum.PLAYING_ZONE, true)?: return

        if(place != null){
            insertCardTo(card.player, card, place, true)
        }
        else if(card.card_data.card_type == CardType.ENCHANTMENT){
            insertCardTo(player, card, LocationEnum.ENCHANTMENT_ZONE, true)
            if(card.isItDestruction()){
                enchantmentDestruction(player, card)
            }
        }
        else{
            when(card.card_data.card_class){
                CardClass.SPECIAL -> {
                    card.addReturnListener(card.player, this)
                    card.special_card_state = SpecialCardEnum.PLAYED
                    insertCardTo(card.player, card, LocationEnum.USED_CARD, true)
                }
                CardClass.NORMAL -> {
                    insertCardTo(card.player, card, LocationEnum.DISCARD, true)
                }
            }
        }
    }

    suspend fun afterCardUsed(card_number: Int, player: PlayerEnum){
        movePlayingCard(player, null, card_number)
    }

    suspend fun afterDestruction(player: PlayerEnum, card_number: Int, location: LocationEnum){
        val card = popCardFrom(player, card_number, LocationEnum.ENCHANTMENT_ZONE, true)?: return
        when(card.card_data.card_class){
            CardClass.SPECIAL -> {
                card.addReturnListener(card.player, this)
                card.special_card_state = SpecialCardEnum.PLAYED
                insertCardTo(card.player, card, LocationEnum.USED_CARD, true)
            }
            CardClass.NORMAL -> {
                insertCardTo(card.player, card, location, true)
            }
        }

        card.nap = card.nap?.let {
            cardToDust(card.player, it, card)
            null
        }
    }

    suspend fun enchantmentDestruction(player: PlayerEnum, card: Card){
        sendDestructionEnchant(getSocket(player), getSocket(player.opposite()), card.card_number)
        card.destructionEnchantmentNormaly(player, this)
        for(enchantmentCard in getPlayer(player).enchantment_card.values){
            enchantmentCard.effectAllMaintainCard(player, this, TextEffectTag.EFFECT_WHEN_DESTRUCTION_OTHER_CARD)
        }
        afterDestruction(player, card.card_number, LocationEnum.DISCARD)
    }

    suspend fun enchantmentDestructionNotNormally(player: PlayerEnum, card: Card){
        sendDestructionEnchant(getSocket(player), getSocket(player.opposite()), card.card_number)
        afterDestruction(player, card.card_number, LocationEnum.COVER_CARD)
    }

    suspend fun enchantmentReduceAll(player: PlayerEnum){
        if(player1.enchantment_card.isEmpty() && player2.enchantment_card.isEmpty()){
            return
        }

        sendReduceNapStart(player1_socket)
        sendReduceNapStart(player2_socket)

        val player1_card: HashMap<Int, Boolean> = HashMap()
        val player2_card: HashMap<Int, Boolean> = HashMap()


        for(i in player1.enchantment_card){
            val nap = i.value.reduceNapNormal()
            if(nap >= 1){
                cardToDust(PlayerEnum.PLAYER1, nap, i.value)
            }
            if(i.value.isItDestruction()){
                player1_card[i.value.card_number] = true
            }
        }

        for(i in player2.enchantment_card){
            val nap = i.value.reduceNapNormal()
            if(nap >= 1){
                cardToDust(PlayerEnum.PLAYER2, nap, i.value)
            }
            if(i.value.isItDestruction()){
                player2_card[i.value.card_number] = true
            }
        }

        sendReduceNapEnd(player1_socket)
        sendReduceNapEnd(player2_socket)

        if(player1_card.isEmpty() && player2_card.isEmpty()){
            return
        }

        when(player){
            PlayerEnum.PLAYER1 -> {
                sendStartSelectEnchantment(player1_socket)
                while(true){
                    sendRequestEnchantmentCard(player1_socket, player1_card.keys.toMutableList(), player2_card.keys.toMutableList())
                    val receive = receiveEnchantment(player1_socket)
                    when(receive.first){
                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
                            if(player1_card[receive.second] == true){
                                val card = player1.enchantment_card[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
                                player1_card.remove(receive.second)
                            }
                            if(player1_card.isEmpty() && player2_card.isEmpty()){
                                break
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
                            if(player2_card[receive.second] == true){
                                val card = player2.enchantment_card[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
                                player2_card.remove(receive.second)
                            }
                            if(player1_card.isEmpty() && player2_card.isEmpty()){
                                break
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_END -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                sendStartSelectEnchantment(player2_socket)
                while(true){
                    sendRequestEnchantmentCard(player2_socket, player2_card.keys.toMutableList(), player1_card.keys.toMutableList())
                    val receive = receiveEnchantment(player2_socket)
                    when(receive.first){
                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
                            if(player2_card[receive.second] == true){
                                val card = player2.enchantment_card[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
                                player2_card.remove(receive.second)
                                if(player1_card.isEmpty() && player2_card.isEmpty()){
                                    break
                                }
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
                            if(player1_card[receive.second] == true){
                                val card = player1.enchantment_card[receive.second]
                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
                                player1_card.remove(receive.second)
                                if(player1_card.isEmpty() && player2_card.isEmpty()){
                                    break
                                }
                            }
                        }
                        CommandEnum.SELECT_ENCHANTMENT_END -> {
                            break
                        }
                        else -> {}
                    }
                }
            }
        }

        if(player1_card.isNotEmpty()){
            for(card_number in player1_card.keys){
                val card = player1.enchantment_card[card_number]
                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
            }
        }

        if(player2_card.isNotEmpty()){
            for(card_number in player2_card.keys){
                val card = player2.enchantment_card[card_number]
                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
            }
        }
    }

    suspend fun gameEnd(winner: PlayerEnum){
        val winner_socket = getSocket(winner)
        val roser_socket = getSocket(winner.opposite())

        sendGameEnd(winner_socket, roser_socket)

        player1_socket.session.close()
        player2_socket.session.close()
    }

    suspend fun auraDamageProcess(player: PlayerEnum, data: MutableList<Int>){
        val nowPlayer = getPlayer(player)
        for (index in data.indices){
            if(index % 2 == 0){
                if(data[index] == LocationEnum.YOUR_AURA.real_number){
                    auraToDust(player, data[index + 1])
                }
                else{
                    cardToDust(player, data[index + 1], nowPlayer.enchantment_card[data[index]]!!)
                    if(nowPlayer.enchantment_card[data[index]]!!.nap!! == 0) enchantmentDestruction(player, nowPlayer.enchantment_card[data[index]]!!)
                }
            }
        }
    }

    //damage first = AURA, damage second = LIFE
    suspend fun processDamage(player: PlayerEnum, command: CommandEnum, damage: Pair<Int, Int>, reconstruct: Boolean){
        val nowPlayer = getPlayer(player)
        if(command == CommandEnum.CHOOSE_AURA){
            if(damage.first == 999){
                processDamage(player, CommandEnum.CHOOSE_LIFE, damage, reconstruct)
            }
            else{
                if(damage.first == 0){
                    logger.insert(Log(player, LogText.GET_AURA_DAMAGE, damage.second, damage.second))
                    return
                }
                val selectable = nowPlayer.checkAuraDamage(damage.first)
                if(damage.second == 999){
                    logger.insert(Log(player, LogText.GET_AURA_DAMAGE, damage.second, damage.second))
                    if(selectable == null || (selectable.size == 1 && selectable[0] == LocationEnum.YOUR_AURA.real_number)){
                        auraToDust(player, damage.first)
                    }
                    else{
                        while(true){
                            val receive = receiveAuraDamageSelect(getSocket(player), selectable, damage.first)
                            if (nowPlayer.auraDamagePossible(receive, damage.first, selectable)){
                                auraDamageProcess(player, receive!!)
                                break
                            }
                        }
                    }
                }
                else{
                    if(selectable == null){
                        processDamage(player, CommandEnum.CHOOSE_LIFE, damage, reconstruct)
                    }
                    else{
                        logger.insert(Log(player, LogText.GET_AURA_DAMAGE, damage.second, damage.second))
                        if(selectable.size == 1 && selectable[0] == LocationEnum.YOUR_AURA.real_number){
                            auraToDust(player, damage.first)
                        }
                        else{
                            while(true){
                                val receive = receiveAuraDamageSelect(getSocket(player), selectable, damage.first)
                                if (nowPlayer.auraDamagePossible(receive, damage.first, selectable)){
                                    auraDamageProcess(player, receive!!)
                                    break
                                }
                            }
                        }
                    }
                }

            }
        }
        else{
            if(damage.second == 999){
                processDamage(player, CommandEnum.CHOOSE_AURA, damage, reconstruct)
            }
            else{
                logger.insert(Log(player, LogText.GET_LIFE_DAMAGE, damage.second, damage.second))
                lifeToSelfFlare(player, damage.second, reconstruct, true)
                if(!reconstruct) chasmProcess(player)
            }
        }
    }

    suspend fun drawCard(player: PlayerEnum, number: Int){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        for(i in 1..number){
            if(now_player.normalCardDeck.size == 0){
                sendChooseDamage(now_socket, CommandEnum.CHOOSE_CHOJO, 1, 1)
                val chosen = receiveChooseDamage(now_socket)
                processDamage(player, chosen, Pair(1, 1), false)
                continue
            }
            val draw_card = now_player.normalCardDeck.first()
            sendDrawCard(now_socket, other_socket, draw_card.card_number)
            now_player.hand[draw_card.card_number] = draw_card
            now_player.normalCardDeck.removeFirst()
        }
    }

    //first means upperside of deck, last means belowside of deck
    suspend fun insertHandToDeck(public: Boolean, Below: Boolean, player: PlayerEnum, card_number: Int): Boolean{
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        return now_player.hand[card_number]?.let {
            if(Below) now_player.normalCardDeck.addLast(it)
            else now_player.normalCardDeck.addFirst(it)
            now_player.hand.remove(card_number)
            sendHandToDeck(now_socket, other_socket, card_number, public, Below)
            true
        }?: false
    }

    fun startPhaseEffectProcess(){
        //TODO("BY RULE 8-1-2")
    }

    fun mainPhaseEffectProcess(){
        //TODO("BY RULE 8-2-2")
    }

    suspend fun endPhaseEffectProcess(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        val returnList = nowPlayer.usedCardReturn(this)
        if(returnList.isNotEmpty()){
           for(cardNumber in returnList) {
               returnSpecialCard(player, cardNumber)
           }
        }
        thisTurnDistance = distanceToken
        nowPlayer.megamiCard?.endPhaseEffect(player, this)
        nowPlayer.megamiCard2?.endPhaseEffect(player, this)
        player1.didBasicOperation = false; player2.didBasicOperation = false
        player1.canNotGoForward = false; player2.canNotGoForward = false
        player1.rangeBuff.clearBuff(); player2.rangeBuff.clearBuff(); player1.attackBuff.clearBuff(); player2.attackBuff.clearBuff()
    }

    //0 = player don't want using card more || 1 = player card use success || 2 = cannot use because there are no installation card
    suspend fun useInstallationOnce(player: PlayerEnum): Int{
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val list = nowPlayer.getInstallationCard()
        if(list.isEmpty()) return 2

        while(true){
            val receive = receiveSelectCard(nowSocket, list, CommandEnum.SELECT_CARD_REASON_INSTALLATION, -1)?: continue
            if (receive.size == 1){
                val card = nowPlayer.getCardFromCover(receive[0])?: continue
                if (useCardFrom(player, card, LocationEnum.COVER_CARD, false, null, null, null, true)) {
                    break
                }
            }
            else if(receive.isEmpty()) return 0
        }
        return 1
    }

    suspend fun installationProcess(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        if(nowPlayer.getInstallationCard().isEmpty()) return
        if(nowPlayer.infiniteInstallationCheck()){
            while(true){
                when(useInstallationOnce(player)){
                    0 -> break
                    2 -> break
                    else -> {}
                }
            }
        }
        else{
            useInstallationOnce(player)
        }
    }

    suspend fun deckReconstruct(player: PlayerEnum, damage: Boolean){
        val nowPlayer = getPlayer(player)

        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())

        installationProcess(player)

        sendDeckReconstruct(nowSocket, otherSocket) // 독 카드 몇장인지 보내줘야 될 듯?
        if(damage){
            processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), true)
        }
        Card.cardReconstructInsert(nowPlayer.discard, nowPlayer.cover_card, nowPlayer.normalCardDeck)
    }

    suspend fun cardUseNormal(player: PlayerEnum, commandEnum: CommandEnum, card_number: Int): Boolean{
        if(card_number == -1){
            return false
        }
        if(commandEnum == CommandEnum.ACTION_USE_CARD_HAND){
            val card = getCardFrom(player, card_number, LocationEnum.HAND)?: return false
            if(useCardFrom(player, card, LocationEnum.HAND, false, null, null, null, true)) return true
        }
        else if(commandEnum == CommandEnum.ACTION_USE_CARD_SPECIAL){
            val card = getCardFrom(player, card_number, LocationEnum.SPECIAL_CARD)?: return false
            if(useCardFrom(player, card, LocationEnum.SPECIAL_CARD, false, null, null, null, true)) return true
        }
        else{
            return false
        }
        return false
    }

    suspend fun basicOperationCost(player: PlayerEnum, card_number: Int): Boolean{
        val nowPlayer = getPlayer(player)

        if(card_number == -1){
            return if(nowPlayer.concentration == 0) false
            else {
                decreaseConcentration(player)
                true
            }
        } else{
            val card = nowPlayer.getCardFromHand(card_number)?: return false
            if(!card.card_data.canCover) return false
            popCardFrom(player, card_number, LocationEnum.HAND, false)
            insertCardTo(player, card, LocationEnum.COVER_CARD, false)
            return true
        }
    }

    suspend fun basicOperationEnchantmentCheck(player: PlayerEnum, command: CommandEnum): Boolean{
        for(card in getPlayer(player.opposite()).enchantment_card.values){
            if(card.operationForbidCheck(false, command, this)) return false
        }
        return true
    }

    suspend fun canDoBasicOperation(player: PlayerEnum, command: CommandEnum): Boolean{
        val now_player = getPlayer(player)
        return when(command){
            CommandEnum.ACTION_GO_FORWARD ->
                !(now_player.aura + now_player.freezeToken == now_player.maxAura || distanceToken == 0 || thisTurnDistance <= getAdjustSwellDistance(player))
                        && !(getPlayer(player).canNotGoForward)
            CommandEnum.ACTION_GO_BACKWARD -> {
                !(now_player.aura == 0 || distanceToken == 10) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_GO_BACKWARD)
            }
            CommandEnum.ACTION_WIND_AROUND -> !(dust == 0 || now_player.aura == now_player.maxAura)
            CommandEnum.ACTION_INCUBATE -> now_player.aura != 0
            CommandEnum.ACTION_BREAK_AWAY -> {
                !(dust == 0 || thisTurnDistance > getAdjustSwellDistance(player) || distanceToken == 10) && basicOperationEnchantmentCheck(player, CommandEnum.ACTION_GO_BACKWARD)
            }
            else -> false
        }
    }

    suspend fun doBasicOperation(player: PlayerEnum, command: CommandEnum, card: Int){
        getPlayer(player).didBasicOperation = true
        when(command){
            CommandEnum.ACTION_GO_FORWARD -> doGoForward(player, card)
            CommandEnum.ACTION_GO_BACKWARD -> doGoBackward(player, card)
            CommandEnum.ACTION_WIND_AROUND -> doWindAround(player, card)
            CommandEnum.ACTION_INCUBATE -> doIncubate(player, card)
            CommandEnum.ACTION_BREAK_AWAY -> doBreakAway(player, card)
            else -> {}
        }
    }

    //this 5 function must call after check when select
    suspend fun doGoForward(player: PlayerEnum, card: Int){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        if(now_player.aura == now_player.maxAura || distanceToken == 0 || thisTurnDistance <= getAdjustSwellDistance(player)) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_GO_FORWARD_YOUR, card)
            distanceToken -= 1
            thisTurnDistance -= 1
            if(thisTurnDistance < 0){
                thisTurnDistance = 0
            }
            getPlayer(player).aura += 1
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.YOUR_AURA, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doGoBackward(player: PlayerEnum, card: Int){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        if(now_player.aura == 0 || distanceToken == 10) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_GO_BACKWARD_YOUR, card)
            now_player.aura -= 1
            distanceToken += 1
            thisTurnDistance += 1
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.DISTANCE, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doWindAround(player: PlayerEnum, card: Int){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        if(dust == 0 || now_player.aura == now_player.maxAura) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_WIND_AROUND_YOUR, card)
            dust -= 1
            now_player.aura += 1
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.YOUR_AURA, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doIncubate(player: PlayerEnum, card: Int){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        if(now_player.aura == 0) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_INCUBATE_YOUR, card)
            now_player.aura -= 1
            now_player.flare += 1
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doBreakAway(player: PlayerEnum, card: Int){
        val now_socket = getSocket(player)
        val other_socket = getSocket(player.opposite())

        if(dust == 0 || thisTurnDistance > getAdjustSwellDistance(player) || distanceToken == 10) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_BREAK_AWAY_YOUR, card)
            dust -= 1
            distanceToken += 1
            thisTurnDistance += 1
            sendMoveToken(getSocket(player), getSocket(player.opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, 1, -1)
        }
    }

    suspend fun coverCard(player: PlayerEnum, select_player: PlayerEnum, reason: Int){
        val nowSocket = getSocket(select_player)

        if(checkCoverAbleHand(player)){
            val list = mutableListOf<Int>()
            for (card in getPlayer(player).hand.values) list.add(card.card_number)
            val cardNumber = receiveCoverCardSelect(nowSocket, list, reason)
            if(cardNumberHashmap[cardNumber] == null || !returnCardDataByName(cardNumberHashmap[cardNumber]!!).canCover){
                coverCard(player, select_player, reason)
                return
            }
            val card = popCardFrom(player, cardNumber, LocationEnum.HAND, false)
            if(card == null) {
                coverCard(player, select_player, reason)
                return
            }
            else{
                insertCardTo(player, card, LocationEnum.COVER_CARD, false)
                return
            }
        }
        else{
            return
        }
    }

    fun checkCoverAbleHand(player: PlayerEnum): Boolean{
        val nowPlayer = getPlayer(player)

        for(card in nowPlayer.hand.values){
            if(card.card_data.canCover) return true
        }
        return false
    }

    suspend fun endTurnHandCheck(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        while (true){
            if(nowPlayer.hand.size <= nowPlayer.max_hand) return
            coverCard(player, player, -1)
        }
    }

    //select_player -> cardUser ||| player -> victim ||| function that select card in list(list check is not needed)
    suspend fun selectCardFrom(player: PlayerEnum, select_player: PlayerEnum, location_list: List<LocationEnum>,
                               reason: CommandEnum, card_number: Int, condition: (Card) -> Boolean): MutableList<Int>?{
        val cardList = mutableListOf<Int>()
        val searchPlayer = getPlayer(player)

        for (location in location_list){
            searchPlayer.insertCardNumber(location, cardList, condition)
        }

        if(cardList.isEmpty()) return null

        while (true){
            return receiveSelectCard(getSocket(select_player), cardList, reason, card_number) ?: continue
        }
    }

    //use this function player cannot select card select number
    suspend fun selectCardFrom(player: PlayerEnum, select_player: PlayerEnum, location_list: List<LocationEnum>,
                               reason: CommandEnum, card_number: Int, listSize: Int, condition: (Card) -> Boolean): MutableList<Int>?{
        val cardList = mutableListOf<Int>()
        val searchPlayer = getPlayer(player)

        for (location in location_list){
            searchPlayer.insertCardNumber(location, cardList, condition)
        }

        if(cardList.isEmpty()) return null
        else if(cardList.size < listSize) return cardList

        while (true){
            val set = mutableSetOf<Int>()
            val list = receiveSelectCard(getSocket(select_player), cardList, reason, card_number) ?: continue
            set.addAll(list)
            if(set.size == listSize) return list
        }
    }

    suspend fun selectCardFrom(player: PlayerEnum, cardList: MutableList<Int>, reason: CommandEnum, card_number: Int,
        listSize: Int): MutableList<Int>{
        while (true){
            val set = mutableSetOf<Int>()
            val list = receiveSelectCard(getSocket(player), cardList, reason, card_number) ?: continue
            set.addAll(list)
            if(set.size == listSize) return list
        }
    }

    suspend fun popCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum, public: Boolean): Card?{
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())
        when(location){
            LocationEnum.COVER_CARD -> for(card in nowPlayer.cover_card) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_COVER_YOUR)
                nowPlayer.cover_card.remove(card)
                return card
            }
            LocationEnum.DISCARD -> for(card in nowPlayer.discard) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_DISCARD_YOUR)
                nowPlayer.discard.remove(card)
                return card
            }
            LocationEnum.DECK -> for(card in nowPlayer.normalCardDeck) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_DECK_YOUR)
                nowPlayer.normalCardDeck.remove(card)
                return card
            }
            LocationEnum.HAND -> {
                if(card_number in nowPlayer.hand){
                    sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_HAND_YOUR)
                    val result = nowPlayer.hand[card_number]
                    nowPlayer.hand.remove(card_number)
                    return result
                }
            }
            LocationEnum.SPECIAL_CARD -> {
                val result = nowPlayer.special_card_deck[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_SPECIAL_YOUR)
                nowPlayer.special_card_deck.remove(card_number)
                return result
            }
            LocationEnum.USED_CARD -> {
                val result = nowPlayer.usedSpecialCard[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_USED_YOUR)
                nowPlayer.usedSpecialCard.remove(card_number)
                return result
            }
            LocationEnum.PLAYING_ZONE -> {
                for(card in nowPlayer.using_card) if (card.card_number == card_number) {
                    sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_PLAYING_YOUR)
                    nowPlayer.using_card.remove(card)
                    return card
                }
            }
            LocationEnum.ENCHANTMENT_ZONE -> {
                val result = nowPlayer.enchantment_card[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, result.card_number, public, CommandEnum.POP_ENCHANTMENT_YOUR)
                nowPlayer.enchantment_card.remove(card_number)
                return result
            }
            LocationEnum.YOUR_DECK_TOP -> {
                if(nowPlayer.normalCardDeck.isEmpty()) return null
                val result = nowPlayer.normalCardDeck.first()
                sendPopCardZone(nowSocket, otherSocket, result.card_number, public, CommandEnum.POP_DECK_YOUR)
                nowPlayer.normalCardDeck.removeFirst()
                return result
            }
            LocationEnum.SEAL_ZONE -> {
                val result = nowPlayer.sealZone[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_SEAL_YOUR)
                nowPlayer.sealZone.remove(card_number)
                return result
            }
            LocationEnum.POISON_BAG -> {
                val result = nowPlayer.poisonBag[cardNumberHashmap[card_number]]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_POISON_BAG_YOUR)
                nowPlayer.poisonBag.remove(cardNumberHashmap[card_number])
                return result
            }
            else -> TODO()
        }
        return null
    }

    suspend fun insertCardTo(player: PlayerEnum, card: Card, location: LocationEnum, public: Boolean){
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.opposite())
        when(location){
            LocationEnum.YOUR_DECK_BELOW -> {
                nowPlayer.normalCardDeck.addLast(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.DECK_BELOW_YOUR)
            }
            LocationEnum.YOUR_DECK_TOP -> {
                nowPlayer.normalCardDeck.addFirst(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.DECK_TOP_YOUR)
            }
            LocationEnum.DISCARD -> {
                nowPlayer.discard.addFirst(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.DISCARD_CARD_YOUR)
            }
            LocationEnum.PLAYING_ZONE -> {
                nowPlayer.using_card.addFirst(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.PLAYING_CARD_YOUR)
            }
            LocationEnum.USED_CARD -> {
                nowPlayer.usedSpecialCard[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.USED_CARD_YOUR)
            }
            LocationEnum.COVER_CARD -> {
                nowPlayer.cover_card.addFirst(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.COVER_CARD_YOUR)
            }
            LocationEnum.ENCHANTMENT_ZONE -> {
                nowPlayer.enchantment_card[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.ENCHANTMENT_CARD_YOUR)
            }
            LocationEnum.SPECIAL_CARD -> {
                nowPlayer.special_card_deck[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.SPECIAL_YOUR)
            }
            LocationEnum.HAND -> {
                nowPlayer.hand[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.HAND_YOUR)
            }
            LocationEnum.SEAL_ZONE -> {
                nowPlayer.sealZone[card.card_number] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.SEAL_YOUR)
            }
            LocationEnum.POISON_BAG -> {
                nowPlayer.poisonBag[card.card_data.card_name] = card
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.POISON_BAG_YOUR)
            }
            else -> TODO()
        }
    }

    suspend fun showSome(show_player: PlayerEnum, command: CommandEnum, card_number: Int){
        val nowPlayer = getPlayer(show_player)
        val list = mutableListOf<Int>()
        when(command){
            CommandEnum.SHOW_COVER_YOUR -> {
                for(card in nowPlayer.cover_card) list.add(card.card_number)
            }
            CommandEnum.SHOW_HAND_ALL_YOUR -> list.addAll(nowPlayer.hand.keys)
            CommandEnum.SHOW_HAND_YOUR -> list.addAll(arrayOf(card_number))
            else -> TODO()
        }
        sendShowInformation(command, getSocket(show_player), getSocket(show_player.opposite()), list)
    }

    fun getCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum): Card?{
        return when(location){
            LocationEnum.HAND -> getPlayer(player).getCardFromHand(card_number)
            LocationEnum.COVER_CARD -> getPlayer(player).getCardFromCover(card_number)
            LocationEnum.DISCARD -> getPlayer(player).getCardFromDiscard(card_number)
            LocationEnum.SPECIAL_CARD -> getPlayer(player).getCardFromSpecial(card_number)
            LocationEnum.YOUR_DECK_TOP -> getPlayer(player).getCardFromDeckTop(card_number)
            else -> TODO()
        }
    }

    suspend fun returnSpecialCard(player: PlayerEnum, card_number: Int): Boolean{
        val card = popCardFrom(player, card_number, LocationEnum.USED_CARD, true)?: return false
        card.special_card_state = SpecialCardEnum.UNUSED
        insertCardTo(player, card, LocationEnum.SPECIAL_CARD, true)
        return true
    }

    //megami special function

    fun getUmbrella(player: PlayerEnum): Umbrella?{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.umbrella
            PlayerEnum.PLAYER2 -> player2.umbrella
        }
    }

    fun getUmbrellaListener(player: PlayerEnum): ArrayDeque<Listener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1UmbrellaListener
            PlayerEnum.PLAYER2 -> player2UmbrellaListener
        }
    }

    suspend fun changeUmbrella(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.umbrella?.let {
            nowPlayer.umbrella = it.opposite()
            sendChangeUmbrella(getSocket(player), getSocket(player.opposite()))
            val umbrellaListener = getUmbrellaListener(player)
            if(!umbrellaListener.isEmpty()){
                for(i in 0..umbrellaListener.size){
                    if(umbrellaListener.isEmpty()) break
                    val now = umbrellaListener.first()
                    umbrellaListener.removeFirst()
                    if(!(now.doAction(this, -1, -1, false, false))){
                        player1Listener.addLast(now)
                    }
                }
            }
            for(card in nowPlayer.hand.values){
                card.checkWhenUmbrellaChange(player, this)
            }
        }
    }

    suspend fun getStratagem(player: PlayerEnum): Stratagem? {
        return when(player){
            PlayerEnum.PLAYER1 -> {
                if(player1.stratagem != null) sendGetStratagem(player1_socket, player2_socket, player1.stratagem!!)
                player1.stratagem
            }
            PlayerEnum.PLAYER2 -> {
                if(player2.stratagem != null) sendGetStratagem(player2_socket, player1_socket, player2.stratagem!!)
                player2.stratagem
            }
        }
    }

    suspend fun setStratagem(player: PlayerEnum, stratagem: Stratagem) {
        when(player){
            PlayerEnum.PLAYER1 -> {
                player1.stratagem = stratagem
                sendSetStratagem(player1_socket, player2_socket, stratagem)
            }
            PlayerEnum.PLAYER2 -> {
                player2.stratagem = stratagem
                sendSetStratagem(player2_socket, player1_socket, stratagem)
            }
        }
    }

    //megami special function
}