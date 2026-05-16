package pro.cashkeeper.inspektor.data

import app.cash.sqldelight.db.SqlDriver
import pro.cashkeeper.inspektor.UnstableInspektorAPI
import pro.cashkeeper.inspektor.data.adapters.instantAdapter
import pro.cashkeeper.inspektor.data.adapters.setMapEntryAdapter

internal const val DB_NAME = "pro.cashkeeper.inspektor.db"

internal expect object DriverFactory {
    fun createDbDriver(): SqlDriver

    fun createTempDbDriver(): SqlDriver
}

internal fun createDatabase(): InspektorDatabase {
    val driver = DriverFactory.createDbDriver()
    return InspektorDatabase(
        driver, HttpTransaction.Adapter(
            requestDateAdapter = instantAdapter,
            responseDateAdapter = instantAdapter,
            requestHeadersAdapter = setMapEntryAdapter,
            responseHeadersAdapter = setMapEntryAdapter,
            originalResponseHeadersAdapter = setMapEntryAdapter,
            originalRequestHeadersAdapter = setMapEntryAdapter,
        )
    )
}

@UnstableInspektorAPI
public expect fun setApplicationId(applicationId: String)