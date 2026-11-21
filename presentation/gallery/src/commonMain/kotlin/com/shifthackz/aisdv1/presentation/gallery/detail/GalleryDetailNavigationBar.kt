package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpNavigationBarItem

/**
 * Shows current-image actions and output/source/info tabs, each with explanatory help.
 *
 * @param state Current image flags, reporting capability and selected detail tab.
 * @param modifier Placement of the persistent action area.
 * @param processIntent Requests mutations, export, form reuse or tab changes on short taps.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailNavigationBar(
    state: GalleryDetailState,
    modifier: Modifier = Modifier,
    processIntent: (GalleryDetailIntent) -> Unit = {},
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.surface).fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        state.content?.let { content ->
            if (content.showReportButton) {
                GalleryDetailActionButton(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Report,
                    label = Localization.string("gallery_action_report"),
                    helpMessage = Localization.string("help_detail_report"),
                    onClick = { processIntent(GalleryDetailIntent.Report) },
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = if (content.liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    label = Localization.string(if (content.liked) "gallery_action_unlike" else "gallery_action_like"),
                    helpMessage = Localization.string("help_detail_like"),
                    onClick = { processIntent(GalleryDetailIntent.ToggleLike) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Edit,
                    label = Localization.string("home_tab_txt_to_img"),
                    helpMessage = Localization.string("help_detail_txt2img"),
                    onClick = { processIntent(GalleryDetailIntent.SendTo.Txt2Img) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Image,
                    label = Localization.string("home_tab_img_to_img"),
                    helpMessage = Localization.string("help_detail_img2img"),
                    onClick = { processIntent(GalleryDetailIntent.SendTo.Img2Img) },
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = if (content.hidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    label = Localization.string("gallery_action_blur"),
                    helpMessage = Localization.string("help_detail_blur"),
                    onClick = { processIntent(GalleryDetailIntent.ToggleVisibility) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Share,
                    label = Localization.string("hint_prompt"),
                    helpMessage = Localization.string("help_detail_prompt"),
                    onClick = { processIntent(GalleryDetailIntent.Share.Params) },
                )
                GalleryDetailActionButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Delete,
                    label = Localization.string("gallery_action_delete"),
                    helpMessage = Localization.string("help_detail_delete"),
                    onClick = { processIntent(GalleryDetailIntent.Delete.Request) },
                )
            }
            NavigationBar(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = persistentBottomBarWindowInsets(),
            ) {
                state.tabs.forEach { tab ->
                    ContextualHelpNavigationBarItem(
                        title = tab.label,
                        message = Localization.string(
                            when (tab) {
                                GalleryDetailTab.IMAGE -> "help_detail_image_tab"
                                GalleryDetailTab.ORIGINAL -> "help_detail_original_tab"
                                GalleryDetailTab.INFO -> "help_detail_info_tab"
                            }
                        ),
                        icon = tab.icon,
                        isSelected = state.selectedTab == tab,
                        onClick = { processIntent(GalleryDetailIntent.SelectTab(tab)) },
                    )
                }
            }
        }
    }
}
