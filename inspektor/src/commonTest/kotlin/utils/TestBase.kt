package utils

import pro.cashkeeper.inspektor.Inspektor
import pro.cashkeeper.inspektor.IsTest
import pro.cashkeeper.inspektor.LogLevel
import pro.cashkeeper.inspektor.UnstableInspektorAPI
import pro.cashkeeper.inspektor.data.DriverFactory
import pro.cashkeeper.inspektor.data.HttpTransaction
import pro.cashkeeper.inspektor.data.InspektorDataSourceImpl
import pro.cashkeeper.inspektor.data.InspektorDatabase
import pro.cashkeeper.inspektor.data.Override
import pro.cashkeeper.inspektor.data.OverrideRepositoryImpl
import pro.cashkeeper.inspektor.data.adapters.instantAdapter
import pro.cashkeeper.inspektor.data.adapters.setMapEntryAdapter
import pro.cashkeeper.inspektor.data.setApplicationId
import pro.cashkeeper.inspektor.platform.NotificationManager
import io.github.xxfast.kstore.storeOf
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import kotlinx.io.files.FileSystem
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

internal val TEST_DIR = Path("test")

abstract class TestBase {
    internal val db by lazy { createTestDb() }

    internal val store by lazy {
        storeOf<List<Override>>(codec = KStoreInMemoryCodec())
    }

    @BeforeTest
    fun createTestDir() {
        with(SystemFileSystem) { if(!exists(TEST_DIR)) createDirectories(TEST_DIR) }
    }

    @AfterTest
    fun deleteTestDir() {
        with(SystemFileSystem) { if(exists(TEST_DIR)) { deleteRecursively(TEST_DIR) } }
    }

    init {
        IsTest = true

        @OptIn(UnstableInspektorAPI::class)
        setApplicationId("com.test.inspektor")
    }

    private fun createTestDb(): InspektorDatabase {
        val driver = DriverFactory.createTempDbDriver()
        return InspektorDatabase(
            driver, HttpTransaction.Adapter(
                requestDateAdapter = instantAdapter,
                responseDateAdapter = instantAdapter,
                requestHeadersAdapter = setMapEntryAdapter,
                responseHeadersAdapter = setMapEntryAdapter,
                originalResponseHeadersAdapter = setMapEntryAdapter,
                originalRequestHeadersAdapter = setMapEntryAdapter,
            )
        )
    }

    protected fun createMockClient(
        logLevel: LogLevel = LogLevel.BODY,
        block: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
    ): HttpClient {
        return HttpClient(MockEngine { block(it) }) {
            install(Inspektor) {
                level = logLevel
                this.dataSource = InspektorDataSourceImpl(db)
                this.overrideRepository = OverrideRepositoryImpl(store)
                this.notificationManager = object : NotificationManager {
                    override fun notify(title: String, message: String) {
                        println("$title: $message")
                    }
                }
            }
        }
    }
}

private fun FileSystem.deleteRecursively(path: Path, mustExist: Boolean = true) {
    list(path).forEach {
        if (metadataOrNull(it)?.isDirectory == true) {
            deleteRecursively(it, mustExist)
        } else {
            delete(it, mustExist)
        }
    }
    delete(path, mustExist)
}