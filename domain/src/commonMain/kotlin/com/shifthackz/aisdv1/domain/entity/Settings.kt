package com.shifthackz.aisdv1.domain.entity

import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken

/**
 * Observable application preferences including the selected generation source.
 *
 * @param shouldDimScreen Whether active foreground generation lowers screen brightness.
 * @param providerSelectionRevision Durable source revision used to invalidate earlier queued work.
 *
 * @author Dmitriy Moroz
 */
data class Settings(
    /**
     * Durable source-selection identity used to invalidate work even after an away/back switch.
     *
     * @author Dmitriy Moroz
     */
    val providerSelectionRevision: Long = 0L,
    /**
     * Deadline for provider setup and metadata requests; defaults to 120 seconds.
     *
     * @author Dmitriy Moroz
     */
    val connectionTimeout: NetworkTimeout = NetworkTimeout.TWO_MINUTES,
    /**
     * Request and socket waiting limit for network image generation.
     *
     * @author Dmitriy Moroz
     */
    val generationRequestTimeout: NetworkTimeout = NetworkTimeout.TWO_MINUTES,
    /**
     * AUTOMATIC1111 server base URL.
     *
     * @author Dmitriy Moroz
     */
    val serverUrl: String = "",
    /**
     * Selected server checkpoint identifier.
     *
     * @author Dmitriy Moroz
     */
    val sdModel: String = "",
    /**
     * Whether bundled demonstration data is active.
     *
     * @author Dmitriy Moroz
     */
    val demoMode: Boolean = false,
    /**
     * Whether developer tools are visible.
     *
     * @author Dmitriy Moroz
     */
    val developerMode: Boolean = false,
    /**
     * Whether local inference exposes cancellation controls.
     *
     * @author Dmitriy Moroz
     */
    val localDiffusionAllowCancel: Boolean = false,
    /**
     * Coroutine scheduler selected for local inference.
     *
     * @author Dmitriy Moroz
     */
    val localDiffusionSchedulerThread: SchedulersToken = SchedulersToken.COMPUTATION,
    /**
     * Whether server connectivity is monitored.
     *
     * @author Dmitriy Moroz
     */
    val monitorConnectivity: Boolean = false,
    /**
     * Whether new generation requests use background scheduling.
     *
     * @author Dmitriy Moroz
     */
    val backgroundGeneration: Boolean = false,
    /**
     * Enables minimum display brightness during active foreground generation.
     *
     * @author Dmitriy Moroz
     */
    val shouldDimScreen: Boolean = false,
    /**
     * Whether completed images are automatically persisted to the gallery.
     *
     * @author Dmitriy Moroz
     */
    val autoSaveAiResults: Boolean = false,
    /**
     * Whether saved images are also exported to the device media library.
     *
     * @author Dmitriy Moroz
     */
    val saveToMediaStore: Boolean = false,
    /**
     * Whether generation forms initially expand advanced controls.
     *
     * @author Dmitriy Moroz
     */
    val formAdvancedOptionsAlwaysShow: Boolean = false,
    /**
     * Whether prompt editors display tags.
     *
     * @author Dmitriy Moroz
     */
    val formPromptTaggedInput: Boolean = false,
    /**
     * Selected generation provider.
     *
     * @author Dmitriy Moroz
     */
    val source: ServerSource = ServerSource.AUTOMATIC1111,
    /**
     * Credential used for Horde generation requests.
     *
     * @author Dmitriy Moroz
     */
    val hordeApiKey: String = "",
    /**
     * Selected Arli AI model identifier.
     *
     * @author Dmitriy Moroz
     */
    val arliAiModel: String = "",
    /**
     * Whether ONNX inference requests Android NNAPI acceleration.
     *
     * @author Dmitriy Moroz
     */
    val localUseNNAPI: Boolean = false,
    /**
     * Whether supported platforms use system dynamic colors.
     *
     * @author Dmitriy Moroz
     */
    val designUseSystemColorPalette: Boolean = false,
    /**
     * Whether appearance follows system dark mode.
     *
     * @author Dmitriy Moroz
     */
    val designUseSystemDarkTheme: Boolean = false,
    /**
     * Manual dark-mode preference when system appearance is disabled.
     *
     * @author Dmitriy Moroz
     */
    val designDarkTheme: Boolean = false,
    /**
     * Selected accent palette token.
     *
     * @author Dmitriy Moroz
     */
    val designColorToken: String = "",
    /**
     * Selected dark background palette token.
     *
     * @author Dmitriy Moroz
     */
    val designDarkThemeToken: String = "",
    /**
     * Gallery column layout preference.
     *
     * @author Dmitriy Moroz
     */
    val galleryGrid: Grid = Grid.Fixed2,
    /**
     * Preferred language code; empty follows the platform language.
     *
     * @author Dmitriy Moroz
     */
    val languageCode: String = "",
)
