package com.shifthackz.aisdv1.domain.gateway

import com.shifthackz.aisdv1.core.common.appbuild.BuildInfoProvider
import com.shifthackz.aisdv1.core.common.platform.Platform
import com.shifthackz.aisdv1.domain.entity.MnnBackend
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class MnnBackendPolicyImplTest {
    @Test
    fun `given MNN backend admission, iOS platform is selected, expected CPU and Metal only`() {
        // Given
        val build = mockk<BuildInfoProvider> { every { platform } returns Platform.IOS }

        // When
        val actual = MnnBackendPolicyImpl(build).supportedBackends

        // Then
        assertEquals(listOf(MnnBackend.CPU, MnnBackend.METAL), actual)
    }

    @Test
    fun `given MNN backend admission, Android platform is selected, expected CPU and OpenCL only`() {
        // Given
        val build = mockk<BuildInfoProvider> { every { platform } returns Platform.ANDROID }

        // When
        val actual = MnnBackendPolicyImpl(build).supportedBackends

        // Then
        assertEquals(listOf(MnnBackend.CPU, MnnBackend.OPENCL), actual)
    }
}
