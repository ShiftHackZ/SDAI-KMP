package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Covers stable release identity, semantic version ordering and channel isolation.
 *
 * @author Dmitriy Moroz
 */
class StableReleaseAppUpdateGatewayTest {
    private val fixture = ReleaseGatewayFixture()

    @After
    fun closeClients() {
        fixture.close()
    }

    @Test
    fun `given stable update discovery, a newer patch is published, expected the published release page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.2.1"))

            // When
            val result = gateway.check()

            // Then
            assertEquals(
                AppUpdate(
                    "1.2.1",
                    "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.2.1"
                ), result
            )
        }

    @Test
    fun `given stable update discovery, a newer minor is published, expected the published release page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.3.0"))

            // When
            val result = gateway.check()

            // Then
            assertEquals(
                AppUpdate(
                    "1.3.0",
                    "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.3.0"
                ), result
            )
        }

    @Test
    fun `given stable update discovery, a newer major is published, expected the published release page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "2.0.0"))

            // When
            val result = gateway.check()

            // Then
            assertEquals(
                AppUpdate(
                    "2.0.0",
                    "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/2.0.0"
                ), result
            )
        }

    @Test
    fun `given stable update discovery, the tag has a v prefix, expected the published release page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "v1.3.0"))

            // When
            val result = gateway.check()

            // Then
            assertEquals(
                AppUpdate(
                    "v1.3.0",
                    "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/v1.3.0"
                ), result
            )
        }

    @Test
    fun `given stable update discovery, the minor exceeds nine, expected the published release page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.10.0"))

            // When
            val result = gateway.check()

            // Then
            assertEquals(
                AppUpdate(
                    "1.10.0",
                    "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/1.10.0"
                ), result
            )
        }

    @Test
    fun `given stable update discovery, the installed version matches, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.2.0"))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, an older patch is published, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.1.9"))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, an older major has a larger minor, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "0.99.99"))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, the tag is missing a patch, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.3"))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, the tag is not numeric, expected no offer`() = runBlocking {
        // Given
        val gateway = fixture.gateway(releasePayload(tag = "latest"))

        // When
        val result = gateway.check()

        // Then
        assertNull(result)
    }

    @Test
    fun `given stable update discovery, the tag contains a prerelease suffix, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.3.0-beta"))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, the tag has a path separator, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = "1.3.0/extra"))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, the tag includes whitespace, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(tag = " 1.3.0"))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, a draft release is returned, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(isDraft = true))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, a prerelease is returned, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(isPrerelease = true))

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given stable update discovery, release notes are null, expected a valid offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(releasePayload(body = null))

            // When
            val result = gateway.check()

            // Then
            assertEquals("1.3.0", result?.version)
        }

    @Test
    fun `given stable update discovery, the server adds unknown metadata, expected a valid offer`() =
        runBlocking {
            // Given
            val payload = releasePayload().dropLast(1) + ",\"future_field\":{\"nested\":true}}"
            val gateway = fixture.gateway(payload)

            // When
            val result = gateway.check()

            // Then
            assertEquals("1.3.0", result?.version)
        }
}
