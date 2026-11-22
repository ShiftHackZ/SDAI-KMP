package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Protects moving-tag discovery and rejects missing, incomplete or older nightly publications.
 *
 * @author Dmitriy Moroz
 */
class NightlyReleaseAppUpdateGatewayTest {
    private val fixture = ReleaseGatewayFixture()

    @After
    fun closeClients() {
        fixture.close()
    }

    @Test
    fun `given nightly update discovery, CI publishes a greater build code, expected the moving nightly page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (101)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertEquals(
                AppUpdate(
                    "1.2.0-dev.20261122.abcdef12",
                    "https://github.com/ShiftHackZ/SDAI-KMP/releases/tag/nightly"
                ), result
            )
            assertEquals(
                "/repos/ShiftHackZ/SDAI-KMP/releases/tags/nightly",
                fixture.requests.single().url.encodedPath
            )
        }

    @Test
    fun `given nightly update discovery, the installed code matches, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (100)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the server build is older, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (99)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the server code is zero, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (0)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the release has a stable tag, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "1.3.0",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (101)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the release is a draft, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    isDraft = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (101)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the APK has not been uploaded, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (101)`",
                    assets = emptyList()
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, only a checksum is attached, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (101)`",
                    assets = listOf("sdai-full-nightly.apk.sha256")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the asset belongs to FOSS, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (101)`",
                    assets = listOf("sdai-foss-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, release notes are null, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = null,
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, release notes have no version, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "No build metadata",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the code is non numeric, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (invalid)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the code exceeds Int range, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (2147483648)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the code is negative, expected no offer`() = runBlocking {
        // Given
        val gateway = fixture.gateway(
            releasePayload(
                tag = "nightly",
                isPrerelease = true,
                body = "- Version: `1.2.0-dev.20261122.abcdef12 (-1)`",
                assets = listOf("sdai-full-nightly.apk")
            ), version = "1.2.0-dev.local"
        )

        // When
        val result = gateway.check()

        // Then
        assertNull(result)
    }

    @Test
    fun `given nightly update discovery, the notes lack code delimiters, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 101`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given nightly update discovery, the code is omitted, expected no offer`() = runBlocking {
        // Given
        val gateway = fixture.gateway(
            releasePayload(
                tag = "nightly",
                isPrerelease = true,
                body = "- Version: `1.2.0-dev.20261122.abcdef12`",
                assets = listOf("sdai-full-nightly.apk")
            ), version = "1.2.0-dev.local"
        )

        // When
        val result = gateway.check()

        // Then
        assertNull(result)
    }

    @Test
    fun `given nightly update discovery, marketing versions match but a rerun raises the code, expected a new offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (101)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.20261122.abcdef12"
            )

            // When
            val result = gateway.check()

            // Then
            assertEquals("1.2.0-dev.20261122.abcdef12", result?.version)
        }

    @Test
    fun `given nightly update discovery, the CI code is the maximum valid integer, expected an offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(
                releasePayload(
                    tag = "nightly",
                    isPrerelease = true,
                    body = "- Version: `1.2.0-dev.20261122.abcdef12 (2147483647)`",
                    assets = listOf("sdai-full-nightly.apk")
                ), version = "1.2.0-dev.local"
            )

            // When
            val result = gateway.check()

            // Then
            assertEquals("1.2.0-dev.20261122.abcdef12", result?.version)
        }
}
