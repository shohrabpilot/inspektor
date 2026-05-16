package pro.cashkeeper.inspektor.platform

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

public actual fun clipEntryOf(text: String): ClipEntry {
    return ClipData.newPlainText(null, text).toClipEntry()
}
