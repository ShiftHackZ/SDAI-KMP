@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp

/**
 * Scrollable content's initial top inset. On iOS it belongs inside the scroll container so
 * rows can pass behind the translucent toolbar; Android retains ordinary scaffold padding.
 *
 * @author Dmitriy Moroz
 */
val LocalToolbarContentInset = staticCompositionLocalOf { 0.dp }

/**
 * Overrides the native toolbar's status-area inset when an enclosing home shell already reserves it.
 * Null leaves safe-area ownership with the route toolbar.
 * @author Dmitriy Moroz
 */
val LocalToolbarStatusBarInset = staticCompositionLocalOf<Dp?> { null }

/**
 * Prevents floating native toolbar content from drawing above the app's modal drawer.
 * @author Dmitriy Moroz
 */
val LocalToolbarObscured = staticCompositionLocalOf { false }

/**
 * Whether route toolbars own iOS status-area painting and native typography.
 * @author Dmitriy Moroz
 */
expect val isPlatformToolbarNative: Boolean

/**
 * Uses compact native-style title typography on iOS without altering Android title styles.
 * @param androidStyle Existing title style selected by the Android screen.
 * @author Dmitriy Moroz
 */
@Composable
fun platformToolbarTitleStyle(androidStyle: TextStyle): TextStyle =
    if (isPlatformToolbarNative) MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold) else androidStyle

/**
 * Hosts route chrome while retaining Android Scaffold's existing layout contract.
 * Scrollable bodies opting into iOS underlap must consume [LocalToolbarContentInset] internally.
 *
 * @param modifier Layout bounds of the route.
 * @param topBar Persistent toolbar and any associated background-work indicator.
 * @param bottomBar Existing persistent route actions.
 * @param contentWindowInsets Insets not already handled by an enclosing app scaffold.
 * @param containerColor Route background color.
 * @param contentColor Existing foreground color for the route content.
 * @param canUnderlapToolbar Whether the body consumes its top inset inside its scroll container.
 * @param content Route body receiving remaining external scaffold padding.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformScaffold(
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    contentWindowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
    containerColor: Color = MaterialTheme.colorScheme.background,
    contentColor: Color = contentColorFor(containerColor),
    canUnderlapToolbar: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
)

/**
 * Centers a route title and keeps platform-specific background rendering behind its controls.
 * iOS uses the screen color at rest and a tinted backdrop once content passes under the bar.
 *
 * @param title Localized title content.
 * @param modifier Toolbar placement.
 * @param navigationIcon Back or drawer action.
 * @param actions Trailing route actions.
 * @param windowInsets Status-area insets owned by this toolbar.
 * @param bottomContentHeight Height reserved for the attached content in the native iOS host.
 * @param bottomContent Attached controls sharing the toolbar's backdrop and scroll behavior.
 * @author Dmitriy Moroz
 */
@Composable
expect fun PlatformTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
    bottomContentHeight: Dp = 0.dp,
    bottomContent: @Composable () -> Unit = {},
)

/**
 * Receives the owning scroll container's real position, including restored/programmatic movement.
 * Android does not change its toolbar appearance in response to this signal.
 *
 * @param isScrolled Whether any content has passed the leading edge of the scroll container.
 * @author Dmitriy Moroz
 */
@Composable
expect fun ToolbarScrollObserver(isScrolled: Boolean)
