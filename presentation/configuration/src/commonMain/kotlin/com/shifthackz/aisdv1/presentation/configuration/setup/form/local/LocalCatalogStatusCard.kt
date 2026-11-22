package com.shifthackz.aisdv1.presentation.configuration.setup.form.local

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.shifthackz.aisdv1.presentation.components.platform.LocalPlatformControlBackgroundColor
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.configuration.setup.LocalCatalogStatus

/**
 * Places shared catalog feedback and recovery beside the affected model list, retaining native theme typography.
 *
 * @param status Refresh lifecycle of this local provider, with READY used for an empty remote catalog.
 * @param isRefreshing Whether a retry is running while this card retains its title, message and action size.
 * @param onRetry Retries this provider without changing setup inputs or another provider's requests.
 * @param modifier Layout modifiers for provider-specific catalog feedback and retry.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun LocalCatalogStatusCard(
    status: LocalCatalogStatus,
    isRefreshing: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val hasFailed = status == LocalCatalogStatus.ERROR
    val title = Localization.string(
        when {
            hasFailed -> "local_catalog_error_title"
            else -> "local_catalog_empty_title"
        }
    )
    Surface(
        modifier = modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        CompositionLocalProvider(LocalPlatformControlBackgroundColor provides MaterialTheme.colorScheme.surfaceVariant) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (hasFailed) Icons.Outlined.CloudOff else Icons.Outlined.Inventory2,
                        contentDescription = null,
                        tint = if (hasFailed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Text(
                    Localization.string(if (hasFailed) "local_catalog_error_body" else "local_catalog_empty_body"),
                    style = MaterialTheme.typography.bodyMedium,
                )
                OutlinedButton(
                    onClick = onRetry,
                    isEnabled = !isRefreshing,
                    modifier = Modifier.align(Alignment.CenterHorizontally).widthIn(min = 144.dp)
                        .heightIn(min = 48.dp),
                ) {
                    Text(
                        Localization.string(if (isRefreshing) "loading" else "retry"),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}
