package com.shifthackz.aisdv1.domain.entity

import android.os.Build

actual fun isServerSourceAvailableOnPlatform(source: ServerSource): Boolean =
    isServerSourceAvailableOnAndroid(source, Build.SUPPORTED_64_BIT_ABIS)

/**
 * Applies the arm64 requirement shared by the MNN and Bonsai native distributions.
 *
 * @param source Candidate provider.
 * @param supported64BitAbis Platform-reported ABIs; missing entries cannot satisfy a native requirement.
 *
 * @author Dmitriy Moroz
 */
fun isServerSourceAvailableOnAndroid(
    source: ServerSource,
    supported64BitAbis: Array<String>?,
): Boolean = when (source) {
    ServerSource.LOCAL_MNN,
    ServerSource.LOCAL_APPLE_BONSAI -> isAndroidBonsaiSupportedInPrinciple(supported64BitAbis)

    else -> true
}

/**
 * Checks the ABI prerequisite without promising device or model compatibility.
 *
 * @param supported64BitAbis Platform-reported 64-bit ABIs.
 *
 * @author Dmitriy Moroz
 */
fun isAndroidBonsaiSupportedInPrinciple(supported64BitAbis: Array<String>?): Boolean =
    supported64BitAbis.orEmpty().any { abi -> abi == ANDROID_BONSAI_ABI }

private const val ANDROID_BONSAI_ABI = "arm64-v8a"
