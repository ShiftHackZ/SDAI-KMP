package com.shifthackz.aisdv1.domain.usecase.splash

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.entity.backup.BackupRecovery
import com.shifthackz.aisdv1.domain.entity.backup.BackupRestoreRequest
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import com.shifthackz.aisdv1.domain.usecase.backup.GetBackupStartupStageUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.LoadPendingBackupUseCase
import com.shifthackz.aisdv1.domain.usecase.backup.GetPendingBackupExportUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SplashNavigationUseCaseImplTest {
    private val preferences = mockk<PreferenceManager>(relaxed = true)
    private val stage = mockk<GetBackupStartupStageUseCase>()
    private val pending = mockk<LoadPendingBackupUseCase>()
    private val export = mockk<GetPendingBackupExportUseCase>()
    private val useCase = SplashNavigationUseCaseImpl(preferences, stage, pending, export)

    @Before
    fun setup() {
        every { preferences.onBoardingComplete } returns true
        every { preferences.source } returns ServerSource.AUTOMATIC1111
        every { preferences.automatic1111ServerUrl } returns "https://server"
        every { stage() } returns BackupStartupStage.COMPLETE
        coEvery { pending() } returns null
        coEvery { export() } returns false
    }

    @Test
    fun `given first app launch, onboarding incomplete, expected onboarding route`() = runTest {
        // Given
        every { preferences.onBoardingComplete } returns false
        // When
        val action = useCase()
        // Then
        assertEquals(SplashNavigationUseCase.Action.LAUNCH_ONBOARDING, action)
    }

    @Test
    fun `given completed onboarding, invitation stage is pending, expected restore invitation`() =
        runTest {
            // Given
            every { stage() } returns BackupStartupStage.INVITATION
            // When
            val action = useCase()
            // Then
            assertEquals(SplashNavigationUseCase.Action.LAUNCH_BACKUP_WELCOME, action)
        }

    @Test
    fun `given first launch restore, process restarts, expected restore only route`() = runTest {
        // Given
        every { stage() } returns BackupStartupStage.RESTORE
        // When
        val action = useCase()
        // Then
        assertEquals(SplashNavigationUseCase.Action.LAUNCH_INITIAL_RESTORE, action)
    }

    @Test
    fun `given first launch recovery, user proceeds to configuration, expected setup even with restored fields`() =
        runTest {
            // Given
            every { stage() } returns BackupStartupStage.CONFIGURATION
            // When
            val action = useCase()
            // Then
            assertEquals(SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP, action)
        }

    @Test
    fun `given existing configured installation, backup stage is complete, expected normal home route`() =
        runTest {
            // Given
            every { stage() } returns BackupStartupStage.COMPLETE
            // When
            val action = useCase()
            // Then
            assertEquals(SplashNavigationUseCase.Action.LAUNCH_HOME, action)
        }

    @Test
    fun `given existing installation, provider setup required by update, expected setup route`() =
        runTest {
            // Given
            every { preferences.forceSetupAfterUpdate } returns true
            // When
            val action = useCase()
            // Then
            assertEquals(SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP, action)
        }

    @Test
    fun `given A1111 selection, endpoint is absent, expected setup route`() = runTest {
        // Given
        every { preferences.automatic1111ServerUrl } returns ""
        // When
        val action = useCase()
        // Then
        assertEquals(SplashNavigationUseCase.Action.LAUNCH_SERVER_SETUP, action)
    }

    @Test
    fun `given local provider selection, A1111 endpoint absent, expected normal home route`() =
        runTest {
            // Given
            every { preferences.source } returns ServerSource.LOCAL_MICROSOFT_ONNX
            every { preferences.automatic1111ServerUrl } returns ""
            // When
            val action = useCase()
            // Then
            assertEquals(SplashNavigationUseCase.Action.LAUNCH_HOME, action)
        }

    @Test
    fun `given interrupted settings restore, process restarts, expected pending restore before home`() =
        runTest {
            // Given
            coEvery { pending() } returns BackupRecovery(
                BackupRestoreRequest(
                    "archive",
                    BackupSelection(),
                    "session"
                ), BackupInspection("archive", 1, "1", "IOS", emptyList(), emptySet())
            )
            // When
            val action = useCase()
            // Then
            assertEquals(SplashNavigationUseCase.Action.LAUNCH_BACKUP_RESTORE, action)
        }

    @Test
    fun `given native export pending, process starts, expected backup route reconnects document result`() =
        runTest {
            // Given
            coEvery { export() } returns true
            // When
            val action = useCase()
            // Then
            assertEquals(SplashNavigationUseCase.Action.LAUNCH_BACKUP_RESTORE, action)
        }

}
