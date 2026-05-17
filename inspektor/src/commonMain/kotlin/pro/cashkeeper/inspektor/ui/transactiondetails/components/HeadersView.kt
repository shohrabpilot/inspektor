package pro.cashkeeper.inspektor.ui.transactiondetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pro.cashkeeper.inspektor.data.HttpTransaction
import pro.cashkeeper.inspektor.ui.components.KeyValueView

@Composable
internal fun HeadersView(
    transaction: HttpTransaction,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // ── Overview ─────────────────────────────────────────────────────────
        SimpleAccordion(title = "Overview", initialExpanded = true) {
            val monoStyle = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
            )

            // URL row — highlighted blue
            KeyValueView(
                "URL",
                transaction.url,
                textStyle = monoStyle.copy(color = Color(0xFF185FA5)),
            )
            KeyValueView("Method", transaction.method, textStyle = monoStyle)

            // Response code with semantic chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    "Response Code",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                transaction.responseCode?.let { code ->
                    ResponseCodeChip(code)
                }
            }

            KeyValueView("Host", transaction.host, textStyle = monoStyle)

            // Error banner
            if (transaction.error != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = transaction.error,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                    )
                }
            }
        }

        // ── Request Headers ───────────────────────────────────────────────────
        val requestHeaders = transaction.requestHeaders ?: emptySet()
        SimpleAccordion(
            title = "Request Headers (${requestHeaders.size})",
            initialExpanded = true,
            content = HeadersListView(
                requestHeaders,
                transaction.originalRequestHeaders,
            )
        )

        // ── Response Headers ──────────────────────────────────────────────────
        val responseHeaders = transaction.responseHeaders ?: emptySet()
        SimpleAccordion(
            title = "Response Headers (${responseHeaders.size})",
            initialExpanded = true,
            content = HeadersListView(
                responseHeaders,
                transaction.originalResponseHeaders,
            )
        )
    }
}

/** Green chip for 2xx, red for 4xx/5xx, amber for 3xx, gray otherwise. */
@Composable
private fun ResponseCodeChip(code: Long) {
    val (bg, fg, icon) = when {
        code in 200..299 -> Triple(Color(0xFFEAF3DE), Color(0xFF27500A), Icons.Default.CheckCircle)
        code in 400..599 -> Triple(Color(0xFFFCEBEB), Color(0xFF791F1F), Icons.Default.Warning)
        code in 300..399 -> Triple(Color(0xFFFAEEDA), Color(0xFF633806), Icons.Default.Warning)
        else             -> Triple(Color(0xFFF1EFE8), Color(0xFF444441), Icons.Default.CheckCircle)
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(12.dp))
        Text(
            text = code.toString(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            ),
            color = fg,
        )
    }
}
