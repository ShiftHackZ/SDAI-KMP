package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Describes a destination without owning navigation or retaining screen state.
 *
 * @param label Localized text displayed beneath the icon and announced by accessibility.
 * @param icon Shared destination vector, identical on both platforms.
 * @param isSelected Whether the caller currently displays this destination.
 * @param helpMessage Localized explanation shown on long press or an accessibility help action.
 * @param onClick Requests selection through the caller's existing navigation or intent handler.
 * @author Dmitriy Moroz
 */
data class PlatformNavigationBarItem(
    val label: String,
    val icon: ImageVector,
    val isSelected: Boolean,
    val helpMessage: String,
    val onClick: () -> Unit,
)
