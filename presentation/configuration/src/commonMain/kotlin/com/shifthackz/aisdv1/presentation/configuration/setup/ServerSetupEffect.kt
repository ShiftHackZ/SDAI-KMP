package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.core.mvi.MviEffect

/**
 * Native UI actions emitted once by the provider setup wizard: dismiss the keyboard, request
 * Android storage management permission or open a supplied external URL.
 *
 * @author Dmitriy Moroz
 */
sealed interface ServerSetupEffect : MviEffect {
    data object HideKeyboard : ServerSetupEffect
    data object LaunchManageStoragePermission : ServerSetupEffect
    data class OpenUrl(val url: String) : ServerSetupEffect
}
