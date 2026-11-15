package com.shifthackz.aisdv1.presentation.widget.image

import androidx.compose.ui.graphics.ImageBitmap
import com.shifthackz.aisdv1.core.common.schedulers.DispatchersProvider
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticContext
import com.shifthackz.aisdv1.core.mvi.diagnostics.UiDiagnosticOperation
import com.shifthackz.aisdv1.domain.gateway.GalleryReadGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext

/**
 * Shared LRU constrained by injected entry, decoded-byte and concurrent-decode limits.
 * Eviction releases references; a bitmap still displayed by Compose is never recycled.
 *
 * @param source Revision-checked SQLite reads.
 * @param decoder Platform sampling decoder.
 * @param budget Explicit entry/byte/concurrency/source limits.
 * @param dispatchers Background decode dispatcher.
 * @author Dmitriy Moroz
 */
internal class BoundedGalleryImageLoader(
    private val source: GalleryReadGateway,
    private val decoder: GalleryImageDecoder,
    private val budget: GalleryMemoryBudget,
    private val dispatchers: DispatchersProvider,
) : GalleryImageLoader {
    private val cache = linkedMapOf<GalleryImageCacheKey, ImageBitmap>()
    private val lock = Mutex()
    private val permits = Semaphore(budget.concurrentDecodes)
    private var bytes = 0L

    /**
     * Returns a cached or bounded sampled display image for the requested content revision.
     * Cache hits do not recheck storage; callers must pass the current revision. On a cache miss,
     * missing, replaced, over-limit or undecodable source content returns null. Cancellation propagates.
     *
     * @param id Persisted identity.
     * @param revision Content identity.
     * @param isInput Original-input selection.
     * @param maxDimension Display edge, bucketed to avoid layout jitter invalidation.
     * @param diagnosticContext Optional fixed-label decode correlation, not a cache key.
     * @author Dmitriy Moroz
     */
    override suspend fun load(
        id: Long,
        revision: String,
        isInput: Boolean,
        maxDimension: Int,
        diagnosticContext: UiDiagnosticContext?
    ): ImageBitmap? {
        val edge = bucket(maxDimension)
        return loadKey(
            GalleryImageCacheKey(id, revision, isInput, edge),
            diagnosticContext
        ) { source.image(id, revision, isInput, budget.encodedChars) }
    }

    /**
     * Samples an unsaved result under the same decode and cache budgets.
     * The result owner retains its original; this cache holds only display images.
     *
     * @param revision Stable unsaved result identity.
     * @param base64 One original retained by the result owner.
     * @param isInput Input/output distinction.
     * @param maxDimension Measured display edge.
     * @param diagnosticContext Optional fixed-label decode correlation, not a cache key.
     * @author Dmitriy Moroz
     */
    override suspend fun loadTransient(
        revision: String,
        base64: String,
        isInput: Boolean,
        maxDimension: Int,
        diagnosticContext: UiDiagnosticContext?
    ): ImageBitmap? =
        loadKey(
            GalleryImageCacheKey(Long.MIN_VALUE, revision, isInput, bucket(maxDimension)),
            diagnosticContext
        ) { base64.takeIf { it.length <= budget.encodedChars } }

    private fun bucket(edge: Int) = (((edge.coerceIn(
        64,
        budget.maxDisplayEdge
    ) + 63) / 64) * 64).coerceAtMost(budget.maxDisplayEdge)

    private suspend fun loadKey(
        key: GalleryImageCacheKey,
        diagnosticContext: UiDiagnosticContext?,
        read: suspend () -> String?
    ): ImageBitmap? {
        fun size(image: ImageBitmap) = image.width.toLong() * image.height * 4
        suspend fun cached() = lock.withLock { cache.remove(key)?.also { cache[key] = it } }
        cached()?.let { return it }
        return permits.withPermit {
            cached()?.let { return@withPermit it }
            val decoded = withContext(dispatchers.io) {
                val encoded = read() ?: return@withContext null
                currentCoroutineContext().ensureActive()
                val span = diagnosticContext?.let {
                    it.diagnostics.beginTrace(
                        it.owner,
                        it.subtree,
                        UiDiagnosticOperation.IMAGE_DECODE
                    )
                }
                try {
                    decoder.decode(encoded, key.edge)
                } finally {
                    diagnosticContext?.diagnostics?.finishTrace(span)
                }
            } ?: return@withPermit null
            currentCoroutineContext().ensureActive()
            lock.withLock {
                cache.remove(key)?.let { bytes -= size(it) }
                cache[key] = decoded
                bytes += size(decoded)
                while (cache.size > budget.cacheEntries || bytes > budget.cacheBytes) {
                    val oldest = cache.entries.first()
                    bytes -= size(oldest.value)
                    cache.remove(oldest.key)
                }
            }
            decoded
        }
    }
}
