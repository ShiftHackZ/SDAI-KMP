package com.shifthackz.aisdv1.presentation.report

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asUiText
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.presentation.components.model.ErrorState
import com.shifthackz.aisdv1.core.navigation.router.ReportRouter
import com.shifthackz.aisdv1.presentation.components.widget.dialog.ErrorDialog
import org.koin.core.parameter.parametersOf

/**
 * Binds report editing and submission feedback for a persisted or cached generation image.
 *
 * @param itemId Positive gallery identity, or a nonpositive value to use the last cached result.
 * @param modifier Layout modifiers for the report screen.
 * @param router Optional back-navigation owner; defaults to the application graph binding.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun ReportScreen(
    itemId: Long,
    modifier: Modifier = Modifier,
    router: ReportRouter? = null,
) {
    val koin = remember { getKoin() }
    val reportRouter = remember(koin, router) {
        router ?: koin.get<ReportRouter>()
    }
    val viewModel = remember(
        koin,
        itemId,
        reportRouter,
    ) {
        koin.get<ReportViewModel> {
            parametersOf(itemId, reportRouter)
        }
    }
    MviComponent(
        viewModel = viewModel,
    ) { state, intentHandler ->
        ReportScreenContent(
            modifier = modifier,
            state = state.toContentState(),
            strings = reportScreenStrings(),
            onNavigateBack = { intentHandler(ReportIntent.NavigateBack) },
            onSubmit = { intentHandler(ReportIntent.Submit) },
            onTextChange = { intentHandler(ReportIntent.UpdateText(it)) },
            onReasonChange = { intentHandler(ReportIntent.UpdateReason(it)) },
        )
        when (val error = state.error) {
            ErrorState.None -> Unit
            ErrorState.Generic -> ErrorDialog(
                text = Localization.string("error_generic").asUiText(),
                onDismissRequest = { intentHandler(ReportIntent.DismissError) },
            )

            is ErrorState.WithMessage -> ErrorDialog(
                text = error.message,
                onDismissRequest = { intentHandler(ReportIntent.DismissError) },
            )
        }
    }
}

/**
 * Maps report submission state to the stateless screen renderer.
 *
 * @author Dmitriy Moroz
 */
internal fun ReportState.toContentState() = ReportScreenContentState(
    loading = loading,
    imageBase64 = imageBase64,
    text = text,
    reason = reason,
    reportSent = reportSent,
)

/**
 * Reads localized report labels and category names during composition.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun reportScreenStrings() = ReportScreenStrings(
    title = Localization.string("report_title"),
    done = Localization.string("report_done"),
    submit = Localization.string("report_submit"),
    sent = Localization.string("report_sent"),
    description = Localization.string("report_description"),
    backContentDescription = Localization.string("action_back"),
    sendContentDescription = Localization.string("action_send"),
    doneContentDescription = Localization.string("action_done"),
    imageContentDescription = Localization.string("action_generate"),
    reasonInappropriateContent = Localization.string("report_reason_inappropriate_content"),
    reasonViolence = Localization.string("report_reason_violence"),
    reasonHatefulSpeech = Localization.string("report_reason_hateful_speech"),
    reasonIntellectualProperty = Localization.string("report_reason_intellectual"),
    reasonAdultContent = Localization.string("report_reason_adult"),
    reasonOther = Localization.string("report_reason_other"),
)
