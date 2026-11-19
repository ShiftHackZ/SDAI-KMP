package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.usecase.backup.CompleteBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.export.BackupExportCoordinator
import com.shifthackz.aisdv1.domain.entity.backup.BackupExportCompletion

/**
 * Queues native outcomes through the graph owner, including results delivered before screen initialization.
 * @param coordinator Owns durable export association independently from screen lifetimes.
 * @author Dmitriy Moroz
 */
internal class CompleteBackupExportUseCaseImpl(private val coordinator: BackupExportCoordinator) :
    CompleteBackupExportUseCase {
    override fun invoke(completion: BackupExportCompletion): Unit = coordinator.submit(completion)
}
