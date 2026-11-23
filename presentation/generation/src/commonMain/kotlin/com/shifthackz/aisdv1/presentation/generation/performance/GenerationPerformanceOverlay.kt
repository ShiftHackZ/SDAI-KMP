package com.shifthackz.aisdv1.presentation.generation.performance

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.shifthackz.aisdv1.core.mvi.LocalMviVisibility
import com.shifthackz.aisdv1.domain.entity.PerformanceSample
import kotlinx.coroutines.flow.Flow

/**
 * Collects resource history only while the generation dialog's tab is visible and resumed.
 * Removing the dialog or backgrounding the app cancels collection and discards its history.
 *
 * @param history Cold preference-controlled sampler supplied by the application composition root.
 * @param modifier Layout applied to the resource panel.
 * @author Dmitriy Moroz
 */
@Composable
fun GenerationPerformanceOverlay(
    history: Flow<List<PerformanceSample>>,
    modifier: Modifier = Modifier,
) {
    val isVisible = LocalMviVisibility.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    var samples by remember(history) { mutableStateOf(emptyList<PerformanceSample>()) }
    LaunchedEffect(history, isVisible, lifecycle) {
        samples = emptyList()
        if (isVisible) lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            try {
                history.collect { samples = it }
            } finally {
                samples = emptyList()
            }
        }
    }
    GenerationPerformanceContent(samples = samples, modifier = modifier)
}
