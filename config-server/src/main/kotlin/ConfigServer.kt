package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.io.File
import java.util.Properties

fun main() {
    embeddedServer(Netty, port = 8888, module = Application::configModule).start(wait = true)
}

fun Application.configModule() {
    routing {
        // 1. Отримання базової конфігурації (application.yaml)
        get("/config/{service}/{profile}") {
            val service = call.parameters["service"]
            val profile = call.parameters["profile"]

            // Шукаємо файл у папці config-repo
            val file = File("config-repo/$service/$profile.yaml")

            if (file.exists()) {
                call.respondText(file.readText(), ContentType.Text.Plain)
            } else {
                call.respond(HttpStatusCode.NotFound, "Config file not found")
            }
        }

        // 2. Отримання динамічних параметрів (Runtime Refresh)
        get("/config/{service}/{profile}/dynamic/{key}") {
            val service = call.parameters["service"]
            val profile = call.parameters["profile"]
            val key = call.parameters["key"]

            val file = File("config-repo/$service/$profile-dynamic.properties")
            if (file.exists()) {
                val props = Properties().apply { load(file.inputStream()) }
                val value = props.getProperty(key)

                if (value != null) {
                    call.respondText(value, ContentType.Text.Plain)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Key not found")
                }
            } else {
                call.respond(HttpStatusCode.NotFound, "Dynamic config not found")
            }
        }
    }
}