package users.application.contracts.mapping

import users.application.contracts.dto.AuthDto
import users.application.contracts.dto.UserDto
import users.application.contracts.dto.UserListDto
import users.application.contracts.dto.UserLogDto
import users.application.contracts.dto.UserLogsListDto
import users.domain.User
import users.domain.UserLog
import users.domain.AuthResult

class UserMapper {
    fun toUserDto(user: User): UserDto {
        return UserDto(
            id = user.id.toString(),
            email = user.email,
            username = user.username,
            name = user.name,
            bio = user.bio,
            createdAt = user.createdAt.toString()
        )
    }

    fun toUserLogDto(log: UserLog): UserLogDto {
        return UserLogDto(
            id = log.id.toString(),
            type = log.type.name,
            createdAt = log.createdAt.toString()
        )
    }

    fun toDto(authResult: AuthResult): AuthDto {
        return AuthDto(
            user = toUserDto(authResult.user),
            accessToken = authResult.token.value,
            expiresAt = authResult.token.expiresAt.toString()
        )
    }

    fun toUserListDto(users: List<User>): UserListDto {
        return UserListDto(users.map { toUserDto(it) })
    }

    fun toUserLogListDto(logs: List<UserLog>): UserLogsListDto {
        return UserLogsListDto(logs.map { toUserLogDto(it) })
    }
}