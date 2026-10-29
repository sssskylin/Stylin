package wardrobe.infrastructure.postgres

import wardrobe.domain.Board
import wardrobe.infrastructure.JdbcWardrobeTransactionRunner
import wardrobe.infrastructure.abstractions.BoardRepository
import java.sql.ResultSet
import java.util.UUID
import kotlin.use

class PostgresBoardRepository(
    private val tx: JdbcWardrobeTransactionRunner,
) : BoardRepository {

    override fun insert(board: Board) {
        tx.connection().prepareStatement(
            "INSERT INTO boards (id, user_id, title, item_count) VALUES (?, ?, ?, ?)"
        ).use { st ->
            st.setObject(1, board.id)
            st.setObject(2, board.userId)
            st.setString(3, board.title)
            st.setInt(4, board.itemCount)
            st.executeUpdate()
        }
    }

    override fun findById(id: UUID, userId: UUID): Board? = find(id, userId, "")

    override fun findByIdForUpdate(id: UUID, userId: UUID): Board? = find(id, userId, " FOR UPDATE")

    private fun find(id: UUID, userId: UUID, lock: String): Board? =
        tx.connection().prepareStatement(
            "SELECT id, user_id, title, item_count FROM boards WHERE id = ? AND user_id = ?$lock"
        ).use { st ->
            st.setObject(1, id)
            st.setObject(2, userId)
            st.executeQuery().use { rs -> if (rs.next()) rs.toBoard() else null }
        }

    override fun findAll(userId: UUID): List<Board> =
        tx.connection().prepareStatement(
            """
            SELECT b.id, b.user_id, b.title,
                   (SELECT count(*) FROM items i WHERE i.board_id = b.id) AS item_count
            FROM boards b
            WHERE b.user_id = ?
            ORDER BY b.id
            """.trimIndent()
        ).use { st ->
            st.setObject(1, userId)
            st.executeQuery().use { rs ->
                buildList { while (rs.next()) add(rs.toBoard()) }
            }
        }

    override fun rename(id: UUID, userId: UUID, name: String) {
        tx.connection().prepareStatement(
            "UPDATE boards SET title = ? WHERE id = ? AND user_id = ?"
        ).use { st ->
            st.setString(1, name)
            st.setObject(2, id)
            st.setObject(3, userId)
            st.executeUpdate()
        }
    }

    override fun delete(id: UUID, userId: UUID): Boolean =
        tx.connection().prepareStatement(
            "DELETE FROM boards WHERE id = ? AND user_id = ?"
        ).use { st ->
            st.setObject(1, id)
            st.setObject(2, userId)
            st.executeUpdate() > 0
        }

    private fun ResultSet.toBoard() = Board(
        id = getObject("id", UUID::class.java),
        userId = getObject("user_id", UUID::class.java),
        title = getString("title"),
        itemCount = getInt("item_count"),
    )
}