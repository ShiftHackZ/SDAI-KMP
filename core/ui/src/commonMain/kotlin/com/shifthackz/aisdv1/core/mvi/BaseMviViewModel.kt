package com.shifthackz.aisdv1.core.mvi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticEventType
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticScreen
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticOperation
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticSubtree
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Owns screen state and one-shot effects independently of the screen's current visibility.
 * Optional diagnostics use a bounded owner identity released when the ViewModel is cleared;
 * attaching a UI consumer observes state without controlling business-job lifetime.
 *
 * @param diagnostics Injected bounded observer; no-op in normal builds.
 * @param diagnosticScreen Fixed label for this ViewModel type.
 * @param initialState Initial application state.
 * @param effectDispatcher Dispatcher retaining existing effect delivery semantics.
 * @author Dmitriy Moroz
 */
abstract class BaseMviViewModel<S : MviState, I : MviIntent, E : MviEffect>(
    override val diagnostics: UiDiagnostics,
    diagnosticScreen: UiDiagnosticScreen,
    initialState: S,
    /**
     * Exposes the `effectDispatcher` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    private val effectDispatcher: CoroutineDispatcher,
) : ViewModel(), MviViewModel<S, I, E> {
    override val diagnosticOwner = diagnostics.register(diagnosticScreen)

    /**
     * Exposes the `_state` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    private val _state = MutableStateFlow(initialState)

    /**
     * Exposes the `state` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    override val state = _state.asStateFlow()

    /**
     * Exposes the `_effect` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    private val _effect = Channel<E>(Channel.BUFFERED)

    /**
     * Exposes the `effect` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    override val effect = _effect.receiveAsFlow()

    /**
     * Exposes the `currentState` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    protected val currentState: S
        get() = state.value

    private var diagnosticAttachments = 0
    private var diagnosticObserverJob: Job? = null
    private var hasBeenCleared = false

    /**
     * Starts one capture observer for the first composed consumer; business jobs remain independent.
     * Attach and the returned detach action run on the UI thread, as Compose lifecycle effects do.
     *
     * @return Idempotent release action for this composition's diagnostic subscription.
     * @author Dmitriy Moroz
     */
    override fun attachDiagnostics(): () -> Unit {
        if (!diagnostics.isSupported || hasBeenCleared) return {}
        diagnosticAttachments++
        if (diagnosticAttachments == 1) {
            diagnosticObserverJob = viewModelScope.launch {
                diagnostics.captureState.collectLatest { isCapturing ->
                    onDiagnosticCaptureChanged(isCapturing)
                    if (isCapturing) {
                        var isInitial = true
                        state.collect {
                            diagnostics.record(
                                diagnosticOwner,
                                if (isInitial) UiDiagnosticEventType.INITIAL_STATE else UiDiagnosticEventType.OBSERVED_EMISSION,
                            )
                            isInitial = false
                        }
                    }
                }
            }
        }
        var isDetached = false
        return {
            if (!isDetached) {
                isDetached = true
                if (diagnosticAttachments > 0) diagnosticAttachments--
                if (diagnosticAttachments == 0) {
                    diagnosticObserverJob?.cancel()
                    diagnosticObserverJob = null
                }
            }
        }
    }

    /**
     * Allows diagnostic controls to reflect explicit capture changes while their screen is attached.
     * This hook must not start a second long-lived capture observer.
     *
     * @param isCapturing Current explicit capture selection.
     * @author Dmitriy Moroz
     */
    protected open fun onDiagnosticCaptureChanged(isCapturing: Boolean) = Unit

    /**
     * Releases diagnostic ownership when the ViewModel lifecycle ends.
     *
     * @author Dmitriy Moroz
     */
    override fun onCleared() {
        hasBeenCleared = true
        diagnosticObserverJob?.cancel()
        diagnosticObserverJob = null
        diagnosticAttachments = 0
        diagnostics.unregister(diagnosticOwner)
        super.onCleared()
    }

    /**
     * Executes the `launch` step in the SDAI core UI layer.
     *
     * @param dispatcher dispatcher value consumed by the API.
     * @param start start value consumed by the API.
     * @param block block value consumed by the API.
     * @return Result produced by `launch`.
     * @author Dmitriy Moroz
     */
    protected fun launch(
        dispatcher: CoroutineDispatcher,
        start: CoroutineStart = CoroutineStart.DEFAULT,
        block: suspend CoroutineScope.() -> Unit,
    ): Job {
        return viewModelScope.launch(dispatcher, start = start) {
            try {
                block()
            } catch (throwable: Throwable) {
                if (throwable is CancellationException) throw throwable
            }
        }
    }

    /**
     * Performs the SDAI side effect handled by `updateState`.
     *
     * @param mutation mutation value consumed by the API.
     * @author Dmitriy Moroz
     */
    protected fun updateState(mutation: (S) -> S) {
        diagnostics.record(diagnosticOwner, UiDiagnosticEventType.MUTATION_REQUEST)
        val span = diagnostics.beginTrace(
            diagnosticOwner,
            UiDiagnosticSubtree.SCREEN,
            UiDiagnosticOperation.STATE_UPDATE
        )
        try {
            _state.update(mutation)
        } finally {
            diagnostics.finishTrace(span)
        }
    }

    /**
     * Executes the `emitState` step in the SDAI core UI layer.
     *
     * @param state state rendered or processed by the component.
     * @author Dmitriy Moroz
     */
    protected fun emitState(state: S) {
        diagnostics.record(diagnosticOwner, UiDiagnosticEventType.MUTATION_REQUEST)
        val span = diagnostics.beginTrace(
            diagnosticOwner,
            UiDiagnosticSubtree.SCREEN,
            UiDiagnosticOperation.STATE_REPLACEMENT
        )
        try {
            _state.value = state
        } finally {
            diagnostics.finishTrace(span)
        }
    }

    /**
     * Executes the `emitEffect` step in the SDAI core UI layer.
     *
     * @param effect effect emitted by the MVI workflow.
     * @author Dmitriy Moroz
     */
    protected fun emitEffect(effect: E) {
        viewModelScope.launch(effectDispatcher) {
            _effect.send(effect)
        }
    }
}
