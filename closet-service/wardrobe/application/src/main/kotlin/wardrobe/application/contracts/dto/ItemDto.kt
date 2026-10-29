package wardrobe.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    val id: String,
    val boardId: String,
    val userId: String,
    val title: String,
    val description: String?,
    val imageUrl: String
)
