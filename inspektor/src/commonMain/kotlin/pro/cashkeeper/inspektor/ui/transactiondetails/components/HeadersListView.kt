package pro.cashkeeper.inspektor.ui.transactiondetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pro.cashkeeper.inspektor.inspektor.generated.resources.Res
import pro.cashkeeper.inspektor.platform.runBlocking as platformRunBlocking
import pro.cashkeeper.inspektor.ui.components.ExpandableKeyValue
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Composable
internal fun HeadersListView(
    headers: Set<Map.Entry<String, List<String>>>,
    originalHeaders: Set<Map.Entry<String, List<String>>>? = null,
): (@Composable ColumnScope.() -> Unit)? = headers.takeIf { it.isNotEmpty() }?.let {
    {
        it.forEachIndexed { index, entry ->
            HeaderRow(
                key = entry.key,
                value = entry.value.joinToString("; "),
                showDivider = index < it.size - 1 || originalHeaders?.isNotEmpty() == true,
                extraContent = headersInfo[entry.key.lowercase()]?.let { doc ->
                    {
                        KeyInfoAndLink(
                            doc.summary,
                            "https://developer.mozilla.org/en-US/docs/${doc.mdnSlug}"
                        )
                    }
                }
            )
        }

        originalHeaders?.takeIf { orig -> orig.isNotEmpty() }?.let { orig ->
            // "Original Headers" divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp)
                Text(
                    text = "Original Headers",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
                HorizontalDivider(Modifier.weight(1f), thickness = 0.5.dp)
            }

            orig.forEachIndexed { index, entry ->
                HeaderRow(
                    key = entry.key,
                    value = entry.value.joinToString("; "),
                    showDivider = index < orig.size - 1,
                    extraContent = headersInfo[entry.key.lowercase()]?.let { doc ->
                        {
                            KeyInfoAndLink(
                                doc.summary,
                                "https://developer.mozilla.org/en-US/docs/${doc.mdnSlug}"
                            )
                        }
                    }
                )
            }
        }
    }
}

/**
 * Single header key-value row.
 * Key is fixed at 140 dp so values from different rows align cleanly.
 */
@Composable
private fun HeaderRow(
    key: String,
    value: String,
    showDivider: Boolean,
    extraContent: (@Composable () -> Unit)? = null,
) {
    ExpandableKeyValue(
        key, value,
        textStyle = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 11.5.sp,
        ),
        content = extraContent?.let { { it() } },
    )
    if (showDivider) {
        HorizontalDivider(thickness = 0.5.dp)
    }
}

internal val headersInfo: Map<String, HeaderDoc> by lazy {
    platformRunBlocking {
        val string = Res.readBytes("files/docs-headers.json").decodeToString()
        Json.decodeFromString<Map<String, HeaderDoc>>(string)
    }
}

@Serializable
internal data class HeaderDoc(
    val mdnSlug: String,
    val name: String,
    val summary: String,
)
