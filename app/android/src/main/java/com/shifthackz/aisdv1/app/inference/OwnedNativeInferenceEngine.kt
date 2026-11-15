package com.shifthackz.aisdv1.app.inference

import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import org.koin.core.KoinApplication

/**
 * Owns both a child runtime and its independent graph; graph teardown never precedes native exit.
 *
 * @param engine Job-scoped injected runtime.
 * @param graph Composition-root graph that created the engine.
 * @author Dmitriy Moroz
 */
internal class OwnedNativeInferenceEngine(
    private val engine: NativeInferenceEngine,
    private val graph: KoinApplication,
) : NativeInferenceEngine {

    private var isClosed = false

    /**
     * Delegates generation to the job-scoped runtime while retaining its graph until explicit close.
     *
     * @param request Immutable generation inputs.
     * @param onProgress Nonblocking step callback.
     * @param onStage Sanitized stage callback.
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit
    ): ByteArray =
        engine.generate(request, onProgress, onStage)

    /**
     * Signals cooperative termination only.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt() = engine.interrupt()

    /**
     * Disposes collaborators once, on their inference owner thread.
     *
     * @author Dmitriy Moroz
     */
    override fun close() {
        if (!isClosed) {
            isClosed = true
            try {
                engine.close()
            } finally {
                graph.close()
            }
        }
    }
}
