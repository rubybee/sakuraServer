package com.sakurageto

import com.sakurageto.card.CardSet
import com.sakurageto.gamelogic.megamispecial.storyboard.StoryBoard
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.sakurageto.plugins.*

fun main() {
    CardSet.init()
    StoryBoard.init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSockets()
    configureSerialization()
    configureMonitoring()
    configureRouting()
}
