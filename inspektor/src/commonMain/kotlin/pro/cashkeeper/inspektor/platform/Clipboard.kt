package pro.cashkeeper.inspektor.platform

import androidx.compose.ui.platform.ClipEntry

/**
 * Creates a [ClipEntry] for the given text.
 */
public expect fun clipEntryOf(text: String): ClipEntry
