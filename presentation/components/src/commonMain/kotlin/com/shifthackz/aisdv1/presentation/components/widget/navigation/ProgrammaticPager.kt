package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlVisibility
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Animates externally selected peer pages without accepting swipe navigation or changing history.
 * Retains page compositions; only intersecting pages mount native controls and collect visible state.
 * The selected, settled page owns input and accessibility.
 *
 * @param selectedPage Index selected by the screen's existing state owner.
 * @param pageCount Fixed number of peer pages.
 * @param modifier Bounds of the page viewport.
 * @param shouldAnimate Whether to animate selection or synchronize after an interactive transition.
 * @param backProgress Optional interactive movement from the selected page to its preceding page.
 * @param content Retained content for each page index.
 * @author Dmitriy Moroz
 */
@Composable
fun ProgrammaticPager(
    selectedPage: Int,
    pageCount: Int,
    modifier: Modifier = Modifier,
    shouldAnimate: Boolean = true,
    backProgress: Float? = null,
    content: @Composable (Int) -> Unit,
) {
    val pager = rememberPagerState(initialPage = selectedPage, pageCount = { pageCount })
    val isParentVisible = LocalMviVisibility.current
    val areParentControlsVisible = LocalPlatformControlVisibility.current
    val isParentInputEnabled = LocalNavigationInputEnabled.current
    LaunchedEffect(selectedPage, shouldAnimate, backProgress) {
        if (backProgress != null && selectedPage > 0) {
            val position = selectedPage - backProgress.coerceIn(0f, 1f)
            val page = position.roundToInt()
            pager.scrollToPage(page, position - page)
        } else if (shouldAnimate) pager.animateScrollToPage(
            selectedPage,
            animationSpec = tween(240)
        )
        else pager.scrollToPage(selectedPage)
    }
    HorizontalPager(
        state = pager,
        modifier = modifier,
        userScrollEnabled = false,
        beyondViewportPageCount = pageCount - 1,
        key = { it },
    ) { page ->
        val isVisible = abs(page - pager.currentPage - pager.currentPageOffsetFraction) < 1f
        val isInteractive =
            page == selectedPage && !pager.isScrollInProgress && backProgress == null
        CompositionLocalProvider(
            LocalMviVisibility provides (isParentVisible && isVisible),
            LocalPlatformControlVisibility provides (areParentControlsVisible && isVisible),
            LocalNavigationInputEnabled provides (isParentInputEnabled && isInteractive),
        ) {
            Box(
                if (isInteractive) Modifier.fillMaxSize() else Modifier.fillMaxSize()
                    .clearAndSetSemantics {}
                    .pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) awaitPointerEvent(PointerEventPass.Initial).changes.forEach { it.consume() }
                        }
                    }) {
                content(page)
            }
        }
    }
}
