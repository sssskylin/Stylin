package users.domain.exceptions

class UserAlreadyExistsException : UsersException("Email or username is already taken")
