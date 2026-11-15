package com.shifthackz.aisdv1.presentation.theme.global

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
 * Observes the persisted theme projection without invalidating the application for unrelated edits.
 *
 * @author Dmitriy Moroz
 * @param diagnostics Injected fixed-capacity opt-in state/composition observer.
 * @param dispatchersProvider Dispatches initial preference collection and effect delivery.
 * @param preferenceManager Source of persisted application preferences.
 * @param onError Receives failures from preference observation.
 */
class AiSdAppThemeViewModel(
    diagnostics: UiDiagnostics,
    /**
     * Exposes the `dispatchersProvider` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
    private val dispatchersProvider: DispatchersProvider,
    preferenceManager: PreferenceManager,
    /**
     * Exposes the `onError` value used by the SDAI presentation layer.
     *
     * @author Dmitriy Moroz
     */
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
