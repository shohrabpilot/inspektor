package pro.cashkeeper.inspektor.ui.transactiondetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Colored pill badge showing the HTTP method.
 * Colors follow semantic conventions:
 *   GET    → blue
 *   POST   → teal/green
 *   PUT    → amber
 *   PATCH  → amber (lighter)
 *   DELETE → red
 *   HEAD / OPTIONS / … → gray
 */
@Composable
internal fun MethodBadge(method: String) {
    val (bg, fg) = methodColors(method)
    Text(
        text = method,
        style = MaterialTheme.typography.labelSmall.copy(
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            letterSpacing = 0.04.sp,
        ),
        color = fg,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun methodColors(method: String): Pair<Color, Color> = when (method.uppercase()) {
    "GET"     -> Color(0xFFE6F1FB) to Color(0xFF0C447C)
    "POST"    -> Color(0xFFEAF3DE) to Color(0xFF27500A)
    "PUT"     -> Color(0xFFFAEEDA) to Color(0xFF633806)
    "PATCH"   -> Color(0xFFFAEEDA) to Color(0xFF854F0B)
    "DELETE"  -> Color(0xFFFCEBEB) to Color(0xFF791F1F)
    else      -> Color(0xFFF1EFE8) to Color(0xFF444441)
}
