package com.sakurageto

import com.sakurageto.card.*
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.gamelogic.PlayerStatus
import com.sakurageto.protocol.*
import io.mockk.mockk
import org.junit.Before

open class ApplicationTest {
    protected var player1Connection = ConnectionTest(PlayerEnum.PLAYER1, mockk())
    protected var player2Connection = ConnectionTest(PlayerEnum.PLAYER2, mockk())

    protected var gameStatus = GameStatus(PlayerStatus(PlayerEnum.PLAYER1), PlayerStatus(PlayerEnum.PLAYER2), player1Connection, player2Connection)

    protected suspend fun addCard(playerEnum: PlayerEnum, card_name: CardName, location: LocationEnum){
        val player = gameStatus.getPlayer(playerEnum)
        val card = Card.cardMakerByName(player.firstTurn, card_name, playerEnum, location, gameStatus.version)
        gameStatus.insertCardTo(playerEnum, card, location, false)
    }

    protected suspend fun useCard(playerEnum: PlayerEnum, card_name: CardName, location: LocationEnum){
        val player = gameStatus.getPlayer(playerEnum)
        val command = when(location){
            LocationEnum.HAND -> CommandEnum.ACTION_USE_CARD_HAND
            LocationEnum.SPECIAL_CARD -> CommandEnum.ACTION_USE_CARD_SPECIAL
            LocationEnum.READY_SOLDIER_ZONE -> CommandEnum.ACTION_USE_CARD_SOLDIER
            LocationEnum.COVER_CARD -> CommandEnum.ACTION_USE_CARD_COVER
            else -> CommandEnum.ACTION_USE_CARD_HAND
        }
        gameStatus.cardUseNormal(playerEnum, command, card_name.toCardNumber(player.firstTurn))
    }

    protected fun getCard(playerEnum: PlayerEnum, card_name: CardName, location: LocationEnum): Card?{
        val player = gameStatus.getPlayer(playerEnum)
        return gameStatus.getCardFrom(playerEnum, card_name.toCardNumber(player.firstTurn), location)
    }

    protected fun haveCard(playerEnum: PlayerEnum, card_name: CardName, location: LocationEnum)
        = getCard(playerEnum, card_name, location)?.card_data?.card_name == card_name

    protected suspend fun startPhase(){
        when(gameStatus.turnPlayer){
            PlayerEnum.PLAYER1 -> {
                player1Connection.putReceiveData(SakuraBaseData(CommandEnum.SELECT_ENCHANTMENT_END))
                player1Connection.putReceiveData(SakuraBaseData(CommandEnum.DECK_RECONSTRUCT_NO))
                player1Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
                player1Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
            }
            PlayerEnum.PLAYER2 -> {
                player2Connection.putReceiveData(SakuraBaseData(CommandEnum.SELECT_ENCHANTMENT_END))
                player2Connection.putReceiveData(SakuraBaseData(CommandEnum.DECK_RECONSTRUCT_NO))
                player2Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
                player2Connection.putReceiveData(SakuraBaseData(CommandEnum.CHOOSE_AURA))
            }
        }
        gameStatus.startPhase()
    }

    protected fun resetValue(aura1: Int, aura2: Int, life1: Int, life2: Int, distance: Int, dust: Int){
        gameStatus.player1.aura = aura1; gameStatus.player2.aura = aura2; gameStatus.player1.life = life1
        gameStatus.player2.life = life2; gameStatus.distanceToken = distance; gameStatus.dust = dust
    }

    protected fun makeData(player: PlayerEnum, command: CommandEnum, card_name: CardName): SakuraData{
        return SakuraBaseData(command, card_name.toCardNumber(gameStatus.getPlayer(player).firstTurn))
    }

    protected fun makeData(command: CommandEnum): SakuraData{
        return SakuraBaseData(command, 0)
    }

    protected fun makeData(command: CommandEnum, data: MutableList<Int>): SakuraData{
        return SakuraArrayData(command, data)
    }

    @Before
    open fun reset() {
        //life = 10, aura = 3, flare = 0, concentration = 0,
        player1Connection = ConnectionTest(PlayerEnum.PLAYER1, mockk())
        player2Connection = ConnectionTest(PlayerEnum.PLAYER2, mockk())
        gameStatus = GameStatus(PlayerStatus(PlayerEnum.PLAYER1),
            PlayerStatus(PlayerEnum.PLAYER2), player1Connection, player2Connection)
        gameStatus.version = GameVersion.VERSION_9
        gameStatus.nowPhase = GameStatus.MAIN_PHASE; gameStatus.turnPlayer = PlayerEnum.PLAYER1
        gameStatus.turnNumber = 3
        gameStatus.firstTurnPlayer = PlayerEnum.PLAYER1; gameStatus.player1.firstTurn = true; gameStatus.player2.firstTurn = false
        gameStatus.player1.fullAction = false; gameStatus.player2.fullAction = false
    }

//    fun testRoot() = testApplication {
//        application {
//            configureRouting()
//        }
//        client.get("/").apply {
//            assertEquals(HttpStatusCode.OK, status)
//            assertEquals("Hello World!", bodyAsText())
//        }
//    }
}