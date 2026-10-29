package users.application.contracts

import users.application.contracts.dto.AuthDto
import users.application.contracts.dto.UserDto
import users.application.contracts.dto.UserLogsListDto
import users.application.contracts.requests.ChangeBioRequest
import users.application.contracts.requests.ChangeEmailRequest
import users.application.contracts.requests.ChangeNameRequest
import users.application.contracts.requests.ChangeUsernameRequest
import users.application.contracts.requests.DeleteAccountRequest
import users.application.contracts.requests.LoginRequest
import users.application.contracts.requests.RegisterRequest
import java.util.UUID

interface UserService {
    fun register(req: RegisterRequest): AuthDto;
    fun login(req: LoginRequest): AuthDto;
    fun getUserInfo(userId: UUID): UserDto;
    fun logs(userId: UUID, limit: Int): UserLogsListDto;
    fun updateEmail(req: ChangeEmailRequest): UserDto;
    fun updateUsername(req: ChangeUsernameRequest): UserDto;
    fun updateName(req: ChangeNameRequest): UserDto;
    fun updateBio(req: ChangeBioRequest): UserDto;
    fun deleteAccount(req: DeleteAccountRequest);
}