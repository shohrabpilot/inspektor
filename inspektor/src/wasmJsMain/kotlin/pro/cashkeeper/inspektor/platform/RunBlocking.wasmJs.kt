package pro.cashkeeper.inspektor.platform

public actual fun <T> runBlocking(block: suspend () -> T): T {
    error("runBlocking is not supported on Wasm")
}
