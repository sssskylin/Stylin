package app

import wardrobe.infrastructure.s3.S3Config
import users.infrastructure.security.JwtConfig
import java.time.Duration

data class AppConfig(
    val port: Int,
    val db: DbConfig,
    val jwt: JwtConfig,
    val s3: S3Config,
    val maxPhotoBytes: Int,
) {
    companion object {
        fun fromEnv(): AppConfig {
            val secret = required("JWT_SECRET")
            require(secret.toByteArray().size >= 32) { "JWT_SECRET must be at least 32 bytes long" }

            return AppConfig(
                port = optional("PORT")?.toInt() ?: 8080,
                db = DbConfig(
                    url = required("DB_URL"),
                    username = required("DB_USERNAME"),
                    password = required("DB_PASSWORD"),
                    poolSize = optional("DB_POOL_SIZE")?.toInt() ?: 10,
                ),
                jwt = JwtConfig(
                    secret = secret,
                    issuer = optional("JWT_ISSUER") ?: "closet",
                    audience = optional("JWT_AUDIENCE") ?: "closet-clients",
                    ttl = Duration.ofMinutes(optional("JWT_TTL_MINUTES")?.toLong() ?: 60),
                ),
                s3 = S3Config(
                    bucket = required("S3_BUCKET"),
                    region = optional("S3_REGION") ?: "us-east-1",
                    endpoint = optional("S3_ENDPOINT"),
                    publicEndpoint = optional("S3_PUBLIC_ENDPOINT"),
                    accessKey = optional("S3_ACCESS_KEY"),
                    secretKey = optional("S3_SECRET_KEY"),
                    pathStyle = optional("S3_PATH_STYLE")?.toBoolean() ?: false,
                    urlTtl = Duration.ofSeconds(optional("S3_URL_TTL_SECONDS")?.toLong() ?: 3600),
                    createBucket = optional("S3_CREATE_BUCKET")?.toBoolean() ?: false,
                ),
                maxPhotoBytes = optional("MAX_PHOTO_BYTES")?.toInt() ?: (10 * 1024 * 1024),
            )
        }

        private fun optional(name: String): String? = System.getenv(name)?.takeIf { it.isNotBlank() }

        private fun required(name: String): String =
            optional(name) ?: error("Environment variable $name is required")
    }
}
