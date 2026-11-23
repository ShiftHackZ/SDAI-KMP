@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.generation.img2img

import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformScaffold
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.ToolbarBackButton
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.PlatformTopAppBar
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import com.shifthackz.aisdv1.presentation.components.widget.action.PrimaryButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import com.shifthackz.aisdv1.presentation.components.widget.input.PlatformSlider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.theme.global.SystemBarAppearance
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentTopAppBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.theme.sliderColors

/**
 * Edits mask strokes and mask-generation settings using the shared image-to-image state.
 * Viewport zoom/pan and selected editor tab remain local to this composition.
 *
 * @param modifier Layout modifiers for the inpaint editor scaffold.
 * @param image Full-resolution source admitted by the original-image loader.
 * @param state Current strokes, brush and inpaint request parameters.
 * @param processIntent Publishes stroke/parameter changes to the shared image-to-image ViewModel.
 * @param onClose Returns to the source form without discarding shared mask state.
 * @param shouldStartOnAdjustments Initial editor section; otherwise starts on drawing.
 * @param isInitiallyDrawing Initial drawing mode rather than viewport transformation.
 * @param initialZoom Initial viewport scale, clamped to the editor's supported range.
 * @param adjustmentScrollState Optional caller-owned parameter position; otherwise the active Adjust tab owns it.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ImageInPaintScreenContent(
    modifier: Modifier = Modifier,
    image: ImageBitmap,
    state: ImageInPaintState,
    processIntent: (ImageToImageIntent) -> Unit,
    onClose: () -> Unit,
    shouldStartOnAdjustments: Boolean = false,
    isInitiallyDrawing: Boolean = true,
    initialZoom: Float = MIN_IN_PAINT_ZOOM,
    adjustmentScrollState: ScrollState? = null,
) {
    var selectedTab by remember { mutableStateOf(if (shouldStartOnAdjustments) ImageInPaintTab.Adjust else ImageInPaintTab.Draw) }
    var isDrawing by remember { mutableStateOf(isInitiallyDrawing) }
    var zoom by remember {
        mutableFloatStateOf(
            initialZoom.coerceIn(
                MIN_IN_PAINT_ZOOM,
                MAX_IN_PAINT_ZOOM
            )
        )
    }
    var pan by remember { mutableStateOf(Offset.Zero) }
    if (!LocalInspectionMode.current) SystemBarAppearance(
        colorScheme = MaterialTheme.colorScheme,
        isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f,
    )

    PlatformScaffold(
        modifier = modifier,
        topBar = {
            PlatformTopAppBar(
                title = {
                    Text(text = Localization.string("in_paint_title"))
                },
                navigationIcon = {
                    ToolbarBackButton(
                        onClick = onClose,
                        icon = Icons.AutoMirrored.Outlined.ArrowBack
                    )
                },
                windowInsets = persistentTopAppBarWindowInsets(),
            )
        },
        bottomBar = {
            Column {
                AnimatedVisibility(
                    visible = selectedTab == ImageInPaintTab.Draw,
                    enter = slideInVertically(initialOffsetY = { it }),
                    exit = slideOutVertically(targetOffsetY = { it }),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            val isEnabled = state.strokes.isNotEmpty()
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                isEnabled = isEnabled,
                                onClick = { processIntent(ImageToImageIntent.UndoInPaintStroke) },
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Undo,
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = Localization.string("action_undo"),
                                    color = LocalContentColor.current,
                                )
                            }
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                isEnabled = isEnabled,
                                onClick = { processIntent(ImageToImageIntent.ClearInPaintMask) },
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CleaningServices,
                                    contentDescription = null,
                                )
                                Text(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    text = Localization.string("action_clear"),
                                    color = LocalContentColor.current,
                                )
                            }
                            DrawModeButton(
                                modifier = Modifier.weight(1f),
                                isSelected = isDrawing,
                                onClick = { isDrawing = !isDrawing },
                            )
                        }
                        BrushSizeSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            size = state.brushSize,
                            onValueChanged = {
                                processIntent(ImageToImageIntent.UpdateInPaintBrushSize(it))
                            },
                        )
                        ZoomSlider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            zoom = zoom,
                            onZoomChanged = { value ->
                                zoom = value
                                if (value <= MIN_IN_PAINT_ZOOM) {
                                    pan = Offset.Zero
                                }
                            },
                            onResetClick = {
                                zoom = MIN_IN_PAINT_ZOOM
                                pan = Offset.Zero
                            },
                        )
                    }
                }
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    windowInsets = persistentBottomBarWindowInsets(),
                ) {
                    ImageInPaintTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            label = {
                                Text(
                                    text = tab.label,
                                    color = LocalContentColor.current,
                                )
                            },
                            colors = NavigationBarItemDefaults.colors().copy(
                                selectedIndicatorColor = MaterialTheme.colorScheme.primary,
                            ),
                            icon = {
                                Icon(
                                    imageVector = when (tab) {
                                        ImageInPaintTab.Draw -> Icons.Default.Image
                                        ImageInPaintTab.Adjust -> Icons.Default.Tune
                                    },
                                    contentDescription = null,
                                )
                            },
                            onClick = { selectedTab = tab },
                        )
                    }
                }
            }
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        ) {
            when (selectedTab) {
                ImageInPaintTab.Draw -> ImageInPaintCanvas(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth(),
                    image = image,
                    state = state,
                    drawEnabled = isDrawing,
                    zoom = zoom,
                    pan = pan,
                    transformEnabled = !isDrawing,
                    onZoomChange = { value ->
                        zoom = value
                        if (value <= MIN_IN_PAINT_ZOOM) {
                            pan = Offset.Zero
                        }
                    },
                    onPanChange = { value -> pan = value },
                    onStrokeDrawn = { stroke ->
                        processIntent(ImageToImageIntent.DrawInPaintStroke(stroke))
                    },
                )

                ImageInPaintTab.Adjust -> ImageInPaintParamsForm(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    model = state,
                    processIntent = processIntent,
                    scrollState = adjustmentScrollState ?: rememberScrollState(),
                )
            }
        }
    }
}


/**
 * Switches between stroke drawing and viewport transformation using the selected button style.
 *
 * @param modifier Placement within the brush-action row.
 * @param isSelected Whether stroke drawing is active.
 * @param onClick Toggles drawing mode without changing stored mask strokes.
 * @author Dmitriy Moroz
 */
