package app

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.UnsupportedMediaTypeException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.ContentTransformationException
import io.ktor.server.response.respond
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.SerializationException
import users.presentation.registerUsersErrors
import wardrobe.presentation.registerWardrobeErrors

fun Application.installStatusPages() {
    install(StatusPages) {
        registerUsersErrors()
        registerWardrobeErrors()

        exception<UnsupportedMediaTypeException> { call, _ ->
            call.respondError(HttpStatusCode.UnsupportedMediaType, "unsupported_media_type", "Unsupported Content-Type for this endpoint")
        }
        exception<BadRequestException> { call, e -> call.respondBadRequest(e, ourMessage = e.message) }
        exception<ContentTransformationException> { call, e -> call.respondBadRequest(e, ourMessage = null) }
        exception<SerializationException> { call, e -> call.respondBadRequest(e, ourMessage = null) }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled error", cause)
            call.respondError(HttpStatusCode.InternalServerError, "internal_error", "Internal server error")
        }
    }
}

private suspend fun ApplicationCall.respondBadRequest(e: Throwable, ourMessage: String?) {
    val missing = generateSequence(e) { it.cause }.filterIsInstance<MissingFieldException>().firstOrNull()
    when {
        missing != null -> respondError(
            HttpStatusCode.UnprocessableEntity, "missing_fields",
            "Missing required fields: ${missing.missingFields.joinToString()}",
        )
        e.cause == null && ourMessage != null -> respondError(HttpStatusCode.BadRequest, "bad_request", ourMessage)
        else -> respondError(HttpStatusCode.BadRequest, "malformed_request", "Malformed request body")
    }
}

suspend fun ApplicationCall.respondError(status: HttpStatusCode, code: String, message: String) =
    respond(status, mapOf("code" to code, "message" to message))
