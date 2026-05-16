package pro.cashkeeper.inspektor.ui.transactiondetails

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import pro.cashkeeper.inspektor.data.InspektorDataSource
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn


internal class TransactionDetailsViewModel(
    transactionId: Long,
    dataSource: InspektorDataSource,
) : ViewModel() {
    val transaction = dataSource.getTransactionFlow(transactionId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
}
