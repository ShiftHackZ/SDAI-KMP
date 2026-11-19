package com.shifthackz.aisdv1.data.repository

import com.shifthackz.aisdv1.domain.repository.WakeLock
import com.shifthackz.aisdv1.domain.repository.WakeLockRepository

/**
 * Supplies the shared CPU wake-lock API on iOS, where no Android-style partial lock exists.
 * Display idle prevention belongs to the resumed application UI, which restores it on pause.
 *
 * @author Dmitriy Moroz
 */
internal class IosWakeLockRepository : WakeLockRepository {
    /**
     * Shared no-op CPU lock; display protection is managed by the resumed UI.
     * @author Dmitriy Moroz
     */
    override val wakeLock: WakeLock = NoOpIosWakeLock
}

/**
 * CPU wake locks are unsupported on iOS; foreground display protection is owned by the UI.
 * @author Dmitriy Moroz */
private object NoOpIosWakeLock : WakeLock {
    /**
     * Leaves CPU scheduling to iOS.
     * @param timeout Android CPU lock timeout in milliseconds; unused on iOS.
     * @author Dmitriy Moroz */
    override fun acquire(timeout: Long) = Unit

    /**
     * Has no CPU resource to release on iOS.
     * @author Dmitriy Moroz */
    override fun release() = Unit
}
