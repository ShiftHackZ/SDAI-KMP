package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.usecase.backup.LoadBackupInventoryUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import kotlinx.coroutines.withContext

/**
 * Runs the strict inventory archive operation on the IO dispatcher.
 *
 * @param archives Injected archive framing and staging owner.
 * @param dispatchers Runs file and database work off the UI thread.
 * @author Dmitriy Moroz
 */
internal class LoadBackupInventoryUseCaseImpl(
    private val archives: BackupArchiveStore,
    private val dispatchers: DispatchersProvider
) : LoadBackupInventoryUseCase {

    override suspend operator fun invoke(): BackupInspection =
        withContext(dispatchers.io) { archives.inventory() }
}
