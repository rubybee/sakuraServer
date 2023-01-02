package com.sakurageto.gamelogic

import com.sakurageto.Connection
import com.sakurageto.card.*
import com.sakurageto.protocol.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.serialization.descriptors.PrimitiveKind

class GameStatus(val player1: PlayerStatus, val player2: PlayerStatus, val player1_socket: Connection, val player2_socket: Connection) {
    var start_distance = 10

    var swell_distance = 2

    var distance_token = 10
    var distance = 10
    var dust = 0

    var player1_life_listner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()
    var player2_life_listner: ArrayDeque<ImmediateBackListner> = ArrayDeque<ImmediateBackListner>()

    lateinit var first_turn: PlayerEnum

    inline fun getPlayer(player: PlayerEnum): PlayerStatus{
        return if(player ==  PlayerEnum.PLAYER1) player1 else player2
    }

    inline fun getSocket(player: PlayerEnum): Connection{
        return if(player ==  PlayerEnum.PLAYER1) player1_socket else player2_socket
    }

    fun setFirstTurn(player: PlayerEnum){
        first_turn = player
        when(player){
            PlayerEnum.PLAYER1 -> {
                player2.concentration = 1
            }
            PlayerEnum.PLAYER2 -> {
                player1.concentration = 1
            }
        }
    }


    fun addImmediateLifeListner(player: PlayerEnum, listner: ImmediateBackListner){
        when(player){
            PlayerEnum.PLAYER1 -> player1_life_listner.addLast(listner)
            PlayerEnum.PLAYER2 -> player2_life_listner.addLast(listner)
        }
    }

