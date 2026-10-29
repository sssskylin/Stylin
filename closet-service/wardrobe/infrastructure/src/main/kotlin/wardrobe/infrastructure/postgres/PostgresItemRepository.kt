package wardrobe.infrastructure.postgres

import wardrobe.domain.ImageRef
import wardrobe.domain.Item
import wardrobe.infrastructure.JdbcWardrobeTransactionRunner
import wardrobe.infrastructure.abstractions.ItemRepository
import java.sql.ResultSet
import java.util.UUID
import kotlin.use

class PostgresItemRepository(
    private val tx: JdbcWardrobeTransactionRunner,
) : ItemRepository {

    private companion object {
        const val COLUMNS = "id, board_id, user_id, title, description, image_key"
    }

    override fun insert(item: Item) {
        tx.connection().prepareStatement(
            """
            INSERT INTO items (id, board_id, user_id, title, description, image_key)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { st ->
            st.setObject(1, item.id)
            st.setObject(2, item.boardId)
            st.setObject(3, item.userId)
            st.setString(4, item.title)
            st.setString(5, item.description)

            // ПРЕДПОЛОЖЕНИЕ: ImageRef хранит строку (например, URL или ключ S3) в поле .value или .key.
            // Если ваша ImageRef устроена иначе, замените item.image.toString() на нужное поле.
            st.setString(6, item.image.toString())

            st.executeUpdate()
        }
    }

    override fun findById(id: UUID, boardId: UUID, userId: UUID): Item? = find(id, boardId, userId, "")

    override fun findByIdForUpdate(id: UUID, boardId: UUID, userId: UUID): Item? =
        find(id, boardId, userId, " FOR UPDATE")

    private fun find(id: UUID, boardId: UUID, userId: UUID, lock: String): Item? =
        tx.connection().prepareStatement(
            "SELECT $COLUMNS FROM items WHERE id = ? AND board_id = ? AND user_id = ?$lock"
        ).use { st ->
            st.setObject(1, id)
            st.setObject(2, boardId)
            st.setObject(3, userId)
            st.executeQuery().use { rs -> if (rs.next()) rs.toItem() else null }
        }

    override fun update(item: Item) {
        tx.connection().prepareStatement(
            """
            UPDATE items
            SET title = ?, description = ?, image_key = ?
            WHERE id = ? AND board_id = ? AND user_id = ?
            """.trimIndent()
        ).use { st ->
            st.setString(1, item.title)
            st.setString(2, item.description)
            st.setString(3, item.image.toString())

            st.setObject(4, item.id)
            st.setObject(5, item.boardId)
            st.setObject(6, item.userId)
            st.executeUpdate()
        }
    }

    override fun delete(id: UUID, boardId: UUID, userId: UUID): Boolean =
        tx.connection().prepareStatement(
            "DELETE FROM items WHERE id = ? AND board_id = ? AND user_id = ?"
        ).use { st ->
            st.setObject(1, id)
            st.setObject(2, boardId)
            st.setObject(3, userId)
            st.executeUpdate() > 0
        }

    override fun findByBoard(boardId: UUID, userId: UUID): List<Item> =
        tx.connection().prepareStatement(
            "SELECT $COLUMNS FROM items WHERE board_id = ? AND user_id = ? ORDER BY id"
        ).use { st ->
            st.setObject(1, boardId)
            st.setObject(2, userId)
            st.executeQuery().use { rs -> buildList { while (rs.next()) add(rs.toItem()) } }
        }

    override fun countByBoard(boardId: UUID): Int =
        tx.connection().prepareStatement("SELECT count(*) FROM items WHERE board_id = ?").use { st ->
            st.setObject(1, boardId)
            st.executeQuery().use { rs -> if (rs.next()) rs.getInt(1) else 0 }
        }

    override fun imageKeysByBoard(boardId: UUID, userId: UUID): List<String> =
        tx.connection().prepareStatement(
            "SELECT image_key FROM items WHERE board_id = ? AND user_id = ?"
        ).use { st ->
            st.setObject(1, boardId)
            st.setObject(2, userId)
            st.executeQuery().use { rs -> buildList { while (rs.next()) add(rs.getString(1)) } }
        }

    override fun imageKeysByUser(userId: UUID): List<String> =
        tx.connection().prepareStatement("SELECT image_key FROM items WHERE user_id = ?").use { st ->
            st.setObject(1, userId)
            st.executeQuery().use { rs -> buildList { while (rs.next()) add(rs.getString(1)) } }
        }

    private fun ResultSet.toItem() = Item(
        id = getObject("id", UUID::class.java),
        boardId = getObject("board_id", UUID::class.java),
        userId = getObject("user_id", UUID::class.java),
        title = getString("title"),
        description = getString("description"),
        image = ImageRef(getString("image_key"))
    )
}