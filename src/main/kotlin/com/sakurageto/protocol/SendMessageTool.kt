package com.sakurageto.protocol

import com.sakurageto.Connection
import com.sakurageto.card.CardName
import com.sakurageto.card.PlayerEnum
import com.sakurageto.protocol.CommandEnum.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.Exception
import kotlin.contracts.contract

//send function
suspend fun sendUsedCardReturn(player: Connection, card_number: Int) {
    val data = SakuraCardCommand(RETURN_SPECIAL_CARD, card_number)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendReduceNapStart(player: Connection){
    val data = SakuraCardCommand(REDUCE_NAP_START, -1)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendReduceNapEnd(player: Connection){
    val data = SakuraCardCommand(REDUCE_NAP_END, -1)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendStartSelectEnchantment(player: Connection){
    val data = SakuraCardCommand(SELECT_ENCHANTMENT_START, -1)
    player.session.send(Json.encodeToString(data))
}

suspend fun sendRequestEnchantmentCard(player: Connection, card_list_your: MutableList<Int>, card_list_other: MutableList<Int>){
    val data_your = SakuraSendData(SELECT_ENCHANTMENT_YOUR, card_list_your)
    val data_other = SakuraSendData(SELECT_ENCHANTMENT_OTHER, card_list_other)
    player.session.send(Json.encodeToString(data_your))
    player.session.send(Json.encodeToString(data_other))
}

suspend fun sendDestructionNotNormal(mine: Connection, other: Connection, card_number: Int){
    val data_your = SakuraCardCommand(DESTRUCTION_NOT_NORMALY_ENCHANTENT_YOUR, card_number)
    val data_other = SakuraCardCommand(DESTRUCTION_NOT_NORMALY_ENCHANTMENT_OTHER, card_number)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}
suspend fun sendDestructionEnchant(mine: Connection, other: Connection, card_number: Int){
    val data_your = SakuraCardCommand(DESTRUCTION_ENCHANTMENT_YOUR, card_number)
    val data_other = SakuraCardCommand(DESTRUCTION_ENCHANTMENT_OTHER, card_number)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendEnchantmentZone(mine: Connection, other: Connection, card_number: Int){
    val data_your = SakuraCardCommand(ENCHANTMENT_CARD_YOUR, card_number)
    val data_other = SakuraCardCommand(ENCHANTMENT_CARD_OTHER, card_number)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendCoverZone(mine: Connection, other: Connection, card_number: Int, public: Boolean){
    val data_your = SakuraCardCommand(COVER_CARD_YOUR, card_number)
    val data_other = if(public) SakuraCardCommand(COVER_CARD_OTHER, card_number)
        else SakuraCardCommand(COVER_CARD_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendDiscardZone(mine: Connection, other: Connection, card_number: Int){
    val data_your = SakuraCardCommand(DISCARD_CARD_YOUR, card_number)
    val data_other = SakuraCardCommand(DISCARD_CARD_OTHER, card_number)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendUsedZone(mine: Connection, other: Connection, card_number: Int){
    val data_your = SakuraCardCommand(USED_CARD_YOUR, card_number)
    val data_other = SakuraCardCommand(USED_CARD_OTHER, card_number)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun makeAttackComplete(mine: Connection, other: Connection, card_number: Int){
    val data_your = SakuraCardCommand(MAKE_ATTACK_COMPLETE_YOUR, card_number)
    val data_other = SakuraCardCommand(MAKE_ATTACK_COMPLETE_OTHER, card_number)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendAttackInformation(mine: Connection, other: Connection, data: MutableList<Int>) {
    val data_your = SakuraSendData(ATTACK_INFORMATION_YOUR, data)
    val data_other = SakuraSendData(ATTACK_INFORMATION_OTHER, data)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendChooseDamage(mine: Connection, command: CommandEnum, aura: Int, life: Int){
    val pre_data = SakuraCardCommand(CHOOSE_DAMAGE, -1)
    val data = SakuraSendData(command, mutableListOf(aura, life))
    mine.session.send(Json.encodeToString(pre_data))
    mine.session.send(Json.encodeToString(data))
}
suspend fun sendRequestReact(mine: Connection){
    val data = SakuraCardCommand(REACT_REQUEST, -1)
    mine.session.send(Json.encodeToString(data))
}

suspend fun sendMoveToken(mine: Connection, other: Connection, what: TokenEnum, from: LocationEnum, to: LocationEnum, number: Int, card_number: Int){
    val pre_data = SakuraCardCommand(MOVE_TOKEN, card_number)
    mine.session.send(Json.encodeToString(pre_data))
    other.session.send(Json.encodeToString(pre_data))
    val data_your = SakuraSendData(MOVE_TOKEN, mutableListOf(what.real_number, from.real_number, to.real_number, number, card_number))
    val data_other = SakuraSendData(MOVE_TOKEN, mutableListOf(what.real_number, from.Opposite().real_number, to.Opposite().real_number, number, card_number))
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendAddConcentration(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(ADD_CONCENTRATION_YOUR, -1)
    val data_other = SakuraCardCommand(ADD_CONCENTRATION_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendDecreaseConcentration(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(DECREASE_CONCENTRATION_YOUR, -1)
    val data_other = SakuraCardCommand(DECREASE_CONCENTRATION_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendUseCardMeesage(mine: Connection, other: Connection, reaction: Boolean, card_number: Int){
    val data_your = if(reaction) SakuraCardCommand(USE_CARD_YOUR_REACTION, card_number) else SakuraCardCommand(
        USE_CARD_YOUR, card_number)
    val data_other = if(reaction) SakuraCardCommand(USE_CARD_OTHER_REACTION, card_number) else SakuraCardCommand(
        USE_CARD_OTHER, card_number)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendRemoveShrink(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(REMOVE_SHRINK_YOUR, -1)
    val data_other = SakuraCardCommand(REMOVE_SHRINK_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendHandToDeck(mine: Connection, other: Connection, card_number: Int, public: Boolean, below: Boolean){
    val data_your = SakuraCardCommand(if (below) CARD_HAND_TO_DECK_BELOW_YOUR else CARD_HAND_TO_DECK_UPPER_YOUR, card_number)
    val data_other = if(public) SakuraCardCommand(if (below) CARD_HAND_TO_DECK_BELOW_OTHER else CARD_HAND_TO_DECK_UPPER_OTHER, card_number)
    else SakuraCardCommand(if (below) CARD_HAND_TO_DECK_BELOW_OTHER else CARD_HAND_TO_DECK_UPPER_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendHandToCover(mine: Connection, other: Connection, card_number: Int, public: Boolean){
    val data_your = SakuraCardCommand(CARD_HAND_TO_COVER_YOUR, card_number)
    val data_other = if(public) SakuraCardCommand(CARD_HAND_TO_COVER_OTHER, card_number) else SakuraCardCommand(
        CARD_HAND_TO_COVER_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendDrawCard(mine: Connection, other: Connection, card_number: Int){
    val data_your = SakuraCardCommand(DRAW_CARD_YOUR, card_number)
    val data_other = SakuraCardCommand(DRAW_CARD_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendMuligunEnd(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(MULIGUN_END, -1)
    val data_other = SakuraCardCommand(MULIGUN_END, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendStartPhaseStart(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(START_START_PHASE_YOUR, -1)
    val data_other = SakuraCardCommand(START_START_PHASE_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendDeckReconstruct(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(DECK_RECONSTRUCT_YOUR, -1)
    val data_other = SakuraCardCommand(DECK_RECONSTRUCT_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendMainPhaseStart(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(START_MAIN_PHASE_YOUR, -1)
    val data_other = SakuraCardCommand(START_MAIN_PHASE_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendEndPhaseStart(mine: Connection, other: Connection){
    val data_your = SakuraCardCommand(START_END_PHASE_YOUR, -1)
    val data_other = SakuraCardCommand(START_END_PHASE_OTHER, -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendActionRequest(mine: Connection){
    val data_your = SakuraCardCommand(ACTION_REQUEST, -1)
    mine.session.send(Json.encodeToString(data_your))
}

suspend fun sendDoBasicAction(mine: Connection, other: Connection, command: CommandEnum){
    val data_your = SakuraCardCommand(command, -1)
    val data_other = SakuraCardCommand(command.Opposite(), -1)
    mine.session.send(Json.encodeToString(data_your))
    other.session.send(Json.encodeToString(data_other))
}

suspend fun sendGameEnd(winner: Connection, loser: Connection){
    val data_winner = SakuraCardCommand(GAME_END_WINNER, -1)
    val data_loser = SakuraCardCommand(GAME_END_LOSER, -1)
    winner.session.send(Json.encodeToString(data_winner))
    loser.session.send(Json.encodeToString(data_loser))
}

suspend fun sendCoverCardSelect(player: Connection){
    val data = SakuraCardCommand(COVER_CARD_SELECT, -1)
    player.session.send(Json.encodeToString(data))
}

suspend fun cardEffectSelect(player: Connection){
    val data = SakuraCardCommand(SELECT_CARD_EFFECT)
    player.session.send(Json.encodeToString(data))
}

//receive function
suspend fun waitUntil(player: Connection, wait_command: CommandEnum): SakuraSendData {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try{
                val data = json.decodeFromString<SakuraSendData>(text)
                if (data.command == wait_command){
                    return data
                }
            }catch (e: Exception){
                continue
            }

        }
    }

    return SakuraSendData(SELECT_MODE, null)
}

suspend fun waitCardSetUntil(player: Connection, wait_command: CommandEnum): SakuraCardSetSend {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try{
                val data = json.decodeFromString<SakuraCardSetSend>(text)
                if (data.command == wait_command){
                    return data
                }
            }catch (e: Exception){
                continue
            }
        }
    }

    return SakuraCardSetSend(NULL, null, null)
}

suspend fun receiveEnchantment(player: Connection): Pair<CommandEnum, Int> {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == SELECT_ENCHANTMENT_OTHER || data.command == SELECT_ENCHANTMENT_YOUR){
                    return Pair(data.command, data.card)
                }
                else if(data.command == SELECT_ENCHANTMENT_END){
                    return Pair(data.command, -1)
                }
            }catch (e: Exception){
                continue
            }
        }
    }
    return Pair(SELECT_ENCHANTMENT_END, -1)
}

suspend fun receiveReact(player: Connection): Pair<CommandEnum, Int> {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == REACT_USE_CARD_HAND || data.command == REACT_USE_CARD_SPECIAL){
                    return Pair(data.command, data.card)
                }
                else if(data.command == REACT_NO){
                    return Pair(data.command, -1)
                }
            }catch (e: Exception){
                continue
            }
        }
    }
    return Pair(REACT_NO, -1)
}

suspend fun receiveChooseDamage(player: Connection): CommandEnum {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == CHOOSE_AURA || data.command == CHOOSE_LIFE){
                    return data.command
                }
                else {
                    continue
                }
            }catch (e: Exception){
                continue
            }
        }
    }
    return CHOOSE_AURA
}

suspend fun receiveNapInformation(player: Connection, total: Int, card_number: Int): Pair<Int, Int> {
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    val pre_data = SakuraCardCommand(SELECT_NAP, card_number)
    player.session.send(Json.encodeToString(pre_data))
    val data = SakuraSendData(SELECT_NAP, mutableListOf(total))
    player.session.send(Json.encodeToString(data))
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraSendData>(text)
                if (data.command == SELECT_NAP){
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
    return Pair(0, 0)
}

suspend fun receiveReconstructRequest(player: Connection): Boolean{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    val pre_data = SakuraCardCommand(DECK_RECONSTRUCT_REQUEST, -1)
    player.session.send(Json.encodeToString(pre_data))
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == DECK_RECONSTRUCT_NO){
                    return false
                }
                else if(data.command == DECK_RECONSTRUCT_YES){
                    return true
                }
                else {
                    continue
                }
            }catch (e: Exception){
                continue
            }
        }
    }
    return false
}

suspend fun receiveFullPowerRequest(player: Connection): Boolean{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    val pre_data = SakuraCardCommand(FULL_POWER_REQUEST, -1)
    player.session.send(Json.encodeToString(pre_data))
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == FULL_POWER_NO){
                    return false
                }
                else if(data.command == FULL_POWER_YES){
                    return true
                }
                else {
                    continue
                }
            }catch (e: Exception){
                continue
            }
        }
    }
    return false
}

suspend fun receiveFullPowerActionRequest(player: Connection): Pair<CommandEnum, Int>{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    sendActionRequest(player)
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try{
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == ACTION_USE_CARD_HAND ||
                    data.command == ACTION_USE_CARD_SPECIAL ||
                    data.command == ACTION_END_TURN
                ){
                    return Pair(data.command, data.card)
                }
                else {
                    continue
                }
            }catch (e: Exception){
                continue
            }
        }
    }
    return Pair(FIRST_TURN, -1)
}

suspend fun receiveActionRequest(player: Connection): Pair<CommandEnum, Int>{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    sendActionRequest(player)
    for (frame in player.session.incoming) {
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == ACTION_USE_CARD_HAND ||
                    data.command == ACTION_USE_CARD_SPECIAL ||
                    data.command == ACTION_GO_FORWARD ||
                    data.command == ACTION_GO_BACKWARD ||
                    data.command == ACTION_WIND_AROUND ||
                    data.command == ACTION_INCUBATE ||
                    data.command == ACTION_BREAK_AWAY ||
                    data.command == ACTION_END_TURN
                ){
                    return Pair(data.command, data.card)
                }
                else {
                    continue
                }
            }catch (e: Exception){
                continue
            }
        }
    }
    return Pair(FIRST_TURN, -1)
}

suspend fun receiveCoverCardSelect(player: Connection): Int{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    sendCoverCardSelect(player)
    for(frame in player.session.incoming){
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                if (data.command == COVER_CARD_SELECT){
                    return data.card
                }
                else {
                    continue
                }
            }catch (e: Exception){
                continue
            }

        }
    }
    return -1
}

suspend fun  receiveCardEffectSelect(player: Connection): CommandEnum{
    val json = Json { ignoreUnknownKeys = true; coerceInputValues = true}

    cardEffectSelect(player)
    for(frame in player.session.incoming){
        if (frame is Frame.Text) {
            val text = frame.readText()
            try {
                val data = json.decodeFromString<SakuraCardCommand>(text)
                when(data.command){
                    SELECT_DUST_TO_DISTANCE, SELECT_DISTANCE_TO_DUST -> return  data.command //will be added
                    else -> continue
                }
            }catch (e: Exception){
                continue
            }

        }
    }
    return NULL
}

