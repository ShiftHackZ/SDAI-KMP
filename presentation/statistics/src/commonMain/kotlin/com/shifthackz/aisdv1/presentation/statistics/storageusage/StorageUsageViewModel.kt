package com.shifthackz.aisdv1.presentation.statistics.storageusage

import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserver

import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMnnModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalBonsaiModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalCoreMlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalMediaPipeModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalOnnxModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetLocalSdxlModelsUseCase
import com.shifthackz.aisdv1.domain.usecase.gallery.DeleteAllGalleryUseCase
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import com.shifthackz.aisdv1.core.navigation.router.StorageUsageRouter
import com.shifthackz.aisdv1.domain.entity.setupAllowedModes
import com.shifthackz.aisdv1.presentation.statistics.storageusage.platform.StorageUsagePlatformActions
import com.shifthackz.aisdv1.presentation.components.model.UsageCategory
import com.shifthackz.aisdv1.presentation.components.model.UsageItem
import com.shifthackz.aisdv1.presentation.components.model.UsageState
import com.shifthackz.aisdv1.presentation.components.model.isModelCategory
import com.shifthackz.aisdv1.presentation.components.model.isStorageCategory
import com.shifthackz.aisdv1.presentation.components.model.resolveSelectedCategory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * ViewModel for the standalone storage usage screen.
 *
 * Storage usage is assembled from platform cache bytes, generated gallery payloads, and downloaded
 * model directories. The ViewModel listens to [StorageUsageObserver] instead of lifecycle callbacks
 * so Settings summaries and the standalone screen refresh from the same invalidation signal.
 *
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 * @param dispatchersProvider App coroutine dispatchers used by the MVI base class and IO work.
 * @param galleryReads Reads payload-free gallery storage aggregates.
 * @param deleteAllGalleryUseCase Deletes gallery records after explicit user confirmation.
 * @param getLocalOnnxModelsUseCase Reads locally configured ONNX models.
 * @param getLocalMediaPipeModelsUseCase Reads locally configured MediaPipe models.
 * @param getLocalSdxlModelsUseCase Reads locally configured SDXL models.
 * @param getLocalCoreMlModelsUseCase Reads locally configured Core ML models on supported builds.
 * @param getLocalMnnModelsUseCase Reads MNN catalog identities; custom models are excluded from accounting.
 * @param getLocalBonsaiModelsUseCase Reads catalog Bonsai downloads for supported-platform storage accounting.
 * @param deleteModelUseCase Deletes downloaded local model metadata and files.
 * @param storageUsageObserver Shared invalidation stream used by Settings and this screen.
 * @param buildInfoProvider Build metadata used to filter platform-supported model categories.
 * @param router Standalone route navigation contract.
 * @param platformActions Filesystem bridge for cache and downloaded model byte accounting.
 * @param onError Error callback forwarded to the app-level error handling pipeline.
 *
 * @author Dmitriy Moroz
 */
