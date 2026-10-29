package users.domain

import java.time.Instant

data class AccessToken(
    val value: String,
    val expiresAt: Instant,
) {
    override fun toString() = "AccessToken(expiresAt=$expiresAt)"
}