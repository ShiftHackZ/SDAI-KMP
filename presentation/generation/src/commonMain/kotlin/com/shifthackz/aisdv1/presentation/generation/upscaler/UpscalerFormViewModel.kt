package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.downloadable.GetCachedLocalModelCatalogUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Observes the shared upscaler catalog for all forms retained by the application root.
 *
 * @param preferences Emits changes when upscaler setup commits a new selection.
 * @param models Reads current model metadata and filesystem completeness.
 * @param buildInfo Supplies platform identity for provider artwork.
 * @param dispatchers Moves configuration reads off the UI thread.
 * @author Dmitriy Moroz
 */
class UpscalerFormViewModel(
    private val preferences: PreferenceManager,
    private val models: GetCachedLocalModelCatalogUseCase,
    dispatchers: DispatchersProvider, buildInfo: BuildInfoProvider
) : ViewModel() {
    private val mutableState =
        MutableStateFlow(
            UpscalerFormState(
                UpscalerSource.availableOn(buildInfo.platform).isNotEmpty(), buildInfo.platform
            )
        )
    val state = mutableState.asStateFlow()

    init {
        viewModelScope.launch(dispatchers.io) {
            preferences.observe().collect {
                try {
                    val id = preferences.upscalerModelId
                    val catalog = models(LocalAiModel.Type.Upscaler).filter {
                        buildInfo.platform in UpscalerSource.forModel(it.id).allowedPlatforms
                    }
                    mutableState.value = mutableState.value.copy(
                        model = catalog.firstOrNull { it.id == id }, models = catalog,
                        isLoading = false, error = null
                    )
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    mutableState.value =
                        mutableState.value.copy(
                            model = null,
                            isLoading = false,
                            error = failure.message ?: ""
                        )
                }
            }
        }
    }
}
