package com.shifthackz.aisdv1.presentation.configuration.upscaler

import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.domain.entity.UpscalerFeatureTag
import com.shifthackz.aisdv1.domain.entity.UpscalerSource
import com.shifthackz.aisdv1.presentation.components.model.description
import com.shifthackz.aisdv1.presentation.components.model.displayName
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings
import com.shifthackz.aisdv1.presentation.configuration.setup.source.mapToUi

/**
 * Applies localized word search, capability intersection and date ordering to allowed upscalers.
 *
 * @param strings Localized hosting and readiness labels included in search.
 * @author Dmitriy Moroz
 */
internal fun UpscalerSetupState.visibleSources(strings: ServerSetupStrings): List<UpscalerSource> {
    val words = discovery.query.trim().lowercase().split(Regex("\\s+")).filter(String::isNotBlank)
    val filtered = sources.filter { source ->
        matches(source, discovery.type, discovery.readiness, discovery.tags) &&
                words.all(buildList {
                    add(source.key); add(source.displayName); add(source.version)
                    add(source.description)
                    add(source.type.mapToUi(strings)); add(
                    source.readiness[platform].mapToUi(
                        strings
                    )
                )
                    source.featureTags.forEach { add(it.mapToUi()) }
                }.joinToString(" ").lowercase()::contains)
    }
    return when (discovery.sortOrder) {
        ServerSetupState.SourceSortOrder.DEFAULT -> filtered
        ServerSetupState.SourceSortOrder.RECENTLY_UPDATED -> filtered.sortedByDescending { it.versionSortKey() }
        ServerSetupState.SourceSortOrder.OLDEST_UPDATED -> filtered.sortedBy { it.versionSortKey() }
    }
}

/**
 * Checks filter intersections without the search query so impossible chip combinations are disabled.
 *
 * @param source Candidate already constrained by platform availability.
 * @param type Requested hosting category.
 * @param readiness Accepted maturity values.
 * @param tags Required capabilities.
 * @author Dmitriy Moroz
 */
internal fun UpscalerSetupState.matches(
    source: UpscalerSource, type: ServerSourceType?,
    readiness: Set<ServerSourceReadiness>, tags: Set<UpscalerFeatureTag>
): Boolean =
    (type == null || source.type == type) &&
            (readiness.isEmpty() || source.readiness[platform] in readiness) &&
            source.featureTags.containsAll(tags)

/**
 * Converts the provider implementation date to a stable chronological ordering key.
 *
 * @author Dmitriy Moroz
 */
private fun UpscalerSource.versionSortKey(): Int {
    val parts = version.substringBefore("-").split(".")
    return (parts.getOrNull(0)?.toIntOrNull() ?: 0) * 10_000 +
            (parts.getOrNull(1)?.toIntOrNull() ?: 0) * 100 +
            (parts.getOrNull(2)?.toIntOrNull() ?: 0)
}
