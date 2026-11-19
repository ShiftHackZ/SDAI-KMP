@file:OptIn(ExperimentalForeignApi::class)

package com.shifthackz.aisdv1.data.performance

import com.shifthackz.aisdv1.domain.entity.CpuUsageCounters
import com.shifthackz.aisdv1.domain.entity.PerformanceReading
import com.shifthackz.aisdv1.domain.gateway.PerformanceGateway
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.get
import kotlinx.cinterop.IntVar
import kotlinx.cinterop.alloc
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.sizeOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.value
import platform.Foundation.NSProcessInfo
import platform.darwin.CPU_STATE_IDLE
import platform.darwin.CPU_STATE_MAX
import platform.darwin.HOST_CPU_LOAD_INFO
import platform.darwin.HOST_VM_INFO64
import platform.darwin.host_cpu_load_info_data_t
import platform.darwin.vm_statistics64_data_t
import platform.darwin.host_statistics
import platform.darwin.host_statistics64
import platform.darwin.mach_host_self
import platform.darwin.mach_port_deallocate
import platform.posix.getpagesize
import platform.darwin.KERN_SUCCESS
import platform.darwin.MACH_TASK_BASIC_INFO
import platform.darwin.mach_task_basic_info_data_t
import platform.darwin.mach_task_self_
import platform.darwin.task_info
import platform.posix.CLOCK_MONOTONIC
import platform.posix.RUSAGE_SELF
import platform.posix.clock_gettime
import platform.posix.getrusage
import platform.posix.rusage
import platform.posix.timespec

/**
 * Reads iOS resident app memory and cumulative CPU time using public Mach/POSIX APIs.
 * Counters cover the in-process native inference engines as well as the UI runtime.
 * @author Dmitriy Moroz
 */
internal class IosPerformanceGateway : PerformanceGateway {
    override suspend fun read(): PerformanceReading = memScoped {
        val usage = alloc<rusage>()
        check(getrusage(RUSAGE_SELF, usage.ptr) == 0)
        val memory = alloc<mach_task_basic_info_data_t>()
        val count = alloc<IntVar> {
            value = (sizeOf<mach_task_basic_info_data_t>() / sizeOf<IntVar>()).toInt()
        }
        check(
            task_info(
                mach_task_self_,
                MACH_TASK_BASIC_INFO.toUInt(),
                memory.ptr.reinterpret(),
                count.ptr.reinterpret()
            ) == KERN_SUCCESS
        )
        val clock = alloc<timespec>()
        check(clock_gettime(CLOCK_MONOTONIC.toUInt(), clock.ptr) == 0)
        val cpuNanos = (usage.ru_utime.tv_sec + usage.ru_stime.tv_sec) * 1_000_000_000L +
                (usage.ru_utime.tv_usec + usage.ru_stime.tv_usec) * 1_000L
        val host = mach_host_self()
        val deviceCpu = try {
            runCatching {
                val cpu = alloc<host_cpu_load_info_data_t>()
                count.value = (sizeOf<host_cpu_load_info_data_t>() / sizeOf<IntVar>()).toInt()
                check(
                    host_statistics(
                        host,
                        HOST_CPU_LOAD_INFO,
                        cpu.ptr.reinterpret(),
                        count.ptr.reinterpret()
                    ) == KERN_SUCCESS
                )
                val total = (0 until CPU_STATE_MAX).sumOf { cpu.cpu_ticks[it].toLong() }
                CpuUsageCounters(total - cpu.cpu_ticks[CPU_STATE_IDLE].toLong(), total)
            }
        } finally {
            mach_port_deallocate(mach_task_self_, host)
        }
        val memoryHost = mach_host_self()
        val deviceUsedMemory = try {
            val vm = alloc<vm_statistics64_data_t>()
            count.value = (sizeOf<vm_statistics64_data_t>() / sizeOf<IntVar>()).toInt()
            check(
                host_statistics64(
                    memoryHost,
                    HOST_VM_INFO64,
                    vm.ptr.reinterpret(),
                    count.ptr.reinterpret()
                ) == KERN_SUCCESS
            )
            (NSProcessInfo.processInfo.physicalMemory.toLong() - vm.free_count.toLong() * getpagesize()).coerceAtLeast(
                0L
            )
        } finally {
            mach_port_deallocate(mach_task_self_, memoryHost)
        }
        PerformanceReading(
            elapsedNanos = clock.tv_sec * 1_000_000_000L + clock.tv_nsec,
            cpuTimeNanos = mapOf("app" to cpuNanos),
            processorCount = NSProcessInfo.processInfo.processorCount.toInt(),
            residentBytes = memory.resident_size.toLong(),
            totalMemoryBytes = NSProcessInfo.processInfo.physicalMemory.toLong(),
            deviceCpu = deviceCpu,
            deviceUsedMemoryBytes = deviceUsedMemory,
        )
    }
}
