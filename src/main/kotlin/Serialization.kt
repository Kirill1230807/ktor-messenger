import com.example.api.RegisterRequestDto
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.requestvalidation.RequestValidation
import io.ktor.server.plugins.requestvalidation.ValidationResult

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json()
    }
}

// Валідація вхідних даних
fun Application.configureValidation() {
    install(RequestValidation) {
        validate<RegisterRequestDto> { dto ->
            if (!dto.email.contains("@")) {
                ValidationResult.Invalid("Неправильний формат email")
            } else if (dto.password.length < 6) {
                ValidationResult.Invalid("Пароль не має бути коротшим за 6 символів")
            } else {
                ValidationResult.Valid
            }
        }
    }
}