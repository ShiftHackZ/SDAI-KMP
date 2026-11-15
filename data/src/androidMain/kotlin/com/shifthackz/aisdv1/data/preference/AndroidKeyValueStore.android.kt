package com.shifthackz.aisdv1.data.preference

import android.content.Context
import android.content.SharedPreferences
import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import org.koin.mp.KoinPlatform

/**
 * Shared preference-suite owner that hides unconfirmed configuration memory from all readers.
 * Unrelated settings remain writable; checked persistence never holds the memory lock.
 *
 * @param preferences Existing platform preference cache.
 * @author Dmitriy Moroz
 */
private class AndroidPreferencesKeyValueStore(
    private val preferences: SharedPreferences,
) : KeyValueStore {
    private val memoryLock = Any()

    /**
     * Serializes only short memory operations, never disk commit or synchronization.
     *
     * @param block Non-suspending preference memory access.
     * @return Result of the serialized operation.
     * @author Dmitriy Moroz
     */
    private fun <T> withMemoryLock(block: () -> T): T = synchronized(memoryLock, block)

    private var configurationMask: ConfigurationMemoryMask? = null
    private var isBatchInFlight = false

    /**
     * Returns the last trusted string while its configuration write is pending.
     *
     * @param key Existing preference key.
     * @param default Caller fallback, including when the trusted entry was absent.
     * @author Dmitriy Moroz
     */
    override fun getString(key: String, default: String): String = withMemoryLock {
        val mask = configurationMask
        if (mask != null && mask.strings.containsKey(key)) mask.strings[key] ?: default
        else preferences.getString(key, default) ?: default
    }

    /**
     * Updates an unrelated setting; a pending configuration owns its masked keys exclusively.
     *
     * @param key Existing preference key; conflicting configuration writes fail explicitly.
     * @param value New ordinary setting value.
     * @author Dmitriy Moroz
     */
    override fun putString(key: String, value: String) = withMemoryLock {
        requireWritable(key)
        preferences.edit().putString(key, value).apply()
    }

    /**
     * Uses the same trusted-memory and exact-retry rules for a standalone durable identity write.
     *
     * @param key Existing source/revision key; a conflicting pending batch fails explicitly.
     * @param value Complete encoded identity value, masked until checked persistence succeeds.
     * @author Dmitriy Moroz
     */
    override fun putStringDurably(key: String, value: String) =
        putConfigurationDurably(mapOf(key to value), emptyMap())

    /**
     * Masks complete configuration keys before platform memory can change, then checks disk.
     * Failure retains the original trusted view; only an identical complete retry is admitted.
     * Successful persistence exposes the complete candidate atomically to subsequent readers.
     *
     * @param strings Complete candidate string entries, including credential association.
     * @param booleans Complete candidate boolean entries.
     * @author Dmitriy Moroz
     */
    override fun putConfigurationDurably(
        strings: Map<String, String>,
        booleans: Map<String, Boolean>
    ) {
        val batch = withMemoryLock {
            if (isBatchInFlight) persistenceFailure()
            val current = configurationMask
            if (current != null && !current.matches(strings, booleans)) persistenceFailure()
            if (current == null) configurationMask = ConfigurationMemoryMask(
                strings.keys.associateWith { key -> preferences.getString(key, null) },
                booleans.keys.associateWith { key ->
                    if (preferences.contains(key)) preferences.getBoolean(
                        key,
                        false
                    ) else null
                },
                strings.toMap(),
                booleans.toMap(),
            )
            isBatchInFlight = true
            checkNotNull(configurationMask)
        }
        var hasPersisted = false
        try {
            check(preferences.edit().apply {
                batch.candidateStrings.forEach { (key, value) -> putString(key, value) }
                batch.candidateBooleans.forEach { (key, value) -> putBoolean(key, value) }
            }.commit()) { "Configuration persistence failed" }
            hasPersisted = true
        } finally {
            withMemoryLock {
                if (hasPersisted) configurationMask = null
                isBatchInFlight = false
            }
        }
    }

    /**
     * Returns the trusted boolean or the caller default for an absent trusted entry.
     *
     * @param key Existing boolean preference key.
     * @param default Caller fallback preserved during pending configuration persistence.
     * @author Dmitriy Moroz
     */
    override fun getBoolean(key: String, default: Boolean): Boolean = withMemoryLock {
        val mask = configurationMask
        if (mask != null && mask.booleans.containsKey(key)) mask.booleans[key] ?: default
        else preferences.getBoolean(key, default)
    }

    /**
     * Writes unrelated booleans without exposing or overwriting masked configuration entries.
     *
     * @param key Existing preference key; pending configuration conflicts fail explicitly.
     * @param value New ordinary boolean value.
     * @author Dmitriy Moroz
     */
    override fun putBoolean(key: String, value: Boolean) = withMemoryLock {
        requireWritable(key)
        preferences.edit().putBoolean(key, value).apply()
    }

    /**
     * Reads an ordinary integer; configuration batches contain no integer entries.
     *
     * @param key Existing integer preference key.
     * @param default Caller fallback for an absent value.
     * @author Dmitriy Moroz
     */
    override fun getInt(key: String, default: Int): Int =
        withMemoryLock { preferences.getInt(key, default) }

    /**
     * Writes an ordinary integer, rejecting accidental overlap with pending configuration keys.
     *
     * @param key Existing integer preference key.
     * @param value New ordinary integer value.
     * @author Dmitriy Moroz
     */
    override fun putInt(key: String, value: Int) = withMemoryLock {
        requireWritable(key)
        preferences.edit().putInt(key, value).apply()
    }

    /**
     * Refuses conflicting standalone writes rather than replacing pending transaction ownership.
     *
     * @param key Preference key requested by an ordinary setter.
     * @author Dmitriy Moroz
     */
    private fun requireWritable(key: String) {
        if (configurationMask?.contains(key) == true) persistenceFailure()
    }

    /**
     * Reports a safe retryable persistence conflict without exposing candidate or trusted values.
     *
     * @author Dmitriy Moroz
     */
    private fun persistenceFailure(): Nothing =
        throw AuthorizationStorageFailure(AuthorizationStorageFailure.Reason.PERSISTENCE)
}

/**
 * Opens one platform preference-suite owner; DI shares it across settings and configuration.
 *
 * @param name Existing suite filename, preserving installed configuration compatibility.
 * @return Owner whose trusted read view masks unconfirmed complete configuration writes.
 * @author Dmitriy Moroz
 */
internal actual fun createKeyValueStore(name: String): KeyValueStore {
    val context = KoinPlatform.getKoin().get<Context>()
    return AndroidPreferencesKeyValueStore(
        preferences = context.getSharedPreferences(name, Context.MODE_PRIVATE),
    )
}
