package com.shifthackz.aisdv1.presentation.gallery.detail

/**
 * Navigation-owned metadata cache; originals and decoded images belong to separate bounded loaders.
 *
 * @author Dmitriy Moroz
 */
interface GalleryDetailContentCache {
    /**
     * Returns retained metadata for this identity, or null on a cache miss.
     *
     * @param id Gallery identity.
     *
     * @author Dmitriy Moroz
     */
    operator fun get(id: Long): GalleryDetailContent?

    /**
     * Replaces retained metadata within the entry and text budgets.
     * An over-budget item is not retained; oldest inserted entries are evicted until the cache fits.
     *
     * @param id Gallery identity.
     * @param content Metadata to retain.
     *
     * @author Dmitriy Moroz
     */
    operator fun set(id: Long, content: GalleryDetailContent)

    /**
     * Drops the retained entry if present; a missing identity is a no-op.
     *
     * @param id Gallery identity.
     *
     * @author Dmitriy Moroz
     */
    fun remove(id: Long)

    /**
     * Drops entries outside the current navigation window; it does not load missing entries.
     *
     * @param ids Current item and immediate neighbors.
     *
     * @author Dmitriy Moroz
     */
    fun retain(ids: Set<Long>)

    /**
     * Reports whether metadata for this identity is currently retained.
     *
     * @param id Gallery identity.
     *
     * @author Dmitriy Moroz
     */
    fun containsKey(id: Long): Boolean
}
