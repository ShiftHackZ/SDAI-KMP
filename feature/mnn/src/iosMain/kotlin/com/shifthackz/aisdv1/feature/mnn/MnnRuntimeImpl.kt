package com.shifthackz.aisdv1.feature.mnn

import com.shifthackz.aisdv1.domain.entity.NativeGenerationRequest
import com.shifthackz.aisdv1.domain.entity.MnnWorkOwner
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.Foundation.NSLock
import platform.Foundation.NSUUID

/**
 * Suspends through native drain so cancellation cannot release active model leases early.
 *
 * @param bridge Explicit Swift host adapter, without mutable global registration.
 *
 * @author Dmitriy Moroz
 */
@OptIn(ExperimentalEncodingApi::class)
internal class MnnRuntimeImpl(private val bridge: MnnAppleBridge) : MnnRuntime {
    private val lock = NSLock()
    private var activeOwner: String? = null

    /**
     * Waits for exactly one native completion even after caller cancellation, then fences late output.
     *
     * @param request Immutable model path and supported generation inputs.
     * @param onProgress Active caller's bounded native overall percentage callback.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun generate(
        request: NativeGenerationRequest,
        onProgress: (Int) -> Unit
    ): ByteArray {
        val context = currentCoroutineContext()
        context.ensureActive()
        val owner = context[MnnWorkOwner]?.id ?: NSUUID().UUIDString
        lock.lock()
        try {
            check(activeOwner == null) { "MNN runtime is busy" }; activeOwner = owner
        } finally {
            lock.unlock()
        }
        val cancellation = CoroutineScope(context).launch(start = CoroutineStart.UNDISPATCHED) {
            try {
                awaitCancellation()
            } finally {
                bridge.interrupt(owner)
            }
        }
        try {
            val result = withContext(NonCancellable) {
                suspendCoroutine { continuation ->
                    bridge.generate(owner, request, { percent ->
                        if (context[Job]?.isActive != false) onProgress(percent)
                    }) { continuation.resume(it) }
                }
            }
            context.ensureActive()
            if (result.isCancelled) throw CancellationException("MNN generation cancelled")
            check(result.errorMessage == null) { result.errorMessage ?: "MNN generation failed" }
            return Base64.decode(checkNotNull(result.imageBase64) { "MNN returned no image" })
        } finally {
            cancellation.cancel()
            lock.lock()
            try {
                if (activeOwner == owner) activeOwner = null
            } finally {
                lock.unlock()
            }
        }
    }

    /**
     * Requests nonblocking cancellation only for the matching live owner.
     *
     * @param ownerId Exact worker identity, or null for the active foreground operation.
     *
     * @author Dmitriy Moroz
     */
    override fun interrupt(ownerId: String?) {
        lock.lock()
        val owner = try {
            activeOwner
        } finally {
            lock.unlock()
        }
        owner?.takeIf { ownerId == null || ownerId == it }?.let(bridge::interrupt)
    }
}
