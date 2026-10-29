package wardrobe.application.services

import io.github.oshai.kotlinlogging.KotlinLogging
import wardrobe.domain.Board
import wardrobe.infrastructure.abstractions.BoardRepository
import wardrobe.infrastructure.abstractions.ItemRepository
import wardrobe.infrastructure.abstractions.ImageStorage
import wardrobe.infrastructure.abstractions.TransactionRunner
import wardrobe.domain.exceptions.BoardNotFoundException
import wardrobe.application.contracts.BoardService
import wardrobe.application.contracts.WardrobeValidator
import wardrobe.application.contracts.dto.BoardDto
import wardrobe.application.contracts.dto.BoardListDto
import wardrobe.application.contracts.dto.BoardWithItemsDto
import wardrobe.application.contracts.mapping.WardrobeMapper
import wardrobe.application.contracts.requests.CreateBoardRequest
import wardrobe.application.contracts.requests.RenameBoardRequest
import java.util.UUID

class DefaultBoardService(
    private val boards: BoardRepository,
    private val items: ItemRepository,
    private val storage: ImageStorage,
    private val tx: TransactionRunner,
    private val validator: WardrobeValidator,
    private val mapper: WardrobeMapper,
    private val ids: () -> UUID = UUID::randomUUID,
) : BoardService {

    private val log = KotlinLogging.logger {}

    override fun create(req: CreateBoardRequest): BoardDto {
        val title = validator.boardName(req.title)
        val board = Board(
            id = ids(),
            userId = req.userId,
            title = title,
            itemCount = 0
        )
        tx.inTransaction { boards.insert(board) }
        return mapper.toDto(board)
    }

    override fun get(userId: UUID, boardId: UUID): BoardWithItemsDto = tx.inTransaction {
        val board = boards.findById(boardId, userId) ?: throw BoardNotFoundException()
        val boardItems = items.findByBoard(boardId, userId)
        mapper.toDto(board, boardItems)
    }

    override fun list(userId: UUID): BoardListDto =
        tx.inTransaction {
            BoardListDto(
                boards.findAll(userId).map { mapper.toDto(it) })
        }

    override fun rename(req: RenameBoardRequest): BoardDto {
        val newTitle = validator.boardName(req.title)
        return tx.inTransaction {
            val current = boards.findByIdForUpdate(req.boardId, req.userId) ?: throw BoardNotFoundException()
            if (current.title == newTitle) return@inTransaction mapper.toDto(current)

            boards.rename(current.id, current.userId, newTitle)
            mapper.toDto(current.copy(title = newTitle))
        }
    }

    override fun delete(userId: UUID, boardId: UUID) {
        val keys = tx.inTransaction {
            boards.findByIdForUpdate(boardId, userId) ?: throw BoardNotFoundException()
            val keys = items.imageKeysByBoard(boardId, userId)
            boards.delete(boardId, userId)
            keys
        }
        runCatching { storage.deleteAll(keys) }
            .onFailure { log.warn("Board {} deleted, but {} images were not removed from storage", boardId, keys.size, it) }
    }
}