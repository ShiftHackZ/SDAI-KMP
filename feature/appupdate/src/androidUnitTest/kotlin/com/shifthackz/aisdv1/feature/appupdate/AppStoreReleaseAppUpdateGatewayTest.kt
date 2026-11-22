package com.shifthackz.aisdv1.feature.appupdate

import com.shifthackz.aisdv1.feature.appupdate.domain.entity.AppUpdate
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks Apple application identity, public version ordering and distribution-specific endpoints.
 *
 * @author Dmitriy Moroz
 */
class AppStoreReleaseAppUpdateGatewayTest {
    private val fixture = ReleaseGatewayFixture()

    @After
    fun closeClients() {
        fixture.close()
    }

    @Test
    fun `given App Store discovery, a newer patch is published, expected the trusted App Store page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(appStorePayload(version = "1.2.1"), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertEquals(AppUpdate("1.2.1", "https://apps.apple.com/app/id6778314183"), result)
        }

    @Test
    fun `given App Store discovery, a newer minor is published, expected the trusted App Store page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(appStorePayload(version = "1.3.0"), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertEquals(AppUpdate("1.3.0", "https://apps.apple.com/app/id6778314183"), result)
        }

    @Test
    fun `given App Store discovery, a newer major is published, expected the trusted App Store page`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(appStorePayload(version = "2.0.0"), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertEquals(AppUpdate("2.0.0", "https://apps.apple.com/app/id6778314183"), result)
        }

    @Test
    fun `given App Store discovery, the version matches, expected no offer`() = runBlocking {
        // Given
        val gateway = fixture.gateway(appStorePayload(version = "1.2.0"), isAppStore = true)

        // When
        val result = gateway.check()

        // Then
        assertNull(result)
    }

    @Test
    fun `given App Store discovery, the published version is older, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(appStorePayload(version = "1.1.9"), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given App Store discovery, the store version is malformed, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(appStorePayload(version = "invalid"), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given App Store discovery, the version has a tag prefix, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(appStorePayload(version = "v1.3.0"), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given App Store discovery, the version is a prerelease, expected no offer`() =
        runBlocking {
            // Given
            val gateway =
                fixture.gateway(appStorePayload(version = "1.3.0-beta"), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given App Store discovery, lookup returns no matches, expected no offer`() = runBlocking {
        // Given
        val gateway = fixture.gateway("{\"results\":[]}", isAppStore = true)

        // When
        val result = gateway.check()

        // Then
        assertNull(result)
    }

    @Test
    fun `given App Store discovery, another application is returned, expected no offer`() =
        runBlocking {
            // Given
            val gateway = fixture.gateway(appStorePayload(trackId = 123L), isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertNull(result)
        }

    @Test
    fun `given App Store discovery, the matching result follows another application, expected the correct application version`() =
        runBlocking {
            // Given
            val payload = Json.encodeToString(
                AppStoreLookup(
                    listOf(
                        AppStoreRelease(123L, "9.0.0"),
                        AppStoreRelease(6778314183L, "1.3.0")
                    )
                )
            )
            val gateway = fixture.gateway(payload, isAppStore = true)

            // When
            val result = gateway.check()

            // Then
            assertEquals("1.3.0", result?.version)
        }

    @Test
    fun `given App Store discovery, a nightly shaped installed version is supplied, expected Apple lookup only`() =
        runBlocking {
            // Given
            val gateway =
                fixture.gateway(appStorePayload(), version = "1.2.0-dev.local", isAppStore = true)

            // When
            gateway.check()

            // Then
            assertEquals("itunes.apple.com", fixture.requests.single().url.host)
            assertEquals("6778314183", fixture.requests.single().url.parameters["id"])
        }
}
