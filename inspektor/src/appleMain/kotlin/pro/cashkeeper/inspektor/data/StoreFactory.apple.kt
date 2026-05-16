package pro.cashkeeper.inspektor.data

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.extensions.listStoreOf
import kotlinx.io.files.Path
import pro.cashkeeper.inspektor.platform.getAppDataDir

internal actual fun createOverrideStore(): KStore<List<Override>> {
    return listStoreOf<Override>(file = Path("${getAppDataDir()}/overrideStore"))
}
