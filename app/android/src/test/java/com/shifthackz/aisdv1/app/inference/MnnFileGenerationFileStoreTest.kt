package com.shifthackz.aisdv1.app.inference

import android.app.Application
import android.content.Context
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import io.mockk.every
import io.mockk.mockk
import java.io.File
import java.util.UUID
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Checks MNN request fields through the real atomic IPC file format.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, application = Application::class, sdk = [28])
class MnnFileGenerationFileStoreTest {
    @get:Rule
    val temporary = TemporaryFolder()
    private val context = mockk<Context>()
    private val job = UUID.randomUUID().toString()
    private lateinit var store: FileGenerationFileStore
    private lateinit var model: File

    @Before
    fun initialize() {
        every { context.filesDir } returns temporary.root
        model = temporary.newFolder("model").canonicalFile
        store = FileGenerationFileStore(context, mockk(relaxed = true))
    }

    @Test
    fun `given MNN IPC request, CPU profile and identity are captured, expected exact exchange round trip`() {
        // Given
        val original = request(MnnBackend.CPU)

        // When
        store.writeRequest(job, original)
        val restored = store.readRequest(job)

        // Then
        assertEquals(original, restored)
        assertEquals(4, JSONObject(requestFile().readText()).getInt("version"))
    }

    @Test
    fun `given MNN IPC request, OpenCL and supplementary prompt are captured, expected backend and UTF8 preserved`() {
        // Given
        val original = request(MnnBackend.OPENCL)

        // When
        store.writeRequest(job, original)
        val restored = store.readRequest(job)

        // Then
        assertEquals(original, restored)
        assertEquals("cat 😀 日本語", restored.payload.prompt)
    }

    @Test
    fun `given MNN IPC request, backend name is unknown, expected rejection without fallback`() {
        // Given
        store.writeRequest(job, request(MnnBackend.CPU))
        val json = JSONObject(requestFile().readText()).put("mnnBackend", "AUTO")
        requestFile().writeText(json.toString())

        // When
        val action = { store.readRequest(job); Unit }

        // Then
        assertThrows(IllegalArgumentException::class.java, action)
    }

    @Test
    fun `given MNN IPC request, captured field is missing, expected strict protocol rejection`() {
        // Given
        store.writeRequest(job, request(MnnBackend.CPU))
        val json = JSONObject(requestFile().readText()).apply { remove("mnnActivationId") }
        requestFile().writeText(json.toString())

        // When
        val failure = runCatching { store.readRequest(job) }.exceptionOrNull()

        // Then
        assertEquals(true, failure != null)
    }

    /**
     * Supplies supported MNN fields while retaining the transient IPC's provider-neutral defaults.
     *
     * @param backend Explicit backend submitted by the generation form.
     *
     * @author Dmitriy Moroz
     */
    private fun request(backend: MnnBackend) = bonsaiRequest(model.path).let { base ->
        base.copy(
            engine = NativeGenerationEngine.MNN, payload = base.payload.copy(
                prompt = "cat 😀 日本語", negativePrompt = "", width = 512, height = 512,
                cfgScale = 7.5f, sampler = "PNDM", mnnBackend = backend,
                mnnModelId = "mnn_sd15", mnnActivationId = "a".repeat(64),
                mnnProfile = "sd15-512-general-int8-v1"
            )
        )
    }

    /**
     * Locates the UUID request written by the production atomic exchange store.
     *
     * @author Dmitriy Moroz
     */
    private fun requestFile() = File(temporary.root, "inference/jobs/$job/request.json")
}
