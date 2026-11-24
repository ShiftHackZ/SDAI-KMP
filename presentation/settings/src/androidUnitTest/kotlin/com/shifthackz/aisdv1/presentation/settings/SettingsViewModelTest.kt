package com.shifthackz.aisdv1.presentation.settings

import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.appbuild.BuildVersion
import com.shifthackz.aisdv1.core.common.links.LinksProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.NetworkUsage
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.Settings
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.caching.ClearAppCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.GetStableDiffusionModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.sdmodel.SelectStableDiffusionModelUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ObserveNetworkUsageUseCase
import com.shifthackz.aisdv1.domain.usecase.stabilityai.ObserveStabilityAiCreditsUseCase
import com.shifthackz.aisdv1.presentation.settings.mocks.mockStableDiffusionModels
import com.shifthackz.aisdv1.core.navigation.LaunchSource
import com.shifthackz.aisdv1.core.navigation.router.SettingsRouter
import com.shifthackz.aisdv1.presentation.settings.platform.SettingsPlatformActions
import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserver
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.mnn.MnnCatalogResult
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMnnModelsUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import com.shifthackz.aisdv1.domain.entity.isServerSourceAvailableOnAndroid
import com.shifthackz.aisdv1.domain.entity.isServerSourceAvailableOnPlatform
import io.mockk.verify
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Verifies Settings state aggregation, including observed storage/network usage summaries.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val settings = MutableStateFlow(Settings(sdModel = "title_5598"))
    private val stabilityCredits = MutableStateFlow(5598f)
    private val networkUsage = MutableStateFlow(NetworkUsage())
    private val getStableDiffusionModelsUseCase = mockk<GetStableDiffusionModelsUseCase>()
    private val observeStabilityAiCreditsUseCase = mockk<ObserveStabilityAiCreditsUseCase>()
    private val selectStableDiffusionModelUseCase = mockk<SelectStableDiffusionModelUseCase>()
    private val clearAppCacheUseCase = mockk<ClearAppCacheUseCase>()
    private val galleryReads = mockk<GalleryReadGateway>()
    private val getAllGalleryUseCase = mockk<GetAllGalleryUseCase>()
    private val getLocalOnnxModelsUseCase = mockk<GetLocalOnnxModelsUseCase>()
    private val getLocalMediaPipeModelsUseCase = mockk<GetLocalMediaPipeModelsUseCase>()
    private val getLocalSdxlModelsUseCase = mockk<GetLocalSdxlModelsUseCase>()
    private val getLocalCoreMlModelsUseCase = mockk<GetLocalCoreMlModelsUseCase>()
    private val getLocalBonsaiModelsUseCase = mockk<GetLocalBonsaiModelsUseCase>()
    private val getLocalMnnModelsUseCase = mockk<GetLocalMnnModelsUseCase>()
    private val getCachedLocalModelCatalogUseCase = mockk<GetCachedLocalModelCatalogUseCase>()
    private val observeNetworkUsageUseCase = mockk<ObserveNetworkUsageUseCase>()
    private val preferenceManager = mockk<PreferenceManager>(relaxed = true)
    private val router = TestSettingsRouter()
    private val platformActions = TestSettingsPlatformActions()
    private val linksProvider = TestLinksProvider()
    private lateinit var storageUsageObserver: StorageUsageObserver
    private lateinit var dispatchersProvider: DispatchersProvider
    private var viewModelScope: CoroutineScope? = null

    @Before
    fun initialize() {
        mockkStatic(::isServerSourceAvailableOnPlatform)
        every { isServerSourceAvailableOnPlatform(any()) } answers {
            isServerSourceAvailableOnAndroid(firstArg(), arrayOf("arm64-v8a"))
        }
        storageUsageObserver = FakeStorageUsageObserver()
        coEvery { getStableDiffusionModelsUseCase() } returns mockStableDiffusionModels
        coEvery { clearAppCacheUseCase() } returns Unit
        coEvery { getAllGalleryUseCase() } returns emptyList()
        coEvery { galleryReads.storageBytes() } coAnswers { getAllGalleryUseCase().sumOf { it.image.encodeToByteArray().size.toLong() + it.inputImage.encodeToByteArray().size } }
        coEvery { getLocalOnnxModelsUseCase() } returns emptyList()
        coEvery { getLocalMediaPipeModelsUseCase() } returns emptyList()
        coEvery { getLocalSdxlModelsUseCase() } returns emptyList()
        coEvery { getLocalCoreMlModelsUseCase() } returns emptyList()
        coEvery { getLocalBonsaiModelsUseCase() } returns emptyList()
        coEvery { getCachedLocalModelCatalogUseCase(any()) } returns emptyList()
        coEvery { getLocalMnnModelsUseCase() } returns MnnCatalogResult(emptyList())
        every { observeStabilityAiCreditsUseCase() } returns stabilityCredits
        every { observeNetworkUsageUseCase() } returns networkUsage
        every { preferenceManager.observe() } returns settings
        every { preferenceManager.developerMode } returns false
    }

    @After
    fun tearDown() {
        unmockkStatic(::isServerSourceAvailableOnPlatform)
        viewModelScope?.cancel()
        viewModelScope = null
    }

    @Test
    fun `given settings, initialized, expected UI state updated with settings and models`() =
        runTest {
            // Given
            // initialize() supplies title_5598 model selection and test-version build metadata through the
            // existing preference and model stubs.

            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            val actual = viewModel.state.value
            // Then
            assertEquals(false, actual.loading)
            assertEquals("title_5598", actual.sdModelSelected)
            assertEquals(0f, actual.stabilityAiCredits)
            assertEquals("test-version", actual.appVersion)

        }

    @Test
    fun `given settings, provider changed to stability ai, expected credits updated`() = runTest {
        // Given

        val viewModel = createViewModel()
        advanceUntilIdle()

        // When
        settings.value = settings.value.copy(source = ServerSource.STABILITY_AI)
        advanceUntilIdle()

        // Then
        assertEquals(5598f, viewModel.state.value.stabilityAiCredits)

    }

    @Test
    fun `given settings, network usage changes, expected settings summary bytes update`() =
        runTest {
            // Given

            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            networkUsage.value = NetworkUsage(
                modelDownloadBytes = 1024L,
                configBytes = 512L,
                inferenceBytes = 2048L,
            )
            advanceUntilIdle()

            // Then
            assertEquals(3584L, viewModel.state.value.networkUsageBytes)

        }

    @Test
    fun `given settings, storage observer notified, expected settings summary bytes update`() =
        runTest {
            // Given

            platformActions.appCacheBytes = 3_221_225_472L
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(3_221_225_472L, viewModel.state.value.storageUsageBytes)

            platformActions.appCacheBytes = 0L
            storageUsageObserver.notifyChanged()
            advanceUntilIdle()

            assertEquals(0L, viewModel.state.value.storageUsageBytes)

        }

    @Test
    fun `given settings, platform hides filesystem residue, expected settings storage summary is empty`() =
        runTest {
            // Given

            platformActions.visibleStorageThresholdBytes = 1024L
            platformActions.appCacheBytes = 64L
            // When
            val viewModel = createViewModel()
            advanceUntilIdle()

            // Then
            assertEquals(0L, viewModel.state.value.storageUsageBytes)
            assertEquals(64L, platformActions.appCacheBytes)

        }

    @Test
    fun `given Settings storage summary, MNN catalog and custom snapshots exist, expected only catalog bytes`() =
        runTest {
            // Given
            val catalog = LocalAiModel.CustomMnn.copy(id = "mnn-catalog", downloaded = true)
            coEvery { getLocalMnnModelsUseCase() } returns MnnCatalogResult(
                listOf(catalog, LocalAiModel.CustomMnn.copy(downloaded = true)),
            )
            platformActions.modelBytes.putAll(
                mapOf(
                    catalog.id to 4096L,
                    LocalAiModel.CustomMnn.id to 8192L,
                    "mnn_custom" to 16384L
                ),
            )
            // When
            val viewModel = createViewModel(object : BuildInfoProvider by TestBuildInfoProvider {
                override val type = BuildType.FULL
            })
            advanceUntilIdle()

            // Then
            assertEquals(4096L, viewModel.state.value.storageUsageBytes)
        }

    @Test
    fun `given Settings storage summary, Core ML custom and unknown directories exist, expected only catalog bytes`() =
        runTest {
            // Given
            val catalog = LocalAiModel.CustomCoreMl.copy(id = "coreml-catalog", downloaded = true)
            coEvery { getLocalCoreMlModelsUseCase() } returns listOf(
                catalog, LocalAiModel.CustomCoreMl.copy(downloaded = true),
            )
            platformActions.modelBytes.putAll(
                mapOf(
                    catalog.id to 4096L,
                    LocalAiModel.CustomCoreMl.id to 8192L,
                    "unknown-custom" to 16384L
                ),
            )
            // When
            val viewModel = createViewModel(object : BuildInfoProvider by TestBuildInfoProvider {
                override val type = BuildType.FULL
                override val platform = Platform.IOS
            })
            advanceUntilIdle()

            // Then
            assertEquals(4096L, viewModel.state.value.storageUsageBytes)
        }

    @Test
    fun `given settings, launch url intents, expected platform opens configured urls`() = runTest {
        // Given

        val viewModel = createViewModel()

        // When
        viewModel.processIntent(SettingsIntent.LaunchUrl.OpenPolicy)
        viewModel.processIntent(SettingsIntent.LaunchUrl.OpenSourceCode)

        // Then
        assertEquals(
            listOf("https://policy.test", "https://source.test"),
            platformActions.openedUrls
        )

    }

    @Test
    fun `given settings, background generation enabled and permission granted, expected preference updated`() =
        runTest {
            // Given

            val viewModel = createViewModel()

            // When
            viewModel.processIntent(SettingsIntent.UpdateFlag.BackgroundGeneration(true))
            advanceUntilIdle()

            // Then
            verify { preferenceManager.backgroundGeneration = true }

        }

    @Test
    fun `given settings, denied storage permission, expected manual permission modal`() = runTest {
        // Given

        platformActions.requiresStoragePermissionForMediaStoreOverride = true
        platformActions.storagePermissionGranted = false
        // When
        val viewModel = createViewModel()

        viewModel.processIntent(SettingsIntent.UpdateFlag.SaveToMediaStore(true))
        advanceUntilIdle()

        // Then
        assertTrue(viewModel.state.value.screenModal is SettingsModal.ManualPermission)

    }

    @Test
    fun `given settings, navigation intent, expected router opens server setup from settings`() =
        runTest {
            // Given

            val viewModel = createViewModel()

            // When
            viewModel.processIntent(SettingsIntent.NavigateConfiguration)

            // Then
            assertEquals(LaunchSource.SETTINGS, router.serverSetupSource)

        }

    @Test
    fun `given settings, usage navigation intents, expected router opens standalone usage routes`() =
        runTest {
            // Given

            val viewModel = createViewModel()

            // When
            viewModel.processIntent(SettingsIntent.NavigateStorageUsage)
            viewModel.processIntent(SettingsIntent.NavigateNetworkUsage)

            // Then
            assertEquals(true, router.storageUsageOpened)
            assertEquals(true, router.networkUsageOpened)

        }

    @Test
    fun `given settings, language selected, expected preference updated and modal closed`() =
        runTest {
            // Given

            val viewModel = createViewModel()

            viewModel.processIntent(SettingsIntent.Action.PickLanguage)
            // When
            viewModel.processIntent(SettingsIntent.Action.SetLanguage("uk"))

            // Then
            verify { preferenceManager.languageCode = "uk" }
            assertEquals(SettingsModal.None, viewModel.state.value.screenModal)

        }

    @Test
    fun `given performance overlay settings, preference snapshots change, expected switch follows saved choice`() =
        runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()
            assertTrue(viewModel.state.value.shouldShowPerformanceOverlay)

            // When
            settings.value = settings.value.copy(shouldShowPerformanceOverlay = false)
            advanceUntilIdle()

            // Then
            assertEquals(false, viewModel.state.value.shouldShowPerformanceOverlay)
        }

    @Test
    fun `given performance overlay settings, switch is disabled then enabled, expected exact choices persisted`() =
        runTest {
            // Given
            val viewModel = createViewModel()
            advanceUntilIdle()

            // When
            viewModel.processIntent(SettingsIntent.UpdateFlag.PerformanceOverlay(false))
            viewModel.processIntent(SettingsIntent.UpdateFlag.PerformanceOverlay(true))
            advanceUntilIdle()

            // Then
            verify(exactly = 1) { preferenceManager.shouldShowPerformanceOverlay = false }
            verify(exactly = 1) { preferenceManager.shouldShowPerformanceOverlay = true }
        }

    @Test
    fun `given settings view model on the information section, Blog is selected, expected blog navigation`() =
        runTest {
            // Given
            val viewModel = createViewModel()

            // When
            viewModel.processIntent(SettingsIntent.NavigateBlog)
            advanceUntilIdle()

            // Then
            assertTrue(router.isBlogOpened)
        }

    private fun TestScope.createViewModel(
        buildInfoProvider: BuildInfoProvider = TestBuildInfoProvider,
    ): SettingsViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        dispatchersProvider = object : DispatchersProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val ui: CoroutineDispatcher = dispatcher
            override val immediate: CoroutineDispatcher = dispatcher
        }
        viewModelScope = CoroutineScope(SupervisorJob() + dispatcher)
        return SettingsViewModel(
            galleryReads = galleryReads,
            diagnostics = mockk(relaxed = true),
            dispatchersProvider = dispatchersProvider,
            getStableDiffusionModelsUseCase = getStableDiffusionModelsUseCase,
            observeStabilityAiCreditsUseCase = observeStabilityAiCreditsUseCase,
            selectStableDiffusionModelUseCase = selectStableDiffusionModelUseCase,
            clearAppCacheUseCase = clearAppCacheUseCase,
            getLocalOnnxModelsUseCase = getLocalOnnxModelsUseCase,
            getLocalMediaPipeModelsUseCase = getLocalMediaPipeModelsUseCase,
            getLocalSdxlModelsUseCase = getLocalSdxlModelsUseCase,
            getLocalCoreMlModelsUseCase = getLocalCoreMlModelsUseCase,
            getLocalBonsaiModelsUseCase = getLocalBonsaiModelsUseCase,
            getLocalMnnModelsUseCase = getLocalMnnModelsUseCase,
            getCachedLocalModelCatalogUseCase = getCachedLocalModelCatalogUseCase,
            observeNetworkUsageUseCase = observeNetworkUsageUseCase,
            storageUsageObserver = storageUsageObserver,
            preferenceManager = preferenceManager,
            debugMenuAccessor = DebugMenuAccessorImpl(preferenceManager),
            buildInfoProvider = buildInfoProvider,
            linksProvider = linksProvider,
            router = router,
            platformActions = platformActions,
        )
    }
}

