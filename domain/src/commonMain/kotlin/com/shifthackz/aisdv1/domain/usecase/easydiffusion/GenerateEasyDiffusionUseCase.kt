package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionRequest
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/** Runs a captured request as sequential tasks under one deadline and ownership token.
 * @author Dmitriy Moroz */
interface GenerateEasyDiffusionUseCase {
    /**
     * Submits each image once, reopens only its stream and suppresses cancelled results.
     * @param request Immutable endpoint, model, controls and source revision captured at dispatch.
     * @return Successful images in order; errors stop subsequent submissions.
     * @author Dmitriy Moroz */
    suspend operator fun invoke(request: EasyDiffusionRequest): List<AiGenerationResult>
}
