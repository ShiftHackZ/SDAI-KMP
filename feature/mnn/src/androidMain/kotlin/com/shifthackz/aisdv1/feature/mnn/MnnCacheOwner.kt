package com.shifthackz.aisdv1.feature.mnn

/**
 * Reserves private attempt storage and reclaims abandoned MNN attempts under one process lock.
 *
 * @author Dmitriy Moroz
 */
internal interface MnnCacheOwner {
    /**
     * Fails if another attempt owns the cache or stale attempt storage cannot be removed.
     * The returned lease must remain open until all native mappings have been destroyed.
     *
     * @author Dmitriy Moroz
     */
    fun acquire(): MnnCacheLease
}
