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
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.compactDisplayName
import com.shifthackz.aisdv1.presentation.components.model.description
import com.shifthackz.aisdv1.presentation.components.model.providerIcon
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Identifies the active generation provider and offers one clear route to its configuration.
 * Local-provider artwork follows the current platform through the shared icon mapping.
 *
 * @param source Active provider retained by the screen's configuration state.
 * @param platform Host platform selecting the local-provider label and icon.
 * @param onClick Opens provider configuration through the owning screen's router.
 * @param modifier Placement of the complete navigation card.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun CurrentProviderCard(
    source: ServerSource,
    platform: Platform,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContextualHelp(
        title = source.compactDisplayName(platform),
        message = source.description(platform) + "\n\n" + Localization.string("help_provider_configuration"),
        titleStartIcon = source.providerIcon(platform),
        modifier = modifier.fillMaxWidth(),
    ) {
        PlatformSurface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        Icon(source.providerIcon(platform), contentDescription = null)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = Localization.string("title_provider"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = source.compactDisplayName(platform),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Icon(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                )
            }
        }
    }
}
