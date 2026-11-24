@file:OptIn(ExperimentalLayoutApi::class)

package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.action.OutlinedButton
import com.shifthackz.aisdv1.presentation.configuration.setup.content.ServerSetupStrings

/**
 * Shared filter-sheet frame and reset action for generation and super-resolution discovery.
 *
 * @param hasActiveFilters Whether reset can change the current filter draft.
 * @param strings Localized reset caption.
 * @param onReset Clears filters while retaining the query and sort order.
 * @param modifier Layout of the complete sheet.
 * @param content Hosting, readiness and capability sections owned by the picker.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderFiltersSheet(
    hasActiveFilters: Boolean, strings: ServerSetupStrings,
    onReset: () -> Unit, modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier.fillMaxWidth().height(500.dp).navigationBarsPadding().padding(16.dp)) {
        Column(
            Modifier.fillMaxWidth().weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
        OutlinedButton(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            isEnabled = hasActiveFilters, onClick = onReset
        ) { Text(strings.reset) }
    }
}

/**
 * Shared filter heading and wrapping chip geometry.
 *
 * @param title Localized filter category.
 * @param modifier Layout of the section.
 * @param content Available filter chips including disabled impossible combinations.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderFilterSection(
    title: String, modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        FlowRow(
            Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp), content = content
        )
    }
}
