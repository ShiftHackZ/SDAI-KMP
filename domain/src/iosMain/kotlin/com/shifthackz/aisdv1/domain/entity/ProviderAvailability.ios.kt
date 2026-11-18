package com.shifthackz.aisdv1.domain.entity

import platform.UIKit.UIDevice

actual fun isServerSourceAvailableOnPlatform(source: ServerSource): Boolean = when (source) {
    ServerSource.LOCAL_APPLE_CORE_ML,
        -> isAppleLocalRuntimeAvailable()

    ServerSource.LOCAL_APPLE_BONSAI,
        -> isAppleBonsaiRuntimeAvailable()

    else -> true
}

private fun isAppleLocalRuntimeAvailable(): Boolean =
    UIDevice.currentDevice.systemVersion
        .split(".")
        .mapNotNull(String::toIntOrNull)
        .let { parts ->
            val major = parts.getOrElse(0) { 0 }
            val minor = parts.getOrElse(1) { 0 }
            major > 16 || major == 16 && minor >= 2
        }

private fun isAppleBonsaiRuntimeAvailable(): Boolean =
    UIDevice.currentDevice.systemVersion
        .split(".")
        .mapNotNull(String::toIntOrNull)
        .let { parts ->
            val major = parts.getOrElse(0) { 0 }
            major >= 17
        }
