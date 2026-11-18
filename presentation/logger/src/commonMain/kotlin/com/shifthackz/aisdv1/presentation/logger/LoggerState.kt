package com.shifthackz.aisdv1.presentation.logger

import androidx.compose.runtime.Immutable
import com.shifthackz.aisdv1.core.mvi.MviState

/**
 * Current application-log snapshot and loading status.
 *
 * @param loading Whether the log reader is loading a fresh snapshot.
 * @param text Displayed log text, empty before loading or after a failed read.
 *
 * @author Dmitriy Moroz
 */
@Immutable
data class LoggerState(
    val loading: Boolean = true,
    val text: String = "",
) : MviState
