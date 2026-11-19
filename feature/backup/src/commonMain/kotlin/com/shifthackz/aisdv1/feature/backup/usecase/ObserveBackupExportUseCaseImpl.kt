package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.usecase.backup.ObserveBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.export.BackupExportCoordinator
import kotlinx.coroutines.flow.StateFlow
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportOperation

/**
 * Shares export progress and completion with each recreated screen without transferring job ownership.
 * @param coordinator Owns durable export association independently from screen lifetimes.
 * @author Dmitriy Moroz
 */
internal class ObserveBackupExportUseCaseImpl(private val coordinator: BackupExportCoordinator) :
    ObserveBackupExportUseCase {
    override fun invoke(): StateFlow<BackupExportOperation> = coordinator.observe()
}
