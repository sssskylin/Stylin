package wardrobe.application.contracts.mapping

import wardrobe.domain.Board
import wardrobe.domain.Item
import wardrobe.infrastructure.s3.S3ImageStorage

class WardrobeMapper(storage: S3ImageStorage) {
    fun toDto(board: Board): wardrobe.application.contracts.dto.BoardDto =
        _root_ide_package_.wardrobe.application.contracts.dto.BoardDto(
            id = board.id.toString(),
            userId = board.userId.toString(),
            title = board.title,
            itemCount = board.itemCount
        )

    fun toDto(item: Item): wardrobe.application.contracts.dto.ItemDto =
        _root_ide_package_.wardrobe.application.contracts.dto.ItemDto(
            id = item.id.toString(),
            boardId = item.boardId.toString(),
            userId = item.userId.toString(),
            title = item.title,
            description = item.description,
            imageUrl = item.image.key
        )

    fun toDto(board: Board, items: List<Item>): wardrobe.application.contracts.dto.BoardWithItemsDto =
        _root_ide_package_.wardrobe.application.contracts.dto.BoardWithItemsDto(
            board = toDto(board),
            items = items.map { toDto(it) }
        )
}