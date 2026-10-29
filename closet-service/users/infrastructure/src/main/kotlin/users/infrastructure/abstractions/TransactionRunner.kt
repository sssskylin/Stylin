package users.infrastructure.abstractions

interface TransactionRunner {

    fun <T> inTransaction(block: () -> T): T
}