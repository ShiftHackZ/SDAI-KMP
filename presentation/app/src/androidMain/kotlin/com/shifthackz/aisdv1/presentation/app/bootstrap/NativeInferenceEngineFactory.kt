package com.shifthackz.aisdv1.presentation.app.bootstrap

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine

/**
 * Composition-root factory for an independent, child-only native job graph.
 *
 * @author Dmitriy Moroz
 */
interface NativeInferenceEngineFactory {
    /**
     * Creates a child-only runtime and its independent dependency graph.
     * The caller owns the returned engine and must close it after native execution returns.
     *
     * @param request Immutable job configuration used to construct runtime collaborators.
     * @author Dmitriy Moroz
     */
    fun create(request: NativeGenerationRequest): NativeInferenceEngine
}
