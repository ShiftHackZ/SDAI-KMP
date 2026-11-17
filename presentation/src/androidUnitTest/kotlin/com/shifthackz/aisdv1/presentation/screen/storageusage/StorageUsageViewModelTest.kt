package com.shifthackz.aisdv1.presentation.screen.storageusage

import com.shifthackz.aisdv1.domain.entity.mnn.MnnCatalogResult
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.appbuild.BuildVersion
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.OpenAiOAuthIdentity
import com.shifthackz.aisdv1.presentation.mocks.mockAiGenerationResult
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.GetAllGalleryUseCase
import com.shifthackz.aisdv1.core.navigation.router.StorageUsageRouter
import com.shifthackz.aisdv1.presentation.screen.storageusage.model.StorageUsageIntent
import com.shifthackz.aisdv1.presentation.screen.storageusage.model.StorageUsageModal
import com.shifthackz.aisdv1.presentation.screen.storageusage.platform.StorageUsagePlatformActions
import com.shifthackz.aisdv1.presentation.model.UsageCategory
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMnnModelsUseCase
import io.mockk.coVerify
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import io.mockk.every
import com.shifthackz.aisdv1.presentation.screen.setup.platform.isServerSourceAvailableOnAndroid
import com.shifthackz.aisdv1.presentation.screen.setup.platform.isServerSourceAvailableOnPlatform
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
 * Verifies storage usage loading, confirmation dialogs, and observer-driven refreshes.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StorageUsageViewModelTest {

    private val galleryReads = mockk<GalleryReadGateway>()
    private val getAllGalleryUseCase = mockk<GetAllGalleryUseCase>()
    private val deleteAllGalleryUseCase = mockk<DeleteAllGalleryUseCase>()
    private val getLocalOnnxModelsUseCase = mockk<GetLocalOnnxModelsUseCase>()
    private val getLocalMediaPipeModelsUseCase = mockk<GetLocalMediaPipeModelsUseCase>()
    private val getLocalSdxlModelsUseCase = mockk<GetLocalSdxlModelsUseCase>()
    private val getLocalCoreMlModelsUseCase = mockk<GetLocalCoreMlModelsUseCase>()
    private val getLocalBonsaiModelsUseCase = mockk<GetLocalBonsaiModelsUseCase>()
    private val getLocalMnnModelsUseCase = mockk<GetLocalMnnModelsUseCase>()
    private val deleteModelUseCase = mockk<DeleteModelUseCase>()
    private val storageUsageObserver = StorageUsageObserver()
    private val router = TestStorageUsageRouter()
    private val platformActions = TestStorageUsagePlatformActions()
    private var onnxModels = emptyList<LocalAiModel>()
    private lateinit var dispatchersProvider: DispatchersProvider
    private var viewModelScope: CoroutineScope? = null

    @Before
    fun initialize() {
        mockkStatic(::isServerSourceAvailableOnPlatform)
        every { isServerSourceAvailableOnPlatform(any()) } answers {
            isServerSourceAvailableOnAndroid(firstArg(), arrayOf("arm64-v8a"))
        }
        coEvery { getAllGalleryUseCase() } returns emptyList()
        coEvery { galleryReads.storageBytes() } coAnswers { getAllGalleryUseCase().sumOf { it.image.encodeToByteArray().size.toLong() + it.inputImage.encodeToByteArray().size } }
        coEvery { deleteAllGalleryUseCase() } returns Unit
        coEvery { getLocalOnnxModelsUseCase() } answers { onnxModels }
        coEvery { getLocalMediaPipeModelsUseCase() } returns emptyList()
        coEvery { getLocalSdxlModelsUseCase() } returns emptyList()
        coEvery { getLocalCoreMlModelsUseCase() } returns emptyList()
        coEvery { getLocalBonsaiModelsUseCase() } returns emptyList()
        coEvery { getLocalMnnModelsUseCase() } returns MnnCatalogResult(emptyList())
        coEvery { deleteModelUseCase(any()) } returns Unit
    }

    @After
    fun tearDown() {
        unmockkStatic(::isServerSourceAvailableOnPlatform)
        viewModelScope?.cancel()
        viewModelScope = null
        onnxModels = emptyList()
        platformActions.reset()
        router.backInvocations = 0
    }

    @Test
    fun `compatible and OAuth gallery images contribute to storage totals`() = runTest {
        val image =
            "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+jO0sAAAAASUVORK5CYII="
        val compatible = mockAiGenerationResult.copy(
            id = 1L, image = image, inputImage = "", modelName = "gpt-image-2",
        )
        val oauth = compatible.copy(
            id = 2L, openAiOAuthIdentity = OpenAiOAuthIdentity("test-account", "test-revision"),
        )
        coEvery { getAllGalleryUseCase() } returns listOf(compatible, oauth)

        val viewModel = createViewModel()
        advanceUntilIdle()

        val galleryBytes =
            viewModel.state.value.usage.items.first { it.category == UsageCategory.GALLERY }.bytes
        assertEquals(image.encodeToByteArray().size * 2L, galleryBytes)
        assertEquals(galleryBytes, viewModel.state.value.usage.totalBytes)
    }

    @Test
    fun `initialized, expected real storage usage loaded`() = runTest {
        platformActions.appCacheBytes = 2048L
        val viewModel = createViewModel()
        advanceUntilIdle()

        val actual = viewModel.state.value.usage
        assertEquals(false, actual.loading)
        assertEquals(2048L, actual.totalBytes)
        assertEquals(2048L, actual.items.first { it.category == UsageCategory.CACHE }.bytes)
    }

    @Test
    fun `given clear all requested, expected confirmation before deleting cache`() = runTest {
        platformActions.appCacheBytes = 2048L
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(StorageUsageIntent.RequestClearAll)
        advanceUntilIdle()

        val modal = viewModel.state.value.screenModal
        assertTrue(modal is StorageUsageModal.ClearAll)
        assertEquals(2048L, platformActions.appCacheBytes)

        viewModel.processIntent(
            StorageUsageIntent.ConfirmClearAll(
                categories = (modal as StorageUsageModal.ClearAll).items.map { it.category },
            ),
        )
        assertEquals(StorageUsageModal.None, viewModel.state.value.screenModal)
        assertEquals(true, viewModel.state.value.usage.loading)
        advanceUntilIdle()

        assertEquals(0L, platformActions.appCacheBytes)
        assertEquals(0L, viewModel.state.value.usage.totalBytes)
    }

    @Test
    fun `given category clear requested, expected confirmation before deleting cache`() = runTest {
        platformActions.appCacheBytes = 1024L
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.processIntent(StorageUsageIntent.RequestClearCategory(UsageCategory.CACHE))
        advanceUntilIdle()

        val modal = viewModel.state.value.screenModal
        assertTrue(modal is StorageUsageModal.ClearCategory)
        assertEquals(1024L, (modal as StorageUsageModal.ClearCategory).item.bytes)

        viewModel.processIntent(StorageUsageIntent.ConfirmClearCategory(UsageCategory.CACHE))
        assertEquals(StorageUsageModal.None, viewModel.state.value.screenModal)
        assertEquals(true, viewModel.state.value.usage.loading)
        advanceUntilIdle()

        assertEquals(0L, platformActions.appCacheBytes)
        assertEquals(0L, viewModel.state.value.usage.totalBytes)
    }

    @Test
    fun `given downloaded model appears, expected observer reloads storage usage`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0L, viewModel.state.value.usage.totalBytes)

        onnxModels = listOf(downloadedModel("onnx-1", LocalAiModel.Type.ONNX))
        platformActions.modelBytes["onnx-1"] = 4096L
        storageUsageObserver.notifyChanged()
        advanceUntilIdle()

        val actual = viewModel.state.value.usage
        assertEquals(4096L, actual.totalBytes)
        assertEquals(4096L, actual.items.first { it.category == UsageCategory.MODELS_ONNX }.bytes)
    }

    @Test
    fun `given platform hides filesystem residue, expected usage UI remains empty`() = runTest {
        platformActions.visibleStorageThresholdBytes = 1024L
        platformActions.appCacheBytes = 64L
        onnxModels = listOf(downloadedModel("onnx-residue", LocalAiModel.Type.ONNX))
        platformActions.modelBytes["onnx-residue"] = 64L
        val viewModel = createViewModel()
        advanceUntilIdle()

        val actual = viewModel.state.value.usage

        assertEquals(0L, actual.totalBytes)
        assertEquals(0L, actual.items.first { it.category == UsageCategory.CACHE }.bytes)
        assertEquals(0L, actual.items.first { it.category == UsageCategory.MODELS_ONNX }.bytes)
        assertEquals(64L, platformActions.appCacheBytes)
        assertEquals(64L, platformActions.modelBytes.getValue("onnx-residue"))
    }

    @Test
    fun `custom identities from every Android provider contribute no model bytes`() = runTest {
        val customs = listOf(
            LocalAiModel.CustomOnnx, LocalAiModel.CustomMediaPipe, LocalAiModel.CustomSdxl,
            LocalAiModel.CustomBonsai, LocalAiModel.CustomMnn,
        ).map { it.copy(downloaded = true) }
        onnxModels = listOf(customs[0])
        coEvery { getLocalMediaPipeModelsUseCase() } returns listOf(customs[1])
        coEvery { getLocalSdxlModelsUseCase() } returns listOf(customs[2])
        coEvery { getLocalBonsaiModelsUseCase() } returns listOf(customs[3])
        coEvery { getLocalMnnModelsUseCase() } returns MnnCatalogResult(listOf(customs[4]))
        customs.forEach { platformActions.modelBytes[it.id] = 8192L }
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0L, viewModel.state.value.usage.totalBytes)
        assertTrue(viewModel.state.value.usage.items.all { it.modelIds.isEmpty() })
    }

    @Test
    fun `MNN totals and clearing include catalog models but exclude custom copies`() = runTest {
        val catalog = downloadedModel("mnn-catalog", LocalAiModel.Type.Mnn)
        coEvery { getLocalMnnModelsUseCase() } returns MnnCatalogResult(
            listOf(catalog, LocalAiModel.CustomMnn.copy(downloaded = true)),
        )
        platformActions.modelBytes.putAll(
            mapOf(catalog.id to 4096L, LocalAiModel.CustomMnn.id to 8192L, "mnn_custom" to 16384L),
        )
        val viewModel = createViewModel()
        advanceUntilIdle()

        val item =
            viewModel.state.value.usage.items.first { it.category == UsageCategory.MODELS_MNN }
        assertEquals(4096L, item.bytes)
        assertEquals(listOf(catalog.id), item.modelIds)
        assertEquals(4096L, viewModel.state.value.usage.totalBytes)
        viewModel.processIntent(StorageUsageIntent.ConfirmClearCategory(UsageCategory.MODELS_MNN))
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteModelUseCase(catalog.id) }
        coVerify(exactly = 0) { deleteModelUseCase(LocalAiModel.CustomMnn.id) }
        coVerify(exactly = 0) { deleteModelUseCase("mnn_custom") }
    }

    @Test
    fun `iOS Core ML accounting and clear all use only downloaded catalog identities`() = runTest {
        val catalog = downloadedModel("coreml-catalog", LocalAiModel.Type.CoreMl)
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
        val viewModel = createViewModel(object : BuildInfoProvider by TestBuildInfoProvider {
            override val platform = Platform.IOS
        })
        advanceUntilIdle()

        val item =
            viewModel.state.value.usage.items.first { it.category == UsageCategory.MODELS_CORE_ML }
        assertEquals(4096L, item.bytes)
        assertEquals(listOf(catalog.id), item.modelIds)
        viewModel.processIntent(StorageUsageIntent.ConfirmClearAll(listOf(UsageCategory.MODELS_CORE_ML)))
        advanceUntilIdle()

        coVerify(exactly = 1) { deleteModelUseCase(catalog.id) }
        coVerify(exactly = 0) { deleteModelUseCase(LocalAiModel.CustomCoreMl.id) }
        coVerify(exactly = 0) { deleteModelUseCase("unknown-custom") }
    }

    @Test
    fun `given back intent, expected router navigates back`() = runTest {
        val viewModel = createViewModel()

        viewModel.processIntent(StorageUsageIntent.NavigateBack)

        assertEquals(1, router.backInvocations)
    }

    private fun TestScope.createViewModel(
        buildInfoProvider: BuildInfoProvider = TestBuildInfoProvider,
    ): StorageUsageViewModel {
        val dispatcher = StandardTestDispatcher(testScheduler)
        dispatchersProvider = object : DispatchersProvider {
            override val io: CoroutineDispatcher = dispatcher
            override val ui: CoroutineDispatcher = dispatcher
            override val immediate: CoroutineDispatcher = dispatcher
        }
        viewModelScope = CoroutineScope(SupervisorJob() + dispatcher)
        return StorageUsageViewModel(
            galleryReads = galleryReads,
            diagnostics = mockk(relaxed = true),
            dispatchersProvider = dispatchersProvider,
            deleteAllGalleryUseCase = deleteAllGalleryUseCase,
            getLocalOnnxModelsUseCase = getLocalOnnxModelsUseCase,
            getLocalMediaPipeModelsUseCase = getLocalMediaPipeModelsUseCase,
            getLocalSdxlModelsUseCase = getLocalSdxlModelsUseCase,
            getLocalCoreMlModelsUseCase = getLocalCoreMlModelsUseCase,
            getLocalMnnModelsUseCase = getLocalMnnModelsUseCase,
            getLocalBonsaiModelsUseCase = getLocalBonsaiModelsUseCase,
            deleteModelUseCase = deleteModelUseCase,
            storageUsageObserver = storageUsageObserver,
            buildInfoProvider = buildInfoProvider,
            router = router,
            platformActions = platformActions,
        )
    }
}

