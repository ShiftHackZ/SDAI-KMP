package com.shifthackz.aisdv1.presentation.generation.preview.upscaler

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import com.shifthackz.aisdv1.presentation.generation.preview.upscaler.UpscalerComponentPreviewCase.Kind

/**
 * Enumerates reusable controls, including collapsed, disabled, unconfigured and error states.
 *
 * @author Dmitriy Moroz
 */
internal class UpscalerComponentPreviewParameterProvider :
    PreviewParameterProvider<UpscalerComponentPreviewCase> {
    private val form = UpscalerPreviewFixtures.form
    private val configuration = UpscalerPreviewFixtures.configuration
    private val mnn = UpscalerPreviewFixtures.mnn
    private val mnnForm = form.copy(platform = Platform.IOS, model = mnn, models = listOf(mnn))
    private val cases = listOf(
        UpscalerComponentPreviewCase("Parameters · x4"),
        UpscalerComponentPreviewCase(
            "Parameters · x2",
            configuration = configuration.copy(scaleFactor = 2),
            outputSize = UpscalerImageSize(1024, 1024)
        ),
        UpscalerComponentPreviewCase("Parameters · Collapsed", isExpanded = false),
        UpscalerComponentPreviewCase("Parameters · Disabled", isEnabled = false),
        UpscalerComponentPreviewCase(
            "Parameters · No downloaded models",
            formState = form.copy(
                model = null,
                models = form.models.map { it.copy(downloaded = false) }),
            configuration = null,
            outputSize = null
        ),
        UpscalerComponentPreviewCase(
            "Parameters · MNN",
            formState = mnnForm,
            configuration = UpscalerConfiguration(mnn.id)
        ),
        UpscalerComponentPreviewCase(
            "Parameters · Long model name",
            formState = form.copy(
                model = form.model?.copy(name = "Real-ESRGAN detailed illustration restoration and super-resolution model"),
                models = form.models.map { it.copy(name = "Real-ESRGAN detailed illustration restoration and super-resolution model") })
        ),
        UpscalerComponentPreviewCase("Parameters · Pipeline", isStandalone = false),
        UpscalerComponentPreviewCase("Form · Standalone", Kind.FORM),
        UpscalerComponentPreviewCase(
            "Form · Pipeline off",
            Kind.FORM,
            configuration = null,
            isStandalone = false
        ),
        UpscalerComponentPreviewCase("Form · Pipeline on", Kind.FORM, isStandalone = false),
        UpscalerComponentPreviewCase(
            "Form · Disabled pipeline",
            Kind.FORM,
            isEnabled = false,
            isStandalone = false
        ),
        UpscalerComponentPreviewCase(
            "Form · Loading",
            Kind.FORM,
            formState = form.copy(isLoading = true, model = null, models = emptyList()),
            configuration = null
        ),
        UpscalerComponentPreviewCase(
            "Form · Setup required",
            Kind.FORM,
            formState = form.copy(model = null, models = emptyList()),
            configuration = null
        ),
        UpscalerComponentPreviewCase(
            "Form · Catalog error",
            Kind.FORM,
            formState = form.copy(error = "The model catalog could not be loaded. Open configuration to retry.")
        ),
        UpscalerComponentPreviewCase("Provider · ESRGAN", Kind.PROVIDER),
        UpscalerComponentPreviewCase(
            "Provider · MNN Android",
            Kind.PROVIDER,
            formState = mnnForm.copy(platform = Platform.ANDROID),
            configuration = UpscalerConfiguration(mnn.id)
        ),
        UpscalerComponentPreviewCase(
            "Provider · MNN iOS metadata",
            Kind.PROVIDER,
            formState = mnnForm,
            configuration = UpscalerConfiguration(mnn.id)
        ),
        UpscalerComponentPreviewCase("Provider · Unavailable", Kind.PROVIDER, configuration = null),
        UpscalerComponentPreviewCase("Enable · Off", Kind.ENABLE, configuration = null),
        UpscalerComponentPreviewCase("Enable · On", Kind.ENABLE),
        UpscalerComponentPreviewCase(
            "Enable · Disabled off",
            Kind.ENABLE,
            configuration = null,
            isEnabled = false
        ),
        UpscalerComponentPreviewCase("Enable · Disabled on", Kind.ENABLE, isEnabled = false),
        UpscalerComponentPreviewCase("Output · Unknown", Kind.OUTPUT, outputSize = null),
        UpscalerComponentPreviewCase(
            "Output · Square 4096",
            Kind.OUTPUT,
            outputSize = UpscalerImageSize(4096, 4096)
        ),
        UpscalerComponentPreviewCase(
            "Output · Portrait",
            Kind.OUTPUT,
            outputSize = UpscalerImageSize(3072, 4096)
        ),
        UpscalerComponentPreviewCase(
            "Output · Landscape",
            Kind.OUTPUT,
            outputSize = UpscalerImageSize(4096, 2048)
        ),
    )

    /**
     * Named component states using inert model and scale callbacks.
     *
     * @author Dmitriy Moroz
     */
    override val values = cases.asSequence()

    /**
     * Identifies the component and its visible configuration in the picker.
     *
     * @param index Position in the ordered component-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
