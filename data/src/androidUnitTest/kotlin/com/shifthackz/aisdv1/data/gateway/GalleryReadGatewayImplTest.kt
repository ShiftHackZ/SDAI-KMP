package com.shifthackz.aisdv1.data.gateway

import com.shifthackz.aisdv1.storage.db.persistent.dao.GenerationResultDao
import com.shifthackz.aisdv1.storage.gateway.GalleryImagePayloadReader
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Test

/**
 * Checks UTF8 projection conversion without changing gallery identity, budgets or storage failures.
 *
 * @author Dmitriy Moroz
 */
class GalleryReadGatewayImplTest {
    private val dao = mockk<GenerationResultDao>()
    private val payloads = mockk<GalleryImagePayloadReader>()
    private val gateway = GalleryReadGatewayImpl(dao, payloads)

    @Test
    fun `given gallery image read, bounded UTF8 bytes are returned, expected unchanged encoded pixels`() =
        runTest {
            // Given
            val encoded = "data:image/png;base64,iVBORw0KGgo="
            coEvery {
                payloads.read(
                    42,
                    "revision",
                    false,
                    1000
                )
            } returns encoded.encodeToByteArray()

            // When
            val result = gateway.image(42, "revision", false, 1000)

            // Then
            assertEquals(encoded, result)
            coVerify(exactly = 1) { payloads.read(42, "revision", false, 1000) }
        }

    @Test
    fun `given gallery image read, source fails revision or size admission, expected unavailable image`() =
        runTest {
            // Given
            coEvery { payloads.read(42, "stale", true, 1000) } returns null

            // When
            val result = gateway.image(42, "stale", true, 1000)

            // Then
            assertNull(result)
        }

    @Test
    fun `given gallery image read, database fails, expected original failure propagated`() =
        runTest {
            // Given
            val failure = IllegalStateException("storage unavailable")
            coEvery { payloads.read(42, "revision", false, 1000) } throws failure

            // When
            val actual =
                runCatching { gateway.image(42, "revision", false, 1000) }.exceptionOrNull()

            // Then
            assertSame(failure, actual)
        }
}
