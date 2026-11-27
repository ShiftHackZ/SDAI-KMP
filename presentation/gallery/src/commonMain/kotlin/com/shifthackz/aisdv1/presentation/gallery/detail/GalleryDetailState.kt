package com.shifthackz.aisdv1.presentation.gallery.detail

import kotlin.uuid.Uuid
import kotlin.uuid.ExperimentalUuidApi
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.ImageBitmap
import com.shifthackz.aisdv1.core.localization.formatter.formatGenerationDuration
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.shortDisplayName
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult

/**
 * Current gallery detail metadata and a bounded neighboring-page window.
 *
 * @param loading Whether the requested detail entry is loading.
 * @param tabs Available output/original/info tabs for the current generation type.
 * @param selectedTab Tab currently displayed in the detail renderer.
 * @param galleryItemIds Ordered persistent identities used for adjacent-item navigation.
 * @param dialog Current confirmation or error overlay.
 * @param content Metadata of the currently selected detail item.
 * @param pagerContents Bounded metadata window around the current detail item.
 * @param pagerContentStartIndex Global gallery index represented by the first window entry.
 * @param pagerCurrentIndex Global gallery index of the currently displayed item.
 * @author Dmitriy Moroz
 */
@Immutable
data class GalleryDetailState(
    val loading: Boolean = true,
    val tabs: List<GalleryDetailTab> = emptyList(),
    val selectedTab: GalleryDetailTab = GalleryDetailTab.IMAGE,
    val galleryItemIds: List<Long> = emptyList(),
    val dialog: GalleryDetailDialog = GalleryDetailDialog.None,
    val content: GalleryDetailContent? = null,
    val pagerContents: List<GalleryDetailContent> = emptyList(),
    val pagerContentStartIndex: Int = 0,
    val pagerCurrentIndex: Int = 0,
) : MviState

/**
 * Formatted generation metadata and optional transient image content for one detail page.
 * Persisted entries may contain metadata only; original bytes are resolved on explicit actions.
 *
 * @param showReportButton Whether reporting is available in this distribution.
 * @param generationType Underlying text-to-image or image-to-image generation kind.
 * @param id Persistent identity, or transient identity for the last cached result.
 * @param imageBase64 Optional retained encoded output for a transient result; may be blank for metadata-only pages.
 * @param imageRevision Opaque content revision used by bounded display loaders.
 * @param image Optional already decoded output preview.
 * @param inputImageBase64 Optional retained encoded source for a transient image-to-image result.
 * @param inputImage Optional already decoded source preview.
 * @param createdAt Formatted creation timestamp for display.
 * @param type Serialized generation-kind label displayed/exported as metadata.
 * @param modelName Recorded checkpoint/model display name.
 * @param upscalerModelName Dedicated postprocessing checkpoint, absent for original output.
 * @param civitaiUrl Public Civitai post URL displayed alongside the recorded generation metadata.
 * @param provider Recorded provider display name; null for legacy images.
 * @param acceleration Recorded execution configuration; null when unavailable.
 * @param generationDuration Formatted elapsed time; null for legacy images.
 * @param deviceName Originating device vendor and model; null for legacy images.
 * @param osVersion Originating OS release and Android API level; null for legacy images.
 * @param prompt Recorded positive prompt.
 * @param negativePrompt Recorded negative prompt.
 * @param size Formatted output dimensions.
 * @param samplingSteps Formatted sampling step count.
 * @param cfgScale Formatted classifier-free guidance scale.
 * @param restoreFaces Localized face-restoration setting.
 * @param sampler Recorded provider sampler identifier.
 * @param seed Recorded primary seed text.
 * @param subSeed Recorded variation seed text.
 * @param subSeedStrength Formatted variation-seed influence.
 * @param denoisingStrength Formatted image-to-image denoising strength.
 * @param hidden Whether preview pixels should be obscured.
 * @param liked Whether the generation is a favorite.
 * @author Dmitriy Moroz
 */
@Immutable
data class GalleryDetailContent(
    val showReportButton: Boolean = false,
    val generationType: AiGenerationResult.Type,
    val id: Long,
    val imageBase64: String,
    val imageRevision: String = "",
    val image: ImageBitmap?,
    val inputImageBase64: String?,
    val inputImage: ImageBitmap?,
    val createdAt: String,
    val type: String,
    val modelName: String = "",
    val civitaiUrl: String? = null,
    val upscalerModelName: String? = null,
    val provider: String? = null,
    val acceleration: String? = null,
    val generationDuration: String? = null,
    val deviceName: String? = null,
    val osVersion: String? = null,
    val prompt: String,
    val negativePrompt: String,
    val size: String,
    val samplingSteps: String,
    val cfgScale: String,
    val restoreFaces: String,
    val sampler: String,
    val seed: String,
    val subSeed: String,
    val subSeedStrength: String,
    val denoisingStrength: String,
    val hidden: Boolean,
    val liked: Boolean = false,
)

/**
 * Delete confirmation or public operation failure displayed over gallery detail.
 *
 * @author Dmitriy Moroz
 */
@Immutable
sealed interface GalleryDetailDialog {

