package com.shifthackz.aisdv1.data.repository

import android.os.Build
import com.shifthackz.aisdv1.data.local.MnnModelFileStore
import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.entity.mnn.MnnBundleManifest
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers

/**
 * Verifies host admission, immutable selection and gallery ownership around the isolated gateway.
 *
 * @author Dmitriy Moroz
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class MnnGenerationRepositoryImplTest {
    private val media = mockk<MediaStoreGateway>()
    private val results = mockk<GenerationResultDataSource.Local>()
    private val background = mockk<BackgroundWorkObserver>()
    private val preferences = mockk<PreferenceManager>()
    private val gateway = mockk<NativeGenerationGateway>()
    private val models = mockk<DownloadableModelDataSource.Local>()
    private val files = mockk<MnnModelFileStore>()
    private val validator = mockk<MnnBundleValidator>()
    private val payload = TextToImagePayload(
        prompt = "cat 😀",
        negativePrompt = "",
        samplingSteps = 20,
        cfgScale = 7.5f,
        width = 512,
        height = 512,
        restoreFaces = false,
        seed = "42",
        subSeed = "",
        subSeedStrength = 0f,
        sampler = "PNDM",
        nsfw = false,
        batchCount = 1,
        style = null,
        quality = null,
        openAiModel = null,
        stabilityAiClipGuidance = null,
        stabilityAiStylePreset = null,
        sourceSnapshot = ServerSource.LOCAL_MNN,
        mnnModelId = "mnn_sd15",
        mnnActivationId = "a".repeat(64),
        mnnProfile = "sd15-512-general-int8-v1"
    )
    private val root = File("/owned/mnn_sd15")
    private val model = LocalAiModel(
        "mnn_sd15",
        LocalAiModel.Type.Mnn,
        "SD 1.5",
        "1 GB",
        emptyList(),
        downloaded = true
    )
    private val manifest =
        MnnBundleManifest("sd15-512-general-int8-v1", emptyList(), "a".repeat(64))
    private var isPayloadReleased = false
    private var isSelectionReleased = false
    private val repository = MnnGenerationRepositoryImpl(
        media,
        results,
        background,
        preferences,
        gateway,
        models,
        files,
        validator
    )

    @Before
    fun initialize() {
        ReflectionHelpers.setStaticField(
            Build::class.java,
            "SUPPORTED_64_BIT_ABIS",
            arrayOf("arm64-v8a")
        )
        every { preferences.localMnnModelId } returns model.id
        every { preferences.localMnnActivationId } returns manifest.activationId
        every { preferences.autoSaveAiResults } returns false
        every { preferences.saveToMediaStore } returns false
        every { background.hasActiveTasks() } returns false
        coEvery { models.getById(any()) } returns model
        every { files.directory(any(), any()) } returns root
        every { files.acquire(any(), any()) } answers { AutoCloseable { isPayloadReleased = true } }
        every {
            files.acquireSelection(
                any(),
                any()
            )
        } answers { AutoCloseable { isSelectionReleased = true } }
        coEvery { validator(root.path) } returns manifest
        coEvery { gateway.generate(any()) } returns "aW1hZ2U="
    }

    @Test
    fun `given MNN repository, validated request completes, expected leased gateway and original output metadata`() =
        runTest {
            // Given
            coEvery { gateway.generate(any()) } coAnswers {
                assertFalse(isPayloadReleased)
                assertEquals(
                    NativeGenerationRequest(
                        NativeGenerationEngine.MNN,
                        payload,
                        root.path
                    ), firstArg()
                )
                "aW1hZ2U="
            }

            // When
            val result = repository.generateFromText(payload)

            // Then
            assertTrue(isPayloadReleased)
            assertEquals("aW1hZ2U=", result.image)
            assertEquals(payload.prompt, result.prompt)
            assertEquals(payload.seed, result.seed)
            assertEquals(payload.width, result.width)
            assertEquals(model.name, result.modelName)
            coVerify(exactly = 0) { results.insert(any()) }
        }

    @Test
    fun `given MNN repository, background owns result persistence, expected completed output inserted before lease release`() =
        runTest {
            // Given
            every { background.hasActiveTasks() } returns true
            coEvery { results.insert(any()) } coAnswers { assertFalse(isPayloadReleased); 73L }

            // When
            val result = repository.generateFromText(payload)

            // Then
            assertEquals(73L, result.id)
            assertTrue(isPayloadReleased)
            coVerify(exactly = 1) { results.insert(match { it.image == "aW1hZ2U=" }) }
        }

    @Test
    fun `given MNN repository, gateway fails, expected no gallery publication and released host lease`() =
        runTest {
            // Given
            val failure = IllegalStateException("child failed")
            coEvery { gateway.generate(any()) } throws failure

            // When
            val actual = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            assertTrue(isPayloadReleased)
            coVerify(exactly = 0) { results.insert(any()) }
        }

    @Test
    fun `given MNN repository, gateway is cancelled, expected same cancellation and released host lease`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancel")
            coEvery { gateway.generate(any()) } throws cancellation

            // When
            val actual = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertSame(cancellation, actual)
            assertTrue(isPayloadReleased)
            coVerify(exactly = 0) { results.insert(any()) }
        }

    @Test
    fun `given MNN repository, queued model differs from selection, expected rejection before gateway and result publication`() =
        runTest {
            // Given
            every { preferences.localMnnModelId } returns "mnn_other"

            // When
            val failure = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            coVerify(exactly = 0) { gateway.generate(any()); results.insert(any()) }
            verify(exactly = 0) { files.acquire(any(), any()) }
        }

    @Test
    fun `given MNN repository, queued activation differs from selection, expected rejection before gateway and result publication`() =
        runTest {
            // Given
            every { preferences.localMnnActivationId } returns "b".repeat(64)

            // When
            val failure = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            coVerify(exactly = 0) { gateway.generate(any()); results.insert(any()) }
            verify(exactly = 0) { files.acquire(any(), any()) }
        }

    @Test
    fun `given MNN repository, catalog model is not downloaded, expected rejection before gateway and result publication`() =
        runTest {
            // Given
            coEvery { models.getById(any()) } returns model.copy(downloaded = false)

            // When
            val failure = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            coVerify(exactly = 0) { gateway.generate(any()); results.insert(any()) }
            verify(exactly = 0) { files.acquire(any(), any()) }
        }

    @Test
    fun `given MNN repository, catalog type belongs to another runtime, expected rejection before gateway and result publication`() =
        runTest {
            // Given
            coEvery { models.getById(any()) } returns model.copy(type = LocalAiModel.Type.ONNX)

            // When
            val failure = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            coVerify(exactly = 0) { gateway.generate(any()); results.insert(any()) }
            verify(exactly = 0) { files.acquire(any(), any()) }
        }

    @Test
    fun `given MNN repository, platform lacks arm64, expected rejection before gateway and result publication`() =
        runTest {
            // Given
            ReflectionHelpers.setStaticField(
                Build::class.java,
                "SUPPORTED_64_BIT_ABIS",
                arrayOf("x86_64")
            )

            // When
            val failure = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            coVerify(exactly = 0) { gateway.generate(any()); results.insert(any()) }
            verify(exactly = 0) { files.acquire(any(), any()) }
        }

    @Test
    fun `given MNN repository, selection changes while hashing, expected no native call and released lease`() =
        runTest {
            // Given
            coEvery { validator(root.path) } coAnswers {
                every { preferences.localMnnActivationId } returns "b".repeat(64)
                manifest
            }

            // When
            val failure = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            assertTrue(isPayloadReleased)
            coVerify(exactly = 0) { gateway.generate(any()); results.insert(any()) }
        }

    @Test
    fun `given MNN repository, bundle manifest differs from queued identity, expected no native call and released lease`() =
        runTest {
            // Given
            coEvery { validator(root.path) } returns manifest.copy(activationId = "b".repeat(64))

            // When
            val failure = runCatching { repository.generateFromText(payload) }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalStateException)
            assertTrue(isPayloadReleased)
            coVerify(exactly = 0) { gateway.generate(any()) }
        }

    @Test
    fun `given MNN model validation, catalog snapshot succeeds, expected both owners retained until close`() =
        runTest {
            // Given
            val id = model.id

            // When
            val lease = repository.validateModel(id, "/ignored")

            // Then
            assertEquals(manifest.activationId, lease.activationId)
            assertFalse(isPayloadReleased)
            assertFalse(isSelectionReleased)
            coVerify(exactly = 0) { files.importCustom(any()) }
            lease.close()
            assertTrue(isPayloadReleased)
            assertTrue(isSelectionReleased)
        }

    @Test
    fun `given MNN model validation, custom source is prepared, expected captured owned identity validated`() =
        runTest {
            // Given
            coEvery { files.importCustom("/Downloads/custom") } returns manifest.activationId
            coEvery { models.getById(LocalAiModel.CustomMnn.id) } returns LocalAiModel.CustomMnn

            // When
            val lease = repository.validateModel(LocalAiModel.CustomMnn.id, "/Downloads/custom")

            // Then
            verify(exactly = 1) {
                files.acquireSelection(
                    LocalAiModel.CustomMnn.id,
                    manifest.activationId
                ); files.acquire(LocalAiModel.CustomMnn.id, manifest.activationId)
            }
            assertEquals(manifest.activationId, lease.activationId)
            lease.close()
        }

    @Test
    fun `given MNN model validation, integrity check fails, expected both owners released and exact failure`() =
        runTest {
            // Given
            val failure = IllegalArgumentException("corrupt")
            coEvery { validator(any()) } throws failure

            // When
            val actual =
                runCatching { repository.validateOwnedModel(model.id, "") }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            assertTrue(isPayloadReleased)
            assertTrue(isSelectionReleased)
        }

    @Test
    fun `given MNN model validation, payload lock cannot be acquired, expected selection owner released`() =
        runTest {
            // Given
            val failure = IllegalStateException("busy")
            every { files.acquire(any(), any()) } throws failure

            // When
            val actual =
                runCatching { repository.validateOwnedModel(model.id, "") }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
            assertTrue(isSelectionReleased)
            assertFalse(isPayloadReleased)
        }

    @Test
    fun `given MNN model validation, cancellation and payload cleanup both fail, expected cancellation retained with suppressed cleanup`() =
        runTest {
            // Given
            val cancellation = CancellationException("cancelled")
            val cleanup = IllegalStateException("cannot release payload")
            coEvery { validator(any()) } throws cancellation
            every { files.acquire(any(), any()) } returns AutoCloseable { throw cleanup }

            // When
            val failure =
                runCatching { repository.validateOwnedModel(model.id, "") }.exceptionOrNull()

            // Then
            assertSame(cancellation, failure)
            assertEquals(listOf(cleanup), cancellation.suppressed.toList())
            assertTrue(isSelectionReleased)
        }

    @Test
    fun `given MNN model validation, custom identity is malformed, expected no resource acquisition`() =
        runTest {
            // Given
            coEvery { models.getById(LocalAiModel.CustomMnn.id) } returns LocalAiModel.CustomMnn

            // When
            val failure = runCatching {
                repository.validateOwnedModel(
                    LocalAiModel.CustomMnn.id,
                    "invalid"
                )
            }.exceptionOrNull()

            // Then
            assertTrue(failure is IllegalArgumentException)
            verify(exactly = 0) {
                files.acquireSelection(any(), any()); files.acquire(
                any(),
                any()
            )
            }
        }

    @Test
    fun `given MNN repository, progress is observed, expected only MNN gateway stream`() {
        // Given
        val status = emptyFlow<LocalDiffusionStatus>()
        every { gateway.observeStatus(NativeGenerationEngine.MNN) } returns status

        // When
        val actual = repository.observeStatus()

        // Then
        assertSame(status, actual)
    }

    @Test
    fun `given MNN repository, interrupt is requested, expected gateway cancellation independent of preferences`() =
        runTest {
            // Given
            coEvery { gateway.cancelActive(null) } returns true

            // When
            repository.interruptGeneration()

            // Then
            coVerify(exactly = 1) { gateway.cancelActive(null) }
        }
}
