package app

import users.infrastructure.JdbcTransactionRunner
import wardrobe.infrastructure.JdbcWardrobeTransactionRunner
import users.infrastructure.PostgresUserLogRepository
import users.infrastructure.PostgresUserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import users.application.contracts.mapping.UserMapper
import users.application.services.DefaultUserValidator
import wardrobe.infrastructure.postgres.PostgresBoardRepository
import wardrobe.infrastructure.postgres.PostgresItemRepository
import wardrobe.infrastructure.s3.S3ImageStorage
import users.infrastructure.security.BcryptPasswordHasher
import users.infrastructure.security.JwtTokenIssuer
import wardrobe.application.contracts.mapping.WardrobeMapper
import wardrobe.application.services.DefaultBoardService
import wardrobe.application.services.DefaultItemService
import wardrobe.application.services.DefaultWardrobeValidator
import wardrobe.application.services.ImageSpecifier
import java.time.Clock

object AppModule {

    fun build(config: AppConfig): Dependencies {
        val dataSource = createDataSource(config.db)
        Migrations.run(dataSource)

        val clock = Clock.systemUTC()

        val storage = S3ImageStorage(config.s3)
        if (config.s3.createBucket) storage.ensureBucketExists()

        val usersTx = JdbcTransactionRunner(dataSource)
        val userService = DefaultUserService(
            users = PostgresUserRepository(usersTx),
            logs = PostgresUserLogRepository(usersTx),
            hasher = BcryptPasswordHasher(),
            tokens = JwtTokenIssuer(config.jwt, clock),
            tx = usersTx,
            clock = clock,
            validator = DefaultUserValidator(),
            mapper = UserMapper()
        )

        val wardrobeTx = JdbcWardrobeTransactionRunner(dataSource)
        val validator = DefaultWardrobeValidator()
        val boardRepository = PostgresBoardRepository(wardrobeTx)
        val itemRepository = PostgresItemRepository(wardrobeTx)

        val itemService = DefaultItemService(
            items = itemRepository,
            boards = boardRepository,
            storage = storage,
            tx = wardrobeTx,
            validator = validator,
            specifier = ImageSpecifier(config.maxPhotoBytes),
            mapper = WardrobeMapper(storage),

            )

        return Dependencies(
            users = userService,
            accountDeleter = AccountDeletion(userService, itemService, storage),
            boards = DefaultBoardService(
                boardRepository,
                itemRepository,
                storage,
                wardrobeTx,
                validator, WardrobeMapper(storage)
            ),
            items = itemService,
            mapper = WardrobeMapper(storage),
            closeables = listOf(dataSource, storage),
        )
    }

    private fun createDataSource(db: DbConfig): HikariDataSource =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = db.url
                username = db.username
                password = db.password
                driverClassName = "org.postgresql.Driver"
                maximumPoolSize = db.poolSize
                minimumIdle = 2
                connectionTimeout = 5_000
                poolName = "closet-pool"
            }
        )
}
