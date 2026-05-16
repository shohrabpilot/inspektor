package pro.cashkeeper.inspektor.platform

internal actual fun NotificationManager(): NotificationManager = object : NotificationManager {
    override fun notify(title: String, message: String) {
        // Not implemented for Wasm
    }
}
