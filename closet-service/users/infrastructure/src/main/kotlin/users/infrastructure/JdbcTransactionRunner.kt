package users.infrastructure

import users.infrastructure.abstractions.TransactionRunner
import java.sql.Connection
import javax.sql.DataSource

class JdbcTransactionRunner(
    private val dataSource: DataSource,
) : TransactionRunner {

    private val currentConnection = ThreadLocal<Connection?>()

    fun connection(): Connection =
        currentConnection.get()
            ?: error("Database operation must run inside a transaction")

    override fun <T> inTransaction(block: () -> T): T {
        if (currentConnection.get() != null) {
            return block()
        }

        dataSource.connection.use { connection ->
            connection.autoCommit = false
            currentConnection.set(connection)

            try {
                val result = block()
                connection.commit()
                return result
            } catch (e: Throwable) {
                try {
                    connection.rollback()
                } catch (rollbackError: Throwable) {
                    e.addSuppressed(rollbackError)
                }
                throw e
            } finally {
                currentConnection.remove()
            }
        }
    }
}
