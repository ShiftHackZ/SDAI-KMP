package com.shifthackz.aisdv1.presentation.settings.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.settings.SettingsListItem
import com.shifthackz.aisdv1.presentation.settings.SettingsState

/**
 * Supplies provider visibility, capability, preference and lower-section states for Settings previews.
 * Lazy-list anchors ensure appearance, community and information rows are actually composed.
 *
 * @author Dmitriy Moroz
 */
internal class SettingsContentPreviewParameterProvider :
    PreviewParameterProvider<SettingsPreviewCase> {
    private val ready = SettingsPreviewFixtures.ready
    private val cases = listOf(
        SettingsPreviewCase("Loading", SettingsState()),
        SettingsPreviewCase("Automatic1111 · Model selected", ready),
        SettingsPreviewCase(
            "Automatic1111 · No models",
            ready.copy(sdModels = emptyList(), sdModelSelected = "")
        ),
        SettingsPreviewCase(
            "Stability AI · Credits",
            ready.copy(serverSource = ServerSource.STABILITY_AI, stabilityAiCredits = 123.4567f)
        ),
        SettingsPreviewCase(
            "Stability AI · Zero credits",
            ready.copy(serverSource = ServerSource.STABILITY_AI)
        ),
        SettingsPreviewCase(
            "ONNX · CPU",
            ready.copy(serverSource = ServerSource.LOCAL_MICROSOFT_ONNX)
        ),
        SettingsPreviewCase(
            "ONNX · NNAPI warning",
            ready.copy(serverSource = ServerSource.LOCAL_MICROSOFT_ONNX, localUseNNAPI = true)
        ),
        SettingsPreviewCase(
            "ONNX · NNAPI unavailable",
            ready.copy(serverSource = ServerSource.LOCAL_MICROSOFT_ONNX),
            isNnapiAvailable = false,
        ),
        SettingsPreviewCase(
            "MNN · Local provider",
            ready.copy(serverSource = ServerSource.LOCAL_MNN)
        ),
        SettingsPreviewCase(
            "SwarmUI · Connection monitoring",
            ready.copy(serverSource = ServerSource.SWARM_UI)
        ),
        SettingsPreviewCase(
            "OpenAI · Simplified controls",
            ready.copy(serverSource = ServerSource.OPEN_AI)
        ),
        SettingsPreviewCase(
            "Onboarding · Appearance only",
            ready.copy(onBoardingDemo = true),
            SettingsListItem.LOOK_HEADER
        ),
        SettingsPreviewCase(
            "Background generation · Enabled",
            ready.copy(backgroundGeneration = true, shouldDimScreen = true),
            SettingsListItem.BACKGROUND_GENERATION,
        ),
        SettingsPreviewCase(
            "Background generation · Unavailable",
            ready.copy(backgroundGenerationAvailable = false)
        ),
        SettingsPreviewCase(
            "Background generation · Preference warnings",
            ready.copy(
                backgroundGeneration = true,
                shouldDimScreen = true,
                autoSaveAiResults = false
            ),
            SettingsListItem.APP_HEADER,
        ),
        SettingsPreviewCase(
            "Preferences · Enabled",
            ready.copy(
                shouldDimScreen = true,
                formAdvancedOptionsAlwaysShow = true,
                formPromptTaggedInput = true
            ),
            SettingsListItem.APP_HEADER,
        ),
        SettingsPreviewCase(
            "Preferences · Disabled",
            ready.copy(
                monitorConnectivity = false,
                autoSaveAiResults = false,
                saveToMediaStore = false,
                shouldShowPerformanceOverlay = false
            ),
            SettingsListItem.APP_HEADER,
        ),
        SettingsPreviewCase("Appearance · Manual", ready, SettingsListItem.LOOK_HEADER),
        SettingsPreviewCase(
            "Appearance · System",
            ready.copy(useSystemDarkTheme = true),
            SettingsListItem.LOOK_HEADER
        ),
        SettingsPreviewCase(
            "Appearance · Loading",
            ready.copy(loading = true),
            SettingsListItem.LOOK_HEADER
        ),
        SettingsPreviewCase(
            "Appearance · Teal accent",
            ready.copy(colorToken = ColorToken.TEAL),
            SettingsListItem.ACCENT_COLOR
        ),
        SettingsPreviewCase("Integrations", ready, SettingsListItem.INTEGRATIONS_HEADER),
        SettingsPreviewCase("Community links", ready, SettingsListItem.COMMUNITY_HEADER),
        SettingsPreviewCase("Information links", ready, SettingsListItem.INFO_HEADER),
        SettingsPreviewCase(
            "Version footer",
            ready.copy(appVersion = "1.3.0-dev.20261123.abcdef123456 (240)"),
            SettingsListItem.VERSION
        ),
        SettingsPreviewCase(
            "Developer mode",
            ready.copy(developerMode = true),
            SettingsListItem.DEBUG_MENU
        ),
        SettingsPreviewCase(
            "Usage · Empty",
            ready.copy(storageUsageBytes = 0L, networkUsageBytes = 0L),
            SettingsListItem.STORAGE_USAGE
        ),
        SettingsPreviewCase(
            "Usage · Large totals",
            ready.copy(
                storageUsageBytes = 1_250_000_000_000L,
                networkUsageBytes = 9_650_000_000_000L
            ),
            SettingsListItem.STORAGE_USAGE,
        ),
    ) + DarkThemeToken.entries.map { token ->
        SettingsPreviewCase(
            "Appearance · $token palette",
            ready.copy(darkThemeToken = token),
            SettingsListItem.LOOK_HEADER
        )
    }

    /**
     * Fixed snapshots consumed by the light and dark content previews.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<SettingsPreviewCase> = cases.asSequence()

    /**
     * Names the provider, preference or list section shown by each preview.
     *
     * @param index Position in the ordered scenario sequence.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
