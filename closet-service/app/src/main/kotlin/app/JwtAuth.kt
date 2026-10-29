package app

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.jwt.jwt
import users.infrastructure.security.JwtConfig
import java.util.UUID

fun AuthenticationConfig.jwtAuth(config: JwtConfig) {
    jwt(JWT_AUTH) {
        realm = "closet"
        verifier(
            JWT.require(Algorithm.HMAC256(config.secret))
                .withIssuer(config.issuer)
                .withAudience(config.audience)
                .build()
        )
        validate { credential ->
            credential.payload.subject
                ?.let { runCatching { UUID.fromString(it) }.getOrNull() }
                ?.let { UserIdPrincipal(it.toString()) }
        }
        challenge { _, _ ->
            call.response.headers.append(HttpHeaders.WWWAuthenticate, "Bearer realm=\"closet\"")
            call.respondError(HttpStatusCode.Unauthorized, "unauthorized", "Authentication required")
        }
    }
}
