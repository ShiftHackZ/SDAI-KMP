package com.shifthackz.aisdv1.domain.usecase.generation

import com.shifthackz.aisdv1.domain.entity.UpscalerConfiguration
import com.shifthackz.aisdv1.domain.entity.UpscalerImageSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks portrait/landscape fitting, small inputs and live scale changes without image allocation.
 *
 * @author Dmitriy Moroz
 */
class GetUpscalerOutputSizeUseCaseImplTest {
    private val useCase = GetUpscalerOutputSizeUseCaseImpl()
    private val configuration = UpscalerConfiguration("upscaler_test")

    @Test
    fun `given output dimensions, oversized landscape uses x4, expected longest prepared edge 1024 and full aspect ratio`() {
        // Given
        val width = 2048
        val height = 1152

        // When
        val result = useCase(width, height, configuration)

        // Then
        assertEquals(UpscalerImageSize(4096, 2304), result)
    }

    @Test
    fun `given output dimensions, oversized portrait uses x2, expected aspect ratio and final factor preserved`() {
        // Given
        val selection = configuration.copy(scaleFactor = 2)

        // When
        val result = useCase(768, 1536, selection)

        // Then
        assertEquals(UpscalerImageSize(1024, 2048), result)
    }

    @Test
    fun `given output dimensions, input already fits, expected original dimensions multiplied without enlargement to 1024`() {
        // Given
        val width = 418
        val height = 257

        // When
        val result = useCase(width, height, configuration)

        // Then
        assertEquals(UpscalerImageSize(1672, 1028), result)
    }

    @Test
    fun `given output dimensions, very thin source is fitted, expected positive minor edge without overflow`() {
        // Given
        val width = Int.MAX_VALUE
        val height = 1

        // When
        val result = useCase(width, height, configuration)

        // Then
        assertEquals(UpscalerImageSize(4096, 4), result)
    }

    @Test
    fun `given output dimensions, input or model is absent, expected no predicted output`() {
        // Given
        val missingInput = 0

        // When
        val withoutInput = useCase(missingInput, 512, configuration)
        val withoutModel = useCase(512, 512, null)

        // Then
        assertNull(withoutInput)
        assertNull(withoutModel)
    }
}
