package users.application.contracts.requests

import java.util.UUID

data class DeleteAccountRequest(
    val userId: UUID,
    val currentPassword: String,
)