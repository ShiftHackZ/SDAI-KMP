package com.shifthackz.aisdv1.data.performance

import android.app.ActivityManager
import android.content.Context
import android.os.Process
import android.os.SystemClock
import android.system.Os
import android.system.OsConstants
import com.shifthackz.aisdv1.domain.entity.CpuUsageCounters
import com.shifthackz.aisdv1.domain.entity.PerformanceReading
import com.shifthackz.aisdv1.domain.gateway.NativeGenerationGateway
import com.shifthackz.aisdv1.domain.gateway.PerformanceGateway
import java.io.File

/**
 * Combines host-owned counters with on-demand child IPC readings. Android blocks sibling /proc
 * reads even for the app's inference process; those reads must never stand in for worker telemetry.
 * RSS may count shared pages in both processes.
 * @param context Application context used to read device memory capacity and availability.
 * @param nativeGateway Current inference connection, queried without starting a worker.
 * @author Dmitriy Moroz
 */
internal class AndroidPerformanceGateway(
    context: Context,
    private val nativeGateway: NativeGenerationGateway,
) : PerformanceGateway {
    private val activityManager =
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    private val pageBytes = Os.sysconf(OsConstants._SC_PAGESIZE)

    override suspend fun read(): PerformanceReading {
        check(pageBytes > 0)
        val worker = nativeGateway.readProcessPerformance()
        val fields =
            File("/proc/self/stat").readText().substringAfterLast(") ").trim().split(Regex("\\s+"))
        val memory = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memory)
        return PerformanceReading(
            elapsedNanos = SystemClock.elapsedRealtimeNanos(),
            cpuTimeNanos = buildMap {
                put("host", Process.getElapsedCpuTime() * 1_000_000L)
                worker?.let { put(it.identity, it.cpuTimeNanos) }
            },
            processorCount = Runtime.getRuntime().availableProcessors(),
            residentBytes = fields[21].toLong() * pageBytes + (worker?.residentBytes ?: 0L),
            totalMemoryBytes = memory.totalMem,
            deviceUsedMemoryBytes = (memory.totalMem - memory.availMem).coerceIn(
                0,
                memory.totalMem
            ),
            deviceCpu = runCatching {
                val ticks = File("/proc/stat").useLines { lines ->
                    lines.first().trim().split(Regex("\\s+")).drop(1).take(8).map(String::toLong)
                }
                require(ticks.size >= 4)
                val total = ticks.sum()
                CpuUsageCounters(total - ticks[3] - ticks.getOrElse(4) { 0L }, total)
            },
        )
    }
}
