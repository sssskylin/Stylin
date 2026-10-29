package wardrobe.presentation.routes

import wardrobe.presentation.io
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import java.util.UUID
import io.ktor.server.response.respond
import io.ktor.server.routing.patch
import wardrobe.application.contracts.BoardService
import wardrobe.application.contracts.ItemService
import wardrobe.application.contracts.mapping.WardrobeMapper
import wardrobe.application.contracts.requests.CreateBoardRequest
import wardrobe.application.contracts.requests.RenameBoardRequest
import wardrobe.presentation.userId

fun Route.boardRoutes(boards: BoardService, items: ItemService, mapper: WardrobeMapper, maxPhotoBytes: Int) {
    route("/boards") {
        get {
            val userId = call.userId
            call.respond(io { boards.list(userId) })
        }

        post {
            val userId = call.userId
            val body = call.receive<CreateBoardRequest>()
            val board = io { boards.create(body.copy(userId = userId)) }
            call.respond(HttpStatusCode.Created, board)
        }

        route("/{boardId}") {
            get {
                val userId = call.userId
                val boardId = UUID.fromString(call.parameters["boardId"])
                call.respond(io { boards.get(userId, boardId) })
            }

            patch {
                val userId = call.userId
                val boardId = UUID.fromString(call.parameters["boardId"])
                val body = call.receive<RenameBoardRequest>()
                val board = io { boards.rename(body.copy(userId = userId, boardId = boardId)) }
                call.respond(board)
            }

            delete {
                val userId = call.userId
                val boardId = UUID.fromString(call.parameters["boardId"])
                io { boards.delete(userId, boardId) }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}