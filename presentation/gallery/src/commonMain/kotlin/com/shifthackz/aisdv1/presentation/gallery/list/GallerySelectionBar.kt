package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.action.CircleIconButton
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalFloatingNavigationBarInset
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformBlurredPanel
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.isPlatformToolbarNative

/**
 * Keeps Android's selection footer and floats iOS controls in a rounded material panel above its tabs.
 * The iOS panel owns its native backdrop and glass circles together so both sample the gallery.
 *
 * @param state Current selected items and aggregate toggle actions.
 * @param processIntent Requests selection operations through the gallery ViewModel.
 * @author Dmitriy Moroz
 */
@Composable
internal fun GallerySelectionBar(state: GalleryState, processIntent: (GalleryIntent) -> Unit) {
    if (isPlatformToolbarNative) {
        key(state.selection.isNotEmpty()) {
            PlatformBlurredPanel(
                modifier = Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = LocalFloatingNavigationBarInset.current + 8.dp
                    )
                    .fillMaxWidth()
                    .height(if (state.selection.isNotEmpty()) 120.dp else 64.dp),
                cornerRadius = 16.dp,
            ) {
                Column(Modifier.padding(8.dp)) {
                    if (state.selection.isNotEmpty()) {
                        GallerySelectionActionsBar(state, processIntent)
                        Spacer(Modifier.height(8.dp))
                    }
                    GallerySelectionSummary(state, processIntent)
                }
            }
        }
    } else {
        Column {
            AnimatedVisibility(
                visible = state.selection.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                GallerySelectionActionsBar(state, processIntent)
            }
            GallerySelectionSummary(state, processIntent)
        }
    }
}

/**
 * Renders the existing group operations with native iOS glass and Android Material circles.
 *
 * @param state Aggregate like and visibility actions for the current selection.
 * @param processIntent Receives group operations and confirmation requests.
 * @author Dmitriy Moroz
 */
@Composable
private fun GallerySelectionActionsBar(
    state: GalleryState,
    processIntent: (GalleryIntent) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (isPlatformToolbarNative) Color.Transparent else MaterialTheme.colorScheme.surfaceTint)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircleIconButton(
            onClick = { processIntent(GalleryIntent.ToggleSelectionLike) },
            icon = if (state.shouldLikeSelection) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = Localization.string(if (state.shouldLikeSelection) "gallery_action_like" else "gallery_action_unlike"),
        )
        CircleIconButton(
            onClick = { processIntent(GalleryIntent.ToggleSelectionVisibility) },
            icon = if (state.shouldHideSelection) Icons.Default.VisibilityOff else Icons.Default.Visibility,
            contentDescription = Localization.string("gallery_action_blur"),
        )
        CircleIconButton(
            onClick = { processIntent(GalleryIntent.Delete.Selection.Request) },
            icon = Icons.Default.Delete,
            contentDescription = Localization.string("gallery_action_delete"),
        )
        CircleIconButton(
            onClick = { processIntent(GalleryIntent.Export.Selection.Request) },
            icon = Icons.Default.Share,
            contentDescription = Localization.string("action_export"),
        )
    }
}

/**
 * Shows the selection count and a single action to clear the selection or leave selection mode.
 * iOS emphasizes that action with its primary button so contrast does not depend on the backdrop.
 *
 * @param state Selected items determining the count and clear/cancel action.
 * @param processIntent Receives selection clearing or dismissal.
 * @author Dmitriy Moroz
 */
@Composable
private fun GallerySelectionSummary(state: GalleryState, processIntent: (GalleryIntent) -> Unit) {
    val onClearSelection = {
        if (state.selection.isNotEmpty()) processIntent(GalleryIntent.UnselectAll)
        else processIntent(GalleryIntent.ChangeSelectionMode(false))
    }
    val actionText =
        Localization.string(if (state.selection.isNotEmpty()) "gallery_menu_unselect_all" else "cancel")
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (isPlatformToolbarNative) Color.Transparent else MaterialTheme.colorScheme.surfaceTint),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f)
                .padding(start = if (isPlatformToolbarNative) 8.dp else 16.dp),
            text = Localization.string("gallery_menu_selected", "${state.selection.size}"),
            style = if (isPlatformToolbarNative) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
            lineHeight = 17.sp,
            fontWeight = FontWeight.W400,
        )
        if (isPlatformToolbarNative) {
            PrimaryButton(onClick = onClearSelection, modifier = Modifier.height(48.dp)) {
                Text(actionText, textAlign = TextAlign.Center)
            }
        } else {
            TextButton(modifier = Modifier.padding(end = 16.dp), onClick = onClearSelection) {
                Text(
                    actionText.uppercase(),
                    textAlign = TextAlign.Center,
                    color = LocalContentColor.current
                )
            }
        }
    }
}
