package com.shifthackz.aisdv1.app.inference

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import android.os.SystemClock
import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import com.shifthackz.aisdv1.domain.entity.LocalDiffusionStatus
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationFailure
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Validates every child callback against its binding, random incarnation, job and event sequence.
 *
 * @param context Host context used for a private explicit service binding.
 * @author Dmitriy Moroz
 */
internal class AndroidInferenceConnection(private val context: Context) : InferenceConnection {
    override val processId: Int? get() = pid.takeIf { it > 0 }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val ready = CompletableDeferred<Unit>()
    private val identityReady = CompletableDeferred<Unit>()
    private val result = CompletableDeferred<Unit>()
    private val performanceMutex = Mutex()
    private var resourceRequestId = 0L
    private var pendingPerformance: CompletableDeferred<ProcessPerformanceReading>? = null
    private val dead = CompletableDeferred<Unit>()
    private val reply = Messenger(Handler(Looper.getMainLooper(), ::onMessage))
    private var remote: Messenger? = null
    private var binder: IBinder? = null
    private var incarnation = ""

    @Volatile
    private var pid = -1
    private var sequence = -1L
    private var job = ""
    private var isBound = false
    private var hasConnected = false
    private var hasUnexpectedBinding = false
    private var isCancellationRequested = false
    private var hasSubmitted = false
    private var lastHeartbeat = SystemClock.elapsedRealtime()
    private var heartbeat: Job? = null
    private var expectedEngine: NativeGenerationEngine? = null
    private var onStage: (NativeGenerationStage) -> Unit = {}
    private var onProgress: (LocalDiffusionStatus) -> Unit = {}
    private val death = IBinder.DeathRecipient { scope.launch { processDied() } }
    private val binding = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            if (!isBound || hasConnected || dead.isCompleted) {
                hasUnexpectedBinding = true
                if (isBound) runCatching { context.unbindService(this) }
                isBound = false
                return
            }
            // An initial late handshake is still needed to identify and reap a cancelled child.
            hasConnected = true
            binder = service
            try {
                service.linkToDeath(death, 0)
                remote = Messenger(service)
                send(InferenceProtocol.HELLO)
            } catch (_: Exception) {
                processDied()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            processDied()
        }

        override fun onBindingDied(name: ComponentName) {
            processDied()
        }

