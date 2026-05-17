package pro.cashkeeper.inspektor.ui.transactionlist

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pro.cashkeeper.inspektor.data.GetAllLatestWithLimit
import pro.cashkeeper.inspektor.data.InspektorDataSourceImpl
import pro.cashkeeper.inspektor.platform.FileSharer
import pro.cashkeeper.inspektor.platform.getAppName
import pro.cashkeeper.inspektor.ui.UiEvent
import pro.cashkeeper.inspektor.ui.components.AddOverrideIcon
import pro.cashkeeper.inspektor.ui.components.DateRangeButton
import pro.cashkeeper.inspektor.ui.components.DateRangePickerDialog
import pro.cashkeeper.inspektor.ui.components.DefaultIconButton
import pro.cashkeeper.inspektor.ui.components.Logo
import pro.cashkeeper.inspektor.ui.components.SimpleSearchBar
import pro.cashkeeper.inspektor.ui.theme.errorColor
import pro.cashkeeper.inspektor.ui.theme.successColor
import pro.cashkeeper.inspektor.ui.theme.warningColor
import pro.cashkeeper.inspektor.ui.transactionlist.components.DeleteDialog
import pro.cashkeeper.inspektor.utils.DateFormatters
import pro.cashkeeper.inspektor.utils.TimeFormatters
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime

// ─── Status color helpers ─────────────────────────────────────────────────────

private fun statusCodeColor(code: Long?): Color = when {
    code == null        -> Color(0xFF888780)
    code < 300          -> Color(0xFF3B6D11) // green-700
    code < 400          -> Color(0xFF854F0B) // amber-700
    code < 500          -> Color(0xFFA32D2D) // red-700
    else                -> Color(0xFF791F1F) // red-800
}

private fun statusCodeBg(code: Long?): Color = when {
    code == null        -> Color(0xFFF1EFE8) // gray-50
    code < 300          -> Color(0xFFEAF3DE) // green-50
    code < 400          -> Color(0xFFFAEEDA) // amber-50
    code < 500          -> Color(0xFFFCEBEB) // red-50
    else                -> Color(0xFFFCEBEB)
}

private fun statusCodeBorder(code: Long?): Color = when {
    code == null        -> Color(0xFFB4B2A9) // gray-200
    code < 300          -> Color(0xFFC0DD97) // green-100
    code < 400          -> Color(0xFFFAC775) // amber-100
    code < 500          -> Color(0xFFF7C1C1) // red-100
    else                -> Color(0xFFF7C1C1)
}

private fun statusDotColor(code: Long?): Color = when {
    code == null        -> Color(0xFFB4B2A9)
    code < 300          -> Color(0xFF639922) // green-400
    code < 400          -> Color(0xFFBA7517) // amber-600
    code < 500          -> Color(0xFFE24B4A) // red-400
    else                -> Color(0xFFA32D2D)
}

// ─── Entry composable (with ViewModel) ───────────────────────────────────────

@Composable
internal fun TransactionListScreen(
    openTransaction: (Long) -> Unit,
    openOverridesScreen: () -> Unit,
    openAddOverrideScreen: (Long) -> Unit,
) {
    val viewModel = viewModel<TransactionListViewModel> {
        TransactionListViewModel(InspektorDataSourceImpl.Instance, FileSharer())
    }
    val snackbarHostState = remember { SnackbarHostState() }
    var alertDialogData by remember { mutableStateOf<UiEvent.ShowErrorDialog?>(null) }

    if (alertDialogData != null) {
        AlertDialog(
            onDismissRequest = { alertDialogData = null },
            title = { Text("Error") },
            text = { Text(alertDialogData?.message ?: "") },
            confirmButton = {
                TextButton(onClick = { alertDialogData = null }) { Text("OK") }
            },
        )
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowSnackBar    -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.ShowErrorDialog -> {
                    alertDialogData = event
                }

                else                       -> {}
            }
        }
    }

    TransactionListScreen(
        transactions         = viewModel.transactions.collectAsState().value,
        searchTermState      = viewModel.searchFieldState,
        onClickTransaction   = openTransaction,
        openOverridesScreen  = openOverridesScreen,
        onAddOverride        = openAddOverrideScreen,
        allCount             = viewModel.allCount.collectAsState().value,
        startDate            = viewModel.startDate.collectAsState().value,
        endDate              = viewModel.endDate.collectAsState().value,
        onDateRangeSelected  = viewModel::onDateRangeSelected,
        onDeleteTransactions = viewModel::deleteTransactions,
        onDeleteAllTransactions = viewModel::deleteAllTransactions,
        onShareAsHar         = viewModel::shareAsHar,
        snackbarHostState    = snackbarHostState,
    )
}

