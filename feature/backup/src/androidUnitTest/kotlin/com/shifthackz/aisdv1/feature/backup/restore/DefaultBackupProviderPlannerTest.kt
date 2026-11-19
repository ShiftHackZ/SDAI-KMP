package com.shifthackz.aisdv1.feature.backup.restore

import com.shifthackz.aisdv1.domain.entity.backup.BackupCategory
import com.shifthackz.aisdv1.domain.entity.backup.BackupPreferences
import com.shifthackz.aisdv1.domain.entity.backup.BackupSelection
import com.shifthackz.aisdv1.domain.entity.backup.BackupValue
import com.shifthackz.aisdv1.domain.feature.auth.AuthorizationCredentials
import com.shifthackz.aisdv1.feature.backup.codec.DefaultBackupPreferencesCodec
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class DefaultBackupProviderPlannerTest {
    private val planner = DefaultBackupProviderPlanner(DefaultBackupPreferencesCodec(Json))
    private val selection = BackupSelection(setOf(BackupCategory.PROVIDERS), setOf("custom"))
    private fun preferences(vararg values: Pair<String, Map<String, BackupValue>>) =
        BackupPreferences(mapOf(*values), emptyMap())

    private fun text(value: String) = BackupValue.Text(value)

    @Test
    fun `given missing provider configuration, backup is selected, expected fill without conflict`() {
        // Given
        val incoming = preferences("custom" to mapOf("serverUrl" to text("https://server")))
        // When
        val plan = planner.plan(preferences(), incoming, selection, AuthorizationCredentials.None)
        // Then
        assertEquals(incoming.providers, plan.providers)
        assertEquals(emptySet<String>(), plan.conflicts)
    }

    @Test
    fun `given existing saved default, different backup field is selected, expected provider conflict`() {
        // Given
        val current = preferences("custom" to mapOf("demoMode" to BackupValue.Flag(false)))
        val incoming = preferences("custom" to mapOf("demoMode" to BackupValue.Flag(true)))
        // When
        val plan = planner.plan(current, incoming, selection, AuthorizationCredentials.None)
        // Then
        assertEquals(setOf("custom"), plan.conflicts)
    }

    @Test
    fun `given existing provider, backup omits a field, expected omitted field is not replaced`() {
        // Given
        val current = preferences(
            "custom" to mapOf(
                "serverUrl" to text("https://server"),
                "model" to text("keep")
            )
        )
        val incoming = preferences("custom" to mapOf("serverUrl" to text("https://server")))
        // When
        val plan = planner.plan(current, incoming, selection, AuthorizationCredentials.None)
        // Then
        assertEquals(emptySet<String>(), plan.conflicts)
        assertEquals(setOf("serverUrl"), plan.providers.getValue("custom").keys)
    }

    @Test
    fun `given configured unselected provider, backup contains its fields, expected fields remain unselected`() {
        // Given
        val incoming = preferences("swarm_ui" to mapOf("serverUrl" to text("https://other")))
        // When
        val plan = planner.plan(preferences(), incoming, selection, AuthorizationCredentials.None)
        // Then
        assertEquals(emptyMap<String, Map<String, BackupValue>>(), plan.providers)
    }

    @Test
    fun `given shared Basic Auth and unselected SwarmUI, A1111 credentials restore, expected shared credentials skipped`() {
        // Given
        val current = preferences(
            "custom" to mapOf("serverUrl" to text("a")),
            "swarm_ui" to mapOf("serverUrl" to text("b"))
        )
        val incoming = preferences(
            "custom" to mapOf(
                "basicLogin" to text("new"),
                "basicPassword" to text("new password")
            )
        )
        // When
        val plan = planner.plan(
            current,
            incoming,
            selection,
            AuthorizationCredentials.HttpBasic("old", "old password")
        )
        // Then
        assertNull(plan.credentials)
        assertEquals(2, plan.skippedCount)
        assertEquals(setOf("swarm_ui"), plan.retainedCredentialProviders)
        assertEquals(emptySet<String>(), plan.conflicts)
    }

    @Test
    fun `given shared Basic Auth and both providers selected, credentials differ, expected both named conflicts`() {
        // Given
        val current = preferences(
            "custom" to mapOf("serverUrl" to text("a")),
            "swarm_ui" to mapOf("serverUrl" to text("b"))
        )
        val incoming = preferences(
            "custom" to mapOf(
                "basicLogin" to text("new"),
                "basicPassword" to text("new password")
            )
        )
        val all = selection.copy(providers = setOf("custom", "swarm_ui"))
        // When
        val plan = planner.plan(
            current,
            incoming,
            all,
            AuthorizationCredentials.HttpBasic("old", "old password")
        )
        // Then
        assertEquals(setOf("custom", "swarm_ui"), plan.conflicts)
        assertEquals(AuthorizationCredentials.HttpBasic("new", "new password"), plan.credentials)
    }

    @Test
    fun `given Basic Auth with omitted password, login changes, expected destination password retained`() {
        // Given
        val incoming = preferences("custom" to mapOf("basicLogin" to text("new")))
        // When
        val plan = planner.plan(
            preferences(),
            incoming,
            selection,
            AuthorizationCredentials.HttpBasic("old", "keep")
        )
        // Then
        assertEquals(AuthorizationCredentials.HttpBasic("new", "keep"), plan.credentials)
    }

    @Test
    fun `given provider conflict consent, destination changes afterward, expected fingerprint changes`() {
        // Given
        val incoming = preferences("custom" to mapOf("serverUrl" to text("import")))
        val before = planner.plan(
            preferences("custom" to mapOf("serverUrl" to text("before"))),
            incoming,
            selection,
            AuthorizationCredentials.None
        )
        // When
        val after = planner.plan(
            preferences("custom" to mapOf("serverUrl" to text("after"))),
            incoming,
            selection,
            AuthorizationCredentials.None
        )
        // Then
        assertNotEquals(before.fingerprint, after.fingerprint)
    }

    @Test
    fun `given two contradictory shared credential records, both selected, expected rejection`() {
        // Given
        val incoming = preferences(
            "custom" to mapOf("basicLogin" to text("one")),
            "swarm_ui" to mapOf("basicLogin" to text("two"))
        )
        // When
        val failure = assertThrows(IllegalArgumentException::class.java) {
            planner.plan(
                preferences(),
                incoming,
                selection.copy(providers = setOf("custom", "swarm_ui")),
                AuthorizationCredentials.None
            )
        }
        // Then
        assertEquals("Conflicting shared Basic Auth records", failure.message)
    }
}
