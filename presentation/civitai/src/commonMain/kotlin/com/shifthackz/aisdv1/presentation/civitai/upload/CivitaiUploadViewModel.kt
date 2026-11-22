package com.shifthackz.aisdv1.presentation.civitai.upload

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.domain.entity.CivitaiUploadStatus
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import com.shifthackz.aisdv1.domain.usecase.civitai.UploadToCivitaiUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.SaveLastResultToCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.caching.GetLastResultFromCacheUseCase
import com.shifthackz.aisdv1.domain.usecase.generation.SaveGenerationResultUseCase
import kotlinx.coroutines.CancellationException

/**
 * Captures one gallery image, serializes taps and distinguishes opening a receipt from publishing.
 *
 * @param imageId Persistent gallery identity, or nonpositive for the retained unsaved generation.
 * @param repository Reads authorization and durable publication receipts.
 * @param upload Shared duplicate-guarded publication operation.
 * @param cachedResult Loads original content for an unsaved gallery result.
 * @param save Persists transient content before publishing it.
 * @param cache Retains the saved identity when returning to the last generated result.
 * @param dispatchers IO and effect dispatchers.
 * @param diagnostics Bounded screen observer, never given image bytes or credentials.
 * @author Dmitriy Moroz
 */
class CivitaiUploadViewModel(
    private val imageId: Long,
    private val repository: CivitaiRepository,
    private val upload: UploadToCivitaiUseCase,
    private val cachedResult: GetLastResultFromCacheUseCase,
    private val save: SaveGenerationResultUseCase,
    private val cache: SaveLastResultToCacheUseCase,
    private val dispatchers: DispatchersProvider,
    diagnostics: UiDiagnostics,
) : BaseMviViewModel<CivitaiUploadState, CivitaiUploadIntent, CivitaiUploadEffect>(
    diagnostics,
    NavigationDestination.GALLERY_DETAIL.asDiagnosticScreen(),
    CivitaiUploadState(),
    dispatchers.immediate,
) {
    private var savedImageId: Long? = imageId.takeIf { it > 0 }

    /**
     * Routes gallery publication actions while preventing repeated taps during communication.
     *
     * @param intent Upload, settings navigation or modal dismissal for the captured image.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: CivitaiUploadIntent) {
        if (currentState.phase == CivitaiUploadPhase.COMMUNICATING) return
        when (intent) {
            CivitaiUploadIntent.SIGN_IN -> {
                updateState { CivitaiUploadState() }
                emitEffect(CivitaiUploadEffect.SignIn)
            }

            CivitaiUploadIntent.DISMISS -> updateState { CivitaiUploadState() }
            CivitaiUploadIntent.UPLOAD -> publish()
        }
    }

    /**
     * Runs the captured output through the normal saved-gallery upload path.
     *
     * @author Dmitriy Moroz
     */
    private fun publish() {
        updateState { CivitaiUploadState(CivitaiUploadPhase.COMMUNICATING) }
        launch(dispatchers.io) {
            try {
                if (repository.account() == null) {
                    updateState { CivitaiUploadState(CivitaiUploadPhase.LOGIN_REQUIRED) }
                    return@launch
                }
                val id = savedImageId ?: cachedResult().let { original ->
                    val persistentId = save(original)
                    savedImageId = persistentId
                    cache(original.copy(id = persistentId))
                    persistentId
                }
                val existing = repository.receipt(id)
                val existingUrl = existing?.url
                if (existing?.status == CivitaiUploadStatus.PUBLISHED && existingUrl != null) {
                    updateState { CivitaiUploadState() }
                    emitEffect(CivitaiUploadEffect.OpenPost(existingUrl))
                } else {
                    upload(id)
                    updateState { CivitaiUploadState(CivitaiUploadPhase.SUCCESS) }
                }
            } catch (error: CancellationException) {
                throw error
            } catch (_: Exception) {
                updateState { CivitaiUploadState(CivitaiUploadPhase.FAILURE) }
            }
        }
    }
}
