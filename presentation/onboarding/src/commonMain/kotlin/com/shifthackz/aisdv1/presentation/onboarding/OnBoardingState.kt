package com.shifthackz.aisdv1.presentation.onboarding

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.DarkThemeToken

/**
 * Appearance and platform availability used to render the onboarding pager.
 *
 * @param darkThemeToken Saved dark palette demonstrated by the appearance page.
 * @param appVersion Version label shown by the settings preview.
 * @param showLocalDiffusionPage Whether the current platform/build has a local-generation page.
 * @author Dmitriy Moroz
 */
@Immutable
data class OnBoardingState(
    val darkThemeToken: DarkThemeToken = DarkThemeToken.FRAPPE,
    val appVersion: String = "",
    val showLocalDiffusionPage: Boolean = false,
) : MviState
