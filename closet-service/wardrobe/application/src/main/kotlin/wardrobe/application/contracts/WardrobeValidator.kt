package wardrobe.application.contracts

interface WardrobeValidator {
    fun boardName(raw: String): String
    fun itemTitle(raw: String?): String?
}