        override fun onNullBinding(name: ComponentName) {
            processDied()
        }
    }

    /**
     * Binds and validates one child incarnation, persists its identity, then awaits this job completion.
     * Delivers only matching job events; binding, protocol or child-exit failures propagate to the host.
     *
     * @param job Committed request identity.
     * @param engine Expected runtime identifier.
     * @param onStage Sanitized stage consumer.
     * @param onConnected Durable PID/incarnation publication before submission.
     * @param onProgress Progress consumer for this job.
     * @author Dmitriy Moroz
     */
    override suspend fun run(
        job: String,
        engine: NativeGenerationEngine,
        onProgress: (LocalDiffusionStatus) -> Unit,
        onStage: (NativeGenerationStage) -> Unit,
        onConnected: suspend (Int, String) -> Unit
    ) {
        this.job = job
        this.onProgress = onProgress
        this.expectedEngine = engine
        this.onStage = onStage
        try {
            withTimeout(30_000) {
                withContext(Dispatchers.Main.immediate) {
                    isBound = context.bindService(
                        Intent(context, NativeGenerationService::class.java),
                        binding,
                        Context.BIND_AUTO_CREATE
                    )
                    if (!isBound) fail(NativeGenerationFailure.Category.CONNECTION)
                }
                ready.await()
            }
        } catch (_: TimeoutCancellationException) {
            throw NativeGenerationFailure(NativeGenerationFailure.Category.CONNECTION)
        } catch (failure: CancellationException) {
            throw failure
        } catch (failure: NativeGenerationFailure) {
            throw failure
        } catch (_: Exception) {
            throw NativeGenerationFailure(NativeGenerationFailure.Category.CONNECTION)
        }
        onConnected(pid, incarnation)
        withContext(Dispatchers.Main.immediate) {
            if (isCancellationRequested) throw CancellationException("Local generation cancelled")
            hasSubmitted = true
            send(InferenceProtocol.SUBMIT)
            heartbeat = scope.launch {
                while (true) {
                    delay(5_000)
                    if (SystemClock.elapsedRealtime() - lastHeartbeat >= 60_000) {
                        fail(NativeGenerationFailure.Category.UNRESPONSIVE)
                        break
                    }
                    send(InferenceProtocol.PING)
                }
            }
        }
        result.await()
    }

    /**
     * Serializes on-demand resource requests on the control dispatcher. A short read deadline affects
     * only the overlay; it never interrupts native generation or its heartbeat.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun readPerformance(): ProcessPerformanceReading? = performanceMutex.withLock {
        withContext(Dispatchers.Main.immediate) {
            if (!hasSubmitted || result.isCompleted) return@withContext null
            val response = CompletableDeferred<ProcessPerformanceReading>()
            pendingPerformance = response
            resourceRequestId++
            try {
                send(InferenceProtocol.READ_PERFORMANCE)
                withTimeoutOrNull(2_000) { response.await() }
                    ?: error("Inference resource reading timed out")
            } finally {
                pendingPerformance = null
            }
        }
    }

    private fun onMessage(message: Message): Boolean {
        val data = message.data
        if (message.sendingUid != Process.myUid() || data.getInt(InferenceProtocol.VERSION_KEY) != InferenceProtocol.VERSION) return true
        val token = data.getString(InferenceProtocol.INCARNATION).orEmpty()
        if (message.what == InferenceProtocol.READY && incarnation.isEmpty()) {
            if (token.length != 36 || data.getInt(InferenceProtocol.PID) == Process.myPid()) return true
            incarnation = token
            pid = data.getInt(InferenceProtocol.PID)
            sequence = data.getLong(InferenceProtocol.SEQUENCE)
            lastHeartbeat = SystemClock.elapsedRealtime()
            identityReady.complete(Unit)
            ready.complete(Unit)
            return true
        }
        val next = data.getLong(InferenceProtocol.SEQUENCE)
        if (token != incarnation || next <= sequence) return true
        sequence = next
        if (message.what == InferenceProtocol.PONG) {
            lastHeartbeat = SystemClock.elapsedRealtime(); return true
        }
        if (data.getString(InferenceProtocol.JOB) != job || data.getString(InferenceProtocol.ENGINE_KEY) != expectedEngine?.name) return true
        when (message.what) {
            InferenceProtocol.PERFORMANCE -> {
                if (data.getLong(InferenceProtocol.RESOURCE_REQUEST_ID) != resourceRequestId) return true
                val cpu = data.getLong(InferenceProtocol.CPU_NANOS, -1)
                val rss = data.getLong(InferenceProtocol.RESIDENT_BYTES, -1)
                if (cpu >= 0 && rss >= 0) {
                    pendingPerformance?.complete(
                        ProcessPerformanceReading(
                            "$pid:$incarnation",
                            cpu,
                            rss
                        )
                    )
                } else {
                    pendingPerformance?.completeExceptionally(IllegalStateException("Inference resource reading failed"))
                }
            }

            InferenceProtocol.STAGE -> if (!result.isCompleted) {
                runCatching {
                    NativeGenerationStage.valueOf(
                        data.getString(InferenceProtocol.STAGE_KEY).orEmpty()
                    )
                }.getOrNull()?.let(onStage)
            }

            InferenceProtocol.PROGRESS -> if (!result.isCompleted && !isCancellationRequested) {
                val total = data.getInt(InferenceProtocol.TOTAL)
                val current = data.getInt(InferenceProtocol.CURRENT)
                if (total in 0..1000 && current in 0..total) onProgress(
                    LocalDiffusionStatus(
                        current,
                        total
                    )
                )
            }

            InferenceProtocol.SUCCESS -> if (!isCancellationRequested) result.complete(Unit) else result.cancel()
            InferenceProtocol.FAILURE -> if (isCancellationRequested) result.cancel() else fail(
                NativeGenerationFailure.Category.ENGINE
            )
        }
        return true
    }

    private fun send(what: Int) {
        try {
            remote?.send(InferenceProtocol.message(what, incarnation, job, replyTo = reply).apply {
                if (what == InferenceProtocol.READ_PERFORMANCE) data.putLong(
                    InferenceProtocol.RESOURCE_REQUEST_ID,
                    resourceRequestId
                )
                if (what == InferenceProtocol.SUBMIT) data.putString(
                    InferenceProtocol.ENGINE_KEY,
                    expectedEngine?.name
                )
            })
        } catch (_: Exception) {
            processDied()
        }
    }

    private fun processDied() {
        pendingPerformance?.completeExceptionally(IllegalStateException("Inference process exited"))
        dead.complete(Unit)
        fail(NativeGenerationFailure.Category.PROCESS_DIED)
    }

    private fun fail(category: NativeGenerationFailure.Category) {
        val failure = NativeGenerationFailure(category)
        ready.completeExceptionally(failure)
        result.completeExceptionally(failure)
    }

    /**
     * Waits for native cleanup; only the owned process may be escalated after the grace period.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun cancel() {
        withContext(Dispatchers.Main.immediate) {
            isCancellationRequested = true
            if (hasSubmitted && incarnation.isNotEmpty()) send(InferenceProtocol.CANCEL)
            else {
                ready.cancel(); result.cancel()
            }
        }
        if (withTimeoutOrNull(10_000) { result.join(); true } != true) terminateOwned()
    }

    private suspend fun terminateOwned() = withContext(Dispatchers.Main.immediate) {
        send(InferenceProtocol.EXIT)
        if (withTimeoutOrNull(2_000) { dead.await(); true } == true) return@withContext
        val manager = context.getSystemService(ActivityManager::class.java)
        val owned = manager.runningAppProcesses?.any {
            it.pid == pid && it.uid == Process.myUid() && it.processName == "${context.packageName}:inference"
        } == true
        if (owned && pid > 0 && pid != Process.myPid() && binder?.isBinderAlive == true && incarnation.isNotEmpty()) Process.killProcess(
            pid
        )
    }

    /**
     * Releases the binding only after requesting validated process teardown.
     *
     * @author Dmitriy Moroz
     */
    override suspend fun close(): Boolean {
        heartbeat?.cancel()
        if (isBound && incarnation.isEmpty() && !dead.isCompleted) withTimeoutOrNull(5_000) { identityReady.await() }
        if (binder != null && !dead.isCompleted) terminateOwned()
        val hasDied =
            (!isBound && binder == null) || withTimeoutOrNull(5_000) { dead.await(); true } == true
        withContext(Dispatchers.Main.immediate) {
            binder?.let { runCatching { it.unlinkToDeath(death, 0) } }
            if (isBound) runCatching { context.unbindService(binding) }
            isBound = false
            scope.cancel()
        }
        return hasDied && !hasUnexpectedBinding
    }
}
