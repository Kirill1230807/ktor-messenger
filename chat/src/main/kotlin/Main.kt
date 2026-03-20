package com.example

import com.example.api.chatRoute
import com.example.application.ChatService
import com.example.infrastructure.ExposedMessageRepository
import com.example.infrastructure.MessageTable
import com.example.infrastructure.UserClient
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.netty.EngineMain
import io.ktor.server.routing.routing
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.chatModule() {

    install(ContentNegotiation) {
        json()
    }

    // окрема бд для мікросервісу Chat
    Database.connect("jdbc:h2:mem:chatdb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
    transaction {
        SchemaUtils.create(MessageTable)
    }

    val messageRepository = ExposedMessageRepository()

    val userClient = UserClient()
    val chatService = ChatService(messageRepository, userClient)

    routing {
        chatRoute(chatService)
    }
}