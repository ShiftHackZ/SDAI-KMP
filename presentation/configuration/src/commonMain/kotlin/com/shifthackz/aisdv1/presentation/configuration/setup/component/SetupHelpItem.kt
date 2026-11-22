package com.shifthackz.aisdv1.presentation.configuration.setup.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.shifthackz.aisdv1.core.model.UiText
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelp
import com.shifthackz.aisdv1.presentation.components.widget.item.SettingsItem

/**
 * Adds explanatory help to a Configure-tab link without changing its destination or row style.
 *
 * @param text Localized visible label shared with the help title.
 * @param startIcon Existing row icon, also rendered in the help title.
 * @param helpMessage Localized description of what the linked page provides.
 * @param onClick Opens the original link on a short tap.
 * @param modifier Placement of the complete row and help anchor.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SetupHelpItem(
    text: UiText,
    startIcon: ImageVector,
    helpMessage: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContextualHelp(
        title = text.asString(),
        message = helpMessage,
        titleStartIcon = startIcon,
        modifier = modifier.fillMaxWidth(),
    ) {
        SettingsItem(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            startIcon = startIcon,
            onClick = onClick
        )
    }
}
