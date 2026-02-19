import io.ktor.server.application.*
import io.ktor.server.routing.*

import com.example.api.userRouting
import com.example.application.UserService
import com.example.infrastructure.ExposedUserRepository
import io.ktor.server.plugins.swagger.*

fun Application.configureRouting() {
    val userRepository = ExposedUserRepository()
    val userService = UserService(userRepository)

    routing {
        swaggerUI(path = "swagger", swaggerFile = "openapi/documentation.yaml")
        userRouting(userService)
    }
}