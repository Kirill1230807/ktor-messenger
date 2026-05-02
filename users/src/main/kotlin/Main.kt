package com.example

import com.example.application.UserService
import com.example.infrastructure.ExposedUserRepository
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import com.example.api.userRouting
import com.example.application.ConsulConfigManager
import com.example.infrastructure.UserTable
import io.ktor.server.netty.EngineMain
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

val profile = System.getenv("APP_PROFILE") ?: "dev"
val configManager = ConsulConfigManager("users", profile)

fun main(args: Array<String>) {
    GlobalScope.launch {
        configManager.startWatching()
    }
    EngineMain.main(args)
}

fun Application.userModule() {

    install(ContentNegotiation) {
        json()
    }

    val dbPassword = System.getenv("DB_PASSWORD")
        ?: throw IllegalStateException("DB_PASSWORD environment variable is not set!")

    // окрема бд для мікросервісу User
    Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver",)
    transaction {
        SchemaUtils.create(UserTable)
    }

    val userRepository = ExposedUserRepository()

    val userService = UserService(userRepository)

    routing {
        route("/api/v1") {
            userRouting(userService)
        }
    }
}