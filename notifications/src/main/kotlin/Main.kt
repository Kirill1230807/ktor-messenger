package com.example

import application.ConsulConfigManager
import com.example.api.notificationRoute
import com.example.application.NotificationConsumer
import com.example.application.NotificationService
import com.example.infrastructure.ExposedNotificationRepository
import com.example.infrastructure.NotificationTable
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.EngineMain
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

val profile = System.getenv("APP_PROFILE") ?: "dev"
val configManager = ConsulConfigManager("notification", profile)

fun main(args: Array<String>) {
    GlobalScope.launch {
        configManager.startWatching()
    }
    EngineMain.main(args)
}

fun Application.notificationModule() {

    install(ContentNegotiation) {
        json()
    }

    Database.connect("jdbc:h2:mem:notificationdb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    transaction {
        SchemaUtils.create(NotificationTable)
    }

    val notificationRepository = ExposedNotificationRepository()
    val notificationService = NotificationService(notificationRepository)

    val consumer = NotificationConsumer(notificationService)
    consumer.startListening()

    routing {
        route("/api/v1") {
            notificationRoute(notificationService)
        }
    }
}