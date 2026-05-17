package pro.cashkeeper.inspektor.ui.transactiondetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import pro.cashkeeper.inspektor.data.HttpTransaction
import pro.cashkeeper.inspektor.ui.components.CodeBlock
import pro.cashkeeper.inspektor.ui.components.Format

@Composable
internal fun ResponseBodyView(
    transaction: HttpTransaction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Collapsed "Original Response Body" accordion — only shown when overridden
        if (transaction.originalResponseBody != null) {
            SimpleAccordion(
                title = "Original Response Body",
                initialExpanded = false,
            ) {
                CodeBlock(
                    AnnotatedString(transaction.originalResponseBody),
                    Modifier.fillMaxWidth(),
                    format = Format.parse(
                        transaction.responseContentType,
                        transaction.originalResponseBody,
                    ),
                )
            }
        }

        // Main body
        if (transaction.responseBody.isNullOrEmpty()) {
            EmptyBody()
        } else {
            CodeBlock(
                AnnotatedString(transaction.responseBody),
                Modifier.fillMaxWidth().weight(1f),
                format = Format.parse(
                    transaction.responseContentType,
                    transaction.responseBody,
                ),
            )
        }
    }
}
