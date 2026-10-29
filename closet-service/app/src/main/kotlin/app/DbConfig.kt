package app

data class DbConfig(
    val url: String,
    val username: String,
    val password: String,
    val poolSize: Int,
)
