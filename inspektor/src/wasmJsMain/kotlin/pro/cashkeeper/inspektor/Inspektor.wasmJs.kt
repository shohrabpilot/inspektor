package pro.cashkeeper.inspektor

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import pro.cashkeeper.inspektor.ui.App
import kotlinx.browser.document

@OptIn(ExperimentalComposeUiApi::class)
public actual fun openInspektor() {
    val body = document.body ?: return
    ComposeViewport(body) {
        App()
    }
}
