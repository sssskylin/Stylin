package wardrobe.application.services

import wardrobe.domain.exceptions.ImageTooLargeException
import wardrobe.domain.exceptions.InvalidWardrobeDataException
import wardrobe.domain.exceptions.UnsupportedImageTypeException

class ImageSpecifier(private val maxBytes: Int) {

    data class Detected(val contentType: String, val extension: String)

    fun inspect(bytes: ByteArray): Detected {
        if (bytes.isEmpty()) throw InvalidWardrobeDataException("Photo is empty")
        if (bytes.size > maxBytes) throw ImageTooLargeException(maxBytes)
        return when {
            bytes.startsWith(0xFF, 0xD8, 0xFF) -> Detected("image/jpeg", "jpg")
            bytes.startsWith(0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A) -> Detected("image/png", "png")
            bytes.isWebp() -> Detected("image/webp", "webp")
            else -> throw UnsupportedImageTypeException()
        }
    }

    private fun ByteArray.startsWith(vararg signature: Int): Boolean =
        size >= signature.size && signature.indices.all { this[it] == signature[it].toByte() }

    private fun ByteArray.isWebp(): Boolean =
        size >= 12 &&
                String(this, 0, 4, Charsets.US_ASCII) == "RIFF" &&
                String(this, 8, 4, Charsets.US_ASCII) == "WEBP"
}
