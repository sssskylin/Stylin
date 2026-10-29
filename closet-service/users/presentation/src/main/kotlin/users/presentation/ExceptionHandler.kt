package users.presentation

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import users.domain.exceptions.EmailAlreadyTakenException
import users.domain.exceptions.InvalidCredentialsException
import users.domain.exceptions.InvalidUserDataException
import users.domain.exceptions.UserNotFoundException
import users.domain.exceptions.UsernameAlreadyTakenException

fun StatusPagesConfig.registerUsersErrors() {
    exception<InvalidUserDataException> { call, e ->
        call.respondError(HttpStatusCode.UnprocessableEntity, "invalid_user_data", e.message ?: "Invalid data")
    }
    exception<InvalidCredentialsException> { call, _ ->
        call.respondError(HttpStatusCode.Unauthorized, "invalid_credentials", "Invalid credentials")
    }
    exception<UserNotFoundException> { call, _ ->
        call.respondError(HttpStatusCode.NotFound, "user_not_found", "User not found")
    }
    exception<EmailAlreadyTakenException> { call, _ ->
        call.respondError(HttpStatusCode.Conflict, "email_taken", "Email is already taken")
    }
    exception<UsernameAlreadyTakenException> { call, _ ->
        call.respondError(HttpStatusCode.Conflict, "username_taken", "Username is already taken")
    }
}