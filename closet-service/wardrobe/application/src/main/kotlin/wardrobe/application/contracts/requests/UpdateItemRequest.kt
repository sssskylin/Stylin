package wardrobe.application.contracts.requests

import java.util.UUID

data class UpdateItemRequest(
    val userId: UUID,
    val boardId: UUID,
    val itemId: UUID,
    val title: String,
    val description: String?,
)