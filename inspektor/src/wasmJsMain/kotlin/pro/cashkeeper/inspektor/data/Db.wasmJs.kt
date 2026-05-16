package pro.cashkeeper.inspektor.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.WebWorkerDriver
import org.w3c.dom.Worker
import pro.cashkeeper.inspektor.UnstableInspektorAPI

internal actual object DriverFactory {
    actual fun createDbDriver(): SqlDriver {
        // For simplicity in Wasm, we might want to use a Worker but it requires a lot of setup.
        // Let's check if there is an in-memory option or a simpler way.
        // Actually, many Wasm KMP projects use WebWorkerDriver.
        // For now, let's provide a placeholder or try to use an in-memory driver if possible.
        // SqlDelight's WebWorkerDriver is the standard for Wasm.
        return WebWorkerDriver(
            Worker("sqldelight.worker.js")
        )
    }

    actual fun createTempDbDriver(): SqlDriver {
        return WebWorkerDriver(
            Worker("sqldelight.worker.js")
        )
    }
}

@UnstableInspektorAPI
public actual fun setApplicationId(applicationId: String) {
    // No-op for Wasm
}
