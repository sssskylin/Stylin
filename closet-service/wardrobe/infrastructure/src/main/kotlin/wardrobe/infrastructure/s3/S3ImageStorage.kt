package wardrobe.infrastructure.s3

import wardrobe.infrastructure.abstractions.ImageStorage
import wardrobe.domain.exceptions.ImageStorageException
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import software.amazon.awssdk.services.s3.model.Delete
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.DeleteObjectsRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.ObjectIdentifier
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.net.URI

class S3ImageStorage(private val config: S3Config) : ImageStorage, AutoCloseable {

    private val credentials: AwsCredentialsProvider =
        if (!config.accessKey.isNullOrBlank() && !config.secretKey.isNullOrBlank()) {
            StaticCredentialsProvider.create(AwsBasicCredentials.create(config.accessKey, config.secretKey))
        } else {
            DefaultCredentialsProvider.builder().build()
        }

    private val s3Configuration = S3Configuration.builder()
        .pathStyleAccessEnabled(config.pathStyle)
        .build()

    private val client: S3Client = S3Client.builder()
        .region(Region.of(config.region))
        .credentialsProvider(credentials)
        .serviceConfiguration(s3Configuration)
        .apply { config.endpoint?.takeIf { it.isNotBlank() }?.let { endpointOverride(URI.create(it)) } }
        .build()

    private val presigner: S3Presigner = S3Presigner.builder()
        .region(Region.of(config.region))
        .credentialsProvider(credentials)
        .serviceConfiguration(s3Configuration)
        .apply {
            (config.publicEndpoint ?: config.endpoint)
                ?.takeIf { it.isNotBlank() }
                ?.let { endpointOverride(URI.create(it)) }
        }
        .build()

    override fun put(key: String, contentType: String, content: ByteArray) = storage("put") {
        client.putObject(
            PutObjectRequest.builder()
                .bucket(config.bucket)
                .key(key)
                .contentType(contentType)
                .cacheControl("private, max-age=31536000, immutable")
                .build(),
            RequestBody.fromBytes(content),
        )
        Unit
    }

    override fun delete(key: String) = storage("delete") {
        client.deleteObject(DeleteObjectRequest.builder().bucket(config.bucket).key(key).build())
        Unit
    }

    override fun deleteAll(keys: Collection<String>) {
        keys.distinct().chunked(1000).forEach { chunk ->
            storage("deleteAll") {
                client.deleteObjects(
                    DeleteObjectsRequest.builder()
                        .bucket(config.bucket)
                        .delete(
                            Delete.builder()
                                .objects(chunk.map { ObjectIdentifier.builder().key(it).build() })
                                .quiet(true)
                                .build()
                        )
                        .build()
                )
                Unit
            }
        }
    }

    override fun urlFor(key: String): String =
        presigner.presignGetObject(
            GetObjectPresignRequest.builder()
                .signatureDuration(config.urlTtl)
                .getObjectRequest(GetObjectRequest.builder().bucket(config.bucket).key(key).build())
                .build()
        ).url().toString()

    fun ensureBucketExists() {
        try {
            client.headBucket { it.bucket(config.bucket) }
        } catch (e: S3Exception) {
            if (e.statusCode() != 404) throw e
            client.createBucket { it.bucket(config.bucket) }
        }
    }

    override fun close() {
        presigner.close()
        client.close()
    }

    private inline fun <T> storage(operation: String, block: () -> T): T =
        try {
            block()
        } catch (e: SdkException) {
            throw ImageStorageException("Image storage '$operation' failed", e)
        }
}