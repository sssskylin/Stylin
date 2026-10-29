package wardrobe.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class BoardWithItemsDto(
    val board: BoardDto,
    val items: List<ItemDto>
)
