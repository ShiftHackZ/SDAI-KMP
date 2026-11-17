package com.shifthackz.aisdv1.presentation.screen.setup

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.domain.entity.Configuration
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.LocalModelCatalogResult
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.openaioauth.OpenAiOAuthGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DownloadModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.presentation.screen.setup.platform.NoOpServerSetupDownloadGuard
import com.shifthackz.aisdv1.presentation.screen.storageusage.StorageUsageObserver
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import com.shifthackz.aisdv1.domain.usecase.downloadable.RefreshLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConnectToMnnUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.GetConfigurationUseCase
import com.shifthackz.aisdv1.presentation.screen.setup.model.LocalCatalogStatus
import com.shifthackz.aisdv1.presentation.screen.setup.model.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.screen.setup.platform.isServerSourceAvailableOnAndroid
import com.shifthackz.aisdv1.presentation.screen.setup.platform.isServerSourceAvailableOnPlatform
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * Verifies independent catalog recovery in setup without replacing pending fields or persisted run settings.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ServerSetupCatalogRefreshTest {
    private val getCachedLocalModelCatalogUseCase = mockk<GetCachedLocalModelCatalogUseCase>()
    private val downloadModelUseCase = mockk<DownloadModelUseCase>()
    private val deleteModelUseCase = mockk<DeleteModelUseCase>()
    private val refreshLocalModelCatalogUseCase = mockk<RefreshLocalModelCatalogUseCase>()
    private val getConfigurationUseCase = mockk<GetConfigurationUseCase>()
    private val connectToMnnUseCase = mockk<ConnectToMnnUseCase>()
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val storageUsageObserver = mockk<StorageUsageObserver>(relaxed = true)
    private val openAiOAuthGateway = mockk<OpenAiOAuthGateway>()
    private val buildInfoProvider = mockk<BuildInfoProvider> {
        every { type } returns BuildType.FULL
        every { platform } returns Platform.ANDROID
    }
    private var viewModel: ServerSetupViewModel? = null

    @Before
    fun initialize() {
        mockkStatic(::isServerSourceAvailableOnPlatform)
        every { isServerSourceAvailableOnPlatform(any()) } answers {
            isServerSourceAvailableOnAndroid(firstArg(), arrayOf("arm64-v8a"))
        }
        coEvery { getConfigurationUseCase() } returns Configuration(
            source = ServerSource.LOCAL_MNN,
            localMnnModelId = LocalAiModel.CustomMnn.id,
            localMnnModelPath = "/custom/bundle",
        )
        every { openAiOAuthGateway.observeIdentity() } returns flowOf(null)
        coEvery { openAiOAuthGateway.currentIdentity() } returns null
        coEvery { getCachedLocalModelCatalogUseCase(any()) } returns emptyList()
        coEvery { getCachedLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns listOf(
            LocalAiModel.CustomMnn
        )
        coEvery { deleteModelUseCase(any()) } returns Unit
        coEvery { refreshLocalModelCatalogUseCase(any()) } returns LocalModelCatalogResult(emptyList())
        coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns LocalModelCatalogResult(
            listOf(LocalAiModel.CustomMnn)
        )
        coEvery { connectToMnnUseCase(any(), any(), any()) } returns Result.success(Unit)
    }

    @After
    fun dispose() {
        viewModel?.onRouteDisposed()
        unmockkStatic(::isServerSourceAvailableOnPlatform)
    }

    @Test
    fun `given catalog retry, user switches provider during coalesced refresh, expected pending inputs preserved`() =
        runTest {
            // Given
            coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.ONNX) } returns LocalModelCatalogResult(
                emptyList(),
                IllegalStateException("offline")
            )
            val model = createViewModel()
            advanceUntilIdle()
            assertEquals(
                LocalCatalogStatus.ERROR,
                model.state.value.localCatalogStatuses[LocalAiModel.Type.ONNX]
            )
            assertEquals(
                LocalCatalogStatus.READY,
                model.state.value.localCatalogStatuses[LocalAiModel.Type.Mnn]
            )
            val pending = CompletableDeferred<LocalModelCatalogResult>()
            coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } coAnswers { pending.await() }
            // When
            repeat(2) { model.processIntent(ServerSetupIntent.RetryLocalCatalog(ServerSource.LOCAL_MNN)) }
            runCurrent()
            model.processIntent(ServerSetupIntent.UpdateServerMode(ServerSource.LOCAL_MICROSOFT_ONNX))
            model.processIntent(ServerSetupIntent.AllowLocalCustomModel(true))
            model.processIntent(ServerSetupIntent.SelectLocalModelPath("/pending/onnx"))
            pending.complete(LocalModelCatalogResult(listOf(LocalAiModel.CustomMnn)))
            advanceUntilIdle()
            // Then
            coVerify(exactly = 2) { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) }
            assertEquals(ServerSource.LOCAL_MICROSOFT_ONNX, model.state.value.mode)
            assertEquals("/pending/onnx", model.state.value.localOnnxCustomModelPath)
            assertEquals(true, model.state.value.isLocalMnnCustomModel)
            assertEquals(true, model.state.value.localMnnModels.single().selected)
        }

    @Test
    fun `given MNN setup connection, persisted backend is OpenCL, expected current backend retained`() =
        runTest {
            // Given
            val model = createViewModel()
            advanceUntilIdle()
            every { preferenceManager.localMnnBackend } returns MnnBackend.OPENCL
            // When
            model.processIntent(ServerSetupIntent.ConnectToLocalHost)
            advanceUntilIdle()
            // Then
            coVerify(exactly = 1) {
                connectToMnnUseCase(
                    LocalAiModel.CustomMnn.id,
                    "/custom/bundle",
                    MnnBackend.OPENCL
                )
            }
        }

    @Test
    fun `given catalog refresh, setup is disposed before completion, expected no late state publication`() =
        runTest {
            // Given
            val model = createViewModel()
            advanceUntilIdle()
            val pending = CompletableDeferred<LocalModelCatalogResult>()
            coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } coAnswers { pending.await() }
            model.processIntent(ServerSetupIntent.RetryLocalCatalog(ServerSource.LOCAL_MNN))
            runCurrent()
            val previous = model.state.value
            // When
            model.onRouteDisposed()
            pending.complete(LocalModelCatalogResult(emptyList()))
            advanceUntilIdle()
            // Then
            assertEquals(previous, model.state.value)
        }

    @Test
    fun `given setup initialization, remote catalog remains pending, expected cached downloaded choices visible`() =
        runTest {
            // Given
            val downloaded = LocalAiModel.CustomMnn.copy(id = "owned-mnn", downloaded = true)
            coEvery { getCachedLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns listOf(
                LocalAiModel.CustomMnn,
                downloaded
            )
            val pending = CompletableDeferred<LocalModelCatalogResult>()
            coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } coAnswers { pending.await() }
            // When
            val model = createViewModel()
            advanceUntilIdle()
            // Then
            assertEquals(
                true,
                model.state.value.localMnnModels.first { it.id == downloaded.id }.downloaded
            )
            assertEquals(
                LocalCatalogStatus.LOADING,
                model.state.value.localCatalogStatuses[LocalAiModel.Type.Mnn]
            )
        }

    @Test
    fun `given model download, provider and selection change before completion, expected owning list and late cancel preserved`() =
        runTest {
            // Given
            val catalog = LocalAiModel.CustomMnn.copy(
                id = "mnn-download",
                sources = listOf("https://example.com/model.zip")
            )
            coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns LocalModelCatalogResult(
                listOf(LocalAiModel.CustomMnn, catalog)
            )
            val completion = CompletableDeferred<Unit>()
            coEvery { downloadModelUseCase(catalog.id, any()) } returns flow {
                emit(DownloadState.Downloading(50))
                completion.await()
                emit(DownloadState.Complete("/owned/mnn"))
            }
            val model = createViewModel()
            advanceUntilIdle()
            // When
            model.processIntent(ServerSetupIntent.SelectLocalModel(model.state.value.localMnnModels.first { it.id == catalog.id }))
            model.processIntent(
                ServerSetupIntent.LocalModel.DownloadConfirm(
                    catalog.id,
                    catalog.sources.single(),
                    LocalAiModel.Type.Mnn
                )
            )
            runCurrent()
            val oldProgressRow = model.state.value.localMnnModels.first { it.id == catalog.id }
            model.processIntent(ServerSetupIntent.AllowLocalCustomModel(true))
            model.processIntent(ServerSetupIntent.UpdateServerMode(ServerSource.LOCAL_MICROSOFT_ONNX))
            completion.complete(Unit)
            advanceUntilIdle()
            // Then
            val result = model.state.value.localMnnModels.first { it.id == catalog.id }
            assertEquals(true, result.downloaded)
            assertEquals(false, result.selected)
            assertEquals(
                true,
                model.state.value.localMnnModels.first { it.id == LocalAiModel.CustomMnn.id }.selected
            )
            assertEquals(ServerSource.LOCAL_MICROSOFT_ONNX, model.state.value.mode)
            model.processIntent(ServerSetupIntent.UpdateServerMode(ServerSource.LOCAL_MNN))
            model.processIntent(ServerSetupIntent.LocalModel.ClickReduce(oldProgressRow))
            advanceUntilIdle()
            coVerify(exactly = 0) { deleteModelUseCase(any()) }
            assertEquals(
                true,
                model.state.value.localMnnModels.first { it.id == catalog.id }.downloaded
            )
        }

    @Test
    fun `given active model transfer, cancellation starts before provider switches, expected cleanup stays with owning catalog`() =
        runTest {
            // Given
            val catalog = LocalAiModel.CustomMnn.copy(
                id = "mnn-partial",
                sources = listOf("https://example.com/model.zip")
            )
            coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns LocalModelCatalogResult(
                listOf(catalog)
            )
            coEvery { downloadModelUseCase(catalog.id, any()) } returns flow {
                emit(DownloadState.Downloading(50))
                awaitCancellation()
            }
            val model = createViewModel()
            advanceUntilIdle()
            model.processIntent(
                ServerSetupIntent.LocalModel.DownloadConfirm(
                    catalog.id,
                    catalog.sources.single(),
                    LocalAiModel.Type.Mnn
                )
            )
            runCurrent()
            val row = model.state.value.localMnnModels.single()

            // When
            model.processIntent(ServerSetupIntent.LocalModel.ClickReduce(row))
            model.processIntent(ServerSetupIntent.UpdateServerMode(ServerSource.LOCAL_MICROSOFT_ONNX))
            advanceUntilIdle()

            // Then
            coVerify(exactly = 1) { deleteModelUseCase(catalog.id) }
            assertEquals(false, model.state.value.localMnnModels.single().downloaded)
            assertEquals(
                DownloadState.Unknown,
                model.state.value.localMnnModels.single().downloadState
            )
            assertEquals(ServerSource.LOCAL_MICROSOFT_ONNX, model.state.value.mode)
            verify(exactly = 1) { storageUsageObserver.notifyChanged() }
        }

    @Test
    fun `given transfer cancellation, joined producer has already installed model, expected recovered install and storage notification`() =
        runTest {
            // Given
            val catalog = LocalAiModel.CustomMnn.copy(
                id = "mnn-installed",
                sources = listOf("https://example.com/model.zip")
            )
            coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns LocalModelCatalogResult(
                listOf(catalog)
            )
            coEvery { downloadModelUseCase(catalog.id, any()) } returns flow {
                emit(DownloadState.Downloading(99))
                awaitCancellation()
            }
            val model = createViewModel()
            advanceUntilIdle()
            model.processIntent(
                ServerSetupIntent.LocalModel.DownloadConfirm(
                    catalog.id,
                    catalog.sources.single(),
                    LocalAiModel.Type.Mnn
                )
            )
            runCurrent()
            val row = model.state.value.localMnnModels.single()
            coEvery { getCachedLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns listOf(
                catalog.copy(downloaded = true)
            )

            // When
            model.processIntent(ServerSetupIntent.LocalModel.ClickReduce(row))
            advanceUntilIdle()

            // Then
            assertEquals(true, model.state.value.localMnnModels.single().downloaded)
            assertEquals(
                emptySet<String>(),
                model.state.value.localCatalogActiveDownloads[LocalAiModel.Type.Mnn]
            )
            coVerify(exactly = 0) { deleteModelUseCase(catalog.id) }
            verify(exactly = 1) { storageUsageObserver.notifyChanged() }
        }

    /**
     * Binds the production setup reducer to one virtual scheduler and its real lifecycle cancellation.
     *
     * @author Dmitriy Moroz
     */
    private fun TestScope.createViewModel(): ServerSetupViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchersProvider = object : DispatchersProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val ui: CoroutineDispatcher = dispatcher
            override val immediate: CoroutineDispatcher = dispatcher
        }
        return ServerSetupViewModel(
            diagnostics = mockk(relaxed = true),
            launchSource = LaunchSource.SETTINGS,
            dispatchersProvider = dispatchersProvider,
            buildInfoProvider = buildInfoProvider,
            getCachedLocalModelCatalogUseCase = getCachedLocalModelCatalogUseCase,
            refreshLocalModelCatalogUseCase = refreshLocalModelCatalogUseCase,
            getConfigurationUseCase = getConfigurationUseCase,
            connectToMnnUseCase = connectToMnnUseCase,
            fetchHuggingFaceModelsUseCase = mockk { coEvery { this@mockk.invoke() } returns emptyList() },
            urlValidator = mockk(relaxed = true),
            stringValidator = mockk(relaxed = true),
            filePathValidator = mockk(relaxed = true),
            connectToA1111UseCase = mockk(relaxed = true),
            connectToSwarmUiUseCase = mockk(relaxed = true),
            connectToHordeUseCase = mockk(relaxed = true),
            connectToHuggingFaceUseCase = mockk(relaxed = true),
            connectToLocalDiffusionUseCase = mockk(relaxed = true),
            connectToMediaPipeUseCase = mockk(relaxed = true),
            connectToSdxlUseCase = mockk(relaxed = true),
            connectToCoreMlUseCase = mockk(relaxed = true),
            connectToBonsaiUseCase = mockk(relaxed = true),
            connectToOpenAiCompatibleUseCase = mockk(relaxed = true),
            connectToEasyDiffusionUseCase = mockk(relaxed = true),
            connectToOpenAiUseCase = mockk(relaxed = true),
            connectToStabilityAiUseCase = mockk(relaxed = true),
            connectToFalAiUseCase = mockk(relaxed = true),
            connectToArliAiUseCase = mockk(relaxed = true),
            downloadModelUseCase = downloadModelUseCase,
            deleteModelUseCase = deleteModelUseCase,
            downloadGuard = NoOpServerSetupDownloadGuard,
            storageUsageObserver = storageUsageObserver,
            linksProvider = mockk(relaxed = true),
            preferenceManager = preferenceManager,
            router = mockk(relaxed = true),
            openAiOAuthGateway = openAiOAuthGateway,
            setServerConfigurationUseCase = mockk(relaxed = true),
            retryAuthorizationStorageUseCase = mockk(relaxed = true),
            resetAuthorizationCredentialsUseCase = mockk(relaxed = true),
        ).also { viewModel = it }
    }
}
