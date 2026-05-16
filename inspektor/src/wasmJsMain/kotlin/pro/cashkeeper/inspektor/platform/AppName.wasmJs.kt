package pro.cashkeeper.inspektor.platform

import kotlinx.browser.window

internal actual fun getAppName(): String? {
    return window.document.title.takeIf { it.isNotBlank() }
}
