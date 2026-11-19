package com.shifthackz.aisdv1.feature.backup.usecase

import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.domain.gateway.backup.BackupExportStateStore
import com.shifthackz.aisdv1.domain.usecase.backup.GetPendingBackupExportUseCase
import kotlinx.coroutines.withContext

/**
 * Routes durable pending or completed export outcomes back to the dedicated backup screen.
 * @param records Checked nonsecret export association.
 * @param dispatchers Keeps preference recovery on IO.
 * @author Dmitriy Moroz
 */
internal class GetPendingBackupExportUseCaseImpl(
    private val records: BackupExportStateStore,
    private val dispatchers: DispatchersProvider
) : GetPendingBackupExportUseCase {
    override suspend fun invoke(): Boolean = withContext(dispatchers.io) {
        records.recoverPendingWrite()
        records.read() != null
    }
}
