package com.shifthackz.aisdv1.presentation.faq

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Requests another online fetch after an error or recovery from the cached copy.
 *
 * @author Dmitriy Moroz
 */
internal enum class FaqIntent : MviIntent {
    Retry,
}
