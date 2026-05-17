package pro.cashkeeper.inspektor.ui

import androidx.compose.runtime.Composable
import pro.cashkeeper.inspektor.ui.theme.InspektorTheme
import pro.cashkeeper.inspektor.ui.transactionlist.TransactionListDetailScreen

@Composable
internal fun App() = InspektorTheme {
    TransactionListDetailScreen()
}
