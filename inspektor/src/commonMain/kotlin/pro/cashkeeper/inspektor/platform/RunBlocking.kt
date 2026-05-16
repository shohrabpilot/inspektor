package pro.cashkeeper.inspektor.platform

public expect fun <T> runBlocking(block: suspend () -> T): T
