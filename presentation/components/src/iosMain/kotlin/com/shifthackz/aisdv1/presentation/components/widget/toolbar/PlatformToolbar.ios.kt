@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class,
    ExperimentalForeignApi::class
)

package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationInputEnabled
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitViewController
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor
import com.shifthackz.aisdv1.presentation.components.platform.NativeBlurViewController
import com.shifthackz.aisdv1.presentation.components.platform.rememberThemedNativeContent
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.accessibilityElementsHidden

private val LocalToolbarOverlap = staticCompositionLocalOf<MutableState<Boolean>?> { null }
private val LocalToolbarUnderlap = staticCompositionLocalOf { false }

/** Lets the iOS route toolbar paint and blur its own status area. @author Dmitriy Moroz */
actual val isPlatformToolbarNative: Boolean = true

/**
 * Moves the initial top inset inside opted-in scroll containers while retaining bottom action padding.
 * Native toolbar material samples the composited UIKit hierarchy, so body capture is unnecessary.
 *
 * @param modifier Route placement.
 * @param topBar Persistent toolbar content.
 * @param bottomBar Persistent bottom actions.
 * @param contentWindowInsets Insets owned by the route.
 * @param containerColor Opaque route background.
 * @param contentColor Existing route foreground color.
 * @param canUnderlapToolbar Whether the scroll container consumes LocalToolbarContentInset.
 * @param content Route body with remaining external padding.
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
    val overlap = remember { mutableStateOf(false) }
    val direction = LocalLayoutDirection.current
    val shouldUnderlapToolbar = canUnderlapToolbar && !LocalPlatformControlPreview.current
    CompositionLocalProvider(
        LocalToolbarUnderlap provides shouldUnderlapToolbar,
        LocalToolbarOverlap provides overlap,
    ) {
        Scaffold(
            modifier = modifier,
            topBar = topBar,
            bottomBar = bottomBar,
            contentWindowInsets = contentWindowInsets,
            containerColor = containerColor,
            contentColor = contentColor,
        ) { padding ->
            val topInset = if (shouldUnderlapToolbar) padding.calculateTopPadding() else 0.dp
            CompositionLocalProvider(LocalToolbarContentInset provides topInset) {
                Box(Modifier.fillMaxSize()) {
                    content(
                        if (shouldUnderlapToolbar) PaddingValues(
                            start = padding.calculateStartPadding(direction),
                            end = padding.calculateEndPadding(direction),
                            bottom = padding.calculateBottomPadding(),
                        ) else padding
                    )
                }
            }
        }
    }
}

/**
 * Hosts shared route chrome above native system material, including the status area.
 * UIKit inputs and Compose content both contribute to the blur. The resting background matches
 * the app color. Drawer changes slide the overlay above the viewport while retaining body placement.
 *
 * @param title Localized title content.
 * @param modifier Toolbar placement.
 * @param navigationIcon Leading circular toolbar action.
 * @param actions Trailing circular toolbar actions.
 * @param windowInsets Insets retained for embedded tutorial previews.
 * @param bottomContentHeight Height reserved below the title row in the same native backdrop.
 * @param bottomContent Attached controls sharing the title row's blur and scroll state.
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
    if (LocalPlatformControlPreview.current) {
        Column(modifier) {
            CenterAlignedTopAppBar(
                title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = windowInsets
            )
            if (bottomContentHeight > 0.dp) bottomContent()
        }
        return
    }
    val topPixels = WindowInsets.statusBars.getTop(LocalDensity.current)
    val height = 64.dp + with(LocalDensity.current) { topPixels.toDp() } + bottomContentHeight
    val bounds = modifier.fillMaxWidth().height(height)
    val isObscured = LocalToolbarObscured.current
    val hiddenFraction by animateFloatAsState(
        targetValue = if (isObscured) 1f else 0f,
        animationSpec = tween(220),
        label = "toolbar-drawer-offset",
    )
    if (!LocalMviVisibility.current || !LocalPlatformControlVisibility.current || (isObscured && hiddenFraction >= 1f)) {
        Spacer(bounds)
        return
    }
    val isScrolled = LocalToolbarUnderlap.current && LocalToolbarOverlap.current?.value == true
    val colors = MaterialTheme.colorScheme
    val nativeContent = rememberThemedNativeContent {
        Column {
            CenterAlignedTopAppBar(
                modifier = Modifier.padding(horizontal = 12.dp),
                title = title,
                navigationIcon = navigationIcon,
                actions = actions,
                windowInsets = WindowInsets(0, topPixels, 0, 0),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
            if (bottomContentHeight > 0.dp) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface,
                    LocalPlatformControlBackgroundColor provides Color.Transparent,
                ) {
                    bottomContent()
                }
            }
        }
    }
    UIKitViewController(
        modifier = bounds.offset {
            IntOffset(0, (-height.toPx() * hiddenFraction).roundToInt())
        },
        factory = { NativeBlurViewController(nativeContent) },
        update = {
            it.update(
                isScrolled,
                colors.background.toUIColor(),
                colors.background.luminance() < 0.5f,
                tintAlpha = 0.08,
            )
            it.view.clipsToBounds = true
            it.view.userInteractionEnabled = !isObscured
            it.view.accessibilityElementsHidden = isObscured
        },
        properties = UIKitInteropProperties(
            interactionMode = UIKitInteropInteractionMode.NonCooperative,
            isNativeAccessibilityEnabled = true,
            placedAsOverlay = true,
        ),
    )
}

/**
 * Synchronizes toolbar overlap from the foreground scroll owner, including restored positions.
 * Retained pager previews cannot replace the active page's blur state.
 * @param isScrolled Whether the owning scroll container can move toward its start.
 * @author Dmitriy Moroz
 */
@Composable
actual fun ToolbarScrollObserver(isScrolled: Boolean) {
    val overlap = LocalToolbarOverlap.current
    val canReportScroll = LocalMviVisibility.current && LocalNavigationInputEnabled.current
    SideEffect { if (canReportScroll) overlap?.value = isScrolled }
}
