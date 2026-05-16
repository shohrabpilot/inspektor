package pro.cashkeeper.inspektor.data

import io.github.xxfast.kstore.KStore

internal expect fun createOverrideStore(): KStore<List<Override>>
