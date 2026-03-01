package com.example.api

import com.example.application.ChatService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable

@Serializable
data class SendMessageDto(val senderId: Int, val receiverId: Int, val text: String, val timestamp: Long)

@Serializable
data class MessageResponseDto(
    val id: Int,
    val senderId: Int,
    val receiverId: Int,
    val text: String,
    val timestamp: Long
)

fun Route.chatRoute(chatService: ChatService) {
    route("/chat") {
        post("/messages") {
            val request = call.receive<SendMessageDto>()
            try {
                val message = chatService.sendMessage(request.senderId, request.receiverId, request.text)
                val responseDto = MessageResponseDto(
                    message.id,
                    message.senderId,
                    message.receiverId,
                    message.text,
                    message.timestamp
                )
                call.respond(HttpStatusCode.Created, responseDto)
            } catch (e: IllegalArgumentException) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }

        get("/history/{myId}/{contactId}") {
            val myId = call.parameters["myId"]?.toIntOrNull()
            val contactId = call.parameters["contactId"]?.toIntOrNull()

            if (myId == null || contactId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Incorrect id"))
                return@get
            }

            val history = chatService.getChatHistory(myId, contactId)
            val response = history.map {
                MessageResponseDto(it.id, it.senderId, it.receiverId, it.text, it.timestamp)
            }
            call.respond(HttpStatusCode.OK, response)
        }
    }
}