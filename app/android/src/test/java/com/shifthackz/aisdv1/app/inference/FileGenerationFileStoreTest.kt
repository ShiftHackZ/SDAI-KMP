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
    fun `given Bonsai backends and NSFW values, exchange preserves captured inputs`() {
        for (backend in BonsaiBackend.entries) for (isNsfw in listOf(false, true)) {
            val request = bonsaiRequest(model.path).let {
                it.copy(payload = it.payload.copy(bonsaiBackend = backend, nsfw = isNsfw))
            }

            store.writeRequest(job, request)

            assertEquals(request, store.readRequest(job))
            assertEquals(2, JSONObject(requestFile().readText()).getInt("version"))
        }
    }

    @Test
    fun `given batch request, child exchange admits only one image`() {
        val request = bonsaiRequest(model.path)
        store.writeRequest(job, request.copy(payload = request.payload.copy(batchCount = 3)))

        assertEquals(1, store.readRequest(job).payload.batchCount)
    }

    @Test
    fun `given readable model path with parent segment, child receives canonical path`() {
        store.writeRequest(job, bonsaiRequest(File(model, "../model").path))

        assertEquals(model.canonicalPath, store.readRequest(job).modelPath)
    }

    @Test
    fun `given old request version, attempt is rejected without migration`() {
        writeModifiedRequest { put("version", 1) }

        assertThrows(IllegalArgumentException::class.java) { store.readRequest(job) }
    }

    @Test
    fun `given unknown Bonsai backend, request is rejected instead of selecting AUTO`() {
        writeModifiedRequest { put("bonsaiBackend", "missing-backend") }

        assertThrows(IllegalArgumentException::class.java) { store.readRequest(job) }
    }

    @Test
    fun `given missing backend, request does not supply a default`() {
        writeModifiedRequest { remove("bonsaiBackend") }

        assertThrows(Exception::class.java) { store.readRequest(job) }
    }

    @Test
    fun `given string NSFW value, request is rejected instead of coercing Boolean`() {
        writeModifiedRequest { put("nsfw", "true") }

        assertThrows(IllegalArgumentException::class.java) { store.readRequest(job) }
    }

    @Test
    fun `given missing NSFW field, request is rejected`() {
        writeModifiedRequest { remove("nsfw") }

        assertThrows(Exception::class.java) { store.readRequest(job) }
    }

    @Test
    fun `given unknown engine, request is rejected`() {
        writeModifiedRequest { put("engine", "UNKNOWN") }

        assertThrows(IllegalArgumentException::class.java) { store.readRequest(job) }
    }

    @Test
    fun `given unreadable model path, no generation request is admitted`() {
        writeModifiedRequest { put("model", File(temporary.root, "absent").path) }

        assertThrows(IllegalArgumentException::class.java) { store.readRequest(job) }
    }

    @Test
    fun `given oversized dimensions or invalid steps, request is rejected`() {
        for ((field, value) in listOf(
            "width" to 4097,
            "height" to 0,
            "steps" to 0,
            "steps" to 1001
        )) {
            writeModifiedRequest { put(field, value) }

            assertThrows(IllegalArgumentException::class.java) { store.readRequest(job) }
        }
    }

    @Test
    fun `given excessive prompt size, no request file is committed`() {
        val request = bonsaiRequest(model.path)

        assertThrows(IllegalArgumentException::class.java) {
            store.writeRequest(
                job,
                request.copy(payload = request.payload.copy(prompt = "x".repeat(4 * 1024 * 1024)))
            )
        }
        assertFalse(requestFile().exists())
    }

    @Test
    fun `given result fails validation, no result file is published`() {
        store.writeRequest(job, bonsaiRequest(model.path))
        every { validator.validate(any(), any()) } throws IllegalArgumentException("invalid image")

        assertThrows(IllegalArgumentException::class.java) {
            store.writeResult(
                job,
                byteArrayOf(1)
            )
        }

        assertFalse(File(requestFile().parentFile, "result.png").exists())
    }

    @Test
    fun `given completed output, both write and read validate against captured request`() {
        val request = bonsaiRequest(model.path)
        val output = byteArrayOf(1, 2, 3)
        store.writeRequest(job, request)

        store.writeResult(job, output)
        assertArrayEquals(output, store.readResult(job))

        verify(exactly = 2) { validator.validate(output, request) }
    }

    @Test
    fun `given result validation fails on later read, bytes cannot reach host publication`() {
        store.writeRequest(job, bonsaiRequest(model.path))
        store.writeResult(job, byteArrayOf(1))
        every { validator.validate(any(), any()) } throws IllegalArgumentException("changed image")

        assertThrows(IllegalArgumentException::class.java) { store.readResult(job) }
    }

    @Test
    fun `given path traversal job, storage cannot leave job root`() {
        assertThrows(IllegalArgumentException::class.java) {
            store.writeRequest(
                "../model",
                bonsaiRequest(model.path)
            )
        }
        assertThrows(IllegalArgumentException::class.java) { store.delete("../model") }
        assertTrue(model.exists())
    }

    @Test
    fun `given cleanup, only owned UUID exchange files are removed`() {
        store.writeRequest(job, bonsaiRequest(model.path))
        val foreign = File(temporary.root, "inference/jobs/keep").apply { mkdirs() }

        store.clearAbandoned()

        assertFalse(requestFile().parentFile.exists())
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
