package com.shifthackz.aisdv1.domain.entity.benchmark


/**
 * Static hardware snapshot collected without initializing inference runtimes.
 * Unknown GPU-specific memory values remain null rather than being equated to system RAM.
 *
 * @param platform Native platform whose runtime restrictions apply.
 * @param manufacturer Device vendor reported by the platform.
 * @param model Device/model identity used by conservative hardware-specific policy.
 * @param osVersion Operating-system version reported with the measurement.
 * @param cpuName CPU/chip description supplied by the platform.
 * @param cpuCores Processor count visible to the application.
 * @param gpuName GPU description detected without starting an inference runtime.
 * @param totalRamMb Total physical/system memory in megabytes.
 * @param availableRamMb Available system memory at capture time in megabytes.
 * @param totalVramMb Dedicated GPU memory capacity when the platform exposes it.
 * @param availableVramMb Available dedicated GPU memory when exposed.
 * @param accelerators Acceleration APIs detected by the platform probe.
 * @param acceleratorDiagnostics Probe notes explaining unavailable or uncertain capability information.
 *
 * @author Dmitriy Moroz
 */
data class BenchmarkDeviceInfo(
    val platform: BenchmarkPlatform,
    val manufacturer: String,
    val model: String,
    val osVersion: String,
    val cpuName: String,
    val cpuCores: Int,
    val gpuName: String,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val totalVramMb: Long?,
    val availableVramMb: Long?,
    val accelerators: List<BenchmarkAccelerator>,
    val acceleratorDiagnostics: List<String> = emptyList(),
)
