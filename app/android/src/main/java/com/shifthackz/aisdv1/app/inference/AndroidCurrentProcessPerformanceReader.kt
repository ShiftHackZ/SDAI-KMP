package com.shifthackz.aisdv1.app.inference

import android.os.Process
import android.os.Debug
import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading

/**
 * Reads the current process's proportional resident memory through the public Android API.
 * Shared pages are apportioned so summing host and worker readings avoids counting them twice.
 *
 * @author Dmitriy Moroz
 */
internal class AndroidCurrentProcessPerformanceReader : CurrentProcessPerformanceReader {
    override fun read(): ProcessPerformanceReading {
        val memory = Debug.MemoryInfo()
        Debug.getMemoryInfo(memory)
        val residentKilobytes = memory.dalvikPss.toLong() + memory.nativePss + memory.otherPss
        check(residentKilobytes > 0)
        return ProcessPerformanceReading(
            identity = "${Process.myPid()}:${Process.getStartElapsedRealtime()}",
            cpuTimeNanos = Process.getElapsedCpuTime() * 1_000_000L,
            residentBytes = residentKilobytes * 1024L,
        )
    }
}
