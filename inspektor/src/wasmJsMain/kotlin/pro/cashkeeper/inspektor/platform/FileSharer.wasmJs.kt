package pro.cashkeeper.inspektor.platform

internal actual fun FileSharer(): FileSharer = object : FileSharer {
    override fun shareFile(filePath: String, mimeType: String) {
        // Not implemented for Wasm
    }
}
