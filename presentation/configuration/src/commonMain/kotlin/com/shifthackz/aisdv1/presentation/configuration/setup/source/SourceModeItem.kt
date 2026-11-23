@file:OptIn(ExperimentalLayoutApi::class)

package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.providerIcon
import com.shifthackz.aisdv1.presentation.components.model.readinessFor
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface
import com.shifthackz.aisdv1.presentation.configuration.setup.component.subtitle
import com.shifthackz.aisdv1.presentation.configuration.setup.component.title
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.mappers.mapToUi

/**
 * Renders one provider option in the provider selection list.
 *
 * @param source Provider represented by this row.
 * @param selected Whether the current setup draft selects this provider.
 * @param platform Current host selecting provider readiness, local branding and platform labels.
 * @param strings Localized labels used by the provider title, subtitle and metadata.
 * @param onClick Requests selecting this provider in the owning setup flow.
 * @param modifier Layout modifiers for this selectable provider card.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceModeItem(
    source: ServerSource,
    selected: Boolean,
    platform: Platform,
    strings: ServerSetupStrings,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    val readiness = source.readinessFor(platform)
    PlatformSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            null
        },
    ) {
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
                    imageVector = source.providerIcon(platform),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(vertical = 8.dp),
                    text = source.title(strings, platform),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            Text(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = source.subtitle(strings, platform),
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
                    text = strings.sourceVersion(source.version),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                source.featureTags.forEach { tag ->
                    Text(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceTint,
                                shape = RoundedCornerShape(4.dp),
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        text = tag.mapToUi(),
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

@Composable
private fun SourceMetaChip(
    text: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .padding(4.dp)
            .background(
                color = containerColor,
                shape = RoundedCornerShape(4.dp),
            )
            .padding(horizontal = 4.dp, vertical = 2.dp),
        text = text,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.W500,
        color = contentColor,
    )
}
