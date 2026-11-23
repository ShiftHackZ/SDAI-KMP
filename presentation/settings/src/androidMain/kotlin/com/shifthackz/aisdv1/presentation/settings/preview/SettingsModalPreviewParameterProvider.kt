package com.shifthackz.aisdv1.presentation.settings.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.presentation.settings.SettingsModal

/**
 * Supplies every Settings modal, including empty model choices and supported/unsupported timeout copy.
 * Confirmation and permission callbacks remain inert through the preview's intent and platform sinks.
 *
 * @author Dmitriy Moroz
 */
internal class SettingsModalPreviewParameterProvider :
    PreviewParameterProvider<SettingsPreviewCase> {
    private val ready = SettingsPreviewFixtures.ready
    private val modals = listOf(
        "Clear app cache" to SettingsModal.ClearAppCache,
        "Communicating" to SettingsModal.Communicating,
        "Language" to SettingsModal.Language,
        "Model · Selected" to SettingsModal.SelectSdModel(ready.sdModels, ready.sdModelSelected),
        "Model · No selection" to SettingsModal.SelectSdModel(ready.sdModels, ""),
        "Model · Empty list" to SettingsModal.SelectSdModel(emptyList(), ""),
        "Storage permission" to SettingsModal.ManualPermission("Storage"),
        "Notification permission" to SettingsModal.ManualPermission("Notifications"),
        "Connection timeout · Default" to SettingsModal.ConnectionTimeout(
            NetworkTimeout.TWO_MINUTES,
            supported = true
        ),
        "Connection timeout · Unlimited" to SettingsModal.ConnectionTimeout(
            NetworkTimeout.NO_LIMIT,
            supported = true
        ),
        "Connection timeout · Unsupported engine" to SettingsModal.ConnectionTimeout(
            NetworkTimeout.TWO_MINUTES,
            supported = false
        ),
    ) + Grid.entries.map { grid ->
        "Gallery grid · ${grid.size} columns" to SettingsModal.GalleryGrid(grid)
    } + NetworkTimeout.entries.map { timeout ->
        "Generation timeout · $timeout" to SettingsModal.GenerationTimeout(timeout)
    }
    private val cases = modals.map { (name, modal) ->
        SettingsPreviewCase(name = name, state = ready.copy(screenModal = modal))
    }

    /**
     * Ordered dialog and sheet snapshots used in both preview palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<SettingsPreviewCase> = cases.asSequence()

    /**
     * Names the dialog or picker selection displayed above Settings.
     *
     * @param index Position in the ordered modal sequence.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
