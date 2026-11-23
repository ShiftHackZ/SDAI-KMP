package com.shifthackz.aisdv1.presentation.benchmark

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Shows benchmark failure feedback with the existing acknowledgement action.
 *
 * @param message Localized failure supplied by benchmark state.
 * @param onDismiss Requests clearing the failure without starting another benchmark.
 * @param modifier Card placement above the benchmark content.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BenchmarkErrorCard(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlatformSurface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 6.dp,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = message,
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
            OutlinedButton(
                onClick = onDismiss,
            ) {
                Text(Localization.string("ok"))
            }
        }
    }
}