class StorageUsageViewModel(
    diagnostics: UiDiagnostics,
    dispatchersProvider: DispatchersProvider,
    private val galleryReads: GalleryReadGateway,
    private val deleteAllGalleryUseCase: DeleteAllGalleryUseCase,
    private val getLocalOnnxModelsUseCase: GetLocalOnnxModelsUseCase,
    private val getLocalMediaPipeModelsUseCase: GetLocalMediaPipeModelsUseCase,
    private val getLocalSdxlModelsUseCase: GetLocalSdxlModelsUseCase,
    private val getLocalCoreMlModelsUseCase: GetLocalCoreMlModelsUseCase,
    private val getLocalMnnModelsUseCase: GetLocalMnnModelsUseCase,
    private val getLocalBonsaiModelsUseCase: GetLocalBonsaiModelsUseCase,
    private val deleteModelUseCase: DeleteModelUseCase,
    private val storageUsageObserver: StorageUsageObserver,
    private val buildInfoProvider: BuildInfoProvider,
    private val router: StorageUsageRouter,
    private val platformActions: StorageUsagePlatformActions,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<StorageUsageState, StorageUsageIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = NavigationDestination.STORAGE_USAGE.asDiagnosticScreen(),
    initialState = StorageUsageState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    private val ioDispatcher = dispatchersProvider.io

    init {
        observeStorageUsage()
    }

    override fun processIntent(intent: StorageUsageIntent) {
        when (intent) {
            StorageUsageIntent.NavigateBack -> router.navigateBack()

            is StorageUsageIntent.SelectCategory -> selectCategory(intent.category)

            is StorageUsageIntent.RequestClearCategory -> requestClearCategory(intent.category)

            is StorageUsageIntent.ConfirmClearCategory -> clearCategory(intent.category)

            StorageUsageIntent.RequestClearAll -> requestClearAll()

            is StorageUsageIntent.ConfirmClearAll -> clearAll(intent.categories.toSet())

            StorageUsageIntent.DismissDialog -> updateState {
                it.copy(screenModal = StorageUsageModal.None)
            }
        }
    }

    private fun selectCategory(category: UsageCategory) {
        if (!category.isStorageCategory()) return
        updateState { state ->
            state.copy(usage = state.usage.copy(selectedCategory = category))
        }
    }

    private fun observeStorageUsage() {
        updateState { state ->
            state.copy(usage = state.usage.copy(loading = true))
        }
        launch(ioDispatcher) {
            storageUsageObserver.observe()
                .map {
                    runCatching {
                        loadStorageUsage(currentState.usage.selectedCategory)
                    }
                        .onFailure(onError)
                        .getOrElse { currentState.usage.copy(loading = false) }
                }
                .distinctUntilChanged()
                .collect { usage ->
                    updateState { state -> state.copy(usage = usage) }
                }
        }
    }

    private suspend fun loadStorageUsage(
        selectedCategory: UsageCategory?,
    ): UsageState {
        val onnxModels = getDownloadedModels(getLocalOnnxModelsUseCase::invoke)
        val mediaPipeModels = getDownloadedModels(getLocalMediaPipeModelsUseCase::invoke)
        val sdxlModels = getDownloadedModels(getLocalSdxlModelsUseCase::invoke)
        val coreMlModels = getDownloadedModels(getLocalCoreMlModelsUseCase::invoke)
        val mnnModels = getDownloadedModels { getLocalMnnModelsUseCase().models }
        val mnnModelIds = mnnModels.map(LocalAiModel::id)
        val bonsaiModels = getDownloadedModels(getLocalBonsaiModelsUseCase::invoke)
        val coreMlModelIds = coreMlModels.map(LocalAiModel::id)
        val bonsaiModelIds = bonsaiModels.map(LocalAiModel::id)
        val allowedModes = buildInfoProvider.setupAllowedModes()
        val items = buildList {
            add(
                UsageItem(
                    category = UsageCategory.CACHE,
                    bytes = platformActions.mapStorageBytesForUi(platformActions.getAppCacheBytes()),
                ),
            )
            add(
                UsageItem(
                    category = UsageCategory.GALLERY,
                    bytes = galleryReads.storageBytes(),
                ),
            )
            if (ServerSource.LOCAL_MICROSOFT_ONNX in allowedModes) {
                add(
                    UsageItem(
                        category = UsageCategory.MODELS_ONNX,
                        bytes = platformActions.mapStorageBytesForUi(
                            platformActions.getDownloadedModelsBytes(onnxModels.map(LocalAiModel::id)),
                        ),
                        modelIds = onnxModels.map(LocalAiModel::id),
                    ),
                )
            }
            if (ServerSource.LOCAL_GOOGLE_MEDIA_PIPE in allowedModes) {
                add(
                    UsageItem(
                        category = UsageCategory.MODELS_MEDIAPIPE,
                        bytes = platformActions.mapStorageBytesForUi(
                            platformActions.getDownloadedModelsBytes(
                                mediaPipeModels.map(
                                    LocalAiModel::id
                                )
                            ),
                        ),
                        modelIds = mediaPipeModels.map(LocalAiModel::id),
                    ),
                )
            }
            if (ServerSource.LOCAL_STABLE_DIFFUSION_CPP in allowedModes) {
                add(
                    UsageItem(
                        category = UsageCategory.MODELS_SDXL,
                        bytes = platformActions.mapStorageBytesForUi(
                            platformActions.getDownloadedModelsBytes(sdxlModels.map(LocalAiModel::id)),
                        ),
                        modelIds = sdxlModels.map(LocalAiModel::id),
                    ),
                )
            }
            if (ServerSource.LOCAL_APPLE_CORE_ML in allowedModes) {
                add(
                    UsageItem(
                        category = UsageCategory.MODELS_CORE_ML,
                        bytes = platformActions.mapStorageBytesForUi(
                            platformActions.getDownloadedModelsBytes(coreMlModelIds),
                        ),
                        modelIds = coreMlModelIds,
                    ),
                )
            }
            if (ServerSource.LOCAL_MNN in allowedModes) {
                add(
                    UsageItem(
                        category = UsageCategory.MODELS_MNN,
                        bytes = platformActions.mapStorageBytesForUi(
                            platformActions.getDownloadedModelsBytes(
                                mnnModelIds
                            )
                        ),
                        modelIds = mnnModelIds
                    )
                )
            }
            if (ServerSource.LOCAL_APPLE_BONSAI in allowedModes) {
                add(
                    UsageItem(
                        category = UsageCategory.MODELS_BONSAI,
                        bytes = platformActions.mapStorageBytesForUi(
                            platformActions.getDownloadedModelsBytes(bonsaiModelIds),
                        ),
                        modelIds = bonsaiModelIds,
                    ),
                )
            }
        }
        return UsageState(
            loading = false,
            items = items,
            selectedCategory = items.resolveSelectedCategory(selectedCategory),
        )
    }

    private fun requestClearCategory(category: UsageCategory) {
        if (!category.isStorageCategory()) return
        val item = currentState.usage.items
            .firstOrNull { it.category == category && it.enabled }
            ?: return
        updateState { it.copy(screenModal = StorageUsageModal.ClearCategory(item)) }
    }

    private fun requestClearAll() {
        val items = currentState.usage.items
            .filter { item -> item.enabled && item.category.isStorageCategory() }
        if (items.isEmpty()) return
        updateState {
            it.copy(
                screenModal = StorageUsageModal.ClearAll(
                    items = items,
                    totalBytes = items.sumOf(UsageItem::bytes),
                ),
            )
        }
    }

    private fun clearCategory(category: UsageCategory) {
        if (!category.isStorageCategory()) return
        updateState {
            it.copy(
                screenModal = StorageUsageModal.None,
                usage = it.usage.copy(loading = true),
            )
        }
        launch(ioDispatcher) {
            runCatching {
                clearCategoryInternal(category)
            }
                .onSuccess { storageUsageObserver.notifyChanged() }
                .onFailure { t ->
                    onError(t)
                    updateState {
                        it.copy(
                            screenModal = StorageUsageModal.None,
                            usage = it.usage.copy(loading = false),
                        )
                    }
                }
        }
    }

    private fun clearAll(categories: Set<UsageCategory>) {
        if (categories.isEmpty()) {
            updateState { it.copy(screenModal = StorageUsageModal.None) }
            return
        }
        updateState {
            it.copy(
                screenModal = StorageUsageModal.None,
                usage = it.usage.copy(loading = true),
            )
        }
        launch(ioDispatcher) {
            runCatching {
                if (UsageCategory.CACHE in categories) {
                    platformActions.clearAppCache()
                }
                if (UsageCategory.GALLERY in categories) {
                    deleteAllGalleryUseCase()
                }
                currentState.usage.items
                    .filter { item -> item.category in categories && item.category.isModelCategory() }
                    .flatMap(UsageItem::modelIds)
                    .distinct()
                    .forEach { id -> deleteModelUseCase(id) }
            }
                .onSuccess { storageUsageObserver.notifyChanged() }
                .onFailure { t ->
                    onError(t)
                    updateState {
                        it.copy(
                            screenModal = StorageUsageModal.None,
                            usage = it.usage.copy(loading = false),
                        )
                    }
                }
        }
    }

    private suspend fun clearCategoryInternal(category: UsageCategory) {
        when (category) {
            UsageCategory.CACHE -> platformActions.clearAppCache()
            UsageCategory.GALLERY -> deleteAllGalleryUseCase()
            UsageCategory.MODELS_ONNX,
            UsageCategory.MODELS_MEDIAPIPE,
            UsageCategory.MODELS_SDXL,
            UsageCategory.MODELS_CORE_ML,
            UsageCategory.MODELS_MNN,
            UsageCategory.MODELS_BONSAI,
                -> currentState.usage.items
                .firstOrNull { it.category == category }
                ?.modelIds
                ?.forEach { id -> deleteModelUseCase(id) }

            UsageCategory.TRAFFIC_MODELS,
            UsageCategory.TRAFFIC_CONFIGS,
            UsageCategory.TRAFFIC_INFERENCE,
            UsageCategory.TRAFFIC_CIVITAI,
                -> Unit
        }
    }

    private suspend fun getDownloadedModels(
        getModels: suspend () -> List<LocalAiModel>,
    ): List<LocalAiModel> = runCatching { getModels() }
        .onFailure(onError)
        .getOrDefault(emptyList())
        .filter { model ->
            model.downloaded &&
                    model.id !in customModelIds
        }
}

/**
 * Custom model identities are excluded from catalog storage totals, including app-owned snapshots.
 *
 * @author Dmitriy Moroz
 */
private val customModelIds = setOf(
    LocalAiModel.CustomOnnx.id,
    LocalAiModel.CustomMediaPipe.id,
    LocalAiModel.CustomSdxl.id,
    LocalAiModel.CustomCoreMl.id,
    LocalAiModel.CustomBonsai.id,
    LocalAiModel.CustomMnn.id,
)
