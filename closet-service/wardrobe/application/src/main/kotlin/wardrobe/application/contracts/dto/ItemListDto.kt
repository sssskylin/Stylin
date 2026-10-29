package wardrobe.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class ItemListDto(
    val items: List<ItemDto>
)
