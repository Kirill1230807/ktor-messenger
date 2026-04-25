import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    val secret = "my-super-secret-key"
    val issuer = "http://localhost:8080/"
    val audience = "http://localhost:8080/api"
    val myRealm = "Access to API"

    install(Authentication) {
        jwt("auth-jwt") {
            realm = myRealm
            verifier(
                JWT.require(Algorithm.HMAC256(secret))
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            )
            validate { credential ->
                val userId = credential.payload.getClaim("userId").asString()
                if (userId != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    println("ПОМИЛКА JWT: у токені немає поля userId!")
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized, "Токен невалідний або закінчився термін його дії")
            }
        }
    }
}
