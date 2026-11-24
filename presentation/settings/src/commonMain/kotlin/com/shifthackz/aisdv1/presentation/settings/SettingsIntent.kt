package com.shifthackz.aisdv1.presentation.settings

import com.shifthackz.aisdv1.core.mvi.MviIntent
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.Grid

/**
 * Describes user actions and screen side-effect requests handled by SettingsViewModel.
 *
 * @author Dmitriy Moroz
 */
sealed interface SettingsIntent : MviIntent {

    /**
     * Opens the provider configuration flow from the Settings server section.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateConfiguration : SettingsIntent
    data object NavigateUpscalerConfiguration : SettingsIntent

    /**
     * Opens portable selective backup and restore.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBackup : SettingsIntent

    /**
     * Opens the generation waiting-limit picker.
     *
     * @author Dmitriy Moroz
     */
    data object PickGenerationTimeout : SettingsIntent

    /**
     * Opens the independent provider connection-timeout picker.
     *
     * @author Dmitriy Moroz
     */
    data object PickConnectionTimeout : SettingsIntent

    /**
     * Saves the connection limit without changing the generation waiting limit.
     *
     * @param timeout Selected connection limit, or no limit.
     * @author Dmitriy Moroz
     */
    data class SetConnectionTimeout(val timeout: NetworkTimeout) : SettingsIntent

    /**
     * Persists a waiting limit for subsequent network image generation requests.
     *
     * @param timeout Selected finite limit or no-limit option.
     * @author Dmitriy Moroz
     */
    data class SetGenerationTimeout(val timeout: NetworkTimeout) : SettingsIntent

    /**
     * Opens the benchmark screen from the Settings server section.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBenchmark : SettingsIntent

    /**
     * Opens Civitai account and publication preferences.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateCivitai : SettingsIntent

    /**
     * Opens the configured server's web interface inside SDAI.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateWebUi : SettingsIntent

    /**
     * Opens the native FAQ from the Settings information section.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateFaq : SettingsIntent

    /**
     * Opens the native blog from the information section.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateBlog : SettingsIntent

    data object NavigateStorageUsage : SettingsIntent

    data object NavigateNetworkUsage : SettingsIntent

    /**
     * Opens the debug/developer screen after developer mode is unlocked.
     *
     * @author Dmitriy Moroz
     */
    data object NavigateDeveloperMode : SettingsIntent

    /**
     * Groups actions for the Automatic1111 Stable Diffusion model selector.
     *
     * @author Dmitriy Moroz
     */
    sealed interface SdModel : SettingsIntent {
        /**
         * Opens the currently known remote model list in a modal picker.
         *
         * @author Dmitriy Moroz
         */
        data object OpenChooser : SdModel

        /**
         * Selects one model from the current remote model catalog.
         *
         * @param model Exact provider model identifier chosen in the picker.
         * @author Dmitriy Moroz
         */
        data class Select(val model: String) : SdModel
    }

    /**
     * Groups one-off Settings commands that are not simple boolean updates.
     *
     * @author Dmitriy Moroz
     */
    sealed interface Action : SettingsIntent {
        enum class ClearAppCache : Action {
            Request, Confirm
        }

        data object ReportProblem : Action

        data object AppVersion : Action

        data object PickLanguage : Action

        data class SetLanguage(val languageCode: String) : Action

        sealed interface GalleryGrid : Action {
            data object Pick : GalleryGrid

            data class Set(val grid: Grid) : GalleryGrid
        }

        data object Donate : Action

        data object OnBoarding : Action
    }

    /**
     * Groups external web links opened from Settings.
     *
     * @author Dmitriy Moroz
     */
    sealed interface LaunchUrl : SettingsIntent {
        data object OpenPolicy : LaunchUrl

        data object OpenSourceCode : LaunchUrl

        data object OpenProjectWebsite : LaunchUrl

        data object OpenDeveloperWebsite : LaunchUrl

        data object OpenLicense : LaunchUrl

        data object OpenTelegramCommunity : LaunchUrl

        data object OpenDiscordCommunity : LaunchUrl

        /**
         * Opens SDAI's public LinkedIn page through the current platform's URL handler.
         *
         * @author Dmitriy Moroz
         */
        data object OpenLinkedInCommunity : LaunchUrl
    }

    /**
     * Groups boolean Settings toggles persisted via PreferenceManager.
     *
     * @author Dmitriy Moroz
     */
    sealed interface UpdateFlag : SettingsIntent {
        /**
         * Desired value of the toggled preference.
         *
         * @author Dmitriy Moroz
         */
        val flag: Boolean

        /**
         * Persists use of the Android NNAPI acceleration backend for ONNX generation.
         *
         * @param flag Whether NNAPI should be enabled for subsequent requests.
         * @author Dmitriy Moroz
         */
        data class NNAPI(override val flag: Boolean) : UpdateFlag

        /**
         * Persists foreground generation display dimming.
         *
         * @param flag Whether dimming is enabled.
         * @author Dmitriy Moroz
         */
        data class DimScreen(override val flag: Boolean) : UpdateFlag

        /**
         * Persists visibility of resource graphs in generation dialogs.
         *
         * @param flag Whether the overlay is enabled.
         * @author Dmitriy Moroz
         */
        data class PerformanceOverlay(override val flag: Boolean) : UpdateFlag

        data class MonitorConnection(override val flag: Boolean) : UpdateFlag

        data class AutoSaveResult(override val flag: Boolean) : UpdateFlag

        data class BackgroundGeneration(override val flag: Boolean) : UpdateFlag

        data class SaveToMediaStore(override val flag: Boolean) : UpdateFlag

        data class TaggedInput(override val flag: Boolean) : UpdateFlag

        data class AdvancedFormVisibility(override val flag: Boolean) : UpdateFlag

        data class DynamicColors(override val flag: Boolean) : UpdateFlag

        data class SystemDarkTheme(override val flag: Boolean) : UpdateFlag

        data class DarkTheme(override val flag: Boolean) : UpdateFlag
    }

    data class NewColorToken(val token: ColorToken) : SettingsIntent
    data class NewDarkThemeToken(val token: DarkThemeToken) : SettingsIntent

    /**
     * Closes the currently visible Settings modal, if any.
     *
     * @author Dmitriy Moroz
     */
    data object DismissDialog : SettingsIntent

    /**
     * Requests opening the home drawer from Settings.
     *
     * @author Dmitriy Moroz
     */
    data object OpenDrawer : SettingsIntent
}
