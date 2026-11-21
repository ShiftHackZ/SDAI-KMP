package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform

/**
 * Combines the originating vendor and model without repeating a vendor already present in the model.
 *
 * @receiver Hardware snapshot captured before provider dispatch.
 * @return Recorded device identity, or null when the platform supplied no model.
 * @author Dmitriy Moroz
 */
internal fun BenchmarkDeviceInfo.generationDeviceName(): String? {
    val modelName = model.trim().takeIf { it.isNotEmpty() && it != "Unknown" } ?: return null
    val vendor = manufacturer.trim().takeUnless { it.isEmpty() || it == "Unknown" }
    return if (vendor == null || modelName.startsWith(
            vendor,
            ignoreCase = true
        )
    ) modelName else "$vendor $modelName"
}

/**
 * Retains the originating OS release and labels the Android API number explicitly.
 *
 * @receiver Hardware snapshot captured before provider dispatch.
 * @return OS description, or null when the platform supplied no version.
 * @author Dmitriy Moroz
 */
internal fun BenchmarkDeviceInfo.generationOsVersion(): String? {
    val version = osVersion.trim().takeIf { it.isNotEmpty() && it != "Unknown" } ?: return null
    return if (platform == BenchmarkPlatform.ANDROID) version.replace(
        Regex("\\((\\d+)\\)$"),
        "(API $1)"
    ) else version
}
