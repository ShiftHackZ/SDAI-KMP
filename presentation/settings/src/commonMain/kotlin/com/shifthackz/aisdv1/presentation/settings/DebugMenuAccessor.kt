package com.shifthackz.aisdv1.presentation.settings

/**
 * Retains the version-tap sequence used to unlock developer navigation from Settings.
 *
 * @author Dmitriy Moroz
 */
interface DebugMenuAccessor {
    /**
     * Records one tap and persists developer access when the unlock threshold is reached.
     *
     * @return Whether developer navigation is already unlocked or becomes unlocked on this tap.
     * @author Dmitriy Moroz
     */
    operator fun invoke(): Boolean
}
