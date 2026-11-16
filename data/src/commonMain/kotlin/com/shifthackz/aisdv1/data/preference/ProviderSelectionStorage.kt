package com.shifthackz.aisdv1.data.preference

import com.shifthackz.aisdv1.domain.entity.ServerSource

/**
 * Reads an atomic source/revision envelope, falling back to the pre-revision source key.
 * The monotonically increasing revision survives process restart and away/back source changes.
 * @author Dmitriy Moroz
 */
internal fun KeyValueStore.readProviderSelection(): Pair<ServerSource, Long> {
    val fields = getString(PROVIDER_SELECTION_KEY).split('|')
    if (fields.size == 2) {
        val source = ServerSource.entries.firstOrNull { it.key == fields[0] }
        val revision = fields[1].toLongOrNull()?.takeIf { it >= 0 }
        check(source != null && revision != null) { "Invalid persisted provider selection" }
        return source to revision
    }
    return ServerSource.parse(getString("key_server_source", ServerSource.AUTOMATIC1111.key)) to 0L
}

/**
 * Replaces source and revision as one durable value; selecting the same source retains its revision.
 * Callers serialize replacements and final gallery writes with ProviderSelectionGuard.
 * @param source Newly selected provider.
 * @author Dmitriy Moroz
 */
internal fun KeyValueStore.writeProviderSelection(source: ServerSource) {
    val previous = readProviderSelection()
    if (previous.first == source) return
    check(previous.second < Long.MAX_VALUE) { "Provider selection revision exhausted" }
    putStringDurably(PROVIDER_SELECTION_KEY, "${source.key}|${previous.second + 1}")
}

/**
 * Persists an Easy Diffusion connection field and invalidates captured work atomically when selected.
 * Callers serialize connection/model replacement with ProviderSelectionGuard. Unchanged values and
 * settings for an inactive provider retain the current selection revision.
 * @param key Easy Diffusion endpoint or checkpoint preference key.
 * @param value Replacement endpoint or complete checkpoint identifier.
 * @author Dmitriy Moroz
 */
internal fun KeyValueStore.writeEasyDiffusionSetting(key: String, value: String) {
    val previous = readProviderSelection()
    if (previous.first != ServerSource.EASY_DIFFUSION || getString(key) == value) {
        putString(key, value)
        return
    }
    check(previous.second < Long.MAX_VALUE) { "Provider selection revision exhausted" }
    putConfigurationDurably(
        strings = mapOf(
            key to value,
            PROVIDER_SELECTION_KEY to "${previous.first.key}|${previous.second + 1}",
        ),
        booleans = emptyMap(),
    )
}

private const val PROVIDER_SELECTION_KEY = "key_provider_selection_state"
