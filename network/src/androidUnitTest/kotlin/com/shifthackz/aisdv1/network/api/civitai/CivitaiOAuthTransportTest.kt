package com.shifthackz.aisdv1.network.api.civitai

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.parseQueryString
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Protects PKCE form fields, access-only sessions and strict OAuth failure propagation.
 *
 * @author Dmitriy Moroz
 */
class CivitaiOAuthTransportTest {
    @Test
    fun `given PKCE authorization, code is exchanged, expected registered form fields and access-only tokens`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"access_token":"access","expires_in":3600,"unknown":true}""").use { fixture ->
                // When
                val tokens = fixture.api.exchange("code +/=", "verifier +/=")
                // Then
                val request = fixture.requests.single()
                assertEquals(HttpMethod.Post, request.method)
                assertEquals(
                    "https://auth.civitai.com/api/auth/oauth/token",
                    request.url.toString()
                )
                val fields = parseQueryString(request.bodyText())
                assertEquals("authorization_code", fields["grant_type"])
                assertEquals("code +/=", fields["code"])
                assertEquals("verifier +/=", fields["code_verifier"])
                assertEquals("cc.moroz.sdai://oauth/civitai", fields["redirect_uri"])
                assertEquals("public-client", fields["client_id"])
                assertNull(fields["client_secret"])
                assertNull(request.headers[HttpHeaders.Authorization])
                assertEquals("access", tokens.accessToken)
                assertNull(tokens.refreshToken)
            }
        }

    @Test
    fun `given renewable OAuth session, refresh is requested, expected refresh grant and rotated token pair`() =
        runBlocking {
            // Given
            CivitaiApiFixture("""{"access_token":"new-access","refresh_token":"new-refresh","expires_in":60}""").use { fixture ->
                // When
                val tokens = fixture.api.refresh("old-refresh")
                // Then
                val fields = parseQueryString(fixture.requests.single().bodyText())
                assertEquals("refresh_token", fields["grant_type"])
                assertEquals("old-refresh", fields["refresh_token"])
                assertEquals("public-client", fields["client_id"])
                assertNull(fields["code_verifier"])
                assertEquals("new-refresh", tokens.refreshToken)
            }
        }

    @Test
    fun `given saved refresh token, revocation is requested, expected registered client and token hint`() =
        runBlocking {
            // Given
            CivitaiApiFixture("", HttpStatusCode.NoContent).use { fixture ->
                // When
                fixture.api.revoke("saved-refresh")
                // Then
                val request = fixture.requests.single()
                assertEquals(
                    "https://auth.civitai.com/api/auth/oauth/revoke",
                    request.url.toString()
                )
                val fields = parseQueryString(request.bodyText())
                assertEquals("saved-refresh", fields["token"])
                assertEquals("refresh_token", fields["token_type_hint"])
                assertEquals("public-client", fields["client_id"])
            }
        }

    @Test
    fun `given rejected OAuth grant, token request fails, expected safe failure without retry or secret response`() =
        runBlocking {
            // Given
            CivitaiApiFixture("private server detail", HttpStatusCode.Unauthorized).use { fixture ->
                // When
                val failure =
                    runCatching { fixture.api.exchange("code", "verifier") }.exceptionOrNull()
                // Then
                assertEquals("CIVITAI_TOKEN_FAILED", failure?.message)
                assertEquals(1, fixture.requests.size)
            }
        }

    @Test
    fun `given rejected revocation, disconnect transport fails, expected failure remains observable`() =
        runBlocking {
            // Given
            CivitaiApiFixture("private", HttpStatusCode.ServiceUnavailable).use { fixture ->
                // When
                val failure = runCatching { fixture.api.revoke("refresh") }.exceptionOrNull()
                // Then
                assertEquals("CIVITAI_REVOKE_FAILED", failure?.message)
                assertEquals(1, fixture.requests.size)
            }
        }

    @Test
    fun `given invalid token metadata, exchange succeeds over HTTP, expected malformed session rejected`() =
        runBlocking {
            // Given
            val replies = listOf(
                """{"access_token":"","expires_in":3600}""",
                """{"access_token":"access","refresh_token":"","expires_in":3600}""",
                """{"access_token":"access","expires_in":0}""",
                """{"access_token":"access","expires_in":-1}""",
                """{"expires_in":3600}""",
            )
            for (reply in replies) CivitaiApiFixture(reply).use { fixture ->
                // When
                val failure =
                    runCatching { fixture.api.exchange("code", "verifier") }.exceptionOrNull()
                // Then
                assertTrue(failure != null)
                assertEquals(1, fixture.requests.size)
            }
        }

    @Test
    fun `given cancelled OAuth transport, token request stops, expected cancellation without another request`() =
        runBlocking {
            // Given
            CivitaiApiFixture("").use { fixture ->
                fixture.failure = CancellationException("cancelled")
                // When
                val failure = runCatching { fixture.api.refresh("refresh") }.exceptionOrNull()
                // Then
                assertTrue(failure is CancellationException)
                assertEquals(1, fixture.requests.size)
            }
        }
}