private object TestBuildInfoProvider : BuildInfoProvider {
    override val isDebug: Boolean = true
    override val buildNumber: Int = 5598
    override val version: BuildVersion = BuildVersion()
    override val type: BuildType = BuildType.FOSS
    override val platform: Platform = Platform.ANDROID

    override fun toString(): String = "test-version"
}

private class TestSettingsRouter : SettingsRouter {
    override fun navigateToUpscalerSetup() = Unit
    override fun navigateToFaq() = Unit

    var isBlogOpened = false
        private set

    override fun navigateToBlog() {
        isBlogOpened = true
    }

    override fun navigateToWebUi() = Unit
    override fun navigateToCivitai() = Unit

    override fun navigateToBackup() = Unit

    var serverSetupSource: LaunchSource? = null
    var storageUsageOpened = false
    var networkUsageOpened = false

    override fun openDrawer() = Unit

    override fun closeDrawer() = Unit

    override fun navigateBack() = Unit

    override fun navigateToServerSetup(source: LaunchSource) {
        serverSetupSource = source
    }

    override fun navigateToBenchmark() = Unit

    override fun navigateToStorageUsage() {
        storageUsageOpened = true
    }

    override fun navigateToNetworkUsage() {
        networkUsageOpened = true
    }

    override fun navigateToDebugMenu() = Unit

