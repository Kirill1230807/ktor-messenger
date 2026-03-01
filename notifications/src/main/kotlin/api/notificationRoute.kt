package com.example.api

import com.example.application.NotificationService
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.serialization.Serializable
import kotlin.collections.mapOf

@Serializable
data class CreateNotificationDto(val userId: Int, val message: String)

@Serializable
data class NotificationResponseDto(val id: Int, val userId: Int, val message: String, val isRead: Boolean)

fun Route.notificationRoute(notificationService: NotificationService) {
    route("/notification") {
        post {
            val request = call.receive<CreateNotificationDto>()
            val notification = notificationService.createNotification(request.userId, request.message)
            val responseDto =
                NotificationResponseDto(notification.id, notification.userId, notification.message, notification.isRead)
            call.respond(HttpStatusCode.Created, responseDto)
        }

        get("/user/{userId}") {
            val userId = call.parameters["userId"]?.toIntOrNull()
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Incorrect User id"))
                return@get
            }

            val notifications = notificationService.getUnreadNotifications(userId)
            val response = notifications.map {
                NotificationResponseDto(it.id, it.userId, it.message, it.isRead)
            }
            call.respond(HttpStatusCode.OK, response)
        }

        patch("/{id}/read") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Incorrect Notification id"))
                return@patch
            }

            val isMarked = notificationService.markAsRead(id)
            if (isMarked) {
                call.respond(HttpStatusCode.OK, mapOf("message" to " Notification marked as read"))
            } else {
                call.respond(HttpStatusCode.NotFound, mapOf("message" to " Notification not found"))
            }
        }
    }
}