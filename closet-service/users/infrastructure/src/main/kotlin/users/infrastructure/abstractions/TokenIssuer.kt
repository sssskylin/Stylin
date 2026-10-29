package users.infrastructure.abstractions

import users.domain.AccessToken
import java.util.UUID

interface TokenIssuer {
    fun issue(userId: UUID): AccessToken
}
