package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.gateway.backup.BackupPreferencesGateway
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test

class SetBackupStartupStageUseCaseImplTest {
    @Test
    fun `given setup already complete, stale first launch screen requests configuration, expected durable completion preserved`() {
        // Given
        val preferences =
            mockk<BackupPreferencesGateway>(relaxed = true) { every { readStartupStage() } returns BackupStartupStage.COMPLETE }
        val useCase = SetBackupStartupStageUseCaseImpl(preferences)
        // When
        useCase(BackupStartupStage.CONFIGURATION)
        // Then
        verify(exactly = 0) { preferences.writeStartupStage(any()) }
    }

    @Test
    fun `given invitation stage, user chooses restore, expected checked forward transition before navigation`() {
        // Given
        val preferences =
            mockk<BackupPreferencesGateway>(relaxed = true) { every { readStartupStage() } returns BackupStartupStage.INVITATION }
        val useCase = SetBackupStartupStageUseCaseImpl(preferences)
        // When
        useCase(BackupStartupStage.RESTORE)
        // Then
        verify(exactly = 1) { preferences.writeStartupStage(BackupStartupStage.RESTORE) }
    }
}
