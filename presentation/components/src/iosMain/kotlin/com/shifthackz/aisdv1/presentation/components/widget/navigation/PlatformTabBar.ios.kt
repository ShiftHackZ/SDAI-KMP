package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitInteropInteractionMode
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlPreview
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.presentation.components.platform.toUIColor

/**
 * Embeds UIKit's segmented page selector with native accessibility and caller-owned selection.
 * Manual appearance and accent follow the app theme; covered routes release their native surfaces.
 *
 * @param labels Ordered localized page titles.
 * @param selectedIndex Current page index, within [labels].
 * @param onSelect Requests a page from the shared owner.
 * @param modifier Placement of the complete 56dp strip.
 * @param isEnabled Whether native selection is allowed.
 * @author Dmitriy Moroz
 */
@Composable
actual fun PlatformTabBar(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier,
    isEnabled: Boolean,
) {
    require(selectedIndex in labels.indices)
    if (LocalPlatformControlPreview.current) {
        MaterialTabBar(labels, selectedIndex, onSelect, modifier, isEnabled)
        return
    }
    val bounds = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).height(48.dp)
    if (!LocalMviVisibility.current || !LocalPlatformControlVisibility.current) {
        Spacer(bounds)
        return
    }
    val colors = MaterialTheme.colorScheme
    val backingColor = LocalPlatformControlBackgroundColor.current ?: colors.background
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    UIKitView(
        modifier = bounds,
        factory = { NativeTabBarView() },
        update = {
            it.update(
                labels, selectedIndex, isEnabled,
                colors.background.luminance() < 0.5f, isRtl,
                colors.primary.toUIColor(), colors.onPrimary.toUIColor(),
                colors.onSurface.toUIColor(), colors.surfaceVariant.toUIColor(),
                backingColor.toUIColor(), onSelect,
            )
        },
        onRelease = { it.onSelect = null },
        properties = UIKitInteropProperties(
            interactionMode = if (isEnabled) UIKitInteropInteractionMode.NonCooperative else null,
            isNativeAccessibilityEnabled = true,
        ),
    )
}
