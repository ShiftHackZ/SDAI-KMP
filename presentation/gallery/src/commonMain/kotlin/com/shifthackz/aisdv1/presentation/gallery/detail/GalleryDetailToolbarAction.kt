package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarIconButton
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import com.shifthackz.aisdv1.presentation.gallery.generated.resources.Res
import com.shifthackz.aisdv1.presentation.gallery.generated.resources.ic_share
import org.jetbrains.compose.resources.painterResource

/**
 * Crossfades Export and Share glyphs in one stable toolbar target when the detail tab changes.
 * Image tabs retain the original-image share-sheet action; Info shares generation parameters.
 * The current tab owns the click and accessible label throughout the short visual transition.
 *
 * @param selectedTab Current image/source/info tab, determining the payload to share.
 * @param isEnabled Whether an image is loaded and the action can run.
 * @param processIntent Receives the existing image or parameter share intent.
 * @param modifier Placement of the toolbar help anchor.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDetailToolbarAction(
    selectedTab: GalleryDetailTab,
    isEnabled: Boolean,
    processIntent: (GalleryDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isSharingParams = selectedTab == GalleryDetailTab.INFO
    val title = Localization.string(if (isSharingParams) "action_share_prompt" else "action_export")
    ContextualHelp(
        title = title,
        message = Localization.string(if (isSharingParams) "help_detail_prompt" else "help_detail_export"),
        titleStartIcon = Icons.Default.Share,
        modifier = modifier,
    ) {
        ToolbarIconButton(
            modifier = Modifier.semantics { contentDescription = title },
            isEnabled = isEnabled,
            onClick = {
                processIntent(if (isSharingParams) GalleryDetailIntent.Share.Params else GalleryDetailIntent.Share.Image)
            },
        ) {
            Crossfade(
                targetState = isSharingParams,
                animationSpec = tween(180),
                label = "Gallery share action"
            ) { shouldShowShare ->
                if (shouldShowShare) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        painterResource(Res.drawable.ic_share),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
