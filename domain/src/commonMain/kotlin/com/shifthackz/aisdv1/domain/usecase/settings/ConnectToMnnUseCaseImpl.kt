package com.shifthackz.aisdv1.domain.usecase.settings

import com.shifthackz.aisdv1.domain.entity.MnnBackend
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import kotlinx.coroutines.CancellationException

/**
 * Keeps failed bundle selection from replacing a working provider configuration.
 *
 * @param repository Strict downloaded-bundle validation.
 * @param getConfigurationUseCase Reads the current complete configuration.
 * @param setServerConfigurationUseCase Atomically commits provider selection and its revision.
 *
 * @author Dmitriy Moroz
 */
internal class ConnectToMnnUseCaseImpl(
    private val repository: MnnGenerationRepository,
    private val getConfigurationUseCase: GetConfigurationUseCase,
    private val setServerConfigurationUseCase: SetServerConfigurationUseCase
) : ConnectToMnnUseCase {
    /**
     * Validates before persistence; cleanup failures are suppressed behind a primary failure or cancellation.
     *
     * @param modelId Catalog bundle identity.
     * @param modelPath External folder used only for the standard custom-model identity.
     * @param backend Independently saved execution backend.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun invoke(
        modelId: String,
        modelPath: String,
        backend: MnnBackend
    ): Result<Unit> = try {
        repository.validateModel(modelId, modelPath).use { selection ->
            val configuration = getConfigurationUseCase()
            setServerConfigurationUseCase(
                configuration.copy(
                    source = ServerSource.LOCAL_MNN,
                    localMnnModelId = modelId,
                    localMnnActivationId = selection.activationId,
                    localMnnCustomActivationId = if (modelId == LocalAiModel.CustomMnn.id) selection.activationId else configuration.localMnnCustomActivationId,
                    localMnnModelPath = modelPath,
                    localMnnBackend = backend
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
