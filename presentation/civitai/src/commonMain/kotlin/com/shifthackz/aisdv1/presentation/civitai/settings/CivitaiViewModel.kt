package com.shifthackz.aisdv1.presentation.civitai.settings

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.EmptyEffect
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.navigation.router.CivitaiRouter
import com.shifthackz.aisdv1.domain.entity.CivitaiAuthorizationCancelled
import com.shifthackz.aisdv1.domain.usecase.civitai.SignInToCivitaiUseCase
import com.shifthackz.aisdv1.domain.repository.CivitaiRepository
import kotlinx.coroutines.CancellationException

/**
 * Loads account settings on IO and requires explicit consent before automatic public publishing.
 *
 * @param repository Strict account and receipt persistence boundary.
 * @param dispatchers Native UI and IO dispatchers.
 * @param diagnostics Bounded screen observer that never receives account credentials.
 * @param router Invoking route's navigation owner.
 * @param signIn Native OAuth authorization and validated protected-session establishment.
 * @author Dmitriy Moroz
 */
class CivitaiViewModel(
    private val repository: CivitaiRepository,
    private val dispatchers: DispatchersProvider,
    diagnostics: UiDiagnostics,
    private val router: CivitaiRouter,
    private val signIn: SignInToCivitaiUseCase,
) : BaseMviViewModel<CivitaiState, CivitaiIntent, EmptyEffect>(
    diagnostics,
    NavigationDestination.CIVITAI.asDiagnosticScreen(),
    CivitaiState(),
    dispatchers.immediate,
) {
    init {
        perform {}
    }

    /**
     * Handles account and sharing actions, ignoring repeated operations while a request is active.
     *
     * @param intent User action or dismissal from the Civitai settings screen.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: CivitaiIntent) {
        if (currentState.isBusy && intent != CivitaiIntent.Back) return
        when (intent) {
            is CivitaiIntent.Connect -> perform { repository.connect(intent.key.trim()) }
            CivitaiIntent.SignIn -> perform { signIn() }
            CivitaiIntent.Disconnect -> perform { repository.disconnect() }
            CivitaiIntent.OpenHistory -> router.navigateToCivitaiHistory()
            CivitaiIntent.EnableAutoUpload -> updateState { it.copy(shouldConfirmAutoUpload = true) }
            CivitaiIntent.ConfirmAutoUpload -> perform { repository.setAutoUploadEnabled(true) }
            CivitaiIntent.DisableAutoUpload -> perform { repository.setAutoUploadEnabled(false) }
            CivitaiIntent.Dismiss -> updateState {
                it.copy(
                    hasFailed = false,
                    shouldConfirmAutoUpload = false
                )
            }

            CivitaiIntent.Back -> router.navigateBack()
        }
    }

    /**
     * Runs a user operation before loading a fresh coherent account view.
     *
     * @param operation Strict mutation, or empty for the initial read.
     * @author Dmitriy Moroz
     */
    private fun perform(operation: suspend () -> Unit) {
        updateState { it.copy(isBusy = true, hasFailed = false, shouldConfirmAutoUpload = false) }
        launch(dispatchers.io) {
            try {
                operation()
                val account = repository.account()
                val isAutomatic = repository.isAutoUploadEnabled()
                updateState {
                    it.copy(
                        account = account,
                        isAutoUploadEnabled = isAutomatic,
                        isBusy = false
                    )
                }
            } catch (error: CancellationException) {
                updateState { it.copy(isBusy = false) }
                throw error
            } catch (_: CivitaiAuthorizationCancelled) {
                updateState { it.copy(isBusy = false) }
            } catch (_: Exception) {
                updateState { it.copy(isBusy = false, hasFailed = true) }
            }
        }
    }
}
