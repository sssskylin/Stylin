package app

import liquibase.Contexts
import liquibase.LabelExpression
import liquibase.Liquibase
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor
import javax.sql.DataSource
import liquibase.database.DatabaseFactory as LiquibaseDatabaseFactory

object Migrations {

    private const val CHANGELOG = "db-changelog-master.yaml"

    fun run(dataSource: DataSource) {
        val database = LiquibaseDatabaseFactory.getInstance()
            .findCorrectDatabaseImplementation(JdbcConnection(dataSource.connection))
        Liquibase(CHANGELOG, ClassLoaderResourceAccessor(), database).use {
            it.update(Contexts(), LabelExpression())
        }
    }
}
