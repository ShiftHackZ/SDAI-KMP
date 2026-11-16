package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.export.MediaStoreImageFixture
import com.shifthackz.aisdv1.domain.datasource.EasyDiffusionRemoteDataSource
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertSame
import org.junit.Assert.assertArrayEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.util.Base64

/**
 * Uses actual image decoding, ownership guards and gallery insertion policy with isolated storage.
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class EasyDiffusionRepositoryImplTest {
    private val remote = mockk<EasyDiffusionRemoteDataSource>()
    private val preferences = mockk<PreferenceManager>(relaxed = true)
    private val local = mockk<GenerationResultDataSource.Local>()
    private val media = mockk<MediaStoreGateway>(relaxed = true)
    private val background = mockk<BackgroundWorkObserver>()
    private val registry = InMemoryEasyDiffusionTaskRegistry()
    private val repository = EasyDiffusionRepositoryImpl(
        remote,
        preferences,
        registry,
        ProviderSelectionGuard(),
        media,
        local,
        background
    )
    private val url = "https://server.example"
    private val request = EasyDiffusionRequest(
        url, "session", "folder/model", "lake", "blur",
        512, 512, 20, 7f, 42, 1, selectionRevision = 7
    )
    private val image = MediaStoreImageFixture.PNG.bytes()
    private val terminal = EasyDiffusionFrame(
        status = "succeeded",
        image = "data:image/png;base64," + Base64.getEncoder().encodeToString(image),
        seed = 42,
        model = "actual/model"
    )

    init {
        every { preferences.source } returns ServerSource.EASY_DIFFUSION
        every { preferences.providerSelectionRevision } returns 7
        every { preferences.easyDiffusionUrl } returns url
        every { preferences.autoSaveAiResults } returns true
        every { preferences.saveToMediaStore } returns false
        coEvery { background.hasActiveTasks() } returns false
        coEvery { local.insert(any()) } returns 99
    }

    @Test
    fun `successful output preserves encoded bytes metadata actual dimensions and model`() =
        runTest {
            val owner = Job()
            registry.begin("session", owner, null)
            val result = repository.save(request, terminal)
            assertEquals(99L, result.id)
            assertArrayEquals(image, Base64.getDecoder().decode(result.image))
            assertEquals(1, result.width)
            assertEquals(1, result.height)
            assertEquals("actual/model", result.modelName)
            assertEquals("42", result.seed)
            assertEquals("ddim", result.sampler)
            assertEquals(AiGenerationResult.Type.TEXT_TO_IMAGE, result.type)
            assertEquals("lake", result.prompt)
            coVerify(exactly = 1) { local.insert(match { it.image == result.image }) }
            registry.finish("session")
            owner.complete()
        }

    @Test
    fun `img2img retains input and supports background persistence when autosave is off`() =
        runTest {
            every { preferences.autoSaveAiResults } returns false
            coEvery { background.hasActiveTasks() } returns true
            val owner = Job()
            registry.begin("session", owner, "worker")
            val result = repository.save(
                request.copy(inputImage = "source", strength = 0.5f),
                terminal.copy(model = null)
            )
            assertEquals(AiGenerationResult.Type.IMAGE_TO_IMAGE, result.type)
            assertEquals("source", result.inputImage)
            assertEquals(0.5f, result.denoisingStrength)
            assertEquals(request.model, result.modelName)
            assertEquals(99L, result.id)
            registry.finish("session")
            owner.complete()
        }

    @Test
    fun `stale revision and cancelled owner prevent gallery insertion`() = runTest {
        val owner = Job()
        registry.begin("session", owner, "worker")
        every { preferences.providerSelectionRevision } returns 8
        assertTrue(runCatching { repository.save(request, terminal) }.isFailure)
        every { preferences.providerSelectionRevision } returns 7
        registry.cancel("worker")
        assertTrue(runCatching { repository.save(request, terminal) }.isFailure)
        coVerify(exactly = 0) { local.insert(any()) }
        registry.finish("session")
    }

    @Test
    fun `failed status invalid seed and invalid image cannot enter gallery`() = runTest {
        val owner = Job()
        registry.begin("session", owner, null)
        listOf(
            terminal.copy(status = "failed"), terminal.copy(seed = null),
            terminal.copy(seed = 4294967296L), terminal.copy(image = null),
            terminal.copy(image = "bm90IGFuIGltYWdl")
        ).forEach { frame ->
            assertTrue(runCatching { repository.save(request, frame) }.isFailure)
        }
        coVerify(exactly = 0) { local.insert(any()) }
        registry.finish("session")
        owner.complete()
    }

    @Test
    fun `endpoint caches remain independent and failed refresh propagates`() = runTest {
        val first = listOf(EasyDiffusionModel("folder/first", "First"))
        val second = listOf(EasyDiffusionModel("folder/second", "Second"))
        coEvery { remote.models(url) } returns first
        coEvery { remote.models("https://other.example") } returns second
        repository.fetchModels(url)
        repository.fetchModels("https://other.example")
        val failure = IllegalStateException("offline")
        coEvery { remote.models(url) } throws failure
        assertSame(failure, runCatching { repository.fetchModels(url) }.exceptionOrNull())
        assertEquals(first, repository.getModels(url))
        assertEquals(second, repository.getModels("https://other.example"))
        assertEquals(
            emptyList<EasyDiffusionModel>(),
            repository.getModels("https://missing.example")
        )
    }

    @Test
    fun `storage failure is not converted to successful result`() = runTest {
        val owner = Job()
        registry.begin("session", owner, null)
        val failure = IllegalStateException("disk full")
        coEvery { local.insert(any()) } throws failure
        assertSame(failure, runCatching { repository.save(request, terminal) }.exceptionOrNull())
        registry.finish("session")
        owner.complete()
    }
}
