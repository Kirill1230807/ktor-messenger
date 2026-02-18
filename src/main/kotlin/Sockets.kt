package com.example

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration
import kotlin.time.Duration.Companion.seconds


fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/chat") { // websocketSession
            send("Привіт! Ти підключився до сервера месенджера.")

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val receiveText = frame.readText()

                    send("Сервер отримав: $receiveText")
                }
            }
        }
    }
}
