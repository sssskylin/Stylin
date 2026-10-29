package app

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import io.ktor.server.response.respond
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.UUID
import io.ktor.server.auth.jwt.jwt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

const val JWT_AUTH = "auth-jwt"

@OptIn(ExperimentalSerializationApi::class)
fun Application.installPlugins(config: AppConfig) {
    install(CallId) {
        header(HttpHeaders.XRequestId)
        verify { it.isNotBlank() && it.length <= 100 }
        generate { UUID.randomUUID().toString() }
        replyToHeader(HttpHeaders.XRequestId)
    }

    install(CallLogging) {
        level = Level.INFO
        callIdMdc("call-id")
        filter { it.request.path().startsWith("/api") }
    }

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            }
        )
    }

    install(Authentication) {
        jwt(JWT_AUTH) {
            realm = "closet"

            verifier(
                JWT.require(Algorithm.HMAC256(config.jwt.secret))
                    .withIssuer(config.jwt.issuer)
                    .withAudience(config.jwt.audience)
                    .build()
            )

            validate { credential ->
                val subject = credential.payload.subject
                    ?: return@validate null

                val userId = runCatching {
                    UUID.fromString(subject)
                }.getOrNull() ?: return@validate null

                UserIdPrincipal(userId.toString())
            }

            challenge { _, _ ->
                call.response.headers.append(
                    HttpHeaders.WWWAuthenticate,
                    """Bearer realm="closet""""
                )

                call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf(
                        "code" to "unauthorized",
                        "message" to "Authentication required"
                    )
                )
            }
        }
    }
}