@Composable
private fun DrawModeButton(
    modifier: Modifier = Modifier,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    if (isSelected) {
        PrimaryButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            DrawModeButtonContent()
        }
    } else {
        OutlinedButton(
            modifier = modifier,
            onClick = onClick,
        ) {
            DrawModeButtonContent()
        }
    }
}

/**
 * Displays the brush icon and localized drawing-mode label in either button style.
 *
 * @author Dmitriy Moroz
 */
@Composable
private fun DrawModeButtonContent() {
    Icon(
        imageVector = Icons.Default.Brush,
        contentDescription = null,
    )
    Text(
        modifier = Modifier.padding(start = 8.dp),
        text = Localization.string("action_draw"),
        color = LocalContentColor.current,
    )
}


/**
 * Adjusts viewport zoom and provides an explicit return to the initial image scale.
 *
 * @param modifier Placement of the zoom controls.
 * @param zoom Current viewport scale.
 * @param onZoomChanged Updates the local viewport scale.
 * @param onResetClick Resets zoom and pan without changing mask strokes.
 * @author Dmitriy Moroz
 */
@Composable
private fun ZoomSlider(
    modifier: Modifier = Modifier,
    zoom: Float,
    onZoomChanged: (Float) -> Unit,
    onResetClick: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(
            modifier = Modifier.size(IN_PAINT_SLIDER_LEADING_WIDTH),
            onClick = onResetClick,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = Localization.string("action_reset"),
            )
        }
        PlatformSlider(
            modifier = Modifier.weight(1f),
            value = zoom,
            valueRange = MIN_IN_PAINT_ZOOM..MAX_IN_PAINT_ZOOM,
            colors = sliderColors.copy(
                inactiveTrackColor = MaterialTheme.colorScheme.background,
            ),
            onValueChange = onZoomChanged,
        )
    }
}
