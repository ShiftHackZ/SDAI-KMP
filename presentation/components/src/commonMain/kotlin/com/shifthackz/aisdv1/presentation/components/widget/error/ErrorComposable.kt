package com.shifthackz.aisdv1.presentation.components.widget.error

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.presentation.components.model.ErrorState

/**
 * Resolves shared error state to localized inline error content; absent errors render nothing.
 *
 * @param modifier Layout modifiers for displayed error content.
 * @param state Generic or explicit-message error to present.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ErrorComposable(
    modifier: Modifier = Modifier,
    state: ErrorState,
) {
    if (state is ErrorState.None) return
    ErrorComposableContent(
        modifier = modifier,
        title = Localization.string("error_title"),
        message = when (state) {
            is ErrorState.WithMessage -> state.message.asString()
            else -> Localization.string("error_generic")
        },
    )
}
