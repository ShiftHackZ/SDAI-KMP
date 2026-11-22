package com.shifthackz.aisdv1.presentation.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.components.widget.toolbar.LocalToolbarTopContent

/**
 * Gives native route chrome the home status slot so badge, title and system area share one backdrop.
 * Android keeps the status above the route in the outer layout. The native route owns its complete
 * top inset, allowing scrolled content to pass behind the badge as well as the title.
 *
 * @param hasNativeToolbar Whether the route hosts status inside its native toolbar.
 * @param modifier Placement and remaining outer scaffold padding.
 * @param status Connection indicator, empty when home chrome is absent or monitoring is disabled.
 * @param content Current retained route content below the status indicator.
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomeContentLayout(
    hasNativeToolbar: Boolean,
    modifier: Modifier = Modifier,
    status: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalToolbarTopContent provides if (hasNativeToolbar) status else null,
    ) {
        Column(modifier.fillMaxSize()) {
            if (!hasNativeToolbar) status()
            content()
        }
    }
}
