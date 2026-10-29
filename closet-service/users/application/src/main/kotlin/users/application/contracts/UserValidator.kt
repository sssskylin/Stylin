package users.application.contracts

interface UserValidator {
    fun email(raw: String): String
    fun username(raw: String): String
    fun name(raw: String): String
    fun bio(raw: String?): String?
    fun password(raw: String)
}