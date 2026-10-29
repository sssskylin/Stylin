package wardrobe.presentation.routes

import wardrobe.presentation.io
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import wardrobe.application.contracts.ItemService
import wardrobe.application.contracts.requests.CreateItemRequest
import wardrobe.application.contracts.requests.UpdateItemPhotoRequest
import wardrobe.application.contracts.requests.UpdateItemRequest
import wardrobe.presentation.userId
import java.util.UUID

fun Route.itemRoutes(items: ItemService) {
    route("/boards/{boardId}/items") {
        post {
            val userId = call.userId
            val boardId = UUID.fromString(call.parameters["boardId"])
            val body = call.receive<CreateItemRequest>()
            val item = io { items.create(body.copy(userId = userId, boardId = boardId)) }
            call.respond(HttpStatusCode.Created, item)
        }

        route("/{itemId}") {
            patch {
                val userId = call.userId
                val boardId = UUID.fromString(call.parameters["boardId"])
                val itemId = UUID.fromString(call.parameters["itemId"])
                val body = call.receive<UpdateItemRequest>()
                val item = io {
                    items.update(body.copy(userId = userId, boardId = boardId, itemId = itemId))
                }
                call.respond(item)
            }

            put("/photo") {
                val userId = call.userId
                val boardId = UUID.fromString(call.parameters["boardId"])
                val itemId = UUID.fromString(call.parameters["itemId"])
                val body = call.receive<UpdateItemPhotoRequest>()
                val item = io {
                    items.replacePhoto(body.copy(userId = userId, boardId = boardId, itemId = itemId))
                }
                call.respond(item)
            }

            delete {
                val userId = call.userId
                val boardId = UUID.fromString(call.parameters["boardId"])
                val itemId = UUID.fromString(call.parameters["itemId"])
                io { items.delete(userId, boardId, itemId) }
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}