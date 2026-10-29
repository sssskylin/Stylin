package app

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import users.presentation.routes.authRoutes
import wardrobe.presentation.routes.boardRoutes
import users.presentation.routes.userRoutes

fun Application.configureRouting(deps: Dependencies, config: AppConfig) {
    routing {
        get("/health") { call.respond(HealthResponse("ok")) }

        route("/api/v1") {
            authRoutes(deps.users)

            authenticate(JWT_AUTH) {
                userRoutes(deps.users, deps.accountDeleter)
                boardRoutes(deps.boards, deps.items, deps.mapper, config.maxPhotoBytes)
            }
        }
    }
}
