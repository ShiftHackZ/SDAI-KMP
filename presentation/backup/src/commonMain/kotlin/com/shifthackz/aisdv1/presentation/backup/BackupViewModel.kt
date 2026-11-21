package com.shifthackz.aisdv1.presentation.backup

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.BaseMviViewModel
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnostics
import com.shifthackz.aisdv1.core.mvi.diagnostics.asDiagnosticScreen
import com.shifthackz.aisdv1.core.navigation.NavigationDestination
import com.shifthackz.aisdv1.core.navigation.router.BackupRouter
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreRequest
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.usecase.backup.CreateBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.InspectBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.RestoreBackupUseCase
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportCompletion
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportStatus
import com.shifthackz.aisdv1.domain.usecase.backup.ObserveBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.CompleteBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.CancelBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.DismissBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.GetBackupStartupStageUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.DiscardBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.LoadBackupInventoryUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.LoadPendingBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.SetBackupStartupStageUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Owns independent tab selections, native picker outcomes and resumable restore intent.
 * @param mode Settings tabs or first-launch recovery context.
 * @param router Caller-owned navigation contract.
 * @param diagnostics Bounded nonsecret MVI diagnostics.
 * @param dispatchers IO work and immediate UI publication dispatchers.
 * @param inventory Loads current selectable app data.
 * @param createBackup Creates a complete staged binary archive.
 * @param inspectBackup Stages and validates a native document.
 * @param restoreBackup Merges selected categories with durable retry identity.
 * @param observeExport Observes graph-owned export work across host recreation.
 * @param completeExport Queues native outcomes independently of screen initialization.
 * @param cancelExport Interrupts destination copying while retaining its owned source.
 * @param dismissExport Acknowledges export completion or abandons a staged export.
 * @param getStartupStage Reads acknowledged first-launch progression after pending recovery.
 * @param discardBackup Releases staging after resolving checked writes.
 * @param loadPending Loads nonsecret process-recovery intent.
 * @param setStartupStage Persists first-launch progression before navigation.
 * @author Dmitriy Moroz
 */
