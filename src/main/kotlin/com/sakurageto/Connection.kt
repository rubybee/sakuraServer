package com.sakurageto

import com.sakurageto.card.PlayerEnum
import io.ktor.server.websocket.*




open class Connection(var session: DefaultWebSocketServerSession) {
    var gameEnd: Boolean = false
    var socketPlayer: PlayerEnum = PlayerEnum.PLAYER1
    var roomNumber: Int = -1
    var disconnectTime: Long = -1
}