package pro.cashkeeper.inspektor.ui.transactionlist

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pro.cashkeeper.inspektor.data.HttpTransaction
import pro.cashkeeper.inspektor.data.InspektorDataSource
import pro.cashkeeper.inspektor.har.Har
import pro.cashkeeper.inspektor.har.json
import pro.cashkeeper.inspektor.har.toHarEntry
import pro.cashkeeper.inspektor.platform.FileSharer
import pro.cashkeeper.inspektor.platform.Os
import pro.cashkeeper.inspektor.platform.currentOs
import pro.cashkeeper.inspektor.platform.getAppCacheDir
import pro.cashkeeper.inspektor.platform.ioDispatcher
import pro.cashkeeper.inspektor.ui.UiEvent
import pro.cashkeeper.inspektor.utils.atLocalEndOfDay
import pro.cashkeeper.inspektor.utils.atLocalStartOfDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.io.encodeToSink
import kotlin.time.Duration.Companion.days


internal class TransactionListViewModel(
    private val inspektorDataSource: InspektorDataSource,
    private val fileSharer: FileSharer,
) : ViewModel() {

    private val _startDate = MutableStateFlow((Clock.System.now() - 6.days).atLocalStartOfDay())
    val startDate = _startDate.asStateFlow()
    private val _endDate = MutableStateFlow(Clock.System.now().atLocalEndOfDay())
    val endDate = _endDate.asStateFlow()

    private val _events: MutableStateFlow<UiEvent> = MutableStateFlow(UiEvent.NoEvent)
    val events = _events.asStateFlow()

    val searchFieldState = TextFieldState()
    private val searchFlow = snapshotFlow { searchFieldState.text }

    val allCount = inspektorDataSource.getAllHttpTransactionsCount().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(4000), 0
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions = combine(startDate, endDate, searchFlow) { startDate, endDate, searchTerm ->
        (startDate to endDate) to searchTerm
    }.flatMapLatest { (dates, searchTerm) ->
        val (startDate, endDate) = dates
        val responseCode = if (searchTerm.isDigitsOnly()) searchTerm else ""
        val path = if (responseCode.isNotEmpty()) "" else searchTerm

        inspektorDataSource.getAllLatestHttpTransactionsFilteredFlow(
            startDate,
            endDate,
            "$responseCode",
            "$path"
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(4000), emptyList())


    fun deleteTransactions(beforeDate: Instant) {
        viewModelScope.launch {
            inspektorDataSource.deleteBefore(beforeDate)
        }
    }

    fun onDateRangeSelected(startDate: Instant, endDate: Instant) {
        _startDate.value = startDate.atLocalStartOfDay()
        _endDate.value = endDate.atLocalEndOfDay()
    }

    fun shareAsHar() = viewModelScope.launch {
        try {
            val transactions = inspektorDataSource.getAllLatestHttpTransactionsForDateRange(
                startDate.value,
                endDate.value
            )
            if (transactions.isEmpty()) {
                _events.value = UiEvent.ShowSnackBar("No transactions to share")
                return@launch
            }
            // macOS won't open .har files if supporting application is not installed
            val fileExtension = if (currentOs is Os.Desktop.MACOS) "txt" else "har"

            val harFilePath = "${getAppCacheDir()}/inspektor_share/inspektor.$fileExtension"
            createLogFile(
                filePath = harFilePath,
                transactions = transactions
            )

            fileSharer.shareFile(harFilePath, "text/plain")
        } catch (e: IOException) {
            _events.value = UiEvent.ShowErrorDialog("Error sharing HAR file: ${e.message}")
            println("Error sharing HAR file: ${e.message}")
        } catch (e: Exception) {
            _events.value = UiEvent.ShowErrorDialog("An unexpected error occurred: ${e.message}")
            println("An unexpected error occurred: ${e.message}")
        }
    }
}

private fun CharSequence.isDigitsOnly(): Boolean {
    return all { it.isDigit() }
}


@OptIn(ExperimentalSerializationApi::class)
internal suspend fun createLogFile(
    filePath: String,
    transactions: List<HttpTransaction>
) = withContext(ioDispatcher) {
    val path = Path(filePath)
    try {
        val log = Har.Log(
            creator = Har.Creator(name = "Inspektor"),
            entries = transactions.mapNotNull { it.toHarEntry() },
        )
        print("Creating HAR log with ${log.entries.size} entries at $filePath")
        SystemFileSystem.createDirectories(path.parent!!)
        SystemFileSystem.sink(path).buffered().use {
            json.encodeToSink(Har(log), it)
        }
        println("Successfully wrote text to $filePath")
    } catch (e: IOException) {
        println("Error writing to $filePath: ${e.message}")
        throw e
    } catch (e: Exception) {
        println("An unexpected error occurred: ${e.message}")
        throw e
    }
}