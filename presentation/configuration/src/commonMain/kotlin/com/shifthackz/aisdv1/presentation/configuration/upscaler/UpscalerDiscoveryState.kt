package com.shifthackz.aisdv1.presentation.configuration.upscaler

import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.domain.entity.UpscalerFeatureTag
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Retains independent provider discovery inputs while navigating wizard steps.
 *
 * @param query Space-separated search terms matched across localized provider metadata.
 * @param type Optional hosting category.
 * @param readiness Accepted platform-specific maturity levels; empty accepts all.
 * @param tags Capabilities that every matching provider must supply.
 * @param sortOrder Ordering of providers remaining after search and filtering.
 * @param isFilterVisible Whether the filter sheet is open.
 * @param isSortVisible Whether the sort sheet is open.
 * @author Dmitriy Moroz
 */
internal data class UpscalerDiscoveryState(
    val query: String = "",
    val type: ServerSourceType? = null,
    val readiness: Set<ServerSourceReadiness> = emptySet(),
    val tags: Set<UpscalerFeatureTag> = emptySet(),
    val sortOrder: ServerSetupState.SourceSortOrder = ServerSetupState.SourceSortOrder.DEFAULT,
    val isFilterVisible: Boolean = false,
    val isSortVisible: Boolean = false,
)
