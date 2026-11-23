package com.shifthackz.aisdv1.presentation.backup.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.presentation.backup.BackupScreenMode
import com.shifthackz.aisdv1.presentation.backup.BackupState

/**
 * Shows the standalone first-launch invitation with its artwork and two pinned actions.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Backup · Welcome", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Backup · Welcome", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Compact · Light", group = "Backup · Welcome", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Backup · Welcome", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Backup · Welcome", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Backup · Welcome",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun BackupWelcomePreview() {
    BackupPreview(BackupScreenMode.WELCOME, BackupPreviewCase("Welcome", BackupState()))
}

/**
 * Shows the welcome actions while interaction is temporarily disabled.
 * The fixed state never launches a picker or performs a navigation transition.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Disabled · Light", group = "Backup · Welcome", widthDp = 360, heightDp = 800)
@Preview(
    name = "Disabled · Dark", group = "Backup · Welcome", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BackupWelcomeDisabledPreview() {
    BackupPreview(
        BackupScreenMode.WELCOME,
        BackupPreviewCase("Disabled", BackupState(isPicking = true))
    )
}
