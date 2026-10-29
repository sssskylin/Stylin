
import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import wardrobe.application.contracts.WardrobeValidator
import wardrobe.application.contracts.dto.BoardDto
import wardrobe.application.contracts.dto.BoardWithItemsDto
import wardrobe.application.contracts.mapping.WardrobeMapper
import wardrobe.application.contracts.requests.CreateBoardRequest
import wardrobe.application.contracts.requests.RenameBoardRequest
import wardrobe.application.services.DefaultBoardService
import wardrobe.domain.Board
import wardrobe.domain.ImageRef
import wardrobe.domain.Item
import wardrobe.domain.exceptions.BoardNotFoundException
import wardrobe.infrastructure.abstractions.BoardRepository
import wardrobe.infrastructure.abstractions.ImageStorage
import wardrobe.infrastructure.abstractions.ItemRepository
import wardrobe.infrastructure.abstractions.TransactionRunner
import java.util.UUID

class DefaultBoardServiceTest {

    private lateinit var boards: BoardRepository
    private lateinit var items: ItemRepository
    private lateinit var storage: ImageStorage
    private lateinit var tx: TransactionRunner
    private lateinit var validator: WardrobeValidator
    private lateinit var mapper: WardrobeMapper
    private lateinit var service: DefaultBoardService

    private val userId = UUID.fromString("00000000-0000-0000-0000-0000000000aa")
    private val boardId = UUID.fromString("00000000-0000-0000-0000-0000000000bb")

    private fun board(
        id: UUID = boardId,
        userId: UUID = this.userId,
        title: String = "Summer",
        itemCount: Int = 0,
    ) = Board(id, userId, title, itemCount)

    private fun item(id: UUID = UUID.randomUUID()) = Item(
        id = id,
        userId = userId,
        boardId = boardId,
        title = "T-shirt",
        description = "Summer t-shirt",
        image = ImageRef(key = "image-$id.jpg")
    )

    @BeforeEach
    fun setUp() {
        boards = mockk(relaxed = false)
        items = mockk(relaxed = false)
        storage = mockk(relaxed = true)
        tx = mockk(relaxed = false)
        validator = mockk {
            every { boardName(any()) } answers { firstArg<String>().trim() }
        }
        mapper = mockk(relaxed = true)

        every { tx.inTransaction<Any?>(any()) } answers {
            firstArg<() -> Any?>().invoke()
        }

        service = DefaultBoardService(
            boards = boards,
            items = items,
            storage = storage,
            tx = tx,
            validator = validator,
            mapper = mapper,
            ids = { boardId },
        )
    }

    // ==================== create ====================

    @Test
    fun `create validates title, inserts board inside transaction and returns dto`() {
        val expected = board(title = "Summer")
        val dto = mockk<BoardDto>()

        every { boards.insert(expected) } just Runs
        every { mapper.toDto(expected) } returns dto

        val result = service.create(CreateBoardRequest(userId, "  Summer  "))

        assertThat(result).isSameAs(dto)
        verify(exactly = 1) { boards.insert(expected) }
        verify(exactly = 1) { tx.inTransaction<Any?>(any()) }
    }

    @Test
    fun `create propagates validation error and does not insert`() {
        every { validator.boardName(any()) } throws IllegalArgumentException("empty")

        assertThrows(IllegalArgumentException::class.java) {
            service.create(CreateBoardRequest(userId, ""))
        }
        verify(exactly = 0) { boards.insert(any()) }
    }

    @Test
    fun `create uses ids generator for new board`() {
        val generatedId = UUID.randomUUID()
        service = DefaultBoardService(
            boards = boards,
            items = items,
            storage = storage,
            tx = tx,
            validator = validator,
            mapper = mapper,
            ids = { generatedId }
        )

        val captured = slot<Board>()
        every { boards.insert(capture(captured)) } just Runs
        every { mapper.toDto(any<Board>()) } returns mockk<BoardDto>()

        service.create(CreateBoardRequest(userId = userId, title = "New"))

        assertThat(captured.captured.id).isEqualTo(generatedId)
        assertThat(captured.captured.userId).isEqualTo(userId)
        assertThat(captured.captured.itemCount).isEqualTo(0)
    }

    // ==================== get ====================

    @Test
    fun `get returns mapped dto with items`() {
        val b = board()
        val its = listOf(item(), item())
        val dto = mockk<BoardWithItemsDto>()

        every { boards.findById(boardId, userId) } returns b
        every { items.findByBoard(boardId, userId) } returns its
        every { mapper.toDto(b, its) } returns dto

        val result = service.get(userId, boardId)

        assertThat(result).isSameAs(dto)
        verify { tx.inTransaction<Any?>(any()) }
    }

    @Test
    fun `get throws BoardNotFound when board missing`() {
        every { boards.findById(boardId, userId) } returns null

        assertThrows(BoardNotFoundException::class.java) { service.get(userId, boardId) }
        verify(exactly = 0) { items.findByBoard(any(), any()) }
    }

    // ==================== list ====================

