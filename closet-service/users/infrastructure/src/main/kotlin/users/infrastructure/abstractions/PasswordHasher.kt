package users.infrastructure.abstractions

interface PasswordHasher {
    fun hash(raw: String): String
    fun matches(raw: String, hash: String): Boolean
}