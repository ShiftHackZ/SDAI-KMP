package com.shifthackz.aisdv1.core.localization.formatter

/**
 * Formats nonnegative generation elapsed time as mm:ss, adding a padded hours field when needed.
 *
 * @param milliseconds Elapsed monotonic duration, with negative values clamped to zero.
 * @return Locale-independent clock text; hours are unbounded and never wrap at a day.
 * @author Dmitriy Moroz
 */
fun formatGenerationDuration(milliseconds: Long): String {
    val seconds = milliseconds.coerceAtLeast(0) / 1000
    val minutes = (seconds / 60 % 60).toString().padStart(2, '0')
    val remainder = (seconds % 60).toString().padStart(2, '0')
    val hours = seconds / 3600
    return if (hours > 0) "${
        hours.toString().padStart(2, '0')
    }:$minutes:$remainder" else "$minutes:$remainder"
}
