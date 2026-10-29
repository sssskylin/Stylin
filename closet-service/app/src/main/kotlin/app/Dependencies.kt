package app

import wardrobe.application.contracts.BoardService
import wardrobe.application.contracts.ItemService
import wardrobe.application.contracts.mapping.WardrobeMapper
import users.presentation.routes.AccountDeleter

class Dependencies(
    val users: users.application.contracts.UserService,
    val accountDeleter: AccountDeleter,
    val boards: wardrobe.application.contracts.BoardService,
    val items: wardrobe.application.contracts.ItemService,
    val mapper: wardrobe.application.contracts.mapping.WardrobeMapper,
    private val closeables: List<AutoCloseable>,
) : AutoCloseable {
    override fun close() = closeables.reversed().forEach { runCatching { it.close() } }
}
