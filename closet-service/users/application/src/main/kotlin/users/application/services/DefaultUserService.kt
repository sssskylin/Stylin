package users.application.services

import users.domain.User
import users.domain.UserLogType
import users.infrastructure.abstractions.PasswordHasher
import users.infrastructure.abstractions.TokenIssuer
import users.infrastructure.abstractions.TransactionRunner
import users.infrastructure.abstractions.UserLogRepository
import users.infrastructure.abstractions.UserRepository
import users.domain.AuthResult
import users.domain.UserLog
import users.domain.exceptions.EmailAlreadyTakenException
import users.domain.exceptions.InvalidCredentialsException
import users.domain.exceptions.UserNotFoundException
import users.domain.exceptions.UsernameAlreadyTakenException
import java.time.Clock
import java.util.UUID

class DefaultUserService(
    private val users: UserRepository,
    private val logs: UserLogRepository,
    private val hasher: PasswordHasher,
    private val tokens: TokenIssuer,
    private val tx: TransactionRunner,
    private val clock: Clock,
    private val validator: users.application.contracts.UserValidator,
    private val mapper: users.application.contracts.mapping.UserMapper,
    private val ids: () -> UUID = UUID::randomUUID,
) : users.application.contracts.UserService {

    override fun register(req: users.application.contracts.requests.RegisterRequest): users.application.contracts.dto.AuthDto {
        val email = validator.email(req.email)
        val username = validator.username(req.username)
        val name = validator.name(req.name)
        val bio = validator.bio(req.bio)
        validator.password(req.password)

        val user = User(
            id = ids(),
            email = email,
            username = username,
            name = name,
            bio = bio,
            passwordHash = hasher.hash(req.password),
            createdAt = clock.instant(),
        )

        tx.inTransaction {
            users.save(user)
            log(user.id, UserLogType.REGISTERED)
        }

        return mapper.toDto(AuthResult(user, tokens.issue(user.id)))
    }

    override fun login(req: users.application.contracts.requests.LoginRequest): users.application.contracts.dto.AuthDto {
        val normalized = validator.email(req.email)
        val user = users.findByEmail(normalized)
        if (user == null || !hasher.matches(req.password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }
        tx.inTransaction { log(user.id, UserLogType.LOGGED_IN) }

        return mapper.toDto(AuthResult(user, tokens.issue(user.id)))
    }

    override fun getUserInfo(userId: UUID): users.application.contracts.dto.UserDto = tx.inTransaction {
        val user = users.findById(userId) ?: throw UserNotFoundException()
        log(user.id, UserLogType.PROFILE_VIEWED)
        mapper.toUserDto(user)
    }

    fun getAllUsers(): users.application.contracts.dto.UserListDto = tx.inTransaction {
        val allUsers = users.findAll()
        mapper.toUserListDto(allUsers)
    }

    override fun logs(userId: UUID, limit: Int): users.application.contracts.dto.UserLogsListDto = tx.inTransaction {
        users.findById(userId) ?: throw UserNotFoundException()
        log(userId, UserLogType.LOGS_VIEWED)

        val userLogs = logs.recent(userId, limit.coerceIn(1, 200))
        mapper.toUserLogListDto(userLogs)
    }

    override fun updateEmail(req: users.application.contracts.requests.ChangeEmailRequest): users.application.contracts.dto.UserDto =
        mutate(req.userId, UserLogType.EMAIL_CHANGED) { user ->
            requirePassword(user, req.currentPassword)
            val newEmail = validator.email(req.newEmail)
            if (newEmail == user.email) return@mutate user
            if (users.existsByEmail(newEmail, excludingId = user.id)) throw EmailAlreadyTakenException()
            user.copy(email = newEmail)
        }

    override fun updateUsername(req: users.application.contracts.requests.ChangeUsernameRequest): users.application.contracts.dto.UserDto =
        mutate(req.userId, UserLogType.USERNAME_CHANGED) { user ->
            val newUsername = validator.username(req.newUsername)
            if (newUsername == user.username) return@mutate user
            if (users.existsByUsername(newUsername, excludingId = user.id)) throw UsernameAlreadyTakenException()
            user.copy(username = newUsername)
        }

    override fun updateName(req: users.application.contracts.requests.ChangeNameRequest): users.application.contracts.dto.UserDto =
        mutate(req.userId, UserLogType.NAME_CHANGED) { user ->
            val newName = validator.name(req.newName)
            if (newName == user.name) return@mutate user
            user.copy(name = newName)
        }

    override fun updateBio(req: users.application.contracts.requests.ChangeBioRequest): users.application.contracts.dto.UserDto =
        mutate(req.userId, UserLogType.BIO_CHANGED) { user ->
            val newBio = validator.bio(req.newBio)
            if (newBio == user.bio) return@mutate user
            user.copy(bio = newBio)
        }

    override fun deleteAccount(req: users.application.contracts.requests.DeleteAccountRequest) {
        tx.inTransaction {
            val user = users.findById(req.userId) ?: throw UserNotFoundException()
            requirePassword(user, req.currentPassword)
            log(user.id, UserLogType.ACCOUNT_DELETED)
            users.deleteById(user.id)
        }
    }

    private fun mutate(
        userId: UUID,
        logType: UserLogType,
        block: (User) -> User,
    ): users.application.contracts.dto.UserDto = tx.inTransaction {
        val user = users.findById(userId) ?: throw UserNotFoundException()
        val updated = block(user)
        if (updated !== user) {
            users.save(updated)
            log(user.id, logType)
        }
        mapper.toUserDto(updated)
    }

    private fun requirePassword(user: User, raw: String) {
        if (!hasher.matches(raw, user.passwordHash)) throw InvalidCredentialsException()
    }

    private fun log(userId: UUID, type: UserLogType) {
        logs.add(UserLog(ids(), userId, type, clock.instant()))
    }
}