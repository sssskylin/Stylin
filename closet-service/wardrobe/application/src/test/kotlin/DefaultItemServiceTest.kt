package wardrobe.application.services

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import wardrobe.application.contracts.WardrobeValidator
import wardrobe.application.contracts.dto.ItemDto
import wardrobe.application.contracts.mapping.WardrobeMapper
import wardrobe.application.contracts.requests.CreateItemRequest
import wardrobe.application.contracts.requests.UpdateItemPhotoRequest
import wardrobe.application.contracts.requests.UpdateItemRequest
import wardrobe.domain.*
import wardrobe.domain.exceptions.BoardNotFoundException
import wardrobe.domain.exceptions.InvalidWardrobeDataException
import wardrobe.domain.exceptions.ItemNotFoundException
import wardrobe.infrastructure.abstractions.*
import java.util.UUID

class DefaultItemServiceTest {

    private lateinit var items: ItemRepository
    private lateinit var boards: BoardRepository
    private lateinit var storage: ImageStorage
    private lateinit var tx: TransactionRunner
    private lateinit var validator: WardrobeValidator
    private lateinit var specifier: ImageSpecifier
    private lateinit var mapper: WardrobeMapper
    private lateinit var service: DefaultItemService

    private val userId = UUID.fromString("00000000-0000-0000-0000-0000000000aa")
    private val boardId = UUID.fromString("00000000-0000-0000-0000-0000000000bb")
    private val itemId = UUID.fromString("00000000-0000-0000-0000-0000000000cc")
    private val generatedId = UUID.fromString("11111111-1111-1111-1111-111111111111")

    // Исправлено: Detected содержит только contentType и extension
    private val inspectedImage = ImageSpecifier.Detected(
        contentType = "image/jpeg",
        extension = "jpg"
    )

    private fun item(
        id: UUID = itemId,
        userId: UUID = this.userId,
        boardId: UUID = this.boardId,
        title: String = "Old Title",
        description: String? = "Old Description",
        imageKey: String = "users/$userId/boards/$boardId/items/$id/old.jpg"
    ) = Item(id, userId, boardId, title, description, ImageRef(imageKey))

    private fun board(id: UUID = boardId, userId: UUID = this.userId) =
        Board(id, userId, "Summer", 0)

    @BeforeEach
    fun setUp() {
        items = mockk(relaxed = false)
        boards = mockk(relaxed = false)
        storage = mockk(relaxed = true)
        tx = mockk(relaxed = false)
        validator = mockk {
            every { itemTitle(any()) } answers { firstArg<String>().trim() }
        }
        specifier = mockk {
            every { inspect(any()) } returns inspectedImage
        }
        mapper = mockk(relaxed = true)

        every { tx.inTransaction<Any?>(any()) } answers {
            firstArg<() -> Any?>().invoke()
        }

        service = DefaultItemService(
            items = items,
            boards = boards,
            storage = storage,
            tx = tx,
            validator = validator,
            specifier = specifier,
            mapper = mapper,
            ids = { generatedId },
        )
    }

    // ==================== create ====================

    @Test
    fun `create validates title, checks board, uploads image and inserts item`() {
        val bytes = byteArrayOf(1, 2, 3)
        val req = CreateItemRequest(
            userId = userId,
            boardId = boardId,
            title = "  New T-shirt  ",
            description = "Desc",
            photo = ImageUpload(bytes)
        )

        val expectedKey = "users/$userId/boards/$boardId/items/$generatedId/$generatedId.jpg"
        val expectedItem = Item(generatedId, userId, boardId, "New T-shirt", "Desc", ImageRef(expectedKey))
        val dto = mockk<ItemDto>()

        every { boards.findById(boardId, userId) } returns board()
        every { boards.findByIdForUpdate(boardId, userId) } returns board()
        every { items.countByBoard(boardId) } returns 10
        every { items.insert(expectedItem) } just Runs
        every { mapper.toDto(expectedItem) } returns dto

        val result = service.create(req)

        assertThat(result).isSameAs(dto)
        verifyOrder {
            boards.findById(boardId, userId)
            storage.put(expectedKey, "image/jpeg", bytes)
            boards.findByIdForUpdate(boardId, userId)
            items.countByBoard(boardId)
            items.insert(expectedItem)
        }
    }

    @Test
    fun `create throws BoardNotFound if board missing initially`() {
        every { boards.findById(boardId, userId) } returns null

        assertThrows(BoardNotFoundException::class.java) {
            service.create(CreateItemRequest(
                userId, boardId, "Title", null, ImageUpload(byteArrayOf())
            ))
        }
        verify(exactly = 0) { storage.put(any(), any(), any()) }
    }

    @Test
    fun `create throws BoardNotFound if board disappeared before insert (race condition)`() {
        every { boards.findById(boardId, userId) } returns board()
        every { boards.findByIdForUpdate(boardId, userId) } returns null

        assertThrows(BoardNotFoundException::class.java) {
            service.create(CreateItemRequest(
                userId, boardId, "Title", null, ImageUpload(byteArrayOf())
            ))
        }
        verify { storage.delete(any()) }
    }

