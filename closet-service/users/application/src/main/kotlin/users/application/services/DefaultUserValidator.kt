package users.application.services

import users.application.contracts.UserValidator
import users.domain.exceptions.InvalidUserDataException

class DefaultUserValidator : users.application.contracts.UserValidator {

    private companion object {
        val EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
        val USERNAME = Regex("^[A-Za-z0-9_.]{3,30}$")

        const val EMAIL_MAX = 255
        const val NAME_MAX = 100
        const val BIO_MAX = 500
        const val PASSWORD_MIN = 8
        const val PASSWORD_MAX_BYTES = 72
    }

    override fun email(raw: String): String {
        val v = raw.trim().lowercase()
        if (v.length > EMAIL_MAX || !EMAIL.matches(v)) {
            throw InvalidUserDataException("Email is invalid")
        }
        return v
    }

    override fun username(raw: String): String {
        val v = raw.trim()
        if (!USERNAME.matches(v)) {
            throw InvalidUserDataException("Username must be 3-30 chars: letters, digits, _ and .")
        }
        return v
    }

    override fun name(raw: String): String {
        val v = raw.trim()
        if (v.isEmpty() || v.length > NAME_MAX) {
            throw InvalidUserDataException("Name must be 1-$NAME_MAX characters")
        }
        return v
    }

    override fun bio(raw: String?): String? {
        val v = raw?.trim()?.ifBlank { null }
        if ((v?.length ?: 0) > BIO_MAX) {
            throw InvalidUserDataException("Bio must be at most $BIO_MAX characters")
        }
        return v
    }

    override fun password(raw: String) {
        if (raw.length < PASSWORD_MIN || raw.toByteArray().size > PASSWORD_MAX_BYTES) {
            throw InvalidUserDataException("Password must be $PASSWORD_MIN-$PASSWORD_MAX_BYTES bytes long")
        }
    }
}