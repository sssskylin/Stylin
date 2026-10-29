package wardrobe.domain.exceptions

class ImageTooLargeException(val maxBytes: Int) : WardrobeException("Image must not exceed $maxBytes bytes")
