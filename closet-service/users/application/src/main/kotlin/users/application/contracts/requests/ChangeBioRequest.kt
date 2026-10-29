package users.application.contracts.requests

import java.util.UUID

data class ChangeBioRequest(
    val userId: UUID,
    val newBio: String?,
)