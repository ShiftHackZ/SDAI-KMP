package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.usecase.backup.ExportBackupUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import kotlinx.coroutines.withContext

/**
 * Runs the strict export archive operation on the IO dispatcher.
 * @param archives Injected archive framing and staging owner.
 * @param dispatchers Runs file and database work off the UI thread.
 * @author Dmitriy Moroz
 */
internal class ExportBackupUseCaseImpl(
    private val archives: BackupArchiveStore,
    private val dispatchers: DispatchersProvider
) : ExportBackupUseCase {

    override suspend operator fun invoke(document: String, destination: String): Unit =
        withContext(dispatchers.io) { archives.export(document, destination) }
}
