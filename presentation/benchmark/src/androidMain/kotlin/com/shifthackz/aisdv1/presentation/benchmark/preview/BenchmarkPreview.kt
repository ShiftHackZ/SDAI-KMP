package com.shifthackz.aisdv1.presentation.benchmark.preview

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.presentation.benchmark.BenchmarkScreenContent
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppTheme
import com.shifthackz.aisdv1.presentation.components.theme.global.AiSdAppThemeState

/**
 * Shows the stateless benchmark renderer at the selected section with inert run/share/navigation actions.
 * No ViewModel, Koin graph, hardware probe, benchmark workload or native share flow is started.
 *
 * @param scenario Fixed score/device data, operation state and initially visible section.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BenchmarkPreview(scenario: BenchmarkPreviewCase) {
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = scenario.sectionIndex)
    AiSdAppTheme(
        state = AiSdAppThemeState(
            shouldUseSystemDarkTheme = false,
            isDarkTheme = isSystemInDarkTheme(),
            shouldUseSystemColorPalette = false,
        ),
        applySystemBars = false,
    ) {
        BenchmarkScreenContent(
            state = scenario.state,
            modifier = Modifier.fillMaxSize(),
            processIntent = {},
            listState = listState,
        )
    }
}
