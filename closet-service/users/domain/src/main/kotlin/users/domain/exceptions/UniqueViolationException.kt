package users.domain.exceptions

class UniqueViolationException(
    val constraint: String,
    cause: Throwable? = null,
) : RuntimeException(
    "Unique constraint violation: $constraint",
    cause,
)