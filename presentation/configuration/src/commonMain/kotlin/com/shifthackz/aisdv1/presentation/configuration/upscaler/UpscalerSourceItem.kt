package com.shifthackz.aisdv1.presentation.configuration.upscaler

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.components.model.description
import com.shifthackz.aisdv1.presentation.components.model.displayName
import com.shifthackz.aisdv1.presentation.components.model.providerIcon
import com.shifthackz.aisdv1.presentation.configuration.setup.component.ProviderCatalogItem
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings

/**
 * Maps independent upscaler metadata into the same provider card as generation configuration.
 *
 * @param source Provider represented by this row.
 * @param platform Host selecting local branding and readiness.
 * @param isSelected Whether this provider is the current draft.
 * @param strings Shared wizard and metadata labels.
 * @param onClick Requests selecting the provider without advancing.
 * @param modifier Layout of the selectable card.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerSourceItem(
    source: UpscalerSource, platform: Platform, isSelected: Boolean,
    strings: ServerSetupStrings, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    ProviderCatalogItem(
        title = source.displayName, subtitle = source.description,
        icon = source.providerIcon(platform), readiness = source.readiness[platform],
        version = source.version, tags = source.featureTags.map { it.mapToUi() },
        isSelected = isSelected, strings = strings, onClick = onClick, modifier = modifier,
    )
}
