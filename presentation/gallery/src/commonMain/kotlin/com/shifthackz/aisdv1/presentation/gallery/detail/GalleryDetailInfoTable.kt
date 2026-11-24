@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import com.shifthackz.aisdv1.domain.entity.isCivitaiPostUrl
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarContentInset
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarScrollObserver
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.AiGenerationResult
import com.shifthackz.aisdv1.presentation.components.widget.scrollbar.verticalScrollbar


/**
 * Displays recorded image-operation metadata as alternating rows with per-value clipboard actions.
 * Standalone upscales omit prompt, diffusion-model and sampling-only fields.
 *
 * @param modifier Layout modifiers for the scrollable metadata table.
 * @param content Already formatted generation metadata.
 * @param onCopyTextClick Requests native copying of the activated nonblank value.
 * @param scrollState Retained vertical position within the metadata table.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailsTable(
    modifier: Modifier = Modifier,
    content: GalleryDetailContent,
    onCopyTextClick: (String) -> Unit = {},
    scrollState: ScrollState = rememberScrollState(),
) {
    ToolbarScrollObserver(scrollState.canScrollBackward)
    Column(
        modifier = modifier
            .verticalScrollbar(scrollState, topInset = LocalToolbarContentInset.current)
            .verticalScroll(scrollState)
            .padding(top = LocalToolbarContentInset.current)
            .fillMaxSize(),
    ) {
        val colorOddBg = MaterialTheme.colorScheme.surface
        val colorEvenBg = MaterialTheme.colorScheme.surfaceTint
        val colorText = MaterialTheme.colorScheme.onSurface
        content.detailRows().forEachIndexed { index, row ->
            GalleryDetailRow(
                modifier = Modifier.background(
                    color = if (index % 2 == 0) colorOddBg else colorEvenBg,
                ),
                name = row.name,
                value = row.value,
                color = colorText,
                onCopyTextClick = onCopyTextClick,
                isLink = row.isLink,
            )
        }
    }
}

/**
 * Shows one named metadata value, reserving the clipboard-action width on every row so values align.
 *
 * @param modifier Background and placement of the table row.
 * @param column1Weight Relative width allocated to the parameter name.
 * @param column2Weight Relative width allocated to the parameter value.
 * @param name Localized parameter label.
 * @param value Recorded value; empty and whitespace-only values display a dash without a copy action.
 * @param color Text color for both cells.
 * @param onCopyTextClick Receives the nonblank value when its copy action is activated.
 * @param isLink Whether the value is a canonical public Civitai post link.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailRow(
    modifier: Modifier = Modifier,
    column1Weight: Float = 0.4f,
    column2Weight: Float = 0.6f,
    name: String,
    value: String,
    color: Color,
    onCopyTextClick: (String) -> Unit = {},
    isLink: Boolean = false,
) {
    Row(modifier) {
        GalleryDetailCell(
            text = name,
            modifier = Modifier.weight(column1Weight),
            color = color,
        )
        GalleryDetailCell(
            text = value.ifBlank { "-" },
            isLink = isLink,
            modifier = Modifier.weight(column2Weight),
            color = color,
        )
        if (value.isNotBlank()) {
            IconButton(
                onClick = { onCopyTextClick(value) },
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = Localization.string("action_copy"),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        } else {
            Spacer(Modifier.size(48.dp))
        }
    }
}

/**
 * Renders one padded metadata-table cell.
 *
 * @param modifier Column sizing and placement supplied by the row.
 * @param text Already localized/formatted cell text.
 * @param color Foreground color for the cell text.
 * @param isLink Whether the cell should expose a native browser link rather than ordinary text.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailCell(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    isLink: Boolean = false,
) {
    val linkStyle = TextLinkStyles(
        SpanStyle(
            color = MaterialTheme.colorScheme.primary,
            textDecoration = TextDecoration.Underline,
        )
    )
    Text(
        modifier = modifier
            .padding(start = 12.dp)
            .padding(vertical = 8.dp),
        text = buildAnnotatedString {
            if (isLink && text.isCivitaiPostUrl()) {
                withLink(LinkAnnotation.Url(text, linkStyle)) { append(text) }
            } else append(text)
        },
        color = color,
    )
}

private fun GalleryDetailContent.detailRows(): List<GalleryDetailField> =
    buildList {
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_date"),
                value = createdAt,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_type"),
                value = type,
            )
        )
        add(
            GalleryDetailField(
                Localization.string("gallery_info_field_provider"),
                provider.orEmpty()
            )
        )
        upscalerModelName?.let { model ->
            add(GalleryDetailField(Localization.string("upscaler_model"), model))
        }
        civitaiUrl?.let {
            add(
                GalleryDetailField(
                    Localization.string("civitai_title"),
                    it,
                    isLink = it.isCivitaiPostUrl()
                )
            )
        }
        if (generationType != AiGenerationResult.Type.UPSCALE) {
            add(GalleryDetailField(Localization.string("gallery_info_field_model"), modelName))
        }
        add(
            GalleryDetailField(
                Localization.string("gallery_info_field_acceleration"),
                acceleration.orEmpty()
            )
        )
        add(
            GalleryDetailField(
                Localization.string("gallery_info_field_duration"),
                generationDuration.orEmpty()
            )
        )
        add(
            GalleryDetailField(
                Localization.string("gallery_info_field_device"),
                deviceName.orEmpty()
            )
        )
        add(GalleryDetailField(Localization.string("gallery_info_field_os"), osVersion.orEmpty()))
        if (generationType == AiGenerationResult.Type.UPSCALE) {
            add(
                GalleryDetailField(
                    Localization.string("gallery_info_field_size"),
                    this@detailRows.size
                )
            )
            return@buildList
        }
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_prompt"),
                value = prompt,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_negative_prompt"),
                value = negativePrompt,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_size"),
                value = this@detailRows.size,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_sampling_steps"),
                value = samplingSteps,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_cfg"),
                value = cfgScale,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_restore_faces"),
                value = restoreFaces,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_sampler"),
                value = sampler,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_seed"),
                value = seed,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_sub_seed"),
                value = subSeed,
            )
        )
        add(
            GalleryDetailField(
                name = Localization.string("gallery_info_field_sub_seed_strength"),
                value = subSeedStrength,
            )
        )
        if (generationType == AiGenerationResult.Type.IMAGE_TO_IMAGE) {
            add(
                GalleryDetailField(
                    name = Localization.string("gallery_info_field_denoising_strength"),
                    value = denoisingStrength,
                )
            )
        }
    }

private data class GalleryDetailField(
    val name: String,
    val value: String,
    val isLink: Boolean = false,
)
