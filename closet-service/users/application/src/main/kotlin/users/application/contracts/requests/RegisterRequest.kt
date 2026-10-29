package users.application.contracts.requests

data class RegisterRequest(
    val email: String,
    val username: String,
    val name: String,
    val bio: String?,
    val password: String,
)