    data object None : GalleryDetailDialog

    data object DeleteConfirm : GalleryDetailDialog

    /**
     * A gallery-detail operation failed with public details.
     *
     * @param message User-visible failure explanation.
     * @author Dmitriy Moroz
     */
    data class Error(val message: String) : GalleryDetailDialog
}

/**
 * Detail sections showing generated output, original source or generation metadata.
 *
 * @author Dmitriy Moroz
 */
enum class GalleryDetailTab {
    IMAGE,
    ORIGINAL,
    INFO;

    companion object {
        /**
         * Selects the tabs available for a generation kind.
         *
         * @param type Recorded text-to-image or image-to-image generation kind.
         * @return Output/info tabs, plus original-source tab for image-to-image.
         * @author Dmitriy Moroz
         */
        fun consume(type: AiGenerationResult.Type): List<GalleryDetailTab> = when (type) {
            AiGenerationResult.Type.TEXT_TO_IMAGE -> listOf(IMAGE, INFO)
            AiGenerationResult.Type.IMAGE_TO_IMAGE, AiGenerationResult.Type.UPSCALE -> entries
        }
    }
}

/**
 * Formats a transient generation result for detail without eagerly decoding original pixels.
 *
 * @param showReportButton Whether the current distribution exposes reporting.
 * @receiver Generation result and its encoded images.
 * @return Display metadata retaining transient original bytes for explicit actions.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalUuidApi::class)
internal fun AiGenerationResult.toGalleryDetailContent(
    showReportButton: Boolean,
): GalleryDetailContent =
    GalleryDetailContent(
        showReportButton = showReportButton,
        generationType = type,
        id = id,
        imageBase64 = image,
        imageRevision = imageRevision.ifBlank { Uuid.random().toString() },
        image = null,
        inputImageBase64 = inputImage.takeIf { type != AiGenerationResult.Type.TEXT_TO_IMAGE },
        inputImage = null,
        createdAt = formatGalleryCreatedAt(createdAt),
        type = type.key,
        modelName = modelName,
        civitaiUrl = civitaiUrl,
        upscalerModelName = upscalerModelName,
        provider = providerKey?.let { key ->
            ServerSource.entries.find { it.key == key }?.shortDisplayName() ?: key
        },
        acceleration = acceleration?.let { if (it == "None") Localization.string("gallery_info_acceleration_none") else it },
        generationDuration = generationDurationMillis?.let(::formatGenerationDuration),
        deviceName = deviceName,
        osVersion = osVersion,
        prompt = prompt,
        negativePrompt = negativePrompt,
        size = "$width X $height",
        samplingSteps = samplingSteps.toString(),
        cfgScale = cfgScale.toString(),
        restoreFaces = Localization.string(if (restoreFaces) "yes" else "no"),
        sampler = sampler,
        seed = seed,
        subSeed = subSeed,
        subSeedStrength = subSeedStrength.toString(),
        denoisingStrength = denoisingStrength.toString(),
        hidden = hidden,
        liked = liked,
    )

/**
 * Formats the current generation parameters into localized text for copy/share operations.
 *
 * @receiver Current detail metadata.
 * @return Localized lines containing the recorded prompt and main generation settings.
 * @author Dmitriy Moroz
 */
internal fun GalleryDetailContent.paramsText(): String = buildString {
    appendLine("${Localization.string("gallery_info_field_type")}: $type")

    provider?.let { appendLine("${Localization.string("gallery_info_field_provider")}: $it") }
    upscalerModelName?.let { appendLine("${Localization.string("upscaler_model")}: $it") }
    if (generationType != AiGenerationResult.Type.UPSCALE && modelName.isNotBlank()) {
        appendLine("${Localization.string("gallery_info_field_model")}: $modelName")
    }
    acceleration?.let { appendLine("${Localization.string("gallery_info_field_acceleration")}: $it") }
    deviceName?.let { appendLine("${Localization.string("gallery_info_field_device")}: $it") }
    osVersion?.let { appendLine("${Localization.string("gallery_info_field_os")}: $it") }
    generationDuration?.let { appendLine("${Localization.string("gallery_info_field_duration")}: $it") }

    if (generationType == AiGenerationResult.Type.UPSCALE) {
        appendLine("${Localization.string("gallery_info_field_size")}: $size")
        return@buildString
    }

    if (prompt.isNotEmpty()) {
        appendLine("${Localization.string("gallery_info_field_prompt")}: $prompt")
    }

    if (negativePrompt.isNotEmpty()) {
        appendLine("${Localization.string("gallery_info_field_negative_prompt")}: $negativePrompt")
    }

    appendLine("${Localization.string("gallery_info_field_size")}: $size")
    appendLine("${Localization.string("gallery_info_field_sampler")}: $sampler")
    appendLine("${Localization.string("gallery_info_field_sampling_steps")}: $samplingSteps")
    appendLine("${Localization.string("gallery_info_field_cfg")}: $cfgScale")
    appendLine("${Localization.string("gallery_info_field_restore_faces")}: $restoreFaces")
    appendLine("${Localization.string("gallery_info_field_seed")}: $seed")
}.trim()
