package com.shifthackz.aisdv1.app.inference

import android.app.Application
import android.content.Context
import com.shifthackz.aisdv1.domain.entity.BonsaiBackend
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import java.util.UUID
import org.json.JSONObject
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies the actual atomic exchange files and strict transient request format.
 * Image validation is injected so these tests isolate storage from decoding.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
class FileGenerationFileStoreTest {
    @get:Rule
    val temporary = TemporaryFolder()

    private val context = mockk<Context>()
    private val validator = mockk<PngImageValidator>(relaxed = true)
    private val job = UUID.randomUUID().toString()
    private lateinit var store: FileGenerationFileStore
    private lateinit var model: File

    @Before
    fun initialize() {
        every { context.filesDir } returns temporary.root
        model = temporary.newFolder("model").canonicalFile
        store = FileGenerationFileStore(context, validator)
    }

    @Test
    fun `given generation IPC storage, Bonsai backends and NSFW values vary, expected captured inputs and version preserved`() {
        for (backend in BonsaiBackend.entries) for (isNsfw in listOf(false, true)) {
            // Given
            val request = bonsaiRequest(model.path).let {
                it.copy(payload = it.payload.copy(bonsaiBackend = backend, nsfw = isNsfw))
            }

            // When
            store.writeRequest(job, request)
            val restored = store.readRequest(job)

            // Then
            assertEquals(request, restored)
            assertEquals(4, JSONObject(requestFile().readText()).getInt("version"))
        }
    }

    @Test
    fun `given generation IPC storage, batch contains multiple images, expected one child image`() {
        // Given
        val request = bonsaiRequest(model.path)

        // When
        store.writeRequest(job, request.copy(payload = request.payload.copy(batchCount = 3)))
        val restored = store.readRequest(job)

        // Then
        assertEquals(1, restored.payload.batchCount)
    }

    @Test
    fun `given generation IPC storage, model path contains parent segment, expected canonical child path`() {
        // Given
        val request = bonsaiRequest(File(model, "../model").path)

        // When
        store.writeRequest(job, request)
        val restored = store.readRequest(job)

        // Then
        assertEquals(model.canonicalPath, restored.modelPath)
    }

    @Test
    fun `given generation IPC storage, old request version is read, expected rejection without migration`() {
        // Given
        writeModifiedRequest { put("version", 1) }

        // When
        val actual = runCatching { store.readRequest(job) }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, Bonsai backend is unknown, expected rejection without automatic fallback`() {
        // Given
        writeModifiedRequest { put("bonsaiBackend", "missing-backend") }

        // When
        val actual = runCatching { store.readRequest(job) }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, Bonsai backend is absent, expected strict missing field rejection`() {
        // Given
        writeModifiedRequest { remove("bonsaiBackend") }

        // When
        val actual = runCatching { store.readRequest(job) }

        // Then
        assertThrows(Exception::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, NSFW field is a string, expected rejection without Boolean coercion`() {
        // Given
        writeModifiedRequest { put("nsfw", "true") }

        // When
        val actual = runCatching { store.readRequest(job) }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, NSFW field is absent, expected strict missing field rejection`() {
        // Given
        writeModifiedRequest { remove("nsfw") }

        // When
        val actual = runCatching { store.readRequest(job) }

        // Then
        assertThrows(Exception::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, engine name is unknown, expected request rejection`() {
        // Given
        writeModifiedRequest { put("engine", "UNKNOWN") }

        // When
        val actual = runCatching { store.readRequest(job) }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, model path is unreadable, expected no request admission`() {
        // Given
        writeModifiedRequest { put("model", File(temporary.root, "absent").path) }

        // When
        val actual = runCatching { store.readRequest(job) }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, dimensions or step counts exceed limits, expected request rejection`() {
        for ((field, value) in listOf(
            "width" to 4097,
            "height" to 0,
            "steps" to 0,
            "steps" to 1001
        )) {
            // Given
            writeModifiedRequest { put(field, value) }

            // When
            val actual = runCatching { store.readRequest(job) }

            // Then
            assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
        }
    }

    @Test
    fun `given generation IPC storage, prompt exceeds request byte limit, expected no committed request`() {
        // Given
        val request = bonsaiRequest(model.path)

        // When
        val actual = runCatching {
            store.writeRequest(
                job,
                request.copy(payload = request.payload.copy(prompt = "x".repeat(4 * 1024 * 1024)))
            )
        }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
        assertFalse(requestFile().exists())
    }

    @Test
    fun `given generation IPC storage, result fails image validation, expected no published output`() {
        // Given
        store.writeRequest(job, bonsaiRequest(model.path))
        every { validator.validate(any(), any()) } throws IllegalArgumentException("invalid image")

        // When
        val actual = runCatching { store.writeResult(job, byteArrayOf(1)) }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
        assertFalse(File(requestFile().parentFile, "result.png").exists())
    }

    @Test
    fun `given generation IPC storage, completed output is read back, expected write and read validate captured request`() {
        // Given
        val request = bonsaiRequest(model.path)
        val output = byteArrayOf(1, 2, 3)
        store.writeRequest(job, request)

        // When
        store.writeResult(job, output)
        val actual = store.readResult(job)

        // Then
        assertArrayEquals(output, actual)
        verify(exactly = 2) { validator.validate(output, request) }
    }

    @Test
    fun `given generation IPC storage, previously written result fails later validation, expected no host publication`() {
        // Given
        store.writeRequest(job, bonsaiRequest(model.path))
        store.writeResult(job, byteArrayOf(1))
        every { validator.validate(any(), any()) } throws IllegalArgumentException("changed image")

        // When
        val actual = runCatching { store.readResult(job) }

        // Then
        assertThrows(IllegalArgumentException::class.java) { actual.getOrThrow() }
    }

    @Test
    fun `given generation IPC storage, job identity contains traversal, expected read boundaries preserve model root`() {
        // Given
        val request = bonsaiRequest(model.path)

        // When
        val write = runCatching { store.writeRequest("../model", request) }
        val delete = runCatching { store.delete("../model") }

        // Then
        assertThrows(IllegalArgumentException::class.java) { write.getOrThrow() }
        assertThrows(IllegalArgumentException::class.java) { delete.getOrThrow() }
        assertTrue(model.exists())
    }

    @Test
    fun `given generation IPC storage, abandoned attempts are cleared, expected only owned UUID exchange files removed`() {
        // Given
        store.writeRequest(job, bonsaiRequest(model.path))
        val foreign = File(temporary.root, "inference/jobs/keep").apply { mkdirs() }
        val owned = requireNotNull(requestFile().parentFile)

        // When
        store.clearAbandoned()

        // Then
        assertFalse(owned.exists())
        assertTrue(model.exists())
        assertTrue(foreign.exists())
    }

    private fun requestFile() = File(temporary.root, "inference/jobs/$job/request.json")

    private fun writeModifiedRequest(change: JSONObject.() -> Unit) {
        store.writeRequest(job, bonsaiRequest(model.path))
        val json = JSONObject(requestFile().readText()).apply(change)
        requestFile().writeText(json.toString())
    }
}
