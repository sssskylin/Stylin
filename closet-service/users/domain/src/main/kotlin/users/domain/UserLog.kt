package users.domain

import java.time.Instant
import java.util.UUID

data class UserLog(val id: UUID, val userId: UUID, val type: UserLogType, val createdAt: Instant)
