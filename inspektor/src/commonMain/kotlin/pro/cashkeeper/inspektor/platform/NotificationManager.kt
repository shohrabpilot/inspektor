package pro.cashkeeper.inspektor.platform

internal expect fun NotificationManager(): NotificationManager

internal interface NotificationManager {
    fun notify(title: String, message: String)

    companion object {
        internal const val TAG = "Inspektor pro.cashkeeper.inspektor.platform.NotificationManager"
    }
}