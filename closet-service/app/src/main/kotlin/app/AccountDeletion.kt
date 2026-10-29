package app

import wardrobe.infrastructure.abstractions.ImageStorage
import org.slf4j.LoggerFactory
import users.presentation.routes.AccountDeleter

class AccountDeletion(
    private val users: users.application.contracts.UserService,
    private val items: wardrobe.application.contracts.ItemService,
    private val storage: ImageStorage,
) : AccountDeleter {
    private val log = LoggerFactory.getLogger(AccountDeletion::class.java)

    override fun delete(req: users.application.contracts.requests.DeleteAccountRequest) {
        val keys = items.imageKeys(req.userId)
        users.deleteAccount(req)
        runCatching { storage.deleteAll(keys) }
            .onFailure { log.warn("Account {} deleted, but {} images were not removed from storage", req.userId, keys.size, it) }
    }
}
