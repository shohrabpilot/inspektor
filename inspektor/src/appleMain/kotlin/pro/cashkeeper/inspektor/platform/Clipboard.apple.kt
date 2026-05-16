package pro.cashkeeper.inspektor.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
public actual fun clipEntryOf(text: String): ClipEntry {
    return ClipEntry.withPlainText(text)
}