    //this three function is must check number before use
    suspend fun cardToDust(player: PlayerEnum, number: Int, card_name: CardName){
        dust += number

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_CARD, LocationEnum.DUST, number, card_name)
    }

    //this three function is must check number before use
    suspend fun auraToCard(player: PlayerEnum, number: Int, card: Card){
        var now_player = getPlayer(player)
        var value: Int

        if(now_player.aura >= number){
            value = number
            now_player.aura -= number
        }
        else{
            value = now_player.aura
            now_player.aura = 0
        }

        if(card.nap == null) card.nap = value
        else card.nap = card.nap!! + value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_AURA, LocationEnum.YOUR_CARD, value, card.card_data.card_name)
    }

    //this three function is must check number before use
    suspend fun dustToCard(player: PlayerEnum, number: Int, card: Card){
        var value: Int

        if(dust >= number){
            value = number
            dust -= number
        }
        else{
            value = dust
            dust = 0
        }

        if(card.nap == null) card.nap = value
        else card.nap = card.nap!! + value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.DUST, LocationEnum.YOUR_CARD, value, card.card_data.card_name)
    }

    suspend fun auraToDust(player: PlayerEnum, number: Int){
        var now_player = getPlayer(player)
        var value: Int

        if(now_player.aura >= number){
            value = number
            now_player.aura -= number
        }
        else{
            value = now_player.aura
            now_player.aura = 0
        }

        dust += value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_AURA, LocationEnum.DUST, value, null)
    }

    suspend fun flareToDust(player: PlayerEnum, number: Int){
        var now_player = getPlayer(player)
        var value: Int

        if(now_player.flare >= number){
            value = number
            now_player.flare -= number
        }
        else{
            value = now_player.flare
            now_player.flare = 0
        }

        dust += value

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_FLARE, LocationEnum.DUST, value, null)
    }


    //return endgame
    suspend fun lifeToSelfFlare(player: PlayerEnum, number: Int, reconstruct: Boolean): Boolean{
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player)

        val before = now_player.life

        if(now_player.life > number){
            now_player.life -= number
            now_player.flare += number
        }
        else{
            return true
        }

        if(!reconstruct){
            TODO("납 달린 부여패 파기")
        }

        sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_LIFE, LocationEnum.YOUR_FLARE, number, null)

        when(player){
            PlayerEnum.PLAYER1 -> {
                if(!player1_life_listner.isEmpty()){
                    for(i in 0..player1_life_listner.size){
                        val now = player1_life_listner.first()
                        player1_life_listner.removeFirst()
                        if(now.IsItBack(before, now_player.life, reconstruct)){
                            if(now_player.usedToSpecial(now.card_name)){
                                UsedCardReturn(player1_socket, now.card_name)
                            }
                        }
                        else{
                            player1_life_listner.addLast(now)
                        }
                    }
                }
            }
            PlayerEnum.PLAYER2 -> {
                if(!player2_life_listner.isEmpty()){
                    for(i in 0..player2_life_listner.size){
                        val now = player2_life_listner.first()
                        player2_life_listner.removeFirst()
                        if(now.IsItBack(before, now_player.life, reconstruct)){
                            if(now_player.usedToSpecial(now.card_name)){
                                UsedCardReturn(player2_socket, now.card_name)
                            }
                        }
                        else{
                            player2_life_listner.addLast(now)
                        }
                    }
                }
            }
        }

        return false
    }

    suspend fun dustToAura(player: PlayerEnum, number: Int){
        val now_player = getPlayer(player)
        var value: Int
        if(number > dust){
            value = now_player.plusAura(dust)
            dust -= value
        }
        else{
            value = now_player.plusAura(number)
            dust -= value
        }
        sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.DUST, LocationEnum.YOUR_AURA, value, null)
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
        val now_cost = cost

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
                buff.effect(now_cost)
            }
        }

        return now_cost
    }

    fun applyAllAttackBuff(player: PlayerEnum){
        val now_player = getPlayer(player)

        for(queue in now_player.attack_buf){
            var tempq: ArrayDeque<Buff> = ArrayDeque()
            for(i in queue.indices){
                val now = queue.first()
                queue.removeFirst()
                if(now.condition(player, this)){
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
                if(buff.condition(player, this)){
                    buff.counter *= -1
                    tempq.add(buff)
                }
            }
            for(buff in tempq){
                buff.effect(now_player.pre_attack_card!!)
            }
        }

        return now_player.pre_attack_card!!.rangeCheck(distance)
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

        if(attackCheck(player)){
            cleanRangeTempBuff(other_player.range_buf)
            cleanRangeBuff(now_player.range_buf)
            applyAllAttackBuff(player)
            return true
        }
        else{
            cleanAllBuff()
            return false
        }
    }
    fun getPlayerLife(player: PlayerEnum): Int{
        when (player){
            PlayerEnum.PLAYER1 -> return player1.life
            PlayerEnum.PLAYER2 -> return player2.life
        }
    }

    fun getPlayerAura(player: PlayerEnum): Int{
        when (player){
            PlayerEnum.PLAYER1 -> return player1.aura
            PlayerEnum.PLAYER2 -> return player2.aura
        }
    }

    fun getPlayerFullAction(player: PlayerEnum): Boolean{
        when (player){
            PlayerEnum.PLAYER1 -> return player1.full_action
            PlayerEnum.PLAYER2 -> return player2.full_action
        }
    }

    fun setPlayerFullAction(player: PlayerEnum, full: Boolean){
        when (player){
            PlayerEnum.PLAYER1 -> player1.full_action = full
            PlayerEnum.PLAYER2 -> player2.full_action = full
        }
    }

    fun getPlayerFlare(player: PlayerEnum): Int{
        when(player){
            PlayerEnum.PLAYER1 -> return player1.flare
            PlayerEnum.PLAYER2 -> return player2.flare
        }
    }

    fun getEndTurn(player: PlayerEnum): Boolean{
        when(player){
            PlayerEnum.PLAYER1 -> return player1.end_turn
            PlayerEnum.PLAYER2 -> return player2.end_turn
        }
    }

    fun setEndTurn(player: PlayerEnum, turn: Boolean){
        when(player){
            PlayerEnum.PLAYER1 -> player1.end_turn = turn
            PlayerEnum.PLAYER2 -> player2.end_turn = turn
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
        var now_player = getPlayer(player)

        var now_socket = getSocket(player)
        var other_socket = getSocket(player.Opposite())

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
        var now_player = getPlayer(player)

        var now_socket = getSocket(player)
        var other_socket = getSocket(player.Opposite())

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

    suspend fun afterMakeAttack(card_name: CardName, player: PlayerEnum, react_attack: MadeAttack?){
        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())
        val now_player = getPlayer(player)
        val other_player = getPlayer(player.Opposite())

        if(now_player.pre_attack_card == null){
            return
        }

        var now_attack = now_player.pre_attack_card!!

        makeAttackComplete(now_socket, other_socket, card_name)
        sendAttackInformation(now_socket, other_socket, now_attack.Information())
        if(!other_player.end_turn && react_attack == null){
            while(true){
                sendRequestReact(other_socket)
                val react = receiveReact(other_socket)
                if(react.first == CommandEnum.USE_CARD_IN_HAND){
                    var check_bit = true
                    other_player.getCardFromHand(react.second!!)?.let {
                        if(reactCheck(player.Opposite(), it, now_attack)){
                            val cost = it.canUse(player.Opposite(), this)
                            if(cost == -1){
                                check_bit = false
                                sendUseCardMeesage(other_socket, now_socket, true, it.card_data.card_name)
                                other_player.useCardFromHand(it.card_data.card_name)
                                it.use(player.Opposite(), this, now_attack)
                            }
                        }
                    }
                    if(check_bit){
                        continue
                    }
                }
                else if(react.first == CommandEnum.USE_CARD_IN_SPEICAL){
                    var check_bit = true
                    other_player.getCardFromSpecial(react.second!!)?.let {
                        if(reactCheck(player.Opposite(), it, now_attack)){
                            val cost = it.canUse(player.Opposite(), this)
                            if(cost >= 0){
                                check_bit = false
                                it.special_card_state = SpecialCardEnum.PLAYING
                                sendUseCardMeesage(other_socket, now_socket, true, it.card_data.card_name)
                                flareToDust(player.Opposite(), cost)
                                other_player.useCardFromSpecial(it.card_data.card_name)
                                cleanAfterUseCost()
                                it.use(player.Opposite(), this, now_attack)
                            }
                        }
                    }
                    if(check_bit){
                        continue
                    }
                }
                else{
                    break
                }
            }
        }

        if(now_attack.is_it_valid){
            sendChooseDamage(other_socket, CommandEnum.CHOOSE_CARD_DAMAGE, now_attack.aura_damage, now_attack.life_damage)
            val chosen = receiveChooseDamage(other_socket)
            processDamage(player, chosen, Pair(now_attack.aura_damage, now_attack.life_damage), false)
            now_attack.afterAttackProcess(player, this, react_attack)
        }
    }

    suspend fun processTextDestruction(card_name: CardName, player: PlayerEnum, text: Text){
        when(text.tag){
            TextEffectTag.MAKE_ATTACK -> {
                text.effect!!(player, this, null)
                afterMakeAttack(card_name, player, null)
            }
            else -> {}
        }
    }

    suspend fun afterCardUsed(player: PlayerEnum, card: Card){
        val playing_player = getPlayer(player)
        playing_player.using_card.removeLast()

        if(card.card_data.card_type == CardType.ENCHANTMENT){
            val playing_socket = getSocket(player)
            val not_playing_socket = getSocket(player.Opposite())
            playing_player.enchantment_card[card.card_data.card_name] = card
            sendEnchantmentZone(playing_socket, not_playing_socket, card.card_data.card_name)
        }
        else{
            val owner_player = getPlayer(card.player)
            val owner_socket = getSocket(card.player)
            val not_owner_socket = getSocket(card.player.Opposite())
            when(card.card_data.card_class){
                CardClass.SPECIAL -> {
                    card.special_card_state = SpecialCardEnum.PLAYED
                    owner_player.used_special_card.addLast(card)
                    sendUsedZone(owner_socket, not_owner_socket, card.card_data.card_name)
                }
                CardClass.NORMAL -> {
                    owner_player.discard.addLast(card)
                    sendDiscardZone(owner_socket, not_owner_socket, card.card_data.card_name)
                }
            }
        }
    }

    suspend fun afterDestruction(card: Card){
        val owner_player = getPlayer(card.player)
        val owner_socket = getSocket(card.player)
        val not_owner_socket = getSocket(card.player.Opposite())
        card.nap = null
        when(card.card_data.card_class){
            CardClass.SPECIAL -> {
                card.special_card_state = SpecialCardEnum.PLAYED
                owner_player.used_special_card.addLast(card)
                sendUsedZone(owner_socket, not_owner_socket, card.card_data.card_name)
            }
            CardClass.NORMAL -> {
                owner_player.discard.addLast(card)
                sendDiscardZone(owner_socket, not_owner_socket, card.card_data.card_name)
            }
        }
    }

    suspend fun enchantmentDestruction(player: PlayerEnum, card: Card){
        val effects = card.destructionEnchantmentNormaly()
        for(i in effects){
            processTextDestruction(card.card_data.card_name, player, i)
        }

        when(player){
            PlayerEnum.PLAYER1 -> {
                player1.enchantment_card.remove(card.card_data.card_name)
            }
            PlayerEnum.PLAYER2 -> {
                player2.enchantment_card.remove(card.card_data.card_name)
            }
        }

        afterDestruction(card)
    }

    suspend fun enchantmentReduceAll(player: PlayerEnum){
        TODO("중복 카드 존재 가능성으로 인한, 카드넘버 방식의 납 해결")

//        if(player1.enchantment_card.isEmpty() && player2.enchantment_card.isEmpty()){
//            return
//        }
//
//        sendReduceNapStart(player1_socket)
//        sendReduceNapStart(player2_socket)
//
//        var player1_card: HashMap<CardName, Boolean> = HashMap()
//        var player2_card: HashMap<CardName, Boolean> = HashMap()



//        for(i in player1.enchantment_card){
//            val nap = i.value.reduceNapNormaly()
//            if(nap >= 1){
//                cardToDust(PlayerEnum.PLAYER1, nap, i.key)
//            }
//            if(i.value.isItDestruction()){
//                player1_card[i.key] = true
//            }
//        }
//        for(i in player2.enchantment_card){
//            val nap = i.value.reduceNapNormaly()
//            if(nap >= 1){
//                cardToDust(PlayerEnum.PLAYER2, nap, i.key)
//            }
//            if(i.value.isItDestruction()){
//                player2_card[i.key] = true
//            }
//        }

//        sendReduceNapEnd(player1_socket)
//        sendReduceNapEnd(player2_socket)
//
//        if(player1_card.isEmpty() && player2_card.isEmpty()){
//            return
//        }
//
//        when(player){
//            PlayerEnum.PLAYER1 -> {
//                sendStartSelectEnchantment(player1_socket)
//                sendRequestEnchantmentCard(player1_socket, player1_card.keys.toMutableList(), player2_card.keys.toMutableList())
//                while(true){
//                    val receive = receiveEnchantment(player1_socket)
//                    when(receive.first){
//                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
//                            if(player1_card.containsKey(receive.second)){
//                                val card = player1.enchantment_card[receive.second]
//                                sendDestructionEnchant(player1_socket, player2_socket, receive.second!!)
//                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
//                            }
//                        }
//                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
//                            if(player2_card.containsKey(receive.second)){
//                                val card = player2.enchantment_card[receive.second]
//                                sendDestructionEnchant(player2_socket, player1_socket, receive.second!!)
//                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
//                            }
//                        }
//                        CommandEnum.SELECT_ENCHANTMENT_END -> {
//                            break
//                        }
//                        else -> {}
//                    }
//                }
//            }
//            PlayerEnum.PLAYER2 -> {
//                sendStartSelectEnchantment(player2_socket)
//                sendRequestEnchantmentCard(player2_socket, player2_card.keys.toMutableList(), player1_card.keys.toMutableList())
//                while(true){
//                    val receive = receiveEnchantment(player2_socket)
//                    when(receive.first){
//                        CommandEnum.SELECT_ENCHANTMENT_YOUR -> {
//                            if(player2_card.containsKey(receive.second)){
//                                val card = player2.enchantment_card[receive.second]
//                                sendDestructionEnchant(player2_socket, player1_socket, receive.second!!)
//                                enchantmentDestruction(PlayerEnum.PLAYER2, card!!)
//                            }
//                        }
//                        CommandEnum.SELECT_ENCHANTMENT_OTHER -> {
//                            if(player1_card.containsKey(receive.second)){
//                                val card = player1.enchantment_card[receive.second]
//                                sendDestructionEnchant(player1_socket, player2_socket, receive.second!!)
//                                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
//                            }
//                        }
//                        CommandEnum.SELECT_ENCHANTMENT_END -> {
//                            break
//                        }
//                        else -> {}
//                    }
//                }
//            }
//        }
//
//        if(!player1_card.isEmpty()){
//            for(card_name in player1_card.keys){
//                val card = player1.enchantment_card[card_name]
//                sendDestructionEnchant(player1_socket, player2_socket, card_name)
//                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
//            }
//        }
//
//        if(!player2_card.isEmpty()){
//            for(card_name in player2_card.keys){
//                val card = player2.enchantment_card[card_name]
//                sendDestructionEnchant(player1_socket, player2_socket, card_name)
//                enchantmentDestruction(PlayerEnum.PLAYER1, card!!)
//            }
//        }
    }

    suspend fun gameEnd(winner: PlayerEnum){
        val winner_socket = getSocket(winner)
        val roser_socket = getSocket(winner.Opposite())

        sendGameEnd(winner_socket, roser_socket)

        player1_socket.session.close()
        player2_socket.session.close()
    }

    suspend fun processDamage(player: PlayerEnum, command: CommandEnum, damage: Pair<Int, Int>, reconstruct: Boolean){
        val now_player = getPlayer(player)
        if(command == CommandEnum.CHOOSE_AURA){
            if(damage.first == 999){
                processDamage(player, CommandEnum.CHOOSE_LIFE, damage, reconstruct)
            }
            else{
                if(now_player.aura < damage.first){
                    processDamage(player, CommandEnum.CHOOSE_LIFE, damage, reconstruct)
                }
                else{
                    auraToDust(player, damage.first)
                }
            }
        }
        else{
            if(damage.second == 999){
                processDamage(player, CommandEnum.CHOOSE_AURA, damage, reconstruct)
            }
            else{
                if(lifeToSelfFlare(player, damage.second, reconstruct)) {
                    gameEnd(player)
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
                sendChooseDamage(player1_socket, CommandEnum.CHOOSE_CHOJO, 1, 1)
                val chosen = receiveChooseDamage(now_socket)
                processDamage(player, chosen, Pair(1, 1), false)
                continue
            }
            sendDrawCard(now_socket, other_socket, now_player.normal_card_deck.first().card_data.card_name)
            now_player.hand.add(now_player.normal_card_deck.first())
            now_player.normal_card_deck.removeFirst()
        }
    }

    suspend fun insertHandToDeck(player: PlayerEnum, card_name: CardName): Boolean{
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        for(i in now_player.hand.indices){
            if(now_player.hand[i].card_data.card_name == card_name){
                now_player.normal_card_deck.addLast(now_player.hand[i])
                now_player.hand.removeAt(i)
                sendHandToDeck(now_socket, other_socket, card_name, false)
                return true
            }
        }

        return false
    }

    suspend fun startPhaseEffectProcess(){
        //TODO("BY RULE 8-1-2")
    }

    suspend fun mainPhaseEffectProcess(){
        //TODO("BY RULE 8-2-2")
    }

    suspend fun endPhaseEffectProcess(){
        //TODO("BY RULE 8-3-1")
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

    suspend fun cardUseNormaly(player: PlayerEnum, commandEnum: CommandEnum, card_name: CardName?): Boolean{
        if(card_name == null){
            return false
        }

        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        var using_successly = false

        if(commandEnum == CommandEnum.ACTION_USE_CARD_HAND){
            now_player.getCardFromHand(card_name)?.let{
                val cost = it.canUse(player, this)
                if(cost == -1){
                    using_successly = true
                    sendUseCardMeesage(now_socket, other_socket, false, it.card_data.card_name)
                    now_player.useCardFromHand(it.card_data.card_name)
                    it.use(player, this, null)
                }
            }
        }
        else if(commandEnum == CommandEnum.ACTION_USE_CARD_SPECIAL){
            now_player.getCardFromSpecial(card_name)?.let {
                val cost = it.canUse(player, this)
                if(cost >= 0){
                    using_successly = true
                    it.special_card_state = SpecialCardEnum.PLAYING
                    sendUseCardMeesage(now_socket, other_socket,false, it.card_data.card_name)
                    flareToDust(player.Opposite(), cost)
                    now_player.useCardFromSpecial(it.card_data.card_name)
                    cleanAfterUseCost()
                    it.use(player.Opposite(), this, null)
                }
            }
        }
        else{
            return false
        }

        return using_successly
    }

    suspend fun basicOperationCost(player: PlayerEnum, card_name: CardName): Boolean{
        val now_player = getPlayer(player)

        if(card_name == CardName.CARD_UNNAME){
            if(now_player.concentration == 0) return false
            else {
                decreaseConcentration(player)
                return true
            }
        }
        else if(now_player.fromHandToCover(card_name)){
            sendHandToCover(getSocket(player), getSocket(player.Opposite()), card_name, false)
            return true
        }
        else{
            return false
        }
    }

    fun canDoBasicOperation(player: PlayerEnum, command: CommandEnum): Boolean{
        val now_player = getPlayer(player)
        when(command){
            CommandEnum.ACTION_GO_FORWARD -> return !(now_player.aura == now_player.max_aura || distance_token == 0 || distance <= swell_distance)
            CommandEnum.ACTION_GO_BACKWARD -> return !(now_player.aura == 0 || distance_token == 10)
            CommandEnum.ACTION_WIND_AROUND -> return !(dust == 0 || now_player.aura == now_player.max_aura)
            CommandEnum.ACTION_INCUBATE -> return now_player.aura != 0
            CommandEnum.ACTION_BREAK_AWAY -> return !(dust == 0 || distance > swell_distance || distance_token == 10)
            else -> return false
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

        if(now_player.aura == now_player.max_aura || distance_token == 0 || distance <= swell_distance) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_GO_FORWARD_YOUR)
            distance_token -= 1
            distance -= 1
            if(distance < 0){
                distance = 0
            }
            now_player.aura += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.DISTANCE, LocationEnum.YOUR_AURA, 1, null)
        }
    }

    //this 5 function must call after check when select
    suspend fun doGoBackward(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(now_player.aura == 0 || distance_token == 10) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_GO_BACKWARD_YOUR)
            now_player.aura -= 1
            distance_token += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_AURA, LocationEnum.DISTANCE, 1, null)
        }
    }

    //this 5 function must call after check when select
    suspend fun doWindAround(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(dust == 0 || now_player.aura == now_player.max_aura) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_WIND_AROUND_YOUR)
            dust -= 1
            now_player.aura += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.DUST, LocationEnum.YOUR_AURA, 1, null)
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
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, 1, null)
        }
    }

    //this 5 function must call after check when select
    suspend fun doBreakAway(player: PlayerEnum){
        val now_socket = getSocket(player)
        val other_socket = getSocket(player.Opposite())

        if(dust == 0 || distance > swell_distance || distance_token == 10) return
        else{
            sendDoBasicAction(now_socket, other_socket, CommandEnum.ACTION_BREAK_AWAY_YOUR)
            dust -= 1
            distance_token += 1
            distance += 1
            sendMoveToken(getSocket(player), getSocket(player.Opposite()), LocationEnum.YOUR_AURA, LocationEnum.YOUR_FLARE, 1, null)
        }
    }

    suspend fun endTurnHandCheck(player: PlayerEnum){
        val now_player = getPlayer(player)

        val now_socket = getSocket(player)

        while (true){
            if(now_player.hand.size <= now_player.max_hand){
                return
            }

            for(card in now_player.hand){
                if(card.card_data.can_cover){
                    var card_name = receiveCoverCardSelect(now_socket)
                    if(now_player.fromHandToCover(card_name)){
                        sendHandToCover(getSocket(player), getSocket(player.Opposite()), card_name, false)
                    }
                    break
                }
            }
        }
    }
}