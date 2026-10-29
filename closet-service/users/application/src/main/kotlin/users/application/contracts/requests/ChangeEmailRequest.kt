package users.application.contracts.requests

import java.util.UUID

data class ChangeEmailRequest(val userId: UUID, val newEmail: String, val currentPassword: String)