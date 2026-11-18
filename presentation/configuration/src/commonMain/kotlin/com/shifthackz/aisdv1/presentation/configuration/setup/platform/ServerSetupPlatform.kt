package com.shifthackz.aisdv1.presentation.configuration.setup.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier


/**
 * Opens a native directory picker for a user-managed local-model source.
 * The callback supplies the selected native filesystem path; model import/snapshot ownership stays with domain workflows.
 *
 * @param text Localized label of the directory-selection action.
 * @param onPathSelected Receives the chosen source-folder path after native selection.
 * @param modifier Size and placement of the picker button in the setup form.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal expect fun ServerSetupLocalPathPickerButton(
    text: String,
    onPathSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
)

/**
 * Reports whether custom local-folder setup is available under the current platform/build prerequisites.
 * Android checks storage access; iOS supports native folder selection.
 *
 * @author Dmitriy Moroz
 */
internal expect fun isLocalGenerationSetupAvailable(): Boolean
