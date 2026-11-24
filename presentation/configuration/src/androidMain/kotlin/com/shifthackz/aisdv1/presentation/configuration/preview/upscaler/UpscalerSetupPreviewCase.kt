package com.shifthackz.aisdv1.presentation.configuration.preview.upscaler

import com.shifthackz.aisdv1.presentation.configuration.upscaler.UpscalerSetupState

/**
 * One wizard, discovery sheet or selectable provider-row snapshot.
 *
 * @param name Label shown by the preview picker.
 * @param state Captured configuration, discovery and download state.
 * @param kind Production renderer displayed for this snapshot.
 * @param isSelected Whether an isolated provider row has the selection outline.
 * @author Dmitriy Moroz
 */
internal data class UpscalerSetupPreviewCase(
    val name: String,
    val state: UpscalerSetupState,
    val kind: Kind = Kind.SCREEN,
    val isSelected: Boolean = true,
) {
    /**
     * Configuration surfaces rendered independently of application state owners.
     *
     * @author Dmitriy Moroz
     */
    enum class Kind { SCREEN, FILTERS, SORT, PROVIDER }
}
