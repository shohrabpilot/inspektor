package pro.cashkeeper.inspektor.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import pro.cashkeeper.inspektor.UnstableInspektorAPI
import pro.cashkeeper.inspektor.platform.getAppDataDir
import pro.cashkeeper.inspektor.utils.log
import kotlinx.coroutines.runBlocking
import java.io.File
import java.nio.file.Paths

internal actual object DriverFactory {
    actual fun createDbDriver(): SqlDriver = runBlocking {
        val dbPath = getDatabasePath()
        val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:$dbPath")
        InspektorDatabase.Schema.create(driver).await()
        driver
    }

    actual fun createTempDbDriver(): SqlDriver = runBlocking {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        InspektorDatabase.Schema.create(driver).await()
        driver
    }
}

internal const val DEFAULT_APPLICATION_ID = "pro.cashkeeper.inspektor.desktop"


private fun getDatabasePath(): String {
    val dbPath = Paths.get(getAppDataDir(), DB_NAME).toString()
    File(dbPath).parentFile.mkdirs()
    return dbPath
}

@UnstableInspektorAPI
public actual fun setApplicationId(applicationId: String) {
    APPLICATION_ID = applicationId
}

/**
 * Application ID is used to resolve the folder in which database will be stored.
 */
internal var APPLICATION_ID: String? = null
    set(value) {
        if (field != null && field != value) {
            log("Inspektor") {
                "Application ID has already been set to $field" +
                        "It should not be changed."
            }
        }
        field = value
    }
