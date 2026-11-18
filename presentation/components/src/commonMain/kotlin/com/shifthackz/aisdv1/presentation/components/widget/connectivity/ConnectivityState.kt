package com.shifthackz.aisdv1.presentation.components.widget.connectivity

import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Connection state and indicator visibility consumed by the home status widget.
 *
 * @author Dmitriy Moroz
 */
sealed interface ConnectivityState : MviState {

    /**
     * Whether the current provider permits showing the connection indicator.
     *
     * @author Dmitriy Moroz
     */
    val enabled: Boolean

    /**
     * No connection result has arrived yet.
     *
     * @param enabled Whether the initial-check indicator is shown.
     *
     * @author Dmitriy Moroz
     */
    data class Uninitialized(override val enabled: Boolean = true) : ConnectivityState

    /**
     * The selected provider is reachable.
     *
     * @param enabled Whether the connected indicator is shown.
     *
     * @author Dmitriy Moroz
     */
    data class Connected(override val enabled: Boolean = true) : ConnectivityState

    /**
     * The selected provider is unreachable.
     *
     * @param enabled Whether the disconnected indicator is shown.
     *
     * @author Dmitriy Moroz
     */
    data class Disconnected(override val enabled: Boolean = true) : ConnectivityState

    companion object {
        /**
         * Maps a connectivity result and indicator-visibility flag to display state.
         *
         * @param value Pair of provider reachability followed by indicator visibility.
         * @return Connected or disconnected display state retaining the supplied visibility.
         *
         * @author Dmitriy Moroz
         */
        fun consume(value: Pair<Boolean, Boolean>): ConnectivityState {
            val (connected, enabled) = value
            return if (connected) Connected(enabled) else Disconnected(enabled)
        }
    }
}
