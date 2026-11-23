package com.shifthackz.aisdv1.presentation.backup.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.backup.BackupScreenMode

/**
 * Shows the normal two-tab backup screen with each tab selected and its operation feedback visible.
 *
 * @param scenario Named create or restore snapshot supplied by the parameter provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Backup · Settings tabs", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Backup · Settings tabs", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun BackupSettingsStatesPreview(
    @PreviewParameter(BackupSettingsPreviewParameterProvider::class) scenario: BackupPreviewCase,
) {
    BackupPreview(BackupScreenMode.SETTINGS, scenario)
}

/**
 * Shows the create tab bar, checklist and pinned action across narrow and landscape sizes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Compact · Light", group = "Backup · Settings tabs", widthDp = 320, heightDp = 640)
@Preview(
    name = "Compact · Dark", group = "Backup · Settings tabs", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Backup · Settings tabs", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Backup · Settings tabs",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun BackupSettingsCompactPreview() {
    BackupPreview(
        BackupScreenMode.SETTINGS,
        BackupPreviewCase("Create", BackupPreviewFixtures.create)
    )
}
