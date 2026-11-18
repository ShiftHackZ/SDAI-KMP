package com.shifthackz.aisdv1.presentation.configuration.setup.platform

import com.shifthackz.aisdv1.domain.entity.isAndroidBonsaiSupportedInPrinciple
import com.shifthackz.aisdv1.domain.entity.isServerSourceAvailableOnAndroid

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.appbuild.BuildType
import com.shifthackz.aisdv1.core.common.appbuild.BuildVersion
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.entity.ServerSourceReadiness
import com.shifthackz.aisdv1.presentation.components.model.displayName
import com.shifthackz.aisdv1.presentation.components.model.readinessFor
import com.shifthackz.aisdv1.presentation.configuration.setup.mappers.allowedModes
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test


class ServerSetupPlatformTest {

    @Test
    fun `given server setup platform, Android build, expected Core ML excluded by allowed platforms`() {
        // Given
        // The Android build fixture supplies the platform and build flavor.
        // When
        val allowedModes = TestAndroidBuildInfoProvider.allowedModes
        // Then
        assertFalse(ServerSource.LOCAL_APPLE_CORE_ML in allowedModes)
    }

    @Test
    fun `given server setup platform, Android device has arm64 ABI, expected Bonsai provider visible`() {
        // Given
        val supportedAbis = arrayOf("armeabi-v7a", "arm64-v8a")
        // When
        val isSupported = isAndroidBonsaiSupportedInPrinciple(supportedAbis)
        // Then
        assertTrue(isSupported)
    }

    @Test
    fun `given server setup platform, Android device lacks arm64 ABI, expected Bonsai provider hidden`() {
        // Given
        val supportedAbis = arrayOf("x86_64")
        // When
        val isSupported = isAndroidBonsaiSupportedInPrinciple(supportedAbis)
        // Then
        assertFalse(isSupported)
    }

    @Test
    fun `given server setup platform, platform reports arm64 ABI, expected Bonsai provider visible in setup list`() {
        // Given
        val supported64BitAbis = arrayOf("arm64-v8a")
        // When
        val isAvailable = isServerSourceAvailableOnAndroid(
            source = ServerSource.LOCAL_APPLE_BONSAI,
            supported64BitAbis = supported64BitAbis,
        )
        // Then
        assertTrue(isAvailable)
    }

    @Test
    fun `given server setup platform, Bonsai provider, expected platform specific display name`() {
        // Given
        val source = ServerSource.LOCAL_APPLE_BONSAI
        // When
        val androidName = source.displayName(Platform.ANDROID)
        // Then
        assertEquals("Local Diffusion PrismML Bonsai", androidName)
        // When
        val iosName = source.displayName(Platform.IOS)
        // Then
        assertEquals("Silicon Diffusion PrismML Bonsai", iosName)
    }

    @Test
    fun `given server setup platform, Bonsai provider, expected platform specific readiness`() {
        // Given
        val source = ServerSource.LOCAL_APPLE_BONSAI
        // When
        val androidReadiness = source.readinessFor(Platform.ANDROID)
        // Then
        assertEquals(ServerSourceReadiness.EXPERIMENTAL, androidReadiness)
        // When
        val iosReadiness = source.readinessFor(Platform.IOS)
        // Then
        assertEquals(ServerSourceReadiness.BETA, iosReadiness)
    }
}

private object TestAndroidBuildInfoProvider : BuildInfoProvider {
    override val isDebug: Boolean = true
    override val buildNumber: Int = 0
    override val version: BuildVersion = BuildVersion()
    override val type: BuildType = BuildType.FULL
    override val platform: Platform = Platform.ANDROID
}
