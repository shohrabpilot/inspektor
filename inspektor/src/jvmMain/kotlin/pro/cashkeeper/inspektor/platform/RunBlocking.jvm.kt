package pro.cashkeeper.inspektor.platform

import kotlinx.coroutines.runBlocking as kotlinxRunBlocking

public actual fun <T> runBlocking(block: suspend () -> T): T = kotlinxRunBlocking { block() }
