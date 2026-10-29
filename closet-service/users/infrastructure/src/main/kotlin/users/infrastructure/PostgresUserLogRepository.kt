package users.infrastructure

import users.infrastructure.abstractions.UserLogRepository
import users.domain.UserLog
import users.domain.UserLogType
import java.sql.Timestamp
import java.util.UUID

class PostgresUserLogRepository(
    private val tx: JdbcTransactionRunner,
) : UserLogRepository {

    override fun add(log: UserLog) {
        tx.connection().prepareStatement(
            """
            INSERT INTO user_logs (
                id, user_id, type, created_at
            )
            VALUES (?, ?, ?, ?)
            """.trimIndent()
        ).use { statement ->
            statement.setObject(1, log.id)
            statement.setObject(2, log.userId)
            statement.setString(3, log.type.name)
            statement.setTimestamp(
                4,
                Timestamp.from(log.createdAt)
            )

            statement.executeUpdate()
        }
    }

    override fun recent(
        userId: UUID,
        limit: Int,
    ): List<UserLog> =
        tx.connection().prepareStatement(
            """
            SELECT id, user_id, type, created_at
            FROM user_logs
            WHERE user_id = ?
            ORDER BY created_at DESC
            LIMIT ?
            """.trimIndent()
        ).use { statement ->
            statement.setObject(1, userId)
            statement.setInt(2, limit.coerceIn(1, 200))

            statement.executeQuery().use { result ->
                buildList {
                    while (result.next()) {
                        add(
                            UserLog(
                                id = result.getObject(
                                    "id",
                                    UUID::class.java
                                ),
                                userId = result.getObject(
                                    "user_id",
                                    UUID::class.java
                                ),
                                type = UserLogType.valueOf(
                                    result.getString("type")
                                ),
                                createdAt = result.getTimestamp(
                                    "created_at"
                                ).toInstant(),
                            )
                        )
                    }
                }
            }
        }
}