    @Test
    fun `list maps every board into dto`() {
        val b1 = board(id = UUID.randomUUID(), title = "A")
        val b2 = board(id = UUID.randomUUID(), title = "B")
        val dto1 = mockk<BoardDto>()
        val dto2 = mockk<BoardDto>()

        every { boards.findAll(userId) } returns listOf(b1, b2)
        every { mapper.toDto(b1) } returns dto1
        every { mapper.toDto(b2) } returns dto2

        val result = service.list(userId)

        assertThat(result.boards).containsExactly(dto1, dto2)
    }

    @Test
    fun `list returns empty list when no boards`() {
        every { boards.findAll(userId) } returns emptyList()

        val result = service.list(userId)

        assertThat(result.boards).isEmpty()
    }

    // ==================== rename ====================

    @Test
    fun `rename updates title, calls repository rename and returns updated dto`() {
        val current = board(title = "Old")
        val updatedDto = mockk<BoardDto>()

        every { boards.findByIdForUpdate(boardId, userId) } returns current
        every { boards.rename(boardId, userId, "New") } just Runs
        every { mapper.toDto(current.copy(title = "New")) } returns updatedDto

        val result = service.rename(RenameBoardRequest(boardId, userId, "New"))

        assertThat(result).isSameAs(updatedDto)
        verify { boards.rename(boardId, userId, "New") }
    }

    @Test
    fun `rename validates title before opening transaction`() {
        every { validator.boardName(any()) } throws IllegalArgumentException("bad")

        assertThrows(IllegalArgumentException::class.java) {
            service.rename(RenameBoardRequest(boardId, userId, "bad"))
        }
        verify(exactly = 0) { tx.inTransaction<Any?>(any()) }
        verify(exactly = 0) { boards.rename(any(), any(), any()) }
    }

    @Test
    fun `rename is no-op when title unchanged - no repository call`() {
        val current = board(title = "Same")
        every { boards.findByIdForUpdate(boardId, userId) } returns current
        every { mapper.toDto(current) } returns mockk()

        service.rename(RenameBoardRequest(boardId, userId, "Same"))

        verify(exactly = 0) { boards.rename(any(), any(), any()) }
    }

    @Test
    fun `rename throws BoardNotFound when board missing`() {
        every { boards.findByIdForUpdate(boardId, userId) } returns null

        assertThrows(BoardNotFoundException::class.java) {
            service.rename(RenameBoardRequest(boardId, userId, "New"))
        }
        verify(exactly = 0) { boards.rename(any(), any(), any()) }
    }

    // ==================== delete ====================

//    @Test
//    fun `delete removes board and deletes all image keys from storage`() {
//        val b = board()
//        val keys = listOf("a.jpg", "b.jpg")
//
//        every { boards.findByIdForUpdate(boardId, userId) } returns b
//        every { items.imageKeysByBoard(boardId, userId) } returns keys
//        every { boards.delete(boardId, userId) } just Runs
//        every { storage.deleteAll(keys) } just Runs
//
//        service.delete(userId, boardId)
//
//        verifyOrder {
//            boards.delete(boardId, userId)
//            storage.deleteAll(keys)
//        }
//    }

    @Test
    fun `delete throws BoardNotFound and does not touch storage`() {
        every { boards.findByIdForUpdate(boardId, userId) } returns null

        assertThrows(BoardNotFoundException::class.java) { service.delete(userId, boardId) }
        verify(exactly = 0) { boards.delete(any(), any()) }
        verify(exactly = 0) { storage.deleteAll(any()) }
    }

//    @Test
//    fun `delete swallows storage error - board still deleted`() {
//        val b = board()
//        val keys = listOf("a.jpg")
//
//        every { boards.findByIdForUpdate(boardId, userId) } returns b
//        every { items.imageKeysByBoard(boardId, userId) } returns keys
//        every { boards.delete(boardId, userId) } just Runs
//        every { storage.deleteAll(keys) } throws RuntimeException("storage down")
//
//        service.delete(userId, boardId)
//
//        verify { boards.delete(boardId, userId) }
//        verify { storage.deleteAll(keys) }
//    }

//    @Test
//    fun `delete does not touch storage when key list is empty`() {
//        val b = board()
//
//        every { boards.findByIdForUpdate(boardId, userId) } returns b
//        every { items.imageKeysByBoard(boardId, userId) } returns emptyList()
//        every { boards.delete(boardId, userId) } just Runs
//        every { storage.deleteAll(emptyList()) } just Runs
//
//        service.delete(userId, boardId)
//
//        verify { storage.deleteAll(emptyList()) }
//    }

    @Test
    fun `delete propagates repository exception and storage is never called`() {
        val b = board()
        every { boards.findByIdForUpdate(boardId, userId) } returns b
        every { items.imageKeysByBoard(boardId, userId) } returns listOf("a.jpg")
        every { boards.delete(boardId, userId) } throws RuntimeException("db down")

        assertThrows(RuntimeException::class.java) { service.delete(userId, boardId) }
        verify(exactly = 0) { storage.deleteAll(any()) }
    }
}