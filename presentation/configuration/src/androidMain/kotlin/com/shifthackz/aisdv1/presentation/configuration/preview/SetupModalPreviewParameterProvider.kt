package com.shifthackz.aisdv1.presentation.configuration.preview

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.shifthackz.aisdv1.domain.entity.LocalAiModel
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState

/**
 * Supplies wizard dialogs that can render directly from fixed state without a dialog ViewModel.
 * Mirror selection is covered separately through its stateless download dialog renderer.
 *
 * @author Dmitriy Moroz
 */
internal class SetupModalPreviewParameterProvider :
    PreviewParameterProvider<ServerSetupPreviewCase> {
    private val ready = ConfigurationPreviewFixtures.configure(ServerSource.AUTOMATIC1111)
    private val cases = listOf(
        ServerSetupPreviewCase(
            "Communicating",
            ready.copy(modal = ServerSetupState.Modal.Communicating)
        ),
        ServerSetupPreviewCase(
            "Connect localhost",
            ready.copy(modal = ServerSetupState.Modal.ConnectLocalHost)
        ),
        ServerSetupPreviewCase(
            "Connection unverified",
            ready.copy(modal = ServerSetupState.Modal.ConnectionUnverified)
        ),
        ServerSetupPreviewCase(
            "Error",
            ready.copy(modal = ServerSetupState.Modal.Error("The provider could not be reached. Check the address and try again."))
        ),
        ServerSetupPreviewCase(
            "Long error", ready.copy(
                modal = ServerSetupState.Modal.Error(
                    "The provider returned an unexpected response while configuration was being loaded. Check that the address points to the correct service and that its API is enabled before trying again.",
                )
            )
        ),
        ServerSetupPreviewCase(
            "Delete local model",
            ConfigurationPreviewFixtures.configure(ServerSource.LOCAL_MNN).copy(
                modal = ServerSetupState.Modal.DeleteLocalModelConfirm(
                    ConfigurationPreviewFixtures.downloaded,
                    LocalAiModel.Type.Mnn
                ),
            )
        ),
        ServerSetupPreviewCase(
            "Switch model downloads",
            ready.copy(modal = ServerSetupState.Modal.SwitchModelDownloads(LocalAiModel.Type.Mnn))
        ),
        ServerSetupPreviewCase(
            "Demo · No acknowledgements",
            ready.copy(modal = ServerSetupState.Modal.DemoExplanation())
        ),
        ServerSetupPreviewCase(
            "Demo · First acknowledged",
            ready.copy(modal = ServerSetupState.Modal.DemoExplanation(hasUnderstoodDemo = true))
        ),
        ServerSetupPreviewCase(
            "Demo · Both acknowledged", ready.copy(
                modal = ServerSetupState.Modal.DemoExplanation(
                    hasUnderstoodDemo = true, hasAgreedToAccurateFeedback = true,
                )
            )
        ),
    )

    /**
     * Named wizard feedback and consent states rendered in both palettes.
     *
     * @author Dmitriy Moroz
     */
    override val values: Sequence<ServerSetupPreviewCase> = cases.asSequence()

    /**
     * Identifies the dialog or acknowledgement state above its preview.
     *
     * @param index Position in the ordered modal-state list.
     * @author Dmitriy Moroz
     */
    override fun getDisplayName(index: Int): String = cases[index].name
}
