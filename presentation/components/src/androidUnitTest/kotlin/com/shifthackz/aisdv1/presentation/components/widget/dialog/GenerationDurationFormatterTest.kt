package com.shifthackz.aisdv1.presentation.components.widget.dialog

import com.shifthackz.aisdv1.core.localization.formatter.formatGenerationDuration
import org.junit.Assert.assertEquals
import org.junit.Test

class GenerationDurationFormatterTest {
    @Test
    fun `given generation elapsed time, clock crosses minute hour and day boundaries, expected padded duration without wrapping`() {
        // Given
        val cases = mapOf(
            -1L to "00:00", 0L to "00:00", 999L to "00:00", 1_000L to "00:01",
            59_999L to "00:59", 60_000L to "01:00", 2_441_000L to "40:41",
            3_599_999L to "59:59", 3_600_000L to "01:00:00", 3_662_000L to "01:01:02",
            86_400_000L to "24:00:00", 360_000_000L to "100:00:00",
        )
        // When
        val formatted = cases.keys.associateWith(::formatGenerationDuration)
        // Then
        assertEquals(cases, formatted)
    }
}
