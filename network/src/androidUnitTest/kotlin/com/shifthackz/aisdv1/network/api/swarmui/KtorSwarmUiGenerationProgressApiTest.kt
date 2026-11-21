package com.shifthackz.aisdv1.network.api.swarmui

import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiProgressTransportFixture.Companion.BASE_URL
import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiProgressTransportFixture.Companion.COMPLETE
import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiProgressTransportFixture.Companion.IMAGE
import com.shifthackz.aisdv1.network.api.swarmui.SwarmUiProgressTransportFixture.Companion.REQUEST
import com.shifthackz.aisdv1.network.auth.BasicHttpAuthorization
import com.shifthackz.aisdv1.network.exception.SwarmUiBadSessionException
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Verifies workflow progress and terminal/error frames through the production WebSocket client. @author Dmitriy Moroz */
class KtorSwarmUiGenerationProgressApiTest {
    @Test
    fun `given SwarmUI generation, workflow and node percentages differ, expected only workflow measurement and final image`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(
            listOf(
                """{"status":{"loading_models":1}}""",
                """{"gen_progress":{"overall_percent":0.25,"current_percent":0.9,"preview":"ignored"}}""",
                IMAGE, COMPLETE,
            )
        )
        val progress = mutableListOf<Float>()
        fixture.run { api ->
            // When
            val response = api.generateWithProgress(BASE_URL, REQUEST, null, progress::add)
            // Then
            assertEquals(listOf(0.25f), progress)
            assertEquals(listOf("View/result.png"), response.images)
            assertEquals(REQUEST, fixture.receivedRequest)
            assertEquals(1, fixture.requestCount)
            assertNull(fixture.authorization)
        }
    }

    @Test
    fun `given SwarmUI generation, Basic credentials and proxy prefix are configured, expected authenticated single submission`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(listOf(IMAGE, COMPLETE))
        fixture.run { api ->
            // When
            api.generateWithProgress(BASE_URL, REQUEST, BasicHttpAuthorization("user", "pass")) {}
            // Then
            assertEquals("Basic dXNlcjpwYXNz", fixture.authorization)
            assertEquals(1, fixture.requestCount)
        }
    }

    @Test
    fun `given SwarmUI workflow, node-only frames arrive, expected no fabricated overall measurement`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(
            listOf(
                """{"gen_progress":{"current_percent":0.8}}""", IMAGE, COMPLETE,
            )
        )
        val progress = mutableListOf<Float>()
        fixture.run { api ->
            // When
            api.generateWithProgress(BASE_URL, REQUEST, null, progress::add)
            // Then
            assertTrue(progress.isEmpty())
        }
    }

    @Test
    fun `given SwarmUI workflow, fractions exceed range, expected bounded percentages`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(
            listOf(
                """{"gen_progress":{"overall_percent":-0.5}}""",
                """{"gen_progress":{"overall_percent":1.5}}""", IMAGE, COMPLETE,
            )
        )
        val progress = mutableListOf<Float>()
        fixture.run { api ->
            // When
            api.generateWithProgress(BASE_URL, REQUEST, null, progress::add)
            // Then
            assertEquals(listOf(0f, 1f), progress)
        }
    }

    @Test
    fun `given SwarmUI postprocessing, first image is discarded and grid is returned, expected only retained real image`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(
            listOf(
                IMAGE,
                """{"image":"View/second.png","batch_index":"1"}""",
                """{"image":"View/grid.png","batch_index":"-1"}""",
                """{"discard_indices":[0]}""", COMPLETE,
            )
        )
        fixture.run { api ->
            // When
            val response = api.generateWithProgress(BASE_URL, REQUEST, null) {}
            // Then
            assertEquals(listOf("View/second.png"), response.images)
        }
    }

    @Test
    fun `given SwarmUI generation, session is invalid, expected typed session failure without request replay`() {
        // Given
        val fixture =
            SwarmUiProgressTransportFixture(listOf("""{"error":"Expired","error_id":"invalid_session_id"}"""))
        fixture.run { api ->
            // When
            val result = runCatching { api.generateWithProgress(BASE_URL, REQUEST, null) {} }
            // Then
            assertTrue(result.exceptionOrNull() is SwarmUiBadSessionException)
            assertEquals(1, fixture.requestCount)
        }
    }

    @Test
    fun `given SwarmUI generation, server emits generation error, expected failure without successful partial output`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(
            listOf(
                IMAGE,
                """{"error":"backend failed"}""",
                COMPLETE
            )
        )
        fixture.run { api ->
            // When
            val result = runCatching { api.generateWithProgress(BASE_URL, REQUEST, null) {} }
            // Then
            assertTrue(result.exceptionOrNull() is IllegalStateException)
            assertEquals("backend failed", result.exceptionOrNull()?.message)
            assertEquals(1, fixture.requestCount)
        }
    }

    @Test
    fun `given SwarmUI generation, stream closes after an image without completion marker, expected failure`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(listOf(IMAGE))
        fixture.run { api ->
            // When
            val result = runCatching { api.generateWithProgress(BASE_URL, REQUEST, null) {} }
            // Then
            assertTrue(result.isFailure)
            assertEquals(1, fixture.requestCount)
        }
    }

    @Test
    fun `given SwarmUI generation, completion contains no retained images, expected failure`() {
        // Given
        val fixture =
            SwarmUiProgressTransportFixture(listOf(IMAGE, """{"discard_indices":[0]}""", COMPLETE))
        fixture.run { api ->
            // When
            val result = runCatching { api.generateWithProgress(BASE_URL, REQUEST, null) {} }
            // Then
            assertTrue(result.isFailure)
        }
    }

    @Test
    fun `given SwarmUI generation, malformed frame arrives, expected decoding failure without request replay`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(listOf("{broken", IMAGE, COMPLETE))
        fixture.run { api ->
            // When
            val result = runCatching { api.generateWithProgress(BASE_URL, REQUEST, null) {} }
            // Then
            assertTrue(result.isFailure)
            assertEquals(1, fixture.requestCount)
        }
    }

    @Test
    fun `given SwarmUI generation, deadline expires on an open stream, expected request timeout rather than caller cancellation`() {
        // Given
        val fixture =
            SwarmUiProgressTransportFixture(emptyList(), shouldHoldOpen = true, timeoutMillis = 500)
        fixture.run { api ->
            // When
            val result = runCatching { api.generateWithProgress(BASE_URL, REQUEST, null) {} }
            // Then
            assertTrue(result.exceptionOrNull() is HttpRequestTimeoutException)
        }
    }

    @Test
    fun `given SwarmUI generation, caller cancels after workflow progress, expected cancellation and no replay`() {
        // Given
        val fixture = SwarmUiProgressTransportFixture(
            listOf("""{"gen_progress":{"overall_percent":0.3}}"""), shouldHoldOpen = true,
        )
        fixture.run { api ->
            coroutineScope {
                val observed = CompletableDeferred<Unit>()
                var hasReturned = false
                val job = launch {
                    api.generateWithProgress(BASE_URL, REQUEST, null) { observed.complete(Unit) }
                    hasReturned = true
                }
                observed.await()
                // When
                job.cancelAndJoin()
                // Then
                assertTrue(job.isCancelled)
                assertFalse(hasReturned)
                assertEquals(1, fixture.requestCount)
            }
        }
    }
}
