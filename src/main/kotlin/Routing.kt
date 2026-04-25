import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.response.respond
import io.ktor.server.routing.*
import kotlinx.coroutines.async
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.Date

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

    val secret = "my-super-secret-key"
    val issuer = "http://localhost:8080/"
    val audience = "http://localhost:8080/api"

    routing {
        post("/api/v1/auth/login") {
            val userId = "1"
            val token = JWT.create()
                .withAudience(audience)
                .withIssuer(issuer)
                .withClaim("userId", userId)
                .withClaim("role", "USER")
                .withExpiresAt(Date(System.currentTimeMillis() + 3600000))
                .sign(Algorithm.HMAC256(secret))

            call.respond(mapOf("token" to token))
        }

        authenticate("auth-jwt") {
            rateLimit {

                route("/api/v1/users") {
                    get("/{id}") {
                        val id = call.parameters["id"]
                        val principal = call.principal<JWTPrincipal>()
                        val jwtUserId = principal?.payload?.getClaim("userId")?.asString()
                        val jwtRole = principal?.payload?.getClaim("role")?.asString()

                        val response = client.get("http://localhost:8081/api/v1/users/$id") {
                            header("X-User-Id", jwtUserId)
                            header("X-User-Role", jwtRole)
                        }
                        call.respond(response.status, response.readBytes())
                    }
                }

                route("/api/v1/notification") {
                    get("/user/{userId}") {
                        val userId = call.parameters["userId"]
                        val principal = call.principal<JWTPrincipal>()
                        val jwtUserId = principal?.payload?.getClaim("userId")?.asString()

                        val response = client.get("http://localhost:8083/api/v1/notification/user/$userId") {
                            header("X-User-Id", jwtUserId)
                        }
                        call.respond(response.status, response.readBytes())
                    }
                }

                get("/api/v1/dashboard/{userId}") {
                    val userId = call.parameters["userId"]
                    if (userId == null) {
                        call.respond(HttpStatusCode.BadRequest, mapOf("error" to "Incorrect ID format"))
                        return@get
                    }

                    val principal = call.principal<JWTPrincipal>()
                    val jwtUserId = principal?.payload?.getClaim("userId")?.asString()
                    val jwtRole = principal?.payload?.getClaim("role")?.asString()

                    val userDeferred = async {
                        client.get("http://localhost:8081/api/v1/users/$userId") {
                            header("X-User-Id", jwtUserId)
                            header("X-User-Role", jwtRole)
                        }
                    }
                    val notificationsDeferred = async {
                        client.get("http://localhost:8083/api/v1/notification/user/$userId") {
                            header("X-User-Id", jwtUserId)
                            header("X-User-Role", jwtRole)
                        }
                    }

                    val userResponseData = try {
                        val res = userDeferred.await()
                        if (res.status == HttpStatusCode.OK) res.body<UserResponseDto>() else null
                    } catch (e: Exception) { null }

                    if (userResponseData == null) {
                        call.respond(HttpStatusCode.NotFound, mapOf("error" to "User not found or User Service is unavailable"))
                        return@get
                    }

                    val notificationsResponseData = try {
                        val res = notificationsDeferred.await()
                        if (res.status == HttpStatusCode.OK) res.body<List<NotificationResponseDto>>() else emptyList()
                    } catch (e: Exception) { emptyList() }

                    call.respond(HttpStatusCode.OK, DashboardResponse(userResponseData, notificationsResponseData))
                }
            }
        }
    }
}