package wardrobe.infrastructure.abstractions

import wardrobe.domain.Item
import java.util.UUID

interface ItemRepository {
    fun insert(item: Item)
    fun findById(id: UUID, boardId: UUID, userId: UUID): Item?
    fun findByIdForUpdate(id: UUID, boardId: UUID, userId: UUID): Item?
    fun update(item: Item)
    fun delete(id: UUID, boardId: UUID, userId: UUID): Boolean
    fun findByBoard(boardId: UUID, userId: UUID): List<Item>
    fun countByBoard(boardId: UUID): Int
    fun imageKeysByBoard(boardId: UUID, userId: UUID): List<String>
    fun imageKeysByUser(userId: UUID): List<String>
}
