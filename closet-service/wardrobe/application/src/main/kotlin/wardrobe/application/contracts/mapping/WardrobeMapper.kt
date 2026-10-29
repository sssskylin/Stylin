package wardrobe.application.contracts.mapping

import wardrobe.application.contracts.dto.BoardDto
import wardrobe.application.contracts.dto.BoardWithItemsDto
import wardrobe.application.contracts.dto.ItemDto
import wardrobe.domain.Board
import wardrobe.domain.Item
import wardrobe.infrastructure.s3.S3ImageStorage

class WardrobeMapper(storage: S3ImageStorage) {
    fun toDto(board: Board): BoardDto =
        BoardDto(
            id = board.id.toString(),
            userId = board.userId.toString(),
            title = board.title,
            itemCount = board.itemCount
        )

    fun toDto(item: Item): ItemDto =
        ItemDto(
            id = item.id.toString(),
            boardId = item.boardId.toString(),
            userId = item.userId.toString(),
            title = item.title,
            description = item.description,
            imageUrl = item.image.key
        )

    fun toDto(board: Board, items: List<Item>): BoardWithItemsDto =
        BoardWithItemsDto(
            board = toDto(board),
            items = items.map { toDto(it) }
        )
}