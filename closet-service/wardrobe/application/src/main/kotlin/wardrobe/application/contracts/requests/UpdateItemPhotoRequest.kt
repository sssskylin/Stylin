package wardrobe.application.contracts.requests

import wardrobe.domain.ImageUpload
import java.util.UUID

data class UpdateItemPhotoRequest(
    val userId: UUID,
    val boardId: UUID,
    val itemId: UUID,
    val photo: ImageUpload,
)