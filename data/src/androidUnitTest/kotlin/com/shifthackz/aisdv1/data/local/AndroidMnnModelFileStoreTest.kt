package com.shifthackz.aisdv1.data.local

import com.shifthackz.aisdv1.core.common.file.FileProviderDescriptor
import com.shifthackz.aisdv1.core.common.file.MNN_INFERENCE_LOCK_SUFFIX
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.mnn.MnnBundleFile
import com.shifthackz.aisdv1.domain.entity.mnn.MnnBundleManifest
import com.shifthackz.aisdv1.domain.preference.ConfigurationStore
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/**
 * Checks atomic file ownership using actual ZIP files and OS locks.
 * The validator is a boundary stub; these small archives do not establish model compatibility.
 *
 * @author Dmitriy Moroz
 */
class AndroidMnnModelFileStoreTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val descriptor = mockk<FileProviderDescriptor>()
    private val configuration = mockk<ConfigurationStore>()
    private val validator = mockk<MnnBundleValidator>()
    private val identity = "a".repeat(64)
    private val names = listOf(
        "text_encoder.mnn",
        "text_encoder.mnn.weight",
        "unet.mnn",
        "unet.mnn.weight",
        "vae_decoder.mnn",
        "vae_decoder.mnn.weight",
        "tokenizer.mtok",
        "LICENSE",
        "NOTICE.md"
    )
    private lateinit var owned: File
    private lateinit var store: AndroidMnnModelFileStore

    @Before
    fun initialize() {
        owned = temporary.newFolder("models").canonicalFile
        every { descriptor.localModelDirPath } returns owned.path
        every { configuration.localMnnCustomActivationId } returns ""
        coEvery { validator(any()) } returns manifest()
        store = AndroidMnnModelFileStore(descriptor, validator, configuration)
    }

    @Test
    fun `given MNN activation, complete archive validates, expected atomic root and completion marker`() =
        runTest {
            // Given
            val archive = archive()

            // When
            store.activate("mnn_catalog", archive.path)

            // Then
            val root = File(store.path("mnn_catalog", ""))
            assertTrue(store.isDownloaded("mnn_catalog"))
            assertEquals(identity, File(root, ".complete").readText())
            assertEquals((names + "manifest.json" + ".complete").toSet(), root.list()!!.toSet())
            assertTrue(archive.exists())
            assertFalse(owned.list()!!.any { it.startsWith(".mnn-staging-") })
        }

    @Test
    fun `given MNN activation, validator rejects archive, expected no published root or staging residue`() =
        runTest {
            // Given
            val failure = IllegalArgumentException("hash")
            coEvery { validator(any()) } throws failure

            // When
            val actual =
                runCatching { store.activate("mnn_catalog", archive().path) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            assertFalse(store.isDownloaded("mnn_catalog"))
            assertFalse(owned.list()!!.any { it.startsWith(".mnn-staging-") })
        }

    @Test
    fun `given MNN activation, validation is cancelled, expected no publication and cancellation preserved`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancel")
            coEvery { validator(any()) } throws cancellation

            // When
            val failure =
                runCatching { store.activate("mnn_catalog", archive().path) }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            assertFalse(store.isDownloaded("mnn_catalog"))
            assertFalse(owned.list()!!.any { it.startsWith(".mnn-staging-") })
        }

    @Test
    fun `given MNN activation, archive contains traversal entry, expected outside files untouched`() =
        runTest {
            // Given
            val archive = archive(names.dropLast(1) + "../outside")

            // When
            val failure =
                runCatching { store.activate("mnn_catalog", archive.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertFalse(File(owned, "outside").exists())
            assertFalse(File(temporary.root, "outside").exists())
        }

    @Test
    fun `given MNN activation, archive contains symbolic link mode, expected rejection before publication`() =
        runTest {
            // Given
            val archive = archive()
            val bytes = archive.readBytes()
            val fields = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            val central = (0..bytes.size - 46).first { fields.getInt(it) == 0x02014b50 }
            fields.putInt(central + 38, 0xa000 shl 16)
            archive.writeBytes(bytes)

            // When
            val failure =
                runCatching { store.activate("mnn_catalog", archive.path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            assertFalse(store.isDownloaded("mnn_catalog"))
        }

    @Test
    fun `given MNN activation, archive is incomplete, expected no downloaded model`() = runTest {
        // Given
        val archive = archive(names.dropLast(1))

        // When
        val failure = runCatching { store.activate("mnn_catalog", archive.path) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertFalse(store.isDownloaded("mnn_catalog"))
    }

    @Test
    fun `given MNN activation, existing complete root exists, expected immutable previous bytes preserved`() =
        runTest {
            // Given
            store.activate("mnn_catalog", archive().path)
            val root = File(store.path("mnn_catalog", ""))
            File(root, "LICENSE").writeText("retained")

            // When
            val failure =
                runCatching { store.activate("mnn_catalog", archive().path) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            assertEquals("retained", File(root, "LICENSE").readText())
            assertEquals(identity, File(root, ".complete").readText())
        }

    @Test
    fun `given MNN file ownership, host lease is active during delete, expected deferred removal until close`() =
        runTest {
            // Given
            store.activate("mnn_catalog", archive().path)
            val root = File(store.path("mnn_catalog", ""))
            val lease = store.acquire("mnn_catalog", "")

            // When
            store.delete("mnn_catalog")

            // Then
            assertTrue(root.exists())
            assertFalse(store.isDownloaded("mnn_catalog"))
            assertTrue(File(root, ".delete-requested").exists())
            lease.close()
            assertFalse(root.exists())
        }

    @Test
    fun `given MNN file ownership, child lease survives host close, expected bytes retained until later cleanup`() =
        runTest {
            // Given
            store.activate("mnn_catalog", archive().path)
            val root = File(store.path("mnn_catalog", ""))
            val host = store.acquire("mnn_catalog", "")
            val childFile =
                RandomAccessFile(File(owned, ".mnn_catalog$MNN_INFERENCE_LOCK_SUFFIX"), "rw")
            val child = childFile.channel.lock()

            // When
            store.delete("mnn_catalog")
            host.close()

            // Then
            assertTrue(root.exists())
            child.release()
            childFile.close()
            store.delete("mnn_catalog")
            assertFalse(root.exists())
        }

    @Test
    fun `given MNN file ownership, selection lease is active, expected delete rejected before marker mutation`() =
        runTest {
            // Given
            store.activate("mnn_catalog", archive().path)
            val root = File(store.path("mnn_catalog", ""))
            val lease = store.acquireSelection("mnn_catalog", "")

            // When
            val failure = runCatching { store.delete("mnn_catalog") }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            assertTrue(store.isDownloaded("mnn_catalog"))
            assertFalse(File(root, ".delete-requested").exists())
            lease.close()
            store.delete("mnn_catalog")
            assertFalse(root.exists())
        }

    @Test
    fun `given MNN custom deletion, later snapshot selection is busy, expected all target snapshots untouched`() {
        // Given
        val parent = File(owned, "mnn_custom").apply { mkdir() }
        val first =
            File(parent, "a".repeat(64)).apply { mkdir(); File(this, ".complete").writeText(name) }
        val second =
            File(parent, "b".repeat(64)).apply { mkdir(); File(this, ".complete").writeText(name) }
        val lease = store.acquireSelection(LocalAiModel.CustomMnn.id, second.name)

        // When
        val failure = runCatching { store.delete(LocalAiModel.CustomMnn.id) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalStateException)
        assertTrue(first.exists())
        assertTrue(second.exists())
        assertFalse(File(first, ".delete-requested").exists())
        assertFalse(File(second, ".delete-requested").exists())
        lease.close()
        store.delete(LocalAiModel.CustomMnn.id)
        assertFalse(first.exists())
        assertFalse(second.exists())
    }

    @Test
    fun `given MNN custom import, valid source is prepared, expected owned snapshot and untouched original`() =
        runTest {
            // Given
            val source = temporary.newFolder("Downloads")
            (names + "manifest.json").forEach { File(source, it).writeText("payload") }
            val before = source.listFiles()!!.associate { it.name to it.readText() }

            // When
            val activation = store.importCustom(source.path)

            // Then
            assertEquals(identity, activation)
            assertEquals(before, source.listFiles()!!.associate { it.name to it.readText() })
            assertTrue(
                File(
                    File(store.path(LocalAiModel.CustomMnn.id, identity)),
                    ".complete"
                ).isFile
            )
            assertEquals("", configuration.localMnnCustomActivationId)
            assertFalse(owned.list()!!.any { it.startsWith(".mnn-custom-") })
        }

    @Test
    fun `given MNN custom import, snapshot already exists, expected reuse without replacement`() =
        runTest {
            // Given
            val source = temporary.newFolder("Downloads")
            (names + "manifest.json").forEach { File(source, it).writeText("payload") }
            store.importCustom(source.path)
            val root = File(store.path(LocalAiModel.CustomMnn.id, identity))
            File(root, "LICENSE").writeText("existing")

            // When
            val activation = store.importCustom(source.path)

            // Then
            assertEquals(identity, activation)
            assertEquals("existing", File(root, "LICENSE").readText())
            assertTrue(File(source, "LICENSE").exists())
        }

    @Test
    fun `given MNN file ownership, partial directory lacks marker, expected unavailable acquisition`() {
        // Given
        val root = File(store.path("mnn_partial", "")).apply { mkdir() }
        File(root, "unet.mnn").writeText("partial")

        // When
        val failure = runCatching { store.acquire("mnn_partial", "") }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalStateException)
        assertFalse(store.isDownloaded("mnn_partial"))
    }

    @Test
    fun `given MNN root lookup, identifier contains parent traversal, expected rejection`() {
        // Given
        val invalid = "mnn_../external"

        // When
        val failure = runCatching { File(store.path(invalid, "")) }.exceptionOrNull()

        // Then
        assertTrue(failure is IllegalArgumentException)
        assertThrows(IllegalArgumentException::class.java) {
            File(
                store.path(
                    LocalAiModel.CustomMnn.id,
                    "../identity"
                )
            )
        }
    }

    /**
     * Builds a bounded ten-entry transport archive; model validity remains the validator's responsibility.
     *
     * @param payloadNames Flat payload entries supplied to the archive.
     *
     * @author Dmitriy Moroz
     */
    private fun archive(payloadNames: List<String> = names): File {
        val file = temporary.newFile()
        ZipOutputStream(file.outputStream()).use { zip ->
            (payloadNames + "manifest.json").forEach { name ->
                zip.putNextEntry(ZipEntry(name))
                zip.write("payload".toByteArray())
                zip.closeEntry()
            }
        }
        return file
    }

    /**
     * Provides a small inventory only for testing filesystem transactions, not pinned model validation.
     *
     * @author Dmitriy Moroz
     */
    private fun manifest() = MnnBundleManifest(
        "sd15-512-general-int8-v1",
        names.map { MnnBundleFile(it, 7, "0".repeat(64)) },
        identity
    )
}
