@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Keeps status-area ownership in the existing Android app scaffold.
 *
 * @author Dmitriy Moroz
 */
actual val isPlatformToolbarNative: Boolean = false

/**
 * Delegates route layout to the unchanged Material scaffold on Android.
 *
 * @param modifier Route placement.
 * @param topBar Existing toolbar slot.
 * @param bottomBar Existing actions slot.
 * @param contentWindowInsets Existing inset contract.
 * @param containerColor Existing route color.
 * @param contentColor Existing route foreground color.
 * @param canUnderlapToolbar iOS-only underlap opt-in; Android always uses external padding.
 * @param content Body receiving the original Material padding values.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformScaffold(
    modifier: Modifier,
    topBar: @Composable () -> Unit,
    bottomBar: @Composable () -> Unit,
    contentWindowInsets: WindowInsets,
    containerColor: Color,
    contentColor: Color,
    canUnderlapToolbar: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier, topBar, bottomBar, containerColor = containerColor, contentColor = contentColor,
        contentWindowInsets = contentWindowInsets, content = content
    )
}

/**
 * Preserves the current Material toolbar dimensions, alignment, colors and insets.
 *
 * @param title Existing title slot.
 * @param modifier Toolbar placement.
 * @param navigationIcon Existing leading control.
 * @param actions Existing trailing controls.
 * @param windowInsets Existing status-area inset handling.
 * @param bottomContentHeight Positive native height enabling an attached strip; Android measures it normally.
 * @param bottomContent Attached shared content retaining its ordinary Android layout.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier,
    navigationIcon: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
    windowInsets: WindowInsets,
    bottomContentHeight: Dp,
    bottomContent: @Composable () -> Unit,
) {
    if (bottomContentHeight > 0.dp) {
        Column(modifier) {
            CenterAlignedTopAppBar(
                title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets
            )
            bottomContent()
        }
    } else {
        CenterAlignedTopAppBar(
            title,
            modifier,
            navigationIcon,
            actions,
            windowInsets = windowInsets
        )
    }
}

/**
 * Leaves Android toolbar coloring independent of the body scroll position.
 *
 * @param isScrolled iOS-only body overlap signal.
 * @author Dmitriy Moroz
 */
@Composable
actual fun ToolbarScrollObserver(isScrolled: Boolean) = Unit
