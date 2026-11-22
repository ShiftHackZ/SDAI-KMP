package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Keeps image mutations and form-reuse actions in one row, with an optional provider report action.
 * Very narrow viewports can scroll the row rather than shrinking its 48dp touch targets.
 *
 * @param content Current persisted image flags and reporting capability.
 * @param processIntent Requests the existing gallery operations without changing their semantics.
 * @param modifier Placement of the action row above detail navigation.
 * @param civitaiAction App-supplied Civitai publishing action, retaining independent screen-module ownership.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailActions(
    content: GalleryDetailContent,
    processIntent: (GalleryDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
    civitaiAction: @Composable () -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        GalleryDetailActionButton(
            icon = if (content.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            label = Localization.string(if (content.liked) "gallery_action_unlike" else "gallery_action_like"),
            helpMessage = Localization.string("help_detail_like"),
            tint = if (content.liked) colors.primary else colors.onSurface,
            onClick = { processIntent(GalleryDetailIntent.ToggleLike) },
        )
        GalleryDetailActionButton(
            icon = Icons.Default.TextFields,
            label = Localization.string("home_tab_txt_to_img"),
            helpMessage = Localization.string("help_detail_txt2img"),
            onClick = { processIntent(GalleryDetailIntent.SendTo.Txt2Img) },
        )
        GalleryDetailActionButton(
            icon = Icons.Default.Image,
            label = Localization.string("home_tab_img_to_img"),
            helpMessage = Localization.string("help_detail_img2img"),
            onClick = { processIntent(GalleryDetailIntent.SendTo.Img2Img) },
        )
        GalleryDetailActionButton(
            icon = if (content.hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            label = Localization.string("gallery_action_blur"),
            helpMessage = Localization.string("help_detail_blur"),
            tint = if (content.hidden) colors.primary else colors.onSurface,
            onClick = { processIntent(GalleryDetailIntent.ToggleVisibility) },
        )
        if (content.showReportButton) {
            GalleryDetailActionButton(
                icon = Icons.Default.Report,
                label = Localization.string("gallery_action_report"),
                helpMessage = Localization.string("help_detail_report"),
                onClick = { processIntent(GalleryDetailIntent.Report) },
            )
        }
        civitaiAction()
        GalleryDetailActionButton(
            icon = Icons.Default.Delete,
            label = Localization.string("gallery_action_delete"),
            helpMessage = Localization.string("help_detail_delete"),
            tint = colors.error,
            onClick = { processIntent(GalleryDetailIntent.Delete.Request) },
        )
    }
}
