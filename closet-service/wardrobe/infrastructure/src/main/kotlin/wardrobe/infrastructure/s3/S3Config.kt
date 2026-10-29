package wardrobe.infrastructure.s3

import java.time.Duration

data class S3Config(
    val bucket: String,
    val region: String,
    val endpoint: String?,
    val publicEndpoint: String?,
    val accessKey: String?,
    val secretKey: String?,
    val pathStyle: Boolean,
    val urlTtl: Duration,
    val createBucket: Boolean,
)
