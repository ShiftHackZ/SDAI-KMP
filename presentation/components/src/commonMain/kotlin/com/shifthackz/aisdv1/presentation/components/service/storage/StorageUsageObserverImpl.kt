package com.shifthackz.aisdv1.presentation.components.service.storage

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Maintains one process-wide revision shared by storage writers and readers.
 *
 * @author Dmitriy Moroz
 */
internal class StorageUsageObserverImpl : StorageUsageObserver {
    private val revision = MutableStateFlow(0L)

    override fun observe(): Flow<Long> = revision

    override fun notifyChanged() {
        revision.update { it + 1L }
    }
}
