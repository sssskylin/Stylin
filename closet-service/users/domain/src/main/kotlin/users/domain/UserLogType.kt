package users.domain

enum class UserLogType {
    REGISTERED,
    LOGGED_IN,
    PROFILE_VIEWED,
    LOGS_VIEWED,
    EMAIL_CHANGED,
    USERNAME_CHANGED,
    NAME_CHANGED,
    BIO_CHANGED,
    ACCOUNT_DELETED,
}