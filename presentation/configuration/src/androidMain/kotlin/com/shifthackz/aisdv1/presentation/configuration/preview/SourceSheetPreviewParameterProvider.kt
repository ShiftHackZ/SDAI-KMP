package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.FeatureTag
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.domain.entity.ServerSourceType
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Supplies filter and sort sheet bodies independently of modal entrance animations.
 *
 * @author Dmitriy Moroz
 */
internal class SourceSheetPreviewParameterProvider :
    PreviewParameterProvider<ServerSetupPreviewCase> {
    private val source = ConfigurationPreviewFixtures.source
    private val cases = listOf(
        ServerSetupPreviewCase(
            "Filters · Defaults",
            source.copy(modal = ServerSetupState.Modal.SourceFilters)
        ),
        ServerSetupPreviewCase(
            "Filters · Selected", source.copy(
                modal = ServerSetupState.Modal.SourceFilters,
                sourceTypeFilter = ServerSourceType.LOCAL,
                sourceReadinessFilters = setOf(ServerSourceReadiness.STABLE),
                sourceTagFilters = setOf(FeatureTag.Offline),
            )
        ),
    ) + ServerSetupState.SourceSortOrder.entries.map { sort ->
        ServerSetupPreviewCase(
            "Sort · $sort",
            source.copy(modal = ServerSetupState.Modal.SourceSort, sourceSortOrder = sort)
        )
    }

    /**
     * Named default/selected filter states and every sort selection.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ServerSetupPreviewCase> = cases.asSequence()

    /**
     * Identifies the sheet body and its current choices.
     *
     * @param index Position in the ordered sheet-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