/**
 * Creates a downloaded local AI model fixture for storage usage aggregation tests.
 *
 * @param id Stable local model identifier used by the storage usage row.
 * @param type Local model provider type used to group storage categories.
 *
 * @author Dmitriy Moroz
 */
private fun downloadedModel(
    id: String,
    type: LocalAiModel.Type,
) = LocalAiModel(
    id = id,
    type = type,
    name = id,
    size = "1 MB",
    sources = emptyList(),
    downloaded = true,
)

/**
 * Full-build test provider so all local model categories are allowed during storage tests.
 *
 * @author Dmitriy Moroz
 */
private object TestBuildInfoProvider : BuildInfoProvider {
    override val isDebug: Boolean = true
    override val buildNumber: Int = 5598
    override val version: BuildVersion = BuildVersion()
    override val type: BuildType = BuildType.FULL
    override val platform: Platform = Platform.ANDROID
}

/**
 * Router spy used to assert standalone screen back navigation.
 *
 * @author Dmitriy Moroz
 */
private class TestStorageUsageRouter : StorageUsageRouter {
    var backInvocations = 0

    override fun navigateBack() {
        backInvocations++
    }
}

/**
 * In-memory platform bridge that lets tests mutate cache and model sizes deterministically.
 *
 * @author Dmitriy Moroz
 */
private class TestStorageUsagePlatformActions : StorageUsagePlatformActions {
    var appCacheBytes = 0L
    var visibleStorageThresholdBytes = 0L
    val modelBytes = mutableMapOf<String, Long>()

    override fun mapStorageBytesForUi(bytes: Long): Long {
        val safeBytes = bytes.coerceAtLeast(0L)
        return if (safeBytes < visibleStorageThresholdBytes) 0L else safeBytes
    }

    override suspend fun getAppCacheBytes(): Long = appCacheBytes

    override suspend fun clearAppCache() {
        appCacheBytes = 0L
    }


    override suspend fun getDownloadedModelsBytes(modelIds: List<String>): Long =
        modelIds.distinct().sumOf { id -> modelBytes[id] ?: 0L }

    fun reset() {
        appCacheBytes = 0L
        visibleStorageThresholdBytes = 0L
        modelBytes.clear()
    }
}
