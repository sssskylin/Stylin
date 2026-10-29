package users.application.contracts.requests

import java.util.UUID

data class ChangeNameRequest(
    val userId: UUID,
    val newName: String,
)