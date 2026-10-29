package users.application.contracts.requests

data class LoginRequest(
    val email: String,
    val password: String,
)