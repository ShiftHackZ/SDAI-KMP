package com.shifthackz.aisdv1.presentation.faq.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.FaqCategory
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import com.shifthackz.aisdv1.presentation.components.widget.input.FilterOptionChip

/**
 * Applies a single category filter as the user selects an option in the sheet.
 *
 * @param categories Published groups in the order shown by the FAQ.
 * @param selectedCategory Applied category slug or null when showing all questions.
 * @param onCategoryChange Receives the selected category or null to reset the filter.
 * @param modifier Placement inside the platform sheet.
 * @author Dmitriy Moroz
 */
@Composable
internal fun FaqFilters(
    categories: List<FaqCategory>,
    selectedCategory: String?,
    onCategoryChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = Localization.string("provider_action_filters"),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterOptionChip(
                label = Localization.string("provider_filter_type_all"),
                isSelected = selectedCategory == null,
                onClick = { onCategoryChange(null) },
            )
            categories.forEach { category ->
                FilterOptionChip(
                    label = category.label,
                    isSelected = selectedCategory == category.id,
                    onClick = { onCategoryChange(category.id) },
                )
            }
        }
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            isEnabled = selectedCategory != null,
            onClick = { onCategoryChange(null) },
        ) {
            Text(Localization.string("action_reset"))
        }
    }
}
