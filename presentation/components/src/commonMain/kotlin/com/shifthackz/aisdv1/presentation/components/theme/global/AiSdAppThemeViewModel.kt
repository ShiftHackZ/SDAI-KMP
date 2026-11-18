package com.shifthackz.aisdv1.presentation.components.theme.global

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticComponent
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.EmptyIntent
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * Observes appearance preferences and publishes only distinct shared theme configurations.
 *
 * @param diagnostics Optional bounded observer for theme-state transitions.
 * @param dispatchersProvider Runs preference observation on the immediate UI dispatcher.
 * @param preferenceManager Supplies persisted palette, accent, system-theme and dark-mode choices.
 * @param onError Receives appearance-preference observation failures.
 *
 * @author Dmitriy Moroz
 */
class AiSdAppThemeViewModel(
    diagnostics: UiDiagnostics,
    private val dispatchersProvider: DispatchersProvider,
    preferenceManager: PreferenceManager,
    private val onError: (Throwable) -> Unit = {},
) : BaseMviViewModel<AiSdAppThemeState, EmptyIntent, EmptyEffect>(
    diagnostics = diagnostics,
    diagnosticScreen = UiDiagnosticComponent.AI_SD_APP_THEME,
    initialState = AiSdAppThemeState(),
    effectDispatcher = dispatchersProvider.immediate,
) {

    init {
        launch(dispatchersProvider.immediate) {
            preferenceManager
                .observe()
                .map { settings ->
                    AiSdAppThemeState(
                        shouldUseSystemColorPalette = settings.designUseSystemColorPalette,
                        shouldUseSystemDarkTheme = settings.designUseSystemDarkTheme,
                        isDarkTheme = settings.designDarkTheme,
                        colorToken = ColorToken.parse(settings.designColorToken),
                        darkThemeToken = DarkThemeToken.parse(settings.designDarkThemeToken),
                    )
                }
                .distinctUntilChanged()
                .catch { t -> onError(t) }
                .collect { theme -> updateState { theme } }
        }
    }

    override fun processIntent(intent: EmptyIntent) = Unit
}
