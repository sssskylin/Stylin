package wardrobe.application.contracts

import wardrobe.application.contracts.dto.ItemDto
import wardrobe.application.contracts.requests.CreateItemRequest
import wardrobe.application.contracts.requests.UpdateItemPhotoRequest
import wardrobe.application.contracts.requests.UpdateItemRequest
import java.util.UUID

interface ItemService {
    fun create(req: CreateItemRequest): ItemDto
    fun update(req: UpdateItemRequest): ItemDto
    fun replacePhoto(req: UpdateItemPhotoRequest): ItemDto
    fun delete(userId: UUID, boardId: UUID, itemId: UUID)
    fun imageKeys(userId: UUID): List<String>
}