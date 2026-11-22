package com.shifthackz.aisdv1.presentation.civitai.settings

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.shifthackz.aisdv1.domain.entity.CivitaiAccount
import com.shifthackz.aisdv1.presentation.civitai.preview.CivitaiPreviewTheme

/**
 * Shows the account connection choices and disabled automatic-upload control before sign-in.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Signed out · Light", widthDp = 360, heightDp = 740, showBackground = true)
@Preview(
    name = "Signed out · Dark",
    widthDp = 360,
    heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CivitaiSignedOutPreview() {
    CivitaiSettingsPreview(CivitaiState(isBusy = false))
}

/**
 * Shows a connected account with automatic uploading enabled in both app palettes.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Connected · Light", widthDp = 360, heightDp = 740, showBackground = true)
@Preview(
    name = "Connected · Dark",
    widthDp = 360,
    heightDp = 740,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun CivitaiConnectedPreview() {
    CivitaiSettingsPreview(
        CivitaiState(
            account = CivitaiAccount(id = 1, username = "sample_creator"),
            isAutoUploadEnabled = true,
            isBusy = false,
        )
    )
}

/**
 * Shows the non-dismissible progress feedback while an account operation is pending.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Connecting", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun CivitaiConnectingPreview() {
    CivitaiSettingsPreview(CivitaiState(isBusy = true))
}

/**
 * Shows the public-sharing consent dialog before automatic uploads are enabled.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Upload consent", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun CivitaiUploadConsentPreview() {
    CivitaiSettingsPreview(
        CivitaiState(
            account = CivitaiAccount(id = 1, username = "sample_creator"),
            isBusy = false,
            shouldConfirmAutoUpload = true,
        )
    )
}

/**
 * Shows the recoverable account-operation failure dialog over the signed-out screen.
 *
 * @author Dmitriy Moroz
 */
@Preview(name = "Account error", widthDp = 360, heightDp = 740, showBackground = true)
@Composable
private fun CivitaiAccountErrorPreview() {
    CivitaiSettingsPreview(CivitaiState(isBusy = false, hasFailed = true))
}

/**
 * Renders fixed account state with inert action sinks, without credentials or a view model.
 *
 * @param state Account and modal state illustrated by the preview.
 * @author Dmitriy Moroz
 */
@Composable
private fun CivitaiSettingsPreview(state: CivitaiState) {
    CivitaiPreviewTheme {
        CivitaiContent(state = state, processIntent = {}, openUrl = {})
    }
}
