package com.shifthackz.aisdv1.presentation.debug.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.core.common.schedulers.SchedulersToken

/**
 * Supplies each persisted scheduler selection for the developer menu's choice sheet.
 *
 * @author Dmitriy Moroz
 */
internal class DebugSchedulerPreviewParameterProvider : PreviewParameterProvider<SchedulersToken> {
    /**
     * Every scheduler token supported by the actual selection body.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<SchedulersToken> = SchedulersToken.entries.asSequence()

    /**
     * Identifies the highlighted dispatcher choice.
     *
     * @param index Position in the scheduler declaration order.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = SchedulersToken.entries[index].type
}
