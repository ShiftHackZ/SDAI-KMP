package com.shifthackz.aisdv1.presentation.configuration.setup.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.providerIcon
import com.shifthackz.aisdv1.presentation.components.model.readinessFor
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ProviderCatalogItem
import com.shifthackz.aisdv1.presentation.configuration.setup.component.subtitle
import com.shifthackz.aisdv1.presentation.configuration.setup.component.title
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.mappers.mapToUi

/**
 * Maps generation provider metadata into the shared configuration selection card.
 *
 * @param source Provider represented by this row.
 * @param isSelected Whether the current setup draft selects this provider.
 * @param platform Host selecting readiness, branding and platform labels.
 * @param strings Localized provider and metadata labels.
 * @param onClick Requests selecting the provider.
 * @param modifier Layout of the selectable card.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SourceModeItem(
    source: ServerSource, isSelected: Boolean, platform: Platform,
    strings: ServerSetupStrings, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    ProviderCatalogItem(
        title = source.title(strings, platform), subtitle = source.subtitle(strings, platform),
        icon = source.providerIcon(platform), readiness = source.readinessFor(platform),
        version = source.version, tags = source.featureTags.map { it.mapToUi() },
        isSelected = isSelected, strings = strings, onClick = onClick, modifier = modifier,
    )
}