// ─── Main screen ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun TransactionListScreen(
    transactions: List<GetAllLatestWithLimit> = emptyList(),
    searchTermState: TextFieldState,
    onClickTransaction: (Long) -> Unit,
    openOverridesScreen: () -> Unit,
    onAddOverride: (transaction: Long) -> Unit,
    allCount: Long,
    startDate: Instant,
    endDate: Instant,
    onDateRangeSelected: (Instant, Instant) -> Unit,
    onDeleteTransactions: (Instant) -> Unit,
    onDeleteAllTransactions: () -> Unit,
    onShareAsHar: () -> Unit,
    snackbarHostState: SnackbarHostState,
) {
    var showDateRangePicker     by remember { mutableStateOf(false) }
    var showDeleteDialog        by remember { mutableStateOf(false) }
    var showDeleteAllConfirm    by remember { mutableStateOf(false) }
    var showMenu                by remember { mutableStateOf(false) }
    var showSearch              by remember { mutableStateOf(false) }

    if (showDateRangePicker) {
        DateRangePickerDialog(
            startDate        = startDate,
            endDate          = endDate,
            onDismissRequest = { showDateRangePicker = false },
            onConfirm        = { start, end ->
                onDateRangeSelected(start, end)
                showDateRangePicker = false
            },
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            onDismissRequest = { showDeleteDialog = false },
            onConfirm        = {
                onDeleteTransactions(it)
                showDeleteDialog = false
            },
        )
    }

    if (showDeleteAllConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteAllConfirm = false },
            title   = { Text("Delete all transactions") },
            text    = { Text("Are you sure you want to delete all transactions? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteAllTransactions()
                    showDeleteAllConfirm = false
                }) {
                    Text("Delete all", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllConfirm = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            TopBar(
                showSearch          = showSearch,
                onToggleSearch      = { showSearch = !showSearch },
                showMenu            = showMenu,
                onToggleMenu        = { showMenu = !showMenu },
                onDismissMenu       = { showMenu = false },
                onDeleteClick       = { showDeleteDialog = true; showMenu = false },
                onDeleteAllClick    = { showDeleteAllConfirm = true; showMenu = false },
                onViewOverrides     = { openOverridesScreen(); showMenu = false },
                onExportHar         = { onShareAsHar(); showMenu = false },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier  = Modifier.padding(16.dp),
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues).fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            // ── Search bar ─────────────────────────────────────────────────
            item(key = "search_bar") {
                AnimatedVisibility(
                    visible = showSearch,
                    enter   = fadeIn() + expandVertically(),
                    exit    = fadeOut() + shrinkVertically(),
                ) {
                    Surface(
                        color    = MaterialTheme.colorScheme.surfaceContainerLow,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        SimpleSearchBar(
                            searchFieldState = searchTermState,
                            placeholder      = { Text("Search by status code or path") },
                        )
                    }
                }
            }

            // ── Summary card ───────────────────────────────────────────────
            item(key = "summary_header") {
                SummaryHeader(
                    shownCount      = transactions.size,
                    totalCount      = allCount,
                    transactions    = transactions,
                    startDate       = startDate,
                    endDate         = endDate,
                    onDateRangeClick = { showDateRangePicker = true },
                )
            }

            item(key = "divider") {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                )
            }

            // ── List / empty state ─────────────────────────────────────────
            if (transactions.isEmpty()) {
                item(key = "empty_state") {
                    EmptyState(modifier = Modifier.fillParentMaxHeight(0.5f))
                }
            } else {
                transactions
                    .groupBy { it.requestDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
                    .forEach { (date, group) ->
                        stickyHeader(key = "header_${date}") {
                            DateHeader(
                                label = date?.format(DateFormatters.simpleLocalFormatter) ?: "Unknown",
                                count = group.size,
                            )
                        }
                        items(group, key = { it.id }) { transaction ->
                            TransactionItem(
                                transaction = transaction,
                                onClick     = { onClickTransaction(transaction.id) },
                                onAddOverride = { onAddOverride(transaction.id) },
                                modifier    = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                            )
                        }
                    }
            }
        }
    }
}

