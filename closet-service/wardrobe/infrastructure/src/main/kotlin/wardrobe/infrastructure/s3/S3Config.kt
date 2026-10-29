package wardrobe.infrastructure.s3

import java.time.Duration

data class S3Config(
    val bucket: String,
    val region: String,
    /** Адрес S3 для самого приложения (в Docker это http://s3:9090). Пусто = настоящий AWS S3. */
    val endpoint: String?,
    /** Адрес, который попадёт в presigned URL для клиента (http://localhost:9090). По умолчанию = endpoint. */
    val publicEndpoint: String?,
    val accessKey: String?,
    val secretKey: String?,
    /** true для S3-совместимых серверов (bucket в пути, а не в имени хоста). */
    val pathStyle: Boolean,
    val urlTtl: Duration,
    val createBucket: Boolean,
)
