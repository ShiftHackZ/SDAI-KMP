package com.shifthackz.aisdv1.presentation.components.widget.source

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.components.model.compactDisplayName
import com.shifthackz.aisdv1.presentation.components.model.description
import com.shifthackz.aisdv1.presentation.components.model.providerIcon

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
    ProviderNavigationCard(
        label = Localization.string("generation_provider"),
        name = source.compactDisplayName(platform),
        description = source.description(platform) + "\n\n" + Localization.string("help_provider_configuration"),
        icon = source.providerIcon(platform), onClick = onClick, modifier = modifier,
    )
}
