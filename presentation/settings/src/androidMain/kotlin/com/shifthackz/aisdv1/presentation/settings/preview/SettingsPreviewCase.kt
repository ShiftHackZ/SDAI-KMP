package com.shifthackz.aisdv1.presentation.settings.preview

import com.shifthackz.aisdv1.presentation.settings.SettingsListItem
import com.shifthackz.aisdv1.presentation.settings.SettingsState

/**
 * Describes one Settings preview without retaining a ViewModel or platform service.
 *
 * @param name Human-readable label displayed by the preview parameter provider.
 * @param state Fixed preferences, provider values and optional modal to display.
 * @param firstVisibleRow Row positioned at the top so lower lazy sections are composed in previews.
 * @param isNnapiAvailable Whether this scenario exposes the ONNX hardware-acceleration controls.
 * @param supportsConnectionTimeout Whether this scenario exposes the independent connection limit.
 * @author Dmitriy Moroz
 */
internal data class SettingsPreviewCase(
    val name: String,
    val state: SettingsState,
    val firstVisibleRow: SettingsListItem = SettingsListItem.SERVER_HEADER,
    val isNnapiAvailable: Boolean = true,
    val supportsConnectionTimeout: Boolean = true,
)
