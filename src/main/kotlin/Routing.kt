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
import io.ktor.server.plugins.swagger.*

fun Application.configureRouting() {
    val userRepository = ExposedUserRepository()
    val userService = UserService(userRepository)

    val notificationRepository = ExposedNotificationRepository()
    val notificationService = NotificationService(notificationRepository)

    val messageRepository = ExposedMessageRepository()
    val chatService = ChatService(messageRepository)

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        userRouting(userService)
        notificationRoute(notificationService)
        chatRoute(chatService)
    }
}