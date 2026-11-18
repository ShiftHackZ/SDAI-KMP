package com.shifthackz.aisdv1.presentation.settings

import com.shifthackz.aisdv1.domain.preference.PreferenceManager

/**
 * Counts repeated version taps and persists developer-mode access after the seventh tap.
 * The counter belongs to this accessor instance; an already unlocked preference grants immediate access.
 *
 * @param preferenceManager Reads existing developer access and persists the unlocked state.
 *
 * @author Dmitriy Moroz
 */
internal class DebugMenuAccessorImpl(

    private val preferenceManager: PreferenceManager,
) : DebugMenuAccessor {


    private var tapCount = 0

    /**
     * Records one access tap and reports whether developer navigation is now allowed.
     *
     * @return True for an already unlocked preference or the tap that reaches the unlock threshold.
     *
     * @author Dmitriy Moroz
     */
    override operator fun invoke(): Boolean {
        if (preferenceManager.developerMode) {
            return true
        }
        tapCount++
        if (tapCount >= DEBUG_MENU_ACCESS_TAPS) {
            tapCount = 0
            preferenceManager.developerMode = true
            return true
        }
        return false
    }


    private companion object {

        const val DEBUG_MENU_ACCESS_TAPS = 7
    }
}
