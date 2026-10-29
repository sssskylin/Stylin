package wardrobe.presentation

import wardrobe.domain.exceptions.BoardNotFoundException
import wardrobe.domain.exceptions.ImageStorageException
import wardrobe.domain.exceptions.InvalidWardrobeDataException
import wardrobe.domain.exceptions.ItemNotFoundException
import wardrobe.domain.exceptions.UnsupportedImageTypeException
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import wardrobe.domain.exceptions.ImageTooLargeException

fun StatusPagesConfig.registerWardrobeErrors() {
    exception<InvalidWardrobeDataException> { call, e ->
        call.respondError(HttpStatusCode.UnprocessableEntity, "invalid_data", e.message ?: "Invalid data")
    }
    exception<BoardNotFoundException> { call, _ ->
        call.respondError(HttpStatusCode.NotFound, "board_not_found", "Board not found")
    }
    exception<ItemNotFoundException> { call, _ ->
        call.respondError(HttpStatusCode.NotFound, "item_not_found", "Item not found")
    }
    exception<UnsupportedImageTypeException> { call, _ ->
        call.respondError(HttpStatusCode.UnsupportedMediaType, "unsupported_image", "Only JPEG, PNG and WebP images are supported")
    }
    exception<ImageTooLargeException> { call, e ->
        call.respondError(HttpStatusCode.PayloadTooLarge, "image_too_large", "Image must not exceed ${e.maxBytes} bytes")
    }
    exception<ImageStorageException> { call, _ ->
        call.respondError(HttpStatusCode.BadGateway, "storage_unavailable", "Image storage is temporarily unavailable")
    }
}