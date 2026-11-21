package com.shifthackz.aisdv1.presentation.components.widget.work

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * Places status beside its percentage above a full-width bar; absent status leaves the percentage left-aligned.
 *
 * @param status Optional localized provider stage or sampling counters; blank text is omitted.
 * @param percentage Measured completion from zero to one hundred.
 * @param modifier Layout and surface styling supplied by the background-work card.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BackgroundGenerationProgressContent(
    status: String?,
    percentage: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!status.isNullOrBlank()) Text(
                text = status,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${percentage.coerceIn(0, 100)}%",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
        }
        LinearProgressIndicator(
            progress = { percentage.coerceIn(0, 100) / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
