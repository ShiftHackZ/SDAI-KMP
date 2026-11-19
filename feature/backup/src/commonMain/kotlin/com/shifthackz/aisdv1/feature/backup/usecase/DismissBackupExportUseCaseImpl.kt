package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.usecase.backup.DismissBackupExportUseCase
import com.shifthackz.aisdv1.feature.backup.export.BackupExportCoordinator

/**
 * Acknowledges export state before releasing private source ownership.
 * @param coordinator Process-owned serialized export lifecycle.
 * @author Dmitriy Moroz
 */
internal class DismissBackupExportUseCaseImpl(private val coordinator: BackupExportCoordinator) :
    DismissBackupExportUseCase {
    override suspend fun invoke() = coordinator.dismiss()
}
