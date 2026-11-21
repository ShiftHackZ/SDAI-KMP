package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarStatusBarInset

/**
 * Places home status above route content while assigning the status-area inset to exactly one owner.
 * Native home chrome reserves it before the connection badge; its child toolbar then uses zero.
 * Android and standalone routes retain their existing outer-scaffold or toolbar ownership.
 *
 * @param shouldReserveTopInset Whether this home shell owns the native status area.
 * @param topWindowInsets Safe-area insets supplied by the host platform.
 * @param modifier Placement and remaining outer scaffold padding.
 * @param status Connection indicator, empty when home chrome is absent or monitoring is disabled.
 * @param content Current retained route content below the status indicator.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomeContentLayout(
    shouldReserveTopInset: Boolean,
    topWindowInsets: WindowInsets,
    modifier: Modifier = Modifier,
    status: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalToolbarStatusBarInset provides if (shouldReserveTopInset) 0.dp else null,
    ) {
        Column(
            modifier.fillMaxSize().then(
                if (shouldReserveTopInset) Modifier.windowInsetsPadding(topWindowInsets) else Modifier
            ),
        ) {
            status()
            content()
        }
    }
}
