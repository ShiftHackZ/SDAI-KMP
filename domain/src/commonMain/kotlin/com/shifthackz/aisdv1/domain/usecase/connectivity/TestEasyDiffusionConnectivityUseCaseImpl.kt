package com.shifthackz.aisdv1.domain.usecase.connectivity

import com.shifthackz.aisdv1.domain.repository.EasyDiffusionRepository

/** Interprets server lifecycle without suppressing HTTP or transport failures.
 * @param repository Strict candidate ping operation.
 * @author Dmitriy Moroz */
internal class TestEasyDiffusionConnectivityUseCaseImpl(private val repository: EasyDiffusionRepository) :
    TestEasyDiffusionConnectivityUseCase {

    /** Accepts ready, rendering or model-loading lifecycle states; startup/unknown states and transport failures remain visible.
     * @param url Captured HTTP(S) instance root or reverse-proxy prefix.
     * @author Dmitriy Moroz */
    override suspend fun invoke(url: String) {
        check(repository.ping(url) in setOf("Online", "Rendering", "LoadingModel")) {
            "Easy Diffusion is starting or unavailable; retry when ready"
        }
    }
}
