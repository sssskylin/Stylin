package users.domain.exceptions

class UsernameAlreadyTakenException(message: String = "username must be unique") : UsersException(message)