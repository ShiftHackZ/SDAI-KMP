package com.shifthackz.aisdv1.presentation.backup.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.shifthackz.aisdv1.presentation.backup.BackupScreenMode

/**
 * Shows initial recovery without tabs, including archive selection and every operation dialog.
 *
 * @param scenario Named restore-only snapshot supplied by the preview provider.
 * @author Dmitriy Moroz
 */
@Preview(name = "Light", group = "Backup · Initial restore", widthDp = 360, heightDp = 800)
@Preview(
    name = "Dark", group = "Backup · Initial restore", widthDp = 360, heightDp = 800,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun InitialRestoreStatesPreview(
    @PreviewParameter(InitialRestorePreviewParameterProvider::class) scenario: BackupPreviewCase,
) {
    BackupPreview(BackupScreenMode.FIRST_LAUNCH, scenario)
}

/**
 * Shows the inspected archive and pinned restore action on smaller and wider viewports.
 *
 * @author Dmitriy Moroz
 */
@Preview(
    name = "Compact · Light",
    group = "Backup · Initial restore",
    widthDp = 320,
    heightDp = 640
)
@Preview(
    name = "Compact · Dark", group = "Backup · Initial restore", widthDp = 320, heightDp = 640,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Preview(name = "Landscape", group = "Backup · Initial restore", widthDp = 800, heightDp = 360)
@Preview(
    name = "Large text",
    group = "Backup · Initial restore",
    widthDp = 320,
    heightDp = 640,
    fontScale = 1.5f
)
@Composable
private fun InitialRestoreCompactPreview() {
    BackupPreview(
        BackupScreenMode.FIRST_LAUNCH,
        BackupPreviewCase("Archive", BackupPreviewFixtures.restore)
    )
}
