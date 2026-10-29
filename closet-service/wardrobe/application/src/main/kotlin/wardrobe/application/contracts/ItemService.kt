package wardrobe.application.contracts

import wardrobe.application.contracts.dto.ItemDto
import wardrobe.application.contracts.requests.CreateItemRequest
import wardrobe.application.contracts.requests.UpdateItemPhotoRequest
import wardrobe.application.contracts.requests.UpdateItemRequest
import java.util.UUID

interface ItemService {
    fun create(req: wardrobe.application.contracts.requests.CreateItemRequest): wardrobe.application.contracts.dto.ItemDto
    fun update(req: wardrobe.application.contracts.requests.UpdateItemRequest): wardrobe.application.contracts.dto.ItemDto
    fun replacePhoto(req: wardrobe.application.contracts.requests.UpdateItemPhotoRequest): wardrobe.application.contracts.dto.ItemDto
    fun delete(userId: UUID, boardId: UUID, itemId: UUID)
    fun imageKeys(userId: UUID): List<String>
}