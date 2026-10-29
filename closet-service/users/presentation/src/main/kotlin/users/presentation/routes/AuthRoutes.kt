package users.presentation.routes

import users.application.contracts.requests.LoginRequest
import users.application.contracts.requests.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import users.application.contracts.UserService
import users.domain.exceptions.InvalidCredentialsException
import users.domain.exceptions.InvalidUserDataException
import users.presentation.io

fun Route.authRoutes(users: UserService) {
    route("/auth") {
        post("/register") {
            val body = call.receive<RegisterRequest>()
            val result = io {
                users.register(RegisterRequest(body.email, body.username, body.name, body.bio, body.password))
            }
            call.respond(HttpStatusCode.Created, result)
        }

        post("/login") {
            val body = call.receive<LoginRequest>()
            val result = io {
                try {
                    users.login(LoginRequest(body.email, body.password))
                } catch (e: InvalidUserDataException) {
                    throw InvalidCredentialsException()
                }
            }
            call.respond(result)
        }
    }
}