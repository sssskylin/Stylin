package wardrobe.presentation

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.response.respond
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal

internal suspend fun <T> io(block: () -> T): T = withContext(Dispatchers.IO) { block() }

internal val ApplicationCall.userId: UUID
    get() = UUID.fromString(principal<UserIdPrincipal>()?.name ?: error("wardrobe.presentation.userId requested outside of an authenticated route"))

internal fun ApplicationCall.uuidParam(name: String): UUID =
    runCatching { UUID.fromString(parameters[name]) }.getOrNull()
        ?: throw BadRequestException("Path parameter '$name' must be a valid UUID")

internal suspend fun ApplicationCall.respondError(status: HttpStatusCode, code: String, message: String) =
    respond(status, mapOf("code" to code, "message" to message))