    override fun navigateToDonate() = Unit

    override fun navigateToOnBoarding(source: LaunchSource) = Unit
}

private class TestSettingsPlatformActions : SettingsPlatformActions {
    var requiresStoragePermissionForMediaStoreOverride = false
    var storagePermissionGranted = true
    var notificationPermissionGranted = true
    var appCacheBytes = 0L
    var visibleStorageThresholdBytes = 0L
    val modelBytes = mutableMapOf<String, Long>()
    val openedUrls = mutableListOf<String>()

    override val requiresStoragePermissionForMediaStore: Boolean
        get() = requiresStoragePermissionForMediaStoreOverride
    override val supportsBackgroundGeneration: Boolean = true
    override val supportsConnectionTimeout: Boolean = true
    override val isNnapiAvailable: Boolean = true
    override val backgroundGenerationWarningKey = "settings_item_background_generation_warning"

    override suspend fun requestStoragePermission(): Boolean = storagePermissionGranted

    override suspend fun requestNotificationPermission(): Boolean = notificationPermissionGranted

    override fun mapStorageBytesForUi(bytes: Long): Long {
        val safeBytes = bytes.coerceAtLeast(0L)
        return if (safeBytes < visibleStorageThresholdBytes) 0L else safeBytes
    }

    override suspend fun getAppCacheBytes(): Long = appCacheBytes

