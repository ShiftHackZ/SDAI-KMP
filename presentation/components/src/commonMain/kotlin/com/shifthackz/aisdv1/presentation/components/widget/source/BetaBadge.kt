package com.shifthackz.aisdv1.presentation.components.widget.source

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
            Text(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .background(
                        color = Color(0xFFFFD54F),
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                text = Localization.string("provider_readiness_beta"),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.W500,
                color = Color(0xFF3B2F00),
            )
        }
    }
}