class BackupViewModel(
    val mode: BackupScreenMode,
    private val router: BackupRouter,
    diagnostics: UiDiagnostics,
    private val dispatchers: DispatchersProvider,
    private val inventory: LoadBackupInventoryUseCase,
    private val createBackup: CreateBackupUseCase,
    private val inspectBackup: InspectBackupUseCase,
    private val restoreBackup: RestoreBackupUseCase,
    private val observeExport: ObserveBackupExportUseCase,
    private val completeExport: CompleteBackupExportUseCase,
    private val cancelExport: CancelBackupExportUseCase,
    private val dismissExport: DismissBackupExportUseCase,
    private val getStartupStage: GetBackupStartupStageUseCase,
    private val discardBackup: DiscardBackupUseCase,
    private val loadPending: LoadPendingBackupUseCase,
    private val setStartupStage: SetBackupStartupStageUseCase,
) : BaseMviViewModel<BackupState, BackupIntent, BackupEffect>(
    diagnostics, NavigationDestination.BACKUP.asDiagnosticScreen(),
    BackupState(isRestoreTab = mode == BackupScreenMode.FIRST_LAUNCH), dispatchers.immediate,
) {
    private var operation: Job? = null
    private var documentResult: Job? = null
    private var documentResultId = 0L
    private var pendingExport: BackupExport? = null
    private var restoreRequest: BackupRestoreRequest? = null

    private val exportObserver = launch(dispatchers.immediate) {
        observeExport().collect { export ->
            pendingExport = export.backup
            updateState {
                it.copy(
                    isExporting = export.status == BackupExportStatus.COPYING || export.status == BackupExportStatus.PROCESSING,
                    hasPendingExport = export.backup != null,
                    hasExported = export.status == BackupExportStatus.COMPLETE,
                    hasCanceled = it.hasCanceled || export.status == BackupExportStatus.CANCELED,
                    hasError = it.hasError || export.status == BackupExportStatus.FAILED,
                    isFeedbackVisible = it.isFeedbackVisible || export.status in setOf(
                        BackupExportStatus.COMPLETE,
                        BackupExportStatus.FAILED,
                        BackupExportStatus.CANCELED,
                    ),
                )
            }
        }
    }

    init {
        if (mode != BackupScreenMode.WELCOME) runOperation(shouldClearFeedback = false) {
            val recovery = loadPending()
            if (recovery != null) {
                restoreRequest = recovery.request
                updateState {
                    it.copy(
                        isRestoreTab = true,
                        hasPendingSession = true,
                        isRestoreSelectionLocked = true,
                        restore = BackupPageState(recovery.inspection, recovery.request.selection)
                    )
                }
            }
            if (recovery == null && mode == BackupScreenMode.FIRST_LAUNCH) {
                when (getStartupStage()) {
                    BackupStartupStage.CONFIGURATION -> {
                        withContext(dispatchers.immediate) { router.navigateToInitialConfiguration() }
                        return@runOperation
                    }

                    BackupStartupStage.COMPLETE -> {
                        withContext(dispatchers.immediate) { router.navigateToHomeScreen() }
                        return@runOperation
                    }

                    BackupStartupStage.INVITATION, BackupStartupStage.RESTORE -> Unit
                }
            }
            val values = inventory()
            updateState { it.copy(create = BackupPageState(values, allSelected(values))) }
        }
    }

    /**
     * Applies UI choices or native document outcomes while rejecting overlapping jobs.
     * @param intent User action or native document picker result.
     * @author Dmitriy Moroz
     */
    override fun processIntent(intent: BackupIntent) {
        when (intent) {
            BackupIntent.CancelWork -> {
                documentResult?.cancel(); operation?.cancel(); cancelExport()
            }

            BackupIntent.NavigateBack -> if (canInteract()) leave()
            BackupIntent.Continue -> transition(BackupStartupStage.CONFIGURATION) { router.navigateToInitialConfiguration() }
            BackupIntent.BeginInitialRestore -> transition(BackupStartupStage.RESTORE) { router.navigateToInitialRestore() }
            is BackupIntent.SelectTab -> if (canInteract()) updateState {
                it.copy(
                    isRestoreTab = intent.isRestore,
                    result = null,
                    hasError = false,
                    hasCanceled = false,
                    isFeedbackVisible = false,
                )
            }

            is BackupIntent.ToggleCategory -> if (canEdit()) editPage { page ->
                val selected = page.selection.categories
                val categories =
                    if (intent.category in selected) selected - intent.category else selected + intent.category
                val providers = if (intent.category == BackupCategory.PROVIDERS) {
                    if (intent.category in categories) page.inspection?.providers.orEmpty() else emptySet()
                } else page.selection.providers
                page.copy(selection = BackupSelection(categories, providers))
            }

            is BackupIntent.ToggleProvider -> if (canEdit()) editPage { page ->
                val providers =
                    page.selection.providers.let { if (intent.key in it) it - intent.key else it + intent.key }
                val categories =
                    if (providers.isEmpty()) page.selection.categories - BackupCategory.PROVIDERS else page.selection.categories + BackupCategory.PROVIDERS
                page.copy(selection = BackupSelection(categories, providers))
            }

            BackupIntent.ToggleProvidersExpanded -> if (canInteract()) editPage {
                it.copy(
                    areProvidersExpanded = !it.areProvidersExpanded
                )
            }

            BackupIntent.ChooseFile -> if (canInteract()) runOperation {
                updateState { it.copy(isPicking = true) }
                emitEffect(BackupEffect.OpenDocument)
            }

            is BackupIntent.DocumentSelected -> selectDocument(intent.document)

            BackupIntent.Submit -> if (canInteract()) submit(null)
            BackupIntent.ConfirmProviderReplacement -> currentState.conflict?.let { submit(it.fingerprint) }
            BackupIntent.DismissConflict -> {
                if (!currentState.hasPendingSession) restoreRequest = null
                updateState {
                    it.copy(
                        conflict = null,
                        isRestoreSelectionLocked = restoreRequest != null
                    )
                }
            }

            BackupIntent.DismissFeedback -> if (canInteract()) {
                if (mode == BackupScreenMode.FIRST_LAUNCH && currentState.result?.hasFailed == false) {
                    leave()
                } else updateState { it.copy(isFeedbackVisible = false) }
            }

            is BackupIntent.ExportFinished -> completeExport(intent)
        }
    }

    private fun selectDocument(document: String?) {
        if (document == null) {
            updateState { it.copy(isPicking = false) }
            return
        }
        documentResult?.cancel()
        val resultId = ++documentResultId
        val preceding = operation
        updateState { it.copy(isPicking = true) }
        documentResult = launch(dispatchers.io) {
            try {
                preceding?.join()
                withContext(dispatchers.immediate) {
                    updateState { it.copy(isPicking = false, isRestoreTab = true) }
                    runOperation {
                        val inspected = inspectBackup(document, ::progress)
                        try {
                            clearRestore()
                        } catch (failure: Throwable) {
                            discardBackup(inspected.handle)
                            throw failure
                        }
                        updateState {
                            it.copy(
                                restore = BackupPageState(inspected, allSelected(inspected)),
                                hasPendingSession = false,
                                isRestoreSelectionLocked = false,
                                result = null,
                            )
                        }
                    }
                }
            } finally {
                if (resultId == documentResultId) updateState { it.copy(isPicking = false) }
            }
        }
    }

    private fun submit(fingerprint: String?) {
        val snapshot = currentState
        if (!snapshot.isRestoreTab) {
            if (snapshot.create.selection.categories.isEmpty()) return
            runOperation {
                val backup = pendingExport ?: createBackup(snapshot.create.selection, ::progress)
                pendingExport = backup
                updateState { it.copy(isPicking = true) }
                emitEffect(BackupEffect.ExportDocument(backup))
            }
        } else {
            val inspection = snapshot.restore.inspection ?: return
            if (snapshot.restore.selection.categories.isEmpty()) return
            val request = (restoreRequest ?: BackupRestoreRequest(
                inspection.handle,
                snapshot.restore.selection,
                newSessionId(),
                shouldProceedToConfiguration = mode == BackupScreenMode.FIRST_LAUNCH
            )).copy(confirmedFingerprint = fingerprint)
            restoreRequest = request
            updateState { it.copy(conflict = null, isRestoreSelectionLocked = true) }
            runOperation {
                when (val result = restoreBackup(request, ::progress)) {
                    is BackupRestoreResult.Conflict -> updateState { it.copy(conflict = result) }
                    is BackupRestoreResult.Finished -> {
                        updateState {
                            it.copy(
                                result = result,
                                hasPendingSession = result.hasFailed,
                                isFeedbackVisible = true,
                            )
                        }
                    }
                }
            }
        }
    }

    private fun completeExport(result: BackupIntent.ExportFinished) {
        updateState { it.copy(isPicking = false, isExporting = true) }
        // This callback may precede asynchronous initialization on a recreated host.
        completeExport(
            when {
                result.destination != null -> BackupExportCompletion.DestinationSelected(
                    result.destination,
                    result.handle
                )

                result.hasExported -> BackupExportCompletion.NativeCompleted(result.handle)
                else -> BackupExportCompletion.Canceled(result.handle)
            }
        )
    }

    private fun leave() = runOperation(isLeaving = true) {
        if (mode == BackupScreenMode.FIRST_LAUNCH) setStartupStage(BackupStartupStage.CONFIGURATION)
        clearRestore()
        dismissExport()
        pendingExport = null
        if (mode == BackupScreenMode.FIRST_LAUNCH) {
            withContext(dispatchers.immediate) { router.navigateToInitialConfiguration() }
        } else withContext(dispatchers.immediate) { router.navigateBack() }
    }

    private suspend fun clearRestore() {
        currentState.restore.inspection?.let { discardBackup(it.handle) }
        restoreRequest = null
        updateState {
            it.copy(
                restore = BackupPageState(),
                hasPendingSession = false,
                isRestoreSelectionLocked = false
            )
        }
    }

    private fun transition(stage: BackupStartupStage, navigate: () -> Unit) {
        if (!canInteract()) return
        runOperation {
            setStartupStage(stage)
            withContext(dispatchers.immediate) { navigate() }
        }
    }

    /**
     * Serializes bounded work and publishes cancellable loading plus acknowledged failure feedback.
     * @param shouldClearFeedback Whether a new user operation replaces the prior feedback; startup keeps durable export completion.
     * @param isLeaving Whether this job prepares route exit without presenting a progress dialog.
     * @param block Work run on IO with state publication through the MVI owner.
     * @author Dmitriy Moroz
     */
    private fun runOperation(
        shouldClearFeedback: Boolean = true,
        isLeaving: Boolean = false,
        block: suspend () -> Unit
    ) {
        if (operation?.isActive == true) return
        updateState {
            it.copy(
                isBusy = true,
                isLeaving = isLeaving,
                hasError = if (shouldClearFeedback) false else it.hasError,
                hasCanceled = if (shouldClearFeedback) false else it.hasCanceled,
                isFeedbackVisible = if (shouldClearFeedback) false else it.isFeedbackVisible,
                progress = null
            )
        }
        operation = launch(dispatchers.io) {
            try {
                block()
            } catch (cancelled: CancellationException) {
                updateState { it.copy(hasCanceled = true, isFeedbackVisible = true) }
                throw cancelled
            } catch (_: Exception) {
                updateState { it.copy(hasError = true, isFeedbackVisible = true) }
            } finally {
                updateState { it.copy(isBusy = false, isLeaving = false, progress = null) }
            }
        }
    }

    private fun progress(value: BackupProgress) = updateState { it.copy(progress = value) }
    private fun canInteract() =
        !currentState.isBusy && !currentState.isPicking && !currentState.isExporting

    private fun canEdit() =
        canInteract() && (if (currentState.isRestoreTab) !currentState.isRestoreSelectionLocked else !currentState.hasPendingExport)

    private fun editPage(transform: (BackupPageState) -> BackupPageState) = updateState {
        if (it.isRestoreTab) it.copy(restore = transform(it.restore)) else it.copy(
            create = transform(
                it.create
            )
        )
    }

    private fun allSelected(summary: BackupInspection) =
        BackupSelection(
            summary.entries.filter { it.isAvailable }.map { it.category }.toSet(),
            summary.providers
        )

    private fun newSessionId(): String =
        List(16) { Random.nextInt(256).toString(16).padStart(2, '0') }.joinToString("").let {
            it.take(8) + "-" + it.substring(8, 12) + "-" + it.substring(
                12,
                16
            ) + "-" + it.substring(16, 20) + "-" + it.substring(20)
        }

    /**
     * Stops owned work on route disposal. Interrupted restore staging remains journal-owned for recovery.
     * @author Dmitriy Moroz
     */
    fun onRouteDisposed() {
        documentResult?.cancel()
        operation?.cancel()
        exportObserver.cancel()
    }
}
