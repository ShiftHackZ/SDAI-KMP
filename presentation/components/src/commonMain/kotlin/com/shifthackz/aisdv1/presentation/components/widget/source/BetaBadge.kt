package com.shifthackz.aisdv1.presentation.components.widget.source

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp

/**
 * Explains Beta status on long press while short taps reach the containing setting's action.
 * The 48dp help target has no separate click indication around the smaller visual badge.
 *
 * @param modifier Compose modifier applied to the badge.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun BetaBadge(
    modifier: Modifier = Modifier,
) {
    ContextualHelp(
        title = Localization.string("provider_readiness_beta"),
        message = Localization.string("settings_help_beta"),
        modifier = modifier,
    ) {
        Box(
            Modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp),
            contentAlignment = Alignment.Center
        ) {
            StatusBadge(
                text = Localization.string("provider_readiness_beta"),
                modifier = Modifier.padding(start = 6.dp),
                containerColor = Color(0xFFFFD54F),
                contentColor = Color(0xFF3B2F00),
            )
        }
    }
}
