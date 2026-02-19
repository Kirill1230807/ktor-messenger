package com.example.api

import com.example.application.UserService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import kotlin.collections.mapOf

@Serializable
data class RegisterRequestDto(val username: String, val email: String, val password: String)

@Serializable
data class UserResponseDto(val id: Int, val username: String, val email: String)

fun Route.userRouting(userService: UserService) {
    route("/users") {
        post("/register") {
            val request = call.receive<RegisterRequestDto>()

            try {
                val createdUser = userService.registerUser(request.username, request.email, request.password)

                val responseDto = UserResponseDto(createdUser.id, createdUser.username, createdUser.email)
                call.respond(HttpStatusCode.Created, responseDto)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.Conflict, mapOf("error" to e.message))
            }
        }
    }
}