    override suspend fun clearAppCache() {
        appCacheBytes = 0L
    }


    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long =
        modelIds.distinct().sumOf { modelBytes[it] ?: 0L }

    override fun openUrl(url: String) {
        openedUrls += url
    }

    override fun shareLogFile() = Unit

    override fun showDeveloperModeUnlocked() = Unit

    override fun openAppSettings() = Unit
}

private class TestLinksProvider : LinksProvider {
    override val hordeUrl = ""
    override val hordeSignUpUrl = ""
    override val huggingFaceUrl = ""
    override val openAiInfoUrl = ""
    override val stabilityAiInfoUrl = ""
    override val falAiInfoUrl = ""
    override val arliAiInfoUrl = ""
    override val privacyPolicyUrl = "https://policy.test"
    override val donateUrl = ""
    override val projectWebsiteUrl = "https://project.test"
    override val developerWebsiteUrl = "https://developer.test"
    override val gitHubSourceUrl = "https://source.test"
    override val licenseUrl = "https://license.test"
    override val setupInstructionsUrl = ""
    override val swarmUiInfoUrl = ""
    override val demoModeUrl = ""
    override val telegramCommunityUrl = "https://telegram.test"
    override val discordCommunityUrl = "https://discord.test"
    override val linkedInCommunityUrl = "https://linkedin.test"
}
