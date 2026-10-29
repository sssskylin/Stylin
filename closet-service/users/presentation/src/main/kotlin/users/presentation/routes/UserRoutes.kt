package users.presentation.routes

import users.presentation.io
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import users.application.contracts.UserService
import users.application.contracts.requests.ChangeBioRequest
import users.application.contracts.requests.ChangeEmailRequest
import users.application.contracts.requests.ChangeNameRequest
import users.application.contracts.requests.ChangeUsernameRequest
import users.application.contracts.requests.DeleteAccountRequest
import users.presentation.intQuery
import users.presentation.userId

fun Route.userRoutes(users: UserService, accountDeleter: AccountDeleter) {
    route("/users/me") {
        get {
            val userId = call.userId
            call.respond(io { users.getUserInfo(userId) })
        }

        put("/email") {
            val body = call.receive<ChangeEmailRequest>()
            val userId = call.userId
            val userDto = io { users.updateEmail(ChangeEmailRequest(userId, body.newEmail, body.currentPassword)) }
            call.respond(userDto)
        }

        put("/username") {
            val body = call.receive<ChangeUsernameRequest>()
            val userId = call.userId
            val userDto = io { users.updateUsername(ChangeUsernameRequest(userId, body.newUsername)) }
            call.respond(userDto)
        }

        put("/name") {
            val body = call.receive<ChangeNameRequest>()
            val userId = call.userId
            val userDto = io { users.updateName(ChangeNameRequest(userId, body.newName)) }
            call.respond(userDto)
        }

        put("/bio") {
            val body = call.receive<ChangeBioRequest>()
            val userId = call.userId
            val userDto = io { users.updateBio(ChangeBioRequest(userId, body.newBio)) }
            call.respond(userDto)
        }

        get("/logs") {
            val userId = call.userId
            val limit = call.intQuery("limit", 50)
            call.respond(io { users.logs(userId, limit) })
        }

        delete {
            val body = call.receive<DeleteAccountRequest>()
            val userId = call.userId
            io { accountDeleter.delete(DeleteAccountRequest(userId, body.currentPassword)) }
            call.respond(HttpStatusCode.NoContent)
        }
    }
}