@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.screen.settings.section

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.presentation.screen.settings.model.SettingsIntent

/**
 * Renders the application version and developer footer independently of preference edits.
 *
 * @param appVersion Version label shown above the developer attribution.
 * @param processIntent Receives link and version actions from the section.
 * @author Dmitriy Moroz
 */
@Composable
internal fun SettingsVersionFooter(
    appVersion: String,
    processIntent: (SettingsIntent) -> Unit,
) {
    AnimatedVisibility(visible = appVersion.isNotEmpty()) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { processIntent(SettingsIntent.Action.AppVersion) },
                ),
            text = Localization.string("version", appVersion) +
                    "\n" + Localization.string("settings_footer_developed_by"),
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
        )
    }
}
