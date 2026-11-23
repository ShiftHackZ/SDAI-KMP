@file:OptIn(ExperimentalMaterial3Api::class)

package com.shifthackz.aisdv1.presentation.debug

import com.shifthackz.aisdv1.presentation.components.service.debug.mapToUi

import androidx.compose.material3.ExperimentalMaterial3Api
import com.shifthackz.aisdv1.presentation.components.widget.sheet.PlatformModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.shifthackz.aisdv1.core.localization.Localization
import com.shifthackz.aisdv1.core.model.asString
import com.shifthackz.aisdv1.core.mvi.MviComponent
import org.koin.mp.KoinPlatform.getKoin
import com.shifthackz.aisdv1.presentation.components.modal.ldscheduler.LDSchedulerBottomSheet
import com.shifthackz.aisdv1.core.navigation.router.DebugMenuRouter
import org.koin.core.parameter.parametersOf

/**
 * Binds the debug menu to its remembered ViewModel in the already initialized application graph.
 * Platform controls and diagnostics sharing are delegated through injected interfaces.
 *
 * @param modifier Layout modifiers for the debug screen.
 * @param onMessage Presents transient operation success/failure feedback.
 * @param router Optional route owner; the application graph supplies the default when absent.
 *
 * @author Dmitriy Moroz
 */
@Composable
fun DebugMenuScreen(
    modifier: Modifier = Modifier,
    onMessage: (String) -> Unit = {},
    router: DebugMenuRouter? = null,
) {
    val koin = remember { getKoin() }
    val resolvedRouter = remember(koin, router) {
        router ?: koin.get<DebugMenuRouter>()
    }
    val platformActions = remember(koin) { koin.get<DebugMenuPlatformActions>() }
    val viewModel = remember(
        koin,
        resolvedRouter,
        platformActions,
    ) {
        koin.get<DebugMenuViewModel> {
            parametersOf(resolvedRouter, platformActions)
        }
    }
    MviComponent(
        viewModel = viewModel,
        processEffect = { effect ->
            when (effect) {
                is DebugMenuEffect.Message -> onMessage(effect.message)
            }
        },
    ) { state, intentHandler ->
        DebugMenuScreenContent(
            modifier = modifier,
            strings = debugMenuScreenStrings(),
            state = state.toContentState(),
            processAction = { action -> intentHandler(action.toIntent()) },
        )
        when (val modal = state.screenModal) {
            DebugMenuModal.None -> Unit
            is DebugMenuModal.LDScheduler -> PlatformModalBottomSheet(
                onDismissRequest = { intentHandler(DebugMenuIntent.DismissModal) },
            ) {
                LDSchedulerBottomSheet(
                    currentScheduler = modal.scheduler,
                    onSelected = {
                        intentHandler(DebugMenuIntent.LocalDiffusionScheduler.Confirm(it))
                    },
                )
            }
        }
    }
}

/**
 * Reads localized labels for the debug menu during composition.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun debugMenuScreenStrings() = DebugMenuScreenStrings(
    title = Localization.string("title_debug_menu"),
    diagnosticSection = Localization.string("ui_diagnostics_title"),
    diagnosticCapture = Localization.string("ui_diagnostics_capture"),
    diagnosticReset = Localization.string("ui_diagnostics_reset"),
    diagnosticShare = Localization.string("ui_diagnostics_share"),

    mainSection = Localization.string("debug_section_main"),
    actionLogger = Localization.string("debug_action_logger"),
    actionLoggerClear = Localization.string("debug_action_logger_clear"),
    workManagerSection = Localization.string("debug_section_work_manager"),
    actionWorkRestartTxt2Img = Localization.string("debug_action_work_restart_txt2img"),
    actionWorkRestartImg2Img = Localization.string("debug_action_work_restart_img2img"),
    actionWorkCancelAll = Localization.string("debug_action_work_cancel_all"),
    localDiffusionSection = Localization.string("debug_section_ld"),
    actionLocalDiffusionAllowCancel = Localization.string("debug_action_ld_allow_cancel"),
    actionLocalDiffusionScheduler = Localization.string("debug_action_ld_scheduler"),
    qualityAssuranceSection = Localization.string("debug_section_qa"),
    actionBadBase64 = Localization.string("debug_action_bad_base64"),
    backContentDescription = Localization.string("action_back"),
)

/**
 * Converts debug preferences and capability flags to the stateless renderer, including scheduler text.
 *
 * @author Dmitriy Moroz
 */
@Composable
internal fun DebugMenuState.toContentState() = DebugMenuScreenContentState(
    isUiDiagnosticsSupported = isUiDiagnosticsSupported,
    isUiDiagnosticsCapturing = isUiDiagnosticsCapturing,
    localDiffusionAllowCancel = localDiffusionAllowCancel,
    localDiffusionSchedulerThread = localDiffusionSchedulerThread.mapToUi().asString(),
    showWorkManagerSection = showWorkManagerSection,
    showLocalDiffusionSection = showLocalDiffusionSection,
    showQualityAssuranceSection = showQualityAssuranceSection,
)

/**
 * Maps a stateless debug-menu action to its corresponding MVI intent.
 *
 * @author Dmitriy Moroz
 */
internal fun DebugMenuAction.toIntent(): DebugMenuIntent = when (this) {
    DebugMenuAction.StartUiCapture -> DebugMenuIntent.StartUiCapture
    DebugMenuAction.StopUiCapture -> DebugMenuIntent.StopUiCapture
    DebugMenuAction.ResetUiCapture -> DebugMenuIntent.ResetUiCapture
    DebugMenuAction.ShareUiCapture -> DebugMenuIntent.ShareUiCapture
    DebugMenuAction.NavigateBack -> DebugMenuIntent.NavigateBack
    DebugMenuAction.ViewLogs -> DebugMenuIntent.ViewLogs
    DebugMenuAction.ClearLogs -> DebugMenuIntent.ClearLogs
    DebugMenuAction.RestartTxt2Img -> DebugMenuIntent.WorkManager.RestartTxt2Img
    DebugMenuAction.RestartImg2Img -> DebugMenuIntent.WorkManager.RestartImg2Img
    DebugMenuAction.CancelAllWork -> DebugMenuIntent.WorkManager.CancelAll
    DebugMenuAction.ToggleLocalDiffusionCancel -> DebugMenuIntent.AllowLocalDiffusionCancel
    DebugMenuAction.RequestLocalDiffusionScheduler -> DebugMenuIntent.LocalDiffusionScheduler.Request
    DebugMenuAction.InsertBadBase64 -> DebugMenuIntent.InsertBadBase64
}
