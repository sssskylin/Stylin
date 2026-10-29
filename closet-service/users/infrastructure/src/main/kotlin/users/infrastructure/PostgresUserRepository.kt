package users.infrastructure

import users.infrastructure.abstractions.UserRepository
import users.domain.exceptions.EmailAlreadyTakenException
import users.domain.exceptions.UsernameAlreadyTakenException
import org.postgresql.util.PSQLException
import users.domain.User
import java.sql.ResultSet
import java.sql.Timestamp
import java.util.UUID

class PostgresUserRepository(
    private val tx: JdbcTransactionRunner,
) : UserRepository {

    override fun save(user: User) {
        val connection = tx.connection()

        try {
            connection.prepareStatement(
                """
                INSERT INTO users (
                    id, email, username, name, bio,
                    password_hash, created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE SET
                    email = EXCLUDED.email,
                    username = EXCLUDED.username,
                    name = EXCLUDED.name,
                    bio = EXCLUDED.bio,
                    password_hash = EXCLUDED.password_hash
                """.trimIndent()
            ).use { statement ->
                statement.setObject(1, user.id)
                statement.setString(2, user.email)
                statement.setString(3, user.username)
                statement.setString(4, user.name)
                statement.setString(5, user.bio)
                statement.setString(6, user.passwordHash)
                statement.setTimestamp(
                    7,
                    Timestamp.from(user.createdAt)
                )

                statement.executeUpdate()
            }
        } catch (e: PSQLException) {
            if (e.sqlState == "23505") {
                when (e.serverErrorMessage?.constraint) {
                    "uq_users_email" -> throw EmailAlreadyTakenException()
                    "uq_users_username" -> throw UsernameAlreadyTakenException()
                }
            }

            throw e
        }
    }

    override fun findById(id: UUID): User? =
        tx.connection().prepareStatement(
            """
            SELECT id, email, username, name, bio,
                   password_hash, created_at
            FROM users
            WHERE id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setObject(1, id)

            statement.executeQuery().use { result ->
                if (result.next()) result.toUser() else null
            }
        }

    override fun findByEmail(email: String): User? =
        tx.connection().prepareStatement(
            """
            SELECT id, email, username, name, bio,
            password_hash, created_at
            FROM users
            WHERE email = ?
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, email)

            statement.executeQuery().use { result ->
                if (result.next()) result.toUser() else null
            }
        }

    override fun existsByEmail(
        email: String,
        excludingId: UUID,
    ): Boolean =
        exists(
            """
            SELECT EXISTS (
                SELECT 1 FROM users
                WHERE email = ? AND id <> ?
            )
            """.trimIndent(),
            email,
            excludingId,
        )

    override fun findAll(): List<User> =
        tx.connection().prepareStatement(
            """
            SELECT id, email, username, name, bio, password_hash, created_at
            FROM users
            ORDER BY created_at DESC
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(result.toUser())
                    }
                }
            }
        }

    override fun existsByUsername(
        username: String,
        excludingId: UUID,
    ): Boolean =
        exists(
            """
            SELECT EXISTS (
                SELECT 1 FROM users
                WHERE username = ? AND id <> ?
            )
            """.trimIndent(),
            username,
            excludingId,
        )

    override fun deleteById(id: UUID) {
        tx.connection().prepareStatement(
            "DELETE FROM users WHERE id = ?"
        ).use { statement ->
            statement.setObject(1, id)
            statement.executeUpdate()
        }
    }

    private fun exists(
        sql: String,
        value: String,
        excludingId: UUID,
    ): Boolean =
        tx.connection().prepareStatement(sql).use { statement ->
            statement.setString(1, value)
            statement.setObject(2, excludingId)

            statement.executeQuery().use { result ->
                result.next() && result.getBoolean(1)
            }
        }

    private fun ResultSet.toUser() = User(
        id = getObject("id", UUID::class.java),
        email = getString("email"),
        username = getString("username"),
        name = getString("name"),
        bio = getString("bio"),
        passwordHash = getString("password_hash"),
        createdAt = getTimestamp("created_at").toInstant(),
    )
}