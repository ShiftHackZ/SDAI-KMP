package com.shifthackz.aisdv1.data.preference.backup

import com.shifthackz.aisdv1.data.preference.KeyValueStore
import com.shifthackz.aisdv1.data.preference.KeyValueConfigurationStore
import com.shifthackz.aisdv1.data.preference.PreferenceManagerImpl
import com.shifthackz.aisdv1.domain.entity.Grid
import com.shifthackz.aisdv1.domain.entity.ServerSource
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.domain.entity.NetworkTimeout
import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupStartupStage
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.preference.PreferenceManager
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BackupPreferencesGatewayImplTest {
    private val strings = mutableMapOf<String, String>()
    private val flags = mutableMapOf<String, Boolean>()
    private val integers = mutableMapOf<String, Int>()
    private val store = mockk<KeyValueStore>(relaxed = true)
    private val preferences = mockk<PreferenceManager>(relaxed = true)
    private val gateway = BackupPreferencesGatewayImpl(store, preferences)

    @Before
    fun setup() {
        every { store.contains(any()) } answers { firstArg<String>() in strings || firstArg<String>() in flags || firstArg<String>() in integers }
        every { store.getString(any(), any()) } answers { strings[firstArg()] ?: secondArg() }
        every { store.getBoolean(any(), any()) } answers { flags[firstArg()] ?: secondArg() }
        every { store.getInt(any(), any()) } answers { integers[firstArg()] ?: secondArg() }
        every {
            store.putConfigurationDurably(
                any(),
                any()
            )
        } answers { strings.putAll(firstArg()); flags.putAll(secondArg()); Unit }
        every { preferences.galleryGrid } returns Grid.Fixed2
        every { preferences.connectionTimeout } returns NetworkTimeout.TWO_MINUTES
        every { preferences.generationRequestTimeout } returns NetworkTimeout.TWO_MINUTES
        every { preferences.designColorToken } returns "default"
        every { preferences.designDarkThemeToken } returns "FRAPPE"
        every { preferences.languageCode } returns ""
        every { preferences.textToImageCfgScale } returns 7f
        every { preferences.imageToImageCfgScale } returns 7f
        every { preferences.textToImageSamplingSteps } returns 20
        every { preferences.imageToImageSamplingSteps } returns 20
    }

    @Test
    fun `given fresh default settings, backup inventory reads effective preferences, expected settings remain selectable`() {
        // Given
        every { preferences.autoSaveAiResults } returns true
        // When
        val snapshot = gateway.read()
        // Then
        assertEquals(BackupValue.Flag(true), snapshot.settings["autoSave"])
        assertEquals(BackupValue.Integer(20), snapshot.settings["textToImageSteps"])
        assertTrue(snapshot.providers.isEmpty())
    }

    @Test
    fun `given configured provider with explicit saved default, snapshot is read, expected default presence preserved`() {
        // Given
        flags["key_demo_mode"] = false
        strings["key_server_url"] = "https://configured"
        // When
        val snapshot = gateway.read()
        // Then
        assertEquals(BackupValue.Flag(false), snapshot.providers["custom"]?.get("demoMode"))
        assertFalse(snapshot.providers.getValue("custom").containsKey("model"))
    }

    @Test
    fun `given existing installation, recovery stage is absent, expected completed stage without invitation`() {
        // Given
        flags["key_on_boarding_complete"] = true
        // When
        val stage = gateway.readStartupStage()
        // Then
        assertEquals(BackupStartupStage.COMPLETE, stage)
    }

    @Test
    fun `given onboarding completion, invitation is persisted, expected atomic completed flag and stage batch`() {
        // Given
        flags["key_on_boarding_complete"] = false
        // When
        gateway.writeStartupStage(BackupStartupStage.INVITATION)
        // Then
        verify {
            store.putConfigurationDurably(
                mapOf("key_backup_startup_stage" to "INVITATION"),
                mapOf("key_on_boarding_complete" to true)
            )
        }
        verify(exactly = 0) { store.putBoolean(any(), any()) }
        assertEquals(true, flags["key_on_boarding_complete"])
        assertEquals(BackupStartupStage.INVITATION, gateway.readStartupStage())
    }

    @Test
    fun `given inactive provider restore, selected provider is OpenAI, expected selection and revision untouched`() {
        // Given
        strings["key_provider_selection_state"] = "open_ai|41"
        // When
        gateway.writeProviders(
            mapOf("custom" to mapOf("serverUrl" to BackupValue.Text("https://imported"))),
            null
        )
        // Then
        assertEquals("open_ai|41", strings["key_provider_selection_state"])
        assertEquals("https://imported", strings["key_server_url"])
    }

    @Test
    fun `given selected provider restore, connection changes, expected same source with advanced revision`() {
        // Given
        strings["key_provider_selection_state"] = "easy_diffusion|41"
        strings["key_easy_diffusion_url"] = "https://old"
        // When
        gateway.writeProviders(
            mapOf("easy_diffusion" to mapOf("serverUrl" to BackupValue.Text("https://imported"))),
            null
        )
        // Then
        assertEquals("easy_diffusion|42", strings["key_provider_selection_state"])
    }

    @Test
    fun `given future provider and removed field, compatibility filtering runs, expected only known fields retained`() {
        // Given
        val incoming = BackupPreferences(
            mapOf(
                "future_provider" to mapOf("secret" to BackupValue.Text("ignored")),
                "custom" to mapOf(
                    "serverUrl" to BackupValue.Text("keep"),
                    "removed" to BackupValue.Text("ignored")
                )
            ), mapOf("future_setting" to BackupValue.Flag(true))
        )
        // When
        val supported = gateway.filterSupported(incoming)
        // Then
        assertEquals(
            mapOf("custom" to mapOf("serverUrl" to BackupValue.Text("keep"))),
            supported.providers
        )
        assertTrue(supported.settings.isEmpty())
    }

    @Test
    fun `given known provider field, malformed scalar type is imported, expected validation failure`() {
        // Given
        val incoming = BackupPreferences(
            mapOf("custom" to mapOf("serverUrl" to BackupValue.Integer(1))),
            emptyMap()
        )
        // When
        val failure =
            assertThrows(IllegalArgumentException::class.java) { gateway.filterSupported(incoming) }
        // Then
        assertEquals("Invalid portable preference type", failure.message)
    }

    @Test
    fun `given removed MNN backend and future timeout option, compatibility filtering runs, expected unsupported values skipped`() {
        // Given
        val incoming = BackupPreferences(
            mapOf("local_mnn" to mapOf("backend" to BackupValue.Text("removed_backend"))),
            mapOf("generationTimeout" to BackupValue.Integer(999))
        )
        // When
        val supported = gateway.filterSupported(incoming)
        // Then
        assertTrue(supported.providers.getValue("local_mnn").isEmpty())
        assertTrue(supported.settings.isEmpty())
    }

    @Test
    fun `given quarantined authorization association only, checked replay commits, expected provider invalidation without ordinary field changes`() {
        // Given
        strings["key_authorization_commit_id"] = "old"
        every { store.retryPendingWrite() } answers {
            strings["key_authorization_commit_id"] = "new"; true
        }
        // When
        val recovered = gateway.recoverPendingWrite()
        // Then
        assertEquals(setOf(BackupCategory.PROVIDERS), recovered)
    }

    @Test
    fun `given quarantined language setting, checked replay commits, expected settings invalidation for locale refresh`() {
        // Given
        every { preferences.languageCode } answers { strings["key_language"].orEmpty() }
        every { store.retryPendingWrite() } answers { strings["key_language"] = "de"; true }
        // When
        val recovered = gateway.recoverPendingWrite()
        // Then
        assertEquals(setOf(BackupCategory.SETTINGS), recovered)
    }

    @Test
    fun `given normal Horde configuration commit, full default provider snapshot persists, expected only meaningful provider group in backup`() {
        // Given
        val configuration = KeyValueConfigurationStore(store)
        val candidate = configuration.getConfiguration(AuthorizationCredentials.None)
            .copy(source = ServerSource.HORDE, hordeApiKey = "horde-key")
        configuration.commitConfiguration(candidate, "association")
        val actualPreferences = PreferenceManagerImpl(store)
        val actualGateway = BackupPreferencesGatewayImpl(store, actualPreferences)
        // When
        val snapshot = actualGateway.read()
        // Then
        assertTrue(strings.containsKey("key_swarm_server_url"))
        assertTrue(flags.containsKey("key_demo_mode"))
        assertEquals(setOf("horde"), snapshot.providers.keys)
        assertFalse(snapshot.providers.containsKey("custom"))
        assertFalse(snapshot.providers.containsKey("swarm_ui"))
    }

    @Test
    fun `given normal configured A1111 commit with explicit false default, backup reads existing provider, expected saved default retained for conflict checks`() {
        // Given
        val configuration = KeyValueConfigurationStore(store)
        val candidate = configuration.getConfiguration(AuthorizationCredentials.None)
            .copy(serverUrl = "https://configured", demoMode = false)
        configuration.commitConfiguration(candidate, "association")
        val actualGateway = BackupPreferencesGatewayImpl(store, PreferenceManagerImpl(store))
        // When
        val snapshot = actualGateway.read()
        // Then
        assertEquals(
            BackupValue.Flag(false),
            snapshot.providers.getValue("custom").getValue("demoMode")
        )
        assertFalse(snapshot.providers.containsKey("swarm_ui"))
    }

    @Test
    fun `given legacy partial provider with missing mandatory fields, backup reads nondefault model choice, expected partial configuration retained without setup validation`() {
        // Given
        strings["key_open_ai_compatible_model"] = "user-model"
        // When
        val snapshot = gateway.read()
        // Then
        assertEquals(
            BackupValue.Text("user-model"),
            snapshot.providers.getValue("open_ai_compatible").getValue("model")
        )
    }

}
