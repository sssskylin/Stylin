package users.application.contracts.requests

import java.util.UUID

data class ChangeUsernameRequest(
    val userId: UUID,
    val newUsername: String,
)