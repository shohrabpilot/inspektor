package pro.cashkeeper.inspektor.ui.transactionlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.DisplayMode
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import pro.cashkeeper.inspektor.utils.atLocalStartOfDay
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeleteDialog(
    onDismissRequest: () -> Unit,
    onConfirm: (Instant) -> Unit,
) {
    val datePickerState = rememberDatePickerState(initialDisplayMode = DisplayMode.Picker)
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            Modifier.padding(12.dp).background(
                MaterialTheme.colorScheme.surface, RoundedCornerShape(20.dp)
            )
        ) {
            DatePicker(
                state = datePickerState,
                title = {
                    Text(
                        "Delete Transactions",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                },
                headline = {
                    Text(
                        "Delete transactions before...",
                        modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                showModeToggle = true,
                modifier = Modifier.heightIn(max = 640.dp).weight(1f, fill = false),
            )
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                OutlinedButton(onClick = onDismissRequest) {
                    Text("Cancel")
                }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        onConfirm(
                            datePickerState.selectedDateInstant
                                ?.atLocalStartOfDay(TimeZone.currentSystemDefault())
                                ?: Clock.System.now()
                        )
                        onDismissRequest()
                    }
                ) {
                    Text("Delete")
                }
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
internal val DatePickerState.selectedDateInstant: Instant?
    get() = selectedDateMillis?.let { Instant.fromEpochMilliseconds(it) }