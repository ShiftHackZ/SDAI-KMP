package com.shifthackz.aisdv1.app.inference

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.os.Process
import com.shifthackz.aisdv1.app.AiStableDiffusionClientApp
import com.shifthackz.aisdv1.domain.feature.diffusion.NativeInferenceEngine
import com.shifthackz.aisdv1.domain.entity.NativeGenerationStage
import com.shifthackz.aisdv1.domain.entity.NativeGenerationEngine
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * One private native process, with a responsive control handler and serial inference executor.
 * Fatal native signals terminate this process; the host observes Binder death.
 *
 * @author Dmitriy Moroz
 */
internal class NativeGenerationService : Service() {
    private val incarnation = UUID.randomUUID().toString()
    private val sequence = AtomicLong()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val executor =
        Executors.newSingleThreadExecutor { task -> Thread(task, "sdai-inference") }
            .asCoroutineDispatcher()
    private val control = Handler(Looper.getMainLooper(), ::onMessage)
    private val messenger = Messenger(control)
    private var host: Messenger? = null
    private var death: IBinder.DeathRecipient? = null
    private var activeJob: String? = null
    private var lastStage: NativeGenerationStage? = null
    private val isCancelled = AtomicBoolean(false)

    @Volatile
    private var engineKind: NativeGenerationEngine? = null

    @Volatile
    private var engine: NativeInferenceEngine? = null
    private lateinit var dependencies: InferenceServiceDependencies

    /**
     * Loads only the already composed inference dependencies.
     *
     * @author Dmitriy Moroz
     */
    override fun onCreate() {
        super.onCreate()
        dependencies = (application as AiStableDiffusionClientApp).inferenceDependencies
    }

    /**
     * Returns the child control Messenger; runtime work begins only after a validated job command.
     *
     * @param intent Explicit non-exported host binding.
     * @author Dmitriy Moroz
     */
    override fun onBind(intent: Intent): IBinder = messenger.binder

    private fun onMessage(message: Message): Boolean {
        if (message.sendingUid != Process.myUid() || message.data.getInt(InferenceProtocol.VERSION_KEY) != InferenceProtocol.VERSION) return true
        if (message.what == InferenceProtocol.HELLO) {
            if (host != null && host?.binder != message.replyTo?.binder) return true
            val reply = message.replyTo ?: return true
            if (host == null) {
                val observer = IBinder.DeathRecipient { Process.killProcess(Process.myPid()) }
                runCatching { reply.binder.linkToDeath(observer, 0) }.getOrElse { return true }
                host = reply
                death = observer
            }
            send(InferenceProtocol.READY)
            return true
        }
        if (message.data.getString(InferenceProtocol.INCARNATION) != incarnation || message.replyTo?.binder != host?.binder) return true
        val job = message.data.getString(InferenceProtocol.JOB).orEmpty()
        when (message.what) {
            InferenceProtocol.SUBMIT -> {
                if (activeJob != null || runCatching { UUID.fromString(job) }.isFailure) return true
                val submittedEngine = runCatching {
                    NativeGenerationEngine.valueOf(
                        message.data.getString(InferenceProtocol.ENGINE_KEY).orEmpty()
                    )
                }.getOrNull() ?: return true
                engineKind = submittedEngine
                activeJob = job
                lastStage = null
                isCancelled.set(false)
                scope.launch(executor) {
                    var outcome = InferenceProtocol.FAILURE
                    try {
                        val request = dependencies.files.readRequest(job)
                        require(request.engine == submittedEngine)
                        if (isCancelled.get()) return@launch
                        val ownedEngine = dependencies.engines.create(request)
                        engine = ownedEngine
                        if (isCancelled.get()) return@launch
                        val image = ownedEngine.generate(request, { status ->
                            if (!isCancelled.get()) send(
                                InferenceProtocol.PROGRESS,
                                job,
                                status.current,
                                status.total
                            )
                        }) { stage -> send(InferenceProtocol.STAGE, job, stage = stage) }
                        if (!isCancelled.get()) {
                            send(
                                InferenceProtocol.STAGE,
                                job,
                                stage = NativeGenerationStage.WRITING_RESULT
                            )
                            dependencies.files.writeResult(job, image)
                            outcome = InferenceProtocol.SUCCESS
                        }
                    } catch (_: Exception) {
                        // Failure messages stay in the child; the protocol carries only its category.
                    } finally {
                        try {
                            engine?.close()
                        } catch (_: Exception) {
                            outcome = InferenceProtocol.FAILURE
                        }
                        engine = null
                        withContext(Dispatchers.Main.immediate) {
                            send(if (isCancelled.get()) InferenceProtocol.FAILURE else outcome, job)
                            activeJob = null
                        }
                    }
                }
            }

            InferenceProtocol.CANCEL -> if (job == activeJob) {
                isCancelled.set(true)
                runCatching { engine?.interrupt() }
                scope.launch {
                    delay(10_000)
                    if (activeJob == job) Process.killProcess(Process.myPid())
                }
            }

            InferenceProtocol.PING -> send(InferenceProtocol.PONG)
            InferenceProtocol.EXIT -> Process.killProcess(Process.myPid())
        }
        return true
    }

    private fun send(
        what: Int,
        job: String = "",
        current: Int = 0,
        total: Int = 0,
        stage: NativeGenerationStage? = null
    ) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            control.post { send(what, job, current, total, stage) }
            return
        }
        if (what == InferenceProtocol.STAGE) {
            if (stage == lastStage) return
            lastStage = stage
        }
        val reply = host ?: return
        val message = InferenceProtocol.message(what, incarnation, job, sequence.incrementAndGet())
        message.data.putInt(InferenceProtocol.PID, Process.myPid())
        message.data.putString(InferenceProtocol.ENGINE_KEY, engineKind?.name)
        message.data.putString(InferenceProtocol.STAGE_KEY, stage?.name)
        message.data.putInt(InferenceProtocol.CURRENT, current)
        message.data.putInt(InferenceProtocol.TOTAL, total)
        runCatching { reply.send(message) }.onFailure { Process.killProcess(Process.myPid()) }
    }

    /**
     * Service disposal never closes an engine still executing; process death owns abandoned work.
     *
     * @author Dmitriy Moroz
     */
    override fun onDestroy() {
        death?.let { observer ->
            host?.binder?.let {
                runCatching {
                    it.unlinkToDeath(
                        observer,
                        0
                    )
                }
            }
        }
        scope.cancel()
        executor.close()
        super.onDestroy()
        Process.killProcess(Process.myPid())
    }
}
