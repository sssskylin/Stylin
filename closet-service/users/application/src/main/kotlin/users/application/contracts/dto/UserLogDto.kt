package users.application.contracts.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserLogDto(val id: String, val type: String, val createdAt: String)