package com.sakurageto

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.sakurageto.plugins.*

fun main() {
    embeddedServer(Netty, port = 80, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

//fun main() {
//    embeddedServer(Netty, port = System.getenv("PORT").toInt(), module = Application::module)
//        .start(wait = true)
//}

fun Application.module() {
    configureSockets()
    configureMonitoring()
    configureRouting()
    configurationCORS()
}
