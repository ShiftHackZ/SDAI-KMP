package com.shifthackz.aisdv1.presentation.statistics

import com.shifthackz.aisdv1.presentation.components.service.storage.StorageUsageObserver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** Deterministic revision source for screen state tests. @author Dmitriy Moroz */
internal class FakeStorageUsageObserver : StorageUsageObserver {

    private val revision = MutableStateFlow(0L)

    override fun observe(): Flow<Long> = revision

    override fun notifyChanged() {
        revision.value += 1L
    }
}
