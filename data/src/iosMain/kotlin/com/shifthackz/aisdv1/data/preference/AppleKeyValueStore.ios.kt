package com.shifthackz.aisdv1.data.preference

import com.shifthackz.aisdv1.domain.entity.AuthorizationStorageFailure
import platform.Foundation.NSLock
import platform.Foundation.NSUserDefaults

/**
 * Shared preference-suite owner that hides unconfirmed configuration memory from all readers.
 * Unrelated settings remain writable; checked persistence never holds the memory lock.
 *
 * @param name Existing application preference suite.
 * @author Dmitriy Moroz
 */
private class NsUserDefaultsKeyValueStore(
    private val name: String,
) : KeyValueStore {
    private val memoryLock = NSLock()
    private val defaults = NSUserDefaults(suiteName = name) ?: NSUserDefaults.standardUserDefaults

    /**
     * Serializes only short memory operations, never disk commit or synchronization.
     *
     * @param block Non-suspending preference memory access.
     * @return Result of the serialized operation.
     * @author Dmitriy Moroz
     */
    private fun <T> withMemoryLock(block: () -> T): T {
        memoryLock.lock()
        return try {
            block()
        } finally {
            memoryLock.unlock()
        }
    }

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
        else defaults.stringForKey(key) ?: default
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
        defaults.setObject(value, forKey = key)
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
                strings.keys.associateWith { key -> defaults.stringForKey(key) },
                booleans.keys.associateWith { key ->
                    if (defaults.objectForKey(key) == null) null else defaults.boolForKey(
                        key
                    )
                },
                strings.toMap(),
                booleans.toMap(),
            )
            isBatchInFlight = true
            checkNotNull(configurationMask)
        }
        var hasPersisted = false
        try {
            withMemoryLock {
                val domain = defaults.persistentDomainForName(name)
                    .orEmpty() + batch.candidateStrings + batch.candidateBooleans
                defaults.setPersistentDomain(domain, name)
            }
            check(defaults.synchronize()) { "Configuration persistence failed" }
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
        else if (defaults.objectForKey(key) == null) default else defaults.boolForKey(key)
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
        defaults.setBool(value, forKey = key)
    }

    /**
     * Reads an ordinary integer; configuration batches contain no integer entries.
     *
     * @param key Existing integer preference key.
     * @param default Caller fallback for an absent value.
     * @author Dmitriy Moroz
     */
    override fun getInt(key: String, default: Int): Int = withMemoryLock {
        if (defaults.objectForKey(key) == null) default else defaults.integerForKey(key).toInt()
    }

    /**
     * Writes an ordinary integer, rejecting accidental overlap with pending configuration keys.
     *
     * @param key Existing integer preference key.
     * @param value New ordinary integer value.
     * @author Dmitriy Moroz
     */
    override fun putInt(key: String, value: Int) = withMemoryLock {
        requireWritable(key)
        defaults.setInteger(value.toLong(), forKey = key)
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
internal actual fun createKeyValueStore(name: String): KeyValueStore =
    NsUserDefaultsKeyValueStore(name)
