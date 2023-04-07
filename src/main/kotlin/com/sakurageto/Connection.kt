package com.sakurageto

import com.sakurageto.card.PlayerEnum
import io.ktor.server.websocket.*
import java.util.concurrent.atomic.*

class Connection(var session: DefaultWebSocketServerSession) {
    var gameEnd: Boolean = false
    var socketPlayer: PlayerEnum = PlayerEnum.PLAYER1
    var roomNumber: Int = -1
    var disconnectTime: Long = -1
}