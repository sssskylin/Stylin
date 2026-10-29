package wardrobe.domain

import java.util.UUID

data class Board(
    val id: UUID,
    val userId: UUID,
    val title: String,
    val itemCount: Int,
)
