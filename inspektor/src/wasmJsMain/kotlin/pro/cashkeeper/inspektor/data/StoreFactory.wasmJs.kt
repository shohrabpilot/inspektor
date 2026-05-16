package pro.cashkeeper.inspektor.data

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.Codec
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal actual fun createOverrideStore(): KStore<List<Override>> {
    val state = MutableStateFlow<List<Override>?>(emptyList())
    val codec = object : Codec<List<Override>> {
        override suspend fun decode(): List<Override>? = state.value
        override suspend fun encode(value: List<Override>?) { state.value = value }
    }
    return KStore(codec = codec)
}
