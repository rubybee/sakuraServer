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

    val logger = Logger()

    var startDistance = 10

    suspend fun getAdjustSwellDistance(player: PlayerEnum): Int{
        var nowSwellDistance = swellDistance

        for(card in player1.enchantment_card.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player2.enchantment_card.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player1.used_special_card.values) nowSwellDistance += card.swellAdjust(player, this)
        for(card in player2.used_special_card.values) nowSwellDistance += card.swellAdjust(player, this)

        return nowSwellDistance
    }

    fun getAdjustDistanceDuringAttack(player: PlayerEnum): Int{
        var distance = thisTurnDistance

        //TODO("SOMETHING WILL BE ADDED HERE")

        return distance
    }

    fun getDistance(): Int{
        return thisTurnDistance
    }

    var distanceToken = 10
    var thisTurnDistance = 10
    var dust = 0

    var player1LifeListener: ArrayDeque<ImmediateBackListener> = ArrayDeque<ImmediateBackListener>()
    var player2LifeListener: ArrayDeque<ImmediateBackListener> = ArrayDeque<ImmediateBackListener>()

    val player1UmbrellaListener: ArrayDeque<ImmediateBackListener> = ArrayDeque<ImmediateBackListener>()
    val player2UmbrellaListener: ArrayDeque<ImmediateBackListener> = ArrayDeque<ImmediateBackListener>()

    lateinit var first_turn: PlayerEnum

    fun getPlayer(player: PlayerEnum): PlayerStatus{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    fun getSocket(player: PlayerEnum): Connection{
        return if(player ==  PlayerEnum.PLAYER1) player1_socket else player2_socket
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

    fun getUmbrella(player: PlayerEnum): Umbrella?{
        return when(player){
            PlayerEnum.PLAYER1 -> player1.umbrella
            PlayerEnum.PLAYER2 -> player2.umbrella
        }
    }

    fun getUmbrellaListener(player: PlayerEnum): ArrayDeque<ImmediateBackListener>{
        return when(player){
            PlayerEnum.PLAYER1 -> player1UmbrellaListener
            PlayerEnum.PLAYER2 -> player2UmbrellaListener
        }
    }

    suspend fun changeUmbrella(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        nowPlayer.umbrella?.let {
            val umbrellaListener = getUmbrellaListener(player)
            if(!umbrellaListener.isEmpty()){
                for(i in 0..umbrellaListener.size){
                    if(umbrellaListener.isEmpty()) break
                    val now = umbrellaListener.first()
                    umbrellaListener.removeFirst()
                    if(now.IsItBack(-1, -1, false)){
                        returnSpecialCard(player, now.card_number)
                    }
                    else{
                        player1LifeListener.addLast(now)
                    }
                }
            }
            nowPlayer.umbrella = it.opposite()
            sendChangeUmbrella(getSocket(player), getSocket(player.Opposite()))
            for(card in nowPlayer.hand.values){
                card.checkWhenUmbrellaChange(player, this)
            }
        }
    }
    
    suspend fun setConcentration(player: PlayerEnum, number: Int){
        when(player){
            PlayerEnum.PLAYER1 -> player1.concentration = number
            PlayerEnum.PLAYER2 -> player2.concentration = number
        }
        sendSetConcentration(getSocket(player), getSocket(player.Opposite()), number)
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
        sendSetShrink(getSocket(player), getSocket(player.Opposite()))
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

    fun addImmediateLifeListener(player: PlayerEnum, listener: ImmediateBackListener){
        when(player){
            PlayerEnum.PLAYER1 -> player1LifeListener.addLast(listener)
            PlayerEnum.PLAYER2 -> player2LifeListener.addLast(listener)
        }
    }

    fun addImmediateUmbrellaListener(player: PlayerEnum, listener: ImmediateBackListener){
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

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
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
            sendMoveToken(getSocket(player_aura), getSocket(player_aura.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, value, -1)
        }
        else{
            sendMoveToken(getSocket(player_aura), getSocket(player_aura.Opposite()), TokenEnum.SAKURA_TOKEN,
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

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.DISTANCE, value, card.card_number)
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
        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
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
                false
            }
        }
    }

    //메구미 카드의 경우 개시페이지 여부를 이곳에서 받아서 처리
    suspend fun cardToDust(player: PlayerEnum, number: Int, card: Card){
        if(number == 0 || card.nap == 0 || card.nap == null) return

        if(cardToDustCheck(player, number, card)){
            dust += number

            card.nap = card.nap!! - number

            sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
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

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
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

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DUST, LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, value, card.card_number)
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
        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
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
        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
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

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_AURA, LocationEnum.DUST, value, -1)
    }

    suspend fun flareToDust(player: PlayerEnum, number: Int){
        if(number == 0) return

        val nowPlayer = getPlayer(player)
        var value: Int = number

        if(nowPlayer.flare < value){
            value = nowPlayer.flare
        }
        nowPlayer.flare -= value

        dust += value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_FLARE, LocationEnum.DUST, value, -1)
    }

    suspend fun flareToSelfAura(player: PlayerEnum, number: Int){
        if(number == 0) return

        val nowPlayer = getPlayer(player)
        var value = number

        if(nowPlayer.flare < number){
            value = nowPlayer.flare
        }

        if(nowPlayer.maxAura - nowPlayer.aura > value){
            value = nowPlayer.maxAura - nowPlayer.aura
        }

        nowPlayer.flare -= value
        nowPlayer.aura += value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_FLARE, LocationEnum.YOUR_AURA, value, -1)
    }

    suspend fun chasmProcess(player: PlayerEnum){
        val now_player = getPlayer(player)

        for(enchantment_card in now_player.enchantment_card){
            if(enchantment_card.value.chasmCheck()){
                enchantmentDestructionNotNormally(player, enchantment_card.value)
            }
        }

    }

    suspend fun lifeListenerProcess(player: PlayerEnum, before: Int, reconstruct: Boolean){
        val now_player = getPlayer(player)
        when(player){
            PlayerEnum.PLAYER1 -> {
                if(!player1LifeListener.isEmpty()){
                    for(i in 0..player1LifeListener.size){
                        if(player1LifeListener.isEmpty()) break
                        val now = player1LifeListener.first()
                        player1LifeListener.removeFirst()
                        if(now.IsItBack(before, now_player.life, reconstruct)){
                            returnSpecialCard(player, now.card_number)
                        }
                        else{
                            player1LifeListener.addLast(now)
                        }
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(!player2LifeListener.isEmpty()){
                    for(i in 0..player2LifeListener.size){
                        if(player2LifeListener.isEmpty()) break
                        val now = player2LifeListener.first()
                        player2LifeListener.removeFirst()
                        if(now.IsItBack(before, now_player.life, reconstruct)){
                            returnSpecialCard(player, now.card_number)
                        }
                        else{
                            player2LifeListener.addLast(now)
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

        lifeListenerProcess(player, before, false)
        chasmProcess(player)

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_LIFE, LocationEnum.DUST, number, -1)
        return false
    }

    //return endgame
    suspend fun lifeToSelfFlare(player: PlayerEnum, number: Int, reconstruct: Boolean): Boolean{
        if(number == 0) return false

        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life > number){
            nowPlayer.life -= number
            nowPlayer.flare += number
        }
        else{
            return true
        }

        lifeListenerProcess(player, before, reconstruct)

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_LIFE, LocationEnum.YOUR_FLARE, number, -1)

        return false
    }

    suspend fun addAllCardTextBuff(){
        for(card in player1.enchantment_card){
            card.value.addAttackBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player1.used_special_card){
            card.value.addAttackBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player2.enchantment_card){
            card.value.addAttackBuff(PlayerEnum.PLAYER2, this)
        }
        for(card in player2.used_special_card){
            card.value.addAttackBuff(PlayerEnum.PLAYER2, this)
        }
    }

    suspend fun addAllCardCostBuff(){
        for(card in player1.enchantment_card){
            card.value.addCostBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player1.used_special_card){
            card.value.addCostBuff(PlayerEnum.PLAYER1, this)
        }
        for(card in player2.enchantment_card){
            card.value.addCostBuff(PlayerEnum.PLAYER2, this)
        }
        for(card in player2.used_special_card){
            card.value.addCostBuff(PlayerEnum.PLAYER2, this)
        }
    }

    suspend fun applyAllCostBuff(player: PlayerEnum, cost: Int, card: Card): Int{
        val now_player = getPlayer(player)
        var now_cost = cost

        addAllCardTextBuff()

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

    fun applyAllAttackBuff(player: PlayerEnum){
        val now_player = getPlayer(player)

        for(queue in now_player.attack_buf){
            val tempq: ArrayDeque<Buff> = ArrayDeque()
            for(i in queue.indices){
                val now = queue.first()
                queue.removeFirst()
                if(now.condition(player, this, now_player.pre_attack_card!!)){
                    now.counter -= 1
                    tempq.add(now)
                }
                if(now.counter != 0){
                    queue.addLast(now)
                }
            }
            for(buff in tempq){
                buff.effect(now_player.pre_attack_card!!)
            }
        }

        if(now_player.pre_attack_card!!.aura_damage != 999 && now_player.pre_attack_card!!.aura_damage > 5){
            if(!now_player.pre_attack_card!!.chogek){
                now_player.pre_attack_card!!.aura_damage = 5
            }
        }
    }

    suspend fun attackCheck(player: PlayerEnum): Boolean{
        addAllCardTextBuff()

        val now_player = getPlayer(player)

        for(queue in now_player.range_buf){
            val tempq: ArrayDeque<RangeBuff> = ArrayDeque()
            for(buff in queue){
                if(buff.condition(player, this, now_player.pre_attack_card!!)){
                    buff.counter *= -1
                    tempq.add(buff)
                }
            }
            for(buff in tempq){
                buff.effect(now_player.pre_attack_card!!)
            }
        }

        return now_player.pre_attack_card!!.rangeCheck(getAdjustDistanceDuringAttack(player))
    }

    fun cleanAfterUseCost(){
        cleanCostBuff(player1.cost_buf)
        cleanCostBuff(player2.cost_buf)
    }

    fun cleanCostBuff(){
        cleanCostTempBuff(player1.cost_buf)
        cleanCostTempBuff(player2.cost_buf)
    }

    fun cleanAllBuff(){
        cleanRangeTempBuff(player1.range_buf)
        cleanRangeTempBuff(player2.range_buf)
        cleanAttackTempBuff(player1.attack_buf)
        cleanAttackTempBuff(player2.attack_buf)
    }

    suspend fun addPreAttackZone(player: PlayerEnum, attack: MadeAttack): Boolean{
        val now_player = getPlayer(player)
        val other_player = getPlayer(player.Opposite())

        now_player.addPreAttackZone(attack)

        return if(attackCheck(player)){
            cleanRangeTempBuff(other_player.range_buf)
            cleanRangeBuff(now_player.range_buf)
            applyAllAttackBuff(player)
            true
        } else{
            now_player.pre_attack_card = null
            cleanAllBuff()
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
        when (player){
            PlayerEnum.PLAYER1 -> player1.addAttackBuff(effect)
            PlayerEnum.PLAYER2 -> player2.addAttackBuff(effect)
        }
    }

    fun addThisTurnRangeBuff(player: PlayerEnum, effect: RangeBuff){
        when (player){
            PlayerEnum.PLAYER1 -> player1.addRangeBuff(effect)
            PlayerEnum.PLAYER2 -> player2.addRangeBuff(effect)
        }
    }

    suspend fun addConcentration(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

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
        val other_socket = getSocket(player.Opposite())

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

    suspend fun useCardFrom(player: PlayerEnum, card: Card, location: LocationEnum, react: Boolean, react_attack: MadeAttack?): Boolean{
        val cost = card.canUse(player, this, react_attack)
        if(cost != -2){
            if(location == LocationEnum.COVER_CARD && react) logger.insert(Log(player, LogText.USE_CARD_IN_COVER_AND_REACT, card.card_number, card.card_number))
            else if(location == LocationEnum.COVER_CARD) logger.insert(Log(player, LogText.USE_CARD_IN_COVER, card.card_number, card.card_number))
            else if(react) logger.insert(Log(player, LogText.USE_CARD_REACT, card.card_number, card.card_number))
            else logger.insert(Log(player, LogText.USE_CARD, card.card_number, card.card_number))
            if(cost == -1){
                popCardFrom(player, card.card_number, location, true)
                insertCardTo(player, card, LocationEnum.PLAYING_ZONE, true)
                sendUseCardMeesage(getSocket(player), getSocket(player.Opposite()), react, card.card_number)
                card.use(player, this, react_attack)
                return true
            }
            if(cost >= 0){
                card.special_card_state = SpecialCardEnum.PLAYING
                flareToDust(player, cost)
                cleanAfterUseCost()
                popCardFrom(player, card.card_number, LocationEnum.SPECIAL_CARD, true)
                insertCardTo(player, card, LocationEnum.PLAYING_ZONE, true)
                sendUseCardMeesage(getSocket(player), getSocket(player.Opposite()), react, card.card_number)
                card.use(player, this, react_attack)
                return true
            }
        }
        return false
    }

    suspend fun afterMakeAttack(card_number: Int, player: PlayerEnum, react_attack: MadeAttack?){
        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())
        val now_player = getPlayer(player)
        val other_player = getPlayer(player.Opposite())

        if(now_player.pre_attack_card == null){
            return
        }

        val now_attack = now_player.pre_attack_card!!
        now_player.pre_attack_card = null

        makeAttackComplete(now_socket, other_socket, card_number)
        sendAttackInformation(now_socket, other_socket, now_attack.Information())
        if(!other_player.end_turn && react_attack == null){
            while(true){
                sendRequestReact(other_socket)
                val react = receiveReact(other_socket)
                if(react.first == CommandEnum.REACT_USE_CARD_HAND){
                    val card = other_player.getCardFromHand(react.second)?: continue
                    if(reactCheck(player.Opposite(), card, now_attack)){
                        if(useCardFrom(player.Opposite(), card, LocationEnum.HAND, true, now_attack)) break
                    }

                }
                else if(react.first == CommandEnum.REACT_USE_CARD_SPECIAL){
                    val card = other_player.getCardFromSpecial(react.second)?: continue
                    if(reactCheck(player.Opposite(), card, now_attack)){
                        if(useCardFrom(player.Opposite(), card, LocationEnum.SPECIAL_CARD, true, now_attack)) break
                    }
                }
                else{
                    break
                }
            }
        }

        if(now_attack.isItValid && now_attack.rangeCheck(getAdjustDistanceDuringAttack(player))){
            sendChooseDamage(other_socket, CommandEnum.CHOOSE_CARD_DAMAGE, now_attack.aura_damage, now_attack.life_damage)
            val chosen = receiveChooseDamage(other_socket)
            if(now_attack.bothSideDamage){
                processDamage(player.Opposite(), CommandEnum.CHOOSE_AURA, Pair(now_attack.aura_damage, 999), false)
                processDamage(player.Opposite(), CommandEnum.CHOOSE_LIFE, Pair(999, now_attack.life_damage), false)
            }
            else{
                processDamage(player.Opposite(), chosen, Pair(now_attack.aura_damage, now_attack.life_damage), false)
            }
            now_attack.afterAttackProcess(player, this, react_attack)
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
                    print(getPlayer(card.player).used_special_card)
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
        sendDestructionEnchant(getSocket(player), getSocket(player.Opposite()), card.card_number)
        card.destructionEnchantmentNormaly(player, this)
        afterDestruction(player, card.card_number, LocationEnum.DISCARD)
    }

    suspend fun enchantmentDestructionNotNormally(player: PlayerEnum, card: Card){
        sendDestructionEnchant(getSocket(player), getSocket(player.Opposite()), card.card_number)
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
                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
            }
        }
    }

    suspend fun gameEnd(winner: PlayerEnum){
        val winner_socket = getSocket(winner)
        val roser_socket = getSocket(winner.Opposite())

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
                            val receive = receiveAuraDamageSelect(getSocket(player), selectable)
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
                                val receive = receiveAuraDamageSelect(getSocket(player), selectable)
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
                if(lifeToSelfFlare(player, damage.second, reconstruct)) {
                    gameEnd(player.Opposite())
                }
                if(!reconstruct) chasmProcess(player)
            }
        }
    }

    suspend fun drawCard(player: PlayerEnum, number: Int){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        for(i in 1..number){
            if(now_player.normal_card_deck.size == 0){
                sendChooseDamage(now_socket, CommandEnum.CHOOSE_CHOJO, 1, 1)
                val chosen = receiveChooseDamage(now_socket)
                processDamage(player, chosen, Pair(1, 1), false)
                continue
            }
            val draw_card = now_player.normal_card_deck.first()
            sendDrawCard(now_socket, other_socket, draw_card.card_number)
            now_player.hand[draw_card.card_number] = draw_card
            now_player.normal_card_deck.removeFirst()
        }
    }

    //first means upperside of deck, last means belowside of deck
    suspend fun insertHandToDeck(public: Boolean, Below: Boolean, player: PlayerEnum, card_number: Int): Boolean{
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        return now_player.hand[card_number]?.let {
            if(Below) now_player.normal_card_deck.addLast(it)
            else now_player.normal_card_deck.addFirst(it)
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
        logger.reset()
    }

    //0 = player don't want using card more || 1 = player card use success || 2 = cannot use because there are no installation card
    suspend fun useInstallationOnce(player: PlayerEnum): Int{
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val list = nowPlayer.getInstallationCard()
        if(list.isEmpty()) return 2

        while(true){
            val receive = receiveSelectCard(nowSocket, list, CommandEnum.SELECT_CARD_REASON_INSTALLATION)?: continue
            if (receive.size == 1){
                val card = nowPlayer.getCardFromCover(receive[0])?: continue
                if (useCardFrom(player, card, LocationEnum.COVER_CARD, false, null)) {
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
        val otherSocket = getSocket(player.Opposite())

        installationProcess(player)

        sendDeckReconstruct(nowSocket, otherSocket)
        if(damage){
            processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), true)
        }
        Card.cardReconstructInsert(nowPlayer.discard, nowPlayer.cover_card, nowPlayer.normal_card_deck)
    }

    suspend fun cardUseNormal(player: PlayerEnum, commandEnum: CommandEnum, card_number: Int): Boolean{
        if(card_number == -1){
            return false
        }
        if(commandEnum == CommandEnum.ACTION_USE_CARD_HAND){
            val card = getCardFrom(player, card_number, LocationEnum.HAND)?: return false
            if(useCardFrom(player, card, LocationEnum.HAND, false, null)) return true
        }
        else if(commandEnum == CommandEnum.ACTION_USE_CARD_SPECIAL){
            val card = getCardFrom(player, card_number, LocationEnum.SPECIAL_CARD)?: return false
            if(useCardFrom(player, card, LocationEnum.SPECIAL_CARD, false, null)) return true
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
            popCardFrom(player, card_number, LocationEnum.HAND, false)
            insertCardTo(player, card, LocationEnum.COVER_CARD, false)
            return true
        }
    }

    suspend fun canDoBasicOperation(player: PlayerEnum, command: CommandEnum): Boolean{
        val now_player = getPlayer(player)
        return when(command){
            CommandEnum.ACTION_GO_FORWARD -> !(now_player.aura + now_player.freezeToken == now_player.maxAura || distanceToken == 0 || thisTurnDistance <= getAdjustSwellDistance(player))
            CommandEnum.ACTION_GO_BACKWARD -> !(now_player.aura == 0 || distanceToken == 10)
            CommandEnum.ACTION_WIND_AROUND -> !(dust == 0 || now_player.aura == now_player.maxAura)
            CommandEnum.ACTION_INCUBATE -> now_player.aura != 0
            CommandEnum.ACTION_BREAK_AWAY -> !(dust == 0 || thisTurnDistance > getAdjustSwellDistance(player) || distanceToken == 10)
            else -> false
        }
    }

    suspend fun doBasicOperation(player: PlayerEnum, command: CommandEnum){
        when(command){
            CommandEnum.ACTION_GO_FORWARD -> doGoForward(player)
            CommandEnum.ACTION_GO_BACKWARD -> doGoBackward(player)
            CommandEnum.ACTION_WIND_AROUND -> doWindAround(player)
            CommandEnum.ACTION_INCUBATE -> doIncubate(player)
            CommandEnum.ACTION_BREAK_AWAY -> doBreakAway(player)
            else -> {}
        }
    }

    //this 5 function must call after check when select
    suspend fun doGoForward(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(now_player.aura == now_player.maxAura || distanceToken == 0 || thisTurnDistance <= getAdjustSwellDistance(player)) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_GO_FORWARD_YOUR)
            distanceToken -= 1
            thisTurnDistance -= 1
            if(thisTurnDistance < 0){
                thisTurnDistance = 0
            }
            getPlayer(player).aura += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DISTANCE, LocationEnum.YOUR_AURA, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doGoBackward(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(now_player.aura == 0 || distanceToken == 10) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_GO_BACKWARD_YOUR)
            now_player.aura -= 1
            distanceToken += 1
            thisTurnDistance += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.DISTANCE, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doWindAround(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(dust == 0 || now_player.aura == now_player.maxAura) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_WIND_AROUND_YOUR)
            dust -= 1
            now_player.aura += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.DUST, LocationEnum.YOUR_AURA, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doIncubate(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(now_player.aura == 0) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_INCUBATE_YOUR)
            now_player.aura -= 1
            now_player.flare += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, 1, -1)
        }
    }

    //this 5 function must call after check when select
    suspend fun doBreakAway(player: PlayerEnum){
        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(dust == 0 || thisTurnDistance > getAdjustSwellDistance(player) || distanceToken == 10) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_BREAK_AWAY_YOUR)
            dust -= 1
            distanceToken += 1
            thisTurnDistance += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
                LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, 1, -1)
        }
    }

    suspend fun coverCard(player: PlayerEnum, select_player: PlayerEnum){
        val nowSocket = getSocket(select_player)

        if(checkCoverAbleHand(player)){
            val list = mutableListOf<Int>()
            for (card in getPlayer(player).cover_card) list.add(card.card_number)
            val cardNumber = receiveCoverCardSelect(nowSocket, list)
            if(cardNumberHashmap[cardNumber] == null){
                coverCard(player, select_player)
                return
            }
            else if(!returnCardDataByName(cardNumberHashmap[cardNumber]!!).canCover){
                coverCard(player, select_player)
                return
            }
            val card = popCardFrom(player, cardNumber, LocationEnum.HAND, false)
            if(card == null) {
                coverCard(player, select_player)
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

        for(card in nowPlayer.cover_card){
            if(card.card_data.canCover) return true
        }
        return false
    }

    suspend fun endTurnHandCheck(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        while (true){
            if(nowPlayer.hand.size <= nowPlayer.max_hand) return

            coverCard(player, player)
        }
    }

    //select_player -> cardUser ||| player -> victim ||| function that select card in list(list check is not needed)
    suspend fun selectCardFrom(player: PlayerEnum, select_player: PlayerEnum, location_list: List<LocationEnum>, reason: CommandEnum): MutableList<Int>{
        val cardList = mutableListOf<Int>()
        val searchPlayer = getPlayer(player)

        for (location in location_list){
            searchPlayer.insertCardNumber(location, cardList)
        }

        while (true){
            return receiveSelectCard(getSocket(select_player), cardList, reason) ?: continue
        }
    }

    suspend fun popCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum, public: Boolean): Card?{
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.Opposite())
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
            LocationEnum.DECK -> for(card in nowPlayer.normal_card_deck) if (card.card_number == card_number) {
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_DECK_YOUR)
                nowPlayer.normal_card_deck.remove(card)
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
                val result = nowPlayer.used_special_card[card_number]?: return null
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_USED_YOUR)
                nowPlayer.used_special_card.remove(card_number)
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
                sendPopCardZone(nowSocket, otherSocket, card_number, public, CommandEnum.POP_ENCHANTMENT_YOUR)
                nowPlayer.enchantment_card.remove(card_number)
                return result
            }
            else -> TODO()
        }
        return null
    }

    suspend fun insertCardTo(player: PlayerEnum, card: Card, location: LocationEnum, public: Boolean){
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)
        val otherSocket = getSocket(player.Opposite())
        when(location){
            LocationEnum.YOUR_DECK_BELOW -> {
                nowPlayer.normal_card_deck.addLast(card)
                sendAddCardZone(nowSocket, otherSocket, card.card_number, public, CommandEnum.DECK_BELOW_YOUR)
            }
            LocationEnum.YOUR_DECK_TOP -> {
                nowPlayer.normal_card_deck.addFirst(card)
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
                nowPlayer.used_special_card[card.card_number] = card
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
            else -> TODO()
        }
    }

    fun endlessWindCheck(player: PlayerEnum): Boolean{
        for (card in getPlayer(player).hand.values){
            if(card.card_data.card_type == CardType.ATTACK || !card.card_data.canDiscard) continue
            return false
        }
        return true
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
        sendShowInformation(command, getSocket(show_player), getSocket(show_player.Opposite()), list)
    }

    fun checkCoverFullPower(player: PlayerEnum): Boolean{
        val nowPlayer = getPlayer(player)
        for (card in nowPlayer.cover_card){
            if(card.card_data.sub_type != SubType.FULLPOWER) return false
        }
        return true
    }

    fun getCardFrom(player: PlayerEnum, card_number: Int, location: LocationEnum): Card?{
        return when(location){
            LocationEnum.HAND -> getPlayer(player).getCardFromHand(card_number)
            LocationEnum.COVER_CARD -> getPlayer(player).getCardFromCover(card_number)
            LocationEnum.DISCARD -> getPlayer(player).getCardFromDiscard(card_number)
            LocationEnum.SPECIAL_CARD -> getPlayer(player).getCardFromSpecial(card_number)
            else -> TODO()
        }
    }

    suspend fun returnSpecialCard(player: PlayerEnum, card_number: Int): Boolean{
        val card = popCardFrom(player, card_number, LocationEnum.USED_CARD, true)?: return false
        card.special_card_state = SpecialCardEnum.UNUSED
        insertCardTo(player, card, LocationEnum.SPECIAL_CARD, true)
        return true
    }
}