package com.shifthackz.aisdv1.presentation.generation.preview.performance

import com.shifthackz.aisdv1.presentation.generation.preview.GenerationPreviewTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.generation.performance.GenerationPerformanceContent

/**
 * Displays the real resource graphs and legend with fixed sample history.
 *
 * @param scenario Captured app/device readings and missing-value gaps.
 * @author Dmitriy Moroz
 */
@Composable
internal fun PerformancePreview(scenario: PerformancePreviewCase) {
    GenerationPreviewTheme {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            GenerationPerformanceContent(samples = scenario.samples)
        }
    }
}
