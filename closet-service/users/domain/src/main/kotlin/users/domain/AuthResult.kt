package users.domain

data class AuthResult(val user: User, val token: AccessToken)