package pro.cashkeeper.inspektor.ui.transactiondetails

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pro.cashkeeper.inspektor.data.HttpTransaction
import pro.cashkeeper.inspektor.data.InspektorDataSourceImpl
import pro.cashkeeper.inspektor.platform.clipEntryOf
import pro.cashkeeper.inspektor.ui.components.AddOverrideIcon
import pro.cashkeeper.inspektor.ui.components.DefaultIconButton
import pro.cashkeeper.inspektor.ui.transactiondetails.components.HeadersView
import pro.cashkeeper.inspektor.ui.transactiondetails.components.RequestBodyView
import pro.cashkeeper.inspektor.ui.transactiondetails.components.ResponseBodyView
import pro.cashkeeper.inspektor.utils.toCurlString
import kotlinx.coroutines.launch
import pro.cashkeeper.inspektor.ui.transactiondetails.components.MethodBadge

@Composable
internal fun TransactionDetailsScreen(
    transactionId: Long,
    onBack: () -> Unit,
    openAddOverrideScreen: () -> Unit,
) {
    val viewModel = viewModel(key = "transaction-details-$transactionId") {
        TransactionDetailsViewModel(transactionId, InspektorDataSourceImpl.Instance)
    }
    TransactionDetailsScreen(
        viewModel.transaction.collectAsState().value,
        onBack,
        openAddOverrideScreen,
        onDelete = {
            viewModel.deleteTransaction()
            onBack()
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TransactionDetailsScreen(
    transaction: HttpTransaction?,
    onBack: () -> Unit,
    openAddOverrideScreen: () -> Unit,
    onDelete: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    if (transaction == null) {
                        Text(text = "Loading...")
                        return@CenterAlignedTopAppBar
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Method badge
                        MethodBadge(method = transaction.method ?: "")
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = transaction.path ?: "",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                            ),
                            maxLines = 1,
                        )
                    }
                },
                actions = {
                    DefaultIconButton(
                        onClick = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    clipEntryOf(transaction?.toCurlString() ?: "")
                                )
                                snackbarHostState.showSnackbar("Copied as cURL")
                            }
                        },
                        tooltipText = "Copy as cURL",
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = "Copy as cURL")
                    }
                    DefaultIconButton(
                        onClick = openAddOverrideScreen,
                        tooltipText = "Add Override",
                    ) { AddOverrideIcon() }
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(
                            Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        if (transaction == null) {
            CircularProgressIndicator()
            return@Scaffold
        }

        var selectedTabIndex by remember { mutableStateOf(0) }

        Column(Modifier.padding(paddingValues).fillMaxSize()) {
            PrimaryTabRow(
                selectedTabIndex = selectedTabIndex,
                indicator = {
                    TabRowDefaults.PrimaryIndicator(
                        Modifier.tabIndicatorOffset(selectedTabIndex),
                        color = MaterialTheme.colorScheme.primary
                    )
                },
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Headers") },
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = {
                        Text(
                            "Request" + transaction.requestPayloadSize
                                ?.let { " ($it)" }.orEmpty()
                        )
                    },
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = {
                        Text(
                            "Response" + transaction.responsePayloadSize
                                ?.let { " ($it)" }.orEmpty()
                        )
                    },
                )
            }

            val detailModifier = Modifier.fillMaxSize().weight(1f)
            when (selectedTabIndex) {
                0 -> HeadersView(transaction, detailModifier.verticalScroll(rememberScrollState()))
                1 -> RequestBodyView(transaction, detailModifier)
                2 -> ResponseBodyView(transaction, detailModifier)
            }
        }
    }
}
