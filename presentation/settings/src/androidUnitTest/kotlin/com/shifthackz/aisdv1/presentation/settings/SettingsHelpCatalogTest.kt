package com.shifthackz.aisdv1.presentation.settings

import com.shifthackz.aisdv1.core.localization.Localization
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Verifies every discoverable settings explanation exists in each bundled locale without fallback.
 *
 * @author Dmitriy Moroz
 */
class SettingsHelpCatalogTest {

    @Test
    fun `given en settings help, all explained rows are resolved, expected complete nonblank titles and messages`() {
        // Given
        val catalog = Localization.strings("en")
        val rows = SettingsListItem.entries.filter { it.helpTitleKey() != null }
        // When
        val missing =
            rows.flatMap { listOf(it.helpTitleKey()!!, "settings_help_" + it.name.lowercase()) }
                .distinct().filter { catalog[it].isNullOrBlank() }
        // Then
        assertTrue("Missing en help: $missing", missing.isEmpty())
    }

    @Test
    fun `given de settings help, all explained rows are resolved, expected complete nonblank titles and messages`() {
        // Given
        val catalog = Localization.strings("de")
        val rows = SettingsListItem.entries.filter { it.helpTitleKey() != null }
        // When
        val missing =
            rows.flatMap { listOf(it.helpTitleKey()!!, "settings_help_" + it.name.lowercase()) }
                .distinct().filter { catalog[it].isNullOrBlank() }
        // Then
        assertTrue("Missing de help: $missing", missing.isEmpty())
    }

    @Test
    fun `given ru settings help, all explained rows are resolved, expected complete nonblank titles and messages`() {
        // Given
        val catalog = Localization.strings("ru")
        val rows = SettingsListItem.entries.filter { it.helpTitleKey() != null }
        // When
        val missing =
            rows.flatMap { listOf(it.helpTitleKey()!!, "settings_help_" + it.name.lowercase()) }
                .distinct().filter { catalog[it].isNullOrBlank() }
        // Then
        assertTrue("Missing ru help: $missing", missing.isEmpty())
    }

    @Test
    fun `given uk settings help, all explained rows are resolved, expected complete nonblank titles and messages`() {
        // Given
        val catalog = Localization.strings("uk")
        val rows = SettingsListItem.entries.filter { it.helpTitleKey() != null }
        // When
        val missing =
            rows.flatMap { listOf(it.helpTitleKey()!!, "settings_help_" + it.name.lowercase()) }
                .distinct().filter { catalog[it].isNullOrBlank() }
        // Then
        assertTrue("Missing uk help: $missing", missing.isEmpty())
    }

    @Test
    fun `given tr settings help, all explained rows are resolved, expected complete nonblank titles and messages`() {
        // Given
        val catalog = Localization.strings("tr")
        val rows = SettingsListItem.entries.filter { it.helpTitleKey() != null }
        // When
        val missing =
            rows.flatMap { listOf(it.helpTitleKey()!!, "settings_help_" + it.name.lowercase()) }
                .distinct().filter { catalog[it].isNullOrBlank() }
        // Then
        assertTrue("Missing tr help: $missing", missing.isEmpty())
    }

    @Test
    fun `given zh settings help, all explained rows are resolved, expected complete nonblank titles and messages`() {
        // Given
        val catalog = Localization.strings("zh")
        val rows = SettingsListItem.entries.filter { it.helpTitleKey() != null }
        // When
        val missing =
            rows.flatMap { listOf(it.helpTitleKey()!!, "settings_help_" + it.name.lowercase()) }
                .distinct().filter { catalog[it].isNullOrBlank() }
        // Then
        assertTrue("Missing zh help: $missing", missing.isEmpty())
    }

    @Test
    fun `given inline warnings and version footer, help is requested, expected no redundant help wrapper`() {
        // Given
        val rows = listOf(
            SettingsListItem.NNAPI_WARNING,
            SettingsListItem.BACKGROUND_WARNING,
            SettingsListItem.DIM_SCREEN_WARNING,
            SettingsListItem.AUTO_SAVE_WARNING,
            SettingsListItem.VERSION
        )
        // When
        val keys = rows.map { it.helpTitleKey() }
        // Then
        keys.forEach { assertNull(it) }
    }
}
