package com.shifthackz.aisdv1.data.repository

import android.os.Build
import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.local.MnnModelFileStore
import com.shifthackz.aisdv1.data.mappers.mapLocalDiffusionToAiGenResult
import com.shifthackz.aisdv1.data.mappers.withModelName
import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator

/**
 * Host-only MNN facade preserving strict storage/transport failures and gallery output ownership.
 *
 * @param mediaStoreGateway Exports complete original image bytes through the existing application flow.
 * @param localDataSource Stores the completed gallery result.
 * @param backgroundWorkObserver Existing result persistence ownership policy.
 * @param preferenceManager Current selection used only to reject removed or replaced queued identities.
 * @param nativeGateway Isolated attempt owner and bounded cancellation transport.
 * @param models Catalog metadata and current completion state.
 * @param modelFiles Immutable owned roots and their attempt leases.
 * @param validator Full manifest and payload integrity validation.
 *
 * @author Dmitriy Moroz
 */
internal class MnnGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    private val preferenceManager: PreferenceManager,
    private val nativeGateway: NativeGenerationGateway,
    private val models: DownloadableModelDataSource.Local,
    private val modelFiles: MnnModelFileStore,
    private val validator: MnnBundleValidator
) : CoreGenerationRepository(
    mediaStoreGateway,
    localDataSource, preferenceManager, backgroundWorkObserver
), MnnGenerationRepository {
    /**
     * Forwards only this engine's opaque percentage progress.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus() = nativeGateway.observeStatus(NativeGenerationEngine.MNN)

    /**
     * Prepares a bundle and protects its validated identity until setup finishes persistence.
     *
     * @param modelId Catalog identity.
     * @param sourcePath External root for custom setup; ignored for downloaded catalog identities.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun validateModel(
        modelId: String,
        sourcePath: String
    ): MnnModelSelectionLease {
        requireArm64()
        val activationId =
            if (modelId == LocalAiModel.CustomMnn.id) modelFiles.importCustom(sourcePath) else ""
        return validateOwnedModel(modelId, activationId)
    }

    /**
     * Validates and leases an immutable root through configuration commit without reading the external custom folder.
     *
     * @param modelId Catalog or custom UI identity.
     * @param activationId Captured custom manifest identity; catalog validation discovers its current identity.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun validateOwnedModel(
        modelId: String,
        activationId: String
    ): MnnModelSelectionLease {
        requireArm64()
        val model = models.getById(modelId)
        check(model.type == LocalAiModel.Type.Mnn)
        if (modelId == LocalAiModel.CustomMnn.id) require(activationId.matches(Regex("[0-9a-f]{64}")))
        val selection = modelFiles.acquireSelection(modelId, activationId)
        try {
            val payload = modelFiles.acquire(modelId, activationId)
            try {
                val manifest = validator(modelFiles.directory(modelId, activationId).path)
                if (activationId.isNotEmpty()) check(activationId == manifest.activationId) { "MNN artifact changed" }
                return MnnModelSelectionLeaseImpl(manifest.activationId, payload, selection)
            } catch (failure: Throwable) {
                payload.close()
                throw failure
            }
        } catch (failure: Throwable) {
            selection.close()
            throw failure
        }
    }

    /**
     * Holds host model ownership through transport; a separate child lock protects native readers even if teardown fails.
     *
     * @param payload Supported fixed SD 1.5 parameters; the seed is already resolved by domain policy.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun generateFromText(payload: TextToImagePayload) = run {
        requireArm64()
        val id = requireNotNull(payload.mnnModelId)
        val activationId = requireNotNull(payload.mnnActivationId)
        check(activationId == preferenceManager.localMnnActivationId) { "MNN artifact selection changed" }
        check(id == preferenceManager.localMnnModelId) { "MNN model selection changed" }
        val model = models.getById(id)
        check(model.type == LocalAiModel.Type.Mnn && model.downloaded) { "MNN model is unavailable" }
        modelFiles.acquire(id, activationId).use {
            check(
                validator(
                    modelFiles.directory(
                        id,
                        activationId
                    ).path
                ).activationId == activationId
            ) { "MNN artifact changed" }
            check(id == preferenceManager.localMnnModelId && activationId == preferenceManager.localMnnActivationId) { "MNN artifact selection changed" }
            val encoded = nativeGateway.generate(
                NativeGenerationRequest(
                    NativeGenerationEngine.MNN,
                    payload, modelFiles.directory(id, activationId).path
                )
            )
            insertGenerationResult(
                (payload to encoded).mapLocalDiffusionToAiGenResult().withModelName(model.name)
            )
        }
    }


    /**
     * Cancels the admitted foreground owner; worker cancellation uses its exact gateway identity.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun interruptGeneration() {
        nativeGateway.cancelActive()
    }

    private fun requireArm64() {
        check(
            Build.SUPPORTED_64_BIT_ABIS.orEmpty().contains("arm64-v8a")
        ) { "MNN requires Android arm64" }
    }

}
