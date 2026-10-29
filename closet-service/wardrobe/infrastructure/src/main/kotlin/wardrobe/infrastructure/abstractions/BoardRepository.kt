package wardrobe.infrastructure.abstractions

import wardrobe.domain.Board
import java.util.UUID

interface BoardRepository {
    fun insert(board: Board)
    fun findById(id: UUID, userId: UUID): Board?

    fun findByIdForUpdate(id: UUID, userId: UUID): Board?
    fun findAll(userId: UUID): List<Board>
    fun rename(id: UUID, userId: UUID, name: String)
    fun delete(id: UUID, userId: UUID): Boolean
}