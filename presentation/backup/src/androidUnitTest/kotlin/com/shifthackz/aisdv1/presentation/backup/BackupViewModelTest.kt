package com.shifthackz.aisdv1.presentation.backup

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.navigation.router.BackupRouter
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupEntry
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreResult
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreRequest
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.usecase.backup.CreateBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.InspectBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.RestoreBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.ObserveBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.CompleteBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.CancelBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.DismissBackupExportUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.GetBackupStartupStageUseCase
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportOperation
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportStatus
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportCompletion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.CompletableDeferred
import com.shifthackz.aisdv1.domain.usecase.backup.DiscardBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.LoadBackupInventoryUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.LoadPendingBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.SetBackupStartupStageUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BackupViewModelTest {
    private val router = mockk<BackupRouter>(relaxed = true)
    private val inventory = mockk<LoadBackupInventoryUseCase>()
    private val create = mockk<CreateBackupUseCase>()
    private val inspect = mockk<InspectBackupUseCase>()
    private val restore = mockk<RestoreBackupUseCase>()
    private val exportState = MutableStateFlow(BackupExportOperation())
    private val observeExport =
        mockk<ObserveBackupExportUseCase> { every { this@mockk.invoke() } returns exportState }
    private val completeExport = mockk<CompleteBackupExportUseCase>(relaxed = true)
    private val cancelExport = mockk<CancelBackupExportUseCase>(relaxed = true)
    private val dismissExport = mockk<DismissBackupExportUseCase>(relaxed = true)
    private val getStage =
        mockk<GetBackupStartupStageUseCase> { every { this@mockk.invoke() } returns BackupStartupStage.RESTORE }
    private val discard = mockk<DiscardBackupUseCase>(relaxed = true)
    private val pending = mockk<LoadPendingBackupUseCase>()
    private val startup = mockk<SetBackupStartupStageUseCase>(relaxed = true)
    private val summary = BackupInspection(
        "11111111-1111-1111-1111-111111111111",
        100,
        "1",
        "IOS",
        listOf(BackupEntry(BackupCategory.GALLERY, 2), BackupEntry(BackupCategory.SETTINGS, 1)),
        emptySet()
    )

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun TestScope.viewModel(
        mode: BackupScreenMode = BackupScreenMode.SETTINGS,
        shouldUseImmediateDispatcher: Boolean = false,
    ): BackupViewModel {
        val dispatcher =
            if (shouldUseImmediateDispatcher) UnconfinedTestDispatcher(testScheduler) else StandardTestDispatcher(
                testScheduler
            )
        Dispatchers.setMain(dispatcher)
        coEvery { pending() } returns null
        coEvery { inventory() } returns summary
        val dispatchers =
            mockk<DispatchersProvider> { every { io } returns dispatcher; every { immediate } returns dispatcher }
        return BackupViewModel(
            mode,
            router,
            mockk(relaxed = true),
            dispatchers,
            inventory,
            create,
            inspect,
            restore,
            observeExport,
            completeExport,
            cancelExport,
            dismissExport,
            getStage,
            discard,
            pending,
            startup
        )
    }

    @Test
    fun `given backup tabs, selection changes before switching twice, expected create selection retained`() =
        runTest {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.ToggleCategory(BackupCategory.GALLERY))
            vm.processIntent(BackupIntent.SelectTab(true))
            vm.processIntent(BackupIntent.SelectTab(false))
            // Then
            assertEquals(setOf(BackupCategory.SETTINGS), vm.state.value.create.selection.categories)
            assertFalse(vm.state.value.isRestoreTab)
        }

    @Test
    fun `given restore file picker, user cancels document selection, expected neutral state without inspection`() =
        runTest {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            vm.processIntent(BackupIntent.SelectTab(true))
            vm.processIntent(BackupIntent.ChooseFile)
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.DocumentSelected(null))
            advanceUntilIdle()
            // Then
            assertFalse(vm.state.value.isPicking)
            assertFalse(vm.state.value.hasError)
            assertNull(vm.state.value.restore.inspection)
            coVerify(exactly = 0) { inspect(any(), any()) }
        }

    @Test
    fun `given first launch invitation, user continues, expected configuration stage and route`() =
        runTest {
            // Given
            val vm = viewModel(BackupScreenMode.WELCOME)
            // When
            vm.processIntent(BackupIntent.Continue)
            advanceUntilIdle()
            // Then
            verify { startup(BackupStartupStage.CONFIGURATION); router.navigateToInitialConfiguration() }
            verify(exactly = 0) { router.navigateBack() }
        }

    @Test
    fun `given first launch restore screen, user cancels decision, expected configuration without returning onboarding`() =
        runTest {
            // Given
            val vm = viewModel(BackupScreenMode.FIRST_LAUNCH)
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.NavigateBack)
            advanceUntilIdle()
            // Then
            verify { startup(BackupStartupStage.CONFIGURATION); router.navigateToInitialConfiguration() }
            verify(exactly = 0) { router.navigateBack() }
        }

    @Test
    fun `given staged export, coordinator reports write failure, expected error without saved success`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { create(any(), any()) } returns BackupExport(
                "file:///private/archive",
                "backup.sdaibackup",
                "export"
            )

            advanceUntilIdle()
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.ExportFinished("content://destination", false, "export"))
            advanceUntilIdle()
            exportState.value = BackupExportOperation(BackupExportStatus.FAILED)
            advanceUntilIdle()
            // Then
            assertTrue(vm.state.value.hasError)
            assertFalse(vm.state.value.hasExported)
            verify {
                completeExport(
                    BackupExportCompletion.DestinationSelected(
                        "content://destination",
                        "export"
                    )
                )
            }
            coVerify(exactly = 0) { discard(any()) }
        }

    @Test
    fun `given restore conflict dialog, user cancels replacement, expected editable selection and no success`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inspect(any(), any()) } returns summary
            coEvery { restore(any(), any()) } returns BackupRestoreResult.Conflict(
                setOf("custom"),
                "fingerprint"
            )
            advanceUntilIdle()
            vm.processIntent(BackupIntent.SelectTab(true))
            vm.processIntent(BackupIntent.DocumentSelected("content://backup"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.DismissConflict)
            vm.processIntent(BackupIntent.ToggleCategory(BackupCategory.GALLERY))
            // Then
            assertNull(vm.state.value.conflict)
            assertEquals(
                setOf(BackupCategory.SETTINGS),
                vm.state.value.restore.selection.categories
            )
            assertNull(vm.state.value.result)
        }

    @Test
    fun `given first launch restore, user acknowledges successful result, expected restored setup flow`() =
        runTest {
            // Given
            val vm = viewModel(BackupScreenMode.FIRST_LAUNCH)
            coEvery { inspect(any(), any()) } returns summary
            coEvery { restore(any(), any()) } returns BackupRestoreResult.Finished(
                setOf(
                    BackupCategory.GALLERY,
                    BackupCategory.SETTINGS
                ), 0, false
            )
            advanceUntilIdle()
            vm.processIntent(BackupIntent.DocumentSelected("content://backup"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()
            assertTrue(vm.state.value.isFeedbackVisible)
            verify(exactly = 0) { router.navigateToInitialConfiguration() }
            // When
            vm.processIntent(BackupIntent.DismissFeedback)
            advanceUntilIdle()
            // Then
            verify { startup(BackupStartupStage.CONFIGURATION); router.navigateToInitialConfiguration() }
            coVerify { discard(summary.handle) }
            verify(exactly = 0) { router.navigateBack() }
        }

    @Test
    fun `given retained failed export, screen initializes on immediate dispatcher, expected failure feedback retained`() =
        runTest {
            // Given
            exportState.value = BackupExportOperation(
                BackupExportStatus.FAILED,
                BackupExport("source", "name", "handle")
            )

            // When
            val vm = viewModel(shouldUseImmediateDispatcher = true)
            advanceUntilIdle()

            // Then
            assertTrue(vm.state.value.hasError)
            assertTrue(vm.state.value.isFeedbackVisible)
            assertFalse(vm.state.value.isBusy)
            vm.onRouteDisposed()
        }

    @Test
    fun `given retained canceled export, screen initializes on immediate dispatcher, expected cancellation feedback retained`() =
        runTest {
            // Given
            exportState.value = BackupExportOperation(
                BackupExportStatus.CANCELED,
                BackupExport("source", "name", "handle")
            )

            // When
            val vm = viewModel(shouldUseImmediateDispatcher = true)
            advanceUntilIdle()

            // Then
            assertTrue(vm.state.value.hasCanceled)
            assertTrue(vm.state.value.isFeedbackVisible)
            assertFalse(vm.state.value.isBusy)
            vm.onRouteDisposed()
        }

    @Test
    fun `given failed backup inspection, user acknowledges error, expected file selection remains available`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inspect(any(), any()) } throws IllegalArgumentException("Invalid archive")
            advanceUntilIdle()
            vm.processIntent(BackupIntent.DocumentSelected("content://invalid"))
            advanceUntilIdle()
            assertTrue(vm.state.value.isFeedbackVisible)
            assertTrue(vm.state.value.hasError)

            // When
            vm.processIntent(BackupIntent.DismissFeedback)
            advanceUntilIdle()

            // Then
            assertFalse(vm.state.value.isFeedbackVisible)
            assertFalse(vm.state.value.isBusy)
            assertNull(vm.state.value.restore.inspection)
            verify(exactly = 0) { router.navigateBack(); router.navigateToInitialConfiguration() }
            vm.onRouteDisposed()
        }

    @Test
    fun `given partial restore result, user acknowledges failure, expected journal selection retained for retry`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inspect(any(), any()) } returns summary
            val result = BackupRestoreResult.Finished(setOf(BackupCategory.GALLERY), 0, true)
            coEvery { restore(any(), any()) } returns result
            advanceUntilIdle()
            vm.processIntent(BackupIntent.SelectTab(true))
            vm.processIntent(BackupIntent.DocumentSelected("content://backup"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()

            // When
            vm.processIntent(BackupIntent.DismissFeedback)
            advanceUntilIdle()

            // Then
            assertFalse(vm.state.value.isFeedbackVisible)
            assertEquals(result, vm.state.value.result)
            assertTrue(vm.state.value.hasPendingSession)
            assertTrue(vm.state.value.isRestoreSelectionLocked)
            coVerify(exactly = 0) { discard(any()) }
            vm.onRouteDisposed()
        }

    @Test
    fun `given inspected backup with custom selection, next picker is canceled, expected original file and choices retained`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inspect(any(), any()) } returns summary
            advanceUntilIdle()
            vm.processIntent(BackupIntent.SelectTab(true))
            vm.processIntent(BackupIntent.DocumentSelected("content://original"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.ToggleCategory(BackupCategory.GALLERY))
            // When
            vm.processIntent(BackupIntent.ChooseFile)
            advanceUntilIdle()
            vm.processIntent(BackupIntent.DocumentSelected(null))
            advanceUntilIdle()
            // Then
            assertEquals(summary, vm.state.value.restore.inspection)
            assertEquals(
                setOf(BackupCategory.SETTINGS),
                vm.state.value.restore.selection.categories
            )
            coVerify(exactly = 0) { discard(any()) }
        }

    @Test
    fun `given recreated backup route, native callback arrives before initialization completes, expected result queued without dropping destination`() =
        runTest {
            // Given
            val vm = viewModel()
            val initialization = CompletableDeferred<Unit>()
            every { completeExport(any()) } answers {
                exportState.value = BackupExportOperation(BackupExportStatus.PROCESSING)
            }
            coEvery { pending() } coAnswers { initialization.await(); null }
            // When
            vm.processIntent(
                BackupIntent.ExportFinished(
                    "content://destination",
                    false,
                    "durable-export"
                )
            )
            advanceUntilIdle()
            // Then
            verify(exactly = 1) {
                completeExport(
                    BackupExportCompletion.DestinationSelected(
                        "content://destination",
                        "durable-export"
                    )
                )
            }
            assertTrue(vm.state.value.isExporting)
            initialization.complete(Unit)
            advanceUntilIdle()
            vm.onRouteDisposed()
        }

    @Test
    fun `given reconstructed first launch route, recovery acknowledged configuration, expected direct setup navigation`() =
        runTest {
            // Given
            every { getStage() } returns BackupStartupStage.CONFIGURATION
            // When
            val vm = viewModel(BackupScreenMode.FIRST_LAUNCH)
            advanceUntilIdle()
            // Then
            verify(exactly = 1) { router.navigateToInitialConfiguration() }
            coVerify(exactly = 0) { inventory(); restore(any(), any()) }
            vm.onRouteDisposed()
        }

    @Test
    fun `given recreated export route, durable copy is running, expected editing disabled and source retained on disposal`() =
        runTest {
            // Given
            exportState.value = BackupExportOperation(
                BackupExportStatus.COPYING,
                BackupExport("source", "name", "handle")
            )
            val vm = viewModel()
            advanceUntilIdle()
            val original = vm.state.value.create.selection
            // When
            vm.processIntent(BackupIntent.ToggleCategory(BackupCategory.GALLERY))
            vm.onRouteDisposed()
            // Then
            assertEquals(original, vm.state.value.create.selection)
            assertTrue(vm.state.value.isExporting)
            verify(exactly = 0) { cancelExport() }
            coVerify(exactly = 0) { dismissExport(); discard(any()) }
        }

    @Test
    fun `given recreated import picker, document arrives during initialization, expected queued inspection and visible Restore tab`() =
        runTest {
            // Given
            val vm = viewModel()
            val initialized = CompletableDeferred<Unit>()
            coEvery { pending() } coAnswers { initialized.await(); null }
            coEvery { inspect("content://restored-result", any()) } returns summary
            runCurrent()
            // When
            vm.processIntent(BackupIntent.DocumentSelected("content://restored-result"))
            runCurrent()
            // Then
            coVerify(exactly = 0) { inspect(any(), any()) }
            assertTrue(vm.state.value.isPicking)
            initialized.complete(Unit)
            advanceUntilIdle()
            coVerify(exactly = 1) { inspect("content://restored-result", any()) }
            assertTrue(vm.state.value.isRestoreTab)
            assertEquals(summary, vm.state.value.restore.inspection)
            assertFalse(vm.state.value.isPicking)
            vm.onRouteDisposed()
        }

    @Test
    fun `given reconstructed first launch route, setup has already completed before handoff acknowledgment, expected home without empty restore screen`() =
        runTest {
            // Given
            every { getStage() } returns BackupStartupStage.COMPLETE
            // When
            val vm = viewModel(BackupScreenMode.FIRST_LAUNCH)
            advanceUntilIdle()
            // Then
            verify(exactly = 1) { router.navigateToHomeScreen() }
            verify(exactly = 0) { startup(any()); router.navigateToInitialConfiguration() }
            vm.onRouteDisposed()
        }

    @Test
    fun `given provider child choices, last child is unchecked then another selected, expected parent category follows child selection`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inventory() } returns summary.copy(
                entries = listOf(
                    BackupEntry(
                        BackupCategory.PROVIDERS,
                        2
                    )
                ), providers = setOf("custom", "swarm_ui")
            )
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.ToggleProvider("custom"))
            vm.processIntent(BackupIntent.ToggleProvider("swarm_ui"))
            val empty = vm.state.value.create.selection
            vm.processIntent(BackupIntent.ToggleProvider("swarm_ui"))
            // Then
            assertTrue(empty.categories.isEmpty())
            assertTrue(empty.providers.isEmpty())
            assertEquals(
                setOf(BackupCategory.PROVIDERS),
                vm.state.value.create.selection.categories
            )
            assertEquals(setOf("swarm_ui"), vm.state.value.create.selection.providers)
            vm.onRouteDisposed()
        }

    @Test
    fun `given expanded create providers, user collapses then switches tabs, expected independent expansion and retained selection`() =
        runTest {
            // Given
            val vm = viewModel()
            advanceUntilIdle()
            val original = vm.state.value.create.selection
            // When
            vm.processIntent(BackupIntent.ToggleProvidersExpanded)
            val isCreateExpanded = vm.state.value.create.areProvidersExpanded
            vm.processIntent(BackupIntent.SelectTab(true))
            vm.processIntent(BackupIntent.ToggleProvidersExpanded)
            vm.processIntent(BackupIntent.SelectTab(false))
            // Then
            assertEquals(isCreateExpanded, vm.state.value.create.areProvidersExpanded)
            assertEquals(original, vm.state.value.create.selection)
            vm.onRouteDisposed()
        }

    @Test
    fun `given provider replacement conflict, user confirms, expected same session and exact consent fingerprint`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inspect(any(), any()) } returns summary
            val requests = mutableListOf<BackupRestoreRequest>()
            coEvery { restore(any(), any()) } answers {
                requests += firstArg<BackupRestoreRequest>()
                if (requests.size == 1) BackupRestoreResult.Conflict(setOf("custom"), "revision")
                else BackupRestoreResult.Finished(setOf(BackupCategory.GALLERY), 0, false)
            }
            advanceUntilIdle()
            vm.processIntent(BackupIntent.DocumentSelected("source"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.ConfirmProviderReplacement)
            advanceUntilIdle()
            // Then
            assertEquals(2, requests.size)
            assertEquals(requests.first().sessionId, requests.last().sessionId)
            assertEquals("revision", requests.last().confirmedFingerprint)
            assertNull(vm.state.value.conflict)
            assertTrue(vm.state.value.isFeedbackVisible)
            vm.onRouteDisposed()
        }

    @Test
    fun `given failed partial restore, user edits then retries, expected original locked selection and session reused`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inspect(any(), any()) } returns summary
            val requests = mutableListOf<BackupRestoreRequest>()
            coEvery { restore(any(), any()) } answers {
                requests += firstArg<BackupRestoreRequest>()
                BackupRestoreResult.Finished(setOf(BackupCategory.GALLERY), 0, true)
            }
            advanceUntilIdle()
            vm.processIntent(BackupIntent.DocumentSelected("source"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.DismissFeedback)
            vm.processIntent(BackupIntent.ToggleCategory(BackupCategory.GALLERY))
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()
            // Then
            assertEquals(2, requests.size)
            assertEquals(requests.first(), requests.last())
            assertTrue(vm.state.value.hasPendingSession)
            vm.onRouteDisposed()
        }

    @Test
    fun `given backup creation failure, user submits selected data, expected failure dialog state without picker`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { create(any(), any()) } throws IllegalStateException("full disk")
            advanceUntilIdle()
            // When
            vm.processIntent(BackupIntent.Submit)
            advanceUntilIdle()
            // Then
            assertTrue(vm.state.value.hasError)
            assertTrue(vm.state.value.isFeedbackVisible)
            assertFalse(vm.state.value.isPicking)
            assertFalse(vm.state.value.isBusy)
            assertFalse(vm.state.value.hasExported)
            vm.onRouteDisposed()
        }

    @Test
    fun `given selected valid archive, replacement inspection fails, expected original file and selection retained`() =
        runTest {
            // Given
            val vm = viewModel()
            coEvery { inspect("original", any()) } returns summary
            coEvery { inspect("invalid", any()) } throws IllegalArgumentException("bad archive")
            advanceUntilIdle()
            vm.processIntent(BackupIntent.DocumentSelected("original"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.ToggleCategory(BackupCategory.GALLERY))
            val original = vm.state.value.restore
            // When
            vm.processIntent(BackupIntent.DocumentSelected("invalid"))
            advanceUntilIdle()
            // Then
            assertEquals(original, vm.state.value.restore)
            assertTrue(vm.state.value.hasError)
            coVerify(exactly = 0) { discard(any()) }
            vm.onRouteDisposed()
        }

    @Test
    fun `given running restore, progress is reported then user cancels, expected canceled feedback and cleared busy state`() =
        runTest {
            // Given
            val vm = viewModel()
            val running = CompletableDeferred<Unit>()
            coEvery { inspect(any(), any()) } returns summary
            coEvery { restore(any(), any()) } coAnswers {
                secondArg<(BackupProgress) -> Unit>()(BackupProgress(BackupCategory.GALLERY, 1, 2))
                running.await()
                BackupRestoreResult.Finished(emptySet(), 0, false)
            }
            advanceUntilIdle()
            vm.processIntent(BackupIntent.DocumentSelected("source"))
            advanceUntilIdle()
            vm.processIntent(BackupIntent.Submit)
            runCurrent()
            assertTrue(vm.state.value.isBusy)
            assertEquals(1L, vm.state.value.progress?.completed)
            // When
            vm.processIntent(BackupIntent.CancelWork)
            advanceUntilIdle()
            // Then
            assertTrue(vm.state.value.hasCanceled)
            assertTrue(vm.state.value.isFeedbackVisible)
            assertFalse(vm.state.value.isBusy)
            assertNull(vm.state.value.progress)
            verify { cancelExport() }
            vm.onRouteDisposed()
        }

    @Test
    fun `given first launch invitation, durable stage write fails, expected feedback and no navigation`() =
        runTest {
            // Given
            val vm = viewModel(BackupScreenMode.WELCOME)
            every { startup(any()) } throws IllegalStateException("storage unavailable")
            // When
            vm.processIntent(BackupIntent.BeginInitialRestore)
            advanceUntilIdle()
            // Then
            assertTrue(vm.state.value.hasError)
            assertTrue(vm.state.value.isFeedbackVisible)
            verify(exactly = 0) { router.navigateToInitialRestore() }
            vm.onRouteDisposed()
        }
}
