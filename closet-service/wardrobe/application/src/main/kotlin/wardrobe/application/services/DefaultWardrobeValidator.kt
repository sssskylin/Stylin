package wardrobe.application.services

import wardrobe.application.contracts.WardrobeValidator
import wardrobe.domain.exceptions.InvalidWardrobeDataException

class DefaultWardrobeValidator : WardrobeValidator {

    private companion object {
        const val BOARD_NAME_MAX = 100
        const val TITLE_MAX = 100
    }

    override fun boardName(raw: String): String {
        val v = raw.trim()
        if (v.isEmpty() || v.length > BOARD_NAME_MAX) invalid("Board name must be 1-$BOARD_NAME_MAX characters")
        return v
    }

    override fun itemTitle(raw: String?): String? {
        val v = raw?.trim()?.ifBlank { null } ?: return null
        if (v.length > TITLE_MAX) invalid("Title must be at most $TITLE_MAX characters")
        return v
    }

    private fun invalid(message: String): Nothing = throw InvalidWardrobeDataException(message)
}