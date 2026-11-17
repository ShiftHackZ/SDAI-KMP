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
    fun `retry is source owned coalesced and cannot replace another provider inputs`() = runTest {
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
        repeat(2) { model.processIntent(ServerSetupIntent.RetryLocalCatalog(ServerSource.LOCAL_MNN)) }
        runCurrent()
        model.processIntent(ServerSetupIntent.UpdateServerMode(ServerSource.LOCAL_MICROSOFT_ONNX))
        model.processIntent(ServerSetupIntent.AllowLocalCustomModel(true))
        model.processIntent(ServerSetupIntent.SelectLocalModelPath("/pending/onnx"))
        pending.complete(LocalModelCatalogResult(listOf(LocalAiModel.CustomMnn)))
        advanceUntilIdle()
        coVerify(exactly = 2) { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) }
        assertEquals(ServerSource.LOCAL_MICROSOFT_ONNX, model.state.value.mode)
        assertEquals("/pending/onnx", model.state.value.localOnnxCustomModelPath)
        assertEquals(true, model.state.value.isLocalMnnCustomModel)
        assertEquals(true, model.state.value.localMnnModels.single().selected)
    }

    @Test
    fun `disposed setup cancels pending catalog and reconnect retains current backend`() = runTest {
        val model = createViewModel()
        advanceUntilIdle()
        every { preferenceManager.localMnnBackend } returns MnnBackend.OPENCL
        model.processIntent(ServerSetupIntent.ConnectToLocalHost)
        advanceUntilIdle()
        coVerify(exactly = 1) {
            connectToMnnUseCase(
                LocalAiModel.CustomMnn.id,
                "/custom/bundle",
                MnnBackend.OPENCL
            )
        }
        val pending = CompletableDeferred<LocalModelCatalogResult>()
        coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } coAnswers { pending.await() }
        model.processIntent(ServerSetupIntent.RetryLocalCatalog(ServerSource.LOCAL_MNN))
        runCurrent()
        val previous = model.state.value
        model.onRouteDisposed()
        pending.complete(LocalModelCatalogResult(emptyList()))
        advanceUntilIdle()
        assertEquals(previous, model.state.value)
    }

    @Test
    fun `downloaded cache is visible while remote catalog remains pending`() = runTest {
        val downloaded = LocalAiModel.CustomMnn.copy(id = "owned-mnn", downloaded = true)
        coEvery { getCachedLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } returns listOf(
            LocalAiModel.CustomMnn,
            downloaded
        )
        val pending = CompletableDeferred<LocalModelCatalogResult>()
        coEvery { refreshLocalModelCatalogUseCase(LocalAiModel.Type.Mnn) } coAnswers { pending.await() }
        val model = createViewModel()
        advanceUntilIdle()
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
    fun `download completion stays with its provider and respects later selection and late Cancel`() =
        runTest {
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
            storageUsageObserver = mockk(relaxed = true),
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
