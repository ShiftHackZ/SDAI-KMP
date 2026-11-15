package com.shifthackz.aisdv1.core.mvi

import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the `MviState` contract for the SDAI core UI layer.
 *
 * @author Dmitriy Moroz
 */
interface MviState

/**
 * Defines the `MviIntent` contract for the SDAI core UI layer.
 *
 * @author Dmitriy Moroz
 */
interface MviIntent

/**
 * Defines the `MviEffect` contract for the SDAI core UI layer.
 *
 * @author Dmitriy Moroz
 */
interface MviEffect

/**
 * Defines the `MviViewModel` contract for the SDAI core UI layer.
 *
 * @author Dmitriy Moroz
 */
interface MviViewModel<S : MviState, I : MviIntent, E : MviEffect> {
    /**
     * Injected diagnostic observer; contains no application state.
     *
     * @author Dmitriy Moroz
     */
    val diagnostics: UiDiagnostics

    /**
     * Process-local fixed-label diagnostic owner.
     *
     * @author Dmitriy Moroz
     */
    val diagnosticOwner: Long

    /**
     * Attaches opt-in diagnostic observation for a composed screen; call on the UI thread.
     * Multiple compositions of the same ViewModel share one emission observer.
     *
     * @return Idempotent UI-thread detach action, invoked when that composition is disposed.
     * @author Dmitriy Moroz
     */
    fun attachDiagnostics(): () -> Unit


    /**
     * Exposes the `state` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    val state: StateFlow<S>

    /**
     * Exposes the `effect` value used by the SDAI core UI layer.
     *
     * @author Dmitriy Moroz
     */
    val effect: Flow<E>

    /**
     * Executes the `processIntent` step in the SDAI core UI layer.
     *
     * @param intent intent to process in the MVI workflow.
     * @author Dmitriy Moroz
     */
    fun processIntent(intent: I)
}

/**
 * Provides the `EmptyState` singleton used by the SDAI core UI layer.
 *
 * @author Dmitriy Moroz
 */
object EmptyState : MviState

/**
 * Provides the `EmptyIntent` singleton used by the SDAI core UI layer.
 *
 * @author Dmitriy Moroz
 */
object EmptyIntent : MviIntent

/**
 * Provides the `EmptyEffect` singleton used by the SDAI core UI layer.
 *
 * @author Dmitriy Moroz
 */
object EmptyEffect : MviEffect