    @Test
    fun `create throws InvalidWardrobeDataException when board limit reached`() {
        every { boards.findById(boardId, userId) } returns board()
        every { boards.findByIdForUpdate(boardId, userId) } returns board()
        every { items.countByBoard(boardId) } returns 200

        assertThrows(InvalidWardrobeDataException::class.java) {
            service.create(CreateItemRequest(
                userId, boardId, "Title", null, ImageUpload(byteArrayOf())
            ))
        }
        verify(exactly = 0) { items.insert(any()) }
        verify { storage.delete(any()) }
    }

    @Test
    fun `create rolls back image if db insert fails`() {
        every { boards.findById(boardId, userId) } returns board()
        every { boards.findByIdForUpdate(boardId, userId) } returns board()
        every { items.countByBoard(boardId) } returns 0
        every { items.insert(any()) } throws RuntimeException("db down")

        assertThrows(RuntimeException::class.java) {
            service.create(CreateItemRequest(
                userId, boardId, "Title", null, ImageUpload(byteArrayOf())
            ))
        }
        verify { storage.delete(any()) }
    }

    // ==================== update ====================

    @Test
    fun `update modifies title and description`() {
        val current = item()
        val updated = current.copy(title = "New Title", description = "New Desc")
        val dto = mockk<ItemDto>()

        every { items.findByIdForUpdate(itemId, boardId, userId) } returns current
        every { items.update(updated) } just Runs
        every { mapper.toDto(updated) } returns dto

        val req = UpdateItemRequest(userId, boardId, itemId, "New Title", "New Desc")
        val result = service.update(req)

        assertThat(result).isSameAs(dto)
        verify { items.update(updated) }
    }

    @Test
    fun `update is no-op if nothing changed`() {
        val current = item(title = "Same", description = "Same")
        every { items.findByIdForUpdate(itemId, boardId, userId) } returns current
        every { mapper.toDto(current) } returns mockk()

        val req = UpdateItemRequest(userId, boardId, itemId, "Same", "Same")
        service.update(req)

        verify(exactly = 0) { items.update(any()) }
    }

    @Test
    fun `update throws ItemNotFound`() {
        every { items.findByIdForUpdate(itemId, boardId, userId) } returns null

        assertThrows(ItemNotFoundException::class.java) {
            service.update(UpdateItemRequest(userId, boardId, itemId, "Title", null))
        }
    }

    // ==================== replacePhoto ====================

    @Test
    fun `replacePhoto uploads new image, updates db and deletes old image`() {
        val current = item(imageKey = "old.jpg")
        val newKey = "users/$userId/boards/$boardId/items/$itemId/$generatedId.jpg"
        val saved = current.copy(image = ImageRef(newKey))
        val dto = mockk<ItemDto>()
        val bytes = byteArrayOf(9, 9)

        every { items.findById(itemId, boardId, userId) } returns current
        every { items.findByIdForUpdate(itemId, boardId, userId) } returns current
        every { items.update(saved) } just Runs
        every { mapper.toDto(saved) } returns dto

        val req = UpdateItemPhotoRequest(userId, boardId, itemId, ImageUpload(bytes))
        val result = service.replacePhoto(req)

        assertThat(result).isSameAs(dto)
        verifyOrder {
            storage.put(newKey, "image/jpeg", bytes)
            items.update(saved)
            storage.delete("old.jpg")
        }
    }

    @Test
    fun `replacePhoto throws ItemNotFound initially`() {
        every { items.findById(itemId, boardId, userId) } returns null

        assertThrows(ItemNotFoundException::class.java) {
            service.replacePhoto(UpdateItemPhotoRequest(
                userId, boardId, itemId, ImageUpload(byteArrayOf())
            ))
        }
        verify(exactly = 0) { storage.put(any(), any(), any()) }
    }

    @Test
    fun `replacePhoto rolls back new image if db update fails`() {
        val current = item(imageKey = "old.jpg")
        val newKey = "users/$userId/boards/$boardId/items/$itemId/$generatedId.jpg"

        every { items.findById(itemId, boardId, userId) } returns current
        every { items.findByIdForUpdate(itemId, boardId, userId) } returns current
        every { items.update(any()) } throws RuntimeException("db down")

        assertThrows(RuntimeException::class.java) {
            service.replacePhoto(UpdateItemPhotoRequest(
                userId, boardId, itemId, ImageUpload(byteArrayOf())
            ))
        }

        verify { storage.delete(newKey) }
        verify(exactly = 0) { storage.delete("old.jpg") }
    }

    // ==================== delete ====================

//    @Test
//    fun `delete removes item from db and deletes image`() {
//        val current = item(imageKey = "key.jpg")
//
//        every { items.findByIdForUpdate(itemId, boardId, userId) } returns current
//        every { items.delete(itemId, boardId, userId) } just Runs
//
//        service.delete(userId, boardId, itemId)
//
//        verifyOrder {
//            items.delete(itemId, boardId, userId)
//            storage.delete("key.jpg")
//        }
//    }

    @Test
    fun `delete throws ItemNotFound and does not touch storage`() {
        every { items.findByIdForUpdate(itemId, boardId, userId) } returns null

        assertThrows(ItemNotFoundException::class.java) {
            service.delete(userId, boardId, itemId)
        }
        verify(exactly = 0) { storage.delete(any()) }
    }

    // ==================== imageKeys ====================

    @Test
    fun `imageKeys returns list from repository`() {
        val keys = listOf("a.jpg", "b.jpg")
        every { items.imageKeysByUser(userId) } returns keys

        val result = service.imageKeys(userId)

        assertThat(result).isEqualTo(keys)
    }
}