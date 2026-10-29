package users.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    val id: String,
    val email: String,
    val username: String,
    val name: String,
    val bio: String?,
    val createdAt: String,
)