package users.application.services

import io.mockk.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import users.application.contracts.UserValidator
import users.application.contracts.dto.*
import users.application.contracts.mapping.UserMapper
import users.application.contracts.requests.*
import users.domain.*
import users.domain.exceptions.*
import users.infrastructure.abstractions.*
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class DefaultUserServiceTest {

    private lateinit var users: UserRepository
    private lateinit var logs: UserLogRepository
    private lateinit var hasher: PasswordHasher
    private lateinit var tokens: TokenIssuer
    private lateinit var tx: TransactionRunner
    private lateinit var validator: UserValidator
    private lateinit var mapper: UserMapper
    private lateinit var service: DefaultUserService

    private val fixedId = UUID.fromString("00000000-0000-0000-0000-000000000001")
    private val fixedNow = Instant.parse("2024-01-01T00:00:00Z")
    private val clock = Clock.fixed(fixedNow, ZoneOffset.UTC)

    private fun user(
        id: UUID = fixedId,
        email: String = "user@example.com",
        username: String = "user",
        name: String = "User",
        bio: String? = null,
        hash: String = "hashed",
    ) = User(id, email, username, name, bio, hash, fixedNow)

    @BeforeEach
    fun setUp() {
        users = mockk(relaxed = false)
        logs = mockk(relaxed = true)
        hasher = mockk()
        tokens = mockk()
        tx = mockk()

        every { tx.inTransaction<Any?>(any()) } answers {
            firstArg<() -> Any?>().invoke()
        }

        validator = mockk {
            every { email(any()) } answers { firstArg<String>().trim().lowercase() }
            every { username(any()) } answers { firstArg() }
            every { name(any()) } answers { firstArg() }
            every { bio(any()) } answers { firstArg() }
            every { password(any()) } just Runs
        }
        mapper = mockk(relaxed = true)

        service = DefaultUserService(
            users = users,
            logs = logs,
            hasher = hasher,
            tokens = tokens,
            tx = tx,
            clock = clock,
            validator = validator,
            mapper = mapper,
            ids = { fixedId },
        )
    }

    // ==================== register ====================

    @Test
    fun `register saves user, logs REGISTERED and returns auth dto`() {
        val req = RegisterRequest("NEW@Example.com", "newuser", "New", "hi", "secret")
        val expectedUser = user(email = "new@example.com", username = "newuser", name = "New", bio = "hi", hash = "H")
        val dto = mockk<AuthDto>()

        val expectedToken = AccessToken(value = "token", expiresAt = fixedNow)
        val expectedResult = AuthResult(expectedUser, expectedToken)

        every { hasher.hash("secret") } returns "H"
        every { users.save(any()) } just Runs

        every { tokens.issue(fixedId) } returns expectedToken

        every { mapper.toDto(expectedResult) } returns dto

        val result = service.register(req)

        assertThat(result).isSameAs(dto)
        verify(exactly = 1) { users.save(expectedUser) }
        verify { logs.add(match { it.type == UserLogType.REGISTERED && it.userId == fixedId }) }
        verify(exactly = 1) { tx.inTransaction<Any?>(any()) }
    }
    @Test
    fun `register propagates validator error and does not save`() {
        every { validator.email(any()) } throws IllegalArgumentException("bad email")

        assertThrows(IllegalArgumentException::class.java) {
            service.register(RegisterRequest("bad", "u", "n", null, "p"))
        }
        verify(exactly = 0) { users.save(any()) }
    }

    @Test
    fun `register rolls back logging if save throws`() {
        every { hasher.hash(any()) } returns "H"
        every { users.save(any()) } throws RuntimeException("db down")

        assertThrows(RuntimeException::class.java) {
            service.register(RegisterRequest("a@b.c", "u", "n", null, "p"))
        }
        verify(exactly = 0) { logs.add(any()) }
    }

    // ==================== login ====================

    @Test
    fun `login succeeds with valid credentials and logs LOGGED_IN`() {
        val u = user()
        val dto = mockk<AuthDto>()

        val expectedToken = AccessToken(value = "token", expiresAt = fixedNow)

        every { users.findByEmail("user@example.com") } returns u
        every { hasher.matches("secret", u.passwordHash) } returns true
        every { tokens.issue(u.id) } returns expectedToken
        every { mapper.toDto(AuthResult(u, expectedToken)) } returns dto

        val result = service.login(LoginRequest("USER@example.com", "secret"))

        assertThat(result).isSameAs(dto)
        verify { logs.add(match { it.type == UserLogType.LOGGED_IN }) }
    }

    @Test
    fun `login throws InvalidCredentials when user not found`() {
        every { users.findByEmail("user@example.com") } returns null

        assertThrows(InvalidCredentialsException::class.java) {
            service.login(LoginRequest("user@example.com", "secret"))
        }
        verify(exactly = 0) { logs.add(any()) }
    }

    @Test
    fun `login throws InvalidCredentials when password mismatch`() {
        val u = user()
        every { users.findByEmail("user@example.com") } returns u
        every { hasher.matches("wrong", u.passwordHash) } returns false

        assertThrows(InvalidCredentialsException::class.java) {
            service.login(LoginRequest("user@example.com", "wrong"))
        }
        verify(exactly = 0) { logs.add(any()) }
    }

    // ==================== getUserInfo ====================

    @Test
    fun `getUserInfo returns dto and logs PROFILE_VIEWED`() {
        val u = user()
        val dto = mockk<UserDto>()
        every { users.findById(fixedId) } returns u
        every { mapper.toUserDto(u) } returns dto

        assertThat(service.getUserInfo(fixedId)).isSameAs(dto)
        verify { logs.add(match { it.type == UserLogType.PROFILE_VIEWED }) }
    }

    @Test
    fun `getUserInfo throws UserNotFound`() {
        every { users.findById(fixedId) } returns null
        assertThrows(UserNotFoundException::class.java) { service.getUserInfo(fixedId) }
    }

    // ==================== getAllUsers ====================

    @Test
    fun `getAllUsers maps full list`() {
        val list = listOf(user(), user(id = UUID.randomUUID()))
        val dto = mockk<UserListDto>()
        every { users.findAll() } returns list
        every { mapper.toUserListDto(list) } returns dto

        assertThat(service.getAllUsers()).isSameAs(dto)
    }

    // ==================== logs ====================

    @Test
    fun `logs validates user, logs LOGS_VIEWED and clamps limit high`() {
        val u = user()
        val recent = listOf(UserLog(UUID.randomUUID(), u.id, UserLogType.LOGGED_IN, fixedNow))
        val dto = mockk<UserLogsListDto>()
        every { users.findById(fixedId) } returns u
        every { logs.recent(fixedId, 200) } returns recent
        every { mapper.toUserLogListDto(recent) } returns dto

        assertThat(service.logs(fixedId, 9999)).isSameAs(dto)
        verify { logs.add(match { it.type == UserLogType.LOGS_VIEWED }) }
    }

    @Test
    fun `logs clamps low limit to 1`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { logs.recent(fixedId, 1) } returns emptyList()
        every { mapper.toUserLogListDto(emptyList()) } returns mockk()

        service.logs(fixedId, -5)

        verify { logs.recent(fixedId, 1) }
    }

    @Test
    fun `logs throws UserNotFound`() {
        every { users.findById(fixedId) } returns null
        assertThrows(UserNotFoundException::class.java) { service.logs(fixedId, 10) }
    }

    // ==================== updateEmail ====================

    @Test
    fun `updateEmail changes email, saves and logs EMAIL_CHANGED`() {
        val u = user()
        val updated = u.copy(email = "new@example.com")
        val dto = mockk<UserDto>()
        every { users.findById(fixedId) } returns u
        every { hasher.matches("pwd", u.passwordHash) } returns true
        every { users.existsByEmail("new@example.com", fixedId) } returns false
        every { users.save(updated) } just Runs
        every { mapper.toUserDto(updated) } returns dto

        val result = service.updateEmail(
            ChangeEmailRequest(
                userId = fixedId,
                newEmail = "new@example.com",
                currentPassword = "pwd",
            )
        )

        assertThat(result).isSameAs(dto)
        verify { users.save(updated) }
        verify { logs.add(match { it.type == UserLogType.EMAIL_CHANGED }) }
    }

    @Test
    fun `updateEmail no-op when email unchanged - no save and no log`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { hasher.matches("pwd", u.passwordHash) } returns true
        every { mapper.toUserDto(u) } returns mockk()

        service.updateEmail(
            ChangeEmailRequest(
                userId = fixedId,
                newEmail = "USER@example.com",
                currentPassword = "pwd",
            )
        )

        verify(exactly = 0) { users.save(any()) }
        verify(exactly = 0) { logs.add(any()) }
    }

    @Test
    fun `updateEmail throws EmailAlreadyTaken`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { hasher.matches("pwd", u.passwordHash) } returns true
        every { users.existsByEmail("taken@example.com", fixedId) } returns true

        assertThrows(EmailAlreadyTakenException::class.java) {
            service.updateEmail(
                ChangeEmailRequest(
                    userId = fixedId,
                    newEmail = "taken@example.com",
                    currentPassword = "pwd",
                )
            )
        }
        verify(exactly = 0) { users.save(any()) }
    }

    @Test
    fun `updateEmail throws InvalidCredentials when password wrong`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { hasher.matches("bad", u.passwordHash) } returns false

        assertThrows(InvalidCredentialsException::class.java) {
            service.updateEmail(
                ChangeEmailRequest(
                    userId = fixedId,
                    newEmail = "new@example.com",
                    currentPassword = "bad",
                )
            )
        }
        verify(exactly = 0) { users.save(any()) }
    }

    @Test
    fun `updateEmail throws UserNotFound`() {
        every { users.findById(fixedId) } returns null
        assertThrows(UserNotFoundException::class.java) {
            service.updateEmail(ChangeEmailRequest(fixedId, "pwd", "new@example.com"))
        }
    }

    // ==================== updateUsername ====================

    @Test
    fun `updateUsername changes username and logs USERNAME_CHANGED`() {
        val u = user()
        val updated = u.copy(username = "newname")
        every { users.findById(fixedId) } returns u
        every { users.existsByUsername("newname", fixedId) } returns false
        every { users.save(updated) } just Runs
        every { mapper.toUserDto(updated) } returns mockk()

        service.updateUsername(ChangeUsernameRequest(fixedId, "newname"))

        verify { users.save(updated) }
        verify { logs.add(match { it.type == UserLogType.USERNAME_CHANGED }) }
    }

    @Test
    fun `updateUsername throws UsernameAlreadyTaken`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { users.existsByUsername("taken", fixedId) } returns true

        assertThrows(UsernameAlreadyTakenException::class.java) {
            service.updateUsername(ChangeUsernameRequest(fixedId, "taken"))
        }
        verify(exactly = 0) { users.save(any()) }
    }

    @Test
    fun `updateUsername no-op when same value`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { mapper.toUserDto(u) } returns mockk()

        service.updateUsername(ChangeUsernameRequest(fixedId, "user"))

        verify(exactly = 0) { users.save(any()) }
        verify(exactly = 0) { logs.add(any()) }
    }

    // ==================== updateName ====================

    @Test
    fun `updateName saves and logs NAME_CHANGED`() {
        val u = user()
        val updated = u.copy(name = "New Name")
        every { users.findById(fixedId) } returns u
        every { users.save(updated) } just Runs
        every { mapper.toUserDto(updated) } returns mockk()

        service.updateName(ChangeNameRequest(fixedId, "New Name"))

        verify { users.save(updated) }
        verify { logs.add(match { it.type == UserLogType.NAME_CHANGED }) }
    }

    @Test
    fun `updateName no-op when same value`() {
        val u = user(name = "Same")
        every { users.findById(fixedId) } returns u
        every { mapper.toUserDto(u) } returns mockk()

        service.updateName(ChangeNameRequest(fixedId, "Same"))

        verify(exactly = 0) { users.save(any()) }
        verify(exactly = 0) { logs.add(any()) }
    }

    // ==================== updateBio ====================

    @Test
    fun `updateBio saves and logs BIO_CHANGED`() {
        val u = user(bio = null)
        val updated = u.copy(bio = "hello")
        every { users.findById(fixedId) } returns u
        every { users.save(updated) } just Runs
        every { mapper.toUserDto(updated) } returns mockk()

        service.updateBio(ChangeBioRequest(fixedId, "hello"))

        verify { users.save(updated) }
        verify { logs.add(match { it.type == UserLogType.BIO_CHANGED }) }
    }

    @Test
    fun `updateBio no-op when same value`() {
        val u = user(bio = "same")
        every { users.findById(fixedId) } returns u
        every { mapper.toUserDto(u) } returns mockk()

        service.updateBio(ChangeBioRequest(fixedId, "same"))

        verify(exactly = 0) { users.save(any()) }
        verify(exactly = 0) { logs.add(any()) }
    }

    // ==================== deleteAccount ====================

    @Test
    fun `deleteAccount verifies password, logs ACCOUNT_DELETED and deletes`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { hasher.matches("pwd", u.passwordHash) } returns true
        every { users.deleteById(fixedId) } just Runs

        service.deleteAccount(DeleteAccountRequest(fixedId, "pwd"))

        verifyOrder {
            logs.add(match { it.type == UserLogType.ACCOUNT_DELETED })
            users.deleteById(fixedId)
        }
    }

    @Test
    fun `deleteAccount throws InvalidCredentials on wrong password and does not delete`() {
        val u = user()
        every { users.findById(fixedId) } returns u
        every { hasher.matches("bad", u.passwordHash) } returns false

        assertThrows(InvalidCredentialsException::class.java) {
            service.deleteAccount(DeleteAccountRequest(fixedId, "bad"))
        }
        verify(exactly = 0) { users.deleteById(any()) }
        verify(exactly = 0) { logs.add(any()) }
    }

    @Test
    fun `deleteAccount throws UserNotFound`() {
        every { users.findById(fixedId) } returns null

        assertThrows(UserNotFoundException::class.java) {
            service.deleteAccount(DeleteAccountRequest(fixedId, "pwd"))
        }
        verify(exactly = 0) { users.deleteById(any()) }
    }
}