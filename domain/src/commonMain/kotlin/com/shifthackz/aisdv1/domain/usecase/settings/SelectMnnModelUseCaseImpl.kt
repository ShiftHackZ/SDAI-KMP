package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import kotlinx.coroutines.CancellationException

/**
 * Reuses the last committed custom snapshot even after the external folder is removed.
 *
 * @param repository Strict owned-bundle identity validation.
 * @param getConfigurationUseCase Reads the model/backend selection associated with this picker action.
 * @param setServerConfigurationUseCase Atomically commits the validated manifest identity.
 *
 * @author Dmitriy Moroz
 */
internal class SelectMnnModelUseCaseImpl(
    private val repository: MnnGenerationRepository,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase
) : SelectMnnModelUseCase {
    /**
     * Selects existing owned bytes without reading or copying the original custom folder.
     * Cleanup failures cannot replace a primary configuration failure or cancellation.
     *
     * @param modelId Catalog or custom UI identity.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(modelId: String): Result<Unit> = try {
        val configuration = getConfigurationUseCase()
        check(configuration.source == ServerSource.LOCAL_MNN) { "MNN provider selection changed" }
        repository.validateOwnedModel(
            modelId,
            if (modelId == LocalAiModel.CustomMnn.id) configuration.localMnnCustomActivationId else ""
        ).use { selection ->
            val current = getConfigurationUseCase()
            check(current == configuration) { "MNN configuration changed during selection" }
            setServerConfigurationUseCase(
                current.copy(
                    localMnnModelId = modelId,
                    localMnnActivationId = selection.activationId
                )
            )
            Result.success(Unit)
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: Exception) {
        Result.failure(failure)
    }
}
