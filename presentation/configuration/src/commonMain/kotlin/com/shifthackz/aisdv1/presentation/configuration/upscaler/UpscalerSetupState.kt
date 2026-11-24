package com.shifthackz.aisdv1.presentation.configuration.upscaler

import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ModelDownloadBatch
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus

/**
 * Dedicated configuration draft; generation provider preferences are never edited by this screen.
 *
 * @param platform Current host used for local-provider branding.
 * @param step Active page of the shared configuration wizard.
 * @param models Dedicated checkpoint catalog with current completion state.
 * @param selectedId Draft selection committed only by the setup action.
 * @param downloads Active or terminal upscaler transfers.
 * @param isLoading Whether catalog or configuration work is in progress.
 * @param catalogStatus Catalog refresh outcome presented beside model choices with retry.
 * @param source Provider selected in the configuration draft.
 * @param discovery Search, filtering and sheet state retained across wizard steps.
 * @param error Operation failure awaiting acknowledgement or retry.
 * @param pendingDeleteId Model awaiting destructive confirmation.
 * @author Dmitriy Moroz
 */
internal data class UpscalerSetupState(
    val platform: Platform,
    val step: ServerSetupState.Step = ServerSetupState.Step.SOURCE,
    val models: List<LocalAiModel> = emptyList(),
    val selectedId: String = "",
    val downloads: List<ModelDownloadBatch> = emptyList(),
    val isLoading: Boolean = true,
    val catalogStatus: LocalCatalogStatus = LocalCatalogStatus.LOADING,
    val source: UpscalerSource = UpscalerSource.availableOn(platform).first(),
    val discovery: UpscalerDiscoveryState = UpscalerDiscoveryState(),
    val error: String? = null,
    val pendingDeleteId: String? = null,
) : MviState {
    /**
     * Provider choices derived from their declared platform availability.
     *
     * @author Dmitriy Moroz
     */
    val sources: List<UpscalerSource> get() = UpscalerSource.availableOn(platform)

    /**
     * Whether the selected provider belongs to the host's available choices.
     *
     * @author Dmitriy Moroz
     */
    val isSupported: Boolean get() = source in sources
}
