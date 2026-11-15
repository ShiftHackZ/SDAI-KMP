package com.shifthackz.aisdv1.app.diagnostics

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import com.shifthackz.aisdv1.domain.entity.GenerationFailureContext
import com.shifthackz.aisdv1.domain.gateway.ProcessExitDiagnosticsGateway
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Reads only this app's inference process; raw tombstones/messages are never automatically retained.
 *
 * @param context Host application context.
 * @author Dmitriy Moroz
 */
internal class AndroidProcessExitDiagnosticsGateway(private val context: Context) :
    ProcessExitDiagnosticsGateway {
    /**
     * Returns sanitized inference-process exits matched by PID and attempt start time.
     * Unsupported APIs, an absent handshake or unavailable system history produce an empty list.
     *
     * @param after Attempt start timestamp; older process incarnations are excluded.
     * @param pid PID received from the validated child handshake.
     * @author Dmitriy Moroz
     */
    override suspend fun readInferenceExits(
        after: Long,
        pid: Int?
    ): List<GenerationFailureContext> = withContext(Dispatchers.IO) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R || pid == null) return@withContext emptyList()
        runCatching {
            context.getSystemService(ActivityManager::class.java)
                .getHistoricalProcessExitReasons(context.packageName, pid, 20)
                .filter { it.processName == "${context.packageName}:inference" && it.timestamp >= after }
                .map { exit ->
                    val hasTrace = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
                            runCatching {
                                exit.traceInputStream?.use { true } ?: false
                            }.getOrDefault(false)
                    GenerationFailureContext(exit.timestamp, exit.reason, exit.status, hasTrace)
                }
        }.getOrDefault(emptyList())
    }
}
