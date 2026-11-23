package com.shifthackz.aisdv1.presentation.debug.preview

import android.content.res.Configuration
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken
import com.shifthackz.aisdv1.presentation.components.modal.ldscheduler.LDSchedulerBottomSheet

/**
 * Displays the real scheduler sheet body without opening a native modal or changing preferences.
 *
 * @param scheduler Dispatcher highlighted in the fixed selection state.
 * @author Dmitriy Moroz
 */
@Preview(name = "Scheduler · Light", group = "Debug scheduler", widthDp = 360, heightDp = 480)
@Preview(
    name = "Scheduler · Dark", group = "Debug scheduler", widthDp = 360, heightDp = 480,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DebugSchedulerSelectionPreview(
    @PreviewParameter(DebugSchedulerPreviewParameterProvider::class) scheduler: SchedulersToken,
) {
    DebugSchedulerPreview(scheduler)
}

/**
 * Displays narrow and enlarged-text scheduler choices.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact", group = "Debug scheduler sizes", widthDp = 320, heightDp = 480)
@Preview(
    name = "Large text",
    group = "Debug scheduler sizes",
    widthDp = 360,
    heightDp = 600,
    fontScale = 1.5f
)
@Composable
private fun DebugSchedulerSizesPreview() {
    DebugSchedulerPreview(SchedulersToken.COMPUTATION)
}

/**
 * Places the existing scheduler content at the lower edge of a themed preview surface.
 *
 * @param scheduler Current selection; preview taps leave it unchanged.
 * @author Dmitriy Moroz
 */
@Composable
private fun DebugSchedulerPreview(scheduler: SchedulersToken) {
    DebugMenuPreviewTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Surface(color = MaterialTheme.colorScheme.surface) {
                LDSchedulerBottomSheet(currentScheduler = scheduler, onSelected = {})
            }
        }
    }
}
