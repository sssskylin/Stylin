package users.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserListDto(
    val users: List<users.application.contracts.dto.UserDto>
)
