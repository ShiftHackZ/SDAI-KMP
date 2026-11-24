package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import com.shifthackz.aisdv1.presentation.generation.upscaler.UpscalerFormState

/**
 * One provider, disclosure, switch, dimensions or shared-form snapshot.
 *
 * @param name Preview picker label describing the visual state.
 * @param kind Component renderer selected for this snapshot.
 * @param formState Model catalog, platform branding and configuration feedback.
 * @param configuration Captured model and scale; null disables the optional pipeline.
 * @param outputSize Predicted output dimensions, absent before choosing an input.
 * @param isEnabled Whether component controls accept edits.
 * @param isExpanded Whether the parameters disclosure is open.
 * @param isStandalone Whether the mandatory standalone parameter layout is shown.
 * @author Dmitriy Moroz
 */
internal data class UpscalerComponentPreviewCase(
    val name: String,
    val kind: Kind = Kind.PARAMETERS,
    val formState: UpscalerFormState = UpscalerPreviewFixtures.form,
    val configuration: UpscalerConfiguration? = UpscalerPreviewFixtures.configuration,
    val outputSize: UpscalerImageSize? = UpscalerImageSize(2048, 2048),
    val isEnabled: Boolean = true,
    val isExpanded: Boolean = true,
    val isStandalone: Boolean = true,
) {
    /**
     * Selects a production component without binding its state owner.
     *
     * @author Dmitriy Moroz
     */
    enum class Kind { PARAMETERS, FORM, PROVIDER, ENABLE, OUTPUT }
}
