package com.shifthackz.aisdv1.domain.usecase.easydiffusion

import com.shifthackz.aisdv1.domain.entity.EasyDiffusionConnection
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.gateway.ProviderSelectionGuard
import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/** Serializes connection capture with provider replacement, retaining the submitted revision.
 * @param preferences Independent selected endpoint, checkpoint and durable revision.
 * @param selectionGuard Shared lock used by atomic configuration publication.
 * @author Dmitriy Moroz */
internal class CaptureEasyDiffusionConnectionUseCaseImpl(
    private val preferences: PreferenceManager,
    private val selectionGuard: ProviderSelectionGuard,
) : CaptureEasyDiffusionConnectionUseCase {
    /** Captures one consistent connection or fails before dispatch if the expected endpoint/revision was replaced.
     * @param expectedRevision Submitted revision preserved in the returned connection; null supports legacy payloads.
     * @param expectedUrl Optional catalog endpoint that must still match.
     * @author Dmitriy Moroz */
    override suspend fun invoke(
        expectedRevision: Long?,
        expectedUrl: String?
    ): EasyDiffusionConnection = selectionGuard.withSelection {
        val revision = preferences.providerSelectionRevision
        val url = preferences.easyDiffusionUrl
        val model = preferences.easyDiffusionModel
        check(
            preferences.source == ServerSource.EASY_DIFFUSION &&
                    (expectedRevision == null || expectedRevision == revision) &&
                    (expectedUrl == null || expectedUrl == url)
        ) { "Easy Diffusion connection changed" }
        EasyDiffusionConnection(url, model, expectedRevision ?: revision)
    }
}
