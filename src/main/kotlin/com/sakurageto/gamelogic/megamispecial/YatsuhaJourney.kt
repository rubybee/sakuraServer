package com.sakurageto.gamelogic.megamispecial

import com.sakurageto.card.*
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.log.EventLog
import com.sakurageto.gamelogic.log.LogText
import com.sakurageto.protocol.CommandEnum
import com.sakurageto.protocol.LocationEnum

class YatsuhaJourney(private val startPoint: Int) {
    private var nowPoint = -1
    private var clock = false

    init {
        nowPoint = startPoint
    }

    suspend fun startJourney(player: PlayerEnum, game_status: GameStatus){
        game_status.sendCommand(player, player.opposite(), CommandEnum.SET_JOURNEY_YOUR, nowPoint)
    }

    suspend fun moveJourney(player: PlayerEnum, game_status: GameStatus){
        nowPoint += 1
        if(nowPoint > 4){
            nowPoint = 1
        }
        game_status.sendCommand(player, player.opposite(), CommandEnum.SET_JOURNEY_YOUR, nowPoint)
    }

    suspend fun effectJourney(player: PlayerEnum, gameStatus: GameStatus): Boolean{
        if(isNowEffectTwice()){
            return false
        }
        val nowText = getJourneyEffect()
        nowText.effect!!(-1, player, gameStatus, null)
        clock = true
        return true
    }

    private fun isNowEffectTwice() = startPoint == nowPoint && clock

    private fun getJourneyEffect(): Text{
        return when(nowPoint){
            1 -> journeyEffectOne
            2 -> journeyEffectTwo
            3 -> journeyEffectThree
            4 -> journeyEffectFour
            else -> journeyEffectOne
        }
    }

    companion object{
        val journeyEffectOne = Text(TextEffectTimingTag.USING, TextEffectTag.CHANGE_CONCENTRATION) {_, player, game_status, _->
            game_status.setConcentration(player, 1)
            null
        }

        val journeyEffectTwo = Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_TOKEN) ret@{ _, player, game_status, _->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.YOUR_ENCHANTMENT_ZONE_CARD, LocationEnum.OTHER_ENCHANTMENT_ZONE_CARD),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_JOURNEY_SECOND_EFFECT, 1){ _, _ ->
                true
            }?.let { selected ->
                var nowPlayer = player
                val card = game_status.getCardFrom(player, selected[0], LocationEnum.ENCHANTMENT_ZONE)?:
                game_status.getCardFrom(player.opposite(), selected[0], LocationEnum.ENCHANTMENT_ZONE)?.also {
                    nowPlayer = player.opposite()
                }?: return@ret null

                while(true){
                    when(game_status.receiveCardEffectSelect(player, NUMBER_JOURNEY_SECOND_EFFECT)){
                        CommandEnum.SELECT_ONE -> {
                            game_status.dustToCard(nowPlayer, 1, card, NUMBER_JOURNEY_SECOND_EFFECT)
                            break
                        }
                        CommandEnum.SELECT_TWO -> {
                            game_status.cardToDust(nowPlayer, 1, card, false, NUMBER_JOURNEY_SECOND_EFFECT)
                            break
                        }
                        else -> {}
                    }
                }
                game_status.gameLogger.insert(EventLog(player, LogText.END_EFFECT, NUMBER_JOURNEY_SECOND_EFFECT, -1))
            }
            null
        }

        val journeyEffectThree = Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{_, player, game_status, _->
            game_status.selectCardFrom(player, player, player,
                listOf(LocationEnum.DISCARD_YOUR, LocationEnum.COVER_CARD),
                CommandEnum.SELECT_CARD_REASON_CARD_EFFECT, NUMBER_JOURNEY_THIRD_EFFECT, 1){ _, _ ->
                true
            }?.let { selected ->
                var zone = LocationEnum.DISCARD_YOUR
                val card = game_status.getCardFrom(player, selected[0], LocationEnum.DISCARD_YOUR)?:
                game_status.getCardFrom(player, selected[0], LocationEnum.COVER_CARD)?.also {
                    zone = LocationEnum.COVER_CARD
                }?: return@ret null

                game_status.popCardFrom(player, card.card_number, zone, zone == LocationEnum.DISCARD_YOUR)?.let {
                    game_status.insertCardTo(player, it, LocationEnum.YOUR_DECK_BELOW, zone == LocationEnum.DISCARD_YOUR)
                }
            }
            null
        }

        val journeyEffectFour = Text(TextEffectTimingTag.USING, TextEffectTag.MOVE_CARD) ret@{ _, player, game_status, _->
            game_status.drawCard(player, 1)
            null
        }
    }
}