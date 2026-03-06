import io.ktor.server.plugins.statuspages.StatusPages
import kotlinx.serialization.Serializable
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.requestvalidation.RequestValidationException
import io.ktor.server.response.respond

@Serializable
data class ErrorResponse(
    val statusCode: Int,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

fun Application.configureExceptionHandling() {
    install(StatusPages) {
        exception<RequestValidationException> { call, cause ->
            call.respond(HttpStatusCode.BadRequest, ErrorResponse(400, cause.reasons.joinToString(", ")))
        }
        exception<IllegalArgumentException> { call, cause ->
            call.respond(HttpStatusCode.Conflict, ErrorResponse(409, cause.message ?: "Конфлікт даних"))
        }
        exception<Throwable> { call, cause ->
            cause.printStackTrace() // щоб бачити помилку в консолі
            call.respond(HttpStatusCode.InternalServerError, ErrorResponse(500, "Внутрішня помилка сервера"))
        }
    }
}