// ─── Top app bar ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    showSearch: Boolean,
    onToggleSearch: () -> Unit,
    showMenu: Boolean,
    onToggleMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onDeleteClick: () -> Unit,
    onDeleteAllClick: () -> Unit,
    onViewOverrides: () -> Unit,
    onExportHar: () -> Unit,
) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        navigationIcon = {
            Box(modifier = Modifier.padding(start = 8.dp)) { Logo() }
        },
        title = {
            val appName = remember(Unit) { getAppName() }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text  = "Inspektor",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
                appName?.let {
                    Text(
                        text     = it,
                        style    = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.alpha(0.6f),
                    )
                }
            }
        },
        actions = {
            // Search toggle
            IconButton(onClick = onToggleSearch) {
                AnimatedContent(targetState = showSearch) { isOpen ->
                    if (isOpen) Icon(Icons.Rounded.Close, contentDescription = "Close search")
                    else        Icon(Icons.Rounded.Search, contentDescription = "Search")
                }
            }

            // Overflow menu
            Box {
                IconButton(onClick = onToggleMenu) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = onDismissMenu) {
                    DropdownMenuItem(
                        text        = { Text("Delete") },
                        onClick     = onDeleteClick,
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
                    )
                    DropdownMenuItem(
                        text        = { Text("Delete all") },
                        onClick     = onDeleteAllClick,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                        },
                    )
                    DropdownMenuItem(
                        text        = { Text("View overrides") },
                        onClick     = onViewOverrides,
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    )
                    DropdownMenuItem(
                        text        = { Text("Export as HAR") },
                        onClick     = onExportHar,
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                    )
                }
            }
        },
    )
}

// ─── Summary header ───────────────────────────────────────────────────────────

@Composable
private fun SummaryHeader(
    shownCount: Int,
    totalCount: Long,
    transactions: List<GetAllLatestWithLimit>,
    startDate: Instant,
    endDate: Instant,
    onDateRangeClick: () -> Unit,
) {
    val breakdown = remember(transactions) { computeBreakdown(transactions) }

    Surface(
        color    = MaterialTheme.colorScheme.surfaceContainerLow,
        shape    = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
    ) {
        Column(
            modifier            = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Count row + date button
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = "Transactions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text  = shownCount.toString(),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text     = "of $totalCount",
                            style    = MaterialTheme.typography.bodyMedium,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                }
                DateRangeButton(
                    startDate = startDate,
                    endDate   = endDate,
                    onClick   = onDateRangeClick,
                )
            }

            // Status chips
            if (shownCount > 0) {
                StatusBreakdownRow(breakdown = breakdown)
            }
        }
    }
}

// ─── Status breakdown chips ───────────────────────────────────────────────────

