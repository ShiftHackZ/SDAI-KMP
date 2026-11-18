package com.shifthackz.aisdv1.presentation.settings

/**
 * Structural categories for recycling compatible Settings rows and sharing prefetch estimates.
 * Row identity, saved state and navigation continue to use the distinct SettingsListItem keys.
 *
 * @author Dmitriy Moroz
 */
internal enum class SettingsContentType {
    HEADER,
    ROW,
    SWITCH,
    WARNING,
    COLOR_SELECTOR,
    FOOTER,
}
