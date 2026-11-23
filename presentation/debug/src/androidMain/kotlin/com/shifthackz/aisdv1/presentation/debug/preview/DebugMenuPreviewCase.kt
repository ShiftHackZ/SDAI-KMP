package com.shifthackz.aisdv1.presentation.debug.preview

import com.shifthackz.aisdv1.presentation.debug.DebugMenuState

/**
 * Fixed developer-menu preferences, capabilities and initial scroll position.
 *
 * @param name Identifies the menu state in the preview picker.
 * @param state Preferences and supported sections rendered without application services.
 * @param scrollOffset Initial vertical offset in pixels for lower-section coverage.
 * @author Dmitriy Moroz
 */
internal data class DebugMenuPreviewCase(
    val name: String,
    val state: DebugMenuState,
    val scrollOffset: Int = 0,
)
