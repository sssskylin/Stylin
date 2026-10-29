package wardrobe.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class BoardDto(
    val id: String,
    val userId: String,
    val title: String,
    val itemCount: Int
)