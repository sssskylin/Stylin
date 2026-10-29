package users.presentation

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

internal suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

internal val ApplicationCall.userId: UUID
    get() = UUID.fromString(principal<UserIdPrincipal>()?.name ?: error("users.presentation.userId requested outside of an authenticated route"))

internal fun ApplicationCall.intQuery(name: String, default: Int): Int {
    val raw = request.queryParameters[name] ?: return default
    return raw.toIntOrNull() ?: throw BadRequestException("Query parameter '$name' must be an integer")
}

internal suspend fun ApplicationCall.respondError(status: HttpStatusCode, code: String, message: String) =
    respond(status, mapOf("code" to code, "message" to message))