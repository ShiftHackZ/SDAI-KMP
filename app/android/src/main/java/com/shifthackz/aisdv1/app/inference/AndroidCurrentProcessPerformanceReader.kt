package com.shifthackz.aisdv1.app.inference

import android.os.Process
import android.system.Os
import android.system.OsConstants
import com.shifthackz.aisdv1.domain.entity.ProcessPerformanceReading
import java.io.File

/** Reads only /proc/self, which remains accessible when sibling-process counters are blocked.
 * @author Dmitriy Moroz */
internal class AndroidCurrentProcessPerformanceReader : CurrentProcessPerformanceReader {
    override fun read(): ProcessPerformanceReading {
        val fields =
            File("/proc/self/stat").readText().substringAfterLast(") ").trim().split(Regex("\\s+"))
        val pageBytes = Os.sysconf(OsConstants._SC_PAGESIZE)
        check(pageBytes > 0)
        return ProcessPerformanceReading(
            identity = "${Process.myPid()}:${fields[19]}",
            cpuTimeNanos = Process.getElapsedCpuTime() * 1_000_000L,
            residentBytes = fields[21].toLong() * pageBytes,
        )
    }
}
