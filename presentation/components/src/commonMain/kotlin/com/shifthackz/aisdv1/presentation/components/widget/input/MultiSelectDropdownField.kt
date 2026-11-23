@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.components.widget.input

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.presentation.components.theme.textFieldColors

/**
 * Displays a multi-choice menu with removable chips for the caller-owned selection.
 * The field summarizes the first two labels and the remaining selection count.
 *
 * @param modifier Layout modifiers for the field and chip group.
 * @param label Localized field caption.
 * @param selectedItems Current selected values in display order.
 * @param availableItems All selectable choices.
 * @param onSelectionChanged Receives the full proposed selection after a menu toggle or chip removal.
 * @param displayDelegate Converts each choice to a localized field/menu/chip label.
 *
 * @param isInitiallyExpanded Initial menu visibility; later changes are owned by user interaction.
 * @author Dmitriy Moroz
 */
@Composable
fun <T : Any> MultiSelectDropdownField(
    modifier: Modifier = Modifier,
    label: UiText = UiText.empty,
    selectedItems: List<T> = emptyList(),
    availableItems: List<T> = emptyList(),
    onSelectionChanged: (List<T>) -> Unit = {},
    displayDelegate: (T) -> UiText = { t -> t.toString().asUiText() },
    isInitiallyExpanded: Boolean = false,
) {
    var expanded by remember { mutableStateOf(isInitiallyExpanded) }
    val selectedText = selectedItems
        .map { displayDelegate(it).asString() }
        .let { labels ->
            when {
                labels.size > 2 -> "${labels.take(2).joinToString(", ")} +${labels.size - 2}"
                else -> labels.joinToString(", ")
            }
        }

    Column(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                value = selectedText,
                onValueChange = {},
                readOnly = true,
                label = { Text(label.asString()) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = textFieldColors,
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                containerColor = MaterialTheme.colorScheme.background,
            ) {
                availableItems.forEach { item ->
                    val selected = item in selectedItems
                    DropdownMenuItem(
                        text = { Text(displayDelegate(item).asString()) },
                        onClick = {
                            onSelectionChanged(
                                if (selected) selectedItems - item else selectedItems + item,
                            )
                        },
                    )
                }
            }
        }

        if (selectedItems.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            ) {
                selectedItems.forEach { item ->
                    InputChip(
                        modifier = Modifier.padding(end = 4.dp),
                        selected = false,
                        onClick = { onSelectionChanged(selectedItems - item) },
                        label = { Text(displayDelegate(item).asString()) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = Localization.string("delete"),
                            )
                        },
                    )
                }
            }
        }
    }
}
