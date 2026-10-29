package wardrobe.application.contracts.requests

import wardrobe.domain.ImageUpload
import java.util.UUID

data class CreateItemRequest(
    val userId: UUID,
    val boardId: UUID,
    val title: String,
    val description: String?,
    val photo: ImageUpload,
)