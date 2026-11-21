package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpIconButton

/**
 * Opens the saved-generation selector on a short tap and explains parameter reuse on long press.
 *
 * @param onClick Opens the owning screen's generation-history selector.
 * @param modifier Placement of the help anchor and its button.
 * @author Dmitriy Moroz
 */
@Composable
fun GenerationHistoryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContextualHelpIconButton(
        title = Localization.string("title_generation_history"),
        message = Localization.string("help_generation_history"),
        icon = Icons.Default.History,
        onClick = onClick,
        modifier = modifier,
    )
}
