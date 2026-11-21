package com.shifthackz.aisdv1.presentation.home

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.presentation.components.theme.global.persistentBottomBarWindowInsets
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpNavigationBarItem

/**
 * Carries bottom navigation item data used by [AppScaffold].
 *
 * @param label Text displayed under the item icon.
 * @param selected Whether this item represents the current route.
 * @param icon Icon displayed above the item label.
 * @param helpMessage Localized explanation of what this destination offers.
 * @param onClick Action invoked when the item is selected.
 *
 * @author Dmitriy Moroz
 */
internal data class HomeNavigationBarItem(
    val label: String,
    val selected: Boolean,
    val icon: ImageVector,
    val helpMessage: String,
    val onClick: () -> Unit,
)

/**
 * Renders the app bottom navigation bar.
 *
 * The `windowInsets` value is routed through `persistentBottomBarWindowInsets()` on purpose. A plain
 * `WindowInsets.navigationBars` value looks correct on Android 15+ edge-to-edge devices and on iOS,
 * but it is wrong on older Android devices whose content window is already laid out above the system
 * navigation bar. That mismatch made release builds on a Pixel 3a XL with three-button navigation show
 * an extra bottom gap equal to the system bar height. Keeping the workaround at the persistent
 * navigation component protects all home tabs at once and avoids scattering API-level checks through
 * individual screens.
 *
 * @param items Route destinations with caller-owned selection and click handling.
 * @param modifier Layout modifiers for the persistent navigation bar.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun HomeNavigationBar(
    items: List<HomeNavigationBarItem>,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        windowInsets = persistentBottomBarWindowInsets(),
        modifier = modifier,
    ) {
        items.forEach { item ->
            ContextualHelpNavigationBarItem(
                title = item.label,
                message = item.helpMessage,
                icon = item.icon,
                isSelected = item.selected,
                onClick = item.onClick,
            )
        }
    }
}
