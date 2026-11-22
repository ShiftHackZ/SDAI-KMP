package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.entity.backup.BackupExport
import com.shifthackz.aisdv1.domain.usecase.backup.CreateBackupUseCase
import com.shifthackz.aisdv1.feature.backup.export.BackupExportCoordinator
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import kotlinx.coroutines.withContext

/**
 * Runs the strict create archive operation on the IO dispatcher.
 *
 * @param coordinator Owns source creation and durable association before document presentation.
 * @param dispatchers Runs file and database work off the UI thread.
 * @author Dmitriy Moroz
 */
internal class CreateBackupUseCaseImpl(
    private val coordinator: BackupExportCoordinator,
    private val dispatchers: DispatchersProvider
) : CreateBackupUseCase {

    override suspend operator fun invoke(
        selection: BackupSelection,
        onProgress: (BackupProgress) -> Unit
    ): BackupExport = withContext(dispatchers.io) { coordinator.prepare(selection, onProgress) }
}
