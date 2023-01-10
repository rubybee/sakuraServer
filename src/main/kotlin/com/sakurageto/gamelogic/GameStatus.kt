package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.card.*
import com.sakurageto.card.CardSet.cardNameHashmapSecond
import com.sakurageto.card.CardSet.cardNameHashmapFirst
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

    var distanceToken = 10
    var thisTurnDistance = 10
    var dust = 0

    var player1LifeListner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()
    var player2LifeListner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()

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

    suspend fun receiveCardEffectSelect(player: PlayerEnum): CommandEnum{
        return receiveCardEffectSelect(getSocket(player))
    }

    fun addImmediateLifeListner(player: PlayerEnum, listner: ImmediateBackListner){
        when(player){
            PlayerEnum.PLAYER1 -> player1LifeListner.addLast(listner)
            PlayerEnum.PLAYER2 -> player2LifeListner.addLast(listner)
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
            if(now % 100 == from.real_number || now / 100 == to.real_number) return true
        }
        for(card in player2.enchantment_card.values){
            now = card.forbidTokenMove(PlayerEnum.PLAYER2, this)
            if(now == -1) continue
            if(now % 100 == from.real_number || now / 100 == to.real_number) return true
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

    suspend fun cardToDistance(player: PlayerEnum, number: Int, card: Card){
        if(number == 0 || card.nap == 0 || card.nap == null) return
        var value = number

        if(distanceToken + value > 10) value = 10 - distanceToken

        distanceToken += value
        thisTurnDistance += value

        card.nap = card.nap!! - value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_CARD, LocationEnum.DISTANCE, value, card.card_number)
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
                LocationEnum.YOUR_CARD, LocationEnum.DUST, number, card.card_number)
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
            LocationEnum.YOUR_AURA, LocationEnum.YOUR_CARD, value, card.card_number)
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
            LocationEnum.DUST, LocationEnum.YOUR_CARD, value, card.card_number)
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

        val now_player = getPlayer(player)
        val value: Int

        if(now_player.flare >= number){
            value = number
            now_player.flare -= number
        }
        else{
            value = now_player.flare
            now_player.flare = 0
        }

        dust += value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_FLARE, LocationEnum.DUST, value, -1)
    }

    suspend fun chasmProcess(player: PlayerEnum){
        val now_player = getPlayer(player)

        for(enchantment_card in now_player.enchantment_card){
            if(enchantment_card.value.chasmCheck()){
                enchantmentDestructionNotNormally(player, enchantment_card.value)
            }
        }

    }

    suspend fun lifeListnerProcess(player: PlayerEnum, before: Int, reconstruct: Boolean){
        val now_player = getPlayer(player)
        when(player){
            PlayerEnum.PLAYER1 -> {
                if(!player1LifeListner.isEmpty()){
                    for(i in 0..player1LifeListner.size){
                        val now = player1LifeListner.first()
                        player1LifeListner.removeFirst()
                        if(now.IsItBack(before, now_player.life, reconstruct)){
                            if(now_player.usedToSpecial(now.card_number)){
                                sendUsedCardReturn(player1_socket, player2_socket, now.card_number)
                            }
                        }
                        else{
                            player1LifeListner.addLast(now)
                        }
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(!player2LifeListner.isEmpty()){
                    for(i in 0..player2LifeListner.size){
                        val now = player2LifeListner.first()
                        player2LifeListner.removeFirst()
                        if(now.IsItBack(before, now_player.life, reconstruct)){
                            if(now_player.usedToSpecial(now.card_number)){
                                sendUsedCardReturn(player2_socket, player1_socket, now.card_number)
                            }
                        }
                        else{
                            player2LifeListner.addLast(now)
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

        lifeListnerProcess(player, before, false)
        chasmProcess(player)

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_LIFE, LocationEnum.DUST, number, -1)
        return false
    }

    //return endgame
    suspend fun lifeToSelfFlare(player: PlayerEnum, number: Int, reconstruct: Boolean): Boolean{
        val nowPlayer = getPlayer(player)

        val before = nowPlayer.life

        if(nowPlayer.life > number){
            nowPlayer.life -= number
            nowPlayer.flare += number
        }
        else{
            return true
        }

        lifeListnerProcess(player, before, reconstruct)

        if(!reconstruct){
            chasmProcess(player)
        }

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.YOUR_LIFE, LocationEnum.YOUR_FLARE, number, -1)

        return false
    }

    suspend fun dustToAura(player: PlayerEnum, number: Int){
        val now_player = getPlayer(player)
        val value: Int
        if(number > dust){
            value = now_player.plusAura(dust)
            dust -= value
        }
        else{
            value = now_player.plusAura(number)
            dust -= value
        }
        sendMoveToken(getSocket(player), getSocket(player.Opposite()), TokenEnum.SAKURA_TOKEN,
            LocationEnum.DUST, LocationEnum.YOUR_AURA, value, -1)
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

        if(now_player.pre_attack_card!!.aura_damage > 5){
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
                    var check_bit = true
                    other_player.getCardFromHand(react.second)?.let {
                        if(reactCheck(player.Opposite(), it, now_attack)){
                            val cost = it.canUse(player.Opposite(), this, now_attack)
                            if(cost == -1){
                                check_bit = false
                                logger.insert(Log(player, LogText.USE_CARD_REACT, card_number, card_number))
                                sendUseCardMeesage(other_socket, now_socket, true, it.card_number)
                                other_player.useCardFromHand(it.card_number)
                                it.use(player.Opposite(), this, now_attack)
                            }
                        }
                    }
                    if(check_bit){
                        continue
                    }
                    else{
                        break
                    }
                }
                else if(react.first == CommandEnum.REACT_USE_CARD_SPECIAL){
                    var check_bit = true
                    other_player.getCardFromSpecial(react.second)?.let {
                        if(reactCheck(player.Opposite(), it, now_attack)){
                            val cost = it.canUse(player.Opposite(), this, now_attack)
                            if(cost >= 0){
                                check_bit = false
                                it.special_card_state = SpecialCardEnum.PLAYING
                                logger.insert(Log(player, LogText.USE_CARD_REACT, card_number, card_number))
                                sendUseCardMeesage(other_socket, now_socket, true, it.card_number)
                                flareToDust(player.Opposite(), cost)
                                other_player.useCardFromSpecial(it.card_number)
                                cleanAfterUseCost()
                                it.use(player.Opposite(), this, now_attack)
                            }
                        }
                    }
                    if(check_bit){
                        continue
                    }
                    else{
                        break
                    }
                }
                else{
                    break
                }
            }
        }

        if(now_attack.isItValid && now_attack.rangeCheck(thisTurnDistance)){
            sendChooseDamage(other_socket, CommandEnum.CHOOSE_CARD_DAMAGE, now_attack.aura_damage, now_attack.life_damage)
            val chosen = receiveChooseDamage(other_socket)
            if(now_attack.bothSideDamage){
                auraToDust(player.Opposite(), now_attack.aura_damage)
                lifeToSelfFlare(player.Opposite(), now_attack.life_damage, false)
            }
            else{
                processDamage(player.Opposite(), chosen, Pair(now_attack.aura_damage, now_attack.life_damage), false)
            }
            now_attack.afterAttackProcess(player, this, react_attack)
        }
    }

    suspend fun afterCardUsed(game_status: GameStatus, player: PlayerEnum, card: Card){
        val playing_player = getPlayer(player)
        playing_player.using_card.removeLast()

        if(card.card_data.card_type == CardType.ENCHANTMENT){
            val playing_socket = getSocket(player)
            val not_playing_socket = getSocket(player.Opposite())
            playing_player.enchantment_card[card.card_number] = card
            sendEnchantmentZone(playing_socket, not_playing_socket, card.card_number)
            if(card.nap == 0){
                game_status.enchantmentDestruction(player, card)
            }
        }
        else{
            val owner_player = getPlayer(card.player)
            val owner_socket = getSocket(card.player)
            val not_owner_socket = getSocket(card.player.Opposite())
            when(card.card_data.card_class){
                CardClass.SPECIAL -> {
                    card.addReturnListener(card.player, this)
                    card.special_card_state = SpecialCardEnum.PLAYED
                    owner_player.used_special_card[card.card_number] = card
                    sendUsedZone(owner_socket, not_owner_socket, card.card_number)
                }
                CardClass.NORMAL -> {
                    owner_player.discard.addLast(card)
                    sendDiscardZone(owner_socket, not_owner_socket, card.card_number)
                }
            }
        }
    }

    suspend fun afterDestruction(card: Card, locationEnum: LocationEnum){
        val owner_player = getPlayer(card.player)
        val owner_socket = getSocket(card.player)
        val not_owner_socket = getSocket(card.player.Opposite())
        when(card.card_data.card_class){
            CardClass.SPECIAL -> {
                card.addReturnListener(card.player, this)
                card.special_card_state = SpecialCardEnum.PLAYED
                owner_player.used_special_card[card.card_number] = card
                sendUsedZone(owner_socket, not_owner_socket, card.card_number)
            }
            CardClass.NORMAL -> {
                if(locationEnum == LocationEnum.COVER_CARD){
                    owner_player.cover_card.addLast(card)
                    sendCoverZone(owner_socket, not_owner_socket, card.card_number, true)
                }
                else if(locationEnum == LocationEnum.DISCARD){
                    owner_player.discard.addLast(card)
                    sendDiscardZone(owner_socket, not_owner_socket, card.card_number)
                }
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
        getPlayer(player).enchantment_card.remove(card.card_number)
        afterDestruction(card, LocationEnum.DISCARD)
    }

    suspend fun enchantmentDestructionNotNormally(player: PlayerEnum, card: Card){
        getPlayer(player).enchantment_card.remove(card.card_number)
        sendDestructionEnchant(getSocket(player), getSocket(player.Opposite()), card.card_number)

        afterDestruction(card, LocationEnum.COVER_CARD)
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
                sendRequestEnchantmentCard(player1_socket, player1_card.keys.toMutableList(), player2_card.keys.toMutableList())
                while(true){
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
                sendRequestEnchantmentCard(player2_socket, player2_card.keys.toMutableList(), player1_card.keys.toMutableList())
                while(true){
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
                    return
                }
                val selectable = nowPlayer.checkAuraDamage(damage.first)
                if(damage.second == 999){
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
                if(lifeToSelfFlare(player, damage.second, reconstruct)) {
                    gameEnd(player.Opposite())
                }
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
               if(nowPlayer.usedToSpecial(cardNumber)){
                   sendUsedCardReturn(getSocket(player), getSocket(player.Opposite()), cardNumber)
               }
           }
        }
        thisTurnDistance = distanceToken
        logger.reset()
    }

    suspend fun deckReconstruct(player: PlayerEnum, damage: Boolean){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        sendDeckReconstruct(now_socket, other_socket)
        if(damage){
            processDamage(player, CommandEnum.CHOOSE_LIFE, Pair(999, 1), true)
        }
        Card.cardReconstructInsert(now_player.discard, now_player.cover_card, now_player.normal_card_deck)
    }

    suspend fun cardUseNormaly(player: PlayerEnum, commandEnum: CommandEnum, card_number: Int): Boolean{
        if(card_number == -1){
            return false
        }

        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        var using_successly = false

        if(commandEnum == CommandEnum.ACTION_USE_CARD_HAND){
            now_player.getCardFromHand(card_number)?.let{
                val cost = it.canUse(player, this, null)
                if(cost == -1){
                    using_successly = true
                    logger.insert(Log(player, LogText.USE_CARD, card_number, card_number))
                    sendUseCardMeesage(now_socket, other_socket, false, it.card_number)
                    now_player.useCardFromHand(it.card_number)
                    it.use(player, this, null)
                }
            }
        }
        else if(commandEnum == CommandEnum.ACTION_USE_CARD_SPECIAL){
            now_player.getCardFromSpecial(card_number)?.let {
                val cost = it.canUse(player, this, null)
                if(cost >= 0){
                    using_successly = true
                    it.special_card_state = SpecialCardEnum.PLAYING
                    logger.insert(Log(player, LogText.USE_CARD, card_number, card_number))
                    sendUseCardMeesage(now_socket, other_socket,false, it.card_number)
                    flareToDust(player, cost)
                    now_player.useCardFromSpecial(it.card_number)
                    cleanAfterUseCost()
                    it.use(player, this, null)
                }
            }
        }
        else{
            return false
        }

        return using_successly
    }

    suspend fun basicOperationCost(player: PlayerEnum, card_number: Int): Boolean{
        val now_player = getPlayer(player)

        return if(card_number == -1){
            if(now_player.concentration == 0) false
            else {
                decreaseConcentration(player)
                true
            }
        } else if(now_player.fromHandToCover(card_number)){
            sendHandToCover(getSocket(player), getSocket(player.Opposite()), card_number, false)
            true
        } else{
            false
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

    suspend fun coverCard(player: PlayerEnum){
        val nowPlayer = getPlayer(player)
        val nowSocket = getSocket(player)

        for(card in nowPlayer.hand.values){
            if(card.card_data.can_cover){
                val cardName = receiveCoverCardSelect(nowSocket)
                if(nowPlayer.fromHandToCover(cardName)){
                    sendHandToCover(getSocket(player), getSocket(player.Opposite()), cardName, false)
                    return
                }
                else{
                    coverCard(player)
                    return
                }
            }
        }
    }

    suspend fun endTurnHandCheck(player: PlayerEnum){
        val nowPlayer = getPlayer(player)

        while (true){
            if(nowPlayer.hand.size <= nowPlayer.max_hand){
                return
            }

            coverCard(player)
        }
    }
}