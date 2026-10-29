package users.domain

import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
    val username: String,
    val name: String,
    val bio: String?,
    val passwordHash: String,
    val createdAt: Instant,
)
