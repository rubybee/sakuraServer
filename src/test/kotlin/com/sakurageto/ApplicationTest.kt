@file:Suppress("DEPRECATION")

package com.sakurageto

import com.sakurageto.card.*
import com.sakurageto.gamelogic.GameStatus
import com.sakurageto.gamelogic.GameVersion
import com.sakurageto.gamelogic.PlayerStatus
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ApplicationTest {
    private var player1Connection = ConnectionTest(mockk())
    private var player2Connection = ConnectionTest(mockk())

    private var gameStatus = GameStatus(PlayerStatus(PlayerEnum.PLAYER1), PlayerStatus(PlayerEnum.PLAYER2), player1Connection, player2Connection)

    @BeforeTest
    fun init() {
        CardSet.init()
        StoryBoard.init()
    }

    @Before
    fun reset() {
        //life = 10, aura = 3, flare = 0, concentration = 0,
        gameStatus = GameStatus(PlayerStatus(PlayerEnum.PLAYER1),
            PlayerStatus(PlayerEnum.PLAYER2), player1Connection, player2Connection)
        gameStatus.version = GameVersion.VERSION_9
        player1Connection = ConnectionTest(mockk())
        player2Connection = ConnectionTest(mockk())
    }

    @Test
    fun baseTest() = runBlockingTest {

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