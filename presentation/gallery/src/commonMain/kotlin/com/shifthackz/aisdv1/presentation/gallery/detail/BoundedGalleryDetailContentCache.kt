package com.shifthackz.aisdv1.presentation.gallery.detail

import com.shifthackz.aisdv1.presentation.components.widget.image.GalleryMemoryBudget

/**
 * Keeps navigation metadata within the injected entry and retained-text budgets.
 * Methods are called only on the view-model's immediate dispatcher.
 *
 * @param budget Injected metadata entry/text limits.
 *
 * @author Dmitriy Moroz
 */
internal class BoundedGalleryDetailContentCache(private val budget: GalleryMemoryBudget) :
    GalleryDetailContentCache {
    private val entries = linkedMapOf<Long, GalleryDetailContent>()
    private fun bytes(content: GalleryDetailContent): Long = 2L * (
            content.prompt.length + content.negativePrompt.length + content.imageBase64.length +
                    (content.inputImageBase64?.length ?: 0) + 1024)

    /**
     * Returns retained metadata for this identity, or null on a cache miss.
     *
     * @param id Gallery identity.
     *
     * @author Dmitriy Moroz
     */
    override fun get(id: Long) = entries[id]

    /**
     * Replaces retained metadata within the entry and text budgets.
     * An over-budget item is not retained; oldest inserted entries are evicted until the cache fits.
     *
     * @param id Gallery identity.
     * @param content Bounded metadata candidate.
     *
     * @author Dmitriy Moroz
     */
    override fun set(id: Long, content: GalleryDetailContent) {
        entries.remove(id)
        if (bytes(content) > budget.detailMetadataBytes) return
        entries[id] = content
        while (entries.size > budget.detailEntries || entries.values.sumOf(::bytes) > budget.detailMetadataBytes) entries.remove(
            entries.keys.first()
        )
    }

    /**
     * Drops the retained entry if present; a missing identity is a no-op.
     *
     * @param id Gallery identity.
     *
     * @author Dmitriy Moroz
     */
    override fun remove(id: Long) {
        entries.remove(id)
    }

    /**
     * Drops entries outside the current navigation window; it does not load missing entries.
     *
     * @param ids Current navigation window.
     *
     * @author Dmitriy Moroz
     */
    override fun retain(ids: Set<Long>) {
        entries.keys.retainAll(ids)
    }

    /**
     * Reports whether metadata for this identity is currently retained.
     *
     * @param id Gallery identity.
     *
     * @author Dmitriy Moroz
     */
    override fun containsKey(id: Long) = entries.containsKey(id)
}
