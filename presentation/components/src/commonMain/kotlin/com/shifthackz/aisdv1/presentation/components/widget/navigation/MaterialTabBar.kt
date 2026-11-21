package com.shifthackz.aisdv1.presentation.components.widget.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Retains the existing secondary Material tabs for Android and embedded iOS illustrations.
 *
 * @param labels Ordered localized page titles.
 * @param selectedIndex Current page index, within [labels].
 * @param onSelect Requests the selected page from the caller.
 * @param modifier Placement of the Material tab strip.
 * @param isEnabled Whether each tab can be activated.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MaterialTabBar(
    labels: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    require(selectedIndex in labels.indices)
    SecondaryTabRow(
        selectedTabIndex = selectedIndex,
        modifier = modifier,
        contentColor = MaterialTheme.colorScheme.primary,
    ) {
        labels.forEachIndexed { index, label ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onSelect(index) },
                enabled = isEnabled,
                text = { Text(label) },
            )
        }
    }
}
