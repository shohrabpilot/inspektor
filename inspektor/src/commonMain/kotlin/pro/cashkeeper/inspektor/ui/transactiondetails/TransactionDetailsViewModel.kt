package pro.cashkeeper.inspektor.ui.transactiondetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pro.cashkeeper.inspektor.data.InspektorDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


internal class TransactionDetailsViewModel(
    private val transactionId: Long,
    private val dataSource: InspektorDataSource,
) : ViewModel() {
    val transaction = dataSource.getTransactionFlow(transactionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteTransaction() {
        viewModelScope.launch {
            dataSource.delete(transactionId)
        }
    }
}
