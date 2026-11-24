package com.shifthackz.aisdv1.presentation.configuration.upscaler

import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserver
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.MviEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.navigation.router.ServerSetupRouter
import com.shifthackz.aisdv1.domain.entity.DownloadState
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadRequest
import com.shifthackz.aisdv1.domain.feature.work.ModelDownloadManager
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.DeleteModelUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.downloadable.RefreshLocalModelCatalogUseCase
import com.shifthackz.aisdv1.domain.usecase.settings.ConfigureUpscalerUseCase
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

/**
 * Owns upscaler setup while transfers survive leaving the screen through the shared download manager.
 *
 * @param diagnostics Bounded MVI observer.
 * @param dispatchers Platform IO and UI dispatchers.
 * @param refresh Loads the dedicated catalog.
 * @param cached Reloads download completeness after each transfer snapshot.
 * @param configure Validates and commits independent upscaler selection.
 * @param delete Removes only app-owned catalog downloads.
 * @param downloads Shared persistent download owner.
 * @param preferences Supplies the initial independent model selection.
 * @param buildInfo Supplies platform identity for provider branding.
 * @param router Returns to the calling screen after configuration.
 * @param storageObserver Invalidates both storage summaries after owned model changes.
 * @author Dmitriy Moroz
 */
internal class UpscalerSetupViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchers: DispatchersProvider,
    private val refresh: RefreshLocalModelCatalogUseCase,
    private val cached: GetCachedLocalModelCatalogUseCase,
    private val configure: ConfigureUpscalerUseCase,
    private val delete: DeleteModelUseCase,
    private val downloads: ModelDownloadManager,
    preferences: PreferenceManager,
    buildInfo: BuildInfoProvider,
    private val router: ServerSetupRouter,
    private val storageObserver: StorageUsageObserver,
) : BaseMviViewModel<UpscalerSetupState, UpscalerSetupIntent, MviEffect>(
    diagnostics,
    NavigationDestination.SERVER_SETUP.asDiagnosticScreen(),
    UpscalerSetupState(
        platform = buildInfo.platform,
        selectedId = preferences.upscalerModelId,
        source = UpscalerSource.forModel(preferences.upscalerModelId)
            .takeIf { buildInfo.platform in it.allowedPlatforms }
            ?: UpscalerSource.availableOn(buildInfo.platform).first(),
    ),
    dispatchers.immediate
) {
    init {
        load()
        launch(dispatchers.io) {
            downloads.batches.collect { batches ->
                val owned = batches.filter { it.type == LocalAiModel.Type.Upscaler }
                updateState { it.copy(downloads = owned) }
                if (owned.isNotEmpty()) perform {
                    val models = cached(LocalAiModel.Type.Upscaler)
                    val failure =
                        owned.mapNotNull { (it.progress as? DownloadState.Error)?.error?.message }
                            .firstOrNull()
                    updateState { it.copy(models = models, error = failure ?: it.error) }
                    storageObserver.notifyChanged()
                }
            }
        }
    }

    /**
     * Dispatches wizard navigation and model actions without touching generation configuration.
     *
     * @param intent User action on the current configuration draft.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: UpscalerSetupIntent) {
        when (intent) {
            is UpscalerSetupIntent.Discover -> updateState { it.copy(discovery = intent.discovery) }
            UpscalerSetupIntent.Back -> if (currentState.step == ServerSetupState.Step.CONFIGURE)
                updateState { it.copy(step = ServerSetupState.Step.SOURCE) } else router.navigateBack()

            UpscalerSetupIntent.Next -> updateState { it.copy(step = ServerSetupState.Step.CONFIGURE) }
            UpscalerSetupIntent.Retry -> load()
            UpscalerSetupIntent.Dismiss -> updateState {
                it.copy(
                    error = null,
                    pendingDeleteId = null
                )
            }

            UpscalerSetupIntent.Save -> launch(dispatchers.io) {
                perform {
                    configure(currentState.selectedId)
                    withContext(dispatchers.immediate) { router.navigateBack() }
                }
            }

            is UpscalerSetupIntent.SelectSource -> {
                if (intent.source !in currentState.sources) return
                updateState { state ->
                    state.copy(source = intent.source, selectedId = state.models.firstOrNull {
                        it.downloaded && UpscalerSource.forModel(it.id) == intent.source
                    }?.id.orEmpty())
                }
            }

            is UpscalerSetupIntent.Select -> updateState { it.copy(selectedId = intent.id) }
            is UpscalerSetupIntent.ModelAction -> {
                val model = currentState.models.single { it.id == intent.id }
                if (currentState.downloads.any { it.isRunning && it.modelId == model.id }) {
                    launch(dispatchers.io) {
                        perform {
                            downloads.cancel(
                                LocalAiModel.Type.Upscaler,
                                model.id
                            )
                        }
                    }
                } else if (model.downloaded) updateState { it.copy(pendingDeleteId = model.id) }
                else launch(dispatchers.io) {
                    perform {
                        downloads.start(
                            ModelDownloadRequest(
                                LocalAiModel.Type.Upscaler,
                                model.id,
                                model.sources.first()
                            )
                        )
                    }
                }
            }

            UpscalerSetupIntent.ConfirmDelete -> {
                val id = currentState.pendingDeleteId ?: return
                updateState { it.copy(pendingDeleteId = null) }
                launch(dispatchers.io) { perform { delete(id); storageObserver.notifyChanged(); load() } }
            }
        }
    }

    private fun load() {
        launch(dispatchers.io) {
            perform {
                updateState { it.copy(isLoading = true, error = null) }
                val result = refresh(LocalAiModel.Type.Upscaler)
                updateState {
                    it.copy(
                        isLoading = false,
                        models = result.models,
                        catalogStatus = if (result.failure == null) LocalCatalogStatus.READY else LocalCatalogStatus.ERROR
                    )
                }
            }
        }
    }

    private suspend fun perform(block: suspend () -> Unit) {
        try {
            block()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            updateState { it.copy(isLoading = false, error = failure.message ?: "") }
        }
    }
}
