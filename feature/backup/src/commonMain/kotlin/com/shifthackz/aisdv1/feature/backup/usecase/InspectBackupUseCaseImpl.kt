package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.domain.entity.backup.BackupInspection
import com.shifthackz.aisdv1.domain.entity.backup.BackupProgress
import com.shifthackz.aisdv1.domain.usecase.backup.InspectBackupUseCase
import com.shifthackz.aisdv1.feature.backup.archive.BackupArchiveStore
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import kotlinx.coroutines.withContext

/**
 * Runs the strict inspect archive operation on the IO dispatcher.
 * @param archives Injected archive framing and staging owner.
 * @param dispatchers Runs file and database work off the UI thread.
 * @author Dmitriy Moroz
 */
internal class InspectBackupUseCaseImpl(
    private val archives: BackupArchiveStore,
    private val dispatchers: DispatchersProvider
) : InspectBackupUseCase {

    override suspend operator fun invoke(
        document: String,
        onProgress: (BackupProgress) -> Unit
    ): BackupInspection = withContext(dispatchers.io) { archives.inspect(document, onProgress) }
}
