package com.shifthackz.aisdv1.presentation.generation.modal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.shifthackz.aisdv1.core.localization.formatter.formatGenerationDuration
import kotlinx.coroutines.delay
import kotlin.time.TimeMark

/**
 * Recomputes elapsed monotonic time without accumulating ticker drift or resetting on stage updates.
 *
 * @param startedAt Start owned by the generation screen state.
 * @return mm:ss, or hh:mm:ss after the first hour.
 * @author Dmitriy Moroz
 */
@Composable
internal fun generationElapsedText(startedAt: TimeMark): String {
    var elapsedMillis by remember(startedAt) { mutableLongStateOf(startedAt.elapsedNow().inWholeMilliseconds) }
    LaunchedEffect(startedAt) {
        while (true) {
            elapsedMillis = startedAt.elapsedNow().inWholeMilliseconds
            delay(250)
        }
    }
    return formatGenerationDuration(elapsedMillis)
}
