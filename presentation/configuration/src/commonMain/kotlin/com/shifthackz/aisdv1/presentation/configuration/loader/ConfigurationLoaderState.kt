package com.shifthackz.aisdv1.presentation.configuration.loader

import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Current stage of startup provider/configuration preparation.
 *
 * @param status Initializing, fetching, failed or launching phase shown by the progress renderer.
 *
 * @author Dmitriy Moroz
 */
data class ConfigurationLoaderState(
    val status: Status = Status.Initializing,
) : MviState {
    enum class Status {
        Initializing,
        Fetching,
        Failed,
        Launching,
    }
}
