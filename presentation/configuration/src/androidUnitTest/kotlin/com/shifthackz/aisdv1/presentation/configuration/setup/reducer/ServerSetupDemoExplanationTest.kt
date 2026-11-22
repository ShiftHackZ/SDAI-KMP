package com.shifthackz.aisdv1.presentation.configuration.setup.reducer

import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupIntent
import com.shifthackz.aisdv1.presentation.configuration.setup.ServerSetupState
import com.shifthackz.aisdv1.presentation.configuration.setup.requiresDemoExplanation
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies demo acknowledgement cannot be skipped, retained across attempts or applied to another provider.
 * Connection callbacks count requests without creating network or persistence side effects.
 *
 * @author Dmitriy Moroz
 */
class ServerSetupDemoExplanationTest {
    @Test
    fun `given demo configuration, submit pressed, expected fresh dialog without connection`() {
        // Given
        val fixture = Fixture(candidate(isDemoEnabled = true))

        // When
        fixture.process(ServerSetupIntent.MainButtonClick)

        // Then
        assertEquals(ServerSetupState.Modal.DemoExplanation(), fixture.state.modal)
        assertEquals(0, fixture.validationRequests)
        assertEquals(0, fixture.directConnections)
    }

    @Test
    fun `given demo address and disabled toggle, submit pressed, expected explanation required`() {
        // Given
        val fixture =
            Fixture(candidate(serverUrl = "  HTTPS://DEMO.EXAMPLE:443/preview///?source=review#intro  "))

        // When
        fixture.process(ServerSetupIntent.MainButtonClick)

        // Then
        assertTrue(fixture.state.modal is ServerSetupState.Modal.DemoExplanation)
        assertFalse(fixture.state.demoMode)
        assertEquals(0, fixture.validationRequests)
    }

    @Test
    fun `given demo dialog, zero or one acknowledgement checked, expected confirmation cannot connect`() {
        // Given
        val combinations = listOf(false to false, true to false, false to true)
        combinations.forEach { (hasUnderstood, hasAgreed) ->
            val fixture = Fixture(candidate(isDemoEnabled = true))
            fixture.process(ServerSetupIntent.MainButtonClick)
            fixture.process(ServerSetupIntent.AcknowledgeDemoBehavior(hasUnderstood))
            fixture.process(ServerSetupIntent.AgreeToDemoFeedback(hasAgreed))

            // When
            fixture.process(ServerSetupIntent.ConfirmDemoExplanation)

            // Then
            assertTrue(fixture.state.modal is ServerSetupState.Modal.DemoExplanation)
            assertEquals(0, fixture.validationRequests)
        }
    }

    @Test
    fun `given demo URL and both acknowledgements, confirmed twice, expected one validated demo connection`() {
        // Given
        val fixture = Fixture(candidate(serverUrl = "https://demo.example/preview/"))
        fixture.process(ServerSetupIntent.MainButtonClick)
        fixture.process(ServerSetupIntent.AcknowledgeDemoBehavior(true))
        fixture.process(ServerSetupIntent.AgreeToDemoFeedback(true))

        // When
        fixture.process(ServerSetupIntent.ConfirmDemoExplanation)
        fixture.process(ServerSetupIntent.ConfirmDemoExplanation)

        // Then
        assertEquals(1, fixture.validationRequests)
        assertEquals(0, fixture.directConnections)
        assertTrue(fixture.state.demoMode)
        assertEquals(ServerSetupState.AuthType.ANONYMOUS, fixture.state.authType)
        assertEquals(ServerSetupState.Modal.None, fixture.state.modal)
    }

    @Test
    fun `given demo explanation, cross pressed, expected demo disabled with editable configuration retained`() {
        // Given
        val fixture =
            Fixture(candidate(isDemoEnabled = true, serverUrl = "https://my-server.example"))
        fixture.process(ServerSetupIntent.MainButtonClick)
        fixture.process(ServerSetupIntent.AcknowledgeDemoBehavior(true))

        // When
        fixture.process(ServerSetupIntent.CloseDemoExplanation)

        // Then
        assertFalse(fixture.state.demoMode)
        assertEquals(ServerSetupState.Modal.None, fixture.state.modal)
        assertEquals("https://my-server.example", fixture.state.serverUrl)
        assertEquals(ServerSetupState.Step.CONFIGURE, fixture.state.step)
        assertEquals(0, fixture.validationRequests)
    }

    @Test
    fun `given demo URL with switch off, cross then submit pressed, expected explanation shown again`() {
        // Given
        val fixture = Fixture(candidate(serverUrl = "https://demo.example/preview"))
        fixture.process(ServerSetupIntent.MainButtonClick)
        fixture.process(ServerSetupIntent.CloseDemoExplanation)

        // When
        fixture.process(ServerSetupIntent.MainButtonClick)

        // Then
        assertEquals(ServerSetupState.Modal.DemoExplanation(), fixture.state.modal)
        assertFalse(fixture.state.demoMode)
        assertEquals(0, fixture.validationRequests)
    }

