package com.shifthackz.aisdv1.presentation.theme.global

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.ColorToken
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken

/**
 * Contains only preferences that determine the application theme. Equal theme selections
 * remain equal when unrelated preferences change, allowing the root theme to skip updates.
 *
 * @param shouldUseSystemColorPalette Whether Android dynamic colors replace the static palette.
 * @param shouldUseSystemDarkTheme Whether system appearance determines light or dark mode.
 * @param isDarkTheme Whether manual dark mode is selected when system appearance is disabled.
 * @param colorToken Selected static accent color.
 * @param darkThemeToken Selected static dark palette.
 * @author Dmitriy Moroz
 */
@Immutable
data class AiSdAppThemeState(
    val shouldUseSystemColorPalette: Boolean = false,
    val shouldUseSystemDarkTheme: Boolean = true,
    val isDarkTheme: Boolean = true,
    val colorToken: ColorToken = ColorToken.MAUVE,
    val darkThemeToken: DarkThemeToken = DarkThemeToken.FRAPPE,
) : MviState
