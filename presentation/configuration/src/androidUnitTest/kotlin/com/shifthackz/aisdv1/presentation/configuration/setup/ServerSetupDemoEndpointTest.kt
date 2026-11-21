package com.shifthackz.aisdv1.presentation.configuration.setup

import com.shifthackz.aisdv1.domain.entity.ServerSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Protects the demo acknowledgement gate when users enter equivalent endpoint spellings.
 * @author Dmitriy Moroz
 */
class ServerSetupDemoEndpointTest {

    @Test
    fun `given demo endpoint gate, host and scheme case is entered, expected acknowledgement required`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = "HTTPS://DEMO.EXAMPLE/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(true, isRequired)
    }

    @Test
    fun `given demo endpoint gate, default HTTPS port is entered, expected acknowledgement required`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "https://demo.example:443/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(true, isRequired)
    }

    @Test
    fun `given demo endpoint gate, trailing slashes is entered, expected acknowledgement required`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "https://demo.example/preview///"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(true, isRequired)
    }

    @Test
    fun `given demo endpoint gate, query suffix is entered, expected acknowledgement required`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "https://demo.example/preview?theme=dark"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(true, isRequired)
    }

    @Test
    fun `given demo endpoint gate, fragment suffix is entered, expected acknowledgement required`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "https://demo.example/preview#help"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(true, isRequired)
    }

    @Test
    fun `given demo endpoint gate, surrounding whitespace is entered, expected acknowledgement required`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "  https://demo.example/preview  "
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(true, isRequired)
    }

    @Test
    fun `given demo endpoint gate, credentials is entered, expected acknowledgement required`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "https://user:password@demo.example/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(true, isRequired)
    }

    @Test
    fun `given demo endpoint gate, different port is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "https://demo.example:8443/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, different path is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = "https://demo.example/other"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, path case is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = "https://demo.example/Preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, different protocol is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = "http://demo.example/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, different host is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = "https://real.example/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, missing scheme is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = "demo.example/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, empty address is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = ""
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, host whitespace is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = false,
            demoModeUrl = "https://demo.example/preview",
            serverUrl = "https://demo .example/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, unsupported protocol is entered, expected normal provider submission`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111, demoMode = false,
            demoModeUrl = "https://demo.example/preview", serverUrl = "file://demo.example/preview"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertEquals(false, isRequired)
    }

    @Test
    fun `given demo endpoint gate, another provider is selected with demo switch retained, expected no demo interception`() {
        // Given
        val states = ServerSource.entries.filter { it != ServerSource.AUTOMATIC1111 }
            .map {
                ServerSetupState(
                    mode = it,
                    demoMode = true,
                    demoModeUrl = "https://demo.example",
                    serverUrl = "https://demo.example"
                )
            }
        // When
        val requirements = states.map { it.requiresDemoExplanation }
        // Then
        assertFalse(requirements.any { it })
    }

    @Test
    fun `given demo endpoint gate, explicit demo switch is enabled, expected explanation even before URL synchronization`() {
        // Given
        val state = ServerSetupState(
            mode = ServerSource.AUTOMATIC1111,
            demoMode = true,
            serverUrl = "https://real.example"
        )
        // When
        val isRequired = state.requiresDemoExplanation
        // Then
        assertTrue(isRequired)
    }
}
