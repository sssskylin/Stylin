package wardrobe.application.services

import wardrobe.domain.ImageRef
import wardrobe.domain.Item
import wardrobe.infrastructure.abstractions.BoardRepository
import wardrobe.infrastructure.abstractions.ImageStorage
import wardrobe.infrastructure.abstractions.ItemRepository
import wardrobe.infrastructure.abstractions.TransactionRunner
import wardrobe.domain.exceptions.BoardNotFoundException
import wardrobe.domain.exceptions.InvalidWardrobeDataException
import wardrobe.domain.exceptions.ItemNotFoundException
import wardrobe.application.contracts.ItemService
import wardrobe.application.contracts.WardrobeValidator
import wardrobe.application.contracts.dto.ItemDto
import wardrobe.application.contracts.mapping.WardrobeMapper
import wardrobe.application.contracts.requests.CreateItemRequest
import wardrobe.application.contracts.requests.UpdateItemPhotoRequest
import wardrobe.application.contracts.requests.UpdateItemRequest
import java.util.UUID
import io.github.oshai.kotlinlogging.KotlinLogging

class DefaultItemService(
    private val items: ItemRepository,
    private val boards: BoardRepository,
    private val storage: ImageStorage,
    private val tx: TransactionRunner,
    private val validator: WardrobeValidator,
    private val specifier: ImageSpecifier,
    private val mapper: WardrobeMapper,
    private val ids: () -> UUID = UUID::randomUUID,
) : ItemService {

    private val log = KotlinLogging.logger {}

    private companion object {
        const val MAX_ITEMS_PER_BOARD = 200
    }

    override fun create(req: CreateItemRequest): ItemDto {
        val title = validator.itemTitle(req.title)
        val image = specifier.inspect(req.photo.bytes)

        tx.inTransaction { boards.findById(req.boardId, req.userId) ?: throw BoardNotFoundException() }

        val itemId = ids()
        val key = newImageKey(req.userId, req.boardId, itemId, image.extension)

        storage.put(key, image.contentType, req.photo.bytes)
        try {
            val created = tx.inTransaction {
                boards.findByIdForUpdate(req.boardId, req.userId) ?: throw BoardNotFoundException()
                if (items.countByBoard(req.boardId) >= MAX_ITEMS_PER_BOARD) {
                    throw InvalidWardrobeDataException("Board can contain at most ${MAX_ITEMS_PER_BOARD} items")
                }

                val item = Item(
                    id = itemId,
                    userId = req.userId,
                    boardId = req.boardId,
                    title = req.title,
                    description = req.description,
                    image = ImageRef(key)
                )
                items.insert(item)
                item
            }
            return mapper.toDto(created)
        } catch (e: Throwable) {
            deleteImageSafely(key)
            throw e
        }
    }

    override fun update(req: UpdateItemRequest): ItemDto {
        val title = validator.itemTitle(req.title)

        return tx.inTransaction {
            val current = items.findByIdForUpdate(req.itemId, req.boardId, req.userId) ?: throw ItemNotFoundException()

            val updated = current.copy(title = req.title, description = req.description)
            if (updated == current) return@inTransaction mapper.toDto(current)

            items.update(updated)
            mapper.toDto(updated)
        }
    }

    override fun replacePhoto(req: UpdateItemPhotoRequest): ItemDto {
        val image = specifier.inspect(req.photo.bytes)
        tx.inTransaction { items.findById(req.itemId, req.boardId, req.userId) ?: throw ItemNotFoundException() }

        val newKey = newImageKey(req.userId, req.boardId, req.itemId, image.extension)
        storage.put(newKey, image.contentType, req.photo.bytes)

        val (oldImageRef, updated) = try {
            tx.inTransaction {
                val current = items.findByIdForUpdate(req.itemId, req.boardId, req.userId) ?: throw ItemNotFoundException()
                val newImageRef = ImageRef(newKey)
                val saved = current.copy(image = newImageRef)
                items.update(saved)
                current.image to saved
            }
        } catch (e: Throwable) {
            deleteImageSafely(newKey)
            throw e
        }

        oldImageRef.key?.let { deleteImageSafely(it) }
        return mapper.toDto(updated)
    }

    override fun delete(userId: UUID, boardId: UUID, itemId: UUID) {
        val removed = tx.inTransaction {
            val current = items.findByIdForUpdate(itemId, boardId, userId) ?: throw ItemNotFoundException()
            items.delete(itemId, boardId, userId)
            current
        }
        removed.image.key?.let { deleteImageSafely(it) }
    }

    override fun imageKeys(userId: UUID): List<String> =
        tx.inTransaction { items.imageKeysByUser(userId) }

    private fun newImageKey(userId: UUID, boardId: UUID, itemId: UUID, extension: String) =
        "users/$userId/boards/$boardId/items/$itemId/${ids()}.$extension"

    private fun deleteImageSafely(key: String) {
        runCatching { storage.delete(key) }
            .onFailure { log.warn("Failed to delete image {} from storage, orphaned object left", key, it) }
    }
}