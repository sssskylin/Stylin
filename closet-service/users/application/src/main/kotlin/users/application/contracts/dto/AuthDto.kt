package users.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthDto(
    val user: users.application.contracts.dto.UserDto,
    val accessToken: String,
    val expiresAt: String,
)