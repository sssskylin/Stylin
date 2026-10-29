package wardrobe.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class BoardListDto(
    val boards: List<BoardDto>
)
