package users.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserLogsListDto(
    val logs: List<users.application.contracts.dto.UserLogDto>
)
