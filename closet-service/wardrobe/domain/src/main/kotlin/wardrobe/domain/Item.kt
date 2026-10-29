package wardrobe.domain

import java.util.UUID

data class Item(
    val id: UUID,
    val userId: UUID,
    val boardId: UUID,
    val title: String,
    val description: String?,
    val image: ImageRef
)
