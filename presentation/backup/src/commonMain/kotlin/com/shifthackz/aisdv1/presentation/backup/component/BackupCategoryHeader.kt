package com.shifthackz.aisdv1.presentation.backup.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.triStateToggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Shows category metadata without constraining its text to the header's touch regions.
 * Expandable headers reserve the physical left half for selection and the right half for disclosure;
 * other headers use their entire width for selection. Both regions expose separate accessible actions.
 * Their shared indication covers the full header, including hover and held presses.
 *
 * @param title Localized category name announced with its selection control.
 * @param subtitle Localized inventory count or availability message.
 * @param selectionState Current category selection, including partial provider selection.
 * @param isSelectionEnabled Whether changing this category is currently permitted.
 * @param onToggleSelection Requests a category selection change.
 * @param modifier Placement of the complete header.
 * @param isExpanded Whether the nested provider choices are visible.
 * @param isExpansionEnabled Whether disclosure can change during the current operation.
 * @param onToggleExpanded Optional disclosure action; its presence enables the split touch regions.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BackupCategoryHeader(
    title: String,
    subtitle: String,
    selectionState: ToggleableState,
    isSelectionEnabled: Boolean,
    onToggleSelection: () -> Unit,
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    isExpansionEnabled: Boolean = true,
    onToggleExpanded: (() -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier.fillMaxWidth()
            .clipToBounds()
            .indication(interactionSource, ripple(bounded = false))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clearAndSetSemantics { }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TriStateCheckbox(
                state = selectionState,
                onClick = null,
                enabled = isSelectionEnabled,
            )
            Spacer(Modifier.size(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (onToggleExpanded != null) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = if (isExpansionEnabled) MaterialTheme.colorScheme.onSurface else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    )
                }
            }
        }
        Box(Modifier.matchParentSize()) {
            Box(
                Modifier.align(AbsoluteAlignment.CenterLeft)
                    .fillMaxHeight()
                    .fillMaxWidth(if (onToggleExpanded != null) 0.5f else 1f)
                    .triStateToggleable(
                        state = selectionState,
                        interactionSource = interactionSource,
                        indication = null,
                        enabled = isSelectionEnabled,
                        role = Role.Checkbox,
                        onClick = onToggleSelection,
                    )
                    .semantics { contentDescription = "$title, $subtitle" },
            )
            onToggleExpanded?.let { toggleExpanded ->
                val expansionLabel = Localization.string("backup_provider_choices")
                Box(
                    Modifier.align(AbsoluteAlignment.CenterRight)
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            enabled = isExpansionEnabled,
                            role = Role.Button,
                            onClick = toggleExpanded,
                        )
                        .semantics { contentDescription = expansionLabel },
                )
            }
        }
    }
}
