package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.domain.entity.mnn.MnnBundleManifest
import com.shifthackz.aisdv1.domain.gateway.MnnArchiveExtractor
import com.shifthackz.aisdv1.domain.gateway.MnnBundleFileSystem
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID

@OptIn(ExperimentalForeignApi::class)
class IosMnnModelFileStoreTest {
    /**
     * given iOS MNN activation, validation is cancelled, expected cancellation preserved and no published model.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN activation validation is cancelled expected cancellation preserved and no published model`() =
        runTest {
            // Given
            val fixture = StoreFixture()
            val cancellation = CancellationException("cancel validation")
            fixture.validationFailure = cancellation
            try {
                // When
                val failure =
                    runCatching { fixture.store.activate("mnn_test", "archive") }.exceptionOrNull()

                // Then
                assertTrue(failure === cancellation)
                assertFalse(fixture.store.isDownloaded("mnn_test"))
                assertEquals(emptyList(), fixture.children())
            } finally {
                fixture.close()
            }
        }

    /**
     * given iOS MNN model leases, one reader closes twice during deletion, expected other reader retains files until final close.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN model leases one reader closes twice during deletion expected other reader retains files until final close`() =
        runTest {
            // Given
            val fixture = StoreFixture()
            try {
                fixture.store.activate("mnn_test", "archive")
                val first = fixture.store.acquire("mnn_test")
                val second = fixture.store.acquire("mnn_test")

                // When
                fixture.store.delete("mnn_test")
                first.close()
                first.close()

                // Then
                assertTrue(fixture.exists(fixture.store.path("mnn_test")))
                second.close()
                assertFalse(fixture.exists(fixture.store.path("mnn_test")))
            } finally {
                fixture.close()
            }
        }

    /**
     * given iOS MNN model ownership, deletion occurs during a lease, expected files retained until close.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN model ownership deletion occurs during a lease expected files retained until close`() =
        runTest {
            // Given
            val fixture = StoreFixture()
            try {
                fixture.store.activate("mnn_test", "archive")
                val lease = fixture.store.acquire("mnn_test")

                // When
                fixture.store.delete("mnn_test")

                // Then
                assertFalse(fixture.store.isDownloaded("mnn_test"))
                assertTrue(fixture.exists(fixture.store.path("mnn_test")))
                lease.close()
                assertFalse(fixture.exists(fixture.store.path("mnn_test")))
            } finally {
                fixture.close()
            }
        }

    /**
     * given iOS MNN model selection, deletion is requested before commit, expected busy without unavailable marker.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN model selection deletion is requested before commit expected busy without unavailable marker`() =
        runTest {
            // Given
            val fixture = StoreFixture()
            try {
                fixture.store.activate("mnn_test", "archive")
                val selection = fixture.store.acquireSelection("mnn_test")

                // When
                val result = runCatching { fixture.store.delete("mnn_test") }

                // Then
                assertTrue(result.exceptionOrNull() is IllegalStateException)
                assertTrue(fixture.store.isDownloaded("mnn_test"))
                selection.close()
            } finally {
                fixture.close()
            }
        }

    /**
     * given iOS MNN activation, integrity validation fails, expected no completion and staging cleanup.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN activation integrity validation fails expected no completion and staging cleanup`() =
        runTest {
            // Given
            val fixture = StoreFixture()
            fixture.shouldFailValidation = true
            try {
                // When
                val failure =
                    runCatching { fixture.store.activate("mnn_test", "archive") }.exceptionOrNull()

                // Then
                assertTrue(failure is IllegalArgumentException)
                assertFalse(fixture.store.isDownloaded("mnn_test"))
                assertEquals(emptyList(), fixture.children())
            } finally {
                fixture.close()
            }
        }

    /**
     * given iOS MNN activation, replacement targets a leased complete model, expected original remains installed.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN activation replacement targets a leased complete model expected original remains installed`() =
        runTest {
            // Given
            val fixture = StoreFixture()
            try {
                fixture.store.activate("mnn_test", "archive")
                val lease = fixture.store.acquire("mnn_test")

                // When
                val failure = runCatching {
                    fixture.store.activate(
                        "mnn_test",
                        "replacement"
                    )
                }.exceptionOrNull()

                // Then
                assertTrue(failure is IllegalStateException)
                assertTrue(fixture.store.isDownloaded("mnn_test"))
                assertEquals(listOf("mnn_test"), fixture.children())
                lease.close()
            } finally {
                fixture.close()
            }
        }

    /**
     * given iOS MNN catalog ownership, path escape or custom identity is supplied, expected rejection.
     *
     * @author Dmitriy Moroz
     */
    @Test
    fun `given iOS MNN catalog ownership path escape or custom identity is supplied expected rejection`() {
        // Given
        val fixture = StoreFixture()
        try {
            // When
            val invalidIds =
                listOf("../mnn_escape", "mnn_test/../../escape", "custom_mnn", "mnn_custom")

            // Then
            invalidIds.forEach { id ->
                assertFailsWith<IllegalArgumentException> {
                    fixture.store.path(
                        id
                    )
                }
            }
        } finally {
            fixture.close()
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
private class StoreFixture : AutoCloseable {
    private val manager = NSFileManager.defaultManager
    private val root =
        NSTemporaryDirectory().trimEnd('/') + "/mnn-store-unit-" + NSUUID().UUIDString
    var shouldFailValidation = false
    var validationFailure: Throwable? = null
    private val descriptor = object : FileProviderDescriptor {
        override val providerPath = "unit"
        override val imagesCacheDirPath = root
        override val logsCacheDirPath = root
        override val localModelDirPath = root
        override val workCacheDirPath = root
    }
    private val extractor = object : MnnArchiveExtractor {
        override fun extract(archivePath: String, stagingPath: String) {
            check(manager.createFileAtPath("$stagingPath/manifest.json", null, null))
        }
    }
    private val validator = object : MnnBundleValidator {
        override suspend fun invoke(path: String): MnnBundleManifest {
            validationFailure?.let { throw it }
            require(!shouldFailValidation) { "invalid integrity" }
            return MnnBundleManifest("unit", emptyList(), "a".repeat(64))
        }
    }
    private val files = object : MnnBundleFileSystem {
        override fun canonicalRoot(path: String) = path
        override fun isRegularFile(path: String) = manager.fileExistsAtPath(path)
        override fun readManifest(path: String) = error("Not used by lease units")
        override fun sha256(path: String) = error("Not used by lease units")
        override fun metadata(path: String) = error("Not used by lease units")
        override fun entries(path: String) = error("Not used by lease units")
    }
    val store: IosMnnModelFileStore

    init {
        check(manager.createDirectoryAtPath(root, true, null, null))
        store = IosMnnModelFileStore(descriptor, extractor, validator, files)
    }

    fun exists(path: String) = manager.fileExistsAtPath(path)
    fun children() =
        checkNotNull(manager.contentsOfDirectoryAtPath(root, null)).filterIsInstance<String>()

    override fun close() {
        manager.removeItemAtPath(root, null)
    }
}
