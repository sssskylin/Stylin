package users.presentation.routes

import users.application.contracts.requests.DeleteAccountRequest

fun interface AccountDeleter {
    fun delete(req: DeleteAccountRequest)
}