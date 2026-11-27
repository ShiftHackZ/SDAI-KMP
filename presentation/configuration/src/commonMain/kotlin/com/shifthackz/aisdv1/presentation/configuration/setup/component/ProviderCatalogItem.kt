@file:OptIn(ExperimentalLayoutApi::class)

package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.source.SourceMetaChip
import com.shifthackz.aisdv1.presentation.configuration.setup.source.containerColor
import com.shifthackz.aisdv1.presentation.configuration.setup.source.contentColor
import com.shifthackz.aisdv1.presentation.configuration.setup.source.mapToUi

/**
 * Shared provider card geometry, selection outline and metadata used by both configuration pickers.
 *
 * @param title Localized provider name.
 * @param subtitle Localized description of its execution or hosting model.
 * @param icon Platform-specific provider artwork.
 * @param readiness Maturity resolved for the current host.
 * @param version Implementation update version.
 * @param tags Localized capability labels.
 * @param isSelected Whether the current draft selects this provider.
 * @param strings Shared metadata label formatting.
 * @param onClick Requests selecting this provider.
 * @param modifier Layout of the entire selectable card.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderCatalogItem(
    title: String, subtitle: String, icon: ImageVector,
    readiness: ServerSourceReadiness, version: String, tags: List<String>,
    isSelected: Boolean, strings: ServerSetupStrings, onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProviderSelectionCard(isSelected = isSelected, onClick = onClick, modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
        ) {
            Row {
                Icon(
                    modifier = Modifier
                        .size(42.dp)
                        .padding(top = 8.dp, bottom = 8.dp),
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(vertical = 8.dp),
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.W500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                modifier = Modifier.padding(4.dp),
            ) {
                SourceMetaChip(
                    text = readiness.mapToUi(strings),
                    containerColor = readiness.containerColor(),
                    contentColor = readiness.contentColor(),
                )
                SourceMetaChip(
                    text = strings.sourceVersion(version),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                tags.forEach { tag ->
                    Text(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceTint,
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = tag,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.W300,
                    )
                }
            }
        }
    }
}
