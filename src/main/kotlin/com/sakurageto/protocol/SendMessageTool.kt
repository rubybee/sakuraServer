package com.sakurageto.protocol

import com.sakurageto.Connection
import com.sakurageto.card.toPrivate
import com.sakurageto.gamelogic.megamispecial.Stratagem
import com.sakurageto.protocol.CommandEnum.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}



//send function
suspend fun sendReduceNapStart(player: Connection){
    val data = SakuraBaseData(REDUCE_NAP_START, -1)
    player.send(Json.encodeToString(data))
}

suspend fun sendReduceNapEnd(player: Connection){
    val data = SakuraBaseData(REDUCE_NAP_END, -1)
    player.send(Json.encodeToString(data))
}

suspend fun sendStartSelectEnchantment(player: Connection){
    val data = SakuraBaseData(SELECT_ENCHANTMENT_START, -1)
    player.send(Json.encodeToString(data))
}

suspend fun sendRequestEnchantmentCard(player: Connection, card_list_your: MutableList<Int>, card_list_other: MutableList<Int>){
    val dataYourPre = SakuraBaseData(SELECT_ENCHANTMENT_YOUR, -1)
    val dataOtherPre = SakuraBaseData(SELECT_ENCHANTMENT_OTHER, -1)
    val dataYour = SakuraArrayData(SELECT_ENCHANTMENT_YOUR, card_list_your)
    val dataOther = SakuraArrayData(SELECT_ENCHANTMENT_OTHER, card_list_other)
    player.send(Json.encodeToString(dataYourPre))
    player.send(Json.encodeToString(dataYour))
    player.send(Json.encodeToString(dataOtherPre))
    player.send(Json.encodeToString(dataOther))
}
suspend fun sendDestructionEnchant(mine: Connection, other: Connection, card_number: Int){
    val dataYour = SakuraBaseData(DESTRUCTION_ENCHANTMENT_YOUR, card_number)
    val dataOther = SakuraBaseData(DESTRUCTION_ENCHANTMENT_OTHER, card_number)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendPopCardZone(mine: Connection, other: Connection, card_number: Int, public: Boolean, command: CommandEnum){
    val dataYour = SakuraBaseData(command, card_number)
    val dataOther = if(public) SakuraBaseData(command.oppositeCommand(), card_number)
    else SakuraBaseData(command.oppositeCommand(), card_number.toPrivate())
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendAddCardZone(mine: Connection, other: Connection, card_number: Int, publicForOther: Boolean, command: CommandEnum, publicForYour: Boolean = true){
    val dataYour = if(publicForYour)SakuraBaseData(command, card_number)
    else SakuraBaseData(command, card_number.toPrivate())
    val dataOther = if(publicForOther) SakuraBaseData(command.oppositeCommand(), card_number)
    else SakuraBaseData(command.oppositeCommand(), card_number.toPrivate())
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun makeAttackComplete(mine: Connection, other: Connection, card_number: Int){
    val dataYour = SakuraBaseData(MAKE_ATTACK_COMPLETE_YOUR, card_number)
    val dataOther = SakuraBaseData(MAKE_ATTACK_COMPLETE_OTHER, card_number)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendAttackInformation(mine: Connection, other: Connection, data: MutableList<Int>) {
    val dataYour = SakuraArrayData(ATTACK_INFORMATION_YOUR, data)
    val dataOther = SakuraArrayData(ATTACK_INFORMATION_OTHER, data)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendChooseDamage(mine: Connection, command: CommandEnum, aura: Int, life: Int){
    val preData = SakuraBaseData(CHOOSE_DAMAGE, -1)
    val data = SakuraArrayData(command, mutableListOf(aura, life))
    mine.send(Json.encodeToString(preData))
    mine.send(Json.encodeToString(data))
}
suspend fun sendRequestReact(mine: Connection){
    val data = SakuraBaseData(REACT_REQUEST, -1)
    mine.send(Json.encodeToString(data))
}

/**
 card_number2 only used for two card is needed (only for card to card)
 */
suspend fun sendMoveToken(mine: Connection, other: Connection, what: TokenEnum, from: LocationEnum, to: LocationEnum, number: Int, card_number: Int, card_number2: Int = -1){
    if(number <= 0) return
    val preData = SakuraBaseData(MOVE_TOKEN, card_number)
    mine.send(Json.encodeToString(preData))
    other.send(Json.encodeToString(preData))
    val dataYour = SakuraArrayData(MOVE_TOKEN, mutableListOf(what.real_number, from.real_number, to.real_number, number, card_number, card_number2))
    val dataOther = SakuraArrayData(MOVE_TOKEN, mutableListOf(what.opposite().real_number, from.oppositeLocation().real_number, to.oppositeLocation().real_number, number, card_number, card_number2))
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendAddConcentration(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(ADD_CONCENTRATION_YOUR, -1)
    val dataOther = SakuraBaseData(ADD_CONCENTRATION_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendDecreaseConcentration(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(DECREASE_CONCENTRATION_YOUR, -1)
    val dataOther = SakuraBaseData(DECREASE_CONCENTRATION_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendUseCardMeesage(mine: Connection, other: Connection, reaction: Boolean, card_number: Int){
    val dataYour = if(reaction) SakuraBaseData(USE_CARD_YOUR_REACTION, card_number) else SakuraBaseData(
        USE_CARD_YOUR, card_number)
    val dataOther = if(reaction) SakuraBaseData(USE_CARD_OTHER_REACTION, card_number) else SakuraBaseData(
        USE_CARD_OTHER, card_number)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendSetConcentration(mine: Connection, other: Connection, number: Int){
    val dataYour = SakuraBaseData(SET_CONCENTRATION_YOUR, number)
    val dataOther = SakuraBaseData(SET_CONCENTRATION_OTHER, number)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendSetShrink(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(SET_SHRINK_YOUR, -1)
    val dataOther = SakuraBaseData(SET_SHRINK_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendRemoveShrink(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(REMOVE_SHRINK_YOUR, -1)
    val dataOther = SakuraBaseData(REMOVE_SHRINK_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendHandToDeck(mine: Connection, other: Connection, card_number: Int, public: Boolean, below: Boolean){
    val dataYour = SakuraBaseData(if (below) CARD_HAND_TO_DECK_BELOW_YOUR else CARD_HAND_TO_DECK_UPPER_YOUR, card_number)
    val dataOther = if(public) SakuraBaseData(if (below) CARD_HAND_TO_DECK_BELOW_OTHER else CARD_HAND_TO_DECK_UPPER_OTHER, card_number)
    else SakuraBaseData(if (below) CARD_HAND_TO_DECK_BELOW_OTHER else CARD_HAND_TO_DECK_UPPER_OTHER, card_number.toPrivate())
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendDrawCard(mine: Connection, other: Connection, card_number: Int){
    val dataYour = SakuraBaseData(DRAW_CARD_YOUR, card_number)
    val dataOther = SakuraBaseData(DRAW_CARD_OTHER, card_number.toPrivate())
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendMuligunEnd(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(MULIGUN_END, -1)
    val dataOther = SakuraBaseData(MULIGUN_END, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendStartPhaseStart(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(START_START_PHASE_YOUR, -1)
    val dataOther = SakuraBaseData(START_START_PHASE_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendDeckReconstruct(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(DECK_RECONSTRUCT_YOUR, -1)
    val dataOther = SakuraBaseData(DECK_RECONSTRUCT_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendMainPhaseStart(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(START_MAIN_PHASE_YOUR, -1)
    val dataOther = SakuraBaseData(START_MAIN_PHASE_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendEndPhaseStart(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(START_END_PHASE_YOUR, -1)
    val dataOther = SakuraBaseData(START_END_PHASE_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendActionRequest(mine: Connection){
    val dataYour = SakuraBaseData(ACTION_REQUEST, -1)
    mine.send(Json.encodeToString(dataYour))
}

suspend fun sendDoBasicAction(mine: Connection, other: Connection, command: CommandEnum, card: Int){
    val dataYour = SakuraBaseData(command, if(card == -1) -1 else if(card >= 200000) card - 200000 else card)
    val dataOther = SakuraBaseData(command.oppositeCommand(), if(card == -1) -1 else if(card >= 200000) card - 200000 else 0)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendGameEnd(winner: Connection, loser: Connection){
    val dataWinner = SakuraBaseData(GAME_END_WINNER, -1)
    val dataLoser = SakuraBaseData(GAME_END_LOSER, -1)
    winner.send(Json.encodeToString(dataWinner))
    loser.send(Json.encodeToString(dataLoser))
}

suspend fun sendCardEffectOrder(player: Connection, command: CommandEnum, list: MutableList<Int>){
    val preData = SakuraBaseData(command, -1)
    val data = SakuraArrayData(command, list)
    player.send(Json.encodeToString(preData))
    player.send(Json.encodeToString(data))
}

suspend fun sendCoverCardSelect(player: Connection, list: MutableList<Int>, reason: Int){
    val preData = SakuraBaseData(COVER_CARD_SELECT, reason)
    val data = SakuraArrayData(COVER_CARD_SELECT, list)
    player.send(Json.encodeToString(preData))
    player.send(Json.encodeToString(data))
}

suspend fun sendCardEffectSelect(player: Connection, card_number: Int, command: CommandEnum = SELECT_CARD_EFFECT){
    val data = SakuraBaseData(command, card_number)
    player.send(Json.encodeToString(data))
}

suspend fun sendAuraDamageSelect(player: Connection, auraDamage: Int){
    val data = SakuraBaseData(SELECT_AURA_DAMAGE_PLACE, auraDamage)
    player.send(Json.encodeToString(data))
}

suspend fun sendAuraDamagePlaceInformation(player: Connection, list: MutableList<Int>){
    val data = SakuraArrayData(SELECT_AURA_DAMAGE_PLACE, list)
    player.send(Json.encodeToString(data))
}

suspend fun sendPreCardSelect(player: Connection, reason: CommandEnum, card_number: Int){
    val data = SakuraBaseData(reason, card_number)
    player.send(Json.encodeToString(data))
}

suspend fun sendSimpleSakuraData(player: Connection, list: MutableList<Int>, reason: CommandEnum){
    val data = SakuraArrayData(reason, list)
    player.send(Json.encodeToString(data))
}

suspend fun sendShowInformation(command: CommandEnum, show_player: Connection, look_player: Connection, list: MutableList<Int>){
    val preDataShow = SakuraBaseData(command)
    val preDataLook = SakuraBaseData(command.oppositeCommand())
    val data = SakuraArrayData(command.oppositeCommand(), list)
    show_player.send(Json.encodeToString(preDataShow))
    look_player.send(Json.encodeToString(preDataLook))
    look_player.send(Json.encodeToString(data))
}

suspend fun sendChangeUmbrella(mine: Connection, other: Connection){
    val dataYour = SakuraBaseData(CHANGE_UMBRELLA_YOUR, -1)
    val dataOther = SakuraBaseData(CHANGE_UMBRELLA_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendSetStratagem(mine: Connection, other: Connection, stratagem: Stratagem){
    val dataYour = SakuraBaseData(STRATAGEM_SET_YOUR, if(stratagem == Stratagem.SHIN_SAN) 0 else 1)
    val dataOther = SakuraBaseData(STRATAGEM_SET_OTHER, -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendGetStratagem(mine: Connection, other: Connection, stratagem: Stratagem){
    val dataYour = SakuraBaseData(STRATAGEM_GET_YOUR, if(stratagem == Stratagem.SHIN_SAN) 0 else 1)
    val dataOther = SakuraBaseData(STRATAGEM_GET_OTHER, if(stratagem == Stratagem.SHIN_SAN) 0 else 1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendRequestBasicOperation(mine: Connection, card_number: Int){
    val data = SakuraBaseData(REQUEST_BASIC_OPERATION, card_number)
    mine.send(Json.encodeToString(data))
}

suspend fun sendSimpleCommand(mine: Connection, command: CommandEnum){
    val dataYour = SakuraBaseData(command, -1)
    mine.send(Json.encodeToString(dataYour))
}

suspend fun sendSimpleCommand(mine: Connection, command: CommandEnum, card_number: Int){
    val dataYour = SakuraBaseData(command, card_number)
    mine.send(Json.encodeToString(dataYour))
}

suspend fun sendSimpleCommand(mine: Connection, other: Connection, command: CommandEnum){
    val dataYour = SakuraBaseData(command, -1)
    val dataOther = SakuraBaseData(command.oppositeCommand(), -1)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

suspend fun sendSimpleCommand(mine: Connection, other: Connection, command: CommandEnum, subNumber: Int){
    val dataYour = SakuraBaseData(command, subNumber)
    val dataOther = SakuraBaseData(command.oppositeCommand(), subNumber)
    mine.send(Json.encodeToString(dataYour))
    other.send(Json.encodeToString(dataOther))
}

//receive function
suspend fun receiveSimpleCommand(player: Connection, command: CommandEnum): CommandEnum{
    sendSimpleCommand(player, command)
    return receiveSimpleCommandMain(player)
}

suspend fun receiveSimpleCommandMain(player: Connection): CommandEnum{
    while (true) {
        try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            return data.command
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveSakuraSendData(player: Connection, wait_command: CommandEnum): SakuraArrayData {
    while (true) {
        try {
            val response = json.decodeFromString<SakuraArrayData>(player.receive())
            if (response.command == wait_command) {
                return response
            }
        } catch (e: Exception) {
            continue
        }
    }
}

suspend fun receiveSakuraCardSet(player: Connection, wait_command: CommandEnum): SakuraCardSetData {
    while (true){
        try{
            val response = json.decodeFromString<SakuraCardSetData>(player.receive())
            if (response.command == wait_command){
                return response
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveSakuraCardCommand(player: Connection, wait_command: CommandEnum): SakuraBaseData {
    while (true){
        try{
            val response = json.decodeFromString<SakuraBaseData>(player.receive())
            if (response.command == wait_command){
                return response
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveEnchantment(player: Connection): Pair<CommandEnum, Int> {
    while (true){
        try {
            val response = json.decodeFromString<SakuraBaseData>(player.receive())
            if (response.command == SELECT_ENCHANTMENT_OTHER || response.command == SELECT_ENCHANTMENT_YOUR){
                return Pair(response.command, response.card)
            }
            else if(response.command == SELECT_ENCHANTMENT_END){
                return Pair(response.command, -1)
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveReact(player: Connection): Pair<CommandEnum, Int> {
    while (true){
        try {
            val response = json.decodeFromString<SakuraBaseData>(player.receive())
            if (response.command in CommandEnum.reactCommandSet){
                return Pair(response.command, response.card)
            }
            else if(response.command == REACT_NO){
                return Pair(response.command, -1)
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveChooseDamage(player: Connection): CommandEnum {
    while (true){
        try {
            val response = json.decodeFromString<SakuraBaseData>(player.receive())
            if (response.command == CHOOSE_AURA || response.command == CHOOSE_LIFE){
                return response.command
            }
            else {
                continue
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveNapInformation(player: Connection, total: Int, card_number: Int, command: CommandEnum): Pair<Int, Int> {
    player.send(Json.encodeToString(SakuraBaseData(command, card_number)))
    player.send(Json.encodeToString(SakuraArrayData(command, mutableListOf(total))))
    return receiveNapInformationMain(player, command)
}

suspend fun receiveNapInformationMain(player: Connection, command: CommandEnum): Pair<Int, Int> {
    while (true){
        try {
            val data = json.decodeFromString<SakuraArrayData>(player.receive())
            if (data.command == command){
                data.data?.let {
                    if(it.size >= 2){
                        return(Pair(it[0], it[1]))
                    }
                }
            }
            else {
                continue
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveReconstructRequest(player: Connection): Boolean{
    player.send(Json.encodeToString(SakuraBaseData(DECK_RECONSTRUCT_REQUEST, -1)))
    return receiveReconstructRequestMain(player)
}

suspend fun receiveReconstructRequestMain(player: Connection): Boolean{
    while (true){
        return try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            if (data.command == DECK_RECONSTRUCT_NO) {
                false
            } else if (data.command == DECK_RECONSTRUCT_YES) {
                true
            } else {
                continue
            }
        } catch (e: Exception) {
            continue
        }
    }
}

suspend fun receiveFullPowerRequest(player: Connection): Boolean{
    player.send(Json.encodeToString(SakuraBaseData(FULL_POWER_REQUEST, -1)))
    return receiveFullPowerRequestMain(player)
}

suspend fun receiveFullPowerRequestMain(player: Connection): Boolean {
    while (true) {
        return try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            if (data.command == FULL_POWER_NO) {
                false
            } else if (data.command == FULL_POWER_YES) {
                true
            } else {
                continue
            }
        } catch (e: Exception) {
            continue
        }
    }
}

suspend fun receiveFullPowerActionRequest(player: Connection): Pair<CommandEnum, Int>{
    sendActionRequest(player)
    return receiveFullPowerActionRequestMain(player)
}

suspend fun receiveFullPowerActionRequestMain(player: Connection): Pair<CommandEnum, Int>{
    while (true) {
        try{
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            return Pair(data.command, data.card)
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveActionRequest(player: Connection): Pair<CommandEnum, Int>{
    sendActionRequest(player)
    return receiveActionRequestMain(player)
}

suspend fun receiveActionRequestMain(player: Connection): Pair<CommandEnum, Int>{
    while (true) {
        try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            return Pair(data.command, data.card)
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveCardEffectOrder(player: Connection, command: CommandEnum, list: MutableList<Int>): Int{
    sendCardEffectOrder(player, command, list)
    return receiveSakuraCardCommand(player, command).card
}

suspend fun receiveCoverCardSelect(player: Connection, list: MutableList<Int>, reason: Int): Int{
    sendCoverCardSelect(player, list, reason)
    return receiveSakuraCardCommand(player, COVER_CARD_SELECT).card
}

suspend fun receiveCardEffectSelect(player: Connection, card_number: Int, command: CommandEnum = SELECT_CARD_EFFECT): CommandEnum{
    sendCardEffectSelect(player, card_number, command)
    return receiveCardEffectSelectMain(player)
}

suspend fun receiveCardEffectSelectMain(player: Connection): CommandEnum{
    while (true) {
        try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            if(data.command in CommandEnum.cardEffectSelectCommandSet){
                return data.command
            }
            else{
                continue
            }
        }catch (e: Exception){
            continue
        }
    }
}

//receive data like( [LOCATION_ENUM.AURA, 3, CARD_NUMBER, 2, CARD_NUMBER, 2] )
suspend fun receiveAuraDamageSelect(player: Connection, place_list: MutableList<Int>, auraDamage: Int): MutableList<Int>?{
    sendAuraDamageSelect(player, auraDamage)
    sendAuraDamagePlaceInformation(player, place_list)
    return receiveSakuraSendData(player, SELECT_AURA_DAMAGE_PLACE).data
}

suspend fun receiveSelectCard(player: Connection, card_list: MutableList<Int>, reason: CommandEnum, card_number: Int): MutableList<Int>{
    sendPreCardSelect(player, reason, card_number)
    sendSimpleSakuraData(player, card_list, reason)
    return receiveSelectCardMain(player, card_list, reason, card_number)
}

suspend fun receiveSelectCardMain(player: Connection, card_list: MutableList<Int>, reason: CommandEnum, card_number: Int): MutableList<Int>{
    while (true) {
        try {
            val data = json.decodeFromString<SakuraArrayData>(player.receive())
            if(data.command == reason){
                if(data.data?.all { card_list.contains(it) } == true){
                    return data.data
                }
                sendPreCardSelect(player, reason, card_number)
                sendSimpleSakuraData(player, card_list, reason)
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveBasicOperation(player: Connection, card_number: Int): CommandEnum{
    sendRequestBasicOperation(player, card_number)
    return receiveBasicOperationMain(player)
}

suspend fun receiveBasicOperationMain(player: Connection): CommandEnum{
    while (true) {
        return try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            data.command
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveSelectAct(player: Connection, act_list: MutableList<Int>): Int{
    sendSimpleCommand(player, SELECT_ACT)
    sendSimpleSakuraData(player, act_list, SELECT_ACT)
    return receiveSelectActMain(player, act_list, SELECT_ACT)
}

suspend fun receiveSelectActMain(player: Connection, act_list: MutableList<Int>, reason: CommandEnum): Int{
    while (true) {
        try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            if(data.command == reason){
                if(data.card in act_list) return data.card
                sendSimpleCommand(player, SELECT_ACT)
                sendSimpleSakuraData(player, act_list, SELECT_ACT)
            }
        }catch (e: Exception){
            continue
        }
    }
}

suspend fun receiveSelectDisprove(player: Connection, card_number: Int): CommandEnum{
    sendSimpleCommand(player, CHOOSE_DISPROVE, card_number)
    return receiveSelectDisproveMain(player, card_number)
}

suspend fun receiveSelectDisproveMain(player: Connection, card_number: Int): CommandEnum{
    while (true) {
        try {
            val data = json.decodeFromString<SakuraBaseData>(player.receive())
            if(data.command == SELECT_ONE || data.command == SELECT_NOT){
                return data.command
            }
            sendSimpleCommand(player, CHOOSE_DISPROVE, card_number)
        }catch (e: Exception){
            continue
        }
    }
}



