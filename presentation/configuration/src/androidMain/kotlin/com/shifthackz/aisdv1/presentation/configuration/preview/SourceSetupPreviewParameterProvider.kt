package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Supplies discovery loading, selection, search/filter results and provider-card variants.
 *
 * @author Dmitriy Moroz
 */
internal class SourceSetupPreviewParameterProvider :
    PreviewParameterProvider<ServerSetupPreviewCase> {
    private val source = ConfigurationPreviewFixtures.source
    private val cases = listOf(
        ServerSetupPreviewCase("Loading configuration", ServerSetupState()),
        ServerSetupPreviewCase(
            "First launch · No back arrow",
            source.copy(showBackNavArrow = false)
        ),
        ServerSetupPreviewCase("All providers", source),
        ServerSetupPreviewCase("Search matches", source.copy(sourceSearchQuery = "diffusion")),
        ServerSetupPreviewCase(
            "No search results",
            source.copy(sourceSearchQuery = "no-provider-matches")
        ),
        ServerSetupPreviewCase(
            "Local providers",
            source.copy(mode = ServerSource.LOCAL_MNN, sourceTypeFilter = ServerSourceType.LOCAL)
        ),
        ServerSetupPreviewCase(
            "Cloud providers",
            source.copy(mode = ServerSource.HORDE, sourceTypeFilter = ServerSourceType.CLOUD)
        ),
        ServerSetupPreviewCase(
            "Self-hosted providers",
            source.copy(sourceTypeFilter = ServerSourceType.SELF_HOSTED)
        ),
        ServerSetupPreviewCase(
            "Stable filter",
            source.copy(sourceReadinessFilters = setOf(ServerSourceReadiness.STABLE))
        ),
        ServerSetupPreviewCase(
            "Offline tag filter",
            source.copy(mode = ServerSource.LOCAL_MNN, sourceTagFilters = setOf(FeatureTag.Offline))
        ),
        ServerSetupPreviewCase(
            "Newest first",
            source.copy(sourceSortOrder = ServerSetupState.SourceSortOrder.RECENTLY_UPDATED)
        ),
        ServerSetupPreviewCase(
            "Oldest first",
            source.copy(sourceSortOrder = ServerSetupState.SourceSortOrder.OLDEST_UPDATED)
        ),
    ) + ConfigurationPreviewFixtures.sources.map { provider ->
        ServerSetupPreviewCase("Selected · ${provider.name}", source.copy(mode = provider))
    }

    /**
     * Fixed discovery snapshots rendered in both palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ServerSetupPreviewCase> = cases.asSequence()

    /**
     * Identifies the current provider, filter or discovery stage.
     *
     * @param index Position in the ordered discovery-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
