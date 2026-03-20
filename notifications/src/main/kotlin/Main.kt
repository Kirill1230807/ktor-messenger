package com.example

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
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.notificationModule() {

    // 1. Налаштування JSON-серіалізації
    install(ContentNegotiation) {
        json()
    }

    // 2. Підключення до ізольованої in-memory БД для мікросервісу Notifications
    Database.connect("jdbc:h2:mem:notificationdb;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

    // Створення таблиці notifications у базі даних
    transaction {
        SchemaUtils.create(NotificationTable)
    }

    // 3. Ініціалізація залежностей (Репозиторій та Сервіс)
    val notificationRepository = ExposedNotificationRepository()
    val notificationService = NotificationService(notificationRepository)

    // 4. Запуск RabbitMQ Consumer (слухача подій)
    val consumer = NotificationConsumer(notificationService)
    consumer.startListening()

    // 5. Налаштування маршрутизації (API)
    routing {
        route("/api/v1") {
            notificationRoute(notificationService)
        }
    }
}