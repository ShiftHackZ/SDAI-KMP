package com.shifthackz.aisdv1.presentation.gallery.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformNavigationBar
import com.shifthackz.aisdv1.presentation.components.widget.navigation.PlatformNavigationBarItem
import com.shifthackz.aisdv1.presentation.components.widget.navigation.isPlatformNavigationBarFloating

/**
 * Shows current-image actions and output/source/info tabs, each with explanatory help.
 * iOS uses the screen background behind both rows; Android retains its Material surface.
 *
 * @param state Current image flags, reporting capability and selected detail tab.
 * @param modifier Placement of the persistent action area.
 * @param processIntent Requests mutations, export, form reuse or tab changes on short taps.
 * @param civitaiAction App-supplied Civitai publishing action, retaining independent screen-module ownership.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailNavigationBar(
    state: GalleryDetailState,
    modifier: Modifier = Modifier,
    processIntent: (GalleryDetailIntent) -> Unit = {},
    civitaiAction: @Composable () -> Unit = {},
) {
    val backgroundColor =
        if (isPlatformToolbarNative) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.surface
    CompositionLocalProvider(LocalPlatformControlBackgroundColor provides backgroundColor) {
        Column(
            modifier = modifier.background(backgroundColor).fillMaxWidth()
                .padding(
                    start = 16.dp,
                    top = 10.dp,
                    end = 16.dp,
                    bottom = if (isPlatformNavigationBarFloating) 0.dp else 10.dp
                ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            state.content?.let { content ->
                GalleryDetailActions(
                    content = content,
                    civitaiAction = civitaiAction,
                    processIntent = processIntent,
                )
                PlatformNavigationBar(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    items = state.tabs.map { tab ->
                        PlatformNavigationBarItem(
                            label = tab.label,
                            helpMessage = Localization.string(
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
                    },
                )
            }
        }
    }
}
