package com.shifthackz.aisdv1.presentation.home

import com.shifthackz.aisdv1.core.mvi.MviIntent

/**
 * Drawer visibility requests forwarded to the root navigation owner.
 *
 * @author Dmitriy Moroz
 */
sealed interface DrawerIntent : MviIntent {
    /**
     * Requests that the navigation drawer open.
     *
     * @author Dmitriy Moroz
     */
    data object Open : DrawerIntent

    /**
     * Requests that the navigation drawer close.
     *
     * @author Dmitriy Moroz
     */
    data object Close : DrawerIntent
}
