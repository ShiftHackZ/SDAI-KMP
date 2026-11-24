package com.shifthackz.aisdv1.presentation.generation.upscaler

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.components.model.providerIcon
import com.shifthackz.aisdv1.presentation.components.model.displayName
import com.shifthackz.aisdv1.presentation.components.widget.source.ProviderNavigationCard

/**
 * Identifies the independent upscaler provider and opens its dedicated configuration wizard.
 *
 * @param platform Host selecting local-provider artwork.
 * @param source Selected provider, or null when no upscaler is available.
 * @param onConfigure Opens upscaler configuration through the owning screen.
 * @param modifier Placement of the complete provider navigation card.
 * @author Dmitriy Moroz
 */
@Composable
internal fun UpscalerProviderCard(
    platform: Platform, source: UpscalerSource?, onConfigure: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ProviderNavigationCard(
        name = source?.displayName ?: Localization.string("upscaler_unavailable"),
        label = Localization.string("upscaler_provider"),
        description = Localization.string("upscaler_provider_help"),
        icon = (source ?: UpscalerSource.availableOn(platform).first()).providerIcon(platform),
        onClick = onConfigure, modifier = modifier,
    )
}
