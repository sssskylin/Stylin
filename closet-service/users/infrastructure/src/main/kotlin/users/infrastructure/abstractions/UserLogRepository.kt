package users.infrastructure.abstractions

import users.domain.UserLog
import java.util.UUID

interface UserLogRepository {

    fun add(log: UserLog)

    fun recent(
        userId: UUID,
        limit: Int,
    ): List<UserLog>
}