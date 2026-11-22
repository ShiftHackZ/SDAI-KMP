package com.shifthackz.aisdv1.presentation.gallery.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalFloatingNavigationBarInset

/**
 * Adds subtle staggered scale/rotation while a gallery cell is in selection mode.
 *
 * @param enabled Whether selection animation affects this cell.
 * @param animationStartOffset Initial animation phase offset in milliseconds.
 *
 * @receiver Cell modifier to decorate.
 *
 * @return Modifier applying animated transforms only when enabled.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun Modifier.selectionJiggle(
    enabled: Boolean,
    animationStartOffset: Int,
): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "selection_jiggle")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = .99f,
        animationSpec = infiniteRepeatable(
            animation = tween(188, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(animationStartOffset),
        ),
        label = "selection_jiggle_scale",
    )
    val rotation by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(188, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(animationStartOffset),
        ),
        label = "selection_jiggle_rotation",
    )
    return graphicsLayer {
        scaleX = if (enabled) scale else 1f
        scaleY = if (enabled) scale else 1f
        rotationZ = if (enabled) rotation else 0f
    }
}

/**
 * Shows selection actions/count or the configured media-export destination summary.
 * Visible bars reserve the floating iOS tab bar's inset so their controls remain above it.
 *
 * @param state Current selection flags and media-store information.
 * @param processIntent Requests group operations, selection dismissal or opening the media destination.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryBottomBars(
    state: GalleryState,
    processIntent: (GalleryIntent) -> Unit,
) {
    AnimatedVisibility(
        visible = state.selectionMode,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        GallerySelectionBar(state, processIntent)
    }
    AnimatedVisibility(
        visible = state.mediaStoreInfo.isNotEmpty && !state.selectionMode,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Row(
            modifier = Modifier
                .padding(bottom = LocalFloatingNavigationBarInset.current)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceTint),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier
                    .padding(start = 16.dp)
                    .fillMaxWidth(0.65f),
                text = Localization.string(
                    "gallery_media_store_banner",
                    "${state.mediaStoreInfo.count}"
                ),
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 17.sp,
                fontWeight = FontWeight.W400,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                modifier = Modifier.padding(end = 16.dp),
                onClick = {
                    processIntent(GalleryIntent.OpenMediaStoreFolder)
                },
            ) {
                Text(
                    text = Localization.string("browse").uppercase(),
                    color = LocalContentColor.current,
                )
            }
        }
    }
}


/**
 * Renders gallery deletion/export confirmations, archive progress and public operation errors.
 *
 * @param dialog Current overlay chosen by the gallery ViewModel.
 * @param processIntent Requests confirmation or dismissal through the owning ViewModel.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryDialogRenderer(
    dialog: GalleryDialog,
    processIntent: (GalleryIntent) -> Unit,
) {
    when (dialog) {
        GalleryDialog.None -> Unit
        GalleryDialog.DeleteAllConfirm -> DeleteGalleryDialog(
            titleKey = "interaction_delete_all_title",
            textKey = "interaction_delete_all_sub_title",
            onConfirm = { processIntent(GalleryIntent.Delete.All.Confirm) },
            onDismiss = { processIntent(GalleryIntent.DismissDialog) },
        )

        GalleryDialog.DeleteSelectionConfirm -> DeleteGalleryDialog(
            titleKey = "interaction_delete_selection_title",
            textKey = "interaction_delete_selection_sub_title",
            onConfirm = { processIntent(GalleryIntent.Delete.Selection.Confirm) },
            onDismiss = { processIntent(GalleryIntent.DismissDialog) },
        )

        is GalleryDialog.ConfirmExport -> DecisionInteractiveDialog(
            title = Localization.string("interaction_export_title").asUiText(),
            text = Localization.string(
                if (dialog.exportAll) {
                    "interaction_export_sub_title"
                } else {
                    "interaction_export_sub_title_selection"
                }
            ).asUiText(),
            confirmActionText = Localization.string("action_export").asUiText(),
            onConfirmAction = {
                processIntent(
                    if (dialog.exportAll) {
                        GalleryIntent.Export.All.Confirm
                    } else {
                        GalleryIntent.Export.Selection.Confirm
                    }
                )
            },
            onDismissRequest = { processIntent(GalleryIntent.DismissDialog) },
        )

        GalleryDialog.ExportInProgress -> ProgressDialog(
            title = Localization.string("exporting_progress_title").asUiText(),
            subTitle = Localization.string("exporting_progress_sub_title").asUiText(),
            canDismiss = false,
        )

        is GalleryDialog.Error -> ErrorDialog(
            text = dialog.message.asUiText(),
            onDismissRequest = { processIntent(GalleryIntent.DismissDialog) },
        )
    }
}

/**
 * Builds a localized destructive gallery confirmation from its message keys.
 *
 * @param titleKey Localization key for the destructive-action heading.
 * @param textKey Localization key for the confirmation explanation.
 * @param onConfirm Requests the already described deletion scope.
 * @param onDismiss Closes the confirmation without deleting content.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun DeleteGalleryDialog(
    titleKey: String,
    textKey: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    DecisionInteractiveDialog(
        title = Localization.string(titleKey).asUiText(),
        text = Localization.string(textKey).asUiText(),
        confirmActionText = Localization.string("yes").asUiText(),
        dismissActionText = Localization.string("no").asUiText(),
        onConfirmAction = onConfirm,
        onDismissRequest = onDismiss,
    )
}

/**
 * Renders a loading grid at the user-selected column density.
 *
 * @param modifier Layout modifiers for the placeholder grid.
 * @param grid Density controlling column count and placeholder count.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun GalleryLoadingGrid(
    modifier: Modifier,
    grid: Grid,
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(grid.size),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        galleryShimmerItems(grid)
    }
}

/**
 * Adds a density-dependent viewport of square shimmer cells to a lazy grid.
 *
 * @param grid Gallery density controlling how many placeholder cells are added.
 *
 * @author Dmitriy Moroz
 */
internal fun LazyGridScope.galleryShimmerItems(grid: Grid) {
    val max = when (grid) {
        Grid.Fixed2 -> 6
        Grid.Fixed3 -> 12
        Grid.Fixed4 -> 20
        Grid.Fixed5 -> 30
    }
    repeat(max) { index ->
        item(key = "shimmer_$index") {
            GalleryUiItemShimmer()
        }
    }
}
