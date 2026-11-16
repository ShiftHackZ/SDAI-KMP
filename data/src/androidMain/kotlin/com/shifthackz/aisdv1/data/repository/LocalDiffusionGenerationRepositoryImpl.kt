package com.shifthackz.aisdv1.data.repository

import android.os.Build
import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.local.DownloadableModelFileStore
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
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.LocalDiffusionGenerationRepository

/**
 * Maps the isolated PNG result directly to application/gallery models without a host bitmap.
 *
 * @param mediaStoreGateway Existing original export gateway.
 * @param localDataSource Existing gallery writer.
 * @param backgroundWorkObserver Work persistence policy.
 * @param preferenceManager Model configuration captured before suspension.
 * @param nativeGateway Sole native attempt owner.
 * @param downloadableLocalDataSource Captured model's metadata.
 * @param modelFileStore Model location resolver.
 * @author Dmitriy Moroz
 */
internal class LocalDiffusionGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    private val preferenceManager: PreferenceManager,
    private val nativeGateway: NativeGenerationGateway,
    private val downloadableLocalDataSource: DownloadableModelDataSource.Local,
    private val modelFileStore: DownloadableModelFileStore,
) : CoreGenerationRepository(
    mediaStoreGateway,
    localDataSource,
    preferenceManager,
    backgroundWorkObserver
), LocalDiffusionGenerationRepository {
    /**
     * Observes child progress through the host transport.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus() = nativeGateway.observeStatus(NativeGenerationEngine.ONNX)

    /**
     * Signals cancellation without closing an executing session.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun interruptGeneration() {
        nativeGateway.cancelActive()
    }

    /**
     * Captures model configuration once, then preserves the existing result and persistence mapping.
     * Android below 8.1 (API 27) always submits NNAPI as disabled, even if a saved preference is true;
     * those devices generate through the ONNX Runtime CPU backend.
     *
     * @param payload Immutable generation parameters.
     * @author Dmitriy Moroz
     */
    override suspend fun generateFromText(payload: TextToImagePayload) = run {
        val id = preferenceManager.localOnnxModelId
        val customPath = preferenceManager.localOnnxCustomModelPath
        val isNnapiEnabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 &&
                preferenceManager.localOnnxUseNNAPI
        val model = downloadableLocalDataSource.getById(id)
        check(model.downloaded) { "Model not downloaded." }
        val modelPath =
            if (id == LocalAiModel.CustomOnnx.id) customPath else modelFileStore.resolvePath(model)
        val modelName = model.name.ifBlank { id }
        val encoded = nativeGateway.generate(
            NativeGenerationRequest(
                NativeGenerationEngine.ONNX,
                payload,
                modelPath,
                isNnapiEnabled
            )
        )
        insertGenerationResult(
            (payload to encoded).mapLocalDiffusionToAiGenResult().withModelName(modelName)
        )
    }
}
