package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.data.core.CoreGenerationRepository
import com.shifthackz.aisdv1.data.image.decodedImageDimensions
import com.shifthackz.aisdv1.domain.datasource.GenerationResultDataSource
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionFrame
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionModel
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.EasyDiffusionTask
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.work.BackgroundWorkObserver
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.gateway.EasyDiffusionTaskRegistry
import com.shifthackz.aisdv1.domain.gateway.MediaStoreGateway
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository
import com.shifthackz.aisdv1.domain.datasource.EasyDiffusionRemoteDataSource
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.coroutineContext
import kotlin.io.encoding.Base64
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Maps strict Easy Diffusion protocol calls and preserves original encoded output bytes.
 * @param remote Strict protocol mapping without recovery policy.
 * @param preferences Independent endpoint/checkpoint preferences and source revision.
 * @param registry Current operation ownership, checked before result publication.
 * @param selectionGuard Serializes final result insertion with atomic provider replacement.
 * @param mediaStoreGateway Normal image export path.
 * @param localDataSource Gallery persistence shared with other providers.
 * @param backgroundWorkObserver Normal background/autosave policy.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalTime::class)
internal class EasyDiffusionRepositoryImpl(
    private val remote: EasyDiffusionRemoteDataSource,
    private val preferences: PreferenceManager,
    private val registry: EasyDiffusionTaskRegistry,
    private val selectionGuard: ProviderSelectionGuard,
    mediaStoreGateway: MediaStoreGateway,
    localDataSource: GenerationResultDataSource.Local,
    backgroundWorkObserver: BackgroundWorkObserver,
) : CoreGenerationRepository(
    mediaStoreGateway,
    localDataSource,
    preferences,
    backgroundWorkObserver
), EasyDiffusionRepository {
    private val mutex = Mutex()
    private val catalogs = mutableMapOf<String, List<EasyDiffusionModel>>()

    /** Requests readiness/liveness from the captured endpoint and propagates transport or decoding failures.
     * @param url Captured HTTP(S) instance root or reverse-proxy prefix.
     * @param sessionId Captured operation identity sent with liveness requests; null probes instance readiness only.
     * @author Dmitriy Moroz */

    override suspend fun ping(url: String, sessionId: String?): String = remote.ping(url, sessionId)

    /** Refreshes only the captured endpoint catalog, publishing the cache after a successful complete response.
     * @param url Captured HTTP(S) instance root or reverse-proxy prefix.
     * @author Dmitriy Moroz */

    override suspend fun fetchModels(url: String) {
        val models = remote.models(url)
        mutex.withLock { catalogs[url] = models }
    }

    /** Returns the successfully fetched catalog for this exact endpoint without consulting live preferences.
     * @param url Captured HTTP(S) instance root or reverse-proxy prefix.
     * @author Dmitriy Moroz */

    override suspend fun getModels(url: String): List<EasyDiffusionModel> =
        mutex.withLock { catalogs[url].orEmpty() }

    /** Submits one captured render request exactly once and returns its positive task identity.
     * @param request Immutable endpoint, model, supported generation inputs and original provider revision.
     * @author Dmitriy Moroz */

    override suspend fun submit(request: EasyDiffusionRequest): EasyDiffusionTask =
        remote.submit(request)

    /** Delivers decoded frames for the captured task without retrying or deciding recovery policy.
     * @param task Captured instance, session and positive remote task ID.
     * @param onFrame Receives decoded progress or terminal frames in transport order.
     * @author Dmitriy Moroz */

    override suspend fun readStream(
        task: EasyDiffusionTask,
        onFrame: suspend (EasyDiffusionFrame) -> Unit
    ) = remote.readStream(task, onFrame)

    /** Requests task-scoped stop for the captured positive task ID and propagates server failures.
     * @param task Captured instance, session and positive remote task ID.
     * @author Dmitriy Moroz */

    override suspend fun stop(task: EasyDiffusionTask) = remote.stop(task)

    /** Validates successful output and inserts original encoded bytes into the normal gallery while owner/revision guards remain valid.
     * @param request Immutable endpoint, model, supported generation inputs and original provider revision.
     * @param frame Complete successful task frame containing original encoded output and authoritative metadata.
     * @author Dmitriy Moroz */

    override suspend fun save(
        request: EasyDiffusionRequest,
        frame: EasyDiffusionFrame
    ): AiGenerationResult {
        check(frame.status == "succeeded") { "Easy Diffusion did not complete successfully" }
        val encoded = requireNotNull(frame.image) { "Easy Diffusion returned no image" }
        val raw = if (encoded.startsWith("data:image/")) encoded.substringAfter(
            ";base64,",
            ""
        ) else encoded
        val bytes = Base64.Default.decode(raw.filterNot(Char::isWhitespace))
        val (width, height) = decodedImageDimensions(bytes)
        val seed = requireNotNull(frame.seed) { "Easy Diffusion returned no seed" }
        check(seed in 0L..4294967295L) { "Easy Diffusion returned an invalid seed" }
        return selectionGuard.withSelection {
            registry.withActive(request.sessionId) {
                coroutineContext.ensureActive()
                check(
                    preferences.source == ServerSource.EASY_DIFFUSION &&
                            preferences.providerSelectionRevision == request.selectionRevision && preferences.easyDiffusionUrl == request.url
                ) { "Easy Diffusion generation was replaced" }
                insertGenerationResult(
                    AiGenerationResult(
                        id = 0L,
                        image = Base64.Default.encode(bytes),
                        inputImage = request.inputImage.orEmpty(),
                        createdAt = Clock.System.now().toEpochMilliseconds(),
                        type = if (request.inputImage == null) AiGenerationResult.Type.TEXT_TO_IMAGE else AiGenerationResult.Type.IMAGE_TO_IMAGE,
                        denoisingStrength = request.strength,
                        prompt = request.prompt,
                        negativePrompt = request.negativePrompt,
                        width = width,
                        height = height,
                        samplingSteps = request.steps,
                        cfgScale = request.cfgScale,
                        restoreFaces = false,
                        sampler = "ddim",
                        seed = seed.toString(),
                        subSeed = "",
                        subSeedStrength = 0f,
                        hidden = false,
                        modelName = frame.model?.takeIf(String::isNotBlank) ?: request.model,
                    )
                )
            }
        }
    }

}
