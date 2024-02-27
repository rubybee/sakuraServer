package com.sakurageto

import io.ktor.server.application.*
import io.ktor.server.websocket.*
import io.ktor.util.*
import io.ktor.websocket.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlin.coroutines.CoroutineContext

class DefaultWebSocketServerSessionTest: DefaultWebSocketServerSession {
    override val call: ApplicationCall
        get() = TODO("Not yet implemented")
    override val closeReason: Deferred<CloseReason?>
        get() = TODO("Not yet implemented")
    override val coroutineContext: CoroutineContext
        get() = TODO("Not yet implemented")
    override val extensions: List<WebSocketExtension<*>>
        get() = TODO("Not yet implemented")
    override val incoming: ReceiveChannel<Frame>
        get() = TODO("Not yet implemented")
    override var masking: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override var maxFrameSize: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override val outgoing: SendChannel<Frame>
        get() = TODO("Not yet implemented")
    override var pingIntervalMillis: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override var timeoutMillis: Long
        get() = TODO("Not yet implemented")
        set(value) {}

    override suspend fun flush() {
        TODO("Not yet implemented")
    }

    @InternalAPI
    override fun start(negotiatedExtensions: List<WebSocketExtension<*>>) {
        TODO("Not yet implemented")
    }

    override fun terminate() {
        TODO("Not yet implemented")
    }
}