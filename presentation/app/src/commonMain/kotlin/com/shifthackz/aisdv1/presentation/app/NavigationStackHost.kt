package com.shifthackz.aisdv1.presentation.app

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import com.shifthackz.aisdv1.core.navigation.AppRoute
import com.shifthackz.aisdv1.core.navigation.RootAppRouter
import com.shifthackz.aisdv1.core.navigation.isHomeRoute
import com.shifthackz.aisdv1.presentation.components.widget.navigation.BackTransitionState
import com.shifthackz.aisdv1.presentation.components.widget.navigation.rememberNavigationSnapshotCapture
import kotlin.math.roundToInt

/**
 * Retains stack entry compositions and animates at most the foreground and its Back destination.
 * Removed entries survive until their exit completes, then release their route-owned resources.
 *
 * @param state Authoritative route history and retained home selection.
 * @param backTransition Cancellable platform gesture progress for route pops.
 * @param router Supplies the outgoing visible entry before navigation changes its UI.
 * @param modifier Bounds clipping the complete moving screens.
 * @param homeContent Existing retained peer tabs, shared across navigation history.
 * @param overlayContent Standalone screen created once for each entry identity.
 * @author Dmitriy Moroz
 */
@Composable
internal fun NavigationStackHost(
    state: AiSdAppState,
    backTransition: BackTransitionState,
    router: RootAppRouter,
    modifier: Modifier = Modifier,
    homeContent: @Composable (AppRoute, List<AppRoute>, Boolean) -> Unit,
    overlayContent: @Composable (AppRoute) -> Unit,
) {
    val entries = state.backStack
    val capture = rememberNavigationSnapshotCapture()
    val snapshots = remember { mutableStateMapOf<Long, ImageBitmap>() }
    DisposableEffect(router, capture) {
        val detach = router.observeBeforeCover { entry ->
            capture()?.let { snapshots[entry.id] = it }
        }
        onDispose { detach() }
    }
    var retained by remember { mutableStateOf(entries) }
    var motion by remember { mutableStateOf<NavigationMotion?>(null) }
    val animation = remember { Animatable(1f) }
    var width by remember { mutableStateOf(0) }
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1
    val pendingMotion = motion ?: navigationMotion(retained, entries)
    val isGesturePop = pendingMotion?.isPop == true && backTransition.hasCommittedGesture
    val fraction = when {
        isGesturePop -> 1f
        motion != null -> animation.value
        else -> 0f
    }
    val current = entries.lastOrNull()
    val previous = entries.getOrNull(entries.lastIndex - 1)
    val shouldRevealPrevious = pendingMotion == null && backTransition.isActive
    val foreground = when {
        pendingMotion?.isPop == true -> pendingMotion.outgoing
        pendingMotion != null -> pendingMotion.incoming
        else -> current
    }
    val background = when {
        pendingMotion?.isPop == true -> pendingMotion.incoming
        pendingMotion != null -> pendingMotion.outgoing
        shouldRevealPrevious -> previous
        else -> null
    }
    val foregroundOffset = when {
        pendingMotion?.isPop == true -> fraction
        pendingMotion != null -> 1f - fraction
        else -> backTransition.progress
    }
    val preview = background?.let { snapshots[it.id] }
    val backgroundOffset = when {
        pendingMotion?.isPop == true -> -0.25f * (1f - fraction)
        pendingMotion != null -> -0.25f * fraction
        else -> -0.25f * (1f - backTransition.progress)
    }
    LaunchedEffect(entries) {
        val next = navigationMotion(retained, entries)
        motion = next
        if (next != null) {
            if (!(next.isPop && backTransition.hasCommittedGesture)) {
                animation.snapTo(0f)
                animation.animateTo(1f, tween(240))
            }
        }
        retained = entries
        snapshots.keys.retainAll(entries.map { it.id }.toSet())
        motion = null
        backTransition.reset()
    }
    Box(modifier.fillMaxSize().clipToBounds().onSizeChanged { width = it.width }) {
        val isHomeForeground = foreground?.route?.isHomeRoute() == true
        val isHomeBackground = background?.route?.isHomeRoute() == true
        val isHomePlaced = isHomeForeground || (isHomeBackground && preview == null)
        state.homeRoute?.let { homeRoute ->
            RetainedRouteLayer(
                isPlaced = isHomePlaced,
                isForeground = pendingMotion == null && isHomeForeground && current?.route?.isHomeRoute() == true,
                modifier = Modifier.fillMaxSize().offset {
                    IntOffset(
                        (width * direction * if (isHomeBackground) backgroundOffset else if (isHomeForeground) foregroundOffset else 0f).roundToInt(),
                        0
                    )
                },
            ) {
                homeContent(homeRoute, state.renderedHomeRoutes, isHomePlaced)
            }
        }
        if (preview != null) {
            Image(
                bitmap = preview,
                contentDescription = null,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier.fillMaxSize().offset {
                    IntOffset((width * direction * backgroundOffset).roundToInt(), 0)
                },
            )
        }
        val rendered = (retained + entries).distinctBy { it.id }.filter { !it.route.isHomeRoute() }
        rendered.forEach { entry ->
            key(entry.id) {
                val isFront = entry.id == foreground?.id
                val isBehind = entry.id == background?.id
                RetainedRouteLayer(
                    isPlaced = isFront || (isBehind && preview == null),
                    isForeground = pendingMotion == null && entry.id == current?.id,
                    modifier = Modifier.fillMaxSize().offset {
                        val offset =
                            if (isFront) foregroundOffset else if (isBehind) backgroundOffset else 0f
                        IntOffset((width * direction * offset).roundToInt(), 0)
                    },
                ) {
                    overlayContent(entry.route)
                    if (isBehind) Box(
                        Modifier.fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.12f * (1f + backgroundOffset)))
                    )
                }
            }
        }
    }
}
