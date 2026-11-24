package com.shifthackz.aisdv1.presentation.faq

import com.shifthackz.aisdv1.core.mvi.MviState
import com.shifthackz.aisdv1.domain.entity.FaqContent

/**
 * Load states for the FAQ screen. A cached response still provides the complete browsing experience.
 *
 * @author Dmitriy Moroz
 */
internal sealed interface FaqState : MviState {
    /**
     * Shows shimmer while the initial load or a retry resolves online or saved answers.
     *
     * @author Dmitriy Moroz
     */
    data object Loading : FaqState

    /**
     * Offers Retry after a failed load leaves the screen without usable answers.
     *
     * @author Dmitriy Moroz
     */
    data object Error : FaqState

    /**
     * Displays answers and indicates whether they came from Room.
     *
     * @param content Online or cached FAQ categories.
     * @author Dmitriy Moroz
     */
    data class Ready(val content: FaqContent) : FaqState
}
