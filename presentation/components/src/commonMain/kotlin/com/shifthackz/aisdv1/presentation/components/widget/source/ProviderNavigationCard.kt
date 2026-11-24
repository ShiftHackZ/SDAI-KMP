package com.shifthackz.aisdv1.presentation.components.widget.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Shared provider navigation card for independent generation and upscaler configuration.
 *
 * @param name Selected provider name or a localized unavailable value.
 * @param description Contextual explanation of this provider and its configuration.
 * @param icon Platform-aware provider artwork.
 * @param onClick Opens the owning provider configuration wizard.
 * @param modifier Placement of the complete navigation card.
 * @param label Localized purpose displayed above the provider name.
 * @author Dmitriy Moroz
 */
@Composable
fun ProviderNavigationCard(
    name: String, description: String, icon: ImageVector,
    onClick: () -> Unit, modifier: Modifier = Modifier,
    label: String = Localization.string("title_provider"),
) {
    ContextualHelp(
        title = name, message = description, titleStartIcon = icon,
        modifier = modifier.fillMaxWidth()
    ) {
        PlatformSurface(
            onClick = onClick, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Row(
                Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            icon,
                            contentDescription = null
                        )
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(name, style = MaterialTheme.typography.titleMedium)
                }
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.padding(horizontal = 6.dp)
                )
            }
        }
    }
}
