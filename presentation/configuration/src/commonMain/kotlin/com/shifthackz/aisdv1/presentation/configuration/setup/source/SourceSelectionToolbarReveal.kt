package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    if (fraction <= 0f) return
    if (fraction >= 1f) {
        SourceSelectionToolbar(state, strings, processIntent, modifier)
        return
    }
    PlatformClippedContent(
        modifier.fillMaxWidth().height(SourceSelectionToolbarHeight * fraction)
    ) {
        Layout(
            modifier = Modifier.fillMaxWidth().clipToBounds(),
            content = {
                SourceSelectionToolbar(state, strings, processIntent = {
                    if (state.step == ServerSetupState.Step.SOURCE) processIntent(it)
                })
            },
        ) { measurables, constraints ->
            val fullHeight = SourceSelectionToolbarHeight.roundToPx()
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
