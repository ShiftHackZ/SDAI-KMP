package com.shifthackz.aisdv1.presentation.backup.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.presentation.backup.BackupIntent
import com.shifthackz.aisdv1.presentation.backup.BackupPageState
import com.shifthackz.aisdv1.presentation.components.model.shortDisplayName
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Shared category and nested-provider selection for create and restore. Selection is always caller-owned.
 *
 * @param state Current page inventory and choices; contains no credential values.
 * @param isEnabled Whether selection can change during this operation.
 * @param processIntent Receives selection and disclosure actions.
 * @param modifier Layout applied to the root list container.
 * @author Dmitriy Moroz
 */
@Composable
internal fun BackupSelectionList(
    state: BackupPageState,
    isEnabled: Boolean,
    processIntent: (BackupIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        state.inspection?.entries?.forEach { entry ->
            val category = entry.category
            val isSelected = category in state.selection.categories
            val hasSomeProviders =
                category == BackupCategory.PROVIDERS && state.selection.providers.isNotEmpty()
            val hasAllProviders = state.selection.providers.containsAll(state.inspection.providers)
            PlatformSurface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceTint.copy(alpha = 0.8f),
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Column {
                    BackupCategoryHeader(
                        title = Localization.string("backup_category_" + category.key),
                        subtitle = if (entry.isAvailable || entry.itemCount == 0L) Localization.string(
                            "backup_items",
                            entry.itemCount
                        ) else Localization.string("backup_unavailable"),
                        selectionState = when {
                            hasSomeProviders && !hasAllProviders -> ToggleableState.Indeterminate
                            isSelected -> ToggleableState.On
                            else -> ToggleableState.Off
                        },
                        isSelectionEnabled = isEnabled && entry.isAvailable,
                        onToggleSelection = { processIntent(BackupIntent.ToggleCategory(category)) },
                        isExpanded = state.areProvidersExpanded,
                        isExpansionEnabled = isEnabled,
                        onToggleExpanded = if (category == BackupCategory.PROVIDERS && state.inspection.providers.isNotEmpty()) {
                            { processIntent(BackupIntent.ToggleProvidersExpanded) }
                        } else null,
                    )
                    if (category == BackupCategory.PROVIDERS) {
                        AnimatedVisibility(
                            visible = state.areProvidersExpanded,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut(),
                        ) {
                            Column {
                                state.inspection.providers.sorted().forEach { provider ->
                                    Row(
                                        Modifier.fillMaxWidth().heightIn(min = 48.dp).toggleable(
                                            value = provider in state.selection.providers,
                                            enabled = isEnabled && entry.isAvailable,
                                            role = Role.Checkbox,
                                            onValueChange = {
                                                processIntent(
                                                    BackupIntent.ToggleProvider(
                                                        provider
                                                    )
                                                )
                                            },
                                        ).padding(
                                            start = 44.dp,
                                            end = 16.dp,
                                            top = 8.dp,
                                            bottom = 8.dp
                                        ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = provider in state.selection.providers,
                                            onCheckedChange = null,
                                            enabled = isEnabled && entry.isAvailable
                                        )
                                        Spacer(Modifier.size(12.dp))
                                        Text(
                                            providerName(provider),
                                            style = MaterialTheme.typography.bodyLarge
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Resolves a known stable provider ID through the existing localized short display-name mapping.
 * Unknown IDs retain their literal identifier so inspection can still describe unsupported data.
 *
 * @param key Stable provider wire identifier from backup metadata.
 * @author Dmitriy Moroz
 */
internal fun providerName(key: String): String =
    ServerSource.entries.firstOrNull { it.key == key }?.shortDisplayName() ?: key
