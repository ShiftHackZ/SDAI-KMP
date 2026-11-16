package com.shifthackz.aisdv1.data.remote

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionHttpFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionProtocolFailure
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask
import com.shifthackz.aisdv1.network.api.easydiffusion.EasyDiffusionApi
import com.shifthackz.aisdv1.network.api.easydiffusion.EasyDiffusionHttpException
import com.shifthackz.aisdv1.network.api.easydiffusion.EasyDiffusionProtocolException
import com.shifthackz.aisdv1.network.model.EasyDiffusionModelRaw
import com.shifthackz.aisdv1.network.model.EasyDiffusionTaskMetadata
import com.shifthackz.aisdv1.network.response.EasyDiffusionImage
import com.shifthackz.aisdv1.network.response.EasyDiffusionModels
import com.shifthackz.aisdv1.network.response.EasyDiffusionSubmission
import com.shifthackz.aisdv1.network.response.EasyDiffusionFrame as WireFrame
import com.shifthackz.aisdv1.network.request.EasyDiffusionRequest as WireRequest
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Verifies protocol-to-domain translation without adding recovery policy or hiding failures.
 * @author Dmitriy Moroz
 */
class KtorEasyDiffusionRemoteDataSourceTest {
    private val api = mockk<EasyDiffusionApi>()
    private val source = KtorEasyDiffusionRemoteDataSource(api)
    private val url = "https://server.example/prefix"
    private val task = EasyDiffusionTask(url, "session", 42)
    private val request = EasyDiffusionRequest(
        url,
        "session",
        "folder/model",
        "lake",
        "blur",
        512,
        512,
        20,
        7f,
        42,
        1,
        inputImage = "data:image/png;base64,AQID",
        strength = 0.5f,
        selectionRevision = 7
    )

    @Test
    fun `catalog filters auxiliaries and blank IDs while preserving folders and first duplicate`() =
        runTest {
            coEvery { api.models(url) } returns EasyDiffusionModels(
                listOf(
                    EasyDiffusionModelRaw("folder/model", "", listOf("stable-diffusion")),
                    EasyDiffusionModelRaw("folder/model", "Duplicate", listOf("stable-diffusion")),
                    EasyDiffusionModelRaw("lora", "LoRA", listOf("lora")),
                    EasyDiffusionModelRaw("", "Blank", listOf("stable-diffusion"))
                )
            )
            val models = source.models(url)
            assertEquals(listOf("folder/model"), models.map { it.id })
            assertEquals("folder/model", models.single().name)
        }

    @Test
    fun `submission maps actual supported controls and captures original task endpoint`() =
        runTest {
            val body = slot<WireRequest>()
            coEvery { api.render(url, capture(body)) } returns EasyDiffusionSubmission(
                42,
                "/image/stream/42"
            )
            assertEquals(task, source.submit(request))
            assertEquals(request.inputImage, body.captured.initImage)
            assertEquals(request.strength, body.captured.strength)
            assertEquals(request.model, body.captured.model)
            assertEquals(request.seed, body.captured.seed)
            assertEquals("ddim", body.captured.sampler)
            assertEquals("png", body.captured.format)
            assertEquals(1, body.captured.outputs)
        }

    @Test
    fun `preview images are ignored and terminal output retains reported seed and model`() =
        runTest {
            val image = EasyDiffusionImage("original", 42)
            coEvery { api.stream(url, 42, any()) } coAnswers {
                val consume = thirdArg<suspend (WireFrame) -> Unit>()
                consume(WireFrame(step = 2, totalSteps = 20, output = listOf(image)))
                consume(
                    WireFrame(
                        status = "succeeded", output = listOf(image),
                        taskData = EasyDiffusionTaskMetadata(listOf("actual/model"))
                    )
                )
            }
            val frames = mutableListOf<EasyDiffusionFrame>()
            source.readStream(task) { frames += it }
            assertNull(frames.first().image)
            assertEquals("original", frames.last().image)
            assertEquals(42L, frames.last().seed)
            assertEquals("actual/model", frames.last().model)
        }

    @Test
    fun `zero or multiple terminal images fail instead of publishing partial success`() = runTest {
        listOf(emptyList(), List(2) { EasyDiffusionImage("image", 42) }).forEach { outputs ->
            coEvery { api.stream(url, 42, any()) } coAnswers {
                thirdArg<suspend (WireFrame) -> Unit>()(
                    WireFrame(
                        status = "succeeded",
                        output = outputs
                    )
                )
            }
            var hasPublished = false
            assertTrue(runCatching { source.readStream(task) { hasPublished = true } }.isFailure)
            assertFalse(hasPublished)
        }
    }

    @Test
    fun `HTTP and protocol failures translate while cancellation and caller failures stay intact`() =
        runTest {
            coEvery { api.stop(url, 42) } throws EasyDiffusionHttpException(409)
            assertEquals(
                409,
                (runCatching { source.stop(task) }.exceptionOrNull() as EasyDiffusionHttpFailure).statusCode
            )
            coEvery { api.stop(url, 42) } throws EasyDiffusionProtocolException(
                IllegalStateException("decode")
            )
            assertTrue(runCatching { source.stop(task) }.exceptionOrNull() is EasyDiffusionProtocolFailure)
            val cancelled = CancellationException("caller")
            coEvery { api.stop(url, 42) } throws cancelled
            assertSame(cancelled, runCatching { source.stop(task) }.exceptionOrNull())
        }
}
