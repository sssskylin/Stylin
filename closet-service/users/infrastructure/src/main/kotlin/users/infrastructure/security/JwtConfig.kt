package users.infrastructure.security

import java.time.Duration

data class JwtConfig(
    val secret: String,
    val issuer: String,
    val audience: String,
    val ttl: Duration,
)
