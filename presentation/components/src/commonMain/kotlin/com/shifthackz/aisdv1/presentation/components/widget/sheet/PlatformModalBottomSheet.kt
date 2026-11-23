package com.shifthackz.aisdv1.presentation.components.widget.sheet

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape

/**
 * Retains Material sheet state, anchors, scrim, dragging and dismissal on both platforms.
 * Android delegates its original rendering. iOS wraps the handle, insets and controls in one
 * [PlatformSheetSurface], allowing the drawer-style blur to cover the entire sheet.
 *
 * @param onDismissRequest Removes the sheet after a completed dismissal.
 * @param modifier Sheet sizing and outside spacing, forwarded to the Material host.
 * @param sheetState Caller-owned visibility and partial/expanded anchor policy.
 * @param shape Sheet outline, retaining each caller's existing geometry.
 * @param containerColor Existing Android background and iOS blur tint.
 * @param content Controls inside the sheet's column, with their existing state and callbacks.
 *
 * @author Dmitriy Moroz
 */
@ExperimentalMaterial3Api
@Composable
fun PlatformModalBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shouldUseNativeBlur = isPlatformSheetBlurAvailable()
    if (!shouldUseNativeBlur) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            sheetState = sheetState,
            shape = shape,
            containerColor = containerColor,
            content = content,
        )
        return
    }
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = Color.Transparent,
        contentColor = contentColorFor(containerColor),
        dragHandle = null,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
    ) {
        PlatformSheetSurface(shape = shape, containerColor = containerColor) {
            Column(Modifier.fillMaxWidth().windowInsetsPadding(BottomSheetDefaults.windowInsets)) {
                SheetDragHandle(sheetState, onDismissRequest)
                content()
            }
        }
    }
}
