package com.shifthackz.aisdv1.presentation.components.widget.toolbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.core.localization.Localization

/**
 * Keeps Android icon buttons unchanged and reuses the shared circular glass treatment on iOS.
 * Compose retains callbacks, accessibility and enclosing contextual-help gestures.
 *
 * @param onClick Requests the enabled toolbar action.
 * @param modifier Placement of the button.
 * @param isEnabled Whether activation is available.
 * @param content Shared icon or animated action glyph with a localized accessible label.
 * @author Dmitriy Moroz
 */
@Composable
expect fun ToolbarIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    content: @Composable () -> Unit,
)

/**
 * Reusable toolbar back action with the platform's navigation glyph and button treatment.
 *
 * @param onClick Requests navigation through the owning router.
 * @param modifier Button placement.
 * @param isEnabled Whether navigation is currently allowed.
 * @param icon Existing Android glyph, retained where filled/outlined variants differ.
 * @author Dmitriy Moroz
 */
@Composable
fun ToolbarBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
    icon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
) {
    ToolbarIconButton(onClick, modifier, isEnabled) {
        Icon(platformToolbarBackIcon(icon), Localization.string("action_back"))
    }
}

/**
 * Reuses the back component for secondary generation routes and the drawer action for home tabs.
 * @param canNavigateBack Whether this toolbar belongs to a secondary route.
 * @param onClick Existing router action for the current route.
 * @param modifier Button placement.
 * @author Dmitriy Moroz
 */
@Composable
fun ToolbarNavigationButton(
    canNavigateBack: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (canNavigateBack) ToolbarBackButton(onClick, modifier) else ToolbarIconButton(
        onClick,
        modifier
    ) {
        Icon(Icons.Default.Menu, Localization.string("action_menu"))
    }
}

/**
 * Selects a native chevron on iOS while retaining the supplied Android back vector.
 *
 * @param androidIcon Existing Android back glyph.
 * @author Dmitriy Moroz
 */
internal expect fun platformToolbarBackIcon(androidIcon: ImageVector): ImageVector
