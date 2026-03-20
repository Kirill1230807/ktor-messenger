package com.example

import com.example.application.UserService
import com.example.infrastructure.ExposedUserRepository
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import com.example.api.userRouting
import com.example.infrastructure.UserTable
import io.ktor.server.netty.EngineMain
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun main(args: Array<String>) {
    EngineMain.main(args)
}

fun Application.userModule() {

    install(ContentNegotiation) {
        json()
    }

    // окрема бд для мікросервісу User
    Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")
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