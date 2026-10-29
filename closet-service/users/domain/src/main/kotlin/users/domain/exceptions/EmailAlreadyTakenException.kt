package users.domain.exceptions

class EmailAlreadyTakenException(message: String = "email must be unique") : UsersException(message)