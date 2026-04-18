import com.example.api.chatRoute
import com.example.api.notificationRoute
import io.ktor.server.application.*
import io.ktor.server.routing.*

import com.example.api.userRouting
import com.example.application.ChatService
import com.example.application.NotificationService
import com.example.application.UserService
import com.example.infrastructure.ExposedMessageRepository
import com.example.infrastructure.ExposedNotificationRepository
import com.example.infrastructure.ExposedUserRepository
import com.example.infrastructure.UserClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.response.respond
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class UserResponseDto(val id: Int, val username: String, val email: String)

@Serializable
data class NotificationResponseDto(val id: Int, val userId: Int, val message: String, val isRead: Boolean)

@Serializable
data class DashboardResponse(
    val user: UserResponseDto,
    val unreadNotifications: List<NotificationResponseDto>
)

fun Application.configureRouting() {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    routing {
        route("/api/v1/users") {
            get("/{id}") {
                val id = call.parameters["id"]
                val response = client.get("http://localhost:8081/api/v1/users/$id")
                call.respond(response.status, response.readBytes())
            }
        }

        route("/api/v1/notification") {
            get("/user/{userId}") {
                val userId = call.parameters["userId"]
                val response = client.get("http://localhost:8083/api/v1/notification/user/$userId")
                call.respond(response.status, response.readBytes())
            }
        }

        get("/api/v1/dashboard/{userId}") {
            val userId = call.parameters["userId"]
            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Incorrect ID format"))
                return@get
            }

            val userDeferred = async {
                client.get("http://localhost:8081/api/v1/users/$userId")
            }
            val notificationsDeferred = async {
                client.get("http://localhost:8083/api/v1/notification/user/$userId")
            }

            val userResponseData = try {
                val res = userDeferred.await()
                if (res.status == HttpStatusCode.OK) {
                    res.body<UserResponseDto>()
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

            if (userResponseData == null) {
                call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found or User Service is unavailable"))
                return@get
            }

            val notificationsResponseData = try {
                val res = notificationsDeferred.await()
                if (res.status == HttpStatusCode.OK) {
                    res.body<List<NotificationResponseDto>>()
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }

            val finalResponse = DashboardResponse(
                user = userResponseData,
                unreadNotifications = notificationsResponseData
            )

            call.respond(HttpStatusCode.OK, finalResponse)
        }
    }
}