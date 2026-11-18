package com.shifthackz.aisdv1.presentation.generation.modal.embedding

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.sdembedding.FetchAndGetEmbeddingsUseCase
import com.shifthackz.aisdv1.presentation.generation.modal.extras.ExtrasEffect
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.presentation.components.utils.ExtrasFormatter

/**
 * Loads provider embeddings and edits their inclusion independently in positive and negative prompts.
 *
 * @param diagnostics Optional bounded observer for embedding-editor MVI transitions.
 * @param dispatchersProvider Runs catalog fetching on IO and dispatches confirmed prompt effects.
 * @param fetchAndGetEmbeddingsUseCase Loads textual-inversion keywords for the selected provider.
 * @param preferenceManager Supplies the selected provider identity for catalog/empty-state rendering.
 * @param prompt Positive prompt captured before editing.
 * @param negativePrompt Negative prompt captured before editing.
 * @param onError Receives catalog-load failures after the error state is published.
 *
 * @author Dmitriy Moroz
 */
class EmbeddingViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    private val fetchAndGetEmbeddingsUseCase: FetchAndGetEmbeddingsUseCase,
    private val preferenceManager: PreferenceManager,
    prompt: String,
    negativePrompt: String,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<EmbeddingState, EmbeddingIntent, ExtrasEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.EMBEDDING,
    initialState = EmbeddingState(source = preferenceManager.source),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        loadData(prompt, negativePrompt)
    }

    override fun processIntent(intent: EmbeddingIntent) {
        when (intent) {
            EmbeddingIntent.ApplyNewPrompts -> emitEffect(
                ExtrasEffect.ApplyPrompts(currentState.prompt, currentState.negativePrompt),
            )

            is EmbeddingIntent.ChangeSelector -> updateState {
                it.copy(selector = intent.flag)
            }

            EmbeddingIntent.Close -> emitEffect(ExtrasEffect.Close)

            is EmbeddingIntent.ToggleItem -> updateState { state ->
                state.copy(
                    embeddings = state.embeddings.map {
                        if (it.keyword != intent.item.keyword) it
                        else {
                            if (state.selector) it.copy(isInPrompt = !it.isInPrompt)
                            else it.copy(isInNegativePrompt = !it.isInNegativePrompt)
                        }
                    },
                    prompt = if (!state.selector) state.prompt else ExtrasFormatter.toggleEmbedding(
                        state.prompt,
                        intent.item.keyword,
                    ),
                    negativePrompt = if (state.selector) state.negativePrompt else ExtrasFormatter.toggleEmbedding(
                        state.negativePrompt,
                        intent.item.keyword,
                    ),
                )
            }
        }
    }

    private fun loadData(prompt: String, negativePrompt: String) {
        updateState { state ->
            state.copy(
                loading = true,
                source = preferenceManager.source,
            )
        }

        launch(dispatchersProvider.io) {
            runCatching { fetchAndGetEmbeddingsUseCase() }
                .onSuccess { embeddings ->
                    updateState { state ->
                        state.copy(
                            loading = false,
                            source = preferenceManager.source,
                            error = ErrorState.None,
                            prompt = prompt,
                            negativePrompt = negativePrompt,
                            embeddings = embeddings.map {
                                EmbeddingItemUi(
                                    keyword = it.keyword,
                                    isInPrompt = ExtrasFormatter.isEmbeddingPresentInPrompt(
                                        prompt,
                                        it.keyword,
                                    ),
                                    isInNegativePrompt = ExtrasFormatter.isEmbeddingPresentInPrompt(
                                        negativePrompt,
                                        it.keyword,
                                    ),
                                )
                            },
                        )
                    }
                }
                .onFailure { t ->
                    updateState { it.copy(loading = false, error = ErrorState.Generic) }
                    onError(t)
                }
        }
    }
}
