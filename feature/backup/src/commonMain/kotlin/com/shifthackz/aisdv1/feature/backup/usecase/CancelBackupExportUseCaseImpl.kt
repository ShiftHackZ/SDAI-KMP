package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.usecase.backup.CancelBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.export.BackupExportCoordinator

/**
 * Interrupts active destination copying while retaining a durable source for an explicit retry.
 * @param coordinator Owns durable export association independently from screen lifetimes.
 * @author Dmitriy Moroz
 */
internal class CancelBackupExportUseCaseImpl(private val coordinator: BackupExportCoordinator) :
    CancelBackupExportUseCase {

    override fun invoke(): Unit = coordinator.cancel()
}
