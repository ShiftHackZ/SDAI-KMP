package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkDeviceInfo
import com.shifthackz.aisdv1.domain.entity.benchmark.BenchmarkPlatform
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GenerationDeviceMetadataTest {
    private val device = BenchmarkDeviceInfo(
        platform = BenchmarkPlatform.ANDROID,
        manufacturer = "Google",
        model = "Pixel 7 Pro",
        osVersion = "Android 17 (37)",
        cpuName = "Tensor",
        cpuCores = 8,
        gpuName = "Mali",
        totalRamMb = 8192,
        availableRamMb = 4096,
        totalVramMb = null,
        availableVramMb = null,
        accelerators = emptyList(),
    )

    @Test
    fun `given originating Android device, metadata is formatted, expected vendor model and explicit API level`() {
        // Given
        val snapshot = device
        // When
        val name = snapshot.generationDeviceName()
        val os = snapshot.generationOsVersion()
        // Then
        assertEquals("Google Pixel 7 Pro", name)
        assertEquals("Android 17 (API 37)", os)
    }

    @Test
    fun `given device model includes vendor, metadata is formatted, expected vendor is not repeated`() {
        // Given
        val snapshot = device.copy(model = "Google Pixel 7 Pro")
        // When
        val name = snapshot.generationDeviceName()
        // Then
        assertEquals("Google Pixel 7 Pro", name)
    }

    @Test
    fun `given unavailable device fields, snapshot is formatted, expected unknown values remain absent`() {
        // Given
        val snapshot = device.copy(model = "  ", osVersion = "Unknown")
        // When
        val name = snapshot.generationDeviceName()
        val os = snapshot.generationOsVersion()
        // Then
        assertNull(name)
        assertNull(os)
    }

    @Test
    fun `given originating iOS device, metadata is formatted, expected model name and unchanged OS release`() {
        // Given
        val snapshot = device.copy(
            platform = BenchmarkPlatform.IOS,
            manufacturer = "Apple",
            model = "iPhone 18 Pro Max",
            osVersion = "iOS 18.4"
        )
        // When
        val name = snapshot.generationDeviceName()
        val os = snapshot.generationOsVersion()
        // Then
        assertEquals("Apple iPhone 18 Pro Max", name)
        assertEquals("iOS 18.4", os)
    }

    @Test
    fun `given model is known but manufacturer is absent, snapshot is formatted, expected model remains usable without an invented vendor`() {
        // Given
        val snapshots =
            listOf(device.copy(manufacturer = " "), device.copy(manufacturer = "Unknown"))
        // When
        val names = snapshots.map { it.generationDeviceName() }
        // Then
        assertEquals(listOf("Pixel 7 Pro", "Pixel 7 Pro"), names)
    }

    @Test
    fun `given model is unknown and OS is whitespace, snapshot is formatted, expected both fields remain absent`() {
        // Given
        val snapshot = device.copy(model = "Unknown", osVersion = " 	 ")
        // When
        val name = snapshot.generationDeviceName()
        val os = snapshot.generationOsVersion()
        // Then
        assertNull(name)
        assertNull(os)
    }

    @Test
    fun `given Android OS already labels its API, snapshot is formatted, expected API label is not duplicated`() {
        // Given
        val snapshot = device.copy(osVersion = "Android 17 (API 37)")
        // When
        val os = snapshot.generationOsVersion()
        // Then
        assertEquals("Android 17 (API 37)", os)
    }

}
