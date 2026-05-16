package pro.cashkeeper.inspektor.ui.overriding.editoverride.components

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pro.cashkeeper.inspektor.data.HttpMethod
import pro.cashkeeper.inspektor.ui.components.SimpleDropdown

@Composable
internal fun HttpMethodDropdown(
    selectedMethod: HttpMethod, onMethodSelected: (HttpMethod) -> Unit
) = SimpleDropdown(
    items = HttpMethod.currentlySupported,
    selectedItem = selectedMethod,
    onItemSelected = onMethodSelected,
    itemAsString = { "HTTP ${it.name}" },
    modifier = Modifier.widthIn(max = 160.dp),
)