package users.infrastructure.abstractions

import users.domain.User
import java.util.UUID

interface UserRepository {

    fun save(user: User)

    fun findById(id: UUID): User?

    fun findByEmail(email: String): User?

    fun findAll(): List<User>

    fun existsByEmail(
        email: String,
        excludingId: UUID,
    ): Boolean

    fun existsByUsername(
        username: String,
        excludingId: UUID,
    ): Boolean

    fun deleteById(id: UUID)
}