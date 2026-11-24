package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.presentation.components.widget.surface.PlatformSurface

/**
 * Shows the same selected provider outline in generation and upscaler setup.
 *
 * @param isSelected Whether this provider is selected in the setup draft.
 * @param onClick Selects or opens the provider through the owning wizard.
 * @param modifier Placement in the source list.
 * @param content Provider-specific artwork, name and supporting information.
 * @author Dmitriy Moroz
 */
@Composable
internal fun ProviderSelectionCard(
    isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    PlatformSurface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().semantics {
            selected = isSelected
            role = Role.RadioButton
        },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        content = content,
    )
}
