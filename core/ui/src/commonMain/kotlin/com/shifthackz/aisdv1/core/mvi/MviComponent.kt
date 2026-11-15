package com.shifthackz.aisdv1.core.mvi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.shifthackz.aisdv1.core.mvi.diagnostics.LocalUiDiagnosticContext
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticContext
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSubtree
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSection

/**
 * Collects visible, started screen state while retaining the last rendered value when hidden.
 * Effects remain collected for this composition's lifetime. Diagnostic attachment follows the
 * composed consumer, independently of business jobs owned by the ViewModel.
 *
 * @param viewModel Owner of state, effects and optional bounded diagnostics.
 * @param processEffect Handles one-shot effects while this consumer remains composed.
 * @param content Renders the latest state delivered while the screen is visible and started.
 * @author Dmitriy Moroz
 */
@Composable
fun <S : MviState, I : MviIntent, E : MviEffect> MviComponent(
    viewModel: MviViewModel<S, I, E>,
    processEffect: (effect: E) -> Unit = {},
    content: @Composable (state: S) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect(processEffect)
    }
    val isVisible = LocalMviVisibility.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(viewModel) {
        val detachDiagnostics = viewModel.attachDiagnostics()
        onDispose(detachDiagnostics)
    }
    val collectedState = remember(viewModel) { mutableStateOf(viewModel.state.value) }
    LaunchedEffect(viewModel, isVisible, lifecycle) {
        if (isVisible) lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.state.collect { collectedState.value = it }
        }
    }
    val state by collectedState
    if (viewModel.diagnostics.isSupported) {
        val diagnosticContext = remember(viewModel) {
            UiDiagnosticContext(
                viewModel.diagnostics,
                viewModel.diagnosticOwner,
                UiDiagnosticSubtree.SCREEN
            )
        }
        CompositionLocalProvider(LocalUiDiagnosticContext provides diagnosticContext) {
            UiDiagnosticSection(UiDiagnosticSubtree.SCREEN) { content(state) }
        }
    } else {
        content(state)
    }
}

/**
 * Collects visible, started state and passes intent dispatch to the screen content.
 * Hidden retained tabs keep their last state; effects remain collected while this consumer
 * is composed. Diagnostics observe the wrapper rather than every descendant restart scope.
 *
 * @param viewModel Owner of state, intent processing, effects and optional bounded diagnostics.
 * @param processEffect Handles one-shot effects while this consumer remains composed.
 * @param content Renders the latest observed state and dispatches user intents to the owner.
 * @author Dmitriy Moroz
 */
@Composable
fun <S : MviState, I : MviIntent, E : MviEffect> MviComponent(
    viewModel: MviViewModel<S, I, E>,
    processEffect: (effect: E) -> Unit = {},
    content: @Composable (state: S, processIntent: (I) -> Unit) -> Unit,
) {
    LaunchedEffect(viewModel) {
        viewModel.effect.collect(processEffect)
    }
    val isVisible = LocalMviVisibility.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(viewModel) {
        val detachDiagnostics = viewModel.attachDiagnostics()
        onDispose(detachDiagnostics)
    }
    val collectedState = remember(viewModel) { mutableStateOf(viewModel.state.value) }
    LaunchedEffect(viewModel, isVisible, lifecycle) {
        if (isVisible) lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.state.collect { collectedState.value = it }
        }
    }
    val state by collectedState
    if (viewModel.diagnostics.isSupported) {
        val diagnosticContext = remember(viewModel) {
            UiDiagnosticContext(
                viewModel.diagnostics,
                viewModel.diagnosticOwner,
                UiDiagnosticSubtree.SCREEN
            )
        }
        CompositionLocalProvider(LocalUiDiagnosticContext provides diagnosticContext) {
            UiDiagnosticSection(UiDiagnosticSubtree.SCREEN) {
                content(
                    state,
                    viewModel::processIntent
                )
            }
        }
    } else {
        content(state, viewModel::processIntent)
    }
}
