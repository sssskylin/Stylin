package wardrobe.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class BoardWithItemsDto(
    val board: wardrobe.application.contracts.dto.BoardDto,
    val items: List<wardrobe.application.contracts.dto.ItemDto>
)
