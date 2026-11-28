package com.shifthackz.aisdv1.presentation.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationInputEnabled
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationBarInsetsEnabled
import com.shifthackz.aisdv1.presentation.components.widget.navigation.LocalNavigationBarOwnerEnabled
import com.shifthackz.aisdv1.presentation.components.theme.global.appHorizontalSafeArea

/**
 * Keeps a route's composition alive at real viewport constraints while placing only visible layers.
 * Hidden entries cannot register foreground Back handlers or mount native controls above another route.
 *
 * @param isPlaced Whether this layer is foreground or a visible navigation preview.
 * @param isForeground Whether touch input, accessibility and Back belong to this route.
 * @param hasNavigationBar Whether this route reserves native navigation edges for its lifetime.
 * @param ownsNavigationBar Whether this route may register native tabs during the transition.
 * @param modifier Translation and bounds of the entire route, including embedded native views.
 * @param content Single route composition retained for its entry lifetime.
 * @author Dmitriy Moroz
 */
@Composable
internal fun RetainedRouteLayer(
    isPlaced: Boolean,
    isForeground: Boolean,
    hasNavigationBar: Boolean,
    ownsNavigationBar: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalMviVisibility provides isPlaced,
        LocalPlatformControlVisibility provides isPlaced,
        LocalNavigationInputEnabled provides isForeground,
        LocalNavigationBarInsetsEnabled provides hasNavigationBar,
        LocalNavigationBarOwnerEnabled provides ownsNavigationBar,
    ) {
        Box(modifier.appHorizontalSafeArea()) {
            Layout(
                modifier = if (isForeground) Modifier.fillMaxSize() else Modifier.fillMaxSize()
                    .clearAndSetSemantics {}
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    },
                content = content,
            ) { measurables, constraints ->
                val placeables = measurables.map { it.measure(constraints) }
                layout(constraints.maxWidth, constraints.maxHeight) {
                    if (isPlaced) placeables.forEach { it.placeRelative(0, 0) }
                }
            }
        }
    }
}
