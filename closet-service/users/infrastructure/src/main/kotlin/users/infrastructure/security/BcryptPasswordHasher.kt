package users.infrastructure.security

import users.infrastructure.abstractions.PasswordHasher
import at.favre.lib.crypto.bcrypt.BCrypt

class BcryptPasswordHasher(private val cost: Int = 12) : PasswordHasher {

    override fun hash(raw: String): String =
        BCrypt.withDefaults().hashToString(cost, raw.toCharArray())

    override fun matches(raw: String, hash: String): Boolean =
        BCrypt.verifyer().verify(raw.toCharArray(), hash).verified
}