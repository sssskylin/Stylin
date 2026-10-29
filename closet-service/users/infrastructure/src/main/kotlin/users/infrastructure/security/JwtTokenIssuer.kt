package users.infrastructure.security

import users.domain.AccessToken
import users.infrastructure.abstractions.TokenIssuer
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Clock
import java.util.UUID

class JwtTokenIssuer(
    private val config: JwtConfig,
    private val clock: Clock,
) : TokenIssuer {

    private val algorithm = Algorithm.HMAC256(config.secret)

    override fun issue(userId: UUID): AccessToken {
        val now = clock.instant()
        val expiresAt = now.plus(config.ttl)
        val token = JWT.create()
            .withIssuer(config.issuer)
            .withAudience(config.audience)
            .withSubject(userId.toString())
            .withIssuedAt(now)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
        return AccessToken(token, expiresAt)
    }
}
