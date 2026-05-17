package pro.cashkeeper.inspektor.ui.transactiondetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
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
internal fun RequestBodyView(
    transaction: HttpTransaction,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        // Collapsed "Original Request Body" accordion — only shown when overridden
        if (transaction.originalRequestBody != null) {
            SimpleAccordion(
                title = "Original Request Body",
                initialExpanded = false,
            ) {
                CodeBlock(
                    AnnotatedString(transaction.originalRequestBody),
                    Modifier.fillMaxWidth(),
                    format = Format.parse(
                        transaction.requestContentType,
                        transaction.originalRequestBody,
                    ),
                )
            }
        }

        // Main body
        if (transaction.requestBody.isNullOrEmpty()) {
            EmptyBody()
        } else {
            CodeBlock(
                AnnotatedString(transaction.requestBody),
                Modifier.fillMaxWidth().weight(1f),
                format = Format.parse(
                    transaction.requestContentType,
                    transaction.requestBody,
                ),
            )
        }
    }
}