@Composable
private fun StatusBreakdownRow(breakdown: StatusBreakdown) {
    Row(
        modifier            = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        StatusChip(label = "2xx", count = breakdown.success,     color = successColor, modifier = Modifier.weight(1f))
        StatusChip(label = "3xx", count = breakdown.redirect,    color = warningColor, modifier = Modifier.weight(1f))
        StatusChip(label = "4xx", count = breakdown.clientError, color = errorColor,   modifier = Modifier.weight(1f))
        StatusChip(label = "5xx", count = breakdown.serverError, color = errorColor,   modifier = Modifier.weight(1f))
        if (breakdown.unknown > 0) {
            StatusChip(
                label    = "—",
                count    = breakdown.unknown,
                color    = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatusChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val isActive = count > 0
    Surface(
        color    = if (isActive) color.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceContainer,
        shape    = RoundedCornerShape(10.dp),
        modifier = modifier
            .height(48.dp)
            .border(
                width = 1.dp,
                color = if (isActive) color.copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(10.dp),
            ),
    ) {
        Column(
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally,
        ) {
            Text(
                text  = count.toString(),
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (isActive) color else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text  = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isActive) color.copy(alpha = 0.85f)
                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

// ─── Date sticky header ───────────────────────────────────────────────────────

@Composable
private fun DateHeader(label: String, count: Int) {
    Box(
        modifier        = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = RoundedCornerShape(50),
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = label,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "$count",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

// ─── Improved TransactionItem ─────────────────────────────────────────────────
//
// Drop this into transactionlist/components/TransactionItem.kt
// (replaces or supplements whatever already lives there).

@Composable
internal fun TransactionItem(
    transaction: GetAllLatestWithLimit,
    onClick: () -> Unit,
    onAddOverride: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val code       = transaction.responseCode
    val dotColor   = statusDotColor(code)
    val badgeBg    = statusCodeBg(code)
    val badgeText  = statusCodeColor(code)
    val borderColor = statusCodeBorder(code)
    val isOverridden = transaction.isOverriddenNum > 0

    Surface(
        onClick   = onClick,
        shape     = RoundedCornerShape(12.dp),
        color     = MaterialTheme.colorScheme.surface,
        modifier  = modifier
            .fillMaxWidth()
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
            ),
    ) {
        Row(
            modifier          = Modifier.padding(start = 13.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // Status dot
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )

            // Path + meta
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    transaction.method?.let { method ->
                        Text(
                            text     = method,
                            style    = MaterialTheme.typography.labelSmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                    }
                    Text(
                        text     = transaction.path ?: "—",
                        style    = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color    = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(2.dp))
                val host    = transaction.host ?: ""
                val duration = transaction.tookMs?.let { " · $it ms" } ?: ""
                Text(
                    text  = "$host$duration",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // Badge + time
            Column(horizontalAlignment = Alignment.End) {
                // Status code badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .border(0.5.dp, borderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text  = code?.toString() ?: "—",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = badgeText,
                    )
                }
                Spacer(Modifier.height(2.dp))
                // Timestamp
                transaction.requestDate?.let { instant ->
                    Text(
                        text  = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                            .format(TimeFormatters.simpleLocalAmPm),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (isOverridden) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Overridden",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }

                DefaultIconButton(
                    onClick = onAddOverride,
                    tooltipText = "Add Override",
                ) { AddOverrideIcon() }
            }
        }
    }
}

// ─── Empty state ──────────────────────────────────────────────────────────────

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier              = modifier.fillMaxWidth().padding(32.dp),
        verticalArrangement   = Arrangement.Center,
        horizontalAlignment   = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier         = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center,
        ) {
            Logo(modifier = Modifier.size(56.dp).alpha(0.7f))
        }
        Spacer(Modifier.height(16.dp))
        Text(
            text      = "No transactions",
            style     = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text      = "Make a network request, or adjust the date range and search filters.",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

// ─── Status breakdown model + logic ──────────────────────────────────────────

private data class StatusBreakdown(
    val success:     Int = 0,
    val redirect:    Int = 0,
    val clientError: Int = 0,
    val serverError: Int = 0,
    val unknown:     Int = 0,
)

private fun computeBreakdown(transactions: List<GetAllLatestWithLimit>): StatusBreakdown {
    var success = 0; var redirect = 0; var clientError = 0; var serverError = 0; var unknown = 0
    transactions.forEach {
        when (it.responseCode) {
            null        -> unknown++
            in 0..299   -> success++
            in 300..399 -> redirect++
            in 400..499 -> clientError++
            else        -> serverError++
        }
    }
    return StatusBreakdown(success, redirect, clientError, serverError, unknown)
}
