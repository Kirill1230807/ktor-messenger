package com.example.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

fun Route.chatRoute() {
    route("/chat") {
        get("/ping") {
            call.respondText("Це тест модуля chat.", status = HttpStatusCode.OK)
        }
    }
}