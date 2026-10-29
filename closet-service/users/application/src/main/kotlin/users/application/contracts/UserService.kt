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
    fun register(req: users.application.contracts.requests.RegisterRequest): users.application.contracts.dto.AuthDto;
    fun login(req: users.application.contracts.requests.LoginRequest): users.application.contracts.dto.AuthDto;
    fun getUserInfo(userId: UUID): users.application.contracts.dto.UserDto;
    fun logs(userId: UUID, limit: Int): users.application.contracts.dto.UserLogsListDto;
    fun updateEmail(req: users.application.contracts.requests.ChangeEmailRequest): users.application.contracts.dto.UserDto;
    fun updateUsername(req: users.application.contracts.requests.ChangeUsernameRequest): users.application.contracts.dto.UserDto;
    fun updateName(req: users.application.contracts.requests.ChangeNameRequest): users.application.contracts.dto.UserDto;
    fun updateBio(req: users.application.contracts.requests.ChangeBioRequest): users.application.contracts.dto.UserDto;
    fun deleteAccount(req: users.application.contracts.requests.DeleteAccountRequest);
}