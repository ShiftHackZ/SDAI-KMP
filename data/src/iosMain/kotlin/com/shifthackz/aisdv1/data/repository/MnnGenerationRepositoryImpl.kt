package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.mappers.mapLocalDiffusionToAiGenResult
import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.local.deliverMnnSelection
import com.shifthackz.aisdv1.data.mappers.withModelName
import com.shifthackz.aisdv1.domain.datasource.DownloadableModelDataSource
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.TextToImagePayload
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.gateway.MnnBackendPolicy
import com.shifthackz.aisdv1.domain.gateway.MnnModelSelectionLease
import com.shifthackz.aisdv1.domain.gateway.MnnModelStore
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.MnnGenerationRepository
import com.shifthackz.aisdv1.domain.validator.MnnBundleValidator
import com.shifthackz.aisdv1.feature.mnn.MnnRuntime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Owns catalog selection and complete gallery persistence while native inference drains in-process.
 *
 * @param mediaStoreGateway Normal image export flow.
 * @param localDataSource Persistent gallery storage.
 * @param backgroundWorkObserver Existing background result ownership policy.
 * @param preferences Captured selection guard.
 * @param runtime Injected asynchronous native execution with cancellation drain.
 * @param models Catalog metadata and completion state.
 * @param modelFiles Durable immutable bundle roots and leases.
 * @param validator Shared full integrity checker.
 * @param backendPolicy Platform-specific requested backend admission.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalEncodingApi::class)
internal class MnnGenerationRepositoryImpl(
    mediaStoreGateway: MediaStoreGateway,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
    private val preferences: PreferenceManager,
    private val runtime: MnnRuntime,
    private val models: DownloadableModelDataSource.Local,
    private val modelFiles: MnnModelStore,
    private val validator: MnnBundleValidator,
    private val backendPolicy: MnnBackendPolicy
) : CoreGenerationRepository(
    mediaStoreGateway,
    localDataSource,
    preferences,
    backgroundWorkObserver
),
    MnnGenerationRepository {
    private val admission = Mutex()
    private val status = MutableSharedFlow<LocalDiffusionStatus>(extraBufferCapacity = 32)

    /**
     * Observes overall native percentages; values do not represent denoising steps.
     *
     * @author Dmitriy Moroz
     */
    override fun observeStatus() = status.asSharedFlow()

    /**
     * Validates a catalog selection without reading an irrelevant external-folder preference.
     *
     * @param modelId Catalog identity; custom models are unsupported on iOS.
     * @param sourcePath External Android custom path, ignored for catalog models.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun validateModel(
        modelId: String,
        sourcePath: String
    ): MnnModelSelectionLease {
        return validateOwnedModel(modelId, "")
    }

    /**
     * Retains selection and payload leases after full immutable bundle validation.
     *
     * @param modelId Catalog identity to retain.
     * @param activationId Expected manifest digest, or empty for a new selection.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun validateOwnedModel(
        modelId: String,
        activationId: String
    ): MnnModelSelectionLease =
        deliverMnnSelection(Dispatchers.Default) {
            check(models.getById(modelId).type == LocalAiModel.Type.Mnn)
            val selection = modelFiles.acquireSelection(modelId)
            try {
                val payload = modelFiles.acquire(modelId)
                try {
                    val manifest = validator(modelFiles.path(modelId))
                    check(activationId.isEmpty() || activationId == manifest.activationId) { "MNN artifact changed" }
                    object : MnnModelSelectionLease {
                        override val activationId = manifest.activationId
                        private var isClosed = false
                        override fun close() {
                            if (!isClosed) {
                                isClosed = true
                                selection.use { payload.close() }
                            }
                        }
                    }
                } catch (failure: Throwable) {
                    try {
                        payload.close()
                    } catch (cleanup: Throwable) {
                        failure.addSuppressed(cleanup)
                    }
                    throw failure
                }
            } catch (failure: Throwable) {
                try {
                    selection.close()
                } catch (cleanup: Throwable) {
                    failure.addSuppressed(cleanup)
                }
                throw failure
            }
        }

    /**
     * Serializes immutable input validation, native drain and complete gallery persistence.
     *
     * @param payload Captured supported profile, backend, model identity and resolved seed.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun generateFromText(payload: TextToImagePayload) = admission.withLock {
        withContext(Dispatchers.Default) {
            require(payload.mnnBackend in backendPolicy.supportedBackends)
            require(payload.width == 512 && payload.height == 512 && payload.cfgScale == 7.5f)
            require(payload.batchCount == 1 && payload.negativePrompt.isEmpty() && payload.sampler == "PNDM")
            require(payload.samplingSteps in 1..50 && payload.seed.toLongOrNull() in 0..Int.MAX_VALUE.toLong())
            val id = requireNotNull(payload.mnnModelId)
            val identity = requireNotNull(payload.mnnActivationId)
            check(id == preferences.localMnnModelId && identity == preferences.localMnnActivationId)
            val model = models.getById(id)
            check(model.type == LocalAiModel.Type.Mnn && model.downloaded)
            modelFiles.acquire(id, identity).use {
                check(validator(modelFiles.path(id)).activationId == identity) { "MNN artifact changed" }
                currentCoroutineContext().ensureActive()
                val bytes = runtime.generate(
                    NativeGenerationRequest(
                        NativeGenerationEngine.MNN, payload,
                        modelFiles.path(id)
                    )
                ) { percentage -> status.tryEmit(LocalDiffusionStatus(percentage, 100)) }
                currentCoroutineContext().ensureActive()
                insertGenerationResult(
                    (payload to Base64.encode(bytes)).mapLocalDiffusionToAiGenResult()
                        .withModelName("${model.name} (${payload.mnnBackend.name})")
                )
            }
        }
    }

    /**
     * Requests cooperative cancellation of the current foreground owner without releasing leases.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun interruptGeneration() = runtime.interrupt()

    /**
     * Cancels only the captured worker owner; stale requests cannot affect a successor.
     *
     * @param ownerId Immutable worker identity captured before generation starts.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun interruptOwnedGeneration(ownerId: String) = runtime.interrupt(ownerId)
}