    @Test
    fun `given blocking demo dialog, dismissal and back requested, expected dialog and candidate unchanged`() {
        // Given
        val fixture = Fixture(candidate(isDemoEnabled = true))
        fixture.process(ServerSetupIntent.MainButtonClick)
        val before = fixture.state

        // When
        fixture.process(ServerSetupIntent.DismissDialog)
        fixture.process(ServerSetupIntent.NavigateBack)

        // Then
        assertEquals(before, fixture.state)
        assertEquals(0, fixture.validationRequests)
    }

    @Test
    fun `given acknowledged demo setup, configuration submitted again, expected acknowledgements reset`() {
        // Given
        val fixture = Fixture(candidate(isDemoEnabled = true))
        fixture.process(ServerSetupIntent.MainButtonClick)
        fixture.process(ServerSetupIntent.AcknowledgeDemoBehavior(true))
        fixture.process(ServerSetupIntent.AgreeToDemoFeedback(true))
        fixture.process(ServerSetupIntent.ConfirmDemoExplanation)

        // When
        fixture.process(ServerSetupIntent.MainButtonClick)

        // Then
        assertEquals(ServerSetupState.Modal.DemoExplanation(), fixture.state.modal)
        assertEquals(1, fixture.validationRequests)
    }

    @Test
    fun `given real server or another provider, submit pressed, expected normal validation without demo gate`() {
        // Given
        val candidates = listOf(
            candidate(serverUrl = "https://my-server.example"),
            candidate(isDemoEnabled = true).copy(mode = ServerSource.SWARM_UI),
        )
        candidates.forEach { state ->
            val fixture = Fixture(state)

            // When
            fixture.process(ServerSetupIntent.MainButtonClick)

            // Then
            assertEquals(1, fixture.validationRequests)
            assertEquals(ServerSetupState.Modal.None, fixture.state.modal)
        }
    }

    @Test
    fun `given source selection and demo enabled, next pressed, expected configuration step before explanation`() {
        // Given
        val fixture =
            Fixture(candidate(isDemoEnabled = true).copy(step = ServerSetupState.Step.SOURCE))

        // When
        fixture.process(ServerSetupIntent.MainButtonClick)

        // Then
        assertEquals(ServerSetupState.Step.CONFIGURE, fixture.state.step)
        assertEquals(ServerSetupState.Modal.None, fixture.state.modal)
        assertEquals(0, fixture.validationRequests)
    }

    @Test
    fun `given configured demo endpoint, equivalent and distinct URLs compared, expected only same endpoint matches`() {
        // Given
        val matches = listOf(
            "https://demo.example/preview",
            " HTTPS://DEMO.EXAMPLE:443/preview/// ",
            "https://demo.example/preview?source=review#intro",
        )
        val distinct = listOf(
            "",
            "not a URL",
            "https://other.example/preview",
            "https://demo.example:8443/preview",
            "https://demo.example/Preview",
            "https://demo.example/preview-other",
            "https://demo.example.evil/preview",
        )

        // When
        val matched = matches.map { candidate(serverUrl = it).requiresDemoExplanation }
        val rejected = distinct.map { candidate(serverUrl = it).requiresDemoExplanation }

        // Then
        assertTrue(matched.all { it })
        assertTrue(rejected.none { it })
    }

    /**
     * Builds a candidate with a reserved demonstration endpoint and no saved acknowledgement.
     *
     * @param isDemoEnabled Initial explicit toggle state.
     * @param serverUrl Initial editable server address.
     * @author Dmitriy Moroz
     */
    private fun candidate(isDemoEnabled: Boolean = false, serverUrl: String = "") =
        ServerSetupState(
            step = ServerSetupState.Step.CONFIGURE,
            loadingConfiguration = false,
            demoMode = isDemoEnabled,
            serverUrl = serverUrl,
            demoModeUrl = "https://demo.example/preview",
        )

    /**
     * Drives the production intent processor with state replacement and observable submission counts.
     *
     * @param initialState Candidate setup state before any production intents are processed.
     * @author Dmitriy Moroz
     */
    private class Fixture(initialState: ServerSetupState) {
        var state = initialState
        var validationRequests = 0
        var directConnections = 0
        private val processor = ServerSetupIntentProcessor(
            router = mockk(relaxed = true),
            linksProvider = mockk(relaxed = true),
            currentState = { state },
            updateState = { state = it(state) },
            emitEffect = {},
            localModelDownloadClickReducer = {},
            deleteLocalModel = { _, _ -> },
            download = { _, _, _ -> },
            validateAndConnectToServer = { validationRequests++ },
            connectToServer = { directConnections++ },
        )

        /**
         * Delivers a real setup intent through the production reducer.
         *
         * @param intent User action or attempted stale callback to process.
         * @author Dmitriy Moroz
         */
        fun process(intent: ServerSetupIntent) = processor.process(intent)
    }
}
