package pro.cashkeeper.inspektor.ui.transactionlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.cashkeeper.inspektor.ui.overriding.editoverride.EditOverrideScreen
import pro.cashkeeper.inspektor.ui.overriding.overrideslist.OverridesListScreen
import pro.cashkeeper.inspektor.ui.transactiondetails.TransactionDetailsScreen

@Composable
internal fun TransactionListDetailScreen() {
    var selectedTransactionId by rememberSaveable { mutableStateOf<Long?>(null) }
    var showOverrides by rememberSaveable { mutableStateOf(false) }
    var showEditOverride by rememberSaveable { mutableStateOf(false) }
    var editOverrideId by rememberSaveable { mutableStateOf(0L) }
    var editSourceTransactionId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (showEditOverride) {
        EditOverrideScreen(
            overrideId = editOverrideId,
            transactionId = editSourceTransactionId,
            onBack = {
                showEditOverride = false
                editOverrideId = 0L
                editSourceTransactionId = null
            },
        )
        return
    }

    if (showOverrides) {
        OverridesListScreen(
            openEditOverrideScreen = { overrideId ->
                showEditOverride = true
                editOverrideId = overrideId ?: 0L
                editSourceTransactionId = null
            },
            onBack = { showOverrides = false },
        )
        return
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isWide = maxWidth >= 1000.dp
        val transactionId = selectedTransactionId

        if (isWide) {
            Row(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.42f),
                ) {
                    TransactionListScreen(
                        openTransaction = { id -> selectedTransactionId = id },
                        openOverridesScreen = { showOverrides = true },
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.58f),
                ) {
                    if (transactionId != null) {
                        TransactionDetailsScreen(
                            transactionId = transactionId,
                            onBack = { selectedTransactionId = null },
                            openAddOverrideScreen = {
                                showEditOverride = true
                                editOverrideId = 0L
                                editSourceTransactionId = transactionId
                            },
                        )
                    } else {
                        DetailPlaceholder()
                    }
                }
            }
        } else {
            if (transactionId != null) {
                TransactionDetailsScreen(
                    transactionId = transactionId,
                    onBack = { selectedTransactionId = null },
                    openAddOverrideScreen = {
                        showEditOverride = true
                        editOverrideId = 0L
                        editSourceTransactionId = transactionId
                    },
                )
            } else {
                TransactionListScreen(
                    openTransaction = { id -> selectedTransactionId = id },
                    openOverridesScreen = { showOverrides = true },
                )
            }
        }
    }
}

@Composable
private fun DetailPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Select a transaction to view details",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
