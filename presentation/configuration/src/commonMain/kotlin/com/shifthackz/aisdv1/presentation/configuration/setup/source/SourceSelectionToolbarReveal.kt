package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import com.shifthackz.aisdv1.presentation.components.widget.input.SearchFilterToolbarHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.Layout
import com.shifthackz.aisdv1.presentation.components.platform.PlatformClippedContent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import kotlin.math.roundToInt

/**
 * Reveals search downward from the step indicator and retracts it upward without squeezing controls.
 * Native and Compose chrome consume the same fraction, keeping the backdrop and body inset aligned.
 * At rest the editor stays in its original composition so text updates retain immediate feedback.
 *
 * @param fraction Visible proportion from zero through one.
 * @param state Current query and filter indicators.
 * @param strings Localized search and action labels.
 * @param processIntent Search/filter/sort actions owned by the wizard.
 * @param modifier Placement below the fixed wizard step indicator.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceSelectionToolbarReveal(
    fraction: Float,
    state: ServerSetupState,
    strings: ServerSetupStrings,
    processIntent: (ServerSetupIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    ProviderDiscoveryToolbarReveal(fraction, modifier) {
        SourceSelectionToolbar(state, strings, processIntent = {
            if (state.step == ServerSetupState.Step.SOURCE) processIntent(it)
        })
    }
}

/**
 * Keeps discovery controls at their full size while revealing the same native and Compose height.
 *
 * @param fraction Visible proportion from zero through one.
 * @param modifier Placement under the fixed step strip.
 * @param content Discovery row retained by the owning provider picker.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderDiscoveryToolbarReveal(
    fraction: Float, modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (fraction <= 0f) return
    if (fraction >= 1f) {
        Box(modifier) { content() }
        return
    }
    PlatformClippedContent(
        modifier.fillMaxWidth().height(SearchFilterToolbarHeight * fraction)
    ) {
        Layout(
            modifier = Modifier.fillMaxWidth().clipToBounds(),
            content = content,
        ) { measurables, constraints ->
            val fullHeight = SearchFilterToolbarHeight.roundToPx()
            val visibleHeight = (fullHeight * fraction.coerceIn(0f, 1f)).roundToInt()
            val row = measurables.single().measure(
                constraints.copy(minHeight = fullHeight, maxHeight = fullHeight)
            )
            layout(row.width, visibleHeight) {
                row.placeRelative(0, visibleHeight - fullHeight)
            }
        }
    }
}
