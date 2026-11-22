package com.shifthackz.aisdv1.presentation.civitai.upload

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.mvi.MviComponent
import com.shifthackz.aisdv1.presentation.components.widget.dialog.DecisionInteractiveDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.InfoDialog
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ProgressDialog
import com.shifthackz.aisdv1.presentation.components.widget.help.ContextualHelpCircleIconButton

/**
 * Gallery action and modal feedback supplied by the Civitai presentation owner.
 *
 * @param viewModel Injected publication state owner for the captured gallery image.
 * @param onSignIn Navigates to SDAI's Civitai settings after confirmation.
 * @param openPost Opens a recorded post through the native browser.
 * @param modifier Placement of the gallery action's touch target.
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivitaiUploadAction(
    viewModel: CivitaiUploadViewModel,
    onSignIn: () -> Unit,
    openPost: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    MviComponent(viewModel, processEffect = { effect ->
        when (effect) {
            CivitaiUploadEffect.SignIn -> onSignIn()
            is CivitaiUploadEffect.OpenPost -> openPost(effect.url)
        }
    }) { state, intent ->
        ContextualHelpCircleIconButton(
            title = Localization.string("civitai_title"),
            message = Localization.string("civitai_action_help"),
            icon = Icons.Default.CloudUpload,
            onClick = { intent(CivitaiUploadIntent.UPLOAD) },
            modifier = modifier,
            isEnabled = state.phase != CivitaiUploadPhase.COMMUNICATING,
        )
        when (state.phase) {
            CivitaiUploadPhase.IDLE -> Unit
            CivitaiUploadPhase.COMMUNICATING -> ProgressDialog(canDismiss = false)
            CivitaiUploadPhase.LOGIN_REQUIRED -> DecisionInteractiveDialog(
                title = Localization.string("civitai_sign_in").asUiText(),
                text = Localization.string("civitai_login_required").asUiText(),
                confirmActionText = Localization.string("yes").asUiText(),
                dismissActionText = Localization.string("no").asUiText(),
                onConfirmAction = { intent(CivitaiUploadIntent.SIGN_IN) },
                onDismissRequest = { intent(CivitaiUploadIntent.DISMISS) },
            )

            CivitaiUploadPhase.SUCCESS -> InfoDialog(
                title = Localization.string("civitai_title").asUiText(),
                subTitle = Localization.string("civitai_success").asUiText(),
                onDismissRequest = { intent(CivitaiUploadIntent.DISMISS) },
            )

            CivitaiUploadPhase.FAILURE -> ErrorDialog(
                text = Localization.string("civitai_failure").asUiText(),
                onDismissRequest = { intent(CivitaiUploadIntent.DISMISS) },
            )
        